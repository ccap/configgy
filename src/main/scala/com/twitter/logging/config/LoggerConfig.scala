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
package config

import java.net.InetAddress

class LoggerConfig {
  /**
   * Name of the logging node. The default ("") is the top-level logger.
   */
  var node: String = ""

  /**
   * Log level for this node. Leaving it null is java's secret signal to use the parent logger's
   * level.
   */
  var level: Level = null

  /**
   * Where to send log messages.
   */
  var handlers: List[HandlerConfig] = Nil
  def handlers_=(h: HandlerConfig) { handlers = List(h) }

  /**
   * Override to have log messages stop at this node. Otherwise they are passed up to parent
   * nodes.
   */
  var useParents = true
}

class FormatterConfig {
  /**
   * Should dates in log messages be reported in a different time zone rather than local time?
   * If set, the time zone name must be one known by the java `TimeZone` class.
   */
  var timezone: Option[String] = None
  def timezone_=(zone: String) { timezone = Some(zone) }

  /**
   * Truncate log messages after N characters. 0 = don't truncate (the default).
   */
  var truncateAt: Int = 0

  /**
   * Truncate stack traces in exception logging (line count).
   */
  var truncateStackTracesAt: Int = 30

  /**
   * Use full package names like "com.example.thingy" instead of just the toplevel name like
   * "thingy"?
   */
  var useFullPackageNames: Boolean = false

  /**
   * Format for the log-line prefix, if any.
   *
   * There are two positional format strings (printf-style): the name of the level being logged
   * (for example, "ERROR") and the name of the package that's logging (for example, "jobs").
   *
   * A string in `<` angle brackets `>` will be used to format the log entry's timestamp, using
   * java's `SimpleDateFormat`.
   *
   * For example, a format string of:
   *
   *     "%.3s [<yyyyMMdd-HH:mm:ss.SSS>] %s: "
   *
   * will generate a log line prefix of:
   *
   *     "ERR [20080315-18:39:05.033] jobs: "
   */
  var prefix: String = "%.3s [<yyyyMMdd-HH:mm:ss.SSS>] %s: "

  def apply() = new Formatter(timezone, truncateAt, truncateStackTracesAt, useFullPackageNames,
    prefix)
}

object BasicFormatterConfig extends FormatterConfig {
  override def apply() = BasicFormatter
}

object BareFormatterConfig extends FormatterConfig {
  override def apply() = BareFormatter
}

abstract class SyslogFormatterConfig extends FormatterConfig {
  /**
   * Hostname to prepend to log lines.
   */
  var hostname: String = InetAddress.getLocalHost().getHostName()

  /**
   * Optional server name to insert before log entries.
   */
  var serverName: Option[String] = None

  /**
   * Use new standard ISO-format timestamps instead of old BSD-format?
   */
  var useIsoDateFormat: Boolean = true

  /**
   * Priority level in syslog numbers.
   */
  var priority: Int = SyslogHandler.PRIORITY_USER

  def serverName_=(name: String) { serverName = Some(name) }

  override def apply() = new SyslogFormatter(hostname, serverName, useIsoDateFormat, priority,
    timezone, truncateAt, truncateStackTracesAt)
}

trait HandlerConfig {
  var formatter: FormatterConfig = BasicFormatterConfig

  def apply(): Handler
}

abstract class ThrottledHandlerConfig extends HandlerConfig {
  /**
   * Timespan to consider duplicates. After this amount of time, duplicate entries will be logged
   * again.
   */
  var durationMilliseconds: Int = 0

  /**
   * Maximum duplicate log entries to pass before suppressing them.
   */
  var maxToDisplay: Int = Int.MaxValue

  /**
   * Wrapped handler.
   */
  var handler: HandlerConfig = null

  def apply() = new ThrottledHandler(handler(), durationMilliseconds, maxToDisplay)
}

abstract class FileHandlerConfig extends HandlerConfig {
  /**
   * Filename to log to.
   */
  var filename: String = null

  /**
   * When to roll the logfile.
   */
  var roll: Policy = Policy.Never

  /**
   * Append to an existing logfile, or truncate it?
   */
  var append: Boolean = true

  /**
   * How many rotated logfiles to keep around, maximum. -1 means to keep them all.
   */
  var rotateCount: Int = -1

  def apply() = new FileHandler(filename, roll, append, rotateCount, formatter())
}

abstract class SyslogHandlerConfig extends HandlerConfig {
  /**
   * Syslog server hostname.
   */
  var server: String = "localhost"

  /**
   * Syslog server port.
   */
  var port: Int = SyslogHandler.DEFAULT_PORT

  def apply() = new SyslogHandler(server, port, formatter())
}

class ScribeHandlerConfig extends HandlerConfig {
  // send a scribe message no more frequently than this:
  var bufferTimeMilliseconds = 100

  // don't connect more frequently than this (when the scribe server is down):
  var connectBackoffMilliseconds = 15000

  var maxMessagesPerTransaction = 1000
  var maxMessagesToBuffer = 10000

  var hostname = "localhost"
  var port = 1463
  var category = "scala"

  def apply() = new ScribeHandler(hostname, port, category, bufferTimeMilliseconds,
    connectBackoffMilliseconds, maxMessagesPerTransaction, maxMessagesToBuffer, formatter())
}