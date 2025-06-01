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

package com.github.yellowstonegames.freeze.world;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.world.BiomeMapper;
import com.github.yellowstonegames.world.SimpleWorldMapView;
import com.github.yellowstonegames.world.WorldMapGenerator;

/**
 * Serializer for {@link SimpleWorldMapView}; needs the {@link WorldMapGenerator} implementation used by any given
 * SimpleWorldMapView to also be registered, as well as {@code int[]}. and {@code int[][]} registered (with no
 * serializer needed).
 */
public class SimpleWorldMapViewSerializer extends Serializer<SimpleWorldMapView> {
    public SimpleWorldMapViewSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final SimpleWorldMapView data) {
        kryo.writeClassAndObject(output, data.getWorld());
        output.writeString(data.getBiomeMapper().stringSerialize());
        kryo.writeObjectOrNull(output, data.biomeColorTable, int[].class);
        kryo.writeObjectOrNull(output, data.biomeDarkColorTable, int[].class);
        kryo.writeObjectOrNull(output, data.getColorMap(), int[][].class);
        kryo.writeObjectOrNull(output, data.getColorMapOklab(), int[][].class);
    }

    @Override
    public SimpleWorldMapView read(final Kryo kryo, final Input input, final Class<? extends SimpleWorldMapView> dataClass) {
        SimpleWorldMapView view = new SimpleWorldMapView((WorldMapGenerator) kryo.readClassAndObject(input));
        view.setBiomeMapper(BiomeMapper.SimpleBiomeMapper.recreateFromString(input.readString()));
        System.arraycopy(kryo.readObject(input, int[].class), 0, view.biomeColorTable, 0, view.biomeColorTable.length);
        System.arraycopy(kryo.readObject(input, int[].class), 0, view.biomeDarkColorTable, 0, view.biomeDarkColorTable.length);
        view.setColorMap(kryo.readObject(input, int[][].class));
        view.setColorMapOklab(kryo.readObject(input, int[][].class));
        return view;
    }
}
