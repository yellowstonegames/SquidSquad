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
import com.github.tommyettinger.random.*;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.QuasiRandomTools;
import com.github.yellowstonegames.grid.RotationTools;

import java.util.Arrays;

/**
 * Adapted from SquidLib's MathVisualizer, but stripped down to only include sphere-related math.
 */
public class SphereVisualizer extends ApplicationAdapter {
    public static final int POINT_COUNT = 256;
    public static final float INVERSE_SPEED = 1E-11f;
    private float[][] points = new float[POINT_COUNT][3];
    private int mode = 0;
    private int modes = 19;
    private SpriteBatch batch;
    private ImmediateModeRenderer20 renderer;
    private InputAdapter input;
    private BitmapFont font;
    private ScreenViewport viewport;
    private Camera camera;
    private int[] amounts = new int[512];
    private double[] dAmounts = new double[512];
    private long seed = 1234567890L;
    private long startTime;
    private float[] circleCoord = new float[3];
    private final EnhancedRandom random = new AceRandom(seed);
//    private final EnhancedRandom random = new WhiskerRandom(seed);
//    private final EnhancedRandom random = new ScruffRandom(seed);
//    private final EnhancedRandom random = new MizuchiRandom(seed);
//    private final EnhancedRandom random = new RomuTrioRandom(seed);
//    private final EnhancedRandom random = new DistinctRandom(seed);
//    private final EnhancedRandom random = new RandomRandom(seed);
    private static final float[] pole5 = new float[]{1f, 0f, 0f, 0f, 0f};
    private static final float[] reversePole5 = new float[]{-1f, 0f, 0f, 0f, 0f};


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
     * <br>
     * For the reroll, triggered by pressing R:
     * <br>
     * Best seed: 0x00000000075C3836L with deviation 3.054775
     * On mode 16, minimum distance was 0.003993, between point 548, [-0.295835,-0.091365,-0.203120] and point 598, [-0.295112,-0.093114,-0.206636]
     * Best seed: 0xD1762C9090678AF4L with deviation 0.550118
     * On mode 16, minimum distance was 0.002164, between point 1422, [0.150965,-0.189108,0.412487] and point 2300, [0.150250,-0.188314,0.410605]
     * <br>
     * With POINT_COUNT=256:
     * <br>
     * Best seed: 0x00000000076455CEL with deviation 0.263218
     * On mode 16, minimum distance was 0.011267, between point 69, [0.094057,-0.119855,-0.336831] and point 114, [0.096128,-0.109347,-0.340331]
     * Best seed: 0x2D332D421055FD30L with deviation 0.223974
     * On mode 16, minimum distance was 0.020497, between point 62, [0.442961,-0.081949,-0.126504] and point 100, [0.429359,-0.068555,-0.133967]
     * <br>
     * Using a balanced technique that ensures deviation is 0, we now compare by how high the min distance can be.
     * Best seed: 0x13B542776CCE0317L with best min dist 0.289634
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
                } else if(keycode == Input.Keys.R) {
                    long bestSeed = seed;
                    double bestMinDist = -Double.MAX_VALUE;
                    for (int i = 0; i < 250000; i++) {
                        random.setSeed(seed);
                        Arrays.fill(GRADIENTS_5D_TEMP, 0f);
                        roll(random, GRADIENTS_5D_TEMP);
                        float dist = evaluateMinDistance2(GRADIENTS_5D_TEMP);
                        if(bestMinDist < (bestMinDist = Math.max(bestMinDist, dist))){
                            bestSeed = seed;
                        }
                        seed += 0xDB4F0B9175AE2165L;// 0x9E3779B97F4A7C15L;
                    }
                    System.out.printf("Best seed: 0x%016XL with best min dist %f\n", bestSeed, Math.sqrt(bestMinDist));
                    random.setSeed(bestSeed);
                    Arrays.fill(GRADIENTS_5D_ACE, 0f);
                    roll(random, GRADIENTS_5D_ACE);
                    random.setSeed(seed);
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
            case 12: spherePhiMode();
                break;
            case 13: sphere5DMode();
                break;
            case 14: sphere5DHaltonMode();
                break;
            case 15: sphere5DR5Mode();
                break;
            case 16: sphere5DAceMode();
                break;
            case 17: sphere5DGoldenMode();
                break;
            case 18: sphere5DVDCMode();
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
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereHammersley2Alt(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void spherePhiMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSpherePhi(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphere5DMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 4f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
        }
        renderer.end();
    }

    private void sphere5DHaltonMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 4f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_HALTON);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void sphere5DR5Mode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 4f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_R5);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DAceMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 4f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_ACE);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DGoldenMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 4f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_GOLDEN);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DVDCMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 4f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_VDC);
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
        float x = (float) MathTools.probit((QuasiRandomTools.goldenLong[2][0] * index >>> 12) * 0x1p-52);
        float y = (float) MathTools.probit((QuasiRandomTools.goldenLong[2][1] * index >>> 12) * 0x1p-52);
        float z = (float) MathTools.probit((QuasiRandomTools.goldenLong[2][2] * index >>> 12) * 0x1p-52);

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
        long v = Long.reverse(index);
        float x = (float) MathTools.probit(((QuasiRandomTools.goldenLong[2][0] * index ^ v) >>> 12) * 0x1p-52);
        float y = (float) MathTools.probit(((QuasiRandomTools.goldenLong[2][1] * index ^ v) >>> 12) * 0x1p-52);
        float z = (float) MathTools.probit(((QuasiRandomTools.goldenLong[2][2] * index ^ v) >>> 12) * 0x1p-52);

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
        float lon = (QuasiRandomTools.goldenLong[1][0] * index >>> 41) * 0x1p-23f;
        float u = (QuasiRandomTools.goldenLong[1][1] * index >> 40) * 0x1p-23f;
        float root = (float) Math.sqrt(1f - u * u);
        float[] vector = points[index];
        vector[0] = TrigTools.cosTurns(lon) * root;
        vector[1] = TrigTools.sinTurns(lon) * root;
        vector[2] = u;
    }

    // with 2, 3: minimum distance was 0.003802
    // with 2, 7: minimum distance was 0.006096
    public void onSphereHalton2(final int index)
    {
        float lat = QuasiRandomTools.vanDerCorput(2, index);
        float lon = QuasiRandomTools.vanDerCorput(7, index);
        float u = (lat - 0.5f) * 2f;
        float root = (float) Math.sqrt(1f - u * u);
        float[] vector = points[index];
        vector[0] = TrigTools.cosTurns(lon) * root;
        vector[1] = TrigTools.sinTurns(lon) * root;
        vector[2] = u;
    }

    public void onSphereHammersley2Alt(final int index)
    {
        float theta = (index + 0.5f) / POINT_COUNT;
        float d = (QuasiRandomTools.vanDerCorput(2, index) - 0.5f) * 2f;
        float phi = TrigTools.acosTurns(d);
        float sinPhi = TrigTools.sinTurns(phi);

        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(theta) * sinPhi;
        vector[1] = TrigTools.sinTurns(theta) * sinPhi;
        vector[2] = TrigTools.cosTurns(phi);
    }

    public void onSphereHammersley2(final int index)
    {
        float lat = QuasiRandomTools.vanDerCorput(2, index);
        float lon = (index + 0.5f) / POINT_COUNT;
        float u = (lat - 0.5f) * 2f;
        float root = (float) Math.sqrt(1f - u * u);
        float[] vector = points[index];
        vector[0] = TrigTools.cosTurns(lon) * root;
        vector[1] = TrigTools.sinTurns(lon) * root;
        vector[2] = u;
    }

    // NOTE: This is awful. Preserve this for posterity.
    public void onSpherePhi(final int index)
    {
        final int i = index * 3;
        float x = (float) MathTools.probit((0x9E3779B97F4A7C15L * (i+1) >>> 12) * 0x1p-52);
        float y = (float) MathTools.probit((0x9E3779B97F4A7C15L * (i+2) >>> 12) * 0x1p-52);
        float z = (float) MathTools.probit((0x9E3779B97F4A7C15L * (i+3) >>> 12) * 0x1p-52);

        final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z);
        x *= mag;
        y *= mag;
        z *= mag;

        float[] vector = points[index];
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }

    public void inSphereFrom5D(final int index, final float[] gradients)
    {
        final int i = (index & gradients.length - 8 >>> 3) << 3;
        float x = gradients[i + 0];
        float y = gradients[i + 1];
        float z = gradients[i + 2];
//        float w = GRADIENTS_5D[i + 3];
//        float u = GRADIENTS_5D[i + 4];

        final float mag = 0.5f;

        float[] vector = points[index];
        vector[0] = x * mag;
        vector[1] = y * mag;
        vector[2] = z * mag;
    }

    /**
     * This gradient vector array was quasi-randomly generated after a lot of rejection sampling. Each gradient should
     * have a magnitude of 2.0, matching the magnitude of the center of an edge of a 5D hypercube.
     * This may bias slightly in some directions. The sums of the x, y, z, w, and u components of all 256 vectors are:
     * <br>
     * x: +0.52959638973427, y: +0.31401370534460, z: -0.14792091580658, w: -0.00781214643439, u: -0.58206620017072
     */
    private static final float[] GRADIENTS_5D = {
            -1.6797903571f, -0.0690921662f, -0.7098031356f, -0.5887570823f, +0.5683970756f, 0f, 0f, 0f,
            -1.0516780588f, -0.2945340815f, -1.4440603796f, +0.7418854274f, -0.4141480030f, 0f, 0f, 0f,
            +1.0641252713f, -1.5650070200f, +0.4156350353f, +0.1130875224f, +0.4825444684f, 0f, 0f, 0f,
            +0.8695556873f, +1.0264500068f, -0.3870691013f, -1.1785230203f, -0.8071767413f, 0f, 0f, 0f,
            +0.4036843343f, +0.2265883553f, -1.6373381485f, +0.7147763885f, +0.7706589242f, 0f, 0f, 0f,
            +0.1852080234f, -0.7234241829f, +1.3347979534f, -0.7398257504f, -1.0551434605f, 0f, 0f, 0f,
            -0.1798280717f, -0.9172834905f, -0.1660562308f, +1.5451496683f, +0.8433212279f, 0f, 0f, 0f,
            -0.5376087193f, +1.4095478895f, -1.2573362952f, +0.1736254636f, -0.3363201621f, 0f, 0f, 0f,
            -0.8831071523f, +0.4890748406f, +0.7809592873f, -0.9126098448f, +1.2402311964f, 0f, 0f, 0f,
            -1.7880012565f, -0.0832774541f, -0.0688806429f, +0.8681275071f, -0.1942330063f, 0f, 0f, 0f,
            +1.1634898551f, -0.5052769528f, -0.7356836999f, -0.2313504020f, +1.3402361893f, 0f, 0f, 0f,
            +0.5846946797f, -1.2424919047f, +0.6407004403f, +1.3053017243f, -0.0060293368f, 0f, 0f, 0f,
            +0.4938778800f, +0.7783935437f, +0.0680362272f, +0.1949810810f, -1.7628220502f, 0f, 0f, 0f,
            +0.3495453088f, +0.3175464510f, -1.2837807206f, -1.4389420883f, +0.2415265131f, 0f, 0f, 0f,
            -0.0814475545f, -0.3645019914f, +1.2414338549f, +0.7877420883f, -1.3033836658f, 0f, 0f, 0f,
            -0.6130443974f, -1.7598572531f, +0.3278510206f, -0.4244846722f, +0.4892908001f, 0f, 0f, 0f,
            -0.4462734487f, +0.7987181596f, -0.3741235663f, +1.6266729545f, -0.6138859559f, 0f, 0f, 0f,
            -1.1190041124f, +0.4387897882f, +1.5187622470f, +0.2310331368f, +0.4419029812f, 0f, 0f, 0f,
            +1.7898523809f, -0.0730765445f, +0.2593137052f, -0.6196725486f, -0.5829670729f, 0f, 0f, 0f,
            +1.2710361476f, -0.7953333027f, -0.5194550615f, +0.9617110332f, +0.7464518582f, 0f, 0f, 0f,
            +0.3919460233f, -1.2475586928f, -1.4706983192f, -0.1307051020f, -0.3315693791f, 0f, 0f, 0f,
            +0.2652336693f, +0.6189864328f, +0.3777315952f, -1.7165368300f, +0.6762596023f, 0f, 0f, 0f,
            +0.1369902659f, +0.5491538637f, -1.0396634959f, +0.9490333448f, -1.3031113639f, 0f, 0f, 0f,
            -0.2401683431f, -0.3733848671f, -1.4613950663f, -0.7227050436f, +1.0700115833f, 0f, 0f, 0f,
            -0.6698938436f, -1.3422700176f, +0.7466878175f, +1.0575187021f, -0.2714128520f, 0f, 0f, 0f,
            -0.8847555645f, +1.1306623120f, -0.1640964357f, -0.1686079479f, +1.3723899034f, 0f, 0f, 0f,
            -1.1982151304f, +0.3128615080f, -0.8450972304f, -1.3226474382f, -0.0530339816f, 0f, 0f, 0f,
            +0.8151064240f, -0.0707387889f, +0.4722986821f, +0.1916252778f, -1.7523730337f, 0f, 0f, 0f,
            +1.2690966076f, -1.1058707966f, -0.0729186016f, -1.0707270924f, +0.1211195821f, 0f, 0f, 0f,
            +0.2853585791f, -1.5643353649f, -0.5748320773f, +0.5808419374f, -0.8964463588f, 0f, 0f, 0f,
            +0.2535726091f, +1.1620185372f, +1.5502829093f, -0.2230925697f, +0.3636845578f, 0f, 0f, 0f,
            -0.1259274379f, +0.1397280645f, +0.0818804260f, -1.6542088566f, -1.1052180794f, 0f, 0f, 0f,
            -0.7748098968f, -0.7541305772f, -1.3684352844f, +0.6640618209f, +0.7192798250f, 0f, 0f, 0f,
            -0.7154067153f, -1.0897763229f, +1.1541033599f, -0.5995215703f, -0.7805127283f, 0f, 0f, 0f,
            -1.2205329558f, +1.1140489716f, +0.2019395367f, +0.9671922075f, +0.5412521130f, 0f, 0f, 0f,
            +1.7763124224f, +0.3884232272f, -0.5590859360f, -0.0997516807f, -0.6093554733f, 0f, 0f, 0f,
            +0.7941439015f, -0.1125633933f, +1.2801756800f, -1.1687349208f, +0.5931895645f, 0f, 0f, 0f,
            +1.0158348693f, -1.2589195605f, +0.5779670539f, +0.6776054453f, -0.7681184828f, 0f, 0f, 0f,
            +0.2112048908f, +1.7680263830f, -0.3219879142f, -0.4419318676f, +0.7283510216f, 0f, 0f, 0f,
            -0.0026910087f, +0.5409839017f, -1.7270071907f, +0.8213951690f, -0.2237974892f, 0f, 0f, 0f,
            -0.4138014120f, +0.1597450584f, +0.6839984196f, -0.0929507291f, +1.8239397555f, 0f, 0f, 0f,
            -0.7659506384f, -0.5475010929f, -0.3720789651f, -1.7162535971f, -0.1720261813f, 0f, 0f, 0f,
            -0.7070622912f, -0.8458704904f, -1.0146426125f, +0.3071423194f, +1.2886931343f, 0f, 0f, 0f,
            -1.6125362501f, +0.9425610444f, +0.5399791622f, -0.4685942374f, +0.0121435146f, 0f, 0f, 0f,
            +1.0263600815f, +0.3094855666f, -0.1357539876f, +0.9416267863f, -1.3948883530f, 0f, 0f, 0f,
            +1.0884856898f, -0.2412950015f, -1.6426714098f, -0.0397577982f, +0.2388002976f, 0f, 0f, 0f,
            +0.3883496101f, -0.7333843774f, +0.7553963021f, -1.1941140952f, -1.1466472386f, 0f, 0f, 0f,
            +0.1101824785f, +1.9193422531f, -0.0349560249f, +0.4586533562f, +0.3039741964f, 0f, 0f, 0f,
            -0.2151896625f, +0.8619434800f, -1.1688233084f, -0.6467741803f, -1.1942705221f, 0f, 0f, 0f,
            -0.5440612093f, +0.1020041479f, +1.1614695684f, +1.4233071754f, +0.5646040033f, 0f, 0f, 0f,
            -1.3903047596f, -0.7781814736f, +0.1518957001f, +0.0172015182f, -1.1992156077f, 0f, 0f, 0f,
            -1.1352909369f, -1.0508611233f, -0.5994729301f, -0.9722493258f, +0.5496988654f, 0f, 0f, 0f,
            +1.3336722136f, +0.8735367803f, +1.0383655970f, +0.4365890905f, -0.4352456471f, 0f, 0f, 0f,
            +1.3114501486f, +0.4918768452f, +0.3084333813f, -0.6495376384f, +1.2333391190f, 0f, 0f, 0f,
            +0.6896294960f, -0.2419287464f, -0.7141267659f, +1.6588951215f, -0.4516321269f, 0f, 0f, 0f,
            +0.2176968344f, -0.7421851123f, +1.5213707725f, +0.0438834617f, +1.0417651183f, 0f, 0f, 0f,
            -0.0434372972f, +1.6845774504f, +0.3229918793f, -1.0108819828f, -0.1846777672f, 0f, 0f, 0f,
            -0.3651204958f, +0.6939929190f, -0.4562428562f, +0.6199070461f, +1.6711129711f, 0f, 0f, 0f,
            -0.5890165438f, +0.0561767268f, -1.8733437161f, -0.3722429586f, -0.0438427600f, 0f, 0f, 0f,
            -0.7545212813f, -0.3365185970f, +0.3380918399f, +0.9776020270f, -1.4991467755f, 0f, 0f, 0f,
            -1.7417773586f, -0.9568393557f, -0.2040755992f, +0.0614347980f, +0.0724499544f, 0f, 0f, 0f,
            +0.8480496705f, +0.7472072627f, -1.0543920416f, -0.7610320599f, -1.0156676077f, 0f, 0f, 0f,
            +1.1550078136f, +0.5368673805f, +1.0697388270f, +1.0270433372f, +0.4225768470f, 0f, 0f, 0f,
            +0.6091830897f, -0.3632960094f, -0.2588786131f, -0.6327424895f, -1.7405547329f, 0f, 0f, 0f,
            +0.0677925852f, -0.7943979716f, -1.0479221567f, +1.4543495597f, +0.3886676471f, 0f, 0f, 0f,
            -0.2061357682f, +1.6481340611f, +0.7904935004f, +0.1201597286f, -0.7757859417f, 0f, 0f, 0f,
            -0.7481241996f, +0.8815306333f, -0.0389302309f, -1.3935543711f, +0.8483540397f, 0f, 0f, 0f,
            -1.1501637940f, +0.0500560844f, -1.1550196052f, +0.8588373495f, -0.7764958172f, 0f, 0f, 0f,
            -1.4576210450f, -0.4980765043f, +0.9775175852f, -0.3244367280f, +0.7526359448f, 0f, 0f, 0f,
            +1.0804925776f, -1.0462781211f, +0.0745691035f, +1.2771082010f, -0.3182325797f, 0f, 0f, 0f,
            +0.9560363853f, +1.0747532707f, -0.7908249620f, +0.1795273343f, +1.1283907359f, 0f, 0f, 0f,
            +0.5870023920f, +0.3518098165f, +1.5130869695f, -1.0689826362f, -0.3154393619f, 0f, 0f, 0f,
            +0.2461487893f, -0.3086153639f, +0.2921558695f, +0.9112883678f, +1.7112468522f, 0f, 0f, 0f,
            -0.1666414465f, -1.6148302394f, -1.0133051505f, -0.5432021594f, -0.2066349729f, 0f, 0f, 0f,
            -0.2082660083f, +0.8616008908f, +0.9278341202f, +1.0618169303f, +1.1072207669f, 0f, 0f, 0f,
            -1.4200071139f, +1.1449937745f, +0.7148016266f, +0.3951739916f, +0.0739270175f, 0f, 0f, 0f,
            -1.0824868745f, +0.0130967819f, -0.3737068064f, -0.7706672311f, -1.4472269630f, 0f, 0f, 0f,
            +1.3772509463f, -0.3564008886f, -1.3081930141f, +0.4995798772f, +0.1233256728f, 0f, 0f, 0f,
            +0.9497908429f, -1.3263097649f, +0.4502084198f, -0.2307263072f, -1.0406140073f, 0f, 0f, 0f,
            +0.4549745216f, +0.6615623933f, -0.1955222409f, +1.8045985192f, +0.2460256534f, 0f, 0f, 0f,
            +0.3671055129f, +0.3148111115f, -1.6182062419f, +0.2769362348f, -1.0348151463f, 0f, 0f, 0f,
            +0.0481966276f, -0.4532364953f, +1.1128663911f, -1.3414977121f, +0.8684273419f, 0f, 0f, 0f,
            -0.3576449008f, -1.2810416482f, -0.2006980071f, +1.1378443353f, -0.9466007601f, 0f, 0f, 0f,
            -0.5489241973f, +1.4436359278f, -1.0580643935f, -0.2111030853f, +0.6712173717f, 0f, 0f, 0f,
            -0.7396913767f, +0.4241285251f, +0.6373931479f, -1.6490546808f, -0.3838232614f, 0f, 0f, 0f,
            -1.7438367476f, -0.0103026532f, -0.0174746056f, +0.2859053214f, +0.9364187303f, 0f, 0f, 0f,
            +1.4125223773f, -0.6136774864f, -0.9382744610f, -0.7882620843f, -0.3556183326f, 0f, 0f, 0f,
            +0.6333525580f, -1.2469837002f, +0.8203449431f, +0.6945417557f, +0.9426251178f, 0f, 0f, 0f,
            +0.8639745852f, +1.7229496217f, +0.2131097409f, -0.3490329851f, -0.3431511780f, 0f, 0f, 0f,
            +0.1160084005f, +0.1925427348f, -0.5469449523f, -1.4198630543f, +1.2784011391f, 0f, 0f, 0f,
            -0.1960368134f, -0.4241632531f, +1.8889399989f, +0.4605830623f, -0.0377362652f, 0f, 0f, 0f,
            -0.3716846054f, -0.8276497199f, +0.2058886823f, -0.5926340109f, -1.6683049107f, 0f, 0f, 0f,
            -0.7995956039f, +1.4545513458f, -0.5567146701f, +0.9584702276f, +0.1277922200f, 0f, 0f, 0f,
            -0.9905083489f, +0.4012227581f, +1.3537558791f, -0.1090892883f, -1.0066568711f, 0f, 0f, 0f,
            +1.4450754379f, -0.0281787255f, +0.3053200605f, -1.3288357283f, +0.2278995524f, 0f, 0f, 0f,
            +1.2162147152f, -0.7478839823f, -0.4936637037f, +0.4427814597f, -1.2335850364f, 0f, 0f, 0f,
            +0.4288156741f, -1.2286191885f, -1.4078773154f, -0.4695345709f, +0.3225379959f, 0f, 0f, 0f,
            +0.3329858839f, +1.0484961431f, +0.6324502386f, +1.2260808594f, -0.9415458889f, 0f, 0f, 0f,
            -0.0430825232f, +0.6204968828f, -0.7408650600f, -0.2917703779f, +1.7260117393f, 0f, 0f, 0f,
            -0.2831108338f, -0.2973701593f, -1.2778575475f, -1.3826667300f, -0.5354736652f, 0f, 0f, 0f,
            -0.7626701307f, -1.2292796278f, +0.8192695846f, +0.4886037879f, +0.9986338441f, 0f, 0f, 0f,
            -1.1212378397f, +1.4564460164f, -0.1452464147f, -0.6418766528f, -0.4341526800f, 0f, 0f, 0f,
            -1.4371859530f, +0.3591868101f, -0.7832229698f, +0.7741764284f, +0.7698662281f, 0f, 0f, 0f,
            +1.6195535741f, -0.0783305926f, +1.1220763529f, -0.0880739971f, -0.3226424776f, 0f, 0f, 0f,
            +0.6736622539f, -0.5801267229f, -0.0064584923f, -1.2469663463f, +1.2863379696f, 0f, 0f, 0f,
            +0.3808337389f, -1.7282317745f, -0.8266342493f, +0.4213073506f, -0.0857702241f, 0f, 0f, 0f,
            +0.0748521918f, +0.5865055185f, +0.7547226638f, -0.3937892986f, +1.7104771601f, 0f, 0f, 0f,
            -0.3050023119f, +0.3332256435f, +0.2039469964f, +1.9348633092f, +0.1031690730f, 0f, 0f, 0f,
            -0.5486929801f, -0.3926995085f, -0.7835797197f, -0.0323895314f, -1.7116298814f, 0f, 0f, 0f,
            -0.7373648248f, -0.9164391411f, +1.1634541527f, -1.1082134698f, +0.1861981626f, 0f, 0f, 0f,
            -1.2396832556f, +1.1286466143f, +0.2193465590f, +0.4244818926f, -0.9803287488f, 0f, 0f, 0f,
            +1.7118249987f, +0.5111342927f, -0.5816150480f, -0.5527569748f, +0.4056853108f, 0f, 0f, 0f,
            +0.7213413610f, -0.0659398302f, +1.4422534178f, +0.9666694057f, -0.6788032989f, 0f, 0f, 0f,
            +0.9873966195f, -1.2334566504f, +0.7110411579f, +0.0172849954f, +0.9988765230f, 0f, 0f, 0f,
            +0.1849030939f, -1.6262998800f, -0.3182014494f, -0.9668115017f, -0.5338379006f, 0f, 0f, 0f,
            -0.0537861903f, +0.7112275325f, -1.6810226484f, +0.4784138168f, +0.6607159134f, 0f, 0f, 0f,
            -0.7517873085f, +0.3686878741f, +1.1316388506f, -0.9931706665f, -1.0158201777f, 0f, 0f, 0f,
            -0.7479636489f, -0.4087729589f, -0.2801205440f, +1.4488805036f, +1.0467725708f, 0f, 0f, 0f,
            -1.0753364436f, -1.0487010364f, -1.2861467341f, +0.0451559898f, -0.2960830697f, 0f, 0f, 0f,
            -1.6717166425f, +0.6193692618f, +0.3444359164f, -0.5570386011f, +0.6267512114f, 0f, 0f, 0f,
            +1.6653427265f, +0.6514011681f, -0.1843800816f, +0.8463999253f, -0.2278624001f, 0f, 0f, 0f,
            +0.6180555713f, -0.0980890088f, -0.9637326948f, -0.3818490941f, +1.5917903189f, 0f, 0f, 0f,
            +0.3828037090f, -0.7608509481f, +0.9360620892f, +1.5486593545f, -0.0030206309f, 0f, 0f, 0f,
            +0.0416485569f, -1.5762523250f, +0.0019777673f, +0.0585731018f, -1.2289260701f, 0f, 0f, 0f,
            -0.2886712161f, +0.9630135494f, -1.0923275687f, -1.3265794576f, +0.1904763974f, 0f, 0f, 0f,
            -0.5764811629f, +0.1590907789f, +1.1606879290f, +0.6689389883f, -1.3592953154f, 0f, 0f, 0f,
            -1.6356922055f, -0.7138956424f, +0.2340692949f, -0.6808182666f, +0.5445751314f, 0f, 0f, 0f,
            -1.1383732794f, -0.8340752557f, -0.4924316867f, +1.1297774686f, -0.6996703867f, 0f, 0f, 0f,
            +1.2119764801f, +1.0042477319f, +1.1627125168f, +0.1052984231f, +0.3995138920f, 0f, 0f, 0f,
            +1.0848959808f, +0.5299382966f, +0.3338775173f, -1.2410743362f, -0.9436240820f, 0f, 0f, 0f,
            +0.8223389027f, -0.2257269798f, -0.8855454083f, +1.1320984930f, +1.0986211320f, 0f, 0f, 0f,
            +0.1696512818f, -0.6844004252f, +1.7720906716f, -0.3171057932f, -0.5118135090f, 0f, 0f, 0f,
            -0.0617271001f, +1.6228010367f, +0.2362036330f, +1.0239074576f, +0.5084564115f, 0f, 0f, 0f,
            -0.8016909939f, +1.4462165555f, -0.7627188444f, +0.3252216885f, -0.7604209640f, 0f, 0f, 0f,
            -0.6115306073f, +0.1014550431f, -1.4858078470f, -0.7519599396f, +0.9179697607f, 0f, 0f, 0f,
            -1.5359735435f, -0.5360812013f, +0.6803716202f, +0.9022898547f, -0.2763506754f, 0f, 0f, 0f,
            +1.4311848509f, -0.8591027804f, -0.1752995920f, -0.2145555860f, +1.0662496372f, 0f, 0f, 0f,
            +0.7410642280f, +0.7990758023f, -0.9368640780f, +1.3900908545f, -0.0472735412f, 0f, 0f, 0f,
            +0.4550755889f, +0.2813149456f, +0.5064435170f, +0.1454080862f, -1.8536827027f, 0f, 0f, 0f,
            +0.6584368336f, -0.3398656764f, -0.2473926483f, -1.8321141033f, +0.1819534238f, 0f, 0f, 0f,
            +0.0159960331f, -0.7374889492f, -1.0065472324f, +0.7388568967f, -1.3772462858f, 0f, 0f, 0f,
            -0.2299702397f, +1.8176358053f, +0.7442497214f, -0.2206381235f, +0.2018042090f, 0f, 0f, 0f,
            -0.4069426745f, +0.4769186078f, +0.0089269758f, +1.7464025964f, -0.7462871978f, 0f, 0f, 0f,
            -1.4305778226f, +0.1421159811f, -1.2165719887f, +0.3471454458f, +0.5767952644f, 0f, 0f, 0f,
            -1.4621197220f, -0.3747993576f, +0.9054068790f, -0.6585117031f, -0.6843479237f, 0f, 0f, 0f,
            +1.2555507001f, -1.2133185727f, +0.1361145959f, +0.7938459453f, +0.5502107892f, 0f, 0f, 0f,
            +0.9623281537f, +1.3224211051f, -0.8148529505f, -0.2708155140f, -0.7666815323f, 0f, 0f, 0f,
            +0.3174348857f, +0.2633414906f, +1.0144165277f, -1.5786067523f, +0.5557393117f, 0f, 0f, 0f,
            +0.4312067006f, -0.5747179681f, +0.8536422312f, +0.8761256911f, -1.4097725891f, 0f, 0f, 0f,
            -0.1886268643f, -1.0208135472f, -0.6506500504f, -0.9477019512f, +1.2652569429f, 0f, 0f, 0f,
            -0.3048749941f, +1.3023137339f, +1.3472498676f, +0.5983791689f, -0.1946544138f, 0f, 0f, 0f,
            -0.9288706884f, +0.7613446467f, +0.4729501186f, -0.2114483296f, +1.5129974760f, 0f, 0f, 0f,
            -1.1557323498f, +0.0638806278f, -0.3210150212f, -1.5950470819f, -0.1139129657f, 0f, 0f, 0f,
            +1.0864354794f, -0.3052283529f, -1.1052395274f, +0.2022026495f, +1.2099806929f, 0f, 0f, 0f,
            +1.0414087896f, -1.4163018217f, +0.5940404283f, -0.7457758569f, +0.0221635650f, 0f, 0f, 0f,
            +0.5070316235f, +0.9137533277f, -0.2073217572f, +0.8288949911f, -1.4757793099f, 0f, 0f, 0f,
            +0.3763094088f, +0.4850535903f, -1.8754774606f, -0.2080484396f, +0.2498287114f, 0f, 0f, 0f,
            -0.0253081105f, -0.1921838222f, +0.6575303806f, -1.5122491502f, -1.1149803515f, 0f, 0f, 0f,
            -0.6196419069f, -1.6338762858f, -0.2048715266f, +0.7010005938f, +0.6427425729f, 0f, 0f, 0f,
            -0.5308926042f, +1.4556534130f, -0.8522869910f, -0.5344412052f, -0.7662934602f, 0f, 0f, 0f,
            -1.1271692683f, +0.6619484351f, +0.9450688957f, +1.0599681920f, +0.5240476355f, 0f, 0f, 0f,
            -1.8934489402f, +0.0438491543f, +0.0205347023f, -0.0947675875f, -0.6352368005f, 0f, 0f, 0f,
            +0.5103230547f, +1.3058156973f, +0.1990338991f, -0.7882347287f, +1.1719587297f, 0f, 0f, 0f,
            +0.1384792574f, +0.4610276778f, -0.9727270207f, +1.5951805055f, -0.5267620653f, 0f, 0f, 0f,
            -0.2073797520f, -0.2507461010f, +1.5291534160f, -0.0725161583f, +1.2452113349f, 0f, 0f, 0f,
            -0.5725773198f, -1.0055906561f, +0.3247380428f, -1.5826348743f, -0.2252880459f, 0f, 0f, 0f,
            -0.6862103326f, +1.2996571076f, -0.3961010577f, +0.3505477796f, +1.2490904645f, 0f, 0f, 0f,
            -1.0941521107f, +0.4477460716f, +1.5583661596f, -0.4156823874f, -0.0325219850f, 0f, 0f, 0f,
            +1.0615422136f, +0.0168716535f, +0.2909809882f, +0.7952955764f, -1.4682229009f, 0f, 0f, 0f,
            +0.3529574716f, -0.9860437746f, -1.1438219776f, -0.8624789958f, -0.9224640715f, 0f, 0f, 0f,
            +0.3425330274f, +1.5160688884f, +0.9006480000f, +0.7732736314f, +0.4184343698f, 0f, 0f, 0f,
            -0.1182208812f, +0.4689801454f, -0.3711656837f, -0.8412805777f, -1.7089659070f, 0f, 0f, 0f,
            -0.3895150255f, -0.2763904657f, -1.3594381746f, +1.3110052175f, +0.4528570686f, 0f, 0f, 0f,
            -0.8866701020f, -1.1592070785f, +0.9217069399f, +0.0108062128f, -1.0101458419f, 0f, 0f, 0f,
            -0.9839606799f, +1.3163966058f, -0.0810864936f, -1.0154752113f, +0.5110346685f, 0f, 0f, 0f,
            +1.7393575679f, +0.3972242300f, -0.7097572208f, +0.3707578686f, -0.4190840636f, 0f, 0f, 0f,
            +1.2992926783f, -0.0003032116f, +1.0675928831f, -0.5467297666f, +0.9344358954f, 0f, 0f, 0f,
            +0.3309152609f, -1.5010777228f, -0.7884782610f, +0.0452028175f, +1.0067370548f, 0f, 0f, 0f,
            +0.0527154815f, +0.9848513540f, +1.2271602344f, -1.2005994995f, -0.2827145013f, 0f, 0f, 0f,
            -1.1072848983f, -0.5733937749f, -1.2917946615f, -0.8540935843f, -0.2166343341f, 0f, 0f, 0f,
            -0.5785672345f, -0.5892745270f, +0.9002794950f, +0.8827318293f, +1.3146470384f, 0f, 0f, 0f,
            +1.1323242306f, +0.4385085158f, -0.3984529066f, -0.8482583731f, -1.2834504790f, 0f, 0f, 0f,
            +0.6832479100f, -0.0203722774f, +1.8021714033f, +0.5087858832f, +0.1614695700f, 0f, 0f, 0f,
            +0.6295136760f, -0.7957220411f, +0.5735752524f, -0.5094408070f, -1.5433795577f, 0f, 0f, 0f,
            +0.1464145243f, -1.4152600929f, -0.2997028564f, +1.3388224398f, +0.3055066758f, 0f, 0f, 0f,
            -0.1117532528f, +0.8429678828f, -1.5895178521f, +0.1184502189f, -0.8580902647f, 0f, 0f, 0f,
            -0.6186591707f, +0.3491930628f, +0.8652060160f, -1.4602096806f, +0.7839204512f, 0f, 0f, 0f,
            -1.1893740310f, -0.4873888685f, -0.3368700002f, +1.0489488764f, -1.0649255199f, 0f, 0f, 0f,
            -1.1495757072f, -0.9135567011f, -1.1488759605f, -0.3139079113f, +0.6522543198f, 0f, 0f, 0f,
            +1.2507068251f, +0.9082986588f, +0.4849121115f, +1.1269927255f, -0.3247670313f, 0f, 0f, 0f,
            +1.3812528182f, +0.6859061245f, -0.1144675881f, +0.2171605156f, +1.2495646931f, 0f, 0f, 0f,
            +0.8074888914f, -0.0650160121f, -1.3097078722f, -1.2004749134f, -0.4327353465f, 0f, 0f, 0f,
            +0.3228920807f, -0.6888576407f, +1.0170445092f, +0.7876568168f, +1.3290722555f, 0f, 0f, 0f,
            +0.0052441537f, -1.9617941884f, +0.0477654540f, -0.3352049620f, -0.1915519670f, 0f, 0f, 0f,
            -0.2001799378f, +0.5900368361f, -0.5616998042f, +1.3787410489f, +1.1812497512f, 0f, 0f, 0f,
            -0.9015314851f, +0.3110012919f, +1.7320694688f, +0.2992817832f, -0.0297480605f, 0f, 0f, 0f,
            -0.8591940915f, -0.2863601066f, +0.1461357370f, -0.7274398339f, -1.6214990092f, 0f, 0f, 0f,
            +0.9395832683f, +0.9730323926f, +1.0982291200f, -0.1930711401f, -0.9628123284f, 0f, 0f, 0f,
            +0.5731182373f, +0.3581009267f, +0.2258645391f, +1.8565151569f, +0.2136255940f, 0f, 0f, 0f,
            +0.7011674479f, -0.1226870736f, -0.7909781480f, +0.3959247471f, -1.6464839070f, 0f, 0f, 0f,
            +0.0954972405f, -0.5011463729f, +1.8032529962f, -0.6086202714f, +0.3429177553f, 0f, 0f, 0f,
            -0.1412424735f, -1.6893856796f, +0.3886472390f, +0.7238267164f, -0.6716061327f, 0f, 0f, 0f,
            -0.7766531806f, +1.3490341636f, -0.5674058616f, -0.3739667103f, +1.0559906015f, 0f, 0f, 0f,
            -1.5471155376f, -0.4117408550f, +0.6692645122f, +0.3161027907f, +0.9429035051f, 0f, 0f, 0f,
            +1.5334460025f, -1.0006420984f, -0.1888257316f, -0.6902112872f, -0.3677118033f, 0f, 0f, 0f,
            +0.7809057187f, +1.0330833001f, -1.0077018800f, +0.7218704992f, +0.8867722690f, 0f, 0f, 0f,
            +1.0334456008f, +0.8361364463f, +1.3880171764f, -0.3382417163f, -0.4380261325f, 0f, 0f, 0f,
            -0.0634231536f, -1.1102290519f, -1.5755978089f, +0.5124396730f, -0.1351520699f, 0f, 0f, 0f,
            -0.1846156117f, -1.2027985685f, +0.5261837867f, -0.3886987023f, +1.4461108606f, 0f, 0f, 0f,
            -1.4795808324f, -0.2528893855f, +0.7657021415f, -1.0677045314f, +0.1435088265f, 0f, 0f, 0f,
            +0.8358974012f, +1.4130062170f, -0.7246852387f, -0.7614331388f, +0.4469226390f, 0f, 0f, 0f,
            +0.3586931337f, +0.4076326318f, +1.4558997393f, +0.9580949406f, -0.8170586927f, 0f, 0f, 0f,
            +0.2457835444f, -0.3744186486f, +0.9525361175f, -0.3232545651f, +1.6696055091f, 0f, 0f, 0f,
            -0.2213847655f, -0.7780999043f, -0.5024501129f, -1.6139364700f, -0.6987862901f, 0f, 0f, 0f,
            -0.2574375805f, -1.3890644186f, +1.3509472519f, +0.2010518329f, +0.3724857264f, 0f, 0f, 0f,
            -1.2190443421f, +1.0117162629f, +0.6237377737f, -0.8273041068f, -0.6456626053f, 0f, 0f, 0f,
            -1.4202182379f, +0.1260515345f, -0.3099756452f, +1.0152805943f, +0.9166305590f, 0f, 0f, 0f,
            +1.3394545490f, -0.3743458036f, -1.4096086888f, -0.0615786809f, -0.2737483172f, 0f, 0f, 0f,
            +0.7171369574f, -0.9616513483f, +0.4897305876f, -1.1232009395f, +1.0293322446f, 0f, 0f, 0f,
            +0.1779667703f, +0.3504282910f, -1.0568440088f, -0.4869239513f, +1.5784529288f, 0f, 0f, 0f,
            -0.1204364412f, -0.2136700341f, +1.1047461721f, +1.6490450828f, +0.0051371575f, 0f, 0f, 0f,
            -0.3871281276f, -0.7735057325f, -0.0665288715f, -0.0311266269f, -1.8017840428f, 0f, 0f, 0f,
            -1.1000913946f, +0.6549589413f, +0.8947793392f, +0.4521499773f, -1.1643702335f, 0f, 0f, 0f,
            +1.9292603742f, +0.0932676759f, +0.0543343169f, -0.4404212957f, +0.2689468598f, 0f, 0f, 0f,
            +1.0366997829f, -0.4235317472f, -0.7352650119f, +1.1718961062f, -0.9120961013f, 0f, 0f, 0f,
            +0.3604156986f, +1.2092120205f, +0.2110514542f, -1.3105326841f, -0.8036592445f, 0f, 0f, 0f,
            +0.0668638830f, +0.6759071640f, -1.0954065614f, +0.9579351192f, +1.1921088455f, 0f, 0f, 0f,
            -0.2878226515f, -0.1988228335f, +1.7896272052f, -0.5362533838f, -0.6223297975f, 0f, 0f, 0f,
            -0.5998366869f, -0.8396322334f, +0.3455361034f, +1.4029135129f, +0.9206802585f, 0f, 0f, 0f,
            -0.8343248750f, +1.7431140670f, -0.3975040210f, +0.0398856933f, -0.3253537111f, 0f, 0f, 0f,
            +1.7378988722f, +0.1069270956f, +0.5947543807f, +0.7345800753f, -0.2737397409f, 0f, 0f, 0f,
            +0.3669190706f, -1.0350628357f, -1.2227443172f, +1.1386733496f, -0.0483183134f, 0f, 0f, 0f,
            -0.4256376886f, -0.1980842267f, -1.1814821082f, +0.6186059704f, -1.4145748049f, 0f, 0f, 0f,
            -1.0694433220f, -1.1388959357f, +1.0788632536f, -0.5297257984f, +0.3386025507f, 0f, 0f, 0f,
            -0.8783535738f, +1.2475299432f, -0.0376993977f, +1.0653029541f, -0.7320330673f, 0f, 0f, 0f,
            +1.6644650041f, +0.5820689456f, -0.8613458094f, +0.1111061909f, +0.3694466184f, 0f, 0f, 0f,
            +1.0607200718f, +0.0620356569f, +1.0296431488f, -1.0302379349f, -0.8657189441f, 0f, 0f, 0f,
            -0.8817724023f, +0.9515735227f, +0.6010913410f, +0.4766382991f, -1.3147206521f, 0f, 0f, 0f,
            -0.7611137823f, -0.2756268185f, -0.7300242585f, -1.1275552035f, +1.2411363795f, 0f, 0f, 0f,
            -1.3207783071f, +1.1561698454f, +0.2299470218f, -0.2072522588f, +0.9071862105f, 0f, 0f, 0f,
            -1.1816771520f, -0.7596862015f, -0.9827823279f, -0.6774291571f, -0.7757219970f, 0f, 0f, 0f,
            +1.2474994489f, +1.2266679741f, +0.6167132624f, +0.6372268146f, +0.3906885524f, 0f, 0f, 0f,
            +1.4101961346f, +0.8763908320f, -0.0679690545f, -0.3381071150f, -1.0603536005f, 0f, 0f, 0f,
            +0.4303889934f, +0.0075456308f, -0.7318402639f, -1.7280562703f, +0.5412390715f, 0f, 0f, 0f,
            -1.0150772094f, -0.2501828730f, +0.1938295376f, -1.6850991645f, -0.1729095290f, 0f, 0f, 0f,
            -0.2491682380f, -1.8343103261f, +0.5570892947f, +0.4096496582f, +0.3083171940f, 0f, 0f, 0f,
            +0.6707055360f, +0.7050912787f, +1.0951484850f, -0.8144527819f, +1.0910164227f, 0f, 0f, 0f,
            -0.1253944377f, -0.8069577491f, -1.1981624979f, -0.0909347438f, +1.3744936985f, 0f, 0f, 0f,
            +0.4979431688f, +1.0477297741f, -0.4424841168f, -0.9992478515f, -1.2083155460f, 0f, 0f, 0f,
            +0.3391283580f, +0.5297397571f, +1.8127693422f, +0.5200000016f, +0.2187122697f, 0f, 0f, 0f,
            +0.1729941911f, +0.5513060812f, -1.3295779972f, -1.3236932093f, -0.3823522614f, 0f, 0f, 0f,
            -0.1759985101f, -0.1116624120f, +1.0347327507f, +0.7188695866f, +1.5391915677f, 0f, 0f, 0f,
            +1.3834109634f, -0.5319875518f, -1.0053750542f, +0.8686683761f, +0.1944212023f, 0f, 0f, 0f,
            +0.2655537132f, +1.2074447952f, +0.2300093933f, +1.5279397437f, +0.2899208694f, 0f, 0f, 0f,
            -0.7650007456f, -1.7462692514f, -0.2985746155f, -0.2497276182f, +0.4623925569f, 0f, 0f, 0f,
            +1.5421515027f, +0.1809242613f, +0.6454387145f, +0.2020302919f, +1.0637799497f, 0f, 0f, 0f,
    };

    private final float[] GRADIENTS_5D_HALTON = new float[POINT_COUNT<<3];
    private final float[] GRADIENTS_5D_R5 = new float[POINT_COUNT<<3];
    private final float[] GRADIENTS_5D_ACE = new float[POINT_COUNT<<3];
    private final float[] GRADIENTS_5D_GOLDEN = new float[POINT_COUNT<<3];
    private final float[] GRADIENTS_5D_VDC = new float[POINT_COUNT<<3];

    private final float[] GRADIENTS_5D_TEMP = new float[POINT_COUNT<<3];

    private void roll(final EnhancedRandom random, final float[] gradients5D) {
        for (int i = 0; i < POINT_COUNT; i++) {
            float[] rot = RotationTools.randomRotation5D(random);
            RotationTools.rotate(SphereVisualizer.pole5, rot, gradients5D, i << 3);
            RotationTools.rotate(SphereVisualizer.reversePole5, rot, gradients5D, ++i << 3);
        }
    }

    private void roll(long seed, final float[] gradients5D) {
        for (int i = 0; i < POINT_COUNT; i++) {
            float[] rot = RotationTools.randomRotation5D(seed + i);
            RotationTools.rotate(SphereVisualizer.pole5, rot, gradients5D, i << 3);
            RotationTools.rotate(SphereVisualizer.reversePole5, rot, gradients5D, ++i << 3);
        }
    }

    private float evaluateMinDistance2(final float[] gradients5D) {
        float minDist2 = Float.MAX_VALUE;
        for (int i = 0; i < gradients5D.length; i += 8) {
            float xi = gradients5D[i  ], yi = gradients5D[i+1], zi = gradients5D[i+2], wi = gradients5D[i+3], ui = gradients5D[i+4];
            for (int j = i + 1; j < gradients5D.length; j += 8) {
                float x = xi - gradients5D[j  ], y = yi - gradients5D[j+1], z = zi - gradients5D[j+2],
                        w = wi - gradients5D[j+3], u = ui - gradients5D[j+4];
                minDist2 = Math.min(minDist2, x * x + y * y + z * z + w * w + u * u);
            }
        }
        return minDist2;
    }
    private void printMinDistance(final String name, final float[] gradients5D) {
        System.out.printf("%s:  Min distance %.8f\n", name, Math.sqrt(evaluateMinDistance2(gradients5D)));
    }

    private double evaluateDeviation(final float[] gradients5D) {
        double x = 0, y = 0, z = 0, w = 0, u = 0;
        for (int i = 0; i < gradients5D.length; i += 8) {
            x += gradients5D[i  ];
            y += gradients5D[i+1];
            z += gradients5D[i+2];
            w += gradients5D[i+3];
            u += gradients5D[i+4];
        }
        return Math.sqrt((x * x + y * y + z * z + w * w + u * u) / 5.0); // RMS Error
    }

    private void printDeviation(final String name, final float[] gradients5D) {
        double x = 0, y = 0, z = 0, w = 0, u = 0;
        for (int i = 0; i < gradients5D.length; i += 8) {
            x += gradients5D[i  ];
            y += gradients5D[i+1];
            z += gradients5D[i+2];
            w += gradients5D[i+3];
            u += gradients5D[i+4];
        }
        double rmsError = Math.sqrt((x * x + y * y + z * z + w * w + u * u) / 5.0); // Root Mean Squared Error
        System.out.printf("%s:  x: %.8f, y: %.8f, z: %.8f, w: %.8f, u: %.8f, rms error: %.8f\n",
                name, x, y, z, w, u, rmsError);
    }

    {
        for (int i = 0; i < GRADIENTS_5D.length; i++) {
            GRADIENTS_5D[i] *= 0.5f;
        }
        for (int i = 1; i <= POINT_COUNT; i++) {
            float x = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(3, i));
            float y = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(5, i));
            float z = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(7, i));
            float w = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(11, i));
            float u = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(2, i));

