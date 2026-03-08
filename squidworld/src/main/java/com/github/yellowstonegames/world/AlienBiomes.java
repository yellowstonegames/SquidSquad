package com.github.yellowstonegames.world;

import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;

@Beta
public class AlienBiomes {
    public static Biome[] generateAlienBiomeTable(EnhancedRandom random, Language namer, int liquidAmount,
                                                  int liquidColorOklab, int barrenColorOklab, int lifeColorOklab) {
        Biome[] table = new Biome[66];
        System.arraycopy(Biome.TABLE, 60, table, 60, 6);
        switch (liquidAmount){
            case 0: {
                String coldDesert = namer.word(random, true);
                for (int m = 0; m < 6; m++) {
                    int h = 0;
                    int i = m * 6 + h;
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], coldDesert, DescriptiveColor.unevenMix(barrenColorOklab, 30, liquidColorOklab, m + 1));
                }
                String coldLowlands = namer.word(random, true);
                for (int i = 36, m = 6; i < 60; i += 6, m++) {
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], coldLowlands, DescriptiveColor.unevenMix(barrenColorOklab, 30, DescriptiveColor.BLACK, m));
                }
                String hotDesert = namer.word(random, true);
                for (int m = 0; m < 6; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], hotDesert, DescriptiveColor.unevenMix(barrenColorOklab, 30, liquidColorOklab, m + 1, DescriptiveColor.ORANGE, h + 1));
                    }
                }
                String hotLowlands = namer.word(random, true);
                for (int m = 6; m < 10; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], hotLowlands, DescriptiveColor.unevenMix(barrenColorOklab, 30, DescriptiveColor.BLACK, m, DescriptiveColor.ORANGE, h + 1));

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
                for (int m = 0; m < 6 - liquidAmount; m++) {
                    int h = 0;
                    int i = m * 6 + h;
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], coldDesert, DescriptiveColor.unevenMix(barrenColorOklab, 30, liquidColorOklab, m + 1, DescriptiveColor.WHITE, m));
                }
                for (int m = 6 - liquidAmount; m < 6; m++) {
                    int h = 0;
                    int i = m * 6 + h;
                    table[i] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.ALL[m], namer.word(random, true), DescriptiveColor.unevenMix(lifeColorOklab, 20, liquidColorOklab, m + 1, DescriptiveColor.BLACK, m));
                }
                table[36] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.COAST, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 1, DescriptiveColor.GRAY, 2));
                table[42] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.RIVER, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 2, liquidColorOklab, 4, DescriptiveColor.WHITE, 2));
                table[48] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.LAKE, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 2, liquidColorOklab, 5, DescriptiveColor.WHITE, 2));
                table[54] = new Biome(Biome.Heat.COLDEST, Biome.Moisture.OCEAN, namer.word(random, true), DescriptiveColor.unevenMix(barrenColorOklab, 1, liquidColorOklab, 6));
                String hotDesert = namer.word(random, true);
                for (int m = liquidAmount + 1; m < 6; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], hotDesert, DescriptiveColor.unevenMix(barrenColorOklab, 30, liquidColorOklab, m + 1, DescriptiveColor.ORANGE, h + 1));
                    }
                }
                for (int m = 0; m <= liquidAmount; m++) {
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], namer.word(random, true), DescriptiveColor.unevenMix(lifeColorOklab, 15 + m, liquidColorOklab, m + 1, DescriptiveColor.BLACK, 2, DescriptiveColor.ORANGE, h + 1));
                    }
                }
                String coast = namer.word(random, true);
                for (int h = 1; h < 6; h++) {
                    int i = 36 + h;
                    table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.COAST, coast, DescriptiveColor.unevenMix(barrenColorOklab, 20, DescriptiveColor.APRICOT, h + 3, DescriptiveColor.BUTTER, 2));
                }
                for (int m = 7; m < 10; m++) {
                    String name = namer.word(random, true);
                    for (int h = 1; h < 6; h++) {
                        int i = m * 6 + h;
                        table[i] = new Biome(Biome.Heat.ALL[h], Biome.Moisture.ALL[m], name, DescriptiveColor.unevenMix(barrenColorOklab, 1, liquidColorOklab, m - 3));
                    }
                }
                break;
            }
            default:
                System.arraycopy(Biome.TABLE, 0, table, 0, 60);
        }
        return table;
    }
}
