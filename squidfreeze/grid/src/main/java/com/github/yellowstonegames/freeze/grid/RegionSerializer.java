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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.grid.Region;

/**
 * Serializer for {@link Region}; does not need any other classes to be registered, although registering Coord with
 * {@link CoordSerializer} is a good idea. Uses a compressed String representation that may be better than a naive
 * uncompressed approach, even a binary one.
 */
public class RegionSerializer extends Serializer<Region> {
    public RegionSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Region data) {
        output.writeString(data.toCompressedString());
    }

    @Override
    public Region read(final Kryo kryo, final Input input, final Class<? extends Region> dataClass) {
        return Region.decompress(input.readString());
    }

    @Override
    public Region copy(Kryo kryo, Region original) {
        return new Region(original);
    }
}
