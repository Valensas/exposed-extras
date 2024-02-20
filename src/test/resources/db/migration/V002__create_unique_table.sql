create table unique_table
(
    id    bigint primary key generated always as identity,
    name  text not null unique,
    value int  not null
);