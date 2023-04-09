package ch.ethz.covspectrum.chat

import java.time.LocalDateTime

class ChatConversation (
    val id: String,
    val owner: Int,
    val creationTimestamp: LocalDateTime,
    val toBeLogged: Boolean,
    val messages: MutableList<ChatMessage>
)
