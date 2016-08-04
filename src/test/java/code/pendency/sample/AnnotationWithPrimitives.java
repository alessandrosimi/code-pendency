package code.pendency.sample;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface AnnotationWithPrimitives {
    int intValue();
    boolean booleanValue();
}
