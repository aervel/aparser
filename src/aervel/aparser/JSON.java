package aervel.aparser;

import aervel.aparser.json.Parser;
import aervel.aparser.json.Stringifier;

public interface JSON {

    /**
     * Converts an object to JSON.
     *
     * @param object The object to be converted to JSON.
     * @return A string with JSON representation of underlying object.
     */
    static String stringify(Object object) {
        return Stringifier.stringify(object);
    }

    static <T> T parse(String json, Class<T> type) {
        return Parser.parse(json, type);
    }
}
