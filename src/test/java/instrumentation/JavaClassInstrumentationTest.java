package instrumentation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import org.file.FileFinderSupport;
import org.file.ObjectSerializerSupporter;
import org.instrumentation.JavaClassIntrumentation;
import org.instrumentation.PomFileInstrumentation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class JavaClassInstrumentationTest {

  @Test
  public void expectTrueForInstrumentationOnDeclaredMethod() throws IOException {
    FileFinderSupport fileFinderSupport = new FileFinderSupport("src/test/resources/validProject");
    Assert.assertTrue(fileFinderSupport.createNewDirectory(fileFinderSupport.findFile("Person.java", fileFinderSupport.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    File pomDirectory = fileFinderSupport.findFile("Person.java", fileFinderSupport.getProjectLocalPath());
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
        pomDirectory.getPath());

    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(fileFinderSupport.getProjectLocalPath().getPath()+File.separator+"src"+
            File.separator+"main"+File.separator+"java").relativize(Paths.get(fileFinderSupport.
            getTargetClassLocalPath().getPath())).toString().replace(File.separator,"."));
    objectSerializerSupporter.getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
        fileFinderSupport.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

    JavaClassIntrumentation javaClassIntrumentation = new JavaClassIntrumentation("getFullName", objectSerializerSupporter.getFullSerializerSupporterClass());
    Assert.assertTrue(javaClassIntrumentation.runTransformation(new File(fileFinderSupport.getTargetClassLocalPath()+File.separator+"Person.java")));
    Assert.assertTrue(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/Person.java"))).contains("serializeWithXtreamOut(this);"));
    Assert.assertTrue(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/Person.java"))).contains("import org.ObjectSerializerSupporter;"));
    Assert.assertTrue(javaClassIntrumentation.undoTransformations(new File(fileFinderSupport.getTargetClassLocalPath()+File.separator+"Person.java")));
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());
  }

  @Test
  public void expectFalseForInstrumentationOnUndeclaredMethod() throws IOException {
    FileFinderSupport fileFinderSupport = new FileFinderSupport("src/test/resources/validProject");
    Assert.assertTrue(fileFinderSupport.createNewDirectory(fileFinderSupport.findFile("PersonTwo.java", fileFinderSupport.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    File pomDirectory = fileFinderSupport.findFile("PersonTwo.java", fileFinderSupport.getProjectLocalPath());
    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
        pomDirectory.getPath());

    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(fileFinderSupport.getProjectLocalPath().getPath()+File.separator+"src"+
            File.separator+"main"+File.separator+"java").relativize(Paths.get(fileFinderSupport.
            getTargetClassLocalPath().getPath())).toString().replace(File.separator,"."));
    objectSerializerSupporter.getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
        fileFinderSupport.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

    JavaClassIntrumentation javaClassIntrumentation = new JavaClassIntrumentation("getNoName", objectSerializerSupporter.getFullSerializerSupporterClass());
    javaClassIntrumentation.runTransformation(new File(fileFinderSupport.getTargetClassLocalPath()+File.separator+"PersonTwo.java"));
    Assert.assertFalse(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/PersonTwo.java"))).contains("serializeWithXtreamOut(this);"));
    javaClassIntrumentation.undoTransformations(new File(fileFinderSupport.getTargetClassLocalPath()+File.separator+"PersonTwo.java"));
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());
  }

  @After
  public void removeResourceDirectory(){
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").delete();
  }

  //@After
  public void removeObjectSerializerSupporter(){
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/ObjectSerializerSupporter.java").delete();
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/ObjectSerializerSupporter.java").delete();
  }

  @After
  public void undoChangesOnFileWithInstrumentation() throws IOException {
    String assistant = "public class PersonTwo {\n"
        + "  private String name;\n"
        + "  private String surname;\n"
        + "  \n"
        + "  public PersonTwo(String name,  String surname){\n"
        + "    this.name=name;\n"
        + "    this.surname=surname;\n"
        + "  }\n"
        + "  \n"
        + "  public String getName(){\n"
        + "    return this.name;\n"
        + "  }\n"
        + "  public String getFullName(){\n"
        + "    return name + \" \" + surname;\n"
        + "  }\n"
        + "}\n";
    Files.write( Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/PersonTwo.java"),
        Collections.singleton(assistant), StandardCharsets.UTF_8);
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/PersonTwo.java");
  }
}
