create table json_entity(
    id bigint primary key generated always as identity,
    created_by varchar,
    created_date timestamp with time zone,
    updated_by varchar,
    updated_date timestamp with time zone,
    data json not null
);
