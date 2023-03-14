package aervel.aparser.json;

public abstract class Deserializer {
    private static Deserializer deserializer = new Deserializer() {
    };

    public static <T> T deserialize(String json, Class<T> type, Object replacer) {
        throw new IllegalArgumentException();
    }

}
