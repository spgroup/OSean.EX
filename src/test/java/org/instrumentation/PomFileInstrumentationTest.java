package org.instrumentation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.transform.TransformerException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class PomFileInstrumentationTest {

  @After
  public void undoChangesOnPomFile() throws IOException {
    String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "</project>";
    updatePomFile(str);

  }

  private void updatePomFile(String str) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File("src/test/resources/project/pom.xml").getPath()));
    writer.write(str);

    writer.close();
  }

  @Test
  public void expectTrueForAddingAllDependenciesOnPomFile() throws TransformerException {
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
  }

  @Test
  public void expectTrueForAddingChangesOnPom() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
    Assert.assertTrue(pomFileInstrumentation.addResourcesForGeneratedJar());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForReplacingPluginOnPom() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + " <build>\n"
        + "   <plugins>\n"
        + "       <plugin>\n"
        + "\t\t\t\t<artifactId>maven-assembly-plugin</artifactId>\n"
        + "\t\t\t\t<version>2.3</version>\n"
        + "\t\t\t\t<configuration>\n"
        + "\t\t\t\t\t<appendAssemblyId>false</appendAssemblyId>\n"
        + "\t\t\t\t\t<outputDirectory>${project.build.directory}/releases/</outputDirectory>\n"
        + "\t\t\t\t\t<descriptors>\n"
        + "\t\t\t\t\t\t<descriptor>${basedir}/src/main/assemblies/plugin.xml</descriptor>\n"
        + "\t\t\t\t\t</descriptors>\n"
        + "\t\t\t\t</configuration>\n"
        + "\t\t\t\t<executions>\n"
        + "\t\t\t\t\t<execution>\n"
        + "\t\t\t\t\t\t<phase>package</phase>\n"
        + "\t\t\t\t\t\t<goals>\n"
        + "\t\t\t\t\t\t\t<goal>single</goal>\n"
        + "\t\t\t\t\t\t</goals>\n"
        + "\t\t\t\t\t</execution>\n"
        + "\t\t\t\t</executions>\n"
        + "\t\t\t</plugin>\n"
        + "<plugin>\n"
        + "\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n"
        + "\t\t\t\t<artifactId>maven-gpg-plugin</artifactId>\n"
        + "\t\t\t\t<version>1.4</version>\n"
        + "\t\t\t\t<executions>\n"
        + "\t\t\t\t\t<execution>\n"
        + "\t\t\t\t\t\t<id>sign-artifacts</id>\n"
        + "\t\t\t\t\t\t<phase>verify</phase>\n"
        + "\t\t\t\t\t\t<goals>\n"
        + "\t\t\t\t\t\t\t<goal>sign</goal>\n"
        + "\t\t\t\t\t\t</goals>\n"
        + "\t\t\t\t\t</execution>\n"
        + "\t\t\t\t</executions>\n"
        + "\t\t\t</plugin>"
        + "   </plugins>\n"
        + " </build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForAddingChangesOnPomTwo() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "  <dependencies>"
        + "     <dependency>\n"
        + "          <groupId>org.apache.commons</groupId>\n"
        + "          <artifactId>commons-compress</artifactId>\n"
        + "          <version>1.0</version>\n"
        + "     </dependency>"
        + "     <dependency>"
        + "          <groupId>com.thoughtworks.xstream</groupId>"
        + "          <artifactId>xstream</artifactId>"
        + "          <version>1.4.15</version>"
        + "     </dependency>"
        + "  </dependencies>"
        + "\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
  }

  @Test
  public void expectFalseForChangesOnPomCausedByBadlyPom() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<dependencies>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertFalse(pomFileInstrumentation.addRequiredDependenciesOnPOM());
    Assert.assertFalse(pomFileInstrumentation.addResourcesForGeneratedJar());
    Assert.assertFalse(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForAddingAllDependenciesWithDependencyTagOn() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<dependencies>\n"
        + "</dependencies>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
  }

  @Test
  public void expectFalseForAddingAllDependenciesOnPomFile() throws IOException, TransformerException {

    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<dependencies>\n"
        + "    <dependency>\n"
        + "      <groupId>org.apache.commons</groupId>\n"
        + "      <artifactId>commons-lang3</artifactId>\n"
        + "      <version>3.0</version>\n"
        + "    </dependency>\n"
        + "    <dependency>\n"
        + "      <groupId>com.thoughtworks.xstream</groupId>\n"
        + "      <artifactId>xstream</artifactId>\n"
        + "      <version>1.4.13</version>\n"
        + "    </dependency>"
        + "</dependencies>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
  }

  @Test
  public void expectFalseForAddingXstreamDependency() throws IOException, TransformerException {

    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<dependencies>\n"
        + "    <dependency>\n"
        + "      <groupId>org.apache.commons</groupId>\n"
        + "      <artifactId>commons-lang3</artifactId>\n"
        + "      <version>3.0</version>\n"
        + "    </dependency>\n"
        + "</dependencies>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
  }

  @Test
  public void expectTrueForAddingCommonsLangDependency() throws IOException, TransformerException {

    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<dependencies>\n"
        + "    <dependency>\n"
        + "      <groupId>com.thoughtworks.xstream</groupId>\n"
        + "      <artifactId>xstream</artifactId>\n"
        + "      <version>1.4.13</version>\n"
        + "    </dependency>\n"
        + "</dependencies>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addRequiredDependenciesOnPOM());
  }

  @Test
  public void expectTrueForAddingPluginForJarGeneration() throws TransformerException {
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForAddingPluginForJarGenerationWithBuildTagOn() throws IOException, TransformerException {
    updatePomFile(getPomWithBuildTagOn());

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForAddingPluginForJarGenerationWithBuildAndPluginTagsOn() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "   <plugins>\n"
        + "   </plugins>\n"
        + "</build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForAddingPluginForJarGenerationWithSameArtifactID() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "   <plugins>\n"
        + "       <plugin>\n"
        + "        <artifactId>maven-assembly-plugin</artifactId>\n"
        + "        <version>2.2</version>\n"
        + "        <executions>\n"
        + "          <execution>\n"
        + "            <id>generate-distribution</id>\n"
        + "            <phase>package</phase>\n"
        + "            <goals>\n"
        + "              <goal>single</goal>\n"
        + "            </goals>\n"
        + "          </execution>\n"
        + "        </executions>\n"
        + "        <configuration>\n"
        + "          <descriptors>\n"
        + "            <descriptor>${basedir}/src/assembly/default.xml</descriptor>\n"
        + "          </descriptors>\n"
        + "          <attach>${attach-distribution}</attach>\n"
        + "          <appendAssemblyId>true</appendAssemblyId>\n"
        + "          <tarLongFileMode>gnu</tarLongFileMode>\n"
        + "        </configuration>\n"
        + "      </plugin>\n"
        + "   </plugins>\n"
        + "</build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectFalseForAddingPluginForJarGeneration() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "   <plugins>\n"
        + "            <plugin>\n"
        + "                <artifactId>maven-assembly-plugin</artifactId>\n"
        + "                <version>2.6</version>\n"
        + "                <configuration>\n"
        + "                    <archive>\n"
        + "                        <manifest>\n"
        + "                            <mainClass>fully.qualified.MainClass</mainClass>\n"
        + "                        </manifest>\n"
        + "                    </archive>\n"
        + "                    <descriptorRefs>\n"
        + "                        <descriptorRef>jar-with-dependencies</descriptorRef>\n"
        + "                    </descriptorRefs>\n"
        + "                </configuration>\n"
        + "            </plugin>"
        + "   </plugins>\n"
        + "</build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addPluginForJarWithAllDependencies());
  }

  @Test
  public void expectTrueForAddingsResourcesForJarGeneration() throws TransformerException {
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addResourcesForGeneratedJar());
  }

  @Test
  public void expectTrueForAddingsResourcesForJarGenerationWithBuildTagOn() throws IOException, TransformerException {
    updatePomFile(getPomWithBuildTagOn());

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addResourcesForGeneratedJar());
  }

  @Test
  public void expectTrueForAddingsResourcesForJarGenerationWithBuildAndResourceTagsOn() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "   <resources>\n"
        + "   </resources>\n"
        + "</build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.addResourcesForGeneratedJar());
  }

  @Test
  public void expectFalseForAddingsResourcesForJarGeneration() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "<resources><resource><directory>src/main/resources</directory><includes><include>**/*.xml</include></includes></resource></resources>"
        + "</build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertFalse(pomFileInstrumentation.addResourcesForGeneratedJar());
  }

  @Test
  public void expectTrueForInstrumentationOnDeclaredMethod() throws TransformerException, IOException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>"
        + "\n"
        + "  <build>\n"
        + "    <plugins>\n"
        + "      <plugin>\n"
        + "        <!-- this plugin allows us to ensure Java 5 API compatibility -->\n"
        + "        <groupId>org.codehaus.mojo</groupId>\n"
        + "        <artifactId>animal-sniffer-maven-plugin</artifactId>\n"
        + "        <version>1.9</version>\n"
        + "        <executions>\n"
        + "          <execution>\n"
        + "            <id>animal-sniffer</id>\n"
        + "            <phase>compile</phase>\n"
        + "            <goals>\n"
        + "              <goal>check</goal>\n"
        + "            </goals>\n"
        + "            <configuration>\n"
        + "              <signature>\n"
        + "                <groupId>org.codehaus.mojo.signature</groupId>\n"
        + "                <artifactId>java15</artifactId>\n"
        + "                <version>1.0</version>\n"
        + "              </signature>\n"
        + "            </configuration>\n"
        + "          </execution>\n"
        + "        </executions>\n"
        + "      </plugin>\n"
        + "    </plugins>\n"
        + "  </build>\n"
        + "\n"
        + "</project>");
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    Assert.assertTrue(pomFileInstrumentation.changeAnimalSnifferPluginIfAdded());
  }
  
  @Test
  public void expectTrueForUpdateOldRepository() throws IOException, TransformerException {
    updatePomFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "<repositories>\n"
        + "  <repository>\n"
        + "    <id>osgeo</id>\n"
        + "    <name>OSGeo Release Repository</name>\n"
        + "    <url>http://download.osgeo.org/webdav/geotools/</url>\n"
        + "  </repository>\n"
        + "</repositories>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "<resources><resource><directory>src/main/resources</directory><includes><include>**/*.xml</include></includes></resource></resources>"
        + "</build>\n"
        + "</project>");

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(new File("src/test/resources/project").getPath());
    pomFileInstrumentation.updateOldRepository();
    String pomContent = new String(Files.readAllBytes(pomFileInstrumentation.getPomFile().toPath()));
    Assert.assertTrue(pomContent.contains("https://repo.osgeo.org/repository/release/"));
  }


  public String getPomWithBuildTagOn(){
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "\n"
        + "  <groupId>projectId</groupId>\n"
        + "  <artifactId>project</artifactId>\n"
        + "  <version>1.0</version>\n"
        + "\n"
        + "  <properties>\n"
        + "    <maven.compiler.source>8</maven.compiler.source>\n"
        + "    <maven.compiler.target>8</maven.compiler.target>\n"
        + "  </properties>\n"
        + "\n"
        + "<build>\n"
        + "</build>\n"
        + "</project>";
  }

}
