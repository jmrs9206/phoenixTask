package com.phoenixtask;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "phoenixtask.bootstrap.demo.enabled=false",
    "phoenixtask.controlplane.persistence.enabled=false",
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration"
})
class PhoenixTaskApplicationTests {

  @org.junit.jupiter.api.Test
  void contextLoads() {
  }
}
