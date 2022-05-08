package org.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.util.input.MergeScenarioUnderAnalysis;

public class InputHandler {

  public static List<MergeScenarioUnderAnalysis> splitInputInMergeScenarios(String[] args){
    List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalysisList = new ArrayList<>();
    if (isInputACsvFile(args[0])){
      dealingWithMultipleInput(args[0], mergeScenarioUnderAnalysisList);
    }else{
      // mergeScenarioUnderAnalysisList.add(new MergeScenarioUnderAnalysis(args));
    }
    return mergeScenarioUnderAnalysisList;
  }

  private static boolean isInputACsvFile(String path){
    return new File(path).isFile();
  }

  private static void dealingWithMultipleInput(String csfFile,
      List<MergeScenarioUnderAnalysis> mergeScenarioUnderAnalyses) {
     try {
      BufferedReader csvReader = new BufferedReader(new FileReader(csfFile));
      String row;
      while ((row = csvReader.readLine()) != null) {
        // mergeScenarioUnderAnalyses.add(new
        // MergeScenarioUnderAnalysis(row.split(",")));
      }
      csvReader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static boolean isDirEmpty(final Path directory) throws IOException {
    try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
      return !dirStream.iterator().hasNext();
    }
  }

}
