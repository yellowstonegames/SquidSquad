package com.github.yellowstonegames.core;

import org.junit.Assert;
import org.junit.Test;

public class DigitTest {

    @Test
    public void testHexInt() {
        int[] inputs = {0x00000000, 0x00000001, 0xFFFFFFFF, 0x7FFFFFFF, 0x80000000, 0x12345678, 0x89ABCDEF};
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendUnsigned(esb, i).toString());
        }
    }

    @Test
    public void testHexLong() {
        long[] inputs = {0x00000000L, 0x00000001L, 0xFFFFFFFFL, 0x7FFFFFFFL,  0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
                0x80000000L,  0x8000000000000000L, 0x12345678L, 0x89ABCDEFL, 0x1234567890ABCDEFL, 0xFEDCBA0987654321L};
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.unsigned(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (long i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendUnsigned(esb, i).toString());
        }
    }
}
