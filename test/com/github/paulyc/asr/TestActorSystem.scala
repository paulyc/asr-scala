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

import akka.util.Timeout
import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import scala.concurrent.duration._

/**
 * Created by paulyc on 10/22/13.
 */
trait TestActorSystem {
  implicit val timeout = Timeout(1 second)
  implicit val executionContext = ExecutionContext.Implicits.global
  val actorSystem = ActorSystem()
}
