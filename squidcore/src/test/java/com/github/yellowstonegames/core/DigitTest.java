package com.github.yellowstonegames.core;

import org.junit.Assert;
import org.junit.Test;

public class DigitTest {

    @Test
    public void testUnsignedInt() {
        int[] inputs = {0x00000000, 0x00000001, 0xFFFFFFFF, 0x7FFFFFFF, 0x80000000, 0x12345678, 0x89ABCDEF};
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.unsigned(i));
        }
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), DigitTools.Encoding.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendUnsigned(esb, i).toString());
        }
        for (int i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), DigitTools.Encoding.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedLong() {
        long[] inputs = {0x00000000L, 0x00000001L, 0xFFFFFFFFL, 0x7FFFFFFFL,  0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
                0x80000000L,  0x8000000000000000L, 0x12345678L, 0x89ABCDEFL, 0x1234567890ABCDEFL, 0xFEDCBA0987654321L};
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.unsigned(i));
        }
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), DigitTools.Encoding.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendUnsigned(esb, i).toString());
        }
        for (long i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), DigitTools.Encoding.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedShort() {
        short[] inputs = new short[]{0x0000, 0x0001, (short)0xFFFF, 0x7FFF,
                (short)0x8000, 0x1234, (short)0x89AB, (short)0xCDEF, (short)0x8765};
        for (short i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.unsigned(i));
        }
        for (short i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), DigitTools.Encoding.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (short i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendUnsigned(esb, i).toString());
        }
        for (short i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), DigitTools.Encoding.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedByte() {
        byte[] inputs = new byte[]{0x00, 0x01, (byte)0xFF, 0x7F,
                (byte)0x80, 0x12, (byte)0x89, (byte)0xCD, (byte)0x65};
        for (byte i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.unsigned(i));
        }
        for (byte i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), DigitTools.Encoding.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (byte i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendUnsigned(esb, i).toString());
        }
        for (byte i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), DigitTools.Encoding.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testSignedLong() {
        long[] inputs = {0L, 1L, -1L, 9223372036854775807L, -9223372036854775808L, 2147483647L, -2147483648L, 1234L, -98765L};
        for (long i : inputs) {
            Assert.assertTrue(Long.toString(i).equalsIgnoreCase(DigitTools.Encoding.BASE10.signed(i)));
        }
        for (long i : inputs) {
            Assert.assertTrue(Long.toString(i, 36).equalsIgnoreCase(DigitTools.Encoding.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (long i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), DigitTools.Encoding.BASE10.appendSigned(esb, i).toString());
        }
        for (long i : inputs) {
            Assert.assertEquals(sb.append(Long.toString(i, 2)).toString(), DigitTools.Encoding.BASE2.appendSigned(esb, i).toString());
        }
    }

    @Test
    public void testSignedInt() {
        int[] inputs = {0, 1, -1, 2147483647, -2147483648, 1234, -98765};
        for (int i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(DigitTools.Encoding.BASE10.signed(i)));
        }
        for (int i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(DigitTools.Encoding.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (int i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), DigitTools.Encoding.BASE10.appendSigned(esb, i).toString());
        }
        for (int i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), DigitTools.Encoding.BASE2.appendSigned(esb, i).toString());
        }
    }

    @Test
    public void testSignedShort() {
        short[] inputs = {0, 1, -1, 32767, -32768, 1234, -9876};
        for (short i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(DigitTools.Encoding.BASE10.signed(i)));
        }
        for (short i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(DigitTools.Encoding.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (short i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), DigitTools.Encoding.BASE10.appendSigned(esb, i).toString());
        }
        for (short i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), DigitTools.Encoding.BASE2.appendSigned(esb, i).toString());
        }
    }

    @Test
    public void testSignedByte() {
        byte[] inputs = {0, 1, -1, 127, -128, 12, -87};
        for (byte i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(DigitTools.Encoding.BASE10.signed(i)));
        }
        for (byte i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(DigitTools.Encoding.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (byte i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), DigitTools.Encoding.BASE10.appendSigned(esb, i).toString());
        }
        for (byte i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), DigitTools.Encoding.BASE2.appendSigned(esb, i).toString());
        }
    }

}
