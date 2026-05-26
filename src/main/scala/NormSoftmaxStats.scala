import chisel3._
import chisel3.util._

// ==========================================
// [1] 행(Row) 통계 계산기 (Combinational Tree)
// ==========================================
class RowStatsCalculator extends Module {
  val io = IO(new Bundle {
    val in_vec     = Input(Vec(16, UInt(8.W))) // 양자화된 8비트 입력 배열
    val in_valid   = Input(Bool())
    
    val out_max    = Output(UInt(8.W))         // 최대값
    val out_sum_sq = Output(UInt(20.W))        // 제곱의 합 (8비트 제곱=16비트, 16개 더하면 20비트)
  })

  // 1. Max Tree: 16개의 숫자 중 가장 큰 값을 토너먼트 방식으로 찾음
  val max_val = io.in_vec.reduceTree((a, b) => Mux(a > b, a, b))

  // 2. Sum of Squares Tree: 각 요소를 제곱(x*x)한 뒤, 토너먼트 방식으로 모두 더함
  val sq_vec = VecInit(io.in_vec.map(x => x * x))
  val sum_sq = sq_vec.reduceTree((a, b) => a + b)

  // 계산된 값을 1클록 뒤에 안전하게 출력 (파이프라인 레지스터)
  io.out_max    := RegEnable(max_val, io.in_valid)
  io.out_sum_sq := RegEnable(sum_sq, io.in_valid)
}

// ==========================================
// [2] 4K 통계 버퍼 메모리 (SRAM) 통합 모듈
// ==========================================
class NormSoftmaxBuffers extends Module {
  val io = IO(new Bundle {
    val in_vec    = Input(Vec(16, UInt(8.W)))
    val in_valid  = Input(Bool())

    // S/W나 다음 유닛에서 통계값을 읽어갈 포트
    val read_addr = Input(UInt(12.W)) // 4K 접근을 위한 12비트 주소
    val read_max  = Output(UInt(8.W))
    val read_sum_sq = Output(UInt(20.W))
  })

  // 연산기 인스턴스화
  val statsCalc = Module(new RowStatsCalculator())
  statsCalc.io.in_vec   := io.in_vec
  statsCalc.io.in_valid := io.in_valid

  // 4096 크기의 BRAM(SRAM) 2개 생성
  val max_mem    = SyncReadMem(4096, UInt(8.W))
  val sum_sq_mem = SyncReadMem(4096, UInt(20.W))

  // 데이터가 들어올 때마다 주소를 1씩 올리는 엘리베이터(포인터)
  val write_ptr = RegInit(0.U(12.W))

  // 통계 계산기에서 1클록 지연이 발생하므로, 주소와 쓰기 신호도 1클록 늦춰줌
  val write_en   = RegNext(io.in_valid, false.B)
  val write_addr = RegNext(write_ptr)

  when (io.in_valid) {
    write_ptr := write_ptr + 1.U // 데이터가 들어오면 포인터 증가
  }

  // 1클록 뒤 계산이 완료되면 4K SRAM에 저장
  when (write_en) {
    max_mem.write(write_addr, statsCalc.io.out_max)
    sum_sq_mem.write(write_addr, statsCalc.io.out_sum_sq)
  }

  // S/W 읽기 요청 처리 (1클록 소요)
  io.read_max    := max_mem.read(io.read_addr)
  io.read_sum_sq := sum_sq_mem.read(io.read_addr)
}