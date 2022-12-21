-- Report
create table notice
(
    notice_id        bigint        not null auto_increment,
    type             varchar(25)   not null,
    title            varchar(255)  not null,
    content          varchar(4000) not null,
    use_yn           boolean       not null,
    created_at       datetime(6)   not null,
    last_modified_at datetime(6)   not null,
    primary key (notice_id)
) engine = InnoDB;