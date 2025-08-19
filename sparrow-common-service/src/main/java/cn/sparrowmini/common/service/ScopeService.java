package cn.sparrowmini.common.service;
import cn.sparrowmini.common.model.Scope;
import cn.sparrowmini.common.repository.ScopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScopeService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ScopeRepository scopeRepository;

    @Transactional
    public void synchronize() {
        List<Scope> scopes = new ArrayList<>();

        // 1. 获取所有 Spring 管理的 bean 名称
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = bean.getClass();

            // 如果是代理类，拿到真实的目标类
            if (targetClass.getName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            // 2. 遍历类的方法
            for (Method method : targetClass.getDeclaredMethods()) {
                ScopePermission annotation = method.getAnnotation(ScopePermission.class);
                if (annotation != null) {
                    String scopeCode = annotation.scope();
                    String scopeName = annotation.name() == null ? "" : annotation.name();

                    // 3. 同步到数据库
                    scopeRepository.findByCode(scopeCode).ifPresentOrElse(
                            existing -> {
                                existing.setName(scopeName);
                                scopes.add(existing);
                            },
                            () -> scopes.add(new Scope(scopeName, scopeCode))
                    );
                }
            }
        }

        // 4. 批量保存
        scopeRepository.saveAll(scopes);
    }
}