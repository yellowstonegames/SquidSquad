package com.github.yellowstonegames.text;

import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.random.LaserRandom;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.StringTools;
import org.junit.Test;

public class LanguageTest {
    @Test
    public void testOutput() {
        if(!"true".equals(System.getenv("printing"))) return;

        System.out.println("Known languages:");
        for (int langi = 0; langi < Language.registered.length; langi++) {
            System.out.println(Language.registeredNames[langi]);
        }
        System.out.println();
//// Used to bootstrap SquidLibSharp's language gen class; not useful elsewhere.
//        System.out.println("C# code:");
//        for (int langi = 0; langi < Language.registered.length; langi++) {
//            String name = Language.registeredNames[langi];
//            System.out.println(
//                    "        public static readonly LanguageGen " + name.toUpperCase().replace(' ', '_') + " = new LanguageGen(\n" +
//                            "                new string[]{\"u\"},\n" +
//                            "                new string[]{\"au\"},\n" +
//                            "                new string[]{\"b\"},\n" +
//                            "                new string[]{\"b\", \"lb\"},\n" +
//                            "                new string[]{\"lb\"},\n" +
//                            "                Array.Empty<string>(),\n" +
//                            "                Array.Empty<string>(), new int[]{1, 2}, new double[]{5, 3}, 0.45, 0.45, 0.0, 0.0, null, true).Register(\"" + name + "\");\n");
//        }
//        System.out.println();
        LaserRandom rng = new LaserRandom(0xf00df00L);

        for (int langi = 0; langi < Language.registered.length; langi++) {
            Language flg = Language.registered[langi];
            String name = Language.registeredNames[langi];
            rng.setSeed(0xf00df00L);
            System.out.println("\nImitating language: \"" + name + "\":\n");
            for (int i = 0; i < 40; i++) {
                System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";"},
                        new String[]{".", ".", "!", "?", "..."}, 0.14));
            }
        }
        Language flg;
        System.out.println("\nImitating language: \"Norse with simplified spelling\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.NORSE_SIMPLIFIED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.14));
        }

        System.out.println("\n\nLANGUAGE MIXES:\n");

        System.out.println("\nImitating language: \"English 50%, French (no accents) 50%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.ENGLISH.mix(Language.FRENCH.removeAccents(), 0.5);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        System.out.println("\nImitating language: \"Russian Romanized 65%, English 35%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.RUSSIAN_ROMANIZED.mix(Language.ENGLISH, 0.35);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 10, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.22));
        }
        System.out.println("\nImitating language: \"French 45%, Greek Romanized 55%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.FRENCH.mix(Language.GREEK_ROMANIZED, 0.55);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.22));
        }
        System.out.println("\nImitating language: \"English 75%, Greek Authentic 25%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.ENGLISH.mix(Language.GREEK_AUTHENTIC, 0.25);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        System.out.println("\nImitating language: \"English with added accents\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.ENGLISH.addAccents(0.5, 0.15);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        System.out.println("\nImitating language: \"French 35%, Japanese Romanized 65%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.FRENCH.mix(Language.JAPANESE_ROMANIZED, 0.65);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "...", "..."}, 0.17));
        }

        System.out.println("\nImitating language: \"Russian Romanized 25%, Japanese Romanized 75%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.RUSSIAN_ROMANIZED.mix(Language.JAPANESE_ROMANIZED, 0.75);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.2));
        }

        System.out.println("\nImitating language: \"English with no repeats of the same letter twice in a row\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.ENGLISH.addModifiers(Language.Modifier.NO_DOUBLES);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        System.out.println("\nImitating language: \"Japanese Romanized with frequent doubled consonants\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.JAPANESE_ROMANIZED.addModifiers(Language.Modifier.DOUBLE_CONSONANTS);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        System.out.println("\nImitating language: \"Somali 63%, Japanese Romanized 27%, Swahili 10%\":\n");
        rng.setSeed(0xf00df00L);
        flg = Language.SOMALI.mix(Language.JAPANESE_ROMANIZED, 0.3).mix(Language.SWAHILI, 0.1);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.15));
        }

    }
    @Test
    public void testSentences()
    {
        if(!"true".equals(System.getenv("printing"))) return;

        LaserRandom rng = new LaserRandom(0xf00df00L);
        Language flg;
        System.out.println("\n\nDEFAULT SENTENCES:\n\n");
        flg = Language.ENGLISH;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 10,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        flg = Language.JAPANESE_ROMANIZED;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        flg = Language.FRENCH;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 5, 8,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.1) + "\",");
        flg =  Language.GREEK_ROMANIZED;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 5, 8,
                new String[]{",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.15) + "\",");
        flg = Language.GREEK_AUTHENTIC;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 5, 8,
                new String[]{",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.15) + "\",");
        flg = Language.RUSSIAN_ROMANIZED;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ",", ";"}, new String[]{"!", "!", ".", "...", ".", "?"}, 0.22) + "\",");
        flg = Language.RUSSIAN_AUTHENTIC;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ",", ";"}, new String[]{"!", "!", ".", "...", ".", "?"}, 0.22) + "\",");
        flg = Language.LOVECRAFT;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        flg = Language.SWAHILI;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 8,
                new String[]{",", ",", ";"}, new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = Language.FRENCH.mix(Language.JAPANESE_ROMANIZED, 0.65);
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.17) + "\",");
        flg = Language.ENGLISH.addAccents(0.5, 0.15);
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.17) + "\",");
        flg = Language.RUSSIAN_AUTHENTIC.mix(Language.GREEK_AUTHENTIC, 0.5).mix(Language.FRENCH, 0.35);
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.2) + "\",");
        flg = Language.FANCY_FANTASY_NAME;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.2) + "\",");
        flg = Language.SWAHILI.mix(Language.JAPANESE_ROMANIZED, 0.35); //.mix(Language.FRENCH, 0.35)
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = Language.SWAHILI.mix(Language.JAPANESE_ROMANIZED, 0.32).mix(Language.FANCY_FANTASY_NAME, 0.25);
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = Language.SOMALI.mix(Language.JAPANESE_ROMANIZED, 0.3).mix(Language.SWAHILI, 0.15);
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = Language.INUKTITUT;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = Language.NORSE;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 9, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = Language.NORSE_SIMPLIFIED;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 9, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = Language.NAHUATL;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 3, 6, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.1) + "\",");
        flg = Language.MONGOLIAN;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 3, 8, new String[]{",", ",", ";", ",", " -"},
                new String[]{"!", "?", ".", ".", ".", ".", "..."}, 0.16) + "\",");
        flg = Language.SIMPLISH;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 10, new String[]{" -", ",", ",", ";"},
                new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        flg = Language.KOREAN_ROMANIZED;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 5, 9, new String[]{",", ",", ";", ","},
                new String[]{"!", "?", ".", ".", ".", ".", "..."}, 0.13) + "\",");
        flg = Language.MAORI;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 10, new String[]{",", ",", ";", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", ".", "..."}, 0.14) + "\",");
        flg = Language.SPANISH;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 10, new String[]{",", ",", ",", ";", ","},
                new String[]{"!", "?", ".", ".", ".", "..."}, 0.17) + "\",");
        flg = Language.ALIEN_A;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 10, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.14) + "\",");
        flg = Language.ALIEN_E;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 10, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.14) + "\",");
        flg = Language.ALIEN_I;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 10, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.14) + "\",");
        flg = Language.ALIEN_O;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 10, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.14) + "\",");
        flg = Language.ALIEN_U;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 10, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.14) + "\",");
        flg = Language.INSECT;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 7, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.1) + "\",");
        flg = Language.ELF;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 7, 12, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.22) + "\",");
        flg = Language.DEMONIC;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 3, 7, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "!"}, 0.1) + "\",");
        flg = Language.INFERNAL;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 7, 14, new String[]{",", ",", ",", ";", " -"},
                new String[]{"!", "?", ".", ".", ".", "...", "..."}, 0.23) + "\",");
        flg = Language.GOBLIN;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 8, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", ".", ".", "?", "...", "..."}, 0.13) + "\",");
        flg = Language.KOBOLD;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 4, 10, new String[]{",", ",", ",", ";"},
                new String[]{"!", "?", "!", "?", ".", ".", "..."}, 0.15) + "\",");
        flg = Language.DRAGON;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 8, 13, new String[]{",", ",", ",", ";", " -", ";"},
                new String[]{"!", "?", ".", ".", ".", "...", "...", "...", "..."}, 0.24) + "\",");
        flg = Language.DEEP_SPEECH;
        System.out.println(flg.getName() + ": \"" + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -", ","},
                new String[]{"!", "?", ".", ".", ".", "...", "...", "...", "...?"}, 0.16) + "\",");

    }
    @Test
    public void testNaturalCipher()
    {
        if(!"true".equals(System.getenv("printing"))) return;

        Language[] languages = new Language[]{

                Language.ENGLISH,
                Language.LOVECRAFT,
                Language.JAPANESE_ROMANIZED,
                Language.FRENCH,
                Language.GREEK_ROMANIZED,
                Language.GREEK_AUTHENTIC,
                Language.RUSSIAN_ROMANIZED,
                Language.RUSSIAN_AUTHENTIC,
                Language.SWAHILI,
                Language.SOMALI,
                Language.ARABIC_ROMANIZED,
                Language.HINDI_ROMANIZED.removeAccents(),
                Language.INUKTITUT,
                Language.NORSE_SIMPLIFIED,
                Language.NAHUATL,
                Language.MONGOLIAN,
                Language.KOREAN_ROMANIZED,
                Language.MAORI,
                Language.SPANISH,
                Language.NORSE,
                Language.SIMPLISH,
                Language.FANTASY_NAME,
                Language.FANCY_FANTASY_NAME,
                Language.RUSSIAN_ROMANIZED.mix(Language.SOMALI, 0.25),
                Language.GREEK_ROMANIZED.mix(Language.HINDI_ROMANIZED.removeAccents(), 0.5),
                Language.SWAHILI.mix(Language.FRENCH, 0.3),
                Language.ARABIC_ROMANIZED.mix(Language.JAPANESE_ROMANIZED, 0.4),
                Language.SWAHILI.mix(Language.GREEK_ROMANIZED, 0.4),
                Language.GREEK_ROMANIZED.mix(Language.SOMALI, 0.4),
                Language.ENGLISH.mix(Language.HINDI_ROMANIZED.removeAccents(), 0.4),
                Language.ENGLISH.mix(Language.JAPANESE_ROMANIZED, 0.4),
                Language.SOMALI.mix(Language.HINDI_ROMANIZED.removeAccents(), 0.4),
                Language.FRENCH.addModifiers(Language.modifier("([^aeiou])\\1", "$1ph", 0.3),
                        Language.modifier("([^aeiou])\\1", "$1ch", 0.4),
                        Language.modifier("([^aeiou])\\1", "$1sh", 0.5),
                        Language.modifier("([^aeiou])\\1", "$1", 0.9)),
                Language.JAPANESE_ROMANIZED.addModifiers(Language.Modifier.DOUBLE_VOWELS),
                Language.randomLanguage(Hasher.leraje.hash64("Kittenish")),
                Language.randomLanguage(Hasher.leraje.hash64("Puppyspeak")),
                Language.randomLanguage(Hasher.leraje.hash64("Rabbitese")),
                Language.randomLanguage(Hasher.leraje.hash64("Rabbit Language")),
                Language.randomLanguage(Hasher.leraje.hash64("The Roar Of That Slumbering Shadow Which Mankind Wills Itself To Forget")),
                Language.SOMALI.addModifiers(Language.modifier("([kd])h", "$1"),
                        Language.modifier("([pfsgkcb])([aeiouy])", "$1l$2", 0.35),
                        Language.modifier("ii", "ai"),
                        Language.modifier("uu", "ia"),
                        Language.modifier("([aeo])\\1", "$1"),
                        Language.modifier("^x", "v"),
                        Language.modifier("([^aeiou]|^)u([^aeiou]|$)", "$1a$2", 0.6),
                        Language.modifier("([aeiou])[^aeiou]([aeiou])", "$1v$2", 0.06),
                        Language.modifier("([aeiou])[^aeiou]([aeiou])", "$1l$2", 0.07),
                        Language.modifier("([aeiou])[^aeiou]([aeiou])", "$1n$2", 0.07),
                        Language.modifier("([aeiou])[^aeiou]([aeiou])", "$1z$2", 0.08),
                        Language.modifier("([^aeiou])[aeiou]+$", "$1ia", 0.35),
                        Language.modifier("([^aeiou])[bpdtkgj]", "$1"),
                        Language.modifier("[jg]$", "th"),
                        Language.modifier("g", "c", 0.92),
                        Language.modifier("([aeiou])[wy]$", "$1l", 0.6),
                        Language.modifier("([aeiou])[wy]$", "$1n"),
                        Language.modifier("[qf]$", "l", 0.4),
                        Language.modifier("[qf]$", "n", 0.65),
                        Language.modifier("[qf]$", "s"),
                        Language.modifier("cy", "sp"),
                        Language.modifier("kl", "sk"),
                        Language.modifier("qu+", "qui"),
                        Language.modifier("q([^u])", "qu$1"),
                        Language.modifier("cc", "ch"),
                        Language.modifier("[^aeiou]([^aeiou][^aeiou])", "$1"),
                        Language.Modifier.NO_DOUBLES
                ),
                Language.GOBLIN,
                Language.ELF,
                Language.DEMONIC,
                Language.INFERNAL,
                Language.DRAGON,
                Language.KOBOLD,
                Language.ALIEN_A,
                Language.ALIEN_E,
                Language.ALIEN_I,
                Language.ALIEN_O,
                Language.ALIEN_U,
                Language.INSECT,
                Language.DEEP_SPEECH
        };
        String marked = "What the [?]heck?[?] Check that out will ya? It's probably nothing, but - OH [?]NO, THIS IS BAD!";
        String oz =
                "Uncle Uncles Carbuncle Carbuncles Live Lives Lived Living Liver Livers Livery Liveries\n\n" +
                "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a "+
                "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the "+
                "lumber to build it had to be carried by wagon many miles. There were four walls, "+
                "a floor and a roof, which made one room; and this room contained a rusty looking "+
                "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. "+
                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in "+
                "another corner. There was no garret at all, and no cellar-except a small hole dug "+
                "in the ground, called a cyclone cellar, where the family could go in case one of "+
                "those great whirlwinds arose, mighty enough to crush any building in its path. It "+
                "was reached by a trap door in the middle of the floor, from which a ladder led "+
                "down into the small, dark hole.",
        oz2;
        System.out.println("ORIGINAL:");         
        System.out.println(StringTools.join("\n", StringTools.wrap(oz, 80)));
        System.out.println("\n\nGENERATED:\n");
        LaserRandom sr = new LaserRandom(2252637788195L);
        for(Language lang : languages) {
            System.out.println("\n" + lang.getName() + ":\n");
            Translator cipher = new Translator(lang, 41041041L);
//            System.out.println("princess   : " + cipher.lookup("princess"));
//            System.out.println("princesses : " + cipher.lookup("princesses"));
            int ctr = 0;
            System.out.println(StringTools.join("\n", StringTools.wrap(cipher.cipherMarkup(marked), 80)));
            oz2 = cipher.cipher(oz);
            System.out.println(StringTools.join("\n", StringTools.wrap(oz2, 80)));
            
            ObjectObjectMap<String, String> vocabulary = new ObjectObjectMap<>(16);
            cipher.learnTranslations(vocabulary, "Dorothy", "farmer", "the", "room", "one", "uncle", "aunt");

            System.out.println(StringTools.join("\n", StringTools.wrap(cipher.decipher(oz2, vocabulary), 80)));
            System.out.println();
            System.out.println(StringTools.join("\n", StringTools.wrap(cipher.decipher(oz2, cipher.reverse), 80)));
            System.out.println();

            /*
            Translator cipher = new Translator(lang, 2252637788195L);
            int ctr = 0;
            for (String s : oz) {
                oz2[ctr] = cipher.cipher(s);
                System.out.println(oz2[ctr++]);
            }

            ObjectObjectMap<String, String> vocabulary = new ObjectObjectMap<>(16);
            cipher.learnTranslations(vocabulary, "Dorothy", "farmer", "the", "room", "one", "uncle", "aunt");
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, vocabulary));
            }
            System.out.println();
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, cipher.reverse));
            }
            System.out.println();
            */
            /*
            cipher = new Translator(lang, 0x123456789L);
            ctr = 0;
            for (String s : oz) {
                oz2[ctr] = cipher.cipher(s);
                System.out.println(oz2[ctr++]);
            }

            vocabulary.clear();
            cipher.learnTranslations(vocabulary, "Dorothy", "farmer", "the", "room", "one", "uncle", "aunt");
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, vocabulary));
            }
            System.out.println();
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, cipher.reverse));
            }
            System.out.println();
            */
        }
        /*
        rng.setSeed(0xF00DF00L);
        flg = Language.randomLanguage(CrossHash.Mist.kappa.hash64("Space Speak")).removeAccents();
        for (int i = 0; i < 100; i++) {
            System.out.print(flg.word(rng, true, Math.min(rng.between(1, 6), rng.between(2, 4))) + " ");
        }
        */
        /*
        LaserRandom nrng = new LaserRandom("SquidLib!");

        System.out.println(nrng.getState());
        for(Language lang : languages) {
            for (int n = 0; n < 20; n++) {
                for (int i = 0; i < 4; i++) {
                    System.out.print(nrng.getState() + " : " + lang.word(nrng, false, 3) + ", ");
                }
                System.out.println();
            }
            System.out.println();
        }
        */
    }
