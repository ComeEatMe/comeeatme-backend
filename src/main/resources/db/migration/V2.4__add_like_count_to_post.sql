alter table post add column like_count int not null default 0;

update
    post p
set p.like_count = (select count(*)
                    from likes l
                    where l.post_id = p.post_id);