package aervel.aparser.json;

import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.temporal.Temporal;
import java.util.*;

import static java.util.Arrays.stream;

public class Parser {
    private final Carriage carriage;

    public Parser(Carriage carriage) {
        this.carriage = carriage;
    }

    public static <T> T parse(String json, Class<T> type) {
        return new Parser(new Carriage(json)).parse(type);
    }

    private <T> T parse(Class<T> type) {
        try {
            return type.cast(parse(parse(), type));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ParseException |
                 ClassNotFoundException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object parse(Object object, Type type)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException, ClassNotFoundException, InstantiationException {

        if (object instanceof String value && type instanceof Class<?> cls) {
            if (Number.class.isAssignableFrom(cls)) {
                return cls.getDeclaredMethod("valueOf", String.class).invoke(null, value);
            }

            value = value.substring(1, value.length() - 1); // cut ""

            if (Date.class.isAssignableFrom(cls)) {
                return DateFormat.getInstance().parse(value);
            }

            if (String.class.isAssignableFrom(cls)) {
                return value;
            }

            if (Temporal.class.isAssignableFrom(cls)) {
                return cls.getDeclaredMethod("parse", CharSequence.class).invoke(null, value);
            }
        }

        if (object instanceof Map<?, ?> map && type instanceof Class<?> cls) {
            Field[] fields = stream(cls.getDeclaredFields()).filter(Validator::isValid).toArray(Field[]::new);

            Constructor<?> constructor = cls.getDeclaredConstructor(
                    stream(fields).map(Field::getType).toArray(Class[]::new)
            );

            Object[] arguments = new Object[fields.length];

            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = parse(map.get(fields[i].getName()), fields[i].getGenericType());
            }

            return constructor.newInstance(arguments);
        }

        if (object instanceof List<?> list) {

            if (type instanceof Class<?> cls) {

                if (Collection.class.isAssignableFrom(cls)) {
                    return cls.getDeclaredMethod("copyOf", Collection.class).invoke(null, list);
                }

                if (cls.isArray()) {
                    Class<?> componentType = cls.componentType();
                    Object[] array = (Object[]) Array.newInstance(componentType, list.size());

                    for (int i = 0; i < array.length; i++) {
                        array[i] = parse(list.get(i), componentType);
                    }

                    return array;
                }
            }

            if (type instanceof ParameterizedType parameterizedType) {
                Class<?> cls = Class.forName(type.getTypeName().split("<")[0]);

                if (!Collection.class.isAssignableFrom(cls)) {
                    throw new IllegalArgumentException("Collection expected");
                }

                Class<?> componentType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                Object[] array = (Object[]) Array.newInstance(componentType, list.size());

                for (int i = 0; i < array.length; i++) {
                    array[i] = parse(list.get(i), componentType);
                }

                if (cls.isInterface() && List.class.isAssignableFrom(cls)) {
                    return List.of(array);
                }

                if (cls.isInterface() && Set.class.isAssignableFrom(cls)) {
                    return Set.of(array);
                }

                return cls.getDeclaredConstructor(Collection.class).newInstance(Arrays.asList(array));
            }
        }

        throw new IllegalArgumentException();
    }

    Object parse() {
        carriage.clean();

        if (carriage.get() == '[') {
            return list();
        }

        if (carriage.get() == '{') {
            return map();
        }

        throw new IllegalArgumentException(
                "Unexpected token %s at position %d".formatted(carriage.get(), carriage.position())
        );
    }

    private List<Object> list() {
        List<Object> list = new ArrayList<>();

        while (carriage.get() != ']') {

            carriage.next(); // skip the : or [ characters
            carriage.clean(); // skip all "\r\n\t " after : or [ characters

            if ("{[".contains(carriage.get().toString())) {
                list.add(parse());
            } else {
                list.add(literal());
            }
        }

        if (carriage.hasNext()) {
            carriage.next(); // skip ] character
            carriage.clean(); // skip all "\r\n\t " ]
        }

        return list;
    }

    private Map<String, Object> map() {
        Map<String, Object> map = new HashMap<>();

        while (carriage.get() != '}') {

            if ("{,".contains(carriage.get().toString())) {
                carriage.next(); // skip { or , characters
            }

            String key = key();

            carriage.next(); // skip the : character
            carriage.clean(); // skip all "\r\n\t " after : character

            if ("{[".contains(carriage.get().toString())) {
                map.put(key, parse());
            } else {
                map.put(key, literal());
            }
        }

        if (carriage.hasNext()) {
            carriage.next(); // skip } character
            carriage.clean(); // skip all "\r\n\t " characters
        }

        return map;
    }

    private String literal() {

        StringBuilder builder = new StringBuilder();

        do {
            builder.append(carriage.get());
        } while (!("}],".contains(carriage.next().toString())));

        return builder.toString().trim();
    }

    String key() {
        carriage.clean();

        if (carriage.get() != '"') {
            throw new IllegalArgumentException(
                    "Unexpected token %s at position %d".formatted(carriage.get(), carriage.position())
            );
        }

        StringBuilder builder = new StringBuilder();

        do {
            builder.append(carriage.get());
        } while (carriage.next() != ':');

        String key = builder.toString().trim();

        if (key.charAt(key.length() - 1) != '"') {
            throw new IllegalArgumentException(
                    "Unexpected token %s at position %d".formatted(carriage.get(), carriage.position())
            );
        }

        return key.substring(1, key.length() - 1);
    }
}
