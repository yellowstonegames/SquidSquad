/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.yellowstonegames.core.annotations.Beta;
import org.junit.Assert;
import org.junit.Test;

import static com.github.yellowstonegames.core.DescriptiveColor.*;
import static com.github.yellowstonegames.core.DescriptiveColorRgb.rgba;

public class ColorChecks {
//    public static float limitToGamut(float L, float A, float B, float alpha) {
//        L = Math.min(Math.max(L, 0f), 1f);
//        A = Math.min(Math.max(A, 0f), 1f);
//        B = Math.min(Math.max(B, 0f), 1f);
//        alpha = Math.min(Math.max(alpha, 0f), 1f);
//        final float A2 = (A - 0.5f);
//        final float B2 = (B - 0.5f);
//        final float hue = TrigTools.atan2_(B2, A2);
//        final int idx = (int) (L * 255.999f) << 8 | (int)(256f * hue);
//        final float dist = getRawGamutValue(idx);
//        if(dist * dist * 0x1p-16f >= (A2 * A2 + B2 * B2))
//            return //(TimeUtils.millis() >>> 9 & 1L) == 0L ? Palette.LEAD :
//                    ColorTools.oklab(L, A, B, alpha);
//        return BitConversion.intBitsToFloat(
//                (int) (alpha * 127.999f) << 25 |
//                        (int) (TrigTools.sin_(hue) * dist + 128f) << 16 |
//                        (int) (TrigTools.cos_(hue) * dist + 128f) << 8 |
//                        (int) (L * 255f));
//    }
//    public static float limitToGamut(final float packed) {
//        final int decoded = BitConversion.floatToRawIntBits(packed);
//        final float A = ((decoded >>> 8 & 0xff) - 127.5f);
//        final float B = ((decoded >>> 16 & 0xff) - 127.5f);
//        final float hue = TrigTools.atan2_(B, A);
//        final int idx = (decoded & 0xff) << 8 | (int) (256f * hue);
//        final float dist = getRawGamutValue(idx);
//        if (dist * dist >= (A * A + B * B))
//            return packed;
//        return BitConversion.intBitsToFloat(
//                (decoded & 0xFE0000FF) |
//                        (int) (TrigTools.cos_(hue) * dist + 128f) << 8 |
//                        (int) (TrigTools.sin_(hue) * dist + 128f) << 16);
//    }


