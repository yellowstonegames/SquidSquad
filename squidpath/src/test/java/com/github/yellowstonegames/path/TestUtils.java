/*
MIT License

Copyright (c) 2020 earlygrey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
//            return (int) (Float.floatToIntBits(x) * 0xC13FA9A902A6328FL
//                                + Float.floatToIntBits(y) * 0x91E10DA5C79E7B1DL >>> 32);
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
