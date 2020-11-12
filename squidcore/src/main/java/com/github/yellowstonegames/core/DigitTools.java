package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.BitConversion;

public class DigitTools {
    public static final String mask64 = "0000000000000000000000000000000000000000000000000000000000000000",
            mask32 = "00000000000000000000000000000000",
            mask16 = "0000000000000000",
            mask8 = "00000000";
    /**
     * Constant storing the 16 hexadecimal digits, as char values, in order.
     */
    public static final char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final StringBuilder hexBuilder = new StringBuilder(16).append(mask16);
    public static String hex(long number) {
        for (int i = 0; i < 16; i++) {
            hexBuilder.setCharAt(15 - i, hexDigits[(int)(number >> (i << 2) & 15)]);
        }
        return hexBuilder.toString();
    }

    public static String hex(double number) {
        // avoids creating temporary long values, which can be slow on GWT
        int h = BitConversion.doubleToLowIntBits(number);
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(15 - i, hexDigits[(h >> (i << 2) & 15)]);
        }
        h = BitConversion.doubleToHighIntBits(number);
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(7 - i, hexDigits[(h >> (i << 2) & 15)]);
        }
        return hexBuilder.toString();
    }

    public static String hex(int number) {
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(7 - i, hexDigits[(number >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 8);
    }

    public static String hex(float number) {
        final int h = BitConversion.floatToIntBits(number);
        for (int i = 0; i < 8; i++) {
            hexBuilder.setCharAt(7 - i, hexDigits[(h >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 8);
    }

    public static String hex(short number) {
        for (int i = 0; i < 4; i++) {
            hexBuilder.setCharAt(3 - i, hexDigits[(number >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 4);
    }

    public static String hex(char number) {
        for (int i = 0; i < 4; i++) {
            hexBuilder.setCharAt(3 - i, hexDigits[(number >> (i << 2) & 15)]);
        }
        return hexBuilder.substring(0, 4);
    }

    public static String hex(byte number) {
        hexBuilder.setCharAt(0, hexDigits[(number >> 4 & 15)]);
        hexBuilder.setCharAt(1, hexDigits[(number & 15)]);
        return hexBuilder.substring(0, 2);
    }

    public static StringBuilder appendHex(StringBuilder builder, long number){
        for (int i = 60; i >= 0; i -= 4) {
            builder.append(hexDigits[(int)(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, double number){
        // avoids creating temporary long values, which can be slow on GWT
        int h = BitConversion.doubleToHighIntBits(number);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(hexDigits[(h >> i & 15)]);
        }
        h = BitConversion.doubleToLowIntBits(number);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(hexDigits[(h >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, int number){
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(hexDigits[(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, float number){
        final int h = BitConversion.floatToIntBits(number);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(hexDigits[(h >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, short number){
        for (int i = 12; i >= 0; i -= 4) {
            builder.append(hexDigits[(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, char number){
        for (int i = 12; i >= 0; i -= 4) {
            builder.append(hexDigits[(number >> i & 15)]);
        }
        return builder;
    }
    public static StringBuilder appendHex(StringBuilder builder, byte number){
        builder.append(hexDigits[(number >> 4 & 15)]);
        builder.append(hexDigits[(number & 15)]);
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
    private static final int[] hexCodes = new int[]
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9,-1,-1,-1,-1,-1,-1,
                    -1,10,11,12,13,14,15,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,10,11,12,13,14,15};

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
        } else if (c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else {
            len = 1;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0)
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
        else if(c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs[i]) > 102 || (h = hexCodes[c]) < 0)
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
        else if(c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0)
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
        else if(c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs[i]) > 102 || (h = hexCodes[c]) < 0)
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
        else if(c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0L;
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0 || h > 9)
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
        else if(c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0 || h > 9)
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
        long data = hexCodes[c];
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
        int data = hexCodes[c];
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

}
