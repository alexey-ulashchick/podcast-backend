create keyspace if not exists dashboard
    with replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1 }
     and durable_writes = true;

use dashboard;

create table if not exists users_by_email
(
    email      varchar primary key,
    password   varchar,
    "isActive" boolean
);
