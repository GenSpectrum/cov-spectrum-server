package ch.ethz.covspectrum.chat


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
    private val objectMapper: ObjectMapper
) {

    private val openAIClient = OpenAIClient(System.getenv("COV_SPECTRUM_OPENAI_SECRET"))
    private val lapisClient =
        LapisSqlClient("https://lapis.cov-spectrum.org/gisaid/v1", System.getenv("COV_SPECTRUM_LAPIS_SECRET"))

    @GetMapping("/myInfo")
    fun getMyConversations(accessKey: String): ResponseEntity<UserInfo> {
        val userInfo = chatService.getUserInfo(accessKey)
        userInfo ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        return ResponseEntity(userInfo, HttpStatus.OK)
    }

    @PostMapping("/createConversation")
    fun createConversation(accessKey: String): ResponseEntity<Int> {
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val conversationId = chatService.createConversation(userId)
        return ResponseEntity(conversationId, HttpStatus.OK)
    }

    @GetMapping("/conversation/{id}")
    fun getConversation(@PathVariable id: Int, accessKey: String): ResponseEntity<ChatConversation> {
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val chatConversation = chatService.getConversation(id)
        chatConversation ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        if (userId != chatConversation.owner) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
        return ResponseEntity(chatConversation, HttpStatus.OK)
    }

    @PostMapping("/conversation/{id}/sendMessage")
    fun sendMessage(@PathVariable id: Int, accessKey: String, @RequestBody content: String): ResponseEntity<ChatSystemMessage> {
        val userId = chatService.getUserId(accessKey)
        userId ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val chatConversation = chatService.getConversation(id)
        chatConversation ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        if (userId != chatConversation.owner) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        var openAITotalTokens: Int? = null
        val message = try {
            val response = openAIClient.chat(
                listOf(
                    OpenAIChatRequest.Message("user", content)
                )
            )
            openAITotalTokens = response.usage.total_tokens
            val responseMessageContent = response.choices[0].message.content
            val sql = openAIClient.extractSql(responseMessageContent)
            if (sql == null) {
                ChatSystemMessage(
                    "Sorry, I am not able to answer the question.",
                    null,
                    null
                )
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

        val responseJson = objectMapper.writeValueAsString(message)!!
        chatService.addChatMessagePair(id, content, responseJson, openAITotalTokens ?: 0)
        // TODO Write OpenAI interaction to chat_openai_log

        return ResponseEntity(message, HttpStatus.OK)
    }

}
