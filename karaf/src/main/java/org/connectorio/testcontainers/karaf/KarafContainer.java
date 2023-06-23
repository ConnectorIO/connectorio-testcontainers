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

import java.time.Duration;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class KarafContainer<T extends KarafContainer<T>> extends KarafBaseContainer<T> {

  public KarafContainer(String tag) {
    this("apache/karaf", tag);
  }

  public KarafContainer(String image, String tag) {
    this(DockerImageName.parse(image).withTag(tag));
  }

  public KarafContainer(DockerImageName image) {
    super(image);

    withExposedPorts(8101);
    WaitStrategy waitStrategy = Wait.forListeningPort()
      .withStartupTimeout(Duration.ofSeconds(60));
    waitingFor(waitStrategy);
  }

}
