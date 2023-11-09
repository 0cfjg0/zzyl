package com.zzyl.base;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    String deptAlias() default "";

    String userAlias() default "";
}
