package org.instrumentation;

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

public class SerializedObjectAccessClassIntrumentation extends ObjectSerializerClassIntrumentation{

  public SerializedObjectAccessClassIntrumentation(String targetMethod, String packageName){
    super(targetMethod, packageName);
  }

  @Override
  public boolean undoTransformations(File file){
    try{
      final CompilationUnit cu = getCompilationUnitForFile(file);
      undoChanges(cu);
      removeImport(cu, "SerializedObjectSupporter");
      saveTransformations(file, cu);
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public boolean addSupporterClassAsField(File file){
    try {
      final CompilationUnit cu = getCompilationUnitForFile(file);

      createNewImportDeclaration(cu, "SerializedObjectSupporter");
      transform(cu);
      saveTransformations(file, cu);

      return true;
    }catch (Exception e){
      e.printStackTrace();
    }

    return false;
  }

  private FieldDeclaration createNewField(AST ast){
    //AST ast = cu.getAST();
    VariableDeclarationFragment variableDeclarationFragment = ast.newVariableDeclarationFragment();
    variableDeclarationFragment.setName(ast.newSimpleName("serializedObjectSupporter"));
    FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(variableDeclarationFragment);
    SimpleType simpleType = ast.newSimpleType(ast.newName("SerializedObjectSupporter"));
    ast.newName("SerializedObjectSupporter");
    //TypeDeclaration simpleType = ast.newTypeDeclaration();
    //simpleType.setName(ast.newSimpleName("SerializedObjectSupporter"));
    //simpleType.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    fieldDeclaration.setType(simpleType);
    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    ConstructorInvocation constructorInvocation = ast.newConstructorInvocation();
    constructorInvocation.arguments().add(ast.newName("SerializedObjectSupporter"));
    //ExpressionStatement expressionStatement = ast.newExpressionStatement(constructorInvocation);
    //constructorInvocation.arguments().add(fieldDeclaration);
    /**
     * Block body = ast.newBlock();
     *     CatchClause catchClause = ast.newCatchClause();
     *     SingleVariableDeclaration exDecl = ast.newSingleVariableDeclaration();
     *     exDecl.setType(ast.newSimpleType(ast.newSimpleName("Exception")));
     *     exDecl.setName(ast.newSimpleName("ex"));
     *     catchClause.setException(exDecl);
     *     Block bodyCatch = ast.newBlock();
     *     String auxCatch = exDecl.getName().getIdentifier();
     *     MethodInvocation methodInvocationCatch = ast.newMethodInvocation();
     *     SimpleName simpleNameCatch = ast.newSimpleName(auxCatch);
     *     methodInvocationCatch.setExpression(simpleNameCatch);
     *     methodInvocationCatch.setName(ast.newSimpleName("printStackTrace"));
     *     ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocationCatch);
     *     bodyCatch.statements().add(expressionStatement);
     *     catchClause.setBody(bodyCatch);*/
    //cu.get*/
    /*TypeDeclaration parentType = (TypeDeclaration) fieldDeclaration.getParent();
    int lastFieldIdx = parentType.getFields().length - 1;
    parentType.getFields();
    FieldDeclaration lastFieldInParent = parentType.getFields()[lastFieldIdx];
    boolean isLastFieldDecl = lastFieldInParent.equals(fieldDeclaration);*/
    return fieldDeclaration;
  }

  @Override
  protected void transform(final CompilationUnit cu) {

    cu.accept(new ASTVisitor() {

      public boolean visit(TypeDeclaration node) {
        if (!node.isInterface()){
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
        if (!node.isInterface()){
          for(FieldDeclaration field: node.getFields()){
            if(field.fragments().get(0).toString().equals("serializedObjectSupporter")){
              node.bodyDeclarations().remove(field);
            }
          }
        }
        return true;
      }

    });

  }

}
