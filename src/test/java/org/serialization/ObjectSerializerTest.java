package org.serialization;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.util.input.MergeScenarioUnderAnalysis;

public abstract class ObjectSerializerTest {
  protected static final String BASE_RESOURCES_LOCATION = String.join(File.separator, System.getProperty("user.dir"), "src", "test", "resources");
  protected static final String TOY_PROJECT_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION, "toy-project");
  protected static final String TOY_PROJECT_ONE_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION, "toy-project-1");

  @After
  public void deleteOldJar() {
    try {
      String generatedJarsDir = String.join(File.separator, BASE_RESOURCES_LOCATION, "GeneratedJars");
      FileUtils.deleteDirectory(new File(generatedJarsDir));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @After
  public void deleteTargetFolder() {
    try {
      String targetDir = String.join(File.separator, TOY_PROJECT_ONE_LOCATION, "target");
      FileUtils.deleteDirectory(new File(targetDir));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void assertJarFilesForScenarioExists(MergeScenarioUnderAnalysis scenario) {
    String directoryForGeneratedJars = String.join(File.separator, BASE_RESOURCES_LOCATION, "GeneratedJars", scenario.getProjectName());

    scenario.getMergeScenarioCommits().forEach((sha) -> {
      int finalMethodIndexName = scenario.getTargetMethod().contains("(") ? scenario.getTargetMethod().indexOf("(") : scenario.getTargetMethod().length();
      String methodName = scenario.getTargetMethod().substring(0, finalMethodIndexName);
      String fileName = sha + "-" + methodName + ".jar";
      File expectedOutputFile = new File(directoryForGeneratedJars + File.separator + fileName);
      Assert.assertTrue(expectedOutputFile.exists());
    });
  }

  protected abstract ObjectSerializer getObjectSerializer();
}
