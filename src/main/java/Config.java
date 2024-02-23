public class Config {
  static final String projectDescription = "test the performance of chat language models";

  static final String classPrompt = """
        You are a senior developer at a large financial corporation.
        You are in charge of writing documentation for a project.
        This project """ + projectDescription + """
        Given the below Java class with the list of methods delimited by \n<code>\n
        Write Javadoc comment for the given class""";
        
  static final String methodPrompt = """
        You are a senior developer at a large financial corporation.
        You are in charge of writing documentation for a project.
        This project """ + projectDescription + """
        Given the below Java method delimited by \n<code>\n
        Write Javadoc comment for the given method""";
}
