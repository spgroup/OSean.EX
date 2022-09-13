package org.transformations;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.Transformations;
import org.file.ProjectFileSupporter;
import org.util.input.TransformationOption;

public class TestabilityTransformationsController {
    private Set<String> transformedClasses = new HashSet<>();
    private TransformationOption transformationOption;

    public TestabilityTransformationsController(TransformationOption transformationOption) {
        this.transformationOption = transformationOption;
    }

    public void runTestabilityTransformationsForSerializedObjectClasses(HashSet<File> serializedObjectsFiles) {
        for (File serializedObjectFile : serializedObjectsFiles) {
            if (serializedObjectFile != null) {
                runTestabilityTransformations(serializedObjectFile, transformationOption.applyTransformations(),
                        transformationOption.applyFullTransformations());
            }
        }
    }

    public void applyTestabilityTransformationsTargetClasses(ProjectFileSupporter projectFileSupporter,
            List<String> classes) {
        for (String targetClass : classes) {
            File targetClassFile = projectFileSupporter.searchForFileByName(targetClass + ".java");
            if (targetClassFile != null) {
                runTestabilityTransformations(targetClassFile, transformationOption.applyTransformations(),
                        transformationOption.applyFullTransformations());
            }
        }
    }

    public boolean runTestabilityTransformations(File file, boolean applyTransformations, boolean applyFully) {
        if (transformedClasses.contains(file.getPath())) {
            return true;
        }
        System.out.print("Applying Testability Transformations in " + file.getName() + " : ");
        try {
            Transformations.main(new String[] { new String(file.getPath()),
                    String.valueOf(applyTransformations), String.valueOf(applyFully) });
            System.out.println("SUCCESSFUL");
            transformedClasses.add(file.getPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("UNSUCCESSFUL");
        return false;
    }

    public void clearTransformedClassesList() {
        this.transformedClasses.clear();
    }

    public Set<String> getTransformedClasses() {
        return this.transformedClasses;
    }

}
