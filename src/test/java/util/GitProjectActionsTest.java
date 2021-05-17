package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitProjectActions;

public class GitProjectActionsTest {

  @Test
  public void expectFalseForStatusWithoutUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = new GitProjectActions("src/test/resources/project");
    Assert.assertFalse(gitProjectActions.areThereUncommittedChanges());
  }

  @Test
  public void expectTrueForStatusWithoutUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = new GitProjectActions("src/test/resources/project");
    if (appendTextOnFile("src/test/resources/project/pom.xml")){
      Assert.assertTrue(gitProjectActions.areThereUncommittedChanges());
      gitProjectActions.undoCurrentChanges();
    }else{
      Assert.assertFalse(gitProjectActions.areThereUncommittedChanges());
    }
  }

  @Test
  public void expectTrueForUndoingUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = new GitProjectActions("src/test/resources/project");
    if (appendTextOnFile("src/test/resources/project/pom.xml")){
      Assert.assertTrue(gitProjectActions.undoCurrentChanges());
    }
  }

  @Test
  public void expectFalseForUndoingUncommittedChanges() throws IOException {
    GitProjectActions gitProjectActions = new GitProjectActions("src/test/resources/project");
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
