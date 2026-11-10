package cn.sparrowmini.security.spring;

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface RequiresPermission {String resource();String scope();}
