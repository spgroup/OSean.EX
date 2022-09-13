package org.transformations.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.file.ProjectFileSupporter;

public class ConverterSupporter extends SerializedObjectAccessOutputClass {
  public ArrayList<Path> outputClassesPath = new ArrayList<>();
  public ArrayList<String> classesPathSignature = new ArrayList<>();

  public boolean writeConverterSupporterClassFiles(ProjectFileSupporter projectFileSupporter,
      List<String> serializedObjectMethods, String targerClassLocalPath, String packageName) {
    for (String oneConverter : serializedObjectMethods) {
      if (isFileOnProject(oneConverter, projectFileSupporter)) {
        try (
            FileWriter fw = new FileWriter(
                targerClassLocalPath + File.separator + "Converter" + getClassName(oneConverter) + ".java");
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
          out.print("package " + packageName + ";\n\n");
          out.print("import com.thoughtworks.xstream.converters.Converter;\n");
          out.print("import com.thoughtworks.xstream.converters.MarshallingContext;\n");
          out.print("import com.thoughtworks.xstream.converters.UnmarshallingContext;\n");
          out.print("import com.thoughtworks.xstream.io.HierarchicalStreamReader;\n");
          out.print("import com.thoughtworks.xstream.io.HierarchicalStreamWriter;\n");
          out.print("import java.util.ArrayList;\n");
          out.print("\npublic class Converter" + getClassName(oneConverter));
          out.print(" implements Converter {\n");
          out.print("  public boolean canConvert(Class clazz) {\n");
          out.print("    boolean interfaceImp = " + oneConverter + ".class.isAssignableFrom(clazz);\n");
          out.print("    boolean sameClass = clazz.equals(" + oneConverter + ".class);\n");
          out.print("    return interfaceImp || sameClass;\n");
          out.print("  }\n");
          out.print("\n");
          out.print("  public void marshal(Object value, HierarchicalStreamWriter writer,\n");
          out.print("      MarshallingContext context) {\n");
          out.print("  }\n");
          out.print("\n");
          out.print("  public Object unmarshal(HierarchicalStreamReader reader,\n");
          out.print("      UnmarshallingContext context) {\n");
          out.print("    return null;\n");
          out.print("  }\n");
          out.print("\n}");
          this.classesPathSignature.add(packageName + ".Converter" + getClassName(oneConverter));
          this.outputClassesPath.add(new File(
              targerClassLocalPath + File.separator + "Converter" + getClassName(oneConverter)
                  + ".java")
              .toPath());
        } catch (Exception e) {
          e.printStackTrace();
          return false;
        }
      }
    }
    return true;
  }

  public boolean isFileOnProject(String fileName, ProjectFileSupporter projectFileSupporter) {
    fileName = getClassName(fileName) + ".java";
    File path = projectFileSupporter.searchForFileByName(fileName);
    return (path != null ? true : false);
  }

  public static String getClassName(String fullName) {
    return fullName.substring(fullName.lastIndexOf('.') + 1);
  }

  @Override
  public boolean deleteOldClassSupporter() {
    try {
      for (Path oneConverterClass : this.outputClassesPath) {
        if (oneConverterClass != null && oneConverterClass.toFile().exists()) {
          oneConverterClass.toFile().delete();
        }
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}