package org.transformations.instrumentation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PomFileInstrumentation {
  private File pomFileDirectory;
  private File pomFile;

  public PomFileInstrumentation(String pathPomFile) {
    this.pomFileDirectory = new File(pathPomFile);
    this.pomFile = new File(pathPomFile + File.separator + "pom.xml");
  }

  public File getPomFile() {
    return this.pomFile;
  }

  public File getPomFileDirectory() {
    return this.pomFileDirectory;
  }

  private void saveChangesOnPomFiles(Document document) throws TransformerException {
    DOMSource source = new DOMSource(document);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StreamResult result = new StreamResult(document.getDocumentURI());
    transformer.transform(source, result);
  }

  public boolean addPluginForJarWithAllDependencies() throws TransformerException {
    Document document = getPomFileAsDocument(this.pomFile);
    boolean addedDependency = false;

    if (document != null) {
      document.getDocumentElement().normalize();

      Element root = document.getDocumentElement();
      NodeList nList = document.getElementsByTagName("plugins");

      if (nList.getLength() < 1) {
        NodeList nListBuild = document.getElementsByTagName("build");
        if (nListBuild.getLength() < 1) {
          Node build = document.createElement("build");
          Node plugins = getPluginsNode(document);
          build.appendChild(plugins);
          root.appendChild(build);
          addedDependency = true;
        } else {
          for (int temp = 0; temp < nListBuild.getLength(); temp++) {
            Node build = nListBuild.item(temp);
            Node plugins = getPluginsNode(document);
            build.appendChild(plugins);
            addedDependency = true;
          }
        }
      } else {
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          Node plugin = getPluginMavenAssemblyPlugin(document);
          if ((node.getParentNode().getNodeName().equals("build")
              || node.getParentNode().getNodeName().equals("pluginManagement"))
              && !isNodeAlreadyAvailable(document.getElementsByTagName("plugin"), plugin,
                  document.getElementsByTagName("descriptorRef"))) {
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

  private Document getPomFileAsDocument(File pomFile) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    Document document = null;
    try {
      builder = factory.newDocumentBuilder();
      document = builder.parse(pomFile);
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
    Document document = getPomFileAsDocument(this.pomFile);
    boolean addedDependencies = false;

    if (document != null) {
      document.getDocumentElement().normalize();

      Element root = document.getDocumentElement();
      NodeList nList = document.getElementsByTagName("dependencies");

      if (nList.getLength() < 1) {
        Node dependencies = document.createElement("dependencies");
        Node xstream = getNode(document, "com.thoughtworks.xstream", "xstream", "1.4.15");
        Node commons = getNode(document, "org.apache.commons", "commons-lang3", "3.12.0");
        Node mockitoCore = getNode(document, "org.mockito", "mockito-core", "2.8.9");
        Node mockito = getNode(document, "org.mockito", "mockito-all", "1.10.19");
        dependencies.appendChild(xstream);
        dependencies.appendChild(commons);
        dependencies.appendChild(mockitoCore);
        dependencies.appendChild(mockito);
        root.appendChild(dependencies);
        saveChangesOnPomFiles(document);
        return true;
      } else {
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          if (node.getParentNode().equals(root)) {
            Node xstream = getNode(document, "com.thoughtworks.xstream", "xstream", "1.4.15");
            if (!isDependencyAlreadyAvailable(document, document.getElementsByTagName("dependency"), xstream)) {
              node.appendChild(xstream);
              addedDependencies = true;
            }
            Node commons = getNode(document, "org.apache.commons", "commons-lang3", "3.12.0");
            if (!isDependencyAlreadyAvailable(document, document.getElementsByTagName("dependency"), commons)) {
              node.appendChild(commons);
              addedDependencies = true;
            }
            Node mockitoCore = getNode(document, "org.mockito", "mockito-core", "2.8.9");
            node.appendChild(mockitoCore);

            Node mockito = getNode(document, "org.mockito", "mockito-all", "1.10.19");
            node.appendChild(mockito);
            addedDependencies = true;
          }
          saveChangesOnPomFiles(document);
        }
      }
    }
    return addedDependencies;

  }

  private boolean isNodeForJarWithDependenciesAvailable(NodeList nodeList, Node newNode) {
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      try {
        if (newNode.getFirstChild().getNextSibling().getFirstChild().getNextSibling()
            .getFirstChild().getTextContent()
            .equals(node.getTextContent())) {
          return true;
        }
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  private boolean isNodeAlreadyAvailable(NodeList nodeList, Node newDependency, NodeList descriptorRefs) {
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      if (node.getFirstChild() != null && node.getFirstChild().getNextSibling() != null
          && node.getFirstChild().getNextSibling().getFirstChild() != null &&
          node.getFirstChild().getNextSibling().getFirstChild().getTextContent()
              .equals(newDependency.getFirstChild().getFirstChild().getTextContent())) {

        if (isNodeForJarWithDependenciesAvailable(descriptorRefs, newDependency))
          return true;
        else {
          nodeList.item(temp).getParentNode().removeChild(node);
          return false;
        }
      }
    }
    return false;
  }

  private boolean isDependencyAlreadyAvailable(Document document, NodeList nodeList, Node newDependency)
      throws DOMException, TransformerException {
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      // check if the groupId is already declared on the target dependency
      if (node.getFirstChild().getNextSibling().getFirstChild() != null
          && newDependency.getFirstChild().getFirstChild() != null &&
          node.getFirstChild().getNextSibling().getFirstChild().getTextContent()
              .equals(newDependency.getFirstChild().getFirstChild().getTextContent())
          // check if the artifactID is also declared on the target dependency
          && (node.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild() != null &&
              node.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getTextContent()
                  .equals(newDependency.getFirstChild().getNextSibling().getFirstChild().getTextContent()))) {
        updateVersionNode(document, node,
            newDependency.getFirstChild().getNextSibling().getNextSibling().getFirstChild().getTextContent());
        return true;
      }
    }
    return false;
  }

  private boolean isNodeForResourceAlreadyAvailable(NodeList nodeList, Node newDependency) {
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      if (node.getFirstChild() != null && node.getFirstChild().getFirstChild() != null &&
          node.getFirstChild().getFirstChild().getTextContent()
              .equals(newDependency.getFirstChild().getFirstChild().getTextContent())) {
        return true;
      } else if (node.getFirstChild() != null && node.getFirstChild().getNextSibling() != null
          && node.getFirstChild().getNextSibling().getFirstChild() != null &&
          node.getFirstChild().getNextSibling().getFirstChild().getTextContent()
              .equals(newDependency.getFirstChild().getFirstChild().getTextContent())) {
        return true;
      }
    }
    return false;
  }

  private Node getPluginMavenAssemblyPlugin(Document document) {
    Node plugin = document.createElement("plugin");

    Node artifactId = document.createElement("artifactId");
    artifactId.setTextContent("maven-assembly-plugin");
    Node version = document.createElement("version");
    version.setTextContent("2.3");
    Node configuration = document.createElement("configuration");
    Node descriptor = document.createElement("descriptor");
    descriptor.setTextContent("src/main/assembly/assembly.xml");
    configuration.appendChild(descriptor);
    plugin.appendChild(artifactId);
    plugin.appendChild(version);
    plugin.appendChild(configuration);

    Node executions = document.createElement("executions");
    Node execution = document.createElement("execution");
    Node id = document.createElement("id");
    id.setTextContent("make-assembly");
    execution.appendChild(id);

    Node phase = document.createElement("phase");
    phase.setTextContent("package");
    execution.appendChild(phase);

    Node goals = document.createElement("goals");
    Node goal = document.createElement("goal");
    goal.setTextContent("single");
    goals.appendChild(goal);
    execution.appendChild(goals);

    Node configurationExec = document.createElement("configuration");
    Node archive = document.createElement("archive");
    Node manifest = document.createElement("manifest");
    Node mainClass = document.createElement("mainClass");
    mainClass.setTextContent("fully.qualified.MainClass");
    manifest.appendChild(mainClass);
    archive.appendChild(manifest);
    configurationExec.appendChild(archive);
    execution.appendChild(configurationExec);

    executions.appendChild(execution);
    plugin.appendChild(executions);

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
    Node scope = document.createElement("scope");
    scope.setTextContent("compile");

    dependency.appendChild(groupId);
    dependency.appendChild(artifactId);
    dependency.appendChild(version);
    dependency.appendChild(scope);
    return dependency;
  }

  public boolean addResourcesForGeneratedJar() throws TransformerException {
    Document document = getPomFileAsDocument(this.pomFile);
    boolean addedResource = false;

    if (document != null) {
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

          if (!isNodeForResourceAlreadyAvailable(document.getElementsByTagName("resource"), resource)) {
            node.appendChild(resource);
            addedResource = true;
          }
        }
      }
      saveChangesOnPomFiles(document);
    }
    return addedResource;
  }

  public boolean changeAnimalSnifferPluginIfAdded() throws TransformerException {
    Document document = getPomFileAsDocument(this.pomFile);

    if (document != null) {
      document.getDocumentElement().normalize();
      NodeList nodeList = document.getElementsByTagName("plugin");

      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node.hasChildNodes()) {
          Node myNode = searchForTargetArtifactIdNode(node.getChildNodes());
          if (myNode != null) {
            changeVersionOfTargetPluginNode(myNode.getChildNodes());
            if (changeArtifactIdOfTargetPlugin(document, myNode)) {
              saveChangesOnPomFiles(document);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private Node searchForTargetArtifactIdNode(NodeList nodeList) {

    for (int i = 0; i < nodeList.getLength(); i++) {
      Node currentNode = nodeList.item(i);
      if (currentNode.getNodeName().equals("artifactId") &&
          currentNode.getTextContent().equals("animal-sniffer-maven-plugin")) {
        return currentNode.getParentNode();
      }
    }
    return null;
  }

  private void changeVersionOfTargetPluginNode(NodeList nodeList) {

    for (int i = 0; i < nodeList.getLength(); i++) {
      Node currentNode = nodeList.item(i);
      if (currentNode.getNodeName().equals("version") &&
          !currentNode.getTextContent().equals("1.18")) {
        currentNode.setTextContent("1.18");
      }
    }
  }

  private boolean changeArtifactIdOfTargetPlugin(Document document, Node parentNode) {

    if (document != null) {
      document.getDocumentElement().normalize();
      NodeList nList = document.getElementsByTagName("artifactId");

      if (nList.getLength() > 0) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          if (node.getParentNode().getNodeName().equals("signature") &&
              !node.getTextContent().equals("java18") &&
              parentNode.getTextContent().equals(node.getParentNode().getParentNode().getParentNode().getParentNode()
                  .getParentNode().getTextContent())) {
            node.setTextContent("java18");
            return true;
          }
        }
      }
    }
    return false;
  }

  private Node getPluginsNode(Document document) {
    Node plugins = document.createElement("plugins");
    Node plugin = getPluginMavenAssemblyPlugin(document);
    plugins.appendChild(plugin);
    return plugins;
  }

  private Node getResourcesNode(Document document) {
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
    include.setTextContent("**/*.*l");
    includes.appendChild(include);

    Node includeYml = document.createElement("include");
    includeYml.setTextContent("**/*.yml");
    // includes.appendChild(includeYml);

    resource.appendChild(directory);
    resource.appendChild(includes);
    return resource;
  }

  public void updateOldDependencies() throws TransformerException {
    Document document = getPomFileAsDocument(this.pomFile);

    if (document != null) {
      document.getDocumentElement().normalize();

      NodeList nList = document.getElementsByTagName("version");
      for (int temp = 0; temp < nList.getLength(); temp++) {
        Node node = nList.item(temp);
        if (node.getFirstChild().getNodeValue().contains("SNAPSHOT")
            && node.getParentNode() != null
            && node.getParentNode().getNodeName().equals("parent")) {
          String newVersion = node.getFirstChild().getNodeValue().split("(-)?SNAPSHOT")[0];
          node.setTextContent(newVersion);
        }
      }
      saveChangesOnPomFiles(document);
    }
  }

  public void updateOldRepository() throws TransformerException {
    Document document = getPomFileAsDocument(this.pomFile);

    if (document != null) {
      document.getDocumentElement().normalize();

      NodeList nList = document.getElementsByTagName("url");
      for (int temp = 0; temp < nList.getLength(); temp++) {
        Node node = nList.item(temp);
        if (node.getFirstChild().getNodeValue().equals("http://download.osgeo.org/webdav/geotools/")
            && node.getParentNode() != null
            && node.getParentNode().getNodeName().equals("repository")) {
          node.getFirstChild().setTextContent("https://repo.osgeo.org/repository/release/");
        }
      }
      saveChangesOnPomFiles(document);
    }
  }

  // Method used to fix surefire plugin behavior in elasticsearch-river-mongodb
  // project
  public void changeSurefirePlugin(String targetPackage) throws TransformerException {
    Document document = getPomFileAsDocument(this.pomFile);

    if (document != null) {
      document.getDocumentElement().normalize();

      NodeList nList = document.getElementsByTagName("artifactId");

      int numberPlugins = nList.getLength();
      for (int temp = 0; temp < numberPlugins; temp++) {
        Node node = nList.item(temp);
        Node target = node.getFirstChild();
        String test = target.getNodeValue();
        if (test.equals("maven-surefire-plugin")) {
          Node surefire = document.createElement("plugin");

          Node groupId = document.createElement("groupId");
          groupId.setTextContent("org.apache.maven.plugins");
          Node artifactId = document.createElement("artifactId");
          artifactId.setTextContent("maven-surefire-plugin");
          Node version = document.createElement("version");
          version.setTextContent("2.19.1");

          Node configurationNode = document.createElement("configuration");
          Node includes = document.createElement("includes");
          Node include = document.createElement("include");
          if (targetPackage.equals("")) {
            include.setTextContent("**/*Test.java");
          } else {
            include.setTextContent(targetPackage + ".*");
          }
          includes.appendChild(include);
          configurationNode.appendChild(includes);

          surefire.appendChild(groupId);
          surefire.appendChild(artifactId);
          surefire.appendChild(version);
          surefire.appendChild(configurationNode);
          node.getParentNode().getParentNode().appendChild(surefire);
          node.getParentNode().getParentNode().removeChild(node.getParentNode());
        }
        saveChangesOnPomFiles(document);
      }
    }
  }

  public void changeTagContent(File pomFile, String targetTag, String newValue, ArrayList<String> oldValues)
      throws TransformerException {
    Document document = getPomFileAsDocument(pomFile);

    if (document != null) {
      document.getDocumentElement().normalize();

      NodeList nList = document.getElementsByTagName(targetTag);

      int numberPlugins = nList.getLength();
      for (int temp = 0; temp < numberPlugins; temp++) {
        Node node = nList.item(temp);
        if (oldValues.contains(node.getFirstChild().getNodeValue())) {
          node.getFirstChild().setNodeValue(newValue);
        }
      }
      saveChangesOnPomFiles(document);
    }
  }

  public void removeAllEnforcedDependencies(File dir) throws TransformerException {
    Collection<File> files = getAllPomFiles(dir);
    for (File onePom : files) {
      ArrayList<String> oldValues = new ArrayList<String>(Arrays.asList("enforce"));
      changeTagContent(onePom, "goal", "display-info", oldValues);
    }
  }

  public void updateSourceOption(File dir) throws TransformerException {
    Collection<File> files = getAllPomFiles(dir);
    for (File onePom : files) {
      ArrayList<String> oldValues = new ArrayList<String>(Arrays.asList("1.5", "1.6"));
      changeTagContent(onePom, "source", "1.8", oldValues);
      changeTagContent(onePom, "target", "1.8", oldValues);
    }
  }

  private Collection<File> getAllPomFiles(File dir) {
    return FileUtils.listFiles(
        dir,
        new RegexFileFilter("pom.xml"),
        DirectoryFileFilter.DIRECTORY);
  }

  // Method used to fix incompatibility in mockito plugins used in spring-boot
  // project
  public void changeMockitoCore() throws TransformerException {
    Document document = getPomFileAsDocument(pomFile);

    if (document != null) {
      document.getDocumentElement().normalize();

      NodeList nList = document.getElementsByTagName("artifactId");

      int numberPlugins = nList.getLength();
      for (int temp = 0; temp < numberPlugins; temp++) {
        Node node = nList.item(temp);
        if (node.getFirstChild().getNodeValue().equals("mockito-core"))
          updateVersionNode(document, node.getParentNode(), "1.10.19");
      }
    }
  }

  private void updateVersionNode(Document document, Node node, String newValue) throws TransformerException {
    NodeList nList = node.getChildNodes();
    int numberPlugins = nList.getLength();
    Boolean nodeWithoutVersionTag = true;
    for (int temp = 0; temp < numberPlugins; temp++) {
      Node nodeAux = nList.item(temp);
      String tagName = nodeAux.getNodeName();
      if (tagName.equals("version")) {
        nodeWithoutVersionTag = false;
        nodeAux.getFirstChild().setNodeValue(newValue);
      }
    }
    if (nodeWithoutVersionTag) {
      Node version = document.createElement("version");
      version.setTextContent(newValue);
      node.appendChild(version);
    }
    saveChangesOnPomFiles(document);
  }

}
