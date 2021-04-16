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
        + "        if (!field.getType().isPrimitive() && Modifier.isPublic(field.getModifiers())){\n"
        + "          try {\n"
        + "            Object a = field.get(request);\n"
        + "            XStream xtream = new XStream();\n"
        + "            String xml = xtream.toXML(a);\n"
        + "            if (!count.keySet().contains(field.getName())){\n"
        + "              count.put(field.getName(), 1);\n"
        + "              writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+field.getName()+\"\"+count.get(field.getName())+\".xml\").getPath());\n"
        + "              serializedObjects.put(field.getName(), new ArrayList<Object>());\n"
        + "              serializedObjects.get(field.getName()).add(a);\n"
        + "            }else{\n"
        + "              ArrayList<Object> aux = serializedObjects.get(field.getName());\n"
        + "              boolean isEqual = false;\n"
        + "              for(Object obj: aux){\n"
        + "                if (EqualsBuilder.reflectionEquals(obj, a, false)){\n"
        + "                  isEqual = true;\n"
        + "                  break;\n"
        + "                }\n"
        + "              }\n"
        + "              if (!isEqual){\n"
        + "                count.replace(field.getName(), count.get(field.getName())+1);\n"
        + "                aux.add(a);\n"
        + "                serializedObjects.replace(field.getName(), aux);\n"
        + "                writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+field.getName()+\"\"+count.get(field.getName())+\".xml\").getPath());\n"
        + "              }\n"
        + "\n"
        + "            }\n"
        + "          } catch (IllegalAccessException e) {\n"
        + "            e.printStackTrace();\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "      if (count.keySet().contains(request.getClass().getName())){\n"
        + "        count.replace(request.getClass().getName(), count.get(request.getClass().getName())+1);\n"
        + "      }else{\n"
        + "        count.put(request.getClass().getName(), 1);\n"
        + "      }\n"
        + "      XStream xtream = new XStream();\n"
        + "      String xml = xtream.toXML(request);\n"
        + "      writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+request.getClass().getSimpleName()+\"\"+count.get(request.getClass().getName())+\".xml\").getPath());\n"
        + "    }\n"
        + "  }\n"
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
        + "public static void main(String[] args) {\n"
        + "    List<String> methods = new ArrayList<String>();\n"
        + "    File serializedObjects[] = new File(String.valueOf(ObjectSerializerSupporter.resourceDirectory)).listFiles();\n"
        + "    for(File file: serializedObjects){\n"
        + "      Object serializedObject = null;\n"
        + "      serializedObject = deserializeAny(file);\n"
        + "      if (serializedObject!= null && serializedObject.getClass().getName() != null && !serializedObject.getClass().getCanonicalName().contains(\"EmptyList\")) {\n"
        + "        String aux = String\n"
        + "            .format(\"\\tpublic %s deserializeObject%s() throws IOException {\\n\"\n"
        + "                    +\"\\t\\tjava.io.InputStream s = SerializedObjectSupporter.class.getClassLoader().getResourceAsStream(\\\"%s\\\");\\n\"\n"
        + "                    + \"\\t\\treturn (%s) deserializeAny(readFileContents(s));\\n\" +\n"
        + "                    \"\\t}\", serializedObject.getClass().getCanonicalName(),\n"
        + "                file.getName().replace(\".xml\", \"\"), file.getName(),\n"
        + "                serializedObject.getClass().getCanonicalName(), file.getPath());\n"
        + "        methods.add(aux);\n"
        + "      }\n"
        + "\n"
        + "    }\n"
        + "     for(String method: methods){\n"
        + "         System.out.println(\"\\n\"+method);\n"
        + "     }\n"
        + "  }\n"
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
