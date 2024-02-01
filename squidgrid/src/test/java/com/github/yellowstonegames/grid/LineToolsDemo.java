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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.place.DungeonTools;

public class LineToolsDemo {
    public static void main(String[] args) {
        WhiskerRandom rng = new WhiskerRandom(123);
        long r, h;
        r = Region.approximateBits(rng, 15);
        h = LineTools.flipHorizontal4x4(r);
        char[][] show = ArrayTools.fill(' ', 50, 24);
        ArrayTools.insert(LineTools.decode4x4(r), show, 4, 2);
        ArrayTools.insert(LineTools.decode4x4(r), show, 4, 2);
        ArrayTools.insert(LineTools.decode4x4(h), show, 8, 2);
        ArrayTools.insert(LineTools.decode4x4(LineTools.flipVertical4x4(r)), show, 4, 6);
        ArrayTools.insert(LineTools.decode4x4(LineTools.flipVertical4x4(h)), show, 8, 6);

        r = Region.approximateBits(rng, 13);
        h = Region.approximateBits(rng, 13);
        ArrayTools.insert(LineTools.decode4x4(r), show, 14, 2);
        ArrayTools.insert(LineTools.decode4x4(LineTools.flipHorizontal4x4(r)), show, 18, 2);
        ArrayTools.insert(LineTools.decode4x4(h), show, 14, 6);
        ArrayTools.insert(LineTools.decode4x4(LineTools.flipHorizontal4x4(h)), show, 18, 6);

        r = Region.approximateBits(rng, 10) & LineTools.interiorCircleLarge;
        r ^= LineTools.transpose4x4(r);
        r |= LineTools.exteriorCircleLarge;
        h = LineTools.flipHorizontal4x4(r);
        ArrayTools.insert(LineTools.decode4x4(r), show, 24, 2);
        ArrayTools.insert(LineTools.decode4x4(h), show, 28, 2);
        ArrayTools.insert(LineTools.decode4x4(LineTools.flipVertical4x4(r)), show, 24, 6);
        ArrayTools.insert(LineTools.decode4x4(LineTools.flipVertical4x4(h)), show, 28, 6);


        r = Region.approximateBits(rng, 15);
        h = (r & LineTools.shallowInteriorSquareLarge) | LineTools.exteriorSquareLarge;
        ArrayTools.insert(LineTools.decode4x4(h), show, 34, 2);
        h = LineTools.flipHorizontal4x4(h);
        ArrayTools.insert(LineTools.decode4x4(h), show, 38, 2);

        r = Region.approximateBits(rng, 14);
        h = LineTools.flipVertical4x4(
                (r & LineTools.shallowerInteriorSquareLarge) | LineTools.exteriorDiamondLarge);
        ArrayTools.insert(LineTools.decode4x4(h), show, 34, 6);
        h = LineTools.flipHorizontal4x4(h);
        ArrayTools.insert(LineTools.decode4x4(h), show, 38, 6);


        r = Region.approximateBits(rng, 36);
        r &= LineTools.flipHorizontal4x4(r);

        h = Region.approximateBits(rng, 36);
        h &= LineTools.flipHorizontal4x4(h);
        ArrayTools.insert(LineTools.decode4x4(r), show, 4, 12);
        ArrayTools.insert(LineTools.decode4x4(h), show, 4, 16);

        r = Region.approximateBits(rng, 12);
        r ^= LineTools.flipHorizontal4x4(r);

        h = Region.approximateBits(rng, 12);
        h ^= LineTools.flipHorizontal4x4(h);
        ArrayTools.insert(LineTools.decode4x4(r), show, 11, 12);
        ArrayTools.insert(LineTools.decode4x4(h), show, 11, 16);

        r = Region.approximateBits(rng, 10);
        r |= LineTools.flipHorizontal4x4(r);

        h = Region.approximateBits(rng, 10);
        h |= LineTools.flipHorizontal4x4(h);
        ArrayTools.insert(LineTools.decode4x4(r), show, 18, 12);
        ArrayTools.insert(LineTools.decode4x4(h), show, 18, 16);

        DungeonTools.debugPrint(show);
    }
}
