alter table post add column comment_count int not null default 0;

update
    post p
set p.comment_count = (select count(*)
                       from comment c
                       where c.post_id = p.post_id
                         and c.use_yn = true);