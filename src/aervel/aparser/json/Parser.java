package aervel.aparser.json;

import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.temporal.Temporal;
import java.util.*;

import static java.util.Arrays.stream;

public final class Parser {
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

        if (object == null) {
            return null;
        }

        if (object instanceof String value && type instanceof Class<?> cls) {

            if (value.equals("null")) {
                return null;
            }

            if (Number.class.isAssignableFrom(cls)) {
                return cls.getDeclaredMethod("valueOf", String.class).invoke(null, value);
            }

            if (Boolean.class.isAssignableFrom(cls)) {
                return Boolean.valueOf(value);
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

            constructor.setAccessible(true);

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

            // skip the : or [ characters and then verify if the current character is not the end of list ] character
            // The purpose of verification is to avoid parsing wrong values when parsing an empty list
            if (carriage.next() != ']') {
                carriage.clean(); // skip all "\r\n\t " after : or [ characters

                if ("{[".contains(carriage.get().toString())) {
                    list.add(parse());
                } else {
                    list.add(literal());
                }
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

            carriage.clean();

            // The purpose of verification is to avoid parsing of empty objects
            if (carriage.get() != '}') {
                String key = key();

                carriage.next(); // skip the : character
                carriage.clean(); // skip all "\r\n\t " after : character

                if ("{[".contains(carriage.get().toString())) {
                    map.put(key, parse());
                } else {
                    map.put(key, literal());
                }
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
        } while (!("}]:".contains(carriage.next().toString())));

        String literal = builder.toString().trim();

        if (carriage.get() == ':') {
            int index = literal.lastIndexOf(",", literal.length() - 2);
            carriage.position(carriage.position() - (literal.length() - index));
            return literal.substring(0, index);
        }

        // When a literal has type "*,*" the loop above will break at comma (,), so to prevent return wrong values the
        // if bellow verify the presence of comma at current carriage position and the reflected results in the literal
        // variable. Continues the iteration through recursive call if the verification returned true.
        if (literal.charAt(0) == '"' && carriage.get() == ',' && literal.charAt(literal.length() - 1) != '"') {
            carriage.next(); // skip , at current position
            return builder.append(',').append(literal()).toString();
        }

        // When a literal hash type ""*",*" the loop above will break at (",), so to prevent return incomplete literal
        // the if bellow verify the presence of comma at current carriage position and if true, clean and verify the
        // next position for a valid control character. If absent, continues parsing the literal.
        if (carriage.get() == ',') {
            do {
                builder.append(carriage.get());
            } while ("\n\t\r, ".contains(carriage.next().toString()) && carriage.hasNext());

            if (!("}]\"".contains(carriage.get().toString()))) {
                return builder.append(literal()).toString();
            }
        }

        return literal;
    }

    String key() {
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
