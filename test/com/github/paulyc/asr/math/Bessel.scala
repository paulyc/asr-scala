package com.github.paulyc.asr.math

import org.scalatest._

/**
 * Created by paulyc on 10/17/13.
 */

class BesselSpec extends FlatSpec {
  implicit def fancyDouble(lhs:Double) = new {
    def ~=(rhs: Double)(implicit delta : Double = 0.00001) : Boolean = {
        val diff = math.abs(lhs - rhs)
        diff < delta
    }
  }

  "I_0" should "calculate correct values" in {
    assert(I_0(1.5) ~= 1.64672)
    assert(I_0(2.5) ~= 3.28984)
  }
}
