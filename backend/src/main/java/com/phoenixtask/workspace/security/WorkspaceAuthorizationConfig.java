package com.phoenixtask.workspace.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WorkspaceAuthorizationConfig implements WebMvcConfigurer {

  private final WorkspaceAuthorizationInterceptor authorizationInterceptor;

  public WorkspaceAuthorizationConfig(WorkspaceAuthorizationInterceptor authorizationInterceptor) {
    this.authorizationInterceptor = authorizationInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authorizationInterceptor)
        .addPathPatterns("/api/workspace/**");
  }
}
