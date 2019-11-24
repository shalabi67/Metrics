create table if not exists metrics
(
    id           BIGSERIAL
        constraint metrics_pkey
            primary key,
    cpu          bigint,
    machine_id   bigint,
    memory       bigint,
    metrics_date timestamp
);
