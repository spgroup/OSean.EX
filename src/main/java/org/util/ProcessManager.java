package org.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.util.output.ProcessReport;

public class ProcessManager extends Thread {
  private int budget;

  public ProcessManager(int budget){
    this.budget = budget;
  }

  public boolean computeProcessOutput(Process process, String outputMessage, boolean isTestTask)
      throws IOException, InterruptedException {

    ProcessReport errorGobbler = new
        ProcessReport(process.getErrorStream(), "ERROR");

    ProcessReport outputGobbler = new
        ProcessReport(process.getInputStream(), "OUTPUT");

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

  public String getProcessOutput(Process process, boolean isTest) throws InterruptedException {
    if (isTest){
      if (!process.waitFor(this.budget, TimeUnit.SECONDS)){
//        process.descendants().forEach(p -> p.destroyForcibly());
        process.destroyForcibly();
        return "FINISHED BY TIMEOUT";
      }else{
        return "SUCCESSFUL";
      }
    }else{
      return process.waitFor() == 0 ? "SUCCESSFUL" : "UNSUCCESSFUL";
    }
  }
}
