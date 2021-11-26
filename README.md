# OSean.EX
Object Serialization by Test Suite Execution

This project aims to generate executable jar files with serialized objects for a given commit or a list of them. 
For that, we instrument a <i>target method</i> of a <i>given target class</i>, and then we run the original project test suites. 
As a result, a list of serialized objects is generated as XML files. 

## Getting Started
* Cloning the project: you must clone the repository using the recursive option: ```git clone --recurse-submodules https://github.com/leusonmario/OSean.EX```

* Maven version: we use Maven as a build manager, so you must install Maven 3.6.0 or newer.

* Setting up dependencies: once the repository is available, you must add the jar file to your local Maven repository. 
For that, you may use: ```mvn install:install-file -Dfile=${localRepositoryPath}/src/main/resources/testability-transformations.jar -DgroupId=com.org.testability-transformations -DartifactId=testability-transformations -Dversion=1.0 -Dpackaging=jar```

* Running the tests: you may run: ```mvn clean test```

* Generating object serializer executable file: you may run on terminal: ```mvn clean compile assembly:single```. As a result, the jar file ```ObjectSerialization-1.0-SNAPSHOT-jar-with-dependencies.jar``` is expected to be generated in ```${localRepositoryPath}/target```. 

## Serializing Objects
Once you have the object serializer jar file, you can call it for a specific project. For that, you must inform:
* local project path; 
* target class name; 
* target method name; 
* project name;
* indication for applying the testability transformations (true/false);
* indication for fully applying the testability transformations (empty constructor, true/false);
* list of commits.

For example, consider calling the object serializer for this project: ```java -cp ObjectSerialization-1.0-SNAPSHOT-jar-with-dependencies.jar org.RunSerialization "/home/lmps2/projects/toy-project" "Person.java" "addRelative" "toy-project" true true "abdc125" "abdc156"```.

You may inform up to four (4) commit hashes.
The first commit will be used to create the serialized objects; next, they will be deserialized in all commits previously informed.
As a result, a jar file for each commit will be generated in ```/home/lmps2/projects/GeneratedJars/toy-project``` using each commit hash as the file name. 