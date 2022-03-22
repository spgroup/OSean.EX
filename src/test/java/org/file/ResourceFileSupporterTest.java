package org.file;

import java.io.File;
import javax.xml.transform.TransformerException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ResourceFileSupporterTest {

  @Test
  public void expectNullForProjectWithoutPomFile() throws TransformerException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/project-no-pom");
    File pomDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
    Assert.assertNull(pomDirectory);
  }

  @Test
  public void expectNotNullForProjectWithPomFile() throws TransformerException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/project");
    File pomDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
    Assert.assertNotNull(pomDirectory);
  }

  @Test
  public void expectCreateResourceDirectoryForProjectWithPomFile() throws TransformerException {
    ResourceFileSupporter resourceFileSupporter = new ResourceFileSupporter("src/test/resources/project");
    File pomDirectory = resourceFileSupporter.findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
    Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
    Assert.assertEquals(true, new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources/").exists());
  }

  @After
  public void removeResourceDirectory(){
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources/serializedObjects").delete();
    new File(System.getProperty("user.dir")+File.separator+"src/test/resources/project/src/main/resources").delete();
  }

}
