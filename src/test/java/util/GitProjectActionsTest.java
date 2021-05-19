package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitProjectActions;

public class GitProjectActionsTest {
  public static String projectPath = "src"+File.separator+"test"+File.separator+"resources"+
      File.separator+"toy-project";
  public static String pomPath = projectPath+File.separator+"pom.xml";

  public static Repository getMainRepository() throws IOException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.setGitDir(new File(System.getProperty("user.dir")+File.separator+".git"))
        .readEnvironment()
        .findGitDir()
        .build();
  }
  @Test
  public void expectFalseForStatusWithoutUncommittedChanges() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertFalse(gitProjectActions.areThereUncommittedChanges());
  }

  @Test
  public void expectTrueForStatusWithoutUncommittedChanges() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    if (appendTextOnFile(pomPath)){
      Assert.assertTrue(gitProjectActions.areThereUncommittedChanges());
      gitProjectActions.undoCurrentChanges();
    }
  }

  @Test
  public void expectTrueForUndoingUncommittedChanges() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    if (appendTextOnFile(pomPath)){
      Assert.assertTrue(gitProjectActions.undoCurrentChanges());
    }
  }

  @Test
  public void expectFalseForUndoingUncommittedChanges() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertFalse(gitProjectActions.undoCurrentChanges());
  }

  @Test
  public void expectTrueForLastCommitSHA() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    gitProjectActions.checkoutCommit("c8dec98");
    Assert.assertFalse(gitProjectActions.undoCurrentChanges());
    Assert.assertEquals("c8dec98410cf141494b9fb26513ba89c689a33c5", gitProjectActions.getCurrentSHA());
    gitProjectActions.checkoutPreviousSHA();
  }

  @Test
  public void expectFalseForLastCommitSHA() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertNotEquals("c8dec98410cf141494b9fb26513ba89c689a33c55", gitProjectActions.getCurrentSHA());
    gitProjectActions.checkoutPreviousSHA();
  }

  @Test
  public void expectTrueForLastCommitSHAAfterCheckingOut() throws IOException {
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    gitProjectActions.checkoutCommit("ab930a716c8f426fee2a45cecf2881de9a514c1c");
    String lastSHA = gitProjectActions.getLastSHA();
    Assert.assertEquals("ab930a716c8f426fee2a45cecf2881de9a514c1c", gitProjectActions.getCurrentSHA());
    gitProjectActions.checkoutPreviousSHA();
    Assert.assertEquals(lastSHA, gitProjectActions.getCurrentSHA());
    gitProjectActions.checkoutPreviousSHA();
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
