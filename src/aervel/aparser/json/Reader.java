package aervel.aparser.json;


import java.io.IOException;

public final class Reader extends java.io.Reader {
    private final StringBuffer buffer;
    private int position = 0;
    private int[] location = {0, 1};

    public Reader(String s) {
        buffer = new StringBuffer(s);
    }

    @Override
    public int read() {

        if (buffer.length() == position) {
            return -1;
        }

        int read = buffer.charAt(position++);
        location[0]++;

        if (read == '\n') {
            location[1]++;
            location[0] = 1;
        }

        boolean skipSpace = read == '\r' || read == '\t' || read == '\f' || read == '\n';

        // skip escaped characters
        while ((skipSpace && read == ' ') || read == '\r' || read == '\t' || read == '\f' || read == '\n') {

            if (buffer.length() == position) {
                return -1;
            }

            read = buffer.charAt(position++);
            location[0]++;
        }

        return read;
    }

    @Override
    public long skip(long n) {
        try {
            return super.skip(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        int start = position;

        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        while (off < len) {
            cbuf[off++] = (char) read();
        }

        return start == position ? -1 : position - start;
    }

    @Override
    public void close() {

    }

    public int get() {
        return buffer.charAt(position > 0 ? position - 1 : position);
    }

    public void checkNext(char...chars) throws IllegalArgumentException {
        read();
        check(chars);
    }

    public void check(char... chars) throws IllegalArgumentException {
        int read = position == 0 ? read() : get();

        while (read == ' ') read = read();

        for (char c : chars) {
            if (read == c) return;
        }

        throw new IllegalArgumentException("Invalid char at line %d column %d".formatted(location[1], location[0]));
    }

}
