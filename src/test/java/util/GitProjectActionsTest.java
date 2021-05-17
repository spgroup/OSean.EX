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
  private static String projectPath = "src"+File.separator+"test"+File.separator+"resources"+
      File.separator+"toy-project";
  private static String pomPath = projectPath+File.separator+"pom.xml";

  private Repository getMainRepository() throws IOException {
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
