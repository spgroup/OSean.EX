package org.serialization;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.util.GitProjectActionsTest.getMainRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.transform.TransformerException;
import org.RunSerialization;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitProjectActions;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;
import org.util.GitProjectActionsTest;

public class ObjectSerializerMavenTest {
  private static final String BASE_RESOURCES_LOCATION = String.join(File.separator, System.getProperty("user.dir"), "src", "test", "resources");
  private static final String TOY_PROJET_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION, "toy-project");
  private static final String TOY_PROJET_ONE_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION, "toy-project-1");

  private String directoryForGeneratedJars = String.join(File.separator, BASE_RESOURCES_LOCATION, "GeneratedJars", "toy-project");
  private String directoryForGeneratedJarsToyProject1 = String.join(File.separator, BASE_RESOURCES_LOCATION, "GeneratedJars", "toy-project-1");

  @Test
  public void expectJarGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_LOCATION)
        .targetClass("Person.java")
        .targetMethod("getOccupation")
        .projectName("toy-project")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(Arrays.asList("7810b85dd711ac2648675dcfe5e65539aec1ea1d"))
        .build();
    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"7810b85dd711ac2648675dcfe5e65539aec1ea1d-getOccupation.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationForToyProjectWithPartialTransformations() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_LOCATION)
        .targetClass("Person.java")
        .targetMethod("getOccupation")
        .projectName("toy-project")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(false)
            .applyFullTransformations(false)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(Arrays.asList("7810b85dd711ac2648675dcfe5e65539aec1ea1d"))
        .build();
    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"7810b85dd711ac2648675dcfe5e65539aec1ea1d-getOccupation.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationUsingDefaultCall() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_LOCATION)
        .targetClass("Person.java")
        .targetMethod("getName")
        .projectName("toy-project")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(Arrays.asList("85077377978f98e31e637c121b5987e01725f5fd"))
        .build();

    RunSerialization.runAnalysis(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"85077377978f98e31e637c121b5987e01725f5fd-getName.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationForTwoInputs() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_LOCATION)
        .targetClass("Person.java")
        .targetMethod("getName")
        .projectName("toy-project")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(
            Arrays.asList("85077377978f98e31e637c121b5987e01725f5fd", "5215c5d623a131ac94284be5c3c42c2124618e99"))
        .build();

    RunSerialization.runAnalysis(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"85077377978f98e31e637c121b5987e01725f5fd-getName.jar").exists());
    Assert.assertTrue(new File(directoryForGeneratedJars+File.separator+"5215c5d623a131ac94284be5c3c42c2124618e99-getName.jar").exists());
    deleteOldJar();
  }

  // This test will need to be refactored once the CLI allows usage of CSVs
  // @Test
  // public void expectJarGenerationUsingDefaultCallByCSVFile() throws
  // IOException, InterruptedException, TransformerException {
  // RunSerialization.runAnalysis(new String[]{createInputFile()});
  // Repository subRepo =
  // SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
  // GitProjectActionsTest.projectPath);
  // GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
  // Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  // Assert.assertTrue(new
  // File(directoryForGeneratedJars+File.separator+"5215c5d623a131ac94284be5c3c42c2124618e99-getName.jar").exists());
  // Assert.assertTrue(new
  // File(directoryForGeneratedJars+File.separator+"85077377978f98e31e637c121b5987e01725f5fd-getName.jar").exists());
  // deleteOldJar();
  // }

  private void deleteOldJar(){
    try {
      FileUtils.deleteDirectory(new File(System.getProperty("user.dir")+File.separator+"src"+
          File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void deleteTargetFolder(){
    try {
      FileUtils.deleteDirectory(new File(System.getProperty("user.dir")+File.separator+"src"+
          File.separator+"test"+File.separator+"resources"+File.separator+"toy-project-1"+File.separator+"target"));
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
        "Person.java","getName","toy-project","true","true","60", "maven","85077377978f98e31e637c121b5987e01725f5fd", "5215c5d623a131ac94284be5c3c42c2124618e99"));
      csvWriter.flush();
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return inputFile;
  }

  @Test
  public void expectJarGenerationForToyProjectMethodWithParameters() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_LOCATION)
        .targetClass("Person.java")
        .targetMethod("Person(java.lang.String, int, java.lang.String)")
        .projectName("toy-project")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(Arrays.asList("7810b85dd711ac2648675dcfe5e65539aec1ea1d"))
        .build();

    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(
        new File(directoryForGeneratedJars + File.separator + "7810b85dd711ac2648675dcfe5e65539aec1ea1d-Person.jar")
            .exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationForProjectWithMockitoPluginProblem() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("Person(java.lang.String, int, java.lang.String)")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(Arrays.asList("00c4a9fd0ae7587499f942cf2238fcf90b287baa"))
        .build();

    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(
        directoryForGeneratedJarsToyProject1 + File.separator + "00c4a9fd0ae7587499f942cf2238fcf90b287baa-Person.jar")
        .exists());
    deleteOldJar();
    deleteTargetFolder();
  }

  @Test
  public void expectJarGenerationForProjectWithTestCompilationProblem() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("Person(java.lang.String, int, java.lang.String)")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(
            Arrays.asList("00c4a9fd0ae7587499f942cf2238fcf90b287baa", "70fcc5af960e08ac057dfc5f3990225fafa9fd7d"))
        .build();
    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(
        directoryForGeneratedJarsToyProject1 + File.separator + "70fcc5af960e08ac057dfc5f3990225fafa9fd7d-Person.jar")
        .exists());
    deleteOldJar();
    deleteTargetFolder();
  }

  @Test
  public void dontSerializeEqualObjects() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("Person(java.lang.String, int, java.lang.String)")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(
            Arrays.asList("a7ceadcb1061874a72f950bdf48a691b68d0622b"))
        .build();

    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    ObjectSerializerMaven spySerializer = spy(objectSerializer);
    doReturn(false).when(spySerializer).cleanResourceDirectory();
    spySerializer.startSerialization(mergeScenarioUnderAnalysis);
    Assert.assertFalse(new File(System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"
                      +File.separator+"resources"+File.separator+"toy-project-1"
                      +File.separator+"src"+File.separator+"main"+File.separator+"resources"
                      +File.separator+"serializedObjects"+File.separator+"Person3.xml").exists());
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    deleteOldJar();
    deleteTargetFolder();
  }

  @Test
  public void dontSerializeWhenReachesTimeLimit() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJET_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("Person(java.lang.String, int, java.lang.String)")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(50)
            .build())
        .buildManager("maven")
        .mergeScenarioCommits(
            Arrays.asList("916ac4231c0566c39a469bde5cbb8802782ec81b"))
        .build();

    ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
    ObjectSerializerMaven spySerializer = spy(objectSerializer);
    doReturn(false).when(spySerializer).cleanResourceDirectory();
    spySerializer.startSerialization(mergeScenarioUnderAnalysis);
    Assert.assertFalse(new File(System.getProperty("user.dir")+File.separator+"src"+ File.separator+"test"
                      +File.separator+"resources"+File.separator+"toy-project-1"
                      +File.separator+"src"+File.separator+"main"+File.separator+"resources"
                      +File.separator+"serializedObjects"+File.separator+"Person3.xml").exists());
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    deleteOldJar();
    deleteTargetFolder();
  }
}
