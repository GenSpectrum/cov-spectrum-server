create table pango_lineage_recombinant (
    name text not null,
    parent_position integer not null,
    parent text not null,
    primary key (name, parent_position)
);
