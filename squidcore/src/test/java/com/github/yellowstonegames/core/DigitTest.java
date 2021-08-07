package com.github.yellowstonegames.core;

import org.junit.Assert;
import org.junit.Test;

public class DigitTest {

    @Test
    public void testHexInt() {
        int[] inputs = {0x00000000, 0x00000001, 0xFFFFFFFF, 0x7FFFFFFF, 0x80000000, 0x12345678, 0x89ABCDEF};
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.hex(i), DigitTools.Encoding.BASE16.encode(i));
        }
        StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
        for (int i : inputs) {
            Assert.assertEquals(DigitTools.appendHex(sb, i).toString(), DigitTools.Encoding.BASE16.appendEncoded(esb, i).toString());
        }

    }
}
