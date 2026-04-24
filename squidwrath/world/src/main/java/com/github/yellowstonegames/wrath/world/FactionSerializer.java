/*
 * Copyright (c) 2020-2025 See AUTHORS file.
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

package com.github.yellowstonegames.wrath.world;

import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.world.PoliticalMapper.Faction;
import org.apache.fory.Fory;
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;

/**
 * ALlows {@link com.github.yellowstonegames.world.PoliticalMapper.Faction} to be serialized.
 * Needs nothing else to be registered.
 */
public class FactionSerializer extends Serializer<Faction> {
    public FactionSerializer(Fory fory) {
        super(fory.getConfig(), Faction.class);
    }

    @Override
    public void write(WriteContext fory, Faction data) {
        fory.writeString(data.language == null ? null : data.language.stringSerialize());
        fory.writeString(data.name == null ? null : data.name);
        fory.writeString(data.shortName == null ? null : data.shortName);
        fory.writeRef(data.preferredBiomes == null ? null : data.preferredBiomes.toArray(new String[0]));
        fory.writeRef(data.blockedBiomes == null ? null : data.blockedBiomes.toArray(new String[0]));
        fory.writeRef(data.preferredHeight == null ? null : data.preferredHeight);
        fory.writeRef(data.preferredHeat == null ? null : data.preferredHeat);
        fory.writeRef(data.preferredMoisture == null ? null : data.preferredMoisture);
    }

    @Override
    public Faction read(ReadContext fory) {
        return new Faction(
                Language.stringDeserialize(fory.readString()),
                fory.readString(),
                fory.readString(),
                (String[])fory.readRef(),
                (String[])fory.readRef(),
                (int[])fory.readRef(),
                (int[])fory.readRef(),
                (int[])fory.readRef()
                );
    }
}
