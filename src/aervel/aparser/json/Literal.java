package aervel.aparser.json;

public abstract class Literal {

    private static final Literal literal = new Literal() {
    };

    public static <T> T of(String object) {
        return (T) literal.create(object);
    }

    private Object create(String value) {
        if (isNumber(value)) {

            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                if (value.length() < 10) {
                    return Integer.parseInt(value);
                }

                long number = Long.parseLong(value);

                if (number < Integer.MAX_VALUE) {
                    return (int) number;
                }

                return number;
            }
        }

        return null;
    }

    public boolean isNumber(String value) {
        char[] chars = value.toCharArray();
        boolean comma = false;

        for (char c : chars) {
            if (c == 46) {
                if (comma) {
                    return false;
                } else {
                    comma = true;
                }
            } else if (c < 48 || c > 57) {
                return false;
            }
        }

        return true;
    }
}
