/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.yellowstonegames.text;

import com.github.tommyettinger.ds.*;
import com.github.tommyettinger.ds.support.util.IntIterator;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.core.WeightedTable;

import java.util.*;

/**
 * Based on work by Nolithius available at the following two sites:
 * <a href="https://github.com/Nolithius/weighted-letter-namegen">GitHub for weighted-letter-namegen</a>
 * <a href="http://code.google.com/p/weighted-letter-namegen/">Google Code for weighted-letter-namegen</a>
 *
 * @see Language Language is meant for generating more than just names, and can imitate language styles.
 * @author Eben Howard
 */
public class NameGenerator {
//<editor-fold defaultstate="collapsed" desc="Viking Style static name list">

    public static final String[] VIKING_STYLE_NAMES = new String[]{
            "Andor",
            "Baatar",
            "Balder",
            "Beowulf",
            "Drogo",
            "Freya",
            "Frigg",
            "Grog",
            "Grunt",
            "Hodor",
            "Hrothgar",
            "Hrun",
            "Ingmar",
            "Korg",
            "Loki",
            "Lothar",
            "Odin",
            "Ragnar",
            "Theodrin",
            "Thor",
            "Yngvar",
            "Xandor"
    };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Star Wars Style static name list">
    public static final String[] STAR_WARS_STYLE_NAMES = new String[]{
            "Lutoif Vap",
            "Nasoi Seert",
            "Jitpai",
            "Sose Tog",
            "Vainau",
            "Jairkau",
            "Tirka Kist",
            "Boush",
            "Wofe Fou",
            "Voxin Voges",
            "Koux Boiti",
            "Loim Dar",
            "Gaungu",
            "Mut Tep",
            "Foimo Saispi",
            "Toneeg Vaiba",
            "Nix Nast",
            "Gup Dangisp",
            "Distark Toonausp",
            "Tex Brinki",
            "Kat Tosha",
            "Tauna Foip",
            "Frip Kex",
            "Feda Lun",
            "Tafa",
            "Zeesheerk",
            "Cremoim Kixoop",
            "Pago",
            "Kesha Diplo"
    };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA male names static name list">
    public static final String[] COMMON_USA_MALE_NAMES = new String[]{
            "James",
            "John",
            "Robert",
            "Michael",
            "William",
            "David",
            "Richard",
            "Charles",
            "Joseph",
            "Thomas",
            "Christopher",
            "Daniel",
            "Paul",
            "Mark",
            "Donald",
            "George",
            "Kenneth",
            "Steven",
            "Edward",
            "Brian",
            "Ronald",
            "Anthony",
            "Kevin",
            "Jason",
            "Matthew",
            "Gary",
            "Timothy",
            "Jose",
            "Larry",
            "Jeffrey",
            "Frank",
            "Scott",
            "Eric",
            "Stephen",
            "Andrew",
            "Raymond",
            "Gregory",
            "Joshua",
            "Jerry",
            "Dennis",
            "Walter",
            "Patrick",
            "Peter",
            "Harold",
            "Douglas",
            "Henry",
            "Carl",
            "Arthur",
            "Ryan",
            "Roger"
    };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA female names static name list">
    public static final String[] COMMON_USA_FEMALE_NAMES = new String[]{
            "Mary",
            "Patricia",
            "Linda",
            "Barbara",
            "Elizabeth",
            "Jennifer",
            "Maria",
            "Susan",
            "Margaret",
            "Dorothy",
            "Lisa",
            "Nancy",
            "Karen",
            "Betty",
            "Helen",
            "Sandra",
            "Donna",
            "Carol",
            "Ruth",
            "Sharon",
            "Michelle",
            "Laura",
            "Sarah",
            "Kimberly",
            "Deborah",
            "Jessica",
            "Shirley",
            "Cynthia",
            "Angela",
            "Melissa",
            "Brenda",
            "Amy",
            "Anna",
            "Crystal",
            "Virginia",
            "Kathleen",
            "Pamela",
            "Martha",
            "Becky",
            "Amanda",
            "Stephanie",
            "Carolyn",
            "Christine",
            "Pearl",
            "Janet",
            "Catherine",
            "Frances",
            "Ann",
            "Joyce",
            "Diane",
            "Jane",
            "Shauna",
            "Trisha",
            "Eileen",
            "Danielle",
            "Jacquelyn",
            "Lynn",
            "Hannah",
            "Brittany"
    };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA last names static name list">
    public static final String[] COMMON_USA_LAST_NAMES = new String[]{
            "Smith",
            "Johnson",
            "Williams",
            "Brown",
            "Jones",
            "Miller",
            "Davis",
            "Wilson",
            "Anderson",
            "Taylor",
            "Black",
            "Moore",
            "Martin",
            "Jackson",
            "Thompson",
            "White",
            "Clark",
            "Lewis",
            "Robinson",
            "Walker",
            "Willis",
            "Carter",
            "King",
            "Lee",
            "Grant",
            "Howard",
            "Morris",
            "Bartlett",
            "Paine",
            "Wayne",
            "Lorraine"
    };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Lovecraft Mythos style static name list">
    public static final String[] LOVECRAFT_MYTHOS_NAMES = new String[]{
            "Koth",
            "Ghlatelilt",
            "Siarlut",
            "Nyogongogg",
            "Nyialan",
            "Nyithiark",
            "Lyun",
            "Kethoshigr",
            "Shobik",
            "Tekogr",
            "Hru-yn",
            "Lya-ehibos",
            "Hruna-oma-ult",
            "Shabo'en",
            "Shrashangal",
            "Shukhaniark",
            "Thaghum",
            "Shrilang",
            "Lukhungu'ith",
            "Nyun",
            "Nyia-ongin",
            "Shogia-usun",
            "Lyu-yl",
            "Liathiagragr",
            "Cthathagg",
            "Hri'osurkut",
            "Shothegh",
            "No-orleshigh",
            "Zvriangekh",
            "Nyesashiv",
            "Lyarkio",
            "Le'akh",
            "Liashi-en",
            "Shurkano'im",
            "Hrakhanoth",
            "Ghlotsuban",
            "Cthitughias",
            "Ftanugh"
    };
//</editor-fold>

