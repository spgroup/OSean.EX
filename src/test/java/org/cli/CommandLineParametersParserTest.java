package org.cli;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.util.input.MergeScenarioUnderAnalysis;

class CommandLineParametersParserTest {
  protected static final String BASE_RESOURCES_LOCATION = String.join(File.separator, System.getProperty("user.dir"),
      "src", "test", "resources");
  protected static final String TOY_PROJECT_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION,
      "toy-project");

  private CommandLineParametersParser getCommandLineParametersParser() {
    return new CommandLineParametersParser();
  }

  @Test
  void mavenIsTheDefaultBuildManager() {
    String[] args = {
        "-l",
        TOY_PROJECT_LOCATION,
        "-c",
        "Person",
        "-m",
        "getName",
        "-p",
        "toy-project",
        "-h",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
    };

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args);

    Assert.assertEquals("maven", result.getBuildManager());
  }

  @Test
  void gradeIsTheBuildManagerIfFlagIsProvided() {
    String[] args = {
        "-l",
        TOY_PROJECT_LOCATION,
        "-c",
        "Person",
        "-m",
        "getName",
        "-p",
        "toy-project",
        "-g",
        "-h",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
    };

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args);

    Assert.assertEquals("gradle", result.getBuildManager());
  }
}