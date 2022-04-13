package org.instrumentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;

public class ObjectSerializerClassIntrumentation {
  protected String packageName;
  protected String targetMethod;
  protected List<String> targetClasses;

  public ObjectSerializerClassIntrumentation(String targetMethod, String packageName){
    this.packageName = packageName;
    this.targetMethod = targetMethod;
    this.targetClasses = new ArrayList<>();
  }

  public String getPackageName() {
    return packageName;
  }

  public String getTargetMethod() {
    return targetMethod;
  }

  public boolean undoTransformations(File file){
    try{
      final CompilationUnit cu = getCompilationUnitForFile(file);

      undoChanges(cu);
      removeImport(cu, "ObjectSerializerSupporter");

      saveTransformations(file, cu);
      return true;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public boolean removeImport(CompilationUnit cu, String importedClass){
    try{
      int i = 0;
      for (Object importDeclaration: cu.imports()){
        if(importDeclaration instanceof ImportDeclaration &&
            ((ImportDeclaration) importDeclaration).getName().toString().contains(importedClass)){
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

  protected void createNewImportDeclaration(CompilationUnit cu, String serializedObjectSupporter) {
    AST ast = cu.getAST();
    ImportDeclaration id = ast.newImportDeclaration();
    String classToImport = getClassPathToImport(serializedObjectSupporter);
    id.setName(ast.newName(classToImport.split("\\.")));
    cu.imports().add(id);
  }

  protected String getClassPathToImport(String className) {
    return this.packageName.equals("") ? className
        : this.packageName + "."+className;
  }

  public boolean runTransformation(File file) throws IOException {
    try {
      final CompilationUnit cu = getCompilationUnitForFile(file);

      transform(cu);
      createNewImportDeclaration(cu, "ObjectSerializerSupporter");

      saveTransformations(file, cu);

      return true;
    }catch (Exception e){
      e.printStackTrace();
    }

    return false;
  }

  protected void saveTransformations(File file, CompilationUnit cu) throws IOException {
    FileWriter fooWriter = new FileWriter(file, false); // true to append
    fooWriter.write(cu.toString());
    fooWriter.close();
  }

  protected CompilationUnit getCompilationUnitForFile(File file) throws IOException {
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

  protected void transform(final CompilationUnit cu) {

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

  protected void undoChanges(final CompilationUnit cu) {

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
      getTargetClasses(node);
    }
  }

  private void getTargetClasses(MethodDeclaration node){
    for (Object parameter: node.parameters()) {
      SingleVariableDeclaration parameterVariable = (SingleVariableDeclaration) parameter;
      this.targetClasses.add(parameterVariable.getType().toString());
    }
  }

  private void removeMethodCallForSerialization(MethodDeclaration node){
    if (isNodeTheTargetMethod(node)){
      removeAstNode(node);
    }
  }

  private boolean isNodeTheTargetMethod(MethodDeclaration node) {
    if (this.targetMethod.contains("(")){
      if (node.getName().toString().equals(this.targetMethod.split("\\(")[0])) {
        String[] parameters = this.targetMethod.split(this.targetMethod.split("\\(")[0])[1].replace("(","").replace(")","").split(",");
        List parameterMethod = node.parameters();
        if (parameterMethod.size() == parameters.length) {
          int i = 0;

          while (i < parameters.length) {
            SingleVariableDeclaration aux2 = (SingleVariableDeclaration) parameterMethod.get(i);
            if (!parameters[i].contains(aux2.getType().toString())) {
              return false;
            }
            i++;
          }
          return true;
        }
      }
      return false;
    }else {
      return node.getName().toString().equals(this.targetMethod);
    }
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

    if (!isMethodStatic(nodeMethod)){
      ThisExpression expression = ast.newThisExpression();
      body.statements().add(ast.newExpressionStatement(getMethodInvocation("serializeWithXtreamOut",
          expression, ast)));
    }

    for(ASTNode parameter: parameters){
      SingleVariableDeclaration aux = (SingleVariableDeclaration) parameter;
      SimpleName simpleName = ast.newSimpleName(aux.getName().getIdentifier());
      body.statements().add(ast.newExpressionStatement(getMethodInvocation(
          "serializeWithXtreamOut", simpleName, ast)));
    }

    result.setBody(body);
    result.catchClauses().add(catchClause);
    if (nodeMethod.isConstructor()) {
      nodeMethod.getBody().statements().add(nodeMethod.getBody().statements().size(), result);
    }else {
      nodeMethod.getBody().statements().add(0, result);
    }

    return nodeMethod;
  }

  private boolean isMethodStatic(MethodDeclaration methodDeclaration){
    for (Object modifier: methodDeclaration.modifiers()){
      if (modifier instanceof Modifier && ((Modifier) modifier).isStatic()){
        return true;
      }
    }
    return false;
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

  public List<String> getTargetClasses(){
    return this.targetClasses;
  }

}
