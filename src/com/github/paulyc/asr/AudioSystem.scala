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

import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import scala.collection.mutable
import akka.util.Timeout
import akka.pattern.ask
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext

/**
 * Created by paulyc on 10/16/13.
 */

trait AudioSystemDefaults {
  type InternalSample = Float
  type InternalFrame = StereoFrame
  type SampleBuffer = Array[InternalFrame]

  trait SampleBufferFactory {
    def apply() : SampleBuffer
    def getCount : Int
  }

  val DefaultBufferFrames = 1024
  val DefaultChannels = 2
  val DefaultSampleRate = 48000

  implicit val executionContext : ExecutionContext = ExecutionContext.Implicits.global
}

object AudioSystem extends AudioSystemDefaults {
  lazy val actorSystem = ActorSystem("AudioActorSystem")
  lazy val bufferPoolActor = actorSystem.actorOf(Props(new BufferPoolActor(DefaultBufferFactory)), "BufferPoolActor")
  lazy val zeroSourceActor = actorSystem.actorOf(Props[ZeroSourceActor], "ZeroSourceActor")

  object DefaultBufferFactory extends SampleBufferFactory {
    private var count = 0
    def getCount = count
    def apply() = {
      count += 1
      new SampleBuffer(AudioSystem.DefaultBufferFrames)
    }
  }
}

case class Frame(channels: Int)
case class StereoFrame(var left: AudioSystem.InternalSample, var right: AudioSystem.InternalSample) {
  def apply(left: AudioSystem.InternalSample, right: AudioSystem.InternalSample) {
    this.left = left
    this.right = right
  }
}

case class BufferConfig(sampleRate: Int)
case class AudioBuffer(buffer: AudioSystem.SampleBuffer, config: BufferConfig)

case class BufferRequest(config: BufferConfig)
case class BufferResponse(buffer: AudioBuffer)

case class AllocateBuffer()
case class GotBuffer(buffer : AudioSystem.SampleBuffer)
case class FreeBuffer(buffer : AudioSystem.SampleBuffer)

object InternalBufferConfig extends BufferConfig(AudioSystem.DefaultSampleRate)
class InternalAudioBuffer(override val buffer: AudioSystem.SampleBuffer) extends AudioBuffer(buffer, InternalBufferConfig)
object InternalAudioBuffer {
  def apply(buffer: AudioSystem.SampleBuffer) = new InternalAudioBuffer(buffer)
}

abstract class AudioSystemActor(implicit val outputSamplingRate: Int = AudioSystem.DefaultSampleRate) extends Actor
  with AudioSystemDefaults {
  implicit val timeout = Timeout(5000L)

  def allocateBuffer() : SampleBuffer = {
    AudioSystem.bufferPoolActor ? AllocateBuffer() onComplete {
      case Success(buffer) => buffer
      case Failure(_) => throw new Exception("Something blew up in allocateBuffer!")
    }
    null
  }

  def freeBuffer(buffer: SampleBuffer) {
    AudioSystem.bufferPoolActor ! FreeBuffer(buffer)
  }
}

class ZeroSourceActor extends AudioSystemActor {
  def receive = {
    case BufferRequest(_) => sender ! zeroBufferReply
  }

  def zeroBufferReply = {
    val buffer = allocateBuffer()
    for (frame <- buffer) {
      frame(0.0f, 0.0f)
    }
    BufferResponse(AudioBuffer(buffer, InternalBufferConfig))
  }
}

class FileSourceActor extends AudioSystemActor {

  val NativeSampleRate = 44100
  val NativeChannels = 2

  val sampleRateConverter : Option[ActorRef] = NativeSampleRate == outputSamplingRate match {
    case true => None
    case false => Some(context.actorOf(Props(new ConvertSampleRateActor(NativeSampleRate, outputSamplingRate))))
  }

  object InputBufferConfig extends BufferConfig(NativeSampleRate)
  object OutputBufferConfig extends BufferConfig(outputSamplingRate)

  def receive = {
    case BufferRequest(_) => returnBuffer()
    //case BufferRequest(DefaultBufferConfig()) => returnBuffer()
    //case BufferRequest(BufferConfig(AudioSystem.DefaultBufferSize, NativeChannels, sampleRate)) => returnBufferWithSampleRate()
    //case _ => throw new Exception("Buffer type not supported")
    //case BufferRequest(BufferConfig(samples, NativeChannels, NativeSampleRate)) => returnBuffer(samples)
    //case BufferRequest(BufferConfig(samples, NativeChannels, sampleRate)) => returnBufferWithSampleRate(samples, sampleRate)
    //case BufferRequest(BufferConfig(samples, channels, sampleRate)) => throw new Exception("Channel conversion not supported")
  }

  def returnBuffer() {
    val buffer = allocateBuffer()

    for (frame <- buffer) {
      frame.left = 0.0f
      frame.right = 0.0f
    }

    // get data from file

    sampleRateConverter match {
      case Some(converter) => converter ! ConvertSampleRateRequest(AudioBuffer(buffer, InputBufferConfig), sender)
      case None => sender ! BufferResponse(AudioBuffer(buffer, OutputBufferConfig))
    }
  }
}