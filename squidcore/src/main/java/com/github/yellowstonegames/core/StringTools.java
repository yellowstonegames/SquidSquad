/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.TextTools;
import com.github.tommyettinger.ds.ObjectList;
import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.Replacer;

import java.util.Iterator;
import java.util.List;

/**
 * Various utility functions for handling readable natural-language text. This has tools to wrap long
 * CharSequences to fit in a maximum width, and generally tidy up generated text. This last step includes padding left
 * and right (including a "strict" option that truncates Strings that are longer than the padded size), Capitalizing
 * Each Word, Capitalizing the first word in a sentence, replacing "a improper usage of a" with "an improved
 * replacement using an," etc. This also has a lot of predefined categories of chars that are considered widely enough
 * supported by fonts, like {@link #COMMON_PUNCTUATION} and {@link #LATIN_LETTERS_UPPER}.
 * @author <a href="https://github.com/tommyettinger">Tommy Ettinger</a>
 */
public final class StringTools {
    /**
     * No need to instantiate.
     */
    private StringTools() {
    }

    /**
     * Use {@link com.github.tommyettinger.digital.TextTools#join(CharSequence, Object[])} instead.
     * @param delimiter
     * @param elements
     * @return
     */
    @Deprecated
    public static String join(CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    public static String joinArrays(CharSequence delimiter, char[]... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, long... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, double... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, int... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, float... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, short... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, char... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, byte... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, boolean... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    /**
     * Like {@link com.github.tommyettinger.digital.Base#join(String, long[])}, but this appends an 'L' to each number,
     * so they can be read in by Java.
     * Replaced by {@link com.github.tommyettinger.digital.Base#joinReadable(String, long[])} in most circumstances (the
     * replacement needs a String delimiter, while this takes a CharSequence).
     *
     * @param delimiter
     * @param elements
     * @return
     */
    public static String joinReadably(CharSequence delimiter, long... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(elements.length << 2);
        sb.append(elements[0]).append('L');
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]).append('L');
        }
        return sb.toString();
    }

    /**
     * Like {@link com.github.tommyettinger.digital.Base#appendJoined(StringBuilder, String, long[])}, but this appends an 'L' to each number so they
     * can be read in by Java.
     * Replaced by {@link com.github.tommyettinger.digital.Base#appendJoinedReadable(StringBuilder, String, long[])} in
     * most circumstances (the replacement needs a String delimiter, while this takes a CharSequence).
     * @param sb
     * @param delimiter
     * @param elements
     * @return
     */
    public static StringBuilder appendJoinedReadably(StringBuilder sb, CharSequence delimiter, long... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]).append('L');
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]).append('L');
        }
        return sb;
    }

    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, CharSequence... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }

    public static StringBuilder appendJoinedArrays(StringBuilder sb, CharSequence delimiter, char[]... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }

    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, long... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, double... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, int... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, float... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, short... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, char... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, byte... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, boolean... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a String, using "1" for true and "0" for false.
     * This is "dense" because it doesn't have any delimiters between elements.
     * Using {@link TextTools#joinDense(boolean...)} is recommended instead.
     *
     * @param elements an array or vararg of booleans
     * @return a String using 1 for true elements and 0 for false, or the empty string if elements is null or empty
     * @deprecated
     */
    @Deprecated
    public static String joinDense(boolean... elements) {
        return joinDense('1', '0', elements);
    }
    /**
     * Joins the boolean array {@code elements} without delimiters into a String, using the char {@code t} for
     * true and the char {@code f} for false. This is "dense" because it doesn't have any delimiters between
     * elements.
     * Using {@link TextTools#joinDense(char, char, boolean...)} is recommended instead.
     * @param t the char to write for true values
     * @param f the char to write for false values
     * @param elements an array or vararg of booleans
     * @return a String using 1 for true elements and 0 for false, or the empty string if elements is null or empty
     * @deprecated
     */
    @Deprecated
    public static String joinDense(char t, char f, boolean... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i] ? t : f);
        }
        return sb.toString();
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a StringBuilder, using "1" for true and "0" for
     * false. This is "dense" because it doesn't have any delimiters between elements.
     * Using {@link TextTools#appendJoinedDense(StringBuilder, boolean...)} is recommended instead.
     * @param sb a StringBuilder that will be modified in-place
     * @param elements an array or vararg of booleans
     * @return sb after modifications (if elements was non-null)
     * @deprecated
     */
    @Deprecated
    public static StringBuilder appendJoinedDense(StringBuilder sb, boolean... elements) {
        return TextTools.appendJoinedDense(sb, '1', '0', elements);
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a StringBuilder, using the char {@code t} for
     * true and the char {@code f} for false. This is "dense" because it doesn't have any delimiters between
     * elements.
     * Using {@link TextTools#appendJoinedDense(StringBuilder, char, char, boolean...)} is recommended instead.
     * @param sb a StringBuilder that will be modified in-place
     * @param t the char to write for true values
     * @param f the char to write for false values
     * @param elements an array or vararg of booleans
     * @return sb after modifications (if elements was non-null)
     * @deprecated
     */
    @Deprecated
    public static StringBuilder appendJoinedDense(StringBuilder sb, char t, char f, boolean... elements) {
        if (sb == null || elements == null) return sb;
        if(elements.length == 0) return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i] ? t : f);
        }
        return sb;
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object; it
     * takes a non-vararg Object array instead.
     * Using {@link TextTools#join(CharSequence, Object[])} is recommended instead.
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if the array is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     * @deprecated
     */
    @Deprecated
    public static String join(CharSequence delimiter, Object[] elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(elements.length << 2);
        sb.append(elements[0]);
        if(delimiter == null) delimiter = "";
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. This can take any Iterable of any type for its
     * elements parameter.
     * Using {@link TextTools#join(CharSequence, Iterable)} is recommended instead.
     *
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if Iterable is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     * @deprecated
     */
    @Deprecated
    public static String join(CharSequence delimiter, Iterable<?> elements) {
        if (elements == null) return "";
        Iterator<?> it = elements.iterator();
        if(!it.hasNext()) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(it.next());
        if(delimiter == null) delimiter = "";
        while(it.hasNext()) {
            sb.append(delimiter).append(it.next());
        }
        return sb.toString();
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object; it
     * takes a non-vararg Object array instead.
     * Using {@link TextTools#appendJoined(StringBuilder, CharSequence, Object[])} is recommended instead.
     * @param sb a StringBuilder that will be modified in-place
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if the array is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return sb after modifications (if elements was non-null)
     * @deprecated
     */
    @Deprecated
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, Object[] elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(elements[0]);
        if(delimiter == null) delimiter = "";
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb;
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. This can take any Iterable of any type for its
     * {@code elements} parameter.
     * Using {@link TextTools#appendJoined(StringBuilder, CharSequence, Iterable)} is recommended instead.
     * @param sb a StringBuilder that will be modified in-place
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if Iterable is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return sb after modifications (if elements was non-null)
     * @deprecated
     */
    @Deprecated
    public static StringBuilder appendJoined(StringBuilder sb, CharSequence delimiter, Iterable<?> elements) {
        if (sb == null || elements == null) return sb;
        Iterator<?> it = elements.iterator();
        if(!it.hasNext()) return sb;
        sb.append(it.next());
        if(delimiter == null) delimiter = "";
        while(it.hasNext()) {
            sb.append(delimiter).append(it.next());
        }
        return sb;
    }

    /**
     * Searches text for the exact contents of the char array search; returns true if text contains search.
     * Use {@link TextTools#contains(CharSequence, CharSequence)} instead.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     * @deprecated
     */
    @Deprecated
    public static boolean contains(CharSequence text, CharSequence search) {
        return !(text == null || text.length() == 0 || search == null || search.length() == 0)
                && containsPart(text, search) == search.length();
    }

    /**
     * Tries to find as much of the char array {@code search} in the CharSequence {@code text}, always starting from the
     * beginning of search (if the beginning isn't found, then it finds nothing), and returns the length of the found
     * part of search (0 if not found).
     * Use {@link TextTools#containsPart(CharSequence, CharSequence)} instead.
     *
     * @param text a CharSequence to search in
     * @param search a char array to look for
     * @return the length of the searched-for char array that was found
     * @deprecated
     */
    @Deprecated
    public static int containsPart(CharSequence text, CharSequence search)
    {
        if(text == null || text.length() == 0 || search == null || (search.length() == 0))
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
     * Use {@link TextTools#contains(CharSequence, char[])} instead.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     * @deprecated
     */
    @Deprecated
    public static boolean contains(CharSequence text, char[] search) {
        return !(text == null || text.length() == 0 || search == null || search.length == 0)
                && containsPart(text, search) == search.length;
    }

    /**
     * Tries to find as much of the char array {@code search} in the CharSequence {@code text}, always starting from the
     * beginning of search (if the beginning isn't found, then it finds nothing), and returns the length of the found
     * part of search (0 if not found).
     * Use {@link TextTools#containsPart(CharSequence, char[])} instead.
     * @param text a CharSequence to search in
     * @param search a char array to look for
     * @return the length of the searched-for char array that was found
     * @deprecated
     */
    @Deprecated
    public static int containsPart(CharSequence text, char[] search)
    {
        if(text == null || text.length() == 0 || search == null || (search.length == 0))
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
     * This is almost certainly too specific to be useful outside a handful of cases, but it isn't marked as deprecated
     * because it was removed from TextTools. If you for whatever reason need this, it is here.
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
        char s = (bl == 0) ? (search.length == 0 ? suffix.charAt(0) : search[0]) : prefix.charAt(0);
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

    /**
     * Use {@link TextTools#replace(CharSequence, CharSequence, CharSequence)} instead.
     * @param text
     * @param before
     * @param after
     * @return
     * @deprecated
     */
    @Deprecated
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
     * This delegates to {@link TextTools#safeSubstring(String, int, int)}.
     * @param source the String to get a substring from
     * @param beginIndex the first index, inclusive; will be treated as 0 if negative
     * @param endIndex the index after the last character (exclusive); if negative this will be source.length()
     * @return the substring of source between beginIndex and endIndex, or "" if any parameters are null/invalid
     */
    public static String safeSubstring(String source, int beginIndex, int endIndex)
    {
        return TextTools.safeSubstring(source, beginIndex, endIndex);
    }

    /**
     * Like {@link String#split(String)} but doesn't use any regex for splitting (the delimiter is a literal String).
     * @param source the String to get split-up substrings from
     * @param delimiter the literal String to split on (not a regex); will not be included in the returned String array
     * @return a String array consisting of at least one String (the entirety of Source if nothing was split)
     */
    public static String[] split(String source, String delimiter) {
        return TextTools.split(source, delimiter);
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

    /**
     * Word-wraps the given String (or other CharSequence, such as a StringBuilder) so it is split into zero or more
     * Strings as lines of text, with the given width as the maximum width for a line. This correctly splits most (all?)
     * text in European languages on spaces (treating all whitespace characters matched by the regex '\\s' as breaking),
     * and also uses the English-language rule (probably used in other languages as well) of splitting on hyphens and
     * other dash characters (Unicode category Pd) in the middle of a word. This means for a phrase like "UN Secretary
     * General Ban-Ki Moon", if the width was 12, then the Strings in the List returned would be
     * <br>
     * <pre>
     * "UN Secretary"
     * "General Ban-"
     * "Ki Moon"
     * </pre>
     * Spaces are not preserved if they were used to split something into two lines, but dashes are.
     * @param longText a probably-large piece of text that needs to be split into multiple lines with a max width
     * @param width the max width to use for any line, removing trailing whitespace at the end of a line
     * @return a List of Strings for the lines after word-wrapping
     */
    public static List<String> wrap(CharSequence longText, int width)
    {
        if(width <= 0)
            return new ObjectList<>(0);
        return wrap(new ObjectList<>(longText.length() / width + 2), longText, width);
    }
    /**
     * Word-wraps the given String (or other CharSequence, such as a StringBuilder) so it is split into zero or more
     * Strings as lines of text, with the given width as the maximum width for a line; appends the word-wrapped lines to
     * the given List of Strings and does not create a new List. This correctly splits most (all?) text in European
     * languages on spaces (treating all whitespace characters matched by the regex '\\s' as breaking), and also uses
     * the English-language rule (probably used in other languages as well) of splitting on hyphens and other dash
     * characters (Unicode category Pd) in the middle of a word. This means for a phrase like "UN Secretary General
     * Ban-Ki Moon", if the width was 12, then the Strings in the List returned would be
     * <br>
     * <pre>
     * "UN Secretary"
     * "General Ban-"
     * "Ki Moon"
     * </pre>
     * Spaces are not preserved if they were used to split something into two lines, but dashes are.
     * @param receiving the List of String to append the word-wrapped lines to
     * @param longText a probably-large piece of text that needs to be split into multiple lines with a max width
     * @param width the max width to use for any line, removing trailing whitespace at the end of a line
     * @return the given {@code receiving} parameter, after appending the lines from word-wrapping
     */
    public static List<String> wrap(List<String> receiving, CharSequence longText, int width)
    {
        if(width <= 0 || receiving == null)
            return receiving;
        Matcher widthMatcher = Pattern.compile("(?:({=Y}(?!\\s).{1," + width + "})((?<=\\p{Pd})|(\\s+)))|({=Y}\\S{1," + width + "})").matcher(longText + "\n");
        while (widthMatcher.find())
        {
            receiving.add(widthMatcher.group("Y"));
        }
        return receiving;
    }

    public static final Pattern whitespacePattern = Pattern.compile("\\s+"),
            nonSpacePattern = Pattern.compile("\\S+");
    private static final Matcher matcher = new Matcher(whitespacePattern);
    public static int indexOf(CharSequence text, Pattern regex, int beginIndex)
    {
        matcher.setPattern(regex);
        matcher.setTarget(text);
        matcher.setPosition(beginIndex);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    public static int indexOf(CharSequence text, String regex, int beginIndex)
    {
        matcher.setPattern(Pattern.compile(regex));
        matcher.setTarget(text);
        matcher.setPosition(beginIndex);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    public static int indexOf(CharSequence text, Pattern regex)
    {
        matcher.setPattern(regex);
        matcher.setTarget(text);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    public static int indexOf(CharSequence text, String regex)
    {
        matcher.setPattern(Pattern.compile(regex));
        matcher.setTarget(text);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    private static final Matcher capitalizeMatcher = Pattern.compile("(?<!\\pL)(\\pL)(\\pL*)(\\PL*)").matcher();
    private static final StringBuilder sb = new StringBuilder(64);

    /**
     * Capitalizes Each Word In The Parameter {@code original}, Returning A New String.
     * @param original a CharSequence, such as a StringBuilder or String, which could have CrAzY capitalization
     * @return A String With Each Word Capitalized At The Start And The Rest In Lower Case
     */
    public static String capitalize(final CharSequence original) {
        if (original == null || original.length() == 0) {
            return "";
        }
        sb.setLength(0);
        capitalizeMatcher.setTarget(original);
        while (capitalizeMatcher.find()) {
            sb.append(capitalizeMatcher.group(1).toUpperCase());
            capitalizeMatcher.getGroup(2, sb, 1); // mode 1 is case-insensitive, which lower-cases result
            capitalizeMatcher.getGroup(3, sb);
        }
        return sb.toString();
    }
    private static final Matcher sentenceMatcher = Pattern.compile("(\\PL*)((\\pL)([^.?!]*)($|[.?!]+))(\\PL*)").matcher();
    // group 1 before letters, group 2 whole sentence, group 3 first letter, group 4 rest of sentence, group 5 closing punctuation, group 6 remainder of non-letters

    /**
     * Attempts to scan for sentences in {@code original}, capitalizes the first letter of each sentence, and otherwise
     * leaves the CharSequence untouched as it returns it as a String. Sentences are detected with a crude heuristic of
     * "does it have periods, exclamation marks, or question marks at the end, or does it reach the end of input? If
     * yes, it's a sentence."
     * @param original a CharSequence that is expected to contain sentence-like data that needs capitalization; existing upper-case letters will stay upper-case.
     * @return a String where the first letter of each sentence (detected as best this can) is capitalized.
     */
    public static String sentenceCase(final CharSequence original) {
        if (original == null || original.length() == 0) {
            return "";
        }
        sb.setLength(0);
        sentenceMatcher.setTarget(original);
        while (sentenceMatcher.find()) {
            sentenceMatcher.getGroup(1, sb);
            sb.append(sentenceMatcher.group(3).toUpperCase());
            sentenceMatcher.getGroup(4, sb); // use getGroup(4, sb, 1) if this should lower-case the rest
            sentenceMatcher.getGroup(5, sb);
            sentenceMatcher.getGroup(6, sb);
        }
        return sb.toString();
    }
    private static final Replacer anReplacer = new Replacer(Pattern.compile("\\b(a)(\\p{G}+)(?=[àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųu])", Pattern.IGNORE_CASE | Pattern.UNICODE), "$1n$2");

    /**
     * A simple method that looks for any occurrences of the word 'a' followed by some non-zero amount of whitespace and
     * then any vowel starting the following word (such as 'a item'), then replaces each such improper 'a' with 'an'
     * (such as 'an item'). The regex used here isn't bulletproof, but it should be fairly robust, handling when you
     * have multiple whitespace chars, different whitespace chars (like carriage return and newline), accented vowels in
     * the following word (but not in the initial 'a', which is expected to use English spelling rules), and the case of
     * the initial 'a' or 'A'.
     * <br>
     * Gotta love Regexodus; this is a two-liner that uses features specific to that regular expression library.
     * @param text the (probably generated English) multi-word text to search for 'a' in and possibly replace with 'an'
     * @return a new String with every improper 'a' replaced
     */
    public static String correctABeforeVowel(final CharSequence text){
        return anReplacer.replace(text);
    }

    /**
     * A constant containing only chars that are reasonably likely to be supported by broad fonts and thus display-able.
     * This assumes the font supports Latin, Greek, and Cyrillic alphabets, with good support for extended Latin (at
     * least for European languages) but not required to be complete enough to support the very large Vietnamese set of
     * extensions to Latin, nor to support any International Phonetic Alphabet (IPA) chars. It also assumes box drawing
     * characters are supported and a handful of common dingbats, such as male and female signs. It does not include
     * the tab, newline, or carriage return characters, since these don't usually make sense on a grid of chars.
     */
    public static final String PERMISSIBLE_CHARS =
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno"+
                    "pqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàá"+
                    "âãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİı"+
                    "ĴĵĶķĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƒǺǻǼǽǾǿ"+
                    "ȘșȚțȷˆˇˉˋ˘˙˚˛˜˝΄΅Ά·ΈΉΊΌΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυ"+
                    "φχψωϊϋόύώЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхц"+
                    "чшщъыьэюяѐёђѓєѕіїјљњћќѝўџѴѵҐґẀẁẂẃẄẅỲỳ–—‘’‚‛“”„†‡•…‰‹›ⁿ₤€№™Ω℮←↑→↓∆−√≈" +
                    "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬■□▲▼○●◦♀♂♠♣♥♦♪";

    public static final String BOX_DRAWING_SINGLE = "─│┌┐└┘├┤┬┴┼";
    public static final String BOX_DRAWING_DOUBLE = "═║╔╗╚╝╠╣╦╩╬";
    public static final String BOX_DRAWING = "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬";
    public static final String VISUAL_SYMBOLS = "←↑→↓■□▲▼○●◦♀♂♠♣♥♦♪";
    public static final String DIGITS = "0123456789";
    public static final String MARKS = "~`^'¨¯°´¸ˆˇˉˋ˘˙˚˛˜˝΄΅‘’‚‛";
    /**
     * Can be used to match an index with one in {@link #GROUPING_SIGNS_CLOSE} to find the closing char (this way only).
     */
    public static final String GROUPING_SIGNS_OPEN  = "([{<«‘‛“‹";
    /**
     * An index in {@link #GROUPING_SIGNS_OPEN} can be used here to find the closing char for that opening one.
     */
    public static final String GROUPING_SIGNS_CLOSE = ")]}>»’’”›";
    public static final String COMMON_PUNCTUATION = "!\"%&'*+,-./:;<>?•…–—";
    public static final String MODERN_PUNCTUATION = "@\\^_`|~¦©®™´№♀♂♪";
    public static final String UNCOMMON_PUNCTUATION = "§¶¨ªº¯°·¸¡¿·‚„†‡";
    public static final String TECHNICAL_PUNCTUATION = "#%'*+,-./<=>^|¬°µ±¹²³ⁿ¼½¾×÷‰№Ω℮∆−√≈";
    public static final String PUNCTUATION = COMMON_PUNCTUATION + MODERN_PUNCTUATION + UNCOMMON_PUNCTUATION +
            TECHNICAL_PUNCTUATION + GROUPING_SIGNS_OPEN + GROUPING_SIGNS_CLOSE;
    public static final String CURRENCY = "$¢£¤¥₤€";
    public static final String SPACING = " ";
    public static final String ENGLISH_LETTERS_UPPER =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ENGLISH_LETTERS_LOWER =
            "abcdefghijklmnopqrstuvwxyz";
    public static final String ENGLISH_LETTERS = ENGLISH_LETTERS_UPPER + ENGLISH_LETTERS_LOWER;

    public static final String LATIN_EXTENDED_LETTERS_UPPER =
            "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞĀĂĄĆĈĊČĎĐĒĔĖĘĚĜĞĠĢĤĦĨĪĬĮİĴĶĹĻĽĿŁŃŅŇŊŌŎŐŒŔŖŘŚŜŞŠŢŤŨŪŬŮŰŲŴŶŸŹŻŽǺǼǾȘȚẀẂẄỲßSFJ";
    public static final String LATIN_EXTENDED_LETTERS_LOWER =
            "àáâãäåæçèéêëìíîïðñòóôõöøùúûüýþāăąćĉċčďđēĕėęěĝğġģĥħĩīĭįıĵķĺļľŀłńņňŋōŏőœŕŗřśŝşšţťũūŭůűųŵŷÿźżžǻǽǿșțẁẃẅỳßſƒȷ";
    public static final String LATIN_EXTENDED_LETTERS = LATIN_EXTENDED_LETTERS_UPPER + LATIN_EXTENDED_LETTERS_LOWER;

    public static final String LATIN_LETTERS_UPPER = ENGLISH_LETTERS_UPPER + LATIN_EXTENDED_LETTERS_UPPER;
    public static final String LATIN_LETTERS_LOWER = ENGLISH_LETTERS_LOWER + LATIN_EXTENDED_LETTERS_LOWER;
    public static final String LATIN_LETTERS = LATIN_LETTERS_UPPER + LATIN_LETTERS_LOWER;

    /**
     * Includes the letter Sigma, 'Σ', twice because it has two lower-case forms in {@link #GREEK_LETTERS_LOWER}. This
     * lets you use one index for both lower and upper case, like with Latin and Cyrillic.
     */
    public static final String GREEK_LETTERS_UPPER =
            "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΣΤΥΦΧΨΩΆΈΉΊΌΎΏΪΫΪΫ";
    /**
     * Includes both lower-case forms for Sigma, 'ς' and 'σ', but this matches the two upper-case Sigma in
     * {@link #GREEK_LETTERS_UPPER}. This lets you use one index for both lower and upper case, like with Latin and
     * Cyrillic.
     */
    public static final String GREEK_LETTERS_LOWER =
            "αβγδεζηθικλμνξοπρςστυφχψωάέήίόύώϊϋΐΰ";

    public static final String GREEK_LETTERS = GREEK_LETTERS_UPPER + GREEK_LETTERS_LOWER;

    public static final String CYRILLIC_LETTERS_UPPER =
            "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏѴҐ";
    public static final String CYRILLIC_LETTERS_LOWER =
            "абвгдежзийклмнопрстуфхцчшщъыьэюяѐёђѓєѕіїјљњћќѝўџѵґ";
    public static final String CYRILLIC_LETTERS = CYRILLIC_LETTERS_UPPER + CYRILLIC_LETTERS_LOWER;

    public static final String LETTERS_UPPER = LATIN_LETTERS_UPPER + GREEK_LETTERS_UPPER + CYRILLIC_LETTERS_UPPER;
    public static final String LETTERS_LOWER = LATIN_LETTERS_LOWER + GREEK_LETTERS_LOWER + CYRILLIC_LETTERS_LOWER;
    public static final String LETTERS = LETTERS_UPPER + LETTERS_LOWER;
    public static final String LETTERS_AND_NUMBERS = LETTERS + DIGITS;
}
