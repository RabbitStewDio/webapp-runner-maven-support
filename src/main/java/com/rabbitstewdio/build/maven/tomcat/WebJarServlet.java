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
import org.webjars.WebJarAssetLocator;

import java.io.InputStream;
import java.util.SortedMap;
import java.util.regex.Pattern;

import static org.webjars.WebJarAssetLocator.getFullPathIndex;

public class WebJarServlet extends AbstractContentServlet {

  private final WebJarAssetLocator webJarAssetLocator;
  private final ClassLoader projectClassLoader;

  public WebJarServlet(final ClassLoader projectClassLoader, Log log) {
    super(log);
    this.projectClassLoader = projectClassLoader;
    webJarAssetLocator = createWebJarAssetLocator();
  }

  protected InputStream findResource(String resourcePath) {
    try {
      String fullPath = webJarAssetLocator.getFullPath(resourcePath);
      return projectClassLoader.getResourceAsStream(fullPath);
    } catch (Exception ignoreToRespondWith404) {
      return null;
    }
  }

  private WebJarAssetLocator createWebJarAssetLocator() {
    SortedMap<String, String> fullPathIndex = getFullPathIndex(Pattern.compile(".*"), projectClassLoader);
    return new WebJarAssetLocator(fullPathIndex);
  }


}
