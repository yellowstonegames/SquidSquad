package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.BitConversion;
import com.github.tommyettinger.ds.support.EnhancedRandom;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Provides ways to encode digits in different base systems, or radixes, and decode numbers written in those bases. This
 * includes base systems such as binary ({@link #BASE2}, using just 0 and 1), octal ({@link #BASE8}, 0 through 7),
 * decimal ({@link #BASE10}, 0 through 9), hexadecimal ({@link #BASE16}, 0-9 then A-F), and the even larger
 * hexatrigesimal ({@link #BASE36}, 0 through 9 then A-Z). Of special note are the two different approaches to encoding
 * base-64 data: {@link #BASE64} is the standard format, and {@link #URI_SAFE} is the different format used when
 * encoding data for a URI (typically meant for the Internet). Each of these base systems provides a way to write bytes,
 * shorts, ints, and longs as variable-character-count signed numbers or as fixed-character-count unsigned numbers,
 * using {@link #signed(long)} and {@link #unsigned(long)} respectively. There is only one reading method for each size
 * of number, but it is capable of reading both the signed and unsigned results, and never throws an Exception (it just
 * returns 0 if no number could be read).
 */
public class Base {
    /**
     * Binary, using the digits 0 and 1.
     */
    public static final Base BASE2  = new Base("01");
    /**
     * Octal, using the digits 0-7.
     */
    public static final Base BASE8  = new Base("01234567");
    /**
     * Decimal, using the digits 0-9.
     */
    public static final Base BASE10 = new Base("0123456789");
    /**
     * Hexadecimal, using the digits 0-9 and then A-F (case-insensitive).
     */
    public static final Base BASE16 = new Base("0123456789ABCDEF");
    /**
     * Hexatrigesimal, using the digits 0-9 and then A-Z (case-insensitive).
     */
    public static final Base BASE36 = new Base("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    /**
     * One of two base-64 schemes available here, this is the more-standard one, using the digits A-Z, then a-z, then
     * 0-9, then + and / (case-sensitive). This uses * in place of + to indicate a positive sign, and ~ in place of - .
     */
    public static final Base BASE64 = new Base("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", false, '=', '*', '~');
    /**
     * One of two base-64 schemes available here, this is meant for URI-encoding, using the digits A-Z, then a-z, then
     * 0-9, then + and - (case-sensitive). This uses * in place of + to indicate a positive sign, and ~ in place of - .
     */
    public static final Base URI_SAFE = new Base("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-", false, '$', '*', '~');

    private static final List<Base> BASES = Arrays.asList(BASE2, BASE8, BASE10, BASE16, BASE36, BASE64, URI_SAFE);

    /**
     * Returns an immutable List of the Base instances this knows about from the start. Mostly useful for testing.
     * @return an immutable List of all Base instances this knows about from the start
     */
    public static List<Base> values() {
        return BASES;
    }

    /**
     * The digits this will encode to, in order from smallest to largest. These must all be in the ASCII range.
     */
    public final char[] toEncoded;
    /**
     * An array of the digit values corresponding to different ASCII codepoints, with -1 used for codepoints that do
     * not correspond to any digit in this base.
     */
    public final int[] fromEncoded;

    /**
     * When an encoding needs to indicate that a char is not considered part of a number, it uses this padding char;
     * this is mostly relevant for other code using Base-64 and URI-safe encodings, and is not used here. It
     * defaults to the space char, {@code ' '}, if not specified.
     */
    public final char paddingChar;
    public final char positiveSign;
    public final char negativeSign;
    /**
     * What base or radix this uses; if you use {@link #unsigned(int)}, then base must be an even number.
     */
    public final int base;
    private final int length1Byte, length2Byte, length4Byte, length8Byte;
    private final char[] progress;

    public Base(String digits) {
        this(digits, true, ' ', '+', '-');
    }

    public Base(String digits, boolean caseInsensitive, char padding, char positiveSign, char negativeSign) {
        paddingChar = padding;
        this.positiveSign = positiveSign;
        this.negativeSign = negativeSign;
        toEncoded = digits.toCharArray();
        base = toEncoded.length;
        fromEncoded = new int[128];

        Arrays.fill(fromEncoded, -1);

        for (int i = 0; i < base; i++) {
            char to = toEncoded[i];
            fromEncoded[to & 127] = i;
            if (caseInsensitive)
                fromEncoded[Character.toLowerCase(to) & 127] = i;
        }
        double logBase = 1.0 / Math.log(base);
        length1Byte = (int) Math.ceil(Math.log(0x1p8) * logBase);
        length2Byte = (int) Math.ceil(Math.log(0x1p16) * logBase);
        length4Byte = (int) Math.ceil(Math.log(0x1p32) * logBase);
        length8Byte = (int) Math.ceil(Math.log(0x1p64) * logBase);
        progress = new char[length8Byte + 1];
    }

    /**
     * Returns a seemingly-gibberish Base that uses a radix of 72 and a randomly-ordered set of characters to represent
     * the different digit values. This is randomized by an EnhancedRandom, so if the parameter is seeded identically
     * (and is the same implementation), then an equivalent Base will be produced. This randomly chooses 72 digits from
     * a large set, <code>ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&amp;*-_=+</code>, and
     * sets the positive and negative signs to two different chars left over. The padding char is always space, ' '.
     * @param random an EnhancedRandom used to shuffle the possible digits
     * @return a new Base with 72 random digits, as well as a random positive and negative sign
     */
    public static Base scrambledBase(@Nonnull EnhancedRandom random){
        char[] options = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*-_=+".toCharArray();
        random.shuffle(options);
        char plus = options[options.length - 2], minus = options[options.length - 1];

        Base base = new Base("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*-", false, ' ',
                plus, minus);

        System.arraycopy(options, 0, base.toEncoded, 0, 72);
        Arrays.fill(base.fromEncoded, -1);

        for (int i = 0; i < base.base; i++) {
            base.fromEncoded[base.toEncoded[i] & 127] = i;
        }

        return base;
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any long
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(long number) {
        final int len = length8Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            long quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[(int) (number - quotient * base)];
            number = quotient;
        }
        return String.valueOf(progress, 0, length8Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any long
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, long number) {
        final int len = length8Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            long quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[(int) (number - quotient * base)];
            number = quotient;
        }
        return builder.append(progress, 0, length8Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any long
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(long number) {
        int run = length8Byte;
        final long sign = number >> -1;
        // number is made negative because 0x8000000000000000L and -(0x8000000000000000L) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[(int) -(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any long
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, long number) {
        int run = length8Byte;
        final long sign = number >> -1;
        // number is made negative because 0x8000000000000000L and -(0x8000000000000000L) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[(int) -(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the long they represent, or 0 if nothing could be read. The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(long)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the long that cs represents
     */
    public long readLong(final CharSequence cs) {
        return readLong(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the long they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(long)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the long that cs represents
     */
    public long readLong(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length8Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length8Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length8Byte;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return data * len;
            data *= base;
            data += h;
        }
        return data * len;
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any int
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(int number) {
        final int len = length4Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[number - quotient * base];
            number = quotient;
        }
        return String.valueOf(progress, 0, length4Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any int
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, int number) {
        final int len = length4Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[number - quotient * base];
            number = quotient;
        }
        return builder.append(progress, 0, length4Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any int
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(int number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any int
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, int number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the int they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(int)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFF" would return the int -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier
     * JDKs. This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit,
     * or stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the int that cs represents
     */
    public int readInt(final CharSequence cs) {
        return readInt(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the int they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(int)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFF" would return the int -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the int that cs represents
     */
    public int readInt(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length4Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length4Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length4Byte;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return data * len;
            data *= base;
            data += h;
        }
        return data * len;
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any short
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(short number) {
        final int len = length2Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFFFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (short) quotient;
        }
        return String.valueOf(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any short
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, short number) {
        final int len = length2Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFFFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (short) quotient;
        }
        return builder.append(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any short
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(short number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (short) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any short
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, short number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (short) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the short they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(short)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFF" would return the short -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for shorts.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the short that cs represents
     */
    public short readShort(final CharSequence cs) {
        return readShort(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the short they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(short)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFF" would return the short -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for shorts.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the short that cs represents
     */
    public short readShort(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length2Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length2Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length2Byte;
        }
        short data = (short) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return (short) (data * len);
            data *= base;
            data += h;
        }
        return (short) (data * len);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any byte
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(byte number) {
        final int len = length1Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFF) - quotient * base];
            number = (byte) quotient;
        }
        return String.valueOf(progress, 0, length1Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any byte
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, byte number) {
        final int len = length1Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFF) - quotient * base];
            number = (byte) quotient;
        }
        return builder.append(progress, 0, length1Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any byte
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(byte number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (byte) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any byte
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, byte number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (byte) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0) break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the byte they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(byte)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FF" would return the byte -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for bytes.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the byte that cs represents
     */
    public byte readByte(final CharSequence cs) {
        return readByte(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the byte they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(byte)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FF" would return the byte -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for bytes.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the byte that cs represents
     */
    public byte readByte(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length1Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length1Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length1Byte;
        }
        byte data = (byte) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return (byte) (data * len);
            data *= base;
            data += h;
        }
        return (byte) (data * len);
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any double
     * @return a new String containing the bits of {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(double number) {
        return unsigned(BitConversion.doubleToRawLongBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any double
     * @return {@code builder}, with the bits of {@code number} appended in the radix this specifies
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, double number) {
        return appendUnsigned(builder, BitConversion.doubleToRawLongBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any double
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(double number) {
        return signed(BitConversion.doubleToRawLongBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any double
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, double number) {
        return appendSigned(builder, BitConversion.doubleToRawLongBits(number));
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the double those bits represent, or 0.0 if nothing could be read. The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(double)}, {@link #unsigned(double)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the double that cs represents
     */
    public double readDouble(final CharSequence cs) {
        return BitConversion.longBitsToDouble(readLong(cs, 0, cs.length()));
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the double those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(double)}, {@link #unsigned(double)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the double that cs represents
     */
    public double readDouble(final CharSequence cs, final int start, int end) {
        return BitConversion.longBitsToDouble(readLong(cs, start, end));
    }


    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any float
     * @return a new String containing the bits of {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(float number) {
        return unsigned(BitConversion.floatToRawIntBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any float
     * @return {@code builder}, with the bits of {@code number} appended in the radix this specifies
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, float number) {
        return appendUnsigned(builder, BitConversion.floatToRawIntBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any float
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(float number) {
        return signed(BitConversion.floatToRawIntBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any float
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, float number) {
        return appendSigned(builder, BitConversion.floatToRawIntBits(number));
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the float those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(float)}, {@link #unsigned(float)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the float that cs represents
     */
    public float readFloat(final CharSequence cs) {
        return BitConversion.intBitsToFloat(readInt(cs, 0, cs.length()));
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the float those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(float)}, {@link #unsigned(float)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the float that cs represents
     */
    public float readFloat(final CharSequence cs, final int start, int end) {
        return BitConversion.intBitsToFloat(readInt(cs, start, end));
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any char
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String unsigned(char number) {
        final int len = length2Byte - 1;
        for (int i = 0; i <= len; i++) {
            int quotient = number / base;
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (char) quotient;
        }
        return String.valueOf(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any char
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendUnsigned(@Nonnull StringBuilder builder, char number) {
        final int len = length2Byte - 1;
        for (int i = 0; i <= len; i++) {
            int quotient = number / base;
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (char) quotient;
        }
        return builder.append(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any char
     * @return a new String containing {@code number} in the radix this specifies.
     */
    @Nonnull
    public String signed(char number) {
        int run = length8Byte;
        for (; ; run--) {
            progress[run] = toEncoded[number % base];
            if ((number /= base) == 0) break;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any char
     * @return {@code builder}, with the encoded {@code number} appended
     */
    @Nonnull
    public StringBuilder appendSigned(@Nonnull StringBuilder builder, char number) {
        int run = length8Byte;
        for (; ; run--) {
            progress[run] = toEncoded[number % base];
            if ((number /= base) == 0) break;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the char they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * Note that chars are unsigned 16-bit numbers by default, so even having a sign runs counter to the normal
     * behavior; {@link #unsigned(char)} behaves as Java expects it, while {@link #signed(char)} is the anomaly.
     * This means chars are always in the 0 to 65535 range, so if you give this a String representing a negative number,
     * it treats it like a negative short and effectively casts it to char.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for chars.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @return the char that cs represents
     */
    public char readChar(final CharSequence cs) {
        return readChar(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the char they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * Note that chars are unsigned 16-bit numbers by default, so even having a sign runs counter to the normal
     * behavior; {@link #unsigned(char)} behaves as Java expects it, while {@link #signed(char)} is the anomaly.
     * This means chars are always in the 0 to 65535 range, so if you give this a String representing a negative number,
     * it treats it like a negative short and effectively casts it to char.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for chars.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (+ or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the char that cs represents
     */
    public char readChar(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length2Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length2Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length2Byte;
        }
        char data = (char) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return (char) (data * len);
            data *= base;
            data += h;
        }
        return (char) (data * len);
    }

}
