package com.tsong.cmall.config.annotation;

import com.tsong.cmall.common.Constants;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {
    int lockTime() default Constants.REPEAT_INTERVAL_TIME;
}
