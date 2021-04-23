package org.instrumentation;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PomFileInstrumentation {
  private File pomFileDirectory;
  private File pomFile;

  public PomFileInstrumentation(String pathPomFile){
    this.pomFileDirectory = new File(pathPomFile);
    this.pomFile = new File(pathPomFile+File.separator+"pom.xml");
  }

  public File getPomFile() {
    return pomFile;
  }

  public File getPomFileDirectory() {
    return this.pomFileDirectory;
  }

  private void saveChangesOnPomFiles(Document document) throws TransformerException {
    DOMSource source = new DOMSource(document);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StreamResult result = new StreamResult(this.pomFile.getPath());
    transformer.transform(source, result);
  }

  public boolean addPluginForJarWithAllDependencies() throws TransformerException {
    Document document = getPomFileAsDocument();
    boolean addedDependency = false;

    if (document != null){
      document.getDocumentElement().normalize();

      Element root = document.getDocumentElement();
      NodeList nList = document.getElementsByTagName("plugins");

      if (nList.getLength() < 1){
        NodeList nListBuild = document.getElementsByTagName("build");
        if (nListBuild.getLength() < 1){
          Node build = document.createElement("build");
          Node plugins = getPluginsNode(document);
          build.appendChild(plugins);
          root.appendChild(build);
          addedDependency = true;
        }else{
          for (int temp = 0; temp < nListBuild.getLength(); temp++) {
            Node build = nListBuild.item(temp);
            Node plugins = getPluginsNode(document);
            build.appendChild(plugins);
            addedDependency = true;
          }
        }
      } else{
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          Node plugin = getPluginNode(document);
          if (!isNodeAlreadyAvailable(document.getElementsByTagName("plugin"), plugin)){
            node.appendChild(plugin);
            saveChangesOnPomFiles(document);
            addedDependency = true;
          }
        }
      }
      saveChangesOnPomFiles(document);
    }
    return addedDependency;

  }

  private Document getPomFileAsDocument() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    Document document = null;
    try {
      builder = factory.newDocumentBuilder();
      document = builder.parse(this.pomFile);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return document;
  }

  public boolean addRequiredDependenciesOnPOM() throws TransformerException {
    Document document = getPomFileAsDocument();
    boolean addedDependencies = false;

    if (document != null){
      document.getDocumentElement().normalize();

      Element root = document.getDocumentElement();
      NodeList nList = document.getElementsByTagName("dependencies");

      if (nList.getLength() < 1){
        Node dependencies = document.createElement("dependencies");
        Node xstream = getNode(document, "com.thoughtworks.xstream", "xstream", "1.4.15");
        Node commons = getNode(document, "org.apache.commons", "commons-lang3", "3.0");
        dependencies.appendChild(xstream);
        dependencies.appendChild(commons);
        root.appendChild(dependencies);
        saveChangesOnPomFiles(document);
        return true;
      }else{
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          if (node.getParentNode().equals(root)) {
            Node xstream = getNode(document, "com.thoughtworks.xstream", "xstream", "1.4.15");
            if (!isDependencyAlreadyAvailable(document.getElementsByTagName("dependency"), xstream)){
              node.appendChild(xstream);
              addedDependencies = true;
            }
            Node commons = getNode(document, "org.apache.commons", "commons-lang3", "3.0");
            if (!isDependencyAlreadyAvailable(document.getElementsByTagName("dependency"), commons)) {
              node.appendChild(commons);
              addedDependencies = true;
            }
          }
          saveChangesOnPomFiles(document);
        }
      }
    }
    return addedDependencies;

  }

  private boolean isNodeAlreadyAvailable(NodeList nodeList, Node newDependency){
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      if(node.getFirstChild() != null && node.getFirstChild().getFirstChild() != null &&
          node.getFirstChild().getFirstChild().getTextContent().equals(newDependency.getFirstChild().getFirstChild().getTextContent())){
        return true;
      }else if(node.getFirstChild() != null && node.getFirstChild().getNextSibling() != null && node.getFirstChild().getNextSibling().getFirstChild() != null &&
          node.getFirstChild().getNextSibling().getFirstChild().getTextContent().equals(newDependency.getFirstChild().getFirstChild().getTextContent())){
        return true;
      }
    }
    return false;
  }

  private boolean isDependencyAlreadyAvailable(NodeList nodeList, Node newDependency){
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      //check if the groupId is already declared on the target dependency
      if(node.getFirstChild().getNextSibling().getFirstChild() != null && newDependency.getFirstChild().getFirstChild() != null &&
          node.getFirstChild().getNextSibling().getFirstChild().getTextContent().
              equals(newDependency.getFirstChild().getFirstChild().getTextContent())
      //check if the artifactID is also declared on the target dependency
      && (node.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild() != null &&
         node.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getTextContent().
             equals(newDependency.getFirstChild().getNextSibling().getFirstChild().getTextContent()))) {
        return true;
      }
    }
    return false;
  }

  private Node getPluginNode(Document document){
    Node plugin = document.createElement("plugin");
    Node artifactId = document.createElement("artifactId");
    artifactId.setTextContent("maven-assembly-plugin");
    Node configuration = document.createElement("configuration");
    Node archive = document.createElement("archive");
    Node manifest = document.createElement("manifest");
    Node mainClass = document.createElement("mainClass");
    mainClass.setTextContent("fully.qualified.MainClass");
    manifest.appendChild(mainClass);
    archive.appendChild(manifest);
    configuration.appendChild(archive);

    Node descriptorRefs = document.createElement("descriptorRefs");
    Node descriptorRef = document.createElement("descriptorRef");
    descriptorRef.setTextContent("jar-with-dependencies");
    descriptorRefs.appendChild(descriptorRef);
    configuration.appendChild(descriptorRefs);

    plugin.appendChild(artifactId);
    plugin.appendChild(configuration);

    return plugin;
  }

  private Node getNode(Document document, String groupIdValue, String artifactIdValue, String versionValue) {
    Node dependency = document.createElement("dependency");
    Node groupId = document.createElement("groupId");
    groupId.setTextContent(groupIdValue);
    Node artifactId = document.createElement("artifactId");
    artifactId.setTextContent(artifactIdValue);
    Node version = document.createElement("version");
    version.setTextContent(versionValue);

    dependency.appendChild(groupId);
    dependency.appendChild(artifactId);
    dependency.appendChild(version);
    return dependency;
  }

  public boolean addResourcesForGeneratedJar() throws TransformerException {
    Document document = getPomFileAsDocument();
    boolean addedResource = false;

    if (document != null){
      document.getDocumentElement().normalize();
      Element root = document.getDocumentElement();
      NodeList nList = document.getElementsByTagName("resources");

      if (nList.getLength() < 1) {
        NodeList nListBuild = document.getElementsByTagName("build");
        if (nListBuild.getLength() < 1) {
          Node build = document.createElement("build");
          Node resources = getResourcesNode(document);
          build.appendChild(resources);
          root.appendChild(build);
          addedResource = true;
        } else {
          for (int temp = 0; temp < nListBuild.getLength(); temp++) {
            Node nodeBuild = nListBuild.item(temp);
            Node resources = getResourcesNode(document);
            nodeBuild.appendChild(resources);
            addedResource = true;
          }
        }
      } else {
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          Node resource = getResourceNode(document);

          if (!isNodeAlreadyAvailable(document.getElementsByTagName("resource"), resource)) {
            node.appendChild(resource);
            addedResource = true;
          }
        }
      }
      saveChangesOnPomFiles(document);
    }
    return addedResource;
  }

  private Node getPluginsNode(Document document){
    Node plugins = document.createElement("plugins");
    Node plugin = getPluginNode(document);
    plugins.appendChild(plugin);
    return plugins;
  }

  private Node getResourcesNode(Document document){
    Node resources = document.createElement("resources");
    Node resource = getResourceNode(document);
    resources.appendChild(resource);
    return resources;
  }

  private Node getResourceNode(Document document) {
    Node resource = document.createElement("resource");
    Node directory = document.createElement("directory");
    directory.setTextContent("src/main/resources");
    Node includes = document.createElement("includes");
    Node include = document.createElement("include");
    include.setTextContent("**/*.xml");
    includes.appendChild(include);

    resource.appendChild(directory);
    resource.appendChild(includes);
    return resource;
  }

}
