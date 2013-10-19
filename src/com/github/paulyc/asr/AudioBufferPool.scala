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

package com.github.paulyc.asr

import akka.actor.Actor

/**
 * Created by paulyc on 10/17/13.
 */
class AudioBufferPool(factory: AudioSystem.SampleBufferFactory) extends AudioSystemDefaults {
  var bufferList : List[AudioSystem.SampleBuffer] = Nil

  def getBuffer : AudioSystem.SampleBuffer = {
    bufferList match {
      case head :: tail => {
        bufferList = tail
        head
      }
      case Nil => factory()
    }
  }

  def storeBuffer(buffer : AudioSystem.SampleBuffer) {
    bufferList ::= buffer
  }
}

class BufferPoolActor(factory: AudioSystem.SampleBufferFactory) extends Actor {
  val pool = new AudioBufferPool(factory)

  def receive = {
    case AllocateBuffer() => sender ! GotBuffer(pool.getBuffer)
    case FreeBuffer(buffer) => pool.storeBuffer(buffer)
  }
}
