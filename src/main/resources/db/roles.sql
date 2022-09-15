create user covspectrum_main password '<password>';

create user covspectrum_ingest_owid password '<password>';
grant select, insert, update, delete
on table cases_raw_owid
to covspectrum_ingest_owid;

create user covspectrum_ingest_pangoalias password '<password>';
grant select, insert, update, delete
on table pango_lineage_alias
to covspectrum_ingest_pangoalias;
