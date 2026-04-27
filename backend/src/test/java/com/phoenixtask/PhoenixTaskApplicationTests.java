package com.phoenixtask;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhoenixTaskApplicationTests {

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }
}
