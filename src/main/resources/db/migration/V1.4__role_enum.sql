UPDATE user_roles SET roles = 'ADMIN' WHERE roles = 'ROLE_ADMIN';
UPDATE user_roles SET roles = 'USER' WHERE roles = 'ROLE_USER';
UPDATE user_roles SET roles = 'REPORTER' WHERE roles = 'ROLE_REPORTER';