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

import akka.actor.ActorRef

/**
 * Created by paulyc on 10/17/13.
 */

class SampleRateConverter(private var inputSamplingRate: Double, private var outputSamplingRate: Double) {
  private var inputSamplingPeriod = 0.0
  private var outputSamplingPeriod = 0.0
  private var impulseResponseFrequency = 0.0
  private var impulseResponsePeriod = 0.0
  private var impulseResponseScale = 0.0
  private var rho = 0.0

  //private var

  def setOutputSamplingRate(rate: Double) {
    outputSamplingRate = rate
    outputSamplingPeriod = 1.0 / outputSamplingRate
    impulseResponseFrequency = scala.math.min(inputSamplingRate, outputSamplingRate)
    impulseResponsePeriod = 1.0 / impulseResponseFrequency

    rho = outputSamplingRate / inputSamplingRate
    impulseResponseScale = scala.math.min(1.0, rho)
  }

  def setInputSamplingRate(rate: Double) {
    inputSamplingRate = rate
    inputSamplingPeriod = 1.0 / inputSamplingRate
    setOutputSamplingRate(outputSamplingRate)
  }

  setInputSamplingRate(inputSamplingRate)


}

class ConvertSampleRateActor(inputRate: Int, outputRate: Int) extends AudioSystemActor {
  val converter = new SampleRateConverter(inputRate, outputRate)

  def handleBufferRequest() {}

  /*def receive = {
    case ConvertSampleRateRequest(AudioBuffer(buffer, BufferConfig(inputRate)), requestor) => {
      //converter.convert(buffer)
      requestor ! BufferResponse(AudioBuffer(buffer, BufferConfig(outputRate)))
    }
    case _ => println("Invalid ConvertSampleRateActor request")
  }*/
}
