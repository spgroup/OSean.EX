package org;

import java.io.IOException;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.serialization.ObjectSerializerGradle;
import org.serialization.ObjectSerializerMaven;
import org.util.InputHandler;
import org.util.input.MergeScenarioUnderAnalysis;

public class RunSerialization {

  public static void main(String[] args)
      throws IOException, InterruptedException, TransformerException {
    runAnalysis(args);
  }

  /***
   * The values on args are used to drive the serialization process
   * The first input is the local path of the project under analysis
   * The second input is the class holding the method that the instrumentation will be applied
   * The third input is the method where the instrumentation will be applied
   * The fourth input is input is generated java jar file name
   * The fifth input indicates the application of testability transformations
   * The sixth input indicates the application of full testability transformations
   * The seventh input is the bufget time in seconds for serialization 
   * The eighth input indicates the building tool used on the project
   * For ninth and beyond, the list of commits hashes
   */

  // java -cp ObjectSerialization-1.0-SNAPSHOT-jar-with-dependencies.jar org.RunSerialization "/home/lmps2/projects/toy-project" "Person.java" "addRelative" "toy-project" "true" "true" "60" "maven" "abdc125" "abdc156"
  public static void runAnalysis(String[] args)
      throws IOException, InterruptedException, TransformerException {
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses = InputHandler.splitInputInMergeScenarios(args);
    if (mergeScenarioUnderAnalyses.size() > 0) {
      for(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis: mergeScenarioUnderAnalyses){
        if (mergeScenarioUnderAnalysis.getBuildManager().compareToIgnoreCase("gradle") == 0){
          ObjectSerializerGradle objectSerializer = new ObjectSerializerGradle();
          objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
        } else if (mergeScenarioUnderAnalysis.getBuildManager().compareToIgnoreCase("maven") == 0) {
          ObjectSerializerMaven objectSerializer = new ObjectSerializerMaven();
          objectSerializer.startSerialization(mergeScenarioUnderAnalysis);
        } else {
          printCorrectSequenceMensage();
        }
      }
    }else{
      printCorrectSequenceMensage();
    }

  }

  private static void printCorrectSequenceMensage() {
    System.out.println("Please inform all inputs required to run the serialization process\n");
    System.out.println("1º: local project path");
    System.out.println("2º: class holding the target method");
    System.out.println("3º: target method");
    System.out.println("4º: project name");
    System.out.println("5º: indication for applying testability transformations");
    System.out.println("6º: indication for applying full testability transformations");
    System.out.println("7º: budget for serialization");
    System.out.println("8º: build tool used on the project");
    System.out.println("9º: list of commit hashes");
  }

}
