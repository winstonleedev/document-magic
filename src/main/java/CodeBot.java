import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

public class CodeBot {

  static String MODEL_NAME = "llama2:chat"; // try "mistral", "llama2", "codellama", "phi" or "tinyllama"
  static String HOST = "localhost";
  static Integer PORT = 11434;
  static String url = String.format("http://%s:%d/api/generate/", HOST, PORT);
  static boolean isDebug = false;

  static ChatLanguageModel model = OllamaChatModel.builder()
      .baseUrl(url)
      .modelName(MODEL_NAME)
      .build();

  public static String extractCommentBlock(String input) {
    Pattern pattern = Pattern.compile("\\/\\*([\\S\\s]+)\\*\\/");
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      return matcher.group(0);
    } else {
      return "";
    }
  }

  public static String extractCommentText(String input) {
    String[] lines = input.split("\n");
    List<String> resultLines = new ArrayList<>();
    for (int i = 1; i < lines.length - 1; i++) {
      String currentLine = lines[i];
      if (currentLine.length() >= 3) {
        resultLines.add(lines[i].substring(2).trim());
      }
    }
    return String.join("\n", resultLines);
  }

  public static void doLog(String message) {
    if (isDebug) {
      doLog(message);
    }
  }

  public static String commentOnMethod(String codeBlock) {

    String prompt = """
        You are a senior developer at a large financial corporation.
        You are in charge of writing documentation for a project.
        This project test the performance of chat language models.
        Given the below Java method delimited by \n<code>\n
        Write Javadoc comment for the given method""";

    String answer = "";
    while (answer.isEmpty()) {
      answer = model.generate(String.format("%s\n<code>\n%s\n<code>\n", prompt, codeBlock));

      doLog("Result of commentCode:\n" + answer);

      answer = extractCommentBlock(answer);

      doLog("Result of extractComments:\n" + answer);

      answer = extractCommentText(answer);

      doLog("Result of extractCommentText:\n" + answer);

    }

    return answer;
  }

  public static String commentOnClass(String codeBlock) {

    String prompt = """
        You are a senior developer at a large financial corporation.
        You are in charge of writing documentation for a project.
        This project test the performance of chat language models.
        Given the below Java class with the list of methods delimited by \n<code>\n
        Write Javadoc comment for the given class""";

    String answer = "";
    while (answer.isEmpty()) {
      answer = model.generate(String.format("%s\n<code>\n%s\n<code>\n", prompt, codeBlock));

      doLog("Result of commentCode:\n" + answer);

      answer = extractCommentBlock(answer);
      doLog("Result of extractComments:\n" + answer);

      answer = extractCommentText(answer);
      doLog("Result of extractCommentText:\n" + answer);

    }

    return answer;
  }

  /**
   * Simple visitor implementation for visiting MethodDeclaration nodes.
   */
  private static class MethodVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
      doLog("Processing class: " + n.getName());

      // For methods without Javadoc
      if (n.getJavadocComment().isEmpty()) {
        List<MethodDeclaration> methodList = n.getMethods();
        String methods = methodList
            .stream()
            .map(MethodDeclaration::getDeclarationAsString)
            .collect(Collectors.joining("\n"));

        String javadoc = commentOnClass(methods);
        n.setComment(new JavadocComment(javadoc).parse().toComment());
        doLog("Produced Javadoc:\n" + n.getComment().toString());

      }
      super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration method, Void arg) {
      // here you can access the attributes of the method.
      // this method will be called for all methods in this
      // CompilationUnit, including inner class methods
      doLog("Processing method: " + method.getName());

      // For methods without Javadoc
      if (method.getJavadocComment().isEmpty() && method.getBody().isPresent()) {
        String declaration = method.getDeclarationAsString();
        doLog("Method declaration:\n" + declaration);

        String body = method.getBody().get().toString();
        doLog("Method body:\n" + body);

        String javadoc = commentOnMethod(String.format("%s\n%s", declaration, body));
        method.setComment(new JavadocComment(javadoc).parse().toComment());
        doLog("Produced Javadoc:\n" + method.getComment().toString());

      }
      super.visit(method, arg);
    }
  }

  public static void main(String[] args) {

    Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

    // SourceRoot is a tool that read and writes Java files from packages on a
    // certain root directory.
    // In this case the root directory is found by taking the root from the current
    // Maven module,
    // with src/main/resources appended.
    SourceRoot sourceRoot = new SourceRoot(Paths.get(args[0]));

    // Our sample is in the root of this directory, so no package name.
    try {
      List<ParseResult<CompilationUnit>> result = sourceRoot.tryToParse();

      for (ParseResult<CompilationUnit> parseResult : result) {
        var rr = parseResult.getResult();
        if (rr.isEmpty()) {
          continue;
        }

        rr.get().accept(new MethodVisitor(), null);
      }
    } catch (IOException e) {
      // Error parsing
      Log.error(e);
    }

    // This saves all the files we just read to an output directory.
    sourceRoot.saveAll(Paths.get(args[1]));
  }
}
