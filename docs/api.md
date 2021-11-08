# API

## Overview

This API serves the [CoV-Spectrum frontend application](https://github.com/cevo-public/cov-spectrum-website) and provides it with all the needed data apart from genome sequence data. For the sequence data, [LAPIS](https://github.com/cevo-public/LAPIS) is used.

In following, the request parameters are not required unless it is stated explicitly.

## Resources Endpoints

### Scientific articles and pre-prints

Returns publications and pre-prints on medRxiv and bioRxiv that mention a pangolin lineage in the title or abstract.

**Request:**

```
GET /resource/article
Request params:
  - pangolinLineage: string (required)
```

**Response:**

```
[
  {
    doi: string,
    title: string,
    authors: string,
    date: date,
    category?: string,
    published?: string,
    server: string,
    abstract?: string
  }
]
```

### Cases

Returns case data.

**Request:**

```
GET /resource/case
Request params:
  - region: string
  - country: string
  - devision: string
  - fields: string, comma-separated (default: "")
```

**Response:**

```
[
  {
    region?: string,
    country?: string,
    division?: string,
    date?: Date,
    age?: integer,
    sex?: "Male"|"Female",
    hospitalized?: boolean,
    died?: boolean,
    newCases?: integer,
    newDeaths?: integer
  }
]
```

`newCases` gives the number of cases with the same attributes, and `newDeaths` gives the number of deaths. Only the fields defined in the request parameter `fields` and `newCases` and `newDeaths` will be returned.

### Countries

Returns the list of countries that CoV-Spectrum knows. It maps the country names that CoV-Spectrum uses to the name that GISAID uses.

**Request:**

```
GET /resource/country
```

**Response:**

```
[
  {
    covSpectrumName: string,
    gisaidName: string,
    region: string
  }
]
```

### Pango lineage aliases

Returns the list of pango lineage aliases.

**Request:**

```
GET /resource/pango-lineage-alias
```

**Response:**

```
[
  {
    alias: string,
    fullName: string
  }
]
```

### Reference genome

Returns information about the reference genome.

**Request:**

```
GET /resource/reference-genome
```

**Response:**

```
{
  nucSeq: string,
  genes: [
    {
      name: string,
      startPosition: integer,
      endPosition: integer,
      aaSeq: string
    }
  ]
}
```

### Wastewater

Returns wastewater data. Currently, we only have wastewater data for Switzerland, and the endpoint will return `null` if another country than Switzerland (or no country) is requested.

**Request:**

```
GET /resource/wastewater
Request params:
  - region: string
  - country: string
  - devision: string
```

**Response:**

```
{
  data: [
    {
      location: string,
      variantName: string,
      data: {
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
    }
  ]
}
```



## Computed

### Model: chen2021Fitness

Based on Chen et al. (2021): "Quantification of the spread of SARS-CoV-2 variant B.1.1.7 in Switzerland"

**Request:**

```
GET /computed/model/chen2021Fitness
Request params:
  - region: string
  - country: string
  - mutations: string, comma-separated (required)
  - matchPercentage: float (default: 1)
  - dataType: string (possible values: "SURVEILLANCE")
  - alpha: float (default: 0.95)
  - generationTime: float (default: 4.8)
  - reproductionNumberWildtype: float (default: 1)
  - plotStartDate: date (required)
  - plotEndDate: date (required)
  - initialWildtypeCases: integer (default: 1000)
  - initialVariantCases: integer (default: 100)
```

**Response:**

```
{
  daily: {
    t: [date],
    proportion: [float],
    ciLower: [float],
    ciUpper: [float]
  },
  params: {
    a: {
      value: float,
      ciLower: float,
      ciUpper: float
    },
    t0: {
      value: float,
      ciLower: float,
      ciUpper: float
    },
    fc: {
      value: float,
      ciLower: float,
      ciUpper: float
    },
    fd: {
      value: float,
      ciLower: float,
      ciUpper: float
    }
  },
  plotAbsoluteNumbers: {
    t: [date],
    variantCases: [integer],
    wildtypeCases: [integer]
  },
  plotProportion: {
    t: [date],
    proportion: [float],
    ciLower: [float],
    ciUpper: [float]
  }
}
```


## Internal

### User's country

Returns the region and country of the user based on the IP-address.

**Request:**

```
GET /internal/my-country
```

**Response:**

```
{
  region?: string,
  country?: string
}
```


## Authentication

It is possible to maintain a private area on CoV-Spectrum in which certain data are only provided to authorized users. This is currently not being used, i.e., every feature is public. We implemented the authentication procedure because we had Swiss-only data that were considered as confidential, and we are keeping the authentication in case that we are working with confidential data again.

JSON Web Tokens (JWT) are used for authentication. Upon login, a token will be provided. The token has to be sent with every subsequent request. There are two ways to send the token:

* In the request header: `Authorization: Bearer <token>` (the preferred way)
* In the query params: `?jwt=<token>`

Two API endpoints are related to the user authentication:

### Login

Returns a JWT token if the submitted credentials are valid.

**Request:**

```
POST /internal/login
Request body:
  {
    username: string,
    password: string
  }
```

**Response:**

```
{
  token: string
}
```


### Temporary JWT Token

Returns a JWT token with a TTL of 3 minutes that can be used to authenticate access to a particular endpoint.

**Request:**

```
POST /internal/create-temporary-jwt
Request params:
  - restrictionEndpoint: string (required)
```

**Response**

```
{
  token: string
}
```

The endpoint is only available to logged-in users.

