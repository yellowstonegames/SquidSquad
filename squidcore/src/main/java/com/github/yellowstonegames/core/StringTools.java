package com.github.yellowstonegames.core;

import java.util.Iterator;

/**
 * Various utility functions for dealing with Strings, CharSequences, and char[]s. This has tools to join arrays and
 * Iterables of various items into long Strings, split/search/count occurrences of literal char arrays or CharSequences
 * without using any regex, and generally tidy up generated text. This last step includes padding left and right
 * (including a "strict" option that truncates Strings that are longer than the padded size).
 * @author Tommy Ettinger
 */
public class StringTools {
    public static String join(CharSequence delimiter, CharSequence... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }

    public static String joinArrays(CharSequence delimiter, char[]... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, char[]... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }

    public static String join(CharSequence delimiter, long... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, long... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, double... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, double... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, int... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, int... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, float... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, float... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, short... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, short... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, char... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, char... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, byte... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, byte... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    public static String join(CharSequence delimiter, boolean... elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, boolean... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object, such
     * as {@link #join(CharSequence, Iterable)}; it takes a non-vararg Object array instead.
     * @param delimiter the String or other CharSequence to separate items in elements with
     * @param elements the Object items to stringify and join into one String; if the array is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     */
    public static String join(CharSequence delimiter, Object[] elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, Object... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]);
        }
        return stringBuilder;
    }
    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. This can take any Iterable of any type for its
     * elements parameter.
     * @param delimiter the String or other CharSequence to separate items in elements with
     * @param elements the Object items to stringify and join into one String; if Iterable is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     */
    public static String join(CharSequence delimiter, Iterable<?> elements) {
        return join(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder join(StringBuilder stringBuilder, CharSequence delimiter, Iterable<?> elements) {
        if (elements == null) return stringBuilder;
        Iterator<?> it = elements.iterator();
        if(!it.hasNext()) return stringBuilder;
        stringBuilder.append(it.next());
        while(it.hasNext()) {
            stringBuilder.append(delimiter).append(it.next());
        }
        return stringBuilder;
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a String, using "1" for true and "0" for false.
     * @param elements an array or vararg of booleans
     * @return a String using 1 for true elements and 0 for false, or "N" if elements is null
     */
    public static String joinAlt(boolean... elements) {
        return joinAlt(new StringBuilder(64), elements).toString();
    }

    public static StringBuilder joinAlt(StringBuilder stringBuilder, boolean... elements) {
        if (elements == null) return stringBuilder.append("N");
        if(elements.length == 0) return stringBuilder;
        for (int i = 0; i < elements.length; i++) {
            stringBuilder.append(elements[i] ? '1' : '0');
        }
        return stringBuilder;
    }

    /**
     * Like {@link #join(CharSequence, long...)}, but this appends an 'L' to each number so they can be read in by Java.
     * @param delimiter
     * @param elements
     * @return
     */
    public static String joinAlt(CharSequence delimiter, long... elements) {
        return joinAlt(new StringBuilder(64), delimiter, elements).toString();
    }
    public static StringBuilder joinAlt(StringBuilder stringBuilder, CharSequence delimiter, long... elements) {
        if (elements == null || elements.length == 0) return stringBuilder;
        stringBuilder.append(elements[0]).append('L');
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(delimiter).append(elements[i]).append('L');
        }
        return stringBuilder;
    }

    /**
     * Searches text for the exact contents of the char array search; returns true if text contains search.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     */
    public static boolean contains(CharSequence text, CharSequence search) {
        return !(text == null || text.length() == 0 || search == null || search.length() <= 0)
                && containsPart(text, search) == search.length();
    }

    /**
     * Tries to find as much of the char array {@code search} in the CharSequence {@code text}, always starting from the
     * beginning of search (if the beginning isn't found, then it finds nothing), and returns the length of the found
     * part of search (0 if not found).
     * @param text a CharSequence to search in
     * @param search a char array to look for
     * @return the length of the searched-for char array that was found
     */
    public static int containsPart(CharSequence text, CharSequence search)
    {
        if(text == null || text.length() == 0 || search == null || (search.length() <= 0))
            return 0;
        int sl = search.length(), tl = text.length() - sl, f = 0;
        char s = search.charAt(0);
        PRIMARY:
        for (int i = 0; i <= tl; i++) {
            if (text.charAt(i) == s) {
                for (int j = i + 1, x = 1; x < sl; j++, x++) {
                    if (text.charAt(j) != search.charAt(x)) {
                        f = Math.max(f, x);
                        continue PRIMARY;
                    }
                }
                return sl;
            }
        }
        return f;
    }

    /**
     * Searches text for the exact contents of the char array search; returns true if text contains search.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     */
    public static boolean contains(CharSequence text, char[] search) {
        return !(text == null || text.length() == 0 || search == null || search.length <= 0)
                && containsPart(text, search) == search.length;
    }

    /**
     * Tries to find as much of the char array {@code search} in the CharSequence {@code text}, always starting from the
     * beginning of search (if the beginning isn't found, then it finds nothing), and returns the length of the found
     * part of search (0 if not found).
     * @param text a CharSequence to search in
     * @param search a char array to look for
     * @return the length of the searched-for char array that was found
     */
    public static int containsPart(CharSequence text, char[] search)
    {
        if(text == null || text.length() == 0 || search == null || (search.length <= 0))
            return 0;
        int sl = search.length, tl = text.length() - sl, f = 0;
        char s = search[0];
        PRIMARY:
        for (int i = 0; i <= tl; i++) {
            if (text.charAt(i) == s) {
                for (int j = i + 1, x = 1; x < sl; j++, x++) {
                    if (text.charAt(j) != search[x]) {
                        f = Math.max(f, x);
                        continue PRIMARY;
                    }
                }
                return sl;
            }
        }
        return f;
    }

    /**
     * Tries to find as much of the sequence {@code prefix search suffix} as it can in text, where prefix and suffix are
     * CharSequences for some reason and search is a char array. Returns the length of the sequence it was able to
     * match, up to {@code prefix.length() + search.length + suffix.length()}, or 0 if no part of the looked-for
     * sequence could be found.
     * <br>
     * This is almost certainly too specific to be useful outside of a handful of cases.
     * @param text a CharSequence to search in
     * @param search a char array to look for, surrounded by prefix and suffix
     * @param prefix a mandatory prefix before search, separated for some weird optimization reason
     * @param suffix a mandatory suffix after search, separated for some weird optimization reason
     * @return the length of the searched-for prefix+search+suffix that was found
     */
    public static int containsPart(CharSequence text, char[] search, CharSequence prefix, CharSequence suffix)
    {
        if(prefix == null) prefix = "";
        if(suffix == null) suffix = "";
        int bl = prefix.length(), el = suffix.length();
        if(text == null || text.length() == 0 || search == null || (search.length + bl + el <= 0))
            return 0;
        int sl = bl + search.length + el, tl = text.length() - sl, f = 0, sl2 = sl - el;
        char s = (bl <= 0) ? (search.length <= 0 ? suffix.charAt(0) : search[0]) : prefix.charAt(0);
        PRIMARY:
        for (int i = 0; i <= tl; i++) {
            if(text.charAt(i) == s)
            {
                for (int j = i+1, x = 1; x < sl; j++, x++) {
                    if(x < bl)
                    {
                        if (text.charAt(j) != prefix.charAt(x)) {
                            f = Math.max(f, x);
                            continue PRIMARY;
                        }
                    }
                    else if(x < sl2)
                    {
                        if (text.charAt(j) != search[x-bl]) {
                            f = Math.max(f, x);
                            continue PRIMARY;
                        }
                    }
                    else
                    {
                        if (text.charAt(j) != suffix.charAt(x - sl2)) {
                            f = Math.max(f, x);
                            continue PRIMARY;
                        }
                    }
                }
                return sl;
            }
        }
        return f;
    }

    public static String replace(CharSequence text, CharSequence before, CharSequence after) {
        if(text instanceof String)
        {
            return ((String)text).replace(before, after);
        }
        String t = text.toString();
        return t.replace(before, after);
    }

    /**
     * Scans repeatedly in {@code source} for the String {@code search}, not scanning the same char twice except as part
     * of a larger String, and returns the number of instances of search that were found, or 0 if source is null or if
     * search is null or empty.
     * @param source a String to look through
     * @param search a String to look for
     * @return the number of times search was found in source
     */
    public static int count(final String source, final String search)
    {
        if(source == null || search == null || source.isEmpty() || search.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(search, idx+1)) >= 0)
            ++amount;
        return amount;
    }

    /**
     * Scans repeatedly in {@code source} for the codepoint {@code search} (which is usually a char literal), not
     * scanning the same section twice, and returns the number of instances of search that were found, or 0 if source is
     * null.
     * @param source a String to look through
     * @param search a codepoint or char to look for
     * @return the number of times search was found in source
     */
    public static int count(final String source, final int search)
    {
        if(source == null || source.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(search, idx+1)) >= 0)
            ++amount;
        return amount;
    }
    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the String {@code search}, not scanning the same char twice except as part of a larger String, and returns the
     * number of instances of search that were found, or 0 if source or search is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * @param source a String to look through
     * @param search a String to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times search was found in source
     */
    public static int count(final String source, final String search, final int startIndex, int endIndex)
    {
        if(endIndex < 0) endIndex = 0x7fffffff;
        if(source == null || search == null || source.isEmpty() || search.isEmpty()
                || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex-1;
        while ((idx = source.indexOf(search, idx+1)) >= 0 && idx < endIndex)
            ++amount;
        return amount;
    }

    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the codepoint {@code search} (which is usually a char literal), not scanning the same section twice, and returns
     * the number of instances of search that were found, or 0 if source is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * @param source a String to look through
     * @param search a codepoint or char to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times search was found in source
     */
    public static int count(final String source, final int search, final int startIndex, int endIndex)
    {
        if(endIndex < 0) endIndex = 0x7fffffff;
        if(source == null || source.isEmpty() || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex-1;
        while ((idx = source.indexOf(search, idx+1)) >= 0 && idx < endIndex)
            ++amount;
        return amount;
    }

    /**
     * Like {@link String#substring(int, int)} but returns "" instead of throwing any sort of Exception.
     * @param source the String to get a substring from
     * @param beginIndex the first index, inclusive; will be treated as 0 if negative
     * @param endIndex the index after the last character (exclusive); if negative this will be source.length()
     * @return the substring of source between beginIndex and endIndex, or "" if any parameters are null/invalid
     */
    public static String safeSubstring(String source, int beginIndex, int endIndex)
    {
        if(source == null || source.isEmpty()) return "";
        if(beginIndex < 0) beginIndex = 0;
        if(endIndex < 0 || endIndex > source.length()) endIndex = source.length();
        if(beginIndex > endIndex) return "";
        return source.substring(beginIndex, endIndex);
    }

    /**
     * Like {@link String#split(String)} but doesn't use any regex for splitting (delimiter is a literal String).
     * @param source the String to get split-up substrings from
     * @param delimiter the literal String to split on (not a regex); will not be included in the returned String array
     * @return a String array consisting of at least one String (all of Source if nothing was split)
     */
    public static String[] split(String source, String delimiter) {
        int amount = count(source, delimiter);
        if (amount <= 0) return new String[]{source};
        String[] splat = new String[amount+1];
        int dl = delimiter.length(), idx = -dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = safeSubstring(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = safeSubstring(source, idx+dl, source.length());
        }
        else
        {
            splat[amount] = safeSubstring(source, idx+dl, idx2);
        }
        return splat;
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the right with spaces until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with spaces to reach the given minimum length
     */
    public static String padRight(String text, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padRightStrict(text, ' ', minimumLength);
        return text;
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the right with padChar
     * until it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param padChar the char to use to pad text, if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with padChar to reach the given minimum length
     */
    public static String padRight(String text, char padChar, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padRightStrict(text, padChar, minimumLength);
        return text;
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its right side with spaces until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly extra spaces
     */
    public static String padRightStrict(String text, int totalLength) {
        return padRightStrict(text, ' ', totalLength);
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its right side with padChar until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param padChar the char to use to fill any remaining length
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly padChar
     */
    public static String padRightStrict(String text, char padChar, int totalLength) {
        char[] c = new char[totalLength];
        int len = text.length();
        text.getChars(0, Math.min(len, totalLength), c, 0);
        for (int i = len; i < totalLength; i++) {
            c[i] = padChar;
        }
        return String.valueOf(c);
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the left with spaces until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with spaces to reach the given minimum length
     */
    public static String padLeft(String text, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padLeftStrict(text, ' ', minimumLength);
        return text;
    }
    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the left with padChar until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param padChar the char to use to pad text, if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with padChar to reach the given minimum length
     */
    public static String padLeft(String text, char padChar, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padLeftStrict(text, padChar, minimumLength);
        return text;
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its left side with spaces until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly extra spaces
     */
    public static String padLeftStrict(String text, int totalLength) {
        return padLeftStrict(text, ' ', totalLength);
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its left side with padChar until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param padChar the char to use to fill any remaining length
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly padChar
     */
    public static String padLeftStrict(String text, char padChar, int totalLength) {
        char[] c = new char[totalLength];
        int len = text.length();
        text.getChars(0, Math.min(len, totalLength), c, Math.max(0, totalLength - len));
        for (int i = totalLength - len - 1; i >= 0; i--) {
            c[i] = padChar;
        }
        return String.valueOf(c);
    }
}
