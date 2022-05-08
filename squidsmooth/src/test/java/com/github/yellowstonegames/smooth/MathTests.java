package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.MathUtils;
import com.github.yellowstonegames.core.TrigTools;

import static com.github.yellowstonegames.core.MathTools.truncate;

public class MathTests {
    public static void main(String[] args){
        System.out.printf("Math.cos(Math.toRadians(90f)): %+1.9g, Math.sin(Math.toRadians(90f)): %+1.9g\n", Math.cos(Math.toRadians(90f)), Math.sin(Math.toRadians(90f)));
        System.out.printf("Math.cos(Math.toRadians(90.0)): %+1.9g, Math.sin(Math.toRadians(90.0)): %+1.9g\n", Math.cos(Math.toRadians(90.0)), Math.sin(Math.toRadians(90.0)));
        System.out.printf("truncate(Math.cos(Math.toRadians(90.0))): %+1.9g, truncate(Math.sin(Math.toRadians(90.0))): %+1.9g\n", truncate(Math.cos(Math.toRadians(90.0))), truncate(Math.sin(Math.toRadians(90.0))));
        System.out.printf("MathUtils.cosDeg(90f): %+1.9g, MathUtils.sinDeg(90f): %+1.9g\n", MathUtils.cosDeg(90f), MathUtils.sinDeg(90f));
        System.out.printf("truncate(MathUtils.cosDeg(90f)): %+1.9g, truncate(MathUtils.sinDeg(90f)): %+1.9g\n", truncate(MathUtils.cosDeg(90f)), truncate(MathUtils.sinDeg(90f)));
        System.out.printf("TrigTools.cosDegrees(90f): %+1.9g, TrigTools.sinDegrees(90f): %+1.9g\n", TrigTools.cosDeg(90f), TrigTools.sinDeg(90f));
        System.out.printf("TrigTools.cosDegrees(90.0): %+1.9g, TrigTools.sinDegrees(90.0): %+1.9g\n", TrigTools.cosDeg(90.0), TrigTools.sinDeg(90.0));
        System.out.println();
        System.out.printf("Math.cos(Math.toRadians(30f)): %+1.9g, Math.sin(Math.toRadians(30f)): %+1.9g\n", Math.cos(Math.toRadians(30f)), Math.sin(Math.toRadians(30f)));
        System.out.printf("Math.cos(Math.toRadians(30.0)): %+1.9g, Math.sin(Math.toRadians(30.0)): %+1.9g\n", Math.cos(Math.toRadians(30.0)), Math.sin(Math.toRadians(30.0)));
        System.out.printf("truncate(Math.cos(Math.toRadians(30.0))): %+1.9g, truncate(Math.sin(Math.toRadians(30.0))): %+1.9g\n", truncate(Math.cos(Math.toRadians(30.0))), truncate(Math.sin(Math.toRadians(30.0))));
        System.out.printf("MathUtils.cosDeg(30f): %+1.9g, MathUtils.sinDeg(30f): %+1.9g\n", MathUtils.cosDeg(30f), MathUtils.sinDeg(30f));
        System.out.printf("truncate(MathUtils.cosDeg(30f)): %+1.9g, truncate(MathUtils.sinDeg(30f)): %+1.9g\n", truncate(MathUtils.cosDeg(30f)), truncate(MathUtils.sinDeg(30f)));
        System.out.printf("TrigTools.cosDegrees(30f): %+1.9g, TrigTools.sinDegrees(30f): %+1.9g\n", TrigTools.cosDeg(30f), TrigTools.sinDeg(30f));
        System.out.printf("TrigTools.cosDegrees(30.0): %+1.9g, TrigTools.sinDegrees(30.0): %+1.9g\n", TrigTools.cosDeg(30.0), TrigTools.sinDeg(30.0));
    }
}
