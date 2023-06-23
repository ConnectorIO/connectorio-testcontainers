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
package org.connectorio.testcontainers.karaf.std;

import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.connectorio.testcontainers.karaf.Customization;
import org.connectorio.testcontainers.karaf.KarafBasedContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;
import org.connectorio.testcontainers.karaf.shell.Shell;

/**
 * Provides a shell connectivity.
 */
public class ShellCustomization implements Customization {

  /**
   * SSH port to use, will be replaced with mapped port after container is started.
   */
  private final int port;
  private final String prompt;
  private Shell shell;

  public ShellCustomization() {
    this(8101);
  }

  public ShellCustomization(int port) {
    this(port, "karaf@root()>");
  }

  public ShellCustomization(int port, String prompt) {
    this.port = port;
    this.prompt = prompt;
  }

  @Override
  public Set<Class<? extends Customization>> getDependencies() {
    Class<? extends Customization> behaviorClass = KarafUserCustomization.class;
    return new HashSet<>(List.of(behaviorClass));
  }

  @Override
  public void starting(LaunchContext context, KarafBasedContainer<?> container) {
  }

  @Override
  public void started(LaunchContext context, KarafBasedContainer<?> container) {
    Integer mappedPort = container.getMappedPort(port);
    if (mappedPort == null) {
      throw new IllegalStateException("Port " + port + " is not exposed by container");
    }

    KarafUserCustomization adminAccount = context.getCustomization(KarafUserCustomization.class);
    shell = new Shell("localhost", mappedPort, prompt, adminAccount.getUsername(),adminAccount.getPassowrd());
  }

  public Shell getShell() {
    try {
      shell.open();
      return shell;
    } catch (JSchException | IOException e) {
      throw new IllegalStateException("Could not open shell", e);
    }
  }

}
