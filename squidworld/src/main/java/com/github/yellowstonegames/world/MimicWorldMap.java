/*
 * Copyright (c) 2022 See AUTHORS file.
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

import com.github.yellowstonegames.core.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;

/**
 * An unusual map generator that imitates an existing map (such as a map of Earth, which it can do by default). It
 * uses the Mollweide projection (an elliptical map projection, the same as what EllipticalWorldMap uses) for both its
 * input and output; <a href="https://yellowstonegames.github.io/SquidLib/MimicWorld.png">an example can be seen here</a>,
 * imitating Earth using a 512x256 world map as a Region for input.
 */
public class MimicWorldMap extends EllipticalWorldMap {

    public Region earth;
    public Region shallow;
    public Region coast;
    public Region earthOriginal;

    /**
     * Constructs a concrete WorldMapGenerator for a map that should look like Earth using an elliptical projection
     * (specifically, a Mollweide projection).
     * Always makes a 512x256 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link MimicWorldMap#MimicWorldMap(long, Noise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, DEFAULT_NOISE, 1f}.
     */
    public MimicWorldMap() {
        this(0x1337BABE1337D00DL
                , DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
     * The initial seed is set to the same large long every time, and it's likely that you would set the seed when
     * you call {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
     */
    public MimicWorldMap(Region toMimic) {
        this(0x1337BABE1337D00DL, toMimic, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
     * Takes an initial seed and the Region containing land positions. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param toMimic     the world map to imitate, as a Region with land as "on"; the height and width will be copied
     */
    public MimicWorldMap(long initialSeed, Region toMimic) {
        this(initialSeed, toMimic, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
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
    public MimicWorldMap(long initialSeed, Region toMimic, float octaveMultiplier) {
        this(initialSeed, toMimic, DEFAULT_NOISE, octaveMultiplier);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
     * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
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
    public MimicWorldMap(long initialSeed, Region toMimic, Noise noiseGenerator) {
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
    public MimicWorldMap(long initialSeed, Region toMimic, Noise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
        earth = toMimic;
        earthOriginal = earth.copy();
        coast = earth.copy().not().fringe(2);
        shallow = earth.copy().fringe(2);
    }

    /**
     * Stores a 512x256 Region that shows an Earth map with elliptical (Mollweide) projection, in a format
     * that can be read back with {@link Region#decompress(String)}. By using Region's compression,
     * this takes up a lot less room than it would with most text-based formats, and even beats uncompressed binary
     * storage of the map by a factor of 5.8f. The map data won't change here, so this should stay compatible.
     */
    public static final String EARTH_ENCODED = "Ƥ䊅⑃л䡶呢ġˁɐܰ〉Ⱔた硦ท䐨ࠨ偐⁀⻠\u20C0䁠⢠䂵ªᐳࠨ僡Ҡ朦䈢ప\u082F〬⁂䁸䁤䁐䁠灠₀⁀เဨူዐ\u087Cぞြㄥモ䀰䁠䡠㣠Ⅰ⢥榤嬲Ѓƫ♇\u0EBEᅹ兀ᄀᅀ㽀ᲁⅠ塠⁄⁀ɀᏻ∧儣⳦琥∣嶌ઠ幠✯䁰䱏␥䨣䓽⨶慠Հᄴ≨䐢䘢ᄠ⒠ᆂⅈ䭰༨߅ቂ⸍演狼焥ࣱࠫ␤∣眡ⶨⶁ\u0FBD\u2453䞁㙄⃐ᒳ\u087C㞑ㄛ䞱堧Р⿊ı*㓍⣀ᡰࡨ܄ϱ勽фΚ恋ှ校Ⱖ箨圠暠䑠ⷄ\u0E91ࠠ昤ሠ椠囖㴰̴ϱ䉎ဩ㖦Ԡ≠㴰ڄʱ䈭ဩ尧㧘㛛䧗儠㥠ズ≪\u0874ڣ儫梠ᨈᄨÒt\u1259栥戠⢤\u202DЪ䒙n⁀hq⨶૰ɼυ᭠\u1CACŤ䡊穀ਸۑ䋂\u0530¢Ąയˡ⋡=搵䷥唠⩌∰Ôʜ㱫ԈȆİ\u1AC0ૺߊ牴Ƹ䐢⤡ѠῸƂ楱㝅伬⅘\u09DB䇀椨ᭇ侀燽墕䁔ñáʿᐡᤡ㥠ⵈ\u20F5湠\u1AF0འ椠牠ʾ䇑ⷭℬᘮႠ㶀ᄨǶŤ⩔䵄ŭĔឦ搢犡Ѡ᪰ࡣ壝䐵ૄÅĕ㢸✲\"䂚仭ၓ⨲ᶫ⬱祠᠐ڏ穓ര≥䱢圀\u0FBD̶㺢ᩩ䅄碠⛰ᩯ䠷\u0D84妣点ࡠ\u1AD0ࢤ䣌喣ᓴĹ\u0093༼ㄳ͊ㄨዄ\u0A46朱V倲揬⺽挬ἴ⍨㭵\u2028焢⌰࣮憄̧⍨榻䨠რᕬᄠᴀဣ戣ᠮ栺㕠സω節˴˴㗫䅄⣿৸㜼簄دॸਦ删瑠ㄪለ\u202Dሥ庠\u0A84ࢠᵠǈހ婄玑䏘䝝9ူ䈠㯠∫⓿㢨ōäᱤč恡ࢢK偙琠溠笰ǊŶᵨ5ĔʤĄ₍κ\u009A⁉ـ㙼搡恠㍫墈᧨䙸挣∠ᑀ⁈䁃㶀ᄌԥ催ގ烊㩨Ɲ¨۬ÚゑȘ㔮䥒ᤃעࡂᲁ劁佲9あיEࠨ㡈⁾呅⸹㬴焰ͪŴ⺄簲璡兖▩⥆ય⡄ڭ☊㒢⬠▰ࣧ㚽䱂碕ʠ硊๏ࢠѰੀ硠èѡP〠㎼䀹ࠢ⇄ጮ㷉ℨ\u0085¤᥆ೢᘲ狈屰⣈奌ა䗌塃呁ⲷṌ䔙 櫇ၕⵈ⫠▍㏕焣ަ狋\u1FF0勂ፒᡄ㽅抨Ḳ炞\u0085呞ᱡС䋧ࣗ䒡朙复ᳬŌॶ\u08CE㓄⊡⠭૰叄㡃縫\u1CA8㦤အ⸣綔䳱劀沂ࡇ沃\u244Eࠡ撠⨨浂ᆷྐ禮䟬届ダ失䖓湛ݙ岯\u31BC⅀ۤœ珤ሆ⭢䐄窕⎬㚤㒭䥕⁐懐j\u009Fम椨ᑃ夊྆篌ℬ㖢繮㶨\u2028㐡劖嫢〣ຯ✑㋠䓂Ꭰ\u2434۬Þ傆ි܂丗ᡠܘڛ畞✱\u008CР 䀠Ю場㽑夑ᮡ℠ᔀỆःۜ䔕㱤㜽䍦\u082E䖍≴အᘢ♍\u0C71ᩙ䰵䉠Ƙ\u0084皲溉泀Ȓ䍵\u2E6A欖≸䍽ℰƶ²在k䄕ڔ₠ᕰҁ浩校Ⴀ䤰①䪐⩋湀ݾƈエ恲䐰Ϫ6Ạ噀ZƬ\u08D0ᑂ\u0BACߴ⡈╸䋪䐻⡠筫℠ؐྱⲢ㦸ဢԡ琳\u318Fဠ䣠፹紌ࡣɁᢖ㧤|怮㈠ڲᄢ\\ᠶ䴨\u0EF0簲秸‧ม缧桲㚠桠ìʨᾤႋ䛺呩㻮ༀᐸ㴠ጐǸ䪽惈ᅢ奊楸ť\u0881Μ巣怩ࠡ䡠ᜈ\u0092梉掼喭Ⴀྈߦ㽯⁔企*䠨䂔⇭到Ƹ䣴⭬嘵⇋Ʋ㡀͇ǯᡢ⸾䎐⋧ᐥР潠㩤\u0094怠挐㣠㸵Đᚄჼ䵁ਿᄠઈೱ䅠䴰ဠ⾡࠰ĵ¨ࠠ㖠\u0EE8䂸䠡溠䦯₠Ј͆Ⰶᓤࠡ㫠笨!䁺䐞岭抐\u1758ᘡ桬ঋᝂȠ∀\u0CF0㨭ㅰ\u0083䱴⚨⇪僐ⳡ忰ο嵃ع壆ᓀ๏ŀỢ㛪䱡-䠠౯䫁ଣℌẺ睔㋴ዩᡊ乀憫偭\u0EE9㔲恉䛪ڳ∩Җ〫䴘烦㫌掀Ⱒ䱎݃乐樮ˢ硤䚰㼧プ䢡㫿㕤ᾢ嵇⠌溇傖¬䈤怷歐⨩☈ଂ䐧爠રી楀̮īᡃ△ᨉᥬ\u2060ҴŰ㢤K怦ㅉप÷␠ᓠʮ䡠˔ʴ戦暑\u0080㼬㨭\u0BE1罸ᔠẴர䒡傜\u0D04ᐶ䁄ቢ示⋈㞃⛄ⲕ截᥆㘢*倬玸ޢ݉唠䔠桠ӕΨ․ġ伩ò♁ࡠՄ¸ढበͺÈ⢤\u0095⁁\u007F͇䭮Ш㈌ᄚ䮅аĕ¤໑坑ᆾ嚱䩻ᙀදݧ数ḩ听䊀夰䆘婢疢䁰㎡\u128E␠⬀⢡㥨⠬㬽᳕Թ勝ဴ們㗆䊔弄橵ݭ睫䣡ⅰሾⱏᛰ潉推‧稠婘䈠㌀ᔲ≀⍐ᯈ㊓䦼ऩᬄ⎸ମ犡ᱢ㶆✲碣媉ˡ孨⁘Ẵබ愠ၐเ㛁↟匱嵽扬䠂ڤ唘₮\u10CBĥ東Ĵ᷂猱\u18ADͧ曱〨\u2CF8䂠̸̶㽁ฬ䞨㖽掺٢⦤\u0BC4ၤ笱呀⩬ᳲԝᘣ\u202Cࢳ呋毘ޤ彺䅐᧨昙ଵ࠰û䃙仙䍸䇖ᔊ䑚惪ᖢ栳Ɋᬱㄇ䗡⤾䅼墯䚶䄲…ሡ㰩䊌ᡠ拠\u05FD°愤z怸ᇚ彐屆䙕䈧㤔吣儱Ġ\u0EE8Ǒ\u083Fㇴဠ㺡䏶㏅倣ỡ\u0A7E䆉ϓ碥∠ᠰت၀·Hର䥀:ſ\u1AE3э栘箩ၷ€珨a䁠̄͗⿐⢒円磬エࠡⱠ璩甬䊐尤慶厣\u243C\u0588ᐇ摠Ǵǭ⤥煇烆䀤إҚР㛠ⳡⱫ爦單⩶ࠤ暨ᄨဣἡ惋䄠တ\u0BBB測ᇌᏓ佔寄ⓡ㎏䠬╬晽䦪䁍ᆭㄡᚰぼ∤\u009D⁁Ɉαᬶ㐠㩠ƣ㹷暐笡⛽店н᪓ጱ㥈䄲毼ၬ⋀㬧緢ࠠ\u1AE0㍯ᚾ⽠ⰸ↘ۓୣΨ‣娠㔯滎β泫ⴚ䈨ù䁌楀崥ᢋ劍刻㭨嗭儍ⅴඦݤ\u05F9昤媠ϼŭ䀢琤獩ঊワ壗守䳤犋ʒ⦻↫᱐⡂杛愎ူ罠䓉傊ਰ儋湀᱗炟歨䀯㴤\u2060ƺኔ䶢\u0BE5ᅄ䦮恄ࢣ㎁䨅眶䈼澞․ᘡ䩪惤纻ᝊ䠔窃䤜焅࠵夗ᇂ橂̾啬癚䅀佷፱Ȅ棚⁃ցৄ悳‧䴣抻䂣༧\u0A4E͋ҭၤ嫛灬瑕\u0BA2◟璼絓堻ᇔⅺ≠  ";

    /**
     * Constructs a 512x256 elliptical world map that will use land forms with a similar shape to Earth.
     *
     * @param initialSeed
     * @param noiseGenerator
     * @param octaveMultiplier
     */
    public MimicWorldMap(long initialSeed, Noise noiseGenerator, float octaveMultiplier) {
        this(initialSeed,
                Region.decompress(EARTH_ENCODED), noiseGenerator, octaveMultiplier);
    }

    /**
     * Copies the MimicWorldMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a MimicWorldMap to copy
     */
    public MimicWorldMap(MimicWorldMap other) {
        super(other);
        earth = other.earth.copy();
        earthOriginal = other.earthOriginal.copy();
        coast = other.coast.copy();
        shallow = other.shallow.copy();
    }


    /**
     * Meant for making maps conform to the Mollweide (elliptical) projection that MimicWorldMap uses.
     *
     * @param rectangular A Region where "on" represents land and "off" water, using any rectangular projection
     * @return a reprojected version of {@code rectangular} that uses an elliptical projection
     */
    public static Region reprojectToElliptical(Region rectangular) {
        int width = rectangular.width, height = rectangular.height;
        Region t = new Region(width, height);
        float yPos, xPos,
                th, thx, thy, lon, lat, ipi = 0.99999f / TrigTools.PI,
                rx = width * 0.25f - 0.5f, irx = 1f / rx, hw = width * 0.5f,
                ry = height * 0.5f, iry = 1f / ry;

        yPos = -ry;
        for (int y = 0; y < height; y++, yPos++) {
            thx = TrigTools.asin((yPos) * iry);
            lon = (thx == TrigTools.HALF_PI || thx == -TrigTools.HALF_PI) ? thx : TrigTools.HALF_PI * irx / TrigTools.cos(thx);
            thy = thx * 2f;
            lat = TrigTools.asin((thy + TrigTools.sin(thy)) * ipi);
            xPos = 0;
            for (int x = 0; x < width; x++, xPos++) {
                th = lon * (xPos - hw);
                if (th > -TrigTools.PI && th < TrigTools.PI
                        && rectangular.contains((int) ((th + 1) * hw), (int) ((lat + 1) * ry))) {
                    t.insert(x, y);
                }
            }
        }
        return t;
    }

    @Override
    public int wrapX(final int x, int y) {
        y = Math.max(0, Math.min(y, height - 1));
        if (x < edges[y << 1])
            return edges[y << 1 | 1];
        else if (x > edges[y << 1 | 1])
            return edges[y << 1];
        else return x;
    }

    @Override
    public int wrapY(final int x, final int y) {
        return Math.max(0, Math.min(y, height - 1));
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
                h, temp, yPos, xPos,
                i_uw = usedWidth / (float) width,
                i_uh = usedHeight / (float) height,
                th, thx, thy, lon, lat, ipi = 0.99999f / TrigTools.PI,
                rx = width * 0.25f, irx = 1f / rx, hw = width * 0.5f,
                ry = height * 0.5f, iry = 1f / ry;
        yPos = startY - ry;
        for (int y = 0; y < height; y++, yPos += i_uh) {

            thx = TrigTools.asin((yPos) * iry);
            lon = (thx == TrigTools.HALF_PI || thx == -TrigTools.HALF_PI) ? thx : TrigTools.HALF_PI * irx / TrigTools.cos(thx);
            thy = thx * 2f;
            lat = TrigTools.asin((thy + TrigTools.sin(thy)) * ipi);

            qc = TrigTools.cos(lat);
            qs = TrigTools.sin(lat);

            boolean inSpace = true;
            xPos = startX;
            for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                th = lon * (xPos - hw);
                if (th < -3.141592653589793f || th > 3.141592653589793f) {
                    heightCodeData[x][y] = 10000;
                    inSpace = true;
                    continue;
                }
                if (inSpace) {
                    inSpace = false;
                    edges[y << 1] = x;
                }
                edges[y << 1 | 1] = x;
                ps = TrigTools.sin(th) * qc;
                pc = TrigTools.cos(th) * qc;
                xPositions[x][y] = pc;
                yPositions[x][y] = ps;
                zPositions[x][y] = qs;
                if (earth.contains(x, y)) {
                    h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(pc + terrain.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                            ps, qs, seedA)) * 0.85f;
                    if (coast.contains(x, y))
                        h += 0.05f;
                    else
                        h += 0.15f;
                } else {
                    h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(pc + terrain.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                            ps, qs, seedA)) * -0.9f;
                    if (shallow.contains(x, y))
                        h = (h - 0.08f) * 0.375f;
                    else
                        h = (h - 0.125f) * 0.75f;
                }
                heightData[x][y] = h;
                heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
                        , qs, seedB));
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                        , seedC));
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
        float heightDiff = 2f / (maxHeightActual - minHeightActual),
                heatDiff = 0.8f / (maxHeat0 - minHeat0),
                wetDiff = 1f / (maxWet0 - minWet0),
                hMod,
                halfHeight = (height - 1) * 0.5f, i_half = 1f / (halfHeight);
        float minHeightActual0 = minHeightActual;
        float maxHeightActual0 = maxHeightActual;
        yPos = startY + i_uh;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = (float) Math.pow(Math.abs(yPos - halfHeight) * i_half, 1.5f);
            temp *= (2.4f - temp);
            temp = 2.2f - temp;
            for (int x = 0; x < width; x++) {
//                    heightData[x][y] = (h = (heightData[x][y] - minHeightActual) * heightDiff - 1f);
//                    minHeightActual0 = Math.min(minHeightActual0, h);
//                    maxHeightActual0 = Math.max(maxHeightActual0, h);
                h = heightData[x][y];
                if (heightCodeData[x][y] == 10000) {
                    heightCodeData[x][y] = 1000;
                    continue;
                } else
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
                heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6f) * temp);
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
