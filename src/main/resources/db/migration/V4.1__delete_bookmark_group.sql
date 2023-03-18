alter table bookmark drop index UK_bookmark_bookmark_group_post;

delete from bookmark where bookmark_group_id is not null;

alter table bookmark drop column bookmark_group_id;

drop table if exists bookmark_group;

alter table bookmark
    add constraint UK_bookmark_post_member unique (post_id, member_id);