    @Test
    public void testLimitToGamut(){
        Assert.assertEquals(BitConversion.floatToRawIntBits(limitToGamut(0xFF000080)),
                BitConversion.floatToRawIntBits(limitToGamut(0xFF000080)));
        Assert.assertEquals(BitConversion.floatToRawIntBits(limitToGamut(0xFF00FF80)),
                BitConversion.floatToRawIntBits(limitToGamut(0xFF00FF80)));
        Assert.assertEquals(BitConversion.floatToRawIntBits(limitToGamut(0xFFFFFF80)),
                BitConversion.floatToRawIntBits(limitToGamut(0xFFFFFF80)));
        Assert.assertEquals(BitConversion.floatToRawIntBits(limitToGamut(0xFFFF0080)),
                BitConversion.floatToRawIntBits(limitToGamut(0xFFFF0080)));
        Assert.assertEquals(BitConversion.floatToRawIntBits(limitToGamut(0xFF002080)),
                BitConversion.floatToRawIntBits(limitToGamut(0xFF002080)));
        Assert.assertEquals(BitConversion.floatToRawIntBits(limitToGamut(0xFFC0C0A0)),
                BitConversion.floatToRawIntBits(limitToGamut(0xFFC0C0A0)));
        int params4 = limitToGamut((int)(0.6196f * 255f), (int)(0.6078f * 255f), (int)(0.3882f * 255f), 255);
        System.out.printf("params4: L %f  A %f  B %f  H %f  S %f\n", channelL(params4), channelA(params4), channelB(params4), hue(params4), saturation(params4));
        int params1 = limitToGamut((int)(0.6196f * 255f) | (int)(0.6078f * 255f) << 8 | (int)(0.3882f * 255f) << 16 | 255 << 24);
        System.out.printf("params1: L %f  A %f  B %f  H %f  S %f\n", channelL(params1), channelA(params1), channelB(params1), hue(params1), saturation(params1));
        Assert.assertEquals(BitConversion.floatToRawIntBits(params4),
                BitConversion.floatToRawIntBits(params1));
    }

//    @Test
//    public void testRandomColors() {
//        FourWheelRandom random = new FourWheelRandom(1);
//        for (int i = 0; i < 1000000; i++) {
//            float l = random.nextInclusiveFloat();
//            float a = random.nextFloat(0.2f, 0.8f);
//            float b = random.nextFloat(0.2f, 0.8f);
//            float f = DescriptiveColor.oklab(l, a, b, 1f);
//            if(ColorTools.inGamut(f) != ColorTools.inGamut(l, a, b)){
//                System.out.printf("%.4f %.4f %.4f is having problems! ColorTools.inGamut(f)=%s, ColorTools.inGamut(l, a, b)=%s\n", l, a, b,
//                        ColorTools.inGamut(f), ColorTools.inGamut(l, a, b));
//                System.out.printf("%.4f %.4f %.4f is f.\n\n", ColorTools.channelL(f), ColorTools.channelA(f), ColorTools.channelB(f));
//            }
//        }
//    }
//
//    @Test
//    public void testPalette() {
//        FloatArray hues = new FloatArray(512);
//        for(String name : FullPalette.NAMES){
//            float color = FullPalette.NAMED.get(name, FullPalette.GRAY);
//            float L = (ColorTools.channelL(color));
//            float A = ColorTools.channelA(color);
//            float B = ColorTools.channelB(color);
//            if(!inGamut(L, A, B)){
//                System.out.printf("%s is having problems! It has L=%a,A=%a,B=%a\n", name, L, A, B);
//                hues.add(oklabHue(color));
//            }
////            if(inGamut(L, A, B)){
////                System.out.printf("%s is doing fine.    It has L=%f,A=%f,B=%f\n", name, L, A, B);
////            }
//            A = (A + 0.5f) % 1f;
//            B = (B + 0.5f) % 1f;
//            if(inGamut(L, A, B)){
//                System.out.printf("%s's inverse should not be in gamut!!!!! It has L=%f,A=%f,B=%f\n", name, L, A, B);
//            }
//        }
//        hues.sort();
//        for (int i = 0; i < 10 && i < hues.size; i++) {
//            System.out.println(hues.get(i));
//        }
//        System.out.println("...");
//        for (int i = hues.size - 10; i >= 0 && i < hues.size; i++) {
//            System.out.println(hues.get(i));
//        }
//    }
    public static double reverseLight(double L) {
        return Math.pow(L, 2.0/3.0);
    }
//    public static double reverseLight(double L) {
//        L = Math.sqrt(L * 0x0.ffp0); // 255.0/256.0
//        final double shape = 1.52, turning = 0.963;
//        final double d = turning - L;
//        double r;
//        if(d < 0)
//            r = ((1.0 - turning) * (L - 1.0)) / (1.0 - (L + shape * d)) + 1.0;
//        else
//            r = (turning * L) / (1e-50 + (L + shape * d));
//        return r;
//    }

    /**
     * Returns true if the given Oklab values are valid to convert losslessly back to RGBA.
     * @param L lightness channel, as a double from 0 to 1
     * @param A green-to-red chromatic channel, as a double from 0 to 1
     * @param B blue-to-yellow chromatic channel, as a double from 0 to 1
     * @return true if the given Oklab channels can be converted back and forth to RGBA
     */
    public static boolean inGamut(double L, double A, double B)
    {
        L = reverseLight(L);

        A -= 0x1.fdfdfep-2;
        B -= 0x1.fdfdfep-2;

        double l = (L + +0.3963377774 * A + +0.2158037573 * B);
        l *= l * l;
        double m = (L + -0.1055613458 * A + -0.0638541728 * B);
        m *= m * m;
        double s = (L + -0.0894841775 * A + -1.2914855480 * B);
        s *= s * s;

//        final double r = +4.0767245293 * l - 3.3072168827 * m + 0.2307590544 * s;
//        if(r < -0x1p-8 || r > 0x101p-8) return false;
//        final double g = -1.2681437731 * l + 2.6093323231 * m - 0.3411344290 * s;
//        if(g < -0x1p-8 || g > 0x101p-8) return false;
//        final double b = -0.0041119885 * l - 0.7034763098 * m + 1.7068625689 * s;
//        return (b >= -0x1p-8 && b <= 0x101p-8);

        double dr = Math.sqrt((+4.0767245293 * l - 3.3072168827 * m + 0.2307590544 * s))*255;
        final int r = (int)dr;
        if(Double.isNaN(dr) || r < 0 || r > 255) return false;
        double dg = Math.sqrt((-1.2681437731 * l + 2.6093323231 * m - 0.3411344290 * s))*255;
        final int g = (int)dg;
        if(Double.isNaN(dg) || g < 0 || g > 255) return false;
        double db = Math.sqrt((-0.0041119885 * l - 0.7034763098 * m + 1.7068625689 * s))*255;
        final int b = (int)db;
        return (!Double.isNaN(db) && b >= 0 && b <= 255);
    }

