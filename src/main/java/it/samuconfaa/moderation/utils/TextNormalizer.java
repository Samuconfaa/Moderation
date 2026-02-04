package it.samuconfaa.moderation.utils;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class TextNormalizer {

    private static final Map<Character, Character> CHAR_REPLACEMENTS = new HashMap<>();

    static {
        CHAR_REPLACEMENTS.put('0', 'o');
        CHAR_REPLACEMENTS.put('1', 'i');
        CHAR_REPLACEMENTS.put('3', 'e');
        CHAR_REPLACEMENTS.put('4', 'a');
        CHAR_REPLACEMENTS.put('5', 's');
        CHAR_REPLACEMENTS.put('7', 't');
        CHAR_REPLACEMENTS.put('8', 'b');

        CHAR_REPLACEMENTS.put('@', 'a');
        CHAR_REPLACEMENTS.put('$', 's');
        CHAR_REPLACEMENTS.put('€', 'e');
        CHAR_REPLACEMENTS.put('!', 'i');
        CHAR_REPLACEMENTS.put('|', 'i');
        CHAR_REPLACEMENTS.put('(', 'c');
        CHAR_REPLACEMENTS.put(')', 'c');
        CHAR_REPLACEMENTS.put('[', 'c');
        CHAR_REPLACEMENTS.put(']', 'c');
        CHAR_REPLACEMENTS.put('{', 'c');
        CHAR_REPLACEMENTS.put('}', 'c');
        CHAR_REPLACEMENTS.put('&', 'a');
        CHAR_REPLACEMENTS.put('#', 'h');
        CHAR_REPLACEMENTS.put('+', 't');
        CHAR_REPLACEMENTS.put('*', 'x');
        CHAR_REPLACEMENTS.put('%', 'x');
    }


    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String normalized = text.toLowerCase();

        normalized = removeAccents(normalized);

        StringBuilder sb = new StringBuilder(normalized.length());
        for (char c : normalized.toCharArray()) {
            Character replacement = CHAR_REPLACEMENTS.get(c);
            sb.append(replacement != null ? replacement : c);
        }
        normalized = sb.toString();

        normalized = normalized.replaceAll("\\s", "");

        normalized = normalized.replaceAll("(.)\\1+", "$1");

        return normalized;
    }

    private static String removeAccents(String text) {
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "");
    }
}