package com.phoenixtask.workspace.infrastructure.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phoenixtask.workspace")
public class WorkspaceProperties {

  private final Datasource datasource = new Datasource();
  private final Flyway flyway = new Flyway();
  private final Attachments attachments = new Attachments();

  public Datasource getDatasource() {
    return datasource;
  }

  public Flyway getFlyway() {
    return flyway;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public static class Datasource {
    private String host;
    private Integer port;
    private String database;
    private String schema;
    private String username;
    private String password;
    private final Cache cache = new Cache();

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public Integer getPort() {
      return port;
    }

    public void setPort(Integer port) {
      this.port = port;
    }

    public String getDatabase() {
      return database;
    }

    public void setDatabase(String database) {
      this.database = database;
    }

    public String getSchema() {
      return schema;
    }

    public void setSchema(String schema) {
      this.schema = schema;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public Cache getCache() {
      return cache;
    }

    public static class Cache {
      private int maxSize = 32;
      private int expireAfterMinutes = 30;

      public int getMaxSize() {
        return maxSize;
      }

      public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
      }

      public int getExpireAfterMinutes() {
        return expireAfterMinutes;
      }

      public void setExpireAfterMinutes(int expireAfterMinutes) {
        this.expireAfterMinutes = expireAfterMinutes;
      }
    }
  }

  public static class Flyway {
    private List<String> locations = new ArrayList<>();

    public List<String> getLocations() {
      return locations;
    }

    public void setLocations(List<String> locations) {
      this.locations = locations;
    }
  }

  public static class Attachments {
    private String root;
    private long maxBytes = 10 * 1024 * 1024;

    public String getRoot() {
      return root;
    }

    public void setRoot(String root) {
      this.root = root;
    }

    public long getMaxBytes() {
      return maxBytes;
    }

    public void setMaxBytes(long maxBytes) {
      this.maxBytes = maxBytes;
    }
  }
}
