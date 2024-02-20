create type test_table_type as enum ('Type1', 'Type2');

create table test_table
(
    id            bigint primary key generated always as identity,
    created_by    text,
    created_date  timestamp with time zone,
    updated_by    text,
    updated_date  timestamp with time zone,
    type          test_table_type not null,
    test_json     jsonb,
    test_array    int[],
    test_inet     inet,
    test_interval interval
);
