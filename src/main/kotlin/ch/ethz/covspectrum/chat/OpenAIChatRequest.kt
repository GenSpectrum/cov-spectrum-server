package ch.ethz.covspectrum.chat

data class OpenAIChatRequest (
    val model: String,
    val messages: MutableList<Message>,
    var temperature: Float
) {
    data class Message (
        val role: String,
        val content: String
    )
}
