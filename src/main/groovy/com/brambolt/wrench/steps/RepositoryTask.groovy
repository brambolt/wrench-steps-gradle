package com.brambolt.wrench.steps

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

abstract class RepositoryTask extends DefaultTask {

  Map vcs = [:]

  File destinationDir

  @Override
  Task configure(Closure closure) {
    super.configure(closure)
    configureDefaults()
    if (null == destinationDir)
      throw new GradleException("Set destination directory with destinationDir=<file>")
    this
  }

  void configureDefaults() {
    if (!vcs.containsKey('user') && project.rootProject.hasProperty('vcsUser'))
      vcs.user = project.rootProject.vcsUser
    if (!vcs.containsKey('token') && project.rootProject.hasProperty('vcsToken'))
      vcs.token = project.rootProject.vcsToken
  }

  @TaskAction
  def apply() {
    validate(vcs)
    fromVcs((Map) vcs, destinationDir)
  }

  void validate(Map vcs) {
    if (null == vcs)
      throw new GradleException("Version control specification not provided")
    if (vcs.isEmpty())
      throw new GradleException("Empty version control specification")
    if (!vcs.containsKey('type'))
      throw new GradleException("Source control type not specified:${vcs}")
    if (!vcs.containsKey('uri'))
      throw new GradleException("Source control URI not specified: ${vcs}")
    if (!vcs.containsKey('user'))
      throw new GradleException("Source control user name not specified: ${vcs}")
    if (!vcs.containsKey('token') || vcs.token.trim().isEmpty())
      throw new GradleException("Source control token not specified: ${vcs}")
  }

  void fromVcs(Map<String, Object> vcs, File destinationDir) {
    switch (vcs.type) {
      case 'bzr':
        fromUnsupported(vcs)
        break
      case 'git':
        fromGit(vcs, destinationDir)
        break
      case 'hg':
        fromUnsupported(vcs)
        break
      case 'svn':
        fromUnsupported(vcs)
        break
      case 'tfs':
        fromTfs(vcs, destinationDir)
        break
      default:
        throw new GradleException("Unknown source control system type: ${vcs}")
    }
  }

  abstract void fromGit(Map<String, Object> vcs, File destinationDir)

  void fromTfs(Map vcs, File destinationDir) {
    fromGit(vcs, destinationDir) // TFS is git...
  }

  void fromUnsupported(Map vcs) {
    throw new GradleException("VCS type is not yet supported: ${vcs}")
  }
}




