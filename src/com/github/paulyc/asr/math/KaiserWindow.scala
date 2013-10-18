package com.github.paulyc.asr.math

/**
 * Created by paulyc on 10/17/13.
 */

object KaiserWindow {
  //def apply(beta: Double) : Double = {

//  }
}

class KaiserWindowTable[T](alpha: T) {
  private val tableSize = 10000
  //private val table = new Array[T](tableSize)
}

object KaiserWindowTable extends KaiserWindowTable[Double](2.0)
