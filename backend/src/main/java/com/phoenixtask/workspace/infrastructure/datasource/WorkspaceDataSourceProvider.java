package com.phoenixtask.workspace.infrastructure.datasource;

import com.phoenixtask.shared.tenant.TenantMetadata;
import com.phoenixtask.workspace.infrastructure.config.WorkspaceProperties;
import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceDataSourceProvider {

  private final WorkspaceProperties workspaceProperties;
  private final Map<String, CachedDataSource> cache;
  private final int maxCacheSize;
  private final Duration expireAfter;

  public WorkspaceDataSourceProvider(WorkspaceProperties workspaceProperties) {
    this.workspaceProperties = workspaceProperties;
    int configuredMaxSize = workspaceProperties.getDatasource().getCache().getMaxSize();
    int configuredExpire = workspaceProperties.getDatasource().getCache().getExpireAfterMinutes();
    this.maxCacheSize = Math.max(1, configuredMaxSize);
    this.expireAfter = Duration.ofMinutes(Math.max(1, configuredExpire));
    this.cache = new LinkedHashMap<>(16, 0.75f, true);
  }

  public DataSource getDataSource() {
    TenantMetadata metadata = TenantContext.getRequired();
    String tenantCode = metadata.getTenantCode();
    Instant now = Instant.now();
    synchronized (cache) {
      CachedDataSource cached = cache.get(tenantCode);
      if (cached != null) {
        if (!cached.isExpired(now, expireAfter)) {
          cached.touch(now);
          return cached.dataSource();
        }
        cache.remove(tenantCode);
        cached.close();
      }

      evictIfNeeded();
      DataSource dataSource = buildDataSource(metadata);
      cache.put(tenantCode, new CachedDataSource(dataSource, now));
      return dataSource;
    }
  }

  private DataSource buildDataSource(TenantMetadata metadata) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(buildJdbcUrl(metadata));
    dataSource.setUsername(metadata.getDbUsername());
    dataSource.setPassword(workspaceProperties.getDatasource().getPassword());
    dataSource.setPoolName("phoenixtask-ws-" + metadata.getTenantCode());
    return dataSource;
  }

  private String buildJdbcUrl(TenantMetadata metadata) {
    String base = String.format(
        "jdbc:postgresql://%s:%d/%s",
        metadata.getDbHost(),
        metadata.getDbPort(),
        metadata.getDbName()
    );
    if (metadata.getDbSchema() != null && !metadata.getDbSchema().isBlank()) {
      return base + "?currentSchema=" + metadata.getDbSchema();
    }
    return base;
  }

  private void evictIfNeeded() {
    while (cache.size() >= maxCacheSize) {
      String eldestKey = cache.keySet().iterator().next();
      CachedDataSource eldest = cache.remove(eldestKey);
      if (eldest != null) {
        eldest.close();
      }
    }
  }

  @PreDestroy
  public void shutdown() {
    synchronized (cache) {
      cache.values().forEach(CachedDataSource::close);
      cache.clear();
    }
  }

  private static final class CachedDataSource {
    private final DataSource dataSource;
    private Instant lastAccess;

    private CachedDataSource(DataSource dataSource, Instant lastAccess) {
      this.dataSource = dataSource;
      this.lastAccess = lastAccess;
    }

    private DataSource dataSource() {
      return dataSource;
    }

    private void touch(Instant now) {
      this.lastAccess = now;
    }

    private boolean isExpired(Instant now, Duration ttl) {
      return now.isAfter(lastAccess.plus(ttl));
    }

    private void close() {
      if (dataSource instanceof AutoCloseable closable) {
        try {
          closable.close();
        } catch (Exception ignored) {
          // ignore shutdown errors
        }
      }
    }
  }
}
