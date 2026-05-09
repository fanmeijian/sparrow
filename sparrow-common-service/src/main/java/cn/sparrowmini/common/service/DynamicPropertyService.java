package cn.sparrowmini.common.service;

import cn.sparrowmini.common.SprCache;
import cn.sparrowmini.common.model.BaseTree;
import cn.sparrowmini.common.model.Dict;
import cn.sparrowmini.common.model.Dict_;
import cn.sparrowmini.common.model.dynamic.*;
import cn.sparrowmini.common.repository.DictRepository;
import cn.sparrowmini.common.repository.DynamicPropertyRepository;
import cn.sparrowmini.common.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.annotation.Resource;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.EntityManager;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicPropertyService {
    @Autowired
    SprCache cache;

    @Resource
    private EntityManager entityManager;

    @Autowired
    private List<DynamicPropertyRepository<? extends DynamicProperty, ?>> dynamicPropertyRepositories;

    @Autowired
    private DynamicPropertyRepository<DynamicProperty, DynamicPropertyId> dynamicPropertyRepository;


    @Autowired
    DictRepository dictRepository;

    @Resource
    private DynamicPropertyRepository<? extends DynamicProperty, DynamicPropertyId> dynamicPropertyRepository1;

    public Page<DynamicProperty> getDynamicPropertyList(Pageable pageable, String filter) {
        return dynamicPropertyRepository.findAll(pageable, filter);
    }


    public <T extends DynamicProperty> void saveDynamicProperty(String entityType, Map<String, Object> dynamicProperty) {

        Class<T> dynamicPropertyClass = (Class<T>) cache.getEntityClass(entityType);
        String propertyKey = dynamicProperty.get("propertyKey").toString();

        T entity = null;

        if (dynamicPropertyRepository.existsByKey(entityType, propertyKey)) {
            entity = (T) dynamicPropertyRepository.getReferenceById(new DynamicPropertyId(entityType, propertyKey));
            try {
                JsonUtils.getMapper().updateValue(entity, dynamicProperty);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        } else {
            entity = JsonUtils.getMapper().convertValue(dynamicProperty, dynamicPropertyClass);
        }
        dynamicPropertyRepository.save(entity);
    }

    public <T extends DynamicProperty> void saveDynamicProperty(T dynamicProperty) {
        dynamicPropertyRepository.save(dynamicProperty);
    }

    public DynamicProperty getDynamicProperty(DynamicPropertyId id) {
        return dynamicPropertyRepository1.findById(id).orElseThrow();
    }

    public <T extends DynamicProperty> T getDynamicProperty(Class<? extends DynamicProperty> clazz, DynamicPropertyId id) {
        return (T) getRepository(clazz).findById(id).orElseThrow();
    }

    public <T extends DynamicProperty> T getDynamicProperty(Class<? extends DynamicProperty> clazz, String propertyKey) {
        DiscriminatorValue dv = clazz.getAnnotation(DiscriminatorValue.class);
        if (dv == null) {
            throw new IllegalStateException("实体类 " + clazz.getSimpleName() + " 缺少 @DiscriminatorValue 注解");
        }
        String entityType = dv.value();
        DynamicPropertyId id = new DynamicPropertyId(entityType, propertyKey);
//        return getRepository(clazz).findById(id).orElseThrow();
        return (T) dynamicPropertyRepository1.findById(id).orElseThrow();
    }

    public <T extends DynamicProperty> List<ProviderDataValue> getProviderDataValue(T dynamicProperty) {
        final DynamicPropertyValueProviderType providerType = dynamicProperty.getProviderType();
        if (providerType == null) {
            return new ArrayList<>();
        }
        final List<DynamicProperty.ProviderData> providerData = dynamicProperty.getProviderData();
        final String providerScript = dynamicProperty.getProviderScript();
        final DynamicPropertyTypeEnum type = dynamicProperty.getType();

        List<DynamicProperty.ProviderData> list = new ArrayList<>();
        List<ProviderDataValue> list2 = new ArrayList<>();
        Map<String, Object> vars = new HashMap<>();
        // 将 ProviderData 的 Class 对象传进去，脚本里可以直接用
        vars.put("ProviderData", cn.sparrowmini.common.model.dynamic.DynamicProperty.ProviderData.class);
        switch (providerType) {
            case SCRIPT:
                list = (List<DynamicProperty.ProviderData>) MVEL.eval(providerScript, vars);
                break;
            case DICT:
                List<Dict> dicts = dictRepository.getAllChildren(dynamicProperty.getUrl(), PageRequest.of(0, Integer.MAX_VALUE).withSort(Sort.by(Sort.Order.asc(Dict_.SEQ)))).getContent();
                convertDict2ProviderData(dicts, list2,type);
                return list2;
            default:
                list = providerData;
                break;
        }


        if (type.equals(DynamicPropertyTypeEnum.Integer)) {
            list2 = list.stream().map(m -> new ProviderDataValue(m.getLabel(), Integer.parseInt(m.getValue()))).collect(Collectors.toList());

        } else {
            list2 = list.stream().map(m -> new ProviderDataValue(m.getLabel(), m.getValue())).collect(Collectors.toList());
        }
        return list2;
    }

    private void convertDict2ProviderData(List<?> dicts, List<ProviderDataValue> list2, DynamicPropertyTypeEnum type) {
        dicts.forEach(dict_->{
            Dict dict =  (Dict) dict_;
            ProviderDataValue providerDataValue = new ProviderDataValue();
            providerDataValue.setLabel(dict.getName());
            Object value = type.equals(DynamicPropertyTypeEnum.Integer)?Integer.parseInt(dict.getCode()): dict.getCode() ;
            providerDataValue.setValue(value);
            list2.add(providerDataValue);
            providerDataValue.setChildren(new ArrayList<>());
            if(!dict.getChildren().isEmpty()){
                providerDataValue.setChildCount(dict.getChildren().size());
                convertDict2ProviderData(dict.getChildren(),providerDataValue.getChildren(),type);
            }
        });

    }

    @Transactional
    public void deleteDynamicProperty(Collection<DynamicPropertyId> ids) {
        dynamicPropertyRepository.deleteAllById(ids);
    }

    @Transactional
    public void saveProperty(DynamicProperty dynamicProperty) {
        DynamicPropertyRepository<? extends DynamicProperty, DynamicPropertyId> repository = this.getRepository(dynamicProperty.getClass());
        DynamicProperty dynamicPropertyRef = dynamicProperty;
        if (repository.existsByKey(dynamicProperty.getPropertyKey())) {
            DynamicPropertyId dynamicPropertyId = new DynamicPropertyId(dynamicProperty.getEntityType(), dynamicProperty.getPropertyKey());
            dynamicPropertyRef = repository.getReferenceById(dynamicPropertyId);
            try {
                JsonUtils.getMapper().updateValue(dynamicPropertyRef, dynamicProperty);
                dynamicPropertyRef.setEntityType(dynamicProperty.getEntityType());
                dynamicPropertyRef.setPropertyKey(dynamicProperty.getPropertyKey());
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        }
        getRepository(dynamicProperty.getClass()).save(dynamicProperty);
    }

    public List<? extends DynamicProperty> queryDynamicPropertyByKeys(Class<? extends DynamicProperty> clazz, Collection<String> keys) {
        return getRepository(clazz).findByKeys(keys);
    }

    public Page<? extends DynamicProperty> queryDynamicProperty(Class<? extends DynamicProperty> clazz, Pageable pageable) {
//        return getRepository(clazz).findAll(pageable);
        String discriminator = clazz.getAnnotation(DiscriminatorValue.class).value();
        Specification<DynamicProperty> specification = dynamicPropertyRepository.specificationEqual(DynamicProperty_.ENTITY_TYPE, discriminator);
        return dynamicPropertyRepository.findAll(specification, pageable);
    }

    private DynamicPropertyRepository<? extends DynamicProperty, ?> getRepository(DynamicProperty dynamicProperty) {
        return getRepository(dynamicProperty.getClass());
    }

    private <T extends DynamicProperty, ID> DynamicPropertyRepository<T, ID> getRepository(Class<? extends DynamicProperty> clazz) {
        return (DynamicPropertyRepository<T, ID>) dynamicPropertyRepositories.stream()
                .filter(f -> clazz.equals(f.domainType()))
                .findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private <T extends DynamicProperty, ID> void executeSave(DynamicProperty value) {
        // We cast the value to the repository's expected type T
        // This is safe because we filtered by domainType() in the previous step
        ((DynamicPropertyRepository<T, ID>) getRepository(value)).save((T) value);
    }

}
