package org;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.file.BuildFileException;
import org.junit.Assert;
import org.junit.Test;
import org.serialization.ObjectSerializer;
import org.serialization.ObjectSerializerGradle;
import org.serialization.ObjectSerializerMaven;
import org.util.GitProjectActions;
import org.util.GitProjectActionsTest;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class RunSerializationTest {
    protected static final String BASE_RESOURCES_LOCATION = String.join(File.separator, System.getProperty("user.dir"), "src", "test", "resources");
    protected static final String TOY_PROJECT_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION, "toy-project");
    protected static final String TOY_PROJECT_ONE_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION, "toy-project-1");
    
    @Test
    public void findMavenAsBuildeManager() throws IOException, InterruptedException, TransformerException, BuildFileException {
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
  
        GitProjectActions gitProjectActions = GitProjectActionsTest.getGitProjectActionsAndChangeCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
        ObjectSerializer objectSerializer = RunSerialization.getObjectSerializer(mergeScenarioUnderAnalysis);
        Assert.assertTrue(objectSerializer instanceof ObjectSerializerMaven);
        Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    }

    @Test
    public void findGradleAsBuildeManager() throws IOException, InterruptedException, TransformerException, BuildFileException {
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
  
        GitProjectActions gitProjectActions = GitProjectActionsTest.getGitProjectActionsAndChangeCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
        ObjectSerializer objectSerializer = RunSerialization.getObjectSerializer(mergeScenarioUnderAnalysis);
        Assert.assertTrue(objectSerializer instanceof ObjectSerializerGradle);
        Assert.assertTrue(gitProjectActions.checkoutCommit("main"));
    }
}
