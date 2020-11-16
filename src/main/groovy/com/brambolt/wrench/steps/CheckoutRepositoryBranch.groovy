package com.brambolt.wrench.steps

import com.brambolt.gradle.git.CheckoutBranch

/**
 * Checks out a specific branch of a repository.
 *
 * This code executes during Calypso workspace creation, when the workspace is
 * being created from a specific branch. In this case, the resulting workspace
 * will have the latest revision on that extensions branch. This revision may
 * be later than the extensions revision available when the client repository
 * was built.
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
 * A later install from version <code>15.2.0.28-2018.11.23-123</code> will not
 * use the extensions commit hash <code>abcd</code> available when the build
 * was created. Instead the current head of the extensions branch will be
 * installed.
 *
 * This behavior is appropriate for development but not for testing a specific
 * version.
 */
class CheckoutRepositoryBranch extends RepositoryTask {

  boolean createBranch = false

  @Override
  void fromGit(Map<String, Object> vcs, File destinationDir) {
    project.logger.info("Cloning from ${vcs.uri} to ${destinationDir.absolutePath} (as ${vcs.user}:${vcs.token.substring(0, 8)}...")
    new CheckoutBranch(vcs, project.logger).apply(destinationDir)
  }
}




