package org.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import org.Transformations;
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

      gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));

      FileFinderSupport fileFinderSupport = new FileFinderSupport(mergeScenarioUnderAnalysis.getLocalProjectPath());
      File pomDirectory = fileFinderSupport
          .findFile(mergeScenarioUnderAnalysis.getTargetClass(), fileFinderSupport.getProjectLocalPath());

      if (pomDirectory != null) {
        PomFileInstrumentation pomFileInstrumentation = createAndRunPomFileInstrumentation(pomDirectory);
        fileFinderSupport.createNewDirectory(pomDirectory);

        runTestabilityTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));

        ObjectSerializerSupporter objectSerializerSupporter = createAndAddObjectSerializerSupporter(
            fileFinderSupport, pomFileInstrumentation);

        ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = createAndRunObjectSerializerInstrumentation(
            new File(fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()),
            new ObjectSerializerClassIntrumentation(mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializerSupporter.getFullSerializerSupporterClass()));

        applyTestabilityTransformationsTargetClasses(fileFinderSupport, objectSerializerClassIntrumentation.getTargetClasses());

        SerializedObjectAccessClassIntrumentation serializedObjectAccessClassIntrumentation = new SerializedObjectAccessClassIntrumentation(
            mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializerSupporter.getFullSerializerSupporterClass());

        startProcess(fileFinderSupport.getProjectLocalPath().getPath(), "mvn clean test -Dmaven.test.failure.ignore=true", "Creating Serialized Objects", true);

        objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());

        gitProjectActions.undoCurrentChanges();
        gitProjectActions.checkoutPreviousSHA();

        generateJarsForAllMergeScenarioCommits(gitProjectActions,
            pomDirectory, objectSerializerClassIntrumentation, fileFinderSupport,
            objectSerializerSupporter, serializedObjectAccessClassIntrumentation, mergeScenarioUnderAnalysis);

        gitProjectActions.undoCurrentChanges();
        gitProjectActions.checkoutPreviousSHA();

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

        gitProjectActions.checkoutCommit(mergeScenarioCommit);

        PomFileInstrumentation pomFileInstrumentation = createAndRunPomFileInstrumentation(
            pomDirectory);

        runTestabilityTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
        applyTestabilityTransformationsTargetClasses(fileFinderSupport, objectSerializerClassIntrumentation.getTargetClasses());

        SerializedObjectAccessOutputClass serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();

        objectSerializerSupporter
            .getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
                fileFinderSupport
                    .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        objectSerializerClassIntrumentation.runTransformation(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));


        startProcess(pomDirectory.getAbsolutePath(), "mvn clean compile assembly:single", "Generating jar file with serialized objects", false);

        String generatedJarFile = JarManager.getJarFile(pomFileInstrumentation);

        startProcess(fileFinderSupport.getProjectLocalPath().getPath(), "java -cp " + generatedJarFile
            + " " + getObjectClassPathOnTargetProject(objectSerializerClassIntrumentation), "Generating method list associated to serialized objects", false);

        List<String> methodList = getMethodList(fileFinderSupport, pomFileInstrumentation.getPomFileDirectory());

        objectSerializerClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator
                + mergeScenarioUnderAnalysis.getTargetClass()));
        objectSerializerSupporter.deleteObjectSerializerSupporterClass(
            fileFinderSupport.getTargetClassLocalPath().getPath());

        if (methodList.size() > 0) {
          serializedObjectAccessOutputClass
              .getOutputClass(methodList, fileFinderSupport.getTargetClassLocalPath().getPath(),
                  objectSerializerSupporter.getFullSerializerSupporterClass());
          serializedObjectAccessClassIntrumentation.addSupporterClassAsField(new File(
              fileFinderSupport.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
        }

        if (startProcess(pomDirectory.getAbsolutePath(), "mvn clean compile assembly:single", "Generating jar file with serialized objects", false)) {

          serializedObjectAccessOutputClass.deleteOldClassSupporter();
          serializedObjectAccessClassIntrumentation.undoTransformations(new File(
              fileFinderSupport.getTargetClassLocalPath() + File.separator
                  + mergeScenarioUnderAnalysis.getTargetClass()));
          JarManager.saveGeneratedJarFile(generatedJarFile,
              mergeScenarioUnderAnalysis.getLocalProjectPath()
                  .split(mergeScenarioUnderAnalysis.getProjectName())[0] +
                  File.separator + "GeneratedJars" + File.separator
                  + mergeScenarioUnderAnalysis.getProjectName(), mergeScenarioCommit + ".jar");
        }
        gitProjectActions.undoCurrentChanges();
        gitProjectActions.checkoutPreviousSHA();
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
    pomFileInstrumentation.changeAnimalSnifferPluginIfAdded();
    pomFileInstrumentation.addResourcesForGeneratedJar();
    pomFileInstrumentation.addPluginForJarWithAllDependencies();
    pomFileInstrumentation.changeSurefirePlugin();

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

  private List<String> getMethodList(FileFinderSupport fileFinderSupport, File pom){
    String resourceDirectory =  fileFinderSupport
        .getResourceDirectoryPath(pom);
    List<String> methods = new ArrayList<>();
    List<String> serializedObjectTypes = new ArrayList<>();

    Pattern pattern = Pattern.compile("public [0-9a-zA-Z\\.]* deserialize", Pattern.CASE_INSENSITIVE);
    Matcher matcher;

    if (new File(resourceDirectory+File.separator+"output-methods.txt").exists()){
      try {
        File file = new File(resourceDirectory+File.separator+"output-methods.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          String nextLine = myReader.nextLine();
          matcher = pattern.matcher(nextLine);
          if (matcher.find()) {
            int index = matcher.group(0).lastIndexOf(".");
            String objectType = matcher.group(0).substring(index+1).replace(" deserialize", "");
            if (!serializedObjectTypes.contains(objectType))
              serializedObjectTypes.add(objectType);
          }
          methods.add(nextLine);
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    runTestabilityTransformationsForSerializedObjectClasses(fileFinderSupport, serializedObjectTypes);
    return methods;
  }

  private void runTestabilityTransformationsForSerializedObjectClasses(FileFinderSupport fileFinderSupport, List<String> serializedObjects){
    for(String serializedObject: serializedObjects){
      File serializedObjectFile = fileFinderSupport.searchForFileByName(serializedObject+".java", fileFinderSupport.getProjectLocalPath());
      if (serializedObjectFile != null){
        runTestabilityTransformations(serializedObjectFile);
      }
    }
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

  private void applyTestabilityTransformationsTargetClasses(FileFinderSupport fileFinderSupport, List<String> classes){
    for(String targetClass: classes){
      File targetClassFile = fileFinderSupport.searchForFileByName(targetClass+".java", fileFinderSupport.getProjectLocalPath());
      if (targetClassFile != null){
       runTestabilityTransformations(targetClassFile);
      }
    }
  }

  private boolean startProcess(String directoryPath, String command, String message, boolean isTestTask)
      throws IOException, InterruptedException {
    Process process = Runtime.getRuntime()
        .exec(command, null,
            new File(directoryPath));
    return ProcessManager.computeProcessOutput(process, message, isTestTask);
  }

}
