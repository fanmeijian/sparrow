package cn.sparrowmini.bpm.server.repository;

import cn.sparrowmini.bpm.server.process.GlobalVariableMap;
import cn.sparrowmini.bpm.server.process.model.GlobalVariable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariableMapRepository extends JpaRepository<GlobalVariable,String> {
    GlobalVariable findByContainerIdAndCode(String containerId, String code);
}
