create
extension hstore;
create schema tweets;
create table if not exists tweets."User" (
     "id" VARCHAR NOT NULL PRIMARY KEY,
     "name" VARCHAR
);

create table if not exists tweets."Tweet" (
    "id" VARCHAR NOT NULL PRIMARY KEY,
    "user_id" VARCHAR REFERENCES tweets."User" (id),
    "text" VARCHAR,
    "engagement_ratio" VARCHAR,
    "sentimental_score" VARCHAR
);
