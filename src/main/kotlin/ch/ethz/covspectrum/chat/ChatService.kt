package ch.ethz.covspectrum.chat

import ch.ethz.covspectrum.service.DatabaseService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.random.Random

@Service
class ChatService(
    private val databaseService: DatabaseService,
    private val objectMapper: ObjectMapper
) {

    /**
     * Returns user id if the access key is valid. Otherwise, the function returns null.
     */
    fun getUserId(accessKey: String): Int? {
        // TODO That's not very efficient.. but good enough to start with :)
        return getUserInfo(accessKey)?.id
    }

    /**
     * Returns user info if the access key is valid. Otherwise, the function returns null.
     */
    fun getUserInfo(accessKey: String): UserInfo? {
        val sql = """
            select
              cu.id,
              cu.quota_cents as quota,
              sum(cmp.openai_total_tokens) as total_used_token,
              string_agg(distinct cc.id::text, ',') as conversations
            from
              chat_user cu
              left join chat_conversation cc on cu.id = cc.owner
              left join chat_message_pair cmp on cc.id = cmp.conversation
            where access_key = ?
            group by cu.id;
        """.trimIndent()
        databaseService.getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, accessKey)
                statement.executeQuery().use { rs ->
                    if (rs.next()) {
                        val id = rs.getInt("id")
                        val quota = rs.getInt("quota")
                        val totalUsedTokens = rs.getInt("total_used_token")
                        // $0.002 / 1K tokens for gpt-3.5-turbo
                        // https://openai.com/pricing
                        val quotaUsed = floor(0.2 * totalUsedTokens / 1000).toInt()
                        val conversations = rs.getString("conversations")?.split(",") ?: emptyList()
                        return UserInfo(id, quota, quotaUsed, conversations)
                    }
                    return null
                }
            }
        }
    }

    /**
     * Creates a conversation
     */
    fun createConversation(userId: Int, toBeLogged: Boolean): ChatConversation {
        // Generate conversation ID
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z')
        val randomString = (1..7)
            .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
        val conversationId = "$timestamp-$randomString"

        // Create conversation object
        val conversation = ChatConversation(conversationId, userId, LocalDateTime.now(), toBeLogged, mutableListOf())

        // Store in database if permitted
        if (toBeLogged) {
            val sql = """
                insert into chat_conversation (id, owner, creation_timestamp)
                values (?, ?, ?);
            """.trimIndent()
            databaseService.getConnection().use { conn ->
                conn.prepareStatement(sql).use { statement ->
                    statement.setString(1, conversation.id)
                    statement.setInt(2, conversation.owner)
                    statement.setTimestamp(3, Timestamp.valueOf(conversation.creationTimestamp))
                    statement.execute()
                }
            }
        }

        return conversation
    }

    fun addChatMessagePair(conversationId: String, userPrompt: String, responseJson: String, openAITotalTokens: Int) {
        val sql = """
            insert into chat_message_pair (
              conversation,
              response_timestamp,
              user_prompt,
              response_json,
              openai_total_tokens
            )
            values (?, now(), ?, ?::json, ?);
        """.trimIndent()
        databaseService.getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, conversationId)
                statement.setString(2, userPrompt)
                statement.setString(3, responseJson)
                statement.setInt(4, openAITotalTokens)
                statement.execute()
            }
        }
    }
}
