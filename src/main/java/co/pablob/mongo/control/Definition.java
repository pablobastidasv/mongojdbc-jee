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

    /**
     * Define the name of the collection.
     */
    @Nonbinding
    String collection() default "";

    /**
     * Define the Class that the collection going to handle.
     *
     * Default value is {@link Document}
     */
    @Nonbinding
    Class clazz() default Document.class;
}
