package org;

import java.io.IOException;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.serialization.ObjectSerializer;
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
   * The fourth input is the project name
   * The fifth input is generated java jar file name (optional)
   */
  public static void runAnalysis(String[] args)
      throws IOException, InterruptedException, TransformerException {
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses = InputHandler.splitInputInMergeScenarios(args);
    if (mergeScenarioUnderAnalyses.size() > 0) {
      ObjectSerializer objectSerializer = new ObjectSerializer();
      objectSerializer.startSerialization(mergeScenarioUnderAnalyses);
    }else{
        System.out.println("Please inform all three inputs required to run the serialization process");
        System.out.println("1º: local project path");
        System.out.println("2º: class holding the target method");
        System.out.println("3º: target method");
        System.out.println("4º: project name");
        System.out.println("5º: indication for applying testability transformations");
        System.out.println("6º: indication for applying full testability transformations");
        System.out.println("7º: list of commit hashes");
    }

  }

}
