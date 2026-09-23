package io.kestra.repository.h2.migration;

import java.util.List;

import javax.sql.DataSource;

import io.kestra.jdbc.migration.AbstractSQLMigrationScript;
import io.kestra.repository.h2.H2RepositoryEnabled;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@H2RepositoryEnabled
public class V2_1_91MillbladStudioResourcesMigration extends AbstractSQLMigrationScript {
    private static final String SCRIPT_ID = "2.1.91-millblad-studio-resources";

    private final DataSource dataSource;

    @Inject
    public V2_1_91MillbladStudioResourcesMigration(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String scriptId() {
        return SCRIPT_ID;
    }

    @Override
    public String description() {
        return "Millblad Studio: release targets, infrastructure desired state and releases";
    }

    @Override
    protected DataSource dataSource() {
        return dataSource;
    }

    @Override
    public List<String> sqlResources() {
        return List.of("/migrations/2.1.91-millblad-studio-resources-h2.sql");
    }
}
