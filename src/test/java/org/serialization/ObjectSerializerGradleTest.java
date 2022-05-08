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

public class ObjectSerializerGradleTest {
  private static final String TOY_PROJECT_1_ROOT = System.getProperty("user.dir") + File.separator + "src"
      + File.separator + "test" + File.separator + "resources" + File.separator + "toy-project-1";

  private String directoryForGeneratedJarsToyProject1 = System.getProperty("user.dir")+File.separator+"src"+
    File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"+
    File.separator+"toy-project-1";
  
  private void deleteOldJar(){
    try {
      FileUtils.deleteDirectory(new File(System.getProperty("user.dir")+File.separator+"src"+
          File.separator+"test"+File.separator+"resources"+File.separator+"GeneratedJars"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void expectJarGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    ObjectSerializerGradle objectSerializer = new ObjectSerializerGradle();
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalyses = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_1_ROOT)
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
    objectSerializer.startSerialization(mergeScenarioUnderAnalyses);

    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(), GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJarsToyProject1+File.separator+"1d1562dc88008736fdaec9dfa9c8f4756d21da19-isTeenager.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectJarGenerationForBuildFileWithoutFatJarPlugin() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalyses = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_1_ROOT)
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

    RunSerialization.runAnalysis(mergeScenarioUnderAnalyses);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJarsToyProject1+File.separator+"bc55f776168214586ea7d5d58187df6719f940c2-getName.jar").exists());
    deleteOldJar();
  }
  
  @Test
  /*
   * For more details: Gradle had changed the way it declares project dependencies. For ours propose, we considerer
   * old every gradle version that still acepts "compile" and "testCompile" commands, rather than "implementation" and "testImplementation"
   */
  public void expectJarGenerationForGradleWithOlderVersion() throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalyses = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_1_ROOT)
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

    RunSerialization.runAnalysis(mergeScenarioUnderAnalyses);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJarsToyProject1+File.separator+"c257474a8206e05d82a444bead4222d1dec9f60b-getName.jar").exists());
    deleteOldJar();
  }

  @Test
  public void expectTestFilesJarAndTestFilesListGenerationForToyProject() throws IOException, InterruptedException, TransformerException {
    ObjectSerializerGradle objectSerializer = new ObjectSerializerGradle();

    MergeScenarioUnderAnalysis mergeScenarioUnderAnalyses = MergeScenarioUnderAnalysis.builder()
        .localProjectPath(TOY_PROJECT_1_ROOT)
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

    objectSerializer.startSerialization(mergeScenarioUnderAnalyses);
    Repository subRepo = SubmoduleWalk.getSubmoduleRepository(getMainRepository(),
        GitProjectActionsTest.projectPath);
    GitProjectActions gitProjectActions = new GitProjectActions(subRepo);
    Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    Assert.assertTrue(new File(directoryForGeneratedJarsToyProject1+File.separator+"1d1562dc88008736fdaec9dfa9c8f4756d21da19-TestFiles.jar").exists());
    Assert.assertTrue(testFileListContentExpected(new File(directoryForGeneratedJarsToyProject1+File.separator+"1d1562dc88008736fdaec9dfa9c8f4756d21da19-TestFiles.txt")));
    deleteOldJar();
  }

  private boolean testFileListContentExpected(File file) throws FileNotFoundException {
    Scanner sc = new Scanner(file);
    String className = sc.nextLine();
    sc.close();
    return className.equals("org.PersonTest");
  }
}
