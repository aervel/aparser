package aervel.aparser.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wrapper {

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static Object wrap(Reader reader) {
        reader.check('[', '{');

        if (reader.get() == '{') {
            Map<String, Object> map = new HashMap<>();

            while (reader.get() != '}') {
                reader.checkNext('"');
                String key = string(reader);

                reader.checkNext(':');
                reader.checkNext('{', '[', '"', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

                switch (reader.get()) {
                    case '{', '[' -> map.put(key, wrap(reader));
                    case '"' -> map.put(key, string(reader));
                    default -> map.put(key, number(reader));
                }

                if (reader.get() == '"') reader.skip(1);
            }

            if (reader.get() == '}') reader.skip(1);

            return map;
        } else {
            List<Object> list = new ArrayList<>();

            while (reader.get() != ']') {
                reader.checkNext('{', ']', '[', '"', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

                switch (reader.get()) {
                    case '{', '[' -> list.add(wrap(reader));
                    case '"' -> list.add(string(reader));
                    default -> list.add(number(reader));
                }

                if (reader.get() == '"') reader.skip(1);
            }

            if (reader.get() == ']') reader.skip(1);

            return list;
        }
    }

    private static String number(Reader reader) {
        StringBuilder builder = new StringBuilder();

        do {
            int read = reader.get();

            if (read == ',' || read == '}' || read == ']') {
                break;
            }

            builder.append((char) read);

        } while (reader.read() != -1);

        return builder.toString().trim();
    }

    private static String string(Reader reader) {
        StringBuilder builder = new StringBuilder();

        int read;

        do {

            if ((read = reader.read()) == '"') {
                break;
            }

            // resolve escaped special characters
            if (read == '\\') {
                switch (reader.read()) {
                    case '"' -> read = '"';
                    case 'r' -> read = '\r';
                    case 't' -> read = '\t';
                    case 'f' -> read = '\f';
                    case 'n' -> read = '\n';
                    default -> throw new IllegalArgumentException();
                }
            }

            builder.append((char) read);

        } while (read != -1);

        return builder.toString();
    }

}
