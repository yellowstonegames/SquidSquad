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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TextTools;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;

import java.util.Arrays;

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
     * If you were using {@link MimicLocalMap#MimicLocalMap(long, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, new Noise(DEFAULT_NOISE), 1f}.
     */
    public MimicLocalMap() {
        this(0x1337BABE1337D00DL, new Noise(DEFAULT_NOISE), 1f);
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
        this(0x1337BABE1337D00DL, toMimic, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
     * Takes an initial seed and the Region containing land positions. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param toMimic     the world map to imitate, as a Region with land as "on"; the height and width will be copied
     */
    public MimicLocalMap(long initialSeed, Region toMimic) {
        this(initialSeed, toMimic, new Noise(DEFAULT_NOISE), 1f);
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param toMimic          the world map to imitate, as a Region with land as "on"; the height and width will be copied
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public MimicLocalMap(long initialSeed, Region toMimic, float octaveMultiplier) {
        this(initialSeed, toMimic, new Noise(DEFAULT_NOISE), octaveMultiplier);
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
     * @param initialSeed    the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param toMimic        the world map to imitate, as a Region with land as "on"; the height and width will be copied
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
     */
    public MimicLocalMap(long initialSeed, Region toMimic, INoise noiseGenerator) {
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param toMimic          the world map to imitate, as a Region with land as "on"; the height and width will be copied
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public MimicLocalMap(long initialSeed, Region toMimic, INoise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
        earth = toMimic;
        earthOriginal = earth.copy();
        coast = earth.copy().not().fringe(2);
        shallow = earth.copy().fringe(2);
    }

    /**
     * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}, but this also
     * requires width and height that match the first two lines of the given String (in {@link Base#BASE86}). It is
     * almost always easier to use {@link #recreateFromString(String)} instead.
     * @param width width of the map or maps to generate; must match the first line of the given String in {@link Base#BASE86}
     * @param height height of the map or maps to generate; must match the second line of the given String in {@link Base#BASE86}
     * @param serialized should have been produced by {@link #stringSerialize()}
     */
    public MimicLocalMap(int width, int height, String serialized) {
        super(width, height, serialized);
        String[] parts = TextTools.split(serialized, "\n");

        int i = 45;
        // WorldMapGenerator's many fields were mostly already read.
        // The fields of this class:
        earth = Region.decompress(parts[i++]);
        earthOriginal = Region.decompress(parts[i++]);
        coast = Region.decompress(parts[i++]);
        shallow = Region.decompress(parts[i++]);
    }

    /**
     * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
     * type with {@link #MimicLocalMap(int, int, String)} or (preferably) {@link #recreateFromString(String)}.
     * Uses {@link Base#BASE86} to represent values very concisely, but not at all readably. The String this produces
     * tends to be very long because it includes several 2D arrays and a Region as Strings.
     * @return a String that stores the entire state of this generator
     */
    public String stringSerialize(){
        StringBuilder sb = new StringBuilder(1024);
        Base b = Base.BASE86;

        // WorldMapGenerator fields:
        b.appendUnsigned(sb, width).append('\n');
        b.appendUnsigned(sb, height).append('\n');
        b.appendUnsigned(sb, usedWidth).append('\n');
        b.appendUnsigned(sb, usedHeight).append('\n');
        b.appendUnsigned(sb, landModifier).append('\n');
        b.appendUnsigned(sb, heatModifier).append('\n');
        b.appendUnsigned(sb, minHeat  ).append('\n');
        b.appendUnsigned(sb, maxHeat  ).append('\n');
        b.appendUnsigned(sb, minHeight).append('\n');
        b.appendUnsigned(sb, maxHeight).append('\n');
        b.appendUnsigned(sb, minWet   ).append('\n');
        b.appendUnsigned(sb, maxWet   ).append('\n');
        b.appendUnsigned(sb, centerLongitude).append('\n');
        b.appendUnsigned(sb, zoom).append('\n');
        b.appendUnsigned(sb, startX).append('\n');
        b.appendUnsigned(sb, startY).append('\n');
        b.appendJoined(sb, " ", startCacheX.items, 0, startCacheX.size()).append('\n');
        b.appendJoined(sb, " ", startCacheY.items, 0, startCacheY.size()).append('\n');
        b.appendUnsigned(sb, zoomStartX).append('\n');
        b.appendUnsigned(sb, zoomStartY).append('\n');
        b.appendUnsigned(sb, seedA).append('\n');
        b.appendUnsigned(sb, seedB).append('\n');
        b.appendUnsigned(sb, cacheA).append('\n');
        b.appendUnsigned(sb, cacheB).append('\n');
        b.appendUnsigned(sb, rng.getStateA()).append('\n');
        b.appendUnsigned(sb, rng.getStateB()).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", heightData).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", heatData).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", moistureData).append('\n');
        sb.append(landData.toCompressedString()).append('\n');
        b.appendJoined2D(sb, "\t", " ", heightCodeData).append('\n');

        // FIELDS Of this class:
        sb.append(terrainRidged.stringSerialize()).append('\n');
        sb.append(terrainBasic .stringSerialize()).append('\n');
        sb.append(heat         .stringSerialize()).append('\n');
        sb.append(moisture     .stringSerialize()).append('\n');
        sb.append(otherRidged  .stringSerialize()).append('\n');
        b.appendUnsigned(sb, minHeat0).append('\n');
        b.appendUnsigned(sb, maxHeat0).append('\n');
        b.appendUnsigned(sb, minHeat1).append('\n');
        b.appendUnsigned(sb, maxHeat1).append('\n');
        b.appendUnsigned(sb, minWet0 ).append('\n');
        b.appendUnsigned(sb, maxWet0 ).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", xPositions).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", yPositions).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", zPositions).append('\n');
        sb.append(earth.toCompressedString()).append('\n');
        sb.append(earthOriginal.toCompressedString()).append('\n');
        sb.append(coast.toCompressedString()).append('\n');
        sb.append(shallow.toCompressedString());

        return sb.toString();
    }

    /**
     * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
     * This can get the width and height from the String, which makes this probably preferable to using the constructor
     * {@link #MimicLocalMap(int, int, String)}. This stores the last-generated map in this WorldMapGenerator, where
     * it can be used by other code like a {@link WorldMapView}.
     * @param data the output of {@link #stringSerialize()}
     * @return the map that was serialized, as a new generator
     */
    public static MimicLocalMap recreateFromString(String data) {
        int mid = data.indexOf('\n');
        int width = Base.BASE86.readInt(data, 0, mid);
        int height = Base.BASE86.readInt(data, mid + 1, data.indexOf('\n', mid+1));
        return new MimicLocalMap(width, height, data);
    }

    /**
     * Stores a 256x256 Region that shows an unprojected map of Australia, in a format that can be read back
     * with {@link Region#decompress(String)}. By using Region's compression, this takes up a lot less
     * room than it would with most text-based formats, and even beats uncompressed binary storage of the map by a
     * factor of 9.4f. The map data won't change here, so this should stay compatible.
     * <br>
     * This was flipped vertically early in 2023, to match changes much earlier in squidgrid.
     */
    public static final String AUSTRALIA_ENCODED = "Ƥ䒅⒐᮰囨䈢ħ䐤࠰ࠨ•Ⱙအ䎢ŘňÆ䴣ȢؤF䭠゠ᔤ∠偰ഀՠ₠ኼܨā᭮笁␪Цᇅ扰रࠦ吠䠪ࠦ䠧娮⠬䠬❁ỀកᲪ͠敠ἒ慽Ê䄄洡儠䋻䨡㈠䙬坈མŨྈ䞻䛊哚晪⁞倰h·䡂Ļæ抂㴢္࠮搧䈠ᇩᒠ᩠ɀ༨ʨڤʃ奲ࢠ፠ᆙả䝆䮳りĩ(ॠી᧰྄e॑ᤙ䒠剠⁌ဥࠩФΝ䂂⢴ᑠ㺀ᢣ䗨dBqÚ扜冢࿥੢䐠劣ေ¯䂍䞀ၰ๧ᐓ〈ᄠ塠Ѡ̀ာ⠤ᡤŒęጓ憒‱〿䌳℔ᐼ䊢⁚䤿ӣ◚㙀౴Ӹ抠⣀ĨǊǸ䁃း₺Ý䂁ᜤ䢑V⁄樫焠੠⹸⎲Ĉ䁎勯戡璠悈ᠥ嘡⩩‰ನ檨㡕䶪၁@恑ࠣ䘣ࢠᅀᡎ劰桠Өॢಸ熛փࢸ䀹ఽ䅠勖ਰ۴̄ጺಢ䈠ᙠᨭ⿠焠Ӡܼ䇂䒠ᯀԨĠ愜᪅䦥㶐ୀ৅Ƣ*䂕ॹ∠咠р؄У無~⁆Г椠痠ᲩⰣס㩖ᝋ司楠२ญⳘ䬣汤ǿã㱩ᖷ掠Àݒ㑁c‾䮴,⑒僢ᰣ缠ɋ乨͸䁡绑ס傓䁔瀾ሺÑ䀤ो刡开烀੶Ё䈠䈰״Áj⁑䠡戢碠㘀አ䃉㪙嘈ʂø⸪௰₈㐲暤ƩDᬿ䂖剙書࿠㴢㘩Ĉ䰵掀栰杁4〡Ƞ⭀᫠㠰㹨Zコത䂖ࠠⴠ縣吠ᆠʡ㡀䀧否䣝Ӧ愠ⓀᲢಠո*①ӈԥ獀խ@㟬箬㐱ㆾ簽Ɛᩆᇞ稯禚⟶⣑аβǚ㥎Ḇ⌢㑆 搡⁗ဣ刣౅䑒8怺₵⤦a5ਵ㏰ᩄ猢ฦ䬞㐷䈠呠カ愠ۀᲒ傠ᅼ߃ᙊ䢨ၠླྀš亀ƴ̰刷ʼ墨愠  ";

    /**
     * Constructs a 256x256 unprojected local map that will use land forms with a similar shape to Australia.
     *
     * @param initialSeed
     */
    public MimicLocalMap(long initialSeed) {
        this(initialSeed,
                Region.decompress(AUSTRALIA_ENCODED), new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     * Constructs a 256x256 unprojected local map that will use land forms with a similar shape to Australia.
     *
     * @param initialSeed
     * @param noiseGenerator
     * @param octaveMultiplier
     */
    public MimicLocalMap(long initialSeed, INoise noiseGenerator, float octaveMultiplier) {
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
                    h = MathTools.swayTight(terrainBasic.getNoiseWithSeed(xPos +
                                    terrainRidged.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5f,
                            yPos, seedA)) * 0.85f;
                    if (coast.contains(x, y))
                        h += 0.05f;
                    else
                        h += 0.15f;
                } else {
                    h = MathTools.swayTight(terrainBasic.getNoiseWithSeed(xPos +
                                    terrainRidged.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5f,
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

                if (fresh) {
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);

                    minHeat0 = Math.min(minHeat0, p);
                    maxHeat0 = Math.max(maxHeat0, p);

                    minWet0 = Math.min(minWet0, temp);
                    maxWet0 = Math.max(maxWet0, temp);
                }
            }

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MimicLocalMap)) return false;

        MimicLocalMap that = (MimicLocalMap) o;
        // WorldMapGenerator fields:
        if(width != that.width) return false;
        if(height != that.height) return false;
        if(usedWidth != that.usedWidth) return false;
        if(usedHeight != that.usedHeight) return false;
        if(Float.compare(landModifier, that.landModifier) != 0) return false;
        if(Float.compare(heatModifier, that.heatModifier) != 0) return false;
        if(Float.compare(minHeat  , that.minHeat  ) != 0) return false;
        if(Float.compare(maxHeat  , that.maxHeat  ) != 0) return false;
        if(Float.compare(minHeight, that.minHeight) != 0) return false;
        if(Float.compare(maxHeight, that.maxHeight) != 0) return false;
        if(Float.compare(minWet   , that.minWet   ) != 0) return false;
        if(Float.compare(maxWet   , that.maxWet   ) != 0) return false;
        if(Float.compare(centerLongitude, that.centerLongitude) != 0) return false;
        if(zoom != that.zoom) return false;
        if(startX != that.startX) return false;
        if(startY != that.startY) return false;
        if(!startCacheX.equals(that.startCacheX)) return false;
        if(!startCacheY.equals(that.startCacheY)) return false;
        if(zoomStartX != that.zoomStartX) return false;
        if(zoomStartY != that.zoomStartY) return false;
        if(seedA != that.seedA) return false;
        if(seedB != that.seedB) return false;
        if(cacheA != that.cacheA) return false;
        if(cacheB != that.cacheB) return false;
        if(rng.getStateA() != that.rng.getStateA()) return false;
        if(rng.getStateB() != that.rng.getStateB()) return false;
        if(!Arrays.deepEquals(heightData  , that.heightData  )) return false;
        if(!Arrays.deepEquals(heatData    , that.heatData    )) return false;
        if(!Arrays.deepEquals(moistureData, that.moistureData)) return false;
        if(!landData.equals(that.landData)) return false;
        if(!Arrays.deepEquals(heightCodeData, that.heightCodeData)) return false;

        // Fields Of this class:
        if(!terrainRidged.equals(that.terrainRidged)) return false;
        if(!terrainBasic .equals(that.terrainBasic )) return false;
        if(!heat         .equals(that.heat         )) return false;
        if(!moisture     .equals(that.moisture     )) return false;
        if(!otherRidged  .equals(that.otherRidged  )) return false;
        if(Float.compare(minHeat0, that.minHeat0) != 0) return false;
        if(Float.compare(maxHeat0, that.maxHeat0) != 0) return false;
        if(Float.compare(minHeat1, that.minHeat1) != 0) return false;
        if(Float.compare(maxHeat1, that.maxHeat1) != 0) return false;
        if(Float.compare(minWet0 , that.minWet0 ) != 0) return false;
        if(Float.compare(maxWet0 , that.maxWet0 ) != 0) return false;
        if(!Arrays.deepEquals(xPositions, that.xPositions)) return false;
        if(!Arrays.deepEquals(yPositions, that.yPositions)) return false;
        if(!Arrays.deepEquals(zPositions, that.zPositions)) return false;
        if(!earth.equals(that.earth)) return false;
        if(!shallow.equals(that.shallow)) return false;
        if(!coast.equals(that.coast)) return false;
        if(!earthOriginal.equals(that.earthOriginal)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Hasher.marax.hashBulk(stringSerialize());
    }

    @Override
    public String toString() {
        return "MimicLocalMap { width: " + width + ", height: " + height
                + ", landModifier: " + landModifier + ", heatModifier: " + heatModifier
                + ", seedA: " + seedA + ", seedB: " + seedB
                + ", zoom: " + zoom + ", noise tag: " + terrainBasic.getTag()
                + "}";
    }
}
