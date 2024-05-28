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
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.digital.RoughMath;
import com.github.tommyettinger.function.FloatToFloatFunction;
import com.github.tommyettinger.random.*;
import com.github.yellowstonegames.grid.GradientVectors;
import com.github.yellowstonegames.grid.QuasiRandomTools;
import com.github.yellowstonegames.grid.RotationTools;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.PI;
import static com.github.tommyettinger.digital.TrigTools.SIN_TABLE_D;
import static com.github.tommyettinger.digital.TrigTools.SIN_TO_COS;
import static com.github.tommyettinger.digital.TrigTools.TABLE_MASK;
import static com.github.tommyettinger.digital.TrigTools.cosSmoother;
import static com.github.tommyettinger.digital.TrigTools.cosSmootherTurns;
import static com.github.tommyettinger.digital.TrigTools.sinSmoother;
import static com.github.tommyettinger.digital.TrigTools.sinSmootherTurns;
import static com.github.yellowstonegames.grid.GradientVectors.GRADIENTS_4D;
import static com.github.yellowstonegames.grid.QuasiRandomTools.GOLDEN_FLOATS;
import static com.github.yellowstonegames.grid.QuasiRandomTools.GOLDEN_LONGS;

/**
 * Adapted from SquidLib's MathVisualizer, but stripped down to only include sphere-related math.
 */
public class SphereVisualizer extends ApplicationAdapter {
    public static final int POINT_COUNT = 1 << 14;
    public static final float INVERSE_SPEED = 1E-11f;
    private float[][] points = new float[POINT_COUNT][3];
    private int mode = 27;
    private final int modes = 34;
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
    private int goldenRowA = 1, goldenRowB = 1, goldenColA = 0, goldenColB = 1;
    private final EnhancedRandom random = new AceRandom(seed);
//    private final EnhancedRandom random = new WhiskerRandom(seed);
//    private final EnhancedRandom random = new ScruffRandom(seed);
//    private final EnhancedRandom random = new MizuchiRandom(seed);
//    private final EnhancedRandom random = new RomuTrioRandom(seed);
//    private final EnhancedRandom random = new DistinctRandom(seed);
//    private final EnhancedRandom random = new RandomRandom(seed);

    private static final float[] POLE_3 = new float[]{1f, 0f, 0f};
    private static final double[] POLE_3_D = new double[]{1.0, 0., 0.};

    private static final float[] POLE_4 = new float[]{1f, 0f, 0f, 0f};
    private static final double[] POLE_4_D = new double[]{1.0, 0., 0., 0.};

    private static final float[] POLE_5 = new float[]{1f, 0f, 0f, 0f, 0f};
    private static final double[] POLE_5_D = new double[]{1.0, 0., 0., 0., 0.};

    private static final float[] POLE_6 = new float[]{1f, 0f, 0f, 0f, 0f, 0f};
    private static final double[] POLE_6_D = new double[]{1.0, 0., 0., 0., 0., 0.};

    private final float[] TMP_PT = new float[6];

    private static final float black = Color.BLACK.toFloatBits();
    private static final float blue = Color.BLUE.toFloatBits();
    private static final float cyan = Color.CYAN.toFloatBits();
    private static final float red = Color.RED.toFloatBits();
    private static final float smoke = Color.toFloatBits(0f, 0f, 0f, 0.25f);


