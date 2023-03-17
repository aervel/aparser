package aervel.aparser.json;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Literal {

    private static final Literal literal = new Literal() {
    };

    @SuppressWarnings("unchecked")
    public static <T> T of(String object) {
        return (T) literal.create(object);
    }

    private Object create(String value) {
        if (value == null) {
            return null;
        }

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

        if (isTemporal(value)) {
            if (value.contains("T")) {
                if (value.charAt(value.length() -1) == 'Z') {
                    return Instant.parse(value);
                }

                return LocalDateTime.parse(value);
            }

            return LocalDate.parse(value);
        }

        return value;
    }

    public boolean isTemporal(String value) {
        char[] chars = value.toCharArray();

        if (chars.length < 10) {
            return false;
        }

        // check year [yyyy]-mm-dd
        for (int i = 0; i < 4; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        // check date separator yyyy[-]mm-dd
        if (chars[4] != '-') {
            return false;
        }

        // check month yyyy-[mm]-dd
        for (int i = 5; i < 7; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        // check date separator yyyy-mm[-]dd
        if (chars[7] != '-') {
            return false;
        }

        // check day yyyy-mm-[dd]
        for (int i = 8; i < 10; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        // is date only
        if (chars.length == 10) {
            return true;
        }

        // check datetime separator
        if (chars[10] != 'T') {
            return false;
        }

        // check hour yyyy-mm-ddT[hh]:mm:ss
        for (int i = 11; i < 13; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        // check time separator yyyy-mm-ddThh[:]mm:ss
        if (chars[13] != ':') {
            return false;
        }

        // check minutes yyyy-mm-ddThh:[mm]:ss
        for (int i = 14; i < 16; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        // check time separator yyyy-mm-ddThh:mm[:]ss
        if (chars[16] != ':') {
            return false;
        }

        // check seconds yyyy-mm-ddThh:mm:[ss]
        for (int i = 17; i < 19; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        // is datetime without nanoseconds
        if (chars.length == 19) {
            return true;
        }

        // check nanoseconds separator yyyy-mm-ddThh:mm:ss[.]nnn
        if (chars[19] != '.') {
            return false;
        }

        // check nanoseconds separator yyyy-mm-ddThh:mm:ss.[nnn] or yyyy-mm-ddThh:mm:ss.[nnnZ]
        for (int i = 21; i < chars.length; i++) {
            if (chars[i] == 'Z' && chars.length - 1 == i) {
                return true;
            }
            if (chars[i] < '0' || chars[i] > '9') {
                return false;
            }
        }

        return true;
    }

    public boolean isNumber(String value) {
        char[] chars = value.toCharArray();
        boolean comma = false;

        if (chars[0] == '-' || chars[0] == '+') {
            chars[0] = '0';
        }

        for (char c : chars) {
            if (c == '.') {
                if (comma) {
                    return false;
                } else {
                    comma = true;
                }
            } else if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    }
}
