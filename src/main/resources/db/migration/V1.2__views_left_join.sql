DROP VIEW IF EXISTS bug_view;
DROP VIEW IF EXISTS app_view;
CREATE VIEW bug_view AS (SELECT bug.id, bug.solved, bug.version_code, bug.app_id, bug.title, count(report.id) AS report_count, max(report.date) AS last_report FROM bug LEFT OUTER JOIN report ON report.bug_id = bug.id GROUP BY bug.id);
CREATE VIEW app_view AS (SELECT app.id, app.name, count(report.id) AS report_count FROM app LEFT OUTER JOIN bug ON bug.app_id = app.id LEFT OUTER JOIN report ON report.bug_id = bug.id GROUP BY app.id);