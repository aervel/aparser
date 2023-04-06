package aervel.aparser.json;


import java.io.IOException;

final class Reader extends java.io.Reader {
    private final StringBuffer buffer;
    private int position = 0;
    private int[] location = {0, 1};

    public Reader(String s) {
        buffer = new StringBuffer(s);
    }

    /**
     * Read a single character in a stream of chars and return its codepoint or -1 if the end of stream is reached.
     * <p>
     * The following characters (indentation characters or formatting characters) are skipped in specific conditions:
     * <ul>
     *     <li>Carriage return {@code '\r'}</li>
     *     <li>Tabulation {@code '\t'}</li>
     *     <li>Line fee {@code '\f'}</li>
     *     <li>New line {@code '\n'}</li>
     *     <li>Space {@code ' '}</li>
     * </ul>
     * If the current character represents one of formatting characters excluding space, will be skipped and following
     * spaces to. If current character represents space only but not preceded by another formatting character, then it
     * is returned.
     *
     * @return The character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the end of the stream
     * has been reached.
     */
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

    public void checkNext(char... chars) throws IllegalArgumentException {
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
