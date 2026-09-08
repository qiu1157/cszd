package com.changsha.zhongdian.operationlog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {
    OperationTargetType targetType();

    OperationType operationType();

    String targetIdSpEL() default "";

    String targetNameSpEL() default "";

    String stationIdSpEL() default "";

    String remarkSpEL() default "";

    boolean captureBeforeState() default false;
}
