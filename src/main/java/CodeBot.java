import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.SourceRoot.Callback;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class CodeBot {

  static String MODEL_NAME = "codellama"; // try "mistral", "llama2", "codellama", "phi" or "tinyllama"
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
      // Remove accidental comment terminator
      String currentLine = lines[i]
        .replace("/* implementation */", "")
        .replaceAll("\\*\\/", "").trim();
      if (currentLine.startsWith("*")) {
        resultLines.add(lines[i]
            .substring(1) // Remove "*"
            .trim()); // Remove extra whitespace
      } else {
        resultLines.add(lines[i]
            .trim());
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


    String prompt = Config.methodPrompt;
    String answer = "";
    while (answer.isEmpty()) {
      answer = model.generate(String.format(prompt, codeBlock));

      doLog("Result of commentCode:\n" + answer);

      answer = extractCommentBlock(answer);

      doLog("Result of extractComments:\n" + answer);

      answer = extractCommentText(answer);

      doLog("Result of extractCommentText:\n" + answer);

    }

    return answer;
  }

  public static String commentOnClass(String definition) {


    String prompt = Config.classPrompt;
    String answer = "";
    while (answer.isEmpty()) {
      answer = model.generate(String.format(
          prompt, definition));

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
    public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
      doLog("Processing class: " + clazz.getName());

      // For classes without Javadoc
      if (clazz.getJavadocComment().isEmpty()) {
        
        String javadoc = commentOnClass(clazz.getNameAsString());
        clazz.setComment(new JavadocComment(javadoc).parse().toComment());
        doLog("Produced Javadoc:\n" + clazz.getComment().toString());

      }
      super.visit(clazz, arg);
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
    // certain root directory.o
    SourceRoot sourceRoot = new SourceRoot(Paths.get(args[0]));

    Callback callback = (localPath, absolutePath, parseResult) -> {
      parseResult.getResult().ifPresent(cu -> cu.accept(new MethodVisitor(), null));
      return Callback.Result.SAVE;
    };

    try {
      sourceRoot.parse("", callback);
    } catch (IOException e) {
      // Error parsing
      Log.error(e);
    }
  }
}
