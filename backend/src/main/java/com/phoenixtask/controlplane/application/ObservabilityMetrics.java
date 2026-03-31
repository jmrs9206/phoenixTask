package com.phoenixtask.controlplane.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class ObservabilityMetrics {

  private final MeterRegistry registry;
  private final Counter authLoginSuccess;
  private final Counter authLogout;

  public ObservabilityMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.authLoginSuccess = registry.counter("phoenixtask.auth.login", "outcome", "success");
    this.authLogout = registry.counter("phoenixtask.auth.logout", "outcome", "success");
  }

  public void recordAuthLoginSuccess() {
    authLoginSuccess.increment();
  }

  public void recordAuthLoginFailure(String reason) {
    registry.counter("phoenixtask.auth.login", "outcome", "failure", "reason", safe(reason)).increment();
  }

  public void recordAuthLogout() {
    authLogout.increment();
  }

  public void recordRateLimitHit(String policy) {
    registry.counter("phoenixtask.rate_limit.hit", "policy", safe(policy)).increment();
  }

  public void recordProvisioningOutcome(String outcome) {
    registry.counter("phoenixtask.tenant.provisioning", "outcome", safe(outcome)).increment();
  }

  public void recordLifecycleEvent(String event) {
    registry.counter("phoenixtask.tenant.lifecycle", "event", safe(event)).increment();
  }

  private String safe(String value) {
    return value == null || value.isBlank() ? "unknown" : value;
  }
}
