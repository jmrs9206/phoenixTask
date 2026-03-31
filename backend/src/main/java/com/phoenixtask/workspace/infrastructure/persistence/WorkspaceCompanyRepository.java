package com.phoenixtask.workspace.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.phoenixtask.workspace.infrastructure.datasource.WorkspaceJdbcTemplateProvider;

@Repository
public class WorkspaceCompanyRepository {

  private final WorkspaceJdbcTemplateProvider jdbcTemplateProvider;

  public WorkspaceCompanyRepository(WorkspaceJdbcTemplateProvider jdbcTemplateProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
  }

  public Optional<CompanyWithSettingsRow> fetchCompanyWithSettings() {
    String sql = """
        SELECT
          c.id AS company_id,
          c.code AS company_code,
          c.name AS company_name,
          c.status AS company_status,
          c.owner_user_id AS owner_user_id,
          c.created_at AS company_created_at,
          c.updated_at AS company_updated_at,
          s.id AS settings_id,
          s.timezone AS settings_timezone,
          s.locale AS settings_locale,
          s.week_start AS settings_week_start,
          s.created_at AS settings_created_at,
          s.updated_at AS settings_updated_at
        FROM company c
        JOIN company_settings s ON s.company_id = c.id
        LIMIT 1
        """;

    JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate();
    return jdbcTemplate.query(sql, mapper()).stream().findFirst();
  }

  public Long getCompanyId() {
    String sql = "SELECT id FROM company LIMIT 1";
    return jdbcTemplateProvider.getJdbcTemplate().query(
        sql,
        rs -> rs.next() ? rs.getLong("id") : null
    );
  }

  private RowMapper<CompanyWithSettingsRow> mapper() {
    return (rs, rowNum) -> new CompanyWithSettingsRow(
        rs.getLong("company_id"),
        rs.getString("company_code"),
        rs.getString("company_name"),
        rs.getString("company_status"),
        rs.getLong("owner_user_id"),
        rs.getTimestamp("company_created_at").toLocalDateTime(),
        rs.getTimestamp("company_updated_at").toLocalDateTime(),
        rs.getLong("settings_id"),
        rs.getString("settings_timezone"),
        rs.getString("settings_locale"),
        rs.getString("settings_week_start"),
        rs.getTimestamp("settings_created_at").toLocalDateTime(),
        rs.getTimestamp("settings_updated_at").toLocalDateTime()
    );
  }

  public record CompanyWithSettingsRow(
      Long companyId,
      String companyCode,
      String companyName,
      String companyStatus,
      Long ownerUserId,
      LocalDateTime companyCreatedAt,
      LocalDateTime companyUpdatedAt,
      Long settingsId,
      String settingsTimezone,
      String settingsLocale,
      String settingsWeekStart,
      LocalDateTime settingsCreatedAt,
      LocalDateTime settingsUpdatedAt
  ) {}
}
