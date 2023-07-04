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
package org.connectorio.testcontainers.openhab.std;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.connectorio.testcontainers.karaf.Customization;
import org.connectorio.testcontainers.karaf.KarafBasedContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;
import org.connectorio.testcontainers.karaf.std.KarafUserCustomization;
import org.testcontainers.containers.Container.ExecResult;

/**
 * Creates user API token using shell cli.
 */
public class UserApiTokenCustomization implements Customization {

  private String apiToken;

  @Override
  public Set<Class<? extends Customization>> getDependencies() {
    return new HashSet<>(List.of(UserAccountCustomization.class, KarafUserCustomization.class));
  }

  @Override
  public void starting(LaunchContext context, KarafBasedContainer<?> container) {

  }

  @Override
  public void started(LaunchContext context, KarafBasedContainer<?> container) {
    KarafUserCustomization admin = context.getCustomization(KarafUserCustomization.class);
    UserAccountCustomization account = context.getCustomization(UserAccountCustomization.class);

    String username = account.getUsername();
    ExecResult result = container.execute("echo 'users addApiToken " + username + " admin admin ' | /openhab/runtime/bin/client -p " + admin.getPassowrd());
    if (result.getExitCode() != 0) {
      throw new IllegalStateException("Creation of API token for user " + username + " failed");
    }
    Pattern pattern = Pattern.compile("(oh\\.admin\\..*?)\\s+");
    Matcher matcher = pattern.matcher(result.getStdout());
    if (matcher.find()) {
      this.apiToken = matcher.group(1);
    } else {
      throw new IllegalArgumentException("Could not determine admin access token");
    }
  }

  public String getApiToken() {
    return apiToken;
  }

}
