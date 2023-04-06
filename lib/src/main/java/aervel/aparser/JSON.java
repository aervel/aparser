package aervel.aparser;

import aervel.aparser.json.Deserializer;
import aervel.aparser.json.Serializer;

public interface JSON {

    static String string(Object object, Replacer replacer) {
        return Serializer.serialize(object, replacer);
    }

    static String string(Object object, String... replacer) {
        return Serializer.serialize(object, replacer);
    }

    static String string(Object object) {
        return Serializer.serialize(object, null);
    }

    static <T> T object(String json, Class<T> type, Replacer replacer) {
        return Deserializer.deserialize(json, type, replacer);
    }

    static <T> T object(String json, Class<T> type, String... replacer) {
        return Deserializer.deserialize(json, type, replacer);
    }

    static <T> T object(String json, Class<T> type) {
        return Deserializer.deserialize(json, type, null);
    }
}
