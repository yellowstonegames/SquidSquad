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
