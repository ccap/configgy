/*
 * Copyright 2010 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter
package logging

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress}
import java.util.{logging => javalog}
import org.specs.Specification
import config._
import extensions._

class ScribeHandlerSpec extends Specification {
  val utcFormatter = new FormatterConfig { override val timezone = Some("UTC") }.apply()

  def config(time: Int, max: Int, _formatter: Formatter) = new ScribeHandlerConfig {
    override val formatter = _formatter
    override val category = "test"
    override val bufferTimeMilliseconds = time
    override val maxMessagesToBuffer = max
  }

  val record1 = new javalog.LogRecord(Level.INFO, "This is a message.")
  record1.setMillis(1206769996722L)
  record1.setLoggerName("hello")
  val record2 = new javalog.LogRecord(Level.INFO, "This is another message.")
  record2.setLoggerName("hello")
  record2.setMillis(1206769996722L)

  "ScribeHandler" should {
    "build a scribe RPC call" in {
      val scribe = new ScribeHandler(config(100, 10000, utcFormatter))
      scribe.publish(record1)
      scribe.publish(record2)
      scribe.makeBuffer(2).array.hexlify mustEqual "000000b080010001000000034c6f67000000000f0001" +
        "0c000000020b000100000004746573740b000200000036494e46205b32303038303332392d30353a35333a3" +
        "1362e3732325d2068656c6c6f3a20546869732069732061206d6573736167652e0a000b0001000000047465" +
        "73740b00020000003c494e46205b32303038303332392d30353a35333a31362e3732325d2068656c6c6f3a2" +
        "05468697320697320616e6f74686572206d6573736167652e0a0000"
    }

    "throw away log messages if scribe is too busy" in {
      val scribe = new ScribeHandler(config(5000, 1, BareFormatter))
      scribe.publish(record1)
      scribe.publish(record2)
      scribe.queue.toList mustEqual List("This is another message.\n")
    }
  }
}
