/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.core;

import org.junit.Assert;
import org.junit.Test;
import regexodus.PatternSyntaxException;

import java.util.regex.Pattern;

/**
 * Abduqodiri Qurbonzoda noted an interesting failure case for the JDK's regex engine
 * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">in this Kotlin bug report</a>.
 * This is where I'm trying to figure out the point where the bug occurs for various regexes, and possible remedies.
 * I'm not using Kotlin here, so I won't be able to test the Kotlin Native part of the bug, just Java.
 */
public class RegexLimitTest {
    public static final int LIMIT = 10000;

    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * Indeed, this version does crash Java's regex engine.
     */
    @Test(expected = StackOverflowError.class)
    public void testGroupedOrJUR() {
        Pattern pattern = Pattern.compile("(a|b)+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }

    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * Using a non-capturing group does not help java.util.regex code; this also crashes.
     */
    @Test(expected = StackOverflowError.class)
    public void testNCGroupedOrJUR() {
        Pattern pattern = Pattern.compile("(?:a|b)+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }

    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * Using an independent non-capturing group actually allows this to pass! Note that this is likely to fail on GWT.
     */
    @Test
    public void testINCGroupedOrJUR() {
        Pattern pattern = Pattern.compile("(?>a|b)+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }

    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * Using a character class works where a normal (non-independent) group crashes. It isn't applicable in the same
     * places, though.
     */
    @Test
    public void testCharClassJUR() {
        Pattern pattern = Pattern.compile("[ab]+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }


    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * This does not crash RegExodus! The 2001-era code wins! Cue the dancing baby GIF!
     */
    @Test
    public void testGroupedOrRegExodus() {
        regexodus.Pattern pattern = regexodus.Pattern.compile("(a|b)+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }


    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * This does not crash RegExodus either!
     */
    @Test
    public void testNCGroupedOrRegExodus() {
        regexodus.Pattern pattern = regexodus.Pattern.compile("(?:a|b)+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }

    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * This does not crash RegExodus either!
     */
    @Test
    public void testCharClassRegExodus() {
        regexodus.Pattern pattern = regexodus.Pattern.compile("[ab]+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }

    /**
     * <a href="https://youtrack.jetbrains.com/issue/KT-46211/Kotlin-Native-Stack-overflow-crash-in-Regex-classes-with-simple-pattern-and-very-large-input#focus=Comments-27-6469342.0-0">Based on this Kotlin bug report</a>.
     * The atomic or independent non-capturing group syntax doesn't seem to be fully implemented in RegExodus. You can
     * have one atomic group, but it can't be repeated with {@code +}, {@code *}, or similar qualifiers.
     */
    @Test(expected = PatternSyntaxException.class)
    public void testINCGroupedOrRegExodus() {
        regexodus.Pattern pattern = regexodus.Pattern.compile("(?>a|b)+");
        StringBuilder sb = new StringBuilder(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            sb.append('a');
        }
        Assert.assertTrue(pattern.matcher(sb).matches());
    }
}
