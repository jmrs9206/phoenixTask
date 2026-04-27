package com.phoenixtask.iam.repository;

import com.phoenixtask.iam.model.PasswordResetToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PasswordResetToken> rowMapper = (rs, rowNum) -> new PasswordResetToken(
        rs.getLong("id"),
        rs.getLong("user_id"),
        rs.getString("token_hash"),
        rs.getTimestamp("expires_at").toLocalDateTime(),
        rs.getTimestamp("used_at") != null ? rs.getTimestamp("used_at").toLocalDateTime() : null,
        rs.getTimestamp("created_at").toLocalDateTime()
    );

    public PasswordResetTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        var results = jdbcTemplate.query("SELECT * FROM password_reset_tokens WHERE token_hash = ?", rowMapper, tokenHash);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public PasswordResetToken save(PasswordResetToken token) {
        if (token.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, used_at) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
                ps.setLong(1, token.userId());
                ps.setString(2, token.tokenHash());
                ps.setObject(3, token.expiresAt());
                ps.setObject(4, token.usedAt());
                return ps;
            }, keyHolder);
            return jdbcTemplate.queryForObject("SELECT * FROM password_reset_tokens WHERE id = ?", rowMapper, keyHolder.getKey().longValue());
        } else {
            jdbcTemplate.update(
                "UPDATE password_reset_tokens SET used_at=? WHERE id=?",
                token.usedAt(), token.id()
            );
            return token;
        }
    }
}
