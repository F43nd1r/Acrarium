ALTER TABLE bug_stacktraces DROP FOREIGN KEY FKkd6kp6uoa43v60ddty5dxqugm;
ALTER TABLE bug_stacktraces ADD CONSTRAINT FKkd6kp6uoa43v60ddty5dxqugm FOREIGN KEY (bug_id) REFERENCES bug (id) ON DELETE CASCADE;
