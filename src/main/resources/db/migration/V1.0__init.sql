-- Account
create table account
(
    account_id       bigint       not null auto_increment,
    member_id        bigint       not null,
    username         varchar(255) not null,
    password         varchar(255),
    refresh_token    varchar(255),
    use_yn           bit          not null,
    created_at       datetime(6)  not null,
    last_modified_at datetime(6)  not null,
    primary key (account_id)
) engine = InnoDB;

alter table account
    add constraint UK_account_username unique (username);

create index IX_account_member on account (member_id);


-- Image
create table images
(
    images_id        bigint       not null auto_increment,
    member_id        bigint       not null,
    origin_name      varchar(255) not null,
    stored_name      varchar(255) not null,
    url              varchar(255) not null,
    use_yn           bit          not null,
    created_at       datetime(6)  not null,
    last_modified_at datetime(6)  not null,
    primary key (images_id)
) engine = InnoDB;

create index IX_images_member on images (member_id);


-- Member
create table member
(
    member_id        bigint       not null auto_increment,
    nickname         varchar(25)  not null,
    introduction     varchar(100) not null,
    image_id         bigint,
    use_yn           bit          not null,
    created_at       datetime(6)  not null,
    last_modified_at datetime(6)  not null,
    primary key (member_id)
) engine = InnoDB;

create index IX_member_image on member (image_id);

alter table member
    add constraint UK_member_nickname unique (nickname);


-- Restaurant
create table restaurant
(
    restaurant_id     bigint       not null auto_increment,
    name              varchar(45)  not null,
    phone             varchar(25)  not null,
    address_name      varchar(255) not null,
    road_address_name varchar(255) not null,
    location          point        not null srid 4326,
    use_yn            bit          not null,
    created_at        datetime(6)  not null,
    last_modified_at  datetime(6)  not null,
    primary key (restaurant_id)
) engine = InnoDB;

create index IX_restaurant_name on restaurant (name);

create spatial index IX_restaurant_location on restaurant (location);


-- OpenInfo
create table open_info
(
    open_info_id     bigint      not null auto_increment,
    restaurant_id    bigint      not null,
    management_num   varchar(65) not null,
    service_id       varchar(15) not null,
    name             varchar(15) not null,
    category         varchar(15) not null,
    permission_date  date        not null,
    last_modified_at datetime(6) not null,
    primary key (open_info_id)
) engine = InnoDB;

alter table open_info
    add constraint UK_open_info_restaurant unique (restaurant_id);

alter table open_info
    add constraint UK_open_info_management_num unique (management_num);


-- Post
create table post
(
    post_id          bigint        not null auto_increment,
    member_id        bigint        not null,
    restaurant_id    bigint        not null,
    content          varchar(2000) not null,
    use_yn           bit           not null,
    created_at       datetime(6)   not null,
    last_modified_at datetime(6)   not null,
    primary key (post_id)
) engine = InnoDB;

create index IX_post_member on post (member_id);

create index IX_post_restaurant on post (restaurant_id);


-- PostHashtag
create table post_hashtag
(
    post_hashtag_id bigint      not null auto_increment,
    post_id         bigint      not null,
    hashtag         varchar(45) not null,
    primary key (post_hashtag_id)
) engine = InnoDB;

create index IX_post_hashtag_post on post_hashtag (post_id);

create index IX_post_hashtag_hashtag on post_hashtag (hashtag);

alter table post_hashtag
    add constraint UK_post_hashtag_post_hashtag unique (post_id, hashtag);


-- PostImage
create table post_image
(
    post_image_id bigint not null auto_increment,
    post_id       bigint not null,
    image_id      bigint not null,
    primary key (post_image_id)
) engine = InnoDB;

create index IX_post_image_post on post_image (post_id);

alter table post_image
    add constraint UK_post_image_image unique (image_id);


-- Comment
create table comment
(
    comment_id       bigint        not null auto_increment,
    member_id        bigint        not null,
    post_id          bigint        not null,
    parent_id        bigint,
    content          varchar(1000) not null,
    use_yn           bit           not null,
    created_at       datetime(6)   not null,
    last_modified_at datetime(6)   not null,
    primary key (comment_id)
) engine = InnoDB;

create index IX_comment_member on comment (member_id);

create index IX_comment_post on comment (post_id);

create index IX_comment_parent on comment (parent_id);


-- Like
create table likes
(
    likes_id   bigint      not null auto_increment,
    member_id  bigint      not null,
    post_id    bigint      not null,
    created_at datetime(6) not null,
    primary key (likes_id)
) engine = InnoDB;

create index IX_likes_post on likes (post_id);

create index IX_likes_member on likes (member_id);

alter table likes
    add constraint UK_likes_post_member unique (post_id, member_id);


-- BookmarkGroup
create table bookmark_group
(
    bookmark_group_id bigint      not null auto_increment,
    member_id         bigint      not null,
    name              varchar(25) not null,
    bookmark_count    integer     not null,
    created_at        datetime(6) not null,
    primary key (bookmark_group_id)
) engine = InnoDB;

create index IX_bookmark_group_member on bookmark_group (member_id);

alter table bookmark_group
    add constraint UK_bookmark_group_member_name unique (member_id, name);


-- Bookmark
create table bookmark
(
    bookmark_id       bigint      not null auto_increment,
    member_id         bigint      not null,
    bookmark_group_id bigint,
    post_id           bigint      not null,
    created_at        datetime(6) not null,
    primary key (bookmark_id)
) engine = InnoDB;

create index IX_bookmark_member on bookmark (member_id);

create index IX_bookmark_post on bookmark (post_id);

alter table bookmark
    add constraint UK_bookmark_bookmark_group_post unique (bookmark_group_id, post_id);


-- FavoriteGroup
create table favorite_group
(
    favorite_group_id bigint      not null auto_increment,
    member_id         bigint      not null,
    name              varchar(25) not null,
    favorite_count    integer     not null,
    created_at        datetime(6) not null,
    primary key (favorite_group_id)
) engine = InnoDB;

create index IX_favorite_group_member on favorite_group (member_id);

alter table favorite_group
    add constraint UK_favorite_group_member_name unique (member_id, name);


-- Favorite
create table favorite
(
    favorite_id       bigint      not null auto_increment,
    member_id         bigint      not null,
    favorite_group_id bigint,
    restaurant_id     bigint      not null,
    created_at        datetime(6) not null,
    primary key (favorite_id)
) engine = InnoDB;

create index IX_favorite_member on favorite (member_id);

create index IX_favorite_restaurant on favorite (restaurant_id);

alter table favorite
    add constraint UK_favorite_favorite_group_restaurant unique (favorite_group_id, restaurant_id);
