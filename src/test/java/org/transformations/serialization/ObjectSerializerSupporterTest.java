package org.transformations.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.file.ProjectFileSupporter;
import org.junit.Assert;
import org.junit.Test;
import org.transformations.instrumentation.PomFileInstrumentation;

public class ObjectSerializerSupporterTest {

    @Test
    public void expectAddedObjectSerializerSupporterForClassOnRoot() throws IOException {
        ProjectFileSupporter resourceFileSupporter = new ProjectFileSupporter("src/test/resources/validProject");
        resourceFileSupporter.findTargetClassLocalPath("PersonTwo.java");
        File pomDirectory = resourceFileSupporter
                .findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
        Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
        Assert.assertEquals(true, new File(
                System.getProperty("user.dir") + File.separator + "src/test/resources/validProject/src/main/resources")
                .exists());

        PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
                pomDirectory.getPath());

        ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
                Paths.get(resourceFileSupporter.getProjectLocalPath().getPath() + File.separator + "src" +
                        File.separator + "main" + File.separator + "java")
                        .relativize(Paths.get(resourceFileSupporter.getTargetClassLocalPath().getPath())).toString()
                        .replace(File.separator, "."),
                resourceFileSupporter.getTargetClassLocalPath().getPath());
        objectSerializerSupporter.writeClassFile(resourceFileSupporter.getTargetClassLocalPath().getPath(),
                resourceFileSupporter.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        Assert.assertTrue(new File(System.getProperty("user.dir") + File.separator
                + "src/test/resources/validProject/src/main/java/ObjectSerializerSupporter.java").exists());
        objectSerializerSupporter.deleteThisSupporterClassFile();
        resourceFileSupporter.deleteResourceDirectory();
    }

    @Test
    public void expectAddedObjectSerializerSupporterForClassNotOnRoot() throws IOException {
        ProjectFileSupporter resourceFileSupporter = new ProjectFileSupporter("src/test/resources/validProject");
        resourceFileSupporter.findTargetClassLocalPath("Person.java");
        File pomDirectory = resourceFileSupporter
                .findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
        Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
        Assert.assertEquals(true, new File(
                System.getProperty("user.dir") + File.separator + "src/test/resources/validProject/src/main/resources")
                .exists());

        PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
                pomDirectory.getPath());

        ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
                Paths.get(resourceFileSupporter.getProjectLocalPath().getPath() + File.separator + "src" +
                        File.separator + "main" + File.separator + "java")
                        .relativize(Paths.get(resourceFileSupporter.getTargetClassLocalPath().getPath())).toString()
                        .replace(File.separator, "."),
                resourceFileSupporter.getTargetClassLocalPath().getPath());
        objectSerializerSupporter.writeClassFile(resourceFileSupporter.getTargetClassLocalPath().getPath(),
                resourceFileSupporter.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        Assert.assertTrue(new File(System.getProperty("user.dir") + File.separator
                + "src/test/resources/validProject/src/main/java/org/ObjectSerializerSupporter.java").exists());
        objectSerializerSupporter.deleteThisSupporterClassFile();
        resourceFileSupporter.deleteResourceDirectory();
    }

    @Test
    public void buildSerializerSupporterWithoutPathRelativaze() {
        ProjectFileSupporter resourceFileSupporter = new ProjectFileSupporter("src/test/resources/validProject");
        resourceFileSupporter.findTargetClassLocalPath("Person.java");
        File pomDirectory = resourceFileSupporter
                .findBuildFileDirectory(resourceFileSupporter.getTargetClassLocalPath(), "pom.xml");
        Assert.assertTrue(resourceFileSupporter.createNewDirectory(pomDirectory));
        Assert.assertEquals(true, new File(
                System.getProperty("user.dir") + File.separator + "src/test/resources/validProject/src/main/resources")
                .exists());

        PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
                pomDirectory.getPath());

        ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
                Paths.get(resourceFileSupporter.getTargetClassLocalPath().getPath()).toString().replace(File.separator,
                        "."),
                resourceFileSupporter.getTargetClassLocalPath().getPath());
        objectSerializerSupporter.writeClassFile(resourceFileSupporter.getTargetClassLocalPath().getPath(),
                resourceFileSupporter.getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));
        Assert.assertTrue(objectSerializerSupporter.getClassPackage().equals("org"));

        Assert.assertTrue(new File(System.getProperty("user.dir") + File.separator
                + "src/test/resources/validProject/src/main/java/org/ObjectSerializerSupporter.java").exists());
        objectSerializerSupporter.deleteThisSupporterClassFile();
        resourceFileSupporter.deleteResourceDirectory();
    }

}
