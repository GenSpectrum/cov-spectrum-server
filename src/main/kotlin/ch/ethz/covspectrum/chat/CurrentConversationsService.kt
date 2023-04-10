package ch.ethz.covspectrum.chat

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service // A service is by default singleton
class CurrentConversationsService() {

    val conversations: MutableMap<String, ChatConversation> = mutableMapOf()
    val conversationLastUpdated: MutableMap<String, LocalDateTime> = mutableMapOf()

    fun addConversation(conversation: ChatConversation) {
        val conversationId = conversation.id
        if (conversations.containsKey(conversationId)) {
            throw Exception("The conversation with ID $conversationId already exists.")
        }
        conversations[conversationId] = conversation
        conversationLastUpdated[conversationId] = LocalDateTime.now()
    }

    fun getConversation(id: String): ChatConversation? {
        return conversations[id]
    }

    fun addMessageToConversation(id: String, message: ChatMessage) {
        val conversation = conversations[id] ?: throw Exception("The conversation with ID $id does not exist.")
        conversation.messages.add(message)
    }

    /**
     * Deletes conversations that hasn't been updated for a day.
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    @Synchronized // It's probably dangerous to run this multiple times in parallel
    fun deleteOldConversations() {
        var numberDeleted = 0
        for ((id, updateTime) in conversationLastUpdated.entries) {
            if (updateTime.isBefore(LocalDateTime.now().minusDays(1))) {
                conversations.remove(id)
                conversationLastUpdated.remove(id)
                numberDeleted++
            }
        }
        println("${LocalDateTime.now()}\tCurrentConversationsService: deleting $numberDeleted old conversations")
    }

}
