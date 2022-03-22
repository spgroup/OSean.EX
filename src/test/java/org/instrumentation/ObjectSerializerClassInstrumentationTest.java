package org.instrumentation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.file.ResourceFileSupporter;
import org.file.ObjectSerializerSupporter;
import org.junit.Assert;
import org.junit.Test;

public class ObjectSerializerClassInstrumentationTest {

  @Test
  public void expectTrueForInstrumentationOnDeclaredMethod() throws IOException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/validProject");
    resourceFileSupporter.findTargetClassLocalPath("Person.java", resourceFileSupporter.getProjectLocalPath());
    File pomDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
        pomDirectory.getPath());

    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(resourceFileSupporter.getProjectLocalPath().getPath()+File.separator+"src"+
            File.separator+"main"+File.separator+"java").relativize(Paths.get(resourceFileSupporter.
            getTargetClassLocalPath().getPath())).toString().replace(File.separator,"."));
    objectSerializerSupporter.getOutputClass(resourceFileSupporter.getTargetClassLocalPath().getPath(),
        resourceFileSupporter.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

    ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = new ObjectSerializerClassIntrumentation("getFullName", objectSerializerSupporter.getFullSerializerSupporterClass());
    Assert.assertTrue(objectSerializerClassIntrumentation
        .runTransformation(new File(resourceFileSupporter.getTargetClassLocalPath()+File.separator+"Person.java")));
    Assert.assertTrue(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/Person.java"))).contains("serializeWithXtreamOut(this);"));
    Assert.assertTrue(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/Person.java"))).contains("import org.ObjectSerializerSupporter;"));
    Assert.assertTrue(objectSerializerClassIntrumentation
        .undoTransformations(new File(
            resourceFileSupporter.getTargetClassLocalPath()+File.separator+"Person.java")));
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(
        resourceFileSupporter.getTargetClassLocalPath().getPath());
    resourceFileSupporter.deleteResourceDirectory();
  }

  @Test
  public void expectFalseForInstrumentationOnUndeclaredMethod() throws IOException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/validProject");
    resourceFileSupporter.findTargetClassLocalPath("PersonTwo.java", resourceFileSupporter.getProjectLocalPath());
    File pomDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
        pomDirectory.getPath());

    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(resourceFileSupporter.getProjectLocalPath().getPath()+File.separator+"src"+
            File.separator+"main"+File.separator+"java").relativize(Paths.get(resourceFileSupporter.
            getTargetClassLocalPath().getPath())).toString().replace(File.separator,"."));
    objectSerializerSupporter.getOutputClass(resourceFileSupporter.getTargetClassLocalPath().getPath(),
        resourceFileSupporter.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

    ObjectSerializerClassIntrumentation objectSerializerClassIntrumentation = new ObjectSerializerClassIntrumentation("getNoName", objectSerializerSupporter.getFullSerializerSupporterClass());
    objectSerializerClassIntrumentation
        .runTransformation(new File(resourceFileSupporter.getTargetClassLocalPath()+File.separator+"PersonTwo.java"));
    Assert.assertFalse(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/PersonTwo.java"))).contains("serializeWithXtreamOut(this);"));
    objectSerializerClassIntrumentation
        .undoTransformations(new File(
            resourceFileSupporter.getTargetClassLocalPath()+File.separator+"PersonTwo.java"));
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(
        resourceFileSupporter.getTargetClassLocalPath().getPath());
    resourceFileSupporter.deleteResourceDirectory();
  }

}
