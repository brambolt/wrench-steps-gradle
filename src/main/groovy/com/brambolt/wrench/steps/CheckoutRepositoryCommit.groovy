package com.brambolt.wrench.steps

import com.brambolt.gradle.git.CheckoutCommit

/**
 * Checks out a specific commit in the custom extensions repository.
 *
 * This code executes during Calypso workspace creation, when the workspace is
 * being created from a specific commit. This is the normal use case.
 *
 * For example, a Jenkins build of a client repository could involve the
 * following parameters:
 * <pre>
 *   client repository branch:    release/15.2.0.28-2018.11.23
 *   extensions submodule branch: release/15.2.0.28-2018.11.23
 *   client commit hash:          1234
 *   extensions commit hash:      abcd
 *   produced version:            15.2.0.28-2018.11.23-123
 * </pre>
 *
 * Any later install will be built from the commit hash <code>abcd</code>.
 *
 * @see CheckoutRepositoryBranch
 */
class CheckoutRepositoryCommit extends RepositoryTask {

  @Override
  void fromGit(Map<String, Object> vcs, File destinationDir) {
    project.logger.info("Cloning from ${vcs.uri} to ${destinationDir.absolutePath} (as ${vcs.user}:${vcs.token.substring(0, 8)}...")
    new CheckoutCommit(vcs, project.logger).apply(destinationDir)
  }
}




