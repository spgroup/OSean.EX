package org.util.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessReport extends Thread{

  private InputStream is;
  private String type;
  private List<String> outputs;

  public ProcessReport(InputStream is, String type) {
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
      br.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
