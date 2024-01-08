/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.text;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.NumberedSet;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.StringTools;

import java.util.Collection;

import static com.github.yellowstonegames.text.Thesaurus.*;

/**
 * A utility class to print (typically very large) numbers in a way that players can more-meaningfully tell them apart.
 * It can do this either by converting between a {@code long} number and one large nonsense "word", or by converting
 * between an {@code int} number and a readable English phrase. For example, it can bi-directionally turn {@code long}
 * values like -8798641734435409502 into {@code String}s like nwihyayeetyoruehyazuetro. The advantage here is that
 * nwihyayeetyoruehyazuetro is very different from protoezlebauyauzlutoatra, even though the numbers they are made from
 * are harder to distinguish (-8798641734435409502 vs. -8032477240987739423, when using the default seed). It can also
 * turn phrase {@code String}s like "Scaly vengeance of the pungent congress" and "Fragile eggplant and the mechanical
 * tobacco" (which were generated using a seed of 1 from inputs of 0 and 2).
 * <br>
 * The constructor optionally takes a seed that can greatly change the generated mnemonics, which may be useful if
 * mnemonic strings produced for some purpose should only be decipherable by that program or that play of the game. If
 * no seed is given, this acts as if the seed is 1. Only 256 possible 3-letter sections are used with any given seed,
 * but 431 sections are possible (hand-selected to avoid the likelihood of producing possibly-vulgar words). Two
 * different seeds may use mostly-different selections of "syllable" sections, though a not-very-small amount of overlap
 * in potential generated mnemonic strings must occur between any two seeds.
 * <br>
 * If using a constructor that doesn't specify an array or Collection of String words to use, then this will load its
 * noun and adjective list from {@link Thesaurus}. Thesaurus sometimes receives updates to its word list, but it can
 * have a specific word list loaded from a String using {@link Thesaurus#addArchivedCategoriesAlternate(String)}.
 * <br>
 * Created by Tommy Ettinger on 1/24/2018.
 */
public class Mnemonic {
    private static final String baseTriplets =
            "baibaublabyabeabeebeibeoblebrebwebyebiabiebioblibribwibyiboaboeboiboubrobuobyobuabuebuibrubwubyu" +
            "daudradyadeadeedeodredwediodridwidyidoadoedoidoudroduodyoduadueduidrudwudyu" +
            "haihauhmahrahyahwaheaheeheiheohmehrehwehyehiahiehiohmihrihwihyihmohrohuohyohuahuehuihmuhruhwuhyu" +
            "jaijaujyajwajeajeejeijeojwejyejiajiejiojwijyijoajoejoijoujyo" +
            "kaikaukrakyakeakeekeoklekrekyekiakiokrikwikyikoakoekoikouklokrokyokuokuakuekuikrukyu" +
            "lailaulyalwalealeeleileolwelyelialieliolwilyiloaloeloiluolyolualuilwulyu" +
            "maimaumlamramwamyameameemeimeomlemremwemyemiamiemiomlimrimwimyimoamoemoimoumlomromuomyomuamuemuimlumrumwumyu" +
            "nainaunranwanyaneaneeneonrenwenyenianienionrinwinyinoanoenoinounronuonyonuanuenuinrunwunyu" +
            "paipauplaprapwapyapleprepiapiepioplipripwipyipoapoepoiplopropuopyopluprupyu" +
            "quaquequiquo" +
            "rairauryareareereireoryeriarierioryiroaroeroirouryoruarueruiryu" +
            "saisauskaslasmasnaswasyaseaseeseiseoskeslesmesneswesyesiasiesioskislismisniswisyisoasoesoisouskoslosmosnosuosyosuasuesuiskuslusmusnuswusyu" +
            "taitautratsatwatyateateeteiteotretsetwetyetiatiotritwityitoatoetoitoutrotsotuotyotuatuetuitrutsutwutyu" +
            "veeveiveovrevwevyevieviovrivwivyivoevoivrovuovyovuevuivruvwuvyu" +
            "yaiyauyeayeeyeiyeoyiayieyioyoayoeyoiyouyuayueyuiyuo" +
            "zaizauzvazlazwazyazeazeezeizeozvezlezwezyeziazieziozvizlizwizyizoazoezoizouzvozlozuozyozuazuezuizvuzluzwuzyu";
    public final NumberedSet<String> items;
    public final NumberedSet<String> allAdjectives;
    public final NumberedSet<String> allNouns;

