//package cn.sparrowmini.common.service;
//
//import cn.sparrowmini.common.model.dynamic.DynamicPropertyValue;
//import cn.sparrowmini.common.repository.DynamicPropertyValueRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class DynamicPropertyValueService {
//
//    @Autowired
//    private List<DynamicPropertyValueRepository<?,?>> dynamicPropertyValueRepositories;
//
//
//    public void save(DynamicPropertyValue<?, ?> dynamicPropertyValue){
//        DynamicPropertyValueRepository<?,?> dynamicPropertyValueRepository = dynamicPropertyValueRepositories.stream()
//                .filter(f->dynamicPropertyValue.getClass().equals(f.domainType()))
//                .findFirst().orElseThrow();
//
//        executeSave(dynamicPropertyValueRepository, dynamicPropertyValue);
//    }
//
//
////    public <DynamicProperty,ID> List<DynamicPropertyValue<?, ?>> findAll(ID businessId){
////
////    }
//
//    @SuppressWarnings("unchecked")
//    private <T extends DynamicPropertyValue<?, ?>, ID> void executeSave(DynamicPropertyValueRepository<T, ID> repository, DynamicPropertyValue<?, ?> value) {
//        // We cast the value to the repository's expected type T
//        // This is safe because we filtered by domainType() in the previous step
//        repository.save((T) value);
//    }
//}
