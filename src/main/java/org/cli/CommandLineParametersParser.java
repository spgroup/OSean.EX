package org.cli;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public class CommandLineParametersParser {
  private final Options options;
  private final CommandLineParser parser;
  private final HelpFormatter formatter;

  public CommandLineParametersParser() {
    options = new Options();
    parser = new DefaultParser();
    formatter = new HelpFormatter();

    Option localProjectPath = Option.builder()
        .option("l").longOpt("localProjectPath").hasArg()
        .desc("Local project path").required().build();
    Option targetClassName = Option.builder()
        .option("c").longOpt("targetClassName").hasArg()
        .desc("Target Class Name").required().build();
    Option targetMethodName = Option.builder().option("m").longOpt("targetMethodName").hasArg()
        .desc("Target Method Name").required().build();
    Option projectName = Option.builder().option("p").longOpt("projectName").hasArg()
        .desc("Project name").required().build();
    Option applyTestabilityTransformations = Option.builder().option("att")
        .longOpt("applyTestabilityTransformations")
        .desc("Apply testability transformations").build();
    Option fullyApplyTestabilityTransformations = Option.builder()
        .option("fatt")
        .longOpt("fullyApplyTestabilityTransformations")
        .desc("Fully apply testability transformations").build();
    Option budgetTime = Option.builder()
        .option("b")
        .longOpt("budgetTime")
        .desc("Customize the budget time (Default: 60s)")
        .hasArg().build();
    Option isGradle = Option.builder()
        .option("g")
        .longOpt("gradle")
        .desc("Indicates if Gradle is the used build tool")
        .build();
    Option commits = new Option("h", "commits", true, "Comma separated list of commits");

    options
        .addOption(localProjectPath)
        .addOption(targetClassName)
        .addOption(targetMethodName)
        .addOption(projectName)
        .addOption(applyTestabilityTransformations)
        .addOption(fullyApplyTestabilityTransformations)
        .addOption(budgetTime)
        .addOption(isGradle)
        .addOption(commits);
  }

  public MergeScenarioUnderAnalysis parse(String[] args) {
    try {
      CommandLine cmd = parser.parse(options, args);

      TransformationOption transformationOption = TransformationOption.builder()
          .applyTransformations(cmd.hasOption("att"))
          .applyFullTransformations(cmd.hasOption("fatt"))
          .budget(Integer.parseInt(cmd.getOptionValue("b", "60")))
          .build();

      return MergeScenarioUnderAnalysis.builder()
          .localProjectPath(cmd.getOptionValue("l"))
          .targetClass(cmd.getOptionValue("c"))
          .targetMethod(cmd.getOptionValue("m"))
          .projectName(cmd.getOptionValue("p"))
          .transformationOption(transformationOption)
          .buildManager(cmd.hasOption("gradle") ? "gradle" : "maven")
          .mergeScenarioCommits(List.of(cmd.getOptionValue("h").split(",")))
          .build();
    } catch (ParseException e) {
        System.out.println(e.getMessage());
      formatter.printHelp("OSean.EX", options);
      throw new IllegalArgumentException();
    }
  }
}
