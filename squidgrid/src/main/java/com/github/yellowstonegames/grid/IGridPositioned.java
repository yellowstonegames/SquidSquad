package com.github.yellowstonegames.grid;

/**
 * Allows access to the position of an object, as a Coord.
 */
@FunctionalInterface
public interface IGridPositioned {
    /**
     * Gets the current position of this, as a Coord.
     * @return the Coord position of this object; may technically be null, but this should not usually be allowed
     */
    Coord getCoordPosition();
}
