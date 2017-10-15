package com.erp.lib.server.servlet.annotations;

import com.erp.lib.server.routing.ResponseType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Produces {

    ResponseType value() default ResponseType.JSON;
}
