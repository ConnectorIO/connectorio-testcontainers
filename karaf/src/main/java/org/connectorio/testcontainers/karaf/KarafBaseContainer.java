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

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.connectorio.testcontainers.karaf.context.BasicLaunchContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public abstract class KarafBaseContainer<T extends KarafBaseContainer<T>> extends GenericContainer<T>
  implements KarafBasedContainer<T> {

  protected final BasicLaunchContext context = new BasicLaunchContext();

  private final Map<String, String> loggers = new LinkedHashMap<>();

  private String deployDirectory;

  protected KarafBaseContainer(DockerImageName image) {
    super(image);
  }

  @Override
  public T withCustomization(Customization behavior) {
    this.context.add(behavior);
    return (T) this;
  }

  @Override
  public <X extends Customization> X getCustomization(Class<X> customization) {
    return context.getCustomization(customization);
  }

  public void withLoggerLevel(String logger, String level) {
    this.loggers.put(logger, level);
  }

  public void setKarafBase(String karafBase) {
    this.withEnv("KARAF_BASE", karafBase);
  }

  public String getKarafBase() {
    return getEnvMap().getOrDefault("KARAF_BASE", "/opt/apache-karaf/");
  }

  public void setKarafHome(String karafHome) {
    this.withEnv("KARAF_HOME", karafHome);
  }

  public String getKarafHome() {
    return getEnvMap().getOrDefault("KARAF_HOME", "/opt/apache-karaf/");
  }

  public void setKarafEtc(String karafEtc) {
    this.withEnv("KARAF_ETC", karafEtc);
  }

  public String getKarafEtc() {
    return getEnvMap().getOrDefault("KARAF_ETC", "/opt/apache-karaf/etc/");
  }

  public void setKarafData(String karafData) {
    this.withEnv("KARAF_DATA", karafData);
  }

  public String getKarafData() {
    return getEnvMap().getOrDefault("KARAF_DATA", "/opt/apache-karaf/data/");
  }

  public void setKarafLog(String karafLog) {
    this.withEnv("KARAF_LOG", karafLog);
  }

  public String getKarafLog() {
    return getEnvMap().getOrDefault("KARAF_LOG", "/opt/apache-karaf/data/log/");
  }

  public void setDeployDirectory(String deployDirectory) {
    this.deployDirectory = deployDirectory;
  }

  @Override
  protected void containerIsStarting(InspectContainerResponse containerInfo) {
    context.starting(containerInfo, this);
  }

  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {
    context.started(containerInfo, this);
  }

  public T withDebug(int port) {
    withEnv("KARAF_DEBUG", "1");
    withEnv("JAVA_DEBUG_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:" + port);
    getPortBindings().add("5005:5005");
    return (T) this;
  }

  public String getDeployDirectory() {
    return Optional.ofNullable(deployDirectory)
      .orElse(getKarafHome() + "/deploy");
  }

  public void deployKar(Transferable kar, String name) {
    copyFileToContainer(kar, getDeployDirectory() + "/" + name);
  }

  public void deployKar(MountableFile file) {
    String resolvedPath = file.getResolvedPath();
    String name = resolvedPath.substring(resolvedPath.lastIndexOf('/') + 1);
    copyFileToContainer(file, getDeployDirectory() + "/" + name);
  }

  public ExecResult execute(String command) {
    try {
      ExecResult result = execInContainer("sh", "-c", command);
      logger().info("command executed: {}; exit code: {}; stdout: {}; stderr: {}", command, result.getExitCode(), result.getStdout(), result.getStderr());
      return result;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void start() {
    try (InputStream stream = getClass().getResourceAsStream("/org.ops4j.pax.logging.cfg")) {
      if (stream != null) {
        String paxLogging = new String(stream.readAllBytes());

        for (Entry<String, String> logger : loggers.entrySet()) {
          String code = logger.getKey().replace("", "");
          paxLogging += "\nlog4j2.logger." + code + ".name=" + logger.getKey();
          paxLogging += "\nlog4j2.logger." + code + ".level=" + logger.getValue();
        }

        String path = getKarafEtc();
        withCopyToContainer(Transferable.of(paxLogging.getBytes()), path + "/org.ops4j.pax.logging.cfg");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    super.start();
  }

}
