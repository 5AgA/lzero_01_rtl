import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class TpuTop_16Test extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "TpuTop_16"

  it should "execute full pipeline: MAC -> Deskew -> Accumulate perfectly" in {
    test(new TpuTop_16()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      
      // [1] S/W 정답 계산
      val matrix_A = Array.tabulate(16, 16) { (_, _) => Random.nextInt(5) }
      val matrix_W = Array.tabulate(16, 16) { (_, _) => Random.nextInt(5) }
      
      val expected_matrix = Array.fill(16, 16)(0)
      for (r <- 0 until 16) {
        for (c <- 0 until 16) {
          for (k <- 0 until 16) {
            expected_matrix(r)(c) += matrix_A(r)(k) * matrix_W(k)(c)
          }
        }
      }

      dut.io.load_A.poke(false.B)
      dut.io.feed_A.poke(false.B)
      dut.io.load_W.poke(false.B)

      // [2] 폭포수 Weight 로드 (16 Cycles)
      for (r <- 15 to 0 by -1) {
        for (c <- 0 until 16) {
          dut.io.in_W(c).poke(matrix_W(r)(c).U)
        }
        dut.io.load_W.poke(true.B)
        dut.clock.step(1) 
      }
      dut.io.load_W.poke(false.B) 

      // [3] Data Orchestrator에 Matrix A 로드 (Transpose)
      for (r <- 0 until 16) {
        for (c <- 0 until 16) {
          dut.io.in_mat_A(r)(c).poke(matrix_A(c)(r).U)
        }
      }
      dut.io.load_A.poke(true.B)
      dut.clock.step(1)
      dut.io.load_A.poke(false.B)

      // [4] 어레이 연산 발사 (Feed)
      dut.io.feed_A.poke(true.B)
      
      // 어레이 16 + Deskew 15 = 31 사이클 동안 아무 일도 안 일어남
      println("\n[진행] 하드웨어가 연산 및 Deskew 정렬을 수행 중입니다... (Silent)")
      
      for (t <- 0 until 60) {
        if (t == 16) dut.io.feed_A.poke(false.B) // 입력 16줄 끝
        
        // 32클록부터 data_valid가 켜지며 Accumulator에 자동 저장됨을 감지
        if (dut.io.out_valid.peek().litToBoolean) {
          println(f"Cycle $t%2d : 1줄이 완벽하게 조립되어 Accumulator에 저장되었습니다!")
        }
        dut.clock.step(1)
      }

      // [5] 연산 종료 후 Accumulator 내부 검사 (최종 채점)
      println("\n========== Accumulator(256 버퍼) 최종 결과 검증 ==========")
      for (r <- 0 until 16) {
        dut.io.acc_read_addr.poke(r.U)
        dut.clock.step(1) // 주소 넣고 1클록 뒤에 값 읽기
        
        val actual_row = (0 until 16).map(c => dut.io.out_acc(c).peekInt().toInt).toArray
        
        // 정답 비교
        for (c <- 0 until 16) {
          dut.io.out_acc(c).expect(expected_matrix(r)(c).U)
        }
        println(f"Row $r%2d [Pass] : " + actual_row.mkString("\t"))
      }
      
      println("\n✔️ [최종 성공] Deskew Buffer와 Accumulator 파이프라인이 완벽히 동작합니다!")
    }
  }
}