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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.*;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.QuasiRandomTools;
import com.github.yellowstonegames.grid.RotationTools;
import com.github.yellowstonegames.world.random.RandomRandom;

import java.util.Arrays;
import java.util.Random;

/**
 * Adapted from SquidLib's MathVisualizer, but stripped down to only include sphere-related math.
 */
public class SphereVisualizer extends ApplicationAdapter {
    public static final int POINT_COUNT = 0x10000;
    private float[][] points = new float[POINT_COUNT][3];
    private int mode = 0;
    private int modes = 8;
    private SpriteBatch batch;
    private ImmediateModeRenderer20 renderer;
    private InputAdapter input;
    private BitmapFont font;
    private ScreenViewport viewport;
    private Camera camera;
    private int[] amounts = new int[512];
    private double[] dAmounts = new double[512];
    private long seed = 123456789L;
    private long startTime;
    private float[] circleCoord = new float[3];
    private final EnhancedRandom random = new AceRandom(seed);
//    private final EnhancedRandom random = new WhiskerRandom(seed);
//    private final EnhancedRandom random = new ScruffRandom(seed);
//    private final EnhancedRandom random = new RandomRandom(seed);
    private final float black = Color.BLACK.toFloatBits();
    private final float blue = Color.BLUE.toFloatBits();
    private final float cyan = Color.CYAN.toFloatBits();
    private final float red = Color.RED.toFloatBits();
    private final float smoke = Color.toFloatBits(0f, 0f, 0f, 0.25f);

