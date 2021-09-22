package file;

import java.io.File;
import javax.xml.transform.TransformerException;
import org.file.ResourceFileSupporter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ResourceFileSupporterTest {

  @Test
  public void expectNullForProjectWithoutPomFile() throws TransformerException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/project-no-pom");
    Assert.assertNull(resourceFileSupporter.findFile("Assistant.java", resourceFileSupporter.getProjectLocalPath()));
  }

  @Test
  public void expectNotNullForProjectWithPomFile() throws TransformerException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/project");
    Assert.assertNotNull(resourceFileSupporter.findFile("Assistant.java", resourceFileSupporter.getProjectLocalPath()));
  }

  @Test
  public void expectCreateResourceDirectoryForProjectWithPomFile() throws TransformerException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/project");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(
        resourceFileSupporter.findFile("Assistant.java", resourceFileSupporter.getProjectLocalPath())));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources/").exists());
  }

  @After
  public void removeResourceDirectory(){
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources/serializedObjects").delete();
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources").delete();
  }

}
