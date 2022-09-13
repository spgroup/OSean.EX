package org.transformations.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileDeleteStrategy;

public class SerializedObjectAccessOutputClass {
  protected Path outputClassPath;

  public boolean writeClassFile(List<String> serializedObjectMethods, String targerClassLocalPath, String packageName) {
    try (FileWriter fw = new FileWriter(targerClassLocalPath + File.separator + "SerializedObjectSupporter.java");
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
      out.print("package " + packageName + ";\n\n");
      out.print("import com.thoughtworks.xstream.XStream;\n");
      out.print("import java.io.ByteArrayOutputStream;\n");
      out.print("import java.io.IOException;\n");
      out.print("import java.io.File;\n");
      out.print("import java.io.InputStream;\n");
      out.print("\npublic class SerializedObjectSupporter {\n");
      out.print("\n\tprivate String readFileContents(InputStream file) throws IOException {\n");
      out.print("\t\tByteArrayOutputStream result = new ByteArrayOutputStream();\n");
      out.print("\t\tbyte[] buffer = new byte[1024];\n");
      out.print("\t\tfor (int length; (length = file.read(buffer)) != -1; ) {\n");
      out.print("\t\t\tresult.write(buffer, 0, length);\n");
      out.print("\t\t}\n");
      out.print("\t\treturn String.valueOf(result);\n");
      out.print("\t}");
      out.print("\n");
      out.print("\n\tprivate Object deserializeAny(String object) {\n");
      out.print("\t\tObject obj = null;\n");
      out.print("\t\ttry{\n");
      out.print("\t\t\tobj = deserializeWithXtreamString(object);\n");
      out.print("\t\t}catch (Exception e){\n");
      out.print("\t\t\te.printStackTrace();\n");
      out.print("\t\t}\n");
      out.print("\t\treturn obj;\n");
      out.print("\t}");
      out.print("\n");
      out.print("\n\tprivate Object deserializeWithXtreamString(String e) {\n");
      out.print("\t\tXStream xtream = new XStream();\n");
      out.print("\t\txtream.ignoreUnknownElements();\n");
      out.print("\t\tObject obj = xtream.fromXML(e);\n");
      out.print("\t\treturn obj;\n");
      out.print("\t}");
      out.print("\n");
      serializedObjectMethods.add("}");
      String methods = String.join("\n", serializedObjectMethods);
      out.println(methods);
      this.outputClassPath = new File(targerClassLocalPath + File.separator + "SerializedObjectSupporter.java")
          .toPath();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean deleteOldClassSupporter() throws IOException {
    if (this.outputClassPath != null && this.outputClassPath.toFile().exists()) {
      FileDeleteStrategy.FORCE.delete(this.outputClassPath.toFile());
      return true;
    }
    return false;
  }

}
