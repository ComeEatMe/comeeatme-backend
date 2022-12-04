alter table report add column last_modified_at_tmp datetime(6);

update
    report
set last_modified_at_tmp = from_unixtime(last_modified_at);

alter table report drop last_modified_at;

alter table report change last_modified_at_tmp last_modified_at datetime(6) not null;