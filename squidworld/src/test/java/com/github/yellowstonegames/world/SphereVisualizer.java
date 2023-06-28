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
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.QuasiRandomTools;
import com.github.yellowstonegames.grid.RotationTools;

import java.util.Arrays;

/**
 * Adapted from SquidLib's MathVisualizer, but stripped down to only include sphere-related math.
 */
public class SphereVisualizer extends ApplicationAdapter {
    public static final int POINT_COUNT = 0x4000;
    public static final float INVERSE_SPEED = 1E-11f;
    private float[][] points = new float[POINT_COUNT][3];
    private int mode = 0;
    private int modes = 12;
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
//    private final EnhancedRandom random = new MizuchiRandom(seed);
//    private final EnhancedRandom random = new RomuTrioRandom(seed);
//    private final EnhancedRandom random = new DistinctRandom(seed);
//    private final EnhancedRandom random = new RandomRandom(seed);
    private final float black = Color.BLACK.toFloatBits();
    private final float blue = Color.BLUE.toFloatBits();
    private final float cyan = Color.CYAN.toFloatBits();
    private final float red = Color.RED.toFloatBits();
    private final float smoke = Color.toFloatBits(0f, 0f, 0f, 0.25f);

    /**
     * With seed 123456789 given to a WhiskerRandom for the random types, and 0x4000 points...
     * <br>
     * On mode 0, minimum distance was 0.000117, between point 5453, [-0.098636,0.288150,0.952492] and point 6246, [-0.098746,0.288112,0.952492]
     * On mode 1, minimum distance was 0.000056, between point 12250, [-0.588344,-0.803925,-0.086931] and point 14921, [-0.588308,-0.803954,-0.086900]
     * On mode 2, minimum distance was 0.000122, between point 8244, [0.704946,-0.704698,0.080327] and point 13464, [0.704867,-0.704769,0.080387]
     * On mode 3, minimum distance was 0.000105, between point 4897, [-0.017502,-0.731484,0.681633] and point 11135, [-0.017527,-0.731553,0.681559]
     * On mode 4, minimum distance was 0.000277, between point 9517, [-0.048890,0.193368,0.979908] and point 9778, [-0.048672,0.193202,0.979951]
     * On mode 5, minimum distance was 0.000105, between point 4897, [-0.017502,-0.731484,0.681633] and point 11135, [-0.017527,-0.731553,0.681559]
     * On mode 6, minimum distance was 0.000098, between point 1451, [0.650076,0.288446,0.702994] and point 6780, [0.650002,0.288472,0.703052]
     * On mode 7, minimum distance was 0.000143, between point 2, [-0.528052,-0.029139,0.848712] and point 7833, [-0.528013,-0.029005,0.848741]
     * <br>
     * With seed 123456789 given to an AceRandom for the random types, and 0x4000 points...
     * <br>
     * On mode 0, minimum distance was 0.000000, between point 1176, [0.613156,-0.412766,-0.673546] and point 10151, [0.613156,-0.412766,-0.673546]
     * On mode 1, minimum distance was 0.000179, between point 1937, [-0.538566,0.165698,0.826130] and point 9120, [-0.538657,0.165826,0.826045]
     * On mode 2, minimum distance was 0.000122, between point 8244, [0.704946,-0.704698,0.080327] and point 13464, [0.704867,-0.704769,0.080387]
     * On mode 3, minimum distance was 0.000105, between point 4897, [-0.017502,-0.731484,0.681633] and point 11135, [-0.017527,-0.731553,0.681559]
     * On mode 4, minimum distance was 0.000277, between point 9517, [-0.048890,0.193368,0.979908] and point 9778, [-0.048672,0.193202,0.979951]
     * On mode 5, minimum distance was 0.000105, between point 4897, [-0.017502,-0.731484,0.681633] and point 11135, [-0.017527,-0.731553,0.681559]
     * On mode 6, minimum distance was 0.000098, between point 1451, [0.650076,0.288446,0.702994] and point 6780, [0.650002,0.288472,0.703052]
     * On mode 7, minimum distance was 0.000143, between point 2, [-0.528052,-0.029139,0.848712] and point 7833, [-0.528013,-0.029005,0.848741]
     * <br>
     * AceRandom on mode 0 produced at least two identical points over a run of 16384 random lat/lon pairs,
     * and it did so once for at least seeds 1234567890 and 9876543210, and four times for at least seed 123456789.
     * Testing more generators, having one identical point on mode 0 is the norm, and only WhiskerRandom so far hasn't
     * had any identical points on any modes.
     */
    public void showStats() {
        float minDist2 = Float.MAX_VALUE, dst2;
        int closeI = 0, closeJ = 1, identicalPairs = 0;
        for (int i = 0; i < POINT_COUNT; i++) {
            for (int j = i + 1; j < POINT_COUNT; j++) {
                if (minDist2 != (minDist2 = Math.min(minDist2,
                        dst2 = Vector3.dst2(points[i][0], points[i][1], points[i][2], points[j][0], points[j][1], points[j][2])))) {
                    closeI = i;
                    closeJ = j;
                }
                if(dst2 == 0f && !UIUtils.ctrl())
                    System.out.println("IDENTICAL POINT PAIR #" + ++identicalPairs + " at " + points[i][0] + " " + points[i][1] + " " + points[i][2]);
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
                    if(!UIUtils.ctrl())
                        System.out.println("Changed to mode " + mode);
                    return true;
                } else if (keycode == Input.Keys.MINUS || keycode == Input.Keys.BACKSPACE) {
                    mode = (mode + modes - 1) % modes;
                    if(!UIUtils.ctrl())
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

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        camera.update();
        Arrays.fill(amounts, 0);
        Arrays.fill(dAmounts, 0.0);
        switch (mode) {
            case 0: if(UIUtils.shift()) sphereTrigAltMode();
            else sphereTrigMode();
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
            case 8: if(UIUtils.shift()) sphereFibonacciAltMode();
                else sphereFibonacciMode();
                break;
            case 9: sphereR2Mode();
                break;
            case 10: sphereHalton2Mode();
                break;
            case 11:
                if(UIUtils.shift()) sphereHammersley2AltMode();
                else sphereHammersley2Mode();
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

    private void sphereTrigAltMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereTrigAlt(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }
    private void sphereTrigMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
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

    private void sphereFibonacciMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereFibonacci(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereFibonacciAltMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereFibonacciAlt(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereR2Mode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereR2(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereHalton2Mode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereHalton2(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereHammersley2Mode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereHammersley2(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphereHammersley2AltMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        random.setSeed(seed);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereHammersley2Alt(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    public void onSphereTrigAlt(final int index)
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

    public void onSphereTrig(final int index)
    {
        float lon = random.nextExclusiveFloat();
        float u = random.nextExclusiveSignedFloat();
        float root = (float) Math.sqrt(1f - u * u);

        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(lon) * root;
        vector[1] = TrigTools.sinTurns(lon) * root;
        vector[2] = u;

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
     * <a href="https://extremelearning.com.au/how-to-evenly-distribute-points-on-a-sphere-more-effectively-than-the-canonical-fibonacci-lattice/">Uses Martin Roberts' modified Fibonacci lattice.</a>
     * @param index a non-negative int less than {@link #POINT_COUNT}
     */
    public void onSphereFibonacciAlt(final int index)
    {

        float theta = (index * 0x9E3779B97F4A7C15L >>> 41) * 0x1p-23f;
        float phi = TrigTools.acosTurns(1 - 2 * (index + 0.36f) / (POINT_COUNT - 0.28f));
        float sinPhi = TrigTools.sinTurns(phi);

        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinTurns(theta) * sinPhi;
        vector[2] = TrigTools.cosTurns(phi);
    }

    public void onSphereFibonacci(final int index)
    {
        float lat = (index + 0.36f) / (POINT_COUNT - 0.28f);
        float lon = (index * 0x9E3779B97F4A7C15L >>> 41) * 0x1p-23f;
        float u = 1f - 2f * lat;
//        float u = (lat - 0.5f) * 2f; // may be a tiny bit more precise, but not the same as onSphereFibonacci().
        float root = (float) Math.sqrt(1f - u * u);
        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(lon) * root;
        vector[1] = TrigTools.sinTurns(lon) * root;
        vector[2] = u;
    }

    public void onSphereR2(final int index)
    {
        float theta = (QuasiRandomTools.goldenLong[1][0] * index >>> 41) * 0x1p-23f;
        float d = (QuasiRandomTools.goldenLong[1][1] * index >> 40) * 0x1p-23f;
        float phi = TrigTools.acosTurns(d);
        float sinPhi = TrigTools.sinTurns(phi);

        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinTurns(theta) * sinPhi;
        vector[2] = TrigTools.cosTurns(phi);
    }

    // with 2, 3: minimum distance was 0.003802
    // with 2, 7: minimum distance was 0.006096
    public void onSphereHalton2(final int index)
    {
        float theta = QuasiRandomTools.vanDerCorput(2, index);
        float d = (QuasiRandomTools.vanDerCorput(7, index) - 0.5f) * 2f;
        float phi = TrigTools.acosTurns(d);
        float sinPhi = TrigTools.sinTurns(phi);

        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinTurns(theta) * sinPhi;
        vector[2] = TrigTools.cosTurns(phi);
    }

    public void onSphereHammersley2Alt(final int index)
    {
        float theta = (index + 0.5f) / POINT_COUNT;
        float d = (QuasiRandomTools.vanDerCorput(2, index) - 0.5f) * 2f;
        float phi = TrigTools.acosTurns(d);
        float sinPhi = TrigTools.sinSmootherTurns(phi);

        float[] vector = points[index];

        vector[0] = TrigTools.cosSmootherTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinSmootherTurns(theta) * sinPhi;
        vector[2] = TrigTools.cosSmootherTurns(phi);
    }

    public void onSphereHammersley2(final int index)
    {
        float lat = QuasiRandomTools.vanDerCorput(2, index);
        float lon = (index + 0.5f) / POINT_COUNT;
        float u = (lat - 0.5f) * 2f;
        float root = (float) Math.sqrt(1f - u * u);
        float[] vector = points[index];

        vector[0] = TrigTools.cosSmootherTurns(lon) * root;
        vector[1] = TrigTools.sinSmootherTurns(lon) * root;
        vector[2] = u;
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
