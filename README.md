# Document Magic

## What does it do
It automatically generates Javadoc for a Java project. It adds Javadoc to classes and methods that doesn't have them already.

## How to use
```
mvn package
mvn run <target directory>
```

We assume that you already have git on the target directory. This will allow you to review changes this application made before accepting them.

## Libraries
- Langchain4j
- JavaParser

## Future development
- Support other languages by using ANTLR
