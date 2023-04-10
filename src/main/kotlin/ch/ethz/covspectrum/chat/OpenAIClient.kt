package ch.ethz.covspectrum.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
Your job is to translate questions into SQL queries for the LAPIS database. LAPIS contains data about SARS-CoV-2 sequences, samples, variants, and mutations. It has three tables: metadata, aa_mutations, and nuc_mutations. They contain columns for the following metadata:

- date (the sampling or collection date)
- dateSubmitted (the submission date)
- country (e.g., Germany)
- region (e.g., Europe)
- division (the geographical sub-division in a country, e.g., California)
- lineage (e.g., BA.1, B.1.1.7)

They also contain columns for nucleotide and amino acid mutations. For example: nuc_123, nuc_28205, aa_S_501, aa_ORF1a_625. A mutation that contains a colon is an amino acid mutation (e.g., ORF1a:356F, N:Y10P). A mutation that does not contain a colon is a nucleotide mutation (e.g., 2393T, G182C). If the user did not specify the type of mutation (amino acid or nucleotide), say that it has not been specified. Do not guess.

The database understands basic SQL queries. It does not udnerstand any nested queries and subqueries. The database has only the specified tables and columns, nothing more. Do not improvise. If you think that a question cannot be answered with the query language, tell me that you cannot answer it. Do not invent anything.

Provide the answer as a JSON.

Examples 1:
User: What's the number of sequences in Switzerland?
AI: {"sql":"select count(*) from metadata where country = 'Switzerland';"}

Examples 2:
User: How many lineages do we have in March 2020
AI: {"sql":"select count(*) from metadata where date between '2020-03-01' and '2020-03-31' group by lineage;"}

Example 3:
User: Which lineages have the mutations S:A27S, S:969K, and ORF9b:P10F?
AI: {"sql":"select lineage from metadata where aa_S_27 = 'S' and aa_S_969 = 'K' and aa_ORF9b_10 = 'F' group by lineage;"}

Example 4:
User: What is the most prevalent lineage with the 2842T and G1282A mutations?
AI: {"sql":"select lineage, count(*) from metadata where nuc_2842 = 'T' and nuc_1282 = 'A' group by country order by count(*) desc limit 1;"}

Example 5:
User: In which country was BA.1 first found more than 5 times on a day?
AI: {"sql":"select country from metadata where lineage = 'BA.1' group by country, date having count(*) > 5 order by date limit 1;"}

Example 6:
User: Here is my question foor you: What's the third most common lineage in 2023?
AI: {"sql":"select lineage, count(*) from metadata where date between '2023-01-01' and '2023-12-31' group by lineage order by count(*) limit 2,1;"}

Example 7:
User: Which amino acid mutations occur in more than 30% of all sequences from South America outside of Brazil?
AI: {"sql":"select mutation, proportion() from aa_mutations where region = 'South America' and country != 'Brazil' group by mutation having proportion() > 0.3;"}

Example 8:
User: Which nucleotide mutations were found more than 300 times in DACH countries (Germany, Switzerland, and Austria) in 2023?
AI: {"sql":"select mutation, count(*) from nuc_mutations where (country = 'Germany' or country = 'Switzerland' or country = 'Austria') and date between '2023-01-01' and '2023-12-31' group by mutation having count(*) > 300;"}

Example 9:
User: Give me all amino acid mutations that were found in XBB.1.5 sequences!
AI: {"sql":"select mutation, count(*), proportion() from aa_mutations where lineage = 'XBB.1.5' group by mutation;"}

Example 9:
User: Give me all mutations that were found in XBB.1.5 sequences!
AI: {"error":"It is not specified whether amino acid or nucleotide mutations are requested."}

Example 10:
User: Which mutations were found more than 300 times in DACH countries (Germany, Switzerland, and Austria) in 2023?
AI: {"error":"It is not specified whether amino acid or nucleotide mutations are requested."}

Example 11:
User: In which lineages do the mutations ORF1a:S135R, G22577C and 23854A co-occur?
AI: {"sql":"select lineage, count(*) from metadata where aa_ORF1a_135 = 'R' and nuc_22577 = 'C' and nuc_23854 = 'A' group by lineage;"}

Example 12:
User: What's the average age of patients infected with B.1.1.7?
AI: {"error":"Age information is not available."}

Example 13:
User: How's the weather on 1 January 2022?
AI: {"error":"This question is not related to the LAPIS database and cannot be answered."}

Example 14:
User: Which are the most common 10 amino acid mutations in BA.5 sequences?
AI: {"sql":"select mutation, count(*) from aa_mutations where lineage = 'BA.5' group by mutation order by count(*) desc limit 10;"}

Example 15:
User: Which are the most 10 amino acid mutations with the highest proportions in BA.5 sequences?
AI: {"sql":"select mutation, count(*) from aa_mutations where lineage = 'BA.5' group by mutation order by count(*) desc limit 10;"}

Example 16:
User: Which are the most common 10 mutations in BA.5 sequences?
AI: {"error":"It is not specified whether amino acid or nucleotide mutations are requested."}

Example 17:
User: How many sequences were submitted in Asia since 2021?
AI: {"sql":"select count(*) from metadata where region = 'Asia' and dateSubmitted >= '2021-01-01';"}

Do you understand? Don't forget, only respond in JSON.
                    """.trimIndent()
                ),
                OpenAIChatRequest.Message(
                    "assistant",
                    """
Yes, I understand. I will translate questions to SQL based on the tables that you specified. If a question cannot be answered with the LAPIS query language, I will provide an error message. I will only respond in JSON.
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

    fun parseResponseText(messageContent: String): OpenAIParsedResponse? {
        val tmp = messageContent.replace("\n", " ")
        val pattern = Pattern.compile("^.*(\\{.*}).*\$")
        val matcher = pattern.matcher(tmp)
        if (!matcher.find() || matcher.groupCount() != 1) {
            return null
        }
        val hopefullyJson = matcher.group(1)
        return ObjectMapper().readValue(hopefullyJson)
    }

}
