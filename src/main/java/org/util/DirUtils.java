package org.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirUtils {
  public static boolean isDirEmpty(final Path directory) throws IOException {
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
      return !dirStream.iterator().hasNext();
    }
  }

  public static String findBuildFileInDir(String dir){
    if(new File(dir + File.separator + "pom.xml").exists()){
      return "maven";
    }

    if(new File(dir + File.separator + "build.gradle").exists() || new File(dir + File.separator + "gradlew").exists()){
      return "gradle";
    }

    return null;
  }
}
