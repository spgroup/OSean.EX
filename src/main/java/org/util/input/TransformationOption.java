package org.util.input;

public class TransformationOption {
  private boolean applyTransformations;
  private boolean applyFullTransformations;
  private int budget;

  public TransformationOption(String applyTransformations, String applyFullTransformations, String budget){
    this.applyTransformations = this.isOptionTrue(applyTransformations);
    this.applyFullTransformations = this.isOptionTrue(applyFullTransformations);
    this.budget = Integer.valueOf(budget);
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

  public int getBudget(){
    return this.budget;
  }
}
