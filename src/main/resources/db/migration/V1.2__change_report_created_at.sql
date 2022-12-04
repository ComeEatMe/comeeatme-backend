alter table report add column created_at_tmp datetime(6);

update
    report
set created_at_tmp = from_unixtime(created_at);

alter table report drop created_at;

alter table report change created_at_tmp created_at datetime(6) not null;