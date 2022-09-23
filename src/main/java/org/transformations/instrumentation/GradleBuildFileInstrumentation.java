package org.transformations.instrumentation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GradleBuildFileInstrumentation {
    private File buildFileDirectory;
    private File buildFile;
    private ArrayList<String> fileLines;

    public GradleBuildFileInstrumentation(String pathGradleBuild) {
        this.buildFileDirectory = new File(pathGradleBuild);
        this.buildFile = new File(pathGradleBuild + File.separator + "build.gradle");
        this.fileLines = new ArrayList<>();
        readBuildFile();
    }

    public File getBuildFile() {
        return this.buildFile;
    }

    public File getBuildFileDirectory() {
        return this.buildFileDirectory;
    }

    private void readBuildFile() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(this.buildFile));
            br.lines().forEach(ln -> this.fileLines.add(ln));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFatJarPlugin() {
        int idx = this.fileLines.indexOf("task fatJar(type: Jar) {") + 1;

        if (idx > 0) {
            int aux = 1;
            while (aux > 0) {
                if (this.fileLines.get(idx).contains("{"))
                    aux++;
                if (this.fileLines.get(idx).contains("}"))
                    aux--;

                this.fileLines.remove(idx);
            }
            this.fileLines.remove(--idx);
        }

        this.fileLines.add("task fatJar(type: Jar) {");
        this.fileLines.add(" baseName = project.name + '-jar-with-dependencies'");
        this.fileLines.add(" from { (configurations.runtime).collect { it.isDirectory() ? it : zipTree(it) } }");
        this.fileLines.add(" from { (configurations.testRuntime).collect { it.isDirectory() ? it : zipTree(it) } }");
        this.fileLines.add(" with jar");
        this.fileLines.add("}");
    }

    public void addTestJarPlugin() {
        this.fileLines.add("task testJar(type: Jar) {");
        this.fileLines.add(" baseName = project.name + '-jar-with-dependencies'");
        this.fileLines.add(" from { (configurations.testRuntime).collect { it.isDirectory() ? it : zipTree(it) } }");
        this.fileLines.add(" from sourceSets.test.output");
        this.fileLines.add("}");
    }

    public void addRequiredDependencies() {
        int idx = this.fileLines.indexOf("dependencies {") + 1;

        if (idx > 0) {
            this.fileLines.add(idx, "compile 'org.apache.commons:commons-lang3:3.12.0'\n" +
                    "compile 'com.thoughtworks.xstream:xstream:1.4.15'\n" +
                    "compile 'org.mockito:mockito-core:2.8.9'\n" +
                    "compile 'org.mockito:mockito-all:1.10.19'");
        } else {
            this.fileLines.add("dependencies {");
            this.fileLines.add("compile 'org.apache.commons:commons-lang3:3.12.0'\n" +
                    "compile 'com.thoughtworks.xstream:xstream:1.4.15'\n" +
                    "compile 'org.mockito:mockito-core:2.8.9'\n" +
                    "compile 'org.mockito:mockito-all:1.10.19'");
            this.fileLines.add("}");
        }
    }

    public void updateOldFatJarPlugin() {
        int idx = this.fileLines.indexOf("task fatJar(type: Jar) {") + 2;
        this.fileLines.set(idx,
                " from { (configurations.runtimeClasspath).collect { it.isDirectory() ? it : zipTree(it) } }");
        this.fileLines.add(++idx, " duplicatesStrategy = DuplicatesStrategy.EXCLUDE");
        this.fileLines.set(++idx,
                " from { (configurations.testRuntimeClasspath).collect { it.isDirectory() ? it : zipTree(it) } }");
    }

    public void updateOldTestJarPlugin() {
        int idx = this.fileLines.indexOf("task testJar(type: Jar) {") + 2;
        this.fileLines.set(idx,
                " from { (configurations.testRuntimeClasspath).collect { it.isDirectory() ? it : zipTree(it) } }");
        this.fileLines.add(++idx, " duplicatesStrategy = DuplicatesStrategy.EXCLUDE");
    }

    public void updateOldDependencies() {
        int idx = this.fileLines.indexOf("dependencies {") + 1;
        this.fileLines.set(idx, "implementation 'org.apache.commons:commons-lang3:3.12.0'\n" +
                "implementation 'com.thoughtworks.xstream:xstream:1.4.15'\n" +
                "implementation 'org.mockito:mockito-core:2.8.9'\n" +
                "implementation 'org.mockito:mockito-all:1.10.19'");
    }

    public void saveChanges() {
        BufferedWriter bf;
        try {
            bf = new BufferedWriter(new FileWriter(this.buildFile));
            for (String line : this.fileLines) {
                bf.write(line + "\n");
            }
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
