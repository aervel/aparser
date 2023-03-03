package aervel.aparser.json;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    private final Carriage carriage;

    public Parser(Carriage carriage) {
        this.carriage = carriage;
    }

    Object parse() {
        carriage.clean();

        if (carriage.get() == '[') {

        }

        if (carriage.get() == '{') {
            return map();
        }

        throw new IllegalArgumentException(
                "Unexpected token %s at position %d".formatted(carriage.get(), carriage.position())
        );
    }

    private Map<String, Object> map() {
        Map<String, Object> map = new HashMap<>();
        String key = key();

        carriage.next(); // jump the semicolon (:) character
        carriage.clean(); // jump all "\r\n\t " after semicolon

        if ("{[".contains(carriage.get().toString())) {
            map.put(key, parse());
        } else {
            map.put(key, literal());
        }

        return map;
    }

    private String literal() {
        StringBuilder builder = new StringBuilder();

        do {
            builder.append(carriage.get());
        } while (!("}],".contains(carriage.next().toString())));

        return builder.toString();
    }

    String key() {
        if (carriage.next() != '"') {
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
