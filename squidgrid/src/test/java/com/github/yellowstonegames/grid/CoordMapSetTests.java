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
}
