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

package com.github.yellowstonegames.path.technique;

import com.github.yellowstonegames.grid.Radius;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A struct-like class that holds information about targeting rules for actions or other effects that reach from one
 * square into another one, with certain potential restrictions.
 *
 * @see AreaUtils AreaUtils and its verifyReach method may be useful with this.
 */
public class Reach {
    /**
     * The minimum distance in cells that this Reach can target.
     * Distance will be measured using the {@code metric} field.
     */
    public int minDistance;

    /**
     * The maximum distance in cells that this Reach can target.
     * Distance will be measured using the {@code metric} field.
     */
    public int maxDistance;
    /**
     * An AimLimit enum that may be used to determine limitations to targeting cells; defaults to FREE (no limits other
     * than those from distance), but can also be set to ORTHOGONAL (rook move in chess), DIAGONAL (bishop move in
     * chess), EIGHT_WAY (queen or king move in chess), or null (which usually is equivalent to FREE).
     */
    public @Nullable AimLimit limit;
    /**
     * Determines how distance will be measured. SQUARE or CUBE correspond to 8-way or Chebyshev movement, DIAMOND or
     * OCTAHEDRON correspond to 4-way or Manhattan movement, and CIRCLE or SPHERE correspond to Euclidean movement.
     */
    public Radius metric;

    /**
     * Constructs a Reach with all fields given default values; maxDistance is set to 1, minDistance is set to 0, limit
     * is set to AimLimit.FREE, and metric is set to Radius.SQUARE (8-way movement).
     */
    public Reach() {
        minDistance = 0;
        maxDistance = 1;
        limit = AimLimit.FREE;
        metric = Radius.SQUARE;
    }

    /**
     * Constructs a Reach with the specified maxDistance, to a minimum of 0. Other fields are given default values;
     * minDistance is set to 0, limit is set to AimLimit.FREE, and metric is set to Radius.SQUARE (8-way movement).
     * @param maxDistance the longest distance in cells this Reach can target; will be set to 0 if negative
     */
    public Reach(int maxDistance) {
        minDistance = 0;
        this.maxDistance = Math.max(maxDistance, 0);
        limit = AimLimit.FREE;
        metric = Radius.SQUARE;
    }

    /**
     * Constructs a Reach with the specified minDistance, to a minimum of 0, and maxDistance, to a minimum equal to
     * minDistance (after factoring in any change to meet the minimum of 0). Other fields are given default values;
     * limit is set to AimLimit.FREE, and metric is set to Radius.SQUARE (8-way movement).
     * @param minDistance the shortest distance in cells this Reach can target; will be set to 0 if negative
     * @param maxDistance the longest distance in cells this Reach can target; will be set to the final value of
     *                    minDistance if it is lower than the calculated minDistance
     */
    public Reach(int minDistance, int maxDistance) {
        this.minDistance = Math.max(minDistance, 0);
        this.maxDistance = Math.max(this.minDistance, maxDistance);
        limit = AimLimit.FREE;
        metric = Radius.SQUARE;
    }

    /**
     * Constructs a Reach with the specified minDistance, to a minimum of 0, maxDistance, to a minimum equal to
     * minDistance (after factoring in any change to meet the minimum of 0), and distance calculation metric as a Radius
     * enum. Other than that, limit is set to AimLimit.FREE.
     * @param minDistance the shortest distance in cells this Reach can target; will be set to 0 if negative
     * @param maxDistance the longest distance in cells this Reach can target; will be set to the final value of
     *                    minDistance if it is lower than the calculated minDistance
     * @param metric a Radius enum that determines how distance will be calculated
     */
    public Reach(int minDistance, int maxDistance, Radius metric) {
        this.minDistance = Math.max(minDistance, 0);
        this.maxDistance = Math.max(this.minDistance, maxDistance);
        limit = AimLimit.FREE;
        this.metric = metric;
    }

    /**
     * Constructs a Reach with the specified minDistance, to a minimum of 0, maxDistance, to a minimum equal to
     * minDistance (after factoring in any change to meet the minimum of 0), and distance calculation metric as a Radius
     * enum. Other than that, limit is set to AimLimit.FREE.
     * @param minDistance the shortest distance in cells this Reach can target; will be set to 0 if negative
     * @param maxDistance the longest distance in cells this Reach can target; will be set to the final value of
     *                    minDistance if it is lower than the calculated minDistance
     * @param metric a Radius enum that determines how distance will be calculated
     * @param limit an AimLimit enum that can be used to limit targeting to specific angles, or not at all (if null or
     *              equal to AimLimit.FREE)
     */
    public Reach(int minDistance, int maxDistance, Radius metric, AimLimit limit) {
        this.minDistance = Math.max(minDistance, 0);
        this.maxDistance = Math.max(this.minDistance, maxDistance);
        this.limit = limit;
        this.metric = metric;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reach reach = (Reach) o;

        if (minDistance != reach.minDistance) return false;
        if (maxDistance != reach.maxDistance) return false;
        if (limit != reach.limit) return false;
        return metric == reach.metric;
    }

    @Override
    public int hashCode() {
        int result = minDistance;
        result = 31 * result + maxDistance;
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (metric != null ? metric.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Reach{" +
                "minDistance=" + minDistance +
                ", maxDistance=" + maxDistance +
                ", limit=" + limit +
                ", metric=" + metric +
                '}';
    }
}
