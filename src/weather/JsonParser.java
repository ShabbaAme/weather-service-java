package weather;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class JsonParser {

    private final String s;
    private int pos;

    private JsonParser(String s) {
        this.s = s;
    }


    public static Object parse(String text) throws JsonParseException {
        JsonParser p = new JsonParser(text);
        p.skipWs();
        Object value = p.readValue();
        p.skipWs();
        if (p.pos != p.s.length()) {
            throw p.error("лишние символы после JSON");
        }
        return value;
    }

    public static Object get(Object node, String... keys) {
        Object current = node;
        for (String key : keys) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(key);
        }
        return current;
    }

    private Object readValue() throws JsonParseException {
        char c = peek();
        switch (c) {
            case '{': return readObject();
            case '[': return readArray();
            case '"': return readString();
            case 't': expect("true"); return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null"); return null;
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return readNumber();
                }
                throw error("неожиданный символ '" + c + "'");
        }
    }

    private Map<String, Object> readObject() throws JsonParseException {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // {
        skipWs();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWs();
            if (peek() != '"') {
                throw error("ожидался ключ-строка");
            }
            String key = readString();
            skipWs();
            if (peek() != ':') {
                throw error("ожидалось ':'");
            }
            pos++;
            skipWs();
            map.put(key, readValue());
            skipWs();
            char c = peek();
            pos++;
            if (c == '}') {
                return map;
            }
            if (c != ',') {
                throw error("ожидалось ',' или '}'");
            }
        }
    }

    private List<Object> readArray() throws JsonParseException {
        List<Object> list = new ArrayList<>();
        pos++; // [
        skipWs();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            skipWs();
            list.add(readValue());
            skipWs();
            char c = peek();
            pos++;
            if (c == ']') {
                return list;
            }
            if (c != ',') {
                throw error("ожидалось ',' или ']'");
            }
        }
    }

    private String readString() throws JsonParseException {
        StringBuilder sb = new StringBuilder();
        pos++; 
        while (pos < s.length()) {
            char c = s.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= s.length()) {
                break;
            }
            char e = s.charAt(pos++);
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > s.length()) {
                        throw error("неполная \\u-последовательность");
                    }
                    try {
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw error("неверная \\u-последовательность");
                    }
                    pos += 4;
                    break;
                default:
                    throw error("неизвестная escape-последовательность \\" + e);
            }
        }
        throw error("незакрытая строка");
    }

    private Double readNumber() throws JsonParseException {
        int start = pos;
        while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
            pos++;
        }
        try {
            return Double.parseDouble(s.substring(start, pos));
        } catch (NumberFormatException ex) {
            throw error("неверное число");
        }
    }

    private void expect(String word) throws JsonParseException {
        if (!s.startsWith(word, pos)) {
            throw error("ожидалось " + word);
        }
        pos += word.length();
    }

    private char peek() throws JsonParseException {
        if (pos >= s.length()) {
            throw error("неожиданный конец данных");
        }
        return s.charAt(pos);
    }

    private void skipWs() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
    }

    private JsonParseException error(String msg) {
        return new JsonParseException("Ошибка разбора JSON (позиция " + pos + "): " + msg);
    }
}
