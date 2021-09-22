package file;

import java.io.File;
import javax.xml.transform.TransformerException;
import org.file.FileFinderSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class FileFinderSupportTest {

  @Test
  public void expectNullForProjectWithoutPomFile() throws TransformerException {
    FileFinderSupport fileFinderSupport = new FileFinderSupport("src/test/resources/project-no-pom");
    Assert.assertNull(fileFinderSupport.findFile("Assistant.java", fileFinderSupport.getProjectLocalPath()));
  }

  @Test
  public void expectNotNullForProjectWithPomFile() throws TransformerException {
    FileFinderSupport fileFinderSupport = new FileFinderSupport("src/test/resources/project");
    Assert.assertNotNull(fileFinderSupport.findFile("Assistant.java", fileFinderSupport.getProjectLocalPath()));
  }

  @Test
  public void expectCreateResourceDirectoryForProjectWithPomFile() throws TransformerException {
    FileFinderSupport fileFinderSupport = new FileFinderSupport("src/test/resources/project");
    Assert.assertTrue(fileFinderSupport.createNewDirectory(fileFinderSupport.findFile("Assistant.java", fileFinderSupport.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources/").exists());
  }

  @After
  public void removeResourceDirectory(){
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources/serializedObjects").delete();
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources").delete();
  }

}
