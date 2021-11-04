# Our Data Sources

In following, we explain how we populate the database tables specified in [./database.md](database.md). There are many COVID-19 data sources in the world and there are certainly a great amount of data that we are not using. Thus, please use the following list only as an inspiration, but feel free to try out other datasets.

## Tables

| Table name | Data source |
| --- | --- |
| gene | We use MN908947 (Wuhan-Hu-1/2019) as reference genome. |
| pangolin_lineage_alias | We have a script that checks the alias file in the [pango-designation repository](https://github.com/cov-lineages/pango-designation/blob/master/pango_designation/alias_key.json) every day and updates the table. |
| reference_genome_sequence | We use MN908947 (Wuhan-Hu-1/2019) as reference genome. |
| spectrum_account | Manually curated |
| spectrum_cases | We use the [case data from OWID](https://github.com/owid/covid-19-data/tree/master/public/data) for all countries but Switzerland. For Switzerland, we use case data that we receive from the [Federal Office of Public Health](https://www.bag.admin.ch/bag/en/home.html). |
| spectrum_country_mapping | Manually curated |
| spectrum_waste_water_result | The data are provided by the [Computational Biology Group (CBG) at ETH Zurich](https://bsse.ethz.ch/cbg/research/computational-virology/sarscov2-variants-wastewater-surveillance.html). |
| rxiv_article | The data are downloaded from the [bioRxiv API](https://api.biorxiv.org/). |
| rxiv_article__rxiv_author | The data are downloaded from the [bioRxiv API](https://api.biorxiv.org/). |
| rxiv_author | The data are downloaded from the [bioRxiv API](https://api.biorxiv.org/). |
| pangolin_lineage__rxiv_article | We search the article titles and abstracts for mentions of pango lineages. |
