/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.HilbertCurve;
import com.github.yellowstonegames.grid.RotationTools;

import java.util.Arrays;
import java.util.Random;

/**
 * Adapted from SquidLib's MathVisualizer, but stripped down to only include sphere-related math.
 */
public class SphereVisualizer extends ApplicationAdapter {
    private int mode = 0;
    private int modes = 4;
    private SpriteBatch batch;
    private ImmediateModeRenderer20 renderer;
    private InputAdapter input;
    private BitmapFont font;
    private ScreenViewport viewport;
    private Camera camera;
    private int[] amounts = new int[512];
    private double[] dAmounts = new double[512];
    private long seed = 1L;
    private long startTime;
    private float[] circleCoord = new float[3];
    private WhiskerRandom whisker = new WhiskerRandom(seed);
    private final float black = Color.BLACK.toFloatBits();
    private final float blue = Color.BLUE.toFloatBits();
    private final float cyan = Color.CYAN.toFloatBits();
    private final float red = Color.RED.toFloatBits();
    private final float smoke = Color.toFloatBits(0f, 0f, 0f, 0.25f);

    @Override
    public void create() {
        startTime = TimeUtils.millis();
        Coord.expandPoolTo(512, 512);
        HilbertCurve.init2D();
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("Cozette-standard.fnt"));
        font.setColor(Color.BLACK);
        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        camera = viewport.getCamera();
        renderer = new ImmediateModeRenderer20(0x80000, false, true, 0);
        Arrays.fill(amounts, 0);
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER)
                {
                    mode = (mode + 1) % modes;
                    System.out.println("Changed to mode " + mode);
                    return true;
                } else if(keycode == Input.Keys.MINUS || keycode == Input.Keys.BACKSPACE)
                {
                    mode = (mode + modes - 1) % modes;
                    System.out.println("Changed to mode " + mode);
                    return true;
                }
                if(keycode == Input.Keys.ESCAPE)
                    Gdx.app.exit();

                return false;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    private void sphereTrigMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < 0x40000; i++) {
            onSphereTrig(circleCoord);
            renderer.color(black);
            renderer.vertex(circleCoord[0] * 250 + 260, circleCoord[1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereGaussianMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < 0x40000; i++) {
            onSphereGaussian(circleCoord);
            renderer.color(black);
            renderer.vertex(circleCoord[0] * 250 + 260, circleCoord[1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private static final float[] pole = {1, 0, 0};
//    private static final float[] pole = {0, MathTools.ROOT2_INVERSE, MathTools.ROOT2_INVERSE};
//    private static final float[] pole = {1f/MathTools.ROOT3, 1f/MathTools.ROOT3, 1f/MathTools.ROOT3};

//    private static short time = 0;
    private void sphereRotationMode() {
//        time++;
//        pole[0] = TrigTools.cosDeg(time);
//        pole[1] = 0;
//        pole[2] = TrigTools.sinDeg(time);
//        pole[1] = (float) (LineWobble.wobble(123456789, time / 360.0));
//        float inverse = 1f / (float) Math.sqrt(pole[1] * pole[1] + 1f);
//        pole[0] *= inverse;
//        pole[1] *= inverse;
//        pole[2] *= inverse;
//        float[] rot = {1,0,0,0,1,0,0,0,1};
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < 0x40000; i++) {
            circleCoord[0] = circleCoord[1] = circleCoord[2] = 0f;

//            float s = TrigTools.sinDeg(i);
//            float c = TrigTools.cosDeg(i);
//            rot[0] = rot[4] = c;
//            rot[1] = s;
//            rot[3] = -s;
//            RotationTools.rotate(pole, rot, circleCoord);

            RotationTools.rotate(pole, RotationTools.randomRotation3D(++seed, RotationTools.randomRotation2D(-100000000000L - seed)), circleCoord);
            renderer.color(black);
            renderer.vertex(circleCoord[0] * 250 + 260, circleCoord[1] * 250 + 260, 0);
//            if(!MathTools.isEqual(circleCoord[0] * circleCoord[0] + circleCoord[1] * circleCoord[1] + circleCoord[2] * circleCoord[2], 1f, 0.00001f))
//                System.out.println("Problem coordinate: " + circleCoord[0] + ", " + circleCoord[1] + ", " + circleCoord[2] + " is off by " +
//                        (Math.sqrt(circleCoord[0] * circleCoord[0] + circleCoord[1] * circleCoord[1] + circleCoord[2] * circleCoord[2]) - 1));
        }
//        for (int i = 0; i < 520; i+= 4) {
//            renderer.color(red);
//            renderer.vertex(260, i, 0);
//        }
        renderer.end();
    }

    private void spherePairMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < 0x8000; i++) {
            onSphereGaussian(circleCoord);
            renderer.color(smoke);
            renderer.vertex(circleCoord[0] * 125 + 260 - 126, circleCoord[1] * 125 + 260, 0);
        }
//        time++;
//        pole[0] = TrigTools.cosDeg(time);
//        pole[1] = 0;
//        pole[2] = TrigTools.sinDeg(time);
        for (int i = 0; i < 0x8000; i++) {
            circleCoord[0] = circleCoord[1] = circleCoord[2] = 0f;
            RotationTools.rotate(pole, RotationTools.randomRotation3D(++seed), circleCoord);
            renderer.color(smoke);
            renderer.vertex(circleCoord[0] * 125 + 260 + 126, circleCoord[1] * 125 + 260, 0);
//            if(!MathTools.isEqual(circleCoord[0] * circleCoord[0] + circleCoord[1] * circleCoord[1] + circleCoord[2] * circleCoord[2], 1f, 0.00001f))
//                System.out.println("Problem coordinate: " + circleCoord[0] + ", " + circleCoord[1] + ", " + circleCoord[2] + " is off by " +
//                        (Math.sqrt(circleCoord[0] * circleCoord[0] + circleCoord[1] * circleCoord[1] + circleCoord[2] * circleCoord[2]) - 1));
        }
        renderer.end();

    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        camera.update();
        Arrays.fill(amounts, 0);
        Arrays.fill(dAmounts, 0.0);
        switch (mode) {
            case 0: sphereTrigMode();
            break;
            case 1: sphereGaussianMode();
            break;
            case 2: sphereRotationMode();
            break;
            case 3: spherePairMode();
            break;
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, String.format("Mode %d at %d FPS",
                        mode, Gdx.graphics.getFramesPerSecond()),
                64, 500+22, 256+128, Align.center, true);
//        font.draw(batch, "Lower parameters A/B/C by holding a, b, or c;\nhold Shift and A/B/C to raise.", 64, 500-5, 256+128, Align.center, true);
//        font.draw(batch, "a – lambda; should be greater than 0.0", 64, 500-32, 256+128, Align.center, true);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
        viewport.apply(true);
    }

    public void insideBallRejection(final double[] vector)
    {
        double v1, v2, v3;
        do {
            v1 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            v2 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            v3 = 2 * whisker.nextDouble() - 1; // between -1 and 1
        } while (v1 * v1 + v2 * v2 + v3 * v3 > 1);
        vector[0] = v1;
        vector[1] = v2;
    }
    public final float fastGaussian()
    {
        long a = whisker.nextLong(), b = whisker.nextLong();
        a = (a & 0x0003FF003FF003FFL) +     ((a & 0x0FFC00FFC00FFC00L) >>> 10);
        b = (b & 0x0003FF003FF003FFL) +     ((b & 0x0FFC00FFC00FFC00L) >>> 10);
        a = (a & 0x000000007FF007FFL) +     ((a & 0x0007FF0000000000L) >>> 40);
        b = (b & 0x000000007FF007FFL) +     ((b & 0x0007FF0000000000L) >>> 40);
        return (((a & 0x0000000000000FFFL) + ((a & 0x000000007FF00000L) >>> 20))
                - ((b & 0x0000000000000FFFL) + ((b & 0x000000007FF00000L) >>> 20))) * (0x1p-10f);
    }

    /**
     * Error function from Abramowitz and Stegun, 1964; equation 7.1.27 .
     * See <a href="https://en.wikipedia.org/wiki/Error_function#Approximation_with_elementary_functions">Wikipedia</a>.
     * @param x any finite double
     * @return a double between -1 and 1, inclusive
     */
    public double erf(final double x) {
        final double a1 = 0.0705230784, a2 = 0.0422820123, a3 = 0.0092705272, a4 = 0.0001520143, a5 = 0.0002765672, a6 = 0.0000430638;
        final double sign = Math.signum(x), y1 = sign * x, y2 = y1 * y1, y3 = y1 * y2, y4 = y2 * y2, y5 = y2 * y3, y6 = y3 * y3;
        double n = 1.0 + a1 * y1 + a2 * y2 + a3 * y3 + a4 * y4 + a5 * y5 + a6 * y6;
        n *= n;
        n *= n;
        n *= n;
        return sign * (1.0 - 1.0 / (n * n));
    }

    /**
     * Inverse to the probit function; equivalent to scaled erf(x) from Abramowitz and Stegun, 1964; equation 7.1.27 .
     * See <a href="https://en.wikipedia.org/wiki/Error_function#Approximation_with_elementary_functions">Wikipedia</a>.
     * @param x any finite double
     * @return a double between 0 and 1, inclusive
     */
    public double probitInverse(final double x) {
        final double a1 = 0.0705230784, a2 = 0.0422820123, a3 = 0.0092705272, a4 = 0.0001520143, a5 = 0.0002765672, a6 = 0.0000430638;
        final double sign = Math.signum(x), y1 = sign * x * 0.7071067811865475, y2 = y1 * y1, y3 = y1 * y2, y4 = y2 * y2, y5 = y2 * y3, y6 = y3 * y3;
        double n = 1.0 + a1 * y1 + a2 * y2 + a3 * y3 + a4 * y4 + a5 * y5 + a6 * y6;
        n *= n;
        n *= n;
        n *= n;
        return sign * (0.5 - 0.5 / (n * n)) + 0.5;
    }

    public final float editedCurve()
    {
        long r = whisker.nextLong(), s = whisker.nextLong();
        return ((r >>> 56) - (r >>> 48 & 255) + (r >>> 40 & 255) - (r >>> 32 & 255) + (r >>> 24 & 255) - (r >>> 16 & 255) + (r >>> 8 & 255) - (r & 255)) * 0x1p-8f
                + ((s >>> 48) - (s >>> 32 & 65535) + (s >>> 16 & 65535) - (s & 65535)) * 0x1p-16f;
//        final long r = diver.nextLong(), s = diver.nextLong();
//        return (((r & 0xFFFFFFL) + (r >>> 40)) * 0x1p-25f + (1.0f - ((s & 0xFFFFFFL) * 0x1p-24f) * ((s >>> 40) * 0x1p-24f))) * 0.5f;

//        return 0.1f * (diver.nextFloat() + diver.nextFloat() + diver.nextFloat()
//                + diver.nextFloat() + diver.nextFloat() + diver.nextFloat())
//                + 0.2f * ((1f - diver.nextFloat() * diver.nextFloat()) + (1f - diver.nextFloat() * diver.nextFloat()));

//                - (s & 0xFFFFFFL) - (r >>> 20 & 0xFFFFFFL) - (s >>> 26 & 0xFFFFFFL) - (t >>> 40) - (t >>> 13 & 0xFFFFFFL)
//        return  ((r & 0xFFFFFFL) + (r >>> 20 & 0xFFFFFFL) + (s >>> 40)
//                - (s & 0xFFFFFFL) - (s >>> 20 & 0xFFFFFFL) - (r >>> 40)
//        ) * 0x1p-26f;
//        return ((r & 0xFFFFFFL) * 0x1p-23f - ((s & 0xFFFFFFL) * 0x1p-23f)) * ((r >> 40) * 0x1p-23f) * ((s >> 40) * 0x1p-23f);
    }

    public final double nextGaussian() {
        return whisker.nextGaussian();
    }

    /**
     * <a href="https://marc-b-reynolds.github.io/distribution/2021/03/18/CheapGaussianApprox.html">Credit to Marc B.
     * Reynolds</a>. This version probably could be optimized to minimize maximum error, but I don't really know how.
     * The multiplier was very carefully chosen so the highest and lowest possible results are equally far from 0.
     * @return a normal-distributed double with standard deviation 1.0, actually ranging from -7.929080009460449 to 7.929080009460449
     */
    private double nextGaussianCountX3(){
        long u = whisker.nextLong(), c = Long.bitCount(u) - 32L << 32;
        u *= 0xC6AC29E4C6AC29E5L;
        return 0x1.fb760cp-35 * (c + (u & 0xFFFFFFFFL) - (u >>> 32));
    }

    private double logitGaussian() {
        final double p = nextExclusiveDouble();
        return Math.log(p / (1.0 - p)) * 0.6266570686577501;
    }

    /**
     * Flawed, but maybe could be made to work somehow. Credit to
     * <a href="https://tech.ebayinc.com/engineering/fast-approximate-logarithms-part-i-the-basics/">eBay Tech</a> and
     * <a href="https://gist.github.com/Jacajack/3397dbb8ff22a27dd47c832998edd608">this Gist by Jacajack</a>.
     * @return a very rough approximation of a logistic distributed variable
     */
    private double fastLogitGaussian() {
        final double p = nextExclusiveDouble(), d = p / (1.0 - p);
        // Extract exponent and mantissa
        long xi = BitConversion.doubleToLongBits(d);
        long ei = ( ( xi >>> 52 ) & 0x7ffL ) - 1023L; // Exponent
        long mi = xi & 0x000FFFFFFFFFFFFFL;           // Mantissa

        // Real mantissa value
        double mf = 1.0 + mi * ( 1.0 / 0x000FFFFFFFFFFFFFL );

        // Denormal numbers (optional)
        if ( ei == -1023 ) mf = mi * (1.0 / 0x000FFFFFFFFFFFFFL);

        return (ei + (-0.344845 * mf + 2.024658) * mf - 1.674873) * 0.43436558031807954;
    }

    private double inclusiveDouble() {
        // the simplest, dumbest way of getting a double in [0.0,1.0]
//        return (MathUtils.random.nextLong()|0x8000000000000000L) * 0x1p-63 + 1.0;
        // generate a bounded long between 0 and 0x1p53 inclusive, then multiply by 0x1p-53
        final long rand = MathUtils.random.nextLong();
        final long bound = 0x20000000000001L;
        final long randLow = rand & 0xFFFFFFFFL;
        final long randHigh = (rand >>> 32);
        final long boundHigh = (bound >>> 32);
        return ((randLow * boundHigh >>> 32) + randHigh * boundHigh) * 0x1p-53;
    }

    private double inclusiveDouble2() {
        //make sure to flip the "more magic" switch before operating ;)
        final long bits = whisker.nextLong();
        //generates an exclusive double normally, then adds 0x1p-12 to intentionally incur precision loss, and subtracts
        //0x1p-12 to bring it back to the [0.0,1.0] range. rounding the lowest exclusive value, 0x1p-65, takes it to 0.0
        //while the highest exclusive value, 0x1.fffffffffffffp-1, gets rounded to 1.0 . the mantissa bits are fair!
        return NumberUtils.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52 | bits >>> 12) + 0x1p-12 - 0x1p-12;
    }
    public float inclusiveFloat2 () {
        final long bits = whisker.nextLong();
        return NumberUtils.intBitsToFloat(126 - Long.numberOfTrailingZeros(bits) << 23 | (int)(bits >>> 41)) + 0x1p-22f - 0x1p-22f;
    }

    /**
     * Returns the tangent in turns, using a Padé approximant.
     * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     * @return a double approximation of tan()
     */
    public static double tanTurns(double turns) {
        turns += turns;
        turns += 0.5;
        turns -= Math.floor(turns);
        turns -= 0.5;
        turns *= Math.PI;
        final double x2 = turns * turns, x4 = x2 * x2;
        return turns * ((0.0010582010582010583) * x4 - (0.1111111111111111) * x2 + 1.0)
                / ((0.015873015873015872) * x4 - (0.4444444444444444) * x2 + 1.0);
    }

    public double cauchian() {
        double u = nextExclusiveDouble() - 0.5;
        double turns = u + 0.5;
        turns -= (int)(turns);
        turns -= 0.5;
        turns *= Math.PI;
        final double x2 = turns * turns, x4 = x2 * x2;
        return turns * ((0.0010582010582010583) * x4 - (0.1111111111111111) * x2 + 1.0)
                / ((0.015873015873015872) * x4 - (0.4444444444444444) * x2 + 1.0);
    }


    public void insideBallBoxMuller(final double[] vector)
    {
        double mag = 0.0;
        double v1, v2, v3, s;
        do {
            v1 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            v2 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            v3 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            s = v1 * v1 + v2 * v2 + v3 * v3;
        } while (s > 1 || s == 0);
        double multiplier = Math.sqrt(-2 * Math.log(s) / s);
        mag += (vector[0] = (v1 *= multiplier)) * (v1);
        mag += (vector[1] = (v2 *= multiplier)) * (v2);
        mag += (v3 *= multiplier) * (v3);
        if(mag == 0.0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.cbrt(whisker.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }
    public void insideBallBoxMullerFast(final double[] vector)
    {
        double v1 = fastGaussian(), v2 = fastGaussian(), v3 = fastGaussian();
        double mag = (vector[0] = v1) * v1 + (vector[1] = v2) * v2 + v3 * v3;
        if(mag == 0.0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.cbrt(whisker.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }
    public void insideBallExponential(final double[] vector)
    {
        double v1 = nextGaussian(), v2 = nextGaussian(), v3 = nextGaussian();
        double mag = v1 * v1 + v2 * v2 + v3 * v3 - 2.0 * Math.log(whisker.nextDouble());
        if(mag == 0.0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = 1.0 / Math.sqrt(mag);
        vector[0] = v1 * mag;
        vector[1] = v2 * mag;
    }
    public void insideBallExponentialFast(final double[] vector) {
        double v1 = fastGaussian(), v2 = fastGaussian(), v3 = fastGaussian();//, sq = diver.nextDouble() * diver.nextDouble();
//        double mag = v1 * v1 + v2 * v2 + v3 * v3 + 1.0 / (1.0 - diver.nextDouble() * diver.nextDouble()) - 0.25;
        double mag = v1 * v1 + v2 * v2 + v3 * v3 - 2.0 * Math.log(whisker.nextDouble());
        if (mag == 0.0) {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        } else
            mag = 1.0 / Math.sqrt(mag);
        //if (Math.abs(v3 * mag) < 0.1)
        {
            vector[0] = v1 * mag;
            vector[1] = v2 * mag;
        }
    }
    public void onSphereTrig(final float[] vector)
    {
        float theta = whisker.nextExclusiveFloat();
        float d = whisker.nextExclusiveSignedFloat();
        float phi = TrigTools.acosTurns(d);
        float sinPhi = TrigTools.sinTurns(phi);

        vector[0] = TrigTools.cosTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinTurns(theta) * sinPhi;
        //vector[2] = TrigTools.cosTurns(phi);
    }
    public void onSphereGaussian(final float[] vector)
    {
        float x = (float) whisker.nextGaussian();
        float y = (float) whisker.nextGaussian();
        float z = (float) whisker.nextGaussian();

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
//        z *= mag;
        vector[0] = x;
        vector[1] = y;
        //vector[2] = z;
    }
    public void insideCircleBoxMuller(final double[] vector)
    {
        double mag = 0.0;
        double v1, v2, s;
        do {
            v1 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            v2 = 2 * whisker.nextDouble() - 1; // between -1 and 1
            s = v1 * v1 + v2 * v2;
        } while (s > 1 || s == 0);
        double multiplier = Math.sqrt(-2 * Math.log(s) / s);
        mag += (vector[0] = (v1 *= multiplier)) * (v1);
        mag += (vector[1] = (v2 *= multiplier)) * (v2);
        //mag += -2.0 * Math.log(diver.nextDouble());
        if(mag == 0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.sqrt(whisker.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }
    public void insideCircleBoxMullerFast(final double[] vector)
    {
        double mag = 0.0;
        double v1 = fastGaussian(), v2 = fastGaussian();
        mag += (vector[0] = v1) * v1;
        mag += (vector[1] = v2) * v2;
        if(mag == 0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.sqrt(whisker.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }

    /**
     * Ported by jmelange/terefang from Mersenne Twiser code; the only difference here
     * is where variance is applied.
     * @param mean the center value or average
     * @param variance how far from the mean values should spread
     * @return a normal-distributed value with the given mean and variance
     */
    public double langeBoxMuller(final double mean, final double variance)
    {
        // Return a real number from a normal (Gaussian) distribution with given
        // mean and variance by Box-Muller method
        double r = Math.sqrt( -2.0 * Math.log( nextExclusiveDouble() ) ) * variance;
//        double phi = (2.0 * 3.14159265358979323846264338328) * nextExclusiveDouble();
        return mean + r * TrigTools.cosTurns(nextExclusiveDouble()); // could use NumberTools.cos_(diver.nextDouble()) instead of Math.cos(phi)
    }

    public static int determinePositive16(final int state)
    {
        return state >>> 1 ^ (-(state & 1) & 0xB400);
    }

    public static int serpentinePair(int x, int y) {
        return (x >= y) ? (((x & 1) == 1) ? x * x + y : x * (x + 2) - y) : (((y & 1) == 1) ? y * (y + 2) - x : y * y + x);
    }

    /**
     * This is a simplified version of <a href="https://allendowney.com/research/rand/">this
     * algorithm by Allen Downey</a>. This version can return double values between 2.710505431213761E-20 and
     * 0.9999999999999999, or 0x1.0p-65 and 0x1.fffffffffffffp-1 in hex notation. It cannot return 0 or 1. It has much
     * more uniform bit distribution across its mantissa/significand bits than {@link Random#nextDouble()}. Where Random
     * is less likely to produce a "1" bit for its lowest 5 bits of mantissa (the least significant bits numerically,
     * but potentially important for some uses), with the least significant bit produced half as often as the most
     * significant bit in the mantissa, this has approximately the same likelihood of producing a "1" bit for any
     * positions in the mantissa.
     * @return a random uniform double between 0 and 1 (both exclusive, unlike most nextDouble() implementations)
     */
    public double nextExclusiveDouble(){
        final long bits = whisker.nextLong();
        return NumberUtils.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52 | bits >>> 12);
    }

    /**
     * This version was adapted from jdkgdxds' code for ShaiRandom to use (it's in C# and targets older versions, so it
     * doesn't have the luxury of Long.numberOfTrailingZeros() ). CTZ is the count trailing zeros instruction that
     * Long.numberOfTrailingZeros() compiles to.
     * @return a random uniform double between 0 and 1 (both exclusive, unlike most nextDouble() implementations)
     */
    public static double nextExclusiveDoubleNoCTZ(){
        final long bits = MathUtils.random.nextLong();
        return Double.longBitsToDouble((0x7C10000000000000L + (Double.doubleToRawLongBits(0x80000000000003FFL | bits) & 0xFFF0000000000000L)) | (~bits & 0x000FFFFFFFFFFFFFL));
//        return NumberUtils.longBitsToDouble((0x7C10000000000000L + (NumberUtils.doubleToLongBits(0x8000000000000001L | bits) & 0xFFF0000000000000L)) | (~bits & 0x000FFFFFFFFFFFFFL));
    }
    /**
     * This is a simplified version of <a href="https://allendowney.com/research/rand/">this
     * algorithm by Allen Downey</a>. This version can return double values between 2.7105054E-20 to 0.99999994, or
     * 0x1.0p-65 to 0x1.fffffep-1 in hex notation. It cannot return 0 or 1. It has much
     * more uniform bit distribution across its mantissa/significand bits than {@link Random#nextDouble()}. Where Random
     * is less likely to produce a "1" bit for its lowest 5 bits of mantissa (the least significant bits numerically,
     * but potentially important for some uses), with the least significant bit produced half as often as the most
     * significant bit in the mantissa, this has approximately the same likelihood of producing a "1" bit for any
     * positions in the mantissa.
     * @return a random uniform double between 0 and 1 (both exclusive, unlike most nextDouble() implementations)
     */
    public float nextExclusiveFloat(){
        final long bits = whisker.nextLong();
        return NumberUtils.intBitsToFloat(126 - Long.numberOfTrailingZeros(bits) << 23 | (int)(bits >>> 41));
    }

    /**
     * Gets a random double that may be positive or negative, but cannot be 0, and always has a magnitude less than 1.
     * <br>
     * This is a modified version of <a href="https://allendowney.com/research/rand/">this
     * algorithm by Allen Downey</a>. This version can return double values between -0.9999999999999999 and
     * -5.421010862427522E-20, as well as between 2.710505431213761E-20 and 0.9999999999999999, or -0x1.fffffffffffffp-1
     * to -0x1.0p-64 as well as between 0x1.0p-65 and 0x1.fffffffffffffp-1 in hex notation. It cannot return -1, 0 or 1.
     * It has much more uniform bit distribution across its mantissa/significand bits than {@link Random#nextDouble()},
     * especially when the result of nextDouble() is expanded to the -1.0 to 1.0 range (such as with
     * {@code 2.0 * (nextDouble() - 0.5)}). Where the given example code is unable to produce a "1" bit for its lowest
     * bit of mantissa (the least significant bits numerically, but potentially important for some uses), this has
     * approximately the same likelihood of producing a "1" bit for any positions in the mantissa, and also equal odds
     * for the sign bit.
     * @return a random uniform double between -1 and 1 with a tiny hole around 0 (all exclusive)
     */
    public double nextExclusiveSignedDouble(){
        final long bits = whisker.nextLong();
        return NumberUtils.longBitsToDouble(1022L - Long.numberOfLeadingZeros(bits) << 52 | ((bits << 63 | bits >>> 1) & 0x800FFFFFFFFFFFFFL));
    }

    /**
     * Gets a random float that may be positive or negative, but cannot be 0, and always has a magnitude less than 1.
     * <br>
     * This is a modified version of <a href="https://allendowney.com/research/rand/">this
     * algorithm by Allen Downey</a>. This version can return double values between -0.99999994 and -1.1641532E-10, as
     * well as between 2.7105054E-20 and 0.99999994, or -0x1.fffffep-1 to -0x1.0p-33 as well as between 0x1.0p-65 and
     * 0x1.fffffep-1 in hex notation. It cannot return -1, 0 or 1. It has much more uniform bit distribution across its
     * mantissa/significand bits than {@link Random#nextDouble()}, especially when the result of nextDouble() is
     * expanded to the -1.0 to 1.0 range (such as with {@code 2.0 * (nextDouble() - 0.5)}). Where the given example code
     * is unable to produce a "1" bit for its lowest bit of mantissa (the least significant bits numerically, but
     * potentially important for some uses), this has approximately the same likelihood of producing a "1" bit for any
     * positions in the mantissa, and also equal odds for the sign bit.
     * @return a random uniform double between -1 and 1 with a tiny hole around 0 (all exclusive)
     */
    public float nextExclusiveSignedFloat(){
        final long bits = whisker.nextLong();
        return NumberUtils.intBitsToFloat(126 - Long.numberOfLeadingZeros(bits) << 23 | ((int)bits & 0x807FFFFF));
    }

    public static float hashFloat(float x, float y) {
        long state = 0xC13FA9A902A6328FL * NumberUtils.floatToIntBits(x) + 0x91E10DA5C79E7B1DL * NumberUtils.floatToIntBits(y);
        return ((((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >> 40) * 0x1p-23f;
    }
    private double acbrt(double r)
    {
        double a = 1.4774329094 - 0.8414323527/(r+0.7387320679),
                //a = 0.6042181313 * r + 0.4531635984,
                a3 = a * a * a, a3r = a3 + r;
        //a *= ((a3r + r) / (a3 + a3r));
        return a * ((a3r + r) / (a3 + a3r));
    }

    private float cbrtShape(float x){
        final int i = BitConversion.floatToRawIntBits(x);
        return BitConversion.intBitsToFloat(((i & 0x7FFFFFFF) - 0x3F800000) / 3 + 0x3F800000 | (i & 0x80000000));
    }
    public static double vdc(double base, int index)
    {
        //0.7548776662466927, 0.5698402909980532
        double n = (index+1 & 0x7fffffff);
        base += 0.6180339887498949;
        base *= 0.6180339887498949;
        base -= (int)base;
        double res = 0.0;
        while (n >= 1) {
            res += (n *= base);
            base += 0.6180339887498949;
            base *= 0.6180339887498949;
            base -= (int) base;
        }
        return res - (int)res;
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Visualizer for Math Testing/Checking");
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.setWindowedMode(512, 520);
        new Lwjgl3Application(new SphereVisualizer(), config);
    }

}
