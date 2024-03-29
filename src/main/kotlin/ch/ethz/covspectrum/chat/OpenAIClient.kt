package ch.ethz.covspectrum.chat

import ch.ethz.covspectrum.util.nowUTCDate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class OpenAIClient(
    private val openAISecret: String
) {

    fun chatForSql(previousMessages: List<OpenAIChatRequest.Message>): Pair<OpenAIChatResponse, OpenAIChatRequest> {
        val request = OpenAIChatRequest(
            "gpt-4",
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
- date_submitted (the submission date)
- host (the host from which the sample is from)
- country (e.g., Germany)
- region (e.g., Europe)
- division (the geographical sub-division in a country, e.g., California)
- lineage (e.g., BA.1, B.1.1.7)

They also contain columns for nucleotide and amino acid mutations. For example: nuc_123, nuc_28205, aa_S_501, aa_ORF1a_625. A mutation that contains a colon is an amino acid mutation (e.g., ORF1a:356F, N:Y10P). A mutation that does not contain a colon is a nucleotide mutation (e.g., 2393T, G182C).

The database understands basic SQL queries. It does not understand any nested queries and subqueries. The database has only the specified tables and columns, nothing more. Do not improvise. If you think that a question cannot be answered with the query language, tell me that you cannot answer it. Do not invent anything.

Today's date is ${nowUTCDate()}

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
User: Give me all amino acid mutations that were found in XBB.1.5 sequences from the US!
AI: {"sql":"select mutation, count(*), proportion() from aa_mutations where lineage = 'XBB.1.5' and country = 'USA' group by mutation;"}

Example 10:
User: Which mutations were found more than 300 times in DACH countries (Germany, Switzerland, and Austria) in 2023?
AI: {"sql":"select mutation, count(*) from aa_mutations where (country = 'Germany' or country = 'Switzerland' or country = 'Austria') and date between '2023-01-01' and '2023-12-31' group by mutation having count(*) > 300;"}

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
AI: {"sql":"select mutation, proportion() from aa_mutations where lineage = 'BA.5' group by mutation order by proportion() desc limit 10;"}

Example 16:
User: Which are the most common 10 mutations in BA.5 sequences from the UK?
AI: {"sql":"select mutation, count(*) from aa_mutations where lineage = 'BA.5' and country = 'United Kingdom' group by mutation order by count(*) desc limit 10;"}

Example 17:
User: How many sequences were submitted in Asia since 2021 per month?
AI: {"sql":"select date_trunc('month', date), count(*) from metadata where region = 'Asia' and date_submitted >= '2021-01-01' group by month;"}

Example 18:
User: Which countries submitted sequences that were sampled from Odocoileus virginianus?
AI: {"sql":"select country, count(*) from metadata where host = 'Odocoileus virginianus' group by country;"}

Example 19:
User: How many Felis catus sequences were submitted in 2022?
AI: {"sql":"select count(*) from metadata where host = 'Felis catus' and date_submitted between '2022-01-01' and '2022-12-31';"}

Example 20:
User: For which non-human hosts do we have sequences from the United States?
AI: {"sql":"select host, count(*) from metadata where country = 'USA' and host != 'Human';"}

Example 21:
User: Which lineages from 2020 have the mutations S:501Y and either S:H69- or S:70-?
AI: {"sql":"select lineage, count(*) from metadata where date between '2020-01-01' and '2020-12-31' and aa_S_501 = 'Y' and (aa_S_69 = '-' or aa_S_70 = '-') group by lineage;"}

Example 22:
User: Which variants have S:484K?
AI: {"sql":"select lineage, count(*) from metadata where aa_S_484 = 'K' group by lineage;"}

Example 23:
User: Which mutations co-occur with ORF8:W45-?
AI: {"sql":"select mutation from aa_mutations where aa_ORF8_45 = '-';"}

Example 24:
User: Please give me the number of sequences in Oceania by year.
AI: {"sql":"select date_trunc('year', date) as year, count(*) from metadata where region = 'Oceania' group by year;"}

Example 25:
User: How many sequences were submitted last year?
AI: {"sql":"select count(*) from metadata where date_submitted between '2022-01-01' and '2022-12-31';"}

Example 26:
User: How many sequences with ORF1a:4983G were found in which countries of North America?
AI: {"sql":"select country, count(*) from metadata where aa_ORF1a_4983 = 'G' and region = 'North America' group by country;"}

Example 27:
User: What are the three most common variants in Europe in cats?
AI: {"sql":"select lineage, count(*) from metadata where host = 'Felis catus' and region = 'Europe' group by lineage order by count(*) desc limit 3;"}

Example 28:
User: How often does ORF1a:3606F occur globally?
AI: {"sql":"select count(*) from metadata where aa_ORF1a_3606 = 'F';"}

Example 29:
User: When was the first XBB.1.16 submitted?
AI: {"sql":"select date_submitted from metadata where lineage = 'XBB.1.16' order by date_submitted limit 1;"}

Example 30:
User: Would you mind giving me the number of sequences from South and North America together?
AI: {"sql":"select count(*) from metadata where region = 'North America' or region = 'South America';"}

Example 31:
User: Calculate the monthly distribution of sequences with the N:R203K AA mutation in Europe throughout 2020.
AI: {"sql":"select date_trunc('month', date) as month, count(*) from metadata where region = 'Europe' and date between '2020-01-01' and '2020-12-31' and aa_N_203 = 'K' group by month;"}

Example 32:
User: Explain covid deaths evolution in Finland
AI: {"error":"The LAPIS database does not contain information about COVID-19 deaths. It only contains information about SARS-CoV-2 sequences, samples, variants, and mutations. Therefore, I cannot answer your question."}

Example 33:
User: What is going on with variants in Germany?
AI: {"error":"Your question is too broad and cannot be answered with a specific query. Can you please provide more details or a specific question about variants in Germany?"}

Example 34:
User: What is the next dominant covid variant in the US?
AI: {"error":"I cannot answer that question with the LAPIS database."}

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

        return Pair(response!!, request)
    }

    fun parseSqlResponseText(messageContent: String): OpenAIParsedResponse? {
        val tmp = messageContent.replace("\n", " ")
        // This is a very heuristic way to extract a JSON from the string.
        val firstOpen = tmp.indexOf('{')
        val lastClose = tmp.lastIndexOf('}')
        if (firstOpen == -1 || lastClose == -1) {
            return null
        }
        val hopefullyJson = tmp.substring(firstOpen, lastClose + 1)
        return try {
            ObjectMapper().readValue(hopefullyJson)
        } catch (e: Exception) {
            null
        }
    }

    fun chatForExplanation(sqlQuery: String): Pair<OpenAIChatResponse, OpenAIChatRequest> {
        val request = OpenAIChatRequest(
            "gpt-4",
            mutableListOf(
                OpenAIChatRequest.Message(
                    "system",
                    "You are a helpful assistant."
                ),
                OpenAIChatRequest.Message(
                    "user",
                    """
Imagine that you just executed a SQL query in the LAPIS database. I will give you the SQL query and you will explain in simple words what you did. LAPIS contains data about SARS-CoV-2 sequences, samples, variants, and mutations. It has three tables: metadata, aa_mutations, and nuc_mutations. They contain columns for the following metadata:

- date (the sampling or collection date)
- dateSubmitted (the submission date)
- host (the host from which the sample is from)
- country (e.g., Germany)
- region (e.g., Europe)
- division (the geographical sub-division in a country, e.g., California)
- lineage (e.g., BA.1, B.1.1.7)

They also contain columns for nucleotide and amino acid mutations. For example: nuc_123, nuc_28205, aa_S_501, aa_ORF1a_625. A mutation that contains a colon is an amino acid mutation (e.g., ORF1a:356F, N:Y10P). A mutation that does not contain a colon is a nucleotide mutation (e.g., 2393T, G182C). When you mention a nucleotide mutation, use the notation 485T. When you mention an amino acid mutation, use the notation S:958P.

Examples 1:
User: select count(*) from metadata where country = 'Switzerland';
AI: I counted the total number of entries in the LAPIS database where the country is Switzerland. In other words, I found out how many SARS-CoV-2 sequences are sequenced in Switzerland in the dataset.

Examples 2:
User: select count(*) from metadata where date between '2020-03-01' and '2020-03-31' group by lineage;
AI: I found out how many virus samples were collected for each lineage during March 2020.

Example 3:
User: select lineage from metadata where aa_S_27 = 'S' and aa_S_969 = 'K' and aa_ORF9b_10 = 'F' group by lineage;
AI: I found the different lineages that have specific changes in their proteins: S:27S, S:969K, and ORF9b:F.

Example 4:
User: select lineage, count(*) from metadata where nuc_2842 = 'T' and nuc_1282 = 'A' group by country order by count(*) desc limit 1;
AI: I looked for the most common lineage in the dataset that has specific changes in its genetic code: 2842T and 1282A. I also grouped the results by country to find the location where this particular lineage is most prevalent.

Example 5:
User: select country from metadata where lineage = 'BA.1' group by country, date having count(*) > 5 order by date limit 1;
AI: I identified the first country where at least 6 samples of the BA.1 lineage were collected on the same day.

Example 6:
User: select lineage, count(*) from metadata where date between '2023-01-01' and '2023-12-31' group by lineage order by count(*) limit 2,1;
AI: I identified the third most common virus lineage in the dataset for samples collected during the year 2023 and counted the number of samples with that lineage.

Example 7:
User: select mutation, proportion() from aa_mutations where region = 'South America' and country != 'Brazil' group by mutation having proportion() > 0.3;
AI: I found the amino acid mutations that are present in more than 30% of the virus samples from South America, excluding Brazil. I also provided the proportion of samples with each mutation in that region.

Example 8:
User: select mutation, count(*) from nuc_mutations where (country = 'Germany' or country = 'Switzerland' or country = 'Austria') and date between '2023-01-01' and '2023-12-31' group by mutation having count(*) > 300;
AI: I found the nucleotide mutations that are present in more than 300 virus samples collected in Germany, Switzerland, or Austria during the year 2023. I also counted the number of samples with each mutation.

Example 9:
User: select mutation, count(*), proportion() from aa_mutations where lineage = 'XBB.1.5' group by mutation;
AI: I found the amino acid mutations in the XBB.1.5 lineage and counted the number of samples with each mutation. I also provided the proportion of samples with each mutation in this lineage.

Example 10:
User: select lineage, count(*) from metadata where aa_ORF1a_135 = 'R' and nuc_22577 = 'C' and nuc_23854 = 'A' group by lineage;
AI: I found the different virus lineages that have a specific amino acid change (ORF1a:135R) and two particular nucleotide changes (22577C and 23854A). I also counted how many samples of each lineage have these changes in the dataset.

Example 11:
User: select mutation, count(*) from aa_mutations where lineage = 'BA.5' group by mutation order by count(*) desc limit 10;
AI: I found the top 10 most common amino acid mutations in the BA.5 lineage and counted the number of samples with each mutation.

Example 12:
User: select mutation, proportion() from aa_mutations where lineage = 'BA.5' group by mutation order by proportion() desc limit 10;
AI: I found the 10 amino acid mutations with the highest proportions in the BA.5 lineage and provided the proportion of samples with each mutation within this lineage.

Example 12:
User: select count(*) from metadata where region = 'Asia' and dateSubmitted >= '2021-01-01';
AI: I counted the total number of SARS-CoV-2 sequences submitted from the Asia region since 1 January, 2021.

Example 13:
User: select country, count(*) from metadata where host = 'Odocoileus virginianus' group by country;
AI: I found the number of SARS-CoV-2 samples collected from white-tailed deer (Odocoileus virginianus) for each country in the dataset.

Example 14:
User: select count(*) from metadata where host = 'Felis catus' and date_submitted between '2022-01-01' and '2022-12-31';
AI: I counted the total number of SARS-CoV-2 samples collected from domestic cats (Felis catus) during the year 2022.

Example 15:
User: select host, count(*) from metadata where country = 'Switzerland' and host != 'Human';
AI: I found the number of non-human hosts and the number of SARS-CoV-2 samples collected from each of those hosts in Switzerland.

Example 16:
User: select lineage, count(*) from metadata where date between '2020-01-01' and '2020-12-31' and aa_S_501 = 'Y' and (aa_S_69 = '-' or aa_S_70 = '-') group by lineage;
AI: I found the different virus lineages with a specific protein change (S:501Y) and either one of the two other protein changes (S:69- or S:70-) from samples collected during 2020. I also counted how many samples of each lineage have these changes.

Example 17:
User: select country, date_submitted from metadata where lineage = 'B.1.1.7' order by date_submitted limit 5;
AI: I found the first 5 submitted samples of the B.1.1.7 lineage in the dataset, listing the country where they were collected and the date they were submitted to the database.

Example 18:
User: select host, count(*) from metadata where host != 'Human' group by host order by count(*) desc;
AI: I found the non-human hosts of the SARS-CoV-2 virus and counted the number of samples collected from each host type. I listed the hosts in descending order based on the number of samples.

Example 19:
User: select country from metadata where lineage = 'BA.1' group by country, date having count(*) > 10 order by date limit 1;
AI: I identified the first country where at least 11 samples of the BA.1 lineage were collected on the same day.

Example 20:
User: select lineage from metadata where nuc_23403 = 'G' and nuc_23063 = 'T' and nuc_3037 = 'T' group by lineage;
AI: I found the different virus lineages that have three specific changes in their genetic code: 23403G, 23063T, and 3037T.

Example 21:
User: select count(*) from metadata where lineage = 'XBB.1.5' and host = 'Canis lupus familiaris';
AI: I found the number of SARS-CoV-2 samples of the XBB.1.5 lineage collected from domestic dogs (Canis lupus familiaris).

Example 22:
User: select country, count(*) from metadata where date between '2023-01-01' and '2023-12-31' group by country order by count(*) desc limit 10;
AI: I identified the top 10 countries with the highest number of SARS-CoV-2 samples collected during the year 2023 and counted the number of samples from each country.

Example 23:
User: select count(*) from metadata where country = 'Germany' and date between '2023-02-01' and '2023-02-28' and nuc_1221 = 'T';
AI: I found the number of SARS-CoV-2 samples from Germany collected in February 2023 that have a specific genetic change: 1221T.

Please explain in very simple words. Do not mention any technical terms from SQL. You are explaining to a virologist. Your audience is familiar with virology terminology but not with SQL.

Do you understand?
                    """.trimIndent()
                ),
                OpenAIChatRequest.Message(
                    "assistant",
                    """
Yes, I understand. I will explain the SQL query results in simple words without using technical SQL terms, keeping in mind that the audience is familiar with virology terminology.
                    """.trimIndent()
                ),
                OpenAIChatRequest.Message("user", sqlQuery)
            ),
            0f
        )

        val endpoint = "https://api.openai.com/v1/chat/completions"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(openAISecret)
        val modelHttpReq = HttpEntity(request, headers)
        val response = RestTemplate().postForObject(endpoint, modelHttpReq, OpenAIChatResponse::class.java)

        return Pair(response!!, request)
    }

}
