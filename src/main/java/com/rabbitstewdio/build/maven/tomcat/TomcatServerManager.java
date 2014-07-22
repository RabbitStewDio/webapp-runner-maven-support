/**
 * Copyright 2013 Alistair Dutton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabbitstewdio.build.maven.tomcat;

import webapp.runner.launch.TomcatHandle;

public class TomcatServerManager implements ServerManager {
  private final TomcatConfigurator configurator;
  private TomcatHandle serverHandle;

  public TomcatServerManager(TomcatConfigurator configurator) {
    this.configurator = configurator;
  }

  @Override
  public int start() throws Exception {
    start(0);
    return configurator.getPort();
  }

  @Override
  public void start(int port) throws Exception {
    serverHandle = configurator.create(port);
  }

  @Override
  public void stop() throws Exception {
    if (serverHandle != null) {
      serverHandle.close();
      serverHandle = null;
    }
  }

  @Override
  public void join() throws Exception {
    if (serverHandle != null) {
      serverHandle.join();
    }
  }
}

