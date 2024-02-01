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

package com.github.yellowstonegames.world.random;

import com.github.yellowstonegames.grid.IPointHash;

public class SpectatorPointHash extends IPointHash.IntImpl {
    @Override
    public int hashWithState(int x, int y, int state) {
        return hashAll(x, y, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return hashAll(x, y, z, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return hashAll(x, y, z, w, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return hashAll(x, y, z, w, u, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return hashAll(x, y, z, w, u, v, state);
    }

    public int hashAll(int x, int y, int s) {
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11)));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11)));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11)));
        return s ^ (s << 8 | s >>> 24) ^ (s << 13 | s >>> 19);
    }

    public static int hashAll(int x, int y, int z, int s) {
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        return s ^ (s << 8 | s >>> 24) ^ (s << 13 | s >>> 19);
    }

    public static int hashAll(int x, int y, int z, int w, int s) {
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + w));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + w));
        return s ^ (s << 8 | s >>> 24) ^ (s << 13 | s >>> 19);
    }

    public static int hashAll(int x, int y, int z, int w, int u, int s) {
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + w));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + u));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + w));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + u));
        return s ^ (s << 8 | s >>> 24) ^ (s << 13 | s >>> 19);
    }

    public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + w));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + u));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + v));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + z));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + w));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + u));
        s += (x = (x << 3  | x >>> 29) ^ (y = (y << 24 | y >>> 8 ) + x ^ (s << 21 | s >>> 11) + v));
        return s ^ (s << 8 | s >>> 24) ^ (s << 13 | s >>> 19);
    }

}
