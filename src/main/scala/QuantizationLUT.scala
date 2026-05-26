import chisel3._
import chisel3.util._

// ==========================================
// [1] 단일 1차선 Quantization LUT (4K SRAM)
// ==========================================
class QuantizationLane extends Module {
  val io = IO(new Bundle {
    // 1. 동작 데이터 포트
    val in_32b    = Input(UInt(32.W)) // Accumulator에서 온 32비트 값
    val out_8b    = Output(UInt(8.W)) // 양자화된 8비트 값 (1클록 지연)

    // 2. LUT 세팅(Programming) 포트
    val lut_we    = Input(Bool())     // Write Enable 스위치
    val lut_waddr = Input(UInt(12.W)) // 4K 주소 (0 ~ 4095)
    val lut_wdata = Input(UInt(8.W))  // 저장할 8비트 정답
  })

  // 4096칸 x 8비트 크기의 동기식 메모리 (FPGA의 BRAM으로 합성됨)
  val lut = SyncReadMem(4096, UInt(8.W))

  // S/W에서 LUT 정답지를 채워 넣는 로직
  when (io.lut_we) {
    lut.write(io.lut_waddr, io.lut_wdata)
  }

  // 32비트 데이터에서 하위 12비트만 뜯어내서 주소로 사용 (친구분 메모의 12bit 추출 부분)
  // 실제 NPU에서는 m, s 파라미터에 따라 우측 시프트(Shift)를 먼저 한 뒤 12비트를 자릅니다.
  val read_addr = io.in_32b(11, 0)

  // 메모리에서 주소에 해당하는 값을 꺼냄 (SyncReadMem은 물리적으로 1클록이 걸림)
  io.out_8b := lut.read(read_addr)
}

// ==========================================
// [2] 16차선 통합 Quantization Unit
// ==========================================
class QuantizationUnit_16 extends Module {
  val io = IO(new Bundle {
    val in_vec  = Input(Vec(16, UInt(32.W)))
    val out_vec = Output(Vec(16, UInt(8.W)))

    // S/W가 16개의 차선에 동일한 정답지를 동시에 복사(Broadcast)하기 위한 포트
    val lut_we    = Input(Bool())
    val lut_waddr = Input(UInt(12.W))
    val lut_wdata = Input(UInt(8.W))
  })

  // 1차선 모듈을 16개 복사(Instantiate)
  val lanes = Seq.fill(16)(Module(new QuantizationLane()))

  for (i <- 0 until 16) {
    // 데이터 버스 연결
    lanes(i).io.in_32b := io.in_vec(i)
    io.out_vec(i)      := lanes(i).io.out_8b

    // S/W의 LUT 쓰기 신호를 16개 레인에 동일하게 연결 (Broadcast)
    lanes(i).io.lut_we    := io.lut_we
    lanes(i).io.lut_waddr := io.lut_waddr
    lanes(i).io.lut_wdata := io.lut_wdata
  }
}