package ch.ethz.covspectrum.chat

data class UserInfo (
    val id: Int,
    val quota: Int,
    val quotaUsed: Int,
    val conversationIds: List<String>
)
