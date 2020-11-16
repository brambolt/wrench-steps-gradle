package com.brambolt.wrench.steps

import com.brambolt.gradle.api.artifacts.Artifacts
import com.brambolt.gradle.api.artifacts.Configurations
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

import static com.brambolt.util.Maps.convert

class InjectPublishedProperties extends DefaultTask {

  String groupId

  String artifactId

  String version

  String classifier

  String type

  String dependency

  String configurationName

  Configuration configuration

  String sourcePath

  Object destination

  private Map<String, Object> injectionPoint

  Map destinationProperties

  @Override
  Task configure(Closure closure) {
    super.configure(closure)
    if (null == configuration && (null == configurationName || configurationName.isEmpty()))
      throw new GradleException('No configuration or configuration name provided')
    if (null == configuration)
      configuration = Configurations.getOrCreate(project, configurationName)
    if (null == type || type.isEmpty())
      type = 'jar'
    if (null == dependency || dependency.isEmpty())
      dependency = Artifacts.MapFactory.createDependency([
        group: groupId, artifactId: artifactId, version: version,
        classifier: classifier, type: type])
    project.dependencies.add(configurationName, dependency)
    if (null == destination)
      destination = []
    if (destination instanceof String || destination instanceof GString)
      destination = destination.toString()
    if (destination instanceof String)
      if (destination.isEmpty())
        destination = []
      else
        destination = Arrays.asList(destination.toString().split('\\.'))
    if (!(destination instanceof List))
      throw new GradleException("Expected list or string destination, found: ${destination.toString()}")
    injectionPoint = (destination as List<String>).inject(destinationProperties) { Map node, Object element ->
      String segment = element.toString()
      if (!node.containsKey(segment))
        node[segment] = [:]
      Object value = node[segment]
      if (!(value instanceof Map))
        throw new GradleException("Unable to inject ${destination} into ${destinationProperties}: ${segment}=${value} is not a map")
      value
    }
    this
  }

  @TaskAction
  void apply() {
    configuration.resolve()
    Configurations.requireSingleFile(configuration, configurationName, configurationName)
    File propertiesFile = Files.createTempFile('brambolt', configurationName).toFile()
    propertiesFile.deleteOnExit()
    project.copy {
      from project.zipTree(configuration.singleFile)
      into propertiesFile.parentFile
      include sourcePath
      rename { filename -> propertiesFile.getName() }
    }
    Properties read = new Properties()
    InputStream stream
    try {
      stream = new FileInputStream(propertiesFile)
      read.load(stream)
    } catch (IOException x) {
      throw new GradleException("Unable to read properties from published archive: ${dependency}!${sourcePath}")
    } finally {
      if (null != stream)
        stream.close()
    }
    injectionPoint.putAll(convert(read))
  }
}
