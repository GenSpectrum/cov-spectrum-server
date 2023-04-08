package ch.ethz.covspectrum.chat

class ChatSystemMessage(
    val text: String,
    val data: List<Map<Any, Any>>?,
    val internal: Internal?
) : ChatMessage("GenSpectrum") {
    class Internal(
        val sql: String?
    )
}
