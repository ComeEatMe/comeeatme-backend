alter table favorite drop index UK_favorite_favorite_group_restaurant;

delete from favorite where favorite_group_id is not null;

alter table favorite drop column favorite_group_id;

drop table if exists favorite_group;

alter table favorite
    add constraint UK_favorite_restaurant_member unique (restaurant_id, member_id);
