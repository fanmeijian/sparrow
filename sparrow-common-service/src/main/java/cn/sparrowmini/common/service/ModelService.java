package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.Model;
import cn.sparrowmini.common.model.ModelAttribute;
import cn.sparrowmini.common.model.ModelAttributeId;
import cn.sparrowmini.common.repository.ModelAttributeRepository;
import cn.sparrowmini.common.repository.ModelRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ModelService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelAttributeRepository modelAttributeRepository;

    @Transactional
    public void synchronize() {
        List<String> ignoreFields = List.of("createdDate", "createdBy", "modifiedDate", "modifiedBy", "errMessages", "serialVersionUID","modelName","entityStat","hidden","stat","enabled","dataPermissionTokenId","id","seq");

        Set<EntityType<?>> entityTypes = this.entityManager.getMetamodel().getEntities();
        List<Model> models = new ArrayList<Model>();
        List<ModelAttribute> attributes = new ArrayList<ModelAttribute>();

        entityTypes.forEach(e -> {
            String modelId = e.getJavaType().getName();
            Model model = modelRepository.findById(modelId).orElse(new Model(modelId));

            @SuppressWarnings("unchecked")
            Set<Field> fields = ReflectionUtils.getAllFields(e.getJavaType());

            for (Field field : fields) {
                String fieldName = field.getName();
                if (!ignoreFields.contains(fieldName)) {
                    ModelAttribute modelAttribute = modelAttributeRepository.findById(new ModelAttributeId(modelId, fieldName)).orElse(new ModelAttribute(modelId, fieldName,
                            field.getType().getName()));
                    attributes.add(modelAttribute);
                }
            }
            models.add(model);
        });
        modelRepository.saveAll(models);
        modelAttributeRepository.saveAll(attributes);
    }
}
