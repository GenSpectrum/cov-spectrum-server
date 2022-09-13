-- Pathogen-related data

create table gene (
  gene text primary key,
  reference_aa_sequence text not null,
  start_position integer not null,
  end_position integer not null
);

create table pango_lineage_alias (
  alias text primary key,
  full_name text not null
);
create unique index on pango_lineage_alias (full_name);

create table reference_genome (
  name text primary key,
  seq text not null
);

-- Geographical data

create table country_mapping (
  cov_spectrum_country text,
  cov_spectrum_region text,
  gisaid_country text,
  owid_country text,
  iso_alpha_2 text
);

-- Case data

create table cases_raw_owid (
  iso_country text not null,
  region text not null,
  country text not null,
  date date not null,
  new_cases_per_million double precision,
  new_deaths_per_million double precision,
  new_cases integer,
  new_deaths integer,
  primary key (country, date)
);
create index on cases_raw_owid (region);
create index on cases_raw_owid (country);

create view cases(region, country, division, date, new_cases, new_deaths) as
select
  cm.cov_spectrum_region      as region,
  cm.cov_spectrum_country     as country,
  null::text                  as division,
  cro.date,
  coalesce(cro.new_cases, 0)  as new_cases,
  coalesce(cro.new_deaths, 0) as new_deaths
from
  cases_raw_owid cro
  join country_mapping cm on cro.country = cm.owid_country;

-- Wastewater

create table wastewater_result (
  variant_name text not null,
  location text not null,
  data jsonb not null,
  primary key (variant_name, location)
);

-- User accounts

create table account (
  username text primary key,
  password_hash text not null,
  full_name text
);

-- Collections

create table collection (
  id serial primary key,
  title text not null,
  description text not null,
  maintainers text not null,
  email text not null,
  admin_key text not null,
  creation_date timestamp without time zone not null,
  last_update_date timestamp without time zone not null
);

create table collection_variant (
  id serial primary key,
  collection_id integer references collection (id)
    on update cascade on delete cascade,
  query text not null,
  name text not null,
  description text not null,
  highlighted boolean default false
);
create index on collection_variant (collection_id);

-- Model-specific tables

create table model_huisman_scire_2021_re (
  key text primary key not null,
  calculation_date timestamp,
  calculation_duration_seconds integer,
  request text,
  success boolean not null,
  result text
);
