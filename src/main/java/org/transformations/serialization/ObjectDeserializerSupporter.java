package org.transformations.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class ObjectDeserializerSupporter extends ObjectSerializerSupporter {

  public ObjectDeserializerSupporter(String packageName, String fileLocalPath) {
    super(packageName, fileLocalPath);
    this.classFilePath = fileLocalPath + File.separator + "ObjectDeserializerSupporter.java";
  }

  @Override
  public String getSignatureClass() {
    return getClassPackage() + ".ObjectDeserializerSupporter";
  }

  public boolean writeClassFile(String fileDirectory, String resourceDirectory, List<String> converters) {
    try (FileWriter fw = new FileWriter(fileDirectory + File.separator + "ObjectDeserializerSupporter.java");
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
      out.print("package " + this.fullSerializerSupporterClass + ";\n\n");
      out.print("import com.thoughtworks.xstream.XStream;\n");
      out.print("import com.thoughtworks.xstream.converters.Converter;\n");
      out.print("import java.io.File;\n");
      out.print("import java.io.PrintWriter;\n");
      out.print("import java.lang.reflect.InvocationTargetException;\n");
      out.print("import java.lang.reflect.Modifier;\n");
      out.print("import java.util.ArrayList;\n");
      out.print("import java.util.HashMap;\n");
      out.print("import java.util.List;\n");
      out.print("import java.util.Map;\n\n");
      out.print("");
      out.print("public class ObjectDeserializerSupporter {\n");
      out.print("\n");
      out.print("  private static File resourceDirectory = new File(\"" + resourceDirectory + "\");\n");
      out.print("\n");
      out.print("  public static void main(String[] args) throws ClassNotFoundException {\n");
      out.print("    List<String> methods = new ArrayList<String>();\n");
      out.print(
          "    File serializedObjects[] = new File(String.valueOf(ObjectDeserializerSupporter.resourceDirectory)).listFiles();\n");
      out.print("    for(File file: serializedObjects){\n");
      out.print("      Object serializedObject = null;\n");
      out.print("      serializedObject = deserializeAny(file);\n");
      out.print("      try {\n");
      out.print("        if (serializedObject != null && serializedObject.getClass().getName() != null &&\n");
      out.print("            Modifier.toString(\n");
      out.print("                    Class.forName(getCanonicalClassName(serializedObject)).getModifiers())\n");
      out.print("                .contains(\"public\")) {\n");
      out.print("          String aux = String\n");
      out.print("              .format(\"\\tpublic %s deserializeObject%s() throws IOException {\\n\"\n");
      out.print(
          "                      + \"\\t\\tjava.io.InputStream s = SerializedObjectSupporter.class.getClassLoader().getResourceAsStream(\\\"serializedObjects\\\"+File.separator+\\\"%s\\\");\\n\"\n");
      out.print("                      + \"\\t\\treturn (%s) deserializeAny(readFileContents(s));\\n\" +\n");
      out.print("                      \"\\t}\", getCanonicalClassName(serializedObject),\n");
      out.print("                  file.getName().replace(\".xml\", \"\"), file.getName(),\n");
      out.print("                  getCanonicalClassName(serializedObject), file.getPath());\n");
      out.print("          methods.add(aux);\n");
      out.print("        }\n");
      out.print("      }catch (Exception e){\n");
      out.print("        e.printStackTrace();\n");
      out.print("      }\n");
      out.print("      saveFile(methods, \"output-methods\");\n");
      out.print("    }\n");
      out.print("  }\n");
      out.print("  ");
      out.print("  public static String getCanonicalClassName(Object object){\n");
      out.print("    if (object.getClass().getEnclosingClass() != null){\n");
      out.print("      return object.getClass().getEnclosingClass().getCanonicalName();\n");
      out.print("    }else{\n");
      out.print("      return object.getClass().getCanonicalName();\n");
      out.print("    }\n");
      out.print("  }");
      out.print("");
      out.print("  private static boolean saveFile(List<String> methods, String fileName){\n");
      out.print("    try{\n");
      out.print(
          "      PrintWriter writer = new PrintWriter(resourceDirectory.getPath()+File.separator+fileName+\".txt\");\n");
      out.print("      for(String oneMethod: methods){\n");
      out.print("        writer.println(oneMethod);\n");
      out.print("      }\n");
      out.print("      writer.close();\n");
      out.print("      return true;\n");
      out.print("    }catch(Exception e){\n");
      out.print("      e.printStackTrace();\n");
      out.print("    }\n");
      out.print("    return false;\n");
      out.print("  }\n");
      out.print("\n");
      out.print("  private static Object deserializeAny(File file) {\n");
      out.print("    Object obj = null;\n");
      out.print("    try{\n");
      out.print("      obj = deserializeWithXtream(file);\n");
      out.print("    }catch (Exception e){\n");
      out.print("    }\n");
      out.print("    return obj;\n");
      out.print("  }");
      out.print(" \n");
      out.print("  static private Object deserializeWithXtream(File e)\n");
      out.print(
          "      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {\n");
      out.print("    XStream xtream = new XStream();\n");
      out.print("    xtream.ignoreUnknownElements();\n");
      out.print("    List<String> myConverters = new ArrayList<String>();\n");
      out.print("    " + getConvertersDeclaration(converters) + "\n");
      out.print("    Object obj = null;\n");
      out.print("    try {\n");
      out.print("      obj = xtream.fromXML(e);\n");
      out.print("    }catch (Exception exception){\n");
      out.print("      exception.printStackTrace();\n");
      out.print("    }    if (obj == null) {\n");
      out.print("      for (String converter : myConverters) {\n");
      out.print("        int i = 0;\n");
      out.print("        while (obj == null && i < myConverters.size()) {\n");
      out.print("          xtream.registerConverter(\n");
      out.print("              (Converter) Class.forName(converter).getConstructor().newInstance());\n");
      out.print("          try {\n");
      out.print("            obj = xtream.fromXML(e);\n");
      out.print("          }catch (Exception ex){\n");
      out.print("            ex.printStackTrace();\n");
      out.print("          }\n");
      out.print("          i++;\n");
      out.print("        }\n");
      out.print("      }\n");
      out.print("    }\n");
      out.print("    return obj;\n");
      out.print("  }");
      out.print("}");
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private String getConvertersDeclaration(List<String> converters) {
    String declaration = "";
    for (String converter : converters) {
      declaration += "myConverters.add(" + converter + ".class.getName());\n";
    }
    return declaration;
  }
}
