package org.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.util.output.ProcessReport;

public class ProcessManager extends Thread {
  private int budget;
  private File serializedObjectsDir;

  public ProcessManager(int budget) {
    this.budget = budget;
  }

  public void setSerializedObjectsDir(String serializedObjectsDirPath) {
    this.serializedObjectsDir = new File(serializedObjectsDirPath);
  }

  public boolean computeProcessOutput(Process process, String outputMessage, boolean isTestTask)
      throws IOException, InterruptedException {

    ProcessReport errorGobbler = new ProcessReport(process.getErrorStream(), "ERROR");

    ProcessReport outputGobbler = new ProcessReport(process.getInputStream(), "OUTPUT");

    errorGobbler.start();
    outputGobbler.start();

    String outputProcess = getProcessOutput(process, isTestTask);
    System.out.println(
        outputMessage + " : " + outputProcess);
    if (!outputProcess.equals("UNSUCCESSFUL")) {
      return true;
    } else {
      return false;
    }

  }

  public String getProcessOutput(Process process, boolean isTest) throws InterruptedException, IOException {
    if (isTest) {
      String result = null;
      if (!process.waitFor(this.budget, TimeUnit.SECONDS)) {
        result = "FINISHED BY TIMEOUT";
      }
      process.descendants().forEach(p -> p.destroyForcibly());
      process.destroyForcibly();
      if(result == null){
        result = (DirUtils.isDirEmpty(serializedObjectsDir.toPath()) ? "UNSUCCESSFUL" : "SUCCESSFUL");
      }
      return result;
    } else {
      return process.waitFor() == 0 ? "SUCCESSFUL" : "UNSUCCESSFUL";
    }
  }
}
