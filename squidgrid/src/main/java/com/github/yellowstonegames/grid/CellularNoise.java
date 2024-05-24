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

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An INoise implementation that divides space up into cells, and has configurable ways to get values from cells.
 */
public class CellularNoise implements INoise {

    public enum NoiseType {
        CELL_VALUE, NOISE_LOOKUP,
        DISTANCE, DISTANCE_2,
        DISTANCE_2_ADD, DISTANCE_2_SUB, DISTANCE_2_MUL, DISTANCE_2_DIV, DISTANCE_VALUE;

        public static final NoiseType[] ALL = values();

    }
    public static final CellularNoise instance = new CellularNoise();

    public long seed = 0xBEEFD1CEL;

    public NoiseType noiseType = NoiseType.DISTANCE;

    public CellularNoise() {
    }

    public CellularNoise(long seed) {
        this.seed = seed;
    }

    public CellularNoise(long seed, NoiseType noiseType) {
        this.seed = seed;
        this.noiseType = noiseType;
    }

    public CellularNoise(long seed, int noiseTypeIndex) {
        this.seed = seed;
        setNoiseType(noiseTypeIndex);
    }

    @Override
    public String getTag() {
        return "CelN";
    }

    @Override
    public CellularNoise copy() {
        return new CellularNoise(seed, noiseType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellularNoise that = (CellularNoise) o;

        return seed == that.seed && noiseType == that.noiseType;
    }

    @Override
    public int hashCode() {
        return (int)(seed ^ seed >>> 32) + noiseType.ordinal();
    }

    @Override
    public String toString() {
        return "CellularNoise{" +
               "seed=" + seed +
               ", noiseType=" + noiseType +
               '}';
    }

    @Override
    public int getMinDimension() {
        return 1;
    }

    @Override
    public int getMaxDimension() {
        return 4;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    /**
     * Gets 1D noise with using this generator's {@link #getSeed() seed}.
     * Delegates to {@link LineWobble#bicubicWobble(int, float)}.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoise(float x) {
        return LineWobble.bicubicWobble(seed, x);
    }

    @Override
    public float getNoise(float x, float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    public NoiseType getNoiseType() {
        return noiseType;
    }

    public void setNoiseType(NoiseType noiseType) {
        this.noiseType = NoiseType.ALL[(noiseType.ordinal() % 9 + 9) % 9];
    }

    public void setNoiseType(int noiseType) {
        this.noiseType = NoiseType.ALL[(noiseType % 9 + 9) % 9];
    }

    public float basicCellular(float x, float y, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;
        float distance = 999999;
        int xc = 0, yc = 0;
        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                int hash = LongPointHash.hash256(xi, yi, seed) << 1;
                float vecX = xi - x + gradients[hash];
                float vecY = yi - y + gradients[hash+1];

                float newDistance = vecX * vecX + vecY * vecY;

                if (newDistance < distance) {
                    distance = newDistance;
                    xc = xi;
                    yc = yi;
                }
            }
        }
        switch (noiseType) {
            case CELL_VALUE:
                return LongPointHash.hashAll(xc, yc, seed) * 0x1.0p-63f;

            case NOISE_LOOKUP:
                return SimplexNoise.instance.getNoiseWithSeed(xc * 0.0625f, yc * 0.0625f, seed);

            case DISTANCE:
                return Math.min(Math.max(distance - 1, -1), 1);

            default:
                return 0f;
        }
    }

    public float mergingCellular(float x, float y, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);

        float sum = 0f;
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                int hash = (int)LongPointHash.hashAll(xi, yi, seed);
                int h = (hash & 255) << 1;
                float vecX = xi - x + gradients[h];
                float vecY = yi - y + gradients[h + 1];

                float distance = 1f - (vecX * vecX + vecY * vecY);

                if (distance > 0f) {
                    sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance * 27f;
                }
            }
        }
        return sum / (64f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    public float edgePairCellular(float x, float y, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);

        float distance = 999999;
        float distance2 = 999999;
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = LongPointHash.hash256(xi, yi, seed) << 1;
                        float vecX = xi - x + gradients[hash];
                        float vecY = yi - y + gradients[hash + 1];

                        float newDistance = vecX * vecX + vecY * vecY;

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }

        switch (noiseType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    public float basicCellular(float x, float y, float z, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);
        int zr = MathTools.round(z);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;
        float distance = 999999;
        int xc = 0, yc = 0, zc = 0;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {

                    int hash = LongPointHash.hash256(xi, yi, zi, seed) << 2;
                    float vecX = xi - x + gradients[hash];
                    float vecY = yi - y + gradients[hash + 1];
                    float vecZ = zi - z + gradients[hash + 2];

                    float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                    if (newDistance < distance) {
                        distance = newDistance;
                        xc = xi;
                        yc = yi;
                        zc = zi;
                    }
                }
            }
        }

        switch (noiseType) {
            case CELL_VALUE:
                return LongPointHash.hashAll(xc, yc, zc, seed) * 0x1p-63f;
            case NOISE_LOOKUP:
                return SimplexNoise.instance.getNoiseWithSeed(xc * 0.0625f, yc * 0.0625f, zc * 0.0625f, seed);
            case DISTANCE:
                return distance - 1;
            default:
                return 0f;
        }
    }
    public float mergingCellular(float x, float y, float z, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);
        int zr = MathTools.round(z);

        float sum = 0f;
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    int hash = (int)LongPointHash.hashAll(xi, yi, zi, seed);
                    int h = (hash & 255) << 2;
                    float vecX = xi - x + gradients[h];
                    float vecY = yi - y + gradients[h + 1];
                    float vecZ = zi - z + gradients[h + 2];

                    float distance = 1f - (vecX * vecX + vecY * vecY + vecZ * vecZ);

                    if (distance > 0f) {
                        sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance * 27f;
                    }
                }
            }
        }
        return sum / (64f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    public float edgePairCellular(float x, float y, float z, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);
        int zr = MathTools.round(z);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        float distance = 999999;
        float distance2 = 999999;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    int hash = LongPointHash.hash256(xi, yi, zi, seed) << 2;
                    float vecX = xi - x + gradients[hash];
                    float vecY = yi - y + gradients[hash + 1];
                    float vecZ = zi - z + gradients[hash + 2];

                    float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                    distance2 = Math.max(Math.min(distance2, newDistance), distance);
                    distance = Math.min(distance, newDistance);
                }
            }
        }

        switch (noiseType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    public float basicCellular(float x, float y, float z, float w, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);
        int zr = MathTools.round(z);
        int wr = MathTools.round(w);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_4D;
        float distance = 999999;
        int xc = 0, yc = 0, zc = 0, wc = 0;
        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    for (int wi = wr - 1; wi <= wr + 1; wi++) {
                        int hash = LongPointHash.hash256(xi, yi, zi, wi, seed) << 2;
                        float vecX = xi - x + gradients[hash];
                        float vecY = yi - y + gradients[hash + 1];
                        float vecZ = zi - z + gradients[hash + 2];
                        float vecW = wi - w + gradients[hash + 3];

                        float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ + vecW * vecW;

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                            zc = zi;
                            wc = wi;
                        }
                    }
                }
            }
        }
        switch (noiseType) {
            case CELL_VALUE:
                return LongPointHash.hashAll(xc, yc, zc, wc, seed) * 0x1p-63f;
            case NOISE_LOOKUP:
                return SimplexNoise.instance.getNoiseWithSeed(xc * 0.0625f, yc * 0.0625f, zc * 0.0625f, wc * 0.0625f, seed);
            case DISTANCE:
                return distance - 1;
            default:
                return 0f;
        }
    }
    public float mergingCellular(float x, float y, float z, float w, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);
        int zr = MathTools.round(z);
        int wr = MathTools.round(w);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_4D;
        float sum = 0f;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    for (int wi = wr - 1; wi <= wr + 1; wi++) {
                        int hash = (int)LongPointHash.hashAll(xi, yi, zi, wi, seed);
                        int h = (hash & 255) << 2;
                        float vecX = xi - x + gradients[h];
                        float vecY = yi - y + gradients[h + 1];
                        float vecZ = zi - z + gradients[h + 2];
                        float vecW = wi - w + gradients[h + 3];

                        float distance = 1f - (vecX * vecX + vecY * vecY + vecZ * vecZ + vecW * vecW);

                        if (distance > 0f) {
                            sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance * 27f;
                        }
                    }
                }
            }
        }
        return sum / (64f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    public float edgePairCellular(float x, float y, float z, float w, long seed) {
        int xr = MathTools.round(x);
        int yr = MathTools.round(y);
        int zr = MathTools.round(z);
        int wr = MathTools.round(w);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_4D;

        float distance = 999999;
        float distance2 = 999999;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    for (int wi = wr - 1; wi <= wr + 1; wi++) {
                        int hash = LongPointHash.hash256(xi, yi, zi, wi, seed) << 2;
                        float vecX = xi - x + gradients[hash];
                        float vecY = yi - y + gradients[hash + 1];
                        float vecZ = zi - z + gradients[hash + 2];
                        float vecW = wi - w + gradients[hash + 3];

                        float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ + vecW * vecW;

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
            }
        }

        switch (noiseType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    /**
     * Gets 1D noise with using a specific seed.
     * Delegates to {@link LineWobble#bicubicWobble(int, float)}.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, long seed) {
        return LineWobble.bicubicWobble(seed, x);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, seed);
            case DISTANCE_VALUE:
                return mergingCellular(x, y, seed);
            default:
                return edgePairCellular(x, y, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, z, seed);
            case DISTANCE_VALUE:
                return mergingCellular(x, y, z, seed);
            default:
                return edgePairCellular(x, y, z, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, z, w, seed);
            case DISTANCE_VALUE:
                return mergingCellular(x, y, z, w, seed);
            default:
                return edgePairCellular(x, y, z, w, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return 0f;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return 0f;
    }
    public String stringSerialize() {
        return "`" + seed + "~" + noiseType.ordinal() + '`';
    }

    public CellularNoise stringDeserialize(String data) {
        if (data == null || data.length() < 5)
            return this;
        int middle;
        seed = DigitTools.intFromDec(data, 1, middle = data.indexOf('~', 2));
        int index = DigitTools.intFromDec(data, middle + 1, data.indexOf('`', middle+1));
        setNoiseType(index);
        return this;
    }

    public static CellularNoise recreateFromString(String data) {
        if (data == null || data.length() < 5)
            return null;
        int middle;
        long seed = DigitTools.intFromDec(data, 1, middle = data.indexOf('`', 2));
        int index = DigitTools.intFromDec(data, middle + 1, data.indexOf('`', middle+1));
        return new CellularNoise(seed, index);
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(seed);
        out.writeInt(noiseType.ordinal());
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        setSeed(in.readLong());
        setNoiseType(in.readInt());
    }


}
