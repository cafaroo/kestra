CREATE TABLE IF NOT EXISTS studio_release_targets (
    "key" VARCHAR(250) NOT NULL PRIMARY KEY,
    "value" TEXT NOT NULL,
    "tenant_id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.tenantId')),
    "target_id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.id'))
);
CREATE INDEX IF NOT EXISTS studio_release_targets_tenant ON studio_release_targets ("tenant_id");
CREATE INDEX IF NOT EXISTS studio_release_targets_target ON studio_release_targets ("tenant_id", "target_id");

CREATE TABLE IF NOT EXISTS studio_infrastructure (
    "key" VARCHAR(250) NOT NULL PRIMARY KEY,
    "value" TEXT NOT NULL,
    "tenant_id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.tenantId')),
    "target_id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.targetId'))
);
CREATE INDEX IF NOT EXISTS studio_infrastructure_target ON studio_infrastructure ("tenant_id", "target_id");

CREATE TABLE IF NOT EXISTS studio_releases (
    "key" VARCHAR(250) NOT NULL PRIMARY KEY,
    "value" TEXT NOT NULL,
    "tenant_id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.tenantId')),
    "id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.id')),
    "target_id" VARCHAR(100) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.targetId')),
    "created_at" VARCHAR(64) NOT NULL GENERATED ALWAYS AS (JQ_STRING("value", '.createdAt'))
);
CREATE INDEX IF NOT EXISTS studio_releases_target ON studio_releases ("tenant_id", "target_id", "created_at");
