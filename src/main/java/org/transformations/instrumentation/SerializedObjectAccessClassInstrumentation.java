package org.transformations.instrumentation;

import java.io.File;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class SerializedObjectAccessClassInstrumentation extends ObjectSerializerClassInstrumentation {

  public SerializedObjectAccessClassInstrumentation(String targetMethod, String packageName) {
    super(targetMethod, packageName);
  }

  @Override
  public boolean undoTransformations(File file) {
    try {
      final CompilationUnit cu = getCompilationUnitForFile(file);
      undoChanges(cu);
      removeImport(cu, "SerializedObjectSupporter");
      saveTransformations(file, cu);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean addSupporterClassAsField(File file) {
    try {
      final CompilationUnit cu = getCompilationUnitForFile(file);

      createNewImportDeclaration(cu, "SerializedObjectSupporter");
      transform(cu);
      saveTransformations(file, cu);

      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  private FieldDeclaration createNewField(AST ast) {
    VariableDeclarationFragment variableDeclarationFragment = ast.newVariableDeclarationFragment();
    variableDeclarationFragment.setName(ast.newSimpleName("serializedObjectSupporter"));
    FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(variableDeclarationFragment);
    SimpleType simpleType = ast.newSimpleType(ast.newName("SerializedObjectSupporter"));
    ast.newName("SerializedObjectSupporter");
    fieldDeclaration.setType(simpleType);
    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    ConstructorInvocation constructorInvocation = ast.newConstructorInvocation();
    constructorInvocation.arguments().add(ast.newName("SerializedObjectSupporter"));
    return fieldDeclaration;
  }

  @Override
  protected void transform(final CompilationUnit cu) {

    cu.accept(new ASTVisitor() {

      public boolean visit(TypeDeclaration node) {
        if (!node.isInterface()) {
          FieldDeclaration fieldDeclaration = createNewField(cu.getAST());
          node.bodyDeclarations().add(fieldDeclaration);
        }
        return true;
      }

    });

  }

  @Override
  protected void undoChanges(final CompilationUnit cu) {

    cu.accept(new ASTVisitor() {

      public boolean visit(TypeDeclaration node) {
        if (!node.isInterface()) {
          for (FieldDeclaration field : node.getFields()) {
            if (field.fragments().get(0).toString().equals("serializedObjectSupporter")) {
              node.bodyDeclarations().remove(field);
            }
          }
        }
        return true;
      }

    });

  }

}
