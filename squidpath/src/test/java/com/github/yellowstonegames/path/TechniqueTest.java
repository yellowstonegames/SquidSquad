package com.github.yellowstonegames.path;

import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordFloatOrderedMap;
import com.github.yellowstonegames.grid.Radius;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.path.technique.ConeAOE;
import com.github.yellowstonegames.path.technique.Technique;
import org.junit.Assert;
import org.junit.Test;

public class TechniqueTest {
    @Test
    public void testCone() {
        Region floors = new Region(16, 16).allOn().removeEdges(), edit = floors.copy();
        char[][] bareDungeon = floors.toChars('.', '#');

        ConeAOE cone = new ConeAOE(Coord.get(0, 0), 9, 0, 60, Radius.CIRCLE);
        cone.setMinRange(1);
        cone.setMaxRange(9);
        cone.setMetric(Radius.SQUARE);

        Technique redCone = new Technique("Burning Breath", cone);
        redCone.setMap(bareDungeon);

        Coord user = Coord.get(6, 10),
                west  = user.translate(-1, 0),
                east  = user.translate(1, 0),
                south = user.translate(0, 1),
                north = user.translate(0, -1);
        Assert.assertTrue(redCone.canTarget(user, west ));
        Assert.assertTrue(redCone.canTarget(user, east ));
        Assert.assertTrue(redCone.canTarget(user, south));
        Assert.assertTrue(redCone.canTarget(user, north));

        CoordFloatOrderedMap applied;
        applied = redCone.apply(user, west);
        Assert.assertTrue(applied.getOrDefault(west, 0f) > 0.05f);
        edit.remake(floors).removeAll(applied.order());
        System.out.println(edit + "\n\n");

        applied = redCone.apply(user, east);
        Assert.assertTrue(applied.getOrDefault(east, 0f) > 0.05f);
        edit.remake(floors).removeAll(applied.order());
        System.out.println(edit + "\n\n");

        applied = redCone.apply(user, south);
        Assert.assertTrue(applied.getOrDefault(south, 0f) > 0.05f);
        edit.remake(floors).removeAll(applied.order());
        System.out.println(edit + "\n\n");

        applied = redCone.apply(user, north);
        Assert.assertTrue(applied.getOrDefault(north, 0f) > 0.05f);
        edit.remake(floors).removeAll(applied.order());
        System.out.println(edit + "\n\n");
    }
}
