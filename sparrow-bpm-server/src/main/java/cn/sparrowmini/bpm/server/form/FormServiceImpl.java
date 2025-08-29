package cn.sparrowmini.bpm.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Service
public class FormServiceImpl implements FormService {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private FormSchemaRepository formSchemaRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<FormSchemaView> getFormSchemaList(Pageable pageable) {
        return this.formSchemaRepository.findBy(pageable);
    }


    @Transactional(readOnly = true)
    @Override
    public FormSchemaInfo getFormSchema(String id) {
        return this.formSchemaRepository.findById(id, FormSchemaInfo.class).orElseThrow();
    }

    @Transactional
    @Override
    public void saveFormSchema(FormSchema formSchema) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            em.persist(formSchema);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Transactional
    @Override
    public void saveProcessForm(List<ProcessForm.ProcessFormId> processFormIds) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            processFormIds.forEach(processFormId -> {
                em.persist(new ProcessForm(processFormId));
            });

        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Transactional
    @Override
    public void saveTaskForm(List<TaskForm.TaskFormId> taskFormIds) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            taskFormIds.forEach(taskFormId -> {
                em.merge(new TaskForm(taskFormId));
            });

        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void removeProcessForm(List<ProcessForm.ProcessFormId> processFormId) {

    }

    @Override
    public void removeTaskForm(List<ProcessForm.ProcessFormId> processFormId) {

    }
}
