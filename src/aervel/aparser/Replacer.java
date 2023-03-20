package aervel.aparser;

import aervel.aparser.json.Break;

import java.util.Map;

@FunctionalInterface
public interface Replacer {

    static void skip() {
        throw new Break();
    }

    Map.Entry<String, Object> apply(String key, Object value);

}
