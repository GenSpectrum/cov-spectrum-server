package ch.ethz.covspectrum.chat

data class OpenAIChatResponse (
    val id: String,
    val created: Long,
    val model: String,
    val usage: UsageInfo,
    val choices: List<Choice>
) {
    data class UsageInfo (
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )

    data class Choice (
        val message: OpenAIChatRequest.Message,
        val finish_reason: String,
        val index: Int
    )
}
