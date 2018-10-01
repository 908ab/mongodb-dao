package miyakawalab.tool.mongo.entity;

import com.google.common.base.CaseFormat;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class Util {
    private Util() {}

    static boolean hasAnnotation(Field field, Class<? extends Annotation> annotationType) {
        for (Annotation annotation : field.getAnnotations()) {
            if (AnnotationUtils.getAnnotation(annotation, annotationType) != null) {
                return true;
            }
        }
        return false;
    }

    public static String camelToSnake(String camelString) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelString);
    }
}
