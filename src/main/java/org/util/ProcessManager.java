package org.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessManager extends Thread {

  private InputStream is;
  private String type;
  private List<String> outputs;

  public ProcessManager(InputStream is, String type) {
    this.is = is;
    this.type = type;
    this.outputs = new ArrayList<>();
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

  public static boolean computeProcessOutput(Process process, String outputMessage, boolean isTestTask)
      throws IOException, InterruptedException {

    ProcessManager errorGobbler = new
        ProcessManager(process.getErrorStream(), "ERROR");

    ProcessManager outputGobbler = new
        ProcessManager(process.getInputStream(), "OUTPUT");

    errorGobbler.start();
    outputGobbler.start();

    String outputProcess = getProcessOutput(process, isTestTask);
    System.out.println(
        outputMessage + " : " + outputProcess);
    if (!outputProcess.equals("UNSUCCESSFUL")){
      return true;
    }else{
      return false;
    }

  }

  public static String getProcessOutput(Process process, boolean isTest) throws InterruptedException {
    if (isTest){
      if (!process.waitFor(1, TimeUnit.MINUTES)){
        process.destroy();
        return "FINISHED BY TIMEOUT";
      }else{
        return "SUCCESSFUL";
      }
    }else{
      return process.waitFor() == 0 ? "SUCCESSFUL" : "UNSUCCESSFUL";
    }
  }
}