    private static final char[] vowels = {'a', 'e', 'i', 'o', 'u'};//not using y because it looks strange as a vowel in names
    private static final int LAST_LETTER_CANDIDATES_MAX = 52;

    private EnhancedRandom rng;
    private String[] names;
    private int consonantLimit;
    private IntList sizes;
    private IntObjectMap<IntObjectMap<TableGroup>> letters;
    private CharList firstLetterSamples;
    private CharList lastLetterSamples;
    private final StringDistance dla = new StringDistance(1, 1, 1, 1);

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names an array of Strings that are typical names to be emulated
     */
    public NameGenerator(String[] names) {
        this(names, 2);
    }

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names          an array of Strings that are typical names to be emulated
     * @param consonantLimit the maximum allowed consonants in a row
     */
    public NameGenerator(String[] names, int consonantLimit) {
        this(names, consonantLimit, new AceRandom());
    }

    /**
     * Creates the generator by seeding the provided list of names. The given RNG will be used for
     * all random decisions this has to make, so if it has the same state (and RandomnessSource) on
     * different runs through the program, it will produce the same names reliably.
     *
     * @param names          an array of Strings that are typical names to be emulated
     * @param consonantLimit the maximum allowed consonants in a row
     * @param rng            the source of randomness to be used
     */
    public NameGenerator(String[] names, int consonantLimit, EnhancedRandom rng) {
        this.names = names;
        this.consonantLimit = consonantLimit;
        this.rng = rng;
        init();
    }

