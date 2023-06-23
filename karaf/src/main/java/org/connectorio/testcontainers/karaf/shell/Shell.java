/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.testcontainers.karaf.shell;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;

/**
 * Basic ssh shell client implementation which allows to send commands and grab their output.
 */
public class Shell implements AutoCloseable {

  private final String host;
  private final int port;
  private final String prompt;
  private final String username;
  private final String password;
  private Session session;
  private ChannelShell channel;

  private Pipe in;
  private Pipe out;

  public Shell(String host, int port, String prompt, String username, String password) {
    this.host = host;
    this.port = port;
    this.prompt = prompt;
    this.username = username;
    this.password = password;
  }

  public void open() throws JSchException, IOException {
    JSch sch = new JSch();
    session = sch.getSession(username, host, port);
    session.setConfig("StrictHostKeyChecking", "no");
    session.setConfig("ServerAliveInterval", "10");
    session.setDaemonThread(true);
    session.setServerAliveCountMax(10);
    session.setPassword(password);
    session.connect();
    channel = (ChannelShell) session.openChannel("shell");

    this.in = new Pipe();
    this.out = new Pipe();

    channel.setInputStream(in.input);
    channel.setOutputStream(out.output);
    channel.setPtyType("dumb");
    channel.connect();
    readAnswer("", prompt);
  }

  public void close() {
    if (channel != null) {
      channel.disconnect();
      channel = null;
    }
    if (session != null) {
      session.disconnect();
      session = null;
    }
  }

  public String execute(String command) {
    try {
      in.output.write((command + "\n").getBytes());
      in.output.flush();
      // wait for network

      return readAnswer(command, prompt);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readAnswer(String command, String endSequence) throws IOException {
    StringBuilder output = new StringBuilder();
    byte[] buff = new byte[1024];
    boolean cleaned = false;
    do {
      if (command.length() > 0 && !cleaned) {
        // discard command which gets copied from input to output stream
        int commandIndex = output.indexOf(command);
        if (commandIndex != -1) {
          // remove everything from beginning up to command
          output.delete(0, commandIndex + command.length());
          cleaned = true;
        }
      }
      if (output.indexOf(endSequence) != -1) {
        break;
      }

      out.input.read(buff);
      String chunk = new String(buff).trim();
      output.append(chunk);
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } while (channel.isConnected());
    return output.toString();
  }
}
