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
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.ds.CaseInsensitiveSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import regexodus.Category;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MarkovTest {
    @Test
    public void testBasicMarkov() {
        if(!"true".equals(System.getenv("printing"))) return;
        long seed = 10040L;
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
        seed = 10040L;
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
     * in the  public domain.
     * @throws IOException if "src/test/resources/goetia.txt" cannot be read
     */
    @Test
    public void testGoetiaMarkov() throws IOException {
        if(!"true".equals(System.getenv("printing"))) return;
        long seed = 10040L;
        String goetia = new String(Files.readAllBytes(Paths.get("src/test/resources/goetia.txt")), StandardCharsets.UTF_8);
        MarkovText markovText = new MarkovText();
        markovText.analyze(goetia);
        for (int i = 0; i < 20; i++) {
            System.out.println(markovText.chain(++seed, 120));
        }

        CaseInsensitiveSet names = new CaseInsensitiveSet(ArrayTools.stringSpan(48, 72)){
            @Override
            protected int place(Object item) {
                long hash = hashMultiplier;
                CharSequence cs = (CharSequence) item;
                for (int i = 0, len = cs.length(); i < len; i++) {
                    char c = cs.charAt(i);
                    if(Category.L.contains(c)){
                        hash = Hasher.randomize3(hash + Character.toUpperCase(c));
                    }
                }
                return (int)hash & mask;
            }

            @Override
            protected boolean equate(Object left, @Nullable Object right) {
                CharSequence a = (CharSequence) left;
                CharSequence b = (CharSequence) right;
                if (a == b)
                    return true;
                if(b == null) return false;
                CharSequence l = (CharSequence)left, r = (CharSequence)right;
                int llen = l.length(), rlen = r.length();
                int cl = -1, cr = -1;
                int i = 0, j = 0;
//                int lettersL = 0, lettersR = 0;
                for (; i < llen || j < rlen;) {
                    if(i == llen) cl = -1;
                    else {
                        while (i < llen && !Category.L.contains((char) (cl = l.charAt(i++)))) {
                            cl = -1;
                        }
                    }
                    if(j == rlen) cr = -1;
                    else {
                        while (j < rlen && !Category.L.contains((char) (cr = r.charAt(j++)))) {
                            cr = -1;
                        }
                    }
                    if(cl != cr && Character.toUpperCase(cl) != Character.toUpperCase(cr))
                        return false;
                }
                return true;
            }
        };

        MarkovText copy = markovText.copy();

        seed = 10040L;
        System.out.println();
        Translator cipher = new Translator(Language.LOVECRAFT);
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.println(copy.chain(++seed, 120));
        }
        seed = 10040L;
        System.out.println();
        cipher = new Translator(Language.INFERNAL);
        copy = markovText.copy();
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.println(copy.chain(++seed, 120));
        }
        seed = 10040L;
        System.out.println();
        cipher = new Translator(Language.DEMONIC);
        copy = markovText.copy();
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.println(copy.chain(++seed, 120));
        }
        seed = 10040L;
        System.out.println();
        cipher = new Translator(Language.ANCIENT_EGYPTIAN);
        copy = markovText.copy();
        copy.changeNames(cipher, names);
        for (int i = 0; i < 20; i++) {
            System.out.println(copy.chain(++seed, 120));
        }
    }

}
