package aervel.aparser.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Constructors {

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> of(Field... fields) {
        try {

            if (fields.length == 0) {
                throw new IllegalArgumentException();
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
