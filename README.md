# OSean.EX
Object Serialization by Test Suite Execution

This project aims to generate executable jar files with serialized objects for a given commit or a list of them. 
For that, we instrument a <i>target method</i> of a <i>given target class</i>, and then we run the original project test suites. 
As a result, a list of serialized objects is generated as XML files. 

## Getting Started
* Cloning the project: you must clone the repository using the recursive option: ```git clone --recurse-submodules https://github.com/leusonmario/OSean.EX```

* Maven version: we use Maven as a build manager, so you must install Maven 3.6.0 or newer.
* Java version: Java 9 or above.

* Setting up dependencies: once the repository is available, you must add the jar file to your local Maven repository. 
For that, you may use: ```mvn install:install-file -Dfile=${localRepositoryPath}/src/main/resources/testability-transformations.jar -DgroupId=com.org.testability-transformations -DartifactId=testability-transformations -Dversion=1.0 -Dpackaging=jar```

* Running the tests: you may run: ```mvn clean test```

* Generating object serializer executable file: you may run on terminal: ```mvn clean compile assembly:single```. As a result, the jar file ```ObjectSerialization-1.0-SNAPSHOT-jar-with-dependencies.jar``` is expected to be generated in ```${localRepositoryPath}/target```. 

## Serializing Objects
Once you have the object serializer jar file, you can call it for a specific project using the CLI. For that, you must inform:

- Local project path (-l,--localProjectPath). 
    - It is important that the path provided to this flag is absolute (e.g "/home/lmps2/projects/toy-project"), not a relative path (e.g "./projects/toy-project").
- Target Class Name (-c,--targetClassName)
- Target Method Name (-m,--targetMethodName).
- Project name (-p,--projectName)
- Comma separated list of commits (--commits).

You can also provide these others options:

- Doesn't apply testability transformations (-datt,--doesNotApplyTestabilityTransformations). By default, the application will apply all transformations.
- Doesn't apply full testability transformations (-dfatt,--doesNotApplyFullTestabilityTransformations)
- Serialization budget time in seconds (-b,--budgetTime). By default, a value of 60s is used.
- Indicate usage of Gradle as build tool (-g, --gradle). If this flag is not provided, the application will look for a build tool based on the files present in the project root directory.
- Indicates that serialized objects WON'T be generated (-ds, --doesNotSerialize). If this flag is provided, the application will generate JARs without serialized object for each commit given.

For example, consider calling the object serializer for this project: ```java -cp ObjectSerialization-1.0-SNAPSHOT-jar-with-dependencies.jar org.Main -l /home/lmps2/projects/toy-project -c Person.java -m addRelative -p toy-project -datt -dfatt -b 60 --commits abdc125,abdc156```.

You may inform up to four (4) commit hashes.
The first commit will be used to create the serialized objects; next, they will be deserialized in all commits previously informed.
As a result, a jar file for each commit will be generated in ```/home/lmps2/projects/GeneratedJars/toy-project``` using each commit hash as the file name. 