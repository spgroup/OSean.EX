package org.serialization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.file.AssembyFileSupporter;
import org.instrumentation.PomFileInstrumentation;
import org.util.JarManager;

public class ObjectSerializerMaven extends ObjectSerializer {
    protected AssembyFileSupporter assemblyFileSupporter;
    protected PomFileInstrumentation pomFileInstrumentation;

    @Override
    protected void createBuildFileSupporters() throws TransformerException {
        buildFileDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml"); 
        if (buildFileDirectory != null) {
          assemblyFileSupporter = new AssembyFileSupporter(resourceFileSupporter.getProjectLocalPath().getAbsolutePath());
          assemblyFileSupporter.createNewDirectory(buildFileDirectory);
          pomFileInstrumentation = new PomFileInstrumentation(buildFileDirectory.getPath());
          pomFileInstrumentation.addPluginForJarWithAllDependencies();
          pomFileInstrumentation.updateSourceOption(resourceFileSupporter.getProjectLocalPath());
        }
      }
      
    @Override
    protected void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException {
        String command = "mvn clean test -Dmaven.test.failure.ignore=true";
        String message = "Creating Serialized Objects";
        boolean isTestTask = true;

        if (!startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask)){
          System.out.println("Updating Surefire and MockitoCore plugins...");
          pomFileInstrumentation.changeSurefirePlugin(objectSerializerSupporter.getClassPackage());
          pomFileInstrumentation.changeMockitoCore();
          if(!startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask)){
            System.out.println("Updating old dependencies...");
            pomFileInstrumentation.updateOldDependencies();
            startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask);
          }
        }
    }

    @Override
    protected boolean cleanResourceDirectory() {
      return resourceFileSupporter.deleteResourceDirectory() && assemblyFileSupporter.deleteResourceDirectory();
    }

    @Override
    protected void createAndRunBuildFileInstrumentation(File projectDir) throws TransformerException {
      pomFileInstrumentation = new PomFileInstrumentation(buildFileDirectory.getPath());
      pomFileInstrumentation.addRequiredDependenciesOnPOM();
      pomFileInstrumentation.changeAnimalSnifferPluginIfAdded();
      pomFileInstrumentation.addResourcesForGeneratedJar();
      pomFileInstrumentation.addPluginForJarWithAllDependencies();
      pomFileInstrumentation.updateOldRepository();
      pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "scope", "compile", new ArrayList<String>(Arrays.asList("test")));
      pomFileInstrumentation.removeAllEnforcedDependencies(projectDir);
      pomFileInstrumentation.updateSourceOption(projectDir);
    }

    @Override
    protected boolean generateJarFile() throws IOException, InterruptedException {
      boolean successfulAction = true;
      if (!startProcess(buildFileDirectory.getAbsolutePath(), "mvn clean compile test-compile assembly:single", "Generating jar file with serialized objects and tests", false)){
        startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "mvn clean compile", "Compiling the whole project", false);
        if (!startProcess(buildFileDirectory.getAbsolutePath(), "mvn compile assembly:single", "Generating jar file with serialized objects", false)){
          successfulAction &= startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "mvn clean compile assembly:single", "Generating jar file with serialized objects in root", false);
        }
      }

      generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "target");
      return successfulAction;
    }

    @Override
    protected void generateTestFilesJar() throws IOException, InterruptedException, TransformerException {
      pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "descriptor", "src/main/assembly/assemblyTest.xml", new ArrayList<String>(Arrays.asList("src/main/assembly/assembly.xml")));
      startProcess(buildFileDirectory.getAbsolutePath(), "mvn clean compile test-compile", "Compiling target class module", false);
      
      String compiledSrcFilesPath = buildFileDirectory.getAbsolutePath() + File.separator + "target" + File.separator + "classes";
      File compiledSrcFilesDirectory = new File(compiledSrcFilesPath);
      
      try {
        FileUtils.deleteDirectory(compiledSrcFilesDirectory);
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      startProcess(buildFileDirectory.getAbsolutePath(), "mvn assembly:single", "Generating test files jar", false);
      generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "target");
      pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "descriptor", "src/main/assembly/assembly.xml", new ArrayList<String>(Arrays.asList("src/main/assembly/assemblyTest.xml")));
    }

}
