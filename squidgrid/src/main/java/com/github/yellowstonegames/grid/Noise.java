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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.DigitTools;

import static com.github.tommyettinger.digital.MathTools.fastFloor;
import static com.github.tommyettinger.digital.TrigTools.*;
import static com.github.yellowstonegames.grid.GradientVectors.*;
import static com.github.yellowstonegames.grid.IntPointHash.*;

/**
 * A wide range of noise functions that can all be called from one configurable object. Originally from Jordan Peck's
 * FastNoise library (these functions are sometimes, but not always, very fast for noise that doesn't use the GPU). This
 * This also allows a lot of configuration, and the API is large. Some key parts to keep in mind:
 * <ul>
 *     <li>The noise type, set with {@link #setNoiseType(int)}, controls what algorithm this uses to generate noise, and
 *     affects most of the other options. Choose a "_FRACTAL" noise type like {@link #SIMPLEX_FRACTAL} (the default) if
 *     you want to use any of the fractal options, like octaves, lacunarity, gain, or fractal type.</li>
 *     <li>The frequency, set with {@link #setFrequency(float)}, affects how quickly significant changes in output can
 *     occur over a given span of input values. It defaults to {@code 1f/32f}, though you should try setting this to
 *     {@code 1f} if results look strange.</li>
 *     <li>If your noise type is one of the fractal varieties ({@link #VALUE_FRACTAL}, {@link #PERLIN_FRACTAL},
 *     {@link #SIMPLEX_FRACTAL}, {@link #CUBIC_FRACTAL}, {@link #FOAM_FRACTAL}, {@link #HONEY_FRACTAL},
 *     {@link #MUTANT_FRACTAL}, or {@link #TAFFY_FRACTAL}):
 *     <ul>
 *         <li>Fractal noise can set a fractal type with {@link #setFractalType(int)}, which defaults to {@link #FBM}
 *         (layering noise with different frequencies and strengths), and can also be set to {@link #RIDGED_MULTI}
 *         (which produces strong lines or curves of high values) or {@link #BILLOW} (which is like RIDGED_MULTI but
 *         produces lines or curves of low values). The noise type affects how the other fractal options work, and has a
 *         very strong effect on the appearance of the noise when it changes.</li>
 *         <li>Octaves, set with {@link #setFractalOctaves(int)}, are how many "layers" of noise this will calculate on
 *         each call to get fractal noise. Each octave has its frequency changed based on lacunarity (set with
 *         {@link #setFractalLacunarity(float)}), and contributes a different amount to the resulting value, based on
 *         gain (set with {@link #setFractalGain(float)}). Generally, more octaves result in more detail and slower
 *         generation times. {@link #SIMPLEX_FRACTAL} and {@link #PERLIN_FRACTAL} only really look like noise when they
 *         use more than one octave.</li>
 *         <li>Lacunarity may occasionally need adjustment, but usually you're fine with setting it to 2.0 or 0.5, with
 *         the appearance informing the decision. I think lacunarity means something related to the width of a crescent,
 *         and refers to the exponential shape of a graph of frequency as octaves are added. It defaults to 2.0, and
 *         some usage works better by setting it to 0.5 (rarely).</li>
 *         <li>Gain usually only needs changing if lacunarity is changed, but they can be adjusted independently. You
 *         probably will get the best results if gain is equal to {@code 1f / lacunarity}, or close to that. This means
 *         the default is 0.5, meant to match a lacunarity of 2.0. If you change lacunarity to 0.5, then this should be
 *         2.0. You can move gain out-of-sync with lacunarity, but this can have strange outcomes.</li>
 *     </ul>
 *     </li>
 *     <li>In some cases, you may want unusual or symmetrical artifacts in noise; you can make this happen with
 *     {@link #setPointHash(IPointHash)}, giving it a {@link FlawedPointHash} or an IPointHash you made, and setting
 *     noise type to {@link #CUBIC_FRACTAL} (or {@link #CUBIC}). The point hash is only used by cubic noise.</li>
 *     <li>The {@link #CELLULAR} noise type has lots of extra configuration, and not all of it is well-documented, but
 *     experimenting with settings like {@link #setCellularReturnType(int)} and
 *     {@link #setCellularDistanceFunction(int)} is a good way to see if it can do what you want.</li>
 *     <li>The {@link #setMutation(float)} method allows you to configure {@link #MUTANT}, {@link #MUTANT_FRACTAL},
 *     {@link #TAFFY}, and {@link #TAFFY_FRACTAL} noise in a way that's more precise than changing a seed. Small changes
 *     to mutation cause small changes in the resulting noise, while large changes are similar to editing the seed.
 *     Mutation acts like any other positional component, like x or y, and as such is affected by the frequency. The two
 *     MUTANT types of noise are extremely similar to {@link #FOAM} and {@link #FOAM_FRACTAL}, just with an extra
 *     dimension added for mutation. The two TAFFY types of noise are considerably faster than FOAM in high dimensions,
 *     but are visibly lower-quality in low dimensions.</li>
 *     <li>If you are using {@link #FOAM}, {@link #MUTANT}, {@link #CUBIC}, {@link #TAFFY}, or their _FRACTAL versions,
 *     you can adjust how sharply the noise transitions between light and dark values by using
 *     {@link #setSharpness(float)}. Higher sharpness may be especially useful with more octaves, because most types of
 *     noise slide toward producing mostly central values with more octaves, and higher sharpness helps counter that
 *     "graying out."</li>
 *     <li>You will probably need to adjust the frequency for your particular use case. The default used by
 *     {@link #instance} is {@code 1.0f/32.0f} or {@code 0.03125f}, but world map generation often uses a frequency of 1
 *     or around 1, and some types of noise (especially {@link #FOAM}, {@link #MUTANT}, {@link #TAFFY}, and their
 *     _FRACTAL versions) need higher frequencies to look comparable to other noise types.</li>
 * </ul>
 */
public class Noise implements INoise {
    /**
     * Simple, very fast but very low-quality noise that forms a grid of squares, with their values blending at shared
     * edges somewhat.
     * <br>
     * <a href="https://i.imgur.com/egjotwb.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    public static final int VALUE = 0,
    /**
     * Simple, very fast but very low-quality noise that forms a grid of squares, with their values blending at shared
     * edges somewhat; this version can use {@link #setFractalType(int)}, {@link #setFractalOctaves(int)}, and more, but
     * none of these really disguise the grid it uses.
     * <br>
     * <a href="https://i.imgur.com/egjotwb.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    VALUE_FRACTAL = 1,
    /**
     * Also called Gradient Noise or Classic Perlin noise, this is fast and mid-to-low-quality in 2D, but slows down
     * significantly in higher dimensions while mostly improving in quality. This may have a noticeable grid at 90
     * degree angles (and a little at 45 degree angles).
     * <br>
     * <a href="https://i.imgur.com/MO7hwSI.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    PERLIN = 2,
    /**
     * Also called Gradient Noise or Classic Perlin noise, this is fast and mid-to-low-quality in 2D, but slows down
     * significantly in higher dimensions while mostly improving in quality. This may have a noticeable grid at 90
     * degree angles (and a little at 45 degree angles). This version can use {@link #setFractalType(int)},
     * {@link #setFractalOctaves(int)}, and more.
     * <br>
     * <a href="https://i.imgur.com/MO7hwSI.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    PERLIN_FRACTAL = 3,
    /**
     * Also called Improved Perlin noise, this is always fast but tends to have better quality in lower dimensions. This
     * may have a noticeable grid at 60 degree angles, made of regular triangles in 2D.
     * <br>
     * <a href="https://i.imgur.com/wg3kq5A.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    SIMPLEX = 4,
    /**
     * Also called Improved Perlin noise, this is always fast but tends to have better quality in lower dimensions. This
     * may have a noticeable grid at 60 degree angles, made of regular triangles in 2D. This version can use
     * {@link #setFractalType(int)}, {@link #setFractalOctaves(int)}, and more; it is the default noise type if none is
     * specified.
     * <br>
     * <a href="https://i.imgur.com/wg3kq5A.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    SIMPLEX_FRACTAL = 5,
    /**
     * Creates a Voronoi diagram of 2D or 3D space and fills cells based on the {@link #setCellularReturnType(int)}
     * and {@link #setCellularDistanceFunction(int)}. This is more of an advanced usage, but can yield useful results
     * when oddly-shaped areas should have similar values. This supports 2D and 3D.
     * <br>
     * <a href="https://i.imgur.com/ScRves7.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    CELLULAR = 6,
    /**
     * Creates a Voronoi diagram of 2D or 3D space and fills cells based on the {@link #setCellularReturnType(int)}
     * and {@link #setCellularDistanceFunction(int)}. This is more of an advanced usage, but can yield useful results
     * when oddly-shaped areas should have similar values. This version can use {@link #setFractalType(int)},
     * {@link #setFractalOctaves(int)}, and more. This supports 2D and 3D.
     * <br>
     * <a href="https://i.imgur.com/ScRves7.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    CELLULAR_FRACTAL = 7,
    /**
     * A simple kind of noise that gets a random float for each vertex of a square or cube, and interpolates between all
     * of them to get a smoothly changing value using... uh... some kind of cubic or bicubic interpolation, the
     * documentation for <a href="https://github.com/jobtalle/CubicNoise">CubicNoise</> is not specific.
     * If you're changing the point hashing algorithm with {@link #setPointHash(IPointHash)}, you should usually use
     * this or {@link #CUBIC_FRACTAL} if you want to see any aesthetically-desirable artifacts in the hash. This
     * supports 2D, 3D, and 4D, currently.
     * <br>
     * <a href="https://i.imgur.com/foV90pn.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    CUBIC = 8,
    /**
     * A simple kind of noise that gets a random float for each vertex of a square or cube, and interpolates between all
     * of them to get a smoothly changing value using... uh... some kind of cubic or bicubic interpolation, the
     * documentation for <a href="https://github.com/jobtalle/CubicNoise">CubicNoise</> is not specific.
     * This version can use {@link #setFractalType(int)}, {@link #setFractalOctaves(int)}, and more.
     * If you're changing the point hashing algorithm with {@link #setPointHash(IPointHash)}, you must use this or
     * {@link #CUBIC} to see the effects of an artifact-laden point hash. This supports 2D, 3D, and 4D, currently.
     * <br>
     * <a href="https://i.imgur.com/foV90pn.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    CUBIC_FRACTAL = 9,
    /**
     * A novel kind of noise that works in n-dimensions by averaging n+1 value noise calls, all of them rotated around
     * each other, and with all of the value noise calls after the first adding in the last call's result to part of the
     * position. This yields rather high-quality noise (especially when comparing one octave of FOAM to one octave of
     * {@link #PERLIN} or {@link #SIMPLEX}), but is somewhat slow.
     * <br>
     * <a href="https://i.imgur.com/4ZC9h5t.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    FOAM = 10,
    /**
     * A novel kind of noise that works in n-dimensions by averaging n+1 value noise calls, all of them rotated around
     * each other, and with all of the value noise calls after the first adding in the last call's result to part of the
     * position. This yields rather high-quality noise (especially when comparing one octave of FOAM to one octave of
     * {@link #PERLIN} or {@link #SIMPLEX}), but is somewhat slow. This version can use {@link #setFractalType(int)},
     * {@link #setFractalOctaves(int)}, and more, and usually doesn't need as many octaves as PERLIN or SIMPLEX to
     * attain comparable quality.
     * <br>
     * <a href="https://i.imgur.com/4ZC9h5t.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    FOAM_FRACTAL = 11,
    /**
     * A simple combination of {@link #SIMPLEX} and {@link #VALUE} noise, averaging a call to each and then distorting
     * the result's distribution so it isn't as centrally-biased. The result is somewhere between {@link #FOAM} and
     * {@link #SIMPLEX}, and has less angular bias than Simplex or Value. This gets its name from how it mixes two
     * different geometric honeycombs (a triangular one for 2D Simplex noise and a square one for 2D Value noise).
     * <br>
     * <a href="https://i.imgur.com/bMEPiBA.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    HONEY = 12,
    /**
     * A simple combination of {@link #SIMPLEX_FRACTAL} and {@link #VALUE_FRACTAL} noise, averaging a call to each and
     * then distorting the result's distribution so it isn't as centrally-biased. The result is somewhere between
     * {@link #FOAM_FRACTAL} and {@link #SIMPLEX_FRACTAL}, and has less angular bias than Simplex or Value. This gets
     * its name from how it mixes two different geometric honeycombs (a triangular one for 2D Simplex noise and a square
     * one for 2D Value noise). This version can use {@link #setFractalType(int)}, {@link #setFractalOctaves(int)}, and
     * more, and usually doesn't need as many octaves as PERLIN or SIMPLEX to attain comparable quality, though it
     * drastically improves with just two octaves.
     * <br>
     * <a href="https://i.imgur.com/bMEPiBA.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    HONEY_FRACTAL = 13,
    /**
     * A kind of noise that allows extra configuration via {@link #setMutation(float)}, producing small changes when the
     * mutation value is similar, or large changes if it is very different. This contrasts with changes to the seed,
     * which almost always cause large changes for any difference in seed. The implementation here is the same as
     * {@link #FOAM} with one more dimension, which is filled by the mutation value.
     * <br>
     * <a href="https://i.imgur.com/4ZC9h5t.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    MUTANT = 14,
    /**
     * A kind of noise that allows extra configuration via {@link #setMutation(float)}, producing small changes when the
     * mutation value is similar, or large changes if it is very different. This contrasts with changes to the seed,
     * which almost always cause large changes for any difference in seed. The implementation here is the same as
     * {@link #FOAM_FRACTAL} with one more dimension, which is filled by the mutation value.
     * <br>
     * <a href="https://i.imgur.com/4ZC9h5t.png">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    MUTANT_FRACTAL = 15,
    /**
     * A bubbly, stretchy kind of noise that allows extra configuration via {@link #setMutation(float)}, producing small
     * changes when the mutation value is similar, or large changes if it is very different. This contrasts with changes
     * to the seed, which almost always cause large changes for any difference in seed.
     * This is very similar to {@link #MUTANT} (which is just {@link #FOAM} with an extra dimension), but instead of how
     * FOAM uses {@link #VALUE} with various rotations, this uses its own variety of basic building-block noise
     * (internally called Trill noise). This Trill noise is extremely low-quality on its own, but stays quite fast even
     * in very high dimensions. This means that this is potentially a good option if {@link #MUTANT} is too slow, and
     * you need higher-dimensional (4D and up) noise.
     * <br>
     * <a href="https://i.imgur.com/XaXho6P.png">Noise sample at left, FFT at right.</a> (This is 6D noise.)
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    TAFFY = 16,
    /**
     * A bubbly, stretchy kind of noise that allows extra configuration via {@link #setMutation(float)}, producing small
     * changes when the mutation value is similar, or large changes if it is very different. This contrasts with changes
     * to the seed, which almost always cause large changes for any difference in seed. This is very similar to
     * {@link #MUTANT_FRACTAL} (which is just {@link #FOAM_FRACTAL} with an extra dimension), but instead of how
     * FOAM uses {@link #VALUE} with various rotations, this uses its own variety of basic building-block noise
     * (internally called Trill noise). This Trill noise is extremely low-quality on its own, but stays quite fast even
     * in very high dimensions. This means that this is potentially a good option if {@link #MUTANT_FRACTAL} is too
     * slow, and you need higher-dimensional (4D and up) noise.
     * <br>
     * <a href="https://i.imgur.com/XaXho6P.png">Noise sample at left, FFT at right.</a> (This is 6D noise.)
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    TAFFY_FRACTAL = 17,
    /**
     * Purely chaotic, non-continuous random noise per position; looks like static on a TV screen.
     * <br>
     * <a href="https://i.imgur.com/vBtISSx.jpg">Noise sample at left, FFT at right.</a>
     * <br>
     * This is meant to be used with {@link #setNoiseType(int)}.
     */
    WHITE_NOISE = 18;

    public static final IntObjectMap<String> NOISE_TYPES = IntObjectMap.with(
            VALUE, "Value", VALUE_FRACTAL, "ValueFractal",
            PERLIN, "Perlin", PERLIN_FRACTAL, "PerlinFractal",
            SIMPLEX, "Simplex", SIMPLEX_FRACTAL, "SimplexFractal",
            CELLULAR, "Cellular", CELLULAR_FRACTAL, "CellularFractal",
            CUBIC, "Cubic", CUBIC_FRACTAL, "CubicFractal",
            FOAM, "Foam", FOAM_FRACTAL, "FoamFractal",
            HONEY, "Honey", HONEY_FRACTAL, "HoneyFractal",
            MUTANT, "Mutant", MUTANT_FRACTAL, "MutantFractal",
            TAFFY, "Taffy", TAFFY_FRACTAL, "TaffyFractal",
            WHITE_NOISE, "WhiteNoise");

    /**
     * Simple linear interpolation. May result in artificial-looking noise.
     * Meant to be used with {@link #setInterpolation(int)}.
     */
    public static final int LINEAR = 0;
    /**
     * Cubic interpolation via Hermite spline, more commonly known as "smoothstep".
     * Can be very natural-looking, but can also have problems in higher dimensions
     * (including 3D when used with normals) with seams appearing.
     * Meant to be used with {@link #setInterpolation(int)}.
     */
    public static final int HERMITE = 1;
    /**
     * Quintic interpolation, sometimes known as "smootherstep".
     * This has somewhat steeper transitions than {@link #HERMITE}, but doesn't
     * have any issues with seams.
     * Meant to be used with {@link #setInterpolation(int)}.
     */
    public static final int QUINTIC = 2;

    public static final IntObjectMap<String> INTERPOLATIONS = IntObjectMap.with(
            LINEAR, "Linear", HERMITE, "Hermite", QUINTIC, "Quintic"
    );

    /**
     * "Standard" layered octaves of noise, where each octave has a different frequency and weight.
     * Tends to look cloudy with more octaves, and generally like a natural process.
     * <br>
     * Meant to be used with {@link #setFractalType(int)}.
     */
    public static final int FBM = 0;
    /**
     * A less common way to layer octaves of noise, where most results are biased toward higher values,
     * but "valleys" show up filled with much lower values.
     * This probably has some good uses in 3D or higher noise, but it isn't used too frequently.
     * <br>
     * Meant to be used with {@link #setFractalType(int)}.
     */
    public static final int BILLOW = 1;
    /**
     * A way to layer octaves of noise so most values are biased toward low values but "ridges" of high
     * values run across the noise. This can be a good way of highlighting the least-natural aspects of
     * some kinds of noise; {@link #PERLIN_FRACTAL} has mostly ridges along 45-degree angles,
     * {@link #SIMPLEX_FRACTAL} has many ridges along a triangular grid, and so on. {@link #FOAM_FRACTAL}
     * and {@link #HONEY_FRACTAL} do well with this mode, though, and look something like lightning or
     * bubbling fluids, respectively. Using FOAM or HONEY will have this look natural, but PERLIN in
     * particular will look unnatural if the grid is visible.
     * <br>
     * Meant to be used with {@link #setFractalType(int)}.
     */
    public static final int RIDGED_MULTI = 2;
    /**
     * Layered octaves of noise, where each octave has a different frequency and weight, and the results of
     * earlier octaves affect the inputs to later octave calculations. Tends to look cloudy but with swirling
     * distortions, and generally like a natural process.
     * <br>
     * Meant to be used with {@link #setFractalType(int)}.
     */
    public static final int DOMAIN_WARP = 3;

    public static final IntObjectMap<String> FRACTAL_TYPES = IntObjectMap.with(
            FBM, "FBM", BILLOW, "Billow", RIDGED_MULTI, "Ridged", DOMAIN_WARP, "DomainWarp"
    );

    /**
     * Measures distances "as the crow flies."
     * All points at an equal distance from the origin form a circle.
     * Used only with {@link #CELLULAR} noise.
     * Meant to be used with {@link #setCellularDistanceFunction(int)}.
     */
    public static final int EUCLIDEAN = 0;
    /**
     * Measures distances on a grid, as if allowing only orthogonal movement (with no diagonals).
     * All points at an equal distance from the origin form a diamond shape.
     * Used only with {@link #CELLULAR} noise.
     * Meant to be used with {@link #setCellularDistanceFunction(int)}.
     */
    public static final int MANHATTAN = 1;
    /**
     * Measures distances with an approximation of Euclidean distance that's not 100% accurate.
     * All points at an equal distance from the origin form a rough octagon.
     * Used only with {@link #CELLULAR} noise.
     * Meant to be used with {@link #setCellularDistanceFunction(int)}.
     */
    public static final int NATURAL = 2;

