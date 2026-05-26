import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class NormSoftmaxBuffersTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "NormSoftmaxBuffers"

  it should "calculate max and sum of squares for each row and store them in 4K buffers" in {
    test(new NormSoftmaxBuffers()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      val rows = 5 // 5줄(Row)의 데이터만 테스트
      val test_data = Array.tabulate(rows, 16) { (_, _) => Random.nextInt(20) } // 오버플로우 방지용 작은 숫자

      // ----------------------------------------------------
      // [1] S/W 정답 계산 (Scala의 강력한 컬렉션 함수 사용)
      // ----------------------------------------------------
      val expected_max = Array.fill(rows)(0)
      val expected_sum_sq = Array.fill(rows)(0)

      for (r <- 0 until rows) {
        expected_max(r)    = test_data(r).max // 스칼라 내장 max 함수
        expected_sum_sq(r) = test_data(r).map(x => x * x).sum // 스칼라 내장 제곱 합 함수
      }

      // ----------------------------------------------------
      // [2] 하드웨어에 16x16 데이터 주입 시작
      // ----------------------------------------------------
      println(">> 16-element Vectors 데이터 주입 및 트리 연산 시작...")
      for (r <- 0 until rows) {
        for (c <- 0 until 16) {
          dut.io.in_vec(c).poke(test_data(r)(c).U)
        }
        dut.io.in_valid.poke(true.B)
        dut.clock.step(1) // 1클록마다 1줄씩 밀어 넣음
      }
      dut.io.in_valid.poke(false.B)

      // 파이프라인 지연(1클록) + SRAM 쓰기 완료 대기를 위해 2클록 더 돌림
      dut.clock.step(2)

      // ----------------------------------------------------
      // [3] 4K SRAM 저장 결과 읽기 및 검증
      // ----------------------------------------------------
      println("\n========== 4K SRAM 통계 버퍼 결과 검증 ==========")
      for (r <- 0 until rows) {
        dut.io.read_addr.poke(r.U)
        dut.clock.step(1) // SyncReadMem은 읽을 때 1클록 소요됨

        val actual_max = dut.io.read_max.peekInt().toInt
        val actual_sum_sq = dut.io.read_sum_sq.peekInt().toInt

        // S/W 정답과 일치하는지 단호하게 채점!
        dut.io.read_max.expect(expected_max(r).U)
        dut.io.read_sum_sq.expect(expected_sum_sq(r).U)

        println(f"Row $r%d | 예상 Max: ${expected_max(r)}%3d -> 하드웨어 Max: $actual_max%3d [Pass]")
        println(f"      | 예상 SumSq: ${expected_sum_sq(r)}%6d -> 하드웨어 SumSq: $actual_sum_sq%6d [Pass]")
      }
      
      println("\n✔️ [최종 성공] Reduction Tree 구조와 4K 통계 버퍼가 완벽히 동작합니다!")
    }
  }
}