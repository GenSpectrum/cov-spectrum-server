package ch.ethz.covspectrum.chat


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
    private val currentConversationsService: CurrentConversationsService,
    private val objectMapper: ObjectMapper
) {

    private val openAIClient = OpenAIClient(System.getenv("COV_SPECTRUM_OPENAI_SECRET"))
    private val lapisClient =
        LapisSqlClient("https://lapis.cov-spectrum.org/gisaid/v1", System.getenv("COV_SPECTRUM_LAPIS_SECRET"))

    @GetMapping("/authenticate")
    fun checkAuthentication(accessKey: String): ChatAuthenticationResponse {
        val userId = chatService.getUserId(accessKey)
        return ChatAuthenticationResponse(userId != null)
    }

    @PostMapping("/createConversation")
    fun createConversation(accessKey: String, toBeLogged: Boolean): ResponseEntity<String> {
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val conversation = chatService.createConversation(userId, toBeLogged)
        currentConversationsService.addConversation(conversation)
        return ResponseEntity(conversation.id, HttpStatus.OK)
    }

    @PostMapping("/conversation/{id}/sendMessage")
    fun sendMessage(@PathVariable id: String, accessKey: String, @RequestBody content: String): ResponseEntity<ChatSystemMessage> {
        // Only allow current conversations (i.e., stored in the in-memory storage to be updated)
        val chatConversation = currentConversationsService.getConversation(id)
        chatConversation ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        // User authentication
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        if (userId != chatConversation.owner) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        // Generate response message
        var openAITotalTokens: Int? = null
        val responseMessage = try {
            val response = openAIClient.chat(
                listOf(
                    OpenAIChatRequest.Message("user", content)
                )
            )
            openAITotalTokens = response.usage.total_tokens
            val responseMessageContent = response.choices[0].message.content
            val sql = openAIClient.extractSql(responseMessageContent)
            if (sql == null) {
                val errorReason = openAIClient.extractErrorReason(responseMessageContent)
                var message = "Sorry, I am not able to answer the question."
                if (errorReason != null) {
                    message += " $errorReason"
                }
                ChatSystemMessage(message,null,null)
            } else {
                val data = lapisClient.execute(sql)
                ChatSystemMessage(
                    "Here are the results:",
                    data,
                    ChatSystemMessage.Internal(
                        sql
                    )
                )
            }
        } catch (e: Exception) {
            ChatSystemMessage(
                "Sorry, I am not able to answer the question.",
                null,
                null
            )
        }

        // Format response as JSON
        val responseJson = objectMapper.writeValueAsString(responseMessage)!!

        // Store request and response in the in-memory storage
        currentConversationsService.addMessageToConversation(id, ChatUserMessage(content))
        currentConversationsService.addMessageToConversation(id, responseMessage)

        // Write to persistent database if the message should be logged
        if (chatConversation.toBeLogged) {
            chatService.addChatMessagePair(id, content, responseJson, openAITotalTokens ?: 0)
            // TODO Write OpenAI interaction to chat_openai_log
        }

        return ResponseEntity(responseMessage, HttpStatus.OK)
    }

}
