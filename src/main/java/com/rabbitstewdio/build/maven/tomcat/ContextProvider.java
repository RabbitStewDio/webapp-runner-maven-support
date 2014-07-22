package com.rabbitstewdio.build.maven.tomcat;

import org.apache.maven.plugin.logging.Log;

public interface ContextProvider {

  public void onStartUp(TomcatConfiguration config, Log log);

  public void onShutDown(TomcatConfiguration config, Log log);
}
