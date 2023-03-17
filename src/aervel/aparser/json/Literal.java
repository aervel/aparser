package aervel.aparser.json;

public abstract class Literal {
    private static final Literal literal = new Literal(){};

    public static <T> T of(String object) {
        return literal.create(object);
    }

    private <T> T create(String object) {
        return null;
    }

}
