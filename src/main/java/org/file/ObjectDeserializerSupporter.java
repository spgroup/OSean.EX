package org.file;

import java.io.File;
import java.util.List;

public class ObjectDeserializerSupporter extends ObjectSerializerSupporter{

  public ObjectDeserializerSupporter(String packageName) {
    super(packageName);
  }

  public boolean getOutputClass(String pomFileDirectory, String resourceDirectory, List<String> converters){
    String classText = ""
        + "package "+this.fullSerializerSupporterClass+";\n\n"
        + "import com.thoughtworks.xstream.XStream;\n"
        + "import com.thoughtworks.xstream.converters.Converter;\n"
        + "import java.io.File;\n"
        + "import java.io.PrintWriter;\n"
        + "import java.lang.reflect.InvocationTargetException;\n"
        + "import java.lang.reflect.Modifier;\n"
        + "import java.util.ArrayList;\n"
        + "import java.util.HashMap;\n"
        + "import java.util.List;\n"
        + "import java.util.Map;\n\n"
        + ""
        + "public class ObjectDeserializerSupporter {\n"
        + "\n"
        + "  private static File resourceDirectory = new File(\""+resourceDirectory+"\");\n"
        + "\n"
        + "  public static void main(String[] args) throws ClassNotFoundException {\n"
        + "    List<String> methods = new ArrayList<String>();\n"
        + "    File serializedObjects[] = new File(String.valueOf(ObjectDeserializerSupporter.resourceDirectory)).listFiles();\n"
        + "    for(File file: serializedObjects){\n"
        + "      Object serializedObject = null;\n"
        + "      serializedObject = deserializeAny(file);\n"
        + "      try {\n"
        + "        if (serializedObject != null && serializedObject.getClass().getName() != null &&\n"
        + "            Modifier.toString(\n"
        + "                    Class.forName(getCanonicalClassName(serializedObject)).getModifiers())\n"
        + "                .contains(\"public\")) {\n"
        + "          String aux = String\n"
        + "              .format(\"\\tpublic %s deserializeObject%s() throws IOException {\\n\"\n"
        + "                      + \"\\t\\tjava.io.InputStream s = SerializedObjectSupporter.class.getClassLoader().getResourceAsStream(\\\"serializedObjects\\\"+File.separator+\\\"%s\\\");\\n\"\n"
        + "                      + \"\\t\\treturn (%s) deserializeAny(readFileContents(s));\\n\" +\n"
        + "                      \"\\t}\", getCanonicalClassName(serializedObject),\n"
        + "                  file.getName().replace(\".xml\", \"\"), file.getName(),\n"
        + "                  getCanonicalClassName(serializedObject), file.getPath());\n"
        + "          methods.add(aux);\n"
        + "        }\n"
        + "      }catch (Exception e){\n"
        + "        e.printStackTrace();\n"
        + "      }\n"
        + "      saveFile(methods, \"output-methods\");\n"
        + "    }\n"
        + "  }\n"
        + "  "
        + "  public static String getCanonicalClassName(Object object){\n"
        + "    if (object.getClass().getEnclosingClass() != null){\n"
        + "      return object.getClass().getEnclosingClass().getCanonicalName();\n"
        + "    }else{\n"
        + "      return object.getClass().getCanonicalName();\n"
        + "    }\n"
        + "  }"
        + ""
        + "  private static boolean saveFile(List<String> methods, String fileName){\n"
        + "    try{\n"
        + "      PrintWriter writer = new PrintWriter(resourceDirectory.getPath()+File.separator+fileName+\".txt\");\n"
        + "      for(String oneMethod: methods){\n"
        + "        writer.println(oneMethod);\n"
        + "      }\n"
        + "      writer.close();\n"
        + "      return true;\n"
        + "    }catch(Exception e){\n"
        + "      e.printStackTrace();\n"
        + "    }\n"
        + "    return false;\n"
        + "  }\n"
        + "\n"
        + "  private static Object deserializeAny(File file) {\n"
        + "    Object obj = null;\n"
        + "    try{\n"
        + "      obj = deserializeWithXtream(file);\n"
        + "    }catch (Exception e){\n"
        + "    }\n"
        + "    return obj;\n"
        + "  }"
        + " \n"
        + "  static private Object deserializeWithXtream(File e)\n"
        + "      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {\n"
        + "    XStream xtream = new XStream();\n"
        + "    List<String> myConverters = new ArrayList<String>();\n"
        + "    "+getConvertersDeclaration(converters)+"\n"
        + "    Object obj = null;\n"
        + "    try {\n"
        + "      obj = xtream.fromXML(e);\n"
        + "    }catch (Exception exception){\n"
        + "      exception.printStackTrace();\n"
        + "    }    if (obj == null) {\n"
        + "      for (String converter : myConverters) {\n"
        + "        int i = 0;\n"
        + "        while (obj == null && i < myConverters.size()) {\n"
        + "          xtream.registerConverter(\n"
        + "              (Converter) Class.forName(converter).getConstructor().newInstance());\n"
        + "          try {\n"
        + "            obj = xtream.fromXML(e);\n"
        + "          }catch (Exception ex){\n"
        + "            ex.printStackTrace();\n"
        + "          }\n"
        + "          i++;\n"
        + "        }\n"
        + "      }\n"
        + "    }\n"
        + "    return obj;\n"
        + "  }"
        + "}";
    try {
      saveFile(pomFileDirectory, classText, "ObjectDeserializerSupporter");
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean deleteObjectSerializerSupporterClass(String fileDirectory) {
    return  new File(fileDirectory+File.separator+"ObjectDeserializerSupporter.java").delete();
  }

  private String getConvertersDeclaration(List<String> converters){
    String declaration = "";
    for(String converter: converters){
      declaration += "myConverters.add("+converter+".class.getName());\n";
    }
    return declaration;
  }
}
