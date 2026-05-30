create table products (
    id          uuid primary key,
    name        varchar(255) not null,
    price_cents bigint not null check (price_cents >= 0)
);
