package aervel.aparser.json;

import java.lang.reflect.Field;
import java.util.Arrays;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

/**
 * Provide a set of methods to work with fields.
 */
public abstract class Fields {

    /**
     * Returns true if the field is valid or false otherwise. A field is valid if:
     * <ul>
     *     <li>Is not static</li>
     *     <li>Is not transient</li>
     *     <li>Is accessible for reflection</li>
     * </ul>
     * If at least one of those conditions are false the field is considered not valid.
     *
     * @param field The field to be validated.
     * @return True if the field is valid or false otherwise.
     * @throws NullPointerException If field references to null.
     */
    public static boolean isValid(Field field) throws NullPointerException {
        return !(isStatic(field.getModifiers()) || isTransient(field.getModifiers())) && field.trySetAccessible();
    }


    public static Field[] of(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredFields()).filter(Fields::isValid).toArray(Field[]::new);
    }

}