    /**
     * Default constructor for a Mnemonic generator; equivalent to {@code new Mnemonic(1L)}, and probably a good choice
     * unless you know you need different seeds.
     * <br>
     * This depends on the current (at the time this Mnemonic is constructed) contents of {@link Thesaurus#adjective}
     * and {@link Thesaurus#noun}, which can be modified, and if these contents aren't identical for two different
     * Mnemonic objects (possibly constructed at different times, using different SquidSquad versions), the Mnemonics
     * will have different encoded and decoded forms. You can either save the key set from {@link #allAdjectives} and
     * {@link #allNouns} and pass them to {@link #Mnemonic(long, Collection, Collection)}, or save the Thesaurus state
     * altogether using {@link Thesaurus#archiveCategoriesAlternate()}, loading it before constructing any Mnemonics
     * with {@link Thesaurus#addArchivedCategoriesAlternate(String)}.
     */
    public Mnemonic()
    {
        this(1L);
    }

    /**
     * Constructor for a Mnemonic generator that allows a different seed to be chosen, which will alter the syllables
     * produced by {@link #toMnemonic(long)} and the words produced by {@link #toWordMnemonic(int, boolean)} if you give
     * the same numeric argument to differently-seeded Mnemonic generators. Unless you know you need this, you should
     * probably use {@link #Mnemonic()} to ensure that your text can be decoded.
     * <br>
     * This depends on the current (at the time this Mnemonic is constructed) contents of {@link Thesaurus#adjective}
     * and {@link Thesaurus#noun}, which can be modified, and if these contents aren't identical for two different
     * Mnemonic objects (possibly constructed at different times, using different SquidSquad versions), the Mnemonics
     * will have different encoded and decoded forms. You can either save the key set from {@link #allAdjectives} and
     * {@link #allNouns} and pass them to {@link #Mnemonic(long, Collection, Collection)}, or save the Thesaurus state
     * altogether using {@link Thesaurus#archiveCategoriesAlternate()}, loading it before constructing any Mnemonics
     * with {@link Thesaurus#addArchivedCategoriesAlternate(String)}.
     * @param seed a long seed that will be used to randomize the syllables and words used.
     */
    public Mnemonic(long seed)
    {
        EnhancedRandom rng = new AceRandom(seed);
        int[] order = ArrayTools.range(431);
        rng.shuffle(order);
        int o;
        items = new NumberedSet<>(256, 0.5f);
        for (int i = 0; i < 256; i++) {
            o = order[i];
            items.add(baseTriplets.substring(o * 3, o * 3 + 3));
        }
        allAdjectives = new NumberedSet<>(adjective.size(), 0.5f);
        allNouns = new NumberedSet<>(noun.size(), 0.5f);
        for (int i = 0; i < adjective.size(); i++) {
            ObjectList<String> words = adjective.getAt(i);
            for (int j = 0; j < words.size(); j++) {
                if(-1 == words.get(j).indexOf(' '))
                    allAdjectives.add(words.get(j));
            }
        }
        allAdjectives.shuffle(rng);
        allAdjectives.renumber();
        for (int i = 0; i < noun.size(); i++) {
            ObjectList<String> words = noun.getAt(i);
            for (int j = 0; j < words.size(); j++) {
                if(-1 == words.get(j).indexOf(' '))
                    allNouns.add(words.get(j));
            }
        }
        allNouns.shuffle(rng);
        allNouns.renumber();
    }

