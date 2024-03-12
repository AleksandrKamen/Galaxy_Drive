--liquibase formatted sql

--changeset kamenev:1
CREATE TABLE IF NOT EXISTS users
(
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(64) NOT NULL,
  firstname VARCHAR(64),
  lastname VARCHAR(64),
  role VARCHAR(32)
);