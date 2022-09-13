package org;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.build.BuildGenerator;
import org.build.BuildGeneratorGradle;
import org.build.BuildGeneratorMaven;
import org.cli.CommandLineParametersParser;
import org.file.BuildFileException;
import org.util.DirUtils;
import org.util.GitDriver;
import org.util.ProcessManager;
import org.util.input.MergeScenarioUnderAnalysis;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException, TransformerException {
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalysis = new CommandLineParametersParser().parse(args);
    mergeScenarioUnderAnalysis.forEach(scenario -> runAnalysis(scenario));
  }

  public static void runAnalysis(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {
    try {
      ProcessManager processManager = new ProcessManager(
          mergeScenarioUnderAnalysis.getTransformationOption().getBudget());
      GitDriver gitProjectActions = new GitDriver(mergeScenarioUnderAnalysis.getLocalProjectPath(), processManager);
      gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getLocalProjectPath(),
          mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
      BuildGenerator buildGenerator = getBuildGenerator(mergeScenarioUnderAnalysis, processManager);
      buildGenerator.genTestFilesBuild(mergeScenarioUnderAnalysis,
          mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
      if (mergeScenarioUnderAnalysis.getSerialize()) {
        buildGenerator.genBuildWithSerialization(mergeScenarioUnderAnalysis, gitProjectActions);
      } else {
        buildGenerator.genPlainBuild(mergeScenarioUnderAnalysis, gitProjectActions);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static BuildGenerator getBuildGenerator(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis,
      ProcessManager processManager) throws BuildFileException, TransformerException {
    if (mergeScenarioUnderAnalysis.getBuildManager() != null
        && mergeScenarioUnderAnalysis.getBuildManager().equals("gradle")) {
      return new BuildGeneratorGradle(mergeScenarioUnderAnalysis, processManager);
    } else {
      String buildManager = DirUtils.findBuildFileInDir(mergeScenarioUnderAnalysis.getLocalProjectPath());
      if (buildManager == null) {
        throw new BuildFileException(
            "Can't find pom.xml or build.gradle for project " + mergeScenarioUnderAnalysis.getProjectName());
      }
      return buildManager.equals("maven") ? new BuildGeneratorMaven(mergeScenarioUnderAnalysis, processManager)
          : new BuildGeneratorGradle(mergeScenarioUnderAnalysis, processManager);
    }
  }
}
