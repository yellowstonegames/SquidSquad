package com.github.yellowstonegames.world;

import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.place.Biome;

import static com.github.yellowstonegames.core.DescriptiveColor.*;

/**
 * Takes a {@link WorldMapGenerator}, such as a {@link LocalMap}, {@link RotatingGlobeMap}, or {@link StretchWorldMap},
 * and wraps it so that you can call {@link #generate()} on this to coordinate calls to
 * {@link WorldMapGenerator#generate()} and {@link com.github.yellowstonegames.world.BiomeMapper.DetailedBiomeMapper#makeBiomes(WorldMapGenerator)}.
 * For extra convenience, you can get a possible interpretation of how the generated world would look by calling
 * {@link #show()}, which returns a 2D array of ints as RGBA8888 colors.
 * <br>
 * Created by Tommy Ettinger on 9/6/2019.
 */
public class WorldMapView {
    protected int width, height;
    protected int[][] colorMap;
    protected int[][] colorMapOklab;
    protected WorldMapGenerator world;
    protected BiomeMapper.DetailedBiomeMapper biomeMapper;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getColorMap() {
        return colorMap;
    }

    public int[][] getColorMapOklab() {
        return colorMapOklab;
    }

    public BiomeMapper.DetailedBiomeMapper getBiomeMapper() {
        return biomeMapper;
    }

    public void setBiomeMapper(BiomeMapper.DetailedBiomeMapper biomeMapper) {
        this.biomeMapper = biomeMapper;
    }

    public WorldMapGenerator getWorld() {
        return world;
    }

    public void setWorld(WorldMapGenerator world) {
        this.world = world;
        if(this.width != world.width || this.height != world.height)
        {
            width = world.width;
            height = world.height;
            colorMap = new int[width][height];
            colorMapOklab = new int[width][height];
        }
    }

    public WorldMapView(WorldMapGenerator worldMapGenerator)
    {
        world = worldMapGenerator == null ? new LocalMap() : worldMapGenerator;
        width = world.width;
        height = world.height;
        colorMap = new int[width][height];
        colorMapOklab = new int[width][height];
        this.biomeMapper = new BiomeMapper.DetailedBiomeMapper();
        initialize();
    }
    
    public WorldMapView(long seed, int width, int height)
    {
        this(new LocalMap(seed, width, height));
    }

    public final int[] BIOME_COLOR_TABLE = new int[66], BIOME_DARK_COLOR_TABLE = new int[66];

    public void initialize()
    {
        int b;
        for (int i = 0; i < 66; i++) {
            BIOME_COLOR_TABLE[i] = b = describeOklab(Biome.TABLE[i].colorDescription);
            BIOME_DARK_COLOR_TABLE[i] = darken(b, 0.08f);
            if(i == 60)
                BIOME_COLOR_TABLE[i] = BIOME_DARK_COLOR_TABLE[i];
        }
    }

    /**
     * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
     * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
     * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
     * {@link DescriptiveColor#describeOklab(CharSequence)}, or might be made manually, in advanced cases, with
     * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
     * @param similarColors an array or vararg of packed int Oklab colors with at least one element
     */
    public void match(int... similarColors)
    {
        int b;
        for (int i = 0; i < 66; i++) {
            BIOME_COLOR_TABLE[i] = b = DescriptiveColor.offsetLightness(similarColors[(Hasher.gremory.hash(Biome.TABLE[i].name) >>> 1) % similarColors.length]
            //, DescriptiveColor.describeOklab(Biome.TABLE[i].colorDescription)
            );
            BIOME_DARK_COLOR_TABLE[i] = darken(b, 0.08f);
            if(i == 60)
                BIOME_COLOR_TABLE[i] = BIOME_DARK_COLOR_TABLE[i] = darken(describeOklab(Biome.TABLE[60].colorDescription), 0.08f);
        }
    }

    public void generate()
    {
        final long landA = Hasher.randomize(world.seedA), landB = Hasher.randomize(landA ^ world.seedB);
        final long heat = Hasher.randomize(landB);
        generate(world.seedA, world.seedB, 1f + ((landA & 0xFFFFFF) + (landA >>> 40) - (landB & 0xFFFFFF) - (landB >>> 40)) * 0x1p-27f,
                (heat >>> 40) * 0x1p-24f * 0.375f + 1.0625f);
    }
    public void generate(float landMod, float heatMod)
    {
        generate(world.seedA, world.seedB, landMod, heatMod);
    }
    
    public void generate(long seedA, long seedB, float landMod, float heatMod) {
        world.generate(landMod, heatMod, seedA, seedB | 1L);
        biomeMapper.makeBiomes(world);
    }

    /**
     * After calling {@link #generate()}, you can call this to assign values to {@link #getColorMap()} and
     * {@link #getColorMapOklab()}. This method returns the RGBA colorMap, but it assigns to colorMapOklab
     * at the same time, so you can use the Oklab colors with methods like
     * {@link DescriptiveColor#lighten(int, float)} and {@link DescriptiveColor#lerpColors(int, int, float)}.
     * @return the RGBA8888 colorMap
     */
    public int[][] show()
    {
        int hc, tc, bc; // height, temperature, biome codes
        final int[][] heightCodeData = world.heightCodeData;
        float[][] heightData = world.heightData;
        int[][] heatCodeData = biomeMapper.heatCodeData;
        int[][] biomeCodeData = biomeMapper.biomeCodeData;

        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if(hc == 1000)
                {
                    colorMap[x][y] = toRGBA8888(colorMapOklab[x][y] = BIOME_COLOR_TABLE[60]);
                    continue;
                }
                tc = heatCodeData[x][y];
                bc = biomeCodeData[x][y];
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            colorMap[x][y] = toRGBA8888(colorMapOklab[x][y] = lerpColors(BIOME_COLOR_TABLE[50], BIOME_COLOR_TABLE[12],
                                    ((heightData[x][y] - -1f) / (WorldMapGenerator.sandLower - -1f))));
                            continue PER_CELL;
                        case 4:
                            colorMap[x][y] = toRGBA8888(colorMapOklab[x][y] = lerpColors(BIOME_COLOR_TABLE[0], BIOME_COLOR_TABLE[12],
                                    ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower))));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        colorMap[x][y] = toRGBA8888(colorMapOklab[x][y] = lerpColors(
                                BIOME_COLOR_TABLE[56], BIOME_COLOR_TABLE[43],
                                Math.min(Math.max(((heightData[x][y] + 0.06f) * 8f) / (WorldMapGenerator.sandLower + 1f), 0f), 1f)));
                        break;
                    default:
                        colorMap[x][y] = toRGBA8888(colorMapOklab[x][y] = lerpColors(BIOME_COLOR_TABLE[biomeMapper.extractPartA(bc)],
                                BIOME_DARK_COLOR_TABLE[biomeMapper.extractPartB(bc)], biomeMapper.extractMixAmount(bc)));
                }
            }
        }
        
        return colorMap;
    }
}
