# Database

The CoV-Spectrum server retrieves its data from a PostgreSQL database (version 12+). It mostly reads from the database. In following, we show the *logical tables* that the server requires. The *logical tables* can be implemented as actual tables, or as views or materialized views. In [./our-data-sources.md](./our-data-sources.md), you can find the data sources that we use to populate the database. It is however possible to populate the database in another way as long as the data has the correct format.

The only exception is the table "spectrum_huisman_scire_2021_re". The server needs to write to that table.

## Tables

If not otherwise specified, the columns are nullable.

#### gene

| Column name | Type | Description | Example value |
|---|---|---|---|
| gene | text not null | The abbreviation of the gene | S |
| reference_aa_sequence | text not null | The amino acid sequence of the reference genome | MFVFLVLLPLVSSQCV[...] |
| start_position | integer not null | The start position of the reading frame in the reference genome | 21563 |
| end_position | integer not null | The end position of the reading frame in the reference genome | 25384 |

#### pangolin_lineage_alias

This table stores the pango lineage aliases. The aliases can be found in the [pango-designation repository](https://github.com/cov-lineages/pango-designation/blob/master/pango_designation/alias_key.json).

| Column name | Type | Description | Example value |
|---|---|---|---|
| alias | text not null |  | C |
| full_name | text not null |  | B.1.1.1 |

#### reference_genome_sequence

This table stores the nucleotide sequence of the reference genome. It must have exactly one row.

| Column name | Type | Description | Example value |
|---|---|---|---|
| sequence | text not null |  | ATTAAAGGTTTATACC[...] |

#### spectrum_account

This table stores the credentials of user accounts. It is only relevant, if there are information or features that should only be available to authorized people. If that's not the case, the table can remain empty.

| Column name | Type | Description | Example value |
|---|---|---|---|
| username | text not null | The username is case-insensitive. In the database, the name has to be stored in lower case. | hpotter |
| password_hash | text not null | A bcrypt hash | $2a$12$3FiCJqm0Azxb1Bo/orY2DeZ/ tvZjLHvAXP3.PLC.mg2VzVfrT8xMW |
| full_name | text | The name of the user | Harry Potter |

#### spectrum_cases

This table stores the number of confirmed cases and deaths.

| Column name | Type | Description | Example value |
|---|---|---|---|
| region | text |  | Europe | 
| country | text |  | United Kingdom  | 
| division | text |  | England | 
| date | date |  | 2021-06-01 | 
| age | integer |  | *null* | 
| sex | text | "Male" or "Female" | Male | 
| hospitalized | boolean |  | true | 
| died | boolean |  | false | 
| new_case | bigint | The number of confirmed cases that match the above attributes | 200 | 
| new_deaths | bigint | The number of deaths that match the above attributes | 0 |

#### spectrum_country_mapping

This table stores in the mapping of country names used in different datasets.

| Column name | Type | Description | Example value |
|---|---|---|---|
| cov_spectrum_country | text | The country name that CoV-Spectrum should use | United States |
| cov_spectrum_region | text |  | North America |
| gisaid_country | text | The country name used by GISAID dataset | USA |
| owid_country | text | The country name used by the OWID cases dataset | United States |
| iso_alpha_2 | text | The ISO 3166-1 alpha-2 code | US |

#### spectrum_huisman_scire_2021_re

This table stores the Re results calculated by the huismanScire2021Re model.

| Column name      | Type      | Description                                        | Example value                                                   |
|------------------|-----------|----------------------------------------------------|-----------------------------------------------------------------|
| key              | text      | The (SHA-256) hash of the request                  | BEA43BB0C1D53686CAA40473F2E857676FCAAFCDD9D102BA4CAB75C2ED15BDA8 |
| calculation_date | timestamp | The date/time when the result was calculated       |                                                                 |
| calculation_duration_seconds  | integer   | The duration of the calculation       |                                                                 |
| request          | text      | The request body (JSON) as needed by the model API |                                                                 |
| success          | boolean   | Whether the calculation was successful             |                                                                 |
| result           | text      | The result (JSON) as returned by the model API     |                                                                 |

"key" should be the primary key. The server needs write-access for the table.

#### spectrum_waste_water_result

On CoV-Spectrum, we present results from the [wastewater analysis of the Computational Biology Group (CBG) at ETH Zurich](https://bsse.ethz.ch/cbg/research/computational-virology/sarscov2-variants-wastewater-surveillance.html). The CBG writes the results as JSON into the spectrum_waste_water_result and the server forwards it to the API without making changes to the content.

| Column name | Type | Description | Example value |
|---|---|---|---|
| variant_name | text not null | A pango lineage or "Undetermined" |  |
| location | text not null | The location of the wastewater plant; can be an arbitrary value |  |
| data | jsonb not null | See below |  |

The data are JSONs in the following format:

```
{
  timeseriesSummary: [
    {
      date: Date,
      variant: string,
      location: string,
      proportion: null | number,
      proportionLower: null | number,
      proportionUpper: null | number
    }
  ]
  mutationOccurrences: null | [
    {
      date: Date,
      nucMutation: string,
      proportion: null | number
    }
  ]
}
```

### Scientific publications and pre-prints

CoV-Spectrum has the feature to show scientific publications and pre-prints about pango lineages. We currently use data from medRxiv and bioRxiv, but in principle, it is also possible to use other datasets.

#### rxiv_article

| Column name | Type | Description | Example value |
|---|---|---|---|
| doi | text not null |  | 10.1101/2021.03.05.21252520 |
| version | integer |  | 4 |
| title | text |  | Quantification of the spread of SARS-CoV-2 variant B.1.1.7 in Switzerland |
| date | date |  | 2021-06-02 |
| type | text |  | PUBLISHAHEADOFPRINT |
| category | text |  | epidemiology |
| abstract | text |  | Background. In December 2020, the United Kingdom (UK) [...] |
| license | text |  | cc_by_nc |
| server | text |  | medrxiv |
| jatsxml | text |  | https://www.medrxiv.org/content/early/2021/06/02/2021.03.05.21252520.source.xml |
| published | text | The DOI of the published paper | 10.1016/j.epidem.2021.100480 |

#### rxiv_article__rxiv_author

| Column name | Type | Description | Example value |
|---|---|---|---|
| doi | text not null |  | 10.1101/2021.03.05.21252520 |
| author_id | integer not null | References rxiv_author(id) | 389665 |
| position | integer not null | The position of the author on the paper starting with 1. | 1 |

#### rxiv_author

| Column name | Type | Description | Example value |
|---|---|---|---|
| id | integer not null |  | 389665 |
| name | text |  | Chen, C. |

#### pangolin_lineage__rxiv_article

| Column name | Type | Description | Example value |
|---|---|---|---|
| pangolin_lineage | text not null |  | B.1.1.7 |
| doi | text not null |  | 10.1101/2021.03.05.21252520 |
