package com.hcanyz.environmentvariable.base.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface EvGroup {
    String defaultVariant() default "";

    // Used for not wanting to expose too much information
    boolean hideNonDefault() default false;
}
