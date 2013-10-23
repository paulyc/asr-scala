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

import org.scalatest.FlatSpec
import akka.actor.Props
import akka.pattern.ask
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * Created by paulyc on 10/22/13.
 */
class BufferPoolSpec extends FlatSpec with TestActorSystem {
  val numBuffers = 10
  val bufferPoolActor = actorSystem.actorOf(Props(new BufferPoolActor(AudioSystem.DefaultBufferFactory)))

  "A BufferPoolActor" should "create and pool buffers" in {

    val futureList = Future.traverse((1 to numBuffers).toList)(_ => bufferPoolActor ? AllocateBuffer)
    Await.result(futureList, 5 seconds) foreach { case GotBuffer(x) => bufferPoolActor ! FreeBuffer(x) }
    assert(AudioSystem.DefaultBufferFactory.getCount == numBuffers)
  }

  object TestBufferHandler extends BufferHandler {
    def test() {
      val buffer = allocateBuffer()
      assert(buffer.getClass == classOf[SampleBuffer])
      freeBuffer(buffer)
    }
  }

  "TestBufferHandler" should "get and free buffers" in {
    TestBufferHandler.test()
    // due to using a different BufferPoolActor should allocate another buffer
    assert(AudioSystem.DefaultBufferFactory.getCount == numBuffers+1)
  }
}
