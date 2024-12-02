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

package com.github.yellowstonegames.world;

import com.github.yellowstonegames.grid.IPointHash;

import static com.github.tommyettinger.digital.BitConversion.imul;

/**
 */
@SuppressWarnings("SuspiciousNameCombination")
public final class SlimPointHash extends IPointHash.IntImpl {
    public SlimPointHash() {
        super();
    }

    public SlimPointHash(int state) {
        super(state);
    }

    public int getState() {
        return state;
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return hashAll(x, y, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return hashAll(x, y, z, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return hashAll(x, y, z, w, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return hashAll(x, y, z, w, u, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return hashAll(x, y, z, w, u, v, state);
    }

    /*
s = (imul(0xD1B54A33, s ^ s >>> 16) ^ imul(0xABC98383, x) ^ imul(0x8CB92BA7, y));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
s = (imul(0xDB4F0B95, s ^ s >>> 16) ^ imul(0xBBE0563F, x) ^ imul(0xA0F2EC75, y) ^ imul(0x89E18289, z));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
s = (imul(0xE19B01A3, s ^ s >>> 16) ^ imul(0xC6D1D6C1, x) ^ imul(0xAF36D01B, y) ^ imul(0x9A694437, z) ^ imul(0x881403BD, w));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
s = (imul(0xE60E2B7B, s ^ s >>> 16) ^ imul(0xCEBD76DF, x) ^ imul(0xB9C9AA35, y) ^ imul(0xA6F5777F, z) ^ imul(0x9609C71B, w) ^ imul(0x86D516EB, u));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
s = (imul(0xE95E1DDD, s ^ s >>> 16) ^ imul(0xD4BC74EF, x) ^ imul(0xC1EDBC55, y) ^ imul(0xB0C8AC5D, z) ^ imul(0xA127A315, w) ^ imul(0x92E852C3, u) ^ imul(0x85EB75C3, v));
        s = imul(s ^ s >>> 16, 0x9E3779B9);

     */
    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y point with the given state s
     */
    public static int hashAll(int x, int y, int s) {
        s = (imul(0xD1B54A33, s ^ s >>> 16) ^ imul(0xABC98383, x) ^ imul(0x8CB92BA7, y));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s ^ s >>> 8 ^ s >>> 15 ^ s >>> 21;
    }
    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z point with the given state s
     */
    public static int hashAll(int x, int y, int z, int s) {
        s = (imul(0xDB4F0B95, s ^ s >>> 16) ^ imul(0xBBE0563F, x) ^ imul(0xA0F2EC75, y) ^ imul(0x89E18289, z));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s ^ s >>> 8 ^ s >>> 15 ^ s >>> 21;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z,w point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int s) {
        s = (imul(0xE19B01A3, s ^ s >>> 16) ^ imul(0xC6D1D6C1, x) ^ imul(0xAF36D01B, y) ^ imul(0x9A694437, z) ^ imul(0x881403BD, w));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s ^ s >>> 8 ^ s >>> 15 ^ s >>> 21;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 32-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int u, int s) {
        s = (imul(0xE60E2B7B, s ^ s >>> 16) ^ imul(0xCEBD76DF, x) ^ imul(0xB9C9AA35, y) ^ imul(0xA6F5777F, z) ^ imul(0x9609C71B, w) ^ imul(0x86D516EB, u));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s ^ s >>> 8 ^ s >>> 15 ^ s >>> 21;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
        s = (imul(0xE95E1DDD, s ^ s >>> 16) ^ imul(0xD4BC74EF, x) ^ imul(0xC1EDBC55, y) ^ imul(0xB0C8AC5D, z) ^ imul(0xA127A315, w) ^ imul(0x92E852C3, u) ^ imul(0x85EB75C3, v));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s ^ s >>> 8 ^ s >>> 15 ^ s >>> 21;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y point with the given state s
     */
    public static int hash256(int x, int y, int s) {
        s = (imul(0xD1B54A33, s ^ s >>> 16) ^ imul(0xABC98383, x) ^ imul(0x8CB92BA7, y));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 24;
    }
    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z point with the given state s
     */
    public static int hash256(int x, int y, int z, int s) {
        s = (imul(0xDB4F0B95, s ^ s >>> 16) ^ imul(0xBBE0563F, x) ^ imul(0xA0F2EC75, y) ^ imul(0x89E18289, z));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 24;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int s) {
        s = (imul(0xE19B01A3, s ^ s >>> 16) ^ imul(0xC6D1D6C1, x) ^ imul(0xAF36D01B, y) ^ imul(0x9A694437, z) ^ imul(0x881403BD, w));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 24;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 8-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int s) {
        s = (imul(0xE60E2B7B, s ^ s >>> 16) ^ imul(0xCEBD76DF, x) ^ imul(0xB9C9AA35, y) ^ imul(0xA6F5777F, z) ^ imul(0x9609C71B, w) ^ imul(0x86D516EB, u));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 24;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int v, int s) {
        s = (imul(0xE95E1DDD, s ^ s >>> 16) ^ imul(0xD4BC74EF, x) ^ imul(0xC1EDBC55, y) ^ imul(0xB0C8AC5D, z) ^ imul(0xA127A315, w) ^ imul(0x92E852C3, u) ^ imul(0x85EB75C3, v));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 24;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y point with the given state s
     */
    public static int hash64(int x, int y, int s) {
        s = (imul(0xD1B54A33, s ^ s >>> 16) ^ imul(0xABC98383, x) ^ imul(0x8CB92BA7, y));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 26;
    }
    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z point with the given state s
     */
    public static int hash64(int x, int y, int z, int s) {
        s = (imul(0xDB4F0B95, s ^ s >>> 16) ^ imul(0xBBE0563F, x) ^ imul(0xA0F2EC75, y) ^ imul(0x89E18289, z));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 26;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int s) {
        s = (imul(0xE19B01A3, s ^ s >>> 16) ^ imul(0xC6D1D6C1, x) ^ imul(0xAF36D01B, y) ^ imul(0x9A694437, z) ^ imul(0x881403BD, w));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 26;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 6-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int u, int s) {
        s = (imul(0xE60E2B7B, s ^ s >>> 16) ^ imul(0xCEBD76DF, x) ^ imul(0xB9C9AA35, y) ^ imul(0xA6F5777F, z) ^ imul(0x9609C71B, w) ^ imul(0x86D516EB, u));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 26;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int u, int v, int s) {
        s = (imul(0xE95E1DDD, s ^ s >>> 16) ^ imul(0xD4BC74EF, x) ^ imul(0xC1EDBC55, y) ^ imul(0xB0C8AC5D, z) ^ imul(0xA127A315, w) ^ imul(0x92E852C3, u) ^ imul(0x85EB75C3, v));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 26;
    }


    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y point with the given state s
     */
    public static int hash32(int x, int y, int s) {
        s = (imul(0xD1B54A33, s ^ s >>> 16) ^ imul(0xABC98383, x) ^ imul(0x8CB92BA7, y));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 27;
    }
    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z point with the given state s
     */
    public static int hash32(int x, int y, int z, int s) {
        s = (imul(0xDB4F0B95, s ^ s >>> 16) ^ imul(0xBBE0563F, x) ^ imul(0xA0F2EC75, y) ^ imul(0x89E18289, z));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 27;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int s) {
        s = (imul(0xE19B01A3, s ^ s >>> 16) ^ imul(0xC6D1D6C1, x) ^ imul(0xAF36D01B, y) ^ imul(0x9A694437, z) ^ imul(0x881403BD, w));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 27;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 5-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int u, int s) {
        s = (imul(0xE60E2B7B, s ^ s >>> 16) ^ imul(0xCEBD76DF, x) ^ imul(0xB9C9AA35, y) ^ imul(0xA6F5777F, z) ^ imul(0x9609C71B, w) ^ imul(0x86D516EB, u));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 27;
    }

    /**
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int u, int v, int s) {
        s = (imul(0xE95E1DDD, s ^ s >>> 16) ^ imul(0xD4BC74EF, x) ^ imul(0xC1EDBC55, y) ^ imul(0xB0C8AC5D, z) ^ imul(0xA127A315, w) ^ imul(0x92E852C3, u) ^ imul(0x85EB75C3, v));
        s = imul(s ^ s >>> 16, 0x9E3779B9);
        return s >>> 27;
    }
}
