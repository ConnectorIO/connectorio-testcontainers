package org.connectorio.testcontainers.karaf;

import org.testcontainers.containers.Container;

public interface KarafBasedContainer<SELF extends KarafBaseContainer<SELF>> extends Container<SELF> {

  SELF withCustomization(Customization behavior);

  <X extends Customization> X getCustomization(Class<X> customization);

  String getKarafBase();
  String getKarafHome();
  String getKarafEtc();
  String getKarafData();
  String getKarafLog();

  ExecResult execute(String command);

}
