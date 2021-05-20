package org.util.input;

import java.util.ArrayList;
import java.util.List;

public class MergeScenarioUnderAnalysis {
  private String localProjectPath;
  private String targetClass;
  private String targetMethod;
  private String projectName;
  private List<String> mergeScenarioCommits;

  public MergeScenarioUnderAnalysis(String[] args){
    this.localProjectPath = args[0];
    this.targetClass = args[1];
    this.targetMethod = args[2];
    this.projectName = args[3];
    this.mergeScenarioCommits = parseMergeScenarioCommits(args);
  }

  private List<String> parseMergeScenarioCommits(String[] args){
    List<String> mergeScenarioCommits = new ArrayList<>();
      for(int i=4; i < args.length && args.length > 4; i++){
        if (args[i] != null && args[i] != ""){
          mergeScenarioCommits.add(args[i]);
        }
      }
    return mergeScenarioCommits;
  }

  public String getLocalProjectPath() {
    return localProjectPath;
  }

  public String getTargetClass() {
    return targetClass;
  }

  public String getTargetMethod() {
    return targetMethod;
  }

  public String getProjectName() {
    return projectName;
  }

  public List<String> getMergeScenarioCommits() {
    return mergeScenarioCommits;
  }

}
