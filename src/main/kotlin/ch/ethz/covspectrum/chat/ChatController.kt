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
    fun sendMessage(
        @PathVariable id: String,
        accessKey: String,
        @RequestBody content: String
    ): ResponseEntity<ChatSystemMessage> {
        // Only allow current conversations (i.e., stored in the in-memory storage to be updated)
        val chatConversation = currentConversationsService.getConversation(id)
        chatConversation ?: return ResponseEntity(HttpStatus.GONE)

        // User authentication
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        if (userId != chatConversation.owner) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        // Generate response message
        var openAITotalTokens: Int? = null
        val responseMessage = try {
            val response = openAIClient.chatForSql(
                listOf(
                    OpenAIChatRequest.Message("user", content)
                )
            )
            openAITotalTokens = response.usage.total_tokens
            val responseMessageContent = response.choices[0].message.content
            val responseParsed = openAIClient.parseSqlResponseText(responseMessageContent)

            if (responseParsed?.sql == null || responseParsed.error != null) {
                var message = "Sorry, I am not able to answer the question."
                if (responseParsed?.error != null) {
                    message += " ${responseParsed.error}"
                }
                ChatSystemMessage(null, message, null, null, null)
            } else {
                val internal = ChatSystemMessage.Internal(responseParsed.sql)
                try {
                    val data = lapisClient.execute(responseParsed.sql)

                    // Now that ChatGPT explain the query.
                    val responseMessageContent2 = try {
                        val response2 = openAIClient.chatForExplanation(responseParsed.sql)
                        openAITotalTokens += response2.usage.total_tokens
                        response2.choices[0].message.content
                    } catch (e: Exception) {
                        // Too bad, explanation did not work
                        // TODO Log it so that we can investigate
                        null
                    }

                    ChatSystemMessage(null, "Here are the results:", data, responseMessageContent2, internal)
                } catch (e: Exception) {
                    ChatSystemMessage(null, "Sorry, I am not able to answer the question.", null, null, internal)
                }
            }
        } catch (e: Exception) {
            ChatSystemMessage(null, "Sorry, I am not able to answer the question.", null, null, null)
        }

        // Format response as JSON
        val responseJson = objectMapper.writeValueAsString(responseMessage)!!

        // Store request and response in the in-memory storage
        currentConversationsService.addMessageToConversation(id, ChatUserMessage(content))
        currentConversationsService.addMessageToConversation(id, responseMessage)

        // Write to persistent database if the message should be logged
        if (chatConversation.toBeLogged) {
            val messageId = chatService.addChatMessagePair(id, content, responseJson, openAITotalTokens ?: 0)
            responseMessage.id = messageId
            // TODO Write OpenAI interaction to chat_openai_log
        }

        return ResponseEntity(responseMessage, HttpStatus.OK)
    }

    @PostMapping("/conversation/{conversationId}/message/{messageId}/rateUp")
    fun rateUpMessage(
        @PathVariable conversationId: String,
        @PathVariable messageId: Int,
        accessKey: String
    ): ResponseEntity<Void> {
        return rateMessage(conversationId, messageId, accessKey, true)
    }

    @PostMapping("/conversation/{conversationId}/message/{messageId}/rateDown")
    fun rateDownMessage(
        @PathVariable conversationId: String,
        @PathVariable messageId: Int,
        accessKey: String
    ): ResponseEntity<Void> {
        return rateMessage(conversationId, messageId, accessKey, false)
    }

    fun rateMessage(conversationId: String, messageId: Int, accessKey: String, up: Boolean): ResponseEntity<Void> {
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)

        // Only allow current conversations (i.e., stored in the in-memory storage to be updated)
        val chatConversation = currentConversationsService.getConversation(conversationId)
        chatConversation ?: return ResponseEntity(HttpStatus.GONE)

        // The message must be part of the specified conversation
        val message = chatConversation.messages.find { it is ChatSystemMessage && it.id == messageId }
        message ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        // Only possible if the conversation is logged
        if (!chatConversation.toBeLogged) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        chatService.rateChatMessage(messageId, up)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/conversation/{conversationId}/message/{messageId}/comment")
    fun commentMessage(
        @PathVariable conversationId: String,
        @PathVariable messageId: Int,
        accessKey: String,
        @RequestBody comment: String
    ): ResponseEntity<Void> {
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)

        // Only allow current conversations (i.e., stored in the in-memory storage to be updated)
        val chatConversation = currentConversationsService.getConversation(conversationId)
        chatConversation ?: return ResponseEntity(HttpStatus.GONE)

        // The message must be part of the specified conversation
        val message = chatConversation.messages.find { it is ChatSystemMessage && it.id == messageId }
        message ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        // Only possible if the conversation is logged
        if (!chatConversation.toBeLogged) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        // If comment is empty/contains only white spaces, just ignore it
        if (comment.trim().isEmpty()) {
            return ResponseEntity(HttpStatus.OK)
        }

        chatService.commentMessage(messageId, comment)
        return ResponseEntity(HttpStatus.OK)
    }

}
