package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.file.ResourceFileSupporter;
import org.file.ObjectSerializerSupporter;
import org.instrumentation.PomFileInstrumentation;
import org.junit.Assert;
import org.junit.Test;

public class ObjectSerializerSupporterTest {

  @Test
  public void expectAddedObjectSerializerSupporterForClassOnRoot() throws IOException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/validProject");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(
        resourceFileSupporter.findFile("PersonTwo.java", resourceFileSupporter.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    File pomDirectory = resourceFileSupporter.findFile("PersonTwo.java", resourceFileSupporter.getProjectLocalPath());
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
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(
        resourceFileSupporter.findFile("Person.java", resourceFileSupporter.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    File pomDirectory = resourceFileSupporter.findFile("Person.java", resourceFileSupporter.getProjectLocalPath());
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
  //Construtor do serializer supporter s/ relativizar caminho
  public void teste01() {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/validProject");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(
        resourceFileSupporter.findFile("Person.java", resourceFileSupporter.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/resources").exists());

    File pomDirectory = resourceFileSupporter.findFile("Person.java", resourceFileSupporter.getProjectLocalPath());
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
