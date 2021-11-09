create table if not exists users_by_email
(
    email      varchar primary key,
    id         timeuuid,
    firstName  varchar,
    lastName   varchar,
    imageUrl   varchar
);
