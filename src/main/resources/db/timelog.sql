CREATE TABLE IF NOT EXISTS log
(
    id        integer not null primary key autoincrement,
    activity  integer default 0 not null
        references activity
            on update cascade on delete restrict,
    what      text    not null,
    transport integer
        references meansOfTransport
            on update cascade on delete restrict,
    start     integer not null,
    end       integer
);

CREATE TABLE IF NOT EXISTS activity
(
    id     integer not null primary key autoincrement,
    parent integer default 0 not null
        references activity
            on update cascade on delete set default,
    name   text    not null,
    color  text    default '#DDDDDDFF' not null
);

CREATE TABLE IF NOT EXISTS meansOfTransport
(
    id   integer not null primary key autoincrement,
    name text    not null
);

CREATE TABLE IF NOT EXISTS person
(
    id   integer not null primary key autoincrement,
    name text    not null
);

CREATE TABLE IF NOT EXISTS qualityTime
(
    logEntry integer not null
        references log
            on update cascade on delete cascade,
    person   integer not null
        references person
            on update cascade on delete cascade,
    constraint qualityTime_pk
        unique (logEntry, person)
);

CREATE TABLE IF NOT EXISTS goal
(
    id       integer not null primary key autoincrement,
    activity integer not null
        references activity
            on update cascade on delete cascade,
    interval text    not null
)