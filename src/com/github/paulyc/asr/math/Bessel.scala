/*
 * asr-scala digital signal processor
 *
 * Copyright (C) 2013 Paul Ciarlo <paul.ciarlo@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.paulyc.asr.math

import scala.annotation.tailrec

/**
 * Created by paulyc on 10/17/13.
 */

object I_0 {
  val iterations = 15

  @tailrec
  def iter(x_squared_4: Double, k: Int, sum: Double, kfact: Double) : Double = {
    k < iterations match {
      case true => iter(x_squared_4, k+1, sum + math.pow(x_squared_4, k) / (kfact*kfact), kfact * (k+1))
      case false => sum
    }
  }
  def apply(x: Double) = iter(0.25*x*x, 0, 0.0, 1.0)

/*
  def iter(x_squared_4: Double)(sum_kfact : (Double, Double), k: Int) : (Double, Double) = {
    val (sum, kfact) = sum_kfact
    (sum + math.pow(x_squared_4, k) / (kfact*kfact), kfact * (k+1))
  }
  def apply(x: Double) : Double = (0 until iterations).foldLeft((0.0, 1.0))(iter(0.25*x*x))._1
*/
}
