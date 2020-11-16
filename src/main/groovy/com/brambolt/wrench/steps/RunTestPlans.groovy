package com.brambolt.wrench.steps

import com.brambolt.wrench.target.Host
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

class RunTestPlans extends DefaultTask implements WithEnvironment, WithHost {

  Host host

  String version

  // The JUnit version should not be hardcoded here!
  String consoleLauncher = 'org.junit.platform:junit-platform-console-standalone:1.6.2'

  String configurationName = 'planDependencies'

  Configuration planDependencies

  String planDependency

  String planClass

  String planMethod

  String details = 'tree'

  File reportsDir

  @Override
  Task configure(Closure closure) {
    super.configure(closure)
    group = "${environment.name} Environment"
    description = "Runs the test plans in version ${version}."
    onlyIf { environment.isTestingEnabled() }
    planDependencies = getOrCreateConfiguration()
    if (null == reportsDir)
      reportsDir = new File(project.buildDir, 'test-results')
    if (null == planDependency)
      throw new GradleException('Set the planDependency task property')
    project.dependencies.add(planDependencies.name, planDependency)
    this
  }

  Configuration getOrCreateConfiguration() {
    Configuration existing = project.configurations.findByName(configurationName)
    Configuration configuration = ((null != existing)
      ? existing
      : project.configurations.create(configurationName))
    project.dependencies.add(configuration.name, consoleLauncher)
    configuration
  }

  @TaskAction
  void apply() {
    project.logger.quiet("Running test plans...")
    project.logger.quiet(
      "Class path: ${planDependencies.files.collect { it.absolutePath }.sort().join('\n')}")
    ExecResult result = project.javaexec {
      workingDir project.projectDir
      main 'org.junit.platform.console.ConsoleLauncher'
      classpath = planDependencies
      args getArgsList()
      jvmArgs getJvmArgsList()
      delegate.environment([
        JAVA_HOME: System.getProperty('java.home')
      ])
    }
    result.assertNormalExitValue()
  }

  List<String> getArgsList() {
    [
      '--select-method', "${planClass}#${planMethod}",
      "--details=${details}",
      "--reports-dir=${reportsDir.absolutePath}"
    ]
  }

  List<String> getJvmArgsList() {
    [
      '-Djava.awt.headless=true',
      '-Dcom.brambolt.test.steps.useLongDisplayNames=true',
      "-Dcom.brambolt.environment.id=${environment.id}",
      "-Dcom.brambolt.environment.name=${environment.name}",
      "-Dcom.brambolt.host.name=${host.name}"
    ]
  }
}