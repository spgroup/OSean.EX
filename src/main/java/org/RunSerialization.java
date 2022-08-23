package org;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.cli.CommandLineParametersParser;
import org.file.BuildFileException;
import org.serialization.ObjectSerializer;
import org.serialization.ObjectSerializerGradle;
import org.serialization.ObjectSerializerMaven;
import org.util.DirUtils;
import org.util.GitProjectActions;
import org.util.input.MergeScenarioUnderAnalysis;

public class RunSerialization {
  public static void main(String[] args) throws IOException, InterruptedException, TransformerException {
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalysis = new CommandLineParametersParser().parse(args);
    mergeScenarioUnderAnalysis.forEach(scenario -> runAnalysis(scenario));
  }

  public static void runAnalysis(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {
    try {
      GitProjectActions gitProjectActions = new GitProjectActions(mergeScenarioUnderAnalysis.getLocalProjectPath());
      gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
      ObjectSerializer objectSerializer = getObjectSerializer(mergeScenarioUnderAnalysis);
      objectSerializer.startSerialization(mergeScenarioUnderAnalysis, gitProjectActions);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static ObjectSerializer getObjectSerializer(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) throws BuildFileException {
    if(mergeScenarioUnderAnalysis.getBuildManager() != null && mergeScenarioUnderAnalysis.getBuildManager().equals("gradle")){
      return new ObjectSerializerGradle();
    } else{
      String buildManager = DirUtils.findBuildFileInDir(mergeScenarioUnderAnalysis.getLocalProjectPath());
      if(buildManager == null){
        throw new BuildFileException("Can't find pom.xml or build.gradle for project " + mergeScenarioUnderAnalysis.getProjectName());
      }
      return buildManager.equals("maven") ? new ObjectSerializerMaven()
          : new ObjectSerializerGradle();
    }
  }
}