    /**
     * Constructor that allows you to specify the adjective and noun collections used by
     * {@link #toWordMnemonic(int, boolean)} as well as a seed. This should be useful when you want to enforce a stable
     * relationship between word mnemonics produced by {@link #toWordMnemonic(int, boolean)} and the int values they
     * decode to with {@link #fromWordMnemonic(String)}, because the default can change if the adjective and noun
     * collections in {@link Thesaurus} change. There should be a fairly large amount of unique adjectives and nouns;
     * {@code (long)adjectives.size() * nouns.size() * adjectives.size() * nouns.size()} should be at least 0x80000000L
     * (2147483648L), with case disregarded. If the total is less than that, not all possible ints can be encoded with
     * {@link #toWordMnemonic(int, boolean)}. Having 216 adjectives and 216 nouns is enough for a rough target. Each
     * word (adjectives and nouns alike) can have any characters in it except for space, since space is used during
     * decoding to separate words.
     * @param seed a long seed that will be used to randomize the syllables and words used.
     * @param adjectives a Collection of unique Strings (case-insensitive) that will be used as adjectives
     * @param nouns a Collection of unique Strings (case-insensitive) that will be used as nouns
     */
    public Mnemonic(long seed, Collection<String> adjectives, Collection<String> nouns)
    {
        EnhancedRandom rng = new AceRandom(seed);
        int[] order = ArrayTools.range(431);
        rng.shuffle(order);
        int o;
        items = new NumberedSet<>(256, 0.5f);
        for (int i = 0; i < 256; i++) {
            o = order[i];
            items.add(baseTriplets.substring(o * 3, o * 3 + 3));
        }
        allAdjectives = new NumberedSet<>(adjectives.size(), 0.5f);
        allNouns = new NumberedSet<>(nouns.size(), 0.5f);
        for(String word : adjectives)
            if(-1 == word.indexOf(' ')) allAdjectives.add(word);
        allAdjectives.shuffle(rng);
        allAdjectives.renumber();
        for(String word : nouns)
            if(-1 == word.indexOf(' ')) allNouns.add(word);
        allNouns.shuffle(rng);
        allNouns.renumber();
    }

    /**
     * Constructor that allows you to specify the adjective and noun collections (given as arrays) used by
     * {@link #toWordMnemonic(int, boolean)} as well as a seed. This should be useful when you want to enforce a stable
     * relationship between word mnemonics produced by {@link #toWordMnemonic(int, boolean)} and the int values they
     * decode to with {@link #fromWordMnemonic(String)}, because the default can change if the adjective and noun
     * collections in {@link Thesaurus} change. There should be a fairly large amount of unique adjectives and nouns;
     * {@code (long)adjectives.length * nouns.length * adjectives.length * nouns.length} should be at least 0x80000000L
     * (2147483648L), with case disregarded. If the total is less than that, not all possible ints can be encoded with
     * {@link #toWordMnemonic(int, boolean)}. Having 216 adjectives and 216 nouns is enough for a rough target. Each
     * word (adjectives and nouns alike) can have any characters in it except for space, since space is used during
     * decoding to separate words. You may want to use {@link StringTools#split(String, String)} with space or newline as
     * the delimiter to get a String array from data containing space-separated words or data with one word per line.
     * It's also possible to use {@link String#split(String)}, which can use {@code "\\s"} to split on any whitespace.
     * @param seed a long seed that will be used to randomize the syllables and words used.
     * @param adjectives an array of unique Strings (case-insensitive) that will be used as adjectives
     * @param nouns an array of unique Strings (case-insensitive) that will be used as nouns
     */
    public Mnemonic(long seed, String[] adjectives, String[] nouns)
    {
        EnhancedRandom rng = new AceRandom(seed);
        int[] order = ArrayTools.range(431);
        rng.shuffle(order);
        int o;
        items = new NumberedSet<>(256, 0.5f);
        for (int i = 0; i < 256; i++) {
            o = order[i];
            items.add(baseTriplets.substring(o * 3, o * 3 + 3));
        }
        allAdjectives = new NumberedSet<>(adjectives.length, 0.5f);
        allNouns = new NumberedSet<>(nouns.length, 0.5f);
        for(String word : adjectives)
            if(-1 == word.indexOf(' ')) allAdjectives.add(word);
        allAdjectives.shuffle(rng);
        allAdjectives.renumber();
        for(String word : nouns)
            if(-1 == word.indexOf(' ')) allNouns.add(word);
        allNouns.shuffle(rng);
        allNouns.renumber();
    }

