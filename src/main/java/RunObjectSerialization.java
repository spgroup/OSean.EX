import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.transform.TransformerException;
import org.file.FileFinderSupport;
import org.file.ObjectSerializerSupporter;
import org.file.SerializedObjectAccessOutputClass;
import org.instrumentation.JavaClassIntrumentation;
import org.instrumentation.PomFileInstrumentation;
import org.util.JarManager;
import org.util.ProcessManager;

public class RunObjectSerialization {

  /***
   * The values on args are used to drive the serialization process
   * The first input is the local path of the project under analysis
   * The second input is the class holding the method that the instrumentation will be applied
   * The third input is the method where the instrumentation will be applied
   * The fourth input is the project name
   * The fifth input is generated java jar file name (optional)
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, TransformerException {
    if (args.length > 4){
      FileFinderSupport fileFinderSupport = new FileFinderSupport(args[0]);
      File pomDirectory = fileFinderSupport.findFile(args[1], fileFinderSupport.getProjectLocalPath());

      if (pomDirectory != null) {
        PomFileInstrumentation pomFileInstrumentation = new PomFileInstrumentation(
            pomDirectory.getPath());
        pomFileInstrumentation.addRequiredDependenciesOnPOM();
        pomFileInstrumentation.addResourcesForGeneratedJar();
        pomFileInstrumentation.addPluginForJarWithAllDependencies();
        fileFinderSupport.createNewDirectory(pomDirectory);

        ObjectSerializerSupporter objectSerializerSupporter = new ObjectSerializerSupporter(
            Paths.get(fileFinderSupport.getProjectLocalPath().getPath() + File.separator + "src" +
                File.separator + "main" + File.separator + "java")
                .relativize(Paths.get(fileFinderSupport.
                    getTargetClassLocalPath().getPath())).toString().replace(File.separator, "."));
        objectSerializerSupporter
            .getOutputClass(fileFinderSupport.getTargetClassLocalPath().getPath(),
                fileFinderSupport
                    .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        JavaClassIntrumentation javaClassIntrumentation = new JavaClassIntrumentation(args[2],
            objectSerializerSupporter.getFullSerializerSupporterClass());
        javaClassIntrumentation.runTransformation(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + args[1]));
        runTestabilityTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + args[1]));

        Process process = Runtime.getRuntime()
            .exec("mvn clean test", null,
                new File(fileFinderSupport.getProjectLocalPath().getPath()));
        ProcessManager.computeProcessOutput(process, "Creating Serialized Objects");

        SerializedObjectAccessOutputClass serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();

        Process process4 = Runtime.getRuntime()
            .exec("mvn clean compile assembly:single", null, new File(pomDirectory.getPath()));
        ProcessManager.computeProcessOutput(process4, "Generating jar file with serialized objects");

        String generatedJarFile = JarManager.getJarFile(pomFileInstrumentation);
        Process process2 = Runtime.getRuntime()
            .exec("java -cp " + generatedJarFile
                + " " + getObjectClassPathOnTargetProject(javaClassIntrumentation), null, new File(pomDirectory.getPath()));
        ProcessManager.computeProcessOutput(process2, "Generating method list associated to serialized objects");
        List<String> aux = getMethodList(fileFinderSupport
            .getResourceDirectoryPath(pomFileInstrumentation.getPomFileDirectory()));

        serializedObjectAccessOutputClass
            .getOutputClass(aux, fileFinderSupport.getTargetClassLocalPath().getPath(),
                objectSerializerSupporter.getFullSerializerSupporterClass());

        javaClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + args[1]));
        objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());

        Process process3 = Runtime.getRuntime()
            .exec("mvn clean compile assembly:single", null, new File(pomDirectory.getPath()));
        ProcessManager.computeProcessOutput(process3, "Generating jar file with serialized objects");

        fileFinderSupport.deleteResourceDirectory();
        serializedObjectAccessOutputClass.deleteOldClassSupporter();
        JarManager.saveGeneratedJarFile(generatedJarFile, args[0].split(args[3])[0]+File.separator+"GeneratedJars"+File.separator+args[3], (args.length > 4 ? args[4] : "generated-jar")+".jar");
      }else{
        System.out.println("Please inform all three inputs required to run the serialization process");
        System.out.println("1º: local project path");
        System.out.println("2º: class holding the target method");
        System.out.println("3º: target method");
        System.out.println("4º: project name");
        System.out.println("5º: generated java jar name (optional)");
      }
    }

  }

  private static List<String> getMethodList(String resourceDirectory){
    List<String> methods = new ArrayList<>();
    if (new File(resourceDirectory+File.separator+"output-methods.txt").exists()){
      try {
        File file = new File(resourceDirectory+File.separator+"output-methods.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          methods.add(myReader.nextLine());
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    return methods;
  }

  private static String getObjectClassPathOnTargetProject(JavaClassIntrumentation javaClassIntrumentation) {
    return javaClassIntrumentation.getPackageName() + File.separator
        + "ObjectSerializerSupporter";
  }

  private static boolean runTestabilityTransformations(File file){
    try {
      Process processTestability = Runtime.getRuntime().exec("java -jar "+System.getProperty("user.dir")+File.separator+"src"+
          File.separator+"main"+File.separator+"resources"+File.separator+"testability-transformations.jar" + " " +
          file.toPath().toString());
      ProcessManager.computeProcessOutput(processTestability, "Applying Testability Transformations");
      return true;
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

}
