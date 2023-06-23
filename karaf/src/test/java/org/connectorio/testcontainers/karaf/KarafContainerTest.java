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
package org.connectorio.testcontainers.karaf;

import static org.assertj.core.api.Assertions.assertThat;

import org.connectorio.testcontainers.karaf.shell.Shell;
import org.connectorio.testcontainers.karaf.std.KarafUserCustomization;
import org.connectorio.testcontainers.karaf.std.ShellCustomization;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class KarafContainerTest {

  @Container
  public KarafContainer<?> container = new KarafContainer<>("latest")
    .withCustomization(new ShellCustomization())
    .withCustomization(new KarafUserCustomization("karaf", "karaf"))
    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("container")));

  @Test
  public void verifyKarafContainer() throws Exception {
    assertThat(container.isRunning()).isTrue();

    Thread.sleep(1500);

    Shell shell = container.getCustomization(ShellCustomization.class).getShell();
    String bundles = shell.execute("la -l");
    assertThat(bundles).isNotEmpty()
      .contains("org.apache.karaf");

    String services = shell.execute("ls");
    assertThat(services).isNotEmpty()
      .contains("FeaturesService");
    shell.close();
  }

}
