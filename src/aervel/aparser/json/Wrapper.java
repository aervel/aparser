package aervel.aparser.json;

import java.util.HashMap;
import java.util.Map;

public class Wrapper {

    public static Object wrap(Reader reader) {
        int read = reader.read();

        if (read == '{') {
            Map<String, Object> map = new HashMap<>();

            while (read != '}') {
                String key = key(reader);

                if (reader.read() != ':') {
                    throw new IllegalArgumentException();
                }

                read = reader.read();

                if (read == '{' || read == '[') {
                    map.put(key, wrap(reader));
                }
            }

            return map;
        }

        if (read == '[') {

        }

        throw new IllegalArgumentException();
    }

    private static String key(Reader reader) {
        StringBuilder builder = new StringBuilder();

        if (reader.read() != '"') {
            throw new IllegalArgumentException();
        }

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
