package org.transformations.instrumentation;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClassInstumentationController {
    public ObjectSerializerClassInstrumentation objectSerializerClassInstrumentation;
    public SerializedObjectAccessClassInstrumentation serializedObjectAccessClassInstrumentation;

    public void createObjectSerializerInstrumentation(String targetMethodName, String classPackage) {
        this.objectSerializerClassInstrumentation = new ObjectSerializerClassInstrumentation(
                targetMethodName, classPackage);
    }

    public void createSerializedObjectAccessClassInstrumentation(String targetMethodName, String classPackage) {
        this.serializedObjectAccessClassInstrumentation = new SerializedObjectAccessClassInstrumentation(
                targetMethodName, classPackage);
    }

    public boolean instrumentTargetMethod(File file) throws IOException {
        return this.objectSerializerClassInstrumentation.runTransformation(file);
    }

    public void addTargetClassToTestabilityTransformationsFilesList(String targetClassName) {
        this.objectSerializerClassInstrumentation.getTargetClasses().add(targetClassName);
    }

    public boolean undoInstrumentations(File targetClassFilePath) {
        return this.objectSerializerClassInstrumentation.undoTransformations(targetClassFilePath);
    }

    public boolean addSerializedObjectAccessClassAsField(File targetClassFilePath) {
        return this.serializedObjectAccessClassInstrumentation.addSupporterClassAsField(targetClassFilePath);
    }

    public List<String> getClassesToApplyTestabilityTransformations() {
        return this.objectSerializerClassInstrumentation.getTargetClasses();
    }
}
