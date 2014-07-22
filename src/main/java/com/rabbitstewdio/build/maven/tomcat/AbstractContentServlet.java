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

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public abstract class AbstractContentServlet extends HttpServlet {
  protected static final String LAST_MODIFIED = "Last-Modified";
  private final Log log;

  public AbstractContentServlet(Log log) {
    this.log = log;
  }

  protected abstract InputStream findResource(String path);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String filename = request.getPathInfo();

    InputStream in = findResource(filename);
    if (in == null) {
      log.warn("Unable to find [" + filename + "] on " + getServletName());
      response.setStatus(404);
      return;
    }

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    IOUtils.copy(in, bout);

    response.setCharacterEncoding("UTF-8");

    String mimeType = getServletContext().getMimeType(filename);
    if (mimeType != null) {
      response.setContentType(mimeType);
    }

    response.addDateHeader("Expires", 0L);
    response.setDateHeader(LAST_MODIFIED, new Date().getTime());
    response.setContentLength(bout.size());
    response.getOutputStream().write(bout.toByteArray());
    response.flushBuffer();
  }
}
