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

package com.github.yellowstonegames.path.sg;

public class Path<V> extends Array<V> {

    public static final Path EMPTY_PATH = new Path(0, false);

    float length = 0;

    public Path(int size) {
        super(size, true);
    }

    public Path(int size, boolean resize) {
        super(size, resize);
    }

    /**
     * @return the length of this path, that is, the sum of the edge weights of all edges contained in the path.
     */
    public float getLength() {
        return length;
    }

    protected void setLength(float length) {
        this.length = length;
    }

    public V getFirst() {
        if (isEmpty()) throw new IllegalStateException("Path has no vertices.");
        return get(0);
    }

    public V getLast() {
        if (isEmpty()) throw new IllegalStateException("Path has no vertices.");
        return get(size-1);
    }

}