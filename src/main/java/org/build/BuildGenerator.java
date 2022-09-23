package org.build;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import org.file.ProjectFileSupporter;
import org.transformations.TestabilityTransformationsController;
import org.transformations.instrumentation.ClassInstumentationController;
import org.transformations.serialization.ObjectSerializationController;
import org.util.GitDriver;
import org.util.JarManager;
import org.util.ProcessManager;
import org.util.input.MergeScenarioUnderAnalysis;
import org.util.input.TransformationOption;

public abstract class BuildGenerator {
  public ProjectFileSupporter projectFileSupporter;
  public File buildFileDirectory;
  public ProcessManager processManager;
  public ObjectSerializationController objectSerializationController;
  public ClassInstumentationController classInstumentationController;
  public TestabilityTransformationsController testabilityTransformationsController;
  public String generatedJarFile;
  public String generatedJarsFolderPath;
  public ArrayList<String> testFilesNames = new ArrayList<>();

  protected abstract void createBuildFileSupporters() throws TransformerException;

  protected abstract void generateTestFilesJar() throws IOException, InterruptedException, TransformerException;

  protected abstract void runSerializedObjectCreation() throws IOException, InterruptedException, TransformerException;

  protected abstract void createAndRunBuildFileInstrumentation(File projectLocalPath) throws TransformerException;

  protected abstract boolean generateJarFile() throws IOException, InterruptedException;

  public BuildGenerator(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, ProcessManager processManager)
      throws TransformerException {
    this.projectFileSupporter = new ProjectFileSupporter(mergeScenarioUnderAnalysis.getLocalProjectPath(),
        mergeScenarioUnderAnalysis.getTargetClass());
    this.processManager = processManager;
    this.generatedJarsFolderPath = mergeScenarioUnderAnalysis.getLocalProjectPath()
        .split(mergeScenarioUnderAnalysis.getProjectName())[0] + File.separator + "GeneratedJars" + File.separator
        + mergeScenarioUnderAnalysis.getProjectName();
    this.testabilityTransformationsController = new TestabilityTransformationsController(
        mergeScenarioUnderAnalysis.getTransformationOption());
    createBuildFileSupporters();
  }

  private void startSerialization(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, GitDriver gitProjectActions)
      throws TransformerException, IOException, InterruptedException {
    projectFileSupporter.createNewDirectory(buildFileDirectory);
    createAndRunBuildFileInstrumentation(projectFileSupporter.getProjectLocalPath());

    objectSerializationController = new ObjectSerializationController();
    objectSerializationController.createObjectSerializerSupporter(projectFileSupporter.getProjectLocalPath().getPath(),
        projectFileSupporter.getTargetClassLocalPath().getPath());
    objectSerializationController.injectObjectSerializerSupporterFile(
        projectFileSupporter.getTargetClassLocalPath().getPath(),
        projectFileSupporter.getLocalPathResourceDirectory().getPath());
    objectSerializationController.createObjectDeserializerSupporter(
        projectFileSupporter.getProjectLocalPath().getPath(), projectFileSupporter.getTargetClassLocalPath().getPath());

    classInstumentationController = new ClassInstumentationController();
    classInstumentationController.createObjectSerializerInstrumentation(mergeScenarioUnderAnalysis.getTargetMethod(),
        objectSerializationController.getTargetClassPackage());
    classInstumentationController.instrumentTargetMethod(new File(projectFileSupporter.getTargetClassFilePath()));
    classInstumentationController
        .addTargetClassToTestabilityTransformationsFilesList(projectFileSupporter.getTargetClassName());
    classInstumentationController.createSerializedObjectAccessClassInstrumentation(
        mergeScenarioUnderAnalysis.getTargetMethod(), objectSerializationController.getTargetClassPackage());

    testabilityTransformationsController.applyTestabilityTransformationsTargetClasses(projectFileSupporter,
        classInstumentationController.getClassesToApplyTestabilityTransformations());

    processManager.setSerializedObjectsDir(projectFileSupporter.getLocalPathResourceDirectory().getPath());
    runSerializedObjectCreation();

    objectSerializationController.deleteObjectSerializerSupporterFile();
  }

  private void setProjectToInitialState(GitDriver gitProjectActions) throws InterruptedException {
    gitProjectActions.undoCurrentChanges(projectFileSupporter.getProjectLocalPath().getPath());
    gitProjectActions.cleanChanges(projectFileSupporter.getProjectLocalPath().getPath());
    gitProjectActions.checkoutCommit(projectFileSupporter.getProjectLocalPath().getPath(),
        gitProjectActions.getInitialSHA());
  }

  private void revertChangesToCommit(GitDriver gitProjectActions, String mergeScenarioCommit)
      throws InterruptedException {
    gitProjectActions.undoCurrentChanges(projectFileSupporter.getProjectLocalPath().getPath());
    testabilityTransformationsController.clearTransformedClassesList();
    System.out.println("Done with commit " + mergeScenarioCommit);
  }

