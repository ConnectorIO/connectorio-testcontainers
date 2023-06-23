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
package org.connectorio.testcontainers.openhab;

import java.time.Duration;
import java.util.Optional;
import org.connectorio.testcontainers.karaf.KarafBaseContainer;
import org.connectorio.testcontainers.karaf.KarafBasedContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;
import org.connectorio.testcontainers.karaf.std.KarafUserCustomization;
import org.connectorio.testcontainers.karaf.std.ShellCustomization;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class OpenHABContainer<T extends OpenHABContainer<T>> extends KarafBaseContainer<T> {

  public OpenHABContainer() {
    // fallback openhab version to latest 3.0.x release which is for now 3.0.4
    this(Optional.ofNullable(System.getenv("OPENHAB_VERSION")).orElse("3.0.4"));
  }

  public OpenHABContainer(String tag) {
    this(DockerImageName.parse("openhab/openhab").withTag(tag));
  }

  public OpenHABContainer(String image, String tag) {
    this(DockerImageName.parse(image).withTag(tag));
  }

  public OpenHABContainer(DockerImageName image) {
    super(image);

    withExposedPorts(8101, 8080);

    setKarafEtc("/openhab/userdata/etc");
    setKarafBase("/openhab/userdata");
    setKarafHome("/openhab/runtime");
    setKarafData("/openhab/userdata");
    setKarafLog("/openhab/userdata/logs");
    setDeployDirectory("/openhab/addons");

    WaitStrategy strategy = new WaitAllStrategy()
      .withStrategy(Wait.forListeningPort())
      .withStrategy(new HttpWaitStrategy().forPort(8080).forPath("/rest/spec"))
      .withStrategy(Wait.forLogMessage(".*Startlevel '100' reached.*", 1))
      .withStartupTimeout(Duration.ofSeconds(120));
    waitingFor(strategy);

    withCustomization(new ShellCustomization(8101, "openhab>") {
      @Override
      public void starting(LaunchContext context, KarafBasedContainer<?> container) {
        ExecResult result = container.execute("echo 'sshHost = 0.0.0.0' >> /openhab/userdata/etc/org.apache.karaf.shell.cfg");
        if (result.getExitCode() != 0) {
          throw new IllegalStateException("Could not modify ssh daemon configuration");
        }
      }
    });
    // bypass injection of user, it is enabled by default
    withCustomization(new KarafUserCustomization("openhab", "habopen") {
      @Override
      public void starting(LaunchContext context, KarafBasedContainer<?> container) {
      }

      @Override
      public void started(LaunchContext context, KarafBasedContainer<?> container) {
      }
    });
  }

}
