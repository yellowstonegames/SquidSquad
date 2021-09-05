package com.github.yellowstonegames.core;

/**
 * Allows access to an int identifier for an object, which is often but not necessarily unique.
 * You can generate identifiers however you wish, but a simple counter is probably easiest and fastest, plus it is
 * guaranteed to produce all possible identifiers without duplicates before cycling.
 */
@FunctionalInterface
public interface IIdentified {
    /**
     * Gets the identifier for this object, as an int.
     * @return the int identifier for this
     */
    int getIdentifier();
}
