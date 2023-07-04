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
package org.connectorio.testcontainers.karaf.context;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import org.connectorio.testcontainers.karaf.Customization;
import org.connectorio.testcontainers.karaf.KarafBaseContainer;
import org.connectorio.testcontainers.karaf.LaunchContext;
import org.jetbrains.annotations.Nullable;

public class BasicLaunchContext implements LaunchContext {

  private final Set<Customization> customizations = new TreeSet<>(new Comparator<>() {
    @Override
    public int compare(Customization o1, Customization o2) {
      if (isPartOfDependencyChain(o1, o2.getDependencies())) {
        return -1;
      }

      if (isPartOfDependencyChain(o2, o1.getDependencies())) {
        return 1;
      }
      // equal in case of set might trigger removal of element
      return -1;
    }

    @Nullable
    private boolean isPartOfDependencyChain(Customization customization, Set<Class<? extends Customization>> chain) {
      for (Class<?> type : chain) {
        if (type.isInstance(customization)) {
          return true;
        }
      }
      return false;
    }
  });

  @Override
  public <X extends Customization> X getCustomization(Class<X> type) {
    return customizations.stream()
      .filter(type::isInstance)
      .map(type::cast)
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Requested customization of type " + type.getName() + " not found"));
  }

  @Override
  public void add(Customization behavior) {
    this.customizations.add(behavior);
  }

  public void starting(InspectContainerResponse containerInfo, KarafBaseContainer<?> container) {
    for (Customization customization : customizations) {
      customization.starting(this, container);
    }
  }

  public void started(InspectContainerResponse containerInfo, KarafBaseContainer<?> container) {
    for (Customization customization : customizations) {
      customization.started(this, container);
    }
  }

}
