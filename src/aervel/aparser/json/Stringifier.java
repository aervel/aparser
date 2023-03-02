package aervel.aparser.json;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

public abstract class Stringifier {

    public static String stringify(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Number) {
            return object.toString();
        }

        if (object instanceof Temporal || object instanceof String) {
            return "\"%s\"".formatted(object);
        }

        if (object instanceof Object[] array) {
            return "[%s]".formatted(
                    Arrays.stream(array).map(Stringifier::stringify).collect(Collectors.joining(","))
            );
        }

        if (object instanceof Collection<?> collection) {
            return "[%s]".formatted(
                    collection.stream().map(Stringifier::stringify).collect(Collectors.joining(","))
            );
        }

        return "{%s}".formatted(
                Arrays.stream(object.getClass().getDeclaredFields())
                        .filter(Stringifier::isValid)
                        .map(field -> {
                            try {
                                return "\"%s\":%s".formatted(field.getName(), stringify(field.get(object)));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.joining(","))
        );
    }

    private static boolean isValid(Field field) {
        return !isTransient(field.getModifiers()) && !isStatic(field.getModifiers()) && field.trySetAccessible();
    }
}
