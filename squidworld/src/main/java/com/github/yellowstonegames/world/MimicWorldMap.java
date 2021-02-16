package com.github.yellowstonegames.world;

import com.github.yellowstonegames.core.MathTools;
import com.github.yellowstonegames.core.TrigTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;

/**
 * An unusual map generator that imitates an existing map (such as a map of Earth, which it can do by default). It
 * uses the Mollweide projection (an elliptical map projection, the same as what EllipticalWorldMap uses) for both its
 * input and output; <a href="https://yellowstonegames.github.io/SquidLib/MimicWorld.png">an example can be seen here</a>,
 * imitating Earth using a 512x256 world map as a Region for input.
 */
public class MimicWorldMap extends EllipticalWorldMap {
    private static final long serialVersionUID = 1L;
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
    public static final String EARTH_ENCODED = "Ƥ䊅⑃л䢤㱢ġ٤Ȩࠪ捘Ϝᇌᐰࡐဣ₈䁭âŴനð᱀ᄣϢĢሢŤ\u0087倰…䇍Ĉథ䠨䇰ᢐ࠲ࠨ¨I䗴₧≠だₐ‴䞑ห攠℡ı©ý 傠ʓᤣ窠猡ᄡᤣഡढ愠祒㈡戢ቴ摊╓̀⫀ᄡ亨䜅ਁ灑㢠扠㏁唼⽪䐡㘢ᄡ碠䑅♁儰\u0EA8ӤϻıĒ䁤\u0A7E〨ᠪ䔲ੂƲ您䀪⁝Ꭱ䮦⁄Њǰᅺ兎⊱๚䮯㞵⌱祥䆠ౠ↤\u2E75সסˀӄઃق※瀣ǥ呔²Œ屚ౠ⿈ᄦ䄸ˤűᕱÈ䁁ݕ㮱ࠥ否噂ő<ɠኰ`⤠䍠⸳Ӱᒛ ᠪ䐝⬬֨台䑆ื䊈摤အᐡ祰ᾳ䠠樢ぞĩ昫䋮♡⊓攔㲫₧䆨※ࠨ䉒Ṗࡊ孱⡠Ằܨ㔐湖⍰ᇊ֦٠灆䣝ཤĄ䂦\u2BE3Ƒ⪱ؤ掴ᅺ‥㠯䌙⮘⢯≓䅴፧▪ᄬͼ㊀མ㍹䍁刲⋹䋨掞䭠⁅Ỽᱭ䨡䲠ℶᧄđ\u0090怼䠤向⡍ڪ䋕ڊ佈䈱ޔ 䊱⮛ࣷ䔙:†涽ࢴ䵂ಀ愠Š⪠瘮囼䉐Ԭρ䧂ᡕ䞩y⁊ࠡ䨠㵞⚯灱僋㕰جɁ&⁕㓄eĂ厠䒓ᆶ㏣ᵢô䁘〩㈥⁃\u0E5C䧀ᆝ┽ឨ嚤ᴶ䗣䌰\u173Dᔄǅ$⇷[㏋ᤰ̴İȬ䈠䩀∨Ź¨အ䬡楩้ུ\u0ABA偒\u0A4A济Ђf‡С㪀⮱Ƞ彠⸨懚㦄焐෨䄠⧀ᒠ昤အ™穝䄻尮⠲ၠฤԴ琧吅ै䋱Yࠩ所䀡⠬䒈…吧̚ȴw\u2064\u0E66\u0096瀴䕡E倠䏁䀷ࠨ䄠Ḁᔤ\u0084䀨\u2062歵䮡灱ॢॴ*ၠՒԕT䠡欀㲡䈰䐺⤜摱₹ࢽ朆睐抈笹悜ȼ䝂䀺ᠭⴘܣ㔰҈ć\u0084ƤA䂄檢b 抸䨳⏠禡Ű㜳䀤ᰲ䁩扚С㒠稻႐䀻⠠ₔ‸ᆢ珎ӑ㬭ⷒ㨻䇚ഥჼ\u202DⰤ窵ㄧ眀塀䈼ᘣᢨဠآၠªѡE〱ȠN∨»¼҃㨭ဠ䄡灅ओ繶愊倷ޢ\u0092ᠾ䕹2瀫ኌ∰2Ġ❀×₠നྠ㓞ښѠښڐ曞掁湧́<倳Ƽ͡Җ䀳㠥僨⚪രጨ攠◌ტ抨န硆ᄦᛈဠ㌣⭤ࠢ流ࡀ͔ƄȲ揸䀢堮Ӡ壩ⴘㅘ䄠㝀ᔤâ䁅ୁ㕓仲噄泩戬ू噱♌㋿ٓ㜯࣪㒑٣⩥䢢\u009D恂ǋ棎ө㛁嚅⟓䂀ᗍ勥῀ʬɈ䲡卪卐䐰廼䋀᮲噰䀷䠤ℨ㨨砱㭀煐ٰⷤፗ愠㽀ᄤc䂐⡠獑䠣熡࠴枤䩀ȴΪ\u2D29ێ咨䋍ล戵ⱼ䝬Ⲫ䂀ೃʙ䐰ŊxḨÛºᔥ䂠ỰФᒕ丫㎻㏪桬ମ㈉Â⡎ޠⰄཐ梨『ဤ䝼࠸䴀䳓⠱慠\u0B98ЊṂ瑡⾀⊰䐧¿ᡌ灁\u2438徵-〱ᤰ俿煈崊㏀Ҭ䢆祡\u2D76䐺愎朰W8ԩᠡ䄞ԑ垰䋀㷵⺇Ӡ䀥㘥媄˰䎯棡暼匳ǆဠ㤠Ⓕ沋ἡ䤯恁㊴彬ƶ䘇梉戁Ῡ䐭嶱ㅕ䆰⍁ᢅᚐク䄈Ԩ㸳ǌᡀ㡀Ͼ3ᳲ¦≠捁呪䅐⭭擘ཀ䐡खᢠ䴡䮙\u18AC\u2060ՄȢっ獍Ϡ䌡Ẻ䇡罥樜劺偐⭈ᨄJ怤ᅐ‰⡓☨縊壎䓡ᨢᄠް\u0A49ᤰǘₐ䍠娿䴨䦠⠼ؠ㸈¢!倫\u2060⏰㩂ПᖃЦ㔰ࡈ¹䁝匒ⴠ㠰䂤尻ㅬ幤ࠫၺ↦\u0560τʀ⽀၆ᓰस㉢ఴ絪⤚䢪K〱ĠᅰÁ夿䵤ဣ媡劬䇓䡩縦ˣބ⽠дǢ⢦㔼䯀牥↿洍࠰\u009Dòပᤲ䳹ŃⰦ⧤Ḧ㲔Өᘅ⣂ɼ\u0A29儠ǘఢ挰ť\u0092᩵⠰Ø䀤Ս⬭屔⺲6࠭㴱崠碲䃰‡⨠ࡊ\u202D…ᘣ⋂䈠ᇀಊC戮䣑ဣ羡࠰Ò䄘\u0B59派䃮唢I瀿䎝ୱ療÷ᙥⳞ吠祀ᄤ;怿ͪ㥬y恀☠☨ᡂ5倩₱\u0984ᴧ砭⫃䑵Ӱ歌榚◠㜦䁘რ⻈劳\u1CA5ࡠ\u07BAŚ⾥穌䙀ㆥᢍ桠儣⍁᧡Әႈ尮᭪〞⅁䂙Ӱᤡᠸ\u0382瀴ᅯᙣ桠ьGᵁ睺煒榯䁵䄱\u0BBC㶜ጢ扩䁴榤摠ƺቀ⑨အ呠ଇ䠸ρᏩ䅸塂呧ͬਐ၊䲘፣䠺䨝ᄯ汯 ⶁ劄ᴧ猢جΧૡࠠ⺠ፉ䂠ϼ؞学撐䵃ⴹ恀\u2BF4䮅烒ࠨ⭬灰䀥␤\u2060۬ˀᎃ䑥Ӑ⅏゚ᛠ到č$ౢത秆ಠᗎࢹ⌤\u2060ö̂灳恄➦䐧傚Ⓚ嬹ᬲᄣ纥⛀Ⴠ稬ʮ溮䊫ฺ砥䢤啂倢⒠䆅羐♱ྷ淮Ȁුロ⟘⮌媚枧籠畴䐫ĕ戻ٰj䄈\u0ADC䆰Ԡࠐੈ໕Ġᴈਙ害素ரཔ爸ᠴ㖂剈䍬⍄E偘䊢畤桯\u08C0㎫䱤ঀᠰ䴠ᯨڈ稱ǆ⥡灔柔ണ㬧䉩䰨/䁾◬咨ý灠ጶħҠٴЊ竉烈̰᰾悋¢=࠱᧐㡈瑀º[ᐇ䝧㈑㍑擃䞈إচⶑ妷慈ဣŁ࠰Ĩ䁺ॠ㳢㳨ဠ甠☵⌈ᄢ3ဨ䇾ሃ嬰䈸➬惧༖㐊䂠तզ䰫䈌䲲㕯ƨ〡䬡䶯〮Ӳ[ࠨ㵘〣\u0081⠼㰏ƢF㠫䆨ᑡ䗕←‧ᡡ₀䱮ॅ砿䋸⫧‴Α㈿Ô\u1FF0伶䊄‧务瘵㈸֒ࡔܰᾤ\u20F1䊇᳃䃸ᔢ*〪入\u1CA9瀾䡨怢帠ࠫȠⓀᬗ改坊⍄ށؤبJ\u2064ݢ⎃夦␁磣憨㢠桽ሠᭀ\u0C65⨢㕈ᔇ犖\u0EFC甡₶χ峣壄ǁ℥傅\u1CA7䢭Ȱ⤭䢱đ䲳₭䭻搲㉺㎤ᾔӮ䘌攀丑≍⫉ʼ㌲沟ᐂ灘㿠ㆦ摌Áᤞ⃮䓲娾昱⪬ĮЮ℣ᐔ١廹曀■⁼ዔ․ḡ㨕䅬⥴㫞囘⤩䡅ডୣĠ\u1778ศ༠ᆄᎅ埀Ϛþ所濗♀†弢娐㗀怢䴢ᑊ←⭡屒☪⤤ᬌ\u0D51唰ǳ¤⠡庠Ըి᪡姲娑䅢R㠪嵘\u09DB攺݂…ㅁ䙆䂜∥㧏䤭䙭⠽懠㐭嵈š  ";

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
                rx = width * 0.25f, irx = 1f / rx, hw = width * 0.5f,
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
