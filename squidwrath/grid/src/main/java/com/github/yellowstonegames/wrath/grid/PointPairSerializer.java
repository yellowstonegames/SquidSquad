/*
 * Copyright (c) 2024 See AUTHORS file.
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

package com.github.yellowstonegames.wrath.grid;

import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.crux.PointPair;
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

/**
 * Needs the type of the points to be registered, such as with
 * {@link CoordSerializer} for {@link com.github.yellowstonegames.grid.Coord}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PointPairSerializer extends Serializer<PointPair> {
    public PointPairSerializer(Fury fury) {
        super(fury, PointPair.class);
    }

    @Override
    public void write(MemoryBuffer buffer, PointPair value) {
        fury.writeRef(buffer, value.a);
        fury.writeRef(buffer, value.b);
    }

    @Override
    public PointPair read(MemoryBuffer buffer) {
        return new PointPair((PointN<?>)fury.readRef(buffer), (PointN<?>)fury.readRef(buffer));
    }
}
