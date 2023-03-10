package aervel.aparser.json;

import aervel.aparser.Replacer;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Date;


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
        String packageName = object.getClass().getPackage() != null ? object.getClass().getPackageName() : "";

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            return serializeLiteral(object, writer);
        }

        return writer;
    }

    private Writer serialize(Object object, String[] replacer, Writer writer) {
        String packageName = object.getClass().getPackage() != null ? object.getClass().getPackageName() : "";

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            return serializeLiteral(object, writer);
        }

        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                serialize(field.get(object), replacer, writer);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return writer;
    }

    private Writer serialize(Object object, Writer writer) {
        String packageName = object.getClass().getPackage() != null ? object.getClass().getPackageName() : "";

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            return serializeLiteral(object, writer);
        }

        return writer;
    }

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
