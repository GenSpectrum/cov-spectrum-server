package ch.ethz.covspectrum.chat

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.util.regex.Pattern

class OpenAIClient(
    private val openAISecret: String
) {

    fun chat(previousMessages: List<OpenAIChatRequest.Message>): OpenAIChatResponse {
        val request = OpenAIChatRequest(
            "gpt-3.5-turbo",
            mutableListOf(
                OpenAIChatRequest.Message(
                    "system",
                    "You are a helpful assistant."
                ),
                OpenAIChatRequest.Message(
                    "user",
                    """
                        I want to query a database table called "metadata" to retrieve information about SARS-CoV-2 sequences, variants, and mutations. The "metadata" table contains columns for the following metadata: date, country, region (the continent, e.g., Europe) and lineage. It contains a column for each nucleotide and amino acid (AA) position. For example: nuc_123, aa_S_501. It contains data year 2020 until today (${LocalDate.now()}).

                        A mutation that contains a colon is an amino acid mutation (e.g., ORF1a:356F, N:Y10P). A mutation that does not contain a colon is a nucleotide mutation (e.g., 2393T, G182C).

                        The database understands basic SQL queries. It knows select, from, where, group by, order by, limit, and offset. It does not understand any nested queries. Do not improvise. I would like you to translate questions to a SQL query.

                        If you think that a question cannot be answered with the query language, tell me that you cannot answer it. Answer in the specified format. If you can provide an answer, start with "Execute:" followed by the SQL query. Do not add anything else. If you cannot provide an answer, start with "I cannot answer. Reason:" followed by the reason.

                        Remember: Do not use subqueries. Do not use sub-expressions. Do not use the keyword "in". Do not use the keyword "exists"

                        Examples 1:
                        User: What's the number of sequences in Switzerland?
                        AI: Execute: `select count(*) from metadata where country = 'Switzerland';`

                        Examples 2:
                        User: How many lineages do we have in March 2020
                        AI: Execute: `select count(*) from metadata where date between '2020-03-01' and '2020-03-31' group by lineage;`

                        Example 3:
                        User: How many lineages have the mutations S:A27S, S:N969K, and ORF9b:P10F?
                        AI: Execute: `select count(*) from metadata where aa_S_27 = 'S' and aa_S_969 = 'K' and aa_ORF9b_10 = 'F' group by lineage;`

                        Example 4:
                        User: What is the most prevalent lineage with the 2842T and G1282A mutations?
                        AI: Execute: `select lineage, count(*) from metadata where nuc_2842 = 'T' and nuc_1282 = 'A' group by country order by count(*) desc limit 1;`

                        Example 5:
                        User: In which country was BA.1 first found more than 5 times on a day?
                        AI: Execute: `select country from metadata where lineage = 'BA.1' group by country, date having count(*) > 5 order by date limit 1;`

                        Do you understand?
                    """.trimIndent()
                ),
                OpenAIChatRequest.Message(
                    "assistant",
                    """
                        Yes, I understand your instructions and will provide SQL queries based on the LAPIS query language constraints you specified. If a question cannot be answered with the LAPIS query language, I will inform you that I cannot answer it.
                    """.trimIndent()
                )
            ),
            0f
        )
        request.messages.addAll(previousMessages)

        val endpoint = "https://api.openai.com/v1/chat/completions"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(openAISecret)
        val modelHttpReq = HttpEntity(request, headers)
        val response = RestTemplate().postForObject(endpoint, modelHttpReq, OpenAIChatResponse::class.java)

        return response!!
    }

    fun extractSql(messageContent: String): String? {
        val tmp = messageContent.replace("\n", " ")
        val pattern = Pattern.compile("^.*(`\\s*)(.*select.*from .*;)(\\s*`).*\$")
        val matcher = pattern.matcher(tmp)
        if (matcher.find() && matcher.groupCount() == 3) {
            return matcher.group(2)
        }
        return null
    }

    fun extractErrorReason(messageContent: String): String? {
        val tmp = messageContent.replace("\n", " ")
        if (tmp.startsWith("I cannot answer. Reason:")) {
            return messageContent.replace("I cannot answer. Reason:", "").trim();
        }
        return null
    }

}
