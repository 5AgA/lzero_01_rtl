import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class QuantizationUnit_16Test extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "QuantizationUnit_16"

  it should "program 4K LUT and map 32-bit inputs to 8-bit outputs" in {
    test(new QuantizationUnit_16()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      
      // ----------------------------------------------------
      // [1] S/W에서 LUT 정답지 배열 만들기 (가상 양자화 함수)
      //     규칙: 값 / 16 (스케일링), 최대 255 (ReLU6 같은 클리핑)
      // ----------------------------------------------------
      val lut_software = Array.fill(4096)(0)
      for (i <- 0 until 4096) {
        val scaled = i / 16
        lut_software(i) = if (scaled > 255) 255 else scaled
      }

      // ----------------------------------------------------
      // [2] 하드웨어(SRAM)에 정답지 굽기 (Programming)
      // ----------------------------------------------------
      println(">> 4K LUT(정답지) 하드웨어 메모리에 굽기 시작...")
      dut.io.lut_we.poke(true.B)
      for (addr <- 0 until 4096) {
        dut.io.lut_waddr.poke(addr.U)
        dut.io.lut_wdata.poke(lut_software(addr).U)
        dut.clock.step(1) // 매 클록마다 1칸씩 씀
      }
      dut.io.lut_we.poke(false.B)
      println("✔️ 4K LUT 세팅 완료! (4096 클록 소요)")

      // ----------------------------------------------------
      // [3] 연산 테스트: 32비트 데이터 주입 및 확인
      // ----------------------------------------------------
      println("\n>> 32-bit 데이터 양자화 테스트 시작...")
      
      // 가짜 테스트 데이터 준비 (16차선)
      // 일부러 4096보다 큰 32비트 쓰레기값을 섞어 넣음 (하위 12비트만 잘리는지 확인하기 위해)
      val test_inputs_32b = Array(
        0, 16, 32, 48, 100, 200, 1000, 2000, 4000, 4095, 
        4096,      // 4096은 이진수로 1_000000000000. 하위 12비트는 0이 됨
        4096 + 32, // 하위 12비트는 32가 됨
        1000000, 2000000, 3000000, 4000000 // 거대한 값들
      )

      // 16차선에 데이터 찌르기
      for (i <- 0 until 16) {
        dut.io.in_vec(i).poke(test_inputs_32b(i).U)
      }

      // ★ 중요: SyncReadMem은 읽는 데 1클록이 걸립니다!
      dut.clock.step(1)

      // 1클록 뒤에 결과 채점
      val actual_outputs_8b = (0 until 16).map(i => dut.io.out_vec(i).peekInt().toInt).toArray

      println("\n========== 양자화 결과 ==========")
      for (i <- 0 until 16) {
        val in_val = test_inputs_32b(i)
        // S/W가 예상하는 값: 입력값의 하위 12비트를 추출한 뒤, S/W 배열에서 정답 찾기
        val masked_12b = in_val & 0xFFF // (0xFFF = 4095, 즉 12비트 마스킹)
        val expected_8b = lut_software(masked_12b)
        
        dut.io.out_vec(i).expect(expected_8b.U)
        
        println(f"차선 $i%2d | 입력(32b): $in_val%9d -> 주소(12b): $masked_12b%4d -> 출력(8b): ${actual_outputs_8b(i)}%3d (정답: $expected_8b%3d)")
      }

      println("\n✔️ [최종 성공] 16차선 Quantization LUT가 완벽히 동작합니다!")
    }
  }
}