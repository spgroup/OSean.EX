package org.transformations.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ObjectSerializerSupporter {
  protected String fullSerializerSupporterClass;
  protected String classFilePath;

  public ObjectSerializerSupporter(String packageName, String fileLocalPath) {
    if (packageName.split("src.main.java.").length > 1) {
      this.fullSerializerSupporterClass = packageName.split("src.main.java.")[1];
    } else {
      this.fullSerializerSupporterClass = packageName;
    }
    this.classFilePath = fileLocalPath + File.separator + "ObjectSerializerSupporter.java";
  }

  public String getClassPackage() {
    return this.fullSerializerSupporterClass;
  }

  public String getSignatureClass() {
    return getClassPackage() + ".ObjectSerializerSupporter";
  }

  public boolean writeClassFile(String fileDirectory, String resourceDirectory) {
    try (FileWriter fw = new FileWriter(fileDirectory + File.separator + "ObjectSerializerSupporter.java");
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
      out.print("package " + this.fullSerializerSupporterClass + ";\n\n");
      out.print("import com.thoughtworks.xstream.XStream;\n");
      out.print("import com.thoughtworks.xstream.converters.Converter;\n");
      out.print("import java.io.File;\n");
      out.print("import java.io.FileWriter;\n");
      out.print("import java.io.IOException;\n");
      out.print("import java.io.UnsupportedEncodingException;\n");
      out.print("import java.lang.reflect.Field;\n");
      out.print("import java.lang.reflect.InvocationTargetException;\n");
      out.print("import java.util.ArrayList;\n");
      out.print("import java.util.Collections;\n");
      out.print("import java.util.HashMap;\n");
      out.print("import java.lang.reflect.Modifier;\n");
      out.print("import java.util.List;\n");
      out.print("import java.io.PrintWriter;\n");
      out.print("import java.util.Map;\n");
      out.print("import org.apache.commons.lang3.builder.EqualsBuilder;\n\n");
      out.print("public class ObjectSerializerSupporter {\n");
      out.print("\n");
      out.print("  private static File resourceDirectory = new File(\"" + resourceDirectory + "\");\n");
      out.print(
          "  public static Map<String, ArrayList<Object>> serializedObjects = Collections.synchronizedMap(new HashMap<String, ArrayList<Object>>());\n");
      out.print("  public static ArrayList<String> converters = new ArrayList<String>();\n");
      out.print("\n");
      out.print(
          "  public static void serializeWithXtreamOut(Object request) throws UnsupportedEncodingException, IllegalAccessException {\n");
      out.print("    if (resourceDirectory != null){\n");
      out.print("      for(Field field: request.getClass().getDeclaredFields()) {\n");
      out.print("        if (!field.getType().isPrimitive() && Modifier.isPublic(field.getModifiers())) {\n");
      out.print("          if (org.mockito.Mockito.mockingDetails(field.get(request)).isMock()) {\n");
      out.print("            field.setAccessible(true);\n");
      out.print("            field.set(request, null);\n");
      out.print("          }\n");
      out.print("          try {\n");
      out.print("            Object a = field.get(request);\n");
      out.print("            XStream xtream = new XStream();\n");
      out.print("            String xml = xtream.toXML(a);\n");
      out.print("            if (!serializedObjects.keySet().contains(getClassName(field))) {\n");
      out.print("              serializedObjects.put(getClassName(field), new ArrayList<Object>());\n");
      out.print("              serializedObjects.get(getClassName(field)).add(a);\n");
      out.print("              writeUsingFileWriter(xml, new File(\n");
      out.print("                  resourceDirectory.getPath() + File.separator + getClassName(field) + \"\"\n");
      out.print("                      + serializedObjects.get(getClassName(field)).size() + \".xml\").getPath());\n");
      out.print("              if (!converters.contains(field.getType().getCanonicalName()) && !field.getType()\n");
      out.print("                  .isPrimitive() && !field.getType().toString().contains(\"[\")) {\n");
      out.print("                converters.add(field.getType().getCanonicalName());\n");
      out.print("              }\n");
      out.print("            } else {\n");
      out.print("              ArrayList<Object> aux = serializedObjects.get(getClassName(field));\n");
      out.print("              boolean isEqual = false;\n");
      out.print("              for (Object obj : aux) {\n");
      out.print("                if (EqualsBuilder.reflectionEquals(a, obj, true, null, true)) {\n");
      out.print("                  isEqual = true;\n");
      out.print("                  break;\n");
      out.print("                }\n");
      out.print("              }\n");
      out.print("              if (!isEqual) {\n");
      out.print("                aux.add(a);\n");
      out.print("                serializedObjects.replace(getClassName(field), aux);\n");
      out.print("                writeUsingFileWriter(xml, new File(\n");
      out.print("                    resourceDirectory.getPath() + File.separator + getClassName(field) + \"\"\n");
      out.print(
          "                        + serializedObjects.get(getClassName(field)).size() + \".xml\").getPath());\n");
      out.print("              }\n");
      out.print("\n");
      out.print("            }\n");
      out.print("          } catch (IllegalAccessException e) {\n");
      out.print("            e.printStackTrace();\n");
      out.print("          }\n");
      out.print("        }");
      out.print("      }\n");
      out.print("      if (!request.getClass().isPrimitive()){\n");
      out.print("        XStream xtream = new XStream();\n");
      out.print("        String xml = xtream.toXML(request);\n");
      out.print("        if (serializedObjects.keySet().contains(getClassName(request))){\n");
      out.print("              ArrayList<Object> aux = serializedObjects.get(getClassName(request));\n");
      out.print("              boolean isEqual = false;\n");
      out.print("              for (Object obj : aux) {\n");
      out.print("                if (EqualsBuilder.reflectionEquals(request, obj, true, null, true)) {\n");
      out.print("                  isEqual = true;\n");
      out.print("                  break;\n");
      out.print("                }\n");
      out.print("              }\n");
      out.print("              if (!isEqual) {\n");
      out.print("                aux.add(request);\n");
      out.print("                serializedObjects.replace(getClassName(request), aux);\n");
      out.print("                writeUsingFileWriter(xml, new File(\n");
      out.print("                    resourceDirectory.getPath() + File.separator + getClassName(request) + \"\"\n");
      out.print("                        + aux.size() + \".xml\").getPath());\n");
      out.print("              }\n");
      out.print("        } else{\n");
      out.print("           serializedObjects.put(getClassName(request), new ArrayList<Object>());\n");
      out.print("           serializedObjects.get(getClassName(request)).add(request);\n");
      out.print(
          "           writeUsingFileWriter(xml, new File(resourceDirectory.getPath()+File.separator+getClassName(request)+\"\"+1+\".xml\").getPath());\n");
      out.print("        }\n");
      out.print("        if (!converters.contains(getCanonicalClassName(request))){\n");
      out.print("          converters.add(getCanonicalClassName(request));\n");
      out.print("        }\n");
      out.print("      }");
      out.print("    }\n");
      out.print("    saveFile(converters, \"converters-name\");");
      out.print("  }\n");
      out.print("\n");
      out.print(" public static String getClassNameMethodSignature(Object object) {\n");
      out.print("    if (object.getClass().getEnclosingClass() != null) {\n");
      out.print(
          "      if (object.getClass().getCanonicalName().equals(\"java.util.Arrays.ArrayList\") || object.getClass().getCanonicalName().equals(\"java.util.Collections.UnmodifiableRandomAccessList\")){\n");
      out.print("        return java.util.List.class.getCanonicalName();\n");
      out.print(
          "      } else if (object.getClass().getCanonicalName().equals(\"java.util.Collections.SynchronizedSet\")){\n");
      out.print("        return java.util.Collections.class.getCanonicalName();\n");
      out.print("      }\n");
      out.print("    }\n");
      out.print("      return object.getClass().getCanonicalName();\n");
      out.print("  }\n");
      out.print("\n");
      out.print("  public static String getClassName(Object object){\n");
      out.print("    if (isThereEnclosingClass(object.getClass())){\n");
      out.print("      return object.getClass().getEnclosingClass().getSimpleName();\n");
      out.print("    }else{\n");
      out.print("      return object.getClass().getSimpleName();\n");
      out.print("    }\n");
      out.print("  }\n");
      out.print("\n");
      out.print("  public static String getClassName(Field object){\n");
      out.print("    if (isThereEnclosingClass(object.getType().getClass())){\n");
      out.print("      return object.getType().getEnclosingClass().getSimpleName();\n");
      out.print("    }else{\n");
      out.print("      return object.getType().getSimpleName();\n");
      out.print("    }\n");
      out.print("  }\n");
      out.print("\n");
      out.print("  public static boolean isThereEnclosingClass(Class currentClass){\n");
      out.print("    if (currentClass.getEnclosingClass() != null){\n");
      out.print("      return true;\n");
      out.print("    }else{\n");
      out.print("      return false;\n");
      out.print("    }\n");
      out.print("  }");
      out.print("\n");
      out.print("  private static void writeUsingFileWriter(String data, String path) {\n");
      out.print("    File file = new File(path);\n");
      out.print("    FileWriter fr = null;\n");
      out.print("    try {\n");
      out.print("      fr = new FileWriter(file);\n");
      out.print("      fr.write(data);\n");
      out.print("    } catch (IOException e) {\n");
      out.print("      e.printStackTrace();\n");
      out.print("    }finally{\n");
      out.print("      try {\n");
      out.print("        fr.close();\n");
      out.print("      } catch (IOException e) {\n");
      out.print("        e.printStackTrace();\n");
      out.print("      }\n");
      out.print("    }\n");
      out.print("  }\n");
      out.print(" \n");
      out.print("\n");
      out.print("  public static void main(String[] args) throws ClassNotFoundException {\n");
      out.print("    List<String> methods = new ArrayList<String>();\n");
      out.print(
          "    File serializedObjects[] = new File(String.valueOf(ObjectSerializerSupporter.resourceDirectory)).listFiles();\n");
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
      out.print("                      \"\\t}\", getClassNameMethodSignature(serializedObject),\n");
      out.print("                  file.getName().replace(\".xml\", \"\"), file.getName(),\n");
      out.print("                  getClassNameMethodSignature(serializedObject), file.getPath());\n");
      out.print("          methods.add(aux);\n");
      out.print("        }\n");
      out.print("      }catch (Exception e){\n");
      out.print("        e.printStackTrace();\n");
      out.print("      }\n");
      out.print("      saveFile(methods, \"output-methods\");\n");
      out.print("    }\n");
      out.print("  }\n");
      out.print("\n");
      out.print("  public static String getCanonicalClassName(Object object){\n");
      out.print("    if (object.getClass().getEnclosingClass() != null){\n");
      out.print("      return object.getClass().getEnclosingClass().getCanonicalName();\n");
      out.print("    }else{\n");
      out.print("      return object.getClass().getCanonicalName();\n");
      out.print("    }\n");
      out.print("  }");
      out.print("\n");
      out.print(" private static boolean saveFile(List<String> methods, String fileName){\n");
      out.print("   try{\n");
      out.print(
          "     PrintWriter writer = new PrintWriter(resourceDirectory.getPath()+File.separator+fileName+\".txt\");\n");
      out.print("     for(String oneMethod: methods){\n");
      out.print("         writer.println(oneMethod);\n");
      out.print("     }\n");
      out.print("     writer.close();\n");
      out.print("     return true;\n");
      out.print("   }catch(Exception e){\n");
      out.print("     e.printStackTrace();\n");
      out.print("   }\n");
      out.print("   return false;\n");
      out.print("}\n");
      out.print("\n");
      out.print("  private static Object deserializeAny(File file) {\n");
      out.print("    Object obj = null;\n");
      out.print("    try{\n");
      out.print("      obj = deserializeWithXtream(file);\n");
      out.print("    }catch (Exception e){\n");
      out.print("       System.out.print(e);");
      out.print("    }\n");
      out.print("    return obj;\n");
      out.print("  }\n");
      out.print("\n");
      out.print("  static private Object deserializeWithXtream(File e)\n");
      out.print(
          "      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {\n");
      out.print("    XStream xtream = new XStream();\n");
      out.print("    HashMap<String, String> myConverters = new HashMap<String, String>();\n");
      out.print("    Object obj = null;\n");
      out.print("    try {\n");
      out.print("      obj = xtream.fromXML(e);\n");
      out.print("    }catch (Exception exception){\n");
      out.print("      exception.printStackTrace();\n");
      out.print("    }");
      out.print("    if (obj == null) {\n");
      out.print("      for (Map.Entry<String, String> converter : myConverters.entrySet()) {\n");
      out.print("        int i = 0;\n");
      out.print("        while (obj == null && i < myConverters.size()) {\n");
      out.print("          xtream.registerConverter(\n");
      out.print("              (Converter) Class.forName(converter.getValue()).getConstructor().newInstance());\n");
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
      out.print("  }\n");
      out.print("}");
      out.close();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean deleteThisSupporterClassFile() {
    return new File(this.classFilePath).delete();
  }
}
