package org.serialization;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitProjectActions;
import org.util.GitProjectActionsTest;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class ObjectSerializerMavenTest extends ObjectSerializerTest {
  @Override
  protected ObjectSerializerMaven getObjectSerializer() {
    return new ObjectSerializerMaven();
  }

  @Test
  public void expectJarGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForToyProjectWithPartialTransformations()
      throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationUsingDefaultCall() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForTwoInputs() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForToyProjectMethodWithParameters() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForProjectWithMockitoPluginProblem() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_ONE_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForProjectWithTestCompilationProblem() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_ONE_LOCATION)
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

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void dontSerializeEqualObjects() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_ONE_LOCATION)
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
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }

  @Test
  public void dontSerializeWhenReachesTimeLimit() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
            .localProjectPath(TOY_PROJECT_ONE_LOCATION)
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
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(GitProjectActionsTest.getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
  }
}
