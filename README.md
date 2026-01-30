# SquidSquad
From all corners of the maybe-seven procedurally-generated seas, arise, O Mighty SQUAD Of SQUID, and hark unto me!

![Squid... Squad... Animated Logo](docs/logo.gif)

# tl;dr

Depend on the modules you need by adding dependencies to core/build.gradle . For example,

```
api 'com.squidpony:squidgrid:4.0.7' // important code for anything that has a position here
api 'com.squidpony:squidplace:4.0.7' // allows generating dungeons, caves, wilderness areas as char arrays
api 'com.squidpony:squidstorepath:4.0.7' // adds a dependency for pathfinding and also allows saving related types 
api 'com.squidpony:squidstoretext:4.0.7' // adds a dependency for gibberish generation/translation; allows saving types
```

On all platforms but GWT, this will download everything SquidSquad needs, including `'com.squidpony:squidcore:4.0.7'`,
which is a dependency of all other SquidSquad modules. The `squidstorepath` and `squidstoretext` dependencies pull in
`squidpath` and `squidtext`, as well as allowing saving their various types to JSON using [libGDX](https://libgdx.com/)
and its `Json` class. If you aren't using libGDX, there's other options that don't depend on it, though SquidSquad is
designed with libGDX in mind. That includes the limitations of libGDX; in order to target iOS via RoboVM, this library
doesn't use any APIs from Java 8, and doesn't use any language level higher than 8.

Every module of SquidSquad is an option in gdx-liftoff when making a new project. Just search for squidsquad in the list
of third-party extensions, and check the ones you want. This will also fetch any dependencies of what you select. 

If you want to target the browser, save yourself some hassle and just use TeaVM instead of GWT. TeaVM is also an option
in gdx-liftoff at startup, and it works with Java (better than GWT does, even), Kotlin, Scala, and so on, instead of
needing pure-Java source code that is specifically targeted at GWT.

# What?
SquidSquad is the successor to [SquidLib](https://github.com/yellowstonegames/SquidLib), and can be considered an
overhaul but not a total rewrite. Like SquidLib, it provides tools for all sorts of procedural generation, and is
particularly focused on the needs of roguelike games. It is a group of loosely-linked modules, where you only need to
depend on the modules you need. All the modules depend on `squidcore`, which always depends on
[jdkgdxds](https://github.com/tommyettinger/jdkgdxds) for data structures, the
[digital](https://github.com/tommyettinger/digital) for various number and digit stuff,
[juniper](https://github.com/tommyettinger/juniper) for random number generation, and
[regexodus](https://github.com/tommyettinger/RegExodus) for cross-platform regular expressions with an expanded API.
This is already quite a few dependencies,
but these mostly have roles that were moved out of squidlib-util in earlier versions.

The important `squidgrid` module has an extra dependency on [crux](https://github.com/tommyettinger/crux), which
mostly provides interfaces that other libraries can use without needing `squidgrid` (instead needing `crux`). Some
modules (`squidglyph`, `squidsmooth`, `squidpress`, and all the `squidstore` modules) depend on
[libGDX](https://libgdx.com/), which is recommended for use with SquidSquad but not always required. That means if you
don't use `squidglyph`, `squidsmooth`, `squidpress`, `squidseek`, or `squidstore`, you can use SquidSquad in
purely-server-side code, in tests, or otherwise outside the application lifecycle libGDX expects. `squidstore` modules
also depend on [jdkgdxds-interop](https://github.com/tommyettinger/jdkgdxds_interop) for extra JSON-related code. The
`squidfreeze` modules all depend on [Kryo](https://github.com/EsotericSoftware/kryo) and
[kryo-more](https://github.com/tommyettinger/kryo-more). The`squidwrath` modules all depend on
[Apache Fory](https://fory.apache.org) instead, and use [tantrum](https://github.com/tommyettinger/tantrum) to extend
serialization support. `squidpath` and `squidseek` are very similar, but `squidseek` uses
[Gand](https://github.com/tommyettinger/gand) as a dependency, while `squidpath` mostly has specialized copies of
similar code to Gand in its own module or already available in `squidgrid`. Currently, `squidpath` is recommended of
those two pathfinding modules.

The various dependencies are updated independently of SquidSquad. Some are going to be very familiar if you used
SquidLib before. `digital` is essentially an expansion on the NumberUtils, ArrayTools, and CrossHash classes from
squidlib-util, with some extra features. `jdkgdxds` acts like some of the frequently-used data structures in SquidLib,
such as `OrderedSet` and `IntIntOrderedMap`; these have direct parallels in `jdkgdxds`, but there's also `IntIntMap`,
`LongIntMap`, `LongIntOrderedMap`, `LongFloatOrderedMap`, and so on. Most of these implement the few interfaces they
can, but anything with primitive keys or values doesn't have many options for existing interfaces. `juniper` acts like
a substitute for `RNG` and related classes in squidlib-util; it doesn't have all the same random number generators, but
you can get most of the important/widely-used ones in `squidold`, which should be fairly backwards-compatible. You might
not want to, though; the generators in juniper tend to have passed really large amounts of testing (tens of terabytes of
a suite of tests, and sometimes over 10 petabytes of a single test), and are often extremely fast. Plus, they have more
features out-of-the-box, like easy serialization of potentially much-larger states to Strings; squidlib-util could only
handle 64-bit states for that. `regexodus` is also a dependency of `squidlib-util`; it hasn't changed. It provides
cross-platform regular expressions that work the same on GWT as on desktop, and adds a few features.

# Which?
There are currently quite a few modules here; they depend on each other when necessary, so pulling in one module as a
dependency will usually pull in a few others. The full list is:
 - squidcore
   - Needed by all other modules, this provides core functionality used everywhere else, like utilities for handling
     Strings, conversions between numbers and text, dice and probability tables, and compression for Strings and byte
     arrays. It also importantly contains the code to describe colors, even if it can't display them. All color code in
     SquidSquad is available in a default format, which uses the Oklab color space to blend smoothly, and also in the
     RGB format, which generally makes more sense to programmers because we use RGB colors so much. A game typically
     will choose one of these to use internally, and will use RGB colors when rendering. Some classes here
     aren't directly used by SquidSquad much, like `UniqueIdentifier`, but make sense to use in game code (in the case
     of `UniqueIdentifier`, it can replace `UUID` on Google Web Toolkit targets, where UUID isn't available).
     - Much of the functionality that was in squidcore has been moved to the external `digital` library. 
 - squidgrid
   - Needed by many of the other modules, this provides tools for handling 2D positions on grids, such as the vital
     `Coord` and `Region` classes (for 2D points and regions on a 2D grid), but also `FOV` (Field Of View),
     `BresenhamLine` and `OrthoLine` (for line of sight), `Radiance`, `LightSource`, and `LightingManager` (for handling
     lighting), and `LineTools` (for getting box drawing characters to represent walls, which can be useful in graphical
     games as well as text-based ones by looking up a sprite to draw for a given char). These are all grouped into
     one place in `VisionFramework`, which does a lot of the work that would otherwise be repeated in different
     codebases. `squidgrid` also provides the `Noise` class, which is a large and highly-configurable way of producing
     continuous noise, such as Perlin noise, plus many single-purpose noise classes that also implement `INoise`. You
     can use a `NoiseWrapper` to handle octaves, frequency, etc. for any `INoise`. On top of this, there are
     `Coord`-based collections, such as `CoordObjectMap`, based on the `jdkgdxds` collections but specialized for Coord
     keys. `Region` can also be considered a `Coord`-based collection.
     - This module depends on [crux](https://github.com/tommyettinger/crux), allowing other libraries to share the same
       point interfaces.
       - There are implementations of all the crux interfaces here, such as `Point2Float` and `Point4Int` that are
         mutable and meant to act like the mutable points in libGDX, such as `Vector2`. `Coord` is an immutable version
         of `Point2` from Crux, with a resizable pool of all `Coord`s that are likely to be used. 
 - squidtext
   - This only uses `squidcore`, and has various tools for procedurally-generating text. This text could be readable, as
    `Thesaurus` produces, or could be complete gibberish, as `Language` produces. `Translator` offers a middle ground,
     for text that seems to be unintelligible but can be translated in bits and pieces back to English. `Messaging`
     allows conjugating present-tense text to use singular and/or plural pronouns, and is as correct as its input is at
     the conjugation quality. `MarkovText` allows mixing text from a large original source to make gibberish that sounds
     somewhat like the original. `NameGenerator` and `StringDistance` work together to try to generate names that are
     reasonably like an input corpus, like common names in the USA or Norse mythology.
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
     roguelikes and games like them) using the external library `TextraTypist`. Right now it only provides `GlyphGrid`,
     which makes it somewhat easier to handle a grid of text with smoothly-moving glyphs over it, `GlyphActor`, which
     is a scene2d `Actor` drawn with just one (potentially colorful or styled) glyph, and some classes for `Action`s,
     which can be applied to a `GlyphGrid` or `GlyphActor`.
     - On platforms other than GWT, this module can load [REXPaint](https://www.gridsagegames.com/rexpaint/) .xp files. It
       can also save a `GlyphGrid` to a new .xp file. These use the small `XPIO` class.
 - squidpath
   - Pathfinding, mostly using a modified older version of [simple-graphs](https://github.com/earlygrey/simple-graphs). There
     is also `DijkstraMap` here, which is good for some types of pathfinding that simple-graphs' `A*` algorithm can't
     do as easily. If you're using `A*`, you might want to consider [Gand](https://github.com/tommyettinger/gand),
     which is based on the current simple-graphs release instead of an older one, has most relevant classes marked
     as `Json.Serializable` using libGDX `Json`, and also has path smoothing compatible with `squidgrid`'s `Coord`
     class. Gand also has a backport of `DijkstraMap`, called `GradientGrid`. This depends on `squidgrid`.
     - You may want to use `squidseek` with Gand instead; it provides an alternative `DijkstraMap` and `ZoneOfInfluence`
       but otherwise delegates to Gand.
 - squidseek
   - A trimmed-down version of the above `squidpath` that uses `Gand` for a lot more, including its parent class that
     handles most of `DijkstraMap` here. Other than a `ZoneOfInfluence` class and some work-in-progress code for
     techniques (which shouldn't currently be used...), that's all there is here.
     - This depends on `squidcore`, `squidgrid`, and [Gand](https://github.com/tommyettinger/gand). 
 - squidplace
   - Dungeon generation, mostly, with some code for other type of person-scale map generation as well. Most of the maps
     are produced as `char[][]` grids, and `DungeonTools` provides various utilities for handling such grids. The large
     `DungeonProcessor` class can be used to ensure only the connected areas of a map are preserved, and can place
     doors, water, grass, boulders, and so on, using an environment 2D array to know what can be placed where. A slight
     outlier here is the `Biome` class, which is mostly used by the `squidworld` module for world maps, but can be
     useful for categorizing generated places at the local level, too.
     - This depends on `squidgrid`.
 - squidpress
   - Input handling, wrapping libGDX input classes to match the features SquidLib offers already. This depends on
     libGDX. This supports key rebinding in `SquidInput`, and there's an existing vi-keys rebind in `keymaps/`.
     `SquidMouse` allows mouse input to be mapped to grid cells for games that want that.
 - squidworld
   - World map generation; this can be rather complex, but see the demos and tests in this project for ideas on how to
     use it. See [these previews](docs/worlds/index.html) for the types of map this can produce.
     - This depends on `squidcore`, `squidgrid`, and `squidplace`.
 - squidold
   - Compatibility with older versions of SquidLib (and potentially SquidSquad). This tries to exactly replicate the
     results of some core classes from older SquidLib, such as random number generators and `CrossHash`, but support the
     newer APIs here.
     - Note that the older classes have generally been replaced for good reasons... The newer random number generators
       in `juniper` and the hashing algorithm in `digital`'s `Hasher` class (especially "Bulk" methods there) tend to be
       much more robust, and are also often faster.
     - This depends on `squidcore`.
- squidstore
    - Split up into a few submodules: `squidstorecore`, `squidstoregrid`, `squidstoreold`, `squidstorepath`,
      `squidstoretext`, and `squidstoreworld`, with each one containing the necessary registration code to save and load
      their corresponding module to JSON. This uses libGDX Json and its custom serializers. This is the only module
      meant for serialization that is GWT-compatible, or browser-compatible via TeaVM for that matter. It makes heavy
      use of [jdkgdxds_interop](https://github.com/tommyettinger/jdkgdxds_interop).
 - squidfreeze
    - Like `squidstore`, but using [Kryo](https://github.com/EsotericSoftware/kryo) instead of libGDX Json. Kryo uses
      a binary format, rather than somewhat-human-readable JSON code, and can produce much smaller serialized data in
      most cases, while both serializing and deserializing more quickly than any JSON library I've tried. Kryo is an
      older, more mature format than Fory (see squidwrath, below), which may make it better-suited to saving data you
      want to keep in a stable format for a long time. It typically isn't as fast at serialization or deserialization
      as Fory, as a counterpoint.
    - Unlike most other modules here, `squidfreeze` is not GWT-compatible, because Kryo isn't either.
 - squidwrath
    - Like `squidstore` or `squidfreeze`, but using [Apache Fory](https://fory.apache.org) instead of libGDX Json. Fory
      uses a binary format, like Kryo but incompatible, and can produce generally smaller serialized data in
      most cases, while both serializing and deserializing even more quickly than Kryo.
    - Unlike `squidfreeze`, many classes can be serialized by `squidwrath` without needing a special serializer
      (the class still needs to be registered with Fory, just not with `registerSerializer()`). If a serializer isn't
      present in `squidwrath`, that usually means you don't need a serializer when registering it.
      - All random number generators in `juniper` and any classes in `squidcore` or `squidpath` that can be serialized
        don't need any serializer to be registered. That means `squidwrathcore` and `squidwrathpath` are empty other
        than their tests.
    - Unlike most other modules here, `squidwrath` is not GWT-compatible, because Fory isn't either.

# Why?

Various issues cropped up repeatedly over the five-year development of SquidLib 3.0.0, such as the desire by users to be
able to only use part of the library instead of needing the monolithic squidlib-util JAR. Other issues were more
problematic during development, like how squidlib-util defined its own (elaborate) data structures based on
heavily-altered code from an older version of [fastutil](https://github.com/vigna/fastutil), and needed a lot of effort
to add new types of those data structures. All of SquidLib depended and still (sort-of) depends on Java 7; now with
virtually all targets permitting at least some of Java 8 or even Java 11, there's not much reason to reach back 13 years
to July 2011, when Java 7 came out.

SquidSquad development started in 2020, not long after development started on
[jdkgdxds](https://github.com/tommyettinger/jdkgdxds). As jdkgdxds evolved, it spread out so its random number generator
code could be in a different project, [juniper](https://github.com/tommyettinger/juniper), and its core shared code
could be in [digital](https://github.com/tommyettinger/digital). SquidSquad evolved with this family of libraries,
making heavy use of jdkgdxds' primitive-backed and ordered data structures. Having so many options for ordered maps and
sets was very refreshing coming from SquidLib's development, where each of its primitive-backed data structures had to
be built specially from [FastUtil](https://github.com/vigna/fastutil) sources and adapted to the lack of FastUtil
interfaces and adapter classes (not to mention the lack of Java 8 code). As development slowed down on SquidSquad, it
essentially can be considered mature now, even though the 4.0.0 release is considered the first stable release of the
library. Any bugfixes should be posted quickly and increment all submodule versions, even if they were unchanged.

# How?

You'll probably want to see [the one standalone demo here](https://github.com/yellowstonegames/SquidLib-Demos/tree/master/SquidSquad/DawnSquad);
it's in SquidLib-Demos and uses the wonderful DawnLike tileset by DragonDePlatino and DawnBringer. This demo is also
present as a test in `squidsmooth`, but the standalone version of it shows how you can use SquidSquad in a complete
libGDX application.

# Get?

The dependency situation is complicated because everything depends on `squidcore`, and that depends on several other
libraries. It's easier on projects that don't target GWT; for non-web projects like that, you can probably just depend
on the SquidSquad module(s) you want, and the rest will be obtained by Gradle. Depending on this with Gradle can use a
released version such as the current `4.0.7`, which can be obtained from the main source for dependencies on the
JVM, Maven Central. You can also get a specific commit, typically a newer one, using JitPack. The Maven Central
dependencies [can be seen for each version here on Maven Central](https://search.maven.org/search?q=g:com.squidpony),
and look like `implementation 'com.squidpony:squidcore:4.0.7'`. Maven Central's search will still show (much) older
SquidLib versions as well, such as `squidlib`, `squidlib-util`, and `squidlib-extra`; these are not used at all here.
Some compatibility code is present for porting from SquidLib to SquidSquad in the SquidSquad module `squidold`.

As an alternative, [the JitPack page is here](https://jitpack.io/#yellowstonegames/squidsquad); go to the Commits tab, choose any commit
except for `-SNAPSHOT`, click "Get It", and wait to see if it built successfully. Maybe get yourself some of your
beverage of choice during this time. If it built successfully, "Get It" will be green; if it failed, it will have
changed to "Report" in red. You probably don't have to report a build failure; these often are caused by the build
timing out, rather than any glitch on JitPack's side. If you refresh the page (you might have to click "Get It" again,
though this time it won't take any time at all) and scroll down, all the dependencies will be in a drop-down for you
to select as you see fit. The first Gradle code section isn't needed here; even years-old gdx-setup and gdx-liftoff
projects can download from JitPack like they can from anywhere else, without extra configuration. Dependencies using
JitPack look like `implementation 'com.github.yellowstonegames.squidsquad:squidcore:0123456789'`, where `0123456789`
is a commit version (usually 10 hex digits). Older versions use PascalCase for the names of modules, such as `SquidCore`
instead of `squidcore`.

For GWT... OK. Deep breaths. First, consider if you want to use TeaVM instead, which is probably simpler because it
doesn't need `sources` dependencies. In any case, use [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff) to
create your project. Use Maven only if you are a wizard. Select SquidSquad dependencies here, and gdx-liftoff will take
care of their dependencies on and off GWT. Generate the project. Relax. If you need to add another dependency, from
SquidSquad or somewhere else, my usual recommendation is to generate an empty project with all dependencies you want
selected, then to compare the `gradle.properties` and all `build.gradle` files between your empty and original projects.
Copy over any changes you want, reload your Gradle project, and you're done. There probably won't be many changes, and
they will probably all be in the dependencies, but this ensures all the versions are up-to-date and necessary other
projects are present.

Liftoff fetches SquidSquad from Maven Central, and needs a fixed release for `squidSquadVersion`. Right now, the best
such release is `4.0.7`. You can always use a more recent build of SquidSquad, using JitPack to build a recent
commit. You should typically use a recent commit from [its JitPack page](https://jitpack.io/#yellowstonegames/squidsquad) for your `squidSquadVersion` property.
The group is different for JitPack builds of SquidSquad; change `com.squidpony` to
`com.github.yellowstonegames.squidsquad` when using JitPack. Note that the artifact IDs may have changed if you are
updating from before `4.0.0-beta1` to that release or later; now they are all lower-case to match conventions, so
`SquidGrid` is now `squidgrid`. It's referred to as `squidgrid` in other places, so this simplifies things.

JitPack is generally recommended over the Maven Central alpha or beta releases, because you can (and really
should) specify an exact commit to use on the Commits tab (click "Get It" on any commit except -SNAPSHOT; this will
provide useful info below once it... eventually... builds). I strongly discourage using JitPack's -SNAPSHOT versions,
because they can change without warning and don't tell you what commit you are actually using; use a commit instead!

The other versions go up fairly often as things are fixed or improved, but they will be at least:

  - `digitalVersion`=0.9.9
  - `jdkgdxdsVersion`=2.1.1
  - `juniperVersion`=0.9.0
  - `regExodusVersion`=0.1.21
  - `cruxVersion`=0.1.3
  - `textraTypistVersion`=2.2.11
  - `gdxVersion`=1.14.0

# Help!

Feel free to post an issue in this repo if you're having trouble with SquidSquad in particular. There are also issues
pages for the dependencies if you find a problem with one of those, but if the general problem is SquidSquad-related,
don't be shy about posting the issue here. It could be connected to several different parts! There's also a
[SquidLib and SquidSquad Discord server](https://discord.gg/EmxUBsS) for real-time help, or help with some usage of
SquidSquad that isn't exactly an issue. If you have issues with some libGDX part of a game using SquidSquad, you can
also ask on the [libGDX Discord server](https://libgdx.com/community/discord/), which is very active. The
[libGDX wiki](https://libgdx.com/wiki/) is also a vital resource.

Some common issues do have known simple fixes. Here are some:

If you use ProGuard on desktop or iOS platforms, you need to add a line to your `proguard.pro` file. This allows
`squidcore` and, if needed, `squidgrid` to function after ProGuard does its optimizations:

```
-optimizations !code/simplification/string
```

Note that this line will be added automatically to the Android R8 configuration,
which is subtly different from ProGuard configuration on any other platform.
If you only use ProGuard on Android (where it's really using R8), you shouldn't need
to change any `.pro` files manually to use SquidSquad. You may still need to make
changes to those files to keep things like scene2d.ui from libGDX.

There are some drawbacks to using TeaVM. TeaVM requires language level 11, and the iOS target using RoboVM can't
use language level 11, so you can't easily target TeaVM and RoboVM from the same application. You can potentially stick
to using language level 8 and only compile TeaVM with a JDK 11 or newer. You could use MOE (Multi-OS Engine) to target
iOS with language level 11, though, and could do that alongside TeaVM.


# License

[The Apache License 2.0](LICENSE).

# Thanks!

Most of the code here was by Tommy Ettinger, but not all of it! Various places copy (also Apache-licensed) code from
libGDX, and sometimes other Apache-licensed libraries. Some places use MIT-licensed code, and I've tried to keep the MIT
license header close to where the MIT-licensed code is. The `A*` pathfinding code uses code from 
[simple-graphs](https://github.com/earlygrey/simple-graphs), mostly because I haven't found any better `A*` code for
what we use it for. Some of the world map code uses an MIT-licensed `ProjectionTools` class made by Justin Kunimune.
There are probably other examples throughout here... Where public domain code was copied into here, I really have no
obligation to even credit the original author, but I try to anyway.

The old logo, SquidSquad.png, was AI-generated by [Daniele Conti](https://github.com/fourlastor). Thanks, but we're AI-free without that logo, so
it is gone now! In its place is a simple looping GIF of sinuously wriggling text saying "Squid.... Squad....", which is
made by [a test in squidglyph](squidglyph/src/test/java/com/github/yellowstonegames/glyph/AnimatedLogoGenerator.java).

The various fonts in `assets/` all have their license next to the font files. The Dawnlike files, in `assets/dawnlike`,
require attribution to DawnBringer and DragonDePlatino.

The credits for `assets/Game-Icons.png` are more complex; since the file is from TextraTypist, you should consult
[its list of contributors](https://github.com/tommyettinger/textratypist/blob/10f9ce48315e34289054bf03d50453c7650775d7/README.md#thanks)
and include the whole list (since the PNG includes the work of all those contributors) if you use Game-Icons. In
general, consulting TextraTypist's credits guide is a good idea if you use the assets here. Because we do use 
`Game-Icons.png` here, in some fashion, here are the credits for all the contributors to that image:

- Lorc, http://lorcblog.blogspot.com
- Delapouite, https://delapouite.com
- John Colburn, http://ninmunanmu.com
- Felbrigg, http://blackdogofdoom.blogspot.co.uk
- John Redman, http://www.uniquedicetowers.com
- Carl Olsen, https://twitter.com/unstoppableCarl
- Sbed, http://opengameart.org/content/95-game-icons
- PriorBlue
- Willdabeast, http://wjbstories.blogspot.com
- Viscious Speed, http://viscious-speed.deviantart.com
- Lord Berandas, http://berandas.deviantart.com
- Irongamer, http://ecesisllc.wix.com/home
- HeavenlyDog, http://www.gnomosygoblins.blogspot.com
- Lucas
- Faithtoken, http://fungustoken.deviantart.com
- Skoll
- Andy Meneely, http://www.se.rit.edu/~andy/
- Cathelineau
- Kier Heyl
- Aussiesim
- Sparker, http://citizenparker.com
- Zeromancer
- Rihlsul
- Quoting
- Guard13007, https://guard13007.com
- DarkZaitzev, http://darkzaitzev.deviantart.com
- SpencerDub
- GeneralAce135
- Zajkonur
- Catsu
- Starseeker
- Pepijn Poolman
- Pierre Leducq
- Caro Asercion

(Projects that use `Game-Icons.png` can copy the above list of contributors to comply with its license.)

