package aervel.aparser;

public interface JSON {

    static String string(Object object, Replacer replacer) {
        return null;
    }

    static String string(Object object, String[] replacer) {
        return null;
    }

    static String string(Object object) {
        return null;
    }

    static <T> T object(String json, Class<T> type, Replacer replacer) {
        return null;
    }

    static <T> T object(String json, Class<T> type, String[] replacer) {
        return null;
    }

    static <T> T object(String json, Class<T> type) {
        return null;
    }
}
