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

package com.github.yellowstonegames.place.tileset;

/**
 * Part of the JSON that defines a tileset.
 */
public class Config {

	/* If this list of fields is modified, TilesetsGenerator should be modified too */

    public boolean is_corner;
    //public int num_color_0, num_color_1, num_color_2, num_color_3, num_color_4 = 0, num_color_5 = 0;
    public int[] num_colors;
    public int short_side_length;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public Config() {
        is_corner = true;
        num_colors = new int[] {1,1,1,1};
    }
}
