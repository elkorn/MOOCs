package simulations

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.math

@RunWith(classOf[JUnitRunner])
class CircuitSuite extends CircuitSimulator with FunSuite {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5

  test("andGate example") {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    in1.setSignal(false)
    in2.setSignal(false)
    run

    assert(out.getSignal === false, "and 1")

    in1.setSignal(true)
    run

    assert(out.getSignal === false, "and 2")

    in2.setSignal(true)
    run

    assert(out.getSignal === true, "and 3")
  }

  //
  // to complete with tests for orGate, demux, ...
  //

  test("orGate example") {
    val in1, in2, out = new Wire

    orGate(in1, in2, out)

    in1.setSignal(false)
    in2.setSignal(false)
    run

    assert(out.getSignal == false, "false | false == false")

    in1.setSignal(false)
    in2.setSignal(true)
    run

    assert(out.getSignal == true, "false | true == true")

    in1.setSignal(true)
    in2.setSignal(false)
    run

    assert(out.getSignal == true, "true | false == true")

    in1.setSignal(true)
    in2.setSignal(true)
    run

    assert(out.getSignal == true, "true | true == true")
  }

  test("orGate2 example") {
    val in1, in2, out = new Wire

    orGate2(in1, in2, out)

    in1.setSignal(false)
    in2.setSignal(false)
    run

    assert(out.getSignal == false, "false | false == false")

    in1.setSignal(false)
    in2.setSignal(true)
    run

    assert(out.getSignal == true, "false | true == true")

    in1.setSignal(true)
    in2.setSignal(false)
    run

    assert(out.getSignal == true, "true | false == true")

    in1.setSignal(true)
    in2.setSignal(true)
    run

    assert(out.getSignal == true, "true | true == true")
  }
  
  test("demux example - c == 0") {
    val in = new Wire
    val out : List[Wire] = List(new Wire)
    
    demux(in, List(), out)
    in.setSignal(true)
    run
    
    assert(out.head.getSignal === true, "true == true")
  }
  
  test("demux example - c == 1") {
    val in = new Wire
    val c : List[Wire] = List(new Wire)
    val out : List[Wire] = List(new Wire, new Wire)
    
    demux(in, c, out)
    in.setSignal(true)
    c.head.setSignal(true)
    run
    
    assert(out.head.getSignal === true, "c[1] -> o[1] (main wire)")
    assert(out.drop(1).head.getSignal === false, "c[1] -> o[1] (other wire)")

    c.head.setSignal(false)
    run
    
    assert(out.drop(1).head.getSignal === true, "c[0] -> o[0] (main wire)")
    assert(out.head.getSignal === false, "c[0] -> o[0] (other wire)")
  }
    
  test("demux example - c == 2") {
    val in = new Wire
    val c : List[Wire] = List(new Wire, new Wire)
    val out : List[Wire] = List(new Wire, new Wire, new Wire, new Wire)
    
    demux(in, c, out)
    in.setSignal(true)
    c.head.setSignal(true)
    run
    
    assert(out.head.getSignal === false, "c[0] -> o[0] (main wire)")
    assert(out.drop(1).head.getSignal === true, "c[0] -> o[1] (other wire)")
    assert(out.drop(2).head.getSignal === false, "c[0] -> o[2] (other wire)")
    assert(out.drop(3).head.getSignal === false, "c[0] -> o[3] (other wire)")
    
    c.tail.head.setSignal(true)
    run
    
    assert(out.head.getSignal === true, "c[0] -> o[0] (main wire)")
    assert(out.drop(1).head.getSignal === false, "c[0] -> o[1] (other wire)")
    assert(out.drop(2).head.getSignal === false, "c[0] -> o[2] (other wire)")
    assert(out.drop(3).head.getSignal === false, "c[0] -> o[3] (other wire)")    

    c.head.setSignal(false)
    run
    
    assert(out.last.getSignal === true, "c[3] -> o[3] (main wire)")
    assert(out.drop(2).head.getSignal === false, "c[2] -> o[2] (other wire)")
    assert(out.drop(1).head.getSignal === false, "c[1] -> o[1] (other wire)")
    assert(out.head.getSignal === false, "c[0] -> o[0] (other wire)")

  }
}
