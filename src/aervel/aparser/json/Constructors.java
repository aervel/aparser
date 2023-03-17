package aervel.aparser.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Constructors {

    /**
     * Uses the fields from same class to create and return a constructor. The returned constructor has the same type
     * of parameters in the same order with fields arguments. Those fields must be valid:
     * <ul>
     *     <li>Non-static</li>
     *     <li>Non-transient</li>
     *     <li>Accessible for reflection</li>
     * </ul>
     * <p>
     * The returned constructor is accessible, else an exception is thrown.
     *
     * @param fields The valid fields of class from where the constructor is expected.
     * @param <T>    The type of constructor.
     * @return A constructor which parameter types matches with fields.
     * @throws IllegalArgumentException If fields doesn't have at least a field or if constructor is inaccessible.
     * @throws RuntimeException         If constructor wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> of(Field... fields) throws IllegalArgumentException, RuntimeException {
        try {

            if (fields.length == 0) {
                throw new IllegalArgumentException("fields.length == 0");
            }

            Class<T> cls = (Class<T>) fields[0].getDeclaringClass();
            Class<?>[] parameterTypes = Arrays.stream(fields).map(Field::getType).toArray(Class[]::new);
            Constructor<T> constructor = cls.getDeclaredConstructor(parameterTypes);

            if (constructor.trySetAccessible()) {
                return constructor;
            }

            throw new IllegalArgumentException();

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
