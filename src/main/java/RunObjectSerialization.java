import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.file.FileFinderSupport;
import org.file.ObjectSerializerSupporter;
import org.file.SerializedObjectAccessOutputClass;
import org.instrumentation.JavaClassIntrumentation;
import org.instrumentation.PomFileInstrumentation;

public class RunObjectSerialization {

  /***
   * The values on args are used to drive the serialization process
   * The first input is the local path of the project under analysis
   * The second input is the class holding the method that the instrumentation will be applied
   * The third input is the method where the instrumentation will be applied
   */
  public static void main(String[] args)
      throws IOException, InterruptedException, TransformerException {
    if (args.length > 2){
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

        Process process = Runtime.getRuntime()
            .exec("mvn clean test", null,
                new File(fileFinderSupport.getProjectLocalPath().getPath()));
        System.out.println("Creating Serialized Objects: final status - " + process.waitFor());

        SerializedObjectAccessOutputClass serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();

        Process process4 = Runtime.getRuntime()
            .exec("mvn clean compile assembly:single", null, new File(pomDirectory.getPath()));
        System.out.println(
            "Generating jar file with serialized objects: final status - " + process4.waitFor());

        Process process2 = Runtime.getRuntime()
            .exec("java -cp " + findJarFile(
                new File(pomFileInstrumentation.getPomFileDirectory() + File.separator + "target"))
                + " " + javaClassIntrumentation.getPackageName() + File.separator
                + "ObjectSerializerSupporter", null, new File(pomDirectory.getPath()));
        List<String> aux = getListOfMethodsAssociatedToSerializedObjects(process2);
        System.out.println(
            "Generating method list associated to serialized objects: final status - " + process2
                .waitFor());

        serializedObjectAccessOutputClass
            .getOutputClass(aux, fileFinderSupport.getTargetClassLocalPath().getPath(),
                objectSerializerSupporter.getFullSerializerSupporterClass());

        javaClassIntrumentation.undoTransformations(new File(
            fileFinderSupport.getTargetClassLocalPath() + File.separator + args[1]));
        objectSerializerSupporter.deleteObjectSerializerSupporterClass(fileFinderSupport.getTargetClassLocalPath().getPath());

        Process process3 = Runtime.getRuntime()
            .exec("mvn clean compile assembly:single", null, new File(pomDirectory.getPath()));
        System.out.println(
            "Generating jar file with serialized objects: final status - " + process3.waitFor());
      }else{
        System.out.println("Please inform all three inputs required to run the serialization process");
        System.out.println("First: local project path");
        System.out.println("Second: class holding the target method");
        System.out.println("Third: target method");
      }
    }

  }

  private static List<String> getListOfMethodsAssociatedToSerializedObjects(Process process2) throws IOException {
    BufferedReader stdInput = new BufferedReader(new
        InputStreamReader(process2.getInputStream()));
    String s = null;
    List<String> aux = new ArrayList<>();
    while ((s = stdInput.readLine()) != null) {
      aux.add("\n" + s);
    }
    return aux;
  }

  private static String findJarFile(File targetDirectory){
    File[] list = targetDirectory.listFiles();
    if(list!=null)
      for (File fil : list) {
        if (fil.isDirectory()){
          String aux = findJarFile(fil);
          if (aux!=null){
            return aux;
          }
        }
        else if (fil.getName().contains("jar-with-dependencies")){
          return fil.getAbsolutePath();
        }
      }
    return null;
  }

}
