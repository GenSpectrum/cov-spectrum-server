package ch.ethz.covspectrum.chat

class ChatSystemMessage(
    var id: Int?, // The ID will only be provided if the message is logged
    val textBeforeData: String,
    val data: List<Map<Any, Any>>?,
    val textAfterData: String?,
    val internal: Internal?
) : ChatMessage("GenSpectrum") {
    class Internal(
        val sql: String?
    )
}
