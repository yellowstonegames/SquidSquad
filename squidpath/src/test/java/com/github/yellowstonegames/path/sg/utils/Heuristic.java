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
package com.github.yellowstonegames.path.sg.utils;

/**
 * A function which estimates the distance of a shortest path between two vertices.
 * <br>A heuristic h should be admissible, that is, for any two vertices x and y, h(x,y) &#8804; d(x,y), where d(x,y)
 * is the actual distance of a shortest path from x to y.
 */
public interface Heuristic<V> {

    /**
     *
     * @param u
     * @param v
     * @return an estimation of the distance from u to v.
     * This value should always be at most the actual distance of a shortest path from x to y.
     */
    float getEstimate(V u, V v);

}
