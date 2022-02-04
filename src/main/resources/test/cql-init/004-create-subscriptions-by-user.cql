create table if not exists subscriptions_by_user
(
    userId timeuuid,
    feedId bigint,
    primary key ((userId), feedId)
);