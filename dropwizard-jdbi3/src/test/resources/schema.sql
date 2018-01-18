create table games (
  id             int          not null,
  home_team      varchar(100) not null,
  visitor_team   varchar(100) not null,
  home_scored    int          not null,
  visitor_scored int          not null,
  played_at      date         not null,
  primary key (id)
);