    @Test
    public void testBlues() {
        for (float f = 0.70934484f; f < 0.74934484f; f += 0.001f) {
            System.out.printf("hue %1.8f : limit %1.8f\n", f, chromaLimit(f, 0.2627451f));
        }
    }

    /**
     * min hue difference: 0, max hue difference: 0
     * min sat difference: 0, max sat difference: 0
     * min lit difference: 0, max lit difference: 0
     */
    @Test
    public void testCompareHsl() {
        int maxDiffH = 0, maxDiffS = 0, maxDiffL = 0;
        int minDiffH = 1000, minDiffS = 1000, minDiffL = 1000;
        for (int r = 0; r < 256; r++) {
            float R = r / 255f;
            for (int g = 0; g < 256; g++) {
                float G = g / 255f;
                for (int b = 0; b < 256; b++) {
                    float B = b / 255f;
                    int original  = DescriptiveColorRgb.rgb2hsl(R, G, B, 1f);
                    int alt    = rgb2hslAlt(R, G, B, 1f);
                    int hueO = original >>> 24, satO = original >>> 16 & 255, litO = original >>> 8 & 255;
                    int hueA = alt >>> 24, satA = alt >>> 16 & 255, litA = alt >>> 8 & 255;
                    maxDiffH = Math.max(maxDiffH, hueO - hueA);
                    maxDiffS = Math.max(maxDiffS, satO - satA);
                    maxDiffL = Math.max(maxDiffL, litO - litA);
                    minDiffH = Math.min(minDiffH, hueO - hueA);
                    minDiffS = Math.min(minDiffS, satO - satA);
                    minDiffL = Math.min(minDiffL, litO - litA);

                    if(satO != satA) {
                        System.out.printf("SATURATION DIFFERENCE: O=%d, A=%d, color is 0x%02X%02X%02XFF\n", satO, satA, r, g, b);
                    }
                }
            }
        }
        System.out.printf("min hue difference: %d, max hue difference: %d\n", minDiffH, maxDiffH);
        System.out.printf("min sat difference: %d, max sat difference: %d\n", minDiffS, maxDiffS);
        System.out.printf("min lit difference: %d, max lit difference: %d\n", minDiffL, maxDiffL);

        for(ObjectIntMap.Entry<String> ent : DescriptiveColorRgb.NAMED.entrySet()){
            String name = ent.key;
            int rgba = ent.value;
            float R = (rgba >>> 24) / 255f;
            float G = (rgba >>> 16 & 255) / 255f;
            float B = (rgba >>> 8  & 255) / 255f;
            int original  = DescriptiveColorRgb.rgb2hsl(R, G, B, 1f);
            int alt    = rgb2hslAlt(R, G, B, 1f);
            int hueO = original >>> 24, satO = original >>> 16 & 255, litO = original >>> 8 & 255;
            System.out.println(name);
            System.out.printf("O: H %d, S %d L %d\n", hueO, satO, litO);
            int hueA = alt >>> 24, satA = alt >>> 16 & 255, litA = alt >>> 8 & 255;
            System.out.printf("A: H %d, S %d L %d\n", hueA, satA, litA);
            System.out.printf("Diff: %d %s\n",satO - satA, StringTools.padRightStrict("", '!', satO - satA));

        }
    }
    @Test
    public void testCompareHcl() {
        int maxDiffH = 0, maxDiffC = 0, maxDiffL = 0;
        int minDiffH = 1000, minDiffC = 1000, minDiffL = 1000;
        for (int r = 0; r < 256; r++) {
            float R = r / 255f;
            for (int g = 0; g < 256; g++) {
                float G = g / 255f;
                for (int b = 0; b < 256; b++) {
                    float B = b / 255f;
                    int original  = DescriptiveColorRgb.rgb2hcl(R, G, B, 1f);
                    int alt    = rgb2hclAlt(R, G, B, 1f);
                    int hueO = original >>> 24, chrO = original >>> 16 & 255, litO = original >>> 8 & 255;
                    int hueA = alt >>> 24, chrA = alt >>> 16 & 255, litA = alt >>> 8 & 255;
                    maxDiffH = Math.max(maxDiffH, hueO - hueA);
                    maxDiffC = Math.max(maxDiffC, chrO - chrA);
                    maxDiffL = Math.max(maxDiffL, litO - litA);
                    minDiffH = Math.min(minDiffH, hueO - hueA);
                    minDiffC = Math.min(minDiffC, chrO - chrA);
                    minDiffL = Math.min(minDiffL, litO - litA);

                    if(chrO != chrA) {
                        System.out.printf("CHROMA DIFFERENCE: O=%d, A=%d, color is 0x%02X%02X%02XFF\n", chrO, chrA, r, g, b);
                    }
                }
            }
        }
        System.out.printf("min hue difference: %d, max hue difference: %d\n", minDiffH, maxDiffH);
        System.out.printf("min chr difference: %d, max chr difference: %d\n", minDiffC, maxDiffC);
        System.out.printf("min lit difference: %d, max lit difference: %d\n", minDiffL, maxDiffL);

        for(ObjectIntMap.Entry<String> ent : DescriptiveColorRgb.NAMED.entrySet()){
            String name = ent.key;
            int rgba = ent.value;
            float R = (rgba >>> 24) / 255f;
            float G = (rgba >>> 16 & 255) / 255f;
            float B = (rgba >>> 8  & 255) / 255f;
            int original  = DescriptiveColorRgb.rgb2hcl(R, G, B, 1f);
            int alt    = rgb2hclAlt(R, G, B, 1f);
            int hueO = original >>> 24, chrO = original >>> 16 & 255, litO = original >>> 8 & 255;
            System.out.println(name);
            System.out.printf("O: H %d, C %d L %d\n", hueO, chrO, litO);
            int hueA = alt >>> 24, chrA = alt >>> 16 & 255, litA = alt >>> 8 & 255;
            System.out.printf("A: H %d, C %d L %d\n", hueA, chrA, litA);
            System.out.printf("Diff: %d %s\n",chrO - chrA, StringTools.padRightStrict("", '!', chrO - chrA));

            System.out.printf("0x%08X vs. 0x%08X\n",
                    DescriptiveColorRgb.hcl2rgb(hueO / 255f, chrO / 255f, litO / 255f, 1f),
                    hcl2rgbAlt(hueO / 255f, chrO / 255f, litO / 255f, 1f)
            );
        }
    }

