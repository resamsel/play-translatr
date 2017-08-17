INSERT INTO user_ (id,version,when_created,when_updated,username,name,email,email_validated,active) VALUES ('fe9387fc-b42c-4cae-b2c2-f351cd7b901e',1,'2017.08.17 01:49:11.648','2017.08.17 01:49:11.648','resamsel','René Panzar','rene.samselnig@gmail.com',false,true);
INSERT INTO linked_account (id,version,when_created,when_updated,user_id,provider_user_id,provider_key) VALUES (1,1,'2017.08.17 01:49:11.654','2017.08.17 01:49:11.654','fe9387fc-b42c-4cae-b2c2-f351cd7b901e','113794442910833949954','google');
INSERT INTO access_token (id,version,when_created,when_updated,user_id,name,key,scope) VALUES (1,1,'2017.08.17 01:49:38.857','2017.08.17 01:49:38.857','fe9387fc-b42c-4cae-b2c2-f351cd7b901e','JMeter','MTliYmI1NWUtYzMwNS00Mjc4LWE4ZWYtMDY5ZDI5OWRhNDkzZWIwMjIwMTYtOWFi','read:notification,read:locale,write:locale,read:user,write:message,read:project,write:project,write:key,write:user,read:message,read:key,write:notification');
INSERT INTO project (id,deleted,name,version,when_created,when_updated,owner_id) VALUES ('60d2641c-e9e9-476c-84f9-1b284b583e2f',false,'Test',1,'2017.08.17 01:51:55.183','2017.08.17 01:51:55.183','fe9387fc-b42c-4cae-b2c2-f351cd7b901e');
INSERT INTO project_user (id,version,when_created,when_updated,project_id,user_id,role) VALUES (1,1,'2017.08.17 01:51:55.185','2017.08.17 01:51:55.185','60d2641c-e9e9-476c-84f9-1b284b583e2f','fe9387fc-b42c-4cae-b2c2-f351cd7b901e','Owner');
INSERT INTO log_entry (id,type,content_type,after,when_created,user_id) VALUES ('b644a7e4-b19f-4fdf-ad89-74ef3af722ee','Login','dto.User','{"id":"fe9387fc-b42c-4cae-b2c2-f351cd7b901e","name":"René Panzar","username":"renesamselniggmailcom"}','2017.08.17 01:49:11.709','fe9387fc-b42c-4cae-b2c2-f351cd7b901e');
INSERT INTO log_entry (id,type,content_type,after,when_created,user_id) VALUES ('015b9ef3-c524-47e2-8ba6-6f2ff19eef65','Create','dto.AccessToken','{"userId":"fe9387fc-b42c-4cae-b2c2-f351cd7b901e","name":"JMeter","key":"MTliYmI1NWUtYzMwNS00Mjc4LWE4ZWYtMDY5ZDI5OWRhNDkzZWIwMjIwMTYtOWFi","scope":"read:notification,read:locale,write:locale,read:user,write:message,read:project,write:project,write:key,write:user,read:message,read:key,write:notification"}','2017.08.17 01:49:38.877','fe9387fc-b42c-4cae-b2c2-f351cd7b901e');
INSERT INTO log_entry (id,type,content_type,project_id,after,when_created,user_id) VALUES ('4d4f9263-3663-451d-9aa0-c5b426b8a967','Create','dto.Project','60d2641c-e9e9-476c-84f9-1b284b583e2f','{"id":"60d2641c-e9e9-476c-84f9-1b284b583e2f","name":"Test","ownerId":"fe9387fc-b42c-4cae-b2c2-f351cd7b901e","ownerName":"René Panzar","ownerUsername":"renesamselniggmailcom"}','2017.08.17 01:51:55.204','fe9387fc-b42c-4cae-b2c2-f351cd7b901e');

select setval('linked_account_id_seq', max(id)) from linked_account;
select setval('access_token_id_seq', max(id)) from access_token;
select setval('project_user_id_seq', max(id)) from project_user;