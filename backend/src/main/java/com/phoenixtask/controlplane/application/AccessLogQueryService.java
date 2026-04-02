package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.infrastructure.persistence.AccessLogEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.AccessLogJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AccessLogQueryService {

  private final AccessLogJpaRepository repository;

  public AccessLogQueryService(AccessLogJpaRepository repository) {
    this.repository = repository;
  }

  public List<AccessLogEntity> query(
      String tenantCode,
      String path,
      Integer status,
      LocalDateTime from,
      LocalDateTime to,
      int limit
  ) {
    Specification<AccessLogEntity> spec = (root, query, cb) -> {
      List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
      if (tenantCode != null && !tenantCode.isBlank()) {
        predicates.add(cb.equal(root.get("tenantCode"), tenantCode));
      }
      if (path != null && !path.isBlank()) {
        predicates.add(cb.like(root.get("path"), "%" + path + "%"));
      }
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (from != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
      }
      if (to != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
      }
      return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
    return repository.findAll(spec, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
        .getContent();
  }
}
