package org.util.input;

public class TransformationOption {
  private boolean applyTransformations;
  private boolean applyFullTransformations;

  public TransformationOption(String applyTransformations, String applyFullTransformations){
    this.applyTransformations = this.isOptionTrue(applyTransformations);
    this.applyFullTransformations = this.isOptionTrue(applyFullTransformations);
  }

  private boolean isOptionTrue(String option){
    return Boolean.valueOf(option) ? true : false;
  }

  public boolean applyTransformations() {
    return applyTransformations;
  }

  public boolean applyFullTransformations() {
    return applyFullTransformations;
  }

}
