package org.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessManager extends Thread {

  private InputStream is;
  private String type;
  private List<String> outputs;

  public ProcessManager(InputStream is, String type) {
    this.is = is;
    this.type = type;
    this.outputs = new ArrayList<>();
  }

  public List<String> getOutputs() {
    return this.outputs;
  }

  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null) {
        outputs.add(line);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static List<String> getListOfMethodsAssociatedToSerializedObjects(Process process2)
      throws IOException, InterruptedException {

    ProcessManager errorGobbler = new
        ProcessManager(process2.getErrorStream(), "ERROR");

    ProcessManager outputGobbler = new
        ProcessManager(process2.getInputStream(), "OUTPUT");

    errorGobbler.start();
    outputGobbler.start();

    System.out.println(
        "Generating method list associated to serialized objects: final status - "+ (process2.waitFor() == 0 ? true : false));

    return outputGobbler.getOutputs();
  }
}
