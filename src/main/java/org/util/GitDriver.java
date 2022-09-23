package org.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;

public class GitDriver {
  private Repository repository;
  private Git git;
  private String lastSHA;
  private String initialSHA;
  private ProcessManager processManager;

  public GitDriver(String repositoryGit, ProcessManager processManager) throws IOException {
    this.repository = this.getDefaultRepository(repositoryGit);
    this.git = new Git(this.repository);
    this.lastSHA = getCurrentSHA();
    this.initialSHA = getCurrentSHA();
    this.processManager = processManager;
  }

  public GitDriver(Repository repository, ProcessManager processManager) {
    this.repository = repository;
    this.git = new Git(repository);
    this.lastSHA = getCurrentSHA();
    this.initialSHA = getCurrentSHA();
    this.processManager = processManager;
  }

  public boolean areThereUncommittedChanges() throws InterruptedException {
    try {
      return this.git.status().call().hasUncommittedChanges();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean areThereUntrackedChanges() throws InterruptedException {
    try {
      return this.git.status().call().getUntracked().size() > 0;
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean undoCurrentChanges(String path) throws InterruptedException {
    if (this.areThereUncommittedChanges()) {
      try {
        return processManager.startProcess(path, "git reset --hard", "Git reset hard mode", false, false);
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public boolean checkoutCommit(String path, String sha) {
    try {
      this.lastSHA = getCurrentSHA();
      return processManager.startProcess(path, "git checkout " + sha, "Git checkout", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public boolean checkoutPreviousSHA(String path) {
    try {
      return processManager.startProcess(path, "git checkout " + this.lastSHA, "Git checkout to previous SHA", false,
          false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public String getCurrentSHA() {
    try {
      return this.git.log().setMaxCount(1).call().iterator().next().getName();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getLastSHA() {
    return this.lastSHA;
  }

  public String getInitialSHA() {
    return this.initialSHA;
  }

  public Git getGit() {
    return this.git;
  }

  public Repository getRepository() {
    return this.repository;
  }

  private Repository getDefaultRepository(String projectPath) {
    try {
      String relativeProjectPath = projectPath.split(System.getProperty("user.dir")).length > 1
          ? projectPath.split(System.getProperty("user.dir"))[1]
          : projectPath;
      Repository repository = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), relativeProjectPath);
      if (repository != null) {
        return repository;
      } else {
        return new FileRepositoryBuilder()
            .setGitDir(new File(projectPath + File.separator + ".git"))
            .build();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Repository getMainRepository() throws IOException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.setGitDir(new File(System.getProperty("user.dir") + File.separator + ".git"))
        .readEnvironment()
        .findGitDir()
        .build();
  }

  public boolean addChanges(String path) {
    try {
      return processManager.startProcess(path, "git add .", "Git add all untracked changes in path", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public boolean stashChanges(String path) {
    try {
      return processManager.startProcess(path, "git stash", "Git stash", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public boolean stashPop(String path) {
    try {
      return processManager.startProcess(path, "git stash pop", "Git stash pop", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public boolean resetChanges(String path) {
    try {
      return processManager.startProcess(path, "git reset --mixed", "Git reset --mixed", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public boolean cleanChanges(String path) {
    try {
      return processManager.startProcess(path, "git clean -fx -d", "Git clean -fx -d", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public boolean restoreChanges(String path, String filesToRestore) {
    try {
      return processManager.startProcess(path, "git restore " + filesToRestore, "Git restore files", false, false);
    } catch (IOException | InterruptedException e1) {
      e1.printStackTrace();
    }
    return false;
  }

  public void safeCheckout(String projectPath, String resourcesPath, String commitToCheckout)
      throws InterruptedException {
    addChanges(resourcesPath);
    stashChanges(projectPath);
    checkoutCommit(projectPath, commitToCheckout);
    stashPop(projectPath);
    resetChanges(resourcesPath);
  }
}
