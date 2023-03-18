alter table restaurant add column post_count int not null default 0;

update
    restaurant r
set r.post_count = (select count(*)
                    from post p
                    where p.restaurant_id = r.restaurant_id
                      and p.use_yn = true);