package org.cli;

import org.apache.commons.cli.*;

public class CommandLineParametersParser {
    private final Options options;

    public CommandLineParametersParser() {
        options = new Options();

        Option localProjectPath = Option.builder().option("l").longOpt("localProjectPath").hasArg().desc("Local project path").required().build();
        Option targetClassName = new Option("c", "targetClassName", true, "Target Class Name");
        Option projectName = new Option("l", "localProjectPath", true, "Local project path");
        Option commits = new Option("l", "localProjectPath", true, "Local project path");

        options
                .addOption(localProjectPath)
                .addOption(targetClassName)
                .addOption(projectName)
                .addOption(commits);
    }

    public CommandLineParameters parseParametersFromArguments(String[] args) {
        return CommandLineParameters.builder().build();
    }
}
