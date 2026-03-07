package com.github.yellowstonegames.world;

import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;

@Beta
public class AlienBiomes {
    public static Biome[] generateAlienBiomeTable(EnhancedRandom random, Language namer, int waterRating,
                                                  int barrenColorOklab, int livingColorOklab) {
        Biome[] table = new Biome[66];
        System.arraycopy(Biome.TABLE, 60, table, 60, 6);
        switch (waterRating){
            case 0: {
                String coldDesert = namer.word(random, true);
                for (int m = 0; m < 6; m++) {
                    int h = 0;
                    int i = m * 6 + h;
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], coldDesert, DescriptiveColor.unevenMix(barrenColorOklab, 50, DescriptiveColor.SKY, m + 1));
                }
                String coldLowlands = namer.word(random, true);
                for (int i = 36, m = 6; i < 60; i += 6, m++) {
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], coldLowlands, DescriptiveColor.unevenMix(barrenColorOklab, 50, DescriptiveColor.BLACK, m));
                }
                String hotDesert = namer.word(random, true);
                for (int m = 0; m < 6; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], hotDesert, DescriptiveColor.unevenMix(barrenColorOklab, 50, DescriptiveColor.SKY, m + 1, DescriptiveColor.ORANGE, h + 1));
                    }
                }
                String hotLowlands = namer.word(random, true);
                for (int m = 6; m < 10; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], hotLowlands, DescriptiveColor.unevenMix(barrenColorOklab, 50, DescriptiveColor.BLACK, m, DescriptiveColor.ORANGE, h + 1));

                    }
                }
                break;
            }
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: {
                String coldDesert = namer.word(random, true);
                for (int m = 0; m < 6 - waterRating; m++) {
                    int h = 0;
                    int i = m * 6 + h;
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], coldDesert, DescriptiveColor.unevenMix(barrenColorOklab, 50, DescriptiveColor.SKY, m + 1));
                }
                for (int m = 6 - waterRating; m < 6; m++) {
                    int h = 0;
                    int i = m * 6 + h;
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], namer.word(random, true), DescriptiveColor.unevenMix(livingColorOklab, 10, DescriptiveColor.COBALT, m + 1));
                }
                table[36] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.COAST, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.GRAY, 3));
                table[42] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.RIVER, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.SKY, 4));
                table[48] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.LAKE, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.SKY, 5));
                table[54] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.OCEAN, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.BLUE, 6));
                String hotDesert = namer.word(random, true);
                for (int m = waterRating + 1; m < 6; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], hotDesert, DescriptiveColor.unevenMix(barrenColorOklab, 50, DescriptiveColor.SKY, m + 1, DescriptiveColor.ORANGE, h + 1));
                    }
                }
                for (int m = 1; m <= waterRating; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], namer.word(random, true), DescriptiveColor.unevenMix(livingColorOklab, 10 + m, DescriptiveColor.COBALT, m + 1, DescriptiveColor.ORANGE, h + 1));
                    }
                }
                String coast = namer.word(random, true);
                for (int h = 1; h < 6; h++) {
                    int i = 36 + h;
                    table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.COAST, coast, DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.APRICOT, h + 3, DescriptiveColor.BUTTER, 2));
                }
                for (int m = 7; m < 10; m++) {
                    String name = namer.word(random, true);
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], name, DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.BLUE, m - 3));
                    }
                }
                break;
            }
            default:
                return Biome.TABLE;
        }
        return table;
    }
}
