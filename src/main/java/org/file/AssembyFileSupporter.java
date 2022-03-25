package org.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

public class AssembyFileSupporter extends ResourceFileSupporter{

  public AssembyFileSupporter(String pathFile){
    super(pathFile);
  }

  @Override
  public String getResourceDirectoryPath(File localPomDirectory){
    return localPomDirectory+File.separator+"src"+File.separator+"main"+File.separator+"assembly";
  }

  @Override
  public boolean createNewDirectory(File localPomDirectory){
    File resourceDirectory = new File(getResourceDirectoryPath(localPomDirectory));
    try {
      FileUtils.deleteDirectory(resourceDirectory);
      Files.createDirectories(resourceDirectory.toPath());
      this.localPathResourceDirectory = resourceDirectory;
      createAssemblyFile(this.localPathResourceDirectory.getPath());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean createAssemblyFile(String fileDirectory){
    try(FileWriter fw = new FileWriter(fileDirectory+File.separator+"assembly.xml", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw))
    {
      out.println(getAssemblyContent());
      out.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private String getAssemblyContent(){
    return "<assembly\n"
        + "  xmlns=\"http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.3\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.3\n"
        + "  http://maven.apache.org/xsd/assembly-1.1.3.xsd\">\n"
        + "  <id>jar-with-dependencies</id>\n"
        + "  <formats>\n"
        + "    <format>jar</format>\n"
        + "  </formats>\n"
        + "  <includeBaseDirectory>false</includeBaseDirectory>\n"
        + "  <dependencySets>\n"
        + "    <dependencySet>\n"
        + "      <outputDirectory>/</outputDirectory>\n"
        + "      <useProjectArtifact>true</useProjectArtifact>\n"
        + "      <unpack>true</unpack>\n"
        + "      <scope>test</scope>\n"
        + "    </dependencySet>\n"
        + "  </dependencySets>\n"
        + "  <fileSets>\n"
        + "    <fileSet>\n"
        + "      <directory>${project.build.directory}/test-classes</directory>\n"
        + "      <outputDirectory>/</outputDirectory>\n"
        + "      <includes>\n"
        + "        <include>**/*.class</include>\n"
        + "      </includes>\n"
        + "      <useDefaultExcludes>true</useDefaultExcludes>\n"
        + "    </fileSet>\n"
        + "  </fileSets>\n"
        + "</assembly>";
  }

}
