-- Requests, allocations, month close. Fictional HR workflow only.
CREATE TABLE work_requests (
  id VARCHAR(26) PRIMARY KEY,
  employee_id VARCHAR(26) NOT NULL REFERENCES employees (id),
  request_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  work_date DATE NOT NULL,
  reason TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  decided_at TIMESTAMPTZ,
  decided_by VARCHAR(128)
);

CREATE TABLE time_allocations (
  id VARCHAR(26) PRIMARY KEY,
  employee_id VARCHAR(26) NOT NULL REFERENCES employees (id),
  work_date DATE NOT NULL,
  project VARCHAR(64) NOT NULL,
  minutes INTEGER NOT NULL CHECK (minutes > 0)
);

CREATE TABLE closed_months (
  month CHAR(7) PRIMARY KEY,
  closed_by VARCHAR(128) NOT NULL
);
