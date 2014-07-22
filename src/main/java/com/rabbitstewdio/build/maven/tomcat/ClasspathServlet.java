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

import org.apache.maven.plugin.logging.Log;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class ClasspathServlet extends AbstractContentServlet {
  private final ClassLoader projectClassLoader;

  public ClasspathServlet(ClassLoader projectClassLoader, Log log) {
    super(log);
    this.projectClassLoader = projectClassLoader;

    if (log.isDebugEnabled()) {
      log.debug("Begin Logging Classloader for " + projectClassLoader);
      ClassLoader cl = projectClassLoader;
      while (cl.getParent() != null && cl.getParent() != cl) {
        if (cl instanceof URLClassLoader) {
          URLClassLoader ucl = (URLClassLoader) cl;
          for (URL url : ucl.getURLs()) {
            log.info("  => " + url);
          }
        }
        log.debug("-> " + cl);
        cl = cl.getParent();
      }
      log.debug("Finished Logging Classloader for " + projectClassLoader);
    }
  }

  @Override
  protected InputStream findResource(String path) {
    if (path.startsWith("/")) {
      return projectClassLoader.getResourceAsStream(path.substring(1));
    }
    return projectClassLoader.getResourceAsStream(path);
  }
}
