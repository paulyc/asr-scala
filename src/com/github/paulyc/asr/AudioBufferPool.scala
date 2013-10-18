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
