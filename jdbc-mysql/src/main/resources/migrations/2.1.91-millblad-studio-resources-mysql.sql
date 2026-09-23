CREATE TABLE IF NOT EXISTS `studio_release_targets` (
    `key` VARCHAR(250) NOT NULL PRIMARY KEY,
    `value` JSON NOT NULL,
    `tenant_id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.tenantId') STORED NOT NULL,
    `target_id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.id') STORED NOT NULL,
    INDEX ix_tenant (tenant_id),
    INDEX ix_target (tenant_id, target_id)
) ENGINE INNODB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `studio_infrastructure` (
    `key` VARCHAR(250) NOT NULL PRIMARY KEY,
    `value` JSON NOT NULL,
    `tenant_id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.tenantId') STORED NOT NULL,
    `target_id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.targetId') STORED NOT NULL,
    INDEX ix_target (tenant_id, target_id)
) ENGINE INNODB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `studio_releases` (
    `key` VARCHAR(250) NOT NULL PRIMARY KEY,
    `value` JSON NOT NULL,
    `tenant_id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.tenantId') STORED NOT NULL,
    `id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.id') STORED NOT NULL,
    `target_id` VARCHAR(100) GENERATED ALWAYS AS (value ->> '$.targetId') STORED NOT NULL,
    `created_at` VARCHAR(64) GENERATED ALWAYS AS (value ->> '$.createdAt') STORED NOT NULL,
    INDEX ix_target (tenant_id, target_id, created_at)
) ENGINE INNODB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
