package aervel.aparser.json;

public abstract class Literal {
    private static final Literal literal = new Literal(){};

    public static <T> T of(String object) {
        return literal.create(object);
    }

    private <T> T create(String object) {
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
