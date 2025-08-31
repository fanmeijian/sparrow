package cn.sparrowmini.form.repository;

import cn.sparrowmini.form.model.SparrowForm;
import cn.sparrowmini.form.model.SparrowFormData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormDataRepository extends JpaRepository<SparrowFormData,String> {
    Page<SparrowFormData> findByFormId(String formId, Pageable pageable);
}
