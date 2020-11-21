package com.github.yellowstonegames.place;

/**
 * Standardizes an interface for classes that generate 2D char arrays to be used as place maps.
 */
public interface PlaceGenerator {
    /**
     * Generates a dungeon or other place as a 2D char array. Any implementation may allow its own configuration and
     * customization of how dungeons are generated, but each must provide this as a sane default. Most implementations
     * should use the convention of '#' representing a blocking cell such as a wall and '.' representing a bare floor,
     * but beyond that, anything could be present in the char array.
     * @return a 2D char array representing some kind of map, probably using standard conventions for walls/floors
     */
    char[][] generate();

    /**
     * Gets the most recently-produced place as a 2D char array, usually produced by calling {@link #generate()} or
     * some similar method present in a specific implementation. This normally passes a direct reference and not a
     * copy, so you can normally modify the returned array to propagate changes back into this IPlaceGenerator.
     * @return the most recently-produced dungeon/place as a 2D char array
     */
    char[][] getPlaceGrid();
}
