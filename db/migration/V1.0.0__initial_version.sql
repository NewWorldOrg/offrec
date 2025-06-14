
create table if not exists channel(
  id serial primary key comment "ID",
  guild_id varchar(255) not null comment "サーバーID",
  channel_id varchar(255) not null comment "チャンネルID",
  created_at datetime not null,
  updated_at datetime not null,
  deleted_at datetime
) engine = InnoDB default charset = utf8mb4;

create table if not exists message_delete_queue(
  id serial primary key comment "ID",
  guild_id varchar(255) not null comment "サーバーID",
  channel_id varchar(255) not null comment "チャンネルID",
  message_id varchar(255) not null comment "メッセージID",
  ttl int not null comment "メッセージ削除までの時間（秒）",
  status int not null comment '0: pending, 1: completed, 2: failed',
  created_at datetime not null,
  updated_at datetime not null,
  deleted_at datetime
) engine = InnoDB default charset = utf8mb4;
