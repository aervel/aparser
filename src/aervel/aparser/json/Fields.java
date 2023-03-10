package aervel.aparser.json;

import java.lang.reflect.Field;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

public abstract class Fields {

    public static boolean isValid(Field field) {
        return !(isStatic(field.getModifiers()) || isTransient(field.getModifiers())) && field.trySetAccessible();
    }

}
