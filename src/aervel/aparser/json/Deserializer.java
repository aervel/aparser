package aervel.aparser.json;

import aervel.aparser.Replacer;

import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.temporal.Temporal;
import java.util.*;

public abstract class Deserializer {
    private static final Deserializer deserializer = new Deserializer() {
    };

    public static <T> T deserialize(String json, Class<T> type, Object replacer) {

        if (replacer instanceof Replacer) {
            return deserializer.deserialize(Wrapper.wrap(new Reader(json)), type, (Replacer) replacer);
        }

        if (replacer instanceof String[]) {
            return deserializer.deserialize(Wrapper.wrap(new Reader(json)), type, (String[]) replacer);
        }

        return deserializer.deserialize(Wrapper.wrap(new Reader(json)), type);
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(Object object, Type type, Replacer replacer) {
        if (object == null) {
            return null;
        }

        try {
            if (!(type instanceof Class<?> || type instanceof ParameterizedType)) {
                throw new IllegalArgumentException();
            }

            Class<T> cls = (Class<T>) (type instanceof Class<?> ? type : Class.forName(type.getTypeName().split("<")[0]));

            if (object instanceof List<?> list && cls.isArray()) {
                Class<?> componentType = cls.getComponentType();
                Object array = Array.newInstance(componentType, list.size());

                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, deserialize(list.get(i), componentType, replacer));
                }

                return (T) array;
            }

            if (object instanceof List<?> list && Collection.class.isAssignableFrom(cls)) {

                if (type instanceof ParameterizedType parameterizedType) {
                    Type componentType = parameterizedType.getActualTypeArguments()[0];
                    Collection<?> collection = new ArrayList<>();

                    for (Object o : list) {
                        collection.add(deserialize(o, componentType, replacer));
                    }

                    if (cls.isInterface()) {
                        return (T) cls.getDeclaredMethod("copyOf", Collection.class).invoke(null, collection);
                    }

                    return cls.getDeclaredConstructor(Collection.class).newInstance(collection);
                }

                return (T) list;
            }

            String packageName = cls.getPackage() == null ? "" : cls.getPackageName();

            if (packageName.startsWith("java.") || packageName.startsWith("javax.") || cls.getClassLoader() == null) {

                if (object instanceof String string) {
                    return deserializeLiteral(string, cls);
                }

                return (T) object;
            }

            if (object instanceof Map<?, ?>) {
                Field[] fields = Fields.of(cls);
                Object[] arguments = new Object[fields.length];
                Constructor<T> constructor = Constructors.of(fields);

                Map<String, Object> map = new HashMap<>();

                ((Map<String, Object>) object).forEach((key, value) -> {
                    if (Arrays.stream(fields).noneMatch(field -> field.getName().equals(key))) {
                        Map.Entry<String, Object> entry = replacer.apply(key, value);

                        if (entry != null) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                });

                for (int i = 0; i < arguments.length; i++) {
                    String key = fields[i].getName();

                    if (!map.containsKey(fields[i].getName())) {
                        Object value = deserialize(((Map<?, ?>) object).get(key), fields[i].getGenericType(), replacer);
                        Map.Entry<String, Object> entry = replacer.apply(key, value);

                        if (entry != null) {
                            arguments[i] = entry.getValue();
                        }
                    } else {
                        arguments[i] = deserialize(map.get(key), fields[i].getGenericType(), replacer);
                    }
                }

                return constructor.newInstance(arguments);
            }

            throw new IllegalArgumentException();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T deserialize(Object wrap, Class<T> type, String[] replacer) {
        // Use a Set because is most flexible to query for elements
        Set<String> set = Set.of(replacer);
        // Create a replacer instance that return the entries of keys in replacer array
        Replacer replacer0 = ((key, value) -> set.contains(key) ? Map.entry(key, value) : null);
        // Forward the responsibility of deserialization to a method that uses an instance of Replacer
        return deserialize(wrap, type, replacer0);
    }

    private <T> T deserialize(Object wrap, Class<T> type) {
        // Create a replacer instance that return the entries for all (key, value) in object
        Replacer replacer0 = (Map::entry);
        // Forward the responsibility of deserialization to a method that uses an instance of Replacer
        return deserialize(wrap, type, replacer0);
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeLiteral(String object, Class<?> type) {
        try {
            if (type.isPrimitive()) {
                if (type.equals(byte.class)) type = Byte.class;
                else if (type.equals(short.class)) type = Short.class;
                else if (type.equals(char.class)) type = Character.class;
                else if (type.equals(int.class)) type = Integer.class;
                else if (type.equals(long.class)) type = Long.class;
                else if (type.equals(float.class)) type = Float.class;
                else if (type.equals(double.class)) type = Double.class;
                else if (type.equals(boolean.class)) type = Boolean.class;
            }

            if (Object.class.equals(type)) {
                return (T) object;
            }

            if (Date.class.isAssignableFrom(type)) {
                return (T) DateFormat.getInstance().parse(object);
            }

            if (String.class.isAssignableFrom(type)) {
                return (T) object;
            }

            if (Temporal.class.isAssignableFrom(type)) {
                return (T) type.getDeclaredMethod("parse", CharSequence.class).invoke(null, object);
            }

            if (Character.class.isAssignableFrom(type)) {
                return (T) Character.valueOf(object.charAt(0));
            }

            if (Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
                return (T) type.getDeclaredMethod("valueOf", String.class).invoke(null, object);
            }

            throw new IllegalArgumentException();

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}