  private void applyChangesToCommit(GitDriver gitProjectActions, String mergeScenarioCommit, boolean serializing)
      throws IOException, InterruptedException, TransformerException {
    if (serializing) {
      gitProjectActions.safeCheckout(projectFileSupporter.getProjectLocalPath().getPath(),
          projectFileSupporter.getLocalPathResourceDirectory().getPath(), mergeScenarioCommit);
    } else {
      gitProjectActions.checkoutCommit(projectFileSupporter.getProjectLocalPath().getPath(), mergeScenarioCommit);
    }
    projectFileSupporter.buildFilesPath(projectFileSupporter.getProjectLocalPath());
    createAndRunBuildFileInstrumentation(projectFileSupporter.getProjectLocalPath());
    if (serializing)
      testabilityTransformationsController.applyTestabilityTransformationsTargetClasses(projectFileSupporter,
          classInstumentationController.getClassesToApplyTestabilityTransformations());
  }

  public boolean genPlainBuild(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, GitDriver gitProjectActions)
      throws InterruptedException {
    gitProjectActions.undoCurrentChanges(projectFileSupporter.getProjectLocalPath().getPath());
    try {
      for (String mergeScenarioCommit : mergeScenarioUnderAnalysis.getMergeScenarioCommits()) {
        applyChangesToCommit(gitProjectActions, mergeScenarioCommit, false);
        testabilityTransformationsController.runTestabilityTransformations(
            projectFileSupporter.searchForFileByName(projectFileSupporter.getTargetClassName()),
            mergeScenarioUnderAnalysis.getTransformationOption().applyTransformations(),
            mergeScenarioUnderAnalysis.getTransformationOption().applyFullTransformations());
        if (!generateJarFile()) {
          revertTestabilityTransformationsInTargetClasses(gitProjectActions);
          generateJarFile();
        }
        saveJarFile(mergeScenarioCommit + "-" + mergeScenarioUnderAnalysis.getTargetMethod().split("\\(")[0] + ".jar");
        revertChangesToCommit(gitProjectActions, mergeScenarioCommit);
      }
      setProjectToInitialState(gitProjectActions);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean genBuildWithSerialization(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis,
      GitDriver gitProjectActions) throws TransformerException, IOException, InterruptedException {
    startSerialization(mergeScenarioUnderAnalysis, gitProjectActions);
    gitProjectActions.undoCurrentChanges(projectFileSupporter.getProjectLocalPath().getPath());
    try {
      for (String mergeScenarioCommit : mergeScenarioUnderAnalysis.getMergeScenarioCommits()) {
        applyChangesToCommit(gitProjectActions, mergeScenarioCommit, true);

        objectSerializationController.createSerializedObjectAccessOutputClass();
        objectSerializationController.createConverterSupporter();
        objectSerializationController.injectObjectSerializerSupporterFile(
            projectFileSupporter.getTargetClassLocalPath().getPath(),
            projectFileSupporter.getLocalPathResourceDirectory().getPath());

        classInstumentationController.instrumentTargetMethod(new File(projectFileSupporter.getTargetClassFilePath()));

        List<String> converterList = getConverterList(projectFileSupporter);
        if (converterList.size() > 0) {
          objectSerializationController.injectConverterSupporterClassesFiles(projectFileSupporter, converterList,
              projectFileSupporter.getTargetClassLocalPath().getPath(),
              objectSerializationController.getTargetClassPackage());
        }

        objectSerializationController.injectObjectDeserializerSupporterFile(
            projectFileSupporter.getTargetClassLocalPath().getPath(),
            projectFileSupporter.getLocalPathResourceDirectory().getPath());

        if (!generateJarFile()) {
          revertTestabilityTransformationsInTargetClasses(gitProjectActions);
          classInstumentationController.instrumentTargetMethod(new File(projectFileSupporter.getTargetClassFilePath()));
          generateJarFile();
        }

        processManager.startProcess(projectFileSupporter.getProjectLocalPath().getPath(), "java -cp " + generatedJarFile
            + " " + objectSerializationController.getObjectDeserializerSupporterSignatureClass(),
            "Generating method list associated to serialized objects", false, true);

        classInstumentationController.undoInstrumentations(new File(projectFileSupporter.getTargetClassFilePath()));
        objectSerializationController.deleteObjectSerializerSupporterFile();
        objectSerializationController.deleteObjectDeserializerSupporterFile();
        objectSerializationController.deleteConverterSupporterClassesFiles();

        List<String> methodList = getMethodList(projectFileSupporter,
            mergeScenarioUnderAnalysis.getTransformationOption());
        if (methodList.size() > 0) {
          objectSerializationController.injectSerializedObjectAccessFile(methodList,
              projectFileSupporter.getTargetClassLocalPath().getPath(),
              objectSerializationController.getTargetClassPackage());
          classInstumentationController
              .addSerializedObjectAccessClassAsField(new File(projectFileSupporter.getTargetClassFilePath()));
        }

        if (!generateJarFile()) {
          revertTestabilityTransformationsInTargetClasses(gitProjectActions);
          generateJarFile();
        }

        saveJarFile(mergeScenarioCommit + "-" + mergeScenarioUnderAnalysis.getTargetMethod().split("\\(")[0] + ".jar");
        revertChangesToCommit(gitProjectActions, mergeScenarioCommit);
      }
      setProjectToInitialState(gitProjectActions);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean genTestFilesBuild(MergeScenarioUnderAnalysis mergeScenarioUnderAnalysis, String mergeHashCommit) {
    try {
      if (!checkIfTestFilesJarAndTestFilesTxtExists(mergeHashCommit + "-TestFiles.jar",
          mergeHashCommit + "-TestFiles.txt")) {
        generateTestFilesJar();
        saveJarFile(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0) + "-TestFiles.jar");
        getTestClassesCompleteNames("", new File(buildFileDirectory.getAbsolutePath() + File.separator + "src"
            + File.separator + "test" + File.separator + "java"));
        saveFile(mergeScenarioUnderAnalysis.getMergeScenarioCommits().get(0) + "-TestFiles.txt");
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public List<String> getMethodList(ProjectFileSupporter projectFileSupporter,
      TransformationOption transformationOption) {
    String resourceDirectory = projectFileSupporter.getLocalPathResourceDirectory().getPath();
    List<String> methods = new ArrayList<>();
    HashSet<File> serializedObjectClassesPath = new HashSet<>();

    Pattern pattern = Pattern.compile("public [0-9a-zA-Z\\.]* deserialize", Pattern.CASE_INSENSITIVE);
    Matcher matcher;

    if (new File(resourceDirectory + File.separator + "output-methods.txt").exists()) {
      try {
        File file = new File(resourceDirectory + File.separator + "output-methods.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          String nextLine = myReader.nextLine();
          matcher = pattern.matcher(nextLine);
          if (matcher.find()) {
            int index = matcher.group(0).lastIndexOf(".");
            String objectType = matcher.group(0).substring(index + 1).replace(" deserialize", "");
            File classFile = projectFileSupporter.searchForFileByName(objectType + ".java");
            if (classFile != null && classFile.getPath().contains("/src/test/")) {
              for (int i = 0; i < 3; i++)
                myReader.nextLine();
              continue;
            }
            serializedObjectClassesPath.add(classFile);
          }
          methods.add(nextLine);
        }
        myReader.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    testabilityTransformationsController
        .runTestabilityTransformationsForSerializedObjectClasses(serializedObjectClassesPath);
    return methods;
  }

  public List<String> getConverterList(ProjectFileSupporter projectFileSupporter) {
    String resourceDirectory = projectFileSupporter.getLocalPathResourceDirectory().getPath();
    List<String> converters = new ArrayList<>();

    if (new File(resourceDirectory + File.separator + "converters-name.txt").exists()) {
      try {
        File file = new File(resourceDirectory + File.separator + "converters-name.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          String nextLine = myReader.nextLine();
          converters.add(nextLine);
        }
        myReader.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return converters;
  }

  public void saveJarFile(String fileName) {
    JarManager.saveGeneratedJarFile(generatedJarFile, generatedJarsFolderPath, fileName);
  }

  /*
   * This method will list every class name inside of test directory.
   * The purpose of that is to make it possible to execute those tests
   * (they will be included in the jar generated in the method
   * generateTestFilesJar()) later by going through every name in that list.
   * Note that, for some projects, there will be classes in the test directory
   * that aren't tests, and for obvious reasons,
   * they will fail if you try to execute them.
   */
  public void getTestClassesCompleteNames(String packageName, File directory) {
    File[] testFiles = directory.listFiles();

    for (File file : testFiles) {
      String testSignature = (packageName.equals("") ? file.getName() : packageName + "." + file.getName());
      if (file.isDirectory())
        getTestClassesCompleteNames(testSignature, file);
      else if (file.getName().contains(".java"))
        testFilesNames.add(testSignature.replace(".java", ""));
    }
  }

  public boolean saveFile(String fileName) {
    try {
      PrintWriter writer = new PrintWriter(generatedJarsFolderPath + File.separator + fileName);
      for (String element : testFilesNames) {
        writer.println(element);
      }
      writer.close();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /*
   * Check if already created the jar with project tests and the txt with test
   * classes names
   */
  private boolean checkIfTestFilesJarAndTestFilesTxtExists(String jarFilePath, String txtFilePath) {
    File jarFile = new File(generatedJarsFolderPath + File.separator + jarFilePath);
    File txtFile = new File(generatedJarsFolderPath + File.separator + txtFilePath);
    return (jarFile.exists() && txtFile.exists());
  }

  private void revertTestabilityTransformationsInTargetClasses(GitDriver gitProjectActions)
      throws IOException, InterruptedException {
    String paths = String.join(" ", testabilityTransformationsController.getTransformedClasses());
    System.out.println("Reverting ALL Testability Transformations");
    gitProjectActions.restoreChanges(projectFileSupporter.getProjectLocalPath().getPath(), paths);
  }
}
