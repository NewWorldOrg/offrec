
create table if not exists channel(
  id serial primary key,
  guild_id varchar(255) not null,
  channel_id varchar(255) not null,
  created_at datetime not null,
  updated_at datetime not null,
  deleted_at datetime
) engine = InnoDB default charset = utf8mb4;

create table if not exists message_delete_queue(
  id serial primary key,
  guild_id varchar(255) not null,
  channel_id varchar(255) not null,
  message_id varchar(255) not null,
  ttl int not null,
  created_at datetime not null,
  updated_at datetime not null,
  deleted_at datetime
) engine = InnoDB default charset = utf8mb4;
