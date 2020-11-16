package com.brambolt.wrench.steps

import com.brambolt.gradle.api.artifacts.Artifacts
import com.brambolt.gradle.api.artifacts.Configurations
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

class InstallPublishedArchive extends DefaultTask {

  String groupId

  String artifactId

  String version

  String classifier

  String type

  Object dependency

  String configurationName

  Configuration configuration

  Object destinationDir

  boolean expand = true

  boolean preserve = true

  List<String> inclusions = [ '**/*']

  List<String> exclusions = []

  @Override
  Task configure(Closure closure) {
    super.configure(closure)
    if (null == configuration && (null == configurationName || configurationName.isEmpty()))
      // This must be a single file configuration so we need to be sure we
      // avoid a configuration name collision; using the task name seems fairly
      // safe, if the configuration name is not set explicitly?
      configurationName = name
    if (null == configuration)
      configuration = Configurations.getOrCreate(project, configurationName)
    configureDependency()
    project.dependencies.add(configurationName, dependency)
    this
  }

  void configureDependency() {
    switch (dependency) {
      case null:
      case { it instanceof String && it.trim().isEmpty() }:
        dependency = createDependencyFromProperties()
        break
      case { it instanceof Map }:
        dependency = createDependencyFromMap(dependency as Map)
        break
    }
    if (null == dependency || dependency.isEmpty())
      dependency = createDependencyFromProperties()
    if (null == dependency)
      throw new GradleException("Unable to configure dependency: ${groupId} ${artifactId} ${version} ${type} ${classifier}")
  }

  String createDependencyFromProperties() {
    createDependencyFromMap([
      group: groupId, artifactId: artifactId, version: version,
      classifier: classifier, type: type])
  }

  String createDependencyFromMap(Map properties) {
    Artifacts.MapFactory.createDependency(properties)
  }

  @TaskAction
  void apply() {
    // Resolve the configuration here, and not sooner - if it is done during
    // the configuration phase then the sequence will crash whenever the resource
    // is not available during the configuration phase, which is frequent.
    configuration.resolve()
    Configurations.requireSingleFile(configuration, configurationName, configurationName)
    if (destinationDir instanceof File)
      apply(destinationDir as File)
    else if (destinationDir instanceof Collection)
      apply(destinationDir as Collection)
    else
      throw new GradleException("Unexpected value for destination directory: ${destinationDir}")
  }

  void apply(File dir) {
    if (!preserve && dir.exists())
      project.delete(dir)
    dir.mkdirs()
    project.copy {
      // If expand then zip or tar tree, else just copy the file:
      from getSource()
      // ... into the destination directory...
      into dir
      inclusions.each { include it }
      exclusions.each { exclude it }
      rename { String filename -> renameOutput(filename) }
    }
  }

  Object getSource() {
    expand ? getTree() : configuration.singleFile
  }

  FileTree getTree() {
    if ('tgz' == type || 'tar.gz' == type)
      project.tarTree(project.resources.gzip(configuration.singleFile))
    else project.zipTree(configuration.singleFile)
  }

  void apply(Collection files) {
    files.each { apply(it as File) }
  }

  String renameOutput(String fileName) {
    fileName // Do nothing, by default
  }

  /**
   * This code was stashed here but belongs in a <code>Configurations</code>
   * utility class.
   *
   * @param project The project to create the configuration for
   * @param configurationName The name of the configuration
   * @param dependency The dependency to add to the configuration
   * @return The new configuration
   */
  static Configuration createConfiguration(
    Project project, String configurationName, String dependency) {
    // The following code is a bit odd because it appears that we're running
    // through here multiple times - so we have to make sure we only create and
    // resolve the configuration exactly once:
    Configuration configuration = null
    try {
      // Try to get an existing configuration:
      configuration = project.configurations.getByName(configurationName)
    } catch (UnknownConfigurationException ignored) {
      // No configuration found - fall through!
    }
    if (null == configuration) {
      // No configuration found, create, add dependency, resolve:
      configuration = project.configurations.create(configurationName)
      project.dependencies.add(configurationName, dependency)
    }
    // If the configuration existed previously, then the dependency is not
    // added? Is this intentional? Or just a bug?
    configuration
  }
}
