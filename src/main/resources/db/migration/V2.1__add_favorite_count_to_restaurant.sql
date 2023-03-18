alter table restaurant add column favorite_count int not null default 0;

update
    restaurant r
set r.favorite_count = (select count(*)
                        from favorite f
                        where f.restaurant_id = r.restaurant_id);