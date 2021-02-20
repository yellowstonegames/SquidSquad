package com.github.yellowstonegames.core;

import java.util.Iterator;
import java.util.List;

import static com.github.tommyettinger.ds.support.BitConversion.doubleToMixedIntBits;
import static com.github.tommyettinger.ds.support.BitConversion.floatToRawIntBits;

/**
 * 64-bit and 32-bit hashing functions that we can rely on staying the same cross-platform.
 * This uses the "Curlup" algorithm, which was designed for speed and general-purpose
 * usability, but not cryptographic security. It can take advantage of parallel pipelines
 * on modern processor cores, and passes the stringent SMHasher test battery. Some specific
 * types of input (long arrays, CharSequences, and Iterables) use "WheatHash" instead,
 * which is a variant on "Water" or wyhash that takes 64-bit inputs and produces 64-bit
 * output; it also passes SMHasher.
 * <br>
 * This provides an object-based API and a static API, where a Hasher object is
 * instantiated with a seed, and the static methods take a seed as their first argument.
 * The hashes this returns are always 0 when given null to hash. Arrays with
 * identical elements of identical types will hash identically. Arrays with identical
 * numerical values but different types will sometimes hash differently. This class
 * always provides 64-bit hashes via hash64() and 32-bit hashes via hash().
 * The hash64() and hash() methods use 64-bit math even when producing
 * 32-bit hashes, for GWT reasons. GWT doesn't have the same behavior as desktop and
 * Android applications when using ints because it treats ints mostly like doubles,
 * sometimes, due to it using JavaScript. If we use mainly longs, though, GWT emulates
 * the longs with a more complex technique behind-the-scenes, that behaves the same on
 * the web as it does on desktop or on a phone. Since CrossHash is supposed to be stable
 * cross-platform, this is the way we need to go, despite it being slightly slower.
 * <br>
 * This class also provides static {@link #determine(long)} and {@link #randomize(long)}
 * methods, which are unary hashes (hashes of one item, a number) with variants such as
 * {@link #determineBounded(long, int)} and {@link #randomizeFloat(long)}. The determine()
 * methods are faster but more sensitive to patterns in their input; they are meant to
 * work well on sequential inputs, like 1, 2, 3, etc. The randomize() methods are
 * more-involved, but should be able to handle any kind of input pattern while returning]
 * random results.
 * @author Tommy Ettinger
 */
public class Hasher {
    private final long seed;

    public Hasher() {
        this.seed = 0xC4CEB9FE1A85EC53L;
    }

    public Hasher(long seed) {
        this.seed = randomize(seed);
    }

    /**
     * Fast static randomizing method that takes its state as a parameter; state is expected to change between calls to
     * this. It is recommended that you use {@code DiverRNG.determine(++state)} or {@code DiverRNG.determine(--state)}
     * to produce a sequence of different numbers, and you may have slightly worse quality with increments or decrements
     * other than 1. All longs are accepted by this method, and all longs can be produced; unlike several other classes'
     * determine() methods, passing 0 here does not return 0.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long determine(long state) {
        return (state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * High-quality static randomizing method that takes its state as a parameter; state is expected to change between
     * calls to this. It is suggested that you use {@code DiverRNG.randomize(++state)} or
     * {@code DiverRNG.randomize(--state)} to produce a sequence of different numbers, but any increments are allowed
     * (even-number increments won't be able to produce all outputs, but their quality will be fine for the numbers they
     * can produce). All longs are accepted by this method, and all longs can be produced; unlike several other classes'
     * determine() methods, passing 0 here does not return 0.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize(long state) {
        return (state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28;
    }

    /**
     * Fast static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is recommended that you
     * use {@code DiverRNG.determineBounded(++state, bound)} or {@code DiverRNG.determineBounded(--state, bound)} to
     * produce a sequence of different numbers. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, int bound) {
        return (bound = (int) ((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * High-quality static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is suggested that you
     * use {@code DiverRNG.randomizeBounded(++state)} or {@code DiverRNG.randomize(--state)} to produce a sequence of
     * different numbers, but any increments are allowed (even-number increments won't be able to produce all outputs,
     * but their quality will be fine for the numbers they can produce). All longs are accepted by this method, but not
     * all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any odd-number values for
     * bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */

