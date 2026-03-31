package com.phoenixtask;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "phoenixtask.bootstrap.demo.enabled=false",
    "phoenixtask.controlplane.persistence.enabled=false",
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
class PhoenixTaskApplicationTests {

  @org.junit.jupiter.api.Test
  void contextLoads() {
  }
}
