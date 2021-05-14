package com.github.yellowstonegames.world;

import com.github.yellowstonegames.core.MathTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;

/**
 * An unusual map generator that imitates an existing local map (such as a map of Australia, which it can do by
 * default), without applying any projection or changing heat levels in the polar regions or equator.
 * <a href="http://yellowstonegames.github.io/SquidLib/LocalMimicMap.png" >Example map, showing a variant on Australia</a>
 */
public class MimicLocalMap extends LocalMap {

    public Region earth;
    public Region shallow;
    public Region coast;
    public Region earthOriginal;

    /**
     * Constructs a concrete WorldMapGenerator for a map that should look like Australia, without projecting the
     * land positions or changing heat by latitude. Always makes a 256x256 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link MimicLocalMap#MimicLocalMap(long, Noise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, DEFAULT_NOISE, 1f}.
     */
    public MimicLocalMap() {
        this(0x1337BABE1337D00DL, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
     * The initial seed is set to the same large long every time, and it's likely that you would set the seed when
     * you call {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
     */
    public MimicLocalMap(Region toMimic) {
        this(0x1337BABE1337D00DL, toMimic, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
     * Takes an initial seed and the Region containing land positions. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param toMimic     the world map to imitate, as a Region with land as "on"; the height and width will be copied
     */
    public MimicLocalMap(long initialSeed, Region toMimic) {
        this(initialSeed, toMimic, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
     * Takes an initial seed, the Region containing land positions, and a multiplier that affects the level
     * of detail by increasing or decreasing the number of octaves of noise used. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact.
     * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
     *
     * @param initialSeed      the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param toMimic          the world map to imitate, as a Region with land as "on"; the height and width will be copied
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public MimicLocalMap(long initialSeed, Region toMimic, float octaveMultiplier) {
        this(initialSeed, toMimic, DEFAULT_NOISE, octaveMultiplier);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
     * Takes an initial seed, the Region containing land positions, and parameters for noise generation (a
     * {@link Noise} implementation, which is usually {@link Noise#instance}. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call
     * {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact. Both Noise
     * and Noise make sense to use for {@code noiseGenerator}, and the seed it's constructed with doesn't matter
     * because this will change the seed several times at different scales of noise (it's fine to use the static
     * {@link Noise#instance} or {@link Noise#instance} because they have no changing state between runs
     * of the program). Uses the given noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed    the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param toMimic        the world map to imitate, as a Region with land as "on"; the height and width will be copied
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
     */
    public MimicLocalMap(long initialSeed, Region toMimic, Noise noiseGenerator) {
        this(initialSeed, toMimic, noiseGenerator, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
     * Takes an initial seed, the Region containing land positions, parameters for noise generation (a
     * {@link Noise} implementation, which is usually {@link Noise#instance}, and a multiplier on how many
     * octaves of noise to use, with 1f being normal (high) detail and higher multipliers producing even more
     * detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
     * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
     * cannot be changed after the fact.  Noise will be the fastest 3D generator to use for
     * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
     * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
     * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
     * probably be no lower than 0.5f, but can be arbitrarily high if you're willing to spend much more time on
     * generating detail only noticeable at very high zoom; normally 1f is fine and may even be too high for maps
     * that don't require zooming.
     *
     * @param initialSeed      the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param toMimic          the world map to imitate, as a Region with land as "on"; the height and width will be copied
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public MimicLocalMap(long initialSeed, Region toMimic, Noise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
        earth = toMimic;
        earthOriginal = earth.copy();
        coast = earth.copy().not().fringe(2);
        shallow = earth.copy().fringe(2);
    }

    /**
     * Stores a 256x256 Region that shows an unprojected map of Australia, in a format that can be read back
     * with {@link Region#decompress(String)}. By using Region's compression, this takes up a lot less
     * room than it would with most text-based formats, and even beats uncompressed binary storage of the map by a
     * factor of 9.4f. The map data won't change here, so this should stay compatible.
     */
    public static final String AUSTRALIA_ENCODED = "Ƥ䒅⒐᮰囨䈢ħ䐤࠰ࠨ•Ⱙအ䎢ŘňÆ䴣ȢؤF䭠゠ᔤ∠偰ഀ\u0560₠ኼܨā᭮笁\u242AЦᇅ扰रࠦ吠䠪ࠦ䠧娮⠬䠬❁Ềក\u1CAA͠敠ἒ慽Ê䄄洡儠䋻䨡㈠䙬坈མŨྈ䞻䛊哚晪⁞倰h·䡂Ļæ抂㴢္\u082E搧䈠ᇩᒠ᩠ɀ༨ʨڤʃ奲ࢠ፠ᆙả䝆䮳りĩ(ॠી᧰྄e॑ᤙ䒠剠⁌ဥࠩФΝ䂂⢴ᑠ㺀ᢣ䗨dBqÚ扜冢\u0FE5\u0A62䐠劣ေ¯䂍䞀ၰ\u0E67ᐓ〈ᄠ塠Ѡ̀ာ⠤ᡤŒęጓ憒‱〿䌳℔ᐼ䊢⁚䤿ӣ◚㙀\u0C74Ӹ抠⣀ĨǊǸ䁃း₺Ý䂁ᜤ䢑V⁄樫焠\u0A60\u2E78⎲Ĉ䁎勯戡璠悈ᠥ嘡⩩‰ನ檨㡕䶪၁@恑ࠣ䘣ࢠᅀᡎ劰桠Өॢಸ熛փࢸ䀹ఽ䅠勖ਰ۴̄ጺಢ䈠ᙠᨭ\u2FE0焠Ӡܼ䇂䒠ᯀԨĠ愜᪅䦥㶐ୀ\u09C5Ƣ*䂕ॹ∠咠р\u0604У無~⁆Г椠痠\u1CA9Ⱓס㩖ᝋ司楠२ญⳘ䬣汤ǿã㱩ᖷ掠Àݒ㑁c‾䮴,\u2452僢ᰣ缠ɋ乨\u0378䁡绑ס傓䁔瀾ሺÑ䀤ो刡开烀\u0A76Ё䈠䈰״Áj⁑䠡戢碠㘀አ䃉㪙嘈ʂø⸪௰₈㐲暤ƩDᬿ䂖剙書\u0FE0㴢\u0089㘩Ĉ䰵掀栰杁4〡Ƞ⭀\u1AE0㠰㹨Zコത\u009E䂖ࠠⴠ縣吠ᆠʡ㡀䀧否䣝Ӧ愠Ⓚ\u1CA2ಠո*①ӈԥ獀խ@㟬箬㐱\u31BE簽Ɛᩆᇞ稯禚⟶⣑аβǚ㥎Ḇ⌢㑆 搡⁗ဣ刣\u0C45䑒8怺₵⤦a5ਵ㏰ᩄ猢ฦ䬞㐷䈠呠カ愠ۀ\u1C92傠ᅼ߃ᙊ䢨ၠླྀš亀ƴ̰刷ʼ墨愠  ";

    /**
     * Constructs a 256x256 unprojected local map that will use land forms with a similar shape to Australia.
     *
     * @param initialSeed
     * @param noiseGenerator
     * @param octaveMultiplier
     */
    public MimicLocalMap(long initialSeed, Noise noiseGenerator, float octaveMultiplier) {
        this(initialSeed,
                Region.decompress(AUSTRALIA_ENCODED), noiseGenerator, octaveMultiplier);
    }

    /**
     * Copies the MimicLocalMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a MimicLocalMap to copy
     */
    public MimicLocalMap(MimicLocalMap other) {
        super(other);
        earth = other.earth.copy();
        earthOriginal = other.earthOriginal.copy();
        coast = other.coast.copy();
        shallow = other.shallow.copy();
    }


    protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                              float landMod, float heatMod, long stateA, long stateB) {
        boolean fresh = false;
        if (cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier) {
            minHeight = Float.POSITIVE_INFINITY;
            maxHeight = Float.NEGATIVE_INFINITY;
            minHeat0 = Float.POSITIVE_INFINITY;
            maxHeat0 = Float.NEGATIVE_INFINITY;
            minHeat1 = Float.POSITIVE_INFINITY;
            maxHeat1 = Float.NEGATIVE_INFINITY;
            minHeat = Float.POSITIVE_INFINITY;
            maxHeat = Float.NEGATIVE_INFINITY;
            minWet0 = Float.POSITIVE_INFINITY;
            maxWet0 = Float.NEGATIVE_INFINITY;
            minWet = Float.POSITIVE_INFINITY;
            maxWet = Float.NEGATIVE_INFINITY;
            cacheA = stateA;
            cacheB = stateB;
            fresh = true;
        }
        rng.setState(stateA, stateB);
        long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
        int t;

        landModifier = (landMod <= 0) ? rng.nextFloat(0.29f) + 0.91f : landMod;
        heatModifier = (heatMod <= 0) ? rng.nextFloat(0.45f) * (rng.nextFloat() - 0.5f) + 1.1f : heatMod;

        earth.remake(earthOriginal);

        if (zoom > 0) {
            int stx = Math.min(Math.max((zoomStartX - (width >> 1)) / ((2 << zoom) - 2), 0), width),
                    sty = Math.min(Math.max((zoomStartY - (height >> 1)) / ((2 << zoom) - 2), 0), height);
            for (int z = 0; z < zoom; z++) {
                earth.zoom(stx, sty).expand8way().fray(0.5f).expand();
            }
            coast.remake(earth).not().fringe(2 << zoom).expand().fray(0.5f);
            shallow.remake(earth).fringe(2 << zoom).expand().fray(0.5f);
        } else {
            coast.remake(earth).not().fringe(2);
            shallow.remake(earth).fringe(2);
        }
        float p,
                ps, pc,
                qs, qc,
                h, temp,
                i_w = 1f / width, i_h = 1f / (height),
                i_uw = usedWidth * i_w * i_w, i_uh = usedHeight * i_h * i_h, xPos, yPos = startY * i_h;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            xPos = startX * i_w;
            for (int x = 0; x < width; x++, xPos += i_uw) {
                xPositions[x][y] = (xPos - .5f) * 2f;
                yPositions[x][y] = (yPos - .5f) * 2f;
                zPositions[x][y] = 0f;

                if (earth.contains(x, y)) {
                    h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(xPos +
                                    terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5f,
                            yPos, seedA)) * 0.85f;
                    if (coast.contains(x, y))
                        h += 0.05f;
                    else
                        h += 0.15f;
                } else {
                    h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(xPos +
                                    terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5f,
                            yPos, seedA)) * -0.9f;
                    if (shallow.contains(x, y))
                        h = (h - 0.08f) * 0.375f;
                    else
                        h = (h - 0.125f) * 0.75f;
                }
                //h += landModifier - 1f;
                heightData[x][y] = h;
                heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                + 0.375f * otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                        seedB));
                temp = 0.375f * otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

                minHeightActual = Math.min(minHeightActual, h);
                maxHeightActual = Math.max(maxHeightActual, h);
                if (fresh) {
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);

                    minHeat0 = Math.min(minHeat0, p);
                    maxHeat0 = Math.max(maxHeat0, p);

                    minWet0 = Math.min(minWet0, temp);
                    maxWet0 = Math.max(maxWet0, temp);
                }
            }
            minHeightActual = Math.min(minHeightActual, minHeight);
            maxHeightActual = Math.max(maxHeightActual, maxHeight);

        }
        float heatDiff = 0.8f / (maxHeat0 - minHeat0),
                wetDiff = 1f / (maxWet0 - minWet0),
                hMod;
        yPos = startY * i_h + i_uh;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            for (int x = 0; x < width; x++) {
                h = heightData[x][y];
                heightCodeData[x][y] = (t = codeHeight(h));
                hMod = 1f;
                switch (t) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        h = 0.4f;
                        hMod = 0.2f;
                        break;
                    case 6:
                        h = -0.1f * (h - forestLower - 0.08f);
                        break;
                    case 7:
                        h *= -0.25f;
                        break;
                    case 8:
                        h *= -0.4f;
                        break;
                    default:
                        h *= 0.05f;
                }
                heatData[x][y] = (h = ((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6f);
                if (fresh) {
                    ps = Math.min(ps, h); //minHeat0
                    pc = Math.max(pc, h); //maxHeat0
                }
            }
        }
        if (fresh) {
            minHeat1 = ps;
            maxHeat1 = pc;
        }
        heatDiff = heatModifier / (maxHeat1 - minHeat1);
        qs = Float.POSITIVE_INFINITY;
        qc = Float.NEGATIVE_INFINITY;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                if (fresh) {
                    qs = Math.min(qs, h);
                    qc = Math.max(qc, h);
                    ps = Math.min(ps, temp);
                    pc = Math.max(pc, temp);
                }
            }
        }
        if (fresh) {
            minHeat = qs;
            maxHeat = qc;
            minWet = ps;
            maxWet = pc;
        }
        landData.refill(heightCodeData, 4, 999);
    }
}
