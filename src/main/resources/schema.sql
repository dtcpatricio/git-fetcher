create table if not exists commit_info (
       id bigserial primary key,
       repository varchar(255) not null,
       hash varchar(255) not null,
       email varchar(255) not null,
       timestamp timestamp not null,
       title text
);

create index if not exists "commit_info_repository" on commit_info(repository);
