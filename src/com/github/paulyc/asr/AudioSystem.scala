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
import akka.pattern.ask
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import akka.util.Timeout

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

  implicit val executionContext = ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5 seconds)
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
  def unapply() = {
    (left, right)
  }
}

case class BufferConfig(sampleRate: Int)
case class AudioBuffer(buffer: AudioSystem.SampleBuffer, config: BufferConfig)
case class InternalAudioBuffer(buffer: AudioSystem.SampleBuffer)

case object BufferRequest
case class BufferRequestConfig(config: BufferConfig)
case class BufferResponse(buffer: Option[InternalAudioBuffer])

case object AllocateBuffer
case class GotBuffer(buffer : AudioSystem.SampleBuffer)
case class FreeBuffer(buffer : AudioSystem.SampleBuffer)

object InternalBufferConfig extends BufferConfig(AudioSystem.DefaultSampleRate)

case class SetSource(actor: ActorRef)

trait BufferHandler extends AudioSystemDefaults {
  protected def allocateBuffer() : SampleBuffer = {
    Await.result(AudioSystem.bufferPoolActor ? AllocateBuffer, 1 second) match {
      case GotBuffer(buffer) => buffer
      case _ => null
    }
  }

  protected def freeBuffer(buffer: SampleBuffer) {
    AudioSystem.bufferPoolActor ! FreeBuffer(buffer)
  }
}

abstract class AudioSystemActor(implicit val outputSamplingRate: Int = AudioSystem.DefaultSampleRate) extends Actor
  with BufferHandler {

  protected var sourceActor : Option[ActorRef] = None

  def receive = {
    case SetSource(actor) => sourceActor = Some(actor)
    case BufferRequest  => handleBufferRequest()
  }

  protected def handleBufferRequest()
}

class ZeroSourceActor extends AudioSystemActor {
  protected def handleBufferRequest() {
    val buffer = allocateBuffer()
    for (frame <- buffer) {
      frame(0.0f, 0.0f)
    }
    sender ! BufferResponse(Some(InternalAudioBuffer(buffer)))
  }
}

class SineSourceActor(initialFrequency: Float = 440.0f) extends AudioSystemActor {
  var time = 0.0
  var frequency = initialFrequency
  var _2_pi_f = 2.0f * scala.math.Pi * frequency
  val period = 1.0 / DefaultSampleRate

  def setFrequency(frequency: Float) {
    this.frequency = frequency
    _2_pi_f = 2.0f * scala.math.Pi * frequency
  }

  protected def handleBufferRequest() {
    val buffer = allocateBuffer()
    for (frame <- buffer) {
      val value = scala.math.sin(_2_pi_f * time).toFloat
      frame(value, value)
      time += period
    }
    sender ! BufferResponse(Some(InternalAudioBuffer(buffer)))
  }
}

class FileSourceActor extends AudioSystemActor {

  val NativeSampleRate = 44100
  val NativeChannels = 2

  val sampleRateConverter : Option[ActorRef] = NativeSampleRate == outputSamplingRate match {
    case true => None
    case false => {
      val actor = context.actorOf(Props(new ConvertSampleRateActor(NativeSampleRate, outputSamplingRate)))
      actor ! SetSource(self)
      Some(actor)
    }
  }

  object InputBufferConfig extends BufferConfig(NativeSampleRate)

  protected def handleBufferRequest() {
    sampleRateConverter match {
      case Some(converter) => {
        if (sender == converter) {
          sender ! BufferResponse(nextBufferFromFile())
        } else {
          converter ? BufferRequest onSuccess { case _ => sender ! _ }
        }
      }
      case None => sender ! BufferResponse(nextBufferFromFile())
    }
  }

  protected def nextBufferFromFile() : Option[InternalAudioBuffer] = {
    val buffer = allocateBuffer()
    Some(InternalAudioBuffer(buffer))
  }
}