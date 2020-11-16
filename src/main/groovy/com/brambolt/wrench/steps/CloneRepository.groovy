package com.brambolt.wrench.steps

import com.brambolt.gradle.git.CreateClone
import org.gradle.api.Task

class CloneRepository extends RepositoryTask {

  @Override
  Task configure(Closure closure) {
    super.configure(closure)
    doFirst {
      project.logger.quiet("Cloning ${vcs.uri} into ${destinationDir}...")
    }
    this
  }

  @Override
  void fromGit(Map<String, Object> vcs, File destinationDir) {
    project.logger.info("Cloning from ${vcs.uri} to ${destinationDir.absolutePath} (as ${vcs.user}:${vcs.token.substring(0, 4)}...)")
    new CreateClone(vcs, project.logger).apply(destinationDir)
  }
}