    /**
     * <a href="https://github.com/SpongePowered/noise/blob/master/src/main/java/org/spongepowered/noise/Utils.java#L155-L230">From spongepowered/noise</a>,
     * for testing. This appears to be a Fibonacci Lattice. Every fourth item is 0.0, to allow easier access to a point
     * using bit-masking.
     */
    private static final double[] RANDOM_VECTORS = {
            -0.763874, -0.596439, -0.246489, 0.0, 0.396055, 0.904518, -0.158073, 0.0, -0.499004, -0.8665, -0.0131631, 0.0, 0.468724, -0.824756, 0.316346, 0.0,
            0.829598, 0.43195, 0.353816, 0.0, -0.454473, 0.629497, -0.630228, 0.0, -0.162349, -0.869962, -0.465628, 0.0, 0.932805, 0.253451, 0.256198, 0.0,
            -0.345419, 0.927299, -0.144227, 0.0, -0.715026, -0.293698, -0.634413, 0.0, -0.245997, 0.717467, -0.651711, 0.0, -0.967409, -0.250435, -0.037451, 0.0,
            0.901729, 0.397108, -0.170852, 0.0, 0.892657, -0.0720622, -0.444938, 0.0, 0.0260084, -0.0361701, 0.999007, 0.0, 0.949107, -0.19486, 0.247439, 0.0,
            0.471803, -0.807064, -0.355036, 0.0, 0.879737, 0.141845, 0.453809, 0.0, 0.570747, 0.696415, 0.435033, 0.0, -0.141751, -0.988233, -0.0574584, 0.0,
            -0.58219, -0.0303005, 0.812488, 0.0, -0.60922, 0.239482, -0.755975, 0.0, 0.299394, -0.197066, -0.933557, 0.0, -0.851615, -0.220702, -0.47544, 0.0,
            0.848886, 0.341829, -0.403169, 0.0, -0.156129, -0.687241, 0.709453, 0.0, -0.665651, 0.626724, 0.405124, 0.0, 0.595914, -0.674582, 0.43569, 0.0,
            0.171025, -0.509292, 0.843428, 0.0, 0.78605, 0.536414, -0.307222, 0.0, 0.18905, -0.791613, 0.581042, 0.0, -0.294916, 0.844994, 0.446105, 0.0,
            0.342031, -0.58736, -0.7335, 0.0, 0.57155, 0.7869, 0.232635, 0.0, 0.885026, -0.408223, 0.223791, 0.0, -0.789518, 0.571645, 0.223347, 0.0,
            0.774571, 0.31566, 0.548087, 0.0, -0.79695, -0.0433603, -0.602487, 0.0, -0.142425, -0.473249, -0.869339, 0.0, -0.0698838, 0.170442, 0.982886, 0.0,
            0.687815, -0.484748, 0.540306, 0.0, 0.543703, -0.534446, -0.647112, 0.0, 0.97186, 0.184391, -0.146588, 0.0, 0.707084, 0.485713, -0.513921, 0.0,
            0.942302, 0.331945, 0.043348, 0.0, 0.499084, 0.599922, 0.625307, 0.0, -0.289203, 0.211107, 0.9337, 0.0, 0.412433, -0.71667, -0.56239, 0.0,
            0.87721, -0.082816, 0.47291, 0.0, -0.420685, -0.214278, 0.881538, 0.0, 0.752558, -0.0391579, 0.657361, 0.0, 0.0765725, -0.996789, 0.0234082, 0.0,
            -0.544312, -0.309435, -0.779727, 0.0, -0.455358, -0.415572, 0.787368, 0.0, -0.874586, 0.483746, 0.0330131, 0.0, 0.245172, -0.0838623, 0.965846, 0.0,
            0.382293, -0.432813, 0.81641, 0.0, -0.287735, -0.905514, 0.311853, 0.0, -0.667704, 0.704955, -0.239186, 0.0, 0.717885, -0.464002, -0.518983, 0.0,
            0.976342, -0.214895, 0.0240053, 0.0, -0.0733096, -0.921136, 0.382276, 0.0, -0.986284, 0.151224, -0.0661379, 0.0, -0.899319, -0.429671, 0.0812908, 0.0,
            0.652102, -0.724625, 0.222893, 0.0, 0.203761, 0.458023, -0.865272, 0.0, -0.030396, 0.698724, -0.714745, 0.0, -0.460232, 0.839138, 0.289887, 0.0,
            -0.0898602, 0.837894, 0.538386, 0.0, -0.731595, 0.0793784, 0.677102, 0.0, -0.447236, -0.788397, 0.422386, 0.0, 0.186481, 0.645855, -0.740335, 0.0,
            -0.259006, 0.935463, 0.240467, 0.0, 0.445839, 0.819655, -0.359712, 0.0, 0.349962, 0.755022, -0.554499, 0.0, -0.997078, -0.0359577, 0.0673977, 0.0,
            -0.431163, -0.147516, -0.890133, 0.0, 0.299648, -0.63914, 0.708316, 0.0, 0.397043, 0.566526, -0.722084, 0.0, -0.502489, 0.438308, -0.745246, 0.0,
            0.0687235, 0.354097, 0.93268, 0.0, -0.0476651, -0.462597, 0.885286, 0.0, -0.221934, 0.900739, -0.373383, 0.0, -0.956107, -0.225676, 0.186893, 0.0,
            -0.187627, 0.391487, -0.900852, 0.0, -0.224209, -0.315405, 0.92209, 0.0, -0.730807, -0.537068, 0.421283, 0.0, -0.0353135, -0.816748, 0.575913, 0.0,
            -0.941391, 0.176991, -0.287153, 0.0, -0.154174, 0.390458, 0.90762, 0.0, -0.283847, 0.533842, 0.796519, 0.0, -0.482737, -0.850448, 0.209052, 0.0,
            -0.649175, 0.477748, 0.591886, 0.0, 0.885373, -0.405387, -0.227543, 0.0, -0.147261, 0.181623, -0.972279, 0.0, 0.0959236, -0.115847, -0.988624, 0.0,
            -0.89724, -0.191348, 0.397928, 0.0, 0.903553, -0.428461, -0.00350461, 0.0, 0.849072, -0.295807, -0.437693, 0.0, 0.65551, 0.741754, -0.141804, 0.0,
            0.61598, -0.178669, 0.767232, 0.0, 0.0112967, 0.932256, -0.361623, 0.0, -0.793031, 0.258012, 0.551845, 0.0, 0.421933, 0.454311, 0.784585, 0.0,
            -0.319993, 0.0401618, -0.946568, 0.0, -0.81571, 0.551307, -0.175151, 0.0, -0.377644, 0.00322313, 0.925945, 0.0, 0.129759, -0.666581, -0.734052, 0.0,
            0.601901, -0.654237, -0.457919, 0.0, -0.927463, -0.0343576, -0.372334, 0.0, -0.438663, -0.868301, -0.231578, 0.0, -0.648845, -0.749138, -0.133387, 0.0,
            0.507393, -0.588294, 0.629653, 0.0, 0.726958, 0.623665, 0.287358, 0.0, 0.411159, 0.367614, -0.834151, 0.0, 0.806333, 0.585117, -0.0864016, 0.0,
            0.263935, -0.880876, 0.392932, 0.0, 0.421546, -0.201336, 0.884174, 0.0, -0.683198, -0.569557, -0.456996, 0.0, -0.117116, -0.0406654, -0.992285, 0.0,
            -0.643679, -0.109196, -0.757465, 0.0, -0.561559, -0.62989, 0.536554, 0.0, 0.0628422, 0.104677, -0.992519, 0.0, 0.480759, -0.2867, -0.828658, 0.0,
            -0.228559, -0.228965, -0.946222, 0.0, -0.10194, -0.65706, -0.746914, 0.0, 0.0689193, -0.678236, 0.731605, 0.0, 0.401019, -0.754026, 0.52022, 0.0,
            -0.742141, 0.547083, -0.387203, 0.0, -0.00210603, -0.796417, -0.604745, 0.0, 0.296725, -0.409909, -0.862513, 0.0, -0.260932, -0.798201, 0.542945, 0.0,
            -0.641628, 0.742379, 0.192838, 0.0, -0.186009, -0.101514, 0.97729, 0.0, 0.106711, -0.962067, 0.251079, 0.0, -0.743499, 0.30988, -0.592607, 0.0,
            -0.795853, -0.605066, -0.0226607, 0.0, -0.828661, -0.419471, -0.370628, 0.0, 0.0847218, -0.489815, -0.8677, 0.0, -0.381405, 0.788019, -0.483276, 0.0,
            0.282042, -0.953394, 0.107205, 0.0, 0.530774, 0.847413, 0.0130696, 0.0, 0.0515397, 0.922524, 0.382484, 0.0, -0.631467, -0.709046, 0.313852, 0.0,
            0.688248, 0.517273, 0.508668, 0.0, 0.646689, -0.333782, -0.685845, 0.0, -0.932528, -0.247532, -0.262906, 0.0, 0.630609, 0.68757, -0.359973, 0.0,
            0.577805, -0.394189, 0.714673, 0.0, -0.887833, -0.437301, -0.14325, 0.0, 0.690982, 0.174003, 0.701617, 0.0, -0.866701, 0.0118182, 0.498689, 0.0,
            -0.482876, 0.727143, 0.487949, 0.0, -0.577567, 0.682593, -0.447752, 0.0, 0.373768, 0.0982991, 0.922299, 0.0, 0.170744, 0.964243, -0.202687, 0.0,
            0.993654, -0.035791, -0.106632, 0.0, 0.587065, 0.4143, -0.695493, 0.0, -0.396509, 0.26509, -0.878924, 0.0, -0.0866853, 0.83553, -0.542563, 0.0,
            0.923193, 0.133398, -0.360443, 0.0, 0.00379108, -0.258618, 0.965972, 0.0, 0.239144, 0.245154, -0.939526, 0.0, 0.758731, -0.555871, 0.33961, 0.0,
            0.295355, 0.309513, 0.903862, 0.0, 0.0531222, -0.91003, -0.411124, 0.0, 0.270452, 0.0229439, -0.96246, 0.0, 0.563634, 0.0324352, 0.825387, 0.0,
            0.156326, 0.147392, 0.976646, 0.0, -0.0410141, 0.981824, 0.185309, 0.0, -0.385562, -0.576343, -0.720535, 0.0, 0.388281, 0.904441, 0.176702, 0.0,
            0.945561, -0.192859, -0.262146, 0.0, 0.844504, 0.520193, 0.127325, 0.0, 0.0330893, 0.999121, -0.0257505, 0.0, -0.592616, -0.482475, -0.644999, 0.0,
            0.539471, 0.631024, -0.557476, 0.0, 0.655851, -0.027319, -0.754396, 0.0, 0.274465, 0.887659, 0.369772, 0.0, -0.123419, 0.975177, -0.183842, 0.0,
            -0.223429, 0.708045, 0.66989, 0.0, -0.908654, 0.196302, 0.368528, 0.0, -0.95759, -0.00863708, 0.288005, 0.0, 0.960535, 0.030592, 0.276472, 0.0,
            -0.413146, 0.907537, 0.0754161, 0.0, -0.847992, 0.350849, -0.397259, 0.0, 0.614736, 0.395841, 0.68221, 0.0, -0.503504, -0.666128, -0.550234, 0.0,
            -0.268833, -0.738524, -0.618314, 0.0, 0.792737, -0.60001, -0.107502, 0.0, -0.637582, 0.508144, -0.579032, 0.0, 0.750105, 0.282165, -0.598101, 0.0,
            -0.351199, -0.392294, -0.850155, 0.0, 0.250126, -0.960993, -0.118025, 0.0, -0.732341, 0.680909, -0.0063274, 0.0, -0.760674, -0.141009, 0.633634, 0.0,
            0.222823, -0.304012, 0.926243, 0.0, 0.209178, 0.505671, 0.836984, 0.0, 0.757914, -0.56629, -0.323857, 0.0, -0.782926, -0.339196, 0.52151, 0.0,
            -0.462952, 0.585565, 0.665424, 0.0, 0.61879, 0.194119, -0.761194, 0.0, 0.741388, -0.276743, 0.611357, 0.0, 0.707571, 0.702621, 0.0752872, 0.0,
            0.156562, 0.819977, 0.550569, 0.0, -0.793606, 0.440216, 0.42, 0.0, 0.234547, 0.885309, -0.401517, 0.0, 0.132598, 0.80115, -0.58359, 0.0,
            -0.377899, -0.639179, 0.669808, 0.0, -0.865993, -0.396465, 0.304748, 0.0, -0.624815, -0.44283, 0.643046, 0.0, -0.485705, 0.825614, -0.287146, 0.0,
            -0.971788, 0.175535, 0.157529, 0.0, -0.456027, 0.392629, 0.798675, 0.0, -0.0104443, 0.521623, -0.853112, 0.0, -0.660575, -0.74519, 0.091282, 0.0,
            -0.0157698, -0.307475, -0.951425, 0.0, -0.603467, -0.250192, 0.757121, 0.0, 0.506876, 0.25006, 0.824952, 0.0, 0.255404, 0.966794, 0.00884498, 0.0,
            0.466764, -0.874228, -0.133625, 0.0, 0.475077, -0.0682351, -0.877295, 0.0, -0.224967, -0.938972, -0.260233, 0.0, -0.377929, -0.814757, -0.439705, 0.0,
            -0.305847, 0.542333, -0.782517, 0.0, 0.26658, -0.902905, -0.337191, 0.0, 0.0275773, 0.322158, -0.946284, 0.0, 0.0185422, 0.716349, 0.697496, 0.0,
            -0.20483, 0.978416, 0.0273371, 0.0, -0.898276, 0.373969, 0.230752, 0.0, -0.00909378, 0.546594, 0.837349, 0.0, 0.6602, -0.751089, 0.000959236, 0.0,
            0.855301, -0.303056, 0.420259, 0.0, 0.797138, 0.0623013, -0.600574, 0.0, 0.48947, -0.866813, 0.0951509, 0.0, 0.251142, 0.674531, 0.694216, 0.0,
            -0.578422, -0.737373, -0.348867, 0.0, -0.254689, -0.514807, 0.818601, 0.0, 0.374972, 0.761612, 0.528529, 0.0, 0.640303, -0.734271, -0.225517, 0.0,
            -0.638076, 0.285527, 0.715075, 0.0, 0.772956, -0.15984, -0.613995, 0.0, 0.798217, -0.590628, 0.118356, 0.0, -0.986276, -0.0578337, -0.154644, 0.0,
            -0.312988, -0.94549, 0.0899272, 0.0, -0.497338, 0.178325, 0.849032, 0.0, -0.101136, -0.981014, 0.165477, 0.0, -0.521688, 0.0553434, -0.851339, 0.0,
            -0.786182, -0.583814, 0.202678, 0.0, -0.565191, 0.821858, -0.0714658, 0.0, 0.437895, 0.152598, -0.885981, 0.0, -0.92394, 0.353436, -0.14635, 0.0,
            0.212189, -0.815162, -0.538969, 0.0, -0.859262, 0.143405, -0.491024, 0.0, 0.991353, 0.112814, 0.0670273, 0.0, 0.0337884, -0.979891, -0.196654, 0.0
    };
    private float optimizeStrength = 1f;


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
     * Best seed: 0xEE36A34B8BEC3EFEL with best min dist 0.309696
     * In 6D:
     * Best seed: 0x19844719072412C4L with best min dist 0.366712
     * Best seed: 0x43588A64DE2C7C2BL with best min dist 0.379867
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
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("Cozette-standard.fnt"));
        font.setColor(Color.BLACK);
        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        camera = viewport.getCamera();
        renderer = new ImmediateModeRenderer20(0x80000, false, true, 0);
        Arrays.fill(amounts, 0);
        final int TRIALS = 100000;
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
                } else if(keycode == Input.Keys.F) {
                    float epsilonacci;
                    for (int e = 16; e <= 40; e++) {
                        epsilonacci = MathTools.barronSpline(MathTools.fract(e * 0x1p-4f), 0.7f, 0.5f) + (e >>> 4);
                        float bestA = MathTools.ROOT2 - 1f, bestB = 0.533751168755204288118041f, bestScore;
//                        float bestA = MathTools.ROOT2, bestB = 1.533751168755204288118041f, bestScore;
//                    float bestA = (float) (1.0/Math.sqrt(2.0)), bestB = 1f/1.533751168755204288118041f, bestScore;
                        superFibonacci4D(epsilonacci, GRADIENTS_4D_FIB, bestA, bestB);
                        System.out.printf("Super-Fibonacci spiral (epsilon %5.7f) with a %13.10f, b %13.10f has min dist %f \n", epsilonacci, bestA, bestB, Math.sqrt(bestScore = evaluateMinDistance2_4(GRADIENTS_4D_FIB, 256)));

                        long oldSeed = seed;
                        random.setSeed(seed);
                        for (int i = 0; i < GOLDEN_FLOATS.length; i++) {
                            float[] row = GOLDEN_FLOATS[i];
                            for (int j = 0; j < row.length; j++) {
                                float a = row[j];
//                                float a = row[j] + 1f;
//                            float[] rand = random.randomElement(QuasiRandomTools.GOLDEN_FLOATS);
//                            float b = rand[random.nextInt(rand.length)];
                                for (int k = 0; k < 1000; k++) {
                                    float b = random.nextExclusiveFloat();
//                                    float b = random.nextExclusiveFloat() + 1f;
                                    Arrays.fill(GRADIENTS_4D_FIB, 0f);
                                    superFibonacci4D(epsilonacci, GRADIENTS_4D_FIB, a, b);
                                    float score = evaluateMinDistance2_4(GRADIENTS_4D_FIB, 256);
                                    if (score > bestScore) {
                                        bestA = a;
                                        bestB = b;
                                        bestScore = score;
                                    }
                                }
                            }
                        }
                        //Super-Fibonacci spiral (epsilon 1.0000000) with a  0.5866990089, b  0.3876041770 has min dist 0.352996
                        //Super-Fibonacci spiral (epsilon 1.2941177) with a  0.5866990089, b  0.6119842529 has min dist 0.355762
                        //Super-Fibonacci spiral (epsilon 1.0000000) with a  0.9469839931, b  0.5291057229 has min dist 0.365039
                        //Super-Fibonacci spiral (epsilon 0.9899999) with a  1.9469840527, b  1.0291167498 has min dist 0.365202
                        //Super-Fibonacci spiral (epsilon 1.7692308) with a  0.5866990089, b  0.6119842529 has min dist 0.369163
                        //Super-Fibonacci spiral (epsilon 2.5000000) with a  0.6923295856, b  0.7171460986 has min dist 0.389756

                        // POLAR OPPOSITE MODE
                        //Super-Fibonacci spiral (epsilon 1.0000000) with a  0.9043024778, b  0.4258859158 has min dist 0.355767
                        //Super-Fibonacci spiral (epsilon 1.0847458) with a  0.9319309592, b  0.9044592977 has min dist 0.356069
                        //Super-Fibonacci spiral (epsilon 1.1612903) with a  0.9319309592, b  0.9044592977 has min dist 0.356929
                        //Super-Fibonacci spiral (epsilon 1.2307693) with a  0.9319309592, b  0.9044592977 has min dist 0.357706
                        //Super-Fibonacci spiral (epsilon 1.4054054) with a  0.9319309592, b  0.9044592977 has min dist 0.359645
                        //Super-Fibonacci spiral (epsilon 1.4545455) with a  0.9319309592, b  0.9044592977 has min dist 0.360188
                        //Super-Fibonacci spiral (epsilon 1.7058823) with a  0.8444285393, b  0.6815647483 has min dist 0.363134
                        //Super-Fibonacci spiral (epsilon 1.9152542) with a  0.6556250453, b  0.8184294105 has min dist 0.364910
                        //Super-Fibonacci spiral (epsilon 2.0847459) with a  0.6556250453, b  0.8184294105 has min dist 0.366613
                        //Super-Fibonacci spiral (epsilon 2.4054055) with a  0.6556078792, b  0.3184083104 has min dist 0.369022
                        //Super-Fibonacci spiral (epsilon 2.4545455) with a  0.6556078792, b  0.3184083104 has min dist 0.369142
                        System.out.printf("Super-Fibonacci spiral (epsilon %5.7f) with a %13.10f, b %13.10f has min dist %f \n", epsilonacci, bestA, bestB, Math.sqrt(bestScore));
                        seed = oldSeed;
                    }
                    superFibonacci4D(0.5f, GRADIENTS_4D_FIB, GOLDEN_FLOATS[1][0], GOLDEN_FLOATS[1][1]);
                    System.out.printf("Super-Fibonacci spiral (epsilon %5.7f) with a %13.10f, b %13.10f has min dist %f \n", 0.5f, GOLDEN_FLOATS[1][0], GOLDEN_FLOATS[1][1], Math.sqrt(evaluateMinDistance2_4(GRADIENTS_4D_FIB, 256)));
                } else if(keycode == Input.Keys.NUM_4) {
                    startTime = TimeUtils.millis();
                    long bestSeed = seed;
                    double bestMinDist = -Double.MAX_VALUE;
                    float bestVerify = -100f;
                    RotationTools.Rotator rotator = new RotationTools.Rotator(4, random);
                    for (int i = 0; i < TRIALS; i++) {
                        random.setState(seed, seed + 0x9E3779B97F4A7C15L, seed - 0x9E3779B97F4A7C15L, ~seed + 0x9E3779B97F4A7C15L, ~seed - 0x9E3779B97F4A7C15L);
                        Arrays.fill(GRADIENTS_4D_TEMP, 0f);
                        roll4D(rotator, GRADIENTS_4D_TEMP);
                        float dist = evaluateMinDistance2_4(GRADIENTS_4D_TEMP, 256);
                        if(bestMinDist < (bestMinDist = Math.max(bestMinDist, dist))){
                            bestSeed = seed;
                            bestVerify = GRADIENTS_4D_TEMP[44];
                        }
                        seed += 0xDB4F0B9175AE2165L;// 0x9E3779B97F4A7C15L;
                    }
                    System.out.printf("Best seed: 0x%016XL with best min dist %f\n", bestSeed, Math.sqrt(bestMinDist));
                    System.out.println("Processing " + TRIALS + " spheres took " + TimeUtils.timeSinceMillis(startTime) * 1E-3 + " seconds.");
                    random.setState(bestSeed, bestSeed + 0x9E3779B97F4A7C15L, bestSeed - 0x9E3779B97F4A7C15L, ~bestSeed + 0x9E3779B97F4A7C15L, ~bestSeed - 0x9E3779B97F4A7C15L);
                    Arrays.fill(GRADIENTS_4D_ACE, 0f);
                    roll4D(rotator, GRADIENTS_4D_ACE);
                    if(bestVerify != GRADIENTS_4D_ACE[44])
                    {
                        System.out.printf("INCORRECT RECREATION FROM SEED: %13.10f == %13.10f\n", bestVerify, GRADIENTS_4D_ACE[44]);
                        return false;
                    }
//                    shuffleBlocks(random, GRADIENTS_4D_ACE, 4); // I don't think this is needed for random points
                    printGradients4D(GRADIENTS_4D_ACE);
                } else if(keycode == Input.Keys.NUM_5) {
                    startTime = TimeUtils.millis();
                    long bestSeed = seed;
                    double bestMinDist = -Double.MAX_VALUE;
                    float bestVerify = -100f;
                    for (int i = 0; i < TRIALS; i++) {
                        random.setState(seed, seed + 0x9E3779B97F4A7C15L, seed - 0x9E3779B97F4A7C15L, ~seed + 0x9E3779B97F4A7C15L, ~seed - 0x9E3779B97F4A7C15L);
                        Arrays.fill(GRADIENTS_5D_TEMP, 0f);
                        roll5D(random, GRADIENTS_5D_TEMP);
                        float dist = evaluateMinDistance2_5(GRADIENTS_5D_TEMP);
                        if(bestMinDist < (bestMinDist = Math.max(bestMinDist, dist))){
                            bestSeed = seed;
                            bestVerify = GRADIENTS_5D_TEMP[44];
                        }
                        seed += 0xDB4F0B9175AE2165L;// 0x9E3779B97F4A7C15L;
                    }
                    System.out.printf("Best seed: 0x%016XL with best min dist %f\n", bestSeed, Math.sqrt(bestMinDist));
                    System.out.println("Processing " + TRIALS + " spheres took " + TimeUtils.timeSinceMillis(startTime) * 1E-3 + " seconds.");
                    random.setState(bestSeed, bestSeed + 0x9E3779B97F4A7C15L, bestSeed - 0x9E3779B97F4A7C15L, ~bestSeed + 0x9E3779B97F4A7C15L, ~bestSeed - 0x9E3779B97F4A7C15L);
                    Arrays.fill(GRADIENTS_5D_ACE, 0f);
                    roll5D(random, GRADIENTS_5D_ACE);
                    if(bestVerify != GRADIENTS_5D_ACE[44])
                    {
                        System.out.printf("INCORRECT RECREATION FROM SEED: %13.10f == %13.10f\n", bestVerify, GRADIENTS_5D_ACE[44]);
                        return false;
                    }
                    System.out.println("public static final float[] GRADIENTS_5D = {");
                    for (int i = 0; i < GRADIENTS_5D_ACE.length; i += 8) {
                        System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f, 0.0f, 0.0f,\n",
                                GRADIENTS_5D_ACE[i], GRADIENTS_5D_ACE[i+1], GRADIENTS_5D_ACE[i+2], GRADIENTS_5D_ACE[i+3], GRADIENTS_5D_ACE[i+4]);
                    }
                    System.out.println("};");

                    random.setSeed(seed);
                } else if(keycode == Input.Keys.NUM_6) {

                    // I don't remember why I had this in to debug...
                    //                            random.stringDeserialize("AceR`-B57A320CFBEF3~-57080C1D00C9E1A0~-7A9B48DF183E4A73~76CC474918D335C1~3DDF1B8EC95CBC49`");

                    if (UIUtils.shift()) {
                        startTime = TimeUtils.millis();
                        long bestSeed = seed;
                        double bestMinDist = -Double.MAX_VALUE;
                        float bestVerify = -100f;
                        RotationTools.Rotator rotator = new RotationTools.Rotator(6, random);
                        for (int i = 0; i < TRIALS; i++) {
                            random.setState(seed, seed + 0x9E3779B97F4A7C15L, seed - 0x9E3779B97F4A7C15L, ~seed + 0x9E3779B97F4A7C15L, ~seed - 0x9E3779B97F4A7C15L);
                            Arrays.fill(GRADIENTS_6D_TEMP, 0f);
                            roll6D(rotator, GRADIENTS_6D_TEMP);
                            float dist = evaluateMinDistance2_6(GRADIENTS_6D_TEMP);
                            if (bestMinDist < (bestMinDist = Math.max(bestMinDist, dist))) {
                                bestSeed = seed;
                                bestVerify = GRADIENTS_6D_TEMP[44];
                            }
                            seed += 0xDB4F0B9175AE2165L;// 0x9E3779B97F4A7C15L;
                        }
                        System.out.printf("Best seed: 0x%016XL with best min dist %f\n", bestSeed, Math.sqrt(bestMinDist));
                        System.out.println("Processing " + TRIALS + " spheres took " + TimeUtils.timeSinceMillis(startTime) * 1E-3 + " seconds.");
                        random.setState(bestSeed, bestSeed + 0x9E3779B97F4A7C15L, bestSeed - 0x9E3779B97F4A7C15L, ~bestSeed + 0x9E3779B97F4A7C15L, ~bestSeed - 0x9E3779B97F4A7C15L);
                        Arrays.fill(GRADIENTS_6D_ACE, 0f);
                        roll6D(rotator, GRADIENTS_6D_ACE);
                        if (bestVerify != GRADIENTS_6D_ACE[44]) {
                            System.out.printf("INCORRECT RECREATION FROM SEED: %13.10f == %13.10f\n", bestVerify, GRADIENTS_6D_ACE[44]);
                            return false;
                        }
//                        shuffleBlocks(random, GRADIENTS_6D_ACE, 8);
                        System.out.println("public static final float[] GRADIENTS_6D = {");
                        for (int i = 0; i < GRADIENTS_6D_ACE.length; i += 8) {
                            System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f, 0.0f,\n",
                                    GRADIENTS_6D_ACE[i], GRADIENTS_6D_ACE[i + 1], GRADIENTS_6D_ACE[i + 2], GRADIENTS_6D_ACE[i + 3], GRADIENTS_6D_ACE[i + 4], GRADIENTS_6D_ACE[i + 5]);
                        }
                        System.out.println("};");
                    } else {
                        startTime = TimeUtils.millis();
                        long bestSeed = seed;
                        double bestMinDist = -Double.MAX_VALUE;
                        float bestVerify = -100f;
                        for (int i = 0; i < TRIALS; i++) {
                            random.setState(seed, seed + 0x9E3779B97F4A7C15L, seed - 0x9E3779B97F4A7C15L, ~seed + 0x9E3779B97F4A7C15L, ~seed - 0x9E3779B97F4A7C15L);
                            Arrays.fill(GRADIENTS_6D_TEMP, 0f);
                            roll6D(random, GRADIENTS_6D_TEMP);
                            float dist = evaluateMinDistance2_6(GRADIENTS_6D_TEMP);
                            if (bestMinDist < (bestMinDist = Math.max(bestMinDist, dist))) {
                                bestSeed = seed;
                                bestVerify = GRADIENTS_6D_TEMP[44];
                            }
                            seed += 0xDB4F0B9175AE2165L;// 0x9E3779B97F4A7C15L;
                        }
                        System.out.printf("Best seed: 0x%016XL with best min dist %f\n", bestSeed, Math.sqrt(bestMinDist));
                        System.out.println("Processing took " + TimeUtils.timeSinceMillis(startTime) * 1E-3 + " seconds.");
                        random.setState(bestSeed, bestSeed + 0x9E3779B97F4A7C15L, bestSeed - 0x9E3779B97F4A7C15L, ~bestSeed + 0x9E3779B97F4A7C15L, ~bestSeed - 0x9E3779B97F4A7C15L);
                        Arrays.fill(GRADIENTS_6D_ACE, 0f);
                        roll6D(random, GRADIENTS_6D_ACE);
                        if (bestVerify != GRADIENTS_6D_ACE[44]) {
                            System.out.printf("INCORRECT RECREATION FROM SEED: %13.10f == %13.10f\n", bestVerify, GRADIENTS_6D_ACE[44]);
                            return false;
                        }
//                        shuffleBlocks(random, GRADIENTS_6D_ACE, 8);
                        System.out.println("public static final float[] GRADIENTS_6D = {");
                        for (int i = 0; i < GRADIENTS_6D_ACE.length; i += 8) {
                            System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f, 0.0f,\n",
                                    GRADIENTS_6D_ACE[i], GRADIENTS_6D_ACE[i + 1], GRADIENTS_6D_ACE[i + 2], GRADIENTS_6D_ACE[i + 3], GRADIENTS_6D_ACE[i + 4], GRADIENTS_6D_ACE[i + 5]);
                        }
                        System.out.println("};");
                    }
                    random.setSeed(seed);
                } else if(keycode == Input.Keys.LEFT){
                    if(UIUtils.shift())
                        goldenColA--;
                    else
                        goldenColB--;
                    superFibonacci4D(0.5f, GRADIENTS_4D_FIB, GOLDEN_FLOATS
                                    [goldenRowA = Math.max(goldenRowA, 1) % GOLDEN_FLOATS.length]
                                    [goldenColA = Math.max(goldenColA, 0) % GOLDEN_FLOATS[goldenRowA].length],
                            GOLDEN_FLOATS
                                    [goldenRowB = Math.max(goldenRowB, 1) % GOLDEN_FLOATS.length]
                                    [goldenColB = Math.max(goldenColB, 0) % GOLDEN_FLOATS[goldenRowB].length]);
                    System.out.printf("float a = GOLDEN_FLOATS[%d][%d], b = GOLDEN_FLOATS[%d][%d];\n", goldenRowA, goldenColA, goldenRowB, goldenColB);
                } else if(keycode == Input.Keys.RIGHT){
                    if(UIUtils.shift())
                        goldenColA++;
                    else
                        goldenColB++;
                    superFibonacci4D(0.5f, GRADIENTS_4D_FIB, GOLDEN_FLOATS
                                    [goldenRowA = Math.max(goldenRowA, 1) % GOLDEN_FLOATS.length]
                                    [goldenColA = Math.max(goldenColA, 0) % GOLDEN_FLOATS[goldenRowA].length],
                            GOLDEN_FLOATS
                                    [goldenRowB = Math.max(goldenRowB, 1) % GOLDEN_FLOATS.length]
                                    [goldenColB = Math.max(goldenColB, 0) % GOLDEN_FLOATS[goldenRowB].length]);
                    System.out.printf("float a = GOLDEN_FLOATS[%d][%d], b = GOLDEN_FLOATS[%d][%d];\n", goldenRowA, goldenColA, goldenRowB, goldenColB);
                } else if(keycode == Input.Keys.UP){
                    if(UIUtils.shift())
                        goldenRowA--;
                    else
                        goldenRowB--;
                    superFibonacci4D(0.5f, GRADIENTS_4D_FIB, GOLDEN_FLOATS
                                    [goldenRowA = Math.max(goldenRowA, 1) % GOLDEN_FLOATS.length]
                                    [goldenColA = Math.max(goldenColA, 0) % GOLDEN_FLOATS[goldenRowA].length],
                            GOLDEN_FLOATS
                                    [goldenRowB = Math.max(goldenRowB, 1) % GOLDEN_FLOATS.length]
                                    [goldenColB = Math.max(goldenColB, 0) % GOLDEN_FLOATS[goldenRowB].length]);
                    System.out.printf("float a = GOLDEN_FLOATS[%d][%d], b = GOLDEN_FLOATS[%d][%d];\n", goldenRowA, goldenColA, goldenRowB, goldenColB);
                } else if(keycode == Input.Keys.DOWN){
                    if(UIUtils.shift())
                        goldenRowA++;
                    else
                        goldenRowB++;
                    superFibonacci4D(0.5f, GRADIENTS_4D_FIB, GOLDEN_FLOATS
                                    [goldenRowA = Math.max(goldenRowA, 1) % GOLDEN_FLOATS.length]
                                    [goldenColA = Math.max(goldenColA, 0) % GOLDEN_FLOATS[goldenRowA].length],
                            GOLDEN_FLOATS
                                    [goldenRowB = Math.max(goldenRowB, 1) % GOLDEN_FLOATS.length]
                                    [goldenColB = Math.max(goldenColB, 0) % GOLDEN_FLOATS[goldenRowB].length]);
                    System.out.printf("float a = GOLDEN_FLOATS[%d][%d], b = GOLDEN_FLOATS[%d][%d];\n", goldenRowA, goldenColA, goldenRowB, goldenColB);
                }else if(keycode == Input.Keys.G){
                    // show Gradient Vectors
                    shuffleBlocks(new AceRandom(12345), GRADIENTS_4D_CURRENT, 4);
                    printGradients4D(GRADIENTS_4D_CURRENT);
                } else if (keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE)
                    Gdx.app.exit();

                return false;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    private void printGradients4D(final float[] chosen) {
        printGradients(chosen, 4, 4);
    }
    private void printGradients5D(final float[] chosen) {
        printGradients(chosen, 5, 8);
    }
    private void printGradients6D(final float[] chosen) {
        printGradients(chosen, 6, 8);
    }
    private void printGradients(final float[] chosen, final int dim, final int blockSize) {
        System.out.println("public static final float[] GRADIENTS_4D = {");
        for (int i = 0; i < chosen.length; i += blockSize) {
            System.out.print("   ");
            for (int c = 0; c < dim; c++) {
                System.out.printf(" %0+13.10ff,", chosen[i+c]);
            }
            System.out.println();
//            System.out.print("   ");
//            for (int c = 0; c < dim; c++) {
//                System.out.printf(" %0+13.10ff,", -chosen[i+c]);
//            }
//            System.out.println();
//            i += blockSize;
        }
        System.out.println("};");
    }

    /*
Best seed: 0xC850325B2AEFA61AL with best min dist 0.335129
Processing took 72.523 seconds.
public static final float[] GRADIENTS_6D = {
    -0.4102923274f, +0.0273994915f, +0.3622766435f, -0.4579788148f, +0.5484226346f, +0.4349174201f, 0.0f, 0.0f,
    +0.4102923274f, -0.0273994915f, -0.3622766435f, +0.4579788148f, -0.5484226346f, -0.4349174201f, 0.0f, 0.0f,
    -0.6331945062f, -0.2069558203f, -0.1530175954f, +0.1365562081f, +0.6509645581f, +0.3006943464f, 0.0f, 0.0f,
    +0.6331945062f, +0.2069558203f, +0.1530175954f, -0.1365562081f, -0.6509645581f, -0.3006943464f, 0.0f, 0.0f,
    -0.4208920002f, +0.4555135071f, -0.0730087459f, -0.2191289067f, -0.5559716225f, -0.5028961301f, 0.0f, 0.0f,
    +0.4208920002f, -0.4555135071f, +0.0730087459f, +0.2191289067f, +0.5559716225f, +0.5028961301f, 0.0f, 0.0f,
    +0.1098673344f, +0.3513172269f, +0.2027362138f, -0.0921144187f, -0.8546298742f, +0.2907329798f, 0.0f, 0.0f,
    -0.1098673344f, -0.3513172269f, -0.2027362138f, +0.0921144187f, +0.8546298742f, -0.2907329798f, 0.0f, 0.0f,
    -0.0308368802f, -0.7946988940f, -0.0512897000f, +0.5773004889f, -0.0228009522f, +0.1762846708f, 0.0f, 0.0f,
    +0.0308368802f, +0.7946988940f, +0.0512897000f, -0.5773004889f, +0.0228009522f, -0.1762846708f, 0.0f, 0.0f,
    -0.5510950089f, -0.2037561089f, -0.1507038772f, +0.4635705650f, +0.0297193006f, +0.6452016830f, 0.0f, 0.0f,
    +0.5510950089f, +0.2037561089f, +0.1507038772f, -0.4635705650f, -0.0297193006f, -0.6452016830f, 0.0f, 0.0f,
    -0.1132929921f, -0.0941029638f, -0.4351856411f, -0.1432463378f, -0.1829835773f, +0.8572748899f, 0.0f, 0.0f,
    +0.1132929921f, +0.0941029638f, +0.4351856411f, +0.1432463378f, +0.1829835773f, -0.8572748899f, 0.0f, 0.0f,
    +0.4488697052f, -0.4586375952f, -0.1483388841f, -0.3080208302f, -0.4370904863f, -0.5293749571f, 0.0f, 0.0f,
    -0.4488697052f, +0.4586375952f, +0.1483388841f, +0.3080208302f, +0.4370904863f, +0.5293749571f, 0.0f, 0.0f,
    +0.0574053526f, -0.0629927367f, +0.1664230078f, +0.5296119452f, -0.3503653407f, +0.7495300174f, 0.0f, 0.0f,
    -0.0574053526f, +0.0629927367f, -0.1664230078f, -0.5296119452f, +0.3503653407f, -0.7495300174f, 0.0f, 0.0f,
    -0.2494162917f, -0.3774800599f, -0.7099250555f, -0.0624758974f, -0.5358819366f, -0.0153068677f, 0.0f, 0.0f,
    +0.2494162917f, +0.3774800599f, +0.7099250555f, +0.0624758974f, +0.5358819366f, +0.0153068677f, 0.0f, 0.0f,
    +0.0713946819f, +0.5220707655f, -0.3687057793f, +0.3385064602f, +0.1528570503f, +0.6696634293f, 0.0f, 0.0f,
    -0.0713946819f, -0.5220707655f, +0.3687057793f, -0.3385064602f, -0.1528570503f, -0.6696634293f, 0.0f, 0.0f,
    +0.3751730919f, -0.3160345256f, +0.1478763521f, +0.0856548846f, -0.5217101574f, -0.6767430902f, 0.0f, 0.0f,
    -0.3751730919f, +0.3160345256f, -0.1478763521f, -0.0856548846f, +0.5217101574f, +0.6767430902f, 0.0f, 0.0f,
    -0.1814820170f, +0.3865320683f, +0.0421791673f, -0.4413846433f, -0.5046374798f, -0.6053084731f, 0.0f, 0.0f,
    +0.1814820170f, -0.3865320683f, -0.0421791673f, +0.4413846433f, +0.5046374798f, +0.6053084731f, 0.0f, 0.0f,
    +0.6560095549f, -0.1057339907f, +0.4287990928f, -0.2285138965f, +0.5209618807f, +0.2257940769f, 0.0f, 0.0f,
    -0.6560095549f, +0.1057339907f, -0.4287990928f, +0.2285138965f, -0.5209618807f, -0.2257940769f, 0.0f, 0.0f,
    -0.7355676293f, -0.0437396616f, -0.5281906128f, -0.1680916250f, +0.3456485868f, -0.1741091460f, 0.0f, 0.0f,
    +0.7355676293f, +0.0437396616f, +0.5281906128f, +0.1680916250f, -0.3456485868f, +0.1741091460f, 0.0f, 0.0f,
    -0.1068283319f, +0.6353205442f, -0.6400319934f, -0.2082310319f, -0.3611143827f, -0.0393848866f, 0.0f, 0.0f,
    +0.1068283319f, -0.6353205442f, +0.6400319934f, +0.2082310319f, +0.3611143827f, +0.0393848866f, 0.0f, 0.0f,
    -0.5210713148f, -0.5286400318f, +0.3567543328f, +0.4430956841f, +0.3527485132f, +0.0313904732f, 0.0f, 0.0f,
    +0.5210713148f, +0.5286400318f, -0.3567543328f, -0.4430956841f, -0.3527485132f, -0.0313904732f, 0.0f, 0.0f,
    -0.2621524334f, +0.7827507854f, +0.3127266169f, -0.0235683322f, +0.2010166049f, +0.4240472019f, 0.0f, 0.0f,
    +0.2621524334f, -0.7827507854f, -0.3127266169f, +0.0235683322f, -0.2010166049f, -0.4240472019f, 0.0f, 0.0f,
    -0.3612365723f, +0.6697418690f, +0.4202670455f, +0.3403534889f, -0.1285147369f, -0.3346236944f, 0.0f, 0.0f,
    +0.3612365723f, -0.6697418690f, -0.4202670455f, -0.3403534889f, +0.1285147369f, +0.3346236944f, 0.0f, 0.0f,
    -0.3815435171f, -0.0514329597f, -0.5020418763f, -0.5328508019f, +0.1157996207f, -0.5499035120f, 0.0f, 0.0f,
    +0.3815435171f, +0.0514329597f, +0.5020418763f, +0.5328508019f, -0.1157996207f, +0.5499035120f, 0.0f, 0.0f,
    -0.2437675595f, -0.3742059469f, -0.0257712305f, -0.4499314427f, +0.7685063481f, -0.0827194303f, 0.0f, 0.0f,
    +0.2437675595f, +0.3742059469f, +0.0257712305f, +0.4499314427f, -0.7685063481f, +0.0827194303f, 0.0f, 0.0f,
    +0.6465499401f, -0.0972051099f, +0.4507665932f, +0.1494120210f, +0.3139263391f, +0.4984569848f, 0.0f, 0.0f,
    -0.6465499401f, +0.0972051099f, -0.4507665932f, -0.1494120210f, -0.3139263391f, -0.4984569848f, 0.0f, 0.0f,
    +0.1433223486f, +0.2819157243f, -0.6248235703f, -0.1910795867f, +0.5105260015f, -0.4609008431f, 0.0f, 0.0f,
    -0.1433223486f, -0.2819157243f, +0.6248235703f, +0.1910795867f, -0.5105260015f, +0.4609008431f, 0.0f, 0.0f,
    +0.3310434818f, -0.5044662356f, -0.1112213060f, +0.2061044127f, +0.5647008419f, -0.5120426416f, 0.0f, 0.0f,
    -0.3310434818f, +0.5044662356f, +0.1112213060f, -0.2061044127f, -0.5647008419f, +0.5120426416f, 0.0f, 0.0f,
    +0.0267953873f, -0.5906071663f, +0.7647922039f, +0.1793488264f, -0.1738400906f, -0.0563179851f, 0.0f, 0.0f,
    -0.0267953873f, +0.5906071663f, -0.7647922039f, -0.1793488264f, +0.1738400906f, +0.0563179851f, 0.0f, 0.0f,
    +0.3272268772f, -0.2197315395f, +0.4899304211f, +0.6836864948f, -0.1572189927f, +0.3353562951f, 0.0f, 0.0f,
    -0.3272268772f, +0.2197315395f, -0.4899304211f, -0.6836864948f, +0.1572189927f, -0.3353562951f, 0.0f, 0.0f,
    +0.1752485037f, -0.2555387318f, -0.4111272693f, -0.3136070371f, +0.7978780866f, -0.0018376149f, 0.0f, 0.0f,
    -0.1752485037f, +0.2555387318f, +0.4111272693f, +0.3136070371f, -0.7978780866f, +0.0018376149f, 0.0f, 0.0f,
    +0.0549198389f, +0.4444565773f, +0.4232282341f, +0.2507682443f, -0.4505729377f, -0.5953308344f, 0.0f, 0.0f,
    -0.0549198389f, -0.4444565773f, -0.4232282341f, -0.2507682443f, +0.4505729377f, +0.5953308344f, 0.0f, 0.0f,
    -0.2947680950f, -0.5889636874f, +0.1774486303f, -0.2838816643f, -0.5162461400f, -0.4331816435f, 0.0f, 0.0f,
    +0.2947680950f, +0.5889636874f, -0.1774486303f, +0.2838816643f, +0.5162461400f, +0.4331816435f, 0.0f, 0.0f,
    +0.4026685953f, +0.3230397105f, -0.6855905056f, +0.3005967736f, +0.0639450401f, +0.4111223817f, 0.0f, 0.0f,
    -0.4026685953f, -0.3230397105f, +0.6855905056f, -0.3005967736f, -0.0639450401f, -0.4111223817f, 0.0f, 0.0f,
    +0.3663702011f, +0.3715296388f, -0.5978475809f, -0.3705040216f, +0.1842313856f, -0.4462089241f, 0.0f, 0.0f,
    -0.3663702011f, -0.3715296388f, +0.5978475809f, +0.3705040216f, -0.1842313856f, +0.4462089241f, 0.0f, 0.0f,
    -0.4303388596f, +0.2252877355f, -0.3997274637f, +0.6341809630f, +0.0027421601f, -0.4495316148f, 0.0f, 0.0f,
    +0.4303388596f, -0.2252877355f, +0.3997274637f, -0.6341809630f, -0.0027421601f, +0.4495316148f, 0.0f, 0.0f,
    -0.5852103233f, -0.4514018595f, -0.6062161326f, +0.0632691085f, -0.1914951205f, +0.2135272175f, 0.0f, 0.0f,
    +0.5852103233f, +0.4514018595f, +0.6062161326f, -0.0632691085f, +0.1914951205f, -0.2135272175f, 0.0f, 0.0f,
    -0.3208606243f, -0.1184351370f, -0.2227160931f, -0.7757527232f, +0.0729968250f, -0.4757082462f, 0.0f, 0.0f,
    +0.3208606243f, +0.1184351370f, +0.2227160931f, +0.7757527232f, -0.0729968250f, +0.4757082462f, 0.0f, 0.0f,
    +0.0886416435f, +0.3447810709f, +0.3442713916f, -0.1652077436f, -0.8527719378f, -0.0152234044f, 0.0f, 0.0f,
    -0.0886416435f, -0.3447810709f, -0.3442713916f, +0.1652077436f, +0.8527719378f, +0.0152234044f, 0.0f, 0.0f,
    -0.6935311556f, +0.2337233722f, -0.0153739937f, -0.2704828680f, -0.5485715270f, -0.3000999391f, 0.0f, 0.0f,
    +0.6935311556f, -0.2337233722f, +0.0153739937f, +0.2704828680f, +0.5485715270f, +0.3000999391f, 0.0f, 0.0f,
    -0.3011130095f, -0.0100218952f, +0.8061234355f, -0.3947089314f, -0.0001740009f, +0.3218689859f, 0.0f, 0.0f,
    +0.3011130095f, +0.0100218952f, -0.8061234355f, +0.3947089314f, +0.0001740009f, -0.3218689859f, 0.0f, 0.0f,
    -0.1438477039f, -0.0430872589f, +0.6218242049f, +0.0842250511f, +0.7619541883f, +0.0558407009f, 0.0f, 0.0f,
    +0.1438477039f, +0.0430872589f, -0.6218242049f, -0.0842250511f, -0.7619541883f, -0.0558407009f, 0.0f, 0.0f,
    +0.5918905735f, +0.6846805811f, +0.2444619238f, -0.0012684241f, +0.0003843755f, +0.3480149508f, 0.0f, 0.0f,
    -0.5918905735f, -0.6846805811f, -0.2444619238f, +0.0012684241f, -0.0003843755f, -0.3480149508f, 0.0f, 0.0f,
    -0.2457658648f, +0.0859098956f, -0.0289754011f, +0.3279665709f, +0.1680482328f, +0.8919508457f, 0.0f, 0.0f,
    +0.2457658648f, -0.0859098956f, +0.0289754011f, -0.3279665709f, -0.1680482328f, -0.8919508457f, 0.0f, 0.0f,
    +0.0883234739f, -0.0558291450f, -0.5916984677f, -0.6990013123f, -0.3849540651f, -0.0467216074f, 0.0f, 0.0f,
    -0.0883234739f, +0.0558291450f, +0.5916984677f, +0.6990013123f, +0.3849540651f, +0.0467216074f, 0.0f, 0.0f,
    -0.1616445184f, +0.0830981880f, -0.0279421657f, -0.5604254007f, -0.1720002443f, -0.7890019417f, 0.0f, 0.0f,
    +0.1616445184f, -0.0830981880f, +0.0279421657f, +0.5604254007f, +0.1720002443f, +0.7890019417f, 0.0f, 0.0f,
    +0.3784198761f, +0.3658648133f, -0.4094402790f, +0.3526407480f, +0.4318647981f, +0.4944059551f, 0.0f, 0.0f,
    -0.3784198761f, -0.3658648133f, +0.4094402790f, -0.3526407480f, -0.4318647981f, -0.4944059551f, 0.0f, 0.0f,
    -0.3402678967f, +0.4346303046f, +0.0261884332f, -0.7423233390f, -0.3657560945f, +0.0990294665f, 0.0f, 0.0f,
    +0.3402678967f, -0.4346303046f, -0.0261884332f, +0.7423233390f, +0.3657560945f, -0.0990294665f, 0.0f, 0.0f,
    +0.2991455793f, -0.4159909189f, +0.2353942990f, -0.3794242144f, -0.0997822434f, -0.7267279029f, 0.0f, 0.0f,
    -0.2991455793f, +0.4159909189f, -0.2353942990f, +0.3794242144f, +0.0997822434f, +0.7267279029f, 0.0f, 0.0f,
    -0.6395480037f, +0.1489928365f, +0.3965742588f, -0.6047226787f, +0.2086208463f, -0.0479167402f, 0.0f, 0.0f,
    +0.6395480037f, -0.1489928365f, -0.3965742588f, +0.6047226787f, -0.2086208463f, +0.0479167402f, 0.0f, 0.0f,
    -0.2303277254f, +0.3126686215f, +0.4817704558f, +0.0731797963f, +0.6366199255f, +0.4543615282f, 0.0f, 0.0f,
    +0.2303277254f, -0.3126686215f, -0.4817704558f, -0.0731797963f, -0.6366199255f, -0.4543615282f, 0.0f, 0.0f,
    +0.6599575281f, +0.3599447906f, -0.1367377341f, -0.3232565820f, -0.3456401229f, -0.4384481311f, 0.0f, 0.0f,
    -0.6599575281f, -0.3599447906f, +0.1367377341f, +0.3232565820f, +0.3456401229f, +0.4384481311f, 0.0f, 0.0f,
    +0.5351413488f, +0.6661217809f, +0.1041772068f, -0.4339075983f, -0.0095753074f, +0.2658663690f, 0.0f, 0.0f,
    -0.5351413488f, -0.6661217809f, -0.1041772068f, +0.4339075983f, +0.0095753074f, -0.2658663690f, 0.0f, 0.0f,
    -0.2143388391f, -0.3284190893f, +0.2893404067f, +0.3573090136f, +0.4489135742f, +0.6582462192f, 0.0f, 0.0f,
    +0.2143388391f, +0.3284190893f, -0.2893404067f, -0.3573090136f, -0.4489135742f, -0.6582462192f, 0.0f, 0.0f,
    -0.8162584305f, -0.3472681046f, +0.0630156472f, +0.1182824001f, +0.4416558743f, +0.0102633676f, 0.0f, 0.0f,
    +0.8162584305f, +0.3472681046f, -0.0630156472f, -0.1182824001f, -0.4416558743f, -0.0102633676f, 0.0f, 0.0f,
    +0.7099347115f, -0.1376320422f, -0.0274723470f, -0.4240795076f, +0.5178978443f, -0.1680269241f, 0.0f, 0.0f,
    -0.7099347115f, +0.1376320422f, +0.0274723470f, +0.4240795076f, -0.5178978443f, +0.1680269241f, 0.0f, 0.0f,
    -0.0097455978f, +0.3596805334f, +0.4481194615f, +0.7698826194f, -0.0546553358f, -0.2720606923f, 0.0f, 0.0f,
    +0.0097455978f, -0.3596805334f, -0.4481194615f, -0.7698826194f, +0.0546553358f, +0.2720606923f, 0.0f, 0.0f,
    -0.6778500080f, -0.1667014211f, -0.5443955064f, -0.1058012098f, +0.2301459759f, +0.3901311159f, 0.0f, 0.0f,
    +0.6778500080f, +0.1667014211f, +0.5443955064f, +0.1058012098f, -0.2301459759f, -0.3901311159f, 0.0f, 0.0f,
    -0.3395358324f, -0.8057957888f, +0.0985783339f, -0.1796255410f, +0.3666144907f, -0.2429388613f, 0.0f, 0.0f,
    +0.3395358324f, +0.8057957888f, -0.0985783339f, +0.1796255410f, -0.3666144907f, +0.2429388613f, 0.0f, 0.0f,
    +0.4534530640f, -0.0414590687f, -0.8502305746f, -0.0806169063f, -0.1519675851f, +0.2004396617f, 0.0f, 0.0f,
    -0.4534530640f, +0.0414590687f, +0.8502305746f, +0.0806169063f, +0.1519675851f, -0.2004396617f, 0.0f, 0.0f,
    -0.2885462046f, +0.3596998751f, -0.5182883143f, -0.5192760825f, +0.4365384877f, +0.2419112027f, 0.0f, 0.0f,
    +0.2885462046f, -0.3596998751f, +0.5182883143f, +0.5192760825f, -0.4365384877f, -0.2419112027f, 0.0f, 0.0f,
    +0.6039630175f, +0.2947888970f, -0.3093657494f, -0.2174808085f, -0.6169161797f, -0.1572819352f, 0.0f, 0.0f,
    -0.6039630175f, -0.2947888970f, +0.3093657494f, +0.2174808085f, +0.6169161797f, +0.1572819352f, 0.0f, 0.0f,
    +0.4784471989f, +0.5307044387f, +0.0115604773f, -0.2037467510f, +0.3092451692f, +0.5934318304f, 0.0f, 0.0f,
    -0.4784471989f, -0.5307044387f, -0.0115604773f, +0.2037467510f, -0.3092451692f, -0.5934318304f, 0.0f, 0.0f,
    +0.6469835043f, +0.4019250572f, -0.3637229800f, -0.4140565991f, -0.0823692828f, +0.3306759000f, 0.0f, 0.0f,
    -0.6469835043f, -0.4019250572f, +0.3637229800f, +0.4140565991f, +0.0823692828f, -0.3306759000f, 0.0f, 0.0f,
    -0.0223488212f, +0.7007750273f, +0.3346542716f, +0.3451074362f, -0.3586464524f, -0.3856100440f, 0.0f, 0.0f,
    +0.0223488212f, -0.7007750273f, -0.3346542716f, -0.3451074362f, +0.3586464524f, +0.3856100440f, 0.0f, 0.0f,
    -0.0685151815f, -0.2534404695f, -0.6264464855f, -0.2348726690f, -0.3672263622f, +0.5904389024f, 0.0f, 0.0f,
    +0.0685151815f, +0.2534404695f, +0.6264464855f, +0.2348726690f, +0.3672263622f, -0.5904389024f, 0.0f, 0.0f,
    +0.0722864866f, -0.2877607048f, -0.1823563874f, +0.2480662167f, +0.1255962104f, -0.8952110410f, 0.0f, 0.0f,
    -0.0722864866f, +0.2877607048f, +0.1823563874f, -0.2480662167f, -0.1255962104f, +0.8952110410f, 0.0f, 0.0f,
    +0.7216763496f, -0.3996139467f, +0.0217242539f, -0.4347356558f, -0.0797895789f, +0.3516517878f, 0.0f, 0.0f,
    -0.7216763496f, +0.3996139467f, -0.0217242539f, +0.4347356558f, +0.0797895789f, -0.3516517878f, 0.0f, 0.0f,
    +0.4784886837f, +0.5222305059f, +0.2333526909f, -0.5562754273f, +0.2555786967f, -0.2628834248f, 0.0f, 0.0f,
    -0.4784886837f, -0.5222305059f, -0.2333526909f, +0.5562754273f, -0.2555786967f, +0.2628834248f, 0.0f, 0.0f,
    -0.4846225381f, +0.1626633704f, -0.1288503408f, +0.5557486415f, +0.4889337420f, -0.4173326790f, 0.0f, 0.0f,
    +0.4846225381f, -0.1626633704f, +0.1288503408f, -0.5557486415f, -0.4889337420f, +0.4173326790f, 0.0f, 0.0f,
    -0.0948393345f, +0.0139375180f, -0.4854896665f, +0.4881530702f, +0.0643879399f, +0.7160112262f, 0.0f, 0.0f,
    +0.0948393345f, -0.0139375180f, +0.4854896665f, -0.4881530702f, -0.0643879399f, -0.7160112262f, 0.0f, 0.0f,
    +0.1191697121f, -0.1054357141f, +0.3988540769f, +0.2808192670f, +0.7925403714f, -0.3295717537f, 0.0f, 0.0f,
    -0.1191697121f, +0.1054357141f, -0.3988540769f, -0.2808192670f, -0.7925403714f, +0.3295717537f, 0.0f, 0.0f,
    -0.1252101064f, -0.1225888133f, +0.5913853049f, -0.0152120925f, -0.7054083347f, +0.3488920927f, 0.0f, 0.0f,
    +0.1252101064f, +0.1225888133f, -0.5913853049f, +0.0152120925f, +0.7054083347f, -0.3488920927f, 0.0f, 0.0f,
    -0.3373192549f, +0.1423646063f, -0.5901489854f, -0.1626289487f, +0.3946800828f, +0.5791816711f, 0.0f, 0.0f,
    +0.3373192549f, -0.1423646063f, +0.5901489854f, +0.1626289487f, -0.3946800828f, -0.5791816711f, 0.0f, 0.0f,
    +0.4946297407f, +0.1880782247f, -0.5507902503f, +0.5057336688f, -0.1625511348f, -0.3666178882f, 0.0f, 0.0f,
    -0.4946297407f, -0.1880782247f, +0.5507902503f, -0.5057336688f, +0.1625511348f, +0.3666178882f, 0.0f, 0.0f,
    +0.3268676996f, +0.6602906585f, -0.3660140336f, -0.0498400778f, +0.2228541374f, -0.5206336975f, 0.0f, 0.0f,
    -0.3268676996f, -0.6602906585f, +0.3660140336f, +0.0498400778f, -0.2228541374f, +0.5206336975f, 0.0f, 0.0f,
    -0.1866028309f, -0.0858784765f, -0.4369306266f, +0.8630020618f, +0.1410430223f, -0.0472154319f, 0.0f, 0.0f,
    +0.1866028309f, +0.0858784765f, +0.4369306266f, -0.8630020618f, -0.1410430223f, +0.0472154319f, 0.0f, 0.0f,
    +0.0332865715f, +0.3941048682f, -0.5121189356f, +0.0429575294f, +0.7605923414f, -0.0310103893f, 0.0f, 0.0f,
    -0.0332865715f, -0.3941048682f, +0.5121189356f, -0.0429575294f, -0.7605923414f, +0.0310103893f, 0.0f, 0.0f,
    -0.3338746428f, +0.3648748696f, +0.2913181484f, +0.5917968750f, -0.2301118970f, +0.5170617104f, 0.0f, 0.0f,
    +0.3338746428f, -0.3648748696f, -0.2913181484f, -0.5917968750f, +0.2301118970f, -0.5170617104f, 0.0f, 0.0f,
    +0.7485815287f, -0.2238343954f, +0.3268305063f, -0.4812831283f, +0.2259912491f, +0.0006947815f, 0.0f, 0.0f,
    -0.7485815287f, +0.2238343954f, -0.3268305063f, +0.4812831283f, -0.2259912491f, -0.0006947815f, 0.0f, 0.0f,
    +0.6549444199f, -0.0175904948f, -0.2006822526f, -0.3930950761f, -0.5320196748f, -0.3047885001f, 0.0f, 0.0f,
    -0.6549444199f, +0.0175904948f, +0.2006822526f, +0.3930950761f, +0.5320196748f, +0.3047885001f, 0.0f, 0.0f,
    +0.4640170336f, -0.0512287468f, +0.3114093244f, -0.8225148916f, -0.0044604074f, +0.0923942029f, 0.0f, 0.0f,
    -0.4640170336f, +0.0512287468f, -0.3114093244f, +0.8225148916f, +0.0044604074f, -0.0923942029f, 0.0f, 0.0f,
    -0.8083539009f, +0.1956296414f, -0.1233579814f, -0.0617510751f, +0.1019937396f, -0.5280718803f, 0.0f, 0.0f,
    +0.8083539009f, -0.1956296414f, +0.1233579814f, +0.0617510751f, -0.1019937396f, +0.5280718803f, 0.0f, 0.0f,
    +0.2349500656f, -0.3038652241f, +0.5920889378f, -0.4551561177f, +0.2344802916f, +0.4896395206f, 0.0f, 0.0f,
    -0.2349500656f, +0.3038652241f, -0.5920889378f, +0.4551561177f, -0.2344802916f, -0.4896395206f, 0.0f, 0.0f,
    -0.0537984967f, -0.4354662299f, -0.5761895776f, -0.0092973430f, +0.3004496098f, -0.6205835938f, 0.0f, 0.0f,
    +0.0537984967f, +0.4354662299f, +0.5761895776f, +0.0092973430f, -0.3004496098f, +0.6205835938f, 0.0f, 0.0f,
    +0.2505787611f, -0.0524614528f, +0.0763222873f, +0.0778836459f, -0.8061159849f, +0.5222482085f, 0.0f, 0.0f,
    -0.2505787611f, +0.0524614528f, -0.0763222873f, -0.0778836459f, +0.8061159849f, -0.5222482085f, 0.0f, 0.0f,
    +0.2368814945f, +0.8044263124f, +0.0637550279f, +0.3666293323f, -0.3976365030f, +0.0137477815f, 0.0f, 0.0f,
    -0.2368814945f, -0.8044263124f, -0.0637550279f, -0.3666293323f, +0.3976365030f, -0.0137477815f, 0.0f, 0.0f,
    -0.3599227071f, -0.2783524990f, +0.2736339569f, -0.5383397341f, +0.6012454033f, +0.2584464550f, 0.0f, 0.0f,
    +0.3599227071f, +0.2783524990f, -0.2736339569f, +0.5383397341f, -0.6012454033f, -0.2584464550f, 0.0f, 0.0f,
    -0.3189113140f, -0.0656187832f, -0.8606567979f, +0.0263836943f, -0.0917562470f, +0.3796631992f, 0.0f, 0.0f,
    +0.3189113140f, +0.0656187832f, +0.8606567979f, -0.0263836943f, +0.0917562470f, -0.3796631992f, 0.0f, 0.0f,
    +0.1788638830f, +0.2854136825f, -0.5737010837f, +0.5115166903f, -0.5141300559f, +0.1772974730f, 0.0f, 0.0f,
    -0.1788638830f, -0.2854136825f, +0.5737010837f, -0.5115166903f, +0.5141300559f, -0.1772974730f, 0.0f, 0.0f,
    +0.0839389563f, +0.2013413608f, -0.5184195638f, -0.3760437965f, -0.7342240810f, +0.0562374294f, 0.0f, 0.0f,
    -0.0839389563f, -0.2013413608f, +0.5184195638f, +0.3760437965f, +0.7342240810f, -0.0562374294f, 0.0f, 0.0f,
    -0.5140683055f, +0.3202590346f, +0.2012800425f, -0.3130970597f, +0.1697241515f, +0.6825088263f, 0.0f, 0.0f,
    +0.5140683055f, -0.3202590346f, -0.2012800425f, +0.3130970597f, -0.1697241515f, -0.6825088263f, 0.0f, 0.0f,
    -0.0234888792f, +0.3059980869f, +0.2770833373f, +0.4578476250f, +0.4295824170f, +0.6594488025f, 0.0f, 0.0f,
    +0.0234888792f, -0.3059980869f, -0.2770833373f, -0.4578476250f, -0.4295824170f, -0.6594488025f, 0.0f, 0.0f,
    +0.1035057306f, -0.4207383990f, -0.4682182968f, +0.6973039508f, +0.3142517805f, +0.0897224545f, 0.0f, 0.0f,
    -0.1035057306f, +0.4207383990f, +0.4682182968f, -0.6973039508f, -0.3142517805f, -0.0897224545f, 0.0f, 0.0f,
    -0.6953325272f, -0.3667758405f, -0.2844727635f, +0.2119423151f, -0.4860064983f, -0.1412143707f, 0.0f, 0.0f,
    +0.6953325272f, +0.3667758405f, +0.2844727635f, -0.2119423151f, +0.4860064983f, +0.1412143707f, 0.0f, 0.0f,
    -0.1701402068f, -0.8979806304f, +0.3227835596f, +0.1311450005f, +0.2046307474f, +0.0376982689f, 0.0f, 0.0f,
    +0.1701402068f, +0.8979806304f, -0.3227835596f, -0.1311450005f, -0.2046307474f, -0.0376982689f, 0.0f, 0.0f,
    +0.5171689987f, +0.5724862814f, -0.2912844419f, +0.5097972155f, -0.1099829152f, +0.2189970613f, 0.0f, 0.0f,
    -0.5171689987f, -0.5724862814f, +0.2912844419f, -0.5097972155f, +0.1099829152f, -0.2189970613f, 0.0f, 0.0f,
    +0.2218352556f, -0.4946886301f, +0.1242813542f, +0.3368041515f, -0.2820486724f, -0.7054343820f, 0.0f, 0.0f,
    -0.2218352556f, +0.4946886301f, -0.1242813542f, -0.3368041515f, +0.2820486724f, +0.7054343820f, 0.0f, 0.0f,
    -0.2125054002f, -0.6599932909f, -0.6661007404f, -0.1357711554f, +0.2030009478f, +0.1261623204f, 0.0f, 0.0f,
    +0.2125054002f, +0.6599932909f, +0.6661007404f, +0.1357711554f, -0.2030009478f, -0.1261623204f, 0.0f, 0.0f,
    +0.2071651220f, +0.0297779962f, -0.7990972996f, +0.4456464052f, -0.3093665540f, -0.1527425945f, 0.0f, 0.0f,
    -0.2071651220f, -0.0297779962f, +0.7990972996f, -0.4456464052f, +0.3093665540f, +0.1527425945f, 0.0f, 0.0f,
    +0.2194545269f, +0.4758194685f, -0.5903496742f, -0.1414284706f, -0.4976407886f, -0.3305663764f, 0.0f, 0.0f,
    -0.2194545269f, -0.4758194685f, +0.5903496742f, +0.1414284706f, +0.4976407886f, +0.3305663764f, 0.0f, 0.0f,
    +0.4248082638f, -0.0863814354f, -0.0192309916f, -0.6721471548f, -0.5960708857f, -0.0680014491f, 0.0f, 0.0f,
    -0.4248082638f, +0.0863814354f, +0.0192309916f, +0.6721471548f, +0.5960708857f, +0.0680014491f, 0.0f, 0.0f,
    +0.5470525026f, +0.0133477598f, -0.4758614302f, -0.0962084979f, +0.6486627460f, -0.2099793404f, 0.0f, 0.0f,
    -0.5470525026f, -0.0133477598f, +0.4758614302f, +0.0962084979f, -0.6486627460f, +0.2099793404f, 0.0f, 0.0f,
    +0.1838577986f, +0.4771481156f, -0.2559164166f, -0.0727713332f, +0.8171100020f, -0.0082812458f, 0.0f, 0.0f,
    -0.1838577986f, -0.4771481156f, +0.2559164166f, +0.0727713332f, -0.8171100020f, +0.0082812458f, 0.0f, 0.0f,
    +0.3319898844f, -0.4094194770f, -0.6344996691f, -0.1762437969f, -0.1908908188f, -0.5020627975f, 0.0f, 0.0f,
    -0.3319898844f, +0.4094194770f, +0.6344996691f, +0.1762437969f, +0.1908908188f, +0.5020627975f, 0.0f, 0.0f,
    +0.1891127825f, -0.4269990623f, -0.1553274095f, +0.7389351726f, -0.3465515673f, +0.3027516603f, 0.0f, 0.0f,
    -0.1891127825f, +0.4269990623f, +0.1553274095f, -0.7389351726f, +0.3465515673f, -0.3027516603f, 0.0f, 0.0f,
    -0.0949196219f, +0.0747370273f, +0.7246648073f, -0.3012755215f, +0.5178308487f, -0.3183543682f, 0.0f, 0.0f,
    +0.0949196219f, -0.0747370273f, -0.7246648073f, +0.3012755215f, -0.5178308487f, +0.3183543682f, 0.0f, 0.0f,
    -0.1411747336f, +0.7151866555f, +0.2841590047f, -0.0601031333f, -0.3971146047f, +0.4759400189f, 0.0f, 0.0f,
    +0.1411747336f, -0.7151866555f, -0.2841590047f, +0.0601031333f, +0.3971146047f, -0.4759400189f, 0.0f, 0.0f,
    +0.5285420418f, +0.1655655503f, +0.2944771051f, +0.7186176181f, +0.2955921888f, +0.0522332750f, 0.0f, 0.0f,
    -0.5285420418f, -0.1655655503f, -0.2944771051f, -0.7186176181f, -0.2955921888f, -0.0522332750f, 0.0f, 0.0f,
    +0.3449442387f, +0.0972718075f, -0.2850183547f, +0.7572588325f, +0.3770409524f, -0.2733420730f, 0.0f, 0.0f,
    -0.3449442387f, -0.0972718075f, +0.2850183547f, -0.7572588325f, -0.3770409524f, +0.2733420730f, 0.0f, 0.0f,
    -0.1309762597f, -0.5679270625f, -0.6601130366f, -0.3591396809f, -0.3023610413f, -0.0644285083f, 0.0f, 0.0f,
    +0.1309762597f, +0.5679270625f, +0.6601130366f, +0.3591396809f, +0.3023610413f, +0.0644285083f, 0.0f, 0.0f,
    +0.2244826555f, -0.4464081526f, +0.5022176504f, -0.1933553666f, -0.0820252001f, -0.6737879515f, 0.0f, 0.0f,
    -0.2244826555f, +0.4464081526f, -0.5022176504f, +0.1933553666f, +0.0820252001f, +0.6737879515f, 0.0f, 0.0f,
    +0.1539896727f, +0.2847151756f, +0.2715878189f, +0.6622601151f, -0.5355831981f, +0.3098822534f, 0.0f, 0.0f,
    -0.1539896727f, -0.2847151756f, -0.2715878189f, -0.6622601151f, +0.5355831981f, -0.3098822534f, 0.0f, 0.0f,
    -0.5378221273f, -0.5017089844f, -0.5904147625f, +0.0154055580f, +0.3022935092f, +0.1372116357f, 0.0f, 0.0f,
    +0.5378221273f, +0.5017089844f, +0.5904147625f, -0.0154055580f, -0.3022935092f, -0.1372116357f, 0.0f, 0.0f,
    +0.1878100634f, -0.0067449003f, -0.9253772497f, +0.3268400729f, -0.0376309901f, -0.0108781755f, 0.0f, 0.0f,
    -0.1878100634f, +0.0067449003f, +0.9253772497f, -0.3268400729f, +0.0376309901f, +0.0108781755f, 0.0f, 0.0f,
    -0.1521411538f, +0.0297914669f, +0.3752169013f, +0.3591099977f, -0.1072381437f, +0.8334973454f, 0.0f, 0.0f,
    +0.1521411538f, -0.0297914669f, -0.3752169013f, -0.3591099977f, +0.1072381437f, -0.8334973454f, 0.0f, 0.0f,
    -0.1515252590f, +0.2482029796f, +0.3501936793f, +0.5802695751f, -0.5659892559f, -0.3684334159f, 0.0f, 0.0f,
    +0.1515252590f, -0.2482029796f, -0.3501936793f, -0.5802695751f, +0.5659892559f, +0.3684334159f, 0.0f, 0.0f,
    -0.2185095549f, -0.0525716543f, +0.1914369911f, +0.4235597551f, -0.7261900902f, -0.4539678395f, 0.0f, 0.0f,
    +0.2185095549f, +0.0525716543f, -0.1914369911f, -0.4235597551f, +0.7261900902f, +0.4539678395f, 0.0f, 0.0f,
    -0.3406292200f, -0.4185351431f, +0.1780761182f, -0.7107796073f, -0.0656172335f, -0.4093602896f, 0.0f, 0.0f,
    +0.3406292200f, +0.4185351431f, -0.1780761182f, +0.7107796073f, +0.0656172335f, +0.4093602896f, 0.0f, 0.0f,
    +0.4735870361f, -0.1475064456f, +0.7918011546f, +0.1523651183f, -0.1626453549f, -0.2780987620f, 0.0f, 0.0f,
    -0.4735870361f, +0.1475064456f, -0.7918011546f, -0.1523651183f, +0.1626453549f, +0.2780987620f, 0.0f, 0.0f,
    -0.1846829057f, +0.3010945916f, -0.1225730032f, +0.0280163139f, +0.9003669024f, +0.2208276838f, 0.0f, 0.0f,
    +0.1846829057f, -0.3010945916f, +0.1225730032f, -0.0280163139f, -0.9003669024f, -0.2208276838f, 0.0f, 0.0f,
    +0.4332708120f, -0.3109011650f, +0.8411520123f, +0.0210042447f, +0.0866512954f, +0.0114217028f, 0.0f, 0.0f,
    -0.4332708120f, +0.3109011650f, -0.8411520123f, -0.0210042447f, -0.0866512954f, -0.0114217028f, 0.0f, 0.0f,
    +0.5269995928f, +0.1340612769f, +0.3584396839f, +0.1384107769f, +0.6674175858f, -0.3334905505f, 0.0f, 0.0f,
    -0.5269995928f, -0.1340612769f, -0.3584396839f, -0.1384107769f, -0.6674175858f, +0.3334905505f, 0.0f, 0.0f,
    -0.8232225180f, +0.1287598014f, +0.0783898383f, +0.3512004614f, +0.2263074517f, +0.3535870016f, 0.0f, 0.0f,
    +0.8232225180f, -0.1287598014f, -0.0783898383f, -0.3512004614f, -0.2263074517f, -0.3535870016f, 0.0f, 0.0f,
    -0.1391914487f, -0.6879768968f, +0.5911530852f, -0.0231118053f, -0.2236557752f, -0.3275598884f, 0.0f, 0.0f,
    +0.1391914487f, +0.6879768968f, -0.5911530852f, +0.0231118053f, +0.2236557752f, +0.3275598884f, 0.0f, 0.0f,
    +0.2643657923f, -0.7518967390f, -0.5412572026f, +0.2170440704f, +0.0385756940f, +0.1523365080f, 0.0f, 0.0f,
    -0.2643657923f, +0.7518967390f, +0.5412572026f, -0.2170440704f, -0.0385756940f, -0.1523365080f, 0.0f, 0.0f,
    -0.2056913376f, -0.1038825661f, -0.2111288905f, -0.1502310634f, +0.8060292602f, +0.4796573520f, 0.0f, 0.0f,
    +0.2056913376f, +0.1038825661f, +0.2111288905f, +0.1502310634f, -0.8060292602f, -0.4796573520f, 0.0f, 0.0f,
    -0.2307052016f, -0.2186420858f, +0.3125177622f, -0.3759811521f, +0.1722772121f, +0.7938903570f, 0.0f, 0.0f,
    +0.2307052016f, +0.2186420858f, -0.3125177622f, +0.3759811521f, -0.1722772121f, -0.7938903570f, 0.0f, 0.0f,
    +0.1736516953f, -0.0808940083f, -0.0183684677f, -0.9398810267f, +0.2239948511f, +0.1715025902f, 0.0f, 0.0f,
    -0.1736516953f, +0.0808940083f, +0.0183684677f, +0.9398810267f, -0.2239948511f, -0.1715025902f, 0.0f, 0.0f,
    -0.2039667368f, +0.4740311205f, -0.4512417316f, +0.6671721935f, +0.2004621923f, +0.2115859389f, 0.0f, 0.0f,
    +0.2039667368f, -0.4740311205f, +0.4512417316f, -0.6671721935f, -0.2004621923f, -0.2115859389f, 0.0f, 0.0f,
    +0.0032083988f, +0.3600846231f, -0.5048702359f, +0.5842669010f, -0.1948538274f, +0.4859001040f, 0.0f, 0.0f,
    -0.0032083988f, -0.3600846231f, +0.5048702359f, -0.5842669010f, +0.1948538274f, -0.4859001040f, 0.0f, 0.0f,
    -0.4425323009f, +0.3916659355f, -0.5925540924f, -0.0750466436f, -0.5048115253f, +0.1979288608f, 0.0f, 0.0f,
    +0.4425323009f, -0.3916659355f, +0.5925540924f, +0.0750466436f, +0.5048115253f, -0.1979288608f, 0.0f, 0.0f,
};
     */

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        camera.update();
        Arrays.fill(amounts, 0);
        Arrays.fill(dAmounts, 0.0);
        switch (mode) {
            case 0:
                if (UIUtils.shift()) sphereTrigAltMode();
                else sphereTrigMode();
                break;
            case 1:
                sphereGaussianMode();
                break;
            case 2:
                sphereRotationMode();
                break;
            case 3:
                spherePairMode();
                break;
            case 4:
                sphereHaltonMode();
                break;
            case 5:
                sphereRobertsMode();
                break;
            case 6:
                sphereRobertsVDCMode();
                break;
            case 7:
                sphereBitCountMode();
                break;
            case 8:
                if (UIUtils.shift()) sphereFibonacciAltMode();
                else sphereFibonacciMode();
                break;
            case 9:
                sphereR2Mode();
                break;
            case 10:
                sphereHalton2Mode();
                break;
            case 11:
                if (UIUtils.shift()) sphereHammersley2AltMode();
                else sphereHammersley2Mode();
                break;
            case 12:
                spherePhiMode();
                break;
            case 13:
                sphere5DMode();
                break;
            case 14:
                sphere5DHaltonMode();
                break;
            case 15:
                sphere5DR5Mode();
                break;
            case 16:
                sphere5DAceMode();
                break;
            case 17:
                sphere5DGoldenMode();
                break;
            case 18:
                sphere5DVDCMode();
                break;
            case 19:
                sphere5DUniformMode();
                break;
            case 20:
                sphereHaltonMode();
                break;
            case 21:
                sphereHaltonLogitMode();
                break;
            case 22:
                spherePrecalculatedMode();
                break;
            case 23:
                diskPrecalculatedMode();
                break;
            case 24:
                diskShuffleRandomMode();
                break;
            case 25:
                diskR4RandomMode();
                break;
            case 26:
                diskHaltonRandomMode();
                break;
            case 27:
                diskSuperFibonacciMode();
                break;
            case 28:
                diskSuperFibonacciMarsagliaMode();
                break;
            case 29:
                diskAceRandomMode();
                break;
            case 30:
                uniform3DTo4DMode();
                break;
            case 31:
                inverseStereo4DTo4DMode();
                break;
            case 32:
                inverseStereo3DTo4DMode();
                break;
            case 33:
                optimize4DMode();
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

    private void sphereHaltonLogitMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            onSphereHaltonLogit(i);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 250 + 260, points[i][1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void spherePrecalculatedMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < 256; i++) {
            renderer.color(black);
            renderer.vertex(((float)RANDOM_VECTORS[i<<2] * c + (float)RANDOM_VECTORS[i<<2|2] * s) * 250 + 260, (float)RANDOM_VECTORS[i<<2|1] * 250 + 260, 0);
        }
        renderer.end();
    }

    private void diskPrecalculatedMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D[i<<2|x] * 60 + 62 + x * 124, GRADIENTS_4D[i<<2|y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void diskSuperFibonacciMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_FIB[i<<2|x] * 60 + 62 + x * 124, GRADIENTS_4D_FIB[i<<2|y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void diskSuperFibonacciMarsagliaMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_SFM[i<<2|x] * 60 + 62 + x * 124, GRADIENTS_4D_SFM[i<<2|y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void diskAceRandomMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_ACE[i<<2|x] * 60 + 62 + x * 124, GRADIENTS_4D_ACE[i<<2|y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void optimize4DMode() {
        for (int i = 0; i < 8; i++) {
            optimizeStrength = nudgeAll(GRADIENTS_4D_CURRENT, GRADIENTS_4D_TEMP, optimizeStrength, 4, 4);
        }
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_CURRENT[i<<2|x] * 60 + 62 + x * 124, GRADIENTS_4D_CURRENT[i<<2|y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void diskR4RandomMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_R4[i << 2 | x] * 60 + 62 + x * 124, GRADIENTS_4D_R4[i << 2 | y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void diskHaltonRandomMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_HALTON[i << 2 | x] * 60 + 62 + x * 124, GRADIENTS_4D_HALTON[i << 2 | y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void diskShuffleRandomMode() {
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int i = 0; i < 256; i++) {
                    renderer.color(black);
                    renderer.vertex(GRADIENTS_4D_SHUFFLE[i<<2|x] * 60 + 62 + x * 124, GRADIENTS_4D_SHUFFLE[i<<2|y] * 60 + 62 + y * 124, 0f);
                }

            }
        }
        renderer.end();
    }

    private void uniform3DTo4DMode() {
        final float[] golden = GOLDEN_FLOATS[2]; // 3 elements
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 1; i <= 256; i++) {
            float angle0 = MathTools.fract(golden[0] * i);
            float angle1 = MathTools.fract(golden[1] * i);
            float angle2 = MathTools.fract(golden[2] * i);
            TMP_PT[0] = TrigTools.cosSmootherTurns(angle0);
            TMP_PT[1] = TrigTools.sinSmootherTurns(angle0) * TrigTools.cosSmootherTurns(angle1);
            TMP_PT[2] = TrigTools.sinSmootherTurns(angle0) * TrigTools.sinSmootherTurns(angle1) * TrigTools.cosSmootherTurns(angle2);
            TMP_PT[3] = TrigTools.sinSmootherTurns(angle0) * TrigTools.sinSmootherTurns(angle1) * TrigTools.sinSmootherTurns(angle2);
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    renderer.color(black);
                    renderer.vertex(TMP_PT[x] * 60 + 62 + x * 124, TMP_PT[y] * 60 + 62 + y * 124, 0f);
                }
            }
        }
        renderer.end();
    }

    private void inverseStereo3DTo4DMode() {
        final float[] golden = GOLDEN_FLOATS[2]; // 3 elements
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 1; i <= 256; i++) {
            final float plane1 = (2f * MathTools.fract(golden[0] * i) - 1f);
            final float plane2 = (2f * MathTools.fract(golden[1] * i) - 1f);
            final float plane3 = (2f * MathTools.fract(golden[2] * i) - 1f);
            final float mag = plane1 * plane1 + plane2 * plane2 + plane3 * plane3;
            TMP_PT[0] = (mag - 1f) / (mag + 1f);
            TMP_PT[1] = (plane1 + plane1) / (mag + 1);
            TMP_PT[2] = (plane2 + plane2) / (mag + 1);
            TMP_PT[3] = (plane3 + plane3) / (mag + 1);
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    renderer.color(black);
                    renderer.vertex(TMP_PT[x] * 60 + 62 + x * 124, TMP_PT[y] * 60 + 62 + y * 124, 0f);
                    renderer.color(black);
                    renderer.vertex(-TMP_PT[x] * 60 + 62 + x * 124, -TMP_PT[y] * 60 + 62 + y * 124, 0f);
                }
            }
            ++i;
        }
        renderer.end();
    }

    private void inverseStereo4DTo4DMode() {
        final float[] golden = GOLDEN_FLOATS[3]; // 4 elements
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 1; i <= 2048; i++) {
            final float plane1 = (2f * MathTools.fract(golden[0] * i) - 1f);
            final float plane2 = (2f * MathTools.fract(golden[1] * i) - 1f);
            final float plane3 = (2f * MathTools.fract(golden[2] * i) - 1f);
            final float plane4 = (2f * MathTools.fract(golden[3] * i) - 1f);
            final float mag = plane1 * plane1 + plane2 * plane2 + plane3 * plane3 + plane4 * plane4;
            TMP_PT[0] = (plane1 + plane1) / (mag + 1);
            TMP_PT[1] = (plane2 + plane2) / (mag + 1);
            TMP_PT[2] = (plane3 + plane3) / (mag + 1);
            TMP_PT[3] = (plane4 + plane4) / (mag + 1);
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    renderer.color(black);
                    renderer.vertex(TMP_PT[x] * 60 + 62 + x * 124, TMP_PT[y] * 60 + 62 + y * 124, 0f);
                    renderer.color(black);
                    renderer.vertex(-TMP_PT[x] * 60 + 62 + x * 124, -TMP_PT[y] * 60 + 62 + y * 124, 0f);
                }
            }
            ++i;
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
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
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_HALTON);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
        }
        renderer.end();
    }

    private void sphere5DR5Mode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_R5);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DAceMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_ACE);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DGoldenMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_GOLDEN);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DVDCMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_VDC);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
        }
        renderer.end();
    }
    private void sphere5DUniformMode() {
        float theta = (System.nanoTime() & 0xFFFFFF000000L) * INVERSE_SPEED * 8.0f,
                c = TrigTools.sinSmootherTurns(theta),
                s = TrigTools.cosSmootherTurns(theta);
        renderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < POINT_COUNT; i++) {
            inSphereFrom5D(i, GRADIENTS_5D_U);
            renderer.color(black);
            renderer.vertex((points[i][0] * c + points[i][2] * s) * 125f + 260, points[i][1] * 125f + 260, 0);
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
    public void onSphereHaltonLogit(final int index)
    {
        float x = (float) logit(QuasiRandomTools.vanDerCorput(3, index));
        float y = (float) logit(QuasiRandomTools.vanDerCorput(5, index));
        float z = (float) logit(QuasiRandomTools.vanDerCorput(7, index));

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
        float x = (float) MathTools.probit((QuasiRandomTools.GOLDEN_LONGS[2][0] * index >>> 12) * 0x1p-52);
        float y = (float) MathTools.probit((QuasiRandomTools.GOLDEN_LONGS[2][1] * index >>> 12) * 0x1p-52);
        float z = (float) MathTools.probit((QuasiRandomTools.GOLDEN_LONGS[2][2] * index >>> 12) * 0x1p-52);

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
        float x = (float) MathTools.probit(((QuasiRandomTools.GOLDEN_LONGS[2][0] * index ^ v) >>> 12) * 0x1p-52);
        float y = (float) MathTools.probit(((QuasiRandomTools.GOLDEN_LONGS[2][1] * index ^ v) >>> 12) * 0x1p-52);
        float z = (float) MathTools.probit(((QuasiRandomTools.GOLDEN_LONGS[2][2] * index ^ v) >>> 12) * 0x1p-52);

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
        long a = QuasiRandomTools.GOLDEN_LONGS[2][0] * index;
        long b = QuasiRandomTools.GOLDEN_LONGS[2][1] * index;
        long c = QuasiRandomTools.GOLDEN_LONGS[2][2] * index;
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
//        float u = 1f - 2f * lat;
        float u = 2f * (0.5f - lat);
        float root = (float) Math.sqrt(1f - u * u);
        float[] vector = points[index];

        vector[0] = TrigTools.cosTurns(lon) * root;
        vector[1] = TrigTools.sinTurns(lon) * root;
        vector[2] = u;
    }

    public void onSphereR2(final int index)
    {
        float lon = (QuasiRandomTools.GOLDEN_LONGS[1][0] * index >>> 41) * 0x1p-23f;
        float u = (QuasiRandomTools.GOLDEN_LONGS[1][1] * index >> 40) * 0x1p-23f;
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
     * Meant to imitate {@link MathTools#probit(double)} using the simpler logit function. This scales the actual logit
     * function by {@code Math.sqrt(Math.PI/8.0)}, which makes it have the same slope as probit when x is 0.5. The
     * permissible values for x are between 0.0 and 1.0 inclusive. If you pass 0, you will get negative infinity, and if
     * you pass 1, you will get positive infinity.
     * <br>
     * NOTE: this is usually slower than {@link MathTools#probit(double)}, and this doesn't produce a correct Gaussian
     * distribution. For generating Gaussian variables, {@link Ziggurat#normal(long)} is generally fastest.
     * @param x between 0 and 1, inclusive if you do accept infinite outputs, or exclusive if you do not
     * @return an approximately-normal-distributed double with mu = 0.0, sigma = 1.0
     */
    public static double logit(double x) {
        return 0.6266570686577501 * Math.log(x / (1.0 - x));
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
    public static final float[] GRADIENTS_6D = {
            -0.4102923274f, +0.0273994915f, +0.3622766435f, -0.4579788148f, +0.5484226346f, +0.4349174201f, 0.0f, 0.0f,
            +0.4102923274f, -0.0273994915f, -0.3622766435f, +0.4579788148f, -0.5484226346f, -0.4349174201f, 0.0f, 0.0f,
            -0.6331945062f, -0.2069558203f, -0.1530175954f, +0.1365562081f, +0.6509645581f, +0.3006943464f, 0.0f, 0.0f,
            +0.6331945062f, +0.2069558203f, +0.1530175954f, -0.1365562081f, -0.6509645581f, -0.3006943464f, 0.0f, 0.0f,
            -0.4208920002f, +0.4555135071f, -0.0730087459f, -0.2191289067f, -0.5559716225f, -0.5028961301f, 0.0f, 0.0f,
            +0.4208920002f, -0.4555135071f, +0.0730087459f, +0.2191289067f, +0.5559716225f, +0.5028961301f, 0.0f, 0.0f,
            +0.1098673344f, +0.3513172269f, +0.2027362138f, -0.0921144187f, -0.8546298742f, +0.2907329798f, 0.0f, 0.0f,
            -0.1098673344f, -0.3513172269f, -0.2027362138f, +0.0921144187f, +0.8546298742f, -0.2907329798f, 0.0f, 0.0f,
            -0.0308368802f, -0.7946988940f, -0.0512897000f, +0.5773004889f, -0.0228009522f, +0.1762846708f, 0.0f, 0.0f,
            +0.0308368802f, +0.7946988940f, +0.0512897000f, -0.5773004889f, +0.0228009522f, -0.1762846708f, 0.0f, 0.0f,
            -0.5510950089f, -0.2037561089f, -0.1507038772f, +0.4635705650f, +0.0297193006f, +0.6452016830f, 0.0f, 0.0f,
            +0.5510950089f, +0.2037561089f, +0.1507038772f, -0.4635705650f, -0.0297193006f, -0.6452016830f, 0.0f, 0.0f,
            -0.1132929921f, -0.0941029638f, -0.4351856411f, -0.1432463378f, -0.1829835773f, +0.8572748899f, 0.0f, 0.0f,
            +0.1132929921f, +0.0941029638f, +0.4351856411f, +0.1432463378f, +0.1829835773f, -0.8572748899f, 0.0f, 0.0f,
            +0.4488697052f, -0.4586375952f, -0.1483388841f, -0.3080208302f, -0.4370904863f, -0.5293749571f, 0.0f, 0.0f,
            -0.4488697052f, +0.4586375952f, +0.1483388841f, +0.3080208302f, +0.4370904863f, +0.5293749571f, 0.0f, 0.0f,
            +0.0574053526f, -0.0629927367f, +0.1664230078f, +0.5296119452f, -0.3503653407f, +0.7495300174f, 0.0f, 0.0f,
            -0.0574053526f, +0.0629927367f, -0.1664230078f, -0.5296119452f, +0.3503653407f, -0.7495300174f, 0.0f, 0.0f,
            -0.2494162917f, -0.3774800599f, -0.7099250555f, -0.0624758974f, -0.5358819366f, -0.0153068677f, 0.0f, 0.0f,
            +0.2494162917f, +0.3774800599f, +0.7099250555f, +0.0624758974f, +0.5358819366f, +0.0153068677f, 0.0f, 0.0f,
            +0.0713946819f, +0.5220707655f, -0.3687057793f, +0.3385064602f, +0.1528570503f, +0.6696634293f, 0.0f, 0.0f,
            -0.0713946819f, -0.5220707655f, +0.3687057793f, -0.3385064602f, -0.1528570503f, -0.6696634293f, 0.0f, 0.0f,
            +0.3751730919f, -0.3160345256f, +0.1478763521f, +0.0856548846f, -0.5217101574f, -0.6767430902f, 0.0f, 0.0f,
            -0.3751730919f, +0.3160345256f, -0.1478763521f, -0.0856548846f, +0.5217101574f, +0.6767430902f, 0.0f, 0.0f,
            -0.1814820170f, +0.3865320683f, +0.0421791673f, -0.4413846433f, -0.5046374798f, -0.6053084731f, 0.0f, 0.0f,
            +0.1814820170f, -0.3865320683f, -0.0421791673f, +0.4413846433f, +0.5046374798f, +0.6053084731f, 0.0f, 0.0f,
            +0.6560095549f, -0.1057339907f, +0.4287990928f, -0.2285138965f, +0.5209618807f, +0.2257940769f, 0.0f, 0.0f,
            -0.6560095549f, +0.1057339907f, -0.4287990928f, +0.2285138965f, -0.5209618807f, -0.2257940769f, 0.0f, 0.0f,
            -0.7355676293f, -0.0437396616f, -0.5281906128f, -0.1680916250f, +0.3456485868f, -0.1741091460f, 0.0f, 0.0f,
            +0.7355676293f, +0.0437396616f, +0.5281906128f, +0.1680916250f, -0.3456485868f, +0.1741091460f, 0.0f, 0.0f,
            -0.1068283319f, +0.6353205442f, -0.6400319934f, -0.2082310319f, -0.3611143827f, -0.0393848866f, 0.0f, 0.0f,
            +0.1068283319f, -0.6353205442f, +0.6400319934f, +0.2082310319f, +0.3611143827f, +0.0393848866f, 0.0f, 0.0f,
            -0.5210713148f, -0.5286400318f, +0.3567543328f, +0.4430956841f, +0.3527485132f, +0.0313904732f, 0.0f, 0.0f,
            +0.5210713148f, +0.5286400318f, -0.3567543328f, -0.4430956841f, -0.3527485132f, -0.0313904732f, 0.0f, 0.0f,
            -0.2621524334f, +0.7827507854f, +0.3127266169f, -0.0235683322f, +0.2010166049f, +0.4240472019f, 0.0f, 0.0f,
            +0.2621524334f, -0.7827507854f, -0.3127266169f, +0.0235683322f, -0.2010166049f, -0.4240472019f, 0.0f, 0.0f,
            -0.3612365723f, +0.6697418690f, +0.4202670455f, +0.3403534889f, -0.1285147369f, -0.3346236944f, 0.0f, 0.0f,
            +0.3612365723f, -0.6697418690f, -0.4202670455f, -0.3403534889f, +0.1285147369f, +0.3346236944f, 0.0f, 0.0f,
            -0.3815435171f, -0.0514329597f, -0.5020418763f, -0.5328508019f, +0.1157996207f, -0.5499035120f, 0.0f, 0.0f,
            +0.3815435171f, +0.0514329597f, +0.5020418763f, +0.5328508019f, -0.1157996207f, +0.5499035120f, 0.0f, 0.0f,
            -0.2437675595f, -0.3742059469f, -0.0257712305f, -0.4499314427f, +0.7685063481f, -0.0827194303f, 0.0f, 0.0f,
            +0.2437675595f, +0.3742059469f, +0.0257712305f, +0.4499314427f, -0.7685063481f, +0.0827194303f, 0.0f, 0.0f,
            +0.6465499401f, -0.0972051099f, +0.4507665932f, +0.1494120210f, +0.3139263391f, +0.4984569848f, 0.0f, 0.0f,
            -0.6465499401f, +0.0972051099f, -0.4507665932f, -0.1494120210f, -0.3139263391f, -0.4984569848f, 0.0f, 0.0f,
            +0.1433223486f, +0.2819157243f, -0.6248235703f, -0.1910795867f, +0.5105260015f, -0.4609008431f, 0.0f, 0.0f,
            -0.1433223486f, -0.2819157243f, +0.6248235703f, +0.1910795867f, -0.5105260015f, +0.4609008431f, 0.0f, 0.0f,
            +0.3310434818f, -0.5044662356f, -0.1112213060f, +0.2061044127f, +0.5647008419f, -0.5120426416f, 0.0f, 0.0f,
            -0.3310434818f, +0.5044662356f, +0.1112213060f, -0.2061044127f, -0.5647008419f, +0.5120426416f, 0.0f, 0.0f,
            +0.0267953873f, -0.5906071663f, +0.7647922039f, +0.1793488264f, -0.1738400906f, -0.0563179851f, 0.0f, 0.0f,
            -0.0267953873f, +0.5906071663f, -0.7647922039f, -0.1793488264f, +0.1738400906f, +0.0563179851f, 0.0f, 0.0f,
            +0.3272268772f, -0.2197315395f, +0.4899304211f, +0.6836864948f, -0.1572189927f, +0.3353562951f, 0.0f, 0.0f,
            -0.3272268772f, +0.2197315395f, -0.4899304211f, -0.6836864948f, +0.1572189927f, -0.3353562951f, 0.0f, 0.0f,
            +0.1752485037f, -0.2555387318f, -0.4111272693f, -0.3136070371f, +0.7978780866f, -0.0018376149f, 0.0f, 0.0f,
            -0.1752485037f, +0.2555387318f, +0.4111272693f, +0.3136070371f, -0.7978780866f, +0.0018376149f, 0.0f, 0.0f,
            +0.0549198389f, +0.4444565773f, +0.4232282341f, +0.2507682443f, -0.4505729377f, -0.5953308344f, 0.0f, 0.0f,
            -0.0549198389f, -0.4444565773f, -0.4232282341f, -0.2507682443f, +0.4505729377f, +0.5953308344f, 0.0f, 0.0f,
            -0.2947680950f, -0.5889636874f, +0.1774486303f, -0.2838816643f, -0.5162461400f, -0.4331816435f, 0.0f, 0.0f,
            +0.2947680950f, +0.5889636874f, -0.1774486303f, +0.2838816643f, +0.5162461400f, +0.4331816435f, 0.0f, 0.0f,
            +0.4026685953f, +0.3230397105f, -0.6855905056f, +0.3005967736f, +0.0639450401f, +0.4111223817f, 0.0f, 0.0f,
            -0.4026685953f, -0.3230397105f, +0.6855905056f, -0.3005967736f, -0.0639450401f, -0.4111223817f, 0.0f, 0.0f,
            +0.3663702011f, +0.3715296388f, -0.5978475809f, -0.3705040216f, +0.1842313856f, -0.4462089241f, 0.0f, 0.0f,
            -0.3663702011f, -0.3715296388f, +0.5978475809f, +0.3705040216f, -0.1842313856f, +0.4462089241f, 0.0f, 0.0f,
            -0.4303388596f, +0.2252877355f, -0.3997274637f, +0.6341809630f, +0.0027421601f, -0.4495316148f, 0.0f, 0.0f,
            +0.4303388596f, -0.2252877355f, +0.3997274637f, -0.6341809630f, -0.0027421601f, +0.4495316148f, 0.0f, 0.0f,
            -0.5852103233f, -0.4514018595f, -0.6062161326f, +0.0632691085f, -0.1914951205f, +0.2135272175f, 0.0f, 0.0f,
            +0.5852103233f, +0.4514018595f, +0.6062161326f, -0.0632691085f, +0.1914951205f, -0.2135272175f, 0.0f, 0.0f,
            -0.3208606243f, -0.1184351370f, -0.2227160931f, -0.7757527232f, +0.0729968250f, -0.4757082462f, 0.0f, 0.0f,
            +0.3208606243f, +0.1184351370f, +0.2227160931f, +0.7757527232f, -0.0729968250f, +0.4757082462f, 0.0f, 0.0f,
            +0.0886416435f, +0.3447810709f, +0.3442713916f, -0.1652077436f, -0.8527719378f, -0.0152234044f, 0.0f, 0.0f,
            -0.0886416435f, -0.3447810709f, -0.3442713916f, +0.1652077436f, +0.8527719378f, +0.0152234044f, 0.0f, 0.0f,
            -0.6935311556f, +0.2337233722f, -0.0153739937f, -0.2704828680f, -0.5485715270f, -0.3000999391f, 0.0f, 0.0f,
            +0.6935311556f, -0.2337233722f, +0.0153739937f, +0.2704828680f, +0.5485715270f, +0.3000999391f, 0.0f, 0.0f,
            -0.3011130095f, -0.0100218952f, +0.8061234355f, -0.3947089314f, -0.0001740009f, +0.3218689859f, 0.0f, 0.0f,
            +0.3011130095f, +0.0100218952f, -0.8061234355f, +0.3947089314f, +0.0001740009f, -0.3218689859f, 0.0f, 0.0f,
            -0.1438477039f, -0.0430872589f, +0.6218242049f, +0.0842250511f, +0.7619541883f, +0.0558407009f, 0.0f, 0.0f,
            +0.1438477039f, +0.0430872589f, -0.6218242049f, -0.0842250511f, -0.7619541883f, -0.0558407009f, 0.0f, 0.0f,
            +0.5918905735f, +0.6846805811f, +0.2444619238f, -0.0012684241f, +0.0003843755f, +0.3480149508f, 0.0f, 0.0f,
            -0.5918905735f, -0.6846805811f, -0.2444619238f, +0.0012684241f, -0.0003843755f, -0.3480149508f, 0.0f, 0.0f,
            -0.2457658648f, +0.0859098956f, -0.0289754011f, +0.3279665709f, +0.1680482328f, +0.8919508457f, 0.0f, 0.0f,
            +0.2457658648f, -0.0859098956f, +0.0289754011f, -0.3279665709f, -0.1680482328f, -0.8919508457f, 0.0f, 0.0f,
            +0.0883234739f, -0.0558291450f, -0.5916984677f, -0.6990013123f, -0.3849540651f, -0.0467216074f, 0.0f, 0.0f,
            -0.0883234739f, +0.0558291450f, +0.5916984677f, +0.6990013123f, +0.3849540651f, +0.0467216074f, 0.0f, 0.0f,
            -0.1616445184f, +0.0830981880f, -0.0279421657f, -0.5604254007f, -0.1720002443f, -0.7890019417f, 0.0f, 0.0f,
            +0.1616445184f, -0.0830981880f, +0.0279421657f, +0.5604254007f, +0.1720002443f, +0.7890019417f, 0.0f, 0.0f,
            +0.3784198761f, +0.3658648133f, -0.4094402790f, +0.3526407480f, +0.4318647981f, +0.4944059551f, 0.0f, 0.0f,
            -0.3784198761f, -0.3658648133f, +0.4094402790f, -0.3526407480f, -0.4318647981f, -0.4944059551f, 0.0f, 0.0f,
            -0.3402678967f, +0.4346303046f, +0.0261884332f, -0.7423233390f, -0.3657560945f, +0.0990294665f, 0.0f, 0.0f,
            +0.3402678967f, -0.4346303046f, -0.0261884332f, +0.7423233390f, +0.3657560945f, -0.0990294665f, 0.0f, 0.0f,
            +0.2991455793f, -0.4159909189f, +0.2353942990f, -0.3794242144f, -0.0997822434f, -0.7267279029f, 0.0f, 0.0f,
            -0.2991455793f, +0.4159909189f, -0.2353942990f, +0.3794242144f, +0.0997822434f, +0.7267279029f, 0.0f, 0.0f,
            -0.6395480037f, +0.1489928365f, +0.3965742588f, -0.6047226787f, +0.2086208463f, -0.0479167402f, 0.0f, 0.0f,
            +0.6395480037f, -0.1489928365f, -0.3965742588f, +0.6047226787f, -0.2086208463f, +0.0479167402f, 0.0f, 0.0f,
            -0.2303277254f, +0.3126686215f, +0.4817704558f, +0.0731797963f, +0.6366199255f, +0.4543615282f, 0.0f, 0.0f,
            +0.2303277254f, -0.3126686215f, -0.4817704558f, -0.0731797963f, -0.6366199255f, -0.4543615282f, 0.0f, 0.0f,
            +0.6599575281f, +0.3599447906f, -0.1367377341f, -0.3232565820f, -0.3456401229f, -0.4384481311f, 0.0f, 0.0f,
            -0.6599575281f, -0.3599447906f, +0.1367377341f, +0.3232565820f, +0.3456401229f, +0.4384481311f, 0.0f, 0.0f,
            +0.5351413488f, +0.6661217809f, +0.1041772068f, -0.4339075983f, -0.0095753074f, +0.2658663690f, 0.0f, 0.0f,
            -0.5351413488f, -0.6661217809f, -0.1041772068f, +0.4339075983f, +0.0095753074f, -0.2658663690f, 0.0f, 0.0f,
            -0.2143388391f, -0.3284190893f, +0.2893404067f, +0.3573090136f, +0.4489135742f, +0.6582462192f, 0.0f, 0.0f,
            +0.2143388391f, +0.3284190893f, -0.2893404067f, -0.3573090136f, -0.4489135742f, -0.6582462192f, 0.0f, 0.0f,
            -0.8162584305f, -0.3472681046f, +0.0630156472f, +0.1182824001f, +0.4416558743f, +0.0102633676f, 0.0f, 0.0f,
            +0.8162584305f, +0.3472681046f, -0.0630156472f, -0.1182824001f, -0.4416558743f, -0.0102633676f, 0.0f, 0.0f,
            +0.7099347115f, -0.1376320422f, -0.0274723470f, -0.4240795076f, +0.5178978443f, -0.1680269241f, 0.0f, 0.0f,
            -0.7099347115f, +0.1376320422f, +0.0274723470f, +0.4240795076f, -0.5178978443f, +0.1680269241f, 0.0f, 0.0f,
            -0.0097455978f, +0.3596805334f, +0.4481194615f, +0.7698826194f, -0.0546553358f, -0.2720606923f, 0.0f, 0.0f,
            +0.0097455978f, -0.3596805334f, -0.4481194615f, -0.7698826194f, +0.0546553358f, +0.2720606923f, 0.0f, 0.0f,
            -0.6778500080f, -0.1667014211f, -0.5443955064f, -0.1058012098f, +0.2301459759f, +0.3901311159f, 0.0f, 0.0f,
            +0.6778500080f, +0.1667014211f, +0.5443955064f, +0.1058012098f, -0.2301459759f, -0.3901311159f, 0.0f, 0.0f,
            -0.3395358324f, -0.8057957888f, +0.0985783339f, -0.1796255410f, +0.3666144907f, -0.2429388613f, 0.0f, 0.0f,
            +0.3395358324f, +0.8057957888f, -0.0985783339f, +0.1796255410f, -0.3666144907f, +0.2429388613f, 0.0f, 0.0f,
            +0.4534530640f, -0.0414590687f, -0.8502305746f, -0.0806169063f, -0.1519675851f, +0.2004396617f, 0.0f, 0.0f,
            -0.4534530640f, +0.0414590687f, +0.8502305746f, +0.0806169063f, +0.1519675851f, -0.2004396617f, 0.0f, 0.0f,
            -0.2885462046f, +0.3596998751f, -0.5182883143f, -0.5192760825f, +0.4365384877f, +0.2419112027f, 0.0f, 0.0f,
            +0.2885462046f, -0.3596998751f, +0.5182883143f, +0.5192760825f, -0.4365384877f, -0.2419112027f, 0.0f, 0.0f,
            +0.6039630175f, +0.2947888970f, -0.3093657494f, -0.2174808085f, -0.6169161797f, -0.1572819352f, 0.0f, 0.0f,
            -0.6039630175f, -0.2947888970f, +0.3093657494f, +0.2174808085f, +0.6169161797f, +0.1572819352f, 0.0f, 0.0f,
            +0.4784471989f, +0.5307044387f, +0.0115604773f, -0.2037467510f, +0.3092451692f, +0.5934318304f, 0.0f, 0.0f,
            -0.4784471989f, -0.5307044387f, -0.0115604773f, +0.2037467510f, -0.3092451692f, -0.5934318304f, 0.0f, 0.0f,
            +0.6469835043f, +0.4019250572f, -0.3637229800f, -0.4140565991f, -0.0823692828f, +0.3306759000f, 0.0f, 0.0f,
            -0.6469835043f, -0.4019250572f, +0.3637229800f, +0.4140565991f, +0.0823692828f, -0.3306759000f, 0.0f, 0.0f,
            -0.0223488212f, +0.7007750273f, +0.3346542716f, +0.3451074362f, -0.3586464524f, -0.3856100440f, 0.0f, 0.0f,
            +0.0223488212f, -0.7007750273f, -0.3346542716f, -0.3451074362f, +0.3586464524f, +0.3856100440f, 0.0f, 0.0f,
            -0.0685151815f, -0.2534404695f, -0.6264464855f, -0.2348726690f, -0.3672263622f, +0.5904389024f, 0.0f, 0.0f,
            +0.0685151815f, +0.2534404695f, +0.6264464855f, +0.2348726690f, +0.3672263622f, -0.5904389024f, 0.0f, 0.0f,
            +0.0722864866f, -0.2877607048f, -0.1823563874f, +0.2480662167f, +0.1255962104f, -0.8952110410f, 0.0f, 0.0f,
            -0.0722864866f, +0.2877607048f, +0.1823563874f, -0.2480662167f, -0.1255962104f, +0.8952110410f, 0.0f, 0.0f,
            +0.7216763496f, -0.3996139467f, +0.0217242539f, -0.4347356558f, -0.0797895789f, +0.3516517878f, 0.0f, 0.0f,
            -0.7216763496f, +0.3996139467f, -0.0217242539f, +0.4347356558f, +0.0797895789f, -0.3516517878f, 0.0f, 0.0f,
            +0.4784886837f, +0.5222305059f, +0.2333526909f, -0.5562754273f, +0.2555786967f, -0.2628834248f, 0.0f, 0.0f,
            -0.4784886837f, -0.5222305059f, -0.2333526909f, +0.5562754273f, -0.2555786967f, +0.2628834248f, 0.0f, 0.0f,
            -0.4846225381f, +0.1626633704f, -0.1288503408f, +0.5557486415f, +0.4889337420f, -0.4173326790f, 0.0f, 0.0f,
            +0.4846225381f, -0.1626633704f, +0.1288503408f, -0.5557486415f, -0.4889337420f, +0.4173326790f, 0.0f, 0.0f,
            -0.0948393345f, +0.0139375180f, -0.4854896665f, +0.4881530702f, +0.0643879399f, +0.7160112262f, 0.0f, 0.0f,
            +0.0948393345f, -0.0139375180f, +0.4854896665f, -0.4881530702f, -0.0643879399f, -0.7160112262f, 0.0f, 0.0f,
            +0.1191697121f, -0.1054357141f, +0.3988540769f, +0.2808192670f, +0.7925403714f, -0.3295717537f, 0.0f, 0.0f,
            -0.1191697121f, +0.1054357141f, -0.3988540769f, -0.2808192670f, -0.7925403714f, +0.3295717537f, 0.0f, 0.0f,
            -0.1252101064f, -0.1225888133f, +0.5913853049f, -0.0152120925f, -0.7054083347f, +0.3488920927f, 0.0f, 0.0f,
            +0.1252101064f, +0.1225888133f, -0.5913853049f, +0.0152120925f, +0.7054083347f, -0.3488920927f, 0.0f, 0.0f,
            -0.3373192549f, +0.1423646063f, -0.5901489854f, -0.1626289487f, +0.3946800828f, +0.5791816711f, 0.0f, 0.0f,
            +0.3373192549f, -0.1423646063f, +0.5901489854f, +0.1626289487f, -0.3946800828f, -0.5791816711f, 0.0f, 0.0f,
            +0.4946297407f, +0.1880782247f, -0.5507902503f, +0.5057336688f, -0.1625511348f, -0.3666178882f, 0.0f, 0.0f,
            -0.4946297407f, -0.1880782247f, +0.5507902503f, -0.5057336688f, +0.1625511348f, +0.3666178882f, 0.0f, 0.0f,
            +0.3268676996f, +0.6602906585f, -0.3660140336f, -0.0498400778f, +0.2228541374f, -0.5206336975f, 0.0f, 0.0f,
            -0.3268676996f, -0.6602906585f, +0.3660140336f, +0.0498400778f, -0.2228541374f, +0.5206336975f, 0.0f, 0.0f,
            -0.1866028309f, -0.0858784765f, -0.4369306266f, +0.8630020618f, +0.1410430223f, -0.0472154319f, 0.0f, 0.0f,
            +0.1866028309f, +0.0858784765f, +0.4369306266f, -0.8630020618f, -0.1410430223f, +0.0472154319f, 0.0f, 0.0f,
            +0.0332865715f, +0.3941048682f, -0.5121189356f, +0.0429575294f, +0.7605923414f, -0.0310103893f, 0.0f, 0.0f,
            -0.0332865715f, -0.3941048682f, +0.5121189356f, -0.0429575294f, -0.7605923414f, +0.0310103893f, 0.0f, 0.0f,
            -0.3338746428f, +0.3648748696f, +0.2913181484f, +0.5917968750f, -0.2301118970f, +0.5170617104f, 0.0f, 0.0f,
            +0.3338746428f, -0.3648748696f, -0.2913181484f, -0.5917968750f, +0.2301118970f, -0.5170617104f, 0.0f, 0.0f,
            +0.7485815287f, -0.2238343954f, +0.3268305063f, -0.4812831283f, +0.2259912491f, +0.0006947815f, 0.0f, 0.0f,
            -0.7485815287f, +0.2238343954f, -0.3268305063f, +0.4812831283f, -0.2259912491f, -0.0006947815f, 0.0f, 0.0f,
            +0.6549444199f, -0.0175904948f, -0.2006822526f, -0.3930950761f, -0.5320196748f, -0.3047885001f, 0.0f, 0.0f,
            -0.6549444199f, +0.0175904948f, +0.2006822526f, +0.3930950761f, +0.5320196748f, +0.3047885001f, 0.0f, 0.0f,
            +0.4640170336f, -0.0512287468f, +0.3114093244f, -0.8225148916f, -0.0044604074f, +0.0923942029f, 0.0f, 0.0f,
            -0.4640170336f, +0.0512287468f, -0.3114093244f, +0.8225148916f, +0.0044604074f, -0.0923942029f, 0.0f, 0.0f,
            -0.8083539009f, +0.1956296414f, -0.1233579814f, -0.0617510751f, +0.1019937396f, -0.5280718803f, 0.0f, 0.0f,
            +0.8083539009f, -0.1956296414f, +0.1233579814f, +0.0617510751f, -0.1019937396f, +0.5280718803f, 0.0f, 0.0f,
            +0.2349500656f, -0.3038652241f, +0.5920889378f, -0.4551561177f, +0.2344802916f, +0.4896395206f, 0.0f, 0.0f,
            -0.2349500656f, +0.3038652241f, -0.5920889378f, +0.4551561177f, -0.2344802916f, -0.4896395206f, 0.0f, 0.0f,
            -0.0537984967f, -0.4354662299f, -0.5761895776f, -0.0092973430f, +0.3004496098f, -0.6205835938f, 0.0f, 0.0f,
            +0.0537984967f, +0.4354662299f, +0.5761895776f, +0.0092973430f, -0.3004496098f, +0.6205835938f, 0.0f, 0.0f,
            +0.2505787611f, -0.0524614528f, +0.0763222873f, +0.0778836459f, -0.8061159849f, +0.5222482085f, 0.0f, 0.0f,
            -0.2505787611f, +0.0524614528f, -0.0763222873f, -0.0778836459f, +0.8061159849f, -0.5222482085f, 0.0f, 0.0f,
            +0.2368814945f, +0.8044263124f, +0.0637550279f, +0.3666293323f, -0.3976365030f, +0.0137477815f, 0.0f, 0.0f,
            -0.2368814945f, -0.8044263124f, -0.0637550279f, -0.3666293323f, +0.3976365030f, -0.0137477815f, 0.0f, 0.0f,
            -0.3599227071f, -0.2783524990f, +0.2736339569f, -0.5383397341f, +0.6012454033f, +0.2584464550f, 0.0f, 0.0f,
            +0.3599227071f, +0.2783524990f, -0.2736339569f, +0.5383397341f, -0.6012454033f, -0.2584464550f, 0.0f, 0.0f,
            -0.3189113140f, -0.0656187832f, -0.8606567979f, +0.0263836943f, -0.0917562470f, +0.3796631992f, 0.0f, 0.0f,
            +0.3189113140f, +0.0656187832f, +0.8606567979f, -0.0263836943f, +0.0917562470f, -0.3796631992f, 0.0f, 0.0f,
            +0.1788638830f, +0.2854136825f, -0.5737010837f, +0.5115166903f, -0.5141300559f, +0.1772974730f, 0.0f, 0.0f,
            -0.1788638830f, -0.2854136825f, +0.5737010837f, -0.5115166903f, +0.5141300559f, -0.1772974730f, 0.0f, 0.0f,
            +0.0839389563f, +0.2013413608f, -0.5184195638f, -0.3760437965f, -0.7342240810f, +0.0562374294f, 0.0f, 0.0f,
            -0.0839389563f, -0.2013413608f, +0.5184195638f, +0.3760437965f, +0.7342240810f, -0.0562374294f, 0.0f, 0.0f,
            -0.5140683055f, +0.3202590346f, +0.2012800425f, -0.3130970597f, +0.1697241515f, +0.6825088263f, 0.0f, 0.0f,
            +0.5140683055f, -0.3202590346f, -0.2012800425f, +0.3130970597f, -0.1697241515f, -0.6825088263f, 0.0f, 0.0f,
            -0.0234888792f, +0.3059980869f, +0.2770833373f, +0.4578476250f, +0.4295824170f, +0.6594488025f, 0.0f, 0.0f,
            +0.0234888792f, -0.3059980869f, -0.2770833373f, -0.4578476250f, -0.4295824170f, -0.6594488025f, 0.0f, 0.0f,
            +0.1035057306f, -0.4207383990f, -0.4682182968f, +0.6973039508f, +0.3142517805f, +0.0897224545f, 0.0f, 0.0f,
            -0.1035057306f, +0.4207383990f, +0.4682182968f, -0.6973039508f, -0.3142517805f, -0.0897224545f, 0.0f, 0.0f,
            -0.6953325272f, -0.3667758405f, -0.2844727635f, +0.2119423151f, -0.4860064983f, -0.1412143707f, 0.0f, 0.0f,
            +0.6953325272f, +0.3667758405f, +0.2844727635f, -0.2119423151f, +0.4860064983f, +0.1412143707f, 0.0f, 0.0f,
            -0.1701402068f, -0.8979806304f, +0.3227835596f, +0.1311450005f, +0.2046307474f, +0.0376982689f, 0.0f, 0.0f,
            +0.1701402068f, +0.8979806304f, -0.3227835596f, -0.1311450005f, -0.2046307474f, -0.0376982689f, 0.0f, 0.0f,
            +0.5171689987f, +0.5724862814f, -0.2912844419f, +0.5097972155f, -0.1099829152f, +0.2189970613f, 0.0f, 0.0f,
            -0.5171689987f, -0.5724862814f, +0.2912844419f, -0.5097972155f, +0.1099829152f, -0.2189970613f, 0.0f, 0.0f,
            +0.2218352556f, -0.4946886301f, +0.1242813542f, +0.3368041515f, -0.2820486724f, -0.7054343820f, 0.0f, 0.0f,
            -0.2218352556f, +0.4946886301f, -0.1242813542f, -0.3368041515f, +0.2820486724f, +0.7054343820f, 0.0f, 0.0f,
            -0.2125054002f, -0.6599932909f, -0.6661007404f, -0.1357711554f, +0.2030009478f, +0.1261623204f, 0.0f, 0.0f,
            +0.2125054002f, +0.6599932909f, +0.6661007404f, +0.1357711554f, -0.2030009478f, -0.1261623204f, 0.0f, 0.0f,
            +0.2071651220f, +0.0297779962f, -0.7990972996f, +0.4456464052f, -0.3093665540f, -0.1527425945f, 0.0f, 0.0f,
            -0.2071651220f, -0.0297779962f, +0.7990972996f, -0.4456464052f, +0.3093665540f, +0.1527425945f, 0.0f, 0.0f,
            +0.2194545269f, +0.4758194685f, -0.5903496742f, -0.1414284706f, -0.4976407886f, -0.3305663764f, 0.0f, 0.0f,
            -0.2194545269f, -0.4758194685f, +0.5903496742f, +0.1414284706f, +0.4976407886f, +0.3305663764f, 0.0f, 0.0f,
            +0.4248082638f, -0.0863814354f, -0.0192309916f, -0.6721471548f, -0.5960708857f, -0.0680014491f, 0.0f, 0.0f,
            -0.4248082638f, +0.0863814354f, +0.0192309916f, +0.6721471548f, +0.5960708857f, +0.0680014491f, 0.0f, 0.0f,
            +0.5470525026f, +0.0133477598f, -0.4758614302f, -0.0962084979f, +0.6486627460f, -0.2099793404f, 0.0f, 0.0f,
            -0.5470525026f, -0.0133477598f, +0.4758614302f, +0.0962084979f, -0.6486627460f, +0.2099793404f, 0.0f, 0.0f,
            +0.1838577986f, +0.4771481156f, -0.2559164166f, -0.0727713332f, +0.8171100020f, -0.0082812458f, 0.0f, 0.0f,
            -0.1838577986f, -0.4771481156f, +0.2559164166f, +0.0727713332f, -0.8171100020f, +0.0082812458f, 0.0f, 0.0f,
            +0.3319898844f, -0.4094194770f, -0.6344996691f, -0.1762437969f, -0.1908908188f, -0.5020627975f, 0.0f, 0.0f,
            -0.3319898844f, +0.4094194770f, +0.6344996691f, +0.1762437969f, +0.1908908188f, +0.5020627975f, 0.0f, 0.0f,
            +0.1891127825f, -0.4269990623f, -0.1553274095f, +0.7389351726f, -0.3465515673f, +0.3027516603f, 0.0f, 0.0f,
            -0.1891127825f, +0.4269990623f, +0.1553274095f, -0.7389351726f, +0.3465515673f, -0.3027516603f, 0.0f, 0.0f,
            -0.0949196219f, +0.0747370273f, +0.7246648073f, -0.3012755215f, +0.5178308487f, -0.3183543682f, 0.0f, 0.0f,
            +0.0949196219f, -0.0747370273f, -0.7246648073f, +0.3012755215f, -0.5178308487f, +0.3183543682f, 0.0f, 0.0f,
            -0.1411747336f, +0.7151866555f, +0.2841590047f, -0.0601031333f, -0.3971146047f, +0.4759400189f, 0.0f, 0.0f,
            +0.1411747336f, -0.7151866555f, -0.2841590047f, +0.0601031333f, +0.3971146047f, -0.4759400189f, 0.0f, 0.0f,
            +0.5285420418f, +0.1655655503f, +0.2944771051f, +0.7186176181f, +0.2955921888f, +0.0522332750f, 0.0f, 0.0f,
            -0.5285420418f, -0.1655655503f, -0.2944771051f, -0.7186176181f, -0.2955921888f, -0.0522332750f, 0.0f, 0.0f,
            +0.3449442387f, +0.0972718075f, -0.2850183547f, +0.7572588325f, +0.3770409524f, -0.2733420730f, 0.0f, 0.0f,
            -0.3449442387f, -0.0972718075f, +0.2850183547f, -0.7572588325f, -0.3770409524f, +0.2733420730f, 0.0f, 0.0f,
            -0.1309762597f, -0.5679270625f, -0.6601130366f, -0.3591396809f, -0.3023610413f, -0.0644285083f, 0.0f, 0.0f,
            +0.1309762597f, +0.5679270625f, +0.6601130366f, +0.3591396809f, +0.3023610413f, +0.0644285083f, 0.0f, 0.0f,
            +0.2244826555f, -0.4464081526f, +0.5022176504f, -0.1933553666f, -0.0820252001f, -0.6737879515f, 0.0f, 0.0f,
            -0.2244826555f, +0.4464081526f, -0.5022176504f, +0.1933553666f, +0.0820252001f, +0.6737879515f, 0.0f, 0.0f,
            +0.1539896727f, +0.2847151756f, +0.2715878189f, +0.6622601151f, -0.5355831981f, +0.3098822534f, 0.0f, 0.0f,
            -0.1539896727f, -0.2847151756f, -0.2715878189f, -0.6622601151f, +0.5355831981f, -0.3098822534f, 0.0f, 0.0f,
            -0.5378221273f, -0.5017089844f, -0.5904147625f, +0.0154055580f, +0.3022935092f, +0.1372116357f, 0.0f, 0.0f,
            +0.5378221273f, +0.5017089844f, +0.5904147625f, -0.0154055580f, -0.3022935092f, -0.1372116357f, 0.0f, 0.0f,
            +0.1878100634f, -0.0067449003f, -0.9253772497f, +0.3268400729f, -0.0376309901f, -0.0108781755f, 0.0f, 0.0f,
            -0.1878100634f, +0.0067449003f, +0.9253772497f, -0.3268400729f, +0.0376309901f, +0.0108781755f, 0.0f, 0.0f,
            -0.1521411538f, +0.0297914669f, +0.3752169013f, +0.3591099977f, -0.1072381437f, +0.8334973454f, 0.0f, 0.0f,
            +0.1521411538f, -0.0297914669f, -0.3752169013f, -0.3591099977f, +0.1072381437f, -0.8334973454f, 0.0f, 0.0f,
            -0.1515252590f, +0.2482029796f, +0.3501936793f, +0.5802695751f, -0.5659892559f, -0.3684334159f, 0.0f, 0.0f,
            +0.1515252590f, -0.2482029796f, -0.3501936793f, -0.5802695751f, +0.5659892559f, +0.3684334159f, 0.0f, 0.0f,
            -0.2185095549f, -0.0525716543f, +0.1914369911f, +0.4235597551f, -0.7261900902f, -0.4539678395f, 0.0f, 0.0f,
            +0.2185095549f, +0.0525716543f, -0.1914369911f, -0.4235597551f, +0.7261900902f, +0.4539678395f, 0.0f, 0.0f,
            -0.3406292200f, -0.4185351431f, +0.1780761182f, -0.7107796073f, -0.0656172335f, -0.4093602896f, 0.0f, 0.0f,
            +0.3406292200f, +0.4185351431f, -0.1780761182f, +0.7107796073f, +0.0656172335f, +0.4093602896f, 0.0f, 0.0f,
            +0.4735870361f, -0.1475064456f, +0.7918011546f, +0.1523651183f, -0.1626453549f, -0.2780987620f, 0.0f, 0.0f,
            -0.4735870361f, +0.1475064456f, -0.7918011546f, -0.1523651183f, +0.1626453549f, +0.2780987620f, 0.0f, 0.0f,
            -0.1846829057f, +0.3010945916f, -0.1225730032f, +0.0280163139f, +0.9003669024f, +0.2208276838f, 0.0f, 0.0f,
            +0.1846829057f, -0.3010945916f, +0.1225730032f, -0.0280163139f, -0.9003669024f, -0.2208276838f, 0.0f, 0.0f,
            +0.4332708120f, -0.3109011650f, +0.8411520123f, +0.0210042447f, +0.0866512954f, +0.0114217028f, 0.0f, 0.0f,
            -0.4332708120f, +0.3109011650f, -0.8411520123f, -0.0210042447f, -0.0866512954f, -0.0114217028f, 0.0f, 0.0f,
            +0.5269995928f, +0.1340612769f, +0.3584396839f, +0.1384107769f, +0.6674175858f, -0.3334905505f, 0.0f, 0.0f,
            -0.5269995928f, -0.1340612769f, -0.3584396839f, -0.1384107769f, -0.6674175858f, +0.3334905505f, 0.0f, 0.0f,
            -0.8232225180f, +0.1287598014f, +0.0783898383f, +0.3512004614f, +0.2263074517f, +0.3535870016f, 0.0f, 0.0f,
            +0.8232225180f, -0.1287598014f, -0.0783898383f, -0.3512004614f, -0.2263074517f, -0.3535870016f, 0.0f, 0.0f,
            -0.1391914487f, -0.6879768968f, +0.5911530852f, -0.0231118053f, -0.2236557752f, -0.3275598884f, 0.0f, 0.0f,
            +0.1391914487f, +0.6879768968f, -0.5911530852f, +0.0231118053f, +0.2236557752f, +0.3275598884f, 0.0f, 0.0f,
            +0.2643657923f, -0.7518967390f, -0.5412572026f, +0.2170440704f, +0.0385756940f, +0.1523365080f, 0.0f, 0.0f,
            -0.2643657923f, +0.7518967390f, +0.5412572026f, -0.2170440704f, -0.0385756940f, -0.1523365080f, 0.0f, 0.0f,
            -0.2056913376f, -0.1038825661f, -0.2111288905f, -0.1502310634f, +0.8060292602f, +0.4796573520f, 0.0f, 0.0f,
            +0.2056913376f, +0.1038825661f, +0.2111288905f, +0.1502310634f, -0.8060292602f, -0.4796573520f, 0.0f, 0.0f,
            -0.2307052016f, -0.2186420858f, +0.3125177622f, -0.3759811521f, +0.1722772121f, +0.7938903570f, 0.0f, 0.0f,
            +0.2307052016f, +0.2186420858f, -0.3125177622f, +0.3759811521f, -0.1722772121f, -0.7938903570f, 0.0f, 0.0f,
            +0.1736516953f, -0.0808940083f, -0.0183684677f, -0.9398810267f, +0.2239948511f, +0.1715025902f, 0.0f, 0.0f,
            -0.1736516953f, +0.0808940083f, +0.0183684677f, +0.9398810267f, -0.2239948511f, -0.1715025902f, 0.0f, 0.0f,
            -0.2039667368f, +0.4740311205f, -0.4512417316f, +0.6671721935f, +0.2004621923f, +0.2115859389f, 0.0f, 0.0f,
            +0.2039667368f, -0.4740311205f, +0.4512417316f, -0.6671721935f, -0.2004621923f, -0.2115859389f, 0.0f, 0.0f,
            +0.0032083988f, +0.3600846231f, -0.5048702359f, +0.5842669010f, -0.1948538274f, +0.4859001040f, 0.0f, 0.0f,
            -0.0032083988f, -0.3600846231f, +0.5048702359f, -0.5842669010f, +0.1948538274f, -0.4859001040f, 0.0f, 0.0f,
            -0.4425323009f, +0.3916659355f, -0.5925540924f, -0.0750466436f, -0.5048115253f, +0.1979288608f, 0.0f, 0.0f,
            +0.4425323009f, -0.3916659355f, +0.5925540924f, +0.0750466436f, +0.5048115253f, -0.1979288608f, 0.0f, 0.0f,
    };

    private static final int STANDARD_COUNT = 256;
    private final float[] GRADIENTS_4D_TEMP = new float[STANDARD_COUNT<<2];
    private final float[] GRADIENTS_4D_ACE = new float[STANDARD_COUNT<<2];
    private final float[] GRADIENTS_4D_FIB = new float[STANDARD_COUNT<<2];
    private final float[] GRADIENTS_4D_SFM = new float[STANDARD_COUNT<<2];
    private final float[] GRADIENTS_4D_R4 = new float[STANDARD_COUNT<<2];
    private final float[] GRADIENTS_4D_HALTON = new float[STANDARD_COUNT<<2];
    private final float[] GRADIENTS_4D_SHUFFLE = new float[STANDARD_COUNT<<2];
    private final float[] SHUFFLES = new float[STANDARD_COUNT << 2];
    private final float[] GRADIENTS_4D_CURRENT = new float[STANDARD_COUNT << 2];
    {
//        gaussianHalton(GRADIENTS_4D_CURRENT);
//        gaussianR4(GRADIENTS_4D_CURRENT);
        //Super-Fibonacci spiral (epsilon 2.4545455) with a  0.6556078792, b  0.3184083104 has min dist 0.369142
        superFibonacci4D(2.4545455f, GRADIENTS_4D_CURRENT, 0.6556078792f, 0.3184083104f);
//        superFibonacci4D(1.7692308f, GRADIENTS_4D_CURRENT, 0.5866990089f, 0.6119842529f);
//        marsagliaRandom4D(new AceRandom(1234567890L), GRADIENTS_4D_CURRENT);
//        superFibonacci4D(2.5f, GRADIENTS_4D_CURRENT, 0.6923295856f, 0.7171460986f);
//        superFibonacci4D(0.5f, GRADIENTS_4D_CURRENT, MathTools.ROOT2, 1.533751168755204288118041f);

    }
//    private final float[] GRADIENTS_4D_CURRENT = Arrays.copyOf(GradientVectors.GRADIENTS_4D, STANDARD_COUNT<<2);

    private final float[] GRADIENTS_5D_HALTON = new float[STANDARD_COUNT<<3];
    private final float[] GRADIENTS_5D_R5 = new float[STANDARD_COUNT<<3];
    private final float[] GRADIENTS_5D_ACE = new float[STANDARD_COUNT<<3];
    private final float[] GRADIENTS_5D_GOLDEN = new float[STANDARD_COUNT<<3];
    private final float[] GRADIENTS_5D_VDC = new float[STANDARD_COUNT<<3];
    private final float[] GRADIENTS_5D_U = new float[STANDARD_COUNT<<3];

    private final float[] GRADIENTS_5D_TEMP = new float[STANDARD_COUNT<<3];

    private final float[] GRADIENTS_6D_ACE = new float[STANDARD_COUNT<<3];
    private final float[] GRADIENTS_6D_U = new float[STANDARD_COUNT<<3];

    private final float[] GRADIENTS_6D_TEMP = new float[STANDARD_COUNT<<3];

    /**
     * <a href="https://marcalexa.github.io/superfibonacci/">Based on the algorithm from here</a>.
     * @param epsilon     typically either 0.5f or 0.36f
     * @param gradients4D the gradient vector array to write to, in groups of 4 floats per vector
     */
    private void superFibonacci4D(final float epsilon, final float[] gradients4D, float a, float b) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float s = i + epsilon;
            float t = s / (STANDARD_COUNT - 1 + epsilon + epsilon);
            float r = (float)Math.sqrt(t);
            float c = (float)Math.sqrt(1f-t);
            float alpha = s * a, beta = s * b;
//            gradients4D[i << 2    ] = r * TrigTools.sinSmootherTurns(alpha);
//            gradients4D[i << 2 | 1] = r * TrigTools.cosSmootherTurns(alpha);
//            gradients4D[i << 2 | 2] = c * TrigTools.sinSmootherTurns(beta);
//            gradients4D[i << 2 | 3] = c * TrigTools.cosSmootherTurns(beta);
            gradients4D[i + 1 << 2    ] = -(gradients4D[i << 2    ] = r * TrigTools.sinSmootherTurns(alpha));
            gradients4D[i + 1 << 2 | 1] = -(gradients4D[i << 2 | 1] = r * TrigTools.cosSmootherTurns(alpha));
            gradients4D[i + 1 << 2 | 2] = -(gradients4D[i << 2 | 2] = c * TrigTools.sinSmootherTurns(beta));
            gradients4D[i + 1 << 2 | 3] = -(gradients4D[i << 2 | 3] = c * TrigTools.cosSmootherTurns(beta));
            ++i;
        }
    }

    /**
     * Was <a href="https://marcalexa.github.io/superfibonacci/">based on the algorithm from here</a>, but not anymore.
     * Now <a href="https://projecteuclid.org/journals/annals-of-mathematical-statistics/volume-43/issue-2/Choosing-a-Point-from-the-Surface-of-a-Sphere/10.1214/aoms/1177692644.full">based on an algorithm by Marsaglia</a>.
     * @param epsilon     typically either 0.5f or 0.36f
     * @param gradients4D the gradient vector array to write to, in groups of 4 floats per vector
     */
    private void superFibonacciMarsaglia4D(final float epsilon, final float[] gradients4D, float a, float b) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float upped = i + epsilon;
            float xn = upped / (STANDARD_COUNT - 1 + epsilon + epsilon);
            float y0 = upped / a; y0 -= (int)y0;
            float y1 = upped / b; y1 -= (int)y1;

            // theta 0 and 1 is xn, in turns.
            float radius0 = (float) Math.sqrt(y0);
            float radius1 = (float) Math.sqrt(y1);
            float c = TrigTools.cosTurns(xn);
            float s = TrigTools.sinTurns(xn);
            float x0, x1;
            x0 = c * radius0;
            y0 = s * radius0;
            x1 = c * radius1;
            y1 = s * radius1;

            float dot0 = x0 * x0 + y0 * y0;
            float dot1 = x1 * x1 + y1 * y1;
            float mul = (float)Math.sqrt((1f - dot0)/dot1);
            gradients4D[i << 2    ] = x0;
            gradients4D[i << 2 | 1] = y0;
            gradients4D[i << 2 | 2] = x1 * mul;
            gradients4D[i << 2 | 3] = y1 * mul;
//            gradients4D[i + 1 << 2    ] = -(gradients4D[i << 2    ] = x0);
//            gradients4D[i + 1 << 2 | 1] = -(gradients4D[i << 2 | 1] = y0);
//            gradients4D[i + 1 << 2 | 2] = -(gradients4D[i << 2 | 2] = x1 * mul);
//            gradients4D[i + 1 << 2 | 3] = -(gradients4D[i << 2 | 3] = y1 * mul);
//            ++i;
        }
    }


    /**
     * <a href="https://projecteuclid.org/journals/annals-of-mathematical-statistics/volume-43/issue-2/Choosing-a-Point-from-the-Surface-of-a-Sphere/10.1214/aoms/1177692644.full">Based on an algorithm by Marsaglia</a>.
     * @param random      any non-null EnhancedRandom
     * @param gradients4D the gradient vector array to write to, in groups of 4 floats per vector
     */
    private void marsagliaRandom4D(final EnhancedRandom random, final float[] gradients4D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float theta0 = random.nextExclusiveFloat();
            float radius0 = (float) Math.sqrt(random.nextExclusiveFloat());
            float theta1 = random.nextExclusiveFloat();
            float radius1 = (float) Math.sqrt(random.nextExclusiveFloat());

            float x0, x1, y0, y1;
            x0 = TrigTools.cosTurns(theta0) * radius0;
            y0 = TrigTools.sinTurns(theta0) * radius0;
            x1 = TrigTools.cosTurns(theta1) * radius1;
            y1 = TrigTools.sinTurns(theta1) * radius1;

            float dot0 = x0 * x0 + y0 * y0;
            float dot1 = x1 * x1 + y1 * y1;
            float mul = (float)Math.sqrt((1f - dot0)/dot1);
            gradients4D[i << 2    ] = x0;
            gradients4D[i << 2 | 1] = y0;
            gradients4D[i << 2 | 2] = x1 * mul;
            gradients4D[i << 2 | 3] = y1 * mul;
//            gradients4D[i + 1 << 2    ] = -(gradients4D[i << 2    ] = r * TrigTools.sinSmootherTurns(alpha));
//            gradients4D[i + 1 << 2 | 1] = -(gradients4D[i << 2 | 1] = r * TrigTools.cosSmootherTurns(alpha));
//            gradients4D[i + 1 << 2 | 2] = -(gradients4D[i << 2 | 2] = c * TrigTools.sinSmootherTurns(beta));
//            gradients4D[i + 1 << 2 | 3] = -(gradients4D[i << 2 | 3] = c * TrigTools.cosSmootherTurns(beta));
//            ++i;
        }
//        for (int i = 0; i < STANDARD_COUNT; i++) {
//            float s = i + epsilon;
//            float t = s / (STANDARD_COUNT - 1 + epsilon + epsilon);
//            float r = (float)Math.sqrt(t);
//            float c = (float)Math.sqrt(1f-t);
//            float alpha = s * a, beta = s * b;
////            gradients4D[i << 2    ] = r * TrigTools.sinSmootherTurns(alpha);
////            gradients4D[i << 2 | 1] = r * TrigTools.cosSmootherTurns(alpha);
////            gradients4D[i << 2 | 2] = c * TrigTools.sinSmootherTurns(beta);
////            gradients4D[i << 2 | 3] = c * TrigTools.cosSmootherTurns(beta);
//            gradients4D[i + 1 << 2    ] = -(gradients4D[i << 2    ] = r * TrigTools.sinSmootherTurns(alpha));
//            gradients4D[i + 1 << 2 | 1] = -(gradients4D[i << 2 | 1] = r * TrigTools.cosSmootherTurns(alpha));
//            gradients4D[i + 1 << 2 | 2] = -(gradients4D[i << 2 | 2] = c * TrigTools.sinSmootherTurns(beta));
//            gradients4D[i + 1 << 2 | 3] = -(gradients4D[i << 2 | 3] = c * TrigTools.cosSmootherTurns(beta));
//            ++i;
//        }
    }

    private void marsagliaDetermined4D(float[] gradients4D) {
        int f = 0;
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float theta0 = SHUFFLES[f++];
            float radius0 = (float) Math.sqrt(SHUFFLES[f++]);
            float theta1 = SHUFFLES[f++];
            float radius1 = (float) Math.sqrt(SHUFFLES[f++]);

            float x0, x1, y0, y1;
            x0 = TrigTools.cosTurns(theta0) * radius0;
            y0 = TrigTools.sinTurns(theta0) * radius0;
            x1 = TrigTools.cosTurns(theta1) * radius1;
            y1 = TrigTools.sinTurns(theta1) * radius1;

            float dot0 = x0 * x0 + y0 * y0;
            float dot1 = x1 * x1 + y1 * y1;
            float mul = (float) Math.sqrt((1f - dot0) / dot1);
            gradients4D[i << 2] = x0;
            gradients4D[i << 2 | 1] = y0;
            gradients4D[i << 2 | 2] = x1 * mul;
            gradients4D[i << 2 | 3] = y1 * mul;
        }
    }

    private void gaussianR4(float[] gradients4D) {
        //R4:  Min distance 0.26545448
        long x = GOLDEN_LONGS[4][0] - 0x4000000000000000L;
        long y = GOLDEN_LONGS[4][1] - 0x4000000000000000L;
        long z = GOLDEN_LONGS[4][2] - 0x4000000000000000L;
        long w = GOLDEN_LONGS[4][3] - 0x4000000000000000L;
        //R4:  Min distance 0.20926196
//        long x = GOLDEN_LONGS[4][0];
//        long y = GOLDEN_LONGS[4][1];
//        long z = GOLDEN_LONGS[4][2];
//        long w = GOLDEN_LONGS[4][3];
        //R4:  Min distance 0.14316198
//        long x = GOLDEN_LONGS[0][0];
//        long y = GOLDEN_LONGS[0][0];
//        long z = GOLDEN_LONGS[0][0];
//        long w = GOLDEN_LONGS[0][0];
        //R4:  Min distance 0.21568021
//        long x = 0x8000000000000000L;
//        long y = 0x8000000000000000L;
//        long z = 0x8000000000000000L;
//        long w = 0x8000000000000000L;
        //R4:  Min distance 0.19834335
//        long x = 0L;
//        long y = 0L;
//        long z = 0L;
//        long w = 0L;
        //R4:  Min distance 0.21798963
//        long x = 0x4000000000000000L;
//        long y = 0x4000000000000000L;
//        long z = 0x4000000000000000L;
//        long w = 0x4000000000000000L;
        double[] v4 = new double[4];

        for (int i = 0; i < STANDARD_COUNT; i++) {
            v4[0] = MathTools.probit(((x = x + GOLDEN_LONGS[3][0]) >>> 11) * 0x1p-53);
            v4[1] = MathTools.probit(((y = y + GOLDEN_LONGS[3][1]) >>> 11) * 0x1p-53);
            v4[2] = MathTools.probit(((z = z + GOLDEN_LONGS[3][2]) >>> 11) * 0x1p-53);
            v4[3] = MathTools.probit(((w = w + GOLDEN_LONGS[3][3]) >>> 11) * 0x1p-53);
            double mag = Math.sqrt(
                    v4[0] * v4[0] +
                    v4[1] * v4[1] +
                    v4[2] * v4[2] +
                    v4[3] * v4[3]);
//            gradients4D[i << 2    ] = (float)(v4[0] / mag);
//            gradients4D[i << 2 | 1] = (float)(v4[1] / mag);
//            gradients4D[i << 2 | 2] = (float)(v4[2] / mag);
//            gradients4D[i << 2 | 3] = (float)(v4[3] / mag);

            gradients4D[i + 1 << 2    ] = -(gradients4D[i << 2    ] = (float)(v4[0] / mag));
            gradients4D[i + 1 << 2 | 1] = -(gradients4D[i << 2 | 1] = (float)(v4[1] / mag));
            gradients4D[i + 1 << 2 | 2] = -(gradients4D[i << 2 | 2] = (float)(v4[2] / mag));
            gradients4D[i + 1 << 2 | 3] = -(gradients4D[i << 2 | 3] = (float)(v4[3] / mag));
            ++i;
        }
    }

    private void gaussianHalton(float[] gradients4D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            double x = MathTools.probit(QuasiRandomTools.vanDerCorputD(2, i + 1));
            double y = MathTools.probit(QuasiRandomTools.vanDerCorputD(3, i + 1));
            double z = MathTools.probit(QuasiRandomTools.vanDerCorputD(5, i + 1));
            double w = MathTools.probit(QuasiRandomTools.vanDerCorputD(7, i + 1));
            double mag = Math.sqrt(
                    x * x +
                    y * y +
                    z * z +
                    w * w);
//            gradients4D[i << 2    ] = (float)(x / mag);
//            gradients4D[i << 2 | 1] = (float)(y / mag);
//            gradients4D[i << 2 | 2] = (float)(z / mag);
//            gradients4D[i << 2 | 3] = (float)(w / mag);

            gradients4D[i + 1 << 2    ] = -(gradients4D[i << 2    ] = (float)(x / mag));
            gradients4D[i + 1 << 2 | 1] = -(gradients4D[i << 2 | 1] = (float)(y / mag));
            gradients4D[i + 1 << 2 | 2] = -(gradients4D[i << 2 | 2] = (float)(z / mag));
            gradients4D[i + 1 << 2 | 3] = -(gradients4D[i << 2 | 3] = (float)(w / mag));
            ++i;
        }
    }

    private void roll4D(final RotationTools.Rotator rotator, final float[] gradients4D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            rotator.randomize();
            rotator.rotate(SphereVisualizer.POLE_4, gradients4D, i++ << 2);
            for (int r = 0; r < 4; r++) {
                gradients4D[i << 2 | r] = -gradients4D[i - 1 << 2 | r];
            }
        }
    }

    private void roll5D(final EnhancedRandom random, final float[] gradients5D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float[] rot = RotationTools.randomRotation5D(random);
            RotationTools.rotate(SphereVisualizer.POLE_5, rot, gradients5D, i++ << 3);
//            RotationTools.rotate(SphereVisualizer.POLE_REVERSE_5, rot, gradients5D, ++i << 3);
            for (int r = 0; r < 5; r++) {
                gradients5D[i << 3 | r] = -gradients5D[i - 1 << 3 | r];
            }
        }
    }

    private static void roll5D(final EnhancedRandom random, final double[] gradients5D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            double[] rot = RotationTools.randomDoubleRotation5D(random);
            RotationTools.rotate(SphereVisualizer.POLE_5_D, rot, gradients5D, i++ << 3);
//            RotationTools.rotate(SphereVisualizer.POLE_REVERSE_5_D, rot, gradients5D, ++i << 3);
            for (int r = 0; r < 5; r++) {
                gradients5D[i << 3 | r] = -gradients5D[i - 1 << 3 | r];
            }
        }
    }

    private static void roll5D(long seed, final float[] gradients5D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float[] rot = RotationTools.randomRotation5D(seed + i);
            RotationTools.rotate(SphereVisualizer.POLE_5, rot, gradients5D, i++ << 3);
//            RotationTools.rotate(SphereVisualizer.POLE_REVERSE_5, rot, gradients5D, ++i << 3);
            for (int r = 0; r < 5; r++) {
                gradients5D[i << 3 | r] = -gradients5D[i - 1 << 3 | r];
            }
        }
    }

    private void roll5D(final RotationTools.Rotator rotator, final float[] gradients5D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            rotator.randomize();
            rotator.rotate(SphereVisualizer.POLE_5, gradients5D, i++ << 3);
//            rotator.rotate(SphereVisualizer.POLE_REVERSE_5, gradients5D, ++i << 3);
            for (int r = 0; r < 5; r++) {
                gradients5D[i << 3 | r] = -gradients5D[i - 1 << 3 | r];
            }
        }
    }

    private void roll6D(final EnhancedRandom random, final float[] gradients6D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float[] rot = RotationTools.randomRotation6D(random);
            RotationTools.rotate(SphereVisualizer.POLE_6, rot, gradients6D, i++ << 3);
//            RotationTools.rotate(SphereVisualizer.POLE_REVERSE_6, rot, gradients6D, ++i << 3);
            for (int r = 0; r < 6; r++) {
                gradients6D[i << 3 | r] = -gradients6D[i - 1 << 3 | r];
            }
        }
    }

    private static void roll6D(final EnhancedRandom random, final double[] gradients6D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            double[] rot = RotationTools.randomDoubleRotation6D(random);
            RotationTools.rotate(SphereVisualizer.POLE_6_D, rot, gradients6D, i++ << 3);
//            RotationTools.rotate(SphereVisualizer.POLE_REVERSE_6_D, rot, gradients6D, ++i << 3);
            for (int r = 0; r < 6; r++) {
                gradients6D[i << 3 | r] = -gradients6D[i - 1 << 3 | r];
            }        }
    }

    private static void roll6D(long seed, final float[] gradients6D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            float[] rot = RotationTools.randomRotation6D(seed + i);
            RotationTools.rotate(SphereVisualizer.POLE_6, rot, gradients6D, i++ << 3);
//            RotationTools.rotate(SphereVisualizer.POLE_REVERSE_6, rot, gradients6D, ++i << 3);
            for (int r = 0; r < 6; r++) {
                gradients6D[i << 3 | r] = -gradients6D[i - 1 << 3 | r];
            }        }
    }

    private void roll6D(final RotationTools.Rotator rotator, final float[] gradients6D) {
        for (int i = 0; i < STANDARD_COUNT; i++) {
            rotator.randomize();
            rotator.rotate(SphereVisualizer.POLE_6, gradients6D, i++ << 3);
//            rotator.rotate(SphereVisualizer.POLE_REVERSE_6, gradients6D, ++i << 3);
            for (int r = 0; r < 6; r++) {
                gradients6D[i << 3 | r] = -gradients6D[i - 1 << 3 | r];
            }
        }
    }

    /**
     * Computes the integral of {@code pow(sin(t), m)}, dt from 0 to x, recursively.
     * <a href="https://stackoverflow.com/a/59279721">Ported from this StackOverflow answer by user Erik</a>.
     * @param x
     * @param m
     * @return
     */
    private static float int_sin_m(float x, int m) {
        if(m == 0) return x;
        if(m == 1) return 1 - cosSmoother(x);
        return (m - 1f) / m * int_sin_m(x, m - 2) - cosSmoother(x) * (float)Math.pow(sinSmoother(x), (m - 1f) / m);
    }

    /**
     * Returns func inverse of target between lower and upper.
     * inverse is accurate to an absolute tolerance of 1E-10f, and
     * must be monotonically increasing over the interval lower to upper.
     * <a href="https://stackoverflow.com/a/59279721">Ported from this StackOverflow answer by user Erik</a>.
     * @param func
     * @param target
     * @param lower
     * @param upper
     * @return
     */
    private static float inverse_increasing(FloatToFloatFunction func, float target, float lower, float upper) {
        float atol = 1E-5f;
        float mid = (lower + upper) * 0.5f, approx = func.applyAsFloat(mid);
        while(Math.abs(approx - target) > atol) {
            if (approx > target)
                upper = mid;
            else
                lower = mid;
            mid = (upper + lower) * 0.5f;
            approx = func.applyAsFloat(mid);
        }
        return mid;
    }
    private static final float[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59};
    /**
     * <a href="https://stackoverflow.com/a/59279721">Ported from this StackOverflow answer by user Erik</a>.
     */
    private void uniformND(int d, float[] data, int block) {
        int n = data.length / block;

        for (int i = 0, b = 0; i < n; i++, b += block) {
            Arrays.fill(data, b + 2, b + d, 1f);
            float t = i / (float)n;
            data[b] = sinSmootherTurns(t);
            data[b+1] = cosSmootherTurns(t);
        }

        for (int dim = 2, gold = 0; dim < d; dim++, gold++) {
//            long offset = QuasiRandomTools.goldenLong[d-2][gold];
            float offset = (float)Math.sqrt(PRIMES[gold]);
            float mult = MathTools.gamma(dim * 0.5f + 0.5f) / MathTools.gamma(dim * 0.5f) / (float) Math.sqrt(Math.PI);
            for (int i = 0, b = 0; i < n; i++, b += block) {
                int finalDim = dim;
                float big = i * offset;
                float degree = inverse_increasing((y -> mult * int_sin_m(y, finalDim - 1)), big - (long)big, 0, PI);
                for (int j = 0; j < dim; j++)
                    data[b+j] *= sinSmoother(degree);
                data[b+dim] *= cosSmoother(degree);
            }
        }
    }

    private float evaluateMinDistance2(final float[] gradients, int dim, int blockSize) {
        float minDist2 = Float.MAX_VALUE;
        int limit = gradients.length;
        for (int i = 0; i < limit; i += blockSize) {
            for (int j = 0; j < limit; j += blockSize) {
                if(i == j) continue;
                float mag = 0f;
                for (int c = 0; c < dim; c++) {
                    float diff = gradients[i+c] - gradients[j+c];
                    mag += diff * diff;
                }
                minDist2 = Math.min(minDist2, mag);
            }
        }
        return minDist2;
    }
    private float evaluateMinDistance2_4(final float[] gradients4D, int limit) {
        float minDist2 = Float.MAX_VALUE;
        limit = Math.min(limit << 2, gradients4D.length);
        for (int i = 0; i < limit; i += 4) {
            float xi = gradients4D[i  ], yi = gradients4D[i+1], zi = gradients4D[i+2], wi = gradients4D[i+3];
            for (int j = 0; j < limit; j += 4) {
                if(i == j) continue;
                float x = xi - gradients4D[j  ], y = yi - gradients4D[j+1], z = zi - gradients4D[j+2],
                        w = wi - gradients4D[j+3];
                minDist2 = Math.min(minDist2, x * x + y * y + z * z + w * w);
            }
        }
        return minDist2;
    }
    private void printMinDistance_4(final String name, final float[] gradients4D) {
        System.out.printf("%s:  Min distance %.8f\n", name, Math.sqrt(evaluateMinDistance2(gradients4D, 4, 4)));
    }

    private float evaluateMinDistance2_5(final float[] gradients5D) {
        float minDist2 = Float.MAX_VALUE;
        for (int i = 0; i < gradients5D.length; i += 8) {
            float xi = gradients5D[i  ], yi = gradients5D[i+1], zi = gradients5D[i+2], wi = gradients5D[i+3], ui = gradients5D[i+4];
            for (int j = 0; j < gradients5D.length; j += 8) {
                if(i == j) continue;
                float x = xi - gradients5D[j  ], y = yi - gradients5D[j+1], z = zi - gradients5D[j+2],
                        w = wi - gradients5D[j+3], u = ui - gradients5D[j+4];
                minDist2 = Math.min(minDist2, x * x + y * y + z * z + w * w + u * u);
            }
        }
        return minDist2;
    }
    private void printMinDistance_5(final String name, final float[] gradients5D) {
        System.out.printf("%s:  Min distance %.8f\n", name, Math.sqrt(evaluateMinDistance2_5(gradients5D)));
    }

    private float evaluateMinDistance2_6(final float[] gradients6D) {
        float minDist2 = Float.MAX_VALUE;
        for (int i = 0; i < gradients6D.length; i += 8) {
            float xi = gradients6D[i  ], yi = gradients6D[i+1], zi = gradients6D[i+2], wi = gradients6D[i+3], ui = gradients6D[i+4], vi = gradients6D[i+5];
            for (int j = 0; j < gradients6D.length; j += 8) {
                if(i == j) continue;
                float x = xi - gradients6D[j  ], y = yi - gradients6D[j+1], z = zi - gradients6D[j+2],
                        w = wi - gradients6D[j+3], u = ui - gradients6D[j+4], v = vi - gradients6D[j+5];
                minDist2 = Math.min(minDist2, x * x + y * y + z * z + w * w + u * u + v * v);
            }
        }
        return minDist2;
    }
    private void printMinDistance_6(final String name, final float[] gradients6D) {
        System.out.printf("%s:  Min distance %.8f\n", name, Math.sqrt(evaluateMinDistance2_6(gradients6D)));
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

    private void shuffleLanes(final EnhancedRandom random) {
        float[] items = SHUFFLES;
        final int length = items.length;
        final float invLength = 1f / length;
        for (int i = 0; i < length; i++) {
            items[i] = (i + 0.5f) * invLength;
        }
        for (int i = length - 1; i > 0; i--) {
            final int j = (random.nextInt(i + 1) & -4) | (i & 3);
            final float temp = items[i];
            items[i] = items[j];
            items[j] = temp;
        }
    }

    private static void shuffleBlocks(final EnhancedRandom random, final float[] items, final int blockSize) {
        final int length = items.length / blockSize;
        for (int i = length - 1; i > 0; i--) {
            int a = i * blockSize, b = random.nextInt(i + 1) * blockSize;
            float temp;
            for (int j = 0; j < blockSize; j++) {
                temp = items[a + j];
                items[a + j] = items[b + j];
                items[b + j] = temp;
            }
        }
    }

    private static void shuffleBlocks(final EnhancedRandom random, final double[] items, final int blockSize) {
        final int length = items.length / blockSize;
        for (int i = length - 1; i > 0; i--) {
            int a = i * blockSize, b = random.nextInt(i + 1) * blockSize;
            double temp;
            for (int j = 0; j < blockSize; j++) {
                temp = items[a + j];
                items[a + j] = items[b + j];
                items[b + j] = temp;
            }
        }
    }

    private void printDeviation_5(final String name, final float[] gradients5D) {
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

    private static void normalizeAllInPlace(final float[] items, final int dim, final int blockSize) {
        final int length = items.length;
        for (int a = 0; a < length; a += blockSize) {
            float mag = 0f;
            for (int c = 0; c < dim; c++) {
                mag += items[a+c] * items[a+c];
            }
            for (int c = 0; c < dim; c++) {
                items[a+c] /= (float) Math.sqrt(mag);
            }
        }
    }

    private float nudgeAll(final float[] source, final float[] temp, final float strength,
                                 final int dim, final int blockSize) {
        float oldMinDist = evaluateMinDistance2(source, dim, blockSize);
        Arrays.fill(temp, 0f);
        final int length = source.length;
        for (int a = 0; a < length; a += blockSize) {
            System.arraycopy(source, a, temp, a, dim);
            for (int b = 0; b < length; b += blockSize) {
                if (a == b) continue;
                float mag = 0f, diff;
                for (int c = 0; c < dim; c++) {
                    diff = source[a + c] - source[b + c];
                    mag += diff * diff;
                }
                mag = strength * RoughMath.expRough(-mag);
                for (int c = 0; c < dim; c++) {
                    diff = source[a + c] - source[b + c];
                    temp[a + c] += diff * mag;
                }
            }
        }
        normalizeAllInPlace(temp, dim, blockSize);
        float nextMinDist = evaluateMinDistance2(temp, dim, blockSize);
        if (nextMinDist > oldMinDist) {
            System.arraycopy(temp, 0, source, 0, length);
            System.out.println("Current min dist: " + Math.sqrt(nextMinDist));
            return strength * 1.125f;
        }
        return strength * MathTools.GOLDEN_RATIO_INVERSE;
    }

    {
        if(true){
            System.out.println("4D STUFF\n");

//            superFibonacci4D(0.5f, GRADIENTS_4D_FIB, GOLDEN_FLOATS[4][0], GOLDEN_FLOATS[8][4]);
//            superFibonacci4D(0.5f, GRADIENTS_4D_FIB, MathTools.ROOT2, 1.533751168755204288118041f); //Min distance 0.24475130
            //Super-Fibonacci spiral (epsilon 2.0000000) with a  0.5557308793, b  0.5798825026 has min dist 0.20874050

            //Super-Fibonacci spiral (epsilon 1.0000000) with a  0.5866990089, b  0.3876041770 has min dist 0.352996
            //Super-Fibonacci spiral (epsilon 1.2941177) with a  0.5866990089, b  0.6119842529 has min dist 0.355762
            //Super-Fibonacci spiral (epsilon 1.0000000) with a  0.9469839931, b  0.5291057229 has min dist 0.365039
            //Super-Fibonacci spiral (epsilon 0.9899999) with a  1.9469840527, b  1.0291167498 has min dist 0.365202
            //Super-Fibonacci spiral (epsilon 1.7692308) with a  0.5866990089, b  0.6119842529 has min dist 0.369163
            //Super-Fibonacci spiral (epsilon 2.5000000) with a  0.6923295856, b  0.7171460986 has min dist 0.389756

//            superFibonacci4D(2f, GRADIENTS_4D_FIB, 0.5557308793f, 0.5798825026f); //Min distance 0.20874050
//            superFibonacci4D(2f, GRADIENTS_4D_FIB, 0.5557308793f, 0.5798825026f); //Min distance 0.20874050
//            superFibonacci4D(1.0000000f, GRADIENTS_4D_FIB, 0.9469839931f, 0.5291057229f); //Min distance 0.16605810
//            superFibonacci4D(0.9899999f, GRADIENTS_4D_FIB, 1.9469840527f, 1.0291167498f); //Min distance 0.18563157
//            superFibonacci4D(2.5000000f, GRADIENTS_4D_FIB, 0.6923295856f, 0.7171460986f); //Min distance 0.19518112
//            superFibonacci4D(1.0000000f, GRADIENTS_4D_FIB, 0.5557308793f, 0.5798825026f); //Min distance 0.20874050
//            superFibonacci4D(1.2941177f, GRADIENTS_4D_FIB, 0.5866990089f, 0.6119842529f); //Min distance 0.35576184
            superFibonacci4D(1.7692308f, GRADIENTS_4D_FIB, 0.5866990089f, 0.6119842529f); //Min distance 0.36916307
//            superFibonacciMarsaglia4D(0.5f, GRADIENTS_4D_SFM, MathTools.ROOT2, 1.533751168755204288118041f);
            superFibonacciMarsaglia4D(1.7692308f, GRADIENTS_4D_SFM, 0.5866990089f, 0.6119842529f);
            gaussianR4(GRADIENTS_4D_R4);
            gaussianHalton(GRADIENTS_4D_HALTON);
            marsagliaRandom4D(new AceRandom(1234567890L), GRADIENTS_4D_ACE);
            shuffleLanes(new AceRandom(1234567890L));
            marsagliaDetermined4D(GRADIENTS_4D_SHUFFLE);

//            System.out.println(Base.joinReadable(", ", GRADIENTS_4D_HALTON));
            float[] chosen = GRADIENTS_4D_R4;
            float[] haltonSum = new float[4];
            System.out.println("new float[] {");
            for (int i = 0; i <= chosen.length - 4;) {
                for (int j = 0; j < 4; j++, i++) {
                    System.out.printf("%+14.12ff, ", chosen[i]);
                    haltonSum[j] += chosen[i];
                }
                System.out.println();
            }
            System.out.println("};\n");

            System.out.printf("Sums: x=%14.12ff, y=%14.12ff, z=%14.12ff, w=%14.12ff\n", haltonSum[0], haltonSum[1], haltonSum[2], haltonSum[3]);

            printMinDistance_4("Noise", GRADIENTS_4D);
            printMinDistance_4("Fib", GRADIENTS_4D_FIB);
            printMinDistance_4("SFM", GRADIENTS_4D_SFM);
            printMinDistance_4("R4", GRADIENTS_4D_R4);
            printMinDistance_4("Halton", GRADIENTS_4D_HALTON);
            printMinDistance_4("Ace", GRADIENTS_4D_ACE);
            printMinDistance_4("Shuffle", GRADIENTS_4D_SHUFFLE);

//            System.out.println();
//            System.out.println("public static final float[] GRADIENTS_4D_FIB = {");
//            for (int i = 0; i < GRADIENTS_4D_FIB.length; i += 4) {
//                System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff,\n",
//                        GRADIENTS_4D_FIB[i], GRADIENTS_4D_FIB[i+1], GRADIENTS_4D_FIB[i+2], GRADIENTS_4D_FIB[i+3]);
//            }
//            System.out.println("};");
        }

        if(false) {
            System.out.println("5D STUFF:\n");

            for (int i = 0; i < GRADIENTS_5D.length; i++) {
                GRADIENTS_5D[i] *= 0.5f;
            }
            for (int i = 1; i <= STANDARD_COUNT; i++) {
                float x = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(3, i));
                float y = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(5, i));
                float z = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(7, i));
                float w = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(11, i));
                float u = (float) MathTools.probit(QuasiRandomTools.vanDerCorput(2, i));

//            float r = (float) Math.pow(QuasiRandomTools.vanDerCorput(2, i), 0.2);
                final float mag = 1f / (float) Math.sqrt(x * x + y * y + z * z + w * w + u * u);
                int index = i - 1 << 3;
                GRADIENTS_5D_HALTON[index + 0] = x * mag;
                GRADIENTS_5D_HALTON[index + 1] = y * mag;
                GRADIENTS_5D_HALTON[index + 2] = z * mag;
                GRADIENTS_5D_HALTON[index + 3] = w * mag;
                GRADIENTS_5D_HALTON[index + 4] = u * mag;
            }
            long xl = GOLDEN_LONGS[5][0] - 0x4000000000000000L;
            long yl = GOLDEN_LONGS[5][1] - 0x4000000000000000L;
            long zl = GOLDEN_LONGS[5][2] - 0x4000000000000000L;
            long wl = GOLDEN_LONGS[5][3] - 0x4000000000000000L;
            long ul = GOLDEN_LONGS[5][4] - 0x4000000000000000L;
            for (int i = 1; i <= STANDARD_COUNT; i++) {
                float x = (float) MathTools.probit(((xl += QuasiRandomTools.GOLDEN_LONGS[4][0]) >>> 11) * 0x1p-53);
                float y = (float) MathTools.probit(((yl += QuasiRandomTools.GOLDEN_LONGS[4][1]) >>> 11) * 0x1p-53);
                float z = (float) MathTools.probit(((zl += QuasiRandomTools.GOLDEN_LONGS[4][2]) >>> 11) * 0x1p-53);
                float w = (float) MathTools.probit(((wl += QuasiRandomTools.GOLDEN_LONGS[4][3]) >>> 11) * 0x1p-53);
                float u = (float) MathTools.probit(((ul += QuasiRandomTools.GOLDEN_LONGS[4][4]) >>> 11) * 0x1p-53);

//            float r = (float) Math.pow((QuasiRandomTools.goldenLong[5][5] * i >>> 12) * 0x1p-52, 0.2);
                final float mag = 1f / (float) Math.sqrt(x * x + y * y + z * z + w * w + u * u);
                int index = i - 1 << 3;
                GRADIENTS_5D_R5[index + 0] = x * mag;
                GRADIENTS_5D_R5[index + 1] = y * mag;
                GRADIENTS_5D_R5[index + 2] = z * mag;
                GRADIENTS_5D_R5[index + 3] = w * mag;
                GRADIENTS_5D_R5[index + 4] = u * mag;
            }

            EnhancedRandom random;
            random = new AceRandom(123456789L);
            uniformND(5, GRADIENTS_5D_TEMP, 8);
            float[] rot5 = RotationTools.randomRotation5D(123456789L);

            for (int i = 0, p = 0; i < 256; i++, p += 8) {
                RotationTools.rotate(GRADIENTS_5D_TEMP, p, 5, rot5, GRADIENTS_5D_U, p);
            }
            Arrays.fill(GRADIENTS_5D_TEMP, 0f);
            shuffleBlocks(random, GRADIENTS_5D_U, 8);

            uniformND(6, GRADIENTS_6D_U, 8);
            float[] rot6 = RotationTools.randomRotation6D(123456789L);

            for (int i = 0, p = 0; i < 256; i++, p += 8) {
                RotationTools.rotate(GRADIENTS_6D_TEMP, p, 6, rot6, GRADIENTS_6D_U, p);
            }
            Arrays.fill(GRADIENTS_6D_TEMP, 0f);
            shuffleBlocks(random, GRADIENTS_6D_U, 8);

            random = new AceRandom(0xEE36A34B8BEC3EFEL);
            roll5D(random, GRADIENTS_5D_ACE);
            shuffleBlocks(random, GRADIENTS_5D_ACE, 8);

            double[] GRADIENTS_5D_ACE_D = new double[STANDARD_COUNT << 3];
            random.setSeed(0xEE36A34B8BEC3EFEL);
            roll5D(random, GRADIENTS_5D_ACE_D);
            shuffleBlocks(random, GRADIENTS_5D_ACE_D, 8);

            random = new GoldenQuasiRandom(-1234567890L);
            roll5D(random, GRADIENTS_5D_GOLDEN);

            random = new VanDerCorputQuasiRandom(1L);
            roll5D(random, GRADIENTS_5D_VDC);

            printMinDistance_5("Noise", GRADIENTS_5D);
            printMinDistance_5("Halton", GRADIENTS_5D_HALTON);
            printMinDistance_5("R5", GRADIENTS_5D_R5);
            printMinDistance_5("Ace", GRADIENTS_5D_ACE);
            printMinDistance_5("Golden", GRADIENTS_5D_GOLDEN);
            printMinDistance_5("VDC", GRADIENTS_5D_VDC);
            printMinDistance_5("Uniform", GRADIENTS_5D_U);

            float[] chosen = GRADIENTS_5D_ACE;
            System.out.println("private static final float[] GRADIENTS_5D = {");
            for (int i = 0; i < chosen.length; i += 8) {
                System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f, 0.0f, 0.0f,\n",
                        chosen[i], chosen[i + 1], chosen[i + 2], chosen[i + 3], chosen[i + 4]);
            }
            System.out.println("};\n");

