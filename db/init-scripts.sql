create
extension hstore;
create schema tweets;
create table if not exists tweets."Tweet" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "user_id" VARCHAR,
    "text" VARCHAR,
    "engagement_ratio" VARCHAR,
    "sentimental_score" VARCHAR
);

create table if not exists tweets."User" (
  "id" VARCHAR NOT NULL PRIMARY KEY,
  "name" VARCHAR
);