    /**
     * Initialization, statistically measures letter likelihood.
     */
    private void init() {
        sizes = new IntList();
        letters = new IntObjectMap<>(32);
        firstLetterSamples = new CharList();
        lastLetterSamples = new CharList();

        for (int i = 0; i < names.length - 1; i++) {
            String name = names[i];
            if (name == null || name.isEmpty()) {
                continue;
            }

            // (1) Insert size
            sizes.add(name.length());

            // (2) Grab first letter
            firstLetterSamples.add(name.charAt(0));

            // (3) Grab last letter
            lastLetterSamples.add(name.charAt(name.length() - 1));

            // (4) Process all letters
            for (int n = 0; n < name.length() - 1; n++) {
                char letter = name.charAt(n);
                char nextLetter = name.charAt(n + 1);

                // Create letter if it doesn't exist
                IntObjectMap<TableGroup> wl = letters.computeIfAbsent(letter, k -> new IntObjectMap<>());
                TableGroup basis = wl.get(letter);
                if (basis == null) {
                    basis = new TableGroup(new IntFloatOrderedMap());
                    wl.put(letter, basis);
                }
                basis.map.getAndIncrement(nextLetter, 0, 1);

                // If letter was uppercase (beginning of name), also add a lowercase entry
                if (StringTools.ALL_UNICODE_UPPERCASE_LETTER_SET.contains(letter)) {
                    letter = Character.toLowerCase(letter);

                    basis = wl.get(letter);
                    if (basis == null) {
                        basis = new TableGroup(new IntFloatOrderedMap());
                        wl.put(letter, basis);
                    }
                    basis.map.getAndIncrement(nextLetter, 0, 1);
                }
            }
        }
        IntIterator it = letters.keySet().iterator();
        while (it.hasNext()){
            char c = (char) it.nextInt();
            IntObjectMap<TableGroup> hmt = letters.computeIfAbsent(c, k -> new IntObjectMap<>());
            IntObjectMap.ValueIterator<TableGroup> it2 = letters.get(c).values().iterator();
            while (it2.hasNext()){
                it2.next().initialize();
            }
        }
    }

    private StringBuilder generateInner(StringBuilder name) {
        for (int runs = 0; runs < LAST_LETTER_CANDIDATES_MAX; runs++) {
            name.setLength(0);
            // Pick size
            int size = sizes.random(rng);

            // Pick first letter
            char latest = firstLetterSamples.random(rng);
            name.append(latest);

            for (int i = 1; i < size - 2; i++) {
                name.append(latest = getRandomNextLetter(latest));
            }

            // Attempt to find a last letter
            for (int lastLetterFits = 0; lastLetterFits < LAST_LETTER_CANDIDATES_MAX; lastLetterFits++) {
                char lastLetter = lastLetterSamples.random(rng);
                char intermediateLetterCandidate = getIntermediateLetter(latest, lastLetter);

                // Only attach last letter if the candidate is valid (if no candidate, the antepenultimate letter always occurs at the end)
                if (StringTools.ALL_UNICODE_LETTER_SET.contains(intermediateLetterCandidate)) {
                    name.append(intermediateLetterCandidate).append(lastLetter);
                    break;
                }
            }

            // Check that the word has no triple letter sequences, and that the Levenshtein distance is kosher
            if (validateGrouping(name) && checkLevenshtein(name)) {
                return name;
            }
        }
        name.setLength(0);
        return name.append(rng.randomElement(names));
    }
    /**
     * Gets one random String name.
     *
     * @return a single random String name
     */

    public String generate() {
        return generateInner(new StringBuilder(32)).toString();
    }

    /**
     * Gets an ArrayList of random String names, sized to match amountToGenerate.
     * @param amountToGenerate how many String items to include in the returned ArrayList
     * @return an ArrayList of random String names
     */
    public ArrayList<String> generateList(int amountToGenerate) {
        ArrayList<String> result = new ArrayList<>();

        StringBuilder name = new StringBuilder(32);
        for (int i = 0; i < amountToGenerate; i++) {
            result.add(generateInner(name).toString());
        }

        return result;
    }
    /**
     * Gets an array of random String names, sized to match amountToGenerate.
     *
     * @param amountToGenerate how many String items to include in the returned array
     * @return an array of random String names
     */