//        System.out.println("private static final float[] GRADIENTS_5D = {");
//        for (int i = 0; i < GRADIENTS_5D_ACE_D.length; i += 8) {
//            System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f, 0.0f, 0.0f,\n",
//                    GRADIENTS_5D_ACE_D[i], GRADIENTS_5D_ACE_D[i+1], GRADIENTS_5D_ACE_D[i+2], GRADIENTS_5D_ACE_D[i+3], GRADIENTS_5D_ACE_D[i+4]);
//        }
//        System.out.println("};\n");

            System.out.println("6D STUFF");

            shuffleBlocks(random, GRADIENTS_6D, 8);
            chosen = GRADIENTS_6D;
            System.out.println("public static final float[] GRADIENTS_6D = {");
            for (int i = 0; i < chosen.length; i += 8) {
                System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f, 0.0f,\n",
                        chosen[i], chosen[i + 1], chosen[i + 2], chosen[i + 3], chosen[i + 4], chosen[i + 5]);
            }
            System.out.println("};");

            printMinDistance_6("Noise", GradientVectors.GRADIENTS_6D);
            printMinDistance_6("Ace", GRADIENTS_6D);
        }

        if(false) {
            System.out.println("SHAPE_D STUFF\n");
            random.setSeed(0xEE36A34B8BEC3EFEL);
            double[] rot3 = RotationTools.randomDoubleRotation3D(random);
            double[] GRADIENTS_3D_SHAPE_D = new double[32 << 2];
            double[] rot4 = RotationTools.randomDoubleRotation4D(0xEE36A34B8BEC3EFEL);
            double[] GRADIENTS_4D_SHAPE_D = new double[64 << 2];
            double[] rot2 = RotationTools.randomDoubleRotation2D(-0xEE36A34B8BEC3EFEL);
            double[] GRADIENTS_2D_SHAPE_D = new double[256 << 1];

            double[] items = new double[4];
            // the next block was used to generate GRADIENTS_4D_D, just below it.
            if (false) {
                double big = Math.sqrt(2) + 1,
                        b = big / Math.sqrt(1.0 + big * big * 3.0),
                        s = 1.0 / Math.sqrt(1.0 + big * big * 3.0);
                System.out.println("private static final double[] GRADIENTS_4D_D = {");
                for (int i = 0; i < 64; i++) {
                    Arrays.fill(items, b);
                    items[i >>> 4] = s;
                    for (int j = 0; j < 4; j++)
                        items[j] *= -(i >>> j & 1) | 1;
                    System.out.printf("    %0+13.10f, %0+13.10f, %0+13.10f, %0+13.10f,\n", items[0], items[1], items[2], items[3]);
                }
                System.out.println("};\n");
            }
            double[] GRADIENTS_4D_D = {
                    +0.2325878195, +0.5615166683, +0.5615166683, +0.5615166683,
                    -0.2325878195, +0.5615166683, +0.5615166683, +0.5615166683,
                    +0.2325878195, -0.5615166683, +0.5615166683, +0.5615166683,
                    -0.2325878195, -0.5615166683, +0.5615166683, +0.5615166683,
                    +0.2325878195, +0.5615166683, -0.5615166683, +0.5615166683,
                    -0.2325878195, +0.5615166683, -0.5615166683, +0.5615166683,
                    +0.2325878195, -0.5615166683, -0.5615166683, +0.5615166683,
                    -0.2325878195, -0.5615166683, -0.5615166683, +0.5615166683,
                    +0.2325878195, +0.5615166683, +0.5615166683, -0.5615166683,
                    -0.2325878195, +0.5615166683, +0.5615166683, -0.5615166683,
                    +0.2325878195, -0.5615166683, +0.5615166683, -0.5615166683,
                    -0.2325878195, -0.5615166683, +0.5615166683, -0.5615166683,
                    +0.2325878195, +0.5615166683, -0.5615166683, -0.5615166683,
                    -0.2325878195, +0.5615166683, -0.5615166683, -0.5615166683,
                    +0.2325878195, -0.5615166683, -0.5615166683, -0.5615166683,
                    -0.2325878195, -0.5615166683, -0.5615166683, -0.5615166683,
                    +0.5615166683, +0.2325878195, +0.5615166683, +0.5615166683,
                    -0.5615166683, +0.2325878195, +0.5615166683, +0.5615166683,
                    +0.5615166683, -0.2325878195, +0.5615166683, +0.5615166683,
                    -0.5615166683, -0.2325878195, +0.5615166683, +0.5615166683,
                    +0.5615166683, +0.2325878195, -0.5615166683, +0.5615166683,
                    -0.5615166683, +0.2325878195, -0.5615166683, +0.5615166683,
                    +0.5615166683, -0.2325878195, -0.5615166683, +0.5615166683,
                    -0.5615166683, -0.2325878195, -0.5615166683, +0.5615166683,
                    +0.5615166683, +0.2325878195, +0.5615166683, -0.5615166683,
                    -0.5615166683, +0.2325878195, +0.5615166683, -0.5615166683,
                    +0.5615166683, -0.2325878195, +0.5615166683, -0.5615166683,
                    -0.5615166683, -0.2325878195, +0.5615166683, -0.5615166683,
                    +0.5615166683, +0.2325878195, -0.5615166683, -0.5615166683,
                    -0.5615166683, +0.2325878195, -0.5615166683, -0.5615166683,
                    +0.5615166683, -0.2325878195, -0.5615166683, -0.5615166683,
                    -0.5615166683, -0.2325878195, -0.5615166683, -0.5615166683,
                    +0.5615166683, +0.5615166683, +0.2325878195, +0.5615166683,
                    -0.5615166683, +0.5615166683, +0.2325878195, +0.5615166683,
                    +0.5615166683, -0.5615166683, +0.2325878195, +0.5615166683,
                    -0.5615166683, -0.5615166683, +0.2325878195, +0.5615166683,
                    +0.5615166683, +0.5615166683, -0.2325878195, +0.5615166683,
                    -0.5615166683, +0.5615166683, -0.2325878195, +0.5615166683,
                    +0.5615166683, -0.5615166683, -0.2325878195, +0.5615166683,
                    -0.5615166683, -0.5615166683, -0.2325878195, +0.5615166683,
                    +0.5615166683, +0.5615166683, +0.2325878195, -0.5615166683,
                    -0.5615166683, +0.5615166683, +0.2325878195, -0.5615166683,
                    +0.5615166683, -0.5615166683, +0.2325878195, -0.5615166683,
                    -0.5615166683, -0.5615166683, +0.2325878195, -0.5615166683,
                    +0.5615166683, +0.5615166683, -0.2325878195, -0.5615166683,
                    -0.5615166683, +0.5615166683, -0.2325878195, -0.5615166683,
                    +0.5615166683, -0.5615166683, -0.2325878195, -0.5615166683,
                    -0.5615166683, -0.5615166683, -0.2325878195, -0.5615166683,
                    +0.5615166683, +0.5615166683, +0.5615166683, +0.2325878195,
                    -0.5615166683, +0.5615166683, +0.5615166683, +0.2325878195,
                    +0.5615166683, -0.5615166683, +0.5615166683, +0.2325878195,
                    -0.5615166683, -0.5615166683, +0.5615166683, +0.2325878195,
                    +0.5615166683, +0.5615166683, -0.5615166683, +0.2325878195,
                    -0.5615166683, +0.5615166683, -0.5615166683, +0.2325878195,
                    +0.5615166683, -0.5615166683, -0.5615166683, +0.2325878195,
                    -0.5615166683, -0.5615166683, -0.5615166683, +0.2325878195,
                    +0.5615166683, +0.5615166683, +0.5615166683, -0.2325878195,
                    -0.5615166683, +0.5615166683, +0.5615166683, -0.2325878195,
                    +0.5615166683, -0.5615166683, +0.5615166683, -0.2325878195,
                    -0.5615166683, -0.5615166683, +0.5615166683, -0.2325878195,
                    +0.5615166683, +0.5615166683, -0.5615166683, -0.2325878195,
                    -0.5615166683, +0.5615166683, -0.5615166683, -0.2325878195,
                    +0.5615166683, -0.5615166683, -0.5615166683, -0.2325878195,
                    -0.5615166683, -0.5615166683, -0.5615166683, -0.2325878195,
            };

            int p = 0;

            for (int i = 0; i < ShapeTools.UNIT_DODECAHEDRON_VERTICES_D.length; i++, p += 4) {
                RotationTools.rotate(ShapeTools.UNIT_DODECAHEDRON_VERTICES_D[i], rot3, GRADIENTS_3D_SHAPE_D, p);
            }
            for (int i = 0; i < ShapeTools.UNIT_ICOSAHEDRON_VERTICES_D.length; i++, p += 4) {
                RotationTools.rotate(ShapeTools.UNIT_ICOSAHEDRON_VERTICES_D[i], rot3, GRADIENTS_3D_SHAPE_D, p);
            }

            p = 0;

            for (; p < GRADIENTS_4D_D.length; p += 4) {
                RotationTools.rotate(GRADIENTS_4D_D, p, 4, rot4, GRADIENTS_4D_SHAPE_D, p);
            }

            p = 0;

            for (; p < GRADIENTS_2D_SHAPE_D.length; p += 2) {
                items[0] = SIN_TABLE_D[p << 5];
                items[1] = SIN_TABLE_D[(p << 5) + SIN_TO_COS & TABLE_MASK];
                RotationTools.rotate(items, 0, 2, rot2, GRADIENTS_2D_SHAPE_D, p);
            }
            System.out.println("private static final float[] GRADIENTS_2D = {");
            for (int i = 0; i < GRADIENTS_2D_SHAPE_D.length; i += 2) {
                System.out.printf("    %0+13.10ff, %0+13.10ff,\n",
                        GRADIENTS_2D_SHAPE_D[i], GRADIENTS_2D_SHAPE_D[i + 1]);
            }
            System.out.println("};\n");

            System.out.println("private static final float[] GRADIENTS_3D = {");
            for (int b = 0; b < 8; b++) {
                shuffleBlocks(random, GRADIENTS_3D_SHAPE_D, 4);
                for (int i = 0; i < GRADIENTS_3D_SHAPE_D.length; i += 4) {
                    System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, 0.0f,\n",
                            GRADIENTS_3D_SHAPE_D[i], GRADIENTS_3D_SHAPE_D[i + 1], GRADIENTS_3D_SHAPE_D[i + 2]);
                }
            }
            System.out.println("};\n");

            System.out.println("private static final float[] GRADIENTS_4D = {");
            for (int b = 0; b < 4; b++) {
                shuffleBlocks(random, GRADIENTS_4D_SHAPE_D, 4);
                for (int i = 0; i < GRADIENTS_4D_SHAPE_D.length; i += 4) {
                    System.out.printf("    %0+13.10ff, %0+13.10ff, %0+13.10ff, %0+13.10ff,\n",
                            GRADIENTS_4D_SHAPE_D[i], GRADIENTS_4D_SHAPE_D[i + 1], GRADIENTS_4D_SHAPE_D[i + 2], GRADIENTS_4D_SHAPE_D[i + 3]);
                }
            }
            System.out.println("};\n");
        }
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

