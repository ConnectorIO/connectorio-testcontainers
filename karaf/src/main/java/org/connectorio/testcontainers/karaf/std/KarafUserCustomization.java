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

import java.util.Collections;
import java.util.Set;
import org.connectorio.testcontainers.karaf.Customization;
import org.connectorio.testcontainers.karaf.KarafBasedContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;

/**
 * Appends specified user account to Karaf's users.properties file.
 */
public class KarafUserCustomization implements Customization {

  private final String username;
  private final String password;
  private final String[] roles;

  public KarafUserCustomization(String username, String password) {
    this(username, password, "admin", "manager, viewer", "systembundles", "ssh");
  }

  public KarafUserCustomization(String username, String password, String ... roles) {
    this.username = username;
    this.password = password;
    this.roles = roles;
  }

  @Override
  public Set<Class<? extends Customization>> getDependencies() {
    return Collections.emptySet();
  }

  @Override
  public void starting(LaunchContext context, KarafBasedContainer<?> container) {
    String assignedRoles = roles.length == 0 ? "admin" : String.join(",", roles);
    String command = "echo '" + username + "=" + password  + "," + assignedRoles + "' >> " + container.getKarafEtc() + "/users.properties";
    container.execute(command);
  }

  @Override
  public void started(LaunchContext context, KarafBasedContainer<?> container) {

  }

  public String getUsername() {
    return username;
  }

  public String getPassowrd() {
    return password;
  }
}
