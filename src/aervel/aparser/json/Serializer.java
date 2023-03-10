package aervel.aparser.json;

import aervel.aparser.Replacer;


public abstract class Serializer {
    private static final Serializer serializer = new Serializer() {
    };

    public static String serialize(Object object, Object replacer) {

        if (replacer instanceof Replacer) {
            return serializer.serialize(object, (Replacer) replacer);
        }

        if (replacer instanceof String[]) {
            return serializer.serialize(object, (String[]) replacer);
        }

        return serializer.serialize(object);
    }

    private String serialize(Object object, Replacer replacer) {
        String packageName =  object.getClass().getPackage() != null? object.getClass().getPackageName() : "";

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            return serializeValue(object);
        }

        throw new IllegalStateException();
    }

    private String serialize(Object object, String[] replacer) {
        String packageName =  object.getClass().getPackage() != null? object.getClass().getPackageName() : "";

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {

        }

        throw new IllegalStateException();
    }

    private String serialize(Object object) {
        String packageName =  object.getClass().getPackage() != null? object.getClass().getPackageName() : "";

        if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            return serializeValue(object);
        }

        throw new IllegalStateException();
    }

    private String serializeValue(Object object) {
        throw new IllegalStateException();
    }
}
