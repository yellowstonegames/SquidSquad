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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.support.util.PartialParser;
import org.junit.Assert;
import org.junit.Test;

public class CoordMapSetTests {
    @Test
    public void testAddLegibleSet() {
        CoordSet base = CoordSet.with(Coord.get(1, 1));
        CoordSet adding = CoordSet.with(Coord.get(0, 0), Coord.get(0, 1), Coord.get(1, 0), Coord.get(1, 1), Coord.get(20, 20));
        String str = adding.toString(";");
        System.out.println(str);
        base.addLegible(str, ";");
        Assert.assertEquals(base, adding);
    }
    @Test
    public void testPutLegibleMaps() {
        {
            CoordIntMap base = CoordIntMap.with(Coord.get(1, 1), 3);
            CoordIntMap adding = CoordIntMap.with(Coord.get(0, 0), 0, Coord.get(0, 1), 1, Coord.get(1, 0), 2, Coord.get(1, 1), 3, Coord.get(20, 20), 1000000);
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";");
            Assert.assertEquals(base, adding);
        }
        {
            CoordFloatMap base = CoordFloatMap.with(Coord.get(1, 1), 3);
            CoordFloatMap adding = CoordFloatMap.with(Coord.get(0, 0), 0, Coord.get(0, 1), 1, Coord.get(1, 0), 2, Coord.get(1, 1), 3, Coord.get(20, 20), 1000000);
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";");
            Assert.assertEquals(base, adding);
        }
        {
            CoordLongMap base = CoordLongMap.with(Coord.get(1, 1), 3);
            CoordLongMap adding = CoordLongMap.with(Coord.get(0, 0), 0, Coord.get(0, 1), 1, Coord.get(1, 0), 2, Coord.get(1, 1), 3, Coord.get(20, 20), 1000000);
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";");
            Assert.assertEquals(base, adding);
        }
        {
            CoordObjectMap<String> base = CoordObjectMap.with(Coord.get(1, 1), "quux");
            CoordObjectMap<String> adding = CoordObjectMap.with(Coord.get(0, 0), "foo", Coord.get(0, 1), "bar", Coord.get(1, 0), "baz", Coord.get(1, 1), "quux", Coord.get(20, 20), "Ziltoid the Omniscient");
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";", PartialParser.DEFAULT_STRING);
            Assert.assertEquals(base, adding);
        }
        {
            CoordIntOrderedMap base = CoordIntOrderedMap.with(Coord.get(1, 1), 3);
            CoordIntOrderedMap adding = CoordIntOrderedMap.with(Coord.get(0, 0), 0, Coord.get(0, 1), 1, Coord.get(1, 0), 2, Coord.get(1, 1), 3, Coord.get(20, 20), 1000000);
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";");
            Assert.assertEquals(base, adding);
        }
        {
            CoordFloatOrderedMap base = CoordFloatOrderedMap.with(Coord.get(1, 1), 3);
            CoordFloatOrderedMap adding = CoordFloatOrderedMap.with(Coord.get(0, 0), 0, Coord.get(0, 1), 1, Coord.get(1, 0), 2, Coord.get(1, 1), 3, Coord.get(20, 20), 1000000);
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";");
            Assert.assertEquals(base, adding);
        }
        {
            CoordLongOrderedMap base = CoordLongOrderedMap.with(Coord.get(1, 1), 3);
            CoordLongOrderedMap adding = CoordLongOrderedMap.with(Coord.get(0, 0), 0, Coord.get(0, 1), 1, Coord.get(1, 0), 2, Coord.get(1, 1), 3, Coord.get(20, 20), 1000000);
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";");
            Assert.assertEquals(base, adding);
        }
        {
            CoordObjectOrderedMap<String> base = CoordObjectOrderedMap.with(Coord.get(1, 1), "quux");
            CoordObjectOrderedMap<String> adding = CoordObjectOrderedMap.with(Coord.get(0, 0), "foo", Coord.get(0, 1), "bar", Coord.get(1, 0), "baz", Coord.get(1, 1), "quux", Coord.get(20, 20), "Ziltoid the Omniscient");
            String str = adding.toString(";");
            System.out.println(str);
            base.putLegible(str, ";", PartialParser.DEFAULT_STRING);
            Assert.assertEquals(base, adding);
        }
    }
}
