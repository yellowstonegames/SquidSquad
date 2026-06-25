/*
 * Copyright (c) 2026; see AUTHORS file.
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

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class MarkupTest {
    /**
     * Needed by {@link #stripMarkup(CharSequence)}.
     */
    public static final Pattern MARKUP_PATTERN = Pattern.compile("\\[((?<ESCAPE>\\[)|([^]]*]))");
    /**
     * Given a String or other CharSequence, this removes any libGDX color name markup from text, as well as the "undo
     * color markup" syntax {@code []}, and escapes the term {@code [[} into one square bracket.
     * @param text a CharSequence such as a String that may contain libGDX color markup
     * @return the contents of text without any markup, and with {@code [[} escaped to {@code [}
     */
    public static String stripMarkup(CharSequence text) {
        return MARKUP_PATTERN.matcher(text).replaceAll("${ESCAPE}");
    }

    // the rest is used in tests.

    public static final String MARKUP_0 = "[RED]Kitten [BLUE]Mittens[]! ][[You'll be smitten!][";
    public static final String CLEAN_0 = "Kitten Mittens! ][You'll be smitten!][";

    @Test
    public void testStripMarkup() {
        System.out.println(CLEAN_0);
        System.out.println(MARKUP_PATTERN.matcher(MARKUP_0).replaceAll("${ESCAPE}"));
        Assert.assertEquals(CLEAN_0, MARKUP_PATTERN.matcher(MARKUP_0).replaceAll("${ESCAPE}"));

    }
}