    @Test
    public void testRgb2Any() {
        int arg1, arg4;
        for (int r = 0; r < 256; r++) {
            float R = r / 255f;
            for (int g = 0; g < 256; g++) {
                float G = g / 255f;
                for (int b = 0; b < 256; b++) {
                    float B = b / 255f;
                    int rgba = rgba(R, G, B, 1f);
                    arg1 = DescriptiveColorRgb.rgb2hsl(rgba);
                    arg4 = DescriptiveColorRgb.rgb2hsl(R, G, B, 1f);
                    if(arg1 != arg4) System.out.printf("HSL: 1-arg 0x%08X != 4-arg 0x%08X !\n", arg1, arg4);
                    Assert.assertEquals("HSL values not equal", arg1, arg4);
                    arg1 = DescriptiveColorRgb.rgb2hcl(rgba);
                    arg4 = DescriptiveColorRgb.rgb2hcl(R, G, B, 1f);
                    if(arg1 != arg4) System.out.printf("HCL: 1-arg 0x%08X != 4-arg 0x%08X !\n", arg1, arg4);
                    Assert.assertEquals("HCL values not equal", arg1, arg4);
                    arg1 = DescriptiveColorRgb.rgb2hsb(rgba);
                    arg4 = DescriptiveColorRgb.rgb2hsb(R, G, B, 1f);
                    if(arg1 != arg4) System.out.printf("HSB: 1-arg 0x%08X != 4-arg 0x%08X with actual R=%f G=%f B=%f!\n", arg1, arg4, R, G, B);
                    Assert.assertEquals("HSB values not equal", arg1, arg4);
                }
            }
        }
    }
    /**
     * Credit for this conversion goes to <a href="https://github.com/CypherCove/gdx-tween/blob/8b83629a29173a89a510464e2cc49a0360727476/gdxtween/src/main/java/com/cyphercove/gdxtween/graphics/GtColor.java#L248-L272">cyphercove's gdx-tween library</a>.
     * @param r
     * @param g
     * @param b
     * @param a
     * @return
     */
    @Beta
    public static int rgb2hslAlt(final float r, final float g, final float b, final float a) {
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float chr = max - min;
        float hue, sat, lit;
        if (chr == 0) {
            hue = 0;
        } else if (max == r) {
            hue = MathTools.fract((g - b) / chr / 6);
        } else if (max == g) {
            hue = ((b - r) / chr + 2) / 6;
        } else {
            hue = ((r - g) / chr + 4) / 6;
        }
        float doubleLightness = min + max;
        if (doubleLightness <= 0f || doubleLightness >= 2f) {
            sat = 0f;
        } else {
            sat = chr / (1f - Math.abs(doubleLightness - 1f));
        }
        lit = 0.5f * doubleLightness;
        return rgba(hue, sat, lit, a);
    }

