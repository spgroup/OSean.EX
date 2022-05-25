package org.cli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

  private List<Option> requiredOptions = new ArrayList<>();

  public CommandLineParametersParser() {
    options = new Options();
    parser = new DefaultParser();
    formatter = new HelpFormatter();

    Option localProjectPath = Option.builder()
        .option("l").longOpt("localProjectPath").hasArg()
        .desc("Local project path")
        .build();
    Option targetClassName = Option.builder()
        .option("c").longOpt("targetClassName").hasArg()
        .desc("Target Class Name").build();
    Option targetMethodName = Option.builder().option("m").longOpt("targetMethodName").hasArg()
        .desc("Target Method Name").build();
    Option projectName = Option.builder().option("p").longOpt("projectName").hasArg()
        .desc("Project name").build();
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
    Option commits = Option.builder()
        .longOpt("commits")
        .desc("Comma separated list of commits")
        .hasArg()
        .build();
    Option batchCsv = Option.builder()
        .longOpt("batchCsv")
        .desc("A path to a CSV file containing the input")
        .hasArg()
        .build();
    Option help = Option.builder()
        .option("h")
        .longOpt("help")
        .desc("Shows this help message")
        .build();

    options
        .addOption(localProjectPath)
        .addOption(targetClassName)
        .addOption(targetMethodName)
        .addOption(projectName)
        .addOption(applyTestabilityTransformations)
        .addOption(fullyApplyTestabilityTransformations)
        .addOption(budgetTime)
        .addOption(isGradle)
        .addOption(commits)
        .addOption(batchCsv)
        .addOption(help);

    requiredOptions.add(localProjectPath);
    requiredOptions.add(targetClassName);
    requiredOptions.add(targetMethodName);
    requiredOptions.add(projectName);
    requiredOptions.add(commits);
  }

  public List<MergeScenarioUnderAnalysis> parse(String[] args) {
    try {
      List<MergeScenarioUnderAnalysis> scenarios = new ArrayList<>();

      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("help")) {
        formatter.printHelp("OSean.EX", options);
        return scenarios;
      }

      if (cmd.hasOption("batchCsv")) {
        return readInputFromCsvFile(cmd.getOptionValue("batchCsv"));
      }

      requiredOptions.forEach(option -> {
        if (!cmd.hasOption(option)) {
          throw new IllegalArgumentException(option.getLongOpt() + " is required");
        }
      });

      TransformationOption transformationOption = TransformationOption.builder()
          .applyTransformations(cmd.hasOption("att"))
          .applyFullTransformations(cmd.hasOption("fatt"))
          .budget(Integer.parseInt(cmd.getOptionValue("b", "60")))
          .build();

      scenarios.add(MergeScenarioUnderAnalysis.builder()
          .localProjectPath(cmd.getOptionValue("l"))
          .targetClass(cmd.getOptionValue("c"))
          .targetMethod(cmd.getOptionValue("m"))
          .projectName(cmd.getOptionValue("p"))
          .transformationOption(transformationOption)
          .buildManager(cmd.hasOption("gradle") ? "gradle" : "maven")
          .mergeScenarioCommits(List.of(cmd.getOptionValue("commits").split(",")))
          .build());

      return scenarios;
    } catch (ParseException e) {
      formatter.printHelp("OSean.EX", options);
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private List<MergeScenarioUnderAnalysis> readInputFromCsvFile(String filePath) throws IllegalArgumentException {
    try {
      List<MergeScenarioUnderAnalysis> mergeScenariosUnderAnalysis = new ArrayList<>();
      BufferedReader csvReader = new BufferedReader(new FileReader(filePath));
      String row;
      while ((row = csvReader.readLine()) != null) {
        String[] input = row.split(",");

        mergeScenariosUnderAnalysis.add(MergeScenarioUnderAnalysis.builder()
            .localProjectPath(input[0])
            .targetClass(input[1])
            .targetMethod(input[2])
            .projectName(input[3])
            .transformationOption(
                TransformationOption.builder()
                    .applyTransformations(input[4].equals("true"))
                    .applyFullTransformations(input[5].equals("true"))
                    .budget(Integer.parseInt(input[6]))
                    .build())
            .buildManager(input[7].equals("true") ? "gradle" : "maven")
            .mergeScenarioCommits(List.of(input[8].split(" ")))
            .build());
      }
      csvReader.close();

      return mergeScenariosUnderAnalysis;
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("The provided file could not be found");
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
