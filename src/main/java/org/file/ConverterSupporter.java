package org.file;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConverterSupporter extends SerializedObjectAccessOutputClass{
  public ArrayList<Path> outputClassesPath = new ArrayList<>();
  public ArrayList<String> classesPathSignature = new ArrayList<>();

  @Override
  public boolean getOutputClass(List<String> serializedObjectMethods, String pomFileDirectory, String packageName){
    try {
      for (String oneConverter : serializedObjectMethods) {
        if (isFileOnProject(oneConverter, new File(pomFileDirectory))) {
          String classText = ""
              + "package " + packageName + ";\n\n"
              + "import com.thoughtworks.xstream.converters.Converter;\n"
              + "import com.thoughtworks.xstream.converters.MarshallingContext;\n"
              + "import com.thoughtworks.xstream.converters.UnmarshallingContext;\n"
              + "import com.thoughtworks.xstream.io.HierarchicalStreamReader;\n"
              + "import com.thoughtworks.xstream.io.HierarchicalStreamWriter;\n"
              + "import java.util.ArrayList;\n"
              + "\npublic class Converter" + getClassName(oneConverter)
              + " implements Converter {\n"
              + "  public boolean canConvert(Class clazz) {\n"
              + "    boolean interfaceImp = " + oneConverter + ".class.isAssignableFrom(clazz);\n"
              + "    boolean sameClass = clazz.equals(" + oneConverter + ".class);\n"
              + "    return interfaceImp || sameClass;\n"
              + "  }\n"
              + "\n"
              + "  public void marshal(Object value, HierarchicalStreamWriter writer,\n"
              + "      MarshallingContext context) {\n"
              + "  }\n"
              + "\n"
              + "  public Object unmarshal(HierarchicalStreamReader reader,\n"
              + "      UnmarshallingContext context) {\n"
              + "    return null;\n"
              + "  }\n"
              + "\n}";
          saveFile(pomFileDirectory, classText, "Converter" + getClassName(oneConverter));
          this.classesPathSignature.add(packageName + ".Converter" + getClassName(oneConverter));
          this.outputClassesPath.add(new File(
              pomFileDirectory + File.separator + "Converter" + getClassName(oneConverter)
                  + ".java").toPath());
        }

      }
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public boolean isFileOnProject(String fileName, File file) {
    fileName = fileName.substring(fileName.lastIndexOf(".")+1)+".java";
    File[] list = file.listFiles();
    if(list!=null)
      for (File fil : list){
        if (fil.isDirectory()){
          isFileOnProject(fileName, fil);
        }else if (fileName.equalsIgnoreCase(fil.getName())) {
          return true;
        }
      }
    return false;
  }

  public static String getClassName(String fullName){
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
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }
}