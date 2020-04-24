create table activity_dg_tmp
(
    id              integer not null
        constraint activity_pk
            primary key autoincrement,
    parent          integer default 0 not null
        references activity
            on update cascade on delete set default,
    name            text    not null,
    color           text    default '#DDDDDDFF' not null,
    pointsPerMinute real    default 0 not null
);

insert into activity_dg_tmp(id, parent, name, color, pointsPerMinute)
select id, parent, name, color, pointsPerMinute - 1
from activity;

drop table activity;

alter table activity_dg_tmp
    rename to activity;

PRAGMA user_version = 2;