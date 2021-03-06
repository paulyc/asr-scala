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

/**
 * Created by paulyc on 10/17/13.
 */

class KaiserWindow(alpha: Double) {
  val beta = math.Pi * alpha
  def apply(t: Double) = I_0(beta * math.sqrt(1 - t*t) ) / I_0(beta)
}

class KaiserWindowTable(alpha: Double) extends KaiserWindow(alpha) {
  private val tableSize = 10000
  private val table : Array[Double] = (0 until tableSize).map(kaiser).toArray
  private def indexToTime(i: Int) = i.toDouble / tableSize
  private def timeToIndex(t: Double) = math.min((math.abs(t) * tableSize).toInt, tableSize - 1)

  def kaiser(i: Int) = this(indexToTime(i))
  def get(i: Int) = table(i)
  def get(t: Double) = table(timeToIndex(t))
}

object KaiserWindowTable extends KaiserWindowTable(2.0)
