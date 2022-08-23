package org.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.instrumentation.PomFileInstrumentation;
import org.junit.Assert;
import org.junit.Test;

public class ObjectSerializerSupporterTest {

  @Test
  public void expectAddedObjectSerializerSupporterForClassOnRoot() throws IOException {
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

    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/ObjectSerializerSupporter.java").exists());
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(
        resourceFileSupporter.getTargetClassLocalPath().getPath());
    resourceFileSupporter.deleteResourceDirectory();
  }

  @Test
  public void expectAddedObjectSerializerSupporterForClassNotOnRoot() throws IOException {
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

    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/ObjectSerializerSupporter.java").exists());
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(
        resourceFileSupporter.getTargetClassLocalPath().getPath());
    resourceFileSupporter.deleteResourceDirectory();
  }

  @Test
  public void buildSerializerSupporterWithoutPathRelativaze() {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/validProject");
    resourceFileSupporter.findTargetClassLocalPath("Person.java", resourceFileSupporter.getProjectLocalPath());
    File pomDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
        pomDirectory.getPath());

    ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
        Paths.get(resourceFileSupporter.getTargetClassLocalPath().getPath()).toString().replace(File.separator,"."));
    objectSerializerSupporter.getOutputClass(resourceFileSupporter.getTargetClassLocalPath().getPath(),
        resourceFileSupporter.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));
    Assert.assertTrue(objectSerializerSupporter.getClassPackage().equals("org"));
    
    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/ObjectSerializerSupporter.java").exists());
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(
        resourceFileSupporter.getTargetClassLocalPath().getPath());
    resourceFileSupporter.deleteResourceDirectory();
  }

}
