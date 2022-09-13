package org.util.input;

import java.util.List;

import lombok.Builder;

@Builder
public class MergeScenarioUnderAnalysis {
  private String localProjectPath;
  private String targetClass;
  private String targetMethod;
  private String projectName;
  private String buildManager;
  private List<String> mergeScenarioCommits;
  private TransformationOption transformationOption;
  private boolean serialize;

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

  public String getBuildManager() {
    return buildManager;
  }

  public List<String> getMergeScenarioCommits() {
    return mergeScenarioCommits;
  }

  public TransformationOption getTransformationOption() {
    return transformationOption;
  }

  public boolean getSerialize() {
    return serialize;
  }
}
