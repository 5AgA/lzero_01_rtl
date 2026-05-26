import circt.stage.ChiselStage
import chisel3._
import chisel3.util._

// ==========================================
// [1] MacUnit (PE): 가중치를 아래로 넘겨주는 기능 추가
// ==========================================
class MacUnit extends Module {
  val io = IO(new Bundle {
    val in_a      = Input(UInt(8.W)) 
    val in_c      = Input(UInt(16.W)) 
    
    val load_w    = Input(Bool())     // 가중치 로드 스위치
    val in_w      = Input(UInt(8.W))  // 위에서 내려오는 가중치
    val out_w     = Output(UInt(8.W)) // 아랫집으로 넘겨줄 가중치 (폭포수)

    val out_in    = Output(UInt(8.W))
    val out_mac   = Output(UInt(16.W))
  })

  val weight = RegInit(0.U(8.W))

  when (io.load_w) {
    weight := io.in_w
  }
  
  // 조건에 상관없이 내 가중치를 항상 아래로 흘려보냄
  io.out_w := weight 

  io.out_mac  := RegNext((io.in_a * weight) + io.in_c)
  io.out_in   := RegNext(io.in_a)
}

// ==========================================
// [2] 시스톨릭 어레이: 16가닥의 핀으로 256개 가중치 세팅
// ==========================================
class MatMulUnit_16 extends Module {
  val io = IO(new Bundle{
    val in_A    = Input(Vec(16, UInt(8.W)))
    
    val load_W  = Input(Bool())
    val in_W    = Input(Vec(16, UInt(8.W))) // 256개가 아닌 16개만 받음 (버스 폭 대폭 감소)

    val out_MAC = Output(Vec(16, UInt(16.W)))
  })

  val macs = Seq.fill(16, 16)(Module(new MacUnit()))

  for ( r <- 0 until 16 ){
    for ( c <- 0 until 16 ){
      // 가중치(Weight) 세로 연결 (위 -> 아래)
      if (r == 0) { macs(r)(c).io.in_w := io.in_W(c) } 
      else        { macs(r)(c).io.in_w := macs(r-1)(c).io.out_w }
      
      macs(r)(c).io.load_w := io.load_W

      // 데이터(A) 가로 연결 (좌 -> 우)
      if (c == 0) { macs(r)(c).io.in_a := io.in_A(r) }
      else        { macs(r)(c).io.in_a := macs(r)(c-1).io.out_in }

      // 부분합(C) 세로 연결 (위 -> 아래)
      if (r == 0) { macs(r)(c).io.in_c := 0.U }
      else        { macs(r)(c).io.in_c := macs(r-1)(c).io.out_mac }
    }
  }

  for (c <- 0 until 16) {
    io.out_MAC(c) := macs(15)(c).io.out_mac
  }
}

// ==========================================
// [3] Data Orchestrator: 타이밍 버그 수정 (메모리 포인터 방식)
// ==========================================
class Orch_buffer_16 extends Module {
  val io = IO(new Bundle{
    val in          = Input(Vec(16, UInt(8.W)))
    val load_enable = Input(Bool())
    val sync_enable = Input(Bool())
    val out         = Output(UInt(8.W))
  })

  val mem = RegInit(VecInit(Seq.fill(16)(0.U(8.W))))
  val r_ptr = RegInit(0.U(4.W))

  when (io.load_enable) {
    mem := io.in
    r_ptr := 0.U
  } .elsewhen (io.sync_enable) {
    r_ptr := r_ptr + 1.U // 클록마다 포인터 이동 (Shift 연산 최소화)
  }

  io.out := Mux(io.sync_enable, mem(r_ptr), 0.U)
}

class DataOrchUnit_16 extends Module {
  val io = IO(new Bundle{
    val in_mat      = Input(Vec(16, Vec(16, UInt(8.W))))
    val feed_enable = Input(Bool())
    val load_enable = Input(Bool())
    val skew_vec    = Output(Vec(16, UInt(8.W)))
  })

  val d_orch = Seq.fill(16)(Module(new Orch_buffer_16()))
  
  // ★ 1클록씩 순차적으로 켜지도록 RegNext 파이프라인 형성 (버그 수정 완료)
  val feed_shifter = RegInit(VecInit(Seq.fill(16)(false.B)))
  feed_shifter(0) := io.feed_enable
  for (i <- 1 until 16) {
    feed_shifter(i) := feed_shifter(i-1)
  }
 
