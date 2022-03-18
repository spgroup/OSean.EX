package org.serialization;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.file.AssembyFileSupporter;
import org.instrumentation.PomFileInstrumentation;
import org.util.InputHandler;
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
            createAndRunBuildFileInstrumentation(resourceFileSupporter.getProjectLocalPath());
        }
    }
    
    @Override
    protected void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException {
        String command = "mvn clean test -Dmaven.test.failure.ignore=true";
        String message = "Creating Serialized Objects";
        boolean isTestTask = true;

        startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask);
        
        if (InputHandler.isDirEmpty(new File(objectSerializerSupporter.getResourceDirectory()).toPath())){
          pomFileInstrumentation.changeSurefirePlugin(objectSerializerSupporter.getClassPackage());
          pomFileInstrumentation.changeMockitoCore();
          startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask);
        }
    
        if (InputHandler.isDirEmpty(new File(objectSerializerSupporter.getResourceDirectory()).toPath())){
          pomFileInstrumentation.updateOldDependencies();
          startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), command, message, isTestTask);
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
      pomFileInstrumentation.changeTagContent(pomFileInstrumentation.getPomFile(), "scope", "compile", "test");
      pomFileInstrumentation.removeAllEnforcedDependencies(projectDir);
      pomFileInstrumentation.updateSourceOption(projectDir);
    }

    @Override
    protected void generateJarFile() throws IOException, InterruptedException {
      if (!startProcess(buildFileDirectory.getAbsolutePath(), "mvn clean compile test-compile assembly:single", "Generating jar file with serialized objects", false)){
        startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "mvn clean compile", "Compiling the whole project", false);
        if (!startProcess(buildFileDirectory.getAbsolutePath(), "mvn compile assembly:single", "Generating jar file with serialized objects", false)){
          startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "mvn clean compile assembly:single", "Generating jar file with serialized objects", false);
        }
      }

      generatedJarFile = JarManager.getJarFile(buildFileDirectory.getPath() + File.separator + "target");
    }

}
