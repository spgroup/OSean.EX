package org.util;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitProjectActions {
  private Repository repository;
  private Git git;

  public GitProjectActions(String repositoryGit) throws IOException {
    this.repository = new FileRepositoryBuilder()
        .setGitDir(new File(repositoryGit+File.separator+".git"))
        .build();
    this.git = new Git(this.repository);
  }

  public GitProjectActions(Repository repository){
    this.repository = repository;
    this.git = new Git(repository);
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

}
