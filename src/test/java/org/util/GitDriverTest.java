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

public class GitDriverTest {
  public static String projectPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator
      + "toy-project";
  public static String pomPath = projectPath + File.separator + "pom.xml";
  protected static final ProcessManager PROCESS_MANAGER = new ProcessManager(20);

  @Test
  public void expectFalseForStatusWithoutUncommittedChanges() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    Assert.assertFalse(gitProjectActions.areThereUncommittedChanges());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectTrueForStatusWithUncommittedChanges() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    if (appendTextOnFile(pomPath)) {
      Assert.assertTrue(gitProjectActions.areThereUncommittedChanges());
      gitProjectActions.undoCurrentChanges(projectPath);
    }
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectFalseForUndoingUncommittedChanges() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    Assert.assertFalse(gitProjectActions.undoCurrentChanges(projectPath));
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectTrueForUndoingUncommittedChanges() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    if (appendTextOnFile(pomPath)) {
      Assert.assertTrue(gitProjectActions.undoCurrentChanges(projectPath));
    }
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectEqualsValuesForLastCommitSHA() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit(projectPath, "c8dec98");
    Assert.assertFalse(gitProjectActions.undoCurrentChanges(projectPath));
    Assert.assertEquals("c8dec98410cf141494b9fb26513ba89c689a33c5", gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectNotEqualsForLastCommitSHA() throws IOException {
    GitDriver gitProjectActions = getGitProjectActions();
    Assert.assertNotEquals("c8dec98410cf141494b9fb26513ba89c689a33c55", gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectEqualsForLastCommitShaAfterCheckingOut() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit(projectPath, "ab930a716c8f426fee2a45cecf2881de9a514c1c");
    String lastSHA = gitProjectActions.getLastSHA();
    Assert.assertEquals("ab930a716c8f426fee2a45cecf2881de9a514c1c", gitProjectActions.getCurrentSHA());
    gitProjectActions.checkoutPreviousSHA(projectPath);
    Assert.assertEquals(lastSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectEqualsValueAfterCheckouOnCommit() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit(projectPath, "9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4");
    Assert.assertEquals("9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4", gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectCheckoutToInitialCommit() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit(projectPath, "main");
    String mainSHA = gitProjectActions.getCurrentSHA();
    gitProjectActions.checkoutCommit(projectPath, "9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4");
    gitProjectActions.checkoutCommit(projectPath, "00407114d1232ece915a3609490128f7beb21691");
    gitProjectActions.checkoutCommit(projectPath, "5215c5d623a131ac94284be5c3c42c2124618e99");
    gitProjectActions.checkoutCommit(projectPath, gitProjectActions.getInitialSHA());
    Assert.assertEquals(mainSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectUnstagedChangesAfterCommit() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit(projectPath, "main");
    String mainSHA = gitProjectActions.getCurrentSHA();

    appendTextOnFile(pomPath);
    gitProjectActions.safeCheckout(projectPath, projectPath, "9d9b8d1bf5bee49cdded16fa0619730bc0ccd3a4");

    Assert.assertTrue(gitProjectActions.undoCurrentChanges(projectPath));

    gitProjectActions.checkoutCommit(projectPath, gitProjectActions.getInitialSHA());
    Assert.assertEquals(mainSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @Test
  public void expectNoneUnstagedChangesAfterCleanCommand() throws IOException, InterruptedException {
    GitDriver gitProjectActions = getGitProjectActions();
    gitProjectActions.checkoutCommit(projectPath, "main");
    String mainSHA = gitProjectActions.getCurrentSHA();

    Files.createFile(Paths.get(projectPath + File.separator + "test.xml"));
    Assert.assertTrue(gitProjectActions.areThereUntrackedChanges());
    gitProjectActions.cleanChanges(projectPath);
    Assert.assertFalse(gitProjectActions.areThereUntrackedChanges());

    gitProjectActions.checkoutCommit(projectPath, gitProjectActions.getInitialSHA());
    Assert.assertEquals(mainSHA, gitProjectActions.getCurrentSHA());
    Assert.assertTrue(gitProjectActions.checkoutCommit(projectPath, "main"));
  }

  @NotNull
  private GitDriver getGitProjectActions() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    return new GitDriver(subRepo, PROCESS_MANAGER);
  }

  public static Repository getMainRepository() throws IOException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.setGitDir(new File(System.getProperty("user.dir") + File.separator + ".git"))
        .readEnvironment()
        .findGitDir()
        .build();
  }

  public static GitDriver getGitProjectActionsAndChangeCommit(String repoPath, String commitHash,
      ProcessManager processManager) throws IOException {
    GitDriver gitProjectActions = new GitDriver(repoPath, processManager);
    gitProjectActions.checkoutCommit(repoPath, commitHash);
    return gitProjectActions;
  }

  private boolean appendTextOnFile(String fileName) {
    try {
      Files.write(Paths.get(fileName), "new text".getBytes(), StandardOpenOption.APPEND);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

}
