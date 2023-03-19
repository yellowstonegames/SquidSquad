# SquidSquad
From all corners of the maybe-seven procedurally-generated seas, arise, O Mighty SQUAD Of SQUID, and hark unto me!

# What?
SquidSquad is the successor to [SquidLib](https://github.com/yellowstonegames/SquidLib), and can be considered an
overhaul but not a total rewrite. Like SquidLib, it provides tools for all sorts of procedural generation, and is
particularly focused on the needs of roguelike games. It is a group of loosely-linked modules, where you only need to
depend on the modules you need. All the modules depend on `squidcore`, which always depends on
[jdkgdxds](https://github.com/tommyettinger/jdkgdxds) for data structures, the
[checker-qual](https://github.com/typetools/checker-framework) annotations library,
[digital](https://github.com/tommyettinger/digital) for various number and digit stuff,
[juniper](https://github.com/tommyettinger/juniper) for random number generation, and
[regexodus](https://github.com/tommyettinger/RegExodus) for cross-platform regular expressions with an expanded API.
Some modules (`squidglyph`, `squidsmooth`, and all the `squidstore` modules) depend on [libGDX](https://libgdx.com/),
which is recommended for use with SquidSquad but not always required. That means if you don't use `squidglyph`,
`squidsmooth`, or `squidstore`, you can use SquidSquad in purely-server-side code, in tests, or otherwise outside the
application lifecycle libGDX expects.

# Which?
There are currently quite a few modules here; they depend on each other when necessary, so pulling in one module as a
dependency will usually pull in a few others. The full list is:
 - squidcore
   - Needed by all other modules, this provides core functionality used everywhere else, like utilities for handling
     Strings, conversions between numbers and text, dice and probability tables, and compression for Strings and byte
     arrays. It also importantly contains the code to describe colors, even if it can't display them.
     - Much of the functionality that was in squidcore has been moved to the external `digital` library. 
 - squidgrid
   - Needed by many of the other modules, this provides tools for handling 2D positions on grids, such as the vital
     `Coord` and `Region` classes, but also `FOV` (Field of Vision), `BresenhamLine` and `OrthoLine` (for line of
     sight), `Radiance` and `LightingManager` (for light sources), and `LineTools` (for getting box drawing characters
     to represent walls, which can be useful in graphical games as well as text-based ones). It also provides the
     `Noise` class, which is a large and highly-configurable way of producing continuous noise, such as Perlin noise.
     On top of this, there are `Coord`-based collections, such as `CoordObjectMap`, based on the `jdkgdxds` collections
     but specialized for Coord keys.
 - squidtext
   - This only uses `squidcore`, and has various tools for procedurally-generating text. This text could be readable, as
    `Thesaurus` produces, or could be complete gibberish, as `Language` produces. `Translator` offers a middle ground,
     for text that seems to be unintelligible but can be translated in bits and pieces back to English. `Messaging`
     allows conjugating present-tense text to use singular and/or plural pronouns, and is as correct as its input is at
     the conjugation quality. `MarkovText` allows mixing text from a large original source to make gibberish that sounds
     somewhat like the original.
 - squidsmooth
   - This depends on `squidcore` and `squidgrid`. It provides smooth interpolation for various kinds of `Glider`s, all
     managed by a `Director` that can be paused, resumed, stopped, and restarted. Example `Glider`s are `VectorGlider`,
     which smoothly changes two float components (x and y), `IntColorGlider`, which interpolates between two different
     colors as time goes on, and `AngleGlider`, which is usually used to make rotations turn taking the shorter
     distance. You can chain multiple gliders in a sequence with `SequenceGlider`, and can make multiple Gliders run at
     the same time by merging them.
   - `squidsmooth` can be useful for graphical games, but is less useful for text-based ones.
 - squidglyph
   - This depends on `squidcore` and `squidgrid`. It provides text-based display (meant for classical
     roguelikes and games like them) using the external library `TextraTypist`. Right now it only provides `GlyphMap`,
     which makes it somewhat easier to handle a grid of text with smoothly-moving glyphs over it, `GlyphActor`, which
     is a scene2d `Actor` drawn with just one (potentially colorful or styled) glyph, and some classes for `Action`s,
     which can be applied to a `GlyphMap` or `GlyphActor`.
 - squidpath
   - Pathfinding, mostly using a modified version of [simple-graphs](https://github.com/earlygrey/simple-graphs). There
     is also `DijkstraMap` here, which is good for some types of pathfinding that simple-graphs' `A*` algorithm can't
     do as easily.
 - squidplace
   - Dungeon generation, mostly, with some code for other type of person-scale map generation as well. Most of the maps
     are produced as `char[][]` grids, and `DungeonTools` provides various utilities for handling such grids. The large
     `DungeonProcessor` class can be used to ensure only the connected areas of a map are preserved, and can place
     doors, water, grass, boulders, and so on, using an environment 2D array to know what can be placed where.
 - squidstore
   - Split up into a few submodules: `SquidStoreCore`, `SquidStoreGrid`, `SquidStoreOld`, and `SquidStoreText`, with
     each one containing the necessary registration code to save and load their corresponding module to JSON. This uses
     libGDX Json and its custom serializers.
 - squidworld
   - World map generation; this can be rather complex, but see the demos and tests in this project for ideas on how to
     use it.
 - squidold
   - Compatibility with older versions of SquidLib (and potentially SquidSquad). This tries to exactly replicate the
     results of some core classes from older SquidLib, such as random number generators and `CrossHash`, but support the
     newer APIs here.
 - squidfreeze
    - Like `squidstore`, but using [Kryo](https://github.com/EsotericSoftware/kryo) instead of libGDX Json.

# Why?
Various issues cropped up repeatedly over the five-year development of SquidLib 3.0.0, such as the desire by users to be
able to only use part of the library instead of needing the monolithic squidlib-util JAR. Other issues were more
problematic during development, like how squidlib-util defined its own (elaborate) data structures based on
heavily-altered code from an older version of [fastutil](https://github.com/vigna/fastutil), and needed a lot of effort
to add new types of those data structures. All of SquidLib depended and still (sort-of) depends on Java 7; now with
virtually all targets permitting at least some of Java 8 or even Java 11, there's not much reason to reach back 11 years
to July 2011, when Java 7 came out.

# How?

You'll probably want to see [the one standalone demo here](https://github.com/yellowstonegames/SquidLib-Demos/tree/master/SquidSquad/DawnlikeDemo);
it's in SquidLib-Demos and uses the wonderful DawnLike tileset by DragonDePlatino and DawnBringer. This demo is also
present as a test in `squidsmooth`, but the standalone version of it shows how you can use SquidSquad in a complete
libGDX application.

If you use ProGuard, you need to add this line to your `proguard.pro` file; it allows `squidcore` to function after
ProGuard does its optimizations.

```
-optimizations !code/simplification/string
```

# Get?

The dependency situation is complicated because everything depends on `squidcore`, and that depends on several other
libraries. It's easier on projects that don't target GWT; for non-web projects like that, you can probably just depend
on the SquidSquad module(s) you want, and the rest will be obtained by Gradle. For GWT... OK. Deep breaths. On a new
project that uses [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff), you should check the third-party
extensions `jdkgdxds`, `juniper`, and `regexodus`, plus any others you might want (`jdkgdxds-interop`, for instance, is
required by `squidstore`, and `textratypist` is required by `squidglyph`). For now, let's say we only want the dungeon
map generators in `squidplace`. This depends on `squidcore` and `squidgrid`. For all projects, your core module will
contain dependencies like:
```gradle
	api "com.badlogicgames.gdx:gdx:$gdxVersion"
	api "com.github.yellowstonegames.squidsquad:SquidPlace:$squidSquadVersion"
```

For projects that use GWT, your html module should contain... more:
```gradle
  implementation "com.github.tommyettinger:digital:$digitalVersion:sources"
  implementation "com.github.tommyettinger:funderby:$funderbyVersion:sources"
  implementation "com.github.tommyettinger:jdkgdxds:$jdkgdxdsVersion:sources"
  implementation "com.github.tommyettinger:juniper:$juniperVersion:sources"
  implementation "com.github.tommyettinger:regexodus:$regExodusVersion:sources"
  implementation "com.github.yellowstonegames.squidsquad:SquidCore:$squidSquadVersion:sources"
  implementation "com.github.yellowstonegames.squidsquad:SquidGrid:$squidSquadVersion:sources"
  implementation "com.github.yellowstonegames.squidsquad:SquidPlace:$squidSquadVersion:sources"
```

These fetch SquidSquad from JitPack, so you should use a recent commit from
[its JitPack page](https://jitpack.io/#yellowstonegames/squidsquad) for your `squidSquadVersion` property.

The other versions go up fairly often as things are fixed or improved, but they will be at least:

  - `digitalVersion`=0.2.0
  - `funderbyVersion`=0.0.2
  - `jdkgdxdsVersion`=1.2.1
  - `juniperVersion`=0.2.0
  - `regExodusVersion`=0.1.15
