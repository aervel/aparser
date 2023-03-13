package aervel.aparser.json;


import java.io.IOException;

public final class Reader extends java.io.Reader {
    private final StringBuffer buffer;
    private int column;
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

        boolean skipSpace = read == '\r' || read == '\t' || read == '\f' || read == '\n';

        // skip escaped characters
        while ((skipSpace && read == ' ') || read == '\r' || read == '\t' || read == '\f' || read == '\n') {
            read = buffer.charAt(column++);
        }

        return read;
    }

    private void clean() {

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
