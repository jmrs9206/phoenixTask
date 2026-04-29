package com.phoenixtask.iam.repository;

import com.phoenixtask.iam.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("display_name"),
            rs.getString("password_hash"),
            rs.getString("status"),
            rs.getBoolean("is_platform_internal"),
            rs.getBoolean("must_change_password"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
            rs.getTimestamp("activated_at") != null ? rs.getTimestamp("activated_at").toLocalDateTime() : null);

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByEmail(String email) {
        var results = jdbcTemplate.query("SELECT * FROM users WHERE email = ?", rowMapper, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public Optional<User> findById(Long id) {
        var results = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public java.util.List<String> findRolesByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT r.code FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = ?",
                (rs, rowNum) -> rs.getString("code"),
                userId);
    }

    public java.util.List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id DESC", rowMapper);
    }

    public void addRole(Long userId, String roleCode) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE code = ?",
                userId, roleCode);
    }

    public void removeRole(Long userId, String roleCode) {
        jdbcTemplate.update(
                "DELETE ur FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = ? AND r.code = ?",
                userId, roleCode);
    }

    public User save(User user) {
        if (user.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO users (email, display_name, password_hash, status, is_platform_internal, must_change_password, activated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        new String[] { "id" });
                ps.setString(1, user.email());
                ps.setString(2, user.displayName());
                ps.setString(3, user.passwordHash());
                ps.setString(4, user.status());
                ps.setBoolean(5, user.isPlatformInternal());
                ps.setBoolean(6, user.mustChangePassword());
                ps.setObject(7, user.activatedAt());
                return ps;
            }, keyHolder);
            return findById(keyHolder.getKey().longValue()).orElseThrow();
        } else {
            jdbcTemplate.update(
                    "UPDATE users SET email=?, display_name=?, password_hash=?, status=?, is_platform_internal=?, must_change_password=?, activated_at=? WHERE id=?",
                    user.email(), user.displayName(), user.passwordHash(), user.status(), user.isPlatformInternal(),
                    user.mustChangePassword(), user.activatedAt(), user.id());
            return user;
        }
    }
}
