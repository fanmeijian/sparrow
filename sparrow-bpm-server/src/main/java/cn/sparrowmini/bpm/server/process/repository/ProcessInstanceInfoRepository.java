package cn.sparrowmini.bpm.server.process.repository;

import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessInstanceInfoRepository extends JpaRepository<ProcessInstanceInfo, Long>{
    void deleteByProcessInstanceId(Long pid);
}
