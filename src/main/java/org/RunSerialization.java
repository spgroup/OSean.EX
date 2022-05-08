package org;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.cli.CommandLineParametersParser;
import org.serialization.ObjectSerializer;
import org.serialization.ObjectSerializerGradle;
import org.serialization.ObjectSerializerMaven;
import org.util.input.MergeScenarioUnderAnalysis;

public class RunSerialization {
  public static void main(String[] args) throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = new CommandLineParametersParser().parse(args);
    runAnalysis(mergeScenarioUnderAnalysis);
  }

  public static void runAnalysis(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis)
      throws IOException, InterruptedException, TransformerException {
    ObjectSerializer objectSerializer = getObjectSerializer(mergeScenarioUnderAnalysis);
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
  }

  private static ObjectSerializer getObjectSerializer(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {
    return mergeScenarioUnderAnalysis.getBuildManager().equals("maven") ? new ObjectSerializerMaven()
        : new ObjectSerializerGradle();
  }
}
