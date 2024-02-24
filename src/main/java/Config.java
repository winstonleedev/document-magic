public class Config {
  static final String classPrompt = """
        You are a senior developer at a large financial corporation.
        Given the below Java class with the name delimited by <definition>
        and list of methods delimited by <methods>
        Infer what the class is doing
        Write Javadoc comment for the given class""";
        
  static final String methodPrompt = """
        You are a senior developer at a large financial corporation.
        Given the below Java method delimited by <code>
        Write Javadoc comment for the given method""";
}
