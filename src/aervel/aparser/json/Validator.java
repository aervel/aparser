package aervel.aparser.json;

import java.lang.reflect.Field;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

abstract class Validator {

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
    protected static boolean isValid(Field field) {
        return !isTransient(field.getModifiers()) && !isStatic(field.getModifiers()) && field.trySetAccessible();
    }
}