    public String[] generate(int amountToGenerate)
    {
        return generateList(amountToGenerate).toArray(new String[0]);
    }

    /**
     * Searches for the best fit letter between the letter before and the letter
     * after (non-random). Used to determine penultimate letters in names.
     *
     * @param	letterBefore	The letter before the desired letter.
     * @param	letterAfter	The letter after the desired letter.
     * @return	The best fit letter between the provided letters.
     */
    private char getIntermediateLetter(char letterBefore, char letterAfter) {
        if (StringTools.ALL_UNICODE_LETTER_SET.contains(letterBefore) && StringTools.ALL_UNICODE_LETTER_SET.contains(letterAfter)) {
            // First grab all letters that come after the 'letterBefore'
            IntObjectMap<TableGroup> wl = letters.get(letterBefore);
            if (wl == null) {
                return getRandomNextLetter(letterBefore);
            }
            IntList letterCandidates = wl.get(letterBefore).items;

            char bestFitLetter = '\'';
            float bestFitScore = 0;

            // Step through candidates, and return best scoring letter
            for (int l = 0; l < letterCandidates.size(); l++) {
                char letter = (char)letterCandidates.get(l);
                wl = letters.get(letter);
                if (wl == null) {
                    continue;
                }
                TableGroup weightedLetterGroup = wl.get(letterBefore);
                if (weightedLetterGroup != null) {
                    int index = weightedLetterGroup.items.indexOf(letterAfter);
                    if(index >= 0) {
                        float letterCounter = weightedLetterGroup.weights[index];
                        if (letterCounter > bestFitScore) {
                            bestFitLetter = letter;
                            bestFitScore = letterCounter;
                        }
                    }
                }
            }

            return bestFitLetter;
        } else {
            return '-';
        }
    }

    /**
     * Checks that no three letters happen in succession.
     *
     * @param	name	The name CharSequence
     * @return	True if no triple letter sequence is found.
     */
    private boolean validateGrouping(CharSequence name) {
        for (int i = 2; i < name.length(); i++) {
            char c0 = name.charAt(i);
            char c1 = name.charAt(i-1);
            char c2 = name.charAt(i-2);
            if ((c0 == c1 && c0 == c2) || !(isVowel(c0) || isVowel(c1) || isVowel(c2))) {
                return false;
            }
        }
        int consonants = 0;
        for (int i = 0; i < name.length(); i++) {
            if (isVowel(name.charAt(i))) {
                consonants = 0;
            } else {
                if (++consonants > consonantLimit) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isVowel(char c) {
        switch(c)
        {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
            case 'y':
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks that the Damerau-Levenshtein distance of this name is within a
     * given bias from a name on the master list.
     *
     * @param	name	The name string.
     * @return	True if a name is found that is within the bias.
     */
    private boolean checkLevenshtein(CharSequence name) {
        int levenshteinBias = name.length() / 2;

        for (String name1 : names) {
            int levenshteinDistance = dla.distance(name, name1);
            if (levenshteinDistance <= levenshteinBias) {
                return true;
            }
        }

        return false;
    }

    private char getRandomNextLetter(char letter) {
        if (letters.containsKey(letter)) {
            return letters.get(letter).get(letter).random(rng);
        } else {
            return vowels[rng.nextInt(5)];
        }
    }

    private static class TableGroup {
        public IntList items;
        public float[] weights;
        public WeightedTable table;
        public IntFloatOrderedMap map;

        public TableGroup(IntFloatOrderedMap map) {
            this.map = map;
        }

        public TableGroup initialize(){
            this.items = map.order();
            this.weights = map.values().toArray();
            this.table = new WeightedTable(weights);
            return this;
        }

        public char random(Random random) {
            return (char)items.get(table.random(random));
        }
    }
}
