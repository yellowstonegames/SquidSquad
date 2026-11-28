/*
 * Copyright (c) 2026 See AUTHORS file.
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
 *
 */

package com.github.yellowstonegames.wrath.grid;

import com.github.yellowstonegames.grid.Combiners;
import com.github.yellowstonegames.grid.Combiners.Combiner;
import org.apache.fory.Fory;
import org.apache.fory.config.Config;
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;

/**
 * Fory {@link Serializer} for squidgrid {@link Combiner}s.
 */
public class CombinerSerializer extends Serializer<Combiner> {
    public CombinerSerializer(Fory fory) {
        super(fory.getConfig(), Combiner.class);
    }
    public CombinerSerializer(Config fory) {
        super(fory, Combiner.class);
    }

    @Override
    public void write(WriteContext fory, final Combiner data) {
        fory.writeString(data.getTag());
    }

    @Override
    public Combiner read(ReadContext fory) {
        return Combiners.get(fory.readString());
    }
}
