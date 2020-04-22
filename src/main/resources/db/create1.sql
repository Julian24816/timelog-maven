CREATE TABLE log
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

CREATE TABLE activity
(
    id              integer not null primary key autoincrement,
    parent          integer default 0 not null
        references activity
            on update cascade on delete set default,
    name            text    not null,
    color           text    default '#E6E6E6FF' not null,
    pointsPerMinute real    default 1 not null
);

INSERT INTO activity (id, parent, name)
VALUES (0, 0, 'Activity');

CREATE TABLE meansOfTransport
(
    id   integer not null primary key autoincrement,
    name text    not null
);

CREATE TABLE person
(
    id           integer not null primary key autoincrement,
    name         text    not null,
    pointsFactor real default 1 not null
);

CREATE TABLE qualityTime
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

CREATE TABLE goal
(
    id          integer not null primary key autoincrement,
    activity    integer not null
        references activity
            on update cascade on delete cascade,
    interval    text    not null,
    minDuration int default 0 not null,
    person      int
        references person
            on update cascade on delete cascade
);