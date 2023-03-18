alter table account drop index UK_account_username;

alter table account add index IX_account_username (username);