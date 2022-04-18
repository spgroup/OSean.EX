package org.serialization;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import org.Transformations;
import org.file.ConverterSupporter;
import org.file.ObjectDeserializerSupporter;
import org.file.ObjectSerializerSupporter;
import org.file.ResourceFileSupporter;
import org.file.SerializedObjectAccessOutputClass;
import org.instrumentation.ObjectSerializerClassIntrumentation;
import org.instrumentation.SerializedObjectAccessClassIntrumentation;
import org.util.GitProjectActions;
import org.util.JarManager;
import org.util.ProcessManager;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public abstract class ObjectSerializer {
  public GitProjectActions gitProjectActions;
  public ResourceFileSupporter resourceFileSupporter;
  public File buildFileDirectory;
  public ProcessManager processManager;
  public ObjectSerializerSupporter objectSerializerSupporter;
  public ObjectDeserializerSupporter objectDeserializerSupporter;
  public ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation;
  public SerializedObjectAccessClassIntrumentation serializedObjectAccessClassIntrumentation;
  public String generatedJarFile;
  public ArrayList<String> testFilesNames = new ArrayList<>();
  
  protected abstract void createBuildFileSupporters() throws TransformerException;
  
  protected abstract void generateTestFilesJar() throws IOException, InterruptedException, TransformerException;

  protected abstract boolean cleanResourceDirectory();
  
  protected abstract void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException;
  
  protected abstract void createAndRunBuildFileInstrumentation(File projectLocalPath) throws TransformerException;
  
  protected abstract void generateJarFile() throws IOException, InterruptedException;

  public void startSerialization(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis)
  throws IOException, InterruptedException, TransformerException {
    
    gitProjectActions = new GitProjectActions(mergeScenarioUnderAnalysis.getLocalProjectPath());

    gitProjectActions.checkoutCommit(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0));
    
    resourceFileSupporter = new ResourceFileSupporter(mergeScenarioUnderAnalysis.getLocalProjectPath(), mergeScenarioUnderAnalysis.getTargetClass());
    resourceFileSupporter.findTargetClassLocalPath(resourceFileSupporter.getTargetClassName(), resourceFileSupporter.getProjectLocalPath());
    createBuildFileSupporters();
    processManager = new ProcessManager(mergeScenarioUnderAnalysis.getTransformationOption().getBudget());

    if (buildFileDirectory != null) {
      resourceFileSupporter.createNewDirectory(buildFileDirectory);

      generateTestFilesJar();
      saveJarFile(generatedJarFile, mergeScenarioUnderAnalysis, mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0)+"-TestFiles.jar");
      getTestClassesCompleteNames("", new File(buildFileDirectory.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "java"));
      saveFile(testFilesNames, mergeScenarioUnderAnalysis, mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0)+"-TestFiles.txt");

      createAndRunBuildFileInstrumentation(resourceFileSupporter.getProjectLocalPath());

      runTestabilityTransformations(new File(
          resourceFileSupporter.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()),
          mergeScenarioUnderAnalysis.getTransformationOption().applyTransformations(),
          mergeScenarioUnderAnalysis.getTransformationOption().applyFullTransformations());

      objectSerializerSupporter = createAndAddObjectSerializerSupporter();
      objectDeserializerSupporter = createAndAddObjectDeserializerSupporter();

      objectSerializerClassIntrumentation = createAndRunObjectSerializerInstrumentation(
          new File(resourceFileSupporter.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()),
          new ObjectSerializerClassIntrumentation(mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializerSupporter.getFullSerializerSupporterClass()));

      applyTestabilityTransformationsTargetClasses(resourceFileSupporter, objectSerializerClassIntrumentation.getTargetClasses(), mergeScenarioUnderAnalysis.getTransformationOption());

      serializedObjectAccessClassIntrumentation = new SerializedObjectAccessClassIntrumentation(
          mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializerSupporter.getFullSerializerSupporterClass());

      runSerializedObjectCreation();

      objectSerializerSupporter.deleteObjectSerializerSupporterClass(resourceFileSupporter.getTargetClassLocalPath().getPath());
      
      gitProjectActions.undoCurrentChanges();

      generateJarsForAllMergeScenarioCommits(mergeScenarioUnderAnalysis);

      gitProjectActions.undoCurrentChanges();
      gitProjectActions.cleanChanges();
      gitProjectActions.checkoutCommit(gitProjectActions.getInitialSHA());
      
      cleanResourceDirectory();
    }
  }
  
  public boolean generateJarsForAllMergeScenarioCommits(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis){
    try {
        for (String mergeScenarioCommit : mergeScenarioUnderAnalysis.getMergeScenarioCommits()) {
  
          safeCheckout(gitProjectActions, mergeScenarioCommit);

          createAndRunBuildFileInstrumentation(resourceFileSupporter.getProjectLocalPath());
  
          runTestabilityTransformations(new File(
          resourceFileSupporter.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()),
              mergeScenarioUnderAnalysis.getTransformationOption().applyTransformations(),
              mergeScenarioUnderAnalysis.getTransformationOption().applyFullTransformations());
          applyTestabilityTransformationsTargetClasses(resourceFileSupporter, objectSerializerClassIntrumentation.getTargetClasses(), mergeScenarioUnderAnalysis.getTransformationOption());
  
          SerializedObjectAccessOutputClass serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();
          ConverterSupporter converterSupporter = new ConverterSupporter();
  
          objectSerializerSupporter
              .getOutputClass(resourceFileSupporter.getTargetClassLocalPath().getPath(),
                  resourceFileSupporter
                      .getResourceDirectoryPath(new File(buildFileDirectory.getPath())));
  
          objectSerializerClassIntrumentation.runTransformation(new File(
              resourceFileSupporter.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
  
          List<String> converterList = getConverterList(resourceFileSupporter, new File(buildFileDirectory.getPath()));
          if(converterList.size() > 0){
            converterSupporter.getOutputClass(converterList, resourceFileSupporter.getTargetClassLocalPath().getPath(),
                objectSerializerSupporter.getFullSerializerSupporterClass());
          }
  
          objectDeserializerSupporter
              .getOutputClass(resourceFileSupporter.getTargetClassLocalPath().getPath(),
                  resourceFileSupporter
                      .getResourceDirectoryPath(new File(buildFileDirectory.getPath())),
                  converterSupporter.classesPathSignature);

          generateJarFile();
          
          startProcess(resourceFileSupporter.getProjectLocalPath().getPath(), "java -cp " + generatedJarFile
              + " " + getObjectDeserializerClassPathOnTargetProject(objectSerializerClassIntrumentation),
              "Generating method list associated to serialized objects", false);
  
          List<String> methodList = getMethodList(resourceFileSupporter, new File(buildFileDirectory.getPath()), mergeScenarioUnderAnalysis.getTransformationOption());
  
          objectSerializerClassIntrumentation.undoTransformations(new File(
              resourceFileSupporter.getTargetClassLocalPath() + File.separator
                  + mergeScenarioUnderAnalysis.getTargetClass()));
          objectSerializerSupporter.deleteObjectSerializerSupporterClass(
              resourceFileSupporter.getTargetClassLocalPath().getPath());
          objectDeserializerSupporter.deleteObjectSerializerSupporterClass(resourceFileSupporter.getTargetClassLocalPath().getPath());
          converterSupporter.deleteOldClassSupporter();
  
          if (methodList.size() > 0) {
            serializedObjectAccessOutputClass
                .getOutputClass(methodList, resourceFileSupporter.getTargetClassLocalPath().getPath(),
                    objectSerializerSupporter.getFullSerializerSupporterClass());
            serializedObjectAccessClassIntrumentation.addSupporterClassAsField(new File(
                resourceFileSupporter.getTargetClassLocalPath() + File.separator + mergeScenarioUnderAnalysis.getTargetClass()));
          }

          generateJarFile();
          
          serializedObjectAccessOutputClass.deleteOldClassSupporter();
          serializedObjectAccessClassIntrumentation.undoTransformations(new File(
              resourceFileSupporter.getTargetClassLocalPath() + File.separator
                  + mergeScenarioUnderAnalysis.getTargetClass()));
          saveJarFile(generatedJarFile, mergeScenarioUnderAnalysis, mergeScenarioCommit+"-"+mergeScenarioUnderAnalysis.getTargetMethod().split("\\(")[0]+".jar");
          
          gitProjectActions.undoCurrentChanges();
        }
        return true;
      }catch (Exception e){
        e.printStackTrace();
      }
      return false;
  }
  

  public void safeCheckout(GitProjectActions gitProjectActions, String mergeScenarioCommit) {
    gitProjectActions.addChanges();
    gitProjectActions.stashChanges();
    gitProjectActions.checkoutCommit(mergeScenarioCommit);
    gitProjectActions.stashPop();
    gitProjectActions.restoreChanges();
  }

  public ObjectSerializerClassIntrumentation createAndRunObjectSerializerInstrumentation(File file,
      ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation1) throws IOException {
    ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = objectSerializerClassIntrumentation1;
    objectSerializerClassIntrumentation.runTransformation(file);
    return objectSerializerClassIntrumentation;
  }

  public ObjectSerializerSupporter createAndAddObjectSerializerSupporter() {
    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(resourceFileSupporter.getProjectLocalPath().getPath() + File.separator + "src" +
            File.separator + "main" + File.separator + "java")
            .relativize(Paths.get(resourceFileSupporter.
                getTargetClassLocalPath().getPath())).toString().replace(File.separator, "."));
    objectSerializerSupporter
        .getOutputClass(resourceFileSupporter.getTargetClassLocalPath().getPath(),
            resourceFileSupporter
                .getResourceDirectoryPath(new File(buildFileDirectory.getPath())));
    return objectSerializerSupporter;
  }

  public ObjectDeserializerSupporter createAndAddObjectDeserializerSupporter() {
    ObjectDeserializerSupporter objectDeserializerSupporter = new ObjectDeserializerSupporter(
        Paths.get(resourceFileSupporter.getProjectLocalPath().getPath() + File.separator + "src" +
                File.separator + "main" + File.separator + "java")
            .relativize(Paths.get(resourceFileSupporter.
                getTargetClassLocalPath().getPath())).toString().replace(File.separator, "."));
    return objectDeserializerSupporter;
  }

  public List<String> getMethodList(ResourceFileSupporter resourceFileSupporter, File pom, TransformationOption transformationOption){
    String resourceDirectory =  resourceFileSupporter
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
            File classFile = resourceFileSupporter.searchForFileByName(objectType+".java", resourceFileSupporter.getProjectLocalPath());
            if(classFile != null && classFile.getCanonicalPath().contains("/src/test/")){
              for (int i = 0; i < 3; i++)
                myReader.nextLine();
              continue;
            }else {
              if (!serializedObjectTypes.contains(objectType))
                serializedObjectTypes.add(objectType);
            }
          }
          methods.add(nextLine);
        } myReader.close();
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    runTestabilityTransformationsForSerializedObjectClasses(resourceFileSupporter, serializedObjectTypes, transformationOption);
    return methods;
  }

  public List<String> getConverterList(ResourceFileSupporter resourceFileSupporter, File pom){
    String resourceDirectory =  resourceFileSupporter
        .getResourceDirectoryPath(pom);
    List<String> converters = new ArrayList<>();

    if (new File(resourceDirectory+File.separator+"converters-name.txt").exists()){
      try {
        File file = new File(resourceDirectory+File.separator+"converters-name.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          String nextLine = myReader.nextLine();
          converters.add(nextLine);
        } myReader.close();
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    return converters;
  }

  public void runTestabilityTransformationsForSerializedObjectClasses(
      ResourceFileSupporter resourceFileSupporter, List<String> serializedObjects, TransformationOption transformationOption){
    for(String serializedObject: serializedObjects){
      File serializedObjectFile = resourceFileSupporter.searchForFileByName(serializedObject+".java", resourceFileSupporter.getProjectLocalPath());
      if (serializedObjectFile != null){
        runTestabilityTransformations(serializedObjectFile, transformationOption.applyTransformations(), transformationOption.applyFullTransformations());
      }
    }
  }

  public String getObjectClassPathOnTargetProject(
      ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation) {
    return objectSerializerClassIntrumentation.getPackageName() + File.separator
        + "ObjectSerializerSupporter";
  }

  public String getObjectDeserializerClassPathOnTargetProject(
      ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation) {
    return objectSerializerClassIntrumentation.getPackageName() + File.separator
        + "ObjectDeserializerSupporter";
  }

  public boolean runTestabilityTransformations(File file, boolean applyTransformations, boolean applyFully){
    System.out.print("Applying Testability Transformations : ");
    try {
      Transformations.main(new String[]{new String(file.getPath()),
          String.valueOf(applyTransformations), String.valueOf(applyFully)});
      System.out.println("SUCCESSFUL");
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("UNSUCCESSFUL");
    return false;
  }

  public void applyTestabilityTransformationsTargetClasses(ResourceFileSupporter resourceFileSupporter, List<String> classes, TransformationOption transformationOption){
    for(String targetClass: classes){
      File targetClassFile = resourceFileSupporter.searchForFileByName(targetClass+".java", resourceFileSupporter.getProjectLocalPath());
      if (targetClassFile != null){
        runTestabilityTransformations(targetClassFile, transformationOption.applyTransformations(), transformationOption.applyFullTransformations());
      }
    }
  }

  public boolean startProcess(String directoryPath, String command, String message, boolean isTestTask)
      throws IOException, InterruptedException {
    Process process = Runtime.getRuntime()
        .exec(command, null,
            new File(directoryPath));
    return processManager.computeProcessOutput(process, message, isTestTask);
  }

  public void saveJarFile(String generatedJarFile, MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, String fileName){
    JarManager.saveGeneratedJarFile(generatedJarFile,
        mergeScenarioUnderAnalysis.getLocalProjectPath()
            .split(mergeScenarioUnderAnalysis.getProjectName())[0] +
            File.separator + "GeneratedJars" + File.separator
            + mergeScenarioUnderAnalysis.getProjectName(), fileName);
  }

  public void getTestClassesCompleteNames(String packageName, File directory){
    File[] testFiles = directory.listFiles();

    for(File file : testFiles){
      String testSignature = (packageName.equals("") ? file.getName() : packageName + "." + file.getName());
      if(file.isDirectory())
        getTestClassesCompleteNames(testSignature, file);
      else if(file.getName().contains(".java"))
        testFilesNames.add(testSignature.replace(".java", ""));
    }
  }

  public boolean saveFile(ArrayList<String> list, MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, String fileName){
    try{
       PrintWriter writer = new PrintWriter(
        mergeScenarioUnderAnalysis.getLocalProjectPath()
            .split(mergeScenarioUnderAnalysis.getProjectName())[0] +
            File.separator + "GeneratedJars" + File.separator
            + mergeScenarioUnderAnalysis.getProjectName()+File.separator+fileName);
       for(String element: list){
         writer.println(element);
       }
       writer.close();
       return true;
     }catch(Exception e){
       e.printStackTrace();
     }
     return false;
   }

}
