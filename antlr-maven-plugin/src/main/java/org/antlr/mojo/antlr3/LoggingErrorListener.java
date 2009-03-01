package org.antlr.mojo.antlr3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.antlr.tool.ANTLRErrorListener;
import org.antlr.tool.Message;
import org.antlr.tool.ToolMessage;
import org.apache.maven.plugin.logging.Log;

/**
 * Adapting the ANTLRErrorListener to the Mojo Log.
 * @author jbunting
 */
public class LoggingErrorListener implements ANTLRErrorListener {

  private Log log;

  public LoggingErrorListener(Log log) {
    this.log = log;
  }

  public void info(String s) {
    log.info(s);
  }

  public void error(Message message) {
    log.error(message.toString());
  }

  public void warning(Message message) {
    log.warn(message.toString());
  }

  public void error(ToolMessage toolMessage) {
    log.error(toolMessage.toString());
  }
}
