package com.erp.lib.server.utils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

    private final static Pattern psqlArrayPattern = Pattern.compile("^\\{.{1,}\\}$");

    public static String[] stringToStringArray(String string) {
        Matcher matcher = psqlArrayPattern.matcher(string);

        return matcher.matches()
                ? string.substring(1, string.length() - 1).split(",")
                : null;
    }

    public static String concatenate(String... words) {
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word == null) {
                word = "";
            }
            sb.append(word);
        }

        return sb.toString();
    }

    public static String join(String[] words, String joinString) {
        StringBuilder sb = new StringBuilder();
        int l = words.length - 1;

        for (int n = 0; n < words.length; n++) {
            sb.append(words[n]);
            if (n != l) {
                sb.append(joinString);
            }
        }

        return sb.toString();
    }

    public static String join(Collection<String> words, String joinString) {
        return join(words.toArray(new String[words.size()]), joinString);
    }

    public static String repeat(String word, String joinString, int size) {
        StringBuilder sb = new StringBuilder();
        int l = size - 1;

        for (int n = 0; n < size; n++) {
            sb.append(word);
            if (n != l) {
                sb.append(joinString);
            }
        }

        return sb.toString();
    }

    public static String underscore(String camelCaseWord, char... delimiterChars) {
        if (camelCaseWord == null) {
            return null;
        }

        String result = camelCaseWord.trim();
        if (result.length() == 0) {
            return "";
        }
        result = result.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");
        result = result.replaceAll("([a-z\\d])([A-Z])", "$1_$2");
        result = result.replace('-', '_');
        if (delimiterChars != null) {
            for (char delimiterChar : delimiterChars) {
                result = result.replace(delimiterChar, '_');
            }
        }

        return result.toLowerCase();
    }

    public static String camelCase(String lowerCaseAndUnderscoredWord, boolean uppercaseFirstLetter, char... delimiterChars) {
        if (lowerCaseAndUnderscoredWord == null) {
            return null;
        }

        lowerCaseAndUnderscoredWord = lowerCaseAndUnderscoredWord.trim();

        if (lowerCaseAndUnderscoredWord.length() == 0) {
            return "";
        }
        if (uppercaseFirstLetter) {
            String result = lowerCaseAndUnderscoredWord;
            // Replace any extra delimiters with underscores (before the
            // underscores are converted in the next step)...
            if (delimiterChars != null) {
                for (char delimiterChar : delimiterChars) {
                    result = result.replace(delimiterChar, '_');
                }
            }

            // Change the case at the beginning at after each underscore ...
            return replaceAllWithUppercase(result, "(^|_)(.)", 2);
        }
        if (lowerCaseAndUnderscoredWord.length() < 2) {
            return lowerCaseAndUnderscoredWord;
        }
        return ""
                + Character.toLowerCase(lowerCaseAndUnderscoredWord.charAt(0))
                + camelCase(lowerCaseAndUnderscoredWord, true, delimiterChars).substring(1);
    }

    private static String replaceAllWithUppercase(String input, String regex, int groupNumberToUppercase) {
        Pattern underscoreAndDotPattern = Pattern.compile(regex);
        Matcher matcher = underscoreAndDotPattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(groupNumberToUppercase).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
