create table categories(
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    name varchar(80) not null,
    type varchar(10) not null check (type in ('INCOME', 'EXPENSE')),
    created_at timestamptz not null default now(),
    unique  (user_id, name, type)
);

create index idx_categories_user_id on categories(user_id);