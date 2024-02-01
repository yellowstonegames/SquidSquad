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
package com.github.yellowstonegames.path.sg;

public class Errors {

    public static void throwNullVertexException() {
        throw new IllegalArgumentException("Vertices cannot be null");
    }

    public static void throwNullItemException() {
        throw new IllegalArgumentException("No item can be null");
    }

    public static void throwSameVertexException() {
        throw new IllegalArgumentException("Self loops are not allowed");
    }

    public static void throwVertexNotInGraphVertexException(boolean multiple) {
        if (multiple) throw new IllegalArgumentException("At least one vertex is not in the graph");
        else throw new IllegalArgumentException("Vertex is not in the graph");
    }

    public static void throwModificationException() {
        throw new UnsupportedOperationException("You cannot modify this Collection - use the Graph object.");
    }


}