//            float r = (float) Math.pow(QuasiRandomTools.vanDerCorput(2, i), 0.2);
            final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z + w * w + u * u);
            int index = i - 1 << 3;
            GRADIENTS_5D_HALTON[index + 0] = x * mag;
            GRADIENTS_5D_HALTON[index + 1] = y * mag;
            GRADIENTS_5D_HALTON[index + 2] = z * mag;
            GRADIENTS_5D_HALTON[index + 3] = w * mag;
            GRADIENTS_5D_HALTON[index + 4] = u * mag;
        }

        for (int i = 1; i <= POINT_COUNT; i++) {
            float x = (float) MathTools.probit((QuasiRandomTools.goldenLong[4][0] * i >>> 12) * 0x1p-52);
            float y = (float) MathTools.probit((QuasiRandomTools.goldenLong[4][1] * i >>> 12) * 0x1p-52);
            float z = (float) MathTools.probit((QuasiRandomTools.goldenLong[4][2] * i >>> 12) * 0x1p-52);
            float w = (float) MathTools.probit((QuasiRandomTools.goldenLong[4][3] * i >>> 12) * 0x1p-52);
            float u = (float) MathTools.probit((QuasiRandomTools.goldenLong[4][4] * i >>> 12) * 0x1p-52);

//            float r = (float) Math.pow((QuasiRandomTools.goldenLong[5][5] * i >>> 12) * 0x1p-52, 0.2);
            final float mag = 1f / (float)Math.sqrt(x * x + y * y + z * z + w * w + u * u);
            int index = i - 1 << 3;
            GRADIENTS_5D_R5[index + 0] = x * mag;
            GRADIENTS_5D_R5[index + 1] = y * mag;
            GRADIENTS_5D_R5[index + 2] = z * mag;
            GRADIENTS_5D_R5[index + 3] = w * mag;
            GRADIENTS_5D_R5[index + 4] = u * mag;
        }

        EnhancedRandom random = new AceRandom(0x2D332D421055FD30L);
        roll(random, GRADIENTS_5D_ACE);

        random = new GoldenQuasiRandom(-1234567890L);
        roll(random, GRADIENTS_5D_GOLDEN);

        random = new VanDerCorputQuasiRandom(1L);
        roll(random, GRADIENTS_5D_VDC);

        printMinDistance("Noise", GRADIENTS_5D);
        printMinDistance("Halton", GRADIENTS_5D_HALTON);
        printMinDistance("R5", GRADIENTS_5D_R5);
        printMinDistance("Ace", GRADIENTS_5D_ACE);
        printMinDistance("Golden", GRADIENTS_5D_GOLDEN);
        printMinDistance("VDC", GRADIENTS_5D_VDC);
    }
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Visualizer for Math Testing/Checking");
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(0);
        config.setWindowedMode(512, 530);
        new Lwjgl3Application(new SphereVisualizer(), config);
    }

}
