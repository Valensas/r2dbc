create type enum_entity_type as enum ('Type1', 'Type2');

create table enum_entity(
    id bigint primary key generated always as identity,
    created_by jsonb,
    created_date timestamp with time zone,
    updated_by jsonb,
    updated_date timestamp with time zone,
    type enum_entity_type not null
);
