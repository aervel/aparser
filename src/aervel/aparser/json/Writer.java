package aervel.aparser.json;

import java.io.StringWriter;

public class Writer extends StringWriter {
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
            super.write("\t".repeat(tabs));
        }

        super.write(c);

        last = c != ' ' ? c : last;
    }

    @Override
    public void write(String str) {
        char[] chars = str.toCharArray();
        for (char c : chars) {
            write(c);
        }
    }
}
