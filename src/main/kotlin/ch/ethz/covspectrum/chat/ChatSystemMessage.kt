package ch.ethz.covspectrum.chat

class ChatSystemMessage(
    val text: String,
    val data: List<Map<String, String>>?,
    val internal: Internal?
) : ChatMessage("GenSpectrum") {
    class Internal(
        val sql: String?
    )
}
