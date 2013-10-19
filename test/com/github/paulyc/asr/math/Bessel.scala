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

import org.scalatest._

/**
 * Created by paulyc on 10/17/13.
 */

class BesselSpec extends FlatSpec with FancyDouble {
  "I_0" should "calculate correct values" in {
    assert(I_0(1.5) ~= 1.64672)
    assert(I_0(2.5) ~= 3.28984)
  }

  "I_0" should "calculate a million values" in {
    val startTime = System.currentTimeMillis()
    val values = 0 until 1000000 map { I_0(_) }
    values.zipWithIndex.foreach { case (x,y) => if (y % 100000 == 0) println(x) }
    val duration = System.currentTimeMillis() - startTime
    println("duration was " + duration)
  }
}
