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
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.world.EllipticalWorldMap;

import java.nio.charset.StandardCharsets;

/**
 * TOTALLY BROKEN, DO NOT USE.
 * What appears to be a Kryo bug prevents this from working. It is entirely likely that
 * if you were to run this as if it was working code, you would immediately get a crash.
 * That would require editing this file. Don't do it. Use Fury and SquidWrath instead.
 */
@Beta
public class EllipticalWorldMapSerializer extends Serializer<EllipticalWorldMap> {
    public EllipticalWorldMapSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final EllipticalWorldMap data) {
        String ser = data.stringSerialize();
        byte[] byteStr = ser.getBytes(StandardCharsets.UTF_8);
        output.writeInt(byteStr.length);
        System.out.println(byteStr.length);
        output.writeBytes(byteStr); // MARKED
        //with MARKED line commented OUT, prints:  743360
        //with MARKED line NOT commented, prints:  743360
    }

    @Override
    public EllipticalWorldMap read(final Kryo kryo, final Input input, final Class<? extends EllipticalWorldMap> dataClass) {
        int len = input.readInt();
        System.out.println(len);
        //with MARKED line commented OUT, prints:  743360
        //with MARKED line NOT commented, prints:  540034627

//        String str = new String(input.readBytes(len), StandardCharsets.UTF_8);
//        return EllipticalWorldMap.recreateFromString(str);
        return new EllipticalWorldMap();
    }
}
