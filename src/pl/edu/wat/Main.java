package pl.edu.wat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

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

public class Main { 
  public static void main(String[] args) throws IOException {
    final String fileName = "src/Class.java";
    final String alteredFileName = "src/ClassAltered.java";
    CompilationUnit cu;
    try (FileInputStream in = new FileInputStream(fileName)) {
      cu = JavaParser.parse(in);
    }

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
}
