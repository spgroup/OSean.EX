package org.file;

import org.junit.Test;
import org.RunSerialization;
import org.cli.CommandLineParametersParser;

public class BuildFileExceptionTest {

    @Test(expected = BuildFileException.class)
    public void expectBuildFileExceptionIfProjectDoesntUseMaverOrGradle() throws BuildFileException{
        String[] args = {
            "-l",
            "src/test/resources/project-no-pom",
            "-c",
            "Person",
            "-m",
            "getName",
            "-p",
            "toy-project-1",
            "--commits",
            "c257474a8206e05d82a444bead4222d1dec9f60b"
        };
        RunSerialization.getObjectSerializer(new CommandLineParametersParser().parse(args).get(0));
    }
}
