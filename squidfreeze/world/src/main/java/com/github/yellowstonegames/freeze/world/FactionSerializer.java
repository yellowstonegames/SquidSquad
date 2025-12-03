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
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.world.PoliticalMapper.Faction;

/**
 * Serializer for {@link Faction}; doesn't need anything else registered.
 */
public class FactionSerializer extends Serializer<Faction> {
    public FactionSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Faction data) {
        output.writeString(data.language == null ? null : data.language.stringSerialize());
        output.writeString(data.name == null ? null : data.name);
        output.writeString(data.shortName == null ? null : data.shortName);
        kryo.writeObjectOrNull(output, data.preferredBiomes == null ? null : data.preferredBiomes.toArray(new String[0]), String[].class);
        kryo.writeObjectOrNull(output, data.blockedBiomes == null ? null : data.blockedBiomes.toArray(new String[0]), String[].class);
        kryo.writeObjectOrNull(output, data.preferredHeight, int[].class);
        kryo.writeObjectOrNull(output, data.preferredHeat, int[].class);
        kryo.writeObjectOrNull(output, data.preferredMoisture, int[].class);
    }

    @Override
    public Faction read(final Kryo kryo, final Input input, final Class<? extends Faction> dataClass) {
        return new Faction(Language.stringDeserialize(input.readString()), input.readString(), input.readString(),
                kryo.readObjectOrNull(input, String[].class),
                kryo.readObjectOrNull(input, String[].class),
                kryo.readObjectOrNull(input, int[].class),
                kryo.readObjectOrNull(input, int[].class),
                kryo.readObjectOrNull(input, int[].class)
                );
    }
}
