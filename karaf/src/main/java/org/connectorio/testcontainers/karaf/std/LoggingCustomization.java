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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.connectorio.testcontainers.karaf.Customization;
import org.connectorio.testcontainers.karaf.KarafBasedContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;
import org.testcontainers.images.builder.Transferable;

public class LoggingCustomization implements Customization {

  private final Map<String, String> loggers = new HashMap<>();

  public LoggingCustomization withLoggingLevel(String level, String category) {
    loggers.put(category, level);
    return this;
  }

  @Override
  public Set<Class<? extends Customization>> getDependencies() {
    return Collections.emptySet();
  }

  @Override
  public void starting(LaunchContext context, KarafBasedContainer<?> container) {
    try {
      StringBuilder paxLogging = new StringBuilder(new String(getClass().getResourceAsStream("/org.ops4j.pax.logging.cfg").readAllBytes()));

      for (Entry<String, String> logger : loggers.entrySet()) {
        String code = logger.getKey().replace("", "");
        paxLogging.append("\nlog4j2.logger.").append(code).append(".name=").append(logger.getKey());
        paxLogging.append("\nlog4j2.logger.").append(code).append(".level=").append(logger.getValue());
      }

      String path = container.getKarafEtc();
      container.withCopyToContainer(Transferable.of(paxLogging.toString().getBytes()), path + "/org.ops4j.pax.logging.cfg");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void started(LaunchContext context, KarafBasedContainer<?> container) {

  }

}
