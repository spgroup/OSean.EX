package org.cli;

import lombok.Builder;

import java.util.List;

@Builder
public class CommandLineParameters {
    /**
     * The local path of the project under analysis.
     */
    final String localProjectPath;

    /**
     * The class holding the method in which the instrumentation will be applied.
     */
    final String targetClassName;

    /**
     * The method where the instrumentation will be applied
     */
    final String projectName;

    final boolean applyTestabilityTransformations;
    final boolean applyFullTestabilityTransformations;
    final int budgetTime;
    final String buildTool;
    final List<String> commits;

    public CommandLineParameters(String localProjectPath, String targetClassName, String projectName, boolean applyTestabilityTransformations, boolean applyFullTestabilityTransformations, int budgetTime, String buildTool, List<String> commits) {
        this.localProjectPath = localProjectPath;
        this.targetClassName = targetClassName;
        this.projectName = projectName;
        this.applyTestabilityTransformations = applyTestabilityTransformations;
        this.applyFullTestabilityTransformations = applyFullTestabilityTransformations;
        this.budgetTime = budgetTime;
        this.buildTool = buildTool;
        this.commits = commits;
    }
}
