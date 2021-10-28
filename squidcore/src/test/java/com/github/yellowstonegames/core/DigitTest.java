package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.tommyettinger.ds.support.FourWheelRandom;
import org.junit.Assert;
import org.junit.Test;

public class DigitTest {

    public static final ObjectList<Base> BASES = ObjectList.with(Base.BASE2, Base.BASE8, Base.BASE10, Base.BASE16,
            Base.BASE36, Base.BASE64, Base.URI_SAFE, Base.scrambledBase(new DistinctRandom(123L)), Base.scrambledBase(new FourWheelRandom(1234L)));

    @Test
    public void testUnsignedInt() {
        int[] inputs = {0x00000000, 0x00000001, 0xFFFFFFFF, 0x7FFFFFFF, 0x80000000, 0x12345678, 0x89ABCDEF};
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), Base.BASE16.unsigned(i));
        }
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), Base.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
        }
        for (int i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), Base.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedLong() {
        long[] inputs = {0x00000000L, 0x00000001L, 0xFFFFFFFFL, 0x7FFFFFFFL,  0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
                0x80000000L,  0x8000000000000000L, 0x12345678L, 0x89ABCDEFL, 0x1234567890ABCDEFL, 0xFEDCBA0987654321L};
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), Base.BASE16.unsigned(i));
        }
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), Base.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
        }
        for (long i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), Base.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedShort() {
        short[] inputs = new short[]{0x0000, 0x0001, (short)0xFFFF, 0x7FFF,
                (short)0x8000, 0x1234, (short)0x89AB, (short)0xCDEF, (short)0x8765};
        for (short i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), Base.BASE16.unsigned(i));
        }
        for (short i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), Base.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (short i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
        }
        for (short i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), Base.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedChar() {
        char[] inputs = new char[]{0x0000, 0x0001, (char)0xFFFF, 0x7FFF,
                (char)0x8000, 0x1234, (char)0x89AB, (char)0xCDEF, (char)0x8765};
        for (char i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), Base.BASE16.unsigned(i));
        }
        for (char i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), Base.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (char i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
        }
        for (char i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), Base.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testUnsignedByte() {
        byte[] inputs = new byte[]{0x00, 0x01, (byte)0xFF, 0x7F,
                (byte)0x80, 0x12, (byte)0x89, (byte)0xCD, (byte)0x65};
        for (byte i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), Base.BASE16.unsigned(i));
        }
        for (byte i : inputs) {
            Assert.assertEquals(DigitTools.bin(i), Base.BASE2.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (byte i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
        }
        for (byte i : inputs) {
            Assert.assertEquals(sb.append(DigitTools.bin(i)).toString(), Base.BASE2.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testSignedLong() {
        long[] inputs = {0L, 1L, -1L, 9223372036854775807L, -9223372036854775808L, 2147483647L, -2147483648L, 1234L, -98765L};
        for (long i : inputs) {
            Assert.assertTrue(Long.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
        }
        for (long i : inputs) {
            Assert.assertTrue(Long.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (long i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
        }
        for (long i : inputs) {
            Assert.assertEquals(sb.append(Long.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
        }
        for(Base b : BASES){
            Assert.assertArrayEquals(b.longSplit(b.join(" ", inputs), " "), inputs);
            Assert.assertArrayEquals(b.longSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
        }
    }

    @Test
    public void testSignedInt() {
        int[] inputs = {0, 1, -1, 2147483647, -2147483648, 1234, -98765};
        for (int i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
        }
        for (int i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (int i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
        }
        for (int i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
        }
        for(Base b : BASES){
            Assert.assertArrayEquals(b.intSplit(b.join(" ", inputs), " "), inputs);
            Assert.assertArrayEquals(b.intSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
        }
    }

    @Test
    public void testSignedShort() {
        short[] inputs = {0, 1, -1, 32767, -32768, 1234, -9876};
        for (short i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
        }
        for (short i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (short i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
        }
        for (short i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
        }
        for(Base b : BASES){
            Assert.assertArrayEquals(b.shortSplit(b.join(" ", inputs), " "), inputs);
            Assert.assertArrayEquals(b.shortSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
        }
    }

    @Test
    public void testSignedChar() {
        char[] inputs = {0, 1, 0xFFFF, 32767, 0x8000, 1234, 49876};
        for (char i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
        }
        for (char i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (char i : inputs) {
            Assert.assertEquals(sb.append((int)i).toString(), Base.BASE10.appendSigned(esb, i).toString());
        }
        for (char i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
        }
        for(Base b : BASES){
            Assert.assertArrayEquals(b.charSplit(b.join(" ", inputs), " "), inputs);
            Assert.assertArrayEquals(b.charSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
        }
    }

    @Test
    public void testSignedByte() {
        byte[] inputs = {0, 1, -1, 127, -128, 12, -87};
        for (byte i : inputs) {
            Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
        }
        for (byte i : inputs) {
            Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
        }
        StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
        for (byte i : inputs) {
            Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
        }
        for (byte i : inputs) {
            Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
        }
        for(Base b : BASES){
            Assert.assertArrayEquals(b.byteSplit(b.join(" ", inputs), " "), inputs);
            Assert.assertArrayEquals(b.byteSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
        }
    }

    @Test
    public void testReadLong(){
        long[] inputs = {0x00000000L, 0x00000001L, 0xFFFFFFFFL, 0x7FFFFFFFL,
                0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
                0x80000000L,  0x8000000000000000L, 0x12345678L, 0x89ABCDEFL, 0x1234567890ABCDEFL, 0xFEDCBA0987654321L};

        for(Base enc : BASES)
        {
            for(long in : inputs){
                Assert.assertEquals(in, enc.readLong(enc.signed(in)));
                Assert.assertEquals(in, enc.readLong(enc.unsigned(in)));
            }
        }
    }

    @Test
    public void testReadInt(){
        int[] inputs = {0, 1, -1, 2147483647, -2147483647, -2147483648, 1234, -98765};

        for(Base enc : BASES)
        {
            for(int in : inputs){
                Assert.assertEquals(in, enc.readInt(enc.signed(in)));
                Assert.assertEquals(in, enc.readInt(enc.unsigned(in)));
            }
        }
    }

    @Test
    public void testReadShort(){
        short[] inputs = {0, 1, -1, 32767, -32768, 1234, -9876};

        for(Base enc : BASES)
        {
            for(short in : inputs){
                Assert.assertEquals(in, enc.readShort(enc.signed(in)));
                Assert.assertEquals(in, enc.readShort(enc.unsigned(in)));
            }
        }
    }

    @Test
    public void testReadChar(){
        char[] inputs = {0, 1, 0xFFFF, 32767, 0x8000, 1234, 49876};

        for(Base enc : BASES)
        {
            for(char in : inputs){
                Assert.assertEquals(in, enc.readChar(enc.signed(in)));
                Assert.assertEquals(in, enc.readChar(enc.unsigned(in)));
            }
        }
    }

    @Test
    public void testReadByte(){
        byte[] inputs = {0, 1, -1, 127, -128, 12, -87};

        for(Base enc : BASES)
        {
            for(byte in : inputs){
                Assert.assertEquals(in, enc.readByte(enc.signed(in)));
                Assert.assertEquals(in, enc.readByte(enc.unsigned(in)));
            }
        }
    }

    @Test
    public void testReadDouble(){
        double[] inputs = {0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, 1.5, -1.1};

        for(Base enc : BASES)
        {
            for(double in : inputs){
                Assert.assertEquals(in, enc.readDouble(enc.signed(in)), Double.MIN_VALUE);
                Assert.assertEquals(in, enc.readDouble(enc.unsigned(in)), Double.MIN_VALUE);
            }
            Assert.assertTrue(Double.isNaN(enc.readDouble(enc.signed(Double.NaN))));
            Assert.assertTrue(Double.isNaN(enc.readDouble(enc.unsigned(Double.NaN))));
        }
    }

    @Test
    public void testReadFloat(){
        float[] inputs = {0.0f, -0.0f, 1.0f, -1.0f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, 1.5f, -1.1f};

        for(Base enc : BASES)
        {
            for(float in : inputs){
                Assert.assertEquals(in, enc.readFloat(enc.signed(in)), Float.MIN_VALUE);
                Assert.assertEquals(in, enc.readFloat(enc.unsigned(in)), Float.MIN_VALUE);
            }
            Assert.assertTrue(Float.isNaN(enc.readFloat(enc.signed(Float.NaN))));
            Assert.assertTrue(Float.isNaN(enc.readFloat(enc.unsigned(Float.NaN))));
        }
    }
}
