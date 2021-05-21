package org.util;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;

public class GitProjectActions {
  private Repository repository;
  private Git git;
  private String lastSHA;

  public GitProjectActions(String repositoryGit) throws IOException {
    this.repository = this.getDefaultRepository(repositoryGit);
    this.git = new Git(this.repository);
    this.lastSHA = getCurrentSHA();
  }

  public GitProjectActions(Repository repository){
    this.repository = repository;
    this.git = new Git(repository);
    this.lastSHA = getCurrentSHA();
  }

  public boolean undoCurrentChanges() {
    if (this.areThereUncommittedChanges()) {
      try {
        this.git.reset().setMode(ResetType.HARD).call();
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public boolean areThereUncommittedChanges(){
    try {
      return this.git.status().call().hasUncommittedChanges();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean checkoutCommit(String sha){
    try {
      this.lastSHA = getCurrentSHA();
      this.git.checkout().setName(sha).call();
      return true;
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean checkoutPreviousSHA(){
    try {
      this.git.checkout().setName(this.lastSHA).call();
      return true;
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return false;
  }

  public String getCurrentSHA(){
    try {
      return this.git.log().setMaxCount(1).call().iterator().next().getName();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getLastSHA(){
    return this.lastSHA;
  }

  public Git getGit(){
    return this.git;
  }

  public Repository getRepository(){
    return this.repository;
  }

  private Repository getDefaultRepository(String projectPath){
    try {
      String relativeProjectPath = projectPath.split(System.getProperty("user.dir")).length > 1 ? projectPath.split(System.getProperty("user.dir"))[1] : projectPath;
      Repository repository = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), relativeProjectPath);
      if (repository != null){
        return repository;
      }else{
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
    return builder.setGitDir(new File(System.getProperty("user.dir")+File.separator+".git"))
        .readEnvironment()
        .findGitDir()
        .build();
  }

}