    public static int randomizeBounded(long state, int bound) {
        return (bound = (int) ((bound * (((state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determineFloat(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 30 possible floats between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineFloat(++state)} is recommended to go forwards or
     *              {@code determineFloat(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomizeFloat(++state)}, where the increment for state can be any value and should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by any odd
     * number, but there are only 2 to the 30 possible floats between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomizeFloat(++state)} is recommended to go forwards or
     *              {@code randomizeFloat(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomizeFloat(long state) {
        return (((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;

    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determineDouble(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineDouble(++state)} is recommended to go forwards or
     *              {@code determineDouble(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomizeDouble(++state)}, where the increment for state can be any number but should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by 1, but
     * there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize()} is a completely different algorithm based
     * on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Both determine() and randomize() will produce all
     * long outputs if given all possible longs as input.
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomizeDouble(++state)} is recommended to go forwards or
     *              {@code randomizeDouble(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomizeDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    public Hasher(final CharSequence seed) {
        this(hash64(1L, seed));
    }

    /**
     * Big constant 0.
     */
    public static final long b0 = 0xA0761D6478BD642FL;
    /**
     * Big constant 1.
     */
    public static final long b1 = 0xE7037ED1A0B428DBL;
    /**
     * Big constant 2.
     */
    public static final long b2 = 0x8EBC6AF09C88C6E3L;
    /**
     * Big constant 3.
     */
    public static final long b3 = 0x589965CC75374CC3L;
    /**
     * Big constant 4.
     */
    public static final long b4 = 0x1D8E4E27C47D124FL;
    /**
     * Big constant 5.
     */
    public static final long b5 = 0xEB44ACCAB455D165L;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * <br>
     * This is very similar to wyhash's mum function, but doesn't use 128-bit math because it expects that its
     * arguments are only relevant in their lower 32 bits (allowing their product to fit in 64 bits).
     *
     * @param a a long that should probably only hold an int's worth of data
     * @param b a long that should probably only hold an int's worth of data
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long mum(final long a, final long b) {
        final long n = a * b;
        return n - (n >>> 32);
    }

    /**
     * A slower but higher-quality variant on {@link #mum(long, long)} that can take two arbitrary longs (with any
     * of their 64 bits containing relevant data) instead of mum's 32-bit sections of its inputs, and outputs a
     * 64-bit result that can have any of its bits used.
     * <br>
     * This was changed so it distributes bits from both inputs a little better on July 6, 2019.
     *
     * @param a any long
     * @param b any long
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long wow(final long a, final long b) {
        final long n = (a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25));
        return n ^ (n >>> 32);
    }

    public static final Hasher alpha = new Hasher("alpha"), beta = new Hasher("beta"), gamma = new Hasher("gamma"),
            delta = new Hasher("delta"), epsilon = new Hasher("epsilon"), zeta = new Hasher("zeta"),
            eta = new Hasher("eta"), theta = new Hasher("theta"), iota = new Hasher("iota"),
            kappa = new Hasher("kappa"), lambda = new Hasher("lambda"), mu = new Hasher("mu"),
            nu = new Hasher("nu"), xi = new Hasher("xi"), omicron = new Hasher("omicron"), pi = new Hasher("pi"),
            rho = new Hasher("rho"), sigma = new Hasher("sigma"), tau = new Hasher("tau"),
            upsilon = new Hasher("upsilon"), phi = new Hasher("phi"), chi = new Hasher("chi"), psi = new Hasher("psi"),
            omega = new Hasher("omega"),
            alpha_ = new Hasher("ALPHA"), beta_ = new Hasher("BETA"), gamma_ = new Hasher("GAMMA"),
            delta_ = new Hasher("DELTA"), epsilon_ = new Hasher("EPSILON"), zeta_ = new Hasher("ZETA"),
            eta_ = new Hasher("ETA"), theta_ = new Hasher("THETA"), iota_ = new Hasher("IOTA"),
            kappa_ = new Hasher("KAPPA"), lambda_ = new Hasher("LAMBDA"), mu_ = new Hasher("MU"),
            nu_ = new Hasher("NU"), xi_ = new Hasher("XI"), omicron_ = new Hasher("OMICRON"), pi_ = new Hasher("PI"),
            rho_ = new Hasher("RHO"), sigma_ = new Hasher("SIGMA"), tau_ = new Hasher("TAU"),
            upsilon_ = new Hasher("UPSILON"), phi_ = new Hasher("PHI"), chi_ = new Hasher("CHI"), psi_ = new Hasher("PSI"),
            omega_ = new Hasher("OMEGA"),
            baal = new Hasher("baal"), agares = new Hasher("agares"), vassago = new Hasher("vassago"), samigina = new Hasher("samigina"),
            marbas = new Hasher("marbas"), valefor = new Hasher("valefor"), amon = new Hasher("amon"), barbatos = new Hasher("barbatos"),
            paimon = new Hasher("paimon"), buer = new Hasher("buer"), gusion = new Hasher("gusion"), sitri = new Hasher("sitri"),
            beleth = new Hasher("beleth"), leraje = new Hasher("leraje"), eligos = new Hasher("eligos"), zepar = new Hasher("zepar"),
            botis = new Hasher("botis"), bathin = new Hasher("bathin"), sallos = new Hasher("sallos"), purson = new Hasher("purson"),
            marax = new Hasher("marax"), ipos = new Hasher("ipos"), aim = new Hasher("aim"), naberius = new Hasher("naberius"),
            glasya_labolas = new Hasher("glasya_labolas"), bune = new Hasher("bune"), ronove = new Hasher("ronove"), berith = new Hasher("berith"),
            astaroth = new Hasher("astaroth"), forneus = new Hasher("forneus"), foras = new Hasher("foras"), asmoday = new Hasher("asmoday"),
            gaap = new Hasher("gaap"), furfur = new Hasher("furfur"), marchosias = new Hasher("marchosias"), stolas = new Hasher("stolas"),
            phenex = new Hasher("phenex"), halphas = new Hasher("halphas"), malphas = new Hasher("malphas"), raum = new Hasher("raum"),
            focalor = new Hasher("focalor"), vepar = new Hasher("vepar"), sabnock = new Hasher("sabnock"), shax = new Hasher("shax"),
            vine = new Hasher("vine"), bifrons = new Hasher("bifrons"), vual = new Hasher("vual"), haagenti = new Hasher("haagenti"),
            crocell = new Hasher("crocell"), furcas = new Hasher("furcas"), balam = new Hasher("balam"), alloces = new Hasher("alloces"),
            caim = new Hasher("caim"), murmur = new Hasher("murmur"), orobas = new Hasher("orobas"), gremory = new Hasher("gremory"),
            ose = new Hasher("ose"), amy = new Hasher("amy"), orias = new Hasher("orias"), vapula = new Hasher("vapula"),
            zagan = new Hasher("zagan"), valac = new Hasher("valac"), andras = new Hasher("andras"), flauros = new Hasher("flauros"),
            andrealphus = new Hasher("andrealphus"), kimaris = new Hasher("kimaris"), amdusias = new Hasher("amdusias"), belial = new Hasher("belial"),
            decarabia = new Hasher("decarabia"), seere = new Hasher("seere"), dantalion = new Hasher("dantalion"), andromalius = new Hasher("andromalius"),
            baal_ = new Hasher("BAAL"), agares_ = new Hasher("AGARES"), vassago_ = new Hasher("VASSAGO"), samigina_ = new Hasher("SAMIGINA"),
            marbas_ = new Hasher("MARBAS"), valefor_ = new Hasher("VALEFOR"), amon_ = new Hasher("AMON"), barbatos_ = new Hasher("BARBATOS"),
            paimon_ = new Hasher("PAIMON"), buer_ = new Hasher("BUER"), gusion_ = new Hasher("GUSION"), sitri_ = new Hasher("SITRI"),
            beleth_ = new Hasher("BELETH"), leraje_ = new Hasher("LERAJE"), eligos_ = new Hasher("ELIGOS"), zepar_ = new Hasher("ZEPAR"),
            botis_ = new Hasher("BOTIS"), bathin_ = new Hasher("BATHIN"), sallos_ = new Hasher("SALLOS"), purson_ = new Hasher("PURSON"),
            marax_ = new Hasher("MARAX"), ipos_ = new Hasher("IPOS"), aim_ = new Hasher("AIM"), naberius_ = new Hasher("NABERIUS"),
            glasya_labolas_ = new Hasher("GLASYA_LABOLAS"), bune_ = new Hasher("BUNE"), ronove_ = new Hasher("RONOVE"), berith_ = new Hasher("BERITH"),
            astaroth_ = new Hasher("ASTAROTH"), forneus_ = new Hasher("FORNEUS"), foras_ = new Hasher("FORAS"), asmoday_ = new Hasher("ASMODAY"),
            gaap_ = new Hasher("GAAP"), furfur_ = new Hasher("FURFUR"), marchosias_ = new Hasher("MARCHOSIAS"), stolas_ = new Hasher("STOLAS"),
            phenex_ = new Hasher("PHENEX"), halphas_ = new Hasher("HALPHAS"), malphas_ = new Hasher("MALPHAS"), raum_ = new Hasher("RAUM"),
            focalor_ = new Hasher("FOCALOR"), vepar_ = new Hasher("VEPAR"), sabnock_ = new Hasher("SABNOCK"), shax_ = new Hasher("SHAX"),
            vine_ = new Hasher("VINE"), bifrons_ = new Hasher("BIFRONS"), vual_ = new Hasher("VUAL"), haagenti_ = new Hasher("HAAGENTI"),
            crocell_ = new Hasher("CROCELL"), furcas_ = new Hasher("FURCAS"), balam_ = new Hasher("BALAM"), alloces_ = new Hasher("ALLOCES"),
            caim_ = new Hasher("CAIM"), murmur_ = new Hasher("MURMUR"), orobas_ = new Hasher("OROBAS"), gremory_ = new Hasher("GREMORY"),
            ose_ = new Hasher("OSE"), amy_ = new Hasher("AMY"), orias_ = new Hasher("ORIAS"), vapula_ = new Hasher("VAPULA"),
            zagan_ = new Hasher("ZAGAN"), valac_ = new Hasher("VALAC"), andras_ = new Hasher("ANDRAS"), flauros_ = new Hasher("FLAUROS"),
            andrealphus_ = new Hasher("ANDREALPHUS"), kimaris_ = new Hasher("KIMARIS"), amdusias_ = new Hasher("AMDUSIAS"), belial_ = new Hasher("BELIAL"),
            decarabia_ = new Hasher("DECARABIA"), seere_ = new Hasher("SEERE"), dantalion_ = new Hasher("DANTALION"), andromalius_ = new Hasher("ANDROMALIUS");
    /**
     * Has a length of 192, which may be relevant if automatically choosing a predefined hash functor.
     */
    public static final Hasher[] predefined = new Hasher[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
            kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega,
            alpha_, beta_, gamma_, delta_, epsilon_, zeta_, eta_, theta_, iota_,
            kappa_, lambda_, mu_, nu_, xi_, omicron_, pi_, rho_, sigma_, tau_, upsilon_, phi_, chi_, psi_, omega_,
            baal, agares, vassago, samigina, marbas, valefor, amon, barbatos,
            paimon, buer, gusion, sitri, beleth, leraje, eligos, zepar,
            botis, bathin, sallos, purson, marax, ipos, aim, naberius,
            glasya_labolas, bune, ronove, berith, astaroth, forneus, foras, asmoday,
            gaap, furfur, marchosias, stolas, phenex, halphas, malphas, raum,
            focalor, vepar, sabnock, shax, vine, bifrons, vual, haagenti,
            crocell, furcas, balam, alloces, caim, murmur, orobas, gremory,
            ose, amy, orias, vapula, zagan, valac, andras, flauros,
            andrealphus, kimaris, amdusias, belial, decarabia, seere, dantalion, andromalius,
            baal_, agares_, vassago_, samigina_, marbas_, valefor_, amon_, barbatos_,
            paimon_, buer_, gusion_, sitri_, beleth_, leraje_, eligos_, zepar_,
            botis_, bathin_, sallos_, purson_, marax_, ipos_, aim_, naberius_,
            glasya_labolas_, bune_, ronove_, berith_, astaroth_, forneus_, foras_, asmoday_,
            gaap_, furfur_, marchosias_, stolas_, phenex_, halphas_, malphas_, raum_,
            focalor_, vepar_, sabnock_, shax_, vine_, bifrons_, vual_, haagenti_,
            crocell_, furcas_, balam_, alloces_, caim_, murmur_, orobas_, gremory_,
            ose_, amy_, orias_, vapula_, zagan_, valac_, andras_, flauros_,
            andrealphus_, kimaris_, amdusias_, belial_, decarabia_, seere_, dantalion_, andromalius_};

    public long hash64(final boolean[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = result * 0xEBEDEED9D803C815L
                    + (data[i] ? 0xD96EB1A810CAAF5FL : 0xCAAF5FD96EB1A810L)
                    + (data[i + 1] ? 0xC862B36DAF790DD5L : 0x790DD5C862B36DAFL)
                    + (data[i + 2] ? 0xB8ACD90C142FE10BL : 0x2FE10BB8ACD90C14L)
                    + (data[i + 3] ? 0xAA324F90DED86B69L : 0xD86B69AA324F90DEL)
                    + (data[i + 4] ? 0x9CDA5E693FEA10AFL : 0xEA10AF9CDA5E693FL)
                    + (data[i + 5] ? 0x908E3D2C82567A73L : 0x567A73908E3D2C82L)
                    + (data[i + 6] ? 0x8538ECB5BD456EA3L : 0x456EA38538ECB5BDL)
                    + (data[i + 7] ? 0xD1B54A32D192ED03L : 0x92ED03D1B54A32D1L)
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + (data[i] ? 0xEBEDEED9D803C815L : 0xD9D803C815EBEDEEL);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final byte[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final short[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final char[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final CharSequence data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length();
        for (int i = 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final int[] data, final int length) {
        if (data == null) return 0;
        final int len = Math.min(length, data.length);
        long result = seed ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final long[] data) {
        if (data == null) return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ data[len - 1]);
                break;
            case 2:
                seed = wow(seed + data[len - 2], b2 + data[len - 1]);
                break;
            case 3:
                seed = wow(seed + data[len - 3], b2 + data[len - 2]) ^ wow(seed + data[len - 1], seed ^ b3);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0 ^ seed >>> 32);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final float[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * floatToRawIntBits(data[i])
                    + 0xC862B36DAF790DD5L * floatToRawIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * floatToRawIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * floatToRawIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * floatToRawIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * floatToRawIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * floatToRawIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * floatToRawIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + floatToRawIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final double[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * doubleToMixedIntBits(data[i])
                    + 0xC862B36DAF790DD5L * doubleToMixedIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * doubleToMixedIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * doubleToMixedIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * doubleToMixedIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * doubleToMixedIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * doubleToMixedIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * doubleToMixedIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + doubleToMixedIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public long hash64(final char[] data, final int start, final int end) {
        if (data == null || start >= end) return 0;
        final int len = Math.min(end, data.length);

        long result = seed ^ (len - start) * 0x9E3779B97F4A7C15L;
        int i = start;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public long hash64(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = this.seed;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        return mum(seed ^ seed << 16, len - start ^ b0);
    }


    public long hash64(final char[][] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final int[][] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final long[][] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final CharSequence[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final CharSequence[]... data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            seed = mum(
                    mum(hash64(it.next()) ^ b1, (it.hasNext() ? hash64(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash64(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash64(it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        final int len = data.size();
        long result = seed ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data.get(i))
                    + 0xC862B36DAF790DD5L * hash64(data.get(i + 1))
                    + 0xB8ACD90C142FE10BL * hash64(data.get(i + 2))
                    + 0xAA324F90DED86B69L * hash64(data.get(i + 3))
                    + 0x9CDA5E693FEA10AFL * hash64(data.get(i + 4))
                    + 0x908E3D2C82567A73L * hash64(data.get(i + 5))
                    + 0x8538ECB5BD456EA3L * hash64(data.get(i + 6))
                    + 0xD1B54A32D192ED03L * hash64(data.get(i + 7))
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data.get(i));
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);

    }

    public long hash64(final Object[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public long hash64(final Object data) {
        if (data == null)
            return 0;
        final long h = (data.hashCode() + seed) * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }

    public int hash(final boolean[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = result * 0xEBEDEED9D803C815L
                    + (data[i] ? 0xD96EB1A810CAAF5FL : 0xCAAF5FD96EB1A810L)
                    + (data[i + 1] ? 0xC862B36DAF790DD5L : 0x790DD5C862B36DAFL)
                    + (data[i + 2] ? 0xB8ACD90C142FE10BL : 0x2FE10BB8ACD90C14L)
                    + (data[i + 3] ? 0xAA324F90DED86B69L : 0xD86B69AA324F90DEL)
                    + (data[i + 4] ? 0x9CDA5E693FEA10AFL : 0xEA10AF9CDA5E693FL)
                    + (data[i + 5] ? 0x908E3D2C82567A73L : 0x567A73908E3D2C82L)
                    + (data[i + 6] ? 0x8538ECB5BD456EA3L : 0x456EA38538ECB5BDL)
                    + (data[i + 7] ? 0xD1B54A32D192ED03L : 0x92ED03D1B54A32D1L)
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + (data[i] ? 0xEBEDEED9D803C815L : 0xD9D803C815EBEDEEL);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final byte[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final short[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final char[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final CharSequence data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length();
        for (int i = 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }

    public int hash(final int[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final int[] data, final int length) {
        if (data == null) return 0;
        final int len = Math.min(length, data.length);
        long result = seed ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final long[] data) {
        if (data == null) return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ data[len - 1]);
                break;
            case 2:
                seed = wow(seed + data[len - 2], b2 + data[len - 1]);
                break;
            case 3:
                seed = wow(seed + data[len - 3], b2 + data[len - 2]) ^ wow(seed + data[len - 1], seed ^ b3);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0 ^ seed >>> 32);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final float[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * floatToRawIntBits(data[i])
                    + 0xC862B36DAF790DD5L * floatToRawIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * floatToRawIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * floatToRawIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * floatToRawIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * floatToRawIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * floatToRawIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * floatToRawIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + floatToRawIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final double[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * doubleToMixedIntBits(data[i])
                    + 0xC862B36DAF790DD5L * doubleToMixedIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * doubleToMixedIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * doubleToMixedIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * doubleToMixedIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * doubleToMixedIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * doubleToMixedIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * doubleToMixedIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + doubleToMixedIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public int hash(final char[] data, final int start, final int end) {
        if (data == null || start >= end) return 0;
        final int len = Math.min(end, data.length);

        long result = seed ^ (len - start) * 0x9E3779B97F4A7C15L;
        int i = start;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public int hash(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = this.seed;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
    }


    public int hash(final char[][] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final int[][] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final long[][] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final CharSequence[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final CharSequence[]... data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            seed = mum(
                    mum(hash64(it.next()) ^ b1, (it.hasNext() ? hash64(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash64(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash64(it.next()) ^ b4 ^ ++len : b4)));
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }

    public int hash(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        final int len = data.size();
        long result = seed ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data.get(i))
                    + 0xC862B36DAF790DD5L * hash64(data.get(i + 1))
                    + 0xB8ACD90C142FE10BL * hash64(data.get(i + 2))
                    + 0xAA324F90DED86B69L * hash64(data.get(i + 3))
                    + 0x9CDA5E693FEA10AFL * hash64(data.get(i + 4))
                    + 0x908E3D2C82567A73L * hash64(data.get(i + 5))
                    + 0x8538ECB5BD456EA3L * hash64(data.get(i + 6))
                    + 0xD1B54A32D192ED03L * hash64(data.get(i + 7))
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data.get(i));
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);

    }

    public int hash(final Object[] data) {
        if (data == null) return 0;
        long result = seed ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(data[i])
                    + 0xC862B36DAF790DD5L * hash64(data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public int hash(final Object data) {
        if (data == null) return 0;
        return (int) ((data.hashCode() + seed) * 0x9E3779B97F4A7C15L >>> 32);
    }


    public static long hash64(final long seed, final boolean[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = result * 0xEBEDEED9D803C815L
                    + (data[i] ? 0xD96EB1A810CAAF5FL : 0xCAAF5FD96EB1A810L)
                    + (data[i + 1] ? 0xC862B36DAF790DD5L : 0x790DD5C862B36DAFL)
                    + (data[i + 2] ? 0xB8ACD90C142FE10BL : 0x2FE10BB8ACD90C14L)
                    + (data[i + 3] ? 0xAA324F90DED86B69L : 0xD86B69AA324F90DEL)
                    + (data[i + 4] ? 0x9CDA5E693FEA10AFL : 0xEA10AF9CDA5E693FL)
                    + (data[i + 5] ? 0x908E3D2C82567A73L : 0x567A73908E3D2C82L)
                    + (data[i + 6] ? 0x8538ECB5BD456EA3L : 0x456EA38538ECB5BDL)
                    + (data[i + 7] ? 0xD1B54A32D192ED03L : 0x92ED03D1B54A32D1L)
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + (data[i] ? 0xEBEDEED9D803C815L : 0xD9D803C815EBEDEEL);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final byte[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final short[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final char[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(long seed, final CharSequence data) {
        if (data == null) return 0L;
        seed += b1;
        seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length();
        for (int i = 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final long seed, final int[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final int[] data, final int length) {
        if (data == null) return 0;
        final int len = Math.min(length, data.length);
        long result = randomize(seed) ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final long[] data) {
        if (data == null) return 0;
        long s = randomize(seed), a = s + b4, b = s + b3, c = s + b2, d = s + b1;
        final int len = data.length;
        for (int i = 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            s += a + b + c + d;
        }
        s += b5;
        switch (len & 3) {
            case 1:
                s = wow(s, b1 ^ data[len - 1]);
                break;
            case 2:
                s = wow(s + data[len - 2], b2 + data[len - 1]);
                break;
            case 3:
                s = wow(s + data[len - 3], b2 + data[len - 2]) ^ wow(s + data[len - 1], s ^ b3);
                break;
        }
        s = (s ^ s << 16) * (len ^ b0 ^ s >>> 32);
        return s - (s >>> 31) + (s << 33);
    }

    public static long hash64(final long seed, final float[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * floatToRawIntBits(data[i])
                    + 0xC862B36DAF790DD5L * floatToRawIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * floatToRawIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * floatToRawIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * floatToRawIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * floatToRawIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * floatToRawIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * floatToRawIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + floatToRawIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final double[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * doubleToMixedIntBits(data[i])
                    + 0xC862B36DAF790DD5L * doubleToMixedIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * doubleToMixedIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * doubleToMixedIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * doubleToMixedIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * doubleToMixedIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * doubleToMixedIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * doubleToMixedIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + doubleToMixedIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(final long seed, final char[] data, final int start, final int end) {
        if (data == null || start >= end) return 0;
        final int len = Math.min(end, data.length);

        long result = randomize(seed) ^ (len - start) * 0x9E3779B97F4A7C15L;
        int i = start;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(long seed, final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0L;
        seed += b1;
        seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        return mum(seed ^ seed << 16, len - start ^ b0);
    }


    public static long hash64(final long seed, final char[][] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final int[][] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final long[][] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final CharSequence[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long s = randomize(seed);
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            s = mum(
                    mum(hash64(seed, it.next()) ^ b1, (it.hasNext() ? hash64(seed, it.next()) ^ b2 ^ ++len : b2)) + s,
                    mum((it.hasNext() ? hash64(seed, it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash64(seed, it.next()) ^ b4 ^ ++len : b4)));
        }
        s = (s ^ s << 16) * (len ^ b0);
        return s - (s >>> 31) + (s << 33);
    }

    public static long hash64(final long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        final int len = data.size();
        long result = randomize(seed) ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data.get(i))
                    + 0xC862B36DAF790DD5L * hash64(seed, data.get(i + 1))
                    + 0xB8ACD90C142FE10BL * hash64(seed, data.get(i + 2))
                    + 0xAA324F90DED86B69L * hash64(seed, data.get(i + 3))
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data.get(i + 4))
                    + 0x908E3D2C82567A73L * hash64(seed, data.get(i + 5))
                    + 0x8538ECB5BD456EA3L * hash64(seed, data.get(i + 6))
                    + 0xD1B54A32D192ED03L * hash64(seed, data.get(i + 7))
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data.get(i));
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);

    }

    public static long hash64(final long seed, final Object[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (result ^ result >>> 28);
    }

    public static long hash64(final long seed, final Object data) {
        if (data == null)
            return 0;
        final long h = (data.hashCode() + randomize(seed)) * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }

    public static int hash(final long seed, final boolean[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = result * 0xEBEDEED9D803C815L
                    + (data[i] ? 0xD96EB1A810CAAF5FL : 0xCAAF5FD96EB1A810L)
                    + (data[i + 1] ? 0xC862B36DAF790DD5L : 0x790DD5C862B36DAFL)
                    + (data[i + 2] ? 0xB8ACD90C142FE10BL : 0x2FE10BB8ACD90C14L)
                    + (data[i + 3] ? 0xAA324F90DED86B69L : 0xD86B69AA324F90DEL)
                    + (data[i + 4] ? 0x9CDA5E693FEA10AFL : 0xEA10AF9CDA5E693FL)
                    + (data[i + 5] ? 0x908E3D2C82567A73L : 0x567A73908E3D2C82L)
                    + (data[i + 6] ? 0x8538ECB5BD456EA3L : 0x456EA38538ECB5BDL)
                    + (data[i + 7] ? 0xD1B54A32D192ED03L : 0x92ED03D1B54A32D1L)
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + (data[i] ? 0xEBEDEED9D803C815L : 0xD9D803C815EBEDEEL);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final byte[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final short[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final char[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(long seed, final CharSequence data) {
        if (data == null) return 0;
        seed += b1;
        seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length();
        for (int i = 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }

    public static int hash(final long seed, final int[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final int[] data, final int length) {
        if (data == null) return 0;
        final int len = Math.min(length, data.length);
        long result = randomize(seed) ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final long[] data) {
        if (data == null) return 0;
        long s = randomize(seed), a = s + b4, b = s + b3, c = s + b2, d = s + b1;
        final int len = data.length;
        for (int i = 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            s += a + b + c + d;
        }
        s += b5;
        switch (len & 3) {
            case 1:
                s = wow(s, b1 ^ data[len - 1]);
                break;
            case 2:
                s = wow(s + data[len - 2], b2 + data[len - 1]);
                break;
            case 3:
                s = wow(s + data[len - 3], b2 + data[len - 2]) ^ wow(s + data[len - 1], s ^ b3);
                break;
        }
        s = (s ^ s << 16) * (len ^ b0 ^ s >>> 32);
        return (int) (s - (s >>> 32));
    }

    public static int hash(final long seed, final float[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * floatToRawIntBits(data[i])
                    + 0xC862B36DAF790DD5L * floatToRawIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * floatToRawIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * floatToRawIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * floatToRawIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * floatToRawIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * floatToRawIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * floatToRawIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + floatToRawIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final double[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * doubleToMixedIntBits(data[i])
                    + 0xC862B36DAF790DD5L * doubleToMixedIntBits(data[i + 1])
                    + 0xB8ACD90C142FE10BL * doubleToMixedIntBits(data[i + 2])
                    + 0xAA324F90DED86B69L * doubleToMixedIntBits(data[i + 3])
                    + 0x9CDA5E693FEA10AFL * doubleToMixedIntBits(data[i + 4])
                    + 0x908E3D2C82567A73L * doubleToMixedIntBits(data[i + 5])
                    + 0x8538ECB5BD456EA3L * doubleToMixedIntBits(data[i + 6])
                    + 0xD1B54A32D192ED03L * doubleToMixedIntBits(data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + doubleToMixedIntBits(data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static int hash(final long seed, final char[] data, final int start, final int end) {
        if (data == null || start >= end) return 0;
        final int len = Math.min(end, data.length);

        long result = randomize(seed) ^ (len - start) * 0x9E3779B97F4A7C15L;
        int i = start;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static int hash(long seed, final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        seed += b1;
        seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ b3, b4 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), b3 ^ data.charAt(len - 1));
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3) ^ (long) data.charAt(len - 2) << 16, b1 ^ data.charAt(len - 1));
                break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
    }


    public static int hash(final long seed, final char[][] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final int[][] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final long[][] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final CharSequence[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long s = randomize(seed);
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            s = mum(
                    mum(hash64(seed, it.next()) ^ b1, (it.hasNext() ? hash64(seed, it.next()) ^ b2 ^ ++len : b2)) + s,
                    mum((it.hasNext() ? hash64(seed, it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash64(seed, it.next()) ^ b4 ^ ++len : b4)));
        }
        return (int) mum(s ^ s << 16, len ^ b0);
    }

    public static int hash(final long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        final int len = data.size();
        long result = randomize(seed) ^ len * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data.get(i))
                    + 0xC862B36DAF790DD5L * hash64(seed, data.get(i + 1))
                    + 0xB8ACD90C142FE10BL * hash64(seed, data.get(i + 2))
                    + 0xAA324F90DED86B69L * hash64(seed, data.get(i + 3))
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data.get(i + 4))
                    + 0x908E3D2C82567A73L * hash64(seed, data.get(i + 5))
                    + 0x8538ECB5BD456EA3L * hash64(seed, data.get(i + 6))
                    + 0xD1B54A32D192ED03L * hash64(seed, data.get(i + 7))
            ;
        }
        for (; i < len; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data.get(i));
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);

    }

    public static int hash(final long seed, final Object[] data) {
        if (data == null) return 0;
        long result = randomize(seed) ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * hash64(seed, data[i])
                    + 0xC862B36DAF790DD5L * hash64(seed, data[i + 1])
                    + 0xB8ACD90C142FE10BL * hash64(seed, data[i + 2])
                    + 0xAA324F90DED86B69L * hash64(seed, data[i + 3])
                    + 0x9CDA5E693FEA10AFL * hash64(seed, data[i + 4])
                    + 0x908E3D2C82567A73L * hash64(seed, data[i + 5])
                    + 0x8538ECB5BD456EA3L * hash64(seed, data[i + 6])
                    + 0xD1B54A32D192ED03L * hash64(seed, data[i + 7])
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + hash64(seed, data[i]);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int) (result ^ result >>> 28);
    }

    public static int hash(final long seed, final Object data) {
        if (data == null) return 0;
        return (int) ((data.hashCode() + randomize(seed)) * 0x9E3779B97F4A7C15L >>> 32);
    }
}