/*
Best (subjective) GRADIENTS_4D_ACE so far:

Best seed: 0x602A265ECCC25817L with best min dist 0.375867
Processing 100000 spheres took 6.085 seconds.
public static final float[] GRADIENTS_4D_ACE = {
    -0.2410883307f, +0.6241628528f, -0.4119548500f, -0.6185387969f,
    +0.2410883307f, -0.6241628528f, +0.4119548500f, +0.6185387969f,
    +0.5105605125f, -0.3904691041f, +0.7273227572f, -0.2405483723f,
    -0.5105605125f, +0.3904691041f, -0.7273227572f, +0.2405483723f,
    +0.0195002556f, +0.4135105014f, +0.8058666587f, +0.4233288467f,
    -0.0195002556f, -0.4135105014f, -0.8058666587f, -0.4233288467f,
    +0.9123296738f, -0.2057872415f, +0.3517907262f, +0.0393594541f,
    -0.9123296738f, +0.2057872415f, -0.3517907262f, -0.0393594541f,
    +0.2955628633f, +0.2395007610f, -0.6681082845f, +0.6394629478f,
    -0.2955628633f, -0.2395007610f, +0.6681082845f, -0.6394629478f,
    -0.1955433488f, -0.8000470400f, -0.2307149172f, -0.5181295276f,
    +0.1955433488f, +0.8000470400f, +0.2307149172f, +0.5181295276f,
    -0.0732512474f, +0.2834655643f, -0.0874544680f, -0.9521729946f,
    +0.0732512474f, -0.2834655643f, +0.0874544680f, +0.9521729946f,
    +0.0070208311f, -0.1552237272f, -0.2048036307f, -0.9663909674f,
    -0.0070208311f, +0.1552237272f, +0.2048036307f, +0.9663909674f,
    +0.2946332693f, +0.4637235999f, +0.5240280628f, -0.6508040428f,
    -0.2946332693f, -0.4637235999f, -0.5240280628f, +0.6508040428f,
    +0.0939733982f, +0.9935950041f, -0.0034503639f, -0.0626566857f,
    -0.0939733982f, -0.9935950041f, +0.0034503639f, +0.0626566857f,
    +0.8102910519f, -0.1407626569f, +0.4879997373f, -0.2923543453f,
    -0.8102910519f, +0.1407626569f, -0.4879997373f, +0.2923543453f,
    -0.3768132925f, -0.3133633137f, -0.2657212913f, -0.8301851749f,
    +0.3768132925f, +0.3133633137f, +0.2657212913f, +0.8301851749f,
    -0.8089053035f, +0.5371679068f, +0.2380389273f, +0.0214518756f,
    +0.8089053035f, -0.5371679068f, -0.2380389273f, -0.0214518756f,
    +0.1506042480f, +0.2018570006f, +0.6485483646f, +0.7183016539f,
    -0.1506042480f, -0.2018570006f, -0.6485483646f, -0.7183016539f,
    -0.5776713490f, +0.0253073275f, +0.0301681757f, +0.8153192997f,
    +0.5776713490f, -0.0253073275f, -0.0301681757f, -0.8153192997f,
    -0.4467359781f, -0.5803612471f, +0.5620483756f, -0.3843301535f,
    +0.4467359781f, +0.5803612471f, -0.5620483756f, +0.3843301535f,
    +0.4521242380f, -0.0857864395f, +0.2184989750f, +0.8605129719f,
    -0.4521242380f, +0.0857864395f, -0.2184989750f, -0.8605129719f,
    -0.4719672203f, +0.5449662805f, +0.6656764150f, +0.1927008033f,
    +0.4719672203f, -0.5449662805f, -0.6656764150f, -0.1927008033f,
    +0.1411199570f, +0.8874712586f, -0.3470917940f, -0.2683414817f,
    -0.1411199570f, -0.8874712586f, +0.3470917940f, +0.2683414817f,
    -0.6497822404f, -0.6565487385f, -0.3343784809f, +0.1868629456f,
    +0.6497822404f, +0.6565487385f, +0.3343784809f, -0.1868629456f,
    +0.3221840858f, -0.2436508834f, -0.8889493942f, +0.2158715874f,
    -0.3221840858f, +0.2436508834f, +0.8889493942f, -0.2158715874f,
    -0.4879500866f, +0.7362421155f, +0.2885802388f, +0.3695588410f,
    +0.4879500866f, -0.7362421155f, -0.2885802388f, -0.3695588410f,
    +0.4888226986f, +0.5481864214f, +0.5778125525f, +0.3559161425f,
    -0.4888226986f, -0.5481864214f, -0.5778125525f, -0.3559161425f,
    -0.1265583038f, +0.2364271581f, -0.9179801941f, +0.2922283709f,
    +0.1265583038f, -0.2364271581f, +0.9179801941f, -0.2922283709f,
    +0.5391604900f, +0.1442532986f, +0.8131962419f, +0.1649512053f,
    -0.5391604900f, -0.1442532986f, -0.8131962419f, -0.1649512053f,
    +0.5721789598f, +0.3681834340f, -0.5486941934f, -0.4857851267f,
    -0.5721789598f, -0.3681834340f, +0.5486941934f, +0.4857851267f,
    +0.4616726637f, -0.3690340817f, -0.1994670779f, +0.7815908194f,
    -0.4616726637f, +0.3690340817f, +0.1994670779f, -0.7815908194f,
    -0.2715918422f, -0.1369470954f, +0.0518610068f, -0.9512064457f,
    +0.2715918422f, +0.1369470954f, -0.0518610068f, +0.9512064457f,
    +0.0224583149f, -0.2110642642f, +0.9367582202f, +0.2782652378f,
    -0.0224583149f, +0.2110642642f, -0.9367582202f, -0.2782652378f,
    -0.2700558901f, -0.3444294035f, -0.8989406228f, -0.0185411498f,
    +0.2700558901f, +0.3444294035f, +0.8989406228f, +0.0185411498f,
    -0.8249325752f, -0.5516284108f, +0.0847543404f, +0.0894937068f,
    +0.8249325752f, +0.5516284108f, -0.0847543404f, -0.0894937068f,
    -0.6695151329f, -0.5100330710f, +0.1078759283f, +0.5291299224f,
    +0.6695151329f, +0.5100330710f, -0.1078759283f, -0.5291299224f,
    +0.8223609924f, +0.5330395103f, +0.1540094912f, +0.1259836853f,
    -0.8223609924f, -0.5330395103f, -0.1540094912f, -0.1259836853f,
    -0.4382519126f, -0.0533134043f, +0.2273128629f, -0.8679986000f,
    +0.4382519126f, +0.0533134043f, -0.2273128629f, +0.8679986000f,
    -0.7842869759f, +0.0177071374f, +0.6180903912f, +0.0504442416f,
    +0.7842869759f, -0.0177071374f, -0.6180903912f, -0.0504442416f,
    +0.3306212425f, -0.2690672278f, +0.6598255634f, +0.6188071966f,
    -0.3306212425f, +0.2690672278f, -0.6598255634f, -0.6188071966f,
    +0.7709085941f, -0.5954983830f, -0.0614969283f, -0.2174849510f,
    -0.7709085941f, +0.5954983830f, +0.0614969283f, +0.2174849510f,
    +0.8566231728f, +0.1904136688f, -0.3716481030f, -0.3030131459f,
    -0.8566231728f, -0.1904136688f, +0.3716481030f, +0.3030131459f,
    +0.6766525507f, +0.1654123366f, -0.2456504256f, +0.6741187572f,
    -0.6766525507f, -0.1654123366f, +0.2456504256f, -0.6741187572f,
    -0.6779673696f, +0.4852181077f, +0.4051004648f, +0.3752562106f,
    +0.6779673696f, -0.4852181077f, -0.4051004648f, -0.3752562106f,
    +0.2198252678f, +0.6927061081f, +0.6690646410f, +0.1555228531f,
    -0.2198252678f, -0.6927061081f, -0.6690646410f, -0.1555228531f,
    -0.8095942736f, -0.5430033207f, -0.1552225649f, -0.1600328386f,
    +0.8095942736f, +0.5430033207f, +0.1552225649f, +0.1600328386f,
    -0.2295383215f, -0.2479131669f, -0.9388971329f, +0.0657506138f,
    +0.2295383215f, +0.2479131669f, +0.9388971329f, -0.0657506138f,
    +0.9471589327f, +0.2485488355f, +0.0684203357f, -0.1908730268f,
    -0.9471589327f, -0.2485488355f, -0.0684203357f, +0.1908730268f,
    +0.1655160189f, -0.6968725920f, -0.5854620934f, -0.3797460198f,
    -0.1655160189f, +0.6968725920f, +0.5854620934f, +0.3797460198f,
    -0.7021883726f, +0.1938655823f, -0.4614547789f, -0.5063666105f,
    +0.7021883726f, -0.1938655823f, +0.4614547789f, +0.5063666105f,
    +0.3798977137f, -0.7668240666f, -0.2514968514f, -0.4521150589f,
    -0.3798977137f, +0.7668240666f, +0.2514968514f, +0.4521150589f,
    -0.5538936257f, +0.0539919883f, +0.3787168562f, +0.7394999862f,
    +0.5538936257f, -0.0539919883f, -0.3787168562f, -0.7394999862f,
    -0.6135616302f, -0.5841299891f, -0.5305010080f, -0.0300465226f,
    +0.6135616302f, +0.5841299891f, +0.5305010080f, +0.0300465226f,
    -0.6585783958f, +0.1703124791f, +0.7315702438f, -0.0455310494f,
    +0.6585783958f, -0.1703124791f, -0.7315702438f, +0.0455310494f,
    +0.2619884014f, +0.3242768943f, -0.7994838953f, +0.4324712753f,
    -0.2619884014f, -0.3242768943f, +0.7994838953f, -0.4324712753f,
    +0.4722200632f, +0.8409760594f, +0.1305944473f, +0.2295920104f,
    -0.4722200632f, -0.8409760594f, -0.1305944473f, -0.2295920104f,
    -0.9428711534f, -0.2076089233f, -0.2580037117f, -0.0364231020f,
    +0.9428711534f, +0.2076089233f, +0.2580037117f, +0.0364231020f,
    -0.1557283401f, +0.6237305999f, +0.7282173038f, +0.2375045717f,
    +0.1557283401f, -0.6237305999f, -0.7282173038f, -0.2375045717f,
    +0.3754519224f, -0.1017524227f, -0.8792994618f, -0.2747997642f,
    -0.3754519224f, +0.1017524227f, +0.8792994618f, +0.2747997642f,
    -0.5001568794f, -0.6252747178f, -0.0815520659f, -0.5934841633f,
    +0.5001568794f, +0.6252747178f, +0.0815520659f, +0.5934841633f,
    -0.3584417701f, +0.0256341249f, -0.8682237267f, -0.3421255052f,
    +0.3584417701f, -0.0256341249f, +0.8682237267f, +0.3421255052f,
    +0.0947760344f, +0.3073211908f, +0.7623887658f, +0.5615463853f,
    -0.0947760344f, -0.3073211908f, -0.7623887658f, -0.5615463853f,
    +0.8708484173f, -0.1382642537f, -0.4092732668f, +0.2345229387f,
    -0.8708484173f, +0.1382642537f, +0.4092732668f, -0.2345229387f,
    -0.1879315972f, -0.8952748775f, -0.3952164948f, +0.0834790468f,
    +0.1879315972f, +0.8952748775f, +0.3952164948f, -0.0834790468f,
    +0.3518196344f, +0.7419997454f, +0.5676893592f, -0.0582079440f,
    -0.3518196344f, -0.7419997454f, -0.5676893592f, +0.0582079440f,
    +0.0151752234f, -0.9315510392f, -0.2384975553f, +0.2740464211f,
    -0.0151752234f, +0.9315510392f, +0.2384975553f, -0.2740464211f,
    +0.0340323448f, -0.5385839343f, -0.6959106922f, -0.4737903476f,
    -0.0340323448f, +0.5385839343f, +0.6959106922f, +0.4737903476f,
    +0.6302640438f, -0.7695797682f, +0.0270363986f, -0.0989091992f,
    -0.6302640438f, +0.7695797682f, -0.0270363986f, +0.0989091992f,
    -0.0074661374f, -0.2946998477f, -0.0117766298f, +0.9554881454f,
    +0.0074661374f, +0.2946998477f, +0.0117766298f, -0.9554881454f,
    -0.0640318990f, +0.0183299184f, -0.3019762635f, -0.9509860277f,
    +0.0640318990f, -0.0183299184f, +0.3019762635f, +0.9509860277f,
    +0.1871662140f, -0.3740997910f, +0.9076427817f, -0.0346829891f,
    -0.1871662140f, +0.3740997910f, -0.9076427817f, +0.0346829891f,
    -0.1149399877f, +0.5368647575f, +0.8346890807f, +0.0431139618f,
    +0.1149399877f, -0.5368647575f, -0.8346890807f, -0.0431139618f,
    -0.3601332903f, +0.7450630665f, +0.0332064703f, -0.5604302883f,
    +0.3601332903f, -0.7450630665f, -0.0332064703f, +0.5604302883f,
    +0.5816476345f, +0.7479624152f, +0.2747582197f, -0.1635430455f,
    -0.5816476345f, -0.7479624152f, -0.2747582197f, +0.1635430455f,
    +0.6633393764f, -0.2711504102f, +0.6846160293f, -0.1332643330f,
    -0.6633393764f, +0.2711504102f, -0.6846160293f, +0.1332643330f,
    -0.2257308364f, +0.2439015508f, -0.8724956512f, -0.3582020700f,
    +0.2257308364f, -0.2439015508f, +0.8724956512f, +0.3582020700f,
    -0.0617989898f, +0.6504172683f, +0.6610705256f, +0.3689497113f,
    +0.0617989898f, -0.6504172683f, -0.6610705256f, -0.3689497113f,
    +0.0184791088f, -0.6535685062f, +0.3291111290f, -0.6813169122f,
    -0.0184791088f, +0.6535685062f, -0.3291111290f, +0.6813169122f,
    +0.0087559223f, +0.3366486728f, +0.1264770627f, +0.9330563545f,
    -0.0087559223f, -0.3366486728f, -0.1264770627f, -0.9330563545f,
    +0.0492548943f, -0.7363297939f, -0.1921954453f, -0.6468797922f,
    -0.0492548943f, +0.7363297939f, +0.1921954453f, +0.6468797922f,
    -0.8160794973f, -0.3467327952f, -0.1229041815f, -0.4457412064f,
    +0.8160794973f, +0.3467327952f, +0.1229041815f, +0.4457412064f,
    -0.4737885594f, -0.4579929709f, -0.7169439793f, -0.2275047004f,
    +0.4737885594f, +0.4579929709f, +0.7169439793f, +0.2275047004f,
    -0.8892623186f, -0.3435915709f, -0.2996621728f, +0.0368778706f,
    +0.8892623186f, +0.3435915709f, +0.2996621728f, -0.0368778706f,
    +0.4042479992f, +0.6346157789f, -0.2978903055f, +0.5874587893f,
    -0.4042479992f, -0.6346157789f, +0.2978903055f, -0.5874587893f,
    +0.8994406462f, +0.2609256804f, -0.3147862554f, -0.1543806642f,
    -0.8994406462f, -0.2609256804f, +0.3147862554f, +0.1543806642f,
    +0.1915230751f, -0.1609118432f, +0.0997104645f, +0.9630596638f,
    -0.1915230751f, +0.1609118432f, -0.0997104645f, -0.9630596638f,
    +0.6952043772f, +0.1678513885f, +0.1627946645f, +0.6797164679f,
    -0.6952043772f, -0.1678513885f, -0.1627946645f, -0.6797164679f,
    -0.5321875215f, -0.3127285540f, +0.7502572536f, +0.2368362248f,
    +0.5321875215f, +0.3127285540f, -0.7502572536f, -0.2368362248f,
    +0.1803593636f, +0.5352531672f, +0.4253971875f, -0.7071149945f,
    -0.1803593636f, -0.5352531672f, -0.4253971875f, +0.7071149945f,
    +0.1215845346f, -0.1033571661f, -0.5432291031f, -0.8242795467f,
    -0.1215845346f, +0.1033571661f, +0.5432291031f, +0.8242795467f,
    +0.3408499956f, -0.2358310074f, -0.4674710333f, +0.7808170319f,
    -0.3408499956f, +0.2358310074f, +0.4674710333f, -0.7808170319f,
    -0.4921096563f, +0.1308582723f, +0.5055025816f, +0.6965425014f,
    +0.4921096563f, -0.1308582723f, -0.5055025816f, -0.6965425014f,
    +0.8688088655f, -0.1778717339f, -0.3940700293f, +0.2413325757f,
    -0.8688088655f, +0.1778717339f, +0.3940700293f, -0.2413325757f,
    -0.1640503407f, +0.6962891817f, -0.3710775375f, +0.5920898318f,
    +0.1640503407f, -0.6962891817f, +0.3710775375f, -0.5920898318f,
    -0.7608257532f, +0.4404744804f, +0.2153461128f, -0.4251500368f,
    +0.7608257532f, -0.4404744804f, -0.2153461128f, +0.4251500368f,
    +0.1663640738f, -0.4360108972f, +0.8839678764f, +0.0286064148f,
    -0.1663640738f, +0.4360108972f, -0.8839678764f, -0.0286064148f,
    +0.5438162088f, -0.3054629266f, +0.5036128163f, +0.5977714658f,
    -0.5438162088f, +0.3054629266f, -0.5036128163f, -0.5977714658f,
    -0.5272291899f, +0.3251218796f, +0.2645097375f, -0.7391615510f,
    +0.5272291899f, -0.3251218796f, -0.2645097375f, +0.7391615510f,
    -0.2531236410f, -0.2957711220f, +0.6660848260f, +0.6362223029f,
    +0.2531236410f, +0.2957711220f, -0.6660848260f, -0.6362223029f,
    +0.3033279181f, -0.6361079812f, -0.7057372928f, -0.0727570206f,
    -0.3033279181f, +0.6361079812f, +0.7057372928f, +0.0727570206f,
    +0.4546587467f, -0.3833289742f, -0.7755138278f, -0.2119490802f,
    -0.4546587467f, +0.3833289742f, +0.7755138278f, +0.2119490802f,
    -0.1165118217f, +0.0236030594f, -0.8129264116f, +0.5701038837f,
    +0.1165118217f, -0.0236030594f, +0.8129264116f, -0.5701038837f,
    +0.5974810123f, +0.3711873293f, -0.5217024684f, +0.4827659130f,
    -0.5974810123f, -0.3711873293f, +0.5217024684f, -0.4827659130f,
    -0.4664940238f, +0.8524218202f, +0.1046456695f, +0.2116822004f,
    +0.4664940238f, -0.8524218202f, -0.1046456695f, -0.2116822004f,
    -0.8265207410f, -0.4136629999f, +0.3817452788f, -0.0041166544f,
    +0.8265207410f, +0.4136629999f, -0.3817452788f, +0.0041166544f,
    -0.7141373754f, -0.2687844038f, -0.4646265805f, +0.4493160546f,
    +0.7141373754f, +0.2687844038f, +0.4646265805f, -0.4493160546f,
    -0.3981459141f, -0.7304103374f, -0.3687242866f, +0.4147563279f,
    +0.3981459141f, +0.7304103374f, +0.3687242866f, -0.4147563279f,
    -0.2273231745f, -0.6578984261f, -0.6320225000f, +0.3406478167f,
    +0.2273231745f, +0.6578984261f, +0.6320225000f, -0.3406478167f,
    +0.0305466652f, +0.4381690025f, +0.8026599288f, -0.4034993351f,
    -0.0305466652f, -0.4381690025f, -0.8026599288f, +0.4034993351f,
    +0.1527588367f, -0.4794099033f, +0.7899309397f, -0.3504853845f,
    -0.1527588367f, +0.4794099033f, -0.7899309397f, +0.3504853845f,
    -0.6166512370f, +0.1771474779f, +0.1810778677f, -0.7453663945f,
    +0.6166512370f, -0.1771474779f, -0.1810778677f, +0.7453663945f,
    +0.3524242640f, -0.7607346177f, -0.5343871117f, -0.1072857082f,
    -0.3524242640f, +0.7607346177f, +0.5343871117f, +0.1072857082f,
    +0.2941534519f, -0.7869092226f, +0.1958580315f, +0.5058528185f,
    -0.2941534519f, +0.7869092226f, -0.1958580315f, -0.5058528185f,
    +0.1616976261f, -0.4778105021f, +0.0467376038f, +0.8621866703f,
    -0.1616976261f, +0.4778105021f, -0.0467376038f, -0.8621866703f,
    -0.1934111118f, +0.1618759036f, -0.9268950224f, -0.2779457569f,
    +0.1934111118f, -0.1618759036f, +0.9268950224f, +0.2779457569f,
    -0.8277069330f, -0.5319733620f, +0.0436573178f, -0.1732037812f,
    +0.8277069330f, +0.5319733620f, -0.0436573178f, +0.1732037812f,
    -0.2682933211f, +0.0742803067f, -0.9590982199f, +0.0512968451f,
    +0.2682933211f, -0.0742803067f, +0.9590982199f, -0.0512968451f,
    +0.1095248461f, -0.7042925954f, +0.1229474321f, +0.6905505657f,
    -0.1095248461f, +0.7042925954f, -0.1229474321f, -0.6905505657f,
    -0.7519147396f, -0.5244042873f, +0.0625891536f, -0.3945971131f,
    +0.7519147396f, +0.5244042873f, -0.0625891536f, +0.3945971131f,
    +0.1808984280f, +0.4796522260f, +0.6718333364f, +0.5346484780f,
    -0.1808984280f, -0.4796522260f, -0.6718333364f, -0.5346484780f,
    +0.0946251154f, -0.6945248842f, -0.6832249761f, +0.2046578079f,
    -0.0946251154f, +0.6945248842f, +0.6832249761f, -0.2046578079f,
    -0.1465639472f, +0.7833525538f, -0.3355035782f, -0.5023099184f,
    +0.1465639472f, -0.7833525538f, +0.3355035782f, +0.5023099184f,
    -0.0070763230f, -0.5364752412f, -0.5565795898f, +0.6343212128f,
    +0.0070763230f, +0.5364752412f, +0.5565795898f, -0.6343212128f,
    +0.7598570585f, -0.2476820350f, -0.6001634598f, -0.0327890292f,
    -0.7598570585f, +0.2476820350f, +0.6001634598f, +0.0327890292f,
    +0.4232975245f, +0.6355159283f, +0.3527187109f, +0.5408586264f,
    -0.4232975245f, -0.6355159283f, -0.3527187109f, -0.5408586264f,
    +0.6043568850f, +0.5365307927f, -0.3044735789f, +0.5041661263f,
    -0.6043568850f, -0.5365307927f, +0.3044735789f, -0.5041661263f,
    +0.0935821533f, +0.6487461329f, -0.4574256837f, -0.6009427905f,
    -0.0935821533f, -0.6487461329f, +0.4574256837f, +0.6009427905f,
    +0.8565416336f, -0.2517884076f, +0.4060113132f, +0.1951774657f,
    -0.8565416336f, +0.2517884076f, -0.4060113132f, -0.1951774657f,
    -0.7750592828f, -0.1616514623f, +0.4944065809f, -0.3587673008f,
    +0.7750592828f, +0.1616514623f, -0.4944065809f, +0.3587673008f,
    -0.5032155514f, -0.0314115584f, -0.1666877717f, +0.8473501205f,
    +0.5032155514f, +0.0314115584f, +0.1666877717f, -0.8473501205f,
    -0.3929902911f, -0.6120955348f, -0.2224575877f, -0.6491613388f,
    +0.3929902911f, +0.6120955348f, +0.2224575877f, +0.6491613388f,
    -0.0633620620f, +0.4776667953f, +0.4076049924f, -0.7756787539f,
    +0.0633620620f, -0.4776667953f, -0.4076049924f, +0.7756787539f,
};

 */