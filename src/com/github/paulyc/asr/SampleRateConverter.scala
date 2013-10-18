package com.github.paulyc.asr

import akka.actor.ActorRef

/**
 * Created by paulyc on 10/17/13.
 */

class SampleRateConverter(var inputSamplingRate: Double, var outputSamplingRate: Double) {
  def convert(buffer: AudioSystem.SampleBuffer) {
  }
}

case class ConvertSampleRateRequest(buffer: AudioBuffer, requestor: ActorRef)

class ConvertSampleRateActor(inputRate: Int, outputRate: Int) extends AudioSystemActor {
  val converter = new SampleRateConverter(inputRate, outputRate)

  def receive = {
    case ConvertSampleRateRequest(AudioBuffer(buffer, BufferConfig(inputRate)), requestor) => {
      converter.convert(buffer)
      requestor ! BufferResponse(AudioBuffer(buffer, BufferConfig(outputRate)))
    }
    case _ => println("Invalid ConvertSampleRateActor request")
  }
}