    public Mnemonic(NumberedSet<String> items, NumberedSet<String> allAdjectives, NumberedSet<String> allNouns) {
        if(items == null || items.size() < 256)
            throw new IllegalArgumentException("items must be non-null, with length of at least 256.");
        this.items = items;
        if(allAdjectives == null || allAdjectives.isEmpty())
            throw new IllegalArgumentException("allAdjectives must be non-null and non-empty.");
        this.allAdjectives = allAdjectives;
        if(allNouns == null || allNouns.isEmpty())
            throw new IllegalArgumentException("allNouns must be non-null and non-empty.");
        this.allNouns = allNouns;
    }

    public Mnemonic(Mnemonic other) {
        items = new NumberedSet<>(other.items);
        allAdjectives = new NumberedSet<>(other.allAdjectives);
        allNouns = new NumberedSet<>(other.allNouns);
    }

    /**
     * Given any long, generates a slightly-more-memorable gibberish phrase that can be decoded back to the original
     * long with {@link #fromMnemonic(String)}. Examples of what this can produce are "noahritwimoesaidrubiotso" and
     * "loanuiskohaimrunoizlupwi", generated by a Mnemonic with a seed of 1 from -3743983437744699304L and
     * -8967299915041170097L, respectively. The Strings this returns are always 24 chars long, and contain only the
     * letters a-z.
     * @param number any long
     * @return a 24-character String made of gibberish syllables
     */
    public String toMnemonic(long number)
    {
        return toMnemonic(number, false);
    }

    /**
     * Given any long, generates a slightly-more-memorable gibberish phrase that can be decoded back to the original
     * long with {@link #fromMnemonic(String)}. Examples of what this can produce are "noahritwimoesaidrubiotso" and
     * "loanuiskohaimrunoizlupwi", generated by a Mnemonic with a seed of 1 from -3743983437744699304L and
     * -8967299915041170097L, respectively. The Strings this returns are always 24 chars long. If capitalize is true,
     * then the first letter will be a capital letter from A-Z, all other letters will be a-z (including the first if
     * capitalize is false).
     * @param number any long
     * @param capitalize if true, the initial letter of the returned mnemonic String will be capitalized
     * @return a 24-character String made of gibberish syllables
     */
    public String toMnemonic(long number, boolean capitalize)
    {
        char[] c = new char[24];
        String item;
        int idx = 0;
        item = items.getAt((int)(number & 0xFF));
        c[idx++] = capitalize ? Character.toUpperCase(item.charAt(0)) : item.charAt(0);
        c[idx++] = item.charAt(1);
        c[idx++] = item.charAt(2);

        for (int i = 8; i < 64; i+=8) {
            item = items.getAt((int)(number >>> i & 0xFF));
            c[idx++] = item.charAt(0);
            c[idx++] = item.charAt(1);
            c[idx++] = item.charAt(2);
        }
        return String.valueOf(c);
    }

    /**
     * Takes a String produced by {@link #toMnemonic(long)} or {@link #toMnemonic(long, boolean)} and returns the long
     * used to encode that gibberish String. This can't take just any String; if the given parameter isn't at least 24
     * characters long, this can throw an {@link IndexOutOfBoundsException}, and if it isn't made purely from the 3-char
     * syllables toMnemonic() produces, it won't produce a meaningful result.
     * @param mnemonic a gibberish String produced by {@link #toMnemonic(long)} or {@link #toMnemonic(long, boolean)}
     * @return the long used to generate {@code mnemonic} originally
     */
    public long fromMnemonic(String mnemonic)
    {
        long result = (items.indexOf(mnemonic.substring(0, 3).toLowerCase()) & 0xFFL);
        for (int i = 1; i < 8; i++) {
            result |= (items.indexOf(mnemonic.substring(i * 3, i * 3 + 3)) & 0xFFL) << (i << 3);
        }
        return result;
    }

