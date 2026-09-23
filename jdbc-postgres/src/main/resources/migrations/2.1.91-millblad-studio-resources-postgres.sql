CREATE TABLE IF NOT EXISTS studio_release_targets (
    key VARCHAR(250) NOT NULL PRIMARY KEY,
    value JSONB NOT NULL,
    tenant_id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'tenantId') STORED,
    target_id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'id') STORED
);
CREATE INDEX IF NOT EXISTS studio_release_targets_tenant ON studio_release_targets (tenant_id);
CREATE INDEX IF NOT EXISTS studio_release_targets_target ON studio_release_targets (tenant_id, target_id);

CREATE TABLE IF NOT EXISTS studio_infrastructure (
    key VARCHAR(250) NOT NULL PRIMARY KEY,
    value JSONB NOT NULL,
    tenant_id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'tenantId') STORED,
    target_id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'targetId') STORED
);
CREATE INDEX IF NOT EXISTS studio_infrastructure_target ON studio_infrastructure (tenant_id, target_id);

CREATE TABLE IF NOT EXISTS studio_releases (
    key VARCHAR(250) NOT NULL PRIMARY KEY,
    value JSONB NOT NULL,
    tenant_id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'tenantId') STORED,
    id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'id') STORED,
    target_id VARCHAR(100) GENERATED ALWAYS AS (value ->> 'targetId') STORED,
    created_at VARCHAR(64) GENERATED ALWAYS AS (value ->> 'createdAt') STORED
);
CREATE INDEX IF NOT EXISTS studio_releases_target ON studio_releases (tenant_id, target_id, created_at);
