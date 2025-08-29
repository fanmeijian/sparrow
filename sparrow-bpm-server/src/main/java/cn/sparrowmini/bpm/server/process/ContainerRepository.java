package cn.sparrowmini.bpm.server.process;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerRepository extends JpaRepository<Container, Container.ContainerId> {
}
