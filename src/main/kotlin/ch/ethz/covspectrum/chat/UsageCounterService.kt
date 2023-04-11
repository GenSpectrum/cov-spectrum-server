package ch.ethz.covspectrum.chat

import ch.ethz.covspectrum.service.DatabaseService
import ch.ethz.covspectrum.util.nowUTCDateTime
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PreDestroy

@Service
class UsageCounterService(
    private val databaseService: DatabaseService
) {

    private var numberMessagesSinceLastFlush = AtomicInteger(0)
    private var numberOpenAITokensSinceLastFlush = AtomicInteger(0)

    fun submit(openAITokens: Int) {
        numberMessagesSinceLastFlush.incrementAndGet()
        numberOpenAITokensSinceLastFlush.addAndGet(openAITokens)
    }

    @PreDestroy
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun flushToDatabase() {
        // It's not perfect that these operations are not executed atomically, but it's good enough for us because
        // it is not a big problem if a few tokens are assigned to a wrong minute.
        val numberMessages = numberMessagesSinceLastFlush.getAndSet(0)
        val numberTokens = numberOpenAITokensSinceLastFlush.getAndSet(0)
        val timestamp = nowUTCDateTime()

        if (numberMessages > 0 || numberTokens > 0) {
            val sql = """
                insert into chat_usage_statistics (timestamp, number_messages, number_openai_tokens)
                values (?, ?, ?);
            """.trimIndent()
            databaseService.getConnection().use { conn ->
                conn.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.valueOf(timestamp))
                    statement.setInt(2, numberMessages)
                    statement.setInt(3, numberTokens)
                    statement.execute()
                }
            }
        }
    }

}
