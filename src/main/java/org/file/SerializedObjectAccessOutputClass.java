package org.file;

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

  public boolean getOutputClass(List<String> serializedObjectMethods, String pomFileDirectory, String packageName){
    String classText = ""
        + "package "+packageName+";\n\n"
        + "import com.thoughtworks.xstream.XStream;\n"
        + "import java.io.ByteArrayOutputStream;\n"
        + "import java.io.IOException;\n"
        + "import java.io.File;\n"
        + "import java.io.InputStream;\n"
        + "\npublic class SerializedObjectSupporter {\n"
        + "\n\tprivate String readFileContents(InputStream file) throws IOException {\n"
        + "\t\tByteArrayOutputStream result = new ByteArrayOutputStream();\n"
        + "\t\tbyte[] buffer = new byte[1024];\n"
        + "\t\tfor (int length; (length = file.read(buffer)) != -1; ) {\n"
        + "\t\t\tresult.write(buffer, 0, length);\n"
        + "\t\t}\n"
        + "\t\treturn String.valueOf(result);\n"
        + "\t}"
        + "\n"
        + "\n\tprivate Object deserializeAny(String object) {\n"
        + "\t\tObject obj = null;\n"
        + "\t\ttry{\n"
        + "\t\t\tobj = deserializeWithXtreamString(object);\n"
        + "\t\t}catch (Exception e){\n"
        + "\t\t\te.printStackTrace();\n"
        + "\t\t}\n"
        + "\t\treturn obj;\n"
        + "\t}"
        + "\n"
        + "\n\tprivate Object deserializeWithXtreamString(String e) {\n"
        + "\t\tXStream xtream = new XStream();\n"
        + "\t\txtream.ignoreUnknownElements();\n"
        + "\t\tObject obj = xtream.fromXML(e);\n"
        + "\t\treturn obj;\n"
        + "\t}"
        + "\n";
    try {
      String classTextFinal = "\n}";
      saveFile(pomFileDirectory, classText, "SerializedObjectSupporter");
      for (String oneMethod : serializedObjectMethods) {
        saveFile(pomFileDirectory, oneMethod, "SerializedObjectSupporter");
      }
      saveFile(pomFileDirectory, classTextFinal, "SerializedObjectSupporter");
      this.outputClassPath = new File(pomFileDirectory+File.separator+"SerializedObjectSupporter.java").toPath();
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public boolean saveFile(String fileDirectory, String contents, String fileName) {
    try(FileWriter fw = new FileWriter(fileDirectory+File.separator+fileName+".java", true);
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

  public boolean deleteOldClassSupporter() throws IOException {
    if (this.outputClassPath != null && this.outputClassPath.toFile().exists()){
      FileDeleteStrategy.FORCE.delete(this.outputClassPath.toFile());
      return true;
    }
    return false;
  }

}
