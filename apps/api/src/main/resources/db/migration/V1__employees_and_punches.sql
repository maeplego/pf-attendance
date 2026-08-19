-- P09 attendance slice 1. Employees are fictional. No payroll amounts.
CREATE TABLE employees (
  id VARCHAR(26) PRIMARY KEY,
  sub VARCHAR(128) NOT NULL UNIQUE,
  display_name VARCHAR(200) NOT NULL,
  role VARCHAR(32) NOT NULL
);

CREATE TABLE punches (
  id VARCHAR(26) PRIMARY KEY,
  employee_id VARCHAR(26) NOT NULL REFERENCES employees (id),
  punch_type VARCHAR(32) NOT NULL,
  punched_at TIMESTAMPTZ NOT NULL,
  work_date DATE NOT NULL,
  source VARCHAR(32) NOT NULL
);

CREATE INDEX punches_employee_work_date ON punches (employee_id, work_date, punched_at);
