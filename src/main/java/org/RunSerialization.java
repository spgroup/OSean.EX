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
    runAnalysis(args);
  }

  // java -cp ObjectSerialization-1.0-SNAPSHOT-jar-with-dependencies.jar org.RunSerialization "/home/lmps2/projects/toy-project" "Person.java" "addRelative" "toy-project" "true" "true" "60" "maven" "abdc125" "abdc156"
  public static void runAnalysis(String[] args) throws IOException, InterruptedException, TransformerException {
    MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis = new CommandLineParametersParser().parse(args);
    ObjectSerializer objectSerializer = getObjectSerializer(mergeScenarioUnderAnalysis);
    objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
  }

  private static ObjectSerializer getObjectSerializer(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {
    return mergeScenarioUnderAnalysis.getBuildManager().equals("maven") ? new ObjectSerializerMaven()
        : new ObjectSerializerGradle();
  }
}