    /**
     * Credit for this conversion goes to <a href="https://github.com/CypherCove/gdx-tween/blob/8b83629a29173a89a510464e2cc49a0360727476/gdxtween/src/main/java/com/cyphercove/gdxtween/graphics/GtColor.java#L248-L272">cyphercove's gdx-tween library</a>.
     * @param r
     * @param g
     * @param b
     * @param a
     * @return
     */
    @Beta
    public static int rgb2hclAlt(final float r, final float g, final float b, final float a) {
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float chr = max - min;
        float hue, lit;
        if (chr == 0) {
            hue = 0;
        } else if (max == r) {
            hue = MathTools.fract((g - b) / chr / 6);
        } else if (max == g) {
            hue = ((b - r) / chr + 2) / 6;
        } else {
            hue = ((r - g) / chr + 4) / 6;
        }
        lit = 0.5f * (min + max);
        return rgba(hue, chr, lit, a);
    }

    /**
     * Credit for this conversion goes to <a href="https://github.com/CypherCove/gdx-tween/blob/8b83629a29173a89a510464e2cc49a0360727476/gdxtween/src/main/java/com/cyphercove/gdxtween/graphics/GtColor.java#L318-L366">cyphercove's gdx-tween library</a>.
     * @param hue
     * @param sat
     * @param lit
     * @param a
     * @return
     */
    @Beta
    public static int hsl2rgbAlt(float hue, float sat, float lit, float a) {
        float doubleLightness = lit + lit;
        float chroma = (1 - Math.abs(doubleLightness - 1f)) * sat;
        float v = lit + chroma * 0.5f;
        float s = lit == 0f || lit > 254f / 255f ? 0f : 2 * (1f - lit / v);
        float x = hue * 6;
        int i = Math.min(Math.max((int) x, 0), 5);
        float f = x - i;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        switch (i) {
            case 0:
                return rgba(
                        v,
                        t,
                        p,
                        a);
            case 1:
                return rgba(
                        q,
                        v,
                        p,
                        a);
            case 2:
                return rgba(
                        p,
                        v,
                        t,
                        a);
            case 3:
                return rgba(
                        p,
                        q,
                        v,
                        a);
            case 4:
                return rgba(
                        t,
                        p,
                        v,
                        a);
            default:
                return rgba(
                        v,
                        p,
                        q,
                        a);
        }
    }

    /**
     * Credit for this conversion goes to <a href="https://github.com/CypherCove/gdx-tween/blob/8b83629a29173a89a510464e2cc49a0360727476/gdxtween/src/main/java/com/cyphercove/gdxtween/graphics/GtColor.java#L318-L366">cyphercove's gdx-tween library</a>.
     * @param hue
     * @param chr
     * @param lit
     * @param a
     * @return
     */
    @Beta
    public static int hcl2rgbAlt(float hue, float chr, float lit, float a) {
        float v = lit + chr * 0.5f;
        float d = lit == 0f || lit > 254f / 255f ? 0f : 2 * (1f - lit / v);
        float x = hue * 6;
        int i = Math.min(Math.max((int) x, 0), 5);
        float f = x - i;
        float p = v * (1 - d);
        float q = v * (1 - d * f);
        float t = v * (1 - d * (1 - f));
        switch (i) {
            case 0:
                return rgba(
                        v,
                        t,
                        p,
                        a);
            case 1:
                return rgba(
                        q,
                        v,
                        p,
                        a);
            case 2:
                return rgba(
                        p,
                        v,
                        t,
                        a);
            case 3:
                return rgba(
                        p,
                        q,
                        v,
                        a);
            case 4:
                return rgba(
                        t,
                        p,
                        v,
                        a);
            default:
                return rgba(
                        v,
                        p,
                        q,
                        a);
        }
    }

}
