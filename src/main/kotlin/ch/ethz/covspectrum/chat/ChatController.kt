package ch.ethz.covspectrum.chat

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController {

    private val openAIClient = OpenAIClient(System.getenv("COV_SPECTRUM_OPENAI_SECRET"))
    private val lapisClient =
        LapisSqlClient("https://lapis.cov-spectrum.org/gisaid/v1", System.getenv("COV_SPECTRUM_LAPIS_SECRET"))

    @GetMapping("/myConversations")
    fun getMyConversations(accessKey: String) {
        TODO()
    }

    @PostMapping("/createConversation")
    fun createConversation(accessKey: String): Int {
        TODO()
    }

    @GetMapping("/conversation/{id}")
    fun getConversation(@PathVariable id: Int, accessKey: String): ChatConversation {
        TODO()
    }

    @PostMapping("/conversation/{id}/sendMessage")
    fun sendMessage(@PathVariable id: Int, accessKey: String, @RequestBody content: String): ChatSystemMessage {
        try {

            val response = openAIClient.chat(
                listOf(
                    OpenAIChatRequest.Message("user", content)
                )
            )
            val responseMessageContent = response.choices[0].message.content
            val sql = openAIClient.extractSql(responseMessageContent)
            if (sql == null) {
                return ChatSystemMessage(
                    "Sorry, I am not able to answer the question.",
                    null,
                    null
                )
            }

            val data = lapisClient.execute(sql)
            return ChatSystemMessage(
                "Here are the results:",
                data,
                ChatSystemMessage.Internal(
                    sql
                )
            )
        } catch (e: Exception) {
            return ChatSystemMessage(
                "Sorry, I am not able to answer the question.",
                null,
                null
            )
        }
    }

}
