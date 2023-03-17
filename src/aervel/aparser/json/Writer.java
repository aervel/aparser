package aervel.aparser.json;

final class Writer extends java.io.Writer {
    private final StringBuffer buffer = new StringBuffer();
    private int tabs;
    private int last;


    @Override
    public void write(int c) {
        if (c == '\n' && (last == '{' || last == '[')) {
            tabs++;
        }

        if (last == '\n' && (c == '}' || c == ']')) {
            tabs--;
        }

        if (last == '\n') {
            buffer.append("\t".repeat(tabs));
        }

        buffer.append((char) c);

        last = c != ' ' ? c : last;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {

        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        while (off < len) {
            write(cbuf[off++]);
        }
    }

    @Override
    public void write(String str) {
        char[] chars = str.toCharArray();
        for (char c : chars) {
            write(c);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
