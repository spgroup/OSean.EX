package org.instrumentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.w3c.dom.NodeList;

public class JavaClassIntrumentation {
  private String packageName;
  private String targetMethod;

  public JavaClassIntrumentation(String targetMethod, String packageName){
    this.packageName = packageName;
    this.targetMethod = targetMethod;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getTargetMethod() {
    return targetMethod;
  }

  public final boolean undoTransformations(File file){
    try{
      final CompilationUnit cu = getCompilationUnitForFile(file);

      undoChanges(cu);
      removeImport(cu);

      FileWriter fooWriter = new FileWriter(file, false); // true to append
      fooWriter.write(cu.toString());
      fooWriter.close();
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  private boolean removeImport(CompilationUnit cu){
    try{
      int i = 0;
      for (Object importDeclaration: cu.imports()){
        if(importDeclaration instanceof ImportDeclaration &&
            ((ImportDeclaration) importDeclaration).getName().toString().contains("ObjectSerializerSupporter")){
          cu.imports().remove(i);
          return true;
        }
        i++;
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public final boolean runTransformation(File file) throws IOException {
    try {
      final CompilationUnit cu = getCompilationUnitForFile(file);

      transform(cu);
      AST ast = cu.getAST();
      ImportDeclaration id = ast.newImportDeclaration();
      String classToImport = this.packageName.equals("") ? "ObjectSerializerSupporter"
          : this.packageName + ".ObjectSerializerSupporter";
      id.setName(ast.newName(classToImport.split("\\.")));
      cu.imports().add(id);

      FileWriter fooWriter = new FileWriter(file, false); // true to append
      fooWriter.write(cu.toString());
      fooWriter.close();

      return true;
    }catch (Exception e){
      e.printStackTrace();
    }

    return false;
  }

  private CompilationUnit getCompilationUnitForFile(File file) throws IOException {
    final String str = FileUtils.readFileToString(file);
    org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(str);

    ASTParser parser = ASTParser.newParser(AST.JLS8);
    Map options = JavaCore.getOptions(); // New!
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options); // New!
    parser.setCompilerOptions(options);

    parser.setSource(document.get().toCharArray());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    return (CompilationUnit) parser.createAST(null);
  }

  private void transform(final CompilationUnit cu) {

    cu.accept(new ASTVisitor() {

      public boolean visit(MethodDeclaration node) {
        addMethodCallForSerialization(node);
        return true;
      }

      /*public boolean visit(ImportDeclaration node) {
        return true;
      }

      public boolean visit(MethodInvocation node) {
        return true;
      }

      public boolean visit(TryStatement node) {
        return true;
      }*/

    });

  }

  private void undoChanges(final CompilationUnit cu) {

    cu.accept(new ASTVisitor() {

      public boolean visit(MethodDeclaration node) {
        removeMethodCallForSerialization(node);
        return true;
      }

      public boolean visit(ImportDeclaration node) {
        return true;
      }

    });

  }

  private void addMethodCallForSerialization(MethodDeclaration node){
    if (isNodeTheTargetMethod(node)){
      createAstNodeWithMethodBody(node);
    }
  }

  private void removeMethodCallForSerialization(MethodDeclaration node){
    if (isNodeTheTargetMethod(node)){
      removeAstNode(node);
    }
  }

  private boolean isNodeTheTargetMethod(MethodDeclaration node) {
    return !node.isConstructor() && node.getName().toString().equals(this.targetMethod);
  }


  private MethodInvocation getMethodInvocation(String methodName, ASTNode parameter, AST targetMethod){
    MethodInvocation methodInvocation = targetMethod.newMethodInvocation();
    methodInvocation.setExpression(targetMethod.newSimpleName("ObjectSerializerSupporter"));
    methodInvocation.setName(targetMethod.newSimpleName(methodName));

    methodInvocation.arguments().add(0, parameter);
    return methodInvocation;
  }

  private ASTNode createAstNodeWithMethodBody(MethodDeclaration nodeMethod) {

    AST ast = nodeMethod.getAST();
    TryStatement result = ast.newTryStatement();
    Block body = ast.newBlock();
    CatchClause catchClause = ast.newCatchClause();
    SingleVariableDeclaration exDecl = ast.newSingleVariableDeclaration();
    exDecl.setType(ast.newSimpleType(ast.newSimpleName("Exception")));
    exDecl.setName(ast.newSimpleName("ex"));
    catchClause.setException(exDecl);
    Block bodyCatch = ast.newBlock();
    String auxCatch = exDecl.getName().getIdentifier();
    MethodInvocation methodInvocationCatch = ast.newMethodInvocation();
    SimpleName simpleNameCatch = ast.newSimpleName(auxCatch);
    methodInvocationCatch.setExpression(simpleNameCatch);
    methodInvocationCatch.setName(ast.newSimpleName("printStackTrace"));
    ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocationCatch);
    bodyCatch.statements().add(expressionStatement);
    catchClause.setBody(bodyCatch);

    List<ASTNode> parameters = nodeMethod.parameters();
    ThisExpression expression = ast.newThisExpression();

    body.statements().add(ast.newExpressionStatement(getMethodInvocation("serializeWithXtreamOut",
        expression, ast)));

    for(ASTNode parameter: parameters){
      SingleVariableDeclaration aux = (SingleVariableDeclaration) parameter;
      SimpleName simpleName = ast.newSimpleName(aux.getName().getIdentifier());
      body.statements().add(ast.newExpressionStatement(getMethodInvocation(
          "serializeWithXtreamOut", simpleName, ast)));
    }

    result.setBody(body);
    result.catchClauses().add(catchClause);
    nodeMethod.getBody().statements().add(0, result);

    return nodeMethod;
  }

  private boolean removeAstNode(MethodDeclaration nodeMethod) {
    try{
      int i = 0;
      for(Object statement: nodeMethod.getBody().statements()){
        if(statement instanceof TryStatement){
          for (Object statementTryCatch: ((TryStatement) statement).getBody().statements()){
            if (statementTryCatch instanceof ExpressionStatement &&
                ((ExpressionStatement) statementTryCatch).getExpression().toString().contains("ObjectSerializerSupporter")){
              nodeMethod.getBody().statements().remove(i);
              return true;
            }
          }
          i++;
        }
        i++;
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

}
