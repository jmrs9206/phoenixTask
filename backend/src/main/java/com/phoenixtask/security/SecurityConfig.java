package com.phoenixtask.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AuthTokenAuthenticationFilter authTokenAuthenticationFilter,
      com.phoenixtask.publicapi.security.PublicApiAuthenticationFilter publicApiAuthenticationFilter,
      ControlplaneAdminAuthenticationFilter controlplaneAdminAuthenticationFilter,
      RateLimitingFilter rateLimitingFilter,
      AccessLogFilter accessLogFilter,
      RequestIdFilter requestIdFilter,
      MetricsAuthorizationFilter metricsAuthorizationFilter
  ) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> { })
        .exceptionHandling(ex -> ex.authenticationEntryPoint(
            (request, response, authException) -> response.sendError(
                jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized"
            )
        ))
        .headers(headers -> headers
            .contentTypeOptions(withDefaults())
            .frameOptions(frameOptions -> frameOptions.deny())
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            .cacheControl(withDefaults())
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/system/status").permitAll()
            .requestMatchers("/api/integrations/status").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/webhooks/git/**").permitAll()
            .requestMatchers("/api/controlplane/audit-logs").authenticated()
            .requestMatchers("/api/controlplane/access-logs").authenticated()
            .requestMatchers("/api/controlplane/tenants/**").authenticated()
            .requestMatchers("/api/controlplane/**").authenticated()
            .requestMatchers("/api/workspace/**").authenticated()
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers("/actuator/metrics/**").authenticated()
            .requestMatchers("/actuator/**").denyAll()
            .anyRequest().denyAll()
        );
    http.addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(controlplaneAdminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(publicApiAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(authTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterAfter(accessLogFilter, AuthTokenAuthenticationFilter.class);
    http.addFilterAfter(metricsAuthorizationFilter, AccessLogFilter.class);
    http.addFilterAfter(rateLimitingFilter, MetricsAuthorizationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(corsProperties.getAllowedOrigins());
    config.setAllowedMethods(corsProperties.getAllowedMethods());
    config.setAllowedHeaders(corsProperties.getAllowedHeaders());
    config.setAllowCredentials(corsProperties.isAllowCredentials());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthTokenAuthenticationFilter authTokenAuthenticationFilter(
      com.phoenixtask.controlplane.application.AuthService authService,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper
  ) {
    return new AuthTokenAuthenticationFilter(authService, objectMapper);
  }

  @Bean
  public com.phoenixtask.publicapi.security.PublicApiAuthenticationFilter publicApiAuthenticationFilter(
      com.phoenixtask.publicapi.application.PublicApiKeyService publicApiKeyService,
      com.phoenixtask.publicapi.security.PublicApiScopeRegistry scopeRegistry,
      org.springframework.beans.factory.ObjectProvider<com.phoenixtask.controlplane.application.TenantRegistryLookupService> tenantRegistryLookupServiceProvider,
      org.springframework.beans.factory.ObjectProvider<com.phoenixtask.controlplane.application.audit.AuditLogService> auditLogServiceProvider,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper
  ) {
    return new com.phoenixtask.publicapi.security.PublicApiAuthenticationFilter(
        publicApiKeyService,
        scopeRegistry,
        tenantRegistryLookupServiceProvider.getIfAvailable(),
        auditLogServiceProvider.getIfAvailable(),
        objectMapper
    );
  }

  @Bean
  public ControlplaneAdminAuthenticationFilter controlplaneAdminAuthenticationFilter(
      com.phoenixtask.controlplane.application.ControlplaneAdminKeyService adminKeyService,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper
  ) {
    return new ControlplaneAdminAuthenticationFilter(adminKeyService, objectMapper);
  }

  @Bean
  public RequestIdFilter requestIdFilter() {
    return new RequestIdFilter();
  }

  @Bean
  public AccessLogFilter accessLogFilter(
      com.phoenixtask.controlplane.application.AccessLogService accessLogService
  ) {
    return new AccessLogFilter(accessLogService);
  }

  @Bean
  public RateLimitingFilter rateLimitingFilter(
      RateLimiterService rateLimiterService,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper,
      com.phoenixtask.controlplane.application.ObservabilityMetrics metrics
  ) {
    return new RateLimitingFilter(rateLimiterService, objectMapper, metrics);
  }

  @Bean
  public MetricsAuthorizationFilter metricsAuthorizationFilter(
      com.phoenixtask.controlplane.application.ControlplaneMetricsAuthorizationService metricsAuthorizationService,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper
  ) {
    return new MetricsAuthorizationFilter(metricsAuthorizationService, objectMapper);
  }

  @Bean
  public FilterRegistrationBean<AuthTokenAuthenticationFilter> authTokenAuthenticationFilterRegistration(
      AuthTokenAuthenticationFilter filter
  ) {
    FilterRegistrationBean<AuthTokenAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
