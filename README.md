# Document Magic

## What does it do
It automatically generates Javadoc for a Java project. It adds Javadoc to classes and methods that doesn't have them already.

## How to use
- Install ollama on port 11434 (default ollama port)
- Pull the model `ollama pull codellama`
- Then run `mvn package`
- Run the jar with the path to your `src` directory as parameter

We assume that you already have git on the target directory. This will allow you to review changes this application made before accepting them.

## Libraries
- Langchain4j
- JavaParser

## Future development
- Support other languages by using ANTLR
