package aervel.aparser.json;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.stream.Collectors;
import java.util.Collection;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.Arrays.*;

public final class Stringifier {

    /**
     * Converts a Java object to its JSON representation and return it as String.
     *
     * @param object The object to be converted to JSON.
     * @return A string with JSON representation of underlying object.
     */
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
                    stream(array).map(Stringifier::stringify).collect(Collectors.joining(","))
            );
        }

        if (object instanceof Collection<?> collection) {
            return "[%s]".formatted(
                    collection.stream().map(Stringifier::stringify).collect(Collectors.joining(","))
            );
        }

        /*
         * The last return is for object represented with {} brackets in JSON. The return can be an empty JSON object
         * {}, but never a null object.
         */
        return "{%s}".formatted(stream(object.getClass().getDeclaredFields()).filter(Stringifier::isValid)
                .map(field -> {
                    try {
                        return "\"%s\":%s".formatted(field.getName(), stringify(field.get(object)));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.joining(",")));
    }

    /**
     * Validate the field according to following conditions:
     * <ul>
     *     <li>The field is not {@code transient}</li>
     *     <li>The field is not {@code static}</li>
     *     <li>The field is accessible</li>
     * </ul>
     * <p>
     * If all of those conditions are true, true is returned otherwise, false is returned.
     *
     * @param field The field to be validated.
     * @return True if this is field is accessible, not transient and not static and false otherwise.
     */
    private static boolean isValid(Field field) {
        return !isTransient(field.getModifiers()) && !isStatic(field.getModifiers()) && field.trySetAccessible();
    }
}
