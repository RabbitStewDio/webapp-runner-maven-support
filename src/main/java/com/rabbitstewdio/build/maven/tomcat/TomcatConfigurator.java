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

import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.maven.plugin.logging.Log;
import webapp.runner.launch.AsyncTomcatResource;
import webapp.runner.launch.CommandLineParams;
import webapp.runner.launch.TomcatHandle;
import webapp.runner.launch.helper.ContextDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TomcatConfigurator extends AsyncTomcatResource {
  private final TomcatConfiguration configuration;
  private Log log;
  private int port;

  public TomcatConfigurator(TomcatConfiguration configuration, Log log) {
    this.configuration = configuration;
    this.log = log;
  }

  public TomcatHandle create(int port) throws Exception {
    CommandLineParams params = new CommandLineParams();
    params.port = port;
    params.enableNaming = true;
    params.tomcatUsersLocation =
            configuration.getConfiguration().getProperty("userDatabaseLocation", params.tomcatUsersLocation);
    params.uriEncoding =
            configuration.getConfiguration().getProperty("uriEncoding", params.uriEncoding);
    params.enableCompression = Boolean.parseBoolean
            (configuration.getConfiguration().getProperty("enableCompression", String.valueOf(params.enableCompression)));
    params.enableSSL = Boolean.parseBoolean
            (configuration.getConfiguration().getProperty("enableSSL", String.valueOf(params.enableSSL)));
    params.enableNaming = Boolean.parseBoolean
            (configuration.getConfiguration().getProperty("enableNaming", String.valueOf(params.enableNaming)));
    params.enableClientAuth = Boolean.parseBoolean
            (configuration.getConfiguration().getProperty("enableClientAuth", String.valueOf(params.enableClientAuth)));
    params.enableBasicAuth = Boolean.parseBoolean
            (configuration.getConfiguration().getProperty("enableBasicAuth", String.valueOf(params.enableBasicAuth)));
    params.expandWar = Boolean.parseBoolean
            (configuration.getConfiguration().getProperty("expandWar", String.valueOf(params.expandWar)));
    params.sessionTimeout = Integer.parseInt
            (configuration.getConfiguration().getProperty("sessionTimeout", String.valueOf(params.sessionTimeout)));
    params.compressableMimeTypes =
            configuration.getConfiguration().getProperty("compressableMimeTypes", params.compressableMimeTypes);
    ContextDefinition[] contextDefinition = configure();
    return startServer(params, contextDefinition);
  }

  public int getPort() {
    return port;
  }

  protected ContextDefinition[] configure() {
    List<Context> contexts = configuration.getContexts();
    ArrayList<ContextDefinition> defs = new ArrayList<ContextDefinition>();
    for (Context context : contexts) {
      String contextRoot = prependIfMissing(context.getContextRoot(), "/");
      defs.add(new ContextDefinition(context.getDirectory(), contextRoot, context.getContextXml(), context.getProperties()));
    }
    return defs.toArray(new ContextDefinition[defs.size()]);
  }

  protected String prependIfMissing(String text, String prefix) {
    if (text.startsWith(prefix)) {
      return text;
    }
    return prefix + text;
  }

  protected String getDocBase() {
    return configuration.getConfiguration().getProperty("docBase", configuration.getBasedir().getAbsolutePath());
  }

  protected StandardContext createStaticContext() throws IOException {
    StandardContext ctx = new StandardContext();
    ctx.setName("ROOT");
    ctx.setPath("");
    ctx.setDocBase(getDocBase());
    ctx.addLifecycleListener(new Tomcat.DefaultWebXmlListener());

    ClassLoader projectClassLoader = configuration.getProjectClassLoader();
    ctx.setParentClassLoader(projectClassLoader);

    ContextConfig config = new ContextConfig();
    config.setDefaultWebXml(Constants.NoDefaultWebXml);
    ctx.addLifecycleListener(config);

    Wrapper servlet = Tomcat.addServlet(ctx, "defaultContent", DefaultServlet.class.getName());
    servlet.addInitParameter("readonly", "true");
    servlet.setLoadOnStartup(1);
    servlet.setOverridable(true);

    Wrapper webjar = Tomcat.addServlet(ctx, "webjar", new WebJarServlet(projectClassLoader, getLog()));
    webjar.setLoadOnStartup(1);

    Wrapper cp = Tomcat.addServlet(ctx, "classpath", new ClasspathServlet(projectClassLoader, getLog()));
    cp.setLoadOnStartup(1);

    ctx.addServletMapping("/", "defaultContent");
    ctx.addServletMapping("/webjars/*", "webjar");
    ctx.addServletMapping("/classpath/*", "classpath");
    return ctx;
  }

  public Log getLog() {
    return log;
  }

  @Override
  public void configure(Tomcat tomcat, CommandLineParams commandLineParams,
                        ContextDefinition... warLocations) {
    try {
      if ("true".equals(configuration.getConfiguration().getProperty("configureDefaultRoot", "true"))) {
        tomcat.getHost().addChild(createStaticContext());
      }
      tomcat.getHost().setParentClassLoader(configuration.getProjectClassLoader());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void configureContext(Tomcat tomcat, CommandLineParams commandLineParams,
                               ContextDefinition warLocation, org.apache.catalina.Context context) {
    context.setParentClassLoader(configuration.getProjectClassLoader());
  }

  @Override
  protected void assertServerUp(Tomcat tomcat) {
    super.assertServerUp(tomcat);
    this.port = tomcat.getConnector().getLocalPort();
  }
}
