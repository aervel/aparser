package aervel.aparser;

import java.util.Map;

@FunctionalInterface
public interface Replacer {

    Map.Entry<String, Object> apply(String key, Object value);

}
