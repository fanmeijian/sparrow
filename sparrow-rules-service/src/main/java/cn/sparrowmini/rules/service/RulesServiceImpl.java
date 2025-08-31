package cn.sparrowmini.rules.service;

import cn.sparrowmini.rules.model.RulesAttribute;
import cn.sparrowmini.rules.model.RulesModel;
import cn.sparrowmini.rules.model.RulesModelBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RulesServiceImpl implements RulesService {

    @Override
    public List<RulesModelBean> models(List<String> packages) {
//		Class<?> userClass = ClassUtils.getUserClass(RulesModel.class);
        List<RulesModelBean> rulesModelBeans = new ArrayList<>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(RulesModel.class));
        List<BeanDefinition> beanDefs = new ArrayList<>();
        packages.forEach(package_ -> {
            beanDefs.addAll(provider.findCandidateComponents(package_));
        });

        List<String> annotatedBeans = new ArrayList<>();
        for (BeanDefinition bd : beanDefs) {
            if (bd instanceof AnnotatedBeanDefinition) {
                Map<String, Object> annotAttributeMap = ((AnnotatedBeanDefinition) bd).getMetadata()
                        .getAnnotationAttributes(RulesModel.class.getCanonicalName());
                annotatedBeans.add(annotAttributeMap.get("value").toString());
                System.out.print(bd.getBeanClassName());
                RulesModelBean rulesModelBean = new RulesModelBean(annotAttributeMap.get("value").toString(),
                        bd.getBeanClassName());
                try {
                    Class<?> userClass = Class.forName(bd.getBeanClassName());
                    List<RulesModelBean.Attribute> attributes = Arrays.stream(userClass.getDeclaredFields())
                            .filter(f -> AnnotationUtils.getAnnotation(f, RulesAttribute.class) != null)
                            .map(m -> new RulesModelBean.Attribute(
                                    m.getDeclaredAnnotation(RulesAttribute.class).value(), m.getName()))
                            .collect(Collectors.toList());
                    rulesModelBean.getAttributes().addAll(attributes);

                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                rulesModelBeans.add(rulesModelBean);
            }
        }

        return rulesModelBeans;
    }

}
