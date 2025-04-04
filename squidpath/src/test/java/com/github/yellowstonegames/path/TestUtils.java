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
package com.github.yellowstonegames.path;

import com.github.yellowstonegames.grid.Coord;

class TestUtils {

//    static class Coord {
//
//        float x, y;
//
//        Coord(float x, float y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        float dst (Coord v) {
//            final float x_d = v.x - x;
//            final float y_d = v.y - y;
//            return (float) Math.sqrt(x_d * x_d + y_d * y_d);
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            Coord vector2 = (Coord) o;
//            return Float.compare(vector2.x, x) == 0 &&
//                    Float.compare(vector2.y, y) == 0;
//        }
//
//        @Override
//        public int hashCode() {
//            return (int) (BitConversion.floatToIntBits(x) * 0xC13FA9A902A6328FL
//                                + BitConversion.floatToIntBits(y) * 0x91E10DA5C79E7B1DL >>> 32);
//        }
//
//        @Override
//        public String toString() {
//            return "(" +x +", " + y +')';
//        }
//    }

    static Graph<Coord> makeGridGraph(Graph<Coord> graph, int n) {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Coord v = Coord.get(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i<n-1) {
                    Coord v1 = Coord.get(i, j), v2 = Coord.get(i+1,j);
                    graph.addEdge(v1, v2, v1.distance(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.distance(v2));
                }
                if (j<n-1) {
                    Coord v1 = Coord.get(i, j), v2 = Coord.get(i,j+1);
                    graph.addEdge(v1, v2, v1.distance(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.distance(v2));
                }
            }
        }

        return graph;
    }
}
