package com.rabbitstewdio.build.maven.tomcat;

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public abstract class AbstractTomcatConfiguration implements TomcatConfiguration {
  private MavenProject mavenProject;
  private Properties properties;

  protected AbstractTomcatConfiguration(MavenProject mavenProject, Properties properties) {
    this.mavenProject = mavenProject;
    this.properties = properties;
  }

  @Override
  public File getBasedir() {
    return this.mavenProject.getBasedir();
  }

  @Override
  public ClassLoader getProjectClassLoader() {
    return new ProjectClassLoaderFactory(mavenProject.getArtifacts()).create();
  }

  @Override
  public List<Context> getContexts() {
    return Collections.emptyList();
  }

  @Override
  public Properties getConfiguration() {
    return properties;
  }
}
