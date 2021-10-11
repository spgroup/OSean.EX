package org.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ObjectSerializerSupporter {
  private String fullSerializerSupporterClass;

  public ObjectSerializerSupporter (String packageName){
    if(packageName.split("src.main.java.").length > 1){
      this.fullSerializerSupporterClass = packageName.split("src.main.java.")[1];
    }else{
      this.fullSerializerSupporterClass = packageName;
    }
  }

  public String getFullSerializerSupporterClass(){
    return this.fullSerializerSupporterClass;
  }

  public boolean getOutputClass(String pomFileDirectory, String resourceDirectory){
    String classText = ""
        + "package "+this.fullSerializerSupporterClass+";\n\n"
        + "import com.thoughtworks.xstream.XStream;\n"
        + "import java.io.File;\n"
        + "import java.io.FileWriter;\n"
        + "import java.io.IOException;\n"
        + "import java.io.UnsupportedEncodingException;\n"
        + "import java.lang.reflect.Field;\n"
        + "import java.util.ArrayList;\n"
        + "import java.util.HashMap;\n"
        + "import java.lang.reflect.Modifier;\n"
        + "import java.util.List;\n"
        + "import java.io.PrintWriter;\n"
        + "import org.apache.commons.lang3.builder.EqualsBuilder;\n\n"
        + "public class ObjectSerializerSupporter {\n"
        + "\n"
        + "  private static File resourceDirectory = new File(\""+resourceDirectory+"\");\n"
        + "  public static HashMap<String, Integer> count = new HashMap<String, Integer>();\n"
        + "  public static HashMap<String, ArrayList<Object>> serializedObjects = new HashMap<String, ArrayList<Object>>();\n"
        + "\n"
        + "  public static void serializeWithXtreamOut(Object request) throws UnsupportedEncodingException {\n"
        + "    if (resourceDirectory != null){\n"
        + "      for(Field field: request.getClass().getDeclaredFields()){\n"
        + "          try {\n"
        + "            Object a = field.get(request);\n"
        + "            XStream xtream = new XStream();\n"
        + "            String xml = xtream.toXML(a);\n"
        + "            if (!count.keySet().contains(getClassName(field))){\n"
        + "              count.put(getClassName(field), 1);\n"
        + "              writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+getClassName(field)+\"\"+count.get(getClassName(field))+\".xml\").getPath());\n"
        + "              serializedObjects.put(getClassName(field), new ArrayList<Object>());\n"
        + "              serializedObjects.get(getClassName(field)).add(a);\n"
        + "            }else{\n"
        + "              ArrayList<Object> aux = serializedObjects.get(getClassName(field));\n"
        + "              boolean isEqual = false;\n"
        + "              for(Object obj: aux){\n"
        + "                if (EqualsBuilder.reflectionEquals(obj, a, false)){\n"
        + "                  isEqual = true;\n"
        + "                  break;\n"
        + "                }\n"
        + "              }\n"
        + "              if (!isEqual){\n"
        + "                count.replace(getClassName(field), count.get(getClassName(field))+1);\n"
        + "                aux.add(a);\n"
        + "                serializedObjects.replace(getClassName(field), aux);\n"
        + "                writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+getClassName(field)+\"\"+count.get(getClassName(field))+\".xml\").getPath());\n"
        + "              }\n"
        + "\n"
        + "            }\n"
        + "          } catch (IllegalAccessException e) {\n"
        + "            e.printStackTrace();\n"
        + "          }\n"
        + "      }\n"
        + "      if (count.keySet().contains(getClassName(request))){\n"
        + "        count.replace(getClassName(request), count.get(getClassName(request))+1);\n"
        + "      }else{\n"
        + "        count.put(getClassName(request), 1);\n"
        + "      }\n"
        + "      XStream xtream = new XStream();\n"
        + "      String xml = xtream.toXML(request);\n"
        + "      System.out.println(getClassName(request));\n"
        + "      writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+getClassName(request)+\"\"+count.get(getClassName(request))+\".xml\").getPath());\n"
        + "    }\n"
        + "  }\n"
        + "\n"
        + "  public static String getClassName(Object object){\n"
        + "    if (object.getClass().getEnclosingClass() != null){\n"
        + "      return object.getClass().getEnclosingClass().getSimpleName();\n"
        + "    }else{\n"
        + "      return object.getClass().getSimpleName();\n"
        + "    }\n"
        + "  }\n"
        + "\n"
        + "  public static String getClassName(Field object){\n"
        + "    if (object.getType().getClass().getEnclosingClass() != null){\n"
        + "      return object.getType().getEnclosingClass().getSimpleName();\n"
        + "    }else{\n"
        + "      return object.getType().getSimpleName();\n"
        + "    }\n"
        + "  }"
        + "\n"
        + "  private static void writeUsingFileWriter(String data, String path) {\n"
        + "    File file = new File(path);\n"
        + "    FileWriter fr = null;\n"
        + "    try {\n"
        + "      fr = new FileWriter(file);\n"
        + "      fr.write(data);\n"
        + "    } catch (IOException e) {\n"
        + "      e.printStackTrace();\n"
        + "    }finally{\n"
        + "      try {\n"
        + "        fr.close();\n"
        + "      } catch (IOException e) {\n"
        + "        e.printStackTrace();\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + " \n"
        + "\n"
        + "  public static void main(String[] args) throws ClassNotFoundException {\n"
        + "    List<String> methods = new ArrayList<String>();\n"
        + "    File serializedObjects[] = new File(String.valueOf(ObjectSerializerSupporter.resourceDirectory)).listFiles();\n"
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
        + "      saveFile(methods);\n"
        + "    }\n"
        + "  }\n"
        + "\n"
        + "  public static String getCanonicalClassName(Object object){\n"
        + "    if (object.getClass().getEnclosingClass() != null){\n"
        + "      return object.getClass().getEnclosingClass().getCanonicalName();\n"
        + "    }else{\n"
        + "      return object.getClass().getCanonicalName();\n"
        + "    }\n"
        + "  }"
        + "\n"
        + " private static boolean saveFile(List<String> methods){\n"
        + "   try{\n"
        + "     PrintWriter writer = new PrintWriter(resourceDirectory.getPath()+File.separator+\"output-methods.txt\");\n"
        + "     for(String oneMethod: methods){\n"
        + "         writer.println(oneMethod);\n"
        + "     }\n"
        + "     writer.close();\n"
        + "     return true;\n"
        + "   }catch(Exception e){\n"
        + "     e.printStackTrace();\n"
        + "   }\n"
        + "   return false;\n"
        + "}\n"
        + "\n"
        + "  private static Object deserializeAny(File file) {\n"
        + "    Object obj = null;\n"
        + "    try{\n"
        + "      obj = deserializeWithXtream(file);\n"
        + "    }catch (Exception e){\n"
        + "    }\n"
        + "    return obj;\n"
        + "  }\n"
        + "\n"
        + "  static private Object deserializeWithXtream(File e) {\n"
        + "    XStream xtream = new XStream();\n"
        + "    Object obj = xtream.fromXML(e);\n"
        + "    return obj;\n"
        + "  }\n"
        + "}";
    try {
      saveFile(pomFileDirectory, classText);
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  private boolean saveFile(String fileDirectory, String contents) {
    deleteObjectSerializerSupporterClass(fileDirectory);
    try(FileWriter fw = new FileWriter(fileDirectory+File.separator+"ObjectSerializerSupporter.java", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw))
    {
      out.println(contents);
      out.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean deleteObjectSerializerSupporterClass(String fileDirectory) {
    return  new File(fileDirectory+File.separator+"ObjectSerializerSupporter.java").delete();
  }

}
