package cn.sparrowmini.common.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制所有的权限,放到2.0版本实现吧
 * @author fansword
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScopePermission {
	String username() default "$curUser";
	String scope();
	String name();
}
