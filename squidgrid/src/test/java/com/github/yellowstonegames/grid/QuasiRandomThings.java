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

package com.github.yellowstonegames.grid;

import static com.github.yellowstonegames.grid.QuasiRandomTools.goldenLong;

public class QuasiRandomThings {

    public static void main(String[] args) {
        System.out.println("new float[][] {");
        for (int outer = 0; outer < goldenLong.length; outer++) {
            System.out.print("{ ");
            for (int inner = 0; inner < goldenLong[outer].length; inner++) {
                if(inner > 0 && (inner % 5) == 0)
                    System.out.print("\n  ");
                System.out.printf("%1.16ff, ", (goldenLong[outer][inner] >>> 1) * 0x1p-63);
            }
            System.out.println("},");
        }
        System.out.println("};");
    }
}
