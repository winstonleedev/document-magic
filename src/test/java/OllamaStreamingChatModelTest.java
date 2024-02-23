import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CompletableFuture;

@Testcontainers
class OllamaStreamingChatModelTest {

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
     * 2. Run "docker exec -it ollama ollama run llama2" <- specify the desired model here
     */

    static String MODEL_NAME = "llama2:chat"; // try "mistral", "llama2", "codellama" or "phi"
    static String HOST = "localhost";
    static Integer PORT = 11434;

    @Test
    void streaming_example() {

        String url = String.format("http://%s:%d/api/generate/", HOST, PORT);

        StreamingChatLanguageModel model = OllamaStreamingChatModel.builder()
                .baseUrl(url)
                .modelName(MODEL_NAME)
                .temperature(0.5)
                .build();

        String userMessage = "Write a 100-word poem about Java and AI";

        CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();
        model.generate(userMessage, new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                futureResponse.complete(response);
            }

            @Override
            public void onError(Throwable error) {
                futureResponse.completeExceptionally(error);
            }
        });

        futureResponse.join();
    }
}
