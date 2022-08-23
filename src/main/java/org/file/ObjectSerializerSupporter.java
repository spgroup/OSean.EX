package org.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ObjectSerializerSupporter {
  protected String fullSerializerSupporterClass;
  private String resourceDirectory;

  public ObjectSerializerSupporter (String packageName){
    if(packageName.split("src.main.java.").length > 1){
      this.fullSerializerSupporterClass = packageName.split("src.main.java.")[1];
    }else{
      this.fullSerializerSupporterClass = packageName;
    }
    this.resourceDirectory = "";
  }

  public String getFullSerializerSupporterClass(){
    return this.fullSerializerSupporterClass;
  }

  public boolean getOutputClass(String pomFileDirectory, String resourceDirectory){
    this.resourceDirectory = resourceDirectory;
    String classText = ""
        + "package "+this.fullSerializerSupporterClass+";\n\n"
        + "import com.thoughtworks.xstream.XStream;\n"
        + "import com.thoughtworks.xstream.converters.Converter;\n"
        + "import java.io.File;\n"
        + "import java.io.FileWriter;\n"
        + "import java.io.IOException;\n"
        + "import java.io.UnsupportedEncodingException;\n"
        + "import java.lang.reflect.Field;\n"
        + "import java.lang.reflect.InvocationTargetException;\n"
        + "import java.util.ArrayList;\n"
        + "import java.util.Collections;\n"
        + "import java.util.HashMap;\n"
        + "import java.lang.reflect.Modifier;\n"
        + "import java.util.List;\n"
        + "import java.io.PrintWriter;\n"
        + "import java.util.Map;\n"
        + "import org.apache.commons.lang3.builder.EqualsBuilder;\n\n"
        + "public class ObjectSerializerSupporter {\n"
        + "\n"
        + "  private static File resourceDirectory = new File(\""+resourceDirectory+"\");\n"
        + "  public static Map<String, ArrayList<Object>> serializedObjects = Collections.synchronizedMap(new HashMap<String, ArrayList<Object>>());\n"
        + "  public static ArrayList<String> converters = new ArrayList<String>();\n"
        + "\n"
        + "  public static void serializeWithXtreamOut(Object request) throws UnsupportedEncodingException, IllegalAccessException {\n"
        + "    if (resourceDirectory != null){\n"
        + "      for(Field field: request.getClass().getDeclaredFields()) {\n"
        + "        if (!field.getType().isPrimitive() && Modifier.isPublic(field.getModifiers())) {\n"
        + "          if (org.mockito.Mockito.mockingDetails(field.get(request)).isMock()) {\n"
        + "            field.setAccessible(true);\n"
        + "            field.set(request, null);\n"
        + "          }\n"
        + "          try {\n"
        + "            Object a = field.get(request);\n"
        + "            XStream xtream = new XStream();\n"
        + "            String xml = xtream.toXML(a);\n"
        + "            if (!serializedObjects.keySet().contains(getClassName(field))) {\n"
        + "              serializedObjects.put(getClassName(field), new ArrayList<Object>());\n"
        + "              serializedObjects.get(getClassName(field)).add(a);\n"
        + "              writeUsingFileWriter(xml, new File(\n"
        + "                  resourceDirectory.getPath() + File.separator + getClassName(field) + \"\"\n"
        + "                      + serializedObjects.get(getClassName(field)).size() + \".xml\").getPath());\n"
        + "              if (!converters.contains(field.getType().getCanonicalName()) && !field.getType()\n"
        + "                  .isPrimitive() && !field.getType().toString().contains(\"[\")) {\n"
        + "                converters.add(field.getType().getCanonicalName());\n"
        + "              }\n"
        + "            } else {\n"
        + "              ArrayList<Object> aux = serializedObjects.get(getClassName(field));\n"
        + "              boolean isEqual = false;\n"
        + "              for (Object obj : aux) {\n"
        + "                if (EqualsBuilder.reflectionEquals(a, obj, true, null, true)) {\n"
        + "                  isEqual = true;\n"
        + "                  break;\n"
        + "                }\n"
        + "              }\n"
        + "              if (!isEqual) {\n"
        + "                aux.add(a);\n"
        + "                serializedObjects.replace(getClassName(field), aux);\n"
        + "                writeUsingFileWriter(xml, new File(\n"
        + "                    resourceDirectory.getPath() + File.separator + getClassName(field) + \"\"\n"
        + "                        + serializedObjects.get(getClassName(field)).size() + \".xml\").getPath());\n"
        + "              }\n"
        + "\n"
        + "            }\n"
        + "          } catch (IllegalAccessException e) {\n"
        + "            e.printStackTrace();\n"
        + "          }\n"
        + "        }"
        + "      }\n"
        + "      if (!request.getClass().isPrimitive()){\n"
        + "        XStream xtream = new XStream();\n"
        + "        String xml = xtream.toXML(request);\n"
        + "        if (serializedObjects.keySet().contains(getClassName(request))){\n"
        + "              ArrayList<Object> aux = serializedObjects.get(getClassName(request));\n"
        + "              boolean isEqual = false;\n"
        + "              for (Object obj : aux) {\n"
        + "                if (EqualsBuilder.reflectionEquals(request, obj, true, null, true)) {\n"
        + "                  isEqual = true;\n"
        + "                  break;\n"
        + "                }\n"
        + "              }\n"
        + "              if (!isEqual) {\n"
        + "                aux.add(request);\n"
        + "                serializedObjects.replace(getClassName(request), aux);\n"
        + "                writeUsingFileWriter(xml, new File(\n"
        + "                    resourceDirectory.getPath() + File.separator + getClassName(request) + \"\"\n"
        + "                        + aux.size() + \".xml\").getPath());\n"
        + "              }\n"
        + "        } else{\n"
        + "           serializedObjects.put(getClassName(request), new ArrayList<Object>());\n"
        + "           serializedObjects.get(getClassName(request)).add(request);\n"
        + "           writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+getClassName(request)+\"\"+1+\".xml\").getPath());\n"
        + "        }\n"
        + "        if (!converters.contains(getCanonicalClassName(request))){\n"
        + "          converters.add(getCanonicalClassName(request));\n"
        + "        }\n"
        + "      }"
        + "    }\n"
        + "    saveFile(converters, \"converters-name\");"
        + "  }\n"
        + "\n"
        + " public static String getClassNameMethodSignature(Object object) {\n"
        + "    if (object.getClass().getEnclosingClass() != null) {\n"
        + "      if (object.getClass().getCanonicalName().equals(\"java.util.Arrays.ArrayList\") || object.getClass().getCanonicalName().equals(\"java.util.Collections.UnmodifiableRandomAccessList\")){\n"
        + "        return java.util.List.class.getCanonicalName();\n"
        + "      } else if (object.getClass().getCanonicalName().equals(\"java.util.Collections.SynchronizedSet\")){\n"
        + "        return java.util.Collections.class.getCanonicalName();\n"
        + "      }\n"
        + "    }\n"
        + "      return object.getClass().getCanonicalName();\n"
        + "  }\n"
        + "\n"
        + "  public static String getClassName(Object object){\n"
        + "    if (isThereEnclosingClass(object.getClass())){\n"
        + "      return object.getClass().getEnclosingClass().getSimpleName();\n"
        + "    }else{\n"
        + "      return object.getClass().getSimpleName();\n"
        + "    }\n"
        + "  }\n"
        + "\n"
        + "  public static String getClassName(Field object){\n"
        + "    if (isThereEnclosingClass(object.getType().getClass())){\n"
        + "      return object.getType().getEnclosingClass().getSimpleName();\n"
        + "    }else{\n"
        + "      return object.getType().getSimpleName();\n"
        + "    }\n"
        + "  }\n"
        + "\n"
        + "  public static boolean isThereEnclosingClass(Class currentClass){\n"
        + "    if (currentClass.getEnclosingClass() != null){\n"
        + "      return true;\n"
        + "    }else{\n"
        + "      return false;\n"
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
        + "                      \"\\t}\", getClassNameMethodSignature(serializedObject),\n"
        + "                  file.getName().replace(\".xml\", \"\"), file.getName(),\n"
        + "                  getClassNameMethodSignature(serializedObject), file.getPath());\n"
        + "          methods.add(aux);\n"
        + "        }\n"
        + "      }catch (Exception e){\n"
        + "        e.printStackTrace();\n"
        + "      }\n"
        + "      saveFile(methods, \"output-methods\");\n"
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
        + " private static boolean saveFile(List<String> methods, String fileName){\n"
        + "   try{\n"
        + "     PrintWriter writer = new PrintWriter(resourceDirectory.getPath()+File.separator+fileName+\".txt\");\n"
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
        + "       System.out.println(e);"
        + "    }\n"
        + "    return obj;\n"
        + "  }\n"
        + "\n"
        + "  static private Object deserializeWithXtream(File e)\n"
        + "      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {\n"
        + "    XStream xtream = new XStream();\n"
        + "    HashMap<String, String> myConverters = new HashMap<String, String>();\n"
        + "    Object obj = null;\n"
        + "    try {\n"
        + "      obj = xtream.fromXML(e);\n"
        + "    }catch (Exception exception){\n"
        + "      exception.printStackTrace();\n"
        + "    }"
        + "    if (obj == null) {\n"
        + "      for (Map.Entry<String, String> converter : myConverters.entrySet()) {\n"
        + "        int i = 0;\n"
        + "        while (obj == null && i < myConverters.size()) {\n"
        + "          xtream.registerConverter(\n"
        + "              (Converter) Class.forName(converter.getValue()).getConstructor().newInstance());\n"
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
        + "  }\n"
        + "}";
    try {
      saveFile(pomFileDirectory, classText, "ObjectSerializerSupporter");
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  protected boolean saveFile(String fileDirectory, String contents, String fileName) {
    deleteObjectSerializerSupporterClass(fileDirectory);
    try(FileWriter fw = new FileWriter(fileDirectory+File.separator+fileName+".java", true);
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

  public String getClassPackage(){
    return this.fullSerializerSupporterClass;
  }

  public String getResourceDirectory(){
    return this.resourceDirectory;
  }
}