  for( r <- 0 until 16 ){
    d_orch(r).io.sync_enable  := feed_shifter(r)
    d_orch(r).io.load_enable  := io.load_enable
    d_orch(r).io.in           := io.in_mat(r)
    io.skew_vec(r)            := d_orch(r).io.out
  }
}

// ==========================================
// [4] Deskew Buffer: 삐딱한 출력을 네모 반듯하게 펴주기
// ==========================================
class DeskewBuffer_16 extends Module {
  val io = IO(new Bundle {
    val in_skewed   = Input(Vec(16, UInt(16.W))) 
    val out_aligned = Output(Vec(16, UInt(16.W))) 
  })

  for (c <- 0 until 16) {
    val delay_amount = 15 - c // 0번 열은 15칸 대기, 15번 열은 0칸 대기 (역계단)
    
    if (delay_amount == 0) {
      io.out_aligned(c) := io.in_skewed(c)
    } else {
      io.out_aligned(c) := ShiftRegister(io.in_skewed(c), delay_amount)
    }
  }
}

// ==========================================
// [5] Accumulator: 256 크기의 엘리베이터 버퍼
// ==========================================
class Accumulator_256 extends Module {
  val io = IO(new Bundle {
    val in_aligned = Input(Vec(16, UInt(16.W)))
    val in_valid   = Input(Bool())     // 데이터가 완성되었을 때만 켜짐
    val read_addr  = Input(UInt(4.W))  // 나중에 S/W에서 값을 읽어볼 주소
    val out_read   = Output(Vec(16, UInt(32.W))) // 누산 결과 (오버플로우 방지 32비트)
  })

  // 16x16 = 256 크기의 2D 누산기 버퍼 (SRAM 역할)
  val mem = RegInit(VecInit(Seq.fill(16)(VecInit(Seq.fill(16)(0.U(32.W))))))
  
  // 데이터가 들어올 때마다 엘리베이터 층수(주소)를 1칸씩 올림
  val write_ptr = RegInit(0.U(4.W))

  when (io.in_valid) {
    for (c <- 0 until 16) {
      mem(write_ptr)(c) := mem(write_ptr)(c) + io.in_aligned(c)
    }
    write_ptr := write_ptr + 1.U
  }

  // 외부에서 요청한 주소의 1줄(16개)을 통째로 꺼내줌
  for (c <- 0 until 16) {
    io.out_read(c) := mem(io.read_addr)(c)
  }
}

// ==========================================
// [6] 최상단 통합 Wrapper (TPU Top) - Deskew와 Acc 추가됨
// ==========================================
class TpuTop_16 extends Module {
  val io = IO(new Bundle {
    val in_mat_A    = Input(Vec(16, Vec(16, UInt(8.W))))
    val load_A      = Input(Bool())
    val feed_A      = Input(Bool())

    val in_W        = Input(Vec(16, UInt(8.W))) 
    val load_W      = Input(Bool())

    val acc_read_addr = Input(UInt(4.W))
    val out_acc       = Output(Vec(16, UInt(32.W))) // 최종 정돈된 결과물
    val out_valid     = Output(Bool()) // 밖으로 나가는 데이터가 진짜인지 알려주는 핀
  })

  val orchestrator  = Module(new DataOrchUnit_16())
  val systolicArray = Module(new MatMulUnit_16())
  val deskew        = Module(new DeskewBuffer_16())
  val accumulator   = Module(new Accumulator_256())

  // 파이프라인 조립 (A -> Array -> Deskew -> Acc)
  orchestrator.io.in_mat      := io.in_mat_A
  orchestrator.io.load_enable := io.load_A
  orchestrator.io.feed_enable := io.feed_A

  systolicArray.io.in_A       := orchestrator.io.skew_vec
  systolicArray.io.in_W       := io.in_W
  systolicArray.io.load_W     := io.load_W

  deskew.io.in_skewed         := systolicArray.io.out_MAC
  accumulator.io.in_aligned   := deskew.io.out_aligned

  // ★ 하드웨어 자동 타이밍 동기화 마법:
  // feed_A 스위치를 누른 시점으로부터 정확히 32클록 뒤에 네모 반듯한 1줄이 완성됨.
  val data_valid = ShiftRegister(io.feed_A, 32) 
  
  accumulator.io.in_valid := data_valid
  accumulator.io.read_addr := io.acc_read_addr
  
  io.out_acc   := accumulator.io.out_read
  io.out_valid := data_valid
}

object TPU_Main extends App {
  ChiselStage.emitSystemVerilogFile(new TpuTop_16(), Array("--target-dir", "generated"))
}