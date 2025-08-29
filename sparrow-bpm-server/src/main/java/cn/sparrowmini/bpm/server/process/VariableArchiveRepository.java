package cn.sparrowmini.bpm.server.process;

import cn.sparrowmini.bpm.server.common.VariableArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariableArchiveRepository extends JpaRepository<VariableArchive, VariableArchive.VariableArchiveId> {
}
