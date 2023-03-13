package aervel.aparser.json;


public final class Reader extends java.io.Reader {
    private final StringBuffer buffer;
    private int column;
    private int last;
    private int line;

    public Reader(String s) {
        buffer = new StringBuffer(s);
    }

    @Override
    public int read() {
        int read = buffer.charAt(column++);

        if (read == '\n') {
            line++;
        }

        return read;
    }

    private void clear() {

    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        int start = column;

        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        while (off < len) {
            cbuf[off++] = (char) read();
        }

        return start == column ? -1 : column - start;
    }

    @Override
    public void close() {

    }
}
