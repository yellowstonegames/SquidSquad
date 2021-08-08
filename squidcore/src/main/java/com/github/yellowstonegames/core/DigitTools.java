package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.BitConversion;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Utility class for converting to and from numbers and their String representations.
 */
public class DigitTools {
    /**
     * Stores various ways to encode digits, such as binary ({@link #BASE2}, or just 0 and 1), decimal ({@link #BASE10},
     * 0 through 9), hexadecimal ({@link #BASE16}), and the even larger {@link #BASE36}. Of special note are the two
     * different approaches to encoding base-64 data: {@link #BASE64} is the standard format, and {@link #URI_SAFE} is
     * the different format used when encoding data for a URI (typically meant for the Internet).
     */
    public enum Encoding {
        BASE2("01"),
        BASE8("01234567"),
        BASE10("0123456789"),
        BASE16("0123456789ABCDEF"),
        BASE36("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        BASE64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", false, '='),
        URI_SAFE("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-", false, '$');

        /**
         * The digits this will encode to, in order from smallest to largest.
         */
        public final char[] toEncoded;
        /**
         * An array of the digit values corresponding to different ASCII codepoints
         */
        public final int[] fromEncoded;

        /**
         * When an encoding needs to indicate that a char is not considered part of a number, it uses this padding char;
         * this is mostly relevant for Base-64 and URI-safe encodings.
         */
        public final char paddingChar;
        private final int length1Byte, length2Byte, length4Byte, length8Byte, base;
        private final char[] progress;
        Encoding(String digits){
            this(digits, true, ' ');
        }
        Encoding(String digits, boolean caseInsensitive, char padding){
            paddingChar = padding;
            toEncoded = digits.toCharArray();
            base = toEncoded.length;
            fromEncoded = new int[128];

            //if(caseInsensitive)
            Arrays.fill(fromEncoded, -1);

            for (int i = 0; i < base; i++) {
                char to = toEncoded[i];
                fromEncoded[to] = i;
                if(caseInsensitive)
                    fromEncoded[Character.toLowerCase(to)] = i;
            }
            double logBase = 1.0 / Math.log(base);
            length1Byte = (int)Math.ceil(Math.log(0x1p8) * logBase);
            length2Byte = (int)Math.ceil(Math.log(0x1p16) * logBase);
            length4Byte = (int)Math.ceil(Math.log(0x1p32) * logBase);
            length8Byte = (int)Math.ceil(Math.log(0x1p64) * logBase);
            progress = new char[length8Byte];
        }

        /**
         * Converts the given {@code number} to the base specified by this Encoding, returning a new String.
         * @param number any long
         * @return a new String containing {@code number} in the radix this specifies.
         */
        @Nonnull
        public String unsigned(long number) {
            final int len = length8Byte - 1;
            final long sign = number & 0x8000000000000000L;
            number ^= sign;
            for (int i = 0; i < len; i++) {
                progress[len - i] = toEncoded[(int)(number % base)];
                number /= base;
            }
            progress[0] = toEncoded[(int)((number | (base >>> 1 & sign >> -1)) % base)];
            return String.valueOf(progress, 0, length8Byte);
        }

        /**
         * Converts the given {@code number} to the base specified by this Encoding, appending the result to
         * {@code builder}.
         * @param builder a non-null StringBuilder that will be modified (appended to)
         * @param number any long
         * @return a new String containing {@code number} in the radix this specifies.
         */
        @Nonnull
        public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, long number) {
            final int len = length8Byte - 1;
            final long sign = number & 0x8000000000000000L;
            number ^= sign;
            for (int i = 0; i < len; i++) {
                progress[len - i] = toEncoded[(int)(number % base)];
                number /= base;
            }
            progress[0] = toEncoded[(int)((number | (base >>> 1 & sign >> -1)) % base)];
            return builder.append(progress, 0, length8Byte);
        }

