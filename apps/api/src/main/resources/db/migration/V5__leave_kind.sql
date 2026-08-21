-- Leave subtype on work requests (paid / am_half / pm_half / absence).
ALTER TABLE work_requests ADD COLUMN leave_kind VARCHAR(32) NOT NULL DEFAULT '';
