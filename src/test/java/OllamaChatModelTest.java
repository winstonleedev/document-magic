import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class OllamaChatModelTest {

    /**
     * The first time you run this test, it will download a Docker image with Ollama and a model.
     * It might take a few minutes.
     * <p>
     * This test uses modified Ollama Docker images, which already contain models inside them.
     * All images with pre-packaged models are available here: https://hub.docker.com/repositories/langchain4j
     * <p>
     * However, you are not restricted to these images.
     * You can run any model from https://ollama.ai/library by following these steps:
     * 1. Run "docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama"
     * 2. Run "docker exec -it ollama ollama run mistral" <- specify the desired model here
     */

    static String MODEL_NAME = "llama2:chat"; // try "mistral", "llama2", "codellama", "phi" or "tinyllama"
    static String HOST = "localhost";
    static Integer PORT = 11434;
    static String url = String.format("http://%s:%d/api/generate/", HOST, PORT);

    @Test
    void simple_example() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(MODEL_NAME)
                .build();

        String prompt = """
            You are a senior developer at a large financial corporation.
            You are in charge of writing documentation for a project.
            This project test the performance of chat language models.
            Given the below Java code block delimited by <code>
            Write Javadoc for the given code block.
        """;
        
        String code = """
            void json_output_example() {

                ChatLanguageModel model = OllamaChatModel.builder()
                        .baseUrl(url)
                        .modelName(MODEL_NAME)
                        .format("json")
                        .build();

                String json = model.generate("Give me a JSON with 2 fields: name and age of a John Doe, 42");

                System.out.println(json);
            }
        """;

        String answer = model.generate(String.format("%s\n<code>\n%s\n<code>\n", prompt, code));

        System.out.println(answer);
    }
}
