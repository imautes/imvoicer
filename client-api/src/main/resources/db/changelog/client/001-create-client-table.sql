--liquibase formatted sql
--changeset techgeeknext:create-tables

create table client (
    id bigint not null,
    name varchar(255),
    primary key (id)
) engine=InnoDB;
