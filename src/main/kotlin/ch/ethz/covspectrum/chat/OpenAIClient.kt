package ch.ethz.covspectrum.chat

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
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
                        I would like to query a very database table called LAPIS to retrieve information about SARS-CoV-2 sequences, variants, and mutations. The LAPIS table contains columns for the following metadata: date, country, region (the continent, e.g., Europe), lineage, nucMutations (e.g., A123T, C345G), aaMuations (e.g., S:N501Y, ORF1a:E1695P). It contains a column for each nucleotide and amino acid (AA) position. For example: nuc_123, aa_S_501. It contains data up to today which is the 2023-04-02.

                        The database understands very basic SQL queries. It understands select, from, where, group by, order by, and limit. It does not understand any nested queries. It only has the fields that I specified, nothing else. LAPIS only supports the operators =, >=, <=, >, < and between. It does not support anything else. LAPIS does not support aliases. Do not use aliases to rename fields. LAPIS may only return aggregated data, not individual entries. Do not invent anything. Do not improvise. I would like you to translate questions to a SQL query.

                        Examples 1:
                        User: The question is "What's the number of sequences in Switzerland?"
                        AI: Please execute: select count(*) from lapis where country = 'Switzerland';

                        Examples 2:
                        User: The question is "How many lineages do we have in March 2020"
                        AI: Please execute: select count(*) from lapis where date between '2020-03-01' and '2020-03-31' group by lineage;

                        Example 3:
                        User: The question is "How many lineages have the AA mutations S:A27S, S:N969K, and ORF9b:P10F?"
                        AI: Please execute: elect count(*) from lapis where aa_S_27 = 'S' and aa_S_969 = 'K' and aa_ORF9b_10 = 'F' group by lineage;

                        Example 4:
                        User: The question is "How many sequences were found outside of Germany"
                        AI: I am unfortunately not capable to answer this question at the moment.


                        Please be exact and do not invent anything about the LAPIS query language. Only use the constructs that I told you. If you think that a question cannot be answered with the LAPIS query language, tell me that you cannot answer it. That is very important, tell me that you don't know if you cannot answer a question with absolute certainty. Do not improvise. If you want me to execute a SQL query, do not explain anything. Just give me the query. Do you understand?
                    """.trimIndent()
                ),
                OpenAIChatRequest.Message(
                    "assistant",
                    """
                        Yes, I understand your instructions and will provide SQL queries based on the LAPIS query language constraints you specified. I will not use field aliases. I will not use the "as" keyword. If a question cannot be answered with the LAPIS query language, I will inform you that I cannot answer it.
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
        val pattern = Pattern.compile("^.*(```\\s*)(.*select.*from lapis.*;)(\\s*```).*\$")
        val matcher = pattern.matcher(tmp)
        if (matcher.find() && matcher.groupCount() == 3) {
            return matcher.group(2)
        }
        return null
    }

}

fun main() {
    val client = OpenAIClient(
        System.getenv("OPENAI_SECRET")
    )
    val response = client.chat(listOf(
        OpenAIChatRequest.Message(
            "user",
            "What's the most common 10 lineages in the 3rd quarter of 2022 in Switzerland?"
        )
    ))
    val responseMessageContent = response.choices[0].message.content

    val sql = client.extractSql(responseMessageContent)
    if (sql != null) {
        val lapis = LapisSqlClient("https://lapis.cov-spectrum.org/open/v1")
        val data = lapis.execute(sql)
        println(data)
        return
    }
    println("Sorry, we are currently not able to answer this question.")
}
