/*
 * Copyright (c) 2023 See AUTHORS file.
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

package com.github.yellowstonegames.world;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.github.yellowstonegames.core.Interpolations;
import org.junit.Assert;
import org.junit.Test;

public class InterpolationsTest {
    @Test
    public void compareResults() throws ReflectionException {
        Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);

        // see how many fields are actually interpolations (for safety; other fields may be added in the future)
        int interpolationMembers = 0;
        for (int i = 0; i < interpolationFields.length; i++)
            if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()))
                interpolationMembers++;

        // get interpolation names
        String[] interpolationNames = new String[interpolationMembers];
        for (int i = 0; i < interpolationFields.length; i++)
            if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()))
                interpolationNames[i] = interpolationFields[i].getName();

        // compare results with Interpolations from here
        Interpolations.Interpolator current;
        for (int i = 0; i < interpolationNames.length; i++) {
            Assert.assertNotNull(current = Interpolations.get(interpolationNames[i]));
            for (int j = 0; j <= 16; j++) {
                Assert.assertEquals(
                        interpolationNames[i] + " on " + (j * 0.0625f),
//                if(!MathTools.isEqual(
                        current.apply(j * 0.0625f)
                        , ((Float) ClassReflection.getMethod(Interpolation.class, "apply", Float.TYPE)
                                .invoke(interpolationFields[i].get(Interpolation.class), j * 0.0625f)).floatValue()
                        , 2e-4);
//                {
//                    System.out.println("PROBLEM: " + interpolationNames[i] + " at " + (j * 0.0625f));
//                    System.out.println("  " + current.apply(j * 0.0625f) + " vs. " + ((Float) ClassReflection.getMethod(Interpolation.class, "apply", Float.TYPE)
//                            .invoke(interpolationFields[i].get(Interpolation.class), j * 0.0625f)).floatValue());
//                }
            }
        }

    }
}
