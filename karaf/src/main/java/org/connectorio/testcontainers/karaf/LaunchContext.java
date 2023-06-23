package org.connectorio.testcontainers.karaf;

public interface LaunchContext {

  <X extends Customization> X getCustomization(Class<X> type);

  void add(Customization behavior);

}
