package cn.sparrowmini.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestAuditLogRepository extends JpaRepository<RequestAuditLog, String> {
}
