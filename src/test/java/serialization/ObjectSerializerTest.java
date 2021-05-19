package serialization;

import static util.GitProjectActionsTest.getMainRepository;

import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.RunSerialization;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.junit.Assert;
import org.junit.Test;
import org.serialization.ObjectSerializer;
import org.util.GitProjectActions;
import util.GitProjectActionsTest;

public class ObjectSerializerTest {

  @Test
  public void expectJarGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    deleteOldJar();
    ObjectSerializer objectSerializer = new ObjectSerializer();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "getName",
        "toy-project",
        };
    objectSerializer.startSerialization(args[0], args[1], args[2], args[3]);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src"+
        File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"+
        File.separator+"toy-project"+File.separator+"85077377978f98e31e637c121b5987e01725f5fd.jar").exists());
  }

  @Test
  public void expectJarGenerationUsingDefaultCall() throws IOException, InterruptedException, TransformerException {
    deleteOldJar();
    RunSerialization runSerialization = new RunSerialization();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "getName",
        "toy-project",
    };
    runSerialization.runAnalysis(args);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src"+
        File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"+
        File.separator+"toy-project"+File.separator+"85077377978f98e31e637c121b5987e01725f5fd.jar").exists());
  }

  private void deleteOldJar(){
    new File(System.getProperty("user.dir")+File.separator+"src"+
        File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"+
        File.separator+"toy-project"+File.separator+"85077377978f98e31e637c121b5987e01725f5fd.jar").delete();
  }
}
