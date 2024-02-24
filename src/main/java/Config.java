public class Config {
  static final String classPrompt = """
You are a senior developer at a large financial corporation.
Given the below Java class with the name below, delimited by <definition>:

<definition>
%s
<definition>

And list of its methods below, delimited by <methods>:

<methods>
%s
<methods>

Infer what the class is doing,
Use that information to write Javadoc comment for the given class.
Do not include any information about the class that is not already present in the code.
Do not include its method declaration in the Javadoc comment.
Do not leave generic information in the Javadoc comment.""";
        
  static final String methodPrompt = """
You are a senior developer at a large financial corporation.
Given the below Java method delimited by <code>:

<code>
%s
<code>

Write Javadoc comment for the given method.""";
}
