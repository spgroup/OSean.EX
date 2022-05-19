package org.serialization;

import static org.util.GitProjectActionsTest.getMainRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.xml.transform.TransformerException;

import org.RunSerialization;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitProjectActions;
import org.util.GitProjectActionsTest;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class ObjectSerializerGradleTest extends ObjectSerializerTest {
  @Override
  protected ObjectSerializerGradle getObjectSerializer() {
    return new ObjectSerializerGradle();
  }

  @Test
  public void expectJarGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("isTeenager")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("gradle")
        .mergeScenarioCommits(Arrays.asList("1d1562dc88008736fdaec9dfa9c8f4756d21da19"))
        .build();

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForBuildFileWithoutFatJarPlugin() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("getName")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("gradle")
        .mergeScenarioCommits(Arrays.asList("bc55f776168214586ea7d5d58187df6719f940c2"))
        .build();

    RunSerialization.runAnalysis(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }
  
  @Test
  /*
   * For more details: Gradle had changed the way it declares project dependencies. For ours propose, we considerer
   * old every gradle version that still acepts "compile" and "testCompile" commands, rather than "implementation" and "testImplementation"
   */
  public void expectJarGenerationForGradleWithOlderVersion() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("getName")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("gradle")
        .mergeScenarioCommits(Arrays.asList("c257474a8206e05d82a444bead4222d1dec9f60b"))
        .build();

    RunSerialization.runAnalysis(mergeScenarioUnderAnalysis);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectTestFilesJarAndTestFilesListGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_ONE_LOCATION)
        .targetClass("Person.java")
        .targetMethod("isTeenager")
        .projectName("toy-project-1")
        .transformationOption(TransformationOption.builder()
            .applyTransformations(true)
            .applyFullTransformations(true)
            .budget(60)
            .build())
        .buildManager("gradle")
        .mergeScenarioCommits(Arrays.asList("1d1562dc88008736fdaec9dfa9c8f4756d21da19"))
        .build();

    getObjectSerializer().startSerialization(mergeScenarioUnderAnalysis);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
    Assert.assertTrue(testFileListContentExpected(new File(String.join(File.separator, BASE_RESOURCES_LOCATION,
        "GeneratedJars", "1d1562dc88008736fdaec9dfa9c8f4756d21da19-TestFiles.txt"))));
  }

  private boolean testFileListContentExpected(File file) throws FileNotFoundException {
    Scanner sc = new Scanner(file);
    String className = sc.nextLine();
    sc.close();
    return className.equals("org.PersonTest");
  }
}
