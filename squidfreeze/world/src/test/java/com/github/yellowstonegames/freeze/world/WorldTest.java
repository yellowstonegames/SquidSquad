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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.kryo.jdkgdxds.ObjectObjectMapSerializer;
import com.github.yellowstonegames.world.EllipticalWorldMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class WorldTest {
    @Test
    public void testEllipticalWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(EllipticalWorldMap.class, new EllipticalWorldMapSerializer());

        EllipticalWorldMap data = new EllipticalWorldMap(123, 200, 100);
		data.generate();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            EllipticalWorldMap data2 = kryo.readObject(input, EllipticalWorldMap.class);
//            Assert.assertEquals(data, data2);
        }
    }
}
