package org.build;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.tranformations.instrumentation.GradleBuildFileInstrumentation;
import org.util.JarManager;
import org.util.ProcessManager;
import org.util.input.MergeScenarioUnderAnalysis;

public class BuildGeneratorGradle extends BuildGenerator {
    protected GradleBuildFileInstrumentation gradleBuildFileInstrumentation;

    public BuildGeneratorGradle(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, ProcessManager processManager)
            throws TransformerException {
        super(mergeScenarioUnderAnalysis, processManager);
    }

    @Override
    protected void createBuildFileSupporters() throws TransformerException {
        buildFileDirectory = projectFileSupporter.findBuildFileDirectory(projectFileSupporter.getTargetClassLocalPath(),
                "build.gradle");
        if (buildFileDirectory != null) {
            gradleBuildFileInstrumentation = new GradleBuildFileInstrumentation(buildFileDirectory.getPath());
            gradleBuildFileInstrumentation.addTestJarPlugin();
            gradleBuildFileInstrumentation.saveChanges();
        }
    }

    @Override
    protected void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException {
        String command = "./gradlew clean test -Dtest.ignoreFailures=true";
        String message = "Creating Serialized Objects";
        boolean isTestTask = true;

        if (!processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), command, message,
                isTestTask, true)) {
            System.out.println("Updating plugins and dependecies to new versions of Gradle...");
            gradleBuildFileInstrumentation.updateOldFatJarPlugin();
            gradleBuildFileInstrumentation.updateOldTestJarPlugin();
            gradleBuildFileInstrumentation.updateOldDependencies();
            gradleBuildFileInstrumentation.saveChanges();
            processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), command, message,
                    isTestTask, true);
        }
    }

    @Override
    protected void createAndRunBuildFileInstrumentation(File projectLocalPath) throws TransformerException {
        gradleBuildFileInstrumentation = new GradleBuildFileInstrumentation(buildFileDirectory.getPath());
        gradleBuildFileInstrumentation.addRequiredDependencies();
        gradleBuildFileInstrumentation.addFatJarPlugin();
        gradleBuildFileInstrumentation.saveChanges();
    }

    @Override
    protected boolean generateJarFile() throws IOException, InterruptedException {
        boolean successfulAction = true;
        if (!processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), "./gradlew clean fatJar",
                "Generating jar file", false, true)) {
            System.out.println("Updating plugins and dependecies to new versions of Gradle...");
            gradleBuildFileInstrumentation.updateOldFatJarPlugin();
            gradleBuildFileInstrumentation.updateOldDependencies();
            gradleBuildFileInstrumentation.saveChanges();
            successfulAction &= processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(),
                    "./gradlew clean fatJar", "Generating jar file", false, true);
        }
        generatedJarFile = JarManager
                .getJarFile(buildFileDirectory.getPath() + File.separator + "build" + File.separator + "libs");
        return successfulAction;
    }

    @Override
    protected void generateTestFilesJar() throws IOException, InterruptedException, TransformerException {
        if (!processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(),
                "./gradlew clean testJar",
                "Generating test files jar", false, true)) {
            System.out.println("Updating plugins and dependecies to new versions of Gradle...");
            gradleBuildFileInstrumentation.updateOldTestJarPlugin();
            gradleBuildFileInstrumentation.saveChanges();
            processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), "./gradlew clean testJar",
                    "Generating test files jar", false, true);
        }
        generatedJarFile = JarManager
                .getJarFile(buildFileDirectory.getPath() + File.separator + "build" + File.separator + "libs");
    }

}
