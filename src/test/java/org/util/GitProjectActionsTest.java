package org.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class GitProjectActionsTest {
  public static String projectPath = "src"+File.separator+"test"+File.separator+"resources"+
      File.separator+"toy-project";
  public static String pomPath = projectPath+File.separator+"pom.xml";

  @Test
  public void expectFalseForStatusWithoutUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    Assert.assertFalse(gitProjectActions.areThereUncommittedChanges());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectTrueForStatusWithUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    if (appendTextOnFile(pomPath)){
      Assert.assertTrue(gitProjectActions.areThereUncommittedChanges());
      gitProjectActions.undoCurrentChanges();
    }
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectFalseForUndoingUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    Assert.assertFalse(gitProjectActions.undoCurrentChanges());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectTrueForUndoingUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    if (appendTextOnFile(pomPath)){
      Assert.assertTrue(gitProjectActions.undoCurrentChanges());
    }
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectEqualsValuesForLastCommitSHA() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit("c8dec98");
    Assert.assertFalse(gitProjectActions.undoCurrentChanges());
    Assert.assertEquals("c8dec98410cf141494b9fb26513ba89c689a33c5", gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectNotEqualsForLastCommitSHA() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    Assert.assertNotEquals("c8dec98410cf141494b9fb26513ba89c689a33c55", gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectEqualsForLastCommitShaAfterCheckingOut() throws IOException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit("ab930a716c8f426fee2a45cecf2881de9a514c1c");
    String lastSHA = gitProjectActions.getLastSHA();
    Assert.assertEquals("ab930a716c8f426fee2a45cecf2881de9a514c1c", gitProjectActions.getCurrentSHA());
    gitProjectActions.checkoutPreviousSHA();
    Assert.assertEquals(lastSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectEqualsValueAfterCheckouOnCommit() throws IOException, InterruptedException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit("9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4");
    Assert.assertEquals("9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4", gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }
  
  @Test
  public void expectCheckoutToInitialCommit() throws IOException, InterruptedException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit("main");
    String mainSHA = gitProjectActions.getCurrentSHA();
    gitProjectActions.checkoutCommit("9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4");
    gitProjectActions.checkoutCommit("00407114d1232ece915a3609490128f7beb21691");
    gitProjectActions.checkoutCommit("5215c5d623a131ac94284be5c3c42c2124618e99");
    gitProjectActions.checkoutCommit(gitProjectActions.getInitialSHA());
    Assert.assertEquals(mainSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectUnstagedChangesAfterCommit() throws IOException, InterruptedException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit("main");
    String mainSHA = gitProjectActions.getCurrentSHA();
    
    appendTextOnFile(pomPath);
    gitProjectActions.addChanges();
    gitProjectActions.stashChanges();
    gitProjectActions.checkoutCommit("9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4");
    gitProjectActions.stashPop();
    gitProjectActions.restoreChanges();

    Assert.assertTrue(gitProjectActions.undoCurrentChanges());
    
    gitProjectActions.checkoutCommit(gitProjectActions.getInitialSHA());
    Assert.assertEquals(mainSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void expectNoneUnstagedChangesAfterCleanCommand() throws IOException, InterruptedException {
    GitProjectActions gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit("main");
    String mainSHA = gitProjectActions.getCurrentSHA();
    
    Files.createFile(Paths.get(projectPath+File.separator+"test.xml"));
    Assert.assertTrue(gitProjectActions.areThereUntrackedChanges());
    gitProjectActions.cleanChanges();
    Assert.assertFalse(gitProjectActions.areThereUntrackedChanges());

    gitProjectActions.checkoutCommit(gitProjectActions.getInitialSHA());
    Assert.assertEquals(mainSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @NotNull
  private GitProjectActions getGitProjectActions() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    return new GitProjectActions(subRepo);
  }

  public static Repository getMainRepository() throws IOException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.setGitDir(new File(System.getProperty("user.dir")+File.separator+".git"))
        .readEnvironment()
        .findGitDir()
        .build();
  }

  public static GitProjectActions getGitProjectActionsAndChangeCommit(String repoPath, String commitHash) throws IOException{
      GitProjectActions gitProjectActions = new GitProjectActions(repoPath);
      gitProjectActions.checkoutCommit(commitHash);
      return gitProjectActions;
  }

  private boolean appendTextOnFile(String fileName){
    try {
      Files.write(Paths.get(fileName), "new text".getBytes(), StandardOpenOption.APPEND);
      return true;
    }catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

}
