package org.cli;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.util.input.MergeScenarioUnderAnalysis;

public class CommandLineParametersParserTest {
  protected static final String BASE_RESOURCES_LOCATION = String.join(File.separator, System.getProperty("user.dir"),
      "src", "test", "resources");
  protected static final String TOY_PROJECT_LOCATION = String.join(File.separator, BASE_RESOURCES_LOCATION,
      "toy-project");

  private CommandLineParametersParser getCommandLineParametersParser() {
    return new CommandLineParametersParser();
  }

  @Test
  public void mavenIsTheDefaultBuildManager() {
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

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args).get(0);

    Assert.assertEquals("maven", result.getBuildManager());
  }

  @Test
  public void gradeIsTheBuildManagerIfFlagIsProvided() {
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

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args).get(0);

    Assert.assertEquals("gradle", result.getBuildManager());
  }

  @Test
  public void mergeScenarioCommitsAreProvidedAsCommaSeparated() {
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
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d,85077377978f98e31e637c121b5987e01725f5fd"
    };

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args).get(0);
    List<String> expectedCommitHashes = Arrays.asList(
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d",
        "85077377978f98e31e637c121b5987e01725f5fd");

    Assert.assertEquals(expectedCommitHashes, result.getMergeScenarioCommits());
  }

  @Test
  public void transformationsAreDisabledByDefault() {
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

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args).get(0);

    Assert.assertFalse(result.getTransformationOption().applyTransformations());
    Assert.assertFalse(result.getTransformationOption().applyFullTransformations());
  }

  @Test
  public void transformationsHaveADefaultBudgetOf60() {
    String[] args = {
        "-l",
        TOY_PROJECT_LOCATION,
        "-c",
        "Person",
        "-m",
        "getName",
        "-p",
        "toy-project",
        "-att",
        "-h",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
    };

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args).get(0);

    Assert.assertTrue(result.getTransformationOption().applyTransformations());
    Assert.assertEquals(60, result.getTransformationOption().getBudget());
  }

  @Test
  public void fullTransformationsAreDisabledByDefaultEvenIfTransformationsAreActive() {
    String[] args = {
        "-l",
        TOY_PROJECT_LOCATION,
        "-c",
        "Person",
        "-m",
        "getName",
        "-p",
        "toy-project",
        "-att",
        "-h",
        "7810b85dd711ac2648675dcfe5e65539aec1ea1d"
    };

    MergeScenarioUnderAnalysis result = getCommandLineParametersParser().parse(args).get(0);

    Assert.assertTrue(result.getTransformationOption().applyTransformations());
    Assert.assertFalse(result.getTransformationOption().applyFullTransformations());
  }

  @Test(expected = IllegalArgumentException.class)
  public void listOfCommitHashesIsMandatory() {
    String[] args = {
        "-l",
        TOY_PROJECT_LOCATION,
        "-c",
        "Person",
        "-m",
        "getName",
        "-p",
        "toy-project",
        "-att",
    };

    getCommandLineParametersParser().parse(args);
  }

  @Test
  public void ifBatchCsvIsProvidedItReadsDataFromFile() {
    String[] args = { "--batchCsv", String.join(File.separator, BASE_RESOURCES_LOCATION, "cli", "input.csv") };

    List<MergeScenarioUnderAnalysis> result = getCommandLineParametersParser().parse(args);

    Assert.assertEquals(2, result.size());
  }
}