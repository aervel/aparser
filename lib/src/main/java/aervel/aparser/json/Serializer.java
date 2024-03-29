package aervel.aparser.json;

import aervel.aparser.Replacer;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.*;


public abstract class Serializer {

    private static final Serializer serializer = new Serializer() {
    };

    public static String serialize(Object object, Object replacer) {
        if (replacer instanceof Replacer) {
            return serializer.serialize(object, (Replacer) replacer, new Writer()).toString();
        }

        if (replacer instanceof String[]) {
            return serializer.serialize(object, (String[]) replacer, new Writer()).toString();
        }

        return serializer.serialize(object, new Writer()).toString();
    }

    private Writer serialize(Object object, Replacer replacer, Writer writer) {

        if (object instanceof Object[] objects) {
            object = Arrays.asList(objects);
        }

        if (object instanceof Collection<?> collection) {
            List<Object> list = new ArrayList<>(collection);

            writer.write('[');
            writer.write('\n');

            int count = 0;

            for (Object element : list) {
                try {
                    Writer w = new Writer();

                    if (count > 0) {
                        w.write(',');
                        w.write('\n');
                    }

                    serialize(element, replacer, w);
                    writer.write(w.toString());
                    count++;
                } catch (Break ignored) {

                }
            }

            writer.write('\n');
            writer.write(']');

            return writer;
        }

        String packageName = object.getClass().getPackage() == null ? "" : object.getClass().getPackageName();

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            return serializeLiteral(object, writer);
        }

        Field[] fields = Fields.of(object.getClass());

        writer.write('{');
        writer.write('\n');
        int count = 0;

        for (Field field : fields) {
            try {
                // throw a NullPointerException when field.get(object) return null
                Map.Entry<String, Object> entry = replacer.apply(field.getName(), field.get(object));
                if (entry != null) {

                    if (count > 0) {
                        writer.write(',');
                        writer.write('\n');
                    }

                    serializeLiteral(entry.getKey(), writer);
                    writer.write(':');
                    serialize(entry.getValue(), replacer, writer);

                    count++;
                }
                // caused by Map.entry(k, v)
            } catch (NullPointerException ignored) {

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        writer.write('\n');
        writer.write('}');

        return writer;
    }

    /**
     * Serializes an object or array to its JSON representation.
     * <p>
     * If this object represents a non-array type, the replacer is used to determine which fields will be included and
     * returned during the serialization process. All non-listed fields won't be included or returned in serialization
     * process.
     *
     * @param object   The object to be serialized. Can't be part of {@code java.*} package or {@code javax.*} package.
     * @param replacer An array of string containing the keys to be included in serialization process.
     * @param writer   The writer to write serialized data.
     * @return This writer argument.
     */
    private Writer serialize(Object object, String[] replacer, Writer writer) {
        // Use a Set because is most flexible to query for elements
        Set<String> set = Set.of(replacer);
        // Create a replacer instance that return the entries of keys in replacer array
        Replacer replacer0 = ((key, value) -> set.contains(key) ? Map.entry(key, value) : null);
        // Forward the responsibility of serialization to a method that uses an instance of Replacer
        return serialize(object, replacer0, writer);
    }

    /**
     * Serializes an object or array to its JSON representation.
     * <p>
     * If this object is a non-array type, all valid fields in object will be serialized and returned.
     *
     * @param object The object to be serialized. Can't be part of {@code java.*} package or {@code javax.*} package.
     * @param writer The writer to write serialized data.
     * @return This writer argument.
     */
    private Writer serialize(Object object, Writer writer) {
        // Create a replacer instance that return the entries for all (key, value) in object
        Replacer replacer0 = (Map::entry);
        // Forward the responsibility of serialization to a method that uses an instance of Replacer
        return serialize(object, replacer0, writer);
    }

    /**
     * Serializes an object indented as literal to its literal JSON representation format. Objects created from classes
     * in {@code java.*} package or {@code javax.*} package are indented to be literals. Some examples of literals are:
     * <ul>
     *     <li>{@link  String}</li>
     *     <li>{@link  Number}</li>
     *     <li>{@link  Temporal}</li>
     * </ul>
     * <p>
     * This method provides appropriate representation for each literal in JSON according to official documentation at
     * <a href="https://www.json.org/json-en.html">json.org</a>.
     * <p>
     * If the object is null, null is written to writer. If the object is string and contains escape characters codes
     * those characters are written to displayable string format. If the object is number, a number is written writer.
     *
     * @param object The object to be serialized. Must be part of {@code java.*} package or {@code javax.*} package.
     * @param writer The writer to write serialized data.
     * @return This writer argument.
     */
    private Writer serializeLiteral(Object object, Writer writer) {

        if (object == null) {
            writer.write("null");
        }

        if (object instanceof String value) {
            char[] chars = value.toCharArray();

            writer.write('"');

            for (char c : chars) {
                switch (c) {
                    case '"' -> writer.write("\\\"");
                    case '\t' -> writer.write("\\t");
                    case '\n' -> writer.write("\\n");
                    case '\r' -> writer.write("\\r");
                    case '\f' -> writer.write("\\f");
                    case '\\' -> writer.write("\\");
                    default -> writer.write(c);
                }
            }

            writer.write('"');
        }

        if (object instanceof Number || object instanceof Boolean) {
            writer.write(object.toString());
        }

        if (object instanceof Temporal || object instanceof Date || object instanceof Enum<?>) {
            writer.write('"');
            writer.write(object.toString());
            writer.write('"');
        }

        return writer;
    }
}
