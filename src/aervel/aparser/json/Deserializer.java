package aervel.aparser.json;

import aervel.aparser.Replacer;

import java.util.Map;
import java.util.Set;

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

    private <T> T deserialize(Object wrap, Class<T> type, String[] replacer) {
        // Use a Set because is most flexible to query for elements
        Set<String> set = Set.of(replacer);
        // Create a replacer instance that return the entries of keys in replacer array
        Replacer replacer0 = ((key, value) -> set.contains(key) ? Map.entry(key, value) : null);
        // Forward the responsibility of deserialization to a method that uses an instance of Replacer
        return deserialize(wrap, type, replacer0);
    }

    private <T> T deserialize(Object wrap, Class<T> type, Replacer replacer) {
        throw new IllegalArgumentException();
    }

    private <T> T deserialize(Object wrap, Class<T> type) {
        // Create a replacer instance that return the entries for all (key, value) in object
        Replacer replacer0 = (Map::entry);
        // Forward the responsibility of deserialization to a method that uses an instance of Replacer
        return deserialize(wrap, type, replacer0);
    }

}
