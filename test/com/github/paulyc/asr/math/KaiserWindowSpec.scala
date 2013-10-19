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

import org.scalatest.FlatSpec

/**
 * Created by paulyc on 10/18/13.
 */
class KaiserWindowSpec extends FlatSpec with FancyDouble {
  "KaiserWindowTable" should "have correct values and clamp outside [-1.0, 1.0]" in {
    assert(KaiserWindowTable.get(0.0) ~= 1.0)
    assert(KaiserWindowTable.get(0.5) ~= 0.464862)
    assert(KaiserWindowTable.get(-0.5) ~= 0.464862)
    assert(KaiserWindowTable.get(1.0) ~= 0.0115026)
    assert(KaiserWindowTable.get(1.1) ~= 0.0115026)
    assert(KaiserWindowTable.get(0.25) ~= 0.833118)
  }
}
