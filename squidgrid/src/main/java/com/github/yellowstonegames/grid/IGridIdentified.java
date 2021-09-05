package com.github.yellowstonegames.grid;

import com.github.yellowstonegames.core.IIdentified;

/**
 * Combines IGridPositioned and IIdentified for objects that have a Coord position and an int identifier.
 */
public interface IGridIdentified extends IGridPositioned, IIdentified {

}
