package com.phoenixtask.controlplane.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phoenixtask.auth")
public class AuthProperties {

  private final Session session = new Session();

  public Session getSession() {
    return session;
  }

  public static class Session {
    private int ttlMinutes = 720;
    private int tokenBytes = 32;

    public int getTtlMinutes() {
      return ttlMinutes;
    }

    public void setTtlMinutes(int ttlMinutes) {
      this.ttlMinutes = ttlMinutes;
    }

    public int getTokenBytes() {
      return tokenBytes;
    }

    public void setTokenBytes(int tokenBytes) {
      this.tokenBytes = tokenBytes;
    }
  }
}
