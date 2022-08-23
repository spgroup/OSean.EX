package org.util.input;

import lombok.Builder;

@Builder
public class TransformationOption {
  private boolean applyTransformations;
  private boolean applyFullTransformations;
  private int budget;

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
