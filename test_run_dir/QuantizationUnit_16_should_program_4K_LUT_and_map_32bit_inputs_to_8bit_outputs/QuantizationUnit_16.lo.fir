FIRRTL version 1.2.0
circuit QuantizationUnit_16 :
  module QuantizationLane : @[src/main/scala/QuantizationLUT.scala 7:7]
    input clock : Clock @[src/main/scala/QuantizationLUT.scala 7:7]
    input reset : UInt<1> @[src/main/scala/QuantizationLUT.scala 7:7]
    input io_in_32b : UInt<32> @[src/main/scala/QuantizationLUT.scala 8:14]
    output io_out_8b : UInt<8> @[src/main/scala/QuantizationLUT.scala 8:14]
    input io_lut_we : UInt<1> @[src/main/scala/QuantizationLUT.scala 8:14]
    input io_lut_waddr : UInt<12> @[src/main/scala/QuantizationLUT.scala 8:14]
    input io_lut_wdata : UInt<8> @[src/main/scala/QuantizationLUT.scala 8:14]

    mem lut : @[src/main/scala/QuantizationLUT.scala 20:24]
      data-type => UInt<8>
      depth => 4096
      read-latency => 1
      write-latency => 1
      reader => io_out_8b_MPORT
      writer => MPORT
      read-under-write => undefined
    node _GEN_0 = validif(io_lut_we, io_lut_waddr) @[src/main/scala/QuantizationLUT.scala 23:20 24:14]
    node _GEN_1 = validif(io_lut_we, clock) @[src/main/scala/QuantizationLUT.scala 23:20 24:14]
    node _GEN_2 = mux(io_lut_we, UInt<1>("h1"), UInt<1>("h0")) @[src/main/scala/QuantizationLUT.scala 23:20 24:14 20:24]
    node _GEN_3 = validif(io_lut_we, UInt<1>("h1")) @[src/main/scala/QuantizationLUT.scala 23:20 24:14]
    node _GEN_4 = validif(io_lut_we, io_lut_wdata) @[src/main/scala/QuantizationLUT.scala 23:20 24:14]
    node read_addr = bits(io_in_32b, 11, 0) @[src/main/scala/QuantizationLUT.scala 29:28]
    node _GEN_5 = validif(UInt<1>("h1"), read_addr) @[src/main/scala/QuantizationLUT.scala 32:{24,24}]
    node _GEN_6 = mux(UInt<1>("h1"), UInt<1>("h1"), UInt<1>("h0")) @[src/main/scala/QuantizationLUT.scala 20:24 32:{24,24}]
    node _io_out_8b_WIRE = _GEN_5 @[src/main/scala/QuantizationLUT.scala 32:24]
    node _GEN_7 = validif(UInt<1>("h1"), _io_out_8b_WIRE) @[src/main/scala/QuantizationLUT.scala 32:{24,24}]
    node _GEN_8 = validif(UInt<1>("h1"), clock) @[src/main/scala/QuantizationLUT.scala 32:{24,24}]
    io_out_8b <= lut.io_out_8b_MPORT.data @[src/main/scala/QuantizationLUT.scala 32:13]
    lut.io_out_8b_MPORT.addr <= _GEN_7
    lut.io_out_8b_MPORT.en <= _GEN_6
    lut.io_out_8b_MPORT.clk <= _GEN_8
    lut.MPORT.addr <= _GEN_0
    lut.MPORT.en <= _GEN_2
    lut.MPORT.clk <= _GEN_1
    lut.MPORT.data <= _GEN_4
    lut.MPORT.mask <= _GEN_3

  module QuantizationUnit_16 : @[src/main/scala/QuantizationLUT.scala 38:7]
    input clock : Clock @[src/main/scala/QuantizationLUT.scala 38:7]
    input reset : UInt<1> @[src/main/scala/QuantizationLUT.scala 38:7]
    input io_in_vec_0 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_1 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_2 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_3 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_4 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_5 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_6 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_7 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_8 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_9 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_10 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_11 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_12 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_13 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_14 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_in_vec_15 : UInt<32> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_0 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_1 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_2 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_3 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_4 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_5 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_6 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_7 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_8 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_9 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_10 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_11 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_12 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_13 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_14 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    output io_out_vec_15 : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_lut_we : UInt<1> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_lut_waddr : UInt<12> @[src/main/scala/QuantizationLUT.scala 39:14]
    input io_lut_wdata : UInt<8> @[src/main/scala/QuantizationLUT.scala 39:14]

    inst lanes_0 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_1 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_2 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_3 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_4 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_5 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_6 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_7 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_8 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_9 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_10 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_11 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_12 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_13 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_14 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    inst lanes_15 of QuantizationLane @[src/main/scala/QuantizationLUT.scala 50:34]
    io_out_vec_0 <= lanes_0.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_1 <= lanes_1.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_2 <= lanes_2.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_3 <= lanes_3.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_4 <= lanes_4.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_5 <= lanes_5.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_6 <= lanes_6.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_7 <= lanes_7.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_8 <= lanes_8.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_9 <= lanes_9.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_10 <= lanes_10.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_11 <= lanes_11.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_12 <= lanes_12.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_13 <= lanes_13.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_14 <= lanes_14.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    io_out_vec_15 <= lanes_15.io_out_8b @[src/main/scala/QuantizationLUT.scala 55:24]
    lanes_0.clock <= clock
    lanes_0.reset <= reset
    lanes_0.io_in_32b <= io_in_vec_0 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_0.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_0.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_0.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_1.clock <= clock
    lanes_1.reset <= reset
    lanes_1.io_in_32b <= io_in_vec_1 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_1.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_1.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_1.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_2.clock <= clock
    lanes_2.reset <= reset
    lanes_2.io_in_32b <= io_in_vec_2 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_2.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_2.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_2.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_3.clock <= clock
    lanes_3.reset <= reset
    lanes_3.io_in_32b <= io_in_vec_3 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_3.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_3.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_3.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_4.clock <= clock
    lanes_4.reset <= reset
    lanes_4.io_in_32b <= io_in_vec_4 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_4.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_4.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_4.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_5.clock <= clock
    lanes_5.reset <= reset
    lanes_5.io_in_32b <= io_in_vec_5 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_5.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_5.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_5.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_6.clock <= clock
    lanes_6.reset <= reset
    lanes_6.io_in_32b <= io_in_vec_6 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_6.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_6.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_6.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_7.clock <= clock
    lanes_7.reset <= reset
    lanes_7.io_in_32b <= io_in_vec_7 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_7.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_7.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_7.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_8.clock <= clock
    lanes_8.reset <= reset
    lanes_8.io_in_32b <= io_in_vec_8 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_8.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_8.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_8.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_9.clock <= clock
    lanes_9.reset <= reset
    lanes_9.io_in_32b <= io_in_vec_9 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_9.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_9.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_9.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_10.clock <= clock
    lanes_10.reset <= reset
    lanes_10.io_in_32b <= io_in_vec_10 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_10.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_10.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_10.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_11.clock <= clock
    lanes_11.reset <= reset
    lanes_11.io_in_32b <= io_in_vec_11 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_11.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_11.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_11.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_12.clock <= clock
    lanes_12.reset <= reset
    lanes_12.io_in_32b <= io_in_vec_12 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_12.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_12.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_12.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_13.clock <= clock
    lanes_13.reset <= reset
    lanes_13.io_in_32b <= io_in_vec_13 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_13.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_13.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_13.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_14.clock <= clock
    lanes_14.reset <= reset
    lanes_14.io_in_32b <= io_in_vec_14 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_14.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_14.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_14.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
    lanes_15.clock <= clock
    lanes_15.reset <= reset
    lanes_15.io_in_32b <= io_in_vec_15 @[src/main/scala/QuantizationLUT.scala 54:24]
    lanes_15.io_lut_we <= io_lut_we @[src/main/scala/QuantizationLUT.scala 58:27]
    lanes_15.io_lut_waddr <= io_lut_waddr @[src/main/scala/QuantizationLUT.scala 59:27]
    lanes_15.io_lut_wdata <= io_lut_wdata @[src/main/scala/QuantizationLUT.scala 60:27]
