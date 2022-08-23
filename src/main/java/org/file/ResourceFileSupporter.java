package org.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;

public class ResourceFileSupporter {
  protected File projectLocalPath;
  protected File targetClassLocalPath;
  protected File localPathResourceDirectory;
  protected String targetClassName;

  public ResourceFileSupporter(String pathFile){
    this.projectLocalPath = new File(pathFile);
    this.targetClassLocalPath = new File(pathFile);
  }
  
  public ResourceFileSupporter(String pathFile, String targetClassName){
    this.projectLocalPath = new File(pathFile);
    this.targetClassLocalPath = new File(pathFile);
    this.targetClassName = targetClassName;
  }

  public File getProjectLocalPath(){
    return this.projectLocalPath;
  }

  public File getTargetClassLocalPath() {
    return targetClassLocalPath;
  }

  public File getLocalPathResourceDirectory(){
    return this.localPathResourceDirectory;
  }

  public String getTargetClassName(){
    return this.targetClassName;
  }

  public void findTargetClassLocalPath(String name,File file){
    File targetFile = searchForFileByName(name, file);
    if (targetFile != null){
      this.targetClassLocalPath = new File(targetFile.getPath().split(name)[0]);
    }
  }

  public File searchForFileByName(String name,File file){
    File[] list = file.listFiles();
    if(list!=null)
      for (File fil : list) {
        if (fil.isDirectory()){
          File aux = searchForFileByName(name,fil);
          if (aux!=null){
            return aux;
          }
        }else{
          if (name.equalsIgnoreCase(fil.getName())){
            return fil;
          }
        }
      }
    return null;
  }

  public File findBuildFileDirectory(File file, String buildFileName){
    File[] list = file.listFiles();
    String name = buildFileName;
    File aux = new File(this.projectLocalPath.getPath()).getParentFile();
    if(list!=null && !aux.equals(file)) {
      for (File fil : list) {
        if (name.equalsIgnoreCase(fil.getName())) {
          return fil.getParentFile();
        }
      }
      return findBuildFileDirectory(file.getParentFile(), buildFileName);
    }else{
      return null;
    }
  }

  public String getResourceDirectoryPath(File localPomDirectory){
    return localPomDirectory+File.separator+"src"+File.separator+"main"+File.separator+"resources"
        +File.separator+"serializedObjects";
  }

  public boolean createNewDirectory(File localPomDirectory){
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

  public boolean deleteResourceDirectory(){
    try {
      FileUtils.deleteDirectory(this.localPathResourceDirectory);
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;

  }

}
