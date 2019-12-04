drop table commit_info;

create table commit_info (
       id bigserial primary key,
       repository varchar(255) not null,
       hash varchar(255) not null,
       email varchar(255) not null,
       timestamp timestamp not null,
       title text
);