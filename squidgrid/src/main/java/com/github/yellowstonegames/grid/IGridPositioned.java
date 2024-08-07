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

package com.github.yellowstonegames.grid;

/**
 * Allows getting and setting the position of an object, as a Coord.
 */
public interface IGridPositioned {
    /**
     * Gets the current position of this, as a Coord.
     * @return the Coord position of this object; may technically be null, but this should not usually be allowed
     */
    Coord getCoordPosition();

    /**
     * Sets the current position of this to the given {@code position}.
     * @param position the new position for this object; may technically be null, but this should not usually be allowed
     */
    void setCoordPosition(Coord position);
}
