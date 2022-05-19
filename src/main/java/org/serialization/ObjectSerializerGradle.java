package org.serialization;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.instrumentation.GradleBuildFileInstrumentation;
import org.util.DirUtils;
import org.util.JarManager;


public class ObjectSerializerGradle extends ObjectSerializer{
    protected GradleBuildFileInstrumentation gradleBuildFileInstrumentation;

    @Override
    protected void createBuildFileSupporters() throws TransformerException {
        buildFileDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "build.gradle");
        if (buildFileDirectory != null) {
            gradleBuildFileInstrumentation = new GradleBuildFileInstrumentation(buildFileDirectory.getPath());
            gradleBuildFileInstrumentation.addTestJarPlugin();
            gradleBuildFileInstrumentation.saveChanges();
        }
    }

    @Override
    protected boolean cleanResourceDirectory() {
        return resourceFileSupporter.deleteResourceDirectory();
    }

    @Override
    protected void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException {
        String command = "./gradlew clean test -Dtest.ignoreFailures=true";
        String message = "Creating Serialized Objects";
        boolean isTestTask = true;

        startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask);

        if (DirUtils.isDirEmpty(new File(objectSerializerSupporter.getResourceDirectory()).toPath())){
            gradleBuildFileInstrumentation.updateOldFatJarPlugin();
            gradleBuildFileInstrumentation.updateOldTestJarPlugin();
            gradleBuildFileInstrumentation.updateOldDependencies();
            gradleBuildFileInstrumentation.saveChanges();
            startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask);
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
    protected void generateJarFile() throws IOException, InterruptedException {
        if(!startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "./gradlew clean fatJar", "Generating jar file with serialized objects", false)){
            gradleBuildFileInstrumentation.updateOldFatJarPlugin();
            gradleBuildFileInstrumentation.updateOldDependencies();
            gradleBuildFileInstrumentation.saveChanges();
            startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "./gradlew clean fatJar", "Generating jar file with serialized objects", false);
        }
        generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "build" + File.separator + "libs");
    }

    @Override
    protected void generateTestFilesJar() throws IOException, InterruptedException, TransformerException {
        if(!startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "./gradlew clean testJar", "Generating test files jar", false)){
            gradleBuildFileInstrumentation.updateOldTestJarPlugin();
            gradleBuildFileInstrumentation.saveChanges();
            startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "./gradlew clean testJar", "Generating test files jar", false);
        }
        generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "build" + File.separator + "libs");
    }
    
}
