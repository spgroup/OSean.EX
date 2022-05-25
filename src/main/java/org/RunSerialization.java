package org;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.cli.CommandLineParametersParser;
import org.serialization.ObjectSerializer;
import org.serialization.ObjectSerializerGradle;
import org.serialization.ObjectSerializerMaven;
import org.util.input.MergeScenarioUnderAnalysis;

public class RunSerialization {
  public static void main(String[] args) throws IOException, InterruptedException, TransformerException {
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalysis = new CommandLineParametersParser().parse(args);
    mergeScenarioUnderAnalysis.forEach(scenario -> runAnalysis(scenario));
  }

  public static void runAnalysis(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {
    try {
      ObjectSerializer objectSerializer = getObjectSerializer(mergeScenarioUnderAnalysis);
      objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static ObjectSerializer getObjectSerializer(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {
    return mergeScenarioUnderAnalysis.getBuildManager().equals("maven") ? new ObjectSerializerMaven()
        : new ObjectSerializerGradle();
  }
}
