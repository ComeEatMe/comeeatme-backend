alter table account modify column member_id bigint;

alter table account modify column refresh_token varchar(255) not null default '';

alter table account add column refresh_token_expires_at datetime(6);
update
    account
set refresh_token_expires_at = now();
alter table account modify column refresh_token_expires_at datetime(6) not null;