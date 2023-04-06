package ch.ethz.covspectrum.chat

import ch.ethz.covspectrum.service.DatabaseService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import kotlin.math.floor

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
                        val conversations = rs.getString("conversations")?.split(",")?.map { it.toInt() }
                            ?: emptyList()
                        return UserInfo(id, quota, quotaUsed, conversations)
                    }
                    return null
                }
            }
        }
    }

    /**
     * Creates a conversation and returns the conversation id
     */
    fun createConversation(userId: Int): Int {
        val sql = """
            insert into chat_conversation (owner)
            values (?)
            returning id;
        """.trimIndent()
        databaseService.getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.executeQuery().use { rs ->
                    rs.next()
                    return rs.getInt("id")
                }
            }
        }
    }

    fun getConversation(conversationId: Int): ChatConversation? {
        val sql1 = """
            select owner
            from chat_conversation
            where id = ?;
        """.trimIndent()
        val sql2 = """
            select
              user_prompt,
              response_json
            from chat_message_pair
            where conversation = ?
            order by response_timestamp;
        """.trimIndent()
        databaseService.getConnection().use { conn ->
            val owner = conn.prepareStatement(sql1).use { statement ->
                statement.setInt(1, conversationId)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) {
                        return null
                    }
                    rs.getInt("owner")
                }
            }
            val messages = conn.prepareStatement(sql2).use { statement ->
                statement.setInt(1, conversationId)
                statement.executeQuery().use { rs ->
                    val messages = mutableListOf<ChatMessage>()
                    while (rs.next()) {
                        val userPrompt = rs.getString("user_prompt")
                        val responseJson = rs.getString("response_json")
                        messages.add(ChatUserMessage(userPrompt))
                        val systemMessage: ChatSystemMessage = objectMapper.readValue(responseJson)
                        messages.add(systemMessage)
                    }
                    messages
                }
            }
            return ChatConversation(conversationId, owner, messages)
        }
    }

    fun addChatMessagePair(conversationId: Int, userPrompt: String, responseJson: String, openAITotalTokens: Int) {
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
                statement.setInt(1, conversationId)
                statement.setString(2, userPrompt)
                statement.setString(3, responseJson)
                statement.setInt(4, openAITotalTokens)
                statement.execute()
            }
        }
    }
}
