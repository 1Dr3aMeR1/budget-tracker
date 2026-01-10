create table transactions(
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    category_id uuid references categories(id) on delete set null,
    amount numeric(12, 2) not null check (amount >= 0),
    type varchar(10) not null check (type in ('INCOME', 'EXPENSE')),
    occurred_at timestamptz not null,
    note varchar(255),
    created_at timestamptz not null default now()
);

create index idx_transactions_user_id on transactions(user_id);
create index idx_transactions_occurred_at on transactions(occurred_at);
create index idx_transactions_category_id on transactions(category_id);