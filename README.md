# SquidSquad
From all corners of the maybe-seven procedurally-generated seas, arise, O Mighty SQUAD Of SQUID, and hark unto me!

# What?
SquidSquad is the successor to [SquidLib](https://github.com/yellowstonegames/SquidLib), and can be considered an
overhaul but not a total rewrite. Like SquidLib, it provides tools for all sorts of procedural generation, and is
particularly focused on the needs of roguelike games. It is a group of loosely-linked modules, where you only need to
depend on the modules you need. All the modules depend on `squidcore`, which always depends on
[jdkgdxds](https://github.com/tommyettinger/jdkgdxds) for data structures (which only depends on the frequently-used,
tiny JSR305 annotations library) and [regexodus](https://github.com/tommyettinger/RegExodus) for cross-platform regular
expressions with an expanded API. Some modules (`squidglyph` and `squidsmooth`) depend on [libGDX](https://libgdx.com/),
which is recommended for use with SquidSquad but not always required. That means if you don't use `squidglyph` or
`squidsmooth`, you can use SquidSquad in purely-server-side code, or in tests, or otherwise outside the application
lifecycle libGDX expects.

# Which?
There are currently quite a few modules here; they depend on each other when necessary, so pulling in one module as a
dependency will usually pull in a few others. The full list is:
 - squidcore
   - Needed by all other modules, this provides core functionality used everywhere else, like utilities for handling
     arrays and Strings, conversions between numbers and text, trigonometric approximations, dice and probability
     tables, code to hash arrays and other sequences, and compression for Strings and byte arrays. It also importantly
     contains the code to describe colors, even if it can't display them.
 - squidgrid
   - Needed by many of the other modules, this provides tools for handling 2D positions on grids, such as the vital
     `Coord` and `Region` classes, but also `FOV` (Field of Vision), either `BresenhamLine` or `OrthoLine` (for line of
     sight), `Radiance` and `LightingManager` (for light sources), and `LineTools` (for getting box drawing characters
     to represent walls, which can be useful in graphical games as well as text-based ones). It also provides the
     `Noise` class, which is a large and highly-configurable way of producing continuous noise, such as Perlin noise.
 - squidtext
   - This only uses `squidcore`, and has various tools for procedurally-generating text. This text could be readable, as
    `Thesaurus` produces, or could be complete gibberish, as `Language` produces. `Translator` offers a middle ground,
     for text that seems to be unintelligible but can be translated in bits and pieces back to English.
 - squidglyph, squidpath, squidplace, squidsmooth, squidworld, and squidold
    - Uh... I'll fill these in later.

# Why?
Various issues cropped up repeatedly over the five-year development of SquidLib 3.0.0, such as the desire by users to be
able to only use part of the library instead of needing the monolithic squidlib-util JAR. Other issues were more
problematic during development, like how squidlib-util defined its own (elaborate) data structures based on
heavily-altered code from an older version of [fastutil](https://github.com/vigna/fastutil), and needed a lot of effort
to add new types of those data structures. All of SquidLib depended and still depends on Java 7; now with virtually all
targets permitting at least some of Java 8 or even Java 11, there's not much reason to reach back 10 years to July 2011,
when Java 7 came out.

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