package aervel.aparser.json;

import java.util.Iterator;

final class Carriage implements Iterator<Character> {
    private final char[] chars;
    private int position;

    public Carriage(String json) {
        this.chars = json.toCharArray();
    }

    public int position() {
        return position;
    }

    public Character get() {
        return chars[position];
    }

    @Override
    public boolean hasNext() {
        return position < chars.length - 1;
    }

    @Override
    public Character next() {
        return chars[++position];
    }
}
