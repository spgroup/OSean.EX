package org.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.instrumentation.PomFileInstrumentation;

public class JarManager {

  private static String findJarFile(File targetDirectory){
    File[] list = targetDirectory.listFiles();
    if(list!=null)
      for (File fil : list) {
        if (fil.isDirectory()){
          String aux = findJarFile(fil);
          if (aux!=null){
            return aux;
          }
        }
        else if (fil.getName().contains("jar-with-dependencies")){
          return fil.getAbsolutePath();
        }
      }
    return null;
  }

  public static boolean saveGeneratedJarFile(String generatedJarFile, String destPath, String fileName){
    if (new File(generatedJarFile).exists()){
      try {
        Files.createDirectories(new File(destPath).toPath());
        Files.copy(new File(generatedJarFile).toPath(), new File(destPath+File.separator+fileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public static String getJarFile(PomFileInstrumentation pomFileInstrumentation) {
    return findJarFile(
        new File(pomFileInstrumentation.getPomFileDirectory() + File.separator + "target"));
  }
}