    /**
     * With seed 123456789 given to a WhiskerRandom for the random types...
     * <br>
     * On mode 0, minimum distance was 0.003787, between point 75, [0.515751,0.836292,-0.186055] and point 139, [0.518916,0.834248,-0.186432]
     * On mode 1, minimum distance was 0.001726, between point 244, [0.507996,0.529551,-0.679350] and point 247, [0.508196,0.530826,-0.678204]
     * On mode 2, minimum distance was 0.004055, between point 117, [0.441669,-0.894222,-0.072774] and point 157, [0.438834,-0.895810,-0.070349]
     * On mode 3, minimum distance was 0.008227, between point 8, [0.511021,0.423659,-0.747911] and point 147, [0.512761,0.430063,-0.743049]
     * On mode 4, minimum distance was 0.012335, between point 130, [0.049258,-0.972008,0.229726] and point 250, [0.051183,-0.974644,0.217831]
     * On mode 5, minimum distance was 0.004672, between point 22, [-0.802256,0.284587,-0.524781] and point 226, [-0.800069,0.288543,-0.525959]
     * On mode 6, minimum distance was 0.013058, between point 52, [0.088732,0.995822,0.021552] and point 152, [0.090197,0.995887,0.008577]
     */
    public void showStats() {
        float minDist2 = Float.MAX_VALUE;
        int closeI = 0, closeJ = 1;
        for (int i = 0; i < POINT_COUNT; i++) {
            for (int j = i + 1; j < POINT_COUNT; j++) {
                if (minDist2 != (minDist2 = Math.min(minDist2,
                        Vector3.dst2(points[i][0], points[i][1], points[i][2], points[j][0], points[j][1], points[j][2])))) {
                    closeI = i;
                    closeJ = j;
                }
            }
        }
        System.out.printf("On mode %d, minimum distance was %f, between point %d, [%f,%f,%f] and point %d, [%f,%f,%f]\n",
                mode, Math.sqrt(minDist2),
                closeI, points[closeI][0], points[closeI][1], points[closeI][2],
                closeJ, points[closeJ][0], points[closeJ][1], points[closeJ][2]);
    }
    @Override
    public void create() {
        startTime = TimeUtils.millis();
        Coord.expandPoolTo(512, 512);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("Cozette-standard.fnt"));
        font.setColor(Color.BLACK);
        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        camera = viewport.getCamera();
        renderer = new ImmediateModeRenderer20(0x80000, false, true, 0);
        Arrays.fill(amounts, 0);
        input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER || keycode == Input.Keys.EQUALS) {
                    mode = (mode + 1) % modes;
                    System.out.println("Changed to mode " + mode);
                    return true;
                } else if (keycode == Input.Keys.MINUS || keycode == Input.Keys.BACKSPACE) {
                    mode = (mode + modes - 1) % modes;
                    System.out.println("Changed to mode " + mode);
                    return true;
                } else if (keycode == Input.Keys.P || keycode == Input.Keys.S) {
                    showStats();
                } else if (keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE)
                    Gdx.app.exit();

                return false;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    private void sphereTrigMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereTrig(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereGaussianMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereGaussian(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereHaltonMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereHalton(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereRobertsMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereRoberts(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }
    private void sphereRobertsVDCMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereRobertsVDC(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereBitCountMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereBitCount(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        long seed = this.seed;
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i][0] = points[i][1] = points[i][2] = 0f;

//            float s = TrigTools.sinDeg(i);
//            float c = TrigTools.cosDeg(i);
//            rot[0] = rot[4] = c;
//            rot[1] = s;
//            rot[3] = -s;
//            RotationTools.rotate(pole, rot, circleCoord);

            RotationTools.rotate(pole, RotationTools.randomRotation3D(++seed, RotationTools.randomRotation2D(-100000000000L - seed)), points[i]);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * 1E-10f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereTrig(i);
            renderer.color(smoke);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125 + 260 - 126, points[i][1] * 125 + 260, 0);
        }
//        time++;
//        pole[0] = TrigTools.cosDeg(time);
//        pole[1] = 0;
//        pole[2] = TrigTools.sinDeg(time);
//        long seed = this.seed;
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i][0] = points[i][1] = points[i][2] = 0f;
//            RotationTools.rotate(pole, RotationTools.randomRotation3D(++seed), points[i]);
            onSphereRoberts(i);
            renderer.color(smoke);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125 + 260 + 126, points[i][1] * 125 + 260, 0);
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
            case 4: sphereHaltonMode();
            break;
            case 5: sphereRobertsMode();
            break;
            case 6: sphereRobertsVDCMode();
            break;
            case 7: sphereBitCountMode();
            break;
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, String.format("Mode %d at %d FPS",
                        mode, Gdx.graphics.getFramesPerSecond()),
                64, 518, 256+128, Align.center, true);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
        viewport.apply(true);
    }

    public void onSphereTrig(final int index)
    {
        float theta = random.nextExclusiveFloat();
        float d = random.nextExclusiveSignedFloat();
        float phi = TrigTools.acosTurns(d);
        float sinPhi = TrigTools.sinTurns(phi);

        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinTurns(theta) * sinPhi;
        vector[2] = TrigTools.cosTurns(phi);
    }
    public void onSphereGaussian(final int index)
    {
        float x = (float) random.nextGaussian();
        float y = (float) random.nextGaussian();
        float z = (float) random.nextGaussian();

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
        z *= mag;

        float[] vector = points[index];
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }
    public void onSphereHalton(final int index)
    {
        float x = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(3, index));
        float y = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(5, index));
        float z = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(7, index));

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
        z *= mag;

        float[] vector = points[index];
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }
    public void onSphereRoberts(final int index)
    {
        float x = (float) MathTools.probit((QuasiRandomTools.goldenLong[2][0] * index >>> 41) * 0x1p-23);
        float y = (float) MathTools.probit((QuasiRandomTools.goldenLong[2][1] * index >>> 41) * 0x1p-23);
        float z = (float) MathTools.probit((QuasiRandomTools.goldenLong[2][2] * index >>> 41) * 0x1p-23);

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
        z *= mag;

        float[] vector = points[index];
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }
    public void onSphereRobertsVDC(final int index)
    {
        long v = Long.reverse(index) >>> 1;
        float x = (float) MathTools.probit(((QuasiRandomTools.goldenLong[2][0] * index ^ v) >>> 41) * 0x1p-23);
        float y = (float) MathTools.probit(((QuasiRandomTools.goldenLong[2][1] * index ^ v) >>> 41) * 0x1p-23);
        float z = (float) MathTools.probit(((QuasiRandomTools.goldenLong[2][2] * index ^ v) >>> 41) * 0x1p-23);

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
        z *= mag;

        float[] vector = points[index];
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }
    public void onSphereBitCount(final int index)
    {
        long a = QuasiRandomTools.goldenLong[2][0] * index;
        long b = QuasiRandomTools.goldenLong[2][1] * index;
        long c = QuasiRandomTools.goldenLong[2][2] * index;
        float x = Long.bitCount(a) - 31.5f + a * 0x1p-64f;
        float y = Long.bitCount(b) - 31.5f + b * 0x1p-64f;
        float z = Long.bitCount(c) - 31.5f + c * 0x1p-64f;

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
        z *= mag;

        float[] vector = points[index];
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
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
        final long bits = random.nextLong();
        return NumberUtils.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52 | bits >>> 12);
    }


    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Visualizer for Math Testing/Checking");
        config.setResizable(false);
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setWindowedMode(512, 530);
        new Lwjgl3Application(new SphereVisualizer(), config);
    }

}
