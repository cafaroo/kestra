package io.kestra.repository.h2.migration;

import java.util.List;

import javax.sql.DataSource;

import io.kestra.jdbc.migration.AbstractSQLMigrationScript;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Requires(property = "kestra.repository.type", pattern = "h2|memory")
public class V2_1_90MillbladTenantsMigration extends AbstractSQLMigrationScript {
    private static final String SCRIPT_ID = "2.1.90-millblad-tenants";

    private final DataSource dataSource;

    @Inject
    public V2_1_90MillbladTenantsMigration(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String scriptId() {
        return SCRIPT_ID;
    }

    @Override
    public String description() {
        return "Millblad Studio: create tenant registry";
    }

    @Override
    protected DataSource dataSource() {
        return dataSource;
    }

    @Override
    public List<String> sqlResources() {
        return List.of("/migrations/2.1.90-millblad-tenants-h2.sql");
    }
}
