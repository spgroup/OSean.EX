package org.transformations.serialization;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.file.ProjectFileSupporter;

public class ObjectSerializationController {
        private ObjectSerializerSupporter objectSerializerSupporter;
        private ObjectDeserializerSupporter objectDeserializerSupporter;
        private SerializedObjectAccessOutputClass serializedObjectAccessOutputClass;
        private ConverterSupporter converterSupporter;

        public void createObjectSerializerSupporter(String projectLocalPath,
                        String targetClassLocalPath) {
                this.objectSerializerSupporter = new ObjectSerializerSupporter(
                                Paths.get(projectLocalPath + File.separator + "src" +
                                                File.separator + "main" + File.separator + "java")
                                                .relativize(Paths.get(targetClassLocalPath)).toString()
                                                .replace(File.separator, "."),
                                targetClassLocalPath);
        }

        public boolean injectObjectSerializerSupporterFile(String targetClassLocalPath,
                        String resourcesDirectoryLocalPath) {
                return this.objectSerializerSupporter.writeClassFile(targetClassLocalPath, resourcesDirectoryLocalPath);
        }

        public void createObjectDeserializerSupporter(String projectLocalPath,
                        String targetClassLocalPath) {
                this.objectDeserializerSupporter = new ObjectDeserializerSupporter(
                                Paths.get(projectLocalPath + File.separator + "src" +
                                                File.separator + "main" + File.separator + "java")
                                                .relativize(Paths.get(targetClassLocalPath)).toString()
                                                .replace(File.separator, "."),
                                targetClassLocalPath);
        }

        public String getTargetClassPackage() {
                return this.objectSerializerSupporter.getClassPackage();
        }

        public boolean deleteObjectSerializerSupporterFile() {
                return this.objectSerializerSupporter.deleteThisSupporterClassFile();
        }

        public void createSerializedObjectAccessOutputClass() {
                this.serializedObjectAccessOutputClass = new SerializedObjectAccessOutputClass();
        }

        public void createConverterSupporter() {
                this.converterSupporter = new ConverterSupporter();
        }

        public boolean injectConverterSupporterClassesFiles(ProjectFileSupporter projectFileSupporter,
                        List<String> converterList, String path, String targetClassPackage) {
                return this.converterSupporter.writeConverterSupporterClassFiles(projectFileSupporter, converterList,
                                projectFileSupporter.getTargetClassLocalPath().getPath(),
                                objectSerializerSupporter.getClassPackage());
        }

        public boolean injectObjectDeserializerSupporterFile(String targetClassLocalPath,
                        String resourcesDirectoryLocalPath) {
                return this.objectDeserializerSupporter.writeClassFile(targetClassLocalPath,
                                resourcesDirectoryLocalPath, this.converterSupporter.classesPathSignature);
        }

        public String getObjectDeserializerSupporterSignatureClass() {
                return this.objectDeserializerSupporter.getSignatureClass();
        }

        public boolean deleteObjectDeserializerSupporterFile() {
                return this.objectDeserializerSupporter.deleteThisSupporterClassFile();
        }

        public boolean deleteConverterSupporterClassesFiles() {
                return this.converterSupporter.deleteOldClassSupporter();
        }

        public boolean injectSerializedObjectAccessFile(List<String> methodList, String targetClassLocalPath,
                        String targetClassPackage) {
                return this.serializedObjectAccessOutputClass.writeClassFile(methodList, targetClassLocalPath,
                                targetClassPackage);
        }
}
