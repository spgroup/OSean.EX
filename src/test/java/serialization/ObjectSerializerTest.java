package serialization;

import static util.GitProjectActionsTest.getMainRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.RunSerialization;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.junit.Assert;
import org.junit.Test;
import org.serialization.ObjectSerializer;
import org.util.GitProjectActions;
import org.util.InputHandler;
import org.util.input.MergeScenarioUnderAnalysis;
import util.GitProjectActionsTest;

public class ObjectSerializerTest {
  private String directoryForGeneratedJars = System.getProperty("user.dir")+File.separator+"src"+
      File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"+
      File.separator+"toy-project";

  @Test
  public void expectJarGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    ObjectSerializer objectSerializer = new ObjectSerializer();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "getOccupation",
        "toy-project",
        "true",
        "true",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
        };
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses = InputHandler.splitInputInMergeScenarios(args);
    objectSerializer.startSerialization(mergeScenarioUnderAnalyses);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"7810b85dd711ac2648675dcfe5e65539aec1ea1d.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationForToyProjectWithPartialTransformations() throws IOException, InterruptedException, TransformerException {
    ObjectSerializer objectSerializer = new ObjectSerializer();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "getOccupation",
        "toy-project",
        "false",
        "false",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
    };
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses = InputHandler.splitInputInMergeScenarios(args);
    objectSerializer.startSerialization(mergeScenarioUnderAnalyses);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"7810b85dd711ac2648675dcfe5e65539aec1ea1d.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationUsingDefaultCall() throws IOException, InterruptedException, TransformerException {
    RunSerialization runSerialization = new RunSerialization();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "getName",
        "toy-project",
        "true",
        "true",
        "85077377978f98e31e637c121b5987e01725f5fd"
    };
    runSerialization.runAnalysis(args);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"85077377978f98e31e637c121b5987e01725f5fd.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationForTwoInputs() throws IOException, InterruptedException, TransformerException {
    RunSerialization runSerialization = new RunSerialization();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "getName",
        "toy-project",
        "true",
        "true",
        "85077377978f98e31e637c121b5987e01725f5fd",
        "5215c5d623a131ac94284be5c3c42c2124618e99"
    };
    runSerialization.runAnalysis(args);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"85077377978f98e31e637c121b5987e01725f5fd.jar").exists());
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"5215c5d623a131ac94284be5c3c42c2124618e99.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationUsingDefaultCallByCSVFile() throws IOException, InterruptedException, TransformerException {
    RunSerialization runSerialization = new RunSerialization();
    runSerialization.runAnalysis(new String[]{createInputFile()});
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"5215c5d623a131ac94284be5c3c42c2124618e99.jar").exists());
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"85077377978f98e31e637c121b5987e01725f5fd.jar").exists());
    deleteOldJar();
  }

  private void deleteOldJar(){
    try {
      FileUtils.deleteDirectory(new File(System.getProperty("user.dir")+File.separator+"src"+
          File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String createInputFile(){

    String inputFile = System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+
        File.separator+"input-merge-scenario.csv";
    new File(inputFile).delete();

    try {
      FileWriter csvWriter = new FileWriter(inputFile);
      csvWriter.append(String.join(",", System.getProperty("user.dir")+File.separator+"src"+
              File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java","getName","toy-project","true","true", "85077377978f98e31e637c121b5987e01725f5fd", "5215c5d623a131ac94284be5c3c42c2124618e99"));
      csvWriter.flush();
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return inputFile;
  }

  @Test
  public void expectJarGenerationForToyProjectMethodWithParameters() throws IOException, InterruptedException, TransformerException {
    ObjectSerializer objectSerializer = new ObjectSerializer();
    String[] args = {System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"+File.separator+"resources"+File.separator+"toy-project",
        "Person.java",
        "Person(java.lang.String, int, java.lang.String)",
        "toy-project",
        "true",
        "true",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
    };
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses = InputHandler.splitInputInMergeScenarios(args);
    objectSerializer.startSerialization(mergeScenarioUnderAnalyses);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"7810b85dd711ac2648675dcfe5e65539aec1ea1d.jar").exists());
    deleteOldJar();
  }
}
