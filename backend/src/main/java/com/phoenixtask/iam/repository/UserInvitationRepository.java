package com.phoenixtask.iam.repository;

import com.phoenixtask.iam.model.UserInvitation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
public class UserInvitationRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserInvitation> rowMapper = (rs, rowNum) -> new UserInvitation(
        rs.getLong("id"),
        rs.getString("email"),
        rs.getString("token_hash"),
        rs.getTimestamp("expires_at").toLocalDateTime(),
        rs.getTimestamp("used_at") != null ? rs.getTimestamp("used_at").toLocalDateTime() : null,
        rs.getLong("invited_by_user_id"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );

    public UserInvitationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserInvitation> findByTokenHash(String tokenHash) {
        var results = jdbcTemplate.query("SELECT * FROM user_invitations WHERE token_hash = ?", rowMapper, tokenHash);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public UserInvitation save(UserInvitation invitation) {
        if (invitation.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO user_invitations (email, token_hash, expires_at, used_at, invited_by_user_id) VALUES (?, ?, ?, ?, ?)",
                    new String[]{"id"});
                ps.setString(1, invitation.email());
                ps.setString(2, invitation.tokenHash());
                ps.setObject(3, invitation.expiresAt());
                ps.setObject(4, invitation.usedAt());
                ps.setLong(5, invitation.invitedByUserId());
                return ps;
            }, keyHolder);
            // return with id
            return jdbcTemplate.queryForObject("SELECT * FROM user_invitations WHERE id = ?", rowMapper, keyHolder.getKey().longValue());
        } else {
            jdbcTemplate.update(
                "UPDATE user_invitations SET used_at=? WHERE id=?",
                invitation.usedAt(), invitation.id()
            );
            return invitation;
        }
    }
}
