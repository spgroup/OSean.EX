package org;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.build.BuildGenerator;
import org.build.BuildGeneratorGradle;
import org.build.BuildGeneratorMaven;
import org.file.BuildFileException;
import org.junit.Assert;
import org.junit.Test;
import org.util.GitDriver;
import org.util.GitDriverTest;
import org.util.ProcessManager;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class MainTest {
    protected static final String BASE_RESOURCES_LOCATION = String.join(File.separator, System.getProperty("user.dir"),
            "src", "test", "resources");
    protected static final String TOY_PROJECT_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION,
            "toy-project");
    protected static final String TOY_PROJECT_ONE_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION,
            "toy-project-1");
    protected static final ProcessManager PROCESS_MANAGER = new ProcessManager(20);

    @Test
    public void findMavenAsBuildManager()
            throws IOException, InterruptedException, TransformerException, BuildFileException {
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
                .mergeScenarioCommits(Arrays.asList("85077377978f98e31e637c121b5987e01725f5fd"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        BuildGenerator objectSerializer = Main.getBuildGenerator(mergeScenarioUnderAnalysis, PROCESS_MANAGER);
        gitProjectActions.undoCurrentChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        Assert.assertTrue(objectSerializer instanceof BuildGeneratorMaven);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));
    }

    @Test
    public void findGradleAsBuildManager()
            throws IOException, InterruptedException, TransformerException, BuildFileException {
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
                .mergeScenarioCommits(Arrays.asList("1d1562dc88008736fdaec9dfa9c8f4756d21da19"))
                .build();

        GitDriver gitProjectActions = GitDriverTest.getGitProjectActionsAndChangeCommit(
                mergeScenarioUnderAnalysis.getLocalProjectPath(),
                mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0), PROCESS_MANAGER);
        BuildGenerator objectSerializer = Main.getBuildGenerator(mergeScenarioUnderAnalysis, PROCESS_MANAGER);
        gitProjectActions.undoCurrentChanges(mergeScenarioUnderAnalysis.getLocalProjectPath());
        Assert.assertTrue(objectSerializer instanceof BuildGeneratorGradle);
        Assert.assertTrue(gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), "main"));
    }
}
