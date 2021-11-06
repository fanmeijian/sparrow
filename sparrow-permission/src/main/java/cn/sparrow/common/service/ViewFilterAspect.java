package cn.sparrow.common.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.sparrow.model.common.AbstractSparrowEntity;

@Aspect
@Component
public class ViewFilterAspect {
  @Autowired
  ViewService viewService;

  @Around("@annotation(ViewFilter)")
  public Object filterNoReadObject(ProceedingJoinPoint joinPoint) throws Throwable {
    viewService.filterNoReadObject((Iterable<AbstractSparrowEntity>) joinPoint.proceed());
    return joinPoint.proceed();
  }
}