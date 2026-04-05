package flandretw.bopomofo.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BopomofoConverter {
    private static final Map<Character, Character> KEY_MAP = new HashMap<>();
    private static final Pattern STRICT_PATTERN;

    static {
        // Initials (聲母)
        KEY_MAP.put('1', 'ㄅ');
        KEY_MAP.put('q', 'ㄆ');
        KEY_MAP.put('a', 'ㄇ');
        KEY_MAP.put('z', 'ㄈ');
        KEY_MAP.put('2', 'ㄉ');
        KEY_MAP.put('w', 'ㄊ');
        KEY_MAP.put('s', 'ㄋ');
        KEY_MAP.put('x', 'ㄌ');
        KEY_MAP.put('e', 'ㄍ');
        KEY_MAP.put('d', 'ㄎ');
        KEY_MAP.put('c', 'ㄏ');
        KEY_MAP.put('r', 'ㄐ');
        KEY_MAP.put('f', 'ㄑ');
        KEY_MAP.put('v', 'ㄒ');
        KEY_MAP.put('5', 'ㄓ');
        KEY_MAP.put('t', 'ㄔ');
        KEY_MAP.put('g', 'ㄕ');
        KEY_MAP.put('b', 'ㄖ');
        KEY_MAP.put('y', 'ㄗ');
        KEY_MAP.put('h', 'ㄘ');
        KEY_MAP.put('n', 'ㄙ');

        // Medials (介音)
        KEY_MAP.put('u', 'ㄧ');
        KEY_MAP.put('j', 'ㄨ');
        KEY_MAP.put('m', 'ㄩ');

        // Finals (韻母)
        KEY_MAP.put('8', 'ㄚ');
        KEY_MAP.put('i', 'ㄛ');
        KEY_MAP.put('k', 'ㄜ');
        KEY_MAP.put(',', 'ㄝ');
        KEY_MAP.put('9', 'ㄞ');
        KEY_MAP.put('o', 'ㄟ');
        KEY_MAP.put('l', 'ㄠ');
        KEY_MAP.put('.', 'ㄡ');
        KEY_MAP.put('0', 'ㄢ');
        KEY_MAP.put('p', 'ㄣ');
        KEY_MAP.put(';', 'ㄤ');
        KEY_MAP.put('/', 'ㄥ');
        KEY_MAP.put('-', 'ㄦ');

        // Tones (聲調)
        KEY_MAP.put('6', 'ˊ');
        KEY_MAP.put('3', 'ˇ');
        KEY_MAP.put('4', 'ˋ');
        KEY_MAP.put('7', '˙');
        KEY_MAP.put(' ', ' ');

        String I = "[1qaz2wsxedcrfv5tgbyhn]";
        String M = "[ujm]";
        String F = "[8ik,9ol\\.0p;\\/\\-]";
        String T = "[3467\\s]";

        // A valid tone-less syllable composition
        String C = "(?:" + I + M + "?" + F + "?|" + M + F + "?|" + F + ")";

        // The whole string must be composed of syllables that have a tone,
        // with the exception that the very last syllable can lack a tone.
        STRICT_PATTERN = Pattern.compile("^(?:" + C + T + ")*(?:" + C + T + "?)$");
    }

    public static class BopomofoResult {
        public final boolean changed;
        public final List<Segment> segments;

        public BopomofoResult(boolean changed, List<Segment> segments) {
            this.changed = changed;
            this.segments = segments;
        }
    }

    public static class Segment {
        public final String original;
        public final String translated; // null if no translation applied

        public Segment(String original, String translated) {
            this.original = original;
            this.translated = translated;
        }
    }

    public static BopomofoResult convert(String originalText) {
        if (originalText == null || originalText.trim().isEmpty()) {
            return new BopomofoResult(false, null);
        }

        String normalized = normalizeFullWidth(originalText);
        String text = unShift(normalized).toLowerCase();

        boolean hasBopomofoIndicator = text.matches(".*[0-9,\\.\\/;\\-].*");
        if (STRICT_PATTERN.matcher(text).matches() && (hasBopomofoIndicator || text.length() > 1)) {
            List<Segment> list = new ArrayList<>();
            list.add(new Segment(originalText, translateFully(text)));
            return new BopomofoResult(true, list);
        }

        List<Segment> segments = new ArrayList<>();
        String[] words = text.split("(?<= )|(?= )");
        boolean changed = false;
        int currentIndex = 0;

        for (String word : words) {
            String origWord = originalText.substring(currentIndex, currentIndex + word.length());
            currentIndex += word.length();

            if (word.trim().isEmpty()) {
                segments.add(new Segment(origWord, null));
                continue;
            }

            boolean wordHasIndicator = word.matches(".*[0-9,\\.\\/;\\-].*");
            if (STRICT_PATTERN.matcher(word).matches() && (wordHasIndicator || word.length() > 1)) {
                segments.add(new Segment(origWord, translateFully(word)));
                changed = true;
            } else {
                segments.add(new Segment(origWord, null));
            }
        }

        return new BopomofoResult(changed, changed ? segments : null);
    }

    private static String normalizeFullWidth(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\u3000') {
                chars[i] = ' ';
            } else if (chars[i] >= '\uFF01' && chars[i] <= '\uFF5E') {
                chars[i] = (char) (chars[i] - 0xFEE0);
            }
        }
        return new String(chars);
    }

    private static String unShift(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '!':
                    chars[i] = '1';
                    break;
                case '@':
                    chars[i] = '2';
                    break;
                case '#':
                    chars[i] = '3';
                    break;
                case '$':
                    chars[i] = '4';
                    break;
                case '%':
                    chars[i] = '5';
                    break;
                case '^':
                    chars[i] = '6';
                    break;
                case '&':
                    chars[i] = '7';
                    break;
                case '*':
                    chars[i] = '8';
                    break;
                case '(':
                    chars[i] = '9';
                    break;
                case ')':
                    chars[i] = '0';
                    break;
                case '_':
                    chars[i] = '-';
                    break;
                case '<':
                    chars[i] = ',';
                    break;
                case '>':
                    chars[i] = '.';
                    break;
                case '?':
                    chars[i] = '/';
                    break;
                case ':':
                    chars[i] = ';';
                    break;
                case '"':
                    chars[i] = '\'';
                    break;
                case '+':
                    chars[i] = '=';
                    break;
                case '{':
                    chars[i] = '[';
                    break;
                case '}':
                    chars[i] = ']';
                    break;
                case '|':
                    chars[i] = '\\';
                    break;
            }
        }
        return new String(chars);
    }

    private static String translateFully(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(KEY_MAP.getOrDefault(c, c));
        }
        return sb.toString();
    }
}
