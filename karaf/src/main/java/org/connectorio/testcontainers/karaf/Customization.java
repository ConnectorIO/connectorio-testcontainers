package org.connectorio.testcontainers.karaf;

import java.util.Set;

public interface Customization {

  Set<Class<? extends Customization>> getDependencies();

  void starting(LaunchContext context, KarafBasedContainer<?> container);

  void started(LaunchContext context, KarafBasedContainer<?> container);

}
