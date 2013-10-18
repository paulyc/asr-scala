package com.github.paulyc.asr.math

/**
 * Created by paulyc on 10/17/13.
 */

object I_0 {
  val iterations = 11
  def iter(x_squared_4: Double)(sum_kfact : (Double, Double), k: Int) : (Double, Double) = {
    val (sum, kfact) = sum_kfact
    (sum + math.pow(x_squared_4, k) / (kfact*kfact), kfact * (k+1))
  }
  def apply(x: Double) : Double = (0 until iterations).foldLeft((0.0, 1.0))(iter(0.25*x*x))._1
}