    /**
     * Given any int, generates a short phrase that can be decoded back to the original int with
     * {@link #fromWordMnemonic(String)}. Examples of what this can produce are "Mindful warriors and the pure torch"
     * and "Dynastic earldom and the thousandfold bandit", generated by a Mnemonic with a seed of 1 from -587415991 and
     * -1105099633, respectively. Those Strings were generated using the current state of {@link Thesaurus} and the
     * adjectives and nouns it stores now, and if Thesaurus is added to over time, those Strings won't correspond to
     * those ints anymore. The Strings this returns vary in length. The words this uses by default use only the letters
     * a-z and the single quote (with A-Z for the first character if capitalize is true), with space separating words.
     * If you constructed this Mnemonic with adjective and noun collections or arrays, then this will use only those
     * words and will still separate words with space (and it will capitalize the first char if capitalize is true).
     * @param number any int
     * @param capitalize if true, the initial letter of the returned mnemonic String will be capitalized
     * @return a short phrase that will be uniquely related to number
     */
    public String toWordMnemonic(int number, boolean capitalize)
    {
        final int adjectiveCount = allAdjectives.size(), nounCount = allNouns.size();
        StringBuilder sb = new StringBuilder(80);
        //0x000D76CB inverted by 0x000FBEE3
        //0x000D724B inverted by 0x000E4763
        number = (number ^ 0x7F4A7C15) * 0x000FBEE3;
        // http://marc-b-reynolds.github.io/math/2017/10/13/XorRotate.html
        number = ((number << 5 | number >>> 27) ^ (number << 10 | number >>> 22) ^ (number << 26 | number >>> 6)) * 0x000D724B ^ 0x91E10DA5;
        number ^= number >>> 15;
        boolean negative = (number < 0);
        if(negative) number = ~number;
        sb.append(allAdjectives.getAt(number % adjectiveCount)).append(' ')
                .append(allNouns.getAt((number /= adjectiveCount) % nounCount))
                .append(negative ? " and the " : " of the ")
                .append(allAdjectives.getAt((number /= nounCount) % adjectiveCount)).append(' ')
                .append(allNouns.getAt((number / adjectiveCount) % nounCount));
        if(capitalize)
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Takes a String phrase produced by {@link #toWordMnemonic(int, boolean)} and returns the int used to encode that
     * String. This can't take just any String; it must be produced by {@link #toWordMnemonic(int, boolean)} to give a
     * meaningful result.
     * @param mnemonic a String phrase produced by {@link #toWordMnemonic(int, boolean)}
     * @return the int used to generate {@code mnemonic} originally
     */
    public int fromWordMnemonic(String mnemonic)
    {
        final int adjectiveCount = allAdjectives.size(), nounCount = allNouns.size();
        int idx = mnemonic.indexOf(' '), factor = adjectiveCount;
        boolean negative;
        int result = allAdjectives.indexOf(StringTools.safeSubstring(mnemonic, 0, idx));
        result += factor * allNouns.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        negative = (mnemonic.charAt(idx + 1) == 'a');
        if(negative) idx += 8;
        else idx += 7;
        result += (factor *= nounCount) * allAdjectives.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        result += factor * adjectiveCount * allNouns.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, -1));
        if(negative) result = ~result;
        result ^= result >>> 15 ^ result >>> 30;
        result = (result ^ 0x91E10DA5) * 0x000E4763;
        result ^= (result << 16 | result >>> 16) ^ (result << 27 ^ result >>> 5);
        result = result * 0x000D76CB ^ 0x7F4A7C15;
        return result;
    }

    /**
     * Takes a String phrase produced by {@link #toLongWordMnemonic(long, boolean)} and returns the long used to encode
     * that String. This can't take just any String; it must be produced by {@link #toLongWordMnemonic(long, boolean)}
     * to give a meaningful result.
     * @param mnemonic a String phrase produced by {@link #toLongWordMnemonic(long, boolean)}
     * @return the long used to generate {@code mnemonic} originally
     */
    public long fromLongWordMnemonic(String mnemonic)
    {
        final int adjectiveCount = allAdjectives.size(), nounCount = allNouns.size();
        int idx = mnemonic.indexOf(' ');
        long factor = adjectiveCount;
        boolean negative, odd;
        long result = allAdjectives.indexOf(StringTools.safeSubstring(mnemonic, 0, idx));
        result += factor * allNouns.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        negative = (mnemonic.charAt(idx + 1) == 'a');
        if(negative) idx += 8; // " and the"
        else idx += 7;         // " of the"
        result += (factor *= nounCount) * allAdjectives.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        result += (factor *= adjectiveCount) * allNouns.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        idx += 7; // " versus"
        result += (factor *= nounCount) * allAdjectives.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        result += (factor *= adjectiveCount) * allNouns.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        odd = (mnemonic.charAt(idx + 1) == 'a');
        if(odd) idx += 8;      // " and the"
        else idx += 7;         // " of the"
        result += (factor *= nounCount) * allAdjectives.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        result += (factor * adjectiveCount) * allNouns.indexOf(StringTools.safeSubstring(mnemonic, idx + 1, -1));

        result <<= 1;
        if(negative) result = ~result;
        if(odd) result |= 1L;
        result ^= result >>> 29 ^ result >>> 58;
        result *= 0xDD01F46A7E6FFC65L;
        result ^= result >>> 32;
        result *= 0xDD01F46A7E6FFC65L;
        result ^= result >>> 29 ^ result >>> 58;
        result *= 0xDD01F46A7E6FFC65L;
        result ^= result >>> 32;
        result ^= 0xABC98388FB8FAC03L;
        return result;
    }


    /**
     * Given any long, generates a short phrase that can be decoded back to the original long with
     * {@link #fromWordMnemonic(String)}. Examples of what this can produce are "Mindful warriors and the pure torch"
     * and "Dynastic earldom and the thousandfold bandit", generated by a Mnemonic with a seed of 1 from -587415991 and
     * -1105099633, respectively. Those Strings were generated using the current state of {@link Thesaurus} and the
     * adjectives and nouns it stores now, and if Thesaurus is added to over time, those Strings won't correspond to
     * those longs anymore. The Strings this returns vary in length. The words this uses by default use only the letters
     * a-z and the single quote (with A-Z for the first character if capitalize is true), with space separating words.
     * If you constructed this Mnemonic with adjective and noun collections or arrays, then this will use only those
     * words and will still separate words with space (and it will capitalize the first char if capitalize is true).
     * @param number any int
     * @param capitalize if true, the initial letter of the returned mnemonic String will be capitalized
     * @return a short phrase that will be uniquely related to number
     */
    public String toLongWordMnemonic(long number, boolean capitalize)
    {
        final long adjectiveCount = allAdjectives.size(), nounCount = allNouns.size();
        StringBuilder sb = new StringBuilder(80);
        // Hasher.randomize3(long), but in a form that we know won't ever change.
        number ^= 0xABC98388FB8FAC03L;
        number ^= number >>> 32;
        number *= 0xBEA225F9EB34556DL;
        number ^= number >>> 29;
        number *= 0xBEA225F9EB34556DL;
        number ^= number >>> 32;
        number *= 0xBEA225F9EB34556DL;
        number ^= number >>> 29;
        boolean negative = (number < 0), odd = (number & 1) == 1;
        if(negative) number = ~number;
        number >>>= 1;
        sb.append(allAdjectives.getAt((int)(number % adjectiveCount))).append(' ')
                .append(allNouns.getAt((int)((number /= adjectiveCount) % nounCount)))
                .append(negative ? " and the " : " of the ")
                .append(allAdjectives.getAt((int)((number /= nounCount) % adjectiveCount))).append(' ')
                .append(allNouns.getAt((int)((number /= adjectiveCount) % nounCount))).append(" versus ")
                .append(allAdjectives.getAt((int)((number /= nounCount) % adjectiveCount))).append(' ')
                .append(allNouns.getAt((int)((number /= adjectiveCount) % nounCount)))
                .append(odd ? " and the " : " of the ")
                .append(allAdjectives.getAt((int)((number /= nounCount) % adjectiveCount))).append(' ')
                .append(allNouns.getAt((int)((number / adjectiveCount))));
        if(capitalize)
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mnemonic mnemonic = (Mnemonic) o;

        if (!items.equals(mnemonic.items)) return false;
        if (!allAdjectives.equals(mnemonic.allAdjectives)) return false;
        return allNouns.equals(mnemonic.allNouns);
    }

    @Override
    public int hashCode() {
        int result = items.hashCode();
        result = 31 * result + allAdjectives.hashCode();
        result = 31 * result + allNouns.hashCode();
        return result;
    }
}
