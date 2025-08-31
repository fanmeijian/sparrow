package cn.sparrowmini.form.repository;

import cn.sparrowmini.form.model.SparrowForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository extends JpaRepository<SparrowForm,String> {
    SparrowForm getReferenceByCode(String formId);
}
