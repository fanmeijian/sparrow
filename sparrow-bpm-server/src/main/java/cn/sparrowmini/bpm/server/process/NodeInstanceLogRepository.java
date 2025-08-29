package cn.sparrowmini.bpm.server.process;

import org.jbpm.process.audit.NodeInstanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeInstanceLogRepository extends JpaRepository<NodeInstanceLog, Long> {
    Page<NodeInstanceLog> findByProcessInstanceId(long processInstanceId, Pageable pageable);

    void deleteByProcessInstanceId(Long pid);
}
