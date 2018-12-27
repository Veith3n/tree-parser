package pl.edu.wat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

  private static final List<String> NUMERIC_TYPES = Arrays.asList("long", "int", "integer", "double", "float");

  public static void main(String[] args) throws IOException {
    final String fileName = "src/Class.java";
    final String alteredFileName = "src/ClassAltered.java";
    CompilationUnit cu;
    try (FileInputStream in = new FileInputStream(fileName)) {
      cu = JavaParser.parse(in);
    }

    List<FieldDeclaration> attributes = cu.getChildNodesByType(FieldDeclaration.class);
    initializeNumericAttributes(attributes);

    cu.getClassByName("Class").get().setName("ClassAltered");

    try (FileWriter output = new FileWriter(new File(alteredFileName), false)) {
      output.write(cu.toString());
    }

    File[] files = {new File(alteredFileName)};
    String[] options = {"-d", "out//production//Synthesis"};

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      Iterable<? extends JavaFileObject> compilationUnits =
          fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
      compiler.getTask(null, fileManager, diagnostics, Arrays.asList(options), null, compilationUnits).call();

      diagnostics.getDiagnostics().forEach(d -> System.out.println(d.getMessage(null)));
    }
  }

  private static void initializeNumericAttributes(List<FieldDeclaration> attributes) {
    attributes.stream().
        filter(attribute -> NUMERIC_TYPES.contains(getAttributeName(attribute))).
        forEach(Main::initializeAttribute);
  }

  private static void initializeAttribute(FieldDeclaration attribute) {
    if (!isAttributeInitialized(attribute)) {
      attribute.getVariables().get(0).setInitializer(generateValue(attribute));
    }
  }

  private static boolean isAttributeInitialized(FieldDeclaration node) {
    return node.getVariables().get(0).getInitializer().isPresent();
  }

  private static String generateValue(FieldDeclaration attribute) {
    return needLetterLiteral(getAttributeName(attribute)) ? ("0" + letterLiteral(getFirstLetter(attribute))) : "0";
  }

  private static boolean needLetterLiteral(String name) {
    List<String> whitelist = Arrays.asList("float", "double", "long");
    return whitelist.contains(name);
  }

  private static String letterLiteral(String letter) {
    return letter.equals("l") ? "L" : letter;
  }

  private static String getFirstLetter(FieldDeclaration attribute) {
    return getAttributeName(attribute).substring(0, 1);
  }

  private static String getAttributeName(FieldDeclaration attribute) {
    return attribute.getCommonType().toString().toLowerCase();
  }
}
