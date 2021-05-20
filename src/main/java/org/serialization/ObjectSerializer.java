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
import org.util.input.MergeScenarioUnderAnalysis;

public class ObjectSerializer {

  public void startSerialization(List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses)
      throws IOException, InterruptedException, TransformerException {

    for(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis: mergeScenarioUnderAnalyses){
      GitProjectActions gitProjectActions = new GitProjectActions(mergeScenarioUnderAnalysis.getLocalProjectPath());

      assert gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0)) == true;
      FileFinderSupport fileFinderSupport = new FileFinderSupport(mergeScenarioUnderAnalysis.getLocalProjectPath());
      File pomDirectory = fileFinderSupport
          .findFile(mergeScenarioUnderAnalysis.getTargetClass(), fileFinderSupport.getProjectLocalPath());
      /*List<String> aux = new ArrayList<>();
      aux.add(gitProjectActions.getCurrentSHA());*/

      if (pomDirectory != null) {
        PomFileInstrumentation pomFileInstrumentation = createAndRunPomFileInstrumentation(pomDirectory);
        fileFinderSupport.createNewDirectory(pomDirectory);

        ObjectSerializerSupporter objectSerializerSupporter = createAndAddObjectSerializerSupporter(
            fileFinderSupport, pomFileInstrumentation);

        ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = createAndRunObjectSerializerInstrumentation(
            new File(fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()),
            new ObjectSerializerClassIntrumentation(mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializerSupporter.getFullSerializerSupporterClass()));

        SerializedObjectAccessClassIntrumentation serializedObjectAccessClassIntrumentation = new SerializedObjectAccessClassIntrumentation(
            mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializerSupporter.getFullSerializerSupporterClass());

        startProcess(fileFinderSupport, "mvn clean test -Dmaven.test.failure.ignore=true", "Creating Serialized Objects");

        objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());

        assert gitProjectActions.undoCurrentChanges() == true;
        assert gitProjectActions.checkoutPreviousSHA() == true;

        generateJarsForAllMergeScenarioCommits(gitProjectActions,
            pomDirectory, objectSerializerClassIntrumentation, fileFinderSupport,
            objectSerializerSupporter, serializedObjectAccessClassIntrumentation, mergeScenarioUnderAnalysis);

        assert gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0)) == true;

        fileFinderSupport.deleteResourceDirectory();
      }
    }
  }

  public boolean generateJarsForAllMergeScenarioCommits(GitProjectActions gitProjectActions,
      File pomDirectory, ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation,
      FileFinderSupport fileFinderSupport, ObjectSerializerSupporter objectSerializerSupporter,
      SerializedObjectAccessClassIntrumentation serializedObjectAccessClassIntrumentation, MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis) {

    try {
      for (String mergeScenarioCommit : mergeScenarioUnderAnalysis.getMergeScenarioCommits()) {

        assert gitProjectActions.checkoutCommit(mergeScenarioCommit) == true;

        PomFileInstrumentation pomFileInstrumentation = createAndRunPomFileInstrumentation(
            pomDirectory);

        runTestabilityTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));

        SerializedObjectAccessOutputClass serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();

        objectSerializerSupporter
            .getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
                fileFinderSupport
                    .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        objectSerializerClassIntrumentation.runTransformation(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));


        startProcess(fileFinderSupport, "mvn clean compile assembly:single", "Generating jar file with serialized objects");

        String generatedJarFile = JarManager.getJarFile(pomFileInstrumentation);

        startProcess(fileFinderSupport, "java -cp " + generatedJarFile
            + " " + getObjectClassPathOnTargetProject(objectSerializerClassIntrumentation), "Generating method list associated to serialized objects");

        List<String> aux = getMethodList(fileFinderSupport
            .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        objectSerializerClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
        objectSerializerSupporter.deleteObjectSerializerSupporterClass(
            fileFinderSupport.getTargetClassLocalPath().getPath());

        if (aux.size() > 0) {
          serializedObjectAccessOutputClass
              .getOutputClass(aux, fileFinderSupport.getTargetClassLocalPath().getPath(),
                  objectSerializerSupporter.getFullSerializerSupporterClass());
          serializedObjectAccessClassIntrumentation.addSupporterClassAsField(new File(
              fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
        }

        startProcess(fileFinderSupport, "mvn clean compile assembly:single", "Generating jar file with serialized objects");

        serializedObjectAccessOutputClass.deleteOldClassSupporter();
        serializedObjectAccessClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
        JarManager.saveGeneratedJarFile(generatedJarFile,
            mergeScenarioUnderAnalysis.getLocalProjectPath().split(mergeScenarioUnderAnalysis.getProjectName())[0] +
                File.separator + "GeneratedJars" + File.separator + mergeScenarioUnderAnalysis.getProjectName(), mergeScenarioCommit + ".jar");

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
