package com.phoenixtask.workspace.application.bootstrap;

import com.phoenixtask.workspace.infrastructure.config.WorkspaceProperties;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceDemoBootstrap {

  private static final Logger log = LoggerFactory.getLogger(WorkspaceDemoBootstrap.class);

  private final WorkspaceProperties properties;

  public WorkspaceDemoBootstrap(WorkspaceProperties properties) {
    this.properties = properties;
  }

  public void bootstrapDemoWorkspace() {
    DataSource dataSource = buildDataSource();
    Flyway flyway = buildFlyway(dataSource);

    log.info(
        "Running workspace demo migrations against database '{}' (explicit bootstrap)",
        properties.getDatasource().getDatabase()
    );
    flyway.migrate();
  }

  private DataSource buildDataSource() {
    String url = String.format(
        "jdbc:postgresql://%s:%d/%s",
        properties.getDatasource().getHost(),
        properties.getDatasource().getPort(),
        properties.getDatasource().getDatabase()
    );

    return DataSourceBuilder.create()
        .driverClassName("org.postgresql.Driver")
        .url(url)
        .username(properties.getDatasource().getUsername())
        .password(properties.getDatasource().getPassword())
        .build();
  }

  private Flyway buildFlyway(DataSource dataSource) {
    var config = Flyway.configure()
        .dataSource(dataSource)
        .baselineOnMigrate(false)
        .locations(properties.getFlyway().getLocations().toArray(new String[0]));

    String schema = properties.getDatasource().getSchema();
    if (schema != null && !schema.isBlank()) {
      config.schemas(schema).defaultSchema(schema);
    }

    return config.load();
  }
}
