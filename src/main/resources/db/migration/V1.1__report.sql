-- Report
create table report
(
    report_id        bigint      not null auto_increment,
    reason           varchar(45) not null,
    member_id        bigint      not null,
    post_id          bigint      not null,
    use_yn           boolean     not null,
    created_at       timestamp   not null,
    last_modified_at timestamp   not null,
    primary key (report_id)
) engine = InnoDB;