package aervel.aparser.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final Carriage carriage;

    public Parser(Carriage carriage) {
        this.carriage = carriage;
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

            carriage.next(); // skip the semicolon (:) or bracket ([) characters
            carriage.clean(); // skip all "\r\n\t " after semicolon or bracket


            if ("{[".contains(carriage.get().toString())) {
                list.add(parse());
            } else {
                list.add(literal());
            }

        }

        if (carriage.hasNext()) {
            carriage.next(); // skip ] character
            carriage.clean(); // skip all "\r\n\t " after semicolon
        }

        return list;
    }

    private Map<String, Object> map() {
        Map<String, Object> map = new HashMap<>();

        while (carriage.get() != '}') {

            if ("{,".contains(carriage.get().toString())) {
                carriage.next(); // skip { or ,
            }

            String key = key();

            carriage.next(); // skip the semicolon (:) character
            carriage.clean(); // skip all "\r\n\t " after semicolon

            if ("{[".contains(carriage.get().toString())) {
                map.put(key, parse());
            } else {
                map.put(key, literal());
            }
        }

        if (carriage.hasNext()) {
            carriage.next(); // skip } character
            carriage.clean(); // skip all "\r\n\t " after semicolon
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
