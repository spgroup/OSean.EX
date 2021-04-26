package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.file.FileFinderSupport;
import org.file.ObjectSerializerSupporter;
import org.instrumentation.PomFileInstrumentation;
import org.junit.Assert;
import org.junit.Test;

public class ObjectSerializerSupporterTest {

  @Test
  public void expectAddedObjectSerializerSupporterForClassOnRoot() throws IOException {
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

    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/ObjectSerializerSupporter.java").exists());
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());
    fileFinderSupport.deleteResourceDirectory();
  }

  @Test
  public void expectAddedObjectSerializerSupporterForClassNotOnRoot() throws IOException {
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

    Assert.assertTrue(new File(System.getProperty("user.dir")+File.separator+"src/test/resources/validProject/src/main/java/org/ObjectSerializerSupporter.java").exists());
    objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());
    fileFinderSupport.deleteResourceDirectory();
  }

}
