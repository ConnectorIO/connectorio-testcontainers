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

import java.util.Collections;
import java.util.Set;
import org.connectorio.testcontainers.karaf.Customization;
import org.connectorio.testcontainers.karaf.KarafBasedContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;
import org.connectorio.testcontainers.karaf.std.KarafUserCustomization;
import org.testcontainers.containers.Container.ExecResult;

/**
 * Creates openHAB user account using shell call.
 */
public class UserAccountCustomization implements Customization {

  private final String username;
  private final String password;

  public UserAccountCustomization(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public Set<Class<? extends Customization>> getDependencies() {
    return Collections.emptySet();
  }

  @Override
  public void starting(LaunchContext context, KarafBasedContainer<?> container) {

  }

  @Override
  public void started(LaunchContext context, KarafBasedContainer<?> container) {
    KarafUserCustomization adminUser = context.getCustomization(KarafUserCustomization.class);

    ExecResult result = container.execute("echo 'users add " + username +" " + password + " administrator' | /openhab/runtime/bin/client -p " + adminUser.getPassowrd());
    if (result.getExitCode() != 0) {
      throw new IllegalStateException("Injection of openhab account for " + username + " failed");
    }
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

}
