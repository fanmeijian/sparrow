package cn.sparrowmini.common.service;

import cn.sparrowmini.common.model.dynamic.DynamicProperty;
import cn.sparrowmini.common.model.dynamic.DynamicPropertyValue;
import cn.sparrowmini.common.repository.DynamicPropertyRepository;
import cn.sparrowmini.common.repository.DynamicPropertyValueRepository;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicPropertyService {
    @Autowired
    private List<DynamicPropertyRepository<? extends DynamicProperty, ?>> dynamicPropertyRepositories;

    public DynamicProperty getDynamicProperty(Class<? extends DynamicProperty> clazz,String id) {
        return getRepository(clazz).findById(id).orElseThrow();
    }

    public void saveProperty(DynamicProperty dynamicProperty) {
        DynamicProperty.DynamicPropertyId dynamicPropertyId = new DynamicProperty.DynamicPropertyId(dynamicProperty.getEntityType(), dynamicProperty.getPropertyKey());
        DynamicProperty dynamicPropertyRef = getRepository(dynamicProperty.getClass()).getReferenceById(dynamicPropertyId);
        if(dynamicPropertyRef!=null){
            try {
                JsonUtils.getMapper().updateValue(dynamicPropertyRef, dynamicProperty);
                dynamicPropertyRef.setEntityType(dynamicProperty.getEntityType());
                dynamicPropertyRef.setPropertyKey(dynamicProperty.getPropertyKey());
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        }else {
            dynamicPropertyRef= dynamicProperty;
        }
        getRepository(dynamicProperty.getClass()).save(dynamicProperty);
    }

    public Page<? extends DynamicProperty> queryDynamicProperty(Class<? extends DynamicProperty> clazz, Pageable pageable) {
       return getRepository(clazz).findAll(pageable);
    }

    private  DynamicPropertyRepository<? extends DynamicProperty, ?> getRepository(DynamicProperty dynamicProperty) {
       return getRepository(dynamicProperty.getClass());
    }

    private <T extends DynamicProperty, ID> DynamicPropertyRepository<T, ID> getRepository(Class<? extends DynamicProperty> clazz) {
        return (DynamicPropertyRepository<T, ID>)dynamicPropertyRepositories.stream()
                .filter(f -> clazz.equals(f.domainType()))
                .findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private <T extends DynamicProperty, ID> void executeSave(DynamicProperty value) {
        // We cast the value to the repository's expected type T
        // This is safe because we filtered by domainType() in the previous step
        ((DynamicPropertyRepository<T, ID>)getRepository(value)).save((T) value);
    }

}
