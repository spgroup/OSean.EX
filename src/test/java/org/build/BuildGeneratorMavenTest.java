package org.build;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.util.GitDriver;
import org.util.GitDriverTest;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class BuildGeneratorMavenTest extends BuildGeneratorTest {
    @Override
    protected BuildGeneratorMaven getBuildGenerator(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis)
            throws TransformerException {
        return new BuildGeneratorMaven(mergeScenarioUnderAnalysis, PROCESS_MANAGER);
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
                .serialize(true)
                .mergeScenarioCommits(Arrays.asList("7810b85dd711ac2648675dcfe5e65539aec1ea1d"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genBuildWithSerialization(mergeScenarioUnderAnalysis,
                gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

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
                .serialize(false)
                .mergeScenarioCommits(Arrays.asList("7810b85dd711ac2648675dcfe5e65539aec1ea1d"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

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
                .serialize(true)
                .mergeScenarioCommits(Arrays.asList("85077377978f98e31e637c121b5987e01725f5fd"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genBuildWithSerialization(mergeScenarioUnderAnalysis,
                gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

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
                .serialize(false)
                .mergeScenarioCommits(
                        Arrays.asList("85077377978f98e31e637c121b5987e01725f5fd",
                                "5215c5d623a131ac94284be5c3c42c2124618e99"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

        assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
    }

    @Test
    public void expectJarGenerationForToyProjectMethodWithParameters()
            throws IOException, InterruptedException, TransformerException {
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
                .serialize(true)
                .mergeScenarioCommits(Arrays.asList("7810b85dd711ac2648675dcfe5e65539aec1ea1d"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genBuildWithSerialization(mergeScenarioUnderAnalysis,
                gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

        assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
    }

    @Test
    public void expectJarGenerationForProjectWithMockitoPluginProblem()
            throws IOException, InterruptedException, TransformerException {
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
                .serialize(false)
                .mergeScenarioCommits(Arrays.asList("00c4a9fd0ae7587499f942cf2238fcf90b287baa"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

        assertJarFilesForScenarioExists(mergeScenarioUnderAnalysis);
    }

    @Test
    public void expectJarGenerationForProjectWithTestCompilationProblem()
            throws IOException, InterruptedException, TransformerException {
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
                .serialize(false)
                .mergeScenarioCommits(
                        Arrays.asList("00c4a9fd0ae7587499f942cf2238fcf90b287baa",
                                "70fcc5af960e08ac057dfc5f3990225fafa9fd7d"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        getBuildGenerator(mergeScenarioUnderAnalysis).genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));

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
                .serialize(true)
                .mergeScenarioCommits(
                        Arrays.asList("a7ceadcb1061874a72f950bdf48a691b68d0622b"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        BuildGeneratorMaven buildGenerator = new BuildGeneratorMaven(mergeScenarioUnderAnalysis, PROCESS_MANAGER);
        GitDriver spyGitDriver = spy(gitProjectActions);
        doReturn(false).when(spyGitDriver).cleanChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        buildGenerator.genBuildWithSerialization(mergeScenarioUnderAnalysis, spyGitDriver);
        Assert.assertFalse(new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "toy-project-1"
                + File.separator + "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "serializedObjects" + File.separator + "Person3.xml").exists());
        gitProjectActions.undoCurrentChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        gitProjectActions.cleanChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));
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
                        .budget(20)
                        .build())
                .buildManager("maven")
                .serialize(true)
                .mergeScenarioCommits(
                        Arrays.asList("916ac4231c0566c39a469bde5cbb8802782ec81b"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        BuildGeneratorMaven buildGenerator = new BuildGeneratorMaven(mergeScenarioUnderAnalysis, PROCESS_MANAGER);
        GitDriver spyGitDriver = spy(gitProjectActions);
        doReturn(false).when(spyGitDriver).cleanChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        buildGenerator.genBuildWithSerialization(mergeScenarioUnderAnalysis, spyGitDriver);
        Assert.assertFalse(new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "toy-project-1"
                + File.separator + "src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "serializedObjects" + File.separator + "Person3.xml").exists());
        gitProjectActions.undoCurrentChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        gitProjectActions.cleanChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));
    }
}
