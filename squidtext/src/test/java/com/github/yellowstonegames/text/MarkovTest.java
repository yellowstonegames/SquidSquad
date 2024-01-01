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
import com.github.tommyettinger.digital.TextTools;
import com.github.tommyettinger.ds.CharFilter;
import com.github.tommyettinger.ds.FilteredStringSet;
import com.github.yellowstonegames.TextInternals;
import com.github.yellowstonegames.core.StringTools;
import org.junit.Test;
import regexodus.Category;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MarkovTest {
    @Test
    public void testBasicMarkov() {
        if(!TextInternals.PRINTING) return;
        long seed = 123456789L;
        String oz = "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a " +
                "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the " +
                "lumber to build it had to be carried by wagon many miles. There were four walls, " +
                "a floor and a roof, which made one room; and this room contained a rusty looking " +
                "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. " +
                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in " +
                "another corner. There was no garret at all, and no cellar-except a small hole dug " +
                "in the ground, called a cyclone cellar, where the family could go in case one of " +
                "those great whirlwinds arose, mighty enough to crush any building in its path. It " +
                "was reached by a trap door in the middle of the floor, from which a ladder led " +
                "down into the small, dark hole. When Dorothy stood in the doorway and looked around, " +
                "she could see nothing but the great gray prairie on every side. Not a tree nor a house " +
                "broke the broad sweep of flat country that reached to the edge of the sky in all directions. " +
                "The sun had baked the plowed land into a gray mass, with little cracks running through it. " +
                "Even the grass was not green, for the sun had burned the tops of the long blades until they " +
                "were the same gray color to be seen everywhere. Once the house had been painted, but the sun " +
                "blistered the paint and the rains washed it away, and now the house was as dull and gray as " +
                "everything else.";
        MarkovText markovText = new MarkovText();
        markovText.analyze(oz);
        for (int i = 0; i < 40; i++) {
            System.out.println(markovText.chain(++seed, 100 + (i * 2)));
        }
        seed = 123456789L;
        System.out.println();
        Translator cipher = new Translator(Language.JAPANESE_ROMANIZED);
        markovText.changeNames(cipher);
        for (int i = 0; i < 40; i++) {
            System.out.println(markovText.chain(++seed, 100 + (i * 2)));
        }
    }

    /**
     * I should give credit where it is due; goetia.txt is from
     * <a href="https://en.wikisource.org/wiki/The_Lesser_Key_of_Solomon#Shemhamphorash">The Lesser Key of Solomon</a>,
     * in the public domain.
     * @throws IOException if "src/test/resources/goetia.txt" cannot be read
     */
    @Test
    public void testGoetiaMarkov() throws IOException {
        if(!TextInternals.PRINTING) return;
        long seed = 123456789L;
        String goetia = new String(Files.readAllBytes(Paths.get("src/test/resources/goetia.txt")), StandardCharsets.UTF_8);
        MarkovText markovText = new MarkovText();
        markovText.analyze(goetia);
        for (int i = 0; i < 20; i++) {
            System.out.print("..."+markovText.chain(++seed, 70).replaceFirst("\\P{L}+$", "... "));
        }
        
        FilteredStringSet names = FilteredStringSet.with(CharFilter.getOrCreate("LetterOnlyCaseInsensitive", Category.L::contains, Character::toUpperCase), ArrayTools.stringSpan(48, 72));

        MarkovText copy = markovText.copy();

        seed = 123456789L;
        System.out.println();
        Translator cipher = new Translator(Language.LOVECRAFT);
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.print("..."+copy.chain(++seed, 70).replaceFirst("\\P{L}+$", "... "));
        }
        seed = 123456789L;
        System.out.println();
        cipher = new Translator(Language.INFERNAL);
        copy = markovText.copy();
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.print("..."+copy.chain(++seed, 70).replaceFirst("\\P{L}+$", "... "));
        }
        seed = 123456789L;
        System.out.println();
        cipher = new Translator(Language.DEMONIC);
        copy = markovText.copy();
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.print("..."+copy.chain(++seed, 70).replaceFirst("\\P{L}+$", "... "));
        }
        seed = 123456789L;
        System.out.println();
        cipher = new Translator(Language.ANCIENT_EGYPTIAN);
        copy = markovText.copy();
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.print("..."+copy.chain(++seed, 70).replaceFirst("\\P{L}+$", "... "));
        }
    }

    @Test
    public void testMarkovChar() {
        if (!TextInternals.PRINTING) return;
        String[] names = ArrayTools.stringSpan(120, 72);
        for (int i = 0; i < names.length; i++) {
            names[i] = StringTools.capitalize(names[i]) + ' ' + Language.LOVECRAFT.word(~i, true).replace('-', '\'');
        }
        String merged = TextTools.join(" ", names);
        MarkovChar markov = new MarkovChar();
        markov.analyze(merged);
        System.out.print(markov.chain(1234567890L, 30));
        for (int i = 1; i < 20; i++) {
            System.out.print(", " + markov.chain(i + 12345678L, 30));
        }
        System.out.println();
    }
}

//        CaseInsensitiveSet names = new CaseInsensitiveSet(64){
//
//            @Override
//            protected int place(Object item) {
//                long hash = 0x9E3779B97F4A7C15L; // golden ratio
//                CharSequence cs = (CharSequence) item;
//                for (int i = 0, len = cs.length(); i < len; i++) {
//                    char c = cs.charAt(i);
//                    if(Category.L.contains(c)){
//                        hash = (hash + Character.toUpperCase(c)) * hashMultiplier;
//                    }
//                }
//                return (int)(hash >>> shift);
//            }
//
//            @Override
//            protected boolean equate(Object left, @Nullable Object right) {
//                if (left == right)
//                    return true;
//                if(right == null) return false;
//                CharSequence l = (CharSequence)left, r = (CharSequence)right;
//                int llen = l.length(), rlen = r.length();
//                int cl = -1, cr = -1;
//                int i = 0, j = 0;
//                while (i < llen || j < rlen) {
//                    if(i == llen) cl = -1;
//                    else {
//                        while (i < llen && !Category.L.contains((char) (cl = l.charAt(i++)))) {
//                            cl = -1;
//                        }
//                    }
//                    if(j == rlen) cr = -1;
//                    else {
//                        while (j < rlen && !Category.L.contains((char) (cr = r.charAt(j++)))) {
//                            cr = -1;
//                        }
//                    }
//                    if(cl != cr && Character.toUpperCase(cl) != Character.toUpperCase(cr))
//                        return false;
//                }
//                return true;
//            }
//        };
//        names.addAll(ArrayTools.stringSpan(48, 72));
