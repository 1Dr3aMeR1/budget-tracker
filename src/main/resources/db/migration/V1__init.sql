create table users (
                       id uuid primary key,
                       username varchar(100) not null unique,
                       password_hash varchar(255) not null
);