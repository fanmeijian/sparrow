//package cn.sparrowmini.common.repository;
//
//import cn.sparrowmini.common.model.BaseTree;
//import org.springframework.aop.framework.AopProxyUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//@Component
//public class TreeRepositoryFactory {
//
//    private final Map<Class<?>, BaseTreeRepository<?>> repositoryMap = new HashMap<>();
//
//    @Autowired
//    public TreeRepositoryFactory(List<BaseTreeRepository<?>> repositories) {
//        for (BaseTreeRepository<?> repo : repositories) {
//            Class<?> domainClass = getGenericEntityType(repo);
//            if (domainClass != null) {
//                repositoryMap.put(domainClass, repo);
//            }
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T extends BaseTree> BaseTreeRepository<T> getRepository(Class<?> clazz) {
//        return (BaseTreeRepository<T>) repositoryMap.get(clazz);
//    }
//
//    @SuppressWarnings("unchecked")
//    public static Class<?> getGenericEntityType(BaseTreeRepository<?> repo) {
//        for (Class<?> intf : AopProxyUtils.proxiedUserInterfaces(repo)) {
//            if (BaseTreeRepository.class.isAssignableFrom(intf)) {
//                Type[] genericInterfaces = intf.getGenericInterfaces();
//                for (Type genericInterface : genericInterfaces) {
//                    if (genericInterface instanceof ParameterizedType pt) {
//                        if (BaseTreeRepository.class.equals(pt.getRawType())) {
//                            Type typeArg = pt.getActualTypeArguments()[0];
//                            if (typeArg instanceof Class<?> entityClass) {
//                                return entityClass;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
//}