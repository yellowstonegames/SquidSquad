/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

/**
 * Added to SquidLib by Tommy Ettinger on 7/4/2018, using MIT-licensed work by Justin Kunimune from
 * <a href="https://github.com/jkunimune15/Map-Projections/blob/9f820aba788ba0b37a1c67128a4c861d243b4a46/src/utils/NumericalAnalysis.java">his Map-Projections repo</a>.
 * There have been some small changes made. Copied to SquidSquad with the rest of SquidWorld.
 * @author jkunimune
 * @author <a href="https://github.com/tommyettinger">Tommy Ettinger</a>
 */
public final class ProjectionTools {

    private ProjectionTools(){}

    /**
     * Performs a definite integral using Simpson's rule and a constant step size; hard-coded to integrate a
     * hyperellipse function.
     * @param a The start of the integration region
     * @param b The end of the integration region (must be greater than a)
     * @param h The step size (must be positive)
     * @param kappa the kappa value of the hyperellipse
     * @return some magic stuff needed for Tobler Hyperelliptical maps
     */
    public static float simpsonIntegrateHyperellipse(double a, double b, double h, double kappa) {
        double sum = 0, ik = 1/kappa;
        for (double x = a; x < b; x += h) {
            if (x+h > b) h = b-x;
            sum += h/6*(Math.pow(1 - Math.pow(Math.abs(x), kappa), ik) 
                    + 4*Math.pow(1 - Math.pow(Math.abs(x + h * 0.5), kappa), ik) 
                    + Math.pow(1 - Math.pow(Math.abs(x + h), kappa), ik));
        }
        return (float) sum;
    }

    /**
     * Solves a simple ODE using Simpson's rule and a constant step size; hard-coded to solve a hyperelliptical map
     * projection task. Modifies the float array parameter {@code y} in-place.
     *
     * @param maximum The maximum time value at which to sample (must be positive)
     * @param y       the double array to fill with samples; must not be null and must have length 1 or greater
     * @param h       The internal step size (must be positive)
     * @param alpha   part of the hyperelliptical projection's parameters
     * @param kappa   part of the hyperelliptical projection's parameters
     * @param epsilon calculated beforehand using {@link #simpsonIntegrateHyperellipse(double, double, double, double)}
     */
    public static void simpsonODESolveHyperellipse(final double maximum, final float[] y, final double h, final double alpha, final double kappa, final double epsilon)
    {
        final int n = y.length;
        double t = 0;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            while (t < i * maximum / n) {
                final double tph = Math.min(t + h, i * maximum / n);
                sum += (tph - t) / 6.0 * (Math.abs((alpha + (1-alpha)*Math.pow(1 - Math.pow(Math.abs(t), kappa), 1.0/kappa)) / (alpha + (1-alpha)*epsilon))
                        + 4.0 * Math.abs((alpha + (1-alpha)*Math.pow(1 - Math.pow(Math.abs((t + tph) * 0.5), kappa), 1.0/kappa)) / (alpha + (1-alpha)*epsilon))
                        + Math.abs((alpha + (1-alpha)*Math.pow(1 - Math.pow(Math.abs(tph), kappa), 1.0/kappa)) / (alpha + (1-alpha)*epsilon)));
                t = tph;
            }
            y[i] = (float) sum;
        }
    }

    /**
     * Part of computing a hyperellipse; takes only a y parameter corresponding to the y on a map and a kappa parameter
     * used by Tobler's hyperelliptical projection to determine shape.
     * @param y y on a map, usually -1.0 to 1.0
     * @param kappa one of the Tobler parameters
     * @return I'm guessing the actual y used after hyperelliptical distortion; not sure
     */
    public static float hyperellipse(double y, double kappa) {
        return (float) Math.pow(1.0 - Math.pow(Math.abs(y),kappa), 1.0/kappa);
    }

}