//    @Test
//    public void testNameGen()
//    {
//        if(!TestConfiguration.PRINTING) return;
//        LaserRandom rng = new LaserRandom(2252637788195L);
//        ArrayList<String> men = new WeightedLetterNamegen(WeightedLetterNamegen.COMMON_USA_MALE_NAMES, 2, rng).generateList(50),
//                women = new WeightedLetterNamegen(WeightedLetterNamegen.COMMON_USA_FEMALE_NAMES, 2, rng).generateList(50),
//                family = new WeightedLetterNamegen(WeightedLetterNamegen.COMMON_USA_LAST_NAMES, 2, rng).generateList(100);
//        for (int i = 0; i < 50; i++) {
//            System.out.println(men.get(i) + " " + family.get(i << 1) + ", " + women.get(i) + " " + family.get(i << 1 | 1)
//                    + ", " + Language.SIMPLISH.word(rng, true, rng.betweenWeighted(1, rng.between(1, 4), 3)) + " " + Language.SIMPLISH.word(rng, true, rng.betweenWeighted(1, 4, 3)));
//        }
//    }
//    @Test
//    public void testMarkovText() {
//        if (!TestConfiguration.PRINTING) return;
//        long seed = 10040L;
//        String oz = "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a " +
//                "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the " +
//                "lumber to build it had to be carried by wagon many miles. There were four walls, " +
//                "a floor and a roof, which made one room; and this room contained a rusty looking " +
//                "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. " +
//                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in " +
//                "another corner. There was no garret at all, and no cellar-except a small hole dug " +
//                "in the ground, called a cyclone cellar, where the family could go in case one of " +
//                "those great whirlwinds arose, mighty enough to crush any building in its path. It " +
//                "was reached by a trap door in the middle of the floor, from which a ladder led " +
//                "down into the small, dark hole. When Dorothy stood in the doorway and looked around, " +
//                "she could see nothing but the great gray prairie on every side. Not a tree nor a house " +
//                "broke the broad sweep of flat country that reached to the edge of the sky in all directions. " +
//                "The sun had baked the plowed land into a gray mass, with little cracks running through it. " +
//                "Even the grass was not green, for the sun had burned the tops of the long blades until they " +
//                "were the same gray color to be seen everywhere. Once the house had been painted, but the sun " +
//                "blistered the paint and the rains washed it away, and now the house was as dull and gray as " +
//                "everything else.";
//        MarkovText markovText = new MarkovText();
//        markovText.analyze(oz);
//        for (int i = 0; i < 40; i++) {
//            System.out.println(markovText.chain(++seed, 100 + (i * 2)));
//        }
//        seed = 10040L;
//        System.out.println();
//        Translator cipher = new Translator(Language.JAPANESE_ROMANIZED);
//        markovText.changeNames(cipher);
//        for (int i = 0; i < 40; i++) {
//            System.out.println(markovText.chain(++seed, 100 + (i * 2)));
//        }
//    }
//    @Test
//    public void testMarkovObject() {
//        if (!TestConfiguration.PRINTING) return;
//        long seed = 10040L;
//        MarkovObject<String> markovObject = new MarkovObject<>();
//        String[] ozzes = {"Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a " +
//                "farmer, and Aunt Em, who was the farmer's wife.",
//                "Their house was small, for the lumber to build it had to be carried by wagon many miles.",
//                "There were four walls, a floor and a roof, which made one room; and this room contained a rusty looking" +
//                        " cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds.",
//                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in another corner.",
//                "There was no garret at all, and no cellar-except a small hole dug in the ground, called a cyclone " +
//                        "cellar, where the family could go in case one of those great whirlwinds arose, mighty enough to crush " +
//                        "any building in its path.",
//                "It was reached by a trap door in the middle of the floor, from which a ladder led down into the small, dark hole.",
//                "When Dorothy stood in the doorway and looked around, she could see nothing but the great gray prairie on every side.",
//                "Not a tree nor a house broke the broad sweep of flat country that reached to the edge of the sky in all directions.",
//                "The sun had baked the plowed land into a gray mass, with little cracks running through it.",
//                "Even the grass was not green, for the sun had burned the tops of the long blades until they were the same gray color to be seen everywhere.",
//                "Once the house had been painted, but the sun blistered the paint and the rains washed it away, and now " +
//                        "the house was as dull and gray as everything else."
//        };
//        for(String o : ozzes)
//        {
//            markovObject.analyze(StringKit.split(o, " "));
//        }
//        List<String> ls = new ArrayList<>(60);
//        for (int i = 0; i < 40; i++) {
//            System.out.println(StringKit.join(" ", markovObject.chain(++seed, 10 + i, true, ls)));
//            ls.clear();
//        }
//    }
//
//    @Test
//    public void testMarkovChar() {
//        if (!TestConfiguration.PRINTING) return;
//        long seed = 10040L;
//        MarkovChar markovChar = new MarkovChar();
//        String[] ozzes = {"Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a " +
//                "farmer, and Aunt Em, who was the farmer's wife.",
//                "Their house was small, for the lumber to build it had to be carried by wagon many miles.",
//                "There were four walls, a floor and a roof, which made one room; and this room contained a rusty looking" +
//                        " cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds.",
//                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in another corner.",
//                "There was no garret at all, and no cellar-except a small hole dug in the ground, called a cyclone " +
//                        "cellar, where the family could go in case one of those great whirlwinds arose, mighty enough to crush " +
//                        "any building in its path.",
//                "It was reached by a trap door in the middle of the floor, from which a ladder led down into the small, dark hole.",
//                "When Dorothy stood in the doorway and looked around, she could see nothing but the great gray prairie on every side.",
//                "Not a tree nor a house broke the broad sweep of flat country that reached to the edge of the sky in all directions.",
//                "The sun had baked the plowed land into a gray mass, with little cracks running through it.",
//                "Even the grass was not green, for the sun had burned the tops of the long blades until they were the same gray color to be seen everywhere.",
//                "Once the house had been painted, but the sun blistered the paint and the rains washed it away, and now " +
//                        "the house was as dull and gray as everything else."
//        };
//        markovChar.analyze(StringKit.join("", ozzes));
//        for (int i = 0; i < 10; i++) {
//            System.out.print(StringKit.capitalize(markovChar.chain(++seed, 10 + i)));
//            for (int j = 0; j < 20; j++) {
//                System.out.print(" " + markovChar.chain(++seed, 10 + i).toLowerCase());
//            }
//            System.out.println();
//        }
//        System.out.println();
//        StringBuilder sb = new StringBuilder(200);
//        for (int i = 0; i < 10; i++) {
//            sb.append(markovChar.chain(++seed, 10 + i));
//            for (int j = 0; j < 20; j++) {
//                sb.append(' ').append(markovChar.chain(++seed, 10 + i));
//            }
//            System.out.println(StringKit.capitalize(sb));
//            sb.setLength(0);
//        }
//
//        System.out.println();
//        for (int i = 0; i < 10; i++) {
//            sb.append(markovChar.chain(++seed, 10 + i));
//            for (int j = 0; j < 20; j++) {
//                sb.append(' ').append(markovChar.chain(++seed, 10 + i));
//            }
//            System.out.println(StringKit.sentenceCase(sb));
//            sb.setLength(0);
//        }
//    }
    
    @Test
    public void testRandomLanguage()
    {
        if(!"true".equals(System.getenv("printing"))) return;

        Language lang1 = Language.randomLanguage(0xABCDEF1234567890L).mix(0.375, Language.ELF, 0.25);
        Language lang2 = Language.randomLanguage(0xABCDEF1234567890L).mix(0.375, Language.ELF, 0.25);
        System.out.println(lang1.equals(lang2));
        System.out.println(lang1.sentence(123, 5, 9));
        System.out.println(lang2.sentence(123, 5, 9));
    }
}
