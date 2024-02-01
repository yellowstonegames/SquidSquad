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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TextTools;

/**
 * Utility class for converting to and from numbers and their String representations; this is mostly wrappers around
 * {@link Base} for compatibility. New code should generally prefer using one of the predefined Base constants in that
 * class, since you can control whether you want signed or unsigned output using the Base API. This class only produces
 * unsigned output from its wrapper methods.
 */
public final class DigitTools {
    /**
     * No need to instantiate this.
     */
    private DigitTools() {
    }
    public static String hex(long number) {
        return Base.BASE16.unsigned(number);
    }

    public static String hex(double number) {
        return Base.BASE16.unsigned(number);
    }

    public static String hex(int number) {
        return Base.BASE16.unsigned(number);
    }

    public static String hex(float number) {
        return Base.BASE16.unsigned(number);
    }

    public static String hex(short number) {
        return Base.BASE16.unsigned(number);
    }

    public static String hex(char number) {
        return Base.BASE16.unsigned(number);
    }

    public static String hex(byte number) {
        return Base.BASE16.unsigned(number);
    }

    public static StringBuilder appendHex(StringBuilder builder, long number){
        return Base.BASE16.appendUnsigned(builder, number);
    }
    public static StringBuilder appendHex(StringBuilder builder, double number){
        return Base.BASE16.appendUnsigned(builder, number);
    }
    public static StringBuilder appendHex(StringBuilder builder, int number){
        return Base.BASE16.appendUnsigned(builder, number);
    }
    public static StringBuilder appendHex(StringBuilder builder, float number){
        return Base.BASE16.appendUnsigned(builder, number);
    }
    public static StringBuilder appendHex(StringBuilder builder, short number){
        return Base.BASE16.appendUnsigned(builder, number);
    }
    public static StringBuilder appendHex(StringBuilder builder, char number){
        return Base.BASE16.appendUnsigned(builder, number);
    }
    public static StringBuilder appendHex(StringBuilder builder, byte number){
        return Base.BASE16.appendUnsigned(builder, number);
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
        return Base.BASE2.unsigned(number);
    }

    public static String bin(int number) {
        return Base.BASE2.unsigned(number);
    }

    public static String bin(double number) {
        return Base.BASE2.unsigned(number);
    }

    public static String bin(float number) {
        return Base.BASE2.unsigned(number);
    }

    public static String bin(short number) {
        return Base.BASE2.unsigned(number);
    }

    public static String bin(char number) {
        return Base.BASE2.unsigned(number);
    }

    public static String bin(byte number) {
        return Base.BASE2.unsigned(number);
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
     * <br>
     * This is simply a wrapper around {@link Base#readLong(CharSequence, int, int)}.
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
     * <br>
     * This is simply a wrapper around {@link Base#readLong(CharSequence, int, int)}.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs, final int start, int end) {
        return Base.BASE16.readLong(cs, start, end);
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
        return Base.BASE16.readLong(cs, start, end);
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
        return Base.BASE16.readInt(cs, start, end);
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
        return Base.BASE16.readInt(cs, start, end);
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
        return Base.BASE10.readLong(cs, start, end);
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
        return Base.BASE10.readInt(cs, start, end);
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
        return Base.BASE2.readLong(cs, start, end);
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
        return Base.BASE2.readInt(cs, start, end);
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
     * {@link Base#join(String, long[])}.
     * @param source a String containing decimal numbers separated by {@code delimiter}
     * @param delimiter a non-null, non-empty String that separates numbers in the source
     * @return a long array containing the numbers found in {@code source}
     */
    public static long[] splitLongFromDec(String source, String delimiter) {
        return Base.BASE10.longSplit(source, delimiter);
    }
    /**
     * Given a String containing decimal numbers separated by {@code delimiter} (which is permitted to be present at the
     * end or absent), this parses the numbers into an int array. The {@code source} is commonly generated by
     * {@link Base#join(String, int[])}.
     * @param source a String containing decimal numbers separated by {@code delimiter}
     * @param delimiter a non-null, non-empty String that separates numbers in the source
     * @return an int array containing the numbers found in {@code source}
     */
    public static int[] splitIntFromDec(String source, String delimiter) {
        return Base.BASE10.intSplit(source, delimiter);
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
        int amount = TextTools.count(source, delimiter);
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
