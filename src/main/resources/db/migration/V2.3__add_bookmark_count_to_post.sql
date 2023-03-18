alter table post
    add column bookmark_count int not null default 0;

update
    post p
set p.bookmark_count = (select count(*)
                        from bookmark b
                        where b.post_id = p.post_id);