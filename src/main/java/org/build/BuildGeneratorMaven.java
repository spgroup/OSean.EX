package org.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.file.AssembyFileSupporter;
import org.transformations.instrumentation.PomFileInstrumentation;
import org.util.JarManager;
import org.util.ProcessManager;
import org.util.input.MergeScenarioUnderAnalysis;

public class BuildGeneratorMaven extends BuildGenerator {
  protected AssembyFileSupporter assemblyFileSupporter;
  protected PomFileInstrumentation pomFileInstrumentation;
  
  public BuildGeneratorMaven(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, ProcessManager processManager) throws TransformerException {
    super(mergeScenarioUnderAnalysis, processManager);
  }

  @Override
  protected void createBuildFileSupporters() throws TransformerException {
    buildFileDirectory = projectFileSupporter.findBuildFileDirectory(projectFileSupporter.getTargetClassLocalPath(),
        "pom.xml");
    if (buildFileDirectory != null) {
      assemblyFileSupporter = new AssembyFileSupporter(projectFileSupporter.getProjectLocalPath().getAbsolutePath());
      assemblyFileSupporter.createNewDirectory(buildFileDirectory);
      pomFileInstrumentation = new PomFileInstrumentation(buildFileDirectory.getPath());
      pomFileInstrumentation.addPluginForJarWithAllDependencies();
      pomFileInstrumentation.updateSourceOption(projectFileSupporter.getProjectLocalPath());
    }
  }

  @Override
  protected void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException {
    String command = "mvn clean test -Dmaven.test.failure.ignore=true";
    String message = "Creating Serialized Objects";
    boolean isTestTask = true;

    if (!processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask, true)) {
      System.out.println("Updating Surefire and MockitoCore plugins...");
      pomFileInstrumentation.changeSurefirePlugin(objectSerializationController.getTargetClassPackage());
      pomFileInstrumentation.changeMockitoCore();
      if (!processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask, true)) {
        System.out.println("Updating old dependencies...");
        pomFileInstrumentation.updateOldDependencies();
        processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask, true);
      }
    }
  }

  @Override
  protected void createAndRunBuildFileInstrumentation(File projectDir) throws TransformerException {
    pomFileInstrumentation = new PomFileInstrumentation(buildFileDirectory.getPath());
    pomFileInstrumentation.addRequiredDependenciesOnPOM();
    pomFileInstrumentation.changeAnimalSnifferPluginIfAdded();
    pomFileInstrumentation.addResourcesForGeneratedJar();
    pomFileInstrumentation.addPluginForJarWithAllDependencies();
    pomFileInstrumentation.updateOldRepository();
    pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "scope", "compile",
        new ArrayList<String>(Arrays.asList("test")));
    pomFileInstrumentation.removeAllEnforcedDependencies(projectDir);
    pomFileInstrumentation.updateSourceOption(projectDir);
  }

  @Override
  protected boolean generateJarFile() throws IOException, InterruptedException {
    boolean successfulAction = true;
    if (!processManager.startProcess(buildFileDirectory.getAbsolutePath(), "mvn clean compile test-compile assembly:single",
        "Generating jar file with tests", false, true)) {
      processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), "mvn clean compile",
          "Compiling the whole project", false, true);
      if (!processManager.startProcess(buildFileDirectory.getAbsolutePath(), "mvn compile assembly:single",
          "Generating jar file", false, true)) {
        successfulAction &= processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(),
            "mvn clean compile assembly:single", "Generating jar file in root", false, true);
      }
    }

    generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "target");
    return successfulAction;
  }

  @Override
  protected void generateTestFilesJar() throws IOException, InterruptedException, TransformerException {
    pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "descriptor",
        "src/main/assembly/assemblyTest.xml", new ArrayList<String>(Arrays.asList("src/main/assembly/assembly.xml")));
    processManager.startProcess(buildFileDirectory.getAbsolutePath(), "mvn clean compile test-compile",
        "Compiling target class module", false, true);

    String compiledSrcFilesPath = buildFileDirectory.getAbsolutePath() + File.separator + "target" + File.separator
        + "classes";
    File compiledSrcFilesDirectory = new File(compiledSrcFilesPath);

    try {
      FileUtils.deleteDirectory(compiledSrcFilesDirectory);
    } catch (IOException e) {
      e.printStackTrace();
    }

    processManager.startProcess(buildFileDirectory.getAbsolutePath(), "mvn assembly:single", "Generating test files jar", false, true);
    generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "target");
    pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "descriptor",
        "src/main/assembly/assembly.xml", new ArrayList<String>(Arrays.asList("src/main/assembly/assemblyTest.xml")));
  }

}
