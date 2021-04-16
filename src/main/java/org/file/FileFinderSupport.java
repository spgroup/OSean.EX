package org.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileFinderSupport {
  private File projectLocalPath;
  private File targetClassLocalPath;
  private File localPathResourceDirectory;

  public FileFinderSupport(String pathFile){
    this.projectLocalPath = new File(pathFile);
    this.targetClassLocalPath = new File(pathFile);
  }

  public File getProjectLocalPath(){
    return this.projectLocalPath;
  }

  public File getTargetClassLocalPath() {
    return targetClassLocalPath;
  }

  public File findFile(String name,File file){
    File[] list = file.listFiles();
    if(list!=null)
      for (File fil : list) {
        if (fil.isDirectory()){
          File aux = findFile(name,fil);
          if (aux!=null){
            return aux;
          }
        }
        else if (name.equalsIgnoreCase(fil.getName())){
          this.targetClassLocalPath = new File(fil.getPath().split(name)[0]);
          return findPomDirectory(fil.getParentFile());
        }
      }
    return null;
  }

  private File findPomDirectory(File file){
    File[] list = file.listFiles();
    String name = "pom.xml";
    File aux = new File(this.projectLocalPath.getPath()).getParentFile();
    if(list!=null && !aux.equals(file)) {
      for (File fil : list) {
        if (name.equalsIgnoreCase(fil.getName())) {
          return fil.getParentFile();
        }
      }
      return findPomDirectory(file.getParentFile());
    }else{
      return null;
    }
  }

  public String getResourceDirectoryPath(File localPomDirectory){
    return localPomDirectory+File.separator+"src"+File.separator+"main"+File.separator+"resources";
  }

  public boolean createNewDirectory(File localPomDirectory){
    File resourceDirectory = new File(getResourceDirectoryPath(localPomDirectory));
    if (!resourceDirectory.exists()){
      try {
        Files.createDirectories(resourceDirectory.toPath());
        this.localPathResourceDirectory = resourceDirectory;
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public boolean deleteResourceDirectory(){
    return this.localPathResourceDirectory.delete();
  }

}
