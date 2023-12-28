create table json_entity(
    id bigint primary key generated always as identity,
    created_by jsonb,
    created_date timestamp with time zone,
    updated_by jsonb,
    updated_date timestamp with time zone,
    data json not null
);
