package org.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class ProjectFileSupporter {
  protected File projectLocalPath;
  protected File targetClassLocalPath;
  protected File localPathResourceDirectory;
  protected String targetClassName;
  protected String targetClassFilePath;
  protected HashMap<String, String> projectFilesPath;

  public ProjectFileSupporter(String pathFile) {
    this.projectLocalPath = new File(pathFile);
    this.targetClassLocalPath = new File(pathFile);
    buildFilesPath(this.projectLocalPath);
  }

  public ProjectFileSupporter(String pathFile, String targetClassName) {
    this.projectLocalPath = new File(pathFile);
    this.targetClassName = targetClassName;
    buildFilesPath(this.projectLocalPath);
    findTargetClassLocalPath(this.targetClassName);
  }

  public File getProjectLocalPath() {
    return this.projectLocalPath;
  }

  public File getTargetClassLocalPath() {
    return this.targetClassLocalPath;
  }

  public File getLocalPathResourceDirectory() {
    return this.localPathResourceDirectory;
  }

  public String getTargetClassName() {
    return this.targetClassName;
  }

  public String getTargetClassFilePath() {
    return this.targetClassFilePath;
  }

  public void findTargetClassLocalPath(String name) {
    File targetFile = searchForFileByName(name);
    if (targetFile != null) {
      this.targetClassFilePath = targetFile.getPath();
      this.targetClassLocalPath = new File(targetFile.getPath().split(name)[0]);
    }
  }

  public File searchForFileByName(String name) {
    String filePath = this.projectFilesPath.get(name);
    return (filePath != null ? new File(filePath) : null);
  }

  public File findBuildFileDirectory(File file, String buildFileName) {
    if (outOfProject(file))
      return null;
    File[] list = file.listFiles();
    if (list != null) {
      for (File fil : list) {
        if (buildFileName.equalsIgnoreCase(fil.getName())) {
          return fil.getParentFile();
        }
      }
      return findBuildFileDirectory(file.getParentFile(), buildFileName);
    } else {
      return null;
    }
  }

  public String getResourceDirectoryPath(File localPomDirectory) {
    return localPomDirectory.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator
        + "resources"
        + File.separator + "serializedObjects";
  }

  public boolean createNewDirectory(File localPomDirectory) {
    File resourceDirectory = new File(getResourceDirectoryPath(localPomDirectory));
    try {
      FileUtils.deleteDirectory(resourceDirectory);
      Files.createDirectories(resourceDirectory.toPath());
      this.localPathResourceDirectory = resourceDirectory;
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean deleteResourceDirectory() {
    try {
      FileUtils.deleteDirectory(this.localPathResourceDirectory);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public void buildFilesPath(File root) {
    this.projectFilesPath = new HashMap<>();
    FileUtils.listFiles(root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).stream()
        .filter(file -> file.getName().contains(".java"))
        .forEach(file -> projectFilesPath.put(file.getName(), file.getPath()));
  }

  private boolean outOfProject(File file) {
    File outOfProject = new File(this.projectLocalPath.getPath()).getParentFile();
    return outOfProject.equals(file);
  }
}
