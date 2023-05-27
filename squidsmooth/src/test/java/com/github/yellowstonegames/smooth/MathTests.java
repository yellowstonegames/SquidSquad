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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.digital.TrigTools;

import static com.github.tommyettinger.digital.MathTools.truncate;

public class MathTests {
    public static void main(String[] args){
        System.out.printf("Math.cos(Math.toRadians(90f)): %+1.9g, Math.sin(Math.toRadians(90f)): %+1.9g\n", Math.cos(Math.toRadians(90f)), Math.sin(Math.toRadians(90f)));
        System.out.printf("Math.cos(Math.toRadians(90.0)): %+1.9g, Math.sin(Math.toRadians(90.0)): %+1.9g\n", Math.cos(Math.toRadians(90.0)), Math.sin(Math.toRadians(90.0)));
        System.out.printf("truncate(Math.cos(Math.toRadians(90.0))): %+1.9g, truncate(Math.sin(Math.toRadians(90.0))): %+1.9g\n", truncate(Math.cos(Math.toRadians(90.0))), truncate(Math.sin(Math.toRadians(90.0))));
        System.out.printf("MathUtils.cosDeg(90f): %+1.9g, MathUtils.sinDeg(90f): %+1.9g\n", MathUtils.cosDeg(90f), MathUtils.sinDeg(90f));
        System.out.printf("truncate(MathUtils.cosDeg(90f)): %+1.9g, truncate(MathUtils.sinDeg(90f)): %+1.9g\n", truncate(MathUtils.cosDeg(90f)), truncate(MathUtils.sinDeg(90f)));
        System.out.printf("TrigTools.cosDegrees(90f): %+1.9g, TrigTools.sinDegrees(90f): %+1.9g\n", TrigTools.cosDeg(90f), TrigTools.sinDeg(90f));
        System.out.println();
        System.out.printf("Math.cos(Math.toRadians(30f)): %+1.9g, Math.sin(Math.toRadians(30f)): %+1.9g\n", Math.cos(Math.toRadians(30f)), Math.sin(Math.toRadians(30f)));
        System.out.printf("Math.cos(Math.toRadians(30.0)): %+1.9g, Math.sin(Math.toRadians(30.0)): %+1.9g\n", Math.cos(Math.toRadians(30.0)), Math.sin(Math.toRadians(30.0)));
        System.out.printf("truncate(Math.cos(Math.toRadians(30.0))): %+1.9g, truncate(Math.sin(Math.toRadians(30.0))): %+1.9g\n", truncate(Math.cos(Math.toRadians(30.0))), truncate(Math.sin(Math.toRadians(30.0))));
        System.out.printf("MathUtils.cosDeg(30f): %+1.9g, MathUtils.sinDeg(30f): %+1.9g\n", MathUtils.cosDeg(30f), MathUtils.sinDeg(30f));
        System.out.printf("truncate(MathUtils.cosDeg(30f)): %+1.9g, truncate(MathUtils.sinDeg(30f)): %+1.9g\n", truncate(MathUtils.cosDeg(30f)), truncate(MathUtils.sinDeg(30f)));
        System.out.printf("TrigTools.cosDegrees(30f): %+1.9g, TrigTools.sinDegrees(30f): %+1.9g\n", TrigTools.cosDeg(30f), TrigTools.sinDeg(30f));
    }
}
