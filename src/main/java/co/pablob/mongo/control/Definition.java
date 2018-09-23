package co.pablob.mongo.control;

import org.bson.Document;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Definition {
    @Nonbinding
    String value() default "";

    @Nonbinding
    Class clazz() default Document.class;
}
