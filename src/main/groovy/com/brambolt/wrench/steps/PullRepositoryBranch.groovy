package com.brambolt.wrench.steps

import com.brambolt.gradle.git.PullBranch

class PullRepositoryBranch extends RepositoryTask {

  @Override
  void fromGit(Map<String, Object> vcs, File destinationDir) {
    project.logger.info("Pulling customization updates from ${vcs.uri} to ${destinationDir.absolutePath} (as ${vcs.user}:${vcs.token.substring(0, 8)}...")
    new PullBranch(vcs, project.logger).apply(destinationDir)
  }
}




