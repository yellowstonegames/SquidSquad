package com.github.yellowstonegames.place.tileset;

/**
 * The outermost class in the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Tileset {
    public Config config;
    public Tile[] h_tiles, v_tiles;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public Tileset() {
        config = new Config();
    }
}