-- IdP org tenant. Default matches P01 demo org-demo-a.
ALTER TABLE employees ADD COLUMN org_id TEXT NOT NULL DEFAULT 'org-demo-a';
ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_sub_key;
ALTER TABLE employees ADD CONSTRAINT employees_org_sub UNIQUE (org_id, sub);

ALTER TABLE closed_months ADD COLUMN org_id TEXT NOT NULL DEFAULT 'org-demo-a';
ALTER TABLE closed_months DROP CONSTRAINT IF EXISTS closed_months_pkey;
ALTER TABLE closed_months ADD PRIMARY KEY (org_id, month);
