package cn.sparrowmini.owl.jpa.repository;

import cn.sparrowmini.owl.jpa.model.OwlRelation;
import cn.sparrowmini.owl.jpa.model.OwlRelationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwlRelationRepository extends JpaRepository<OwlRelation, String> {
    List<OwlRelation> findByTargetCodeAndRelationType(String classCode, OwlRelationType owlRelationType);
}