        /**
         * Converts the given {@code number} to the base specified by this Encoding, returning a new String.
         * @param number any int
         * @return a new String containing {@code number} in the radix this specifies.
         */
        @Nonnull
        public String unsigned(int number) {
            final int len = length4Byte - 1;
            final int sign = number & 0x80000000;
            number ^= sign;
            for (int i = 0; i < len; i++) {
                progress[len - i] = toEncoded[number % base];
                number /= base;
            }
            progress[0] = toEncoded[(number | (base >>> 1 & sign >> -1)) % base];
            return String.valueOf(progress, 0, length4Byte);
        }

        /**
         * Converts the given {@code number} to the base specified by this Encoding, appending the result to
         * {@code builder}.
         * @param builder a non-null StringBuilder that will be modified (appended to)
         * @param number any int
         * @return a new String containing {@code number} in the radix this specifies.
         */
        @Nonnull
        public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, int number) {
            final int len = length4Byte - 1;
            final int sign = number & 0x80000000;
            number ^= sign;
            for (int i = 0; i < len; i++) {
                progress[len - i] = toEncoded[number % base];
                number /= base;
            }
            progress[0] = toEncoded[(number | (base >>> 1 & sign >> -1)) % base];
            return builder.append(progress, 0, length4Byte);
        }

        /**
         * Converts the given {@code number} to the base specified by this Encoding, returning a new String.
         * @param number any short
         * @return a new String containing {@code number} in the radix this specifies.
         */
        @Nonnull
        public String unsigned(short number) {
            final int len = length2Byte - 1;
            final int sign = number & 0xFFFF8000;
            number ^= sign;
            for (int i = 0; i < len; i++) {
                progress[len - i] = toEncoded[number % base];
                number /= base;
            }
            progress[0] = toEncoded[(number | (base >>> 1 & sign >> -1)) % base];
            return String.valueOf(progress, 0, length2Byte);
        }

        /**
         * Converts the given {@code number} to the base specified by this Encoding, appending the result to
         * {@code builder}.
         * @param builder a non-null StringBuilder that will be modified (appended to)
         * @param number any short
         * @return a new String containing {@code number} in the radix this specifies.
         */
        @Nonnull
        public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, short number) {
            final int len = length2Byte - 1;
            final int sign = number & 0xFFFF8000;
            number ^= sign;
            for (int i = 0; i < len; i++) {
                progress[len - i] = toEncoded[number % base];
                number /= base;
            }
            progress[0] = toEncoded[(number | (base >>> 1 & sign >> -1)) % base];
            return builder.append(progress, 0, length2Byte);
        }

    }
    public static final String mask64 = "0000000000000000000000000000000000000000000000000000000000000000",
            mask32 = "00000000000000000000000000000000",
            mask16 = "0000000000000000",
            mask8 = "00000000";

    private static final StringBuilder hexBuilder = new StringBuilder(16).append(mask16);
    public static String hex(long number) {
        for (int i = 0; i < 16; i++) {
            hexBuilder.setCharAt(15 - i, Encoding.BASE16.toEncoded[(int)(number >> (i << 2) & 15)]);
        }
        return hexBuilder.toString();
    }

    public static String hex(double number) {
        // avoids creating temporary long values, which can be slow on GWT
        int h = BitConversion.doubleToLowIntBits(number);
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(15 - i, Encoding.BASE16.toEncoded[(h >> (i << 2) & 15)]);
        }
        h = BitConversion.doubleToHighIntBits(number);
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(7 - i, Encoding.BASE16.toEncoded[(h >> (i << 2) & 15)]);
        }
        return hexBuilder.toString();
    }

    public static String hex(int number) {
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(7 - i, Encoding.BASE16.toEncoded[(number >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 8);
    }

    public static String hex(float number) {
        final int h = BitConversion.floatToRawIntBits(number);
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(7 - i, Encoding.BASE16.toEncoded[(h >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 8);
    }

    public static String hex(short number) {
        for (int i = 0; i < 4; i++) {
            hexBuilder.setCharAt(3 - i, Encoding.BASE16.toEncoded[(number >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 4);
    }

    public static String hex(char number) {
        for (int i = 0; i < 4; i++) {
            hexBuilder.setCharAt(3 - i, Encoding.BASE16.toEncoded[(number >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 4);
    }

    public static String hex(byte number) {
        hexBuilder.setCharAt(0, Encoding.BASE16.toEncoded[(number >> 4 & 15)]);
        hexBuilder.setCharAt(1, Encoding.BASE16.toEncoded[(number & 15)]);
        return hexBuilder.substring(0, 2);
    }

    public static StringBuilder appendHex(StringBuilder builder, long number){
        for (int i = 60; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(int)(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, double number){
        // avoids creating temporary long values, which can be slow on GWT
        int h = BitConversion.doubleToHighIntBits(number);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(h >> i & 15)]);
        }
        h = BitConversion.doubleToLowIntBits(number);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(h >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, int number){
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, float number){
        final int h = BitConversion.floatToRawIntBits(number);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(h >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, short number){
        for (int i = 12; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, char number){
        for (int i = 12; i >= 0; i -= 4) {
            builder.append(Encoding.BASE16.toEncoded[(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, byte number){
        builder.append(Encoding.BASE16.toEncoded[(number >> 4 & 15)]);
        builder.append(Encoding.BASE16.toEncoded[(number & 15)]);
        return builder;
    }

    public static String hex(long[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 4);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }

    public static String hex(double[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 4);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }

    public static String hex(int[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 3);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }


    public static String hex(float[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 3);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }

    public static String hex(short[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 2);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }

    public static String hex(char[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 2);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }

    public static String hex(byte[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 1);
        for (int i = 0; i < len; i++) {
            appendHex(sb, numbers[i]);
        }
        return sb.toString();
    }

    public static String bin(long number) {
        String h = Long.toBinaryString(number);
        return mask64.substring(0, 64 - h.length()) + h;
    }

    public static String bin(int number) {
        String h = Integer.toBinaryString(number);
        return mask32.substring(0, 32 - h.length()) + h;
    }

    public static String bin(short number) {
        String h = Integer.toBinaryString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(char number) {
        String h = Integer.toBinaryString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(byte number) {
        String h = Integer.toBinaryString(number & 0xff);
        return mask8.substring(0, 8 - h.length()) + h;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format given a %x in the formatting
     * string, or this class' {@link #hex(long)} method; that is, if the first char of a 16-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you
     * could also simply use "-1 ".
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before the end of cs is reached. If the parse is
     * stopped early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger
     * places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs) {
        return longFromHex(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format given a %x in the formatting
     * string, or this class' {@link #hex(long)} method; that is, if the first char of a 16-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you
     * could also simply use "-1 ". If you use both '-' at the start and have the most significant digit as 8 or higher,
     * such as with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs, final int start, int end) {
        int len, h, lim = 16;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            len = -1;
            h = 0;
            lim = 17;
        } else if (c == '+') {
            len = 1;
            h = 0;
            lim = 17;
        } else if (c > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
            return 0;
        else {
            len = 1;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((c = cs.charAt(i)) > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }

    /**
     * Reads in a char[] containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start and
     * returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(long)} method; that is, if the first digit of a 16-char (or longer) char[] is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFFFFFFFFFF" would return the long -1L when passed to this, though you could also simply use "-1 ".
     * If you use both '-' at the start and have the most significant digit as 8 or higher, such as with
     * "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first (producing -1)
     * and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a char array containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 8 or 9 characters if end is too large, depending on sign)
     * @return the long that cs represents
     */
    public static long longFromHex(final char[] cs, final int start, int end)
    {
        int len, h, lim = 16;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if(c == '-')
        {
            len = -1;
            h = 0;
            lim = 17;
        }
        else if(c == '+')
        {
            len = 1;
            h = 0;
            lim = 17;
        }
        else if(c > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs[i]) > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(int)} method; that is, if the first digit of an 8-char (or longer) CharSequence is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFF" would return the int -1 when passed to this, though you could also simply use "-1 ". If you use
     * both '-' at the start and have the most significant digit as 8 or higher, such as with "-FFFFFFFF", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before the end of cs is reached. If the parse is
     * stopped early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger
     * places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @return the int that cs represents
     */
    public static int intFromHex(final CharSequence cs) {
        return intFromHex(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(int)} method; that is, if the first digit of an 8-char (or longer) CharSequence is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFF" would return the int -1 when passed to this, though you could also simply use "-1 ". If you use
     * both '-' at the start and have the most significant digit as 8 or higher, such as with "-FFFFFFFF", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 8 or 9 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    public static int intFromHex(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 8;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c == '-')
        {
            len = -1;
            h = 0;
            lim = 9;
        }
        else if(c == '+')
        {
            len = 1;
            h = 0;
            lim = 9;
        }
        else if(c > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }
    /**
     * Reads in a char[] containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(int)} method; that is, if the first digit of an 8-char (or longer) char[] is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFF" would return the int -1 when passed to this, though you could also simply use "-1 ". If you use
     * both '-' at the start and have the most significant digit as 8 or higher, such as with "-FFFFFFFF", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a char array containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 8 or 9 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    public static int intFromHex(final char[] cs, final int start, int end)
    {
        int len, h, lim = 8;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if(c == '-')
        {
            len = -1;
            h = 0;
            lim = 9;
        }
        else if(c == '+')
        {
            len = 1;
            h = 0;
            lim = 9;
        }
        else if(c > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs[i]) > 102 || (h = Encoding.BASE16.fromEncoded[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * long they represent, reading at most 19 characters (20 if there is a sign) and returning the result if valid, or
     * 0 if nothing could be read. The leading sign can be '+' or '-' if present. Unlike
     * {@link #intFromDec(CharSequence)}, this can't effectively be used to read unsigned longs as decimal literals,
     * since anything larger than the highest signed long would be larger than the normal limit for longs as text (it
     * would be 20 characters without a sign, where we limit it to 19 without a sign to match normal behavior).
     * <br>
     * Should be fairly close to the JDK's Long.parseLong method, but this also supports CharSequence data instead of
     * just String data, and ignores chars after the number. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before the end of cs is reached. If the parse is stopped early, this behaves as you would expect for
     * a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @return the long that cs represents
     */
    public static long longFromDec(final CharSequence cs) {
        return longFromDec(cs,0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * long they represent between the given positions {@code start} and {@code end}, reading at most 19 characters (20
     * if there is a sign) or until end is reached and returning the result if valid, or 0 if nothing could be read. The
     * leading sign can be '+' or '-' if present. Unlike {@link #intFromDec(CharSequence, int, int)}, this can't
     * effectively be used to read unsigned longs as decimal literals, since anything larger than the highest signed
     * long would be larger than the normal limit for longs as text (it would be 20 characters without a sign, where we
     * limit it to 19 without a sign to match normal behavior).
     * <br>
     * Should be fairly close to the JDK's Long.parseLong method, but this also supports CharSequence data instead of
     * just String data, and allows specifying a start and end. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before end is reached. If the parse is stopped early, this behaves as you would expect for a number
     * with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 19 or 20 characters if end is too large, depending on sign)
     * @return the long that cs represents
     */
    public static long longFromDec(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 19;
        long sign = 1L;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0L;
        char c = cs.charAt(start);
        if(c == '-')
        {
            sign = -1L;
            lim = 20;
            h = 0;
        }
        else if(c == '+')
        {
            lim = 20;
            h = 0;
        }
        else if(c > 102 || (h = Encoding.BASE10.fromEncoded[c]) < 0)
            return 0L;
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = Encoding.BASE10.fromEncoded[c]) < 0)
                return data * sign;
            data = data * 10 + h;
        }
        return data * sign;
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * int they represent, reading at most 10 characters (11 if there is a sign) and returning the result if valid, or 0
     * if nothing could be read. The leading sign can be '+' or '-' if present. This can technically be used to handle
     * unsigned integers in decimal format, but it isn't the intended purpose. If you do use it for handling unsigned
     * ints, 2147483647 is normally the highest positive int and -2147483648 the lowest negative one, but if you give
     * this a number between 2147483647 and {@code 2147483647 + 2147483648}, it will interpret it as a negative number
     * that fits in bounds using the normal rules for converting between signed and unsigned numbers.
     * <br>
     * Should be fairly close to the JDK's Integer.parseInt method, but this also supports CharSequence data instead of
     * just String data, and ignores chars after the number. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before the end of cs is reached. If the parse is stopped early, this behaves as you would expect for
     * a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs) {
        return intFromDec(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * int they represent, reading at most 10 characters (11 if there is a sign) and returning the result if valid, or 0
     * if nothing could be read. The leading sign can be '+' or '-' if present. This can technically be used to handle
     * unsigned integers in decimal format, but it isn't the intended purpose. If you do use it for handling unsigned
     * ints, 2147483647 is normally the highest positive int and -2147483648 the lowest negative one, but if you give
     * this a number between 2147483647 and {@code 2147483647 + 2147483648}, it will interpret it as a negative number
     * that fits in bounds using the normal rules for converting between signed and unsigned numbers.
     * <br>
     * Should be fairly close to the JDK's Integer.parseInt method, but this also supports CharSequence data instead of
     * just String data, and allows specifying a start and end. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before end is reached. If the parse is stopped early, this behaves as you would expect for a number
     * with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 10 or 11 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 10;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c == '-')
        {
            len = -1;
            lim = 11;
            h = 0;
        }
        else if(c == '+')
        {
            len = 1;
            lim = 11;
            h = 0;
        }
        else if(c > 102 || (h = Encoding.BASE10.fromEncoded[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = Encoding.BASE10.fromEncoded[c]) < 0)
                return data * len;
            data = data * 10 + h;
        }
        return data * len;
    }
    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the long they represent,
     * reading at most 64 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 64 chars long.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @return the long that cs represents
     */
    public static long longFromBin(CharSequence cs)
    {
        return longFromBin(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the long they represent,
     * reading at most 64 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 64 chars long.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 64 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromBin(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c < '0' || c > '1')
            return 0;
        long data = Encoding.BASE16.fromEncoded[c];
        for (int i = start+1; i < end && i < start+64; i++) {
            if((c = cs.charAt(i)) < '0' || c > '1')
                return 0;
            data <<= 1;
            data |= c - '0';
        }
        return data;
    }
    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the int they represent,
     * reading at most 32 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 32 chars long.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is a bizarre omission from earlier
     * JDKs. This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @return the int that cs represents
     */
    public static int intFromBin(CharSequence cs)
    {
        return intFromBin(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the int they represent,
     * reading at most 32 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 32 chars long.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is a bizarre omission from earlier
     * JDKs. This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 32 characters if end is too large)
     * @return the int that cs represents
     */
    public static int intFromBin(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c < '0' || c > '1')
            return 0;
        int data = Encoding.BASE16.fromEncoded[c];
        for (int i = start+1; i < end && i < start+32; i++) {
            if((c = cs.charAt(i)) < '0' || c > '1')
                return 0;
            data <<= 1;
            data |= c - '0';
        }
        return data;
    }

    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty boolean array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(boolean... array) {
        return hex(Hasher.astaroth.hash64(array));
    }

    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty byte array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(byte... array) {
        return hex(Hasher.astaroth.hash64(array));
    }
    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty short array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(short... array) {
        return hex(Hasher.astaroth.hash64(array));
    }
    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty char array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(char... array) {
        return hex(Hasher.astaroth.hash64(array));
    }
    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty int array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(int... array) {
        return hex(Hasher.astaroth.hash64(array));
    }

    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty long array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(long... array) {
        return hex(Hasher.astaroth.hash64(array));
    }

    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty float array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(float... array) {
        return hex(Hasher.astaroth.hash64(array));
    }

    /**
     * Hashes {@code array} using {@link Hasher} and converts that hash to a hexadecimal String.
     * @param array a non-null, non-empty double array
     * @return the 16-hex-digit representation of array's hash as a hexadecimal number
     */
    public static String hexHash(double... array) {
        return hex(Hasher.astaroth.hash64(array));
    }

    /**
     * Given a String containing decimal numbers separated by {@code delimiter} (which is permitted to be present at the
     * end or absent), this parses the numbers into a long array. The {@code source} is commonly generated by
     * {@link StringTools#join(CharSequence, long...)}.
     * @param source a String containing decimal numbers separated by {@code delimiter}
     * @param delimiter a non-null, non-empty String that separates numbers in the source
     * @return a long array containing the numbers found in {@code source}
     */
    public static long[] splitLongFromDec(String source, String delimiter) {
        if(source == null || source.length() == 0) return new long[0];
        if(delimiter == null || delimiter.length() == 0) delimiter = " ";
        int amount = StringTools.count(source, delimiter);
        if (amount <= 0) return new long[]{longFromDec(source)};
        long[] splat = new long[amount+1];
        int dl = delimiter.length(), idx = -dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = longFromDec(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = longFromDec(source, idx+dl, source.length());
        }
        else
        {
            splat[amount] = longFromDec(source, idx+dl, idx2);
        }
        return splat;
    }
    /**
     * Given a String containing decimal numbers separated by {@code delimiter} (which is permitted to be present at the
     * end or absent), this parses the numbers into an int array. The {@code source} is commonly generated by
     * {@link StringTools#join(CharSequence, int...)}.
     * @param source a String containing decimal numbers separated by {@code delimiter}
     * @param delimiter a non-null, non-empty String that separates numbers in the source
     * @return an int array containing the numbers found in {@code source}
     */
    public static int[] splitIntFromDec(String source, String delimiter) {
        if(source == null || source.length() == 0) return new int[0];
        if(delimiter == null || delimiter.length() == 0) delimiter = " ";
        int amount = StringTools.count(source, delimiter);
        if (amount <= 0) return new int[]{intFromDec(source)};
        int[] splat = new int[amount+1];
        int dl = delimiter.length(), idx = -dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = intFromDec(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = intFromDec(source, idx+dl, source.length());
        }
        else
        {
            splat[amount] = intFromDec(source, idx+dl, idx2);
        }
        return splat;
    }

    /**
     * Joins an array or varargs of floats into a String, separated by {@code delimiter}. This does not write the floats
     * conventionally; instead it converts them to their int bits and reverses the bytes of that int before appending it
     * to the String. This means the float {@code 0.0f} is written as {@code 0}, most small powers of 2 are 3 or 4
     * digits, and the longest numbers have the very least-significant bits set. This is meant to be decoded by
     * {@link #splitFloatFromBits(String, String)}.
     * @param delimiter what separates the encoded float elements
     * @param elements an array or varargs of float to encode
     * @return a String that holds the encoded floats
     */
    public static String joinFloatsBits(CharSequence delimiter, float... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(BitConversion.floatToReversedIntBits(elements[0]));
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(BitConversion.floatToReversedIntBits(elements[i]));
        }
        return sb.toString();
    }
    public static StringBuilder appendJoinedFloatsBits(StringBuilder sb, CharSequence delimiter, float... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        sb.append(BitConversion.floatToReversedIntBits(elements[0]));
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(BitConversion.floatToReversedIntBits(elements[i]));
        }
        return sb;
    }
    /**
     * Given a String containing decimal (seeming nonsense) numbers separated by {@code delimiter} (which is permitted
     * to be present at the end or absent), this parses the numbers into a float array. The {@code source} is commonly
     * generated by {@link #joinFloatsBits(CharSequence, float...)}.
     * @param source a String containing decimal numbers separated by {@code delimiter}
     * @param delimiter a non-null, non-empty String that separates numbers in the source
     * @return a float array containing the numbers found in {@code source}
     */
    public static float[] splitFloatFromBits(String source, String delimiter) {
        if(source == null || source.length() == 0) return new float[0];
        if(delimiter == null || delimiter.length() == 0) delimiter = " ";
        int amount = StringTools.count(source, delimiter);
        if (amount <= 0) return new float[]{BitConversion.reversedIntBitsToFloat(intFromDec(source))};
        float[] splat = new float[amount+1];
        int dl = delimiter.length(), idx = -dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = BitConversion.reversedIntBitsToFloat(intFromDec(source, idx+dl, idx = source.indexOf(delimiter, idx+dl)));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = BitConversion.reversedIntBitsToFloat(intFromDec(source, idx+dl, source.length()));
        }
        else
        {
            splat[amount] = BitConversion.reversedIntBitsToFloat(intFromDec(source, idx+dl, idx2));
        }
        return splat;
    }

}
