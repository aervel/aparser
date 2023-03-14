package aervel.aparser.json;

import aervel.aparser.Replacer;

import java.util.Map;

public abstract class Deserializer {
    private static final Deserializer deserializer = new Deserializer() {
    };

    public static <T> T deserialize(String json, Class<T> type, Object replacer) {

        return deserializer.deserialize(Wrapper.wrap(new Reader(json)), type);
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
