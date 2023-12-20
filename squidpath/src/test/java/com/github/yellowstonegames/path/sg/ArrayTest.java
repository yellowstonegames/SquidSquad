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

package com.github.yellowstonegames.path.sg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArrayTest {
    @Test
    public void addAllShouldAddAllItemsFromSourceToTargetAndResizeTarget() {
        Array<Integer> target = new Array<>(0);
        Array<Integer> source = new Array<>();
        source.add(3);
        target.add(1);
        target.add(2);
        target.addAll(source);
        assertEquals("Target Array has wrong size.", 3, target.size());
        assertEquals("Item 0 of Target Array was overwritten.", target.get(0), Integer.valueOf(1));
        assertEquals("Item 1 of Target Array was overwritten.", target.get(1), Integer.valueOf(2));
        assertEquals("Item 0 of Source Array was not copied.", target.get(2), Integer.valueOf(3));
    }
    @Test
    public void addAllShouldAddAllItemsFromSourceToTargetAndUpdateTargetSize() {
        Array<Integer> target = new Array<>();
        Array<Integer> source = new Array<>();
        target.add(0);
        source.add(1);
        target.addAll(source);
        assertEquals("Target Array has wrong size.", 2, target.size());
        assertEquals("Item of Target Array was overwritten.", target.get(0), Integer.valueOf(0));
        assertEquals("Item of Source Array was not copied.", target.get(1), source.get(0));
    }
}
