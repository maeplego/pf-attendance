-- Stage A SES: engagement + worksite metadata on employees (employer org still owns the row).
ALTER TABLE employees ADD COLUMN engagement TEXT NOT NULL DEFAULT 'employed';
ALTER TABLE employees ADD COLUMN worksite_code TEXT NOT NULL DEFAULT '';
ALTER TABLE employees ADD COLUMN worksite_name TEXT NOT NULL DEFAULT '';
