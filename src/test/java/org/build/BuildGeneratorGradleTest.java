package org.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitDriver;
import org.util.GitDriverTest;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class BuildGeneratorGradleTest extends BuildGeneratorTest {
  @Override
  protected BuildGeneratorGradle getBuildGenerator(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis)
      throws TransformerException {
    return new BuildGeneratorGradle(mergeScenarioUnderAnalysis, PROCESS_MANAGER);
  }

  @After
  public void deleteBinFolder() {
    try {
      String binDir = String.join(File.separator, TOY_PROJECT_ONE_LOCATION, "app" + File.separator + "bin");
      if (new File(binDir).exists())
        FileUtils.deleteDirectory(new File(binDir));
    } catch (IOException e) {
      e.printStackTrace();
    }
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
        .serialize(true)
        .mergeScenarioCommits(Arrays.asList("1d1562dc88008736fdaec9dfa9c8f4756d21da19"))
        .build();

    GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
        mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0),
        PROCESS_MANAGER);
    getBuildGenerator(mergeScenarioUnderAnalysis).genBuildWithSerialization(mergeScenarioUnderAnalysis,
        gitProjectActions);
    Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectJarGenerationForBuildFileWithoutFatJarPlugin()
      throws IOException, InterruptedException, TransformerException {
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
        .serialize(false)
        .mergeScenarioCommits(Arrays.asList("bc55f776168214586ea7d5d58187df6719f940c2"))
        .build();

    GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
        mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0),
        PROCESS_MANAGER);
    getBuildGenerator(mergeScenarioUnderAnalysis).genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
    Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  /*
   * For more details: Gradle had changed the way it declares project
   * dependencies. For ours propose, we considerer
   * old every gradle version that still acepts "compile" and "testCompile"
   * commands, rather than "implementation" and "testImplementation"
   */
  public void expectJarGenerationForGradleWithOlderVersion()
      throws IOException, InterruptedException, TransformerException {
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
        .serialize(true)
        .mergeScenarioCommits(Arrays.asList("c257474a8206e05d82a444bead4222d1dec9f60b"))
        .build();

    GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
        mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0),
        PROCESS_MANAGER);
    getBuildGenerator(mergeScenarioUnderAnalysis).genBuildWithSerialization(mergeScenarioUnderAnalysis,
        gitProjectActions);
    Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
  }

  @Test
  public void expectTestFilesJarAndTestFilesListGenerationForToyProject()
      throws IOException, InterruptedException, TransformerException {
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
        .serialize(false)
        .mergeScenarioCommits(Arrays.asList("1d1562dc88008736fdaec9dfa9c8f4756d21da19"))
        .build();

    GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
        mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0),
        PROCESS_MANAGER);
    getBuildGenerator(mergeScenarioUnderAnalysis).genTestFilesBuild(mergeScenarioUnderAnalysis,
        mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
    getBuildGenerator(mergeScenarioUnderAnalysis).genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
    Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

    assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
    Assert.assertTrue(testFileListContentExpected(new File(String.join(File.separator, BASE_RESOURCES_LOCATION,
        "GeneratedJars", "toy-project-1", "1d1562dc88008736fdaec9dfa9c8f4756d21da19-TestFiles.txt"))));
  }

  private boolean testFileListContentExpected(File file) throws FileNotFoundException {
    Scanner sc = new Scanner(file);
    String className = sc.nextLine();
    sc.close();
    return className.equals("org.PersonTest");
  }
}
