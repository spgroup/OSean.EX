package org.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.transform.TransformerException;
import org.Transformations;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.file.FileFinderSupport;
import org.file.ObjectSerializerSupporter;
import org.file.SerializedObjectAccessOutputClass;
import org.instrumentation.ObjectSerializerClassIntrumentation;
import org.instrumentation.PomFileInstrumentation;
import org.instrumentation.SerializedObjectAccessClassIntrumentation;
import org.util.GitProjectActions;
import org.util.JarManager;
import org.util.ProcessManager;

public class ObjectSerializer {

  public void startSerialization(String targetProjectPath, String targetClass, String targetMethod, String targetProjectName)
      throws IOException, InterruptedException, TransformerException {

      GitProjectActions gitProjectActions = new GitProjectActions(targetProjectPath);

      FileFinderSupport fileFinderSupport = new FileFinderSupport(targetProjectPath);
      File pomDirectory = fileFinderSupport
          .findFile(targetClass, fileFinderSupport.getProjectLocalPath());
      List<String> aux = new ArrayList<>();
      aux.add(gitProjectActions.getCurrentSHA());

      if (pomDirectory != null) {
        PomFileInstrumentation pomFileInstrumentation = createAndRunPomFileInstrumentation(pomDirectory);
        fileFinderSupport.createNewDirectory(pomDirectory);

        ObjectSerializerSupporter objectSerializerSupporter = createAndAddObjectSerializerSupporter(
            fileFinderSupport, pomFileInstrumentation);

        ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = createAndRunObjectSerializerInstrumentation(
            new File(fileFinderSupport.getTargetClassLocalPath() + File.separator + targetClass),
            new ObjectSerializerClassIntrumentation(targetMethod, objectSerializerSupporter.getFullSerializerSupporterClass()));

        SerializedObjectAccessClassIntrumentation serializedObjectAccessClassIntrumentation = new SerializedObjectAccessClassIntrumentation(
            targetMethod, objectSerializerSupporter.getFullSerializerSupporterClass());

        startProcess(fileFinderSupport, "mvn clean test -Dmaven.test.failure.ignore=true", "Creating Serialized Objects");

        objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());

        gitProjectActions.undoCurrentChanges();
        gitProjectActions.checkoutPreviousSHA();

        generateJarsForAllMergeScenarioCommits(gitProjectActions, aux,
            pomDirectory, objectSerializerClassIntrumentation, fileFinderSupport,
            objectSerializerSupporter, serializedObjectAccessClassIntrumentation, targetProjectPath, targetClass,
            targetMethod, targetProjectName);

        fileFinderSupport.deleteResourceDirectory();
    }
  }

  public boolean generateJarsForAllMergeScenarioCommits(GitProjectActions gitProjectActions, List<String> mergeScenarioCommits,
      File pomDirectory, ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation,
      FileFinderSupport fileFinderSupport, ObjectSerializerSupporter objectSerializerSupporter,
      SerializedObjectAccessClassIntrumentation serializedObjectAccessClassIntrumentation, String targetProjectPath,
      String targetClass, String targetMethod, String targetProjectName) {

    try {
      for (String mergeScenarioCommit : mergeScenarioCommits) {

        assert gitProjectActions.checkoutCommit(mergeScenarioCommit) == true;

        PomFileInstrumentation pomFileInstrumentation = createAndRunPomFileInstrumentation(
            pomDirectory);

        runTestabilityTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + targetClass));

        SerializedObjectAccessOutputClass serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();

        objectSerializerSupporter
            .getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
                fileFinderSupport
                    .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        objectSerializerClassIntrumentation.runTransformation(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + targetClass));


        startProcess(fileFinderSupport, "mvn clean compile assembly:single", "Generating jar file with serialized objects");

        String generatedJarFile = JarManager.getJarFile(pomFileInstrumentation);

        startProcess(fileFinderSupport, "java -cp " + generatedJarFile
            + " " + getObjectClassPathOnTargetProject(objectSerializerClassIntrumentation), "Generating method list associated to serialized objects");

        List<String> aux = getMethodList(fileFinderSupport
            .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        objectSerializerClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + targetClass));
        objectSerializerSupporter.deleteObjectSerializerSupporterClass(
            fileFinderSupport.getTargetClassLocalPath().getPath());

        if (aux.size() > 0) {
          serializedObjectAccessOutputClass
              .getOutputClass(aux, fileFinderSupport.getTargetClassLocalPath().getPath(),
                  objectSerializerSupporter.getFullSerializerSupporterClass());
          serializedObjectAccessClassIntrumentation.addSupporterClassAsField(new File(
              fileFinderSupport.getTargetClassLocalPath() + File.separator + targetClass));
        }

        startProcess(fileFinderSupport, "mvn clean compile assembly:single", "Generating jar file with serialized objects");

        serializedObjectAccessOutputClass.deleteOldClassSupporter();
        serializedObjectAccessClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + targetClass));
        JarManager.saveGeneratedJarFile(generatedJarFile,
            targetProjectPath.split(targetProjectName)[0] + File.separator + "GeneratedJars" + File.separator + targetProjectName,
            mergeScenarioCommit + ".jar");

        assert gitProjectActions.undoCurrentChanges() == true;
        assert gitProjectActions.checkoutPreviousSHA() == true;
      }
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  private PomFileInstrumentation createAndRunPomFileInstrumentation(File pomDirectory)
      throws TransformerException {
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
        pomDirectory.getPath());
    pomFileInstrumentation.addRequiredDependenciesOnPOM();
    pomFileInstrumentation.addResourcesForGeneratedJar();
    pomFileInstrumentation.addPluginForJarWithAllDependencies();

    return pomFileInstrumentation;
  }

  private ObjectSerializerClassIntrumentation createAndRunObjectSerializerInstrumentation(File file,
      ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation1) throws IOException {
    ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = objectSerializerClassIntrumentation1;
    objectSerializerClassIntrumentation.runTransformation(file);
    return objectSerializerClassIntrumentation;
  }

  private ObjectSerializerSupporter createAndAddObjectSerializerSupporter(
      FileFinderSupport fileFinderSupport, PomFileInstrumentation pomFileInstrumentation) {
    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(fileFinderSupport.getProjectLocalPath().getPath() + File.separator + "src" +
            File.separator + "main" + File.separator + "java")
            .relativize(Paths.get(fileFinderSupport.
                getTargetClassLocalPath().getPath())).toString().replace(File.separator, "."));
    objectSerializerSupporter
        .getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
            fileFinderSupport
                .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));
    return objectSerializerSupporter;
  }

  private List<String> getMethodList(String resourceDirectory){
    List<String> methods = new ArrayList<>();
    if (new File(resourceDirectory+File.separator+"output-methods.txt").exists()){
      try {
        File file = new File(resourceDirectory+File.separator+"output-methods.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          methods.add(myReader.nextLine());
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    return methods;
  }

  private String getObjectClassPathOnTargetProject(
      ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation) {
    return objectSerializerClassIntrumentation.getPackageName() + File.separator
        + "ObjectSerializerSupporter";
  }

  private boolean runTestabilityTransformations(File file){
    System.out.print("Applying Testability Transformations : ");
    try {
      Transformations.main(new String[]{new String(file.getPath())});
      System.out.println("SUCCESSFUL");
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("UNSUCCESSFUL");
    return false;
  }

  private void startProcess(FileFinderSupport fileFinderSupport, String command, String message)
      throws IOException, InterruptedException {
    Process process = Runtime.getRuntime()
        .exec(command, null,
            new File(fileFinderSupport.getProjectLocalPath().getPath()));
    ProcessManager.computeProcessOutput(process, message);
  }

}
