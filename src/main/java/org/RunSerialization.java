package org;

import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.serialization.ObjectSerializer;

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
    if (args.length > 3) {
      ObjectSerializer objectSerializer = new ObjectSerializer();
      objectSerializer.startSerialization(args[0], args[1], args[2], args[3]);
    }else{
        System.out.println("Please inform all three inputs required to run the serialization process");
        System.out.println("1º: local project path");
        System.out.println("2º: class holding the target method");
        System.out.println("3º: target method");
        System.out.println("4º: project name");
    }

  }

}