    public static final IntObjectMap<String> CELLULAR_DISTANCE_METRICS = IntObjectMap.with(
            EUCLIDEAN, "Euclidean", MANHATTAN, "Manhattan", NATURAL, "Natural"
    );

    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int CELL_VALUE = 0;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}. Note that this does not allow configuring an extra
     * Noise value to use for lookup (anymore); it always uses 3 octaves of {@link #SIMPLEX_FRACTAL} with {@link #FBM}.
     */
    public static final int NOISE_LOOKUP = 1;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE = 2;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE_2 = 3;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE_2_ADD = 4;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE_2_SUB = 5;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE_2_MUL = 6;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE_2_DIV = 7;
    /**
     * Meant to be used with {@link #setCellularReturnType(int)}.
     */
    public static final int DISTANCE_VALUE = 8;

    public static final IntObjectMap<String> CELLULAR_RETURN_TYPES = IntObjectMap.with(
            CELL_VALUE, "CellValue", NOISE_LOOKUP, "NoiseLookup", DISTANCE, "Distance",
            DISTANCE_2, "Distance2", DISTANCE_2_ADD, "Distance2Add", DISTANCE_2_SUB, "Distance2Sub",
            DISTANCE_2_MUL, "Distance2Mul", DISTANCE_2_DIV, "Distance2Div", DISTANCE_VALUE, "DistanceValue"
    );

    /**
     * @see #getSeed()
     */
    protected int seed;

    /**
     * @see #getFrequency()
     */
    protected float frequency = 0.03125f;

    /**
     * @see #getInterpolation()
     */
    protected int interpolation = HERMITE;

    /**
     * @see #getNoiseType()
     */
    protected int noiseType = SIMPLEX_FRACTAL;

    /**
     * @see #getFractalOctaves()
     */
    protected int octaves = 1;

    /**
     * @see #getFractalLacunarity()
     */
    protected float lacunarity = 2f;
    /**
     * @see #getFractalGain()
     */
    protected float gain = 0.5f;
    /**
     * @see #getFractalType()
     */
    protected int fractalType = FBM;

    private float fractalBounding;

    /**
     * @see #getCellularDistanceFunction()
     */
    protected int cellularDistanceFunction = EUCLIDEAN;

    /**
     * @see #getCellularReturnType()
     */
    protected int cellularReturnType = CELL_VALUE;

    /**
     * @see #getGradientPerturbAmp()
     */
    protected float gradientPerturbAmp = 1f / 0.45f;

    /**
     * @see #getSharpness()
     */
    protected float sharpness = 1f;

    /**
     * Should always be equal to {@code (1f / sharpness)}. This is used by noise type TAFFY as part of a faster
     * calculation to redistribute values toward or away from the center (which there is 0).
     */
    protected float sharpnessInverse = 1f;

    /**
     * @see #getMutation()
     */
    protected float mutation = 0f;

    /**
     * @see #isFractalSpiral()
     */
    protected boolean fractalSpiral = false;

    /**
     * @see #getPointHash()
     */
    protected IPointHash pointHash = new IntPointHash();

    /**
     * A publicly available Noise object with seed 1337, frequency 1.0f/32.0f, 1 octave of Simplex noise using
     * SIMPLEX_FRACTAL noiseType, 2f lacunarity and 0.5f gain. It's encouraged to use methods that temporarily configure
     * this variable, like {@link #getNoiseWithSeed(float, float, int)} rather than changing its settings and using a
     * method that needs that lasting configuration, like {@link #getConfiguredNoise(float, float)}. If you want to use
     * lasting settings, you should create your own new Noise object.
     */
    public static final Noise instance = new Noise();
    /**
     * A constructor that takes no parameters, and uses all default settings with a seed of 1337. An example call to
     * this would be {@code new Noise()}, which makes noise with the seed 1337, a default frequency of 1.0f/32.0f, 1
     * octave of Simplex noise (since this doesn't specify octave count, it always uses 1 even for the
     * SIMPLEX_FRACTAL noiseType this uses, but you can call {@link #setFractalOctaves(int)} later to benefit from the
     * fractal noiseType), and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
     */
    public Noise() {
        this(1337);
    }

    /**
     * A constructor that takes only a parameter for the Noise's seed, which should produce different results for
     * any different seeds. An example call to this would be {@code new Noise(1337)}, which makes noise with the
     * seed 1337, a default frequency of 1.0f/32.0f, 1 octave of Simplex noise (since this doesn't specify octave count,
     * it always uses 1 even for the SIMPLEX_FRACTAL noiseType this uses, but you can call
     * {@link #setFractalOctaves(int)} later to benefit from the fractal noiseType), and normal lacunarity and gain
     * (when unspecified, they are 2f and 0.5f).
     * @param seed the int seed for the noise, which should significantly affect the produced noise
     */
    public Noise(int seed) {
        this.seed = seed;
        calculateFractalBounding();
    }
    /**
     * A constructor that takes two parameters to specify the Noise from the start. An example call to this
     * would be {@code new Noise(1337, 0.02f)}, which makes noise with the seed 1337, a lower
     * frequency, 1 octave of Simplex noise (since this doesn't specify octave count, it always uses 1 even for the
     * SIMPLEX_FRACTAL noiseType this uses, but you can call {@link #setFractalOctaves(int)} later to benefit from the
     * fractal noiseType), and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
     * @param seed the int seed for the noise, which should significantly affect the produced noise
     * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
     */
    public Noise(int seed, float frequency)
    {
        this(seed, frequency, SIMPLEX_FRACTAL, 1, 2f, 0.5f);
    }
    /**
     * A constructor that takes a few parameters to specify the Noise from the start. An example call to this
     * would be {@code new Noise(1337, 0.02f, Noise.SIMPLEX)}, which makes noise with the seed 1337, a lower
     * frequency, 1 octave of Simplex noise (since this doesn't specify octave count, it always uses 1 even for
     * noiseTypes like SIMPLEX_FRACTAL, but using a fractal noiseType can make sense if you call
     * {@link #setFractalOctaves(int)} later), and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
     * @param seed the int seed for the noise, which should significantly affect the produced noise
     * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
     * @param noiseType the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
     */
    public Noise(int seed, float frequency, int noiseType)
    {
        this(seed, frequency, noiseType, 1, 2f, 0.5f);
    }

    /**
     * A constructor that takes several parameters to specify the Noise from the start. An example call to this
     * would be {@code new Noise(1337, 0.02f, Noise.SIMPLEX_FRACTAL, 4)}, which makes noise with the seed 1337, a lower
     * frequency, 4 octaves of Simplex noise, and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
     * @param seed the int seed for the noise, which should significantly affect the produced noise
     * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
     * @param noiseType the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
     * @param octaves how many octaves of noise to use when the noiseType is one of the _FRACTAL types
     */
    public Noise(int seed, float frequency, int noiseType, int octaves)
    {
        this(seed, frequency, noiseType, octaves, 2f, 0.5f);
    }

    /**
     * A constructor that takes a lot of parameters to specify the Noise from the start. An example call to this
     * would be {@code new Noise(1337, 0.02f, Noise.SIMPLEX_FRACTAL, 4, 0.5f, 2f)}, which makes noise with a
     * lower frequency, 4 octaves of Simplex noise, and the "inverse" effect on how those octaves work (which makes
     * the extra added octaves be more significant to the final result and also have a lower frequency, while normally
     * added octaves have a higher frequency and tend to have a minor effect on the large-scale shape of the noise).
     * @param seed the int seed for the noise, which should significantly affect the produced noise
     * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
     * @param noiseType the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
     * @param octaves how many octaves of noise to use when the noiseType is one of the _FRACTAL types
     * @param lacunarity typically 2.0, or 0.5 to change how extra octaves work (inverse mode)
     * @param gain typically 0.5, or 2.0 to change how extra octaves work (inverse mode)
     */
    public Noise(int seed, float frequency, int noiseType, int octaves, float lacunarity, float gain)
    {
        this.seed = seed;
        this.frequency = Math.max(0.0001f, frequency);
        this.noiseType = noiseType;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
        calculateFractalBounding();
    }

    /**
     * Copy constructor; copies all non-temporary fields from  {@code other} into this. This uses the same reference to
     * an {@link IPointHash} set with {@link #setPointHash(IPointHash)}, but otherwise everything it copies is a
     * primitive value.
     * @param other another Noise, which must not be null
     */
    public Noise(final Noise other) {
        this(other.seed, other.frequency, other.noiseType, other.octaves, other.lacunarity, other.gain);
        this.fractalType = other.fractalType;
        this.pointHash = other.pointHash;
        this.interpolation = other.interpolation;
        this.gradientPerturbAmp = other.gradientPerturbAmp;
        this.cellularReturnType = other.cellularReturnType;
        this.cellularDistanceFunction = other.cellularDistanceFunction;
        this.sharpness = other.sharpness;
        this.sharpnessInverse = 1f / this.sharpness;
        this.mutation = other.mutation;
        this.fractalSpiral = other.fractalSpiral;
    }

    @Override
    public String getTag() {
        return "NoiN";
    }

    public StringBuilder appendPretty(StringBuilder sb) {
        sb.append("Noise {")
                .append("\n  Noise Type: ").append(NOISE_TYPES.getOrDefault(noiseType, "???"))
                .append("\n  Fractal Type: ").append(FRACTAL_TYPES.getOrDefault(fractalType, "???"))
                .append("\n  Frequency: ").append(frequency)
                .append("\n  Octaves: ").append(octaves)
                .append("\n  Seed: ").append(seed)
                .append("\n  Lacunarity: ").append(lacunarity)
                .append("\n  Gain: ").append(gain)
                .append("\n  Sharpness: ").append(sharpness)
                .append("\n  Mutation: ").append(mutation)
                .append("\n  Interpolation: ").append(INTERPOLATIONS.getOrDefault(interpolation, "???"))
                .append("\n  Fractal Spiral: ").append(fractalSpiral)
                .append("\n  Cellular Distance Metric: ").append(CELLULAR_DISTANCE_METRICS.getOrDefault(cellularDistanceFunction, "???"))
                .append("\n  Cellular Return Type: ").append(CELLULAR_RETURN_TYPES.getOrDefault(cellularReturnType, "???"))
                .append("\n  Gradient Perturb Amplitude: ").append(gradientPerturbAmp)
        ;
        return sb.append("\n}");
    }

    public String toPrettyString() {
        return appendPretty(new StringBuilder()).toString();
    }

    public void prettyPrint() {
        System.out.println(appendPretty(new StringBuilder()));
    }
    /**
     * Writes all fields of this Noise (except for the {@link #getPointHash()}, which must be stored separately) to a
     * String and returns it. The result of this method can be used by {@link #stringDeserialize(String)}, though if
     * you need a particular IPointHash value, you need to set it yourself on the result of that method.
     * @return a String that stores the data of this Noise object; can be read by {@link #stringDeserialize(String)}
     */
    public String stringSerialize(){
        return "`" + seed + '~' + noiseType + '~' + octaves + '~' +
                fractalType + '~' + interpolation + '~' +
                cellularReturnType + '~' + cellularDistanceFunction + '~' + (fractalSpiral ? '1' : '0') + '~' +
                BitConversion.floatToReversedIntBits(frequency) + '~' +
                BitConversion.floatToReversedIntBits(lacunarity) + '~' +
                BitConversion.floatToReversedIntBits(gain) + '~' +
                BitConversion.floatToReversedIntBits(gradientPerturbAmp) + '~' +
                BitConversion.floatToReversedIntBits(sharpness) + '~' +
                BitConversion.floatToReversedIntBits(mutation) + "`";
    }

    /**
     * Reads in a String that was produced by {@link #stringSerialize()} and produces a copy of the Noise that was
     * stored into it. Note that neither this method nor stringSerialize changes {@link #getPointHash()} from its
     * default value, so if you need a different value for that, you need to set it yourself on the result of this.
     * @param data a String produced by {@link #stringSerialize()}
     * @return a new Noise object matching the data stored in the given String
     */
    public Noise stringDeserialize(String data){
        if(data == null || data.length() < 27)
            return this;
        int pos;
        int seed =                     DigitTools.intFromDec(data,     1, pos = data.indexOf('~'));
        int noiseType =                DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int octaves =                  DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int fractalType =              DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int interpolation =            DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int cellularReturnType =       DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int cellularDistanceFunction = DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        boolean fractalSpiral =        DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1)) == 1;

        float frequency =          BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1)));
        float lacunarity =         BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1)));
        float gain =               BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1)));
        float gradientPerturbAmp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1)));
        float sharpness =      BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1)));
        float mutation =           BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, pos = data.indexOf('`', pos+1)));

        setSeed(seed);
        setFrequency(frequency);
        setNoiseType(noiseType);
        setFractalOctaves(octaves);
        setFractalLacunarity(lacunarity);
        setFractalGain(gain);
        calculateFractalBounding();
        this.fractalType = fractalType;
        this.interpolation = interpolation;
        this.gradientPerturbAmp = gradientPerturbAmp;
        this.cellularReturnType = cellularReturnType;
        this.cellularDistanceFunction = cellularDistanceFunction;
        this.sharpness = sharpness;
        this.sharpnessInverse = 1f / sharpness;
        this.mutation = mutation;
        this.fractalSpiral = fractalSpiral;
        return this;
    }

    @Override
    public Noise copy() {
        return new Noise(this);
    }

    /**
     * @return Returns the seed used by this object
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Sets the seed used for all noise types, as a long.
     * If this is not called, defaults to 1337L.
     * @param seed a seed as a long
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * Sets the frequency for all noise types. If this is not called, it defaults to 0.03125f (or 1f/32f).
     * This setter validates the frequency, and won't set it to a float less than 0.0001f, which is small enough that
     * floating-point precision could be an issue. Lots of things may expect this to be higher than the default; try
     * setting frequency to {@code 1.0f} if you experience issues.
     * @param frequency the frequency for all noise types, as a positive non-zero float
     */
    public void setFrequency(float frequency) {
        this.frequency = Math.max(0.0001f, frequency);
    }

    /**
     * Gets the frequency for all noise types. The default is 0.03125f, or 1f/32f.
     * @return the frequency for all noise types, which should be a positive non-zero float
     */
    public float getFrequency()
    {
        return frequency;
    }

    /**
     * Changes the interpolation method used to smooth between noise values, using one of the following constants from
     * this class (lowest to highest quality): {@link #LINEAR} (0), {@link #HERMITE} (1), or {@link #QUINTIC} (2). If
     * this is not called, it defaults to HERMITE. This is used in Value, Perlin, and Position Perturbing, and because
     * it is used in Value, that makes it also apply to Foam, Honey, and Mutant.
     * @param interpolation an int (0, 1, or 2) corresponding to a constant from this class for an interpolation type
     */
    public void setInterpolation(int interpolation) {
        this.interpolation = Math.min(Math.max(interpolation, 0), 2);
    }

    /**
     * Gets the constant corresponding to the interpolation method used to smooth between noise values. This is always
     * one of the constants {@link #LINEAR} (0), {@link #HERMITE} (1), or {@link #QUINTIC} (2). If this is not called,
     * it defaults to HERMITE. This is used in Value, Perlin, and Position Perturbing, and because it is used in Value,
     * that makes it also apply to Foam, Honey, and Mutant.
     * @return an int (0, 1, or 2) corresponding to a constant from this class for an interpolation type
     */
    public int getInterpolation() {
        return interpolation;
    }
    
    /**
     * Sets the default type of noise returned by {@link #getConfiguredNoise(float, float)}, using one of the following
     * constants in this class:
     * {@link #VALUE} (0), {@link #VALUE_FRACTAL} (1), {@link #PERLIN} (2), {@link #PERLIN_FRACTAL} (3),
     * {@link #SIMPLEX} (4), {@link #SIMPLEX_FRACTAL} (5), {@link #CELLULAR} (6), {@link #CELLULAR_FRACTAL} (7),
     * {@link #CUBIC} (8), {@link #CUBIC_FRACTAL} (9), {@link #FOAM} (10), {@link #FOAM_FRACTAL} (11), {@link #HONEY}
     * (12), {@link #HONEY_FRACTAL} (13), {@link #MUTANT} (14), {@link #MUTANT_FRACTAL} (15), {@link #TAFFY} (16),
     * {@link #TAFFY_FRACTAL} (17), or {@link #WHITE_NOISE} (18).
     * If this isn't called, getConfiguredNoise() will default to SIMPLEX_FRACTAL. Note that if you have a fractal noise
     * type, you can get the corresponding non-fractal noise type by subtracting 1 from the constant this returns. The
     * reverse is not always true, because White Noise has no fractal version.
     * @param noiseType an int from 0 to 17 corresponding to a constant from this class for a noise type
     */
    public void setNoiseType(int noiseType) {
        this.noiseType = noiseType;
    }

    /**
     * Gets the default type of noise returned by {@link #getConfiguredNoise(float, float)}, using one of the following
     * constants in this class:
     * {@link #VALUE} (0), {@link #VALUE_FRACTAL} (1), {@link #PERLIN} (2), {@link #PERLIN_FRACTAL} (3),
     * {@link #SIMPLEX} (4), {@link #SIMPLEX_FRACTAL} (5), {@link #CELLULAR} (6), {@link #CELLULAR_FRACTAL} (7),
     * {@link #CUBIC} (8), {@link #CUBIC_FRACTAL} (9), {@link #FOAM} (10), {@link #FOAM_FRACTAL} (11), {@link #HONEY}
     * (12), {@link #HONEY_FRACTAL} (13), {@link #MUTANT} (14), {@link #MUTANT_FRACTAL} (15), {@link #TAFFY} (16),
     * {@link #TAFFY_FRACTAL} (17), or {@link #WHITE_NOISE} (18).
     * The default is SIMPLEX_FRACTAL. Note that if you have a fractal noise type, you can get the corresponding
     * non-fractal noise type by subtracting 1 from the constant this returns. The reverse is not always true, because
     * White Noise has no fractal version.
     * @return the noise type as a code, from 0 to 17 inclusive
     */
    public int getNoiseType()
    {
        return noiseType;
    }

    /**
     * Sets the octave count for all fractal noise types.
     * If this isn't called, it will default to 1.
     * @param octaves the number of octaves to use for fractal noise types, as a positive non-zero int
     */
    public void setFractalOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);
        calculateFractalBounding();
    }

    /**
     * Gets the octave count for all fractal noise types. The default is 1.
     * @return the number of octaves to use for fractal noise types, as a positive non-zero int
     */
    public int getFractalOctaves()
    {
        return octaves;
    }

    /**
     * Sets the octave lacunarity for all fractal noise types.
     * Lacunarity is a multiplicative change to frequency between octaves. If this isn't called, it defaults to 2.
     * @param lacunarity a non-0 float that will be used for the lacunarity of fractal noise types; commonly 2.0 or 0.5
     */
    public void setFractalLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    /**
     * Gets the octave lacunarity for all fractal noise types.
     * Lacunarity is a multiplicative change to frequency between octaves. If this wasn't changed, it defaults to 2.
     * @return a float that will be used for the lacunarity of fractal noise types; commonly 2.0 or 0.5
     */
    public float getFractalLacunarity() {
        return lacunarity;
    }

    /**
     * Sets the octave gain for all fractal noise types.
     * If this isn't called, it defaults to 0.5.
     * @param gain the gain between octaves, as a float
     */
    public void setFractalGain(float gain) {
        this.gain = gain;
        calculateFractalBounding();
    }

    /**
     * Sets the octave gain for all fractal noise types.
     * This is typically related to {@link #getFractalLacunarity()}, with gain falling as lacunarity rises.
     * If this wasn't changed, it defaults to 0.5.
     * @return the gain between octaves, as a float
     */
    public float getFractalGain() {
        return gain;
    }

    /**
     * Sets the method for combining octaves in all fractal noise types, allowing an int argument corresponding to one
     * of the following constants from this class: {@link #FBM} (0), {@link #BILLOW} (1), {@link #RIDGED_MULTI} (2), or
     * {@link #DOMAIN_WARP} (3). If this hasn't been called, it will use FBM.
     * @param fractalType an int (0, 1, 2, or 3) that corresponds to a constant like {@link #FBM} or {@link #RIDGED_MULTI}
     */
    public void setFractalType(int fractalType) {
        this.fractalType = fractalType;
    }

    /**
     * Gets the method for combining octaves in all fractal noise types, allowing an int argument corresponding to one
     * of the following constants from this class: {@link #FBM} (0), {@link #BILLOW} (1), {@link #RIDGED_MULTI} (2), or
     * {@link #DOMAIN_WARP} (3). The default is FBM.
     * @return the fractal type as a code; 0, 1, 2, or 3
     */
    public int getFractalType()
    {
        return fractalType;
    }

    /**
     * Sets the distance function used in cellular noise calculations, allowing an int argument corresponding to one of
     * the following constants from this class: {@link #EUCLIDEAN} (0), {@link #MANHATTAN} (1), or {@link #NATURAL} (2).
     * If this hasn't been called, it will use EUCLIDEAN.
     * @param cellularDistanceFunction an int that can be 0, 1, or 2, corresponding to a constant from this class
     */
    public void setCellularDistanceFunction(int cellularDistanceFunction) {
        this.cellularDistanceFunction = cellularDistanceFunction;
    }

    /**
     * Gets the distance function used in cellular noise calculations, as an int constant from this class:
     * {@link #EUCLIDEAN} (0), {@link #MANHATTAN} (1), or {@link #NATURAL} (2). If this wasn't changed, it will use
     * EUCLIDEAN.
     * @return an int that can be 0, 1, or 2, corresponding to a constant from this class
     */
    public int getCellularDistanceFunction() {
        return cellularDistanceFunction;
    }

    /**
     * Sets the return type from cellular noise calculations, allowing an int argument corresponding to one of the
     * following constants from this class: {@link #CELL_VALUE} (0), {@link #NOISE_LOOKUP} (1), {@link #DISTANCE} (2),
     * {@link #DISTANCE_2} (3), {@link #DISTANCE_2_ADD} (4), {@link #DISTANCE_2_SUB} (5), {@link #DISTANCE_2_MUL} (6),
     * or {@link #DISTANCE_2_DIV} (7). If this isn't called, it will use CELL_VALUE.
     * @param cellularReturnType a constant from this class (see above JavaDoc)
     */
    public void setCellularReturnType(int cellularReturnType) {
        this.cellularReturnType = cellularReturnType;
    }

    /**
     * Gets the return type from cellular noise calculations, corresponding to a constant from this class:
     * {@link #CELL_VALUE} (0), {@link #NOISE_LOOKUP} (1), {@link #DISTANCE} (2), {@link #DISTANCE_2} (3),
     * {@link #DISTANCE_2_ADD} (4), {@link #DISTANCE_2_SUB} (5), {@link #DISTANCE_2_MUL} (6), or
     * {@link #DISTANCE_2_DIV} (7). If this wasn't changed, it will use CELL_VALUE.
     * @return a constant from this class representing a type of cellular noise calculation
     */
    public int getCellularReturnType() {
        return cellularReturnType;
    }

    /**
     * A no-op method that is here for compatibility with earlier versions.
     * @param noise ignored
     */
    public void setCellularNoiseLookup(Noise noise) {
    }

    /**
     * Sets the maximum perturb distance from original location when using {@link #gradientPerturb2(float[])},
     * {@link #gradientPerturb3(float[])}, {@link #gradientPerturbFractal2(float[])}, or
     * {@link #gradientPerturbFractal3(float[])}; the default is 1.0.
     * @param gradientPerturbAmp the maximum perturb distance from the original location when using relevant methods
     */
    public void setGradientPerturbAmp(float gradientPerturbAmp) {
        this.gradientPerturbAmp = gradientPerturbAmp / 0.45f;
    }

    /**
     * Gets the maximum perturb distance from original location when using {@link #gradientPerturb2(float[])},
     * {@link #gradientPerturb3(float[])}, {@link #gradientPerturbFractal2(float[])}, or
     * {@link #gradientPerturbFractal3(float[])}; the default is 1.0.
     * @return the maximum perturb distance from the original location when using relevant methods
     */
    public float getGradientPerturbAmp(){
        return gradientPerturbAmp * 0.45f;
    }

    /**
     * Gets the "sharpness" for the {@link #FOAM}, {@link #FOAM_FRACTAL}, {@link #MUTANT}, {@link #MUTANT_FRACTAL},
     * {@link #CUBIC}, {@link #CUBIC_FRACTAL}, {@link #TAFFY}, and {@link #TAFFY_FRACTAL} noise types, which is usually
     * around 0.25f to 2.0f, and defaults to 1.0f. High values produce extreme results more often, and low values
     * produce mid-range values more often.
     * <br>
     * This is equivalent to {@link #getSharpness()}.
     * @return the current "sharpness" for some noise types (Foam, Mutant, Cubic)
     */
    public float getFoamSharpness() {
        return sharpness;
    }

    /**
     * Only used with {@link #FOAM}, {@link #FOAM_FRACTAL}, {@link #MUTANT}, {@link #MUTANT_FRACTAL}, {@link #CUBIC},
     * {@link #CUBIC_FRACTAL}, {@link #TAFFY}, and {@link #TAFFY_FRACTAL} noise types, this affects how often the noise
     * will produce very high and very low results (more often with high sharpness values), as opposed to mid-range
     * (more often with low sharpness values).
     * <br>
     * This defaults to 1.0f if not set. It is equivalent to {@link #setSharpness(float)}.
     * @param sharpness higher results (above 1) tend to produce extremes, lower results (below 1) produce mid-range
     */
    public void setFoamSharpness(float sharpness) {
        sharpness = Math.max(0.000001f, sharpness);
        this.sharpness = sharpness;
        sharpnessInverse = 1f / sharpness;
    }

    /**
     * Gets the "sharpness" for the {@link #FOAM}, {@link #FOAM_FRACTAL}, {@link #MUTANT}, {@link #MUTANT_FRACTAL},
     * {@link #CUBIC}, {@link #CUBIC_FRACTAL}, {@link #TAFFY}, and {@link #TAFFY_FRACTAL} noise types, which is usually
     * around 0.25f to 2.0f, and defaults to 1.0f. High values produce extreme results more often, and low values
     * produce mid-range values more often.
     * <br>
     * This is equivalent to {@link #getFoamSharpness()}.
     * @return the current "sharpness" for some noise types (Foam, Mutant, Cubic)
     */
    public float getSharpness() {
        return sharpness;
    }

    /**
     * Only used with {@link #FOAM}, {@link #FOAM_FRACTAL}, {@link #MUTANT}, {@link #MUTANT_FRACTAL}, {@link #CUBIC},
     * {@link #CUBIC_FRACTAL}, {@link #TAFFY}, and {@link #TAFFY_FRACTAL} noise types, this affects how often the noise
     * will produce very high and very low results (more often with high sharpness values), as opposed to mid-range
     * (more often with low sharpness values).
     * <br>
     * This defaults to 1.0f if not set. It is equivalent to {@link #setFoamSharpness(float)}.
     * @param sharpness higher results (above 1) tend to produce extremes, lower results (below 1) produce mid-range
     */
    public void setSharpness(float sharpness) {
        sharpness = Math.max(0.000001f, sharpness);
        this.sharpness = sharpness;
        sharpnessInverse = 1f / sharpness;
    }

    /**
     * Gets the mutation value used by {@link #MUTANT}, {@link #MUTANT_FRACTAL}, {@link #TAFFY}, and
     * {@link #TAFFY_FRACTAL} noise types, which allows making small changes to the result when the mutation values are
     * slightly different.
     * @return the current mutation value, which can be any finite float
     */
    public float getMutation() {
        return mutation;
    }

    /**
     * Sets the mutation value used by {@link #MUTANT}, {@link #MUTANT_FRACTAL}, {@link #TAFFY}, and
     * {@link #TAFFY_FRACTAL} noise types, which can be any finite float. Small changes to the mutation value cause
     * small changes in the result, unlike changes to the seed.
     * @param mutation the mutation value to use, which can be any finite float
     */
    public void setMutation(float mutation) {
        this.mutation = mutation;
    }

    /**
     * Returns true if this uses a spiraling rotation as octaves are added to fractal noise.
     * This mode affects all fractal types when there are 2 or more octaves. It changes
     * {@link #VALUE_FRACTAL}, {@link #CUBIC_FRACTAL}, {@link #PERLIN_FRACTAL}, {@link #SIMPLEX_FRACTAL},
     * and {@link #HONEY_FRACTAL} noise types, but none of the others because those show no real improvement
     * with this on. This mode defaults to false if not set.
     * @return true if using fractal spiral mode, false otherwise
     */
    public boolean isFractalSpiral() {
        return fractalSpiral;
    }

    /**
     * Sets the fractal spiral mode on or off; if on, this uses a spiraling rotation as octaves are added to
     * fractal noise. This mode affects all fractal types when there are 2 or more octaves. It changes
     * {@link #VALUE_FRACTAL}, {@link #CUBIC_FRACTAL}, {@link #PERLIN_FRACTAL}, {@link #SIMPLEX_FRACTAL},
     * and {@link #HONEY_FRACTAL} noise types, but none of the others because those show no real improvement
     * with this on. This mode defaults to false if not set.
     * @param fractalSpiral true to set fractal spiral mode on, false to set it off
     */
    public void setFractalSpiral(boolean fractalSpiral) {
        this.fractalSpiral = fractalSpiral;
    }

    /**
     * Sets the point hash, typically to one with intentional flaws, as found in {@link FlawedPointHash} types.
     * Only matters for {@link #CUBIC} and {@link #CUBIC_FRACTAL} noise types.
     * @param hash a non-null IPointHash implementation
     */
    public void setPointHash(IPointHash hash){
        if(hash != null) this.pointHash = hash;
    }

    /**
     * Gets the current point hash, which is only used for {@link #CUBIC} and {@link #CUBIC_FRACTAL} noise types.
     * This is an {@link IntPointHash} by default.
     * @return an IPointHash implementation, typically an {@link IntPointHash} if not otherwise specified
     */
    public IPointHash getPointHash() {
        return pointHash;
    }


    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = (int)(seed ^ seed >>> 32);
    }

    public double getNoise(double x, double y) {
        return getConfiguredNoise((float)x, (float)y);
    }

    public double getNoise(double x, double y, double z) {
        return getConfiguredNoise((float)x, (float)y, (float)z);
    }

    public double getNoise(double x, double y, double z, double w) {
        return getConfiguredNoise((float)x, (float)y, (float)z, (float)w);
    }

    public double getNoise(double x, double y, double z, double w, double u) {
        return getConfiguredNoise((float)x, (float)y, (float)z, (float)w, (float)u);
    }

    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return getConfiguredNoise((float)x, (float)y, (float)z, (float)w, (float)u, (float)v);
    }



    public double getNoiseWithSeed(double x, double y, long seed) {
        int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        double r = getConfiguredNoise((float)x, (float)y);
        this.seed = s;
        return r;
    }

    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        double r = getConfiguredNoise((float)x, (float)y, (float)z);
        this.seed = s;
        return r;
    }

    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        double r = getConfiguredNoise((float)x, (float)y, (float)z, (float)w);
        this.seed = s;
        return r;
    }

    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        double r = getConfiguredNoise((float)x, (float)y, (float)z, (float)w, (float)u);
        this.seed = s;
        return r;
    }
    
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        double r = getConfiguredNoise((float)x, (float)y, (float)z, (float)w, (float)u, (float)v);
        this.seed = s;
        return r;
    }


    public float getNoise(float x, float y) {
        return getConfiguredNoise(x, y);
    }

    public float getNoise(float x, float y, float z) {
        return getConfiguredNoise(x, y, z);
    }

    public float getNoise(float x, float y, float z, float w) {
        return getConfiguredNoise(x, y, z, w);
    }

    public float getNoise(float x, float y, float z, float w, float u) {
        return getConfiguredNoise(x, y, z, w, u);
    }

    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getConfiguredNoise(x, y, z, w, u, v);
    }

    public float getNoiseWithSeed(float x, float y, int seed) {
        final int s = this.seed;
        this.seed = seed;
        float r = getConfiguredNoise(x, y);
        this.seed = s;
        return r;
    }
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        final int s = this.seed;
        this.seed = seed;
        float r = getConfiguredNoise(x, y, z);
        this.seed = s;
        return r;
    }
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        final int s = this.seed;
        this.seed = seed;
        float r = getConfiguredNoise(x, y, z, w);
        this.seed = s;
        return r;
    }

    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        final int s = this.seed;
        this.seed = seed;
        float r = getConfiguredNoise(x, y, z, w, u);
        this.seed = s;
        return r;
    }

    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        final int s = this.seed;
        this.seed = seed;
        float r = getConfiguredNoise(x, y, z, w, u, v);
        this.seed = s;
        return r;
    }

    public float getNoiseWithSeed(float x, float y, long seed) {
        final int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        float r = getConfiguredNoise(x, y);
        this.seed = s;
        return r;
    }
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        final int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        float r = getConfiguredNoise(x, y, z);
        this.seed = s;
        return r;
    }
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        final int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        float r = getConfiguredNoise(x, y, z, w);
        this.seed = s;
        return r;
    }

    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        final int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        float r = getConfiguredNoise(x, y, z, w, u);
        this.seed = s;
        return r;
    }

    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        final int s = this.seed;
        this.seed = (int) (seed ^ seed >>> 32);
        float r = getConfiguredNoise(x, y, z, w, u, v);
        this.seed = s;
        return r;
    }


    protected static int fastRound(float f) {
        return (int)(f + 16384.5) - 16384;
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }


    protected static float hermiteInterpolator(float t) {
        return t * t * (3 - 2 * t);
    }

    protected static float quinticInterpolator(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 9.999998f);
    }

    protected static float cubicLerp(float a, float b, float c, float d, float t) {
        float p = (d - c) - (a - b);
        return t * (t * t * p + t * ((a - b) - p) + (c - a)) + b;
    }

    private void calculateFractalBounding() {
        float amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        fractalBounding = 1 / ampFractal;
    }

    private float valCoord2D(int seed, int x, int y) {
        return (hashAll(x, y, seed)) * 0x1.0p-31f;
    }

    private float valCoord3D(int seed, int x, int y, int z) {
        return (hashAll(x, y, z, seed)) * 0x1.0p-31f;
    }

    private float valCoord4D(int seed, int x, int y, int z, int w) {
        return (hashAll(x, y, z, w, seed)) * 0x1.0p-31f;
    }

    private float valCoord5D(int seed, int x, int y, int z, int w, int u) {
        return (hashAll(x, y, z, w, u, seed)) * 0x1.0p-31f;
    }

    private float valCoord6D(int seed, int x, int y, int z, int w, int u, int v) {
        return (hashAll(x, y, z, w, u, v, seed)) * 0x1.0p-31f;
    }

    private float phCoord2D(int seed, int x, int y) {
        return (pointHash.hashWithState(x, y, seed)) * 0x1.0p-31f;
    }

    private float phCoord3D(int seed, int x, int y, int z) {
        return (pointHash.hashWithState(x, y, z, seed)) * 0x1.0p-31f;
    }

    private float phCoord4D(int seed, int x, int y, int z, int w) {
        return (pointHash.hashWithState(x, y, z, w, seed)) * 0x1.0p-31f;
    }

    private float phCoord5D(int seed, int x, int y, int z, int w, int u) {
        return (pointHash.hashWithState(x, y, z, w, u, seed)) * 0x1.0p-31f;
    }

    private float phCoord6D(int seed, int x, int y, int z, int w, int u, int v) {
        return (pointHash.hashWithState(x, y, z, w, u, v, seed)) * 0x1.0p-31f;
    }



    protected float gradCoord2D(int seed, int x, int y, float xd, float yd) {
        final int hash = hash256(x, y, seed) << 1;
        return xd * GRADIENTS_2D[hash] + yd * GRADIENTS_2D[hash + 1];
    }

    protected float gradCoord3D(int seed, int x, int y, int z, float xd, float yd, float zd) {
        final int hash = hash32(x, y, z, seed) << 2;
        return xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash+1] + zd * GRADIENTS_3D[hash+2];
    }

    protected float gradCoord4D(int seed, int x, int y, int z, int w, float xd, float yd, float zd, float wd) {
        final int hash = hash256(x, y, z, w, seed) << 2;
        return xd * GRADIENTS_4D[hash] + yd * GRADIENTS_4D[hash + 1] + zd * GRADIENTS_4D[hash + 2] + wd * GRADIENTS_4D[hash + 3];
    }

    protected float gradCoord5D(int seed, int x, int y, int z, int w, int u,
                                float xd, float yd, float zd, float wd, float ud) {
        final int hash = hash256(x, y, z, w, u, seed) << 3;
        return xd * GRADIENTS_5D[hash] + yd * GRADIENTS_5D[hash+1] + zd * GRADIENTS_5D[hash+2] + wd * GRADIENTS_5D[hash+3] + ud * GRADIENTS_5D[hash+4];
    }

    protected float gradCoord6D(int seed, int x, int y, int z, int w, int u, int v,
                                float xd, float yd, float zd, float wd, float ud, float vd) {
        final int hash = hash256(x, y, z, w, u, v, seed) << 3;
        return xd * GRADIENTS_6D[hash] + yd * GRADIENTS_6D[hash+1] + zd * GRADIENTS_6D[hash+2] +
                wd * GRADIENTS_6D[hash+3] + ud * GRADIENTS_6D[hash+4] + vd * GRADIENTS_6D[hash+5];
    }

    /**
     * Given inputs as {@code x} in the range -1.0 to 1.0 that are too biased towards 0.0, this "squashes" the range
     * softly to widen it and spread it away from 0.0 without increasing bias anywhere else. Typically this is called
     * internally by the Perlin noise code to distribute it in a specific (more backwards-compatible) way, using
     * constants such as {@link #ADD2} and {@link #MUL2} for 2D noise, or ADD3 and MUL3 for 3D noise, etc.
     * <br>
     * This starts with a common sigmoid function, {@code x / sqrt(add + x * x)}, but instead of approaching -1 and 1
     * but never reaching them, this multiplies the result so the line crosses -1 when x is -1, and crosses 1 when x is
     * 1. It has a smooth derivative, if that matters to you.
     *
     * @param x a float between -1 and 1
     * @param add if greater than 1, this will have nearly no effect; the lower this goes below 1, the more this will
     *           separate results near the center of the range. This must be greater than or equal to 0.0
     * @param mul typically the result of calling {@link #calculateEqualizeAdjustment(float)} on {@code add}
     * @return a float with a slightly different distribution from {@code x}, but still between -1 and 1
     */
    public static float equalize(float x, float add, float mul) {
        return x * mul / (float) Math.sqrt(add + x * x);
    }

    /**
     * Gets the value to optimally use for {@code mul} in {@link #equalize(float, float, float)}, given the value that
     * will be used as {@code add} there. If mul is calculated in some other way, inputs in the -1 to 1 range won't have
     * outputs in the -1 to 1 range from equalize().
     * <br>
     * This is mathematically the same as using {@code 1f / equalize(1f, add, 1f)}, but has a faster implementation.
     *
     * @param add the value that will be used as {@code add} in a call to {@link #equalize(float, float, float)}
     * @return the value to use as {@code mul} in {@link #equalize(float, float, float)}
     */
    public static float calculateEqualizeAdjustment(float add) {
        return (float) Math.sqrt(add + 1f);
    }

    // The rotation methods below work in a specific dimensionality n (like rotateX2D, which works in 2D, so n is 2 there).
    // Each takes n parameters for an n-dimensional point, and uses them to get the value for one component at a time of
    // a new n-dimensional point. These each use a fixed rotation (there is no way to change how much these rotate a
    // point), but each dimensionality rotates differently.
    // To take a 2D point "alpha" and produce a rotated 2D point "beta," you would call:
    // beta.x = rotateX2D(alpha.x, alpha.y); beta.y = rotateY2D(alpha.x, alpha.y);

    protected static float rotateX2D(float x, float y){ return x * +0.6088885514347261f + y * -0.7943553508622062f; }
    protected static float rotateY2D(float x, float y){ return x * +0.7943553508622062f + y * +0.6088885514347261f; }

    protected static float rotateX3D(float x, float y, float z){ return x * +0.0227966890756033f + y * +0.6762915140143574f + z * -0.7374004675850091f; }
    protected static float rotateY3D(float x, float y, float z){ return x * +0.2495309026014970f + y * +0.7103480212381728f + z * +0.6592220931706847f; }
    protected static float rotateZ3D(float x, float y, float z){ return x * +0.9680388783970242f + y * -0.1990510681264026f + z * -0.1525764462988358f; }

    protected static float rotateX4D(float x, float y, float z, float w){ return x * +0.5699478528112771f + y * +0.7369836852218905f + z * -0.0325828875824773f + w * -0.3639975881105405f; }
    protected static float rotateY4D(float x, float y, float z, float w){ return x * +0.1552282348051943f + y * +0.1770952336543200f + z * -0.7097702517705363f + w * +0.6650917154025483f; }
    protected static float rotateZ4D(float x, float y, float z, float w){ return x * +0.0483833371062336f + y * +0.3124109456042325f + z * +0.6948457959606478f + w * +0.6469518300143685f; }
    protected static float rotateW4D(float x, float y, float z, float w){ return x * +0.8064316315440612f + y * -0.5737907885437848f + z * +0.1179845891415618f + w * +0.0904374415002696f; }

    protected static float rotateX5D(float x, float y, float z, float w, float u){ return x * +0.1524127934921893f + y * -0.2586710352203958f + z * -0.4891826043642151f + w * +0.7663312575129502f + u * -0.2929089192051232f; }
    protected static float rotateY5D(float x, float y, float z, float w, float u){ return x * -0.0716486050004579f + y * -0.5083828718253534f + z * -0.5846508329893165f + w * -0.3242340701968086f + u * +0.5400343264823232f; }
    protected static float rotateZ5D(float x, float y, float z, float w, float u){ return x * +0.5391124130592424f + y * +0.4637201165727557f + z * -0.0268449575347777f + w * +0.2805630001516211f + u * +0.6471616940596671f; }
    protected static float rotateW5D(float x, float y, float z, float w, float u){ return x * -0.4908590743023694f + y * -0.3159190659906883f + z * +0.4868180845277980f + w * +0.4733894151555028f + u * +0.4492456287606979f; }
    protected static float rotateU5D(float x, float y, float z, float w, float u){ return x * +0.6656547456376498f + y * -0.6028584537113622f + z * +0.4289447660591045f + w * -0.0882009139887838f + u * -0.0676076855220496f; }

    protected static float rotateX6D(float x, float y, float z, float w, float u, float v){ return x * -0.0850982316788443f + y * +0.0621411489653063f + z * +0.6423842935800755f + w * +0.5472782330246069f + u * -0.5181072879831091f + v * -0.1137065126038194f; }
    protected static float rotateY6D(float x, float y, float z, float w, float u, float v){ return x * +0.1080560582151551f + y * -0.3254670556393390f + z * -0.3972292333437380f + w * +0.0964380840482216f + u * -0.5818281028726723f + v * +0.6182273380506453f; }
    protected static float rotateZ6D(float x, float y, float z, float w, float u, float v){ return x * +0.2504893307323878f + y * -0.3866469165898269f + z * -0.2346647170372642f + w * +0.7374659593233097f + u * +0.4257828596124605f + v * -0.1106816328431182f; }
    protected static float rotateW6D(float x, float y, float z, float w, float u, float v){ return x * +0.0990858373676681f + y * +0.4040947615164614f + z * +0.3012734241554820f + w * +0.1520113643725959f + u * +0.4036980496402723f + v * +0.7440701998573674f; }
    protected static float rotateU6D(float x, float y, float z, float w, float u, float v){ return x * -0.7720417581190233f + y * -0.5265151283855897f + z * +0.1995725381386031f + w * -0.0464596713813553f + u * +0.2186511264128518f + v * +0.1990962291039879f; }
    protected static float rotateV6D(float x, float y, float z, float w, float u, float v){ return x * +0.5606136879764017f + y * -0.5518123912290505f + z * +0.4997557173523122f + w * -0.3555852919481873f + u * +0.0731165180984564f + v * +0.0560452079067605f; }

    protected static float rotateX7D(float x, float y, float z, float w, float u, float v, float m){ return x * -0.0056246451693253f + y * +0.2208551678461124f + z * -0.0568956770971728f + w * -0.6432848816359004f + u * -0.3348708518986324f + v * +0.6496998046598708f + m * -0.0687675146270736f; }
    protected static float rotateY7D(float x, float y, float z, float w, float u, float v, float m){ return x * +0.0159667398563612f + y * -0.3708057332823732f + z * -0.0255290165081347f + w * -0.1074204597656161f + u * -0.6966577799927093f + v * -0.2848524769688026f + m * +0.5374576476886094f; }
    protected static float rotateZ7D(float x, float y, float z, float w, float u, float v, float m){ return x * +0.0089215457766600f + y * -0.3451906379293453f + z * +0.3653420930191292f + w * +0.5084714165020501f + u * -0.3967137045018020f + v * +0.4026281835802747f + m * -0.4199529479045513f; }
    protected static float rotateW7D(float x, float y, float z, float w, float u, float v, float m){ return x * +0.0389034073294095f + y * -0.5202531080058671f + z * -0.5396631178057520f + w * +0.1521217441742421f + u * +0.3144183583432352f + v * +0.4756642075085537f + m * +0.3037334167449179f; }
    protected static float rotateU7D(float x, float y, float z, float w, float u, float v, float m){ return x * -0.0120867392478421f + y * +0.2988470721412648f + z * +0.5341516385009615f + w * +0.2333218272366783f + u * +0.1612453247801415f + v * +0.3296939247342339f + m * +0.6629005534068590f; }
    protected static float rotateV7D(float x, float y, float z, float w, float u, float v, float m){ return x * -0.8755449392648650f + y * -0.3094947987824562f + z * +0.2386200725428088f + w * -0.2303379633118088f + u * +0.1687924564004422f + v * -0.0239773671533026f + m * -0.0167628366708356f; }
    protected static float rotateM7D(float x, float y, float z, float w, float u, float v, float m){ return x * +0.4820384791758567f + y * -0.4914996236160800f + z * +0.4837833759906052f + w * -0.4381443982669666f + u * +0.3117477166617179f + v * -0.0641025039100050f + m * -0.0491357938317362f; }

    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 2D.
     * @param x x position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param y y position, as a float; the range this should have depends on {@link #getFrequency()}
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singleValueFractalDomainWarp(x, y);
                    default:
                        return singleValueFractalFBM(x, y);
                }
            case FOAM:
                return singleFoam(seed, x, y);
            case FOAM_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y);
                    default:
                        return singleFoamFractalFBM(x, y);
                }
            case MUTANT:
                return singleFoam(seed, x, y, mutation);
            case MUTANT_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, mutation);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, mutation);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, mutation);
                    default:
                        return singleFoamFractalFBM(x, y, mutation);
                }
            case TAFFY:
                return singleTaffy(seed, x, y, mutation);
            case TAFFY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleTaffyFractalBillow(x, y, mutation);
                    case RIDGED_MULTI:
                        return singleTaffyFractalRidgedMulti(x, y, mutation);
                    case DOMAIN_WARP:
                        return singleTaffyFractalDomainWarp(x, y, mutation);
                    default:
                        return singleTaffyFractalFBM(x, y, mutation);
                }
            case HONEY:
                return singleHoney(seed, x, y);
            case HONEY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleHoneyFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleHoneyFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singleHoneyFractalDomainWarp(x, y);
                    default:
                        return singleHoneyFractalFBM(x, y);
                }
            case PERLIN:
                return singlePerlin(seed, x, y);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singlePerlinFractalDomainWarp(x, y);
                    default:
                        return singlePerlinFractalFBM(x, y);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singleSimplexFractalDomainWarp(x, y);
                    default:
                        return singleSimplexFractalFBM(x, y);
                }
            case CELLULAR:
                switch (cellularReturnType) {
                    case CELL_VALUE:
                    case NOISE_LOOKUP:
                    case DISTANCE:
                        return singleCellular(seed, x, y);
                    case DISTANCE_VALUE:
                        return singleCellularMerging(seed, x, y);
                    default:
                        return singleCellular2Edge(seed, x, y);
                }
            case CELLULAR_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCellularFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleCellularFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singleCellularFractalDomainWarp(x, y);
                    default:
                        return singleCellularFractalFBM(x, y);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y);
            case CUBIC:
                return singleCubic(seed, x, y);
            case CUBIC_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCubicFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleCubicFractalRidgedMulti(x, y);
                    case DOMAIN_WARP:
                        return singleCubicFractalDomainWarp(x, y);
                    default:
                        return singleCubicFractalFBM(x, y);
                }
            default:
                return singleSimplex(seed, x, y);
        }
    }

    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 3D.
     * @param x x position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param y y position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param z z position, as a float; the range this should have depends on {@link #getFrequency()}
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y, z);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singleValueFractalDomainWarp(x, y, z);
                    default:
                        return singleValueFractalFBM(x, y, z);
                }
            case FOAM:
                return singleFoam(seed, x, y, z);
            case FOAM_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z);
                    default:
                        return singleFoamFractalFBM(x, y, z);
                }
            case MUTANT:
                return singleFoam(seed, x, y, z, mutation);
            case MUTANT_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, mutation);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, mutation);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, mutation);
                    default:
                        return singleFoamFractalFBM(x, y, z, mutation);
                }
            case TAFFY:
                return singleTaffy(seed, x, y, z, mutation);
            case TAFFY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleTaffyFractalBillow(x, y, z, mutation);
                    case RIDGED_MULTI:
                        return singleTaffyFractalRidgedMulti(x, y, z, mutation);
                    case DOMAIN_WARP:
                        return singleTaffyFractalDomainWarp(x, y, z, mutation);
                    default:
                        return singleTaffyFractalFBM(x, y, z, mutation);
                }
            case HONEY:
                return singleHoney(seed, x, y, z);
            case HONEY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleHoneyFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleHoneyFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singleHoneyFractalDomainWarp(x, y, z);
                    default:
                        return singleHoneyFractalFBM(x, y, z);
                }
            case PERLIN:
                return singlePerlin(seed, x, y, z);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singlePerlinFractalDomainWarp(x, y, z);
                    default:
                        return singlePerlinFractalFBM(x, y, z);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singleSimplexFractalDomainWarp(x, y, z);
                    default:
                        return singleSimplexFractalFBM(x, y, z);
                }
            case CELLULAR:
                switch (cellularReturnType) {
                    case CELL_VALUE:
                    case NOISE_LOOKUP:
                    case DISTANCE:
                        return singleCellular(seed, x, y, z);
                    case DISTANCE_VALUE:
                        return singleCellularMerging(seed, x, y, z);
                    default:
                        return singleCellular2Edge(seed, x, y, z);
                }
            case CELLULAR_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCellularFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleCellularFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singleCellularFractalDomainWarp(x, y, z);
                    default:
                        return singleCellularFractalFBM(x, y, z);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y, z);
            case CUBIC:
                return singleCubic(seed, x, y, z);
            case CUBIC_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCubicFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleCubicFractalRidgedMulti(x, y, z);
                    case DOMAIN_WARP:
                        return singleCubicFractalDomainWarp(x, y, z);
                    default:
                        return singleCubicFractalFBM(x, y, z);
                }
            default:
                return singleSimplex(seed, x, y, z);
        }
    }
    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 4D.
     * @param x x position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param y y position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param z z position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param w w position, as a float; the range this should have depends on {@link #getFrequency()}
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y, float z, float w) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y, z, w);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y, z, w);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y, z, w);
                    case DOMAIN_WARP:
                        return singleValueFractalDomainWarp(x, y, z, w);
                    default:
                        return singleValueFractalFBM(x, y, z, w);
                }
            case FOAM:
                return singleFoam(seed, x, y, z, w);
            case FOAM_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, w);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, w);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, w);
                    default:
                        return singleFoamFractalFBM(x, y, z, w);
                }
            case MUTANT:
                return singleFoam(seed, x, y, z, w, mutation);
            case MUTANT_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, w, mutation);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, w, mutation);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, w, mutation);
                    default:
                        return singleFoamFractalFBM(x, y, z, w, mutation);
                }
            case TAFFY:
                return singleTaffy(seed, x, y, z, w, mutation);
            case TAFFY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleTaffyFractalBillow(x, y, z, w, mutation);
                    case RIDGED_MULTI:
                        return singleTaffyFractalRidgedMulti(x, y, z, w, mutation);
                    case DOMAIN_WARP:
                        return singleTaffyFractalDomainWarp(x, y, z, w, mutation);
                    default:
                        return singleTaffyFractalFBM(x, y, z, w, mutation);
                }
            case HONEY:
                return singleHoney(seed, x, y, z, w);
            case HONEY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleHoneyFractalBillow(x, y, z, w);
                    case RIDGED_MULTI:
                        return singleHoneyFractalRidgedMulti(x, y, z, w);
                    case DOMAIN_WARP:
                        return singleHoneyFractalDomainWarp(x, y, z, w);
                    default:
                        return singleHoneyFractalFBM(x, y, z, w);
                }
            case PERLIN:
                return singlePerlin(seed, x, y, z, w);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y, z, w);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y, z, w);
                    case DOMAIN_WARP:
                        return singlePerlinFractalDomainWarp(x, y, z, w);
                    default:
                        return singlePerlinFractalFBM(x, y, z, w);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y, z, w);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y, z, w);
                    case DOMAIN_WARP:
                        return singleSimplexFractalDomainWarp(x, y, z, w);
                    default:
                        return singleSimplexFractalFBM(x, y, z, w);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y, z, w);
            case CUBIC:
                return singleCubic(seed, x, y, z, w);
            case CUBIC_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCubicFractalBillow(x, y, z, w);
                    case RIDGED_MULTI:
                        return singleCubicFractalRidgedMulti(x, y, z, w);
                    case DOMAIN_WARP:
                        return singleCubicFractalDomainWarp(x, y, z, w);
                    default:
                        return singleCubicFractalFBM(x, y, z, w);
                }

            default:
                return singleSimplex(seed, x, y, z, w);
        }
    }

    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 5D.
     * @param x x position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param y y position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param z z position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param w w position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param u u position, as a float; the range this should have depends on {@link #getFrequency()}
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y, float z, float w, float u) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y, z, w, u);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y, z, w, u);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y, z, w, u);
                    default:
                        return singleValueFractalFBM(x, y, z, w, u);
                }
            case FOAM:
                return singleFoam(seed, x, y, z, w, u);
            case FOAM_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, w, u);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, w, u);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, w, u);
                    default:
                        return singleFoamFractalFBM(x, y, z, w, u);
                }
            case MUTANT:
                return singleFoam(seed, x, y, z, w, u, mutation);
            case MUTANT_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, w, u, mutation);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, w, u, mutation);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, w, u, mutation);
                    default:
                        return singleFoamFractalFBM(x, y, z, w, u, mutation);
                }
            case TAFFY:
                return singleTaffy(seed, x, y, z, w, u, mutation);
            case TAFFY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleTaffyFractalBillow(x, y, z, w, u, mutation);
                    case RIDGED_MULTI:
                        return singleTaffyFractalRidgedMulti(x, y, z, w, u, mutation);
                    case DOMAIN_WARP:
                        return singleTaffyFractalDomainWarp(x, y, z, w, u, mutation);
                    default:
                        return singleTaffyFractalFBM(x, y, z, w, u, mutation);
                }
            case HONEY:
                return singleHoney(seed, x, y, z, w, u);
            case HONEY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleHoneyFractalBillow(x, y, z, w, u);
                    case RIDGED_MULTI:
                        return singleHoneyFractalRidgedMulti(x, y, z, w, u);
                    case DOMAIN_WARP:
                        return singleHoneyFractalDomainWarp(x, y, z, w, u);
                    default:
                        return singleHoneyFractalFBM(x, y, z, w, u);
                }
            case PERLIN:
                return singlePerlin(seed, x, y, z, w, u);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y, z, w, u);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y, z, w, u);
                    case DOMAIN_WARP:
                        return singlePerlinFractalDomainWarp(x, y, z, w, u);
                    default:
                        return singlePerlinFractalFBM(x, y, z, w, u);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y, z, w, u);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y, z, w, u);
                    case DOMAIN_WARP:
                        return singleSimplexFractalDomainWarp(x, y, z, w, u);
                    default:
                        return singleSimplexFractalFBM(x, y, z, w, u);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y, z, w, u);
            default:
                return singleSimplex(seed, x, y, z, w, u);
        }
    }

    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 6D.
     * @param x x position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param y y position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param z z position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param w w position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param u u position, as a float; the range this should have depends on {@link #getFrequency()}
     * @param v v position, as a float; the range this should have depends on {@link #getFrequency()}
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y, float z, float w, float u, float v) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y, z, w, u, v);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y, z, w, u, v);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y, z, w, u, v);
                    case DOMAIN_WARP:
                        return singleValueFractalDomainWarp(x, y, z, w, u, v);
                    default:
                        return singleValueFractalFBM(x, y, z, w, u, v);
                }
            case FOAM:
                return singleFoam(seed, x, y, z, w, u, v);
            case FOAM_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, w, u, v);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, w, u, v);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, w, u, v);
                    default:
                        return singleFoamFractalFBM(x, y, z, w, u, v);
                }
            case MUTANT:
                return singleFoam(seed, x, y, z, w, u, v, mutation);
            case MUTANT_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleFoamFractalBillow(x, y, z, w, u, v, mutation);
                    case RIDGED_MULTI:
                        return singleFoamFractalRidgedMulti(x, y, z, w, u, v, mutation);
                    case DOMAIN_WARP:
                        return singleFoamFractalDomainWarp(x, y, z, w, u, v, mutation);
                    default:
                        return singleFoamFractalFBM(x, y, z, w, u, v, mutation);
                }
            case TAFFY:
                return singleTaffy(seed, x, y, z, w, u, v, mutation);
            case TAFFY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleTaffyFractalBillow(x, y, z, w, u, v, mutation);
                    case RIDGED_MULTI:
                        return singleTaffyFractalRidgedMulti(x, y, z, w, u, v, mutation);
                    case DOMAIN_WARP:
                        return singleTaffyFractalDomainWarp(x, y, z, w, u, v, mutation);
                    default:
                        return singleTaffyFractalFBM(x, y, z, w, u, v, mutation);
                }
            case HONEY:
                return singleHoney(seed, x, y, z, w, u, v);
            case HONEY_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleHoneyFractalBillow(x, y, z, w, u, v);
                    case RIDGED_MULTI:
                        return singleHoneyFractalRidgedMulti(x, y, z, w, u, v);
                    case DOMAIN_WARP:
                        return singleHoneyFractalDomainWarp(x, y, z, w, u, v);
                    default:
                        return singleHoneyFractalFBM(x, y, z, w, u, v);
                }
            case PERLIN:
                return singlePerlin(seed, x, y, z, w, u, v);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y, z, w, u, v);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y, z, w, u, v);
                    case DOMAIN_WARP:
                        return singlePerlinFractalDomainWarp(x, y, z, w, u, v);
                    default:
                        return singlePerlinFractalFBM(x, y, z, w, u, v);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y, z, w, u, v);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y, z, w, u, v);
                    case DOMAIN_WARP:
                        return singleSimplexFractalDomainWarp(x, y, z, w, u, v);
                    default:
                        return singleSimplexFractalFBM(x, y, z, w, u, v);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y, z, w, u, v);
            default:
                return singleSimplex(seed, x, y, z, w, u, v);
        }
    }

    // White Noise

    private int floatToIntMixed(final float f) {
        final int i = BitConversion.floatToIntBits(f);
        return i ^ i >>> 16;
    }

    public float getWhiteNoise(float x, float y) {
        int xi = floatToIntMixed(x);
        int yi = floatToIntMixed(y);

        return valCoord2D(seed, xi, yi);
    }

    public float getWhiteNoise(float x, float y, float z) {
        int xi = floatToIntMixed(x);
        int yi = floatToIntMixed(y);
        int zi = floatToIntMixed(z);

        return valCoord3D(seed, xi, yi, zi);
    }
    
    public float getWhiteNoise(float x, float y, float z, float w) {
        int xi = floatToIntMixed(x);
        int yi = floatToIntMixed(y);
        int zi = floatToIntMixed(z);
        int wi = floatToIntMixed(w);

        return valCoord4D(seed, xi, yi, zi, wi);
    }
    
    public float getWhiteNoise(float x, float y, float z, float w, float u) {
        int xi = floatToIntMixed(x);
        int yi = floatToIntMixed(y);
        int zi = floatToIntMixed(z);
        int wi = floatToIntMixed(w);
        int ui = floatToIntMixed(u);

        return valCoord5D(seed, xi, yi, zi, wi, ui);
    }

    public float getWhiteNoise(float x, float y, float z, float w, float u, float v) {
        int xi = floatToIntMixed(x);
        int yi = floatToIntMixed(y);
        int zi = floatToIntMixed(z);
        int wi = floatToIntMixed(w);
        int ui = floatToIntMixed(u);
        int vi = floatToIntMixed(v);

        return valCoord6D(seed, xi, yi, zi, wi, ui, vi);
    }
    
    // Value Noise
    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >> 22;
    }
    //x should be premultiplied by 0xDB4F1
    //y should be premultiplied by 0xBBE05
    //z should be premultiplied by 0xA0F2F
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >> 22;
    }
    //x should be premultiplied by 0xE19B1
    //y should be premultiplied by 0xC6D1D
    //z should be premultiplied by 0xAF36D
    //w should be premultiplied by 0x9A695
    private static int hashPart1024(final int x, final int y, final int z, final int w, int s) {
        s += x ^ y ^ z ^ w;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >> 22;
    }

    //x should be premultiplied by 0xE60E3
    //y should be premultiplied by 0xCEBD7
    //z should be premultiplied by 0xB9C9B
    //w should be premultiplied by 0xA6F57
    //u should be premultiplied by 0x9609D
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, int s) {
        s += x ^ y ^ z ^ w ^ u;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >> 22;
    }

    //x should be premultiplied by 0xE95E1
    //y should be premultiplied by 0xD4BC7
    //z should be premultiplied by 0xC1EDB
    //w should be premultiplied by 0xB0C8B
    //u should be premultiplied by 0xA127B
    //v should be premultiplied by 0x92E85
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >> 22;
    }

    //0xEBEDF 0xD96EB 0xC862B 0xB8ACD 0xAA323 0x9CDA5 0x908E3
    //x should be premultiplied by 0xEBEDF
    //y should be premultiplied by 0xD96EB
    //z should be premultiplied by 0xC862B
    //w should be premultiplied by 0xB8ACD
    //u should be premultiplied by 0xAA323
    //v should be premultiplied by 0x9CDA5
    //m should be premultiplied by 0x908E3
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, final int m, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v ^ m;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >> 22;
    }


    /**
     * A smooth 1D noise function that produces results between 0.0 and 1.0, and is optimized for
     * usage on GWT. This uses cubic interpolation between random peak or valley points.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between 0f and 1f (both inclusive), smoothly changing with value
     */
    public static float wobbleTight(final int seed, float value)
    {
        int floor = MathTools.fastFloor(value);
        int z = seed + floor, x = z++;
        final float start = ((x = ((x = (x ^ x >>> 16) * 0x21fad) ^ x >>> 15) * 0x73597) >>> 1 ^ x >>> 16) * 0x1p-31f,
                end = ((x = ((x = (z ^ z >>> 16) * 0x21fad) ^ x >>> 15) * 0x73597) >>> 1 ^ x >>> 16) * 0x1p-31f;
        value -= floor;
        value *= value * (3 - 2 * value);
        return (1 - value) * start + value * end;
    }

    // Value noise.

    public float getValueFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleValueFractalFBM(x, y);
            case BILLOW:
                return singleValueFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    protected float singleValueFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleValue(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = singleValue(seed, x, y);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleValue(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleValue(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getValue(float x, float y) {
        return singleValue(seed, x * frequency, y * frequency);
    }


    public float singleValue (int seed, float x, float y) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        switch (interpolation) {
            case HERMITE:
                x = hermiteInterpolator(x);
                y = hermiteInterpolator(y);
                break;
            case QUINTIC:
                x = quinticInterpolator(x);
                y = quinticInterpolator(y);
                break;
        }
        xFloor *= 0xD1B55;
        yFloor *= 0xABC99;
        return ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor, seed))
                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xABC99, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, seed)))
                * 0x1p-9f;
    }

    /**
     * Produces noise from 0 to 1, instead of the normal -1 to 1.
     * @param seed
     * @param x
     * @param y
     * @return noise from 0 to 1.
     */
    protected float valueNoise (int seed, float x, float y) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= 0xD1B55;
        yFloor *= 0xABC99;
        return ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor, seed))
                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xABC99, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, seed)))
                * 0x1p-10f + 0.5f;
    }
    public float getValueFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case BILLOW:
                return singleValueFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y, z);
            default:
                return singleValueFractalFBM(x, y, z);
        }
    }

    protected float singleValueFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleValue(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleValueFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singleValue(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleValue(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleValueFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleValue(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getValue(float x, float y, float z) {
        return singleValue(seed, x * frequency, y * frequency, z * frequency);
    }

    public float singleValue(int seed, float x, float y, float z) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        switch (interpolation) {
            case HERMITE:
                x = hermiteInterpolator(x);
                y = hermiteInterpolator(y);
                z = hermiteInterpolator(z);
                break;
            case QUINTIC:
                x = quinticInterpolator(x);
                y = quinticInterpolator(y);
                z = quinticInterpolator(z);
                break;
        }
        //0xDB4F1, 0xBBE05, 0xA0F2F
        xFloor *= 0xDB4F1;
        yFloor *= 0xBBE05;
        zFloor *= 0xA0F2F;
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor, zFloor, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xBBE05, zFloor, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor + 0xBBE05, zFloor, seed)))
                + z *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xA0F2F, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor, zFloor + 0xA0F2F, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xBBE05, zFloor + 0xA0F2F, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor + 0xBBE05, zFloor + 0xA0F2F, seed)))
        ) * 0x1p-9f;
    }
    /**
     * Produces noise from 0 to 1, instead of the normal -1 to 1.
     * @param seed
     * @param x
     * @param y
     * @param z
     * @return noise from 0 to 1.
     */
    protected float valueNoise(int seed, float x, float y, float z)
    {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        //0xDB4F1, 0xBBE05, 0xA0F2F
        xFloor *= 0xDB4F1;
        yFloor *= 0xBBE05;
        zFloor *= 0xA0F2F;
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor, zFloor, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xBBE05, zFloor, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor + 0xBBE05, zFloor, seed)))
                + z *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xA0F2F, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor, zFloor + 0xA0F2F, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xBBE05, zFloor + 0xA0F2F, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor + 0xBBE05, zFloor + 0xA0F2F, seed)))
        ) * 0x1p-10f + 0.5f;

    }
    public float getValueFractal(float x, float y, float z, float w) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;

        switch (fractalType) {
            case BILLOW:
                return singleValueFractalBillow(x, y, z, w);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y, z, w);
            default:
                return singleValueFractalFBM(x, y, z, w);
        }
    }

    protected float singleValueFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleValue(++seed, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleValueFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singleValue(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleValue(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleValue(seed + i, x, y, z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getValue(float x, float y, float z, float w) {
        return singleValue(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    public float singleValue(int seed, float x, float y, float z, float w) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        switch (interpolation) {
            case HERMITE:
                x = hermiteInterpolator(x);
                y = hermiteInterpolator(y);
                z = hermiteInterpolator(z);
                w = hermiteInterpolator(w);
                break;
            case QUINTIC:
                x = quinticInterpolator(x);
                y = quinticInterpolator(y);
                z = quinticInterpolator(z);
                w = quinticInterpolator(w);
                break;
        }
        //0xE19B1, 0xC6D1D, 0xAF36D, 0x9A695
        xFloor *= 0xE19B1;
        yFloor *= 0xC6D1D;
        zFloor *= 0xAF36D;
        wFloor *= 0x9A695;
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor, wFloor, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xAF36D, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor + 0xAF36D, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor, seed))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor, wFloor + 0x9A695, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor, wFloor + 0x9A695, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xAF36D, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor + 0xAF36D, wFloor + 0x9A695, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor + 0x9A695, seed)))
                ))) * 0x1p-9f;
    }
    protected float valueNoise(int seed, float x, float y, float z, float w)
    {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        //0xE19B1, 0xC6D1D, 0xAF36D, 0x9A695
        xFloor *= 0xE19B1;
        yFloor *= 0xC6D1D;
        zFloor *= 0xAF36D;
        wFloor *= 0x9A695;
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor, wFloor, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xAF36D, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor + 0xAF36D, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor, seed))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor, wFloor + 0x9A695, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor, wFloor + 0x9A695, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xAF36D, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor + 0xAF36D, wFloor + 0x9A695, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor + 0x9A695, seed)))
                ))) * 0x1p-10f + 0.5f;
    }

    public float getValueFractal(float x, float y, float z, float w, float u) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        switch (fractalType) {
            case BILLOW:
                return singleValueFractalBillow(x, y, z, w, u);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y, z, w, u);
            default:
                return singleValueFractalFBM(x, y, z, w, u);
        }
    }
    protected float singleValueFractalFBM(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y, z, w, u);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            
            amp *= gain;
            sum += singleValue(++seed, x, y, z, w, u) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleValueFractalDomainWarp(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float latest = singleValue(seed, x, y, z, w, u);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleValue(++seed, x + a, y + b, z + c, w + d, u + e)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalBillow(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y, z, w, u)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y, z, w, u)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalRidgedMulti(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleValue(seed + i, x, y, z, w, u));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getValue(float x, float y, float z, float w, float u) {
        return singleValue(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency);
    }

    public float singleValue(int seed, float x, float y, float z, float w, float u) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        switch (interpolation) {
            case HERMITE:
                x = hermiteInterpolator(x);
                y = hermiteInterpolator(y);
                z = hermiteInterpolator(z);
                w = hermiteInterpolator(w);
                u = hermiteInterpolator(u);
                break;
            case QUINTIC:
                x = quinticInterpolator(x);
                y = quinticInterpolator(y);
                z = quinticInterpolator(z);
                w = quinticInterpolator(w);
                u = quinticInterpolator(u);
                break; 
        }
        //0xE60E3, 0xCEBD7, 0xB9C9B, 0xA6F57, 0x9609D, 0x86D51
        xFloor *= 0xE60E3;
        yFloor *= 0xCEBD7;
        zFloor *= 0xB9C9B;
        wFloor *= 0xA6F57;
        uFloor *= 0x9609D;
        return ((1 - u) *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor + 0xA6F57, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed)))
                        )))
                + (u *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor, uFloor + 0x9609D, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed)))
                        ))))
        ) * 0x1p-9f;
    }

    protected float valueNoise(int seed, float x, float y, float z, float w, float u) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); /* Won't go outside 0f to 1f range. */
        //0xE60E3, 0xCEBD7, 0xB9C9B, 0xA6F57, 0x9609D, 0x86D51
        xFloor *= 0xE60E3;
        yFloor *= 0xCEBD7;
        zFloor *= 0xB9C9B;
        wFloor *= 0xA6F57;
        uFloor *= 0x9609D;
        return ((1 - u) *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor + 0xA6F57, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed)))
                        )))
                + (u *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor, uFloor + 0x9609D, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed)))
                        ))))
        ) * 0x1p-10f + 0.5f;
    }

    public float getValueFractal(float x, float y, float z, float w, float u, float v) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        switch (fractalType) {
            case BILLOW:
                return singleValueFractalBillow(x, y, z, w, u, v);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y, z, w, u, v);
            default:
                return singleValueFractalFBM(x, y, z, w, u, v);
        }
    }
    protected float singleValueFractalFBM(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y, z, w, u, v);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += singleValue(++seed, x, y, z, w, u, v) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalDomainWarp(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float latest = singleValue(seed, x, y, z, w, u, v);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleValue(++seed, x + a, y + b, z + c, w + d, u + e, v + f)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalBillow(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y, z, w, u, v)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y, z, w, u, v)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleValueFractalRidgedMulti(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleValue(seed + i, x, y, z, w, u, v));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getValue(float x, float y, float z, float w, float u, float v) {
        return singleValue(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency);
    }

    public float singleValue(int seed, float x, float y, float z, float w, float u, float v) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        int vFloor = MathTools.fastFloor(v);
        v -= vFloor;
        switch (interpolation) {
            case HERMITE:
                x = hermiteInterpolator(x);
                y = hermiteInterpolator(y);
                z = hermiteInterpolator(z);
                w = hermiteInterpolator(w);
                u = hermiteInterpolator(u);
                v = hermiteInterpolator(v);
                break;
            case QUINTIC:
                x = quinticInterpolator(x);
                y = quinticInterpolator(y);
                z = quinticInterpolator(z);
                w = quinticInterpolator(w);
                u = quinticInterpolator(u);
                v = quinticInterpolator(v);
                break;
        }
        //0xE95E1, 0xD4BC7, 0xC1EDB, 0xB0C8B, 0xA1279, 0x92E85
        xFloor *= 0xE95E1;
        yFloor *= 0xD4BC7;
        zFloor *= 0xC1EDB;
        wFloor *= 0xB0C8B;
        uFloor *= 0xA127B;
        vFloor *= 0x92E85;
        return ((1 - v) *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                ))))))
        ) * 0x1p-9f;
    }

    protected float valueNoise(int seed, float x, float y, float z, float w, float u, float v) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); /* Won't go outside 0f to 1f range. */
        int vFloor = MathTools.fastFloor(v);
        v -= vFloor;
        v *= v * (1 - v - v + 2); /* Won't go outside 0f to 1f range. */
        //0xE95E1, 0xD4BC7, 0xC1EDB, 0xB0C8B, 0xA1279, 0x92E85
        xFloor *= 0xE95E1;
        yFloor *= 0xD4BC7;
        zFloor *= 0xC1EDB;
        wFloor *= 0xB0C8B;
        uFloor *= 0xA127B;
        vFloor *= 0x92E85;
        return ((1 - v) *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                ))))))
        ) * 0x1p-10f + 0.5f;
    }

    protected float valueNoise(int seed, float x, float y, float z, float w, float u, float v, float m) {
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); /* Won't go outside 0f to 1f range. */
        int vFloor = MathTools.fastFloor(v);
        v -= vFloor;
        v *= v * (1 - v - v + 2); /* Won't go outside 0f to 1f range. */
        int mFloor = MathTools.fastFloor(m);
        m -= mFloor;
        m *= m * (1 - m - m + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= 0xEBEDF;
        yFloor *= 0xD96EB;
        zFloor *= 0xC862B;
        wFloor *= 0xB8ACD;
        uFloor *= 0xAA323;
        vFloor *= 0x9CDA5;
        mFloor *= 0x908E3;
        return
                ((1 - m) *
                        ((1 - v) *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor, seed)))
                                                )))))
                                + (v *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor, seed)))
                                                )))))))
                        + (m *
                        ((1 - v) *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor, mFloor + 0x908E3, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor, mFloor + 0x908E3, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor, mFloor + 0x908E3, seed)))
                                                )))))
                                + (v *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor, vFloor + 0x9CDA5, mFloor + 0x908E3, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed) + x * hashPart1024(xFloor + 0xEBEDF, yFloor + 0xD96EB, zFloor + 0xC862B, wFloor + 0xB8ACD, uFloor + 0xAA323, vFloor + 0x9CDA5, mFloor + 0x908E3, seed)))
                                                ))))))))
                ) * 0x1p-10f + 0.5f;
    }

    // Foam Noise

    public float getFoam(float x, float y) {
        return singleFoam(seed, x * frequency, y * frequency);
    }

    public float singleFoam(int seed, float x, float y) {
        final float p0 = x;
        final float p1 = x * -0.5f + y * 0.8660254037844386f;
        final float p2 = x * -0.5f + y * -0.8660254037844387f;

        float xin = p2;
        float yin = p0;
        final float a = valueNoise(seed, xin, yin);
        seed += 0x9E377;
        xin = p1;
        yin = p2;
        final float b = valueNoise(seed, xin + a, yin);
        seed += 0x9E377;
        xin = p0;
        yin = p1;
        final float c = valueNoise(seed, xin + b, yin);
        final float result = (a + b + c) * F3;
        final float sharp = sharpness * 2.2f;

        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    public float getFoamFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleFoamFractalFBM(x, y);
            case BILLOW:
                return singleFoamFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleFoamFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    protected float singleFoamFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleFoam(seed, x, y);
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;

            amp *= gain;
            sum += singleFoam(seed + i, x, y) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleFoamFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = singleFoam(seed, x, y);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleFoam(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleFoam(seed, x, y)) * 2 - 1;
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;

            amp *= gain;
            sum += (Math.abs(singleFoam(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float t;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleFoam(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;
        }
        return sum * 2f / correction - 1f;
    }


    public float getFoamFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleFoamFractalFBM(x, y, z);
            case BILLOW:
                return singleFoamFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleFoamFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    protected float singleFoamFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleFoam(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleFoam(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleFoamFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singleFoam(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleFoam(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleFoam(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleFoam(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleFoam(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getFoam(float x, float y, float z) {
        return singleFoam(seed, x * frequency, y * frequency, z * frequency);
    }

    public float singleFoam(int seed, float x, float y, float z){
        final float p0 = x;
        final float p1 = x * -0.3333333333333333f + y * 0.9428090415820634f;
        final float p2 = x * -0.3333333333333333f + y * -0.4714045207910317f + z * 0.816496580927726f;
        final float p3 = x * -0.3333333333333333f + y * -0.4714045207910317f + z * -0.816496580927726f;

        float xin = p3;
        float yin = p2;
        float zin = p0;
        final float a = valueNoise(seed, xin, yin, zin);
        seed += 0x9E377;
        xin = p0;
        yin = p1;
        zin = p3;
        final float b = valueNoise(seed, xin + a, yin, zin);
        seed += 0x9E377;
        xin = p1;
        yin = p2;
        zin = p3;
        final float c = valueNoise(seed, xin + b, yin, zin);
        seed += 0x9E377;
        xin = p0;
        yin = p1;
        zin = p2;
        final float d = valueNoise(seed, xin + c, yin, zin);

        final float result = (a + b + c + d) * 0.25f;
        final float sharp = sharpness * 3.3f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;

    }


    protected float singleFoamFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleFoam(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleFoam(++seed, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleFoamFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singleFoam(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleFoam(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleFoam(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleFoam(++seed, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleFoam(seed + i, x, y,  z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getFoam(float x, float y, float z, float w) {
        return singleFoam(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    public float singleFoam(int seed, float x, float y, float z, float w) {
        final float p0 = x;
        final float p1 = x * -0.25f + y *  0.9682458365518543f;
        final float p2 = x * -0.25f + y * -0.3227486121839514f + z *  0.91287092917527690f;
        final float p3 = x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w *  0.7905694150420949f;
        final float p4 = x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * -0.7905694150420947f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        final float a = valueNoise(seed, xin, yin, zin, win);
        seed += 0x9E377;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final float b = valueNoise(seed, xin + a, yin, zin, win);
        seed += 0x9E377;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final float c = valueNoise(seed, xin + b, yin, zin, win);
        seed += 0x9E377;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final float d = valueNoise(seed, xin + c, yin, zin, win);
        seed += 0x9E377;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final float e = valueNoise(seed, xin + d, yin, zin, win);

        final float result = (a + b + c + d + e) * 0.2f;
        final float sharp = sharpness * 4.4f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }
    public float getFoamFractal(float x, float y, float z, float w, float u) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        switch (fractalType) {
            case FBM:
                return singleFoamFractalFBM(x, y, z, w, u);
            case BILLOW:
                return singleFoamFractalBillow(x, y, z, w, u);
            case RIDGED_MULTI:
                return singleFoamFractalRidgedMulti(x, y, z, w, u);
            default:
                return 0;
        }
    }

    protected float singleFoamFractalFBM(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = singleFoam(seed, x, y, z, w, u);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += singleFoam(seed + i, x, y, z, w, u) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleFoamFractalDomainWarp(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float latest = singleFoam(seed, x, y, z, w, u);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleFoam(++seed, x + a, y + b, z + c, w + d, u + e)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleFoamFractalBillow(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = Math.abs(singleFoam(seed, x, y, z, w, u)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleFoam(seed + i, x, y, z, w, u)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalRidgedMulti(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleFoam(seed + i, x, y, z, w, u));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getFoam(float x, float y, float z, float w, float u) {
        return singleFoam(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency);
    }

    public float singleFoam(int seed, float x, float y, float z, float w, float u) {
        final float p0 = x *  0.8157559148337911f + y *  0.5797766823136037f;
        final float p1 = x * -0.7314923478726791f + y *  0.6832997137249108f;
        final float p2 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * 0.9486832980505138f;
        final float p3 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w *   0.8944271909999159f;
        final float p4 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u *  0.7745966692414833f;
        final float p5 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u * -0.7745966692414836f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        float uin = p5;
        final float a = valueNoise(seed, xin, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final float b = valueNoise(seed, xin + a, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final float c = valueNoise(seed, xin + b, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final float d = valueNoise(seed, xin + c, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final float e = valueNoise(seed, xin + d, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final float f = valueNoise(seed, xin + e, yin, zin, win, uin);

        final float result = (a + b + c + d + e + f) * 0.16666666666666666f;
        final float sharp = sharpness * 5.5f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }
    
    public float getFoamFractal(float x, float y, float z, float w, float u, float v) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        switch (fractalType) {
            case FBM:
                return singleFoamFractalFBM(x, y, z, w, u, v);
            case BILLOW:
                return singleFoamFractalBillow(x, y, z, w, u, v);
            case RIDGED_MULTI:
                return singleFoamFractalRidgedMulti(x, y, z, w, u, v);
            default:
                return 0;
        }
    }

    protected float singleFoamFractalFBM(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = singleFoam(seed, x, y, z, w, u, v);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += singleFoam(++seed, x, y, z, w, u, v) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalDomainWarp(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float latest = singleFoam(seed, x, y, z, w, u, v);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleFoam(++seed, x + a, y + b, z + c, w + d, u + e, v + f)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalBillow(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = Math.abs(singleFoam(seed, x, y, z, w, u, v)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleFoam(++seed, x, y, z, w, u, v)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalRidgedMulti(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleFoam(seed + i, x, y, z, w, u, v));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getFoam(float x, float y, float z, float w, float u, float v) {
        return singleFoam(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency);
    }

    public float singleFoam(int seed, float x, float y, float z, float w, float u, float v) {
        final float p0 = x;
        final float p1 = x * -0.16666666666666666f + y *  0.98601329718326940f;
        final float p2 = x * -0.16666666666666666f + y * -0.19720265943665383f + z *  0.96609178307929590f;
        final float p3 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w *  0.93541434669348530f;
        final float p4 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u *  0.8819171036881969f;
        final float p5 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v *  0.7637626158259734f;
        final float p6 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v * -0.7637626158259732f;
        float xin = p0;
        float yin = p5;
        float zin = p3;
        float win = p6;
        float uin = p1;
        float vin = p4;
        final float a = valueNoise(seed, xin, yin, zin, win, uin, vin);
        seed += 0x9E377;
        xin = p2;
        yin = p6;
        zin = p0;
        win = p4;
        uin = p5;
        vin = p3;
        final float b = valueNoise(seed, xin + a, yin, zin, win, uin, vin);
        seed += 0x9E377;
        xin = p1;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p6;
        vin = p5;
        final float c = valueNoise(seed, xin + b, yin, zin, win, uin, vin);
        seed += 0x9E377;
        xin = p6;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p1;
        final float d = valueNoise(seed, xin + c, yin, zin, win, uin, vin);
        seed += 0x9E377;
        xin = p2;
        yin = p1;
        zin = p5;
        win = p0;
        uin = p3;
        vin = p6;
        final float e = valueNoise(seed, xin + d, yin, zin, win, uin, vin);
        seed += 0x9E377;
        xin = p0;
        yin = p4;
        zin = p6;
        win = p3;
        uin = p1;
        vin = p2;
        final float f = valueNoise(seed, xin + e, yin, zin, win, uin, vin);
        seed += 0x9E377;
        xin = p5;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p0;
        final float g = valueNoise(seed, xin + f, yin, zin, win, uin, vin);
        final float result = (a + b + c + d + e + f + g) * 0.14285714285714285f;
        final float sharp = sharpness * 6.6f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    protected float singleFoamFractalFBM(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float sum = singleFoam(seed, x, y, z, w, u, v, m);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
            m *= lacunarity;

            amp *= gain;
            sum += singleFoam(++seed, x, y, z, w, u, v, m) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleFoamFractalDomainWarp(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float latest = singleFoam(seed, x, y, z, w, u, v, m);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            m = m * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 7) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 7) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 7) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 7) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 7) & TrigTools.TABLE_MASK];
            float g = TrigTools.SIN_TABLE[idx + (8192 * 6 / 7) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleFoam(++seed, x + a, y + b, z + c, w + d, u + e, v + f, m + g)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalBillow(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float sum = Math.abs(singleFoam(seed, x, y, z, w, u, v, m)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
            m *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleFoam(++seed, x, y, z, w, u, v, m)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleFoamFractalRidgedMulti(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleFoam(seed + i, x, y, z, w, u, v, m));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
            m *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float singleFoam(int seed, float x, float y, float z, float w, float u, float v, float m) {
        final float p0 = x;
        final float p1 = x * -0.14285714285714285f + y * +0.9897433186107870f;
        final float p2 = x * -0.14285714285714285f + y * -0.1649572197684645f + z * +0.97590007294853320f;
        final float p3 = x * -0.14285714285714285f + y * -0.1649572197684645f + z * -0.19518001458970663f + w * +0.95618288746751490f;
        final float p4 = x * -0.14285714285714285f + y * -0.1649572197684645f + z * -0.19518001458970663f + w * -0.23904572186687872f + u * +0.92582009977255150f;
        final float p5 = x * -0.14285714285714285f + y * -0.1649572197684645f + z * -0.19518001458970663f + w * -0.23904572186687872f + u * -0.30860669992418377f + v * +0.8728715609439696f;
        final float p6 = x * -0.14285714285714285f + y * -0.1649572197684645f + z * -0.19518001458970663f + w * -0.23904572186687872f + u * -0.30860669992418377f + v * -0.4364357804719847f + m * +0.7559289460184545f;
        final float p7 = x * -0.14285714285714285f + y * -0.1649572197684645f + z * -0.19518001458970663f + w * -0.23904572186687872f + u * -0.30860669992418377f + v * -0.4364357804719847f + m * -0.7559289460184544f;
        float xin = p0;
        float yin = p6;
        float zin = p3;
        float win = p7;
        float uin = p1;
        float vin = p4;
        float min = p5;
        final float a = valueNoise(seed, xin, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p2;
        yin = p3;
        zin = p0;
        win = p4;
        uin = p6;
        vin = p5;
        min = p7;
        final float b = valueNoise(seed, xin + a, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p1;
        yin = p2;
        zin = p4;
        win = p3;
        uin = p5;
        vin = p7;
        min = p6;
        final float c = valueNoise(seed, xin + b, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p7;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p6;
        min = p1;
        final float d = valueNoise(seed, xin + c, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p3;
        yin = p1;
        zin = p5;
        win = p6;
        uin = p7;
        vin = p0;
        min = p2;
        final float e = valueNoise(seed, xin + d, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p4;
        yin = p7;
        zin = p6;
        win = p2;
        uin = p0;
        vin = p1;
        min = p3;
        final float f = valueNoise(seed, xin + e, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p5;
        yin = p4;
        zin = p7;
        win = p1;
        uin = p2;
        vin = p3;
        min = p0;
        final float g = valueNoise(seed, xin + f, yin, zin, win, uin, vin, min);
        seed += 0x9E377;
        xin = p6;
        yin = p5;
        zin = p1;
        win = p0;
        uin = p3;
        vin = p2;
        min = p4;
        final float h = valueNoise(seed, xin + g, yin, zin, win, uin, vin, min);
        final float result = (a + b + c + d + e + f + g + h) * 0.125f;
        final float sharp = sharpness * 7.7f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    // Taffy Noise

    public float getTaffy(float x, float y) {
        return singleTaffy(seed, x * frequency, y * frequency);
    }

    protected float trillNoise(int seed, float x, float y) {
        int sx = seed, sy = (seed << 13 | seed >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + x * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * x)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        return sinTurns(sum * 0.16f);
    }

    protected float trillNoise(int seed, float x, float y, float z) {
        int sx = seed, sy = (sx << 13 | sx >>> 19) + 1234567, sz = (sy << 13 | sy >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + z * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * z)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        idx = sz + (int) (z * 95 + x * 21);
        sum += (cos(z)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sz & TABLE_MASK] * x)
                + sin((SIN_TABLE[sz + 4096 & TABLE_MASK]) * z)
        );
        return sinTurns(sum * 0.15f);
    }

    protected float trillNoise(int seed, float x, float y, float z, float w) {
        int sx = seed, sy = (sx << 13 | sx >>> 19) + 1234567,
                sz = (sy << 13 | sy >>> 19) + 1234567,
                sw = (sz << 13 | sz >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + z * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * z)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        idx = sz + (int) (z * 95 + w * 21);
        sum += (cos(z)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sz & TABLE_MASK] * w)
                + sin((SIN_TABLE[sz + 4096 & TABLE_MASK]) * z)
        );
        idx = sw + (int) (w * 95 + x * 21);
        sum += (cos(w)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sw & TABLE_MASK] * x)
                + sin((SIN_TABLE[sw + 4096 & TABLE_MASK]) * w)
        );
        return sinTurns(sum * 0.14f);
    }

    protected float trillNoise(int seed, float x, float y, float z, float w, float u) {
        int sx = seed, sy = (sx << 13 | sx >>> 19) + 1234567,
                sz = (sy << 13 | sy >>> 19) + 1234567,
                sw = (sz << 13 | sz >>> 19) + 1234567,
                su = (sw << 13 | sw >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + z * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * z)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        idx = sz + (int) (z * 95 + w * 21);
        sum += (cos(z)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sz & TABLE_MASK] * w)
                + sin((SIN_TABLE[sz + 4096 & TABLE_MASK]) * z)
        );
        idx = sw + (int) (w * 95 + u * 21);
        sum += (cos(w)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sw & TABLE_MASK] * u)
                + sin((SIN_TABLE[sw + 4096 & TABLE_MASK]) * w)
        );
        idx = su + (int) (u * 95 + x * 21);
        sum += (cos(u)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[su & TABLE_MASK] * x)
                + sin((SIN_TABLE[su + 4096 & TABLE_MASK]) * u)
        );
        return sinTurns(sum * 0.13f);
    }

    protected float trillNoise(int seed, float x, float y, float z, float w, float u, float v) {
        int sx = seed, sy = (sx << 13 | sx >>> 19) + 1234567,
                sz = (sy << 13 | sy >>> 19) + 1234567,
                sw = (sz << 13 | sz >>> 19) + 1234567,
                su = (sw << 13 | sw >>> 19) + 1234567,
                sv = (su << 13 | su >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + z * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * z)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        idx = sz + (int) (z * 95 + w * 21);
        sum += (cos(z)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sz & TABLE_MASK] * w)
                + sin((SIN_TABLE[sz + 4096 & TABLE_MASK]) * z)
        );
        idx = sw + (int) (w * 95 + u * 21);
        sum += (cos(w)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sw & TABLE_MASK] * u)
                + sin((SIN_TABLE[sw + 4096 & TABLE_MASK]) * w)
        );
        idx = su + (int) (u * 95 + v * 21);
        sum += (cos(u)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[su & TABLE_MASK] * v)
                + sin((SIN_TABLE[su + 4096 & TABLE_MASK]) * u)
        );
        idx = sv + (int) (v * 95 + x * 21);
        sum += (cos(v)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sv & TABLE_MASK] * x)
                + sin((SIN_TABLE[sv + 4096 & TABLE_MASK]) * v)
        );
        return sinTurns(sum * 0.12f);
    }

    protected float trillNoise(int seed, float x, float y, float z, float w, float u, float v, float m) {
        int sx = seed, sy = (sx << 13 | sx >>> 19) + 1234567,
                sz = (sy << 13 | sy >>> 19) + 1234567,
                sw = (sz << 13 | sz >>> 19) + 1234567,
                su = (sw << 13 | sw >>> 19) + 1234567,
                sv = (su << 13 | su >>> 19) + 1234567,
                sm = (sv << 13 | sv >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + z * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * z)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        idx = sz + (int) (z * 95 + w * 21);
        sum += (cos(z)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sz & TABLE_MASK] * w)
                + sin((SIN_TABLE[sz + 4096 & TABLE_MASK]) * z)
        );
        idx = sw + (int) (w * 95 + u * 21);
        sum += (cos(w)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sw & TABLE_MASK] * u)
                + sin((SIN_TABLE[sw + 4096 & TABLE_MASK]) * w)
        );
        idx = su + (int) (u * 95 + v * 21);
        sum += (cos(u)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[su & TABLE_MASK] * v)
                + sin((SIN_TABLE[su + 4096 & TABLE_MASK]) * u)
        );
        idx = sv + (int) (v * 95 + m * 21);
        sum += (cos(v)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sv & TABLE_MASK] * m)
                + sin((SIN_TABLE[sv + 4096 & TABLE_MASK]) * v)
        );
        idx = sm + (int) (m * 95 + x * 21);
        sum += (cos(m)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sm & TABLE_MASK] * x)
                + sin((SIN_TABLE[sm + 4096 & TABLE_MASK]) * m)
        );
        return sinTurns(sum * 0.11f);
    }

    protected float trillNoise(int seed, float x, float y, float z, float w, float u, float v, float m, float n) {
        int sx = seed, sy = (sx << 13 | sx >>> 19) + 1234567,
                sz = (sy << 13 | sy >>> 19) + 1234567,
                sw = (sz << 13 | sz >>> 19) + 1234567,
                su = (sw << 13 | sw >>> 19) + 1234567,
                sv = (su << 13 | su >>> 19) + 1234567,
                sm = (sv << 13 | sv >>> 19) + 1234567,
                sn = (sm << 13 | sm >>> 19) + 1234567;
        int idx = sx + (int) (x * 95 + y * 21);
        float sum = (cos(x)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK] * y)
                + sin((SIN_TABLE[sx + 4096 & TABLE_MASK]) * x)
        );
        idx = sy + (int) (y * 95 + z * 21);
        sum += (cos(y)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK] * z)
                + sin((SIN_TABLE[sy + 4096 & TABLE_MASK]) * y)
        );
        idx = sz + (int) (z * 95 + w * 21);
        sum += (cos(z)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sz & TABLE_MASK] * w)
                + sin((SIN_TABLE[sz + 4096 & TABLE_MASK]) * z)
        );
        idx = sw + (int) (w * 95 + u * 21);
        sum += (cos(w)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sw & TABLE_MASK] * u)
                + sin((SIN_TABLE[sw + 4096 & TABLE_MASK]) * w)
        );
        idx = su + (int) (u * 95 + v * 21);
        sum += (cos(u)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[su & TABLE_MASK] * v)
                + sin((SIN_TABLE[su + 4096 & TABLE_MASK]) * u)
        );
        idx = sv + (int) (v * 95 + m * 21);
        sum += (cos(v)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sv & TABLE_MASK] * m)
                + sin((SIN_TABLE[sv + 4096 & TABLE_MASK]) * v)
        );
        idx = sm + (int) (m * 95 + n * 21);
        sum += (cos(m)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sm & TABLE_MASK] * n)
                + sin((SIN_TABLE[sm + 4096 & TABLE_MASK]) * m)
        );
        idx = sn + (int) (n * 95 + x * 21);
        sum += (cos(n)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sn & TABLE_MASK] * x)
                + sin((SIN_TABLE[sn + 4096 & TABLE_MASK]) * n)
        );
        return sinTurns(sum * 0.1f);
    }

    public float singleTaffyVarargs(int seed, float... coordinates) {
        if(coordinates == null)
            return (Hasher.randomize3Float(seed) - 0.5f) * 2f;
        switch (coordinates.length) {
            case 0:
                return (Hasher.randomize3Float(seed) - 0.5f) * 2f;
            case 1:
                return LineWobble.wobble(seed, coordinates[0]);
            case 2:
                return singleTaffy(seed, coordinates[0], coordinates[1]);
            case 3:
                return singleTaffy(seed, coordinates[0], coordinates[1], coordinates[2]);
            case 4:
                return singleTaffy(seed, coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
            case 5:
                return singleTaffy(seed, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4]);
            case 6:
                return singleTaffy(seed, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4],
                        coordinates[5]);
            default:
                return singleTaffy(seed, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4],
                        coordinates[5], coordinates[6]);
        }
    }
    public float singleTaffy(int seed, float x, float y) {
//        final float p0 = x * 1.4104849f + y * 1.8545705f;
//        final float p1 = x * -2.3209028f + y * 0.20569342f;
//        final float p2 = x * 1.0655133f + y * -2.0716872f;
        final float p0 = x * 0.57565147f + y * 0.8176952f;
        final float p1 = x * -0.9655443f + y * 0.26023862f;
        final float p2 = x * -0.17945707f + y * -0.98376584f;

        float xin = p2;
        float yin = p0;
        final float a = trillNoise(seed, xin, yin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p1;
        yin = p2;
        final float b = trillNoise(seed, xin + a, yin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p0;
        yin = p1;
        final float c = trillNoise(seed, xin + b, yin);
        final float result = (a + b + c) * F3;
        final float sharp = sharpnessInverse;
        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        final float diff = 0.5f - result;
//        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    public float getTaffyFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleTaffyFractalFBM(x, y);
            case BILLOW:
                return singleTaffyFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleTaffyFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    protected float singleTaffyFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleTaffy(seed, x, y);
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;

            amp *= gain;
            sum += singleTaffy(seed + i, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleTaffy(seed, x, y)) * 2 - 1;
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;

            amp *= gain;
            sum += (Math.abs(singleTaffy(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float t;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleTaffy(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;
        }
        return sum * 2f / correction - 1f;
    }


    public float getTaffyFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleTaffyFractalFBM(x, y, z);
            case BILLOW:
                return singleTaffyFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleTaffyFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    protected float singleTaffyFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleTaffy(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleTaffy(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleTaffyFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singleTaffy(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleTaffy(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleTaffyFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleTaffy(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleTaffy(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleTaffy(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getTaffy(float x, float y, float z) {
        return singleTaffy(seed, x * frequency, y * frequency, z * frequency);
    }

    public float singleTaffy(int seed, float x, float y, float z){
//        final float p0 = x * 1.6874464f + y * 1.0735568f;
//        final float p1 = x * -1.5251374f + y * 1.2938148f;
//        final float p2 = x * 0.35694847f + y * -1.0980074f + z * 1.6329931f;
//        final float p3 = x * 0.026345633f + y * -1.1543899f + z * -1.6329935f;
        final float p0 = x * 0.7798978f + y * 0.6259069f;
        final float p1 = x * -0.48268777f + y * 0.87579256f;
        final float p2 = x * -0.25014642f + y * -0.5203461f + z * 0.81649655f;
        final float p3 = x * 0.0060886918f + y * -0.5773158f + z * -0.81649673f;

        float xin = p3;
        float yin = p2;
        float zin = p0;
        final float a = trillNoise(seed, xin, yin, zin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p0;
        yin = p1;
        zin = p3;
        final float b = trillNoise(seed, xin + a, yin, zin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p1;
        yin = p2;
        zin = p3;
        final float c = trillNoise(seed, xin + b, yin, zin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p0;
        yin = p1;
        zin = p2;
        final float d = trillNoise(seed, xin + c, yin, zin);

        final float result = (a + b + c + d) * 0.25f;
        final float sharp = sharpnessInverse * (1f/1.25f);
        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        final float diff = 0.5f - result;
//        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }


    protected float singleTaffyFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleTaffy(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleTaffy(++seed, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleTaffyFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singleTaffy(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleTaffy(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleTaffy(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleTaffy(++seed, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleTaffy(seed + i, x, y,  z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }
    public float getTaffyFractal(float x, float y, float z, float w) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;

        switch (fractalType) {
            case FBM:
                return singleTaffyFractalFBM(x, y, z, w);
            case BILLOW:
                return singleTaffyFractalBillow(x, y, z, w);
            case RIDGED_MULTI:
                return singleTaffyFractalRidgedMulti(x, y, z, w);
            default:
                return 0;
        }
    }

    public float getTaffy(float x, float y, float z, float w) {
        return singleTaffy(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    public float singleTaffy(int seed, float x, float y, float z, float w) {
//        final float p0 = x * 1.5337617f + y * 0.6349604f;
//        final float p1 = x * -0.9023025f + y * 1.3933592f;
//        final float p2 = x * 0.10983736f + y * -0.6686897f + z * 1.5153657f;
//        final float p3 = x * -0.11829115f + y * -0.66728836f + z * -0.7576828f + w * 1.3123453f;
//        final float p4 = x * -0.24232098f + y * -0.6328879f + z * -0.7576828f + w * -1.3123453f;
        final float p0 = x * 0.84720474f + y * 0.5312665f;
        final float p1 = x * -0.56399137f + y * 0.82578063f;
        final float p2 = x * -0.18557791f + y * -0.36363098f + z * 0.91287094f;
        final float p3 = x * 0.02354939f + y * -0.40755945f + z * -0.45643544f + w * 0.7905694f;
        final float p4 = x * 0.10397277f + y * -0.39474648f + z * -0.45643544f + w * -0.7905694f;

        float xin = p4;
        float yin = p1;
        float zin = p3;
        float win = p2;
        final float a = trillNoise(seed, xin, yin, zin, win);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p3;
        yin = p2;
        zin = p0;
        win = p4;
        final float b = trillNoise(seed, xin + a, yin, zin, win);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p0;
        yin = p3;
        zin = p4;
        win = p1;
        final float c = trillNoise(seed, xin + b, yin, zin, win);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p2;
        yin = p4;
        zin = p1;
        win = p0;
        final float d = trillNoise(seed, xin + c, yin, zin, win);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p1;
        yin = p0;
        zin = p2;
        win = p3;
        final float e = trillNoise(seed, xin + d, yin, zin, win);

        final float result = (a + b + c + d + e) * 0.2f;
        final float sharp = sharpnessInverse * (1f/1.5f);
        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        final float diff = 0.5f - result;
//        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }
    public float getTaffyFractal(float x, float y, float z, float w, float u) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        switch (fractalType) {
            case FBM:
                return singleTaffyFractalFBM(x, y, z, w, u);
            case BILLOW:
                return singleTaffyFractalBillow(x, y, z, w, u);
            case RIDGED_MULTI:
                return singleTaffyFractalRidgedMulti(x, y, z, w, u);
            default:
                return 0;
        }
    }

    protected float singleTaffyFractalFBM(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = singleTaffy(seed, x, y, z, w, u);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += singleTaffy(seed + i, x, y, z, w, u) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleTaffyFractalDomainWarp(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float latest = singleTaffy(seed, x, y, z, w, u);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleTaffy(++seed, x + a, y + b, z + c, w + d, u + e)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleTaffyFractalBillow(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = Math.abs(singleTaffy(seed, x, y, z, w, u)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleTaffy(seed + i, x, y, z, w, u)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalRidgedMulti(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleTaffy(seed + i, x, y, z, w, u));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getTaffy(float x, float y, float z, float w, float u) {
        return singleTaffy(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency);
    }

    public float singleTaffy(int seed, float x, float y, float z, float w, float u) {
//        final float p0 = x * 0.80512667f + y * 1.0586176f;
//        final float p1 = x * -1.2194887f + y * 0.5307986f;
//        final float p2 = x * 0.12985991f + y * -0.39998323f + z * 1.2617488f;
//        final float p3 = x * -0.19875908f + y * -0.37065464f + z * -0.42058298f + w * 1.1895882f;
//        final float p4 = x * -0.10633402f + y * -0.40691903f + z * -0.42058298f + w * -0.59479415f + u * 1.0302136f;
//        final float p5 = x * -0.012014422f + y * -0.42041135f + z * -0.42058298f + w * -0.59479415f + u * -1.0302138f;
        final float p0 = x * 0.88808066f + y * 0.4596875f;
        final float p1 = x * -0.773132f + y * 0.63424504f;
        final float p2 = x * 0.02839993f + y * -0.31493902f + z * 0.94868326f;
        final float p3 = x * 0.022960793f + y * -0.3153843f + z * -0.3162278f + w * 0.8944272f;
        final float p4 = x * -0.09451942f + y * -0.30177158f + z * -0.3162278f + w * -0.44721365f + u * 0.77459663f;
        final float p5 = x * -0.071354955f + y * -0.30807218f + z * -0.3162278f + w * -0.44721365f + u * -0.7745968f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        float uin = p5;
        final float a = trillNoise(seed, xin, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final float b = trillNoise(seed, xin + a, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final float c = trillNoise(seed, xin + b, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final float d = trillNoise(seed, xin + c, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final float e = trillNoise(seed, xin + d, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final float f = trillNoise(seed, xin + e, yin, zin, win, uin);

        final float result = (a + b + c + d + e + f) * (1f/6f);
        final float sharp = sharpnessInverse * (1f/1.75f);
        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        final float diff = 0.5f - result;
//        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    public float getTaffyFractal(float x, float y, float z, float w, float u, float v) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        switch (fractalType) {
            case FBM:
                return singleTaffyFractalFBM(x, y, z, w, u, v);
            case BILLOW:
                return singleTaffyFractalBillow(x, y, z, w, u, v);
            case RIDGED_MULTI:
                return singleTaffyFractalRidgedMulti(x, y, z, w, u, v);
            default:
                return 0;
        }
    }

    protected float singleTaffyFractalFBM(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = singleTaffy(seed, x, y, z, w, u, v);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += singleTaffy(++seed, x, y, z, w, u, v) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleTaffyFractalDomainWarp(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float latest = singleTaffy(seed, x, y, z, w, u, v);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleTaffy(++seed, x + a, y + b, z + c, w + d, u + e, v + f)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleTaffyFractalBillow(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = Math.abs(singleTaffy(seed, x, y, z, w, u, v)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleTaffy(++seed, x, y, z, w, u, v)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalRidgedMulti(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleTaffy(seed + i, x, y, z, w, u, v));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getTaffy(float x, float y, float z, float w, float u, float v) {
        return singleTaffy(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency);
    }

    public float singleTaffy(int seed, float x, float y, float z, float w, float u, float v) {
//        final float p0 = x * 0.6810281f + y * 0.89544713f;
//        final float p1 = x * -1.0156003f + y * 0.4839226f;
//        final float p2 = x * 0.08490618f + y * -0.277755f + z * 1.0868533f;
//        final float p3 = x * -0.14157186f + y * -0.25363833f + z * -0.27171332f + w * 1.0523412f;
//        final float p4 = x * -0.0781706f + y * -0.27975765f + z * -0.27171332f + w * -0.3507804f + u * 0.99215674f;
//        final float p5 = x * -0.013195699f + y * -0.29017383f + z * -0.27171332f + w * -0.3507804f + u * -0.49607837f + v * 0.8592329f;
//        final float p6 = x * -0.13214019f + y * -0.25867733f + z * -0.27171332f + w * -0.3507804f + u * -0.49607837f + v * -0.859233f;
        final float p0 = x * 0.87910366f + y * 0.4766307f;
        final float p1 = x * -0.6905121f + y * 0.7233209f;
        final float p2 = x * -0.038522165f + y * -0.25530905f + z * 0.9660918f;
        final float p3 = x * -0.10241036f + y * -0.23702061f + z * -0.24152295f + w * 0.9354144f;
        final float p4 = x * 0.054299045f + y * -0.25240397f + z * -0.24152295f + w * -0.3118048f + u * 0.8819171f;
        final float p5 = x * 0.0047030966f + y * -0.2581542f + z * -0.24152295f + w * -0.3118048f + u * -0.44095856f + v * 0.7637626f;
        final float p6 = x * -0.16337831f + y * -0.19993554f + z * -0.24152295f + w * -0.3118048f + u * -0.44095856f + v * -0.7637627f;

        float xin = p0;
        float yin = p5;
        float zin = p3;
        float win = p6;
        float uin = p1;
        float vin = p4;
        final float a = trillNoise(seed, xin, yin, zin, win, uin, vin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p2;
        yin = p6;
        zin = p0;
        win = p4;
        uin = p5;
        vin = p3;
        final float b = trillNoise(seed, xin + a, yin, zin, win, uin, vin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p1;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p6;
        vin = p5;
        final float c = trillNoise(seed, xin + b, yin, zin, win, uin, vin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p6;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p1;
        final float d = trillNoise(seed, xin + c, yin, zin, win, uin, vin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p2;
        yin = p1;
        zin = p5;
        win = p0;
        uin = p3;
        vin = p6;
        final float e = trillNoise(seed, xin + d, yin, zin, win, uin, vin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p0;
        yin = p4;
        zin = p6;
        win = p3;
        uin = p1;
        vin = p2;
        final float f = trillNoise(seed, xin + e, yin, zin, win, uin, vin);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p5;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p0;
        final float g = trillNoise(seed, xin + f, yin, zin, win, uin, vin);
        final float result = (a + b + c + d + e + f + g) * (1f/7f);
        final float sharp = sharpnessInverse * (1f/2f);
        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        final float diff = 0.5f - result;
//        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    protected float singleTaffyFractalFBM(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float sum = singleTaffy(seed, x, y, z, w, u, v, m);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
            m *= lacunarity;

            amp *= gain;
            sum += singleTaffy(++seed, x, y, z, w, u, v, m) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleTaffyFractalDomainWarp(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float latest = singleTaffy(seed, x, y, z, w, u, v, m);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            m = m * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 7) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 7) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 7) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 7) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 7) & TrigTools.TABLE_MASK];
            float g = TrigTools.SIN_TABLE[idx + (8192 * 6 / 7) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleTaffy(++seed, x + a, y + b, z + c, w + d, u + e, v + f, m + g)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalBillow(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float sum = Math.abs(singleTaffy(seed, x, y, z, w, u, v, m)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
            m *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleTaffy(++seed, x, y, z, w, u, v, m)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleTaffyFractalRidgedMulti(float x, float y, float z, float w, float u, float v, float m) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleTaffy(seed + i, x, y, z, w, u, v, m));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
            m *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float singleTaffy(int seed, float x, float y, float z, float w, float u, float v, float m) {
//        final float p0 = x * 0.60535836f + y * 0.79595304f;
//        final float p1 = x * -0.89210075f + y * 0.45183647f;
//        final float p2 = x * 0.06130022f + y * -0.20940745f + z * 0.97590005f;
//        final float p3 = x * -0.1086859f + y * -0.18922584f + z * -0.19518004f + w * 0.9561829f;
//        final float p4 = x * -0.061300192f + y * -0.20943096f + z * -0.19518004f + w * -0.23904574f + u * 0.92582005f;
//        final float p5 = x * -0.012587612f + y * -0.21785453f + z * -0.19518004f + w * -0.23904574f + u * -0.30860677f + v * 0.8728715f;
//        final float p6 = x * -0.10157331f + y * -0.193137f + z * -0.19518004f + w * -0.23904574f + u * -0.30860677f + v * -0.4364359f + m * 0.7559289f;
//        final float p7 = x * -0.13183342f + y * -0.17389362f + z * -0.19518004f + w * -0.23904574f + u * -0.30860677f + v * -0.4364359f + m * -0.7559293f;
        final float p0 = x * 0.8787378f + y * 0.47730482f;
        final float p1 = x * -0.46614632f + y * 0.8847077f;
        final float p2 = x * -0.11980514f + y * -0.18238907f + z * 0.97590005f;
        final float p3 = x * 0.03081842f + y * -0.21601889f + z * -0.19518004f + w * 0.9561829f;
        final float p4 = x * 0.010164112f + y * -0.21797714f + z * -0.19518004f + w * -0.23904574f + u * 0.92582005f;
        final float p5 = x * -0.009578837f + y * -0.21800756f + z * -0.19518004f + w * -0.23904574f + u * -0.30860677f + v * 0.8728715f;
        final float p6 = x * 0.046218276f + y * -0.21324952f + z * -0.19518004f + w * -0.23904574f + u * -0.30860677f + v * -0.4364359f + m * 0.7559289f;
        final float p7 = x * -0.059933554f + y * -0.20982619f + z * -0.19518004f + w * -0.23904574f + u * -0.30860677f + v * -0.4364359f + m * -0.7559293f;

        float xin = p0;
        float yin = p6;
        float zin = p3;
        float win = p7;
        float uin = p1;
        float vin = p4;
        float min = p5;
        final float a = trillNoise(seed, xin, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p2;
        yin = p3;
        zin = p0;
        win = p4;
        uin = p6;
        vin = p5;
        min = p7;
        final float b = trillNoise(seed, xin + a, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p1;
        yin = p2;
        zin = p4;
        win = p3;
        uin = p5;
        vin = p7;
        min = p6;
        final float c = trillNoise(seed, xin + b, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p7;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p6;
        min = p1;
        final float d = trillNoise(seed, xin + c, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p3;
        yin = p1;
        zin = p5;
        win = p6;
        uin = p7;
        vin = p0;
        min = p2;
        final float e = trillNoise(seed, xin + d, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p4;
        yin = p7;
        zin = p6;
        win = p2;
        uin = p0;
        vin = p1;
        min = p3;
        final float f = trillNoise(seed, xin + e, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p5;
        yin = p4;
        zin = p7;
        win = p1;
        uin = p2;
        vin = p3;
        min = p0;
        final float g = trillNoise(seed, xin + f, yin, zin, win, uin, vin, min);
        seed = seed * 0x9E373 ^ 0x7F4A7C15;
        xin = p6;
        yin = p5;
        zin = p1;
        win = p0;
        uin = p3;
        vin = p2;
        min = p4;
        final float h = trillNoise(seed, xin + g, yin, zin, win, uin, vin, min);
        final float result = (a + b + c + d + e + f + g + h) * 0.125f;
        final float sharp = sharpnessInverse * (1f/2.25f);
        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        final float diff = 0.5f - result;
//        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }


    // Classic Perlin Noise
    public float getPerlinFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case DOMAIN_WARP:
                return singlePerlinFractalDomainWarp(x, y);
            case BILLOW:
                return singlePerlinFractalBillow(x, y);
            case RIDGED_MULTI:
                return singlePerlinFractalRidgedMulti(x, y);
            default:
                return singlePerlinFractalFBM(x, y);
        }
    }

    protected float singlePerlinFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singlePerlin(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = singlePerlin(seed, x, y);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singlePerlin(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singlePerlin(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getPerlin(float x, float y) {
        return singlePerlin(seed, x * frequency, y * frequency);
    }

    protected float singlePerlin(int seed, float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float xs, ys;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                break;
        }

        float xd0 = x - x0;
        float yd0 = y - y0;
        float xd1 = xd0 - 1;
        float yd1 = yd0 - 1;

        float xf0 = lerp(gradCoord2D(seed, x0, y0, xd0, yd0), gradCoord2D(seed, x1, y0, xd1, yd0), xs);
        float xf1 = lerp(gradCoord2D(seed, x0, y1, xd0, yd1), gradCoord2D(seed, x1, y1, xd1, yd1), xs);

        return equalize(lerp(xf0, xf1, ys) * SCALE2, ADD2, MUL2);
    }

    public float getPerlinFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singlePerlinFractalFBM(x, y, z);
            case BILLOW:
                return singlePerlinFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singlePerlinFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    protected float singlePerlinFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singlePerlin(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singlePerlin(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singlePerlin(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singlePerlin(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getPerlin(float x, float y, float z) {
        return singlePerlin(seed, x * frequency, y * frequency, z * frequency);
    }

    protected float singlePerlin(int seed, float x, float y, float z) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float xs, ys, zs;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                zs = hermiteInterpolator(z - z0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                zs = quinticInterpolator(z - z0);
                break;
        }

        final float xd0 = x - x0;
        final float yd0 = y - y0;
        final float zd0 = z - z0;
        final float xd1 = xd0 - 1;
        final float yd1 = yd0 - 1;
        final float zd1 = zd0 - 1;

        final float xf00 = lerp(gradCoord3D(seed, x0, y0, z0, xd0, yd0, zd0), gradCoord3D(seed, x1, y0, z0, xd1, yd0, zd0), xs);
        final float xf10 = lerp(gradCoord3D(seed, x0, y1, z0, xd0, yd1, zd0), gradCoord3D(seed, x1, y1, z0, xd1, yd1, zd0), xs);
        final float xf01 = lerp(gradCoord3D(seed, x0, y0, z1, xd0, yd0, zd1), gradCoord3D(seed, x1, y0, z1, xd1, yd0, zd1), xs);
        final float xf11 = lerp(gradCoord3D(seed, x0, y1, z1, xd0, yd1, zd1), gradCoord3D(seed, x1, y1, z1, xd1, yd1, zd1), xs);

        final float yf0 = lerp(xf00, xf10, ys);
        final float yf1 = lerp(xf01, xf11, ys);

        return equalize(lerp(yf0, yf1, zs) * SCALE3, ADD3, MUL3);
    }
    public float getPerlin(float x, float y, float z, float w) {
        return singlePerlin(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    protected float singlePerlin(int seed, float x, float y, float z, float w) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int w0 = fastFloor(w);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;
        int w1 = w0 + 1;

        float xs, ys, zs, ws;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                ws = w - w0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                zs = hermiteInterpolator(z - z0);
                ws = hermiteInterpolator(w - w0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                zs = quinticInterpolator(z - z0);
                ws = quinticInterpolator(w - w0);
                break; 
        }

        final float xd0 = x - x0;
        final float yd0 = y - y0;
        final float zd0 = z - z0;
        final float wd0 = w - w0;
        final float xd1 = xd0 - 1;
        final float yd1 = yd0 - 1;
        final float zd1 = zd0 - 1;
        final float wd1 = wd0 - 1;

        final float xf000 = lerp(gradCoord4D(seed, x0, y0, z0, w0, xd0, yd0, zd0, wd0), gradCoord4D(seed, x1, y0, z0, w0, xd1, yd0, zd0, wd0), xs);
        final float xf100 = lerp(gradCoord4D(seed, x0, y1, z0, w0, xd0, yd1, zd0, wd0), gradCoord4D(seed, x1, y1, z0, w0, xd1, yd1, zd0, wd0), xs);
        final float xf010 = lerp(gradCoord4D(seed, x0, y0, z1, w0, xd0, yd0, zd1, wd0), gradCoord4D(seed, x1, y0, z1, w0, xd1, yd0, zd1, wd0), xs);
        final float xf110 = lerp(gradCoord4D(seed, x0, y1, z1, w0, xd0, yd1, zd1, wd0), gradCoord4D(seed, x1, y1, z1, w0, xd1, yd1, zd1, wd0), xs);
        final float xf001 = lerp(gradCoord4D(seed, x0, y0, z0, w1, xd0, yd0, zd0, wd1), gradCoord4D(seed, x1, y0, z0, w1, xd1, yd0, zd0, wd1), xs);
        final float xf101 = lerp(gradCoord4D(seed, x0, y1, z0, w1, xd0, yd1, zd0, wd1), gradCoord4D(seed, x1, y1, z0, w1, xd1, yd1, zd0, wd1), xs);
        final float xf011 = lerp(gradCoord4D(seed, x0, y0, z1, w1, xd0, yd0, zd1, wd1), gradCoord4D(seed, x1, y0, z1, w1, xd1, yd0, zd1, wd1), xs);
        final float xf111 = lerp(gradCoord4D(seed, x0, y1, z1, w1, xd0, yd1, zd1, wd1), gradCoord4D(seed, x1, y1, z1, w1, xd1, yd1, zd1, wd1), xs);

        final float yf00 = lerp(xf000, xf100, ys);
        final float yf10 = lerp(xf010, xf110, ys);
        final float yf01 = lerp(xf001, xf101, ys);
        final float yf11 = lerp(xf011, xf111, ys);

        final float zf0 = lerp(yf00, yf10, zs);
        final float zf1 = lerp(yf01, yf11, zs);
        return equalize(lerp(zf0, zf1, ws) * SCALE4, ADD4, MUL4);
    }
    protected float singlePerlinFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singlePerlin(++seed, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singlePerlinFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singlePerlin(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singlePerlin(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(++seed, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singlePerlin(seed + i, x, y,  z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getPerlin(float x, float y, float z, float w, float u) {
        return singlePerlin(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency);
    }
    
//    public float minBound = -1f, maxBound = 1f;
    
    protected float singlePerlin(int seed, float x, float y, float z, float w, float u) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int w0 = fastFloor(w);
        int u0 = fastFloor(u);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;
        int w1 = w0 + 1;
        int u1 = u0 + 1;
        
        float xs, ys, zs, ws, us;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                ws = w - w0;
                us = u - u0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                zs = hermiteInterpolator(z - z0);
                ws = hermiteInterpolator(w - w0);
                us = hermiteInterpolator(u - u0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                zs = quinticInterpolator(z - z0);
                ws = quinticInterpolator(w - w0);
                us = quinticInterpolator(u - u0);
                break;
        }

        final float xd0 = x - x0;
        final float yd0 = y - y0;
        final float zd0 = z - z0;
        final float wd0 = w - w0;
        final float ud0 = u - u0;
        final float xd1 = xd0 - 1;
        final float yd1 = yd0 - 1;
        final float zd1 = zd0 - 1;
        final float wd1 = wd0 - 1;
        final float ud1 = ud0 - 1;

        final float xf0000 = lerp(gradCoord5D(seed, x0, y0, z0, w0, u0, xd0, yd0, zd0, wd0, ud0), gradCoord5D(seed, x1, y0, z0, w0, u0, xd1, yd0, zd0, wd0, ud0), xs);
        final float xf1000 = lerp(gradCoord5D(seed, x0, y1, z0, w0, u0, xd0, yd1, zd0, wd0, ud0), gradCoord5D(seed, x1, y1, z0, w0, u0, xd1, yd1, zd0, wd0, ud0), xs);
        final float xf0100 = lerp(gradCoord5D(seed, x0, y0, z1, w0, u0, xd0, yd0, zd1, wd0, ud0), gradCoord5D(seed, x1, y0, z1, w0, u0, xd1, yd0, zd1, wd0, ud0), xs);
        final float xf1100 = lerp(gradCoord5D(seed, x0, y1, z1, w0, u0, xd0, yd1, zd1, wd0, ud0), gradCoord5D(seed, x1, y1, z1, w0, u0, xd1, yd1, zd1, wd0, ud0), xs);
        final float xf0010 = lerp(gradCoord5D(seed, x0, y0, z0, w1, u0, xd0, yd0, zd0, wd1, ud0), gradCoord5D(seed, x1, y0, z0, w1, u0, xd1, yd0, zd0, wd1, ud0), xs);
        final float xf1010 = lerp(gradCoord5D(seed, x0, y1, z0, w1, u0, xd0, yd1, zd0, wd1, ud0), gradCoord5D(seed, x1, y1, z0, w1, u0, xd1, yd1, zd0, wd1, ud0), xs);
        final float xf0110 = lerp(gradCoord5D(seed, x0, y0, z1, w1, u0, xd0, yd0, zd1, wd1, ud0), gradCoord5D(seed, x1, y0, z1, w1, u0, xd1, yd0, zd1, wd1, ud0), xs);
        final float xf1110 = lerp(gradCoord5D(seed, x0, y1, z1, w1, u0, xd0, yd1, zd1, wd1, ud0), gradCoord5D(seed, x1, y1, z1, w1, u0, xd1, yd1, zd1, wd1, ud0), xs);
        final float xf0001 = lerp(gradCoord5D(seed, x0, y0, z0, w0, u1, xd0, yd0, zd0, wd0, ud1), gradCoord5D(seed, x1, y0, z0, w0, u1, xd1, yd0, zd0, wd0, ud1), xs);
        final float xf1001 = lerp(gradCoord5D(seed, x0, y1, z0, w0, u1, xd0, yd1, zd0, wd0, ud1), gradCoord5D(seed, x1, y1, z0, w0, u1, xd1, yd1, zd0, wd0, ud1), xs);
        final float xf0101 = lerp(gradCoord5D(seed, x0, y0, z1, w0, u1, xd0, yd0, zd1, wd0, ud1), gradCoord5D(seed, x1, y0, z1, w0, u1, xd1, yd0, zd1, wd0, ud1), xs);
        final float xf1101 = lerp(gradCoord5D(seed, x0, y1, z1, w0, u1, xd0, yd1, zd1, wd0, ud1), gradCoord5D(seed, x1, y1, z1, w0, u1, xd1, yd1, zd1, wd0, ud1), xs);
        final float xf0011 = lerp(gradCoord5D(seed, x0, y0, z0, w1, u1, xd0, yd0, zd0, wd1, ud1), gradCoord5D(seed, x1, y0, z0, w1, u1, xd1, yd0, zd0, wd1, ud1), xs);
        final float xf1011 = lerp(gradCoord5D(seed, x0, y1, z0, w1, u1, xd0, yd1, zd0, wd1, ud1), gradCoord5D(seed, x1, y1, z0, w1, u1, xd1, yd1, zd0, wd1, ud1), xs);
        final float xf0111 = lerp(gradCoord5D(seed, x0, y0, z1, w1, u1, xd0, yd0, zd1, wd1, ud1), gradCoord5D(seed, x1, y0, z1, w1, u1, xd1, yd0, zd1, wd1, ud1), xs);
        final float xf1111 = lerp(gradCoord5D(seed, x0, y1, z1, w1, u1, xd0, yd1, zd1, wd1, ud1), gradCoord5D(seed, x1, y1, z1, w1, u1, xd1, yd1, zd1, wd1, ud1), xs);

        final float yf000 = lerp(xf0000, xf1000, ys);
        final float yf100 = lerp(xf0100, xf1100, ys);
        final float yf010 = lerp(xf0010, xf1010, ys);
        final float yf110 = lerp(xf0110, xf1110, ys);
        final float yf001 = lerp(xf0001, xf1001, ys);
        final float yf101 = lerp(xf0101, xf1101, ys);
        final float yf011 = lerp(xf0011, xf1011, ys);
        final float yf111 = lerp(xf0111, xf1111, ys);

        final float zf00 = lerp(yf000, yf100, zs);
        final float zf10 = lerp(yf010, yf110, zs);
        final float zf01 = lerp(yf001, yf101, zs);
        final float zf11 = lerp(yf011, yf111, zs);

        final float wf0 = lerp(zf00, zf10, ws);
        final float wf1 = lerp(zf01, zf11, ws);

        return equalize(lerp(wf0, wf1, us) * SCALE5, ADD5, MUL5);
    }
    protected float singlePerlinFractalFBM(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y, z, w, u);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += singlePerlin(seed + i, x, y, z, w, u) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singlePerlinFractalDomainWarp(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float latest = singlePerlin(seed, x, y, z, w, u);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singlePerlin(++seed, x + a, y + b, z + c, w + d, u + e)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalBillow(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y, z, w, u)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(seed + i, x, y, z, w, u)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalRidgedMulti(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singlePerlin(seed + i, x, y, z, w, u));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    
    
    
    
    
    
    
    
    public float getPerlin(float x, float y, float z, float w, float u, float v) {
        return singlePerlin(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency);
    }

    protected float singlePerlin(int seed, float x, float y, float z, float w, float u, float v) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int w0 = fastFloor(w);
        int u0 = fastFloor(u);
        int v0 = fastFloor(v);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;
        int w1 = w0 + 1;
        int u1 = u0 + 1;
        int v1 = v0 + 1;

        float xs, ys, zs, ws, us, vs;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                ws = w - w0;
                us = u - u0;
                vs = v - v0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                zs = hermiteInterpolator(z - z0);
                ws = hermiteInterpolator(w - w0);
                us = hermiteInterpolator(u - u0);
                vs = hermiteInterpolator(v - v0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                zs = quinticInterpolator(z - z0);
                ws = quinticInterpolator(w - w0);
                us = quinticInterpolator(u - u0);
                vs = quinticInterpolator(v - v0);
                break;
        }

        final float xd0 = x - x0;
        final float yd0 = y - y0;
        final float zd0 = z - z0;
        final float wd0 = w - w0;
        final float ud0 = u - u0;
        final float vd0 = v - v0;
        final float xd1 = xd0 - 1;
        final float yd1 = yd0 - 1;
        final float zd1 = zd0 - 1;
        final float wd1 = wd0 - 1;
        final float ud1 = ud0 - 1;
        final float vd1 = vd0 - 1;

        final float xf00000 = lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0, xd0, yd0, zd0, wd0, ud0, vd0), gradCoord6D(seed, x1, y0, z0, w0, u0, v0, xd1, yd0, zd0, wd0, ud0, vd0), xs);
        final float xf10000 = lerp(gradCoord6D(seed, x0, y1, z0, w0, u0, v0, xd0, yd1, zd0, wd0, ud0, vd0), gradCoord6D(seed, x1, y1, z0, w0, u0, v0, xd1, yd1, zd0, wd0, ud0, vd0), xs);
        final float xf01000 = lerp(gradCoord6D(seed, x0, y0, z1, w0, u0, v0, xd0, yd0, zd1, wd0, ud0, vd0), gradCoord6D(seed, x1, y0, z1, w0, u0, v0, xd1, yd0, zd1, wd0, ud0, vd0), xs);
        final float xf11000 = lerp(gradCoord6D(seed, x0, y1, z1, w0, u0, v0, xd0, yd1, zd1, wd0, ud0, vd0), gradCoord6D(seed, x1, y1, z1, w0, u0, v0, xd1, yd1, zd1, wd0, ud0, vd0), xs);
        final float xf00100 = lerp(gradCoord6D(seed, x0, y0, z0, w1, u0, v0, xd0, yd0, zd0, wd1, ud0, vd0), gradCoord6D(seed, x1, y0, z0, w1, u0, v0, xd1, yd0, zd0, wd1, ud0, vd0), xs);
        final float xf10100 = lerp(gradCoord6D(seed, x0, y1, z0, w1, u0, v0, xd0, yd1, zd0, wd1, ud0, vd0), gradCoord6D(seed, x1, y1, z0, w1, u0, v0, xd1, yd1, zd0, wd1, ud0, vd0), xs);
        final float xf01100 = lerp(gradCoord6D(seed, x0, y0, z1, w1, u0, v0, xd0, yd0, zd1, wd1, ud0, vd0), gradCoord6D(seed, x1, y0, z1, w1, u0, v0, xd1, yd0, zd1, wd1, ud0, vd0), xs);
        final float xf11100 = lerp(gradCoord6D(seed, x0, y1, z1, w1, u0, v0, xd0, yd1, zd1, wd1, ud0, vd0), gradCoord6D(seed, x1, y1, z1, w1, u0, v0, xd1, yd1, zd1, wd1, ud0, vd0), xs);

        final float xf00010 = lerp(gradCoord6D(seed, x0, y0, z0, w0, u1, v0, xd0, yd0, zd0, wd0, ud1, vd0), gradCoord6D(seed, x1, y0, z0, w0, u1, v0, xd1, yd0, zd0, wd0, ud1, vd0), xs);
        final float xf10010 = lerp(gradCoord6D(seed, x0, y1, z0, w0, u1, v0, xd0, yd1, zd0, wd0, ud1, vd0), gradCoord6D(seed, x1, y1, z0, w0, u1, v0, xd1, yd1, zd0, wd0, ud1, vd0), xs);
        final float xf01010 = lerp(gradCoord6D(seed, x0, y0, z1, w0, u1, v0, xd0, yd0, zd1, wd0, ud1, vd0), gradCoord6D(seed, x1, y0, z1, w0, u1, v0, xd1, yd0, zd1, wd0, ud1, vd0), xs);
        final float xf11010 = lerp(gradCoord6D(seed, x0, y1, z1, w0, u1, v0, xd0, yd1, zd1, wd0, ud1, vd0), gradCoord6D(seed, x1, y1, z1, w0, u1, v0, xd1, yd1, zd1, wd0, ud1, vd0), xs);
        final float xf00110 = lerp(gradCoord6D(seed, x0, y0, z0, w1, u1, v0, xd0, yd0, zd0, wd1, ud1, vd0), gradCoord6D(seed, x1, y0, z0, w1, u1, v0, xd1, yd0, zd0, wd1, ud1, vd0), xs);
        final float xf10110 = lerp(gradCoord6D(seed, x0, y1, z0, w1, u1, v0, xd0, yd1, zd0, wd1, ud1, vd0), gradCoord6D(seed, x1, y1, z0, w1, u1, v0, xd1, yd1, zd0, wd1, ud1, vd0), xs);
        final float xf01110 = lerp(gradCoord6D(seed, x0, y0, z1, w1, u1, v0, xd0, yd0, zd1, wd1, ud1, vd0), gradCoord6D(seed, x1, y0, z1, w1, u1, v0, xd1, yd0, zd1, wd1, ud1, vd0), xs);
        final float xf11110 = lerp(gradCoord6D(seed, x0, y1, z1, w1, u1, v0, xd0, yd1, zd1, wd1, ud1, vd0), gradCoord6D(seed, x1, y1, z1, w1, u1, v0, xd1, yd1, zd1, wd1, ud1, vd0), xs);

        final float xf00001 = lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v1, xd0, yd0, zd0, wd0, ud0, vd1), gradCoord6D(seed, x1, y0, z0, w0, u0, v1, xd1, yd0, zd0, wd0, ud0, vd1), xs);
        final float xf10001 = lerp(gradCoord6D(seed, x0, y1, z0, w0, u0, v1, xd0, yd1, zd0, wd0, ud0, vd1), gradCoord6D(seed, x1, y1, z0, w0, u0, v1, xd1, yd1, zd0, wd0, ud0, vd1), xs);
        final float xf01001 = lerp(gradCoord6D(seed, x0, y0, z1, w0, u0, v1, xd0, yd0, zd1, wd0, ud0, vd1), gradCoord6D(seed, x1, y0, z1, w0, u0, v1, xd1, yd0, zd1, wd0, ud0, vd1), xs);
        final float xf11001 = lerp(gradCoord6D(seed, x0, y1, z1, w0, u0, v1, xd0, yd1, zd1, wd0, ud0, vd1), gradCoord6D(seed, x1, y1, z1, w0, u0, v1, xd1, yd1, zd1, wd0, ud0, vd1), xs);
        final float xf00101 = lerp(gradCoord6D(seed, x0, y0, z0, w1, u0, v1, xd0, yd0, zd0, wd1, ud0, vd1), gradCoord6D(seed, x1, y0, z0, w1, u0, v1, xd1, yd0, zd0, wd1, ud0, vd1), xs);
        final float xf10101 = lerp(gradCoord6D(seed, x0, y1, z0, w1, u0, v1, xd0, yd1, zd0, wd1, ud0, vd1), gradCoord6D(seed, x1, y1, z0, w1, u0, v1, xd1, yd1, zd0, wd1, ud0, vd1), xs);
        final float xf01101 = lerp(gradCoord6D(seed, x0, y0, z1, w1, u0, v1, xd0, yd0, zd1, wd1, ud0, vd1), gradCoord6D(seed, x1, y0, z1, w1, u0, v1, xd1, yd0, zd1, wd1, ud0, vd1), xs);
        final float xf11101 = lerp(gradCoord6D(seed, x0, y1, z1, w1, u0, v1, xd0, yd1, zd1, wd1, ud0, vd1), gradCoord6D(seed, x1, y1, z1, w1, u0, v1, xd1, yd1, zd1, wd1, ud0, vd1), xs);

        final float xf00011 = lerp(gradCoord6D(seed, x0, y0, z0, w0, u1, v1, xd0, yd0, zd0, wd0, ud1, vd1), gradCoord6D(seed, x1, y0, z0, w0, u1, v1, xd1, yd0, zd0, wd0, ud1, vd1), xs);
        final float xf10011 = lerp(gradCoord6D(seed, x0, y1, z0, w0, u1, v1, xd0, yd1, zd0, wd0, ud1, vd1), gradCoord6D(seed, x1, y1, z0, w0, u1, v1, xd1, yd1, zd0, wd0, ud1, vd1), xs);
        final float xf01011 = lerp(gradCoord6D(seed, x0, y0, z1, w0, u1, v1, xd0, yd0, zd1, wd0, ud1, vd1), gradCoord6D(seed, x1, y0, z1, w0, u1, v1, xd1, yd0, zd1, wd0, ud1, vd1), xs);
        final float xf11011 = lerp(gradCoord6D(seed, x0, y1, z1, w0, u1, v1, xd0, yd1, zd1, wd0, ud1, vd1), gradCoord6D(seed, x1, y1, z1, w0, u1, v1, xd1, yd1, zd1, wd0, ud1, vd1), xs);
        final float xf00111 = lerp(gradCoord6D(seed, x0, y0, z0, w1, u1, v1, xd0, yd0, zd0, wd1, ud1, vd1), gradCoord6D(seed, x1, y0, z0, w1, u1, v1, xd1, yd0, zd0, wd1, ud1, vd1), xs);
        final float xf10111 = lerp(gradCoord6D(seed, x0, y1, z0, w1, u1, v1, xd0, yd1, zd0, wd1, ud1, vd1), gradCoord6D(seed, x1, y1, z0, w1, u1, v1, xd1, yd1, zd0, wd1, ud1, vd1), xs);
        final float xf01111 = lerp(gradCoord6D(seed, x0, y0, z1, w1, u1, v1, xd0, yd0, zd1, wd1, ud1, vd1), gradCoord6D(seed, x1, y0, z1, w1, u1, v1, xd1, yd0, zd1, wd1, ud1, vd1), xs);
        final float xf11111 = lerp(gradCoord6D(seed, x0, y1, z1, w1, u1, v1, xd0, yd1, zd1, wd1, ud1, vd1), gradCoord6D(seed, x1, y1, z1, w1, u1, v1, xd1, yd1, zd1, wd1, ud1, vd1), xs);

        final float yf0000 = lerp(xf00000, xf10000, ys);
        final float yf1000 = lerp(xf01000, xf11000, ys);
        final float yf0100 = lerp(xf00100, xf10100, ys);
        final float yf1100 = lerp(xf01100, xf11100, ys);

        final float yf0010 = lerp(xf00010, xf10010, ys);
        final float yf1010 = lerp(xf01010, xf11010, ys);
        final float yf0110 = lerp(xf00110, xf10110, ys);
        final float yf1110 = lerp(xf01110, xf11110, ys);

        final float yf0001 = lerp(xf00001, xf10001, ys);
        final float yf1001 = lerp(xf01001, xf11001, ys);
        final float yf0101 = lerp(xf00101, xf10101, ys);
        final float yf1101 = lerp(xf01101, xf11101, ys);

        final float yf0011 = lerp(xf00011, xf10011, ys);
        final float yf1011 = lerp(xf01011, xf11011, ys);
        final float yf0111 = lerp(xf00111, xf10111, ys);
        final float yf1111 = lerp(xf01111, xf11111, ys);

        final float zf000 = lerp(yf0000, yf1000, zs);
        final float zf100 = lerp(yf0100, yf1100, zs);

        final float zf010 = lerp(yf0010, yf1010, zs);
        final float zf110 = lerp(yf0110, yf1110, zs);

        final float zf001 = lerp(yf0001, yf1001, zs);
        final float zf101 = lerp(yf0101, yf1101, zs);

        final float zf011 = lerp(yf0011, yf1011, zs);
        final float zf111 = lerp(yf0111, yf1111, zs);

        final float wf00 = lerp(zf000, zf100, ws);
        final float wf10 = lerp(zf010, zf110, ws);
        final float wf01 = lerp(zf001, zf101, ws);
        final float wf11 = lerp(zf011, zf111, ws);

        final float uf0 = lerp(wf00, wf10, us);
        final float uf1 = lerp(wf01, wf11, us);

        return equalize(lerp(uf0, uf1, vs) * SCALE6, ADD6, MUL6);
    }
    protected float singlePerlinFractalFBM(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y, z, w, u, v);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += singlePerlin(++seed, x, y, z, w, u, v) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singlePerlinFractalDomainWarp(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float latest = singlePerlin(seed, x, y, z, w, u, v);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singlePerlin(++seed, x + a, y + b, z + c, w + d, u + e, v + f)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalBillow(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y, z, w, u, v)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(++seed, x, y, z, w, u, v)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singlePerlinFractalRidgedMulti(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singlePerlin(seed + i, x, y, z, w, u, v));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    // 2D Simplex

    public float getSimplexFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y);
            case BILLOW:
                return singleSimplexFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    /**
     * Generates FBM simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves)
    {
        return layered2D(x, y, seed, octaves, 0.03125f);
    }
    /**
     * Generates FBM simplex noise with the given amount of octaves, given frequency, default lacunarity
     * (2) and default gain (0.5) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves, float frequency)
    {
        x *= frequency;
        y *= frequency;

        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x += x;
            y += y;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y) * amp;
        }
        float ampFractal = (1 << octaves) - 1;
        return sum / ampFractal;
    }
    /**
     * Generates FBM simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain ({@code 1.0f / lacunarity}) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;

        float sum = singleSimplex(seed, x, y);
        float amp = 1;
        float gain = 1.0f / lacunarity;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    /**
     * Generates FBM simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (loosely, how much to emphasize lower-frequency octaves) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves, float frequency, float lacunarity, float gain)
    {
        x *= frequency;
        y *= frequency;

        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5).
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves)
    {
        return ridged2D(x, y, seed, octaves, 0.03125f, 2f);
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5).
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves, float frequency)
    {
        return ridged2D(x, y, seed, octaves, frequency, 2f);
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves); gain is not used.
     * @param x
     * @param y
     * @param seed any int
     * @param octaves how many "layers of detail" to generate; at least 1, but note this slows down with many octaves
     * @param frequency often about {@code 1f / 32f}, but generally adjusted for the use case
     * @param lacunarity when {@code octaves} is 2 or more, this affects the change between layers
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;

        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    protected float singleSimplexFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleSimplexFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = singleSimplex(seed, x, y);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleSimplex(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleSimplexFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleSimplexFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getSimplex(float x, float y) {
        return singleSimplex(seed, x * frequency, y * frequency);
    }

    public float singleSimplex(int seed, float x, float y) {
        float t = (x + y) * F2;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);

        t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;

        float x0 = x - X0;
        float y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1 + H2;
        float y2 = y0 - 1 + H2;

        float n = 0f;

        t = LIMIT2 - x0 * x0 - y0 * y0;
        if (t >= 0) {
            t *= t;
            n += t * t * gradCoord2D(seed, i, j, x0, y0);
        }

        t = LIMIT2 - x1 * x1 - y1 * y1;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord2D(seed, i + i1, j + j1, x1, y1);
        }

        t = LIMIT2 - x2 * x2 - y2 * y2;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord2D(seed, i + 1, j + 1, x2, y2);
        }
        return n * 99.20689070704672f; // this is 99.83685446303647 / 1.00635 ; the first number was found by kdotjpg
    }

    // 3D Simplex
    public float getSimplexFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y, z);
            case BILLOW:
                return singleSimplexFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    /**
     * Generates FBM simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5) in 3D.
     *
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves)
    {
        return layered3D(x, y, z, seed, octaves, 0.03125f);
    }
    /**
     * Generates FBM simplex noise with the given amount of octaves, given frequency, and default lacunarity
     * (2) and gain (0.5) in 3D.
     *
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves, float frequency)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x += x;
            y += y;
            z += z;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }
        float ampFractal = (1 << octaves) - 1;
        return sum / ampFractal;
    }
    /**
     * Generates FBM simplex noise with the given amount of octaves, given frequency, and specified lacunarity (the
     * amount of frequency change between octaves) and gain ({@code 1.0f / lacunarity}) in 3D.
     *
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @param frequency
     * @param lacunarity
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;
        float gain = 1f / lacunarity;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    /**
     * Generates FBM simplex noise with the given amount of octaves, given frequency, given lacunarity (the amount
     * of frequency change between octaves) and gain (loosely, how much to emphasize lower-frequency octaves) in 3D.
     *
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @param frequency
     * @param lacunarity
     * @param gain
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity, float gain)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5).
     *
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves)
    {
        return ridged3D(x, y, z, seed, octaves, 0.03125f, 2f);
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves, specified frequency, and the default
     * lacunarity (2) and gain (0.5).
     *
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves, float frequency)
    {
        return ridged3D(x, y, z, seed, octaves, frequency, 2f);
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves); gain is not used.
     *
     * @param x
     * @param y
     * @param z
     * @param seed       any int
     * @param octaves    how many "layers of detail" to generate; at least 1, but note this slows down with many octaves
     * @param frequency  often about {@code 1f / 32f}, but generally adjusted for the use case
     * @param lacunarity when {@code octaves} is 2 or more, this affects the change between layers
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }
    
    protected float singleSimplexFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleSimplexFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singleSimplex(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleSimplex(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleSimplexFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(seed + i, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleSimplexFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getSimplex(float x, float y, float z) {
        return singleSimplex(seed, x * frequency, y * frequency, z * frequency);
    }

    public float singleSimplex(int seed, float x, float y, float z) {
        float t = (x + y + z) * F3;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);

        t = (i + j + k) * G3;
        float x0 = x - (i - t);
        float y0 = y - (j - t);
        float z0 = z - (k - t);

        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else // x0 < z0
            {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else // x0 < y0
        {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else // x0 >= z0
            {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        float x1 = x0 - i1 + G3;
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;
        float x2 = x0 - i2 + F3;
        float y2 = y0 - j2 + F3;
        float z2 = z0 - k2 + F3;
        float x3 = x0 - 0.5f;
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;

        float n = 0;

        t = LIMIT3 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i, j, k, x0, y0, z0);
        }

        t = LIMIT3 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
        }

        t = LIMIT3 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
        }

        t = LIMIT3 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
        }

        return 39.59758f * n;
    }

    // 4D Simplex

    public float getSimplexFractal(float x, float y, float z, float w) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y, z, w);
            case BILLOW:
                return singleSimplexFractalBillow(x, y, z, w);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y, z, w);
            default:
                return 0;
        }
    }

    protected float singleSimplexFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleSimplexFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singleSimplex(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleSimplex(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleSimplexFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y, z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    protected float singleSimplexFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(seed + i, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    public float getSimplex(float x, float y, float z, float w) {
        return singleSimplex(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    public float singleSimplex(int seed, float x, float y, float z, float w) {
        float skew = (x + y + z + w) * F4;
        int i = fastFloor(x + skew);
        int j = fastFloor(y + skew);
        int k = fastFloor(z + skew);
        int l = fastFloor(w + skew);
        float unskew = (i + j + k + l) * G4;
        float X0 = i - unskew;
        float Y0 = j - unskew;
        float Z0 = k - unskew;
        float W0 = l - unskew;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;

        if (z0 > w0) rankz++; else rankw++;
        // @formatter:on

        int i1 = 2 - rankx >>> 31;
        int j1 = 2 - ranky >>> 31;
        int k1 = 2 - rankz >>> 31;
        int l1 = 2 - rankw >>> 31;

        int i2 = 1 - rankx >>> 31;
        int j2 = 1 - ranky >>> 31;
        int k2 = 1 - rankz >>> 31;
        int l2 = 1 - rankw >>> 31;

        int i3 = -rankx >>> 31;
        int j3 = -ranky >>> 31;
        int k3 = -rankz >>> 31;
        int l3 = -rankw >>> 31;

        float x1 = x0 - i1 + G4;
        float y1 = y0 - j1 + G4;
        float z1 = z0 - k1 + G4;
        float w1 = w0 - l1 + G4;

        float x2 = x0 - i2 + 2 * G4;
        float y2 = y0 - j2 + 2 * G4;
        float z2 = z0 - k2 + 2 * G4;
        float w2 = w0 - l2 + 2 * G4;

        float x3 = x0 - i3 + 3 * G4;
        float y3 = y0 - j3 + 3 * G4;
        float z3 = z0 - k3 + 3 * G4;
        float w3 = w0 - l3 + 3 * G4;

        float x4 = x0 - 1 + 4 * G4;
        float y4 = y0 - 1 + 4 * G4;
        float z4 = z0 - 1 + 4 * G4;
        float w4 = w0 - 1 + 4 * G4;

        float t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            t0 *= t0;
            t0 *= t0 * gradCoord4D(seed, i, j, k, l, x0, y0, z0, w0);
        }
        else t0 = 0;
        float t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            t1 *= t1;
            t1 *= t1 * gradCoord4D(seed, i + i1, j + j1, k + k1, l + l1, x1, y1, z1, w1);
        }
        else t1 = 0;
        float t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            t2 *= t2;
            t2 *= t2 * gradCoord4D(seed, i + i2, j + j2, k + k2, l + l2, x2, y2, z2, w2);
        }
        else t2 = 0;
        float t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            t3 *= t3;
            t3 *= t3 * gradCoord4D(seed, i + i3, j + j3, k + k3, l + l3, x3, y3, z3, w3);
        }
        else t3 = 0;
        float t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            t4 *= t4;
            t4 *= t4 * gradCoord4D(seed, i + 1, j + 1, k + 1, l + 1, x4, y4, z4, w4);
        }
        else t4 = 0;

//        float n = (t0 + t1 + t2 + t3 + t4) * 141.000f;
//        return n / (0.750f + Math.abs(n));
        float t = (t0 + t1 + t2 + t3 + t4) * 37.20266f;
        return t / (0.3f * Math.abs(t) + 0.7f);
//        return t / (0.3f * Math.abs(t) + (1f - 0.3f));// gain function for [-1, 1] domain and range
    }
    
    // 5D Simplex
    
    public float getSimplex(float x, float y, float z, float w, float u) {
        return singleSimplex(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency);
    }
    // Simplex Noise
    public float getSimplexFractal(float x, float y, float z, float w, float u) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y, z, w, u);
            case BILLOW:
                return singleSimplexFractalBillow(x, y, z, w, u);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y, z, w, u);
            default:
                return 0;
        }
    }

    protected float singleSimplexFractalFBM(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y, z, w, u);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z, w, u) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleSimplexFractalDomainWarp(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float latest = singleSimplex(seed, x, y, z, w, u);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleSimplex(++seed, x + a, y + b, z + c, w + d, u + e)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleSimplexFractalRidgedMulti(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y, z, w, u));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    protected float singleSimplexFractalBillow(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y, z, w, u)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(seed + i, x, y, z, w, u)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    public float singleSimplex(int seed, float x, float y, float z, float w, float u) {
        float n0, n1, n2, n3, n4, n5;
        float t = (x + y + z + w + u) * F5;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        int h = fastFloor(u + t);
        t = (i + j + k + l + h) * G5;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float U0 = h - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        float u0 = u - U0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;

        if (w0 > u0) rankw++; else ranku++;
        // @formatter:on

        int i1 = 3 - rankx >>> 31;
        int j1 = 3 - ranky >>> 31;
        int k1 = 3 - rankz >>> 31;
        int l1 = 3 - rankw >>> 31;
        int h1 = 3 - ranku >>> 31;

        int i2 = 2 - rankx >>> 31;
        int j2 = 2 - ranky >>> 31;
        int k2 = 2 - rankz >>> 31;
        int l2 = 2 - rankw >>> 31;
        int h2 = 2 - ranku >>> 31;

        int i3 = 1 - rankx >>> 31;
        int j3 = 1 - ranky >>> 31;
        int k3 = 1 - rankz >>> 31;
        int l3 = 1 - rankw >>> 31;
        int h3 = 1 - ranku >>> 31;

        int i4 = -rankx >>> 31;
        int j4 = -ranky >>> 31;
        int k4 = -rankz >>> 31;
        int l4 = -rankw >>> 31;
        int h4 = -ranku >>> 31;

        float x1 = x0 - i1 + G5;
        float y1 = y0 - j1 + G5;
        float z1 = z0 - k1 + G5;
        float w1 = w0 - l1 + G5;
        float u1 = u0 - h1 + G5;

        float x2 = x0 - i2 + 2 * G5;
        float y2 = y0 - j2 + 2 * G5;
        float z2 = z0 - k2 + 2 * G5;
        float w2 = w0 - l2 + 2 * G5;
        float u2 = u0 - h2 + 2 * G5;

        float x3 = x0 - i3 + 3 * G5;
        float y3 = y0 - j3 + 3 * G5;
        float z3 = z0 - k3 + 3 * G5;
        float w3 = w0 - l3 + 3 * G5;
        float u3 = u0 - h3 + 3 * G5;

        float x4 = x0 - i4 + 4 * G5;
        float y4 = y0 - j4 + 4 * G5;
        float z4 = z0 - k4 + 4 * G5;
        float w4 = w0 - l4 + 4 * G5;
        float u4 = u0 - h4 + 4 * G5;

        float x5 = x0 - 1 + 5 * G5;
        float y5 = y0 - 1 + 5 * G5;
        float z5 = z0 - 1 + 5 * G5;
        float w5 = w0 - 1 + 5 * G5;
        float u5 = u0 - 1 + 5 * G5;

        t = LIMIT5 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0;
        if (t < 0) n0 = 0;
        else {
            t *= t;
            n0 = t * t * gradCoord5D(seed, i, j, k, l, h, x0, y0, z0, w0, u0);
        }

        t = LIMIT5 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1;
        if (t < 0) n1 = 0;
        else {
            t *= t;
            n1 = t * t * gradCoord5D(seed, i + i1, j + j1, k + k1, l + l1, h + h1, x1, y1, z1, w1, u1);
        }

        t = LIMIT5 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2;
        if (t < 0) n2 = 0;
        else {
            t *= t;
            n2 = t * t * gradCoord5D(seed, i + i2, j + j2, k + k2, l + l2, h + h2, x2, y2, z2, w2, u2);
        }

        t = LIMIT5 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3;
        if (t < 0) n3 = 0;
        else {
            t *= t;
            n3 = t * t * gradCoord5D(seed, i + i3, j + j3, k + k3, l + l3, h + h3, x3, y3, z3, w3, u3);
        }

        t = LIMIT5 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4;
        if (t < 0) n4 = 0;
        else {
            t *= t;
            n4 = t * t * gradCoord5D(seed, i + i4, j + j4, k + k4, l + l4, h + h4, x4, y4, z4, w4, u4);
        }

        t = LIMIT5 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5;
        if (t < 0) n5 = 0;
        else {
            t *= t;
            n5 = t * t * gradCoord5D(seed, i + 1, j + 1, k + 1, l + 1, h + 1, x5, y5, z5, w5, u5);
        }

//        float n = (n0 + n1 + n2 + n3 + n4 + n5) * 32.000f;
//        return n / (0.700f + Math.abs(n));
        t = (n0 + n1 + n2 + n3 + n4 + n5) * 20.0f;
        return t / (0.5f * Math.abs(t) + 0.5f);
//        return t / (0.5f * Math.abs(t) + (1f - 0.5f));// gain function for [-1, 1] domain and range
    }

    // 6D Simplex

    public float getSimplex(float x, float y, float z, float w, float u, float v) {
        return singleSimplex(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency);
    }

    public float getSimplexFractal(float x, float y, float z, float w, float u, float v) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y, z, w, u, v);
            case BILLOW:
                return singleSimplexFractalBillow(x, y, z, w, u, v);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y, z, w, u, v);
            default:
                return 0;
        }
    }

    protected float singleSimplexFractalFBM(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y, z, w, u, v);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z, w, u, v) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleSimplexFractalDomainWarp(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float latest = singleSimplex(seed, x, y, z, w, u, v);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleSimplex(++seed, x + a, y + b, z + c, w + d, u + e, v + f)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleSimplexFractalRidgedMulti(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleSimplex(seed + i, x, y, z, w, u, v));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    protected float singleSimplexFractalBillow(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y, z, w, u, v)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(seed + i, x, y, z, w, u, v)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    public float singleSimplex(int seed, float x, float y, float z, float w, float u, float v) {
        float n0, n1, n2, n3, n4, n5, n6;
        float t = (x + y + z + w + u + v) * F6;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        int h = fastFloor(u + t);
        int g = fastFloor(v + t);
        t = (i + j + k + l + h + g) * G6;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float U0 = h - t;
        float V0 = g - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        float u0 = u - U0;
        float v0 = v - V0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;
        int rankv = 0;

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;
        if (x0 > v0) rankx++; else rankv++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;
        if (y0 > v0) ranky++; else rankv++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;
        if (z0 > v0) rankz++; else rankv++;

        if (w0 > u0) rankw++; else ranku++;
        if (w0 > v0) rankw++; else rankv++;

        if (u0 > v0) ranku++; else rankv++;
        // @formatter:on

        int i1 = 4 - rankx >>> 31;
        int j1 = 4 - ranky >>> 31;
        int k1 = 4 - rankz >>> 31;
        int l1 = 4 - rankw >>> 31;
        int h1 = 4 - ranku >>> 31;
        int g1 = 4 - rankv >>> 31;

        int i2 = 3 - rankx >>> 31;
        int j2 = 3 - ranky >>> 31;
        int k2 = 3 - rankz >>> 31;
        int l2 = 3 - rankw >>> 31;
        int h2 = 3 - ranku >>> 31;
        int g2 = 3 - rankv >>> 31;

        int i3 = 2 - rankx >>> 31;
        int j3 = 2 - ranky >>> 31;
        int k3 = 2 - rankz >>> 31;
        int l3 = 2 - rankw >>> 31;
        int h3 = 2 - ranku >>> 31;
        int g3 = 2 - rankv >>> 31;

        int i4 = 1 - rankx >>> 31;
        int j4 = 1 - ranky >>> 31;
        int k4 = 1 - rankz >>> 31;
        int l4 = 1 - rankw >>> 31;
        int h4 = 1 - ranku >>> 31;
        int g4 = 1 - rankv >>> 31;

        int i5 = -rankx >>> 31;
        int j5 = -ranky >>> 31;
        int k5 = -rankz >>> 31;
        int l5 = -rankw >>> 31;
        int h5 = -ranku >>> 31;
        int g5 = -rankv >>> 31;

        float x1 = x0 - i1 + G6;
        float y1 = y0 - j1 + G6;
        float z1 = z0 - k1 + G6;
        float w1 = w0 - l1 + G6;
        float u1 = u0 - h1 + G6;
        float v1 = v0 - g1 + G6;

        float x2 = x0 - i2 + 2 * G6;
        float y2 = y0 - j2 + 2 * G6;
        float z2 = z0 - k2 + 2 * G6;
        float w2 = w0 - l2 + 2 * G6;
        float u2 = u0 - h2 + 2 * G6;
        float v2 = v0 - g2 + 2 * G6;

        float x3 = x0 - i3 + 3 * G6;
        float y3 = y0 - j3 + 3 * G6;
        float z3 = z0 - k3 + 3 * G6;
        float w3 = w0 - l3 + 3 * G6;
        float u3 = u0 - h3 + 3 * G6;
        float v3 = v0 - g3 + 3 * G6;

        float x4 = x0 - i4 + 4 * G6;
        float y4 = y0 - j4 + 4 * G6;
        float z4 = z0 - k4 + 4 * G6;
        float w4 = w0 - l4 + 4 * G6;
        float u4 = u0 - h4 + 4 * G6;
        float v4 = v0 - g4 + 4 * G6;

        float x5 = x0 - i5 + 5 * G6;
        float y5 = y0 - j5 + 5 * G6;
        float z5 = z0 - k5 + 5 * G6;
        float w5 = w0 - l5 + 5 * G6;
        float u5 = u0 - h5 + 5 * G6;
        float v5 = v0 - g5 + 5 * G6;

        float x6 = x0 - 1 + 6 * G6;
        float y6 = y0 - 1 + 6 * G6;
        float z6 = z0 - 1 + 6 * G6;
        float w6 = w0 - 1 + 6 * G6;
        float u6 = u0 - 1 + 6 * G6;
        float v6 = v0 - 1 + 6 * G6;

        n0 = LIMIT6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0 - v0 * v0;
        if (n0 <= 0f) n0 = 0f;
        else {
            n0 *= n0;
            n0 *= n0 * gradCoord6D(seed, i, j, k, l, h, g, x0, y0, z0, w0, u0, v0);
        }

        n1 = LIMIT6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1 - v1 * v1;
        if (n1 <= 0f) n1 = 0f;
        else {
            n1 *= n1;
            n1 *= n1 * gradCoord6D(seed, i + i1, j + j1, k + k1, l + l1, h + h1, g + g1, x1, y1, z1, w1, u1, v1);
        }

        n2 = LIMIT6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2 - v2 * v2;
        if (n2 <= 0f) n2 = 0f;
        else {
            n2 *= n2;
            n2 *= n2 * gradCoord6D(seed, i + i2, j + j2, k + k2, l + l2, h + h2, g + g2, x2, y2, z2, w2, u2, v2);
        }

        n3 = LIMIT6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3 - v3 * v3;
        if (n3 <= 0f) n3 = 0f;
        else {
            n3 *= n3;
            n3 *= n3 * gradCoord6D(seed, i + i3, j + j3, k + k3, l + l3, h + h3, g + g3, x3, y3, z3, w3, u3, v3);
        }

        n4 = LIMIT6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4 - v4 * v4;
        if (n4 <= 0f) n4 = 0f;
        else {
            n4 *= n4;
            n4 *= n4 * gradCoord6D(seed, i + i4, j + j4, k + k4, l + l4, h + h4, g + g4, x4, y4, z4, w4, u4, v4);
        }

        n5 = LIMIT6 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5 - v5 * v5;
        if (n5 <= 0f) n5 = 0f;
        else {
            n5 *= n5;
            n5 *= n5 * gradCoord6D(seed, i + i5, j + j5, k + k5, l + l5, h + h5, g + g5, x5, y5, z5, w5, u5, v5);
        }

        n6 = LIMIT6 - x6 * x6 - y6 * y6 - z6 * z6 - w6 * w6 - u6 * u6 - v6 * v6;
        if (n6 <= 0f) n6 = 0f;
        else {
            n6 *= n6;
            n6 *= n6 * gradCoord6D(seed, i + 1, j + 1, k + 1, l + 1, h + 1, g + 1, x6, y6, z6, w6, u6, v6);
        }

//        float n = (n0 + n1 + n2 + n3 + n4 + n5 + n6) * 64.000f;
//        return n / (0.500f + Math.abs(n));
        t = (n0 + n1 + n2 + n3 + n4 + n5 + n6) * 7.499f;
        return t / (0.7f * Math.abs(t) + 0.3f);
//        return t / (0.7f * Math.abs(t) + (1f - 0.7f));// gain function for [-1, 1] domain and range
    }

    // Cubic Noise

    public float getCubicFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleCubicFractalFBM(x, y);
            case BILLOW:
                return singleCubicFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleCubicFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    protected float singleCubicFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleCubic(seed, x, y);
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleCubic(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleCubicFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = singleCubic(seed, x, y);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleCubic(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCubicFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleCubic(seed, x, y)) * 2 - 1;
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleCubic(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCubicFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleCubic(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getCubic(float x, float y) {
        x *= frequency;
        y *= frequency;

        return singleCubic(0, x, y);
    }

    private final static float CUBIC_2D_BOUNDING = 1f / 1.5f / 1.5f;

    protected float singleCubic(int seed, float x, float y) {
        int x1 = fastFloor(x);
        int y1 = fastFloor(y);

        int x0 = x1 - 1;
        int y0 = y1 - 1;
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int x3 = x1 + 2;
        int y3 = y1 + 2;

        float xs = x - (float) x1;
        float ys = y - (float) y1;

        float e =
                (sharpness *
//                (float)Math.pow(4.0f * sharpness,
//        return
                cubicLerp(
                cubicLerp(phCoord2D(seed, x0, y0), phCoord2D(seed, x1, y0), phCoord2D(seed, x2, y0), phCoord2D(seed, x3, y0),
                        xs),
                cubicLerp(phCoord2D(seed, x0, y1), phCoord2D(seed, x1, y1), phCoord2D(seed, x2, y1), phCoord2D(seed, x3, y1),
                        xs),
                cubicLerp(phCoord2D(seed, x0, y2), phCoord2D(seed, x1, y2), phCoord2D(seed, x2, y2), phCoord2D(seed, x3, y2),
                        xs),
                cubicLerp(phCoord2D(seed, x0, y3), phCoord2D(seed, x1, y3), phCoord2D(seed, x2, y3), phCoord2D(seed, x3, y3),
                        xs),
                ys)
        );
        return e / (1f + Math.abs(e));
//        return (e - 1.0f) / (e + 1.0f);
//                * CUBIC_2D_BOUNDING;
    }

    public float getCubicFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleCubicFractalFBM(x, y, z);
            case BILLOW:
                return singleCubicFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleCubicFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    protected float singleCubicFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleCubic(seed, x, y, z);
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleCubic(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleCubicFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singleCubic(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleCubic(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCubicFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleCubic(seed, x, y, z)) * 2 - 1;
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleCubic(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCubicFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleCubic(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getCubic(float x, float y, float z) {
        return singleCubic(seed, x * frequency, y * frequency, z * frequency);
    }

    private final static float CUBIC_3D_BOUNDING = 1f / (1.5f * 1.5f * 1.5f);

    protected float singleCubic(int seed, float x, float y, float z) {
        int x1 = fastFloor(x);
        int y1 = fastFloor(y);
        int z1 = fastFloor(z);

        int x0 = x1 - 1;
        int y0 = y1 - 1;
        int z0 = z1 - 1;
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;
        int x3 = x1 + 2;
        int y3 = y1 + 2;
        int z3 = z1 + 2;

        float xs = x - (float) x1;
        float ys = y - (float) y1;
        float zs = z - (float) z1;

        float e =
                (sharpness *
//                (float)Math.pow(4.0f * sharpness,
//        return
                cubicLerp(
                cubicLerp(
                        cubicLerp(phCoord3D(seed, x0, y0, z0), phCoord3D(seed, x1, y0, z0), phCoord3D(seed, x2, y0, z0), phCoord3D(seed, x3, y0, z0), xs),
                        cubicLerp(phCoord3D(seed, x0, y1, z0), phCoord3D(seed, x1, y1, z0), phCoord3D(seed, x2, y1, z0), phCoord3D(seed, x3, y1, z0), xs),
                        cubicLerp(phCoord3D(seed, x0, y2, z0), phCoord3D(seed, x1, y2, z0), phCoord3D(seed, x2, y2, z0), phCoord3D(seed, x3, y2, z0), xs),
                        cubicLerp(phCoord3D(seed, x0, y3, z0), phCoord3D(seed, x1, y3, z0), phCoord3D(seed, x2, y3, z0), phCoord3D(seed, x3, y3, z0), xs),
                        ys),
                cubicLerp(
                        cubicLerp(phCoord3D(seed, x0, y0, z1), phCoord3D(seed, x1, y0, z1), phCoord3D(seed, x2, y0, z1), phCoord3D(seed, x3, y0, z1), xs),
                        cubicLerp(phCoord3D(seed, x0, y1, z1), phCoord3D(seed, x1, y1, z1), phCoord3D(seed, x2, y1, z1), phCoord3D(seed, x3, y1, z1), xs),
                        cubicLerp(phCoord3D(seed, x0, y2, z1), phCoord3D(seed, x1, y2, z1), phCoord3D(seed, x2, y2, z1), phCoord3D(seed, x3, y2, z1), xs),
                        cubicLerp(phCoord3D(seed, x0, y3, z1), phCoord3D(seed, x1, y3, z1), phCoord3D(seed, x2, y3, z1), phCoord3D(seed, x3, y3, z1), xs),
                        ys),
                cubicLerp(
                        cubicLerp(phCoord3D(seed, x0, y0, z2), phCoord3D(seed, x1, y0, z2), phCoord3D(seed, x2, y0, z2), phCoord3D(seed, x3, y0, z2), xs),
                        cubicLerp(phCoord3D(seed, x0, y1, z2), phCoord3D(seed, x1, y1, z2), phCoord3D(seed, x2, y1, z2), phCoord3D(seed, x3, y1, z2), xs),
                        cubicLerp(phCoord3D(seed, x0, y2, z2), phCoord3D(seed, x1, y2, z2), phCoord3D(seed, x2, y2, z2), phCoord3D(seed, x3, y2, z2), xs),
                        cubicLerp(phCoord3D(seed, x0, y3, z2), phCoord3D(seed, x1, y3, z2), phCoord3D(seed, x2, y3, z2), phCoord3D(seed, x3, y3, z2), xs),
                        ys),
                cubicLerp(
                        cubicLerp(phCoord3D(seed, x0, y0, z3), phCoord3D(seed, x1, y0, z3), phCoord3D(seed, x2, y0, z3), phCoord3D(seed, x3, y0, z3), xs),
                        cubicLerp(phCoord3D(seed, x0, y1, z3), phCoord3D(seed, x1, y1, z3), phCoord3D(seed, x2, y1, z3), phCoord3D(seed, x3, y1, z3), xs),
                        cubicLerp(phCoord3D(seed, x0, y2, z3), phCoord3D(seed, x1, y2, z3), phCoord3D(seed, x2, y2, z3), phCoord3D(seed, x3, y2, z3), xs),
                        cubicLerp(phCoord3D(seed, x0, y3, z3), phCoord3D(seed, x1, y3, z3), phCoord3D(seed, x2, y3, z3), phCoord3D(seed, x3, y3, z3), xs),
                        ys),
                zs)
                );
        return e / (1f + Math.abs(e));
//        return (e - 1.0f) / (e + 1.0f);

//        ;
//        return TrigTools.sinTurns(e);

//        return e * CUBIC_3D_BOUNDING;

    }

    public float getCubicFractal(float x, float y, float z, float w) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;

        switch (fractalType) {
            case FBM:
                return singleCubicFractalFBM(x, y, z, w);
            case BILLOW:
                return singleCubicFractalBillow(x, y, z, w);
            case RIDGED_MULTI:
                return singleCubicFractalRidgedMulti(x, y, z, w);
            default:
                return 0;
        }
    }

    protected float singleCubicFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleCubic(seed, x, y, z, w);
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleCubic(++seed, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleCubicFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singleCubic(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleCubic(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCubicFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleCubic(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleCubic(++seed, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCubicFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleCubic(seed + i, x, y, z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getCubic(float x, float y, float z, float w) {
        return singleCubic(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    private final static float CUBIC_4D_BOUNDING = 1f / (1.5f * 1.5f * 1.5f * 1.5f);

    protected float singleCubic(int seed, float x, float y, float z, float w) {
        int x1 = fastFloor(x);
        int y1 = fastFloor(y);
        int z1 = fastFloor(z);
        int w1 = fastFloor(w);

        int x0 = x1 - 1;
        int y0 = y1 - 1;
        int z0 = z1 - 1;
        int w0 = w1 - 1;
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;
        int w2 = w1 + 1;
        int x3 = x1 + 2;
        int y3 = y1 + 2;
        int z3 = z1 + 2;
        int w3 = w1 + 2;

        float xs = x - (float) x1;
        float ys = y - (float) y1;
        float zs = z - (float) z1;
        float ws = w - (float) w1;

        float e =
                (sharpness *
//                (float)Math.pow(4.0f * sharpness,
//        return
                cubicLerp(
                cubicLerp(
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z0, w0), phCoord4D(seed, x1, y0, z0, w0), phCoord4D(seed, x2, y0, z0, w0), phCoord4D(seed, x3, y0, z0, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z0, w0), phCoord4D(seed, x1, y1, z0, w0), phCoord4D(seed, x2, y1, z0, w0), phCoord4D(seed, x3, y1, z0, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z0, w0), phCoord4D(seed, x1, y2, z0, w0), phCoord4D(seed, x2, y2, z0, w0), phCoord4D(seed, x3, y2, z0, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z0, w0), phCoord4D(seed, x1, y3, z0, w0), phCoord4D(seed, x2, y3, z0, w0), phCoord4D(seed, x3, y3, z0, w0), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z1, w0), phCoord4D(seed, x1, y0, z1, w0), phCoord4D(seed, x2, y0, z1, w0), phCoord4D(seed, x3, y0, z1, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z1, w0), phCoord4D(seed, x1, y1, z1, w0), phCoord4D(seed, x2, y1, z1, w0), phCoord4D(seed, x3, y1, z1, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z1, w0), phCoord4D(seed, x1, y2, z1, w0), phCoord4D(seed, x2, y2, z1, w0), phCoord4D(seed, x3, y2, z1, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z1, w0), phCoord4D(seed, x1, y3, z1, w0), phCoord4D(seed, x2, y3, z1, w0), phCoord4D(seed, x3, y3, z1, w0), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z2, w0), phCoord4D(seed, x1, y0, z2, w0), phCoord4D(seed, x2, y0, z2, w0), phCoord4D(seed, x3, y0, z2, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z2, w0), phCoord4D(seed, x1, y1, z2, w0), phCoord4D(seed, x2, y1, z2, w0), phCoord4D(seed, x3, y1, z2, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z2, w0), phCoord4D(seed, x1, y2, z2, w0), phCoord4D(seed, x2, y2, z2, w0), phCoord4D(seed, x3, y2, z2, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z2, w0), phCoord4D(seed, x1, y3, z2, w0), phCoord4D(seed, x2, y3, z2, w0), phCoord4D(seed, x3, y3, z2, w0), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z3, w0), phCoord4D(seed, x1, y0, z3, w0), phCoord4D(seed, x2, y0, z3, w0), phCoord4D(seed, x3, y0, z3, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z3, w0), phCoord4D(seed, x1, y1, z3, w0), phCoord4D(seed, x2, y1, z3, w0), phCoord4D(seed, x3, y1, z3, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z3, w0), phCoord4D(seed, x1, y2, z3, w0), phCoord4D(seed, x2, y2, z3, w0), phCoord4D(seed, x3, y2, z3, w0), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z3, w0), phCoord4D(seed, x1, y3, z3, w0), phCoord4D(seed, x2, y3, z3, w0), phCoord4D(seed, x3, y3, z3, w0), xs),
                                ys),
                        zs),
                cubicLerp(
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z0, w1), phCoord4D(seed, x1, y0, z0, w1), phCoord4D(seed, x2, y0, z0, w1), phCoord4D(seed, x3, y0, z0, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z0, w1), phCoord4D(seed, x1, y1, z0, w1), phCoord4D(seed, x2, y1, z0, w1), phCoord4D(seed, x3, y1, z0, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z0, w1), phCoord4D(seed, x1, y2, z0, w1), phCoord4D(seed, x2, y2, z0, w1), phCoord4D(seed, x3, y2, z0, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z0, w1), phCoord4D(seed, x1, y3, z0, w1), phCoord4D(seed, x2, y3, z0, w1), phCoord4D(seed, x3, y3, z0, w1), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z1, w1), phCoord4D(seed, x1, y0, z1, w1), phCoord4D(seed, x2, y0, z1, w1), phCoord4D(seed, x3, y0, z1, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z1, w1), phCoord4D(seed, x1, y1, z1, w1), phCoord4D(seed, x2, y1, z1, w1), phCoord4D(seed, x3, y1, z1, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z1, w1), phCoord4D(seed, x1, y2, z1, w1), phCoord4D(seed, x2, y2, z1, w1), phCoord4D(seed, x3, y2, z1, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z1, w1), phCoord4D(seed, x1, y3, z1, w1), phCoord4D(seed, x2, y3, z1, w1), phCoord4D(seed, x3, y3, z1, w1), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z2, w1), phCoord4D(seed, x1, y0, z2, w1), phCoord4D(seed, x2, y0, z2, w1), phCoord4D(seed, x3, y0, z2, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z2, w1), phCoord4D(seed, x1, y1, z2, w1), phCoord4D(seed, x2, y1, z2, w1), phCoord4D(seed, x3, y1, z2, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z2, w1), phCoord4D(seed, x1, y2, z2, w1), phCoord4D(seed, x2, y2, z2, w1), phCoord4D(seed, x3, y2, z2, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z2, w1), phCoord4D(seed, x1, y3, z2, w1), phCoord4D(seed, x2, y3, z2, w1), phCoord4D(seed, x3, y3, z2, w1), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z3, w1), phCoord4D(seed, x1, y0, z3, w1), phCoord4D(seed, x2, y0, z3, w1), phCoord4D(seed, x3, y0, z3, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z3, w1), phCoord4D(seed, x1, y1, z3, w1), phCoord4D(seed, x2, y1, z3, w1), phCoord4D(seed, x3, y1, z3, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z3, w1), phCoord4D(seed, x1, y2, z3, w1), phCoord4D(seed, x2, y2, z3, w1), phCoord4D(seed, x3, y2, z3, w1), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z3, w1), phCoord4D(seed, x1, y3, z3, w1), phCoord4D(seed, x2, y3, z3, w1), phCoord4D(seed, x3, y3, z3, w1), xs),
                                ys),
                        zs),
                cubicLerp(
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z0, w2), phCoord4D(seed, x1, y0, z0, w2), phCoord4D(seed, x2, y0, z0, w2), phCoord4D(seed, x3, y0, z0, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z0, w2), phCoord4D(seed, x1, y1, z0, w2), phCoord4D(seed, x2, y1, z0, w2), phCoord4D(seed, x3, y1, z0, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z0, w2), phCoord4D(seed, x1, y2, z0, w2), phCoord4D(seed, x2, y2, z0, w2), phCoord4D(seed, x3, y2, z0, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z0, w2), phCoord4D(seed, x1, y3, z0, w2), phCoord4D(seed, x2, y3, z0, w2), phCoord4D(seed, x3, y3, z0, w2), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z1, w2), phCoord4D(seed, x1, y0, z1, w2), phCoord4D(seed, x2, y0, z1, w2), phCoord4D(seed, x3, y0, z1, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z1, w2), phCoord4D(seed, x1, y1, z1, w2), phCoord4D(seed, x2, y1, z1, w2), phCoord4D(seed, x3, y1, z1, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z1, w2), phCoord4D(seed, x1, y2, z1, w2), phCoord4D(seed, x2, y2, z1, w2), phCoord4D(seed, x3, y2, z1, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z1, w2), phCoord4D(seed, x1, y3, z1, w2), phCoord4D(seed, x2, y3, z1, w2), phCoord4D(seed, x3, y3, z1, w2), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z2, w2), phCoord4D(seed, x1, y0, z2, w2), phCoord4D(seed, x2, y0, z2, w2), phCoord4D(seed, x3, y0, z2, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z2, w2), phCoord4D(seed, x1, y1, z2, w2), phCoord4D(seed, x2, y1, z2, w2), phCoord4D(seed, x3, y1, z2, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z2, w2), phCoord4D(seed, x1, y2, z2, w2), phCoord4D(seed, x2, y2, z2, w2), phCoord4D(seed, x3, y2, z2, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z2, w2), phCoord4D(seed, x1, y3, z2, w2), phCoord4D(seed, x2, y3, z2, w2), phCoord4D(seed, x3, y3, z2, w2), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z3, w2), phCoord4D(seed, x1, y0, z3, w2), phCoord4D(seed, x2, y0, z3, w2), phCoord4D(seed, x3, y0, z3, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z3, w2), phCoord4D(seed, x1, y1, z3, w2), phCoord4D(seed, x2, y1, z3, w2), phCoord4D(seed, x3, y1, z3, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z3, w2), phCoord4D(seed, x1, y2, z3, w2), phCoord4D(seed, x2, y2, z3, w2), phCoord4D(seed, x3, y2, z3, w2), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z3, w2), phCoord4D(seed, x1, y3, z3, w2), phCoord4D(seed, x2, y3, z3, w2), phCoord4D(seed, x3, y3, z3, w2), xs),
                                ys),
                        zs),
                cubicLerp(
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z0, w3), phCoord4D(seed, x1, y0, z0, w3), phCoord4D(seed, x2, y0, z0, w3), phCoord4D(seed, x3, y0, z0, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z0, w3), phCoord4D(seed, x1, y1, z0, w3), phCoord4D(seed, x2, y1, z0, w3), phCoord4D(seed, x3, y1, z0, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z0, w3), phCoord4D(seed, x1, y2, z0, w3), phCoord4D(seed, x2, y2, z0, w3), phCoord4D(seed, x3, y2, z0, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z0, w3), phCoord4D(seed, x1, y3, z0, w3), phCoord4D(seed, x2, y3, z0, w3), phCoord4D(seed, x3, y3, z0, w3), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z1, w3), phCoord4D(seed, x1, y0, z1, w3), phCoord4D(seed, x2, y0, z1, w3), phCoord4D(seed, x3, y0, z1, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z1, w3), phCoord4D(seed, x1, y1, z1, w3), phCoord4D(seed, x2, y1, z1, w3), phCoord4D(seed, x3, y1, z1, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z1, w3), phCoord4D(seed, x1, y2, z1, w3), phCoord4D(seed, x2, y2, z1, w3), phCoord4D(seed, x3, y2, z1, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z1, w3), phCoord4D(seed, x1, y3, z1, w3), phCoord4D(seed, x2, y3, z1, w3), phCoord4D(seed, x3, y3, z1, w3), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z2, w3), phCoord4D(seed, x1, y0, z2, w3), phCoord4D(seed, x2, y0, z2, w3), phCoord4D(seed, x3, y0, z2, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z2, w3), phCoord4D(seed, x1, y1, z2, w3), phCoord4D(seed, x2, y1, z2, w3), phCoord4D(seed, x3, y1, z2, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z2, w3), phCoord4D(seed, x1, y2, z2, w3), phCoord4D(seed, x2, y2, z2, w3), phCoord4D(seed, x3, y2, z2, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z2, w3), phCoord4D(seed, x1, y3, z2, w3), phCoord4D(seed, x2, y3, z2, w3), phCoord4D(seed, x3, y3, z2, w3), xs),
                                ys),
                        cubicLerp(
                                cubicLerp(phCoord4D(seed, x0, y0, z3, w3), phCoord4D(seed, x1, y0, z3, w3), phCoord4D(seed, x2, y0, z3, w3), phCoord4D(seed, x3, y0, z3, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y1, z3, w3), phCoord4D(seed, x1, y1, z3, w3), phCoord4D(seed, x2, y1, z3, w3), phCoord4D(seed, x3, y1, z3, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y2, z3, w3), phCoord4D(seed, x1, y2, z3, w3), phCoord4D(seed, x2, y2, z3, w3), phCoord4D(seed, x3, y2, z3, w3), xs),
                                cubicLerp(phCoord4D(seed, x0, y3, z3, w3), phCoord4D(seed, x1, y3, z3, w3), phCoord4D(seed, x2, y3, z3, w3), phCoord4D(seed, x3, y3, z3, w3), xs),
                                ys),
                        zs),
                ws)
        );
        return e / (1f + Math.abs(e));
//        return (e - 1.0f) / (e + 1.0f);
//                * CUBIC_4D_BOUNDING;
    }

    // Cellular 2D

    public float getCellular(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (cellularReturnType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return singleCellular(seed, x, y);
            case DISTANCE_VALUE:
                return singleCellularMerging(seed, x, y);
            default:
                return singleCellular2Edge(seed, x, y);
        }
    }

    public float switchCellular(int seed, float x, float y) {
        switch (cellularReturnType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return singleCellular(seed, x, y);
            case DISTANCE_VALUE:
                return singleCellularMerging(seed, x, y);
            default:
                return singleCellular2Edge(seed, x, y);
        }
    }

    protected float singleCellular(int seed, float x, float y) {
        int xr = fastRound(x);
        int yr = fastRound(y);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;
        float distance = 999999;
        int xc = 0, yc = 0;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int vec = hash256(xi, yi, seed) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float newDistance = vecX * vecX + vecY * vecY;

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int vec = hash256(xi, yi, seed) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float newDistance = (Math.abs(vecX) + Math.abs(vecY));

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        }
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int vec = hash256(xi, yi, seed) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float newDistance = (Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        }
                    }
                }
                distance /= 1f + MathTools.ROOT2_INVERSE;
                break;
        }

        switch (cellularReturnType) {
            case CELL_VALUE:
                return valCoord2D(seed, xc, yc);

            case NOISE_LOOKUP:
                int vec = hash256(xc, yc, seed) << 1;

                return layered2D(xc + gradients[vec], yc + gradients[vec+1], 123, 3);

            case DISTANCE:
                return distance - 1;

            default:
                return 0;
        }
    }


    protected float singleCellularMerging(int seed, float x, float y) {
        int xr = fastRound(x);
        int yr = fastRound(y);

        float sum = 0f;
        int hash;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        hash = hashAll(xi, yi, seed);
                        int vec = (hash & 255) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float distance = 1f - (vecX * vecX + vecY * vecY);

                        if (distance > 0f) {
                            distance *= 3f;
                            sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance;
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        hash = hashAll(xi, yi, seed);
                        int vec = (hash & 255) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float distance = 1f - (Math.abs(vecX) + Math.abs(vecY));

                        if (distance > 0f) {
                            distance *= 3f;
                            sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance;
                        }
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        hash = hashAll(xi, yi, seed);
                        int vec = (hash & 255) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float distance = 2f - ((Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY));

                        if (distance > 0f) {
                            distance *= 3f;
                            sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance * 0.125f;
                        }
                    }
                }
                break;
        }
        return sum / (64f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    protected float singleCellular2Edge(int seed, float x, float y) {
        int xr = fastRound(x);
        int yr = fastRound(y);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

        float distance = 999999;
        float distance2 = 999999;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int vec = hash256(xi, yi, seed) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float newDistance = vecX * vecX + vecY * vecY;

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int vec = hash256(xi, yi, seed) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float newDistance = Math.abs(vecX) + Math.abs(vecY);

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int vec = hash256(xi, yi, seed) << 1;

                        float vecX = xi - x + gradients[vec];
                        float vecY = yi - y + gradients[vec+1];

                        float newDistance = (Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
                distance2 /= 1f + MathTools.ROOT2_INVERSE;
                distance /= 1f + MathTools.ROOT2_INVERSE;
                break;
        }

        switch (cellularReturnType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }
    protected float singleCellularFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = switchCellular(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += switchCellular(seed + i, x, y) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleCellularFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = switchCellular(seed, x, y);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = switchCellular(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCellularFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(switchCellular(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(switchCellular(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCellularFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(switchCellular(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if (fractalSpiral) {
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2;
                y = y2;
            }
            x *= lacunarity;
            y *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    // Cellular 3D
    public float getCellular(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (cellularReturnType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return singleCellular(seed, x, y, z);
            case DISTANCE_VALUE:
                return singleCellularMerging(seed, x, y, z);
            default:
                return singleCellular2Edge(seed, x, y, z);
        }
    }

    protected float switchCellular(int seed, float x, float y, float z) {
        switch (cellularReturnType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return singleCellular(seed, x, y, z);
            case DISTANCE_VALUE:
                return singleCellularMerging(seed, x, y, z);
            default:
                return singleCellular2Edge(seed, x, y, z);
        }
    }

    protected float singleCellular(int seed, float x, float y, float z) {
        int xr = fastRound(x);
        int yr = fastRound(y);
        int zr = fastRound(z);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;
        float distance = 999999;
        int xc = 0, yc = 0, zc = 0;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int vec = hash256(xi, yi, zi, seed)<<2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                            if (newDistance < distance) {
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            }
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int vec = hash256(xi, yi, zi, seed)<<2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float newDistance = Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ);

                            if (newDistance < distance) {
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            }
                        }
                    }
                }
                distance /= 1.5f;
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int vec = hash256(xi, yi, zi, seed)<<2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float newDistance = (Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

                            if (newDistance < distance) {
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            }
                        }
                    }
                }
                distance *= 0.5f;
                break;
        }

        switch (cellularReturnType) {
            case CELL_VALUE:
                return valCoord3D(seed, xc, yc, zc);

            case NOISE_LOOKUP:
                int vec = hash256(xc, yc, zc, seed)<<2;
                return layered3D(xc + gradients[vec], yc + gradients[vec+1], zc + gradients[vec+2], 123, 3);

            case DISTANCE:
                return distance - 1;

            default:
                return 0;
        }
    }

    protected float singleCellularMerging(int seed, float x, float y, float z) {
        int xr = fastRound(x);
        int yr = fastRound(y);
        int zr = fastRound(z);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        float sum = 0f;
        int hash;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            hash = hashAll(xi, yi, zi, seed);
                            int vec = (hash & 255) << 2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float distance = 1f - (vecX * vecX + vecY * vecY + vecZ * vecZ);

                            if (distance > 0f) {
                                distance *= 3f;
                                sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance;
                            }
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            hash = hashAll(xi, yi, zi, seed);
                            int vec = (hash & 255) << 2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float distance = 1f - (Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ));

                            if (distance > 0f) {
                                distance *= 3f;
                                sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance;
                            }
                        }
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            hash = hashAll(xi, yi, zi, seed);
                            int vec = (hash & 255) << 2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float distance = 2f - ((Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ));

                            if (distance > 0f) {
                                distance *= 3f;
                                sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance * 0.125f;
                            }
                        }
                    }
                }
                break;
        }
        return sum / (64f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    protected float singleCellular2Edge(int seed, float x, float y, float z) {
        int xr = fastRound(x);
        int yr = fastRound(y);
        int zr = fastRound(z);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        float distance = 999999;
        float distance2 = 999999;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int vec = hash256(xi, yi, zi, seed)<<2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                            distance2 = Math.max(Math.min(distance2, newDistance), distance);
                            distance = Math.min(distance, newDistance);
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int vec = hash256(xi, yi, zi, seed)<<2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float newDistance = Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ);

                            distance2 = Math.max(Math.min(distance2, newDistance), distance);
                            distance = Math.min(distance, newDistance);
                        }
                    }
                }
                distance /= 1.5f;
                distance2 /= 1.5f;
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int vec = hash256(xi, yi, zi, seed)<<2;

                            float vecX = xi - x + gradients[vec];
                            float vecY = yi - y + gradients[vec+1];
                            float vecZ = zi - z + gradients[vec+2];

                            float newDistance = (Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

                            distance2 = Math.max(Math.min(distance2, newDistance), distance);
                            distance = Math.min(distance, newDistance);
                        }
                    }
                }
                distance *= 0.5f;
                distance2 *= 0.5f;
                break;
        }

        switch (cellularReturnType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    protected float singleCellularFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = switchCellular(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += switchCellular(seed + i, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleCellularFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = switchCellular(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = switchCellular(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCellularFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(switchCellular(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(switchCellular(seed + i, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleCellularFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(switchCellular(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getHoney(float x, float y) {
        return singleHoney(seed, x * frequency, y * frequency);
    }

    public float singleHoney(int seed, float x, float y) {
        final float result = (singleSimplex(seed, x, y) + singleValue(seed ^ 0x9E3779B9, x, y)) * 0.5f + 1f;
        return (result <= 1f) ? result * result - 1f : (result - 2f) * -(result - 2f) + 1f;

        //return singleSimplex(seed, x, y, 0.5f * singleSimplex(seed * 0xDAB ^ 0x9E3779BD, x, y));
//        return singleSimplex(seed, x, y + 0.25f * singleSimplex(seed * 0xDAB ^ 0x9E3779BD, y, x));
//        return singleSimplex(seed, x + MathTools.wobble(seed, y) * 0.25f, y + MathTools.wobble(seed, x) * 0.25f);
        
//        final float a = singleSimplex(seed, x, y);
//        seed += 0x9E377;
//        final float b = singleSimplex(seed, x + a * 0.25f, y);
//        return TrigTools.sinTurns((a + b) * 0.25f);
    }

    public float getHoneyFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleHoneyFractalFBM(x, y);
            case BILLOW:
                return singleHoneyFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleHoneyFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    protected float singleHoneyFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleHoney(seed, x, y);
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;

            amp *= gain;
            sum += singleHoney(seed + i, x, y) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleHoneyFractalDomainWarp(float x, float y) {
        int seed = this.seed;
        float latest = singleHoney(seed, x, y);
        float sum = latest;
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleHoney(++seed, x + a, y + b)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleHoney(seed, x, y)) * 2 - 1;
        float amp = 1, t;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;

            amp *= gain;
            sum += (Math.abs(singleHoney(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float t;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleHoney(seed + i, x, y));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            t = x;
            x = y * lacunarity;
            y = t * lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getHoneyFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleHoneyFractalFBM(x, y, z);
            case BILLOW:
                return singleHoneyFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleHoneyFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    protected float singleHoneyFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleHoney(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleHoney(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleHoneyFractalDomainWarp(float x, float y, float z) {
        int seed = this.seed;
        float latest = singleHoney(seed, x, y, z);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleHoney(++seed, x + a, y + b, z + c)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleHoney(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleHoney(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleHoney(seed + i, x, y, z));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getHoney(float x, float y, float z) {
        return singleHoney(seed, x * frequency, y * frequency, z * frequency);
    }

    public float singleHoney(int seed, float x, float y, float z){
        final float result = (singleSimplex(seed, x, y, z) + singleValue(seed ^ 0x9E3779B9, x, y, z)) * 0.5f + 1f;
        return (result <= 1f) ? result * result - 1f : (result - 2f) * -(result - 2f) + 1f;
    }


    protected float singleHoneyFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleHoney(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleHoney(++seed, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleHoneyFractalDomainWarp(float x, float y, float z, float w) {
        int seed = this.seed;
        float latest = singleHoney(seed, x, y, z, w);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleHoney(++seed, x + a, y + b, z + c, w + d)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleHoney(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleHoney(++seed, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleHoney(seed + i, x, y,  z, w));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getHoney(float x, float y, float z, float w) {
        return singleHoney(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }

    public float singleHoney(int seed, float x, float y, float z, float w) {
        final float result = (singleSimplex(seed, x, y, z, w) + singleValue(seed ^ 0x9E3779B9, x, y, z, w)) * 0.5f + 1f;
        return (result <= 1f) ? result * result - 1f : (result - 2f) * -(result - 2f) + 1f;
    }
    public float getHoneyFractal(float x, float y, float z, float w, float u) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        switch (fractalType) {
            case FBM:
                return singleHoneyFractalFBM(x, y, z, w, u);
            case BILLOW:
                return singleHoneyFractalBillow(x, y, z, w, u);
            case RIDGED_MULTI:
                return singleHoneyFractalRidgedMulti(x, y, z, w, u);
            default:
                return 0;
        }
    }

    protected float singleHoneyFractalFBM(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = singleHoney(seed, x, y, z, w, u);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += singleHoney(seed + i, x, y, z, w, u) * amp;
        }

        return sum * fractalBounding;
    }
    protected float singleHoneyFractalDomainWarp(float x, float y, float z, float w, float u) {
        int seed = this.seed;
        float latest = singleHoney(seed, x, y, z, w, u);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleHoney(++seed, x + a, y + b, z + c, w + d, u + e)) * amp;
        }

        return sum * fractalBounding;
    }


    protected float singleHoneyFractalBillow(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = Math.abs(singleHoney(seed, x, y, z, w, u)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleHoney(seed + i, x, y, z, w, u)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalRidgedMulti(float x, float y, float z, float w, float u) {
        final int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleHoney(seed + i, x, y, z, w, u));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getHoney(float x, float y, float z, float w, float u) {
        return singleHoney(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency);
    }

    public float singleHoney(int seed, float x, float y, float z, float w, float u) {
        final float result = (singleSimplex(seed, x, y, z, w, u) + singleValue(seed ^ 0x9E3779B9, x, y, z, w, u)) * 0.5f + 1f;
        return (result <= 1f) ? result * result - 1f : (result - 2f) * -(result - 2f) + 1f;
    }

    public float getHoneyFractal(float x, float y, float z, float w, float u, float v) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        switch (fractalType) {
            case FBM:
                return singleHoneyFractalFBM(x, y, z, w, u, v);
            case BILLOW:
                return singleHoneyFractalBillow(x, y, z, w, u, v);
            case RIDGED_MULTI:
                return singleHoneyFractalRidgedMulti(x, y, z, w, u, v);
            default:
                return 0;
        }
    }

    protected float singleHoneyFractalFBM(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = singleHoney(seed, x, y, z, w, u, v);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += singleHoney(++seed, x, y, z, w, u, v) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalDomainWarp(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float latest = singleHoney(seed, x, y, z, w, u, v);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x = x * lacunarity;
            y = y * lacunarity;
            z = z * lacunarity;
            w = w * lacunarity;
            u = u * lacunarity;
            v = v * lacunarity;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= gain;
            sum += (latest = singleHoney(++seed, x + a, y + b, z + c, w + d, u + e, v + f)) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalBillow(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = Math.abs(singleHoney(seed, x, y, z, w, u, v)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleHoney(++seed, x, y, z, w, u, v)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    protected float singleHoneyFractalRidgedMulti(float x, float y, float z, float w, float u, float v) {
        int seed = this.seed;
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(singleHoney(seed + i, x, y, z, w, u, v));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            u *= lacunarity;
            v *= lacunarity;
        }
        return sum * 2f / correction - 1f;
    }

    public float getHoney(float x, float y, float z, float w, float u, float v) {
        return singleHoney(seed, x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency);
    }

    public float singleHoney(int seed, float x, float y, float z, float w, float u, float v) {
        final float result = (singleSimplex(seed, x, y, z, w, u, v) + singleValue(seed ^ 0x9E3779B9, x, y, z, w, u, v)) * 0.5f + 1f;
        return (result <= 1f) ? result * result - 1f : (result - 2f) * -(result - 2f) + 1f;
    }

    // Gradient Perturb (currently unused)

    public void gradientPerturb2(float[] v2) {
        singleGradientPerturb2(seed, gradientPerturbAmp, frequency, v2);
    }

    public void gradientPerturbFractal2(float[] v2) {
        int seed = this.seed;
        float amp = gradientPerturbAmp * fractalBounding;
        float freq = frequency;

        singleGradientPerturb2(seed, amp, frequency, v2);

        for (int i = 1; i < octaves; i++) {
            freq *= lacunarity;
            amp *= gain;
            singleGradientPerturb2(++seed, amp, freq, v2);
        }
    }

    private void singleGradientPerturb2(int seed, float perturbAmp, float frequency, float[] v2) {
        float xf = v2[0] * frequency;
        float yf = v2[1] * frequency;

        int x0 = fastFloor(xf);
        int y0 = fastFloor(yf);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

        float xs, ys;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = xf - x0;
                ys = yf - y0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(xf - x0);
                ys = hermiteInterpolator(yf - y0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(xf - x0);
                ys = quinticInterpolator(yf - y0);
                break;
        }

        int vec0 = hash256(x0, y0, seed)<<1;
        int vec1 = hash256(x1, y0, seed)<<1;

        float lx0x = lerp(gradients[vec0], gradients[vec1], xs);
        float ly0x = lerp(gradients[vec0+1], gradients[vec1+1], xs);

        vec0 = hash256(x0, y1, seed)<<1;
        vec1 = hash256(x1, y1, seed)<<1;

        float lx1x = lerp(gradients[vec0], gradients[vec1], xs);
        float ly1x = lerp(gradients[vec0+1], gradients[vec1+1], xs);

        v2[0] += lerp(lx0x, lx1x, ys) * perturbAmp;
        v2[1] += lerp(ly0x, ly1x, ys) * perturbAmp;
    }

    public void gradientPerturb3(float[] v3) {
        singleGradientPerturb3(seed, gradientPerturbAmp, frequency, v3);
    }

    public void gradientPerturbFractal3(float[] v3) {
        int seed = this.seed;
        float amp = gradientPerturbAmp * fractalBounding;
        float freq = frequency;

        singleGradientPerturb3(seed, amp, frequency, v3);

        for (int i = 1; i < octaves; i++) {
            freq *= lacunarity;
            amp *= gain;
            singleGradientPerturb3(++seed, amp, freq, v3);
        }
    }

    private void singleGradientPerturb3(int seed, float perturbAmp, float frequency, float[] v3) {
        float xf = v3[0] * frequency;
        float yf = v3[1] * frequency;
        float zf = v3[2] * frequency;

        int x0 = fastFloor(xf);
        int y0 = fastFloor(yf);
        int z0 = fastFloor(zf);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        float xs, ys, zs;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = xf - x0;
                ys = yf - y0;
                zs = zf - z0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(xf - x0);
                ys = hermiteInterpolator(yf - y0);
                zs = hermiteInterpolator(zf - z0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(xf - x0);
                ys = quinticInterpolator(yf - y0);
                zs = quinticInterpolator(zf - z0);
                break;
        }

        int vec0 = hash256(x0, y0, z0, seed) << 2;
        int vec1 = hash256(x1, y0, z0, seed) << 2;

        float lx0x = lerp(gradients[vec0], gradients[vec1], xs);
        float ly0x = lerp(gradients[vec0+1], gradients[vec1+1], xs);
        float lz0x = lerp(gradients[vec0+2], gradients[vec1+2], xs);

        vec0 = hash256(x0, y1, z0, seed) << 2;
        vec1 = hash256(x1, y1, z0, seed) << 2;

        float lx1x = lerp(gradients[vec0], gradients[vec1], xs);    
        float ly1x = lerp(gradients[vec0+1], gradients[vec1+1], xs);
        float lz1x = lerp(gradients[vec0+2], gradients[vec1+2], xs);

        float lx0y = lerp(lx0x, lx1x, ys);
        float ly0y = lerp(ly0x, ly1x, ys);
        float lz0y = lerp(lz0x, lz1x, ys);

        vec0 = hash256(x0, y0, z1, seed) << 2;
        vec1 = hash256(x1, y0, z1, seed) << 2;

        lx0x = lerp(gradients[vec0], gradients[vec1], xs);
        ly0x = lerp(gradients[vec0+1], gradients[vec1+1], xs);
        lz0x = lerp(gradients[vec0+2], gradients[vec1+2], xs);

        vec0 = hash256(x0, y1, z1, seed) << 2;
        vec1 = hash256(x1, y1, z1, seed) << 2;

        lx1x = lerp(gradients[vec0], gradients[vec1], xs);
        ly1x = lerp(gradients[vec0+1], gradients[vec1+1], xs);
        lz1x = lerp(gradients[vec0+2], gradients[vec1+2], xs);

        v3[0] += lerp(lx0y, lerp(lx0x, lx1x, ys), zs) * perturbAmp;
        v3[1] += lerp(ly0y, lerp(ly0x, ly1x, ys), zs) * perturbAmp;
        v3[2] += lerp(lz0y, lerp(lz0x, lz1x, ys), zs) * perturbAmp;
    }

    // Basic Java Code

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Noise noise = (Noise) o;

        if (seed != noise.seed) return false;
        if (Float.compare(noise.frequency, frequency) != 0) return false;
        if (interpolation != noise.interpolation) return false;
        if (noiseType != noise.noiseType) return false;
        if (octaves != noise.octaves) return false;
        if (Float.compare(noise.lacunarity, lacunarity) != 0) return false;
        if (Float.compare(noise.gain, gain) != 0) return false;
        if (fractalType != noise.fractalType) return false;
        if (cellularDistanceFunction != noise.cellularDistanceFunction) return false;
        if (cellularReturnType != noise.cellularReturnType) return false;
        if (Float.compare(noise.gradientPerturbAmp, gradientPerturbAmp) != 0) return false;
        if (Float.compare(noise.sharpness, sharpness) != 0) return false;
        return Float.compare(noise.mutation, mutation) == 0;
    }

    @Override
    public String toString() {
        return "Noise{" +
                "seed=" + seed +
                ", frequency=" + frequency +
                ", interpolation=" + interpolation +
                ", noiseType=" + noiseType +
                ", octaves=" + octaves +
                ", lacunarity=" + lacunarity +
                ", gain=" + gain +
                ", fractalType=" + fractalType +
                ", cellularDistanceFunction=" + cellularDistanceFunction +
                ", cellularReturnType=" + cellularReturnType +
                ", gradientPerturbAmp=" + gradientPerturbAmp +
                ", sharpness=" + sharpness +
                ", mutation=" + mutation +
                ", fractalSpiral=" + fractalSpiral +
                '}';
    }
    
    // Constants

    // These are used for Perlin noise.
    public static final float SCALE2 = 1.4142133f,  ADD2 = 1.0f/1.75f, MUL2 = 1.2535664f;
    public static final float SCALE3 = 1.1547003f,  ADD3 = 0.8f/1.75f, MUL3 = 1.2071217f;
    public static final float SCALE4 = 0.9999999f,  ADD4 = 0.6f/1.75f, MUL4 = 1.1588172f;
    public static final float SCALE5 = 0.89442706f, ADD5 = 0.4f/1.75f, MUL5 = 1.1084094f;
    public static final float SCALE6 = 0.81649643f, ADD6 = 0.2f/1.75f, MUL6 = 1.0555973f;

    // These are used for Simplex noise.
    public static final float F2 = 0.36602540378443864676372317075294f;
    public static final float G2 = 0.21132486540518711774542560974902f;
    public static final float H2 = G2 * 2.0f;
    public static final float LIMIT2 = 0.5f;

    public static final float F3 = (float)(1.0 / 3.0);
    public static final float G3 = (float)(1.0 / 6.0);
    public static final float LIMIT3 = 0.6f;

    public static final float F4 = (float)((Math.sqrt(5.0) - 1.0) * 0.25);
    public static final float G4 = (float)((5.0 - Math.sqrt(5.0)) * 0.05);
    public static final float LIMIT4 = 0.4675f;

    public static final float F5 = (float)((Math.sqrt(6.0) - 1.0) / 5.0);
    public static final float G5 = (float)((6.0 - Math.sqrt(6.0)) / 30.0);
    public static final float LIMIT5 = 0.67f;

    public static final float F6 = (float)((Math.sqrt(7.0) - 1.0) / 6.0);
    public static final float G6 = (float)(F6 / (1.0 + 6.0 * F6));
    public static final float LIMIT6 = 0.69f;

}
