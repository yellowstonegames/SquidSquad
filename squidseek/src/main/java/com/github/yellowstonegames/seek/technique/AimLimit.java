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

package com.github.yellowstonegames.seek.technique;

/**
 * Enum used for common targeting limitations (or lack thereof, in the case of AimLimit.FREE ). AimLimit.ORTHOGONAL will
 * limit single targets or the centers/aimed-at-cells of AOE effects to cells directly, north, south, east or west of
 * the user. AimLimit.DIAGONAL does the same but for northeast, southeast, southwest, or northwest. AimLimit.EIGHT_WAY
 * limits the same things, but is less restrictive, allowing all cells AimLimit.ORTHOGONAL does as well as all cells
 * AimLimit.DIAGONAL allows. AimLimit.FREE allows all cells within any range limit an ability may have.
 */
public enum AimLimit {
    FREE,
    ORTHOGONAL,
    DIAGONAL,
    EIGHT_WAY;

    /**
     * The cached result of {@link #values()}, so you can avoid repeatedly allocating {@code AimLimit[]} objects.
     * DO NOT MODIFY THIS ARRAY.
     */
    public static final AimLimit[] ALL = values();

}
