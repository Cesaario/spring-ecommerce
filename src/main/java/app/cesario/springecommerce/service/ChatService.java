package app.cesario.springecommerce.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ChatService {

    private final HashMap<String, List<ChatMessage>> historicoContexto = new HashMap<>();

    private final static String IDENTIFICADOR_ENCERRAMENTO_CARRINHO = "[COMPRA_ENCERRADA]";
    private final static String CONTEXTO_BASE = "Você é um chatbot para atendimento ao cliente de uma loja de varejo online, o nome da Loja é GPT Varejo Online. Use o mínimo de palavras possíveis nas suas respostas.\n" +
            "\n" +
            "Os produtos disponiveis são eletronicos de todos os tipos, Todos os produtos podem ser gerados por voce, com todos os dados, por exemplo, marca modelo, dimensãoes e etc. Os preços serão gerados na média nacional na moeda real. O cliente poderá adicionar itens no seu carrinho e finalizá-lo quando estiver satisfeito.\n" +
            "\n" +
            "Quando o usuário tentar finalizar a sua compra, adicione " + IDENTIFICADOR_ENCERRAMENTO_CARRINHO + " ao final da mensagem.";
    @Value("${OPEN_AI_TOKEN}")
    private String OPEN_AI_TOKEN;

    private OpenAiService openAiService;

    @PostConstruct
    public void iniciarApiOpenAI() {
        openAiService = new OpenAiService(OPEN_AI_TOKEN, Duration.ofSeconds(60));
    }

    public String conversar(String mensagem, String identificador) {

        var contexto = obterContextoUsuario(identificador);
        contexto.add(new ChatMessage(ChatMessageRole.USER.value(), mensagem));

        var completionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(contexto)
                .n(1)
                .stream(false)
                .build();

        var resposta = openAiService.createChatCompletion(completionRequest).getChoices().get(0).getMessage().getContent();

        contexto.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), resposta));

        atualizarContexto(identificador, contexto);
        verificarEncerramentoDaCompra(identificador, resposta);

        return resposta;
    }

    private void verificarEncerramentoDaCompra(String identificador, String resposta) {
        if(resposta.contains(IDENTIFICADOR_ENCERRAMENTO_CARRINHO))
            historicoContexto.put(identificador, null);
    }

    private List<ChatMessage> obterContextoUsuario(String identificador) {
        var contextoAtual = historicoContexto.get(identificador);
        if (contextoAtual == null) {
            var contextoBase = new ArrayList<ChatMessage>();
            contextoBase.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), CONTEXTO_BASE));
            return contextoBase;
        }
        return contextoAtual;
    }

    private void atualizarContexto(String identificador, List<ChatMessage> novoContexto) {
        historicoContexto.put(identificador, novoContexto);
    }
}
