/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.text;

import com.github.tommyettinger.ds.CaseInsensitiveOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.LaserRandom;
import com.github.tommyettinger.random.TricycleRandom;
import com.github.yellowstonegames.core.GapShuffler;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.StringTools;
import regexodus.*;

import java.util.*;

import static com.github.tommyettinger.ds.ObjectList.with;

/**
 * A text processing class that can swap out occurrences of special keywords and replace them with randomly-selected
 * synonyms. For example, this can take {@code "By the light`adj` fire`noun` in my heart, I will vanquish thee!"}, which
 * has the keywords {@code light`adj`} and {@code fire`noun`}, and could produce
 * {@code "By the shimmering inferno in my heart, I will vanquish thee!"} on one run, and
 * {@code "By the gleaming conflagration in my heart, I will vanquish thee!"} on another. This can also generate some
 * plant names and basic descriptions of potions, as well as make some text sound like {@link #ORK}. 
 * <br>
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class Thesaurus {
    protected static final Pattern wordMatch = Pattern.compile("([\\pL`]+|@)"),
            similarFinder = Pattern.compile(".*?\\b(\\w\\w\\w\\w).*?{\\@1}.*$", "ui");
    public CaseInsensitiveOrderedMap<GapShuffler<String>> mappings;
    public ObjectList<Language.Alteration> alterations = new ObjectList<>(4);
    public EnhancedRandom rng;
    protected GapShuffler<String> plantTermShuffler, fruitTermShuffler, nutTermShuffler, flowerTermShuffler,
            potionTermShuffler, vegetableTermShuffler;
    public Language defaultLanguage = Language.SIMPLISH;
    public transient ObjectList<Language> randomLanguages = new ObjectList<>(2);
    public transient String latestGenerated = "Nationia";
    /**
     * Constructs a new Thesaurus with an unseeded RNG used to shuffle word order.
     */
    public Thesaurus()
    {
        mappings = new CaseInsensitiveOrderedMap<>(256);
        rng = new LaserRandom();
        addKnownCategories();
    }

    /**
     * Constructs a new Thesaurus, seeding its internal LaserRandom (used to shuffle word order) with the next long from
     * the given EnhancedRandom.
     * @param rng an EnhancedRandom that will only be used to get one long (for seeding this class' internal LaserRandom)
     */
    public Thesaurus(EnhancedRandom rng)
    {
        this(rng.nextLong());
    }

    /**
     * Constructs a new Thesaurus, seeding its internal LaserRandom (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a long for seeding this class' LaserRandom
     */
    public Thesaurus(long shuffleSeed)
    {
        mappings = new CaseInsensitiveOrderedMap<>(256);
        this.rng = new LaserRandom(shuffleSeed);
        addKnownCategories();
    }

    /**
     * Constructs a new Thesaurus, seeding its LaserRandom (used to shuffle word order) with shuffleSeedA and shuffleSeedB.
     * @param shuffleSeedA a long for seeding this class' LaserRandom
     * @param shuffleSeedB a long for seeding this class' LaserRandom; if even, will be made odd by adding 1
     */
    public Thesaurus(long shuffleSeedA, long shuffleSeedB)
    {
        mappings = new CaseInsensitiveOrderedMap<>(256);
        this.rng = new LaserRandom(shuffleSeedA, shuffleSeedB);
        addKnownCategories();
    }


    /**
     * Constructs a new Thesaurus, seeding its LaserRandom (used to shuffle word order) with two different hashes of
     * shuffleSeed (produced by {@link Hasher#sallos} and {@link Hasher#sallos_}).
     * @param shuffleSeed a String for seeding this class' RNG
     */
    public Thesaurus(String shuffleSeed)
    {
        this(Hasher.sallos.hash64(shuffleSeed), Hasher.sallos_.hash64(shuffleSeed));
    }

    /**
     * Changes the sequences for all groups of synonyms this can produce, effectively turning this Thesaurus into a
     * different version that knows all the same synonyms and categories but will produce different results.
     * This version of refresh() is meant for cases where you either only have one long of seed, or you know the
     * generator this uses has one state.
     * @param state any long; will be used with {@link EnhancedRandom#setSeed(long)} on the generator shared by this and its mappings
     */
    public void refresh(long state)
    {
        this.rng.setSeed(state);
        final int sz = mappings.size();
        for (int i = 0; i < sz; i++) {
            mappings.getAt(i).setRNG(rng, true);
        }
    }

    /**
     * Changes the sequences for all groups of synonyms this can produce, effectively turning this Thesaurus into a
     * different version that knows all the same synonyms and categories but will produce different results.
     * This version of refresh() is meant for cases where you know the generator this uses has two states, 128 bits.
     * @param stateA any long; the first part of a two-state EnhancedRandom like a {@link LaserRandom}
     * @param stateB any long; the second part of a two-state EnhancedRandom like a {@link LaserRandom}
     */
    public void refresh(long stateA, long stateB)
    {
        this.rng.setSeed(stateA);
        this.rng.setSelectedState(0, stateA);
        this.rng.setSelectedState(1, stateB);
        final int sz = mappings.size();
        for (int i = 0; i < sz; i++) {
            mappings.getAt(i).setRNG(rng, true);
        }
    }

    /**
     * Changes the sequences for all groups of synonyms this can produce, effectively turning this Thesaurus into a
     * different version that knows all the same synonyms and categories but will produce different results.
     * This version of refresh() is meant for cases where you know the generator this uses has three states, 192 bits.
     * @param stateA any long; the first part of a three-state EnhancedRandom like a {@link TricycleRandom}
     * @param stateB any long; the second part of a three-state EnhancedRandom like a {@link TricycleRandom}
     * @param stateC any long; the third part of a three-state EnhancedRandom like a {@link TricycleRandom}
     */
    public void refresh(long stateA, long stateB, long stateC)
    {
        this.rng.setSeed(stateA);
        this.rng.setSelectedState(0, stateA);
        this.rng.setSelectedState(1, stateB);
        this.rng.setSelectedState(2, stateC);
        final int sz = mappings.size();
        for (int i = 0; i < sz; i++) {
            mappings.getAt(i).setRNG(rng, true);
        }
    }

    public Thesaurus addReplacement(CharSequence before, String after)
    {
        mappings.put(before, new GapShuffler<>(after));
        return this;
    }

    /**
     * Allows this Thesaurus to replace a specific keyword, typically containing multiple backtick characters
     * ({@code `}) so it can't be confused with a "real word," with one of the words in synonyms (chosen in shuffled
     * order). The backtick is the only punctuation character that this class' word matcher considers part of a word,
     * both for this reason and because it is rarely used in English text.
     * @param keyword a word (typically containing backticks, {@code `}) that will be replaced by a word from synonyms
     * @param synonyms a Collection of lower-case Strings with similar meaning and the same part of speech
     * @return this for chaining
     */
    public Thesaurus addCategory(CharSequence keyword, Collection<String> synonyms)
    {
        if(synonyms.isEmpty())
            return this;
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng, true);
        mappings.put(keyword, shuffler);
        return this;
    }

    /**
     * Prints out all of the categories this knows and then all of the terms this knows, optionally using HTML list
     * formatting. The list formatting could be useful for Javadocs, as it is used here. Sorts and makes unique the
     * words in each category, but does not sort categories (to preserve the similarity of some categories at close
     * points in the order). This is probably only going to be used internally.
     * @param listFormat if true, this will format the output as an HTML list, otherwise it will be as plain lines
     */
    public void printCategories(boolean listFormat){
        String start = listFormat ? "    <li>" : "";
        String end = listFormat ? "</li>" : "";
        TreeSet<String> synonyms = new TreeSet<>();
        for (int i = 0; i < categories.size(); i++) {
            if(!categories.keyAt(i).toString().contains("`term`")) {
                synonyms.clear();
                synonyms.addAll(categories.getAt(i));
                System.out.println(start + categories.keyAt(i) + " : " + StringTools.join(", ", synonyms) + end);
            }
        }
        System.out.println();
        for (int i = 0; i < categories.size(); i++) {
            if(categories.keyAt(i).toString().contains("`term`")) {
                synonyms.clear();
                synonyms.addAll(categories.getAt(i));
                System.out.println(start + categories.keyAt(i) + " : " + StringTools.join(", ", synonyms) + end);
            }
        }
    }

    /**
     * Adds several pre-made categories to this Thesaurus' known categories, but won't cause it to try to replace normal
     * words with synonyms (only categories, which contain backticks in the name). The keywords this currently knows,
     * and the words it will replace those keywords with, are:
     * <br>
     * <ul>
     *     <li>calm`adj` : calm, harmonious, peaceful, placid, pleasant, serene, tranquil</li>
     *     <li>calm`noun` : calm, harmony, kindness, peace, serenity, tranquility</li>
     *     <li>org`noun` : association, brotherhood, fellowship, foundation, fraternity, group, guild, order, partnership</li>
     *     <li>org`nouns` : associations, brotherhoods, fellowships, foundations, fraternities, groups, guilds, orders, partnerships</li>
     *     <li>empire`adj` : ascendant, dynastic, emir's, hegemonic, imperial, king's, lordly, monarchic, prince's, regal, royal, sultan's</li>
     *     <li>empire`noun` : ascendancy, commonwealth, dominion, dynasty, emirate, empire, hegemony, imperium, kingdom, monarchy, sultanate, triumvirate</li>
     *     <li>empire`nouns` : ascendancies, commonwealths, dominions, dynasties, emirates, empires, hegemonies, imperia, kingdoms, monarchies, sultanates, triumvirates</li>
     *     <li>emperor`noun` : emir, emperor, king, lord, pharaoh, ruler, sultan</li>
     *     <li>emperor`nouns` : emirs, emperors, kings, lords, pharaohs, rulers, sultans</li>
     *     <li>empress`noun` : emira, empress, lady, pharaoh, queen, ruler, sultana</li>
     *     <li>empress`nouns` : emiras, empresses, ladies, pharaohs, queens, rulers, sultanas</li>
     *     <li>union`adj` : allied, associated, confederated, congressional, democratic, federated, independent, people's, unified, united</li>
     *     <li>union`noun` : alliance, coalition, confederacy, confederation, congress, faction, federation, league, republic, union</li>
     *     <li>union`nouns` : alliances, coalitions, confederacies, confederations, congresses, factions, federations, leagues, republics, unions</li>
     *     <li>militia`noun` : fighters, front, irregulars, liberators, militants, militia, rebellion, resistance, warriors</li>
     *     <li>militia`nouns` : fighters, fronts, irregulars, liberators, militants, militias, rebellions, resistances, warriors</li>
     *     <li>gang`noun` : cartel, crew, gang, mafia, mob, posse, syndicate</li>
     *     <li>gang`nouns` : cartels, crews, gangs, mafias, mobs, posses, syndicates</li>
     *     <li>duke`noun` : baron, duke, earl, fief, lord, shogun</li>
     *     <li>duke`nouns` : barons, dukes, earls, fiefs, lords, shoguns</li>
     *     <li>duchy`noun` : barony, duchy, earldom, fiefdom, lordship, shogunate</li>
     *     <li>duchy`nouns` : baronies, duchies, earldoms, fiefdoms, lordships, shogunates</li>
     *     <li>magical`adj` : arcane, enchanted, ensorcelled, magical, mystical, sorcerous</li>
     *     <li>holy`adj` : auspicious, blessed, divine, godly, holy, prophetic, sacred, virtuous</li>
     *     <li>priest`noun` : bishop, cardinal, chaplain, cleric, preacher, priest</li>
     *     <li>priest`nouns` : bishops, cardinals, chaplains, clergy, preachers, priests</li>
     *     <li>unholy`adj` : accursed, bewitched, macabre, occult, profane, unholy, vile</li>
     *     <li>witch`noun` : cultist, defiler, necromancer, occultist, warlock, witch</li>
     *     <li>witch`nouns` : cultists, defilers, necromancers, occultists, warlocks, witches</li>
     *     <li>forest`adj` : bountiful, fertile, lush, natural, primal, verdant</li>
     *     <li>forest`noun` : copse, forest, glen, greenery, grove, jungle, nature, woodland</li>
     *     <li>shaman`noun` : animist, druid, shaman, warden</li>
     *     <li>shaman`nouns` : animists, druids, shamans, wardens</li>
     *     <li>fancy`adj` : glorious, grand, great, magnanimous, magnificent, majestic, powerful</li>
     *     <li>evil`adj` : abhorrent, cruel, debased, evil, heinous, horrible, malevolent, nefarious, scurrilous, terrible, vile, wicked</li>
     *     <li>villain`noun` : blasphemer, evildoer, killer, knave, monster, murderer, villain</li>
     *     <li>villain`nouns` : blasphemers, evildoers, killers, knaves, monsters, murderers, villains</li>
     *     <li>monster`noun` : abomination, beast, creature, demon, devil, fiend, ghoul, monster</li>
     *     <li>monsters`nouns` : abominations, beasts, creatures, demons, devils, fiends, ghouls, monsters</li>
     *     <li>good`adj` : compassionate, flawless, good, kind, moral, perfect, pure, righteous</li>
     *     <li>lethal`adj` : bloodstained, cutthroat, deadly, fatal, lethal, murderous, poisonous, silent, stalking, venomous</li>
     *     <li>lethal`noun` : assassin, blood, killer, murder, ninja, poison, razor, silence, slayer, snake, tiger, venom</li>
     *     <li>blade`noun` : axe, blade, cutlass, flail, glaive, halberd, hammer, hatchet, katana, knife, lance, mace, maul, nunchaku, saber, scimitar, scythe, sickle, spear, stiletto, sword, trident, whip</li>
     *     <li>bow`noun` : atlatl, bolas, bow, crossbow, dagger, javelin, longbow, net, shortbow, shuriken, sling</li>
     *     <li>weapon`noun` : atlatl, axe, blade, bolas, bow, crossbow, cutlass, dagger, flail, glaive, halberd, hammer, hatchet, javelin, katana, knife, lance, longbow, mace, maul, net, nunchaku, saber, scimitar, scythe, shortbow, shuriken, sickle, sling, spear, stiletto, sword, trident, whip</li>
     *     <li>musket`noun` : arquebus, blunderbuss, cannon, flintlock, matchlock, musket, wheellock</li>
     *     <li>grenade`noun` : bomb, explosive, flamethrower, grenade, missile, rocket, warhead</li>
     *     <li>rifle`noun` : firearm, handgun, longarm, pistol, rifle, shotgun</li>
     *     <li>blade`nouns` : axes, blades, cutlasses, flails, glaives, halberds, hammers, hatchets, katana, knives, lances, maces, mauls, nunchaku, sabers, scimitars, scythes, sickles, spears, stilettos, swords, tridents, whips</li>
     *     <li>bow`nouns` : atlatls, bolases, bows, crossbows, daggers, javelins, longbows, nets, shortbows, shuriken, slings</li>
     *     <li>weapon`nouns` : atlatls, axes, blades, bolases, bows, crossbows, cutlasses, daggers, flails, glaives, halberds, hammers, hatchets, javelins, katana, knives, lances, longbows, maces, mauls, nets, nunchaku, sabers, scimitars, scythes, shortbows, shuriken, sickles, slings, spears, stilettos, swords, tridents, whips</li>
     *     <li>musket`nouns` : arquebusses, blunderbusses, cannons, flintlocks, matchlocks, muskets, wheellocks</li>
     *     <li>grenade`nouns` : bombs, explosives, flamethrowers, grenades, missiles, rockets, warheads</li>
     *     <li>rifle`nouns` : firearms, handguns, longarms, pistols, rifles, shotguns</li>
     *     <li>scifi`adj` : genetic, gravitational, laser, nanoscale, phase, photonic, plasma, quantum, tachyonic, warp</li>
     *     <li>tech`adj` : crypto, cyber, digital, electronic, hacker, mechanical, servo, techno, turbo</li>
     *     <li>sole`adj` : final, last, singular, sole, total, true, ultimate</li>
     *     <li>light`adj` : bright, gleaming, glowing, luminous, lunar, radiant, shimmering, solar, stellar</li>
     *     <li>light`noun` : dawn, gleam, glow, light, moon, radiance, shimmer, star, sun, torch</li>
     *     <li>light`nouns` : glimmers, lights, moons, stars, suns, torches</li>
     *     <li>shadow`noun` : blackness, darkness, gloom, murk, shadow, twilight</li>
     *     <li>shadow`nouns` : blackness, darkness, gloom, murk, shadows, twilight</li>
     *     <li>fire`noun` : blaze, conflagration, fire, flame, inferno, pyre</li>
     *     <li>fire`nouns` : blazes, conflagrations, fires, flames, infernos, pyres</li>
     *     <li>ice`noun` : blizzard, chill, cold, frost, ice, snow</li>
     *     <li>ice`nouns` : blizzards, chills, cold, frosts, ice, snow</li>
     *     <li>lightning`noun` : lightning, shock, spark, storm, thunder, thunderbolt</li>
     *     <li>lightning`nouns` : lightning, shocks, sparks, storms, thunder, thunderbolts</li>
     *     <li>ground`noun` : clay, dirt, earth, loam, mud, peat, sand, soil</li>
     *     <li>lake`noun` : bog, fen, glade, lake, pond, puddle, sea, swamp</li>
     *     <li>leaf`noun` : bark, blossom, branch, bud, cress, flower, leaf, root, sap, seed, shoot, stalk, stem, thorn, twig, vine, wood, wort</li>
     *     <li>fruit`noun` : apple, banana, berry, cherry, citron, date, fig, fruit, grape, juniper, kumquat, lemon, lime, loquat, mango, melon, papaya, peach, pear, pineapple, plum, quince</li>
     *     <li>nut`noun` : almond, bean, cashew, chestnut, coconut, hazelnut, lentil, nut, pea, peanut, pecan, pistachio, walnut</li>
     *     <li>vegetable`noun` : artichoke, asparagus, avocado, barley, beet, broccoli, cabbage, carrot, cauliflower, celery, corn, eggplant, fennel, garlic, kale, leek, lettuce, mushroom, onion, parsley, potato, pumpkin, radish, rhubarb, rice, rutabaga, spinach, taro, tomato, truffle, wheat, yam, zucchini</li>
     *     <li>flower`noun` : amaryllis, camellia, chrysanthemum, daisy, dandelion, flower, gardenia, hibiscus, jasmine, lantana, lilac, lily, lotus, mallow, oleander, orchid, peony, petunia, phlox, plumeria, rose, tulip, yarrow</li>
     *     <li>tree`noun` : alder, beech, birch, cactus, cedar, elm, eucalyptus, ficus, hazel, juniper, larch, magnolia, mangrove, maple, oak, palm, pine, tree, willow</li>
     *     <li>bush`noun` : aloe, boxwood, bramble, brier, brush, bush, dogwood, fern, flax, hawthorn, hedge, hemp, holly, honeysuckle, kudzu, manzanita, mesquite, milkweed, nettle, privet, ragweed, ragwort, shrub, silkweed, sorrel, tansy, tea, thicket, thistle, tobacco</li>
     *     <li>flavor`noun` : acid, grease, salt, smoke, soap, spice, sugar</li>
     *     <li>flavor`adj` : bitter, salty, savory, smoky, sour, spicy, sweet</li>
     *     <li>color`adj` : black, blue, brown, gray, green, orange, pink, red, violet, white, yellow</li>
     *     <li>shape`adj` : arched, crusty, delicate, diminutive, drooping, fibrous, flat, fragile, giant, hardy, hollow, large, long, massive, miniature, ovate, pored, ridged, small, spiny, spongy, stiff, stubby, sturdy, tattered, thorny, toothy, tufted, yielding</li>
     *     <li>sensory`adj` : aromatic, feathery, fragrant, furry, fuzzy, glossy, hairy, pungent, rough, rustling, scaly, shaggy, smooth, soft, spotted, striped, weeping</li>
     *     <li>liquid`noun` : brew, broth, elixir, fluid, liquid, potion, serum, tonic</li>
     *     <li>liquid`adj` : bubbling, congealing, effervescent, milky, murky, slimy, sloshing, swirling, thick</li>
     *     <li>bottle`noun` : bottle, canister, flagon, flask, jug, phial, vial</li>
     *     <li>bottle`adj` : brown glass, clear glass, curvaceous glass, dull pewter, fluted crystal, green glass, rough-cut glass, sharp-edged tin, shining silver, smoky glass, tarnished silver</li>
     *     <li>calabash`noun` : calabash, hollow gourd, milk carton, waterskin, wineskin</li>
     *     <li>smart`adj` : aware, brilliant, clever, cunning, genius, mindful, smart, wise</li>
     *     <li>smart`noun` : acumen, awareness, cunning, genius, knowledge, mindfulness, smarts, wisdom</li>
     *     <li>stupid`adj` : careless, complacent, dull, dumb, foolish, idiotic, moronic, reckless, sloppy, stupid</li>
     *     <li>stupid`noun` : carelessness, complacency, foolishness, idiocy, recklessness, sloppiness, stupidity</li>
     *     <li>bandit`noun` : bandit, brigand, highwayman, pirate, raider, rogue, thief</li>
     *     <li>bandit`nouns` : bandits, brigands, highwaymen, pirates, raiders, rogues, thieves</li>
     *     <li>soldier`noun` : combatant, fighter, mercenary, soldier, trooper, warrior</li>
     *     <li>soldier`nouns` : combatants, fighters, mercenaries, soldiers, troops, warriors</li>
     *     <li>guard`noun` : defender, guard, guardian, knight, paladin, protector, sentinel, shield, templar, warden, watchman</li>
     *     <li>guard`nouns` : defenders, guardians, guards, knights, paladins, protectors, sentinels, shields, templars, wardens, watchmen</li>
     *     <li>hunter`noun` : hunter, poacher, stalker, tracker, trapper, warden</li>
     *     <li>explorer`noun` : explorer, nomad, pathfinder, questant, seeker, wanderer</li>
     *     <li>hunter`nouns` : hunters, poachers, stalkers, trackers, trappers, wardens</li>
     *     <li>explorer`nouns` : explorers, nomads, pathfinders, questants, seekers, wanderers</li>
     *     <li>rage`noun` : anger, frenzy, fury, rage, vengeance, wrath</li>
     *     <li>ominous`adj` : baleful, fateful, foreboding, ominous, portentous</li>
     *     <li>many`adj` : countless, infinite, manifold, many, myriad, thousandfold, unlimited</li>
     *     <li>impossible`adj` : abominable, forbidden, impossible, incomprehensible, indescribable, ineffable, unearthly, unspeakable</li>
     *     <li>gaze`noun` : eye, gaze, observation, purveyance, stare, watch</li>
     *     <li>pain`noun` : agony, excruciation, misery, pain, torture</li>
     *     <li>god`noun` : deity, father, god, king, lord, lordship, ruler</li>
     *     <li>goddess`noun` : deity, goddess, lady, ladyship, mother, queen, ruler</li>
     *     <li>hero`noun` : champion, crusader, hero, knight, savior</li>
     *     <li>heroes`nouns` : champions, crusaders, heroes, knights, saviors</li>
     *     <li>heroine`noun` : champion, crusader, heroine, knight, maiden, savior</li>
     *     <li>heroines`nouns` : champions, crusaders, heroines, knights, maidens, saviors</li>
     *     <li>popular`adj` : adored, beloved, revered, worshipped</li>
     *     <li>unpopular`adj` : despised, hated, loathed, reviled</li>
     *     <li>glyph`noun` : glyph, mark, seal, sigil, sign, symbol</li>
     *     <li>glyph`nouns` : glyphs, marks, seals, sigils, signs, symbols</li>
     *     <li>power`noun` : authority, dominance, force, potency, power, strength</li>
     *     <li>power`adj` : authoritative, dominant, forceful, potent, powerful, strong</li>
     * </ul>
     * There are also terms, which typically produce multiple words procedurally and may use {@link #defaultLanguage}.
     * See {@link #makePlantName()}, {@link #makeFruitName()}, {@link #makeNutName()}, {@link #makeFlowerName()},
     * {@link #makeVegetableName()}, and {@link #makePotionDescription()} for more info and examples.
     * <ul>
     *     <li>plant`term` : @'s bush`noun`, @'s bush`noun`-bush`noun`, @'s bush`noun`-tree`noun`, @'s color`adj`\tleaf`noun`, @'s color`adj` flower`noun`, @'s color`adj` tree`noun`, @'s flower`noun`, @'s flower`noun`-bush`noun`, @'s ground`noun`\tleaf`noun`, @'s sensory`adj`-leaf`noun`, @'s shape`adj` bush`noun`, @'s shape`adj` flower`noun`, @'s tree`noun`, @'s tree`noun`-bush`noun`, @'s tree`noun`-tree`noun`, bush`noun` of @, bush`noun`-bush`noun`, bush`noun`-bush`noun` of @, bush`noun`-tree`noun`, bush`noun`-tree`noun` of @, color`adj`\tleaf`noun` bush`noun`, color`adj`\tleaf`noun` tree`noun`, color`adj` bush`noun`, color`adj` flower`noun`, color`adj` flower`noun` of @, color`adj` fruit`noun` tree`noun`, color`adj` nut`noun` tree`noun`, color`adj` tree`noun`, color`adj`-leaf`noun` bush`noun`, color`adj`-leaf`noun` flower`noun`, color`adj`-leaf`noun` tree`noun`, color`adj`-vegetable`noun` tree`noun`, flavor`adj` fruit`noun` tree`noun`, flavor`adj` nut`noun` tree`noun`, flavor`noun`\tleaf`noun` tree`noun`, flower`noun` of @, flower`noun`-bush`noun`, flower`noun`-bush`noun` of @, fruit`noun` bush`noun`, fruit`noun` tree`noun`, ground`noun`\tflower`noun`, ground`noun`\tleaf`noun`, ground`noun`\tleaf`noun` of @, ground`noun`\tvegetable`noun`, leaf`noun` of @, nut`noun` bush`noun`, nut`noun` tree`noun`, sensory`adj` bush`noun`, sensory`adj` bush`noun`-bush`noun`, sensory`adj` bush`noun`-tree`noun`, sensory`adj` flower`noun` of @, sensory`adj` flower`noun`-bush`noun`, sensory`adj` flower`noun`-flower`noun`, sensory`adj` tree`noun`, sensory`adj` tree`noun` of @, sensory`adj` tree`noun`-bush`noun`, sensory`adj` tree`noun`-tree`noun`, sensory`adj`-leaf`noun` bush`noun`, sensory`adj`-leaf`noun` flower`noun`, sensory`adj`-leaf`noun` of @, sensory`adj`-leaf`noun` tree`noun`, shape`adj` bush`noun` of @, shape`adj` flower`noun`, shape`adj`-fruit`noun` tree`noun`, shape`adj`-leaf`noun` flower`noun`, shape`adj`-leaf`noun` tree`noun`, shape`adj`-vegetable`noun` tree`noun`, tree`noun`-bush`noun`, tree`noun`-bush`noun` of @, tree`noun`-tree`noun`, tree`noun`-tree`noun` of @, vegetable`noun` bush`noun`, vegetable`noun` tree`noun`, vegetable`noun`-leaf`noun` tree`noun`</li>
     *     <li>fruit`term` : @'s color`adj` fruit`noun`, @'s flavor`adj` fruit`noun`, @'s fruit`noun`, color`adj` fruit`noun`, color`adj` fruit`noun` of @, color`adj` fruit`noun`-fruit`noun`, flavor`adj` fruit`noun`, flavor`adj` fruit`noun` of @, flavor`adj` fruit`noun`-fruit`noun`, fruit`noun` of @, shape`adj` fruit`noun`, shape`adj` fruit`noun`-fruit`noun`</li>
     *     <li>nut`term` : @'s color`adj` nut`noun`, @'s flavor`adj` nut`noun`, @'s nut`noun`, color`adj` nut`noun`, color`adj` nut`noun` of @, flavor`adj` nut`noun`, nut`noun` of @, sensory`adj` nut`noun`</li>
     *     <li>vegetable`term` : @'s color`adj` vegetable`noun`, @'s flavor`adj` vegetable`noun`, @'s vegetable`noun`, @'s vegetable`noun`-vegetable`noun`, color`adj` sensory`adj` vegetable`noun`, color`adj` vegetable`noun`, color`adj` vegetable`noun` of @, color`adj` vegetable`noun`-vegetable`noun`, flavor`adj` vegetable`noun`, flavor`adj` vegetable`noun`-vegetable`noun`, sensory`adj` shape`adj` vegetable`noun`, sensory`adj` vegetable`noun`, sensory`adj` vegetable`noun`-vegetable`noun`, shape`adj` color`adj` vegetable`noun`, shape`adj` vegetable`noun`, shape`adj` vegetable`noun`-vegetable`noun`, vegetable`noun` of @</li>
     *     <li>flower`term` : @'s color`adj` flower`noun`, @'s flower`noun`, @'s shape`adj` flower`noun`, color`adj` flower`noun`, color`adj` flower`noun` of @, color`adj`-leaf`noun` flower`noun`, flower`noun` of @, ground`noun`\tflower`noun`, sensory`adj` flower`noun` of @, sensory`adj` flower`noun`-flower`noun`, sensory`adj`-leaf`noun` flower`noun`, shape`adj` flower`noun`, shape`adj`-leaf`noun` flower`noun`</li>
     *     <li>potion`term` : a bottle`adj` bottle`noun` containing a few drops of a color`adj` liquid`noun`, a bottle`adj` bottle`noun` filled with a color`adj` liquid`noun`, a bottle`adj` bottle`noun` filled with a liquid`adj` color`adj` liquid`noun`, a bottle`adj` bottle`noun` half-filled with a liquid`adj` color`adj` liquid`noun`, a calabash`noun` filled with a color`adj` liquid`noun`</li>
     * </ul>
     * Capitalizing the first letter in the keyword where it appears in text you call process() on will capitalize the
     * first letter of the produced fake word. Capitalizing the second letter will capitalize the whole produced fake
     * word. This applies only per-instance of each keyword; it won't change the internally-stored list of words.
     * @return this for chaining
     */
    public Thesaurus addKnownCategories()
    {
        for(Map.Entry<CharSequence, ObjectList<String>> kv : categories.entrySet())
        {
            addCategory(kv.getKey(), kv.getValue());
        }
        plantTermShuffler =     mappings.get("plant`term`");
        fruitTermShuffler =     mappings.get("fruit`term`");
        nutTermShuffler =       mappings.get("nut`term`");
        vegetableTermShuffler = mappings.get("vegetable`term`");
        flowerTermShuffler =    mappings.get("flower`term`");
        potionTermShuffler =    mappings.get("potion`term`");
        return this;
    }
//
//    /**
//     * Given an archive String saved by {@link #archiveCategories()} (probably from another version of SquidLib), this
//     * makes the Thesaurus class act like it did in that archive, assuming the {@link #rng} is seeded the same. This
//     * modifies the {@link #mappings} field and the {@link #categories}, {@link #adjective}, {@link #noun}, and
//     * {@link #nouns} static fields, so it can affect other Thesaurus objects produced later (it won't change
//     * previously-made ones, probably).
//     *
//     * If you didn't have an archive of the categories from some version of SquidLib, you can download one of the small
//     * files from <a href="https://github.com/yellowstonegames/SquidLib/tree/master/archives">the 'archives' folder of the
//     * SquidLib repo</a>; there's an archive that acts as a snapshot of SquidLib 3.0.0's Thesaurus class, for instance.
//     * If you save the 3.0.0 archive in a libGDX application's assets folder, you can reload the 3.0.0 definitions into
//     * a Thesaurus called {@code myThesaurus} with:
//     * <br>
//     * {@code myThesaurus.addArchivedCategories(Gdx.files.internal("Thesaurus-3-0-0.txt").readString("UTF-8"));}
//     *
//     * @param archive an archived String of categories produced by {@link #archiveCategories()}
//     * @return this Thesaurus, but static state of the class will also be modified so this may affect other Thesaurus objects
//     */
//    public Thesaurus addArchivedCategories(String archive){
//        final OrderedMap<String, ObjectList<String>> cat = Converters.convertOrderedMap(
//                Converters.convertString,
//                Converters.convertObjectList(Converters.convertString)
//        ).restore(archive);
//
//        mappings.clear();
//
//        categories.clear();
//        categories.putAll(cat);
//
//        adjective.clear();
//        adjective.putAll(cat);
//
//        noun.clear();
//        noun.putAll(cat);
//
//        nouns.clear();
//        nouns.putAll(cat);
//
//        Iterator<String> it = adjective.keySet().iterator();
//        while (it.hasNext()){
//            if(!it.next().contains("`adj`"))
//                it.remove();
//        }
//        it = noun.keySet().iterator();
//        while (it.hasNext()){
//            if(!it.next().contains("`noun`"))
//                it.remove();
//        }
//        it = nouns.keySet().iterator();
//        while (it.hasNext()){
//            if(!it.next().contains("`nouns`"))
//                it.remove();
//        }
//
//        for(Map.Entry<String, ObjectList<String>> kv : categories.entrySet())
//        {
//            addCategory(kv.getKey(), kv.getValue());
//        }
//        plantTermShuffler =  mappings.get("plant`term`");
//        fruitTermShuffler =  mappings.get("fruit`term`");
//        nutTermShuffler =    mappings.get("nut`term`");
//        flowerTermShuffler = mappings.get("flower`term`");
//        potionTermShuffler = mappings.get("potion`term`");
//
//        return this;
//
//    }
    /**
     * Given an archive String saved by {@link #archiveCategoriesAlternate()} (probably from another version of
     * SquidLib), this makes the Thesaurus class act like it did in that archive, assuming the {@link #rng} is seeded
     * the same and uses the same algorithm/RandomnessSource. This modifies the {@link #mappings} field and the
     * {@link #categories}, {@link #adjective}, {@link #noun}, and {@link #nouns} static fields, so it can affect other
     * Thesaurus objects produced later (it won't change previously-made ones, probably).
     * <br>
     * If you didn't have an archive of the categories from some version of SquidLib, you can download one of the small
     * files from <a href="https://github.com/yellowstonegames/SquidLib/tree/master/archives">the 'archives' folder of the
     * SquidLib repo</a>; there's an archive that acts as a snapshot of SquidLib 3.0.0's Thesaurus class, for instance.
     * The files that use this method's format end in {@code .alt.txt}, while the other format uses {@code .squid.txt}.
     * If you save the 3.0.0 archive in a libGDX application's assets folder, you can reload the 3.0.0 definitions into
     * a Thesaurus called {@code myThesaurus} with:
     * <br>
     * {@code myThesaurus.addArchivedCategoriesAlternate(Gdx.files.internal("Thesaurus-3-0-0.alt.txt").readString("UTF-8"));}
     *
     * @param archive an archived String of categories produced by {@link #archiveCategoriesAlternate()}
     * @return this Thesaurus, but static state of the class will also be modified so this may affect other Thesaurus objects
     */
    public Thesaurus addArchivedCategoriesAlternate(String archive){
        String[] lines = archive.split("\r?\n");
        mappings.clear();

        categories.clear();
        adjective.clear();
        noun.clear();
        nouns.clear();
        for (int i = 0, n = lines.length; i < n; i++) {
            int idx = lines[i].indexOf('\u00A0'); // u00A0 is non-breaking space
            String k;
            ObjectList<String> v;
            categories.put(k = lines[i].substring(0, idx), v = ObjectList.with(StringTools.split(lines[i].substring(idx + 1), "\u00A0")));
            adjective.put(k, v);
            noun.put(k, v);
            nouns.put(k, v);
        }

        Iterator<CharSequence> it = adjective.keySet().iterator();
        while (it.hasNext()){
            if(!StringTools.contains(it.next(), "`adj`"))
                it.remove();
        }
        it = noun.keySet().iterator();
        while (it.hasNext()){
            if(!StringTools.contains(it.next(), "`noun`"))
                it.remove();
        }
        it = nouns.keySet().iterator();
        while (it.hasNext()){
            if(!StringTools.contains(it.next(), "`nouns`"))
                it.remove();
        }

        for(Map.Entry<CharSequence, ObjectList<String>> kv : categories.entrySet())
        {
            addCategory(kv.getKey(), kv.getValue());
        }
        plantTermShuffler =  mappings.get("plant`term`");
        fruitTermShuffler =  mappings.get("fruit`term`");
        nutTermShuffler =    mappings.get("nut`term`");
        vegetableTermShuffler = mappings.get("vegetable`term`");
        flowerTermShuffler = mappings.get("flower`term`");
        potionTermShuffler = mappings.get("potion`term`");

        return this;
    }

    /**
     * Adds a large list of words pre-generated by Language and hand-picked for fitness, and makes them
     * accessible with a keyword based on the language. The keywords this currently knows:
     * <br>
     * "lovecraft`pre`", "english`pre`", "greek_romanized`pre`",
     * "greek_authentic`pre`", "french`pre`", "russian_romanized`pre`",
     * "russian_authentic`pre`", "japanese_romanized`pre`", "swahili`pre`",
     * "somali`pre`", "hindi_romanized`pre`", "arabic_romanized`pre`",
     * "inuktitut`pre`", "norse`pre`", "nahuatl`pre`", "mongolian`pre`",
     * "fantasy`pre`", "fancy_fantasy`pre`", "goblin`pre`", "elf`pre`",
     * "demonic`pre`", "infernal`pre`", "simplish`pre`", "alien_a`pre`",
     * "korean_romanized`pre`", "alien_e`pre`", "alien_i`pre`", "alien_o`pre`",
     * "alien_u`pre`", "dragon`pre`", "kobold`pre`", "insect`pre`", "maori`pre`",
     * "spanish`pre`", "deep_speech`pre`", "norse_simplified`pre`",
     * "hletkip`pre`", "ancient_egyptian`pre`", "crow`pre`", "imp`pre`",
     * "malay`pre`", "celestial`pre`", "chinese_romanized`pre`",
     * "cherokee_romanized`pre`", "vietnamese`pre`"
     * <br>
     * These correspond to similarly-named fields in {@link Language}, just without {@code `pre`}; for instance
     * {@code "cherokee_romanized`pre`"} corresponds to {@link Language#CHEROKEE_ROMANIZED}. You can use these
     * same keywords with {@code `gen`} instead of {@code `pre`} to generate at runtime based on the current RNG state,
     * instead of using one of several pre-generated words, and doing that does not require addFakeWords() to be used.
     * <br>
     * Capitalizing the first letter in the keyword where it appears in text you call process() on will capitalize the
     * first letter of the produced fake word, which is often desirable for things like place names. Capitalizing the
     * second letter will capitalize the whole produced fake word. This applies only per-instance of each keyword; it
     * won't change the internally-stored list of words.
     * @return this for chaining
     */
    public Thesaurus addFakeWords()
    {
        //long state = rng.getState();
        for(Map.Entry<CharSequence, Language> kv : languages.entrySet())
        {
            ObjectList<String> words = new ObjectList<>(16);
            for (int i = 0; i < 16; i++) {
                words.add(kv.getValue().word(rng, false, rng.nextInt(2, 4)));
            }
            addCategory(StringTools.replace(kv.getKey(), "`gen", "`pre"), words);
        }
        //rng.setState(state);
        return this;
    }

    private StringBuilder modify(CharSequence text)
    {
        Matcher m;
        StringBuilder sb = new StringBuilder(text);
        Replacer.StringBuilderBuffer tb, working = Replacer.wrap(sb);
        StringBuilder tmp;
        boolean found;
        Language.Alteration alt;
        for (int a = 0; a < alterations.size(); a++) {
            alt = alterations.get(a);
            tmp = working.sb;
            tb = Replacer.wrap(new StringBuilder(tmp.length()));
            m = alt.replacer.getPattern().matcher(tmp);

            found = false;
            while (true) {
                if (rng.nextDouble() < alt.chance) {
                    if (!Replacer.replaceStep(m, alt.replacer.getSubstitution(), tb))
                        break;
                    found = true;
                } else {
                    if (!m.find())
                        break;
                    found = true;
                    m.getGroup(MatchResult.PREFIX, tb);
                    m.getGroup(MatchResult.MATCH, tb);
                    m.setTarget(m, MatchResult.SUFFIX);
                }
            }
            if (found) {
                m.getGroup(MatchResult.TARGET, tb);
                working = tb;
            }
        }
        return working.sb;

    }
    /**
     * Given a String, StringBuilder, or other CharSequence that should contain words this knows synonyms for, this
     * replaces each occurrence of such a known word with one of its synonyms, leaving unknown words untouched. Words
     * that were learned together as synonyms with addSynonyms() will be replaced in such a way that an individual
     * replacement word should not occur too close to a previous occurrence of the same word; that is, replacing the
     * text "You fiend! You demon! You despoiler of creation; devil made flesh!", where "fiend", "demon", and "devil"
     * are all synonyms, would never produce a string that contained "fiend" as the replacement for all three of those.
     * @param text a CharSequence, such as a String, that contains words in the source language
     * @return a String of the translated text.
     */
    public String process(CharSequence text)
    {
//        Replacer rep = wordMatch.replacer(new SynonymSubstitution());
        //String t = rep.replace(text);
        Matcher m = wordMatch.matcher(text);
        Substitution substitution = new SynonymSubstitution();
        Replacer.StringBuilderBuffer dest = Replacer.wrap(new StringBuilder(text.length()));
        while (m.find()) {
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            m.setTarget(m, MatchResult.SUFFIX);
        }
        m.getGroup(MatchResult.TARGET, dest);

        m.setTarget(dest.sb);
        dest.sb.setLength(0);
        while (m.find()) {
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            m.setTarget(m, MatchResult.SUFFIX);
        }
        m.getGroup(MatchResult.TARGET, dest);

        if(alterations.isEmpty())
            return StringTools.replace(StringTools.correctABeforeVowel(dest.sb), "\t", "");
        else
            return StringTools.replace(modify(StringTools.correctABeforeVowel(dest.sb)), "\t", "");
    }

    public String lookup(String word)
    {
        if(word.isEmpty())
            return word;
        if("@".equals(word))
        {
            return defaultLanguage.word(rng, true, rng.nextInt(2, 4));
        }
        String word2 = word.toLowerCase();
        if(mappings.containsKey(word2))
        {
            String nx = mappings.get(word2).next();
            if(nx.isEmpty())
                return nx;
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
                return nx.toUpperCase();
            if(Category.Lu.contains(word.charAt(0)))
            {
                return Character.toUpperCase(nx.charAt(0)) + nx.substring(1);
            }
            return nx;
        }
        else if(languages.containsKey(word2))
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
                return languages.get(word2).word(rng, false, rng.nextInt(2, 4)).toUpperCase();
            if(Category.Lu.contains(word.charAt(0)))
            {
                return languages.get(word2).word(rng, true, rng.nextInt(2, 4));
            }
            return languages.get(word2).word(rng, false, rng.nextInt(2, 4));
        }
        return word;
    }

    private class SynonymSubstitution implements Substitution
    {
        private final StringBuilder temp = new StringBuilder(64);
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            //dest.append(lookup(match.group(0)));
            temp.setLength(0);
            match.getGroup(0, temp);
            writeLookup(dest, temp);
        }
    }

    private void writeLookup(TextBuffer dest, StringBuilder word) {
        if(word == null || word.length() <= 0)
            return;
        GapShuffler<String> mapping;
        Language lang;
        Integer num, numberAdj;
        if(word.charAt(0) == '@' && word.length() == 1)
        {
            dest.append(defaultLanguage.word(rng, true, rng.nextInt(2, 4)));
            return;
        }
        else if((mapping = mappings.get(word)) != null)
        {
            String nx = mapping.next();
            if(nx.isEmpty())
                return;
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(nx.toUpperCase());
                return;
            }
            if(Category.Lu.contains(word.charAt(0)))
            {
                dest.append(Character.toUpperCase(nx.charAt(0)));
                dest.append(nx.substring(1));
                return;
            }
            dest.append(nx);
            return;
        }
        else if((lang = languages.get(word)) != null)
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(lang.word(rng, false, rng.nextInt(2, 4)).toUpperCase());
            }
            else if(Category.Lu.contains(word.charAt(0)))
            {
                dest.append(lang.word(rng, true, rng.nextInt(2, 4)));
            }
            else
            {
                dest.append(lang.word(rng, false, rng.nextInt(2, 4)));
            }
            return;
        }
        else if((num = numbers.get(word)) != null)
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(numberWordInRange(2, num).toUpperCase());
            }
            else if(Category.Lu.contains(word.charAt(0)))
            {
                String w = numberWordInRange(2, num);
                dest.append(Character.toUpperCase(w.charAt(0)));
                dest.append(w.substring(1));
            }
            else
            {
                dest.append(numberWordInRange(2, num));
            }
            return;
        }
        else if((numberAdj = numberAdjectives.get(word)) != null)
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(numberAdjectiveInRange(2, numberAdj).toUpperCase());
            }
            else if(Category.Lu.contains(word.charAt(0)))
            {
                String w = numberAdjectiveInRange(2, numberAdj);
                dest.append(Character.toUpperCase(w.charAt(0)));
                dest.append(w.substring(1));
            }
            else
            {
                dest.append(numberAdjectiveInRange(2, numberAdj));
            }
            return;
        }
        if(dest instanceof Replacer.StringBuilderBuffer)
        {
            ((Replacer.StringBuilderBuffer)dest).sb.append(word);
        }
        else
            dest.append(word.toString());

    }

    private class RandomLanguageSubstitution implements Substitution
    {
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            Language lang = Language.randomLanguage(rng.nextLong());
            randomLanguages.add(lang);
            if(match.isCaptured(1))
            {
                lang = Language.randomLanguage(rng.nextLong());
                randomLanguages.add(lang);
                do {
                    latestGenerated = randomLanguages.get(0).word(rng, true, Math.min(rng.nextInt(2, 5), rng.nextInt(1, 5)))
                            + "-" + randomLanguages.get(1).word(rng, true, Math.min(rng.nextInt(2, 5), rng.nextInt(1, 5)));
                }while (latestGenerated.length() <= 5 || latestGenerated.length() >= 17);
                dest.append(latestGenerated);
            }
            else
            {
                do{
                    latestGenerated = lang.word(rng, true, Math.min(rng.nextInt(2, 5), rng.nextInt(1, 5)));
                }while (latestGenerated.length() <= 2 || latestGenerated.length() >= 11);
                dest.append(latestGenerated);
            }
        }
    }

    private class KnownLanguageSubstitution implements Substitution
    {
        public Language language;
        public KnownLanguageSubstitution(Language lang)
        {
            language = lang;
        }
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            if (match.isCaptured(1)) {
                do
                {
                    latestGenerated = language.word(rng, true, Math.min(rng.nextInt(2, 5), rng.nextInt(1, 5))) +
                            "-" + language.word(rng, true, Math.min(rng.nextInt(2, 5), rng.nextInt(1, 5)));
                }while (latestGenerated.length() <= 5 || latestGenerated.length() >= 17);
                dest.append(latestGenerated);
            } else
            {
                do{
                    latestGenerated = language.word(rng, true, Math.min(rng.nextInt(2, 5), rng.nextInt(1, 5)));
                }while (latestGenerated.length() <= 2 || latestGenerated.length() >= 11);
                dest.append(latestGenerated);
            }
        }
    }

    /**
     * Generates a random possible name for a nation, such as "Iond-Gouccief Alliance" or "The Last Drayo Commonwealth".
     * May use accented characters, as in
     * "Thùdshù-Hyóttiálb Hegemony" or "The Glorious Chô Empire"; if you want to strip these out and replace accented
     * chars with their un-accented counterparts, you can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making nation names, by
     * getting the Language elements of this class' {@link #randomLanguages} field. Using one of these
     * Language objects, you can produce many more words with a similar style to the nation name, like "Drayo" in
     * "The Last Drayo Commonwealth". If more than one language was used in the nation name, as in "Thùdshù-Hyóttiálb
     * Hegemony", you will have two languages in randomLanguages, so here "Thùdshù" would be generated by the first
     * language, and "Hyóttiálb" by the second language. Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can. This also assigns the
     * {@link #latestGenerated} field to contain the part of the nation name without any larger titles; in the case of
     * "The Glorious Chô Empire", the latestGenerated field would be assigned "Chô" at the same time the longer name
     * would be returned. This field will be reassigned if this method is called again.
     *
     * @return a random name for a nation or a loose equivalent to a nation, as a String
     */
    public String makeNationName()
    {
        if(!this.mappings.containsKey("empire`noun`"))
        {
            addKnownCategories();
        }
        String working = process(rng.randomElement(nationTerms));
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(rng.randomElement(nationTerms));
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working);
    }
    /**
     * Generates a random possible name for a nation, such as "Iond-Gouccief Alliance" or "The Last Drayo Commonwealth",
     * with the Language already available instead of randomly created. May use accented characters, as in
     * "Thùdshù Hegemony" or "The Glorious Chô Empire",
     * if the given language can produce them; if you want to strip these out and replace accented chars
     * with their un-accented counterparts, you can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link Language#removeAccents()} on the Language you would give this. This assigns the
     * {@link #latestGenerated} field to contain the part of the nation name without any larger titles; in the case of
     * "The Glorious Chô Empire", the latestGenerated field would be assigned "Chô" at the same time the longer name
     * would be returned. This field will be reassigned if this method is called again.
     * <br>
     * Some nation names use a hyphenated pairing of what would normally be names in two different languages; if one of
     * those names is produced by this it will produce two names in the same linguistic style. The randomLanguages field
     * is not populated by this method; it is assumed that since you are passing this a Language, you already
     * have the one you want to use anyway.
     *
     * @param language a Language that will be used to construct any non-English names
     * @return a random name for a nation or a loose equivalent to a nation, as a String
     */
    public String makeNationName(Language language)
    {
        if(!this.mappings.containsKey("empire`noun`"))
        {
            addKnownCategories();
        }
        String working = process(rng.randomElement(nationTerms));
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(rng.randomElement(nationTerms));
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working);
    }

    /**
     * Generates a random possible name for a plant or tree, such as "Ikesheha's maple" or "sugarleaf birch".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the Language elements of this class' {@link #randomLanguages} field. Using one of these
     * Language objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makePlantName()
    {
        if(!this.mappings.containsKey("tree`noun`"))
        {
            addKnownCategories();
        }
        String working = process(plantTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(plantTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a plant or tree, such as "Ikesheha's maple" or "sugarleaf birch",
     * with the Language already available instead of randomly created. May use accented characters, as in
     * "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link Language#removeAccents()} on the Language you would give this.
     *
     * @param language a Language that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makePlantName(Language language)
    {
        if(!this.mappings.containsKey("tree`noun`"))
        {
            addKnownCategories();
        }
        String working = process(plantTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(plantTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }


    /**
     * Generates a random possible name for a fruit, such as "green lime-melon" or "Ung's date".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the Language elements of this class' {@link #randomLanguages} field. Using one of these
     * Language objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFruitName()
    {
        if(!this.mappings.containsKey("fruit`noun`"))
        {
            addKnownCategories();
        }
        String working = process(fruitTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(fruitTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a fruit, such as "green lime-melon" or "Ung's date",
     * with the Language already available instead of randomly created. May use accented characters, as in
     * "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link Language#removeAccents()} on the Language you would give this.
     *
     * @param language a Language that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFruitName(Language language)
    {
        if(!this.mappings.containsKey("fruit`noun`"))
        {
            addKnownCategories();
        }
        String working = process(fruitTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(fruitTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible name for a nut or bean, such as "nut of Gikoim" or "Pelyt's cashew".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the Language elements of this class' {@link #randomLanguages} field. Using one of these
     * Language objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeNutName()
    {
        if(!this.mappings.containsKey("nut`noun`"))
        {
            addKnownCategories();
        }
        String working = process(nutTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(nutTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a nut or bean, such as "nut of Gikoim" or "Pelyt's cashew",
     * with the Language already available instead of randomly created.
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link Language#removeAccents()} on the Language you would give this.
     *
     * @param language a Language that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeNutName(Language language)
    {
        if(!this.mappings.containsKey("nut`noun`"))
        {
            addKnownCategories();
        }
        String working = process(nutTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(nutTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }


    /**
     * Generates a random possible name for a vegetable, such as "miniature yam" or Iliakshe's spicy zucchini".
     * May use accented characters, as in "Fëangoh's blue cauliflower", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the Language elements of this class' {@link #randomLanguages} field. Using one of these
     * Language objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a vegetable, as a String
     */
    public String makeVegetableName()
    {
        if(!this.mappings.containsKey("vegetable`noun`"))
        {
            addKnownCategories();
        }
        String working = process(vegetableTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(vegetableTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a vegetable, such as "miniature yam" or Iliakshe's spicy zucchini".
     * with the Language already available instead of randomly created.
     * May use accented characters, as in "Fëangoh's blue cauliflower", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link Language#removeAccents()} on the Language you would give this.
     *
     * @param language a Language that will be used to construct any non-English names
     * @return a random name for a vegetable, as a String
     */
    public String makeVegetableName(Language language)
    {
        if(!this.mappings.containsKey("vegetable`noun`"))
        {
            addKnownCategories();
        }
        String working = process(vegetableTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(vegetableTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible name for a flower or flowering plant, such as "tulip of Jirui" or "thorny lilac".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the Language elements of this class' {@link #randomLanguages} field. Using one of these
     * Language objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFlowerName()
    {
        if(!this.mappings.containsKey("flower`noun`"))
        {
            addKnownCategories();
        }
        String working = process(flowerTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(flowerTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a flower or flowering plant, such as "tulip of Jirui" or "thorny lilac",
     * with the Language already available instead of randomly created. May use accented characters, as in
     * "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link Language#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link Language#removeAccents()} on the Language you would give this.
     *
     * @param language a Language that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFlowerName(Language language)
    {
        if(!this.mappings.containsKey("flower`noun`"))
        {
            addKnownCategories();
        }
        String working = process(flowerTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(flowerTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible description for a potion in a container, such as "a smoky glass flask containing a
     * few drops of an orange tonic", "a milk carton filled with a red fluid", "a shining silver bottle filled with an
     * effervescent violet potion", or "a wineskin filled with a black serum".
     *
     * @return a random description for a potion or other liquid in a container
     */
    public String makePotionDescription()
    {
        if(!this.mappings.containsKey("liquid`noun`"))
        {
            addKnownCategories();
        }
        String working = process(potionTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(potionTermShuffler.next());
        return StringTools.correctABeforeVowel(working);
    }

    /**
     * Gets an English word for a given number, if this knows it. These words are known from 0 ("zero") to 20
     * ("twenty"), as well as some higher numbers. If a word isn't known for a number, this returns the number as a
     * String, such as "537" or "-1".
     * @param number the number to get a word for
     * @return the word associated with a number as a String, or if none is known, {@code String.valueOf(number)}.
     */
    public static String numberWord(final int number)
    {
        switch (number){
            case 0: return "zero";
            case 1: return "one";
            case 2: return "two";
            case 3: return "three";
            case 4: return "four";
            case 5: return "five";
            case 6: return "six";
            case 7: return "seven";
            case 8: return "eight";
            case 9: return "nine";
            case 10: return "ten";
            case 11: return "eleven";
            case 12: return "twelve";
            case 13: return "thirteen";
            case 14: return "fourteen";
            case 15: return "fifteen";
            case 16: return "sixteen";
            case 17: return "seventeen";
            case 18: return "eighteen";
            case 19: return "nineteen";
            case 20: return "twenty";
            case 30: return "thirty";
            case 40: return "fourty";
            case 50: return "fifty";
            case 60: return "sixty";
            case 70: return "seventy";
            case 80: return "eighty";
            case 90: return "ninety";
            case 100: return "hundred";
            case 1000: return "thousand";
            case 1000000: return "million";
            case 1000000000: return "billion";
            default: return String.valueOf(number);
        }
    }

    /**
     * Gets an English word that describes a numbered position in some sequence, if this knows it (such as "second" or
     * "eleventh"). These words are known from 1 ("first") to 20 ("twentieth"), as well as some higher numbers. If a
     * word isn't known for a number, this appends a suffix based on the last base-10 digit to the number, as with "0th"
     * or "42nd".
     * @param number the number to get a position adjective for
     * @return the word associated with a number as a String, or if none is known, {@code String.valueOf(number)} followed by a two-char suffix from the last digit.
     */
    public static String numberAdjective(final int number)
    {
        switch (number){
            case 1: return "first";
            case 2: return "second";
            case 3: return "third";
            case 4: return "fourth";
            case 5: return "fifth";
            case 6: return "sixth";
            case 7: return "seventh";
            case 8: return "eighth";
            case 9: return "ninth";
            case 10: return "tenth";
            case 11: return "eleventh";
            case 12: return "twelfth";
            case 13: return "thirteenth";
            case 14: return "fourteenth";
            case 15: return "fifteenth";
            case 16: return "sixteenth";
            case 17: return "seventeenth";
            case 18: return "eighteenth";
            case 19: return "nineteenth";
            case 20: return "twentieth";
            case 30: return "thirtieth";
            case 40: return "fourtieth";
            case 50: return "fiftieth";
            case 60: return "sixtieth";
            case 70: return "seventieth";
            case 80: return "eightieth";
            case 90: return "ninetieth";
            case 100: return "hundredth";
            case 1000: return "thousandth";
            case 1000000: return "millionth";
            case 1000000000: return "billionth";
            default: 
            {
                switch (number % 10){
                    case 1: return number + "st";
                    case 2: return number + "nd";
                    case 3: return number + "rd";
                    default:return number + "th";
                }
            }
        }
    }

    /**
     * Gets an English word for a number, if this knows it, where the number is chosen randomly nextInt lowest and
     * highest, both inclusive. These words are known from 0 ("zero") to 20 ("twenty"), as well as some higher numbers.
     * If a word isn't known for a number, this returns the number as a String, such as "537" or "-1".
     * @param lowest the lower bound for numbers this can choose, inclusive
     * @param highest the upper bound for numbers this can choose, inclusive
     * @return a String word for a number in the given range, such as "six" or "eleven", if it is known
     */
    public String numberWordInRange(int lowest, int highest)
    {
        return numberWord(rng.nextSignedInt(highest + 1 - lowest) + lowest);
    }
    /**
     * Gets an English word that describes a numbered position in some sequence, if this knows it (such as "second" or
     * "eleventh"), where the number is chosen randomly nextInt lowest and highest, both inclusive. These words are
     * known from 1 ("first") to 20 ("twentieth"), as well as some output for other numbers (such as "42nd" or "23rd").
     * @param lowest the lower bound for numbers this can choose, inclusive
     * @param highest the upper bound for numbers this can choose, inclusive
     * @return a String word for a number in the given range, such as "third" or "twelfth", if it is known
     */
    public String numberAdjectiveInRange(int lowest, int highest)
    {
        return numberAdjective(rng.nextSignedInt(highest + 1 - lowest) + lowest);
    }
    
    private static final ObjectList<String> nationTerms = with(
            "Union`adj` Union`noun` of @", "Union`adj` @ Union`noun`", "@ Union`noun`", "@ Union`noun`", "@-@ Union`noun`", "Union`adj` Union`noun` of @",
            "Union`adj` Duchy`nouns` of @",  "The @ Duchy`noun`", "The Fancy`adj` @ Duchy`noun`", "The Sole`adj` @ Empire`noun`",
            "@ Empire`noun`", "@ Empire`noun`", "@ Empire`noun`", "@-@ Empire`noun`", "The Fancy`adj` @ Empire`noun`", "The Fancy`adj` @ Empire`noun`", "The Holy`adj` @ Empire`noun`");

    private static final ObjectList<String> plantTerms = with(
            "@'s color`adj`\tleaf`noun`",
            "@'s tree`noun`",
            "@'s color`adj` tree`noun`",
            "@'s flower`noun`",
            "@'s shape`adj` flower`noun`",
            "@'s color`adj` flower`noun`",
            "flower`noun` of @",
            "leaf`noun` of @",
            "@'s ground`noun`\tleaf`noun`",
            "ground`noun`\tleaf`noun` of @",
            "sensory`adj` tree`noun` of @",
            "sensory`adj` flower`noun` of @",
            "color`adj` flower`noun` of @",
            "ground`noun`\tleaf`noun`",
            "@'s sensory`adj`-leaf`noun`",
            "@'s bush`noun`",
            "@'s shape`adj` bush`noun`",
            "@'s bush`noun`-bush`noun`",
            "@'s tree`noun`-tree`noun`",
            "@'s tree`noun`-bush`noun`",
            "@'s bush`noun`-tree`noun`",
            "@'s flower`noun`-bush`noun`",
            "sensory`adj`-leaf`noun` of @",
            "bush`noun` of @",
            "shape`adj` bush`noun` of @",
            "bush`noun`-bush`noun` of @",
            "tree`noun`-tree`noun` of @",
            "tree`noun`-bush`noun` of @",
            "bush`noun`-tree`noun` of @",
            "flower`noun`-bush`noun` of @",
            "bush`noun`-bush`noun`",
            "tree`noun`-tree`noun`",
            "tree`noun`-bush`noun`",
            "bush`noun`-tree`noun`",
            "flower`noun`-bush`noun`",
            "sensory`adj` bush`noun`-bush`noun`",
            "sensory`adj` tree`noun`-tree`noun`",
            "sensory`adj` tree`noun`-bush`noun`",
            "sensory`adj` bush`noun`-tree`noun`",
            "sensory`adj` flower`noun`-bush`noun`",
            "sensory`adj` bush`noun`",
            "sensory`adj` tree`noun`",
            "sensory`adj`-leaf`noun` bush`noun`",
            "sensory`adj`-leaf`noun` tree`noun`",
            "color`adj`\tleaf`noun` bush`noun`",
            "color`adj`\tleaf`noun` tree`noun`",
            "color`adj`-leaf`noun` bush`noun`",
            "color`adj`-leaf`noun` tree`noun`",
            "color`adj` bush`noun`",
            "color`adj` tree`noun`",
            "shape`adj` flower`noun`",
            "color`adj` flower`noun`",
            "flavor`noun`\tleaf`noun` tree`noun`",
            "fruit`noun` tree`noun`",
            "nut`noun` tree`noun`",
            "vegetable`noun` tree`noun`",
            "fruit`noun` bush`noun`",
            "nut`noun` bush`noun`",
            "vegetable`noun` bush`noun`",
            "flavor`adj` fruit`noun` tree`noun`",
            "flavor`adj` nut`noun` tree`noun`",
            "color`adj` fruit`noun` tree`noun`",
            "color`adj` nut`noun` tree`noun`",
            "shape`adj`-fruit`noun` tree`noun`",
            "shape`adj`-leaf`noun` tree`noun`",
            "sensory`adj` tree`noun`-tree`noun`",
            "sensory`adj`-leaf`noun` tree`noun`",
            "color`adj`-leaf`noun` flower`noun`",
            "shape`adj`-leaf`noun` flower`noun`",
            "sensory`adj` flower`noun`-flower`noun`",
            "sensory`adj`-leaf`noun` flower`noun`",
            "ground`noun`\tflower`noun`",
            "ground`noun`\tvegetable`noun`",
            "vegetable`noun`-leaf`noun` tree`noun`",
            "color`adj`-vegetable`noun` tree`noun`",
            "shape`adj`-vegetable`noun` tree`noun`"
    );
    private static final ObjectList<String> fruitTerms = with(
            "fruit`noun` of @",
            "color`adj` fruit`noun` of @",
            "flavor`adj` fruit`noun` of @",
            "@'s fruit`noun`",
            "@'s flavor`adj` fruit`noun`",
            "@'s color`adj` fruit`noun`",
            "flavor`adj` fruit`noun`",
            "color`adj` fruit`noun`",
            "shape`adj` fruit`noun`",
            "flavor`adj` fruit`noun`-fruit`noun`",
            "color`adj` fruit`noun`-fruit`noun`",
            "shape`adj` fruit`noun`-fruit`noun`"
    );
    private static final ObjectList<String> nutTerms = with(
            "nut`noun` of @",
            "color`adj` nut`noun` of @",
            "@'s nut`noun`",
            "@'s flavor`adj` nut`noun`",
            "@'s color`adj` nut`noun`",
            "flavor`adj` nut`noun`",
            "color`adj` nut`noun`",
            "sensory`adj` nut`noun`"
    );
    private static final ObjectList<String> vegetableTerms = with(
            "vegetable`noun` of @",
            "color`adj` vegetable`noun` of @",
            "@'s vegetable`noun`",
            "@'s flavor`adj` vegetable`noun`",
            "@'s color`adj` vegetable`noun`",
            "flavor`adj` vegetable`noun`",
            "color`adj` vegetable`noun`",
            "sensory`adj` vegetable`noun`",
            "shape`adj` vegetable`noun`",
            "color`adj` sensory`adj` vegetable`noun`",
            "shape`adj` color`adj` vegetable`noun`",
            "sensory`adj` shape`adj` vegetable`noun`",
            "@'s vegetable`noun`-vegetable`noun`",
            "flavor`adj` vegetable`noun`-vegetable`noun`",
            "color`adj` vegetable`noun`-vegetable`noun`",
            "sensory`adj` vegetable`noun`-vegetable`noun`",
            "shape`adj` vegetable`noun`-vegetable`noun`"
    );
    private static final ObjectList<String> flowerTerms = with(
            "flower`noun` of @",
            "sensory`adj` flower`noun` of @",
            "color`adj` flower`noun` of @",
            "@'s flower`noun`",
            "@'s shape`adj` flower`noun`",
            "@'s color`adj` flower`noun`",
            "shape`adj` flower`noun`",
            "color`adj` flower`noun`",
            "color`adj`-leaf`noun` flower`noun`",
            "shape`adj`-leaf`noun` flower`noun`",
            "sensory`adj` flower`noun`-flower`noun`",
            "sensory`adj`-leaf`noun` flower`noun`",
            "ground`noun`\tflower`noun`"
    );
    private static final ObjectList<String> potionTerms = with(
            "a bottle`adj` bottle`noun` filled with a liquid`adj` color`adj` liquid`noun`",
            "a bottle`adj` bottle`noun` filled with a color`adj` liquid`noun`",
            "a calabash`noun` filled with a color`adj` liquid`noun`",
            "a bottle`adj` bottle`noun` half-filled with a liquid`adj` color`adj` liquid`noun`",
            "a bottle`adj` bottle`noun` containing a few drops of a color`adj` liquid`noun`"
        );
    public static final CaseInsensitiveOrderedMap<ObjectList<String>> categories = CaseInsensitiveOrderedMap.with(
            "calm`adj`",
            with("harmonious", "peaceful", "pleasant", "serene", "placid", "tranquil", "calm"),
            "calm`noun`",
            with("harmony", "peace", "kindness", "serenity", "tranquility", "calm"),
            "org`noun`",
            with("fraternity", "brotherhood", "order", "group", "foundation", "association", "guild", "fellowship", "partnership"),
            "org`nouns`",
            with("fraternities", "brotherhoods", "orders", "groups", "foundations", "associations", "guilds", "fellowships", "partnerships"),
            "empire`adj`",
            with("imperial", "prince's", "king's", "sultan's", "regal", "dynastic", "royal", "hegemonic", "monarchic", "ascendant", "emir's", "lordly"),
            "empire`noun`",
            with("empire", "emirate", "kingdom", "sultanate", "dominion", "dynasty", "imperium", "hegemony", "triumvirate", "ascendancy", "monarchy", "commonwealth"),
            "empire`nouns`",
            with("empires", "emirates", "kingdoms", "sultanates", "dominions", "dynasties", "imperia", "hegemonies", "triumvirates", "ascendancies", "monarchies", "commonwealths"),
            "emperor`noun`",
            with("emperor", "emir", "king", "sultan", "lord", "ruler", "pharaoh"),
            "emperor`nouns`",
            with("emperors", "emirs", "kings", "sultans", "lords", "rulers", "pharaohs"),
            "empress`noun`",
            with("empress", "emira", "queen", "sultana", "lady", "ruler", "pharaoh"),
            "empress`nouns`",
            with("empresses", "emiras", "queens", "sultanas", "ladies", "rulers", "pharaohs"),
            "union`adj`",
            with("united", "allied", "people's", "confederated", "federated", "congressional", "independent", "associated", "unified", "democratic"),
            "union`noun`",
            with("union", "alliance", "coalition", "confederation", "federation", "congress", "confederacy", "league", "faction", "republic"),
            "union`nouns`",
            with("unions", "alliances", "coalitions", "confederations", "federations", "congresses", "confederacies", "leagues", "factions", "republics"),
            "militia`noun`",
            with("rebellion", "resistance", "militia", "liberators", "warriors", "fighters", "militants", "front", "irregulars"),
            "militia`nouns`",
            with("rebellions", "resistances", "militias", "liberators", "warriors", "fighters", "militants", "fronts", "irregulars"),
            "gang`noun`",
            with("gang", "syndicate", "mob", "crew", "posse", "mafia", "cartel"),
            "gang`nouns`",
            with("gangs", "syndicates", "mobs", "crews", "posses", "mafias", "cartels"),
            "duke`noun`",
            with("duke", "earl", "baron", "fief", "lord", "shogun"),
            "duke`nouns`",
            with("dukes", "earls", "barons", "fiefs", "lords", "shoguns"),
            "duchy`noun`",
            with("duchy", "earldom", "barony", "fiefdom", "lordship", "shogunate"),
            "duchy`nouns`",
            with("duchies", "earldoms", "baronies", "fiefdoms", "lordships", "shogunates"),
            "magical`adj`",
            with("arcane", "enchanted", "sorcerous", "ensorcelled", "magical", "mystical"),
            "holy`adj`",
            with("auspicious", "divine", "holy", "sacred", "prophetic", "blessed", "godly", "virtuous"),
            "priest`noun`",
            with("priest", "bishop", "chaplain", "cleric", "cardinal", "preacher"),
            "priest`nouns`",
            with("priests", "bishops", "chaplains", "clergy", "cardinals", "preachers"),
            "unholy`adj`",
            with("bewitched", "occult", "unholy", "macabre", "accursed", "profane", "vile"),
            "witch`noun`",
            with("witch", "warlock", "necromancer", "cultist", "occultist", "defiler"),
            "witch`nouns`",
            with("witches", "warlocks", "necromancers", "cultists", "occultists", "defilers"),
            "forest`adj`",
            with("natural", "primal", "verdant", "lush", "fertile", "bountiful"),
            "forest`noun`",
            with("nature", "forest", "greenery", "jungle", "woodland", "grove", "copse", "glen"),
            "shaman`noun`",
            with("shaman", "druid", "warden", "animist"),
            "shaman`nouns`",
            with("shamans", "druids", "wardens", "animists"),
            "fancy`adj`",
            with("grand", "glorious", "magnificent", "magnanimous", "majestic", "great", "powerful"),
            "evil`adj`",
            with("heinous", "scurrilous", "terrible", "horrible", "debased", "wicked", "evil", "malevolent", "nefarious", "vile", "cruel", "abhorrent"),
            "villain`noun`",
            with("villain", "knave", "evildoer", "killer", "blasphemer", "monster", "murderer"),
            "villain`nouns`",
            with("villains", "knaves", "evildoers", "killers", "blasphemers", "monsters", "murderers"),
            "monster`noun`",
            with("fiend", "abomination", "demon", "devil", "ghoul", "monster", "beast", "creature"),
            "monsters`nouns`",
            with("fiends", "abominations", "demons", "devils", "ghouls", "monsters", "beasts", "creatures"),
            "good`adj`",
            with("righteous", "moral", "good", "pure", "compassionate", "flawless", "perfect", "kind"),
            "lethal`adj`",
            with("silent", "lethal", "deadly", "fatal", "venomous", "cutthroat", "murderous", "bloodstained", "stalking", "poisonous"),
            "lethal`noun`",
            with("silence", "killer", "assassin", "ninja", "venom", "poison", "snake", "murder", "blood", "razor", "tiger", "slayer"),
            "blade`noun`", // really any melee weapon
            with("blade", "knife", "sword", "axe", "stiletto", "katana", "scimitar", "hatchet", "spear", "glaive", "halberd",
                    "hammer", "maul", "flail", "mace", "sickle", "scythe", "whip", "lance", "nunchaku", "saber", "cutlass", "trident"),
            "bow`noun`", // really any medieval or earlier ranged weapon
            with("bow", "longbow", "shortbow", "crossbow", "sling", "atlatl", "bolas", "javelin", "net", "shuriken", "dagger"),
            "weapon`noun`", // any medieval or earlier weapon (not including firearms or newer)
            with("blade", "knife", "sword", "axe", "stiletto", "katana", "scimitar", "hatchet", "spear", "glaive", "halberd",
                    "hammer", "maul", "flail", "mace", "sickle", "scythe", "whip", "lance", "nunchaku", "saber", "cutlass", "trident",
                    "bow", "longbow", "shortbow", "crossbow", "sling", "atlatl", "bolas", "javelin", "net", "shuriken", "dagger"),
            "musket`noun`",
            with("arquebus", "blunderbuss", "musket", "matchlock", "flintlock", "wheellock", "cannon"),
            "grenade`noun`",
            with("rocket", "grenade", "missile", "bomb", "warhead", "explosive", "flamethrower"),
            "rifle`noun`",
            with("pistol", "rifle", "handgun", "firearm", "longarm", "shotgun"),
            "blade`nouns`",
            with("blades", "knives", "swords", "axes", "stilettos", "katana", "scimitars", "hatchets", "spears", "glaives", "halberds",
                    "hammers", "mauls", "flails", "maces", "sickles", "scythes", "whips", "lances", "nunchaku", "sabers", "cutlasses", "tridents"),
            "bow`nouns`",
            with("bows", "longbows", "shortbows", "crossbows", "slings", "atlatls", "bolases", "javelins", "nets", "shuriken", "daggers"),
            "weapon`nouns`",
            with("blades", "knives", "swords", "axes", "stilettos", "katana", "scimitars", "hatchets", "spears", "glaives", "halberds",
                    "hammers", "mauls", "flails", "maces", "sickles", "scythes", "whips", "lances", "nunchaku", "sabers", "cutlasses", "tridents",
                    "bows", "longbows", "shortbows", "crossbows", "slings", "atlatls", "bolases", "javelins", "nets", "shuriken", "daggers"),
            "musket`nouns`",
            with("arquebusses", "blunderbusses", "muskets", "matchlocks", "flintlocks", "wheellocks", "cannons"),
            "grenade`nouns`",
            with("rockets", "grenades", "missiles", "bombs", "warheads", "explosives", "flamethrowers"),
            "rifle`nouns`",
            with("pistols", "rifles", "handguns", "firearms", "longarms", "shotguns"),
            "scifi`adj`",
            with("plasma", "warp", "tachyonic", "phase", "gravitational", "photonic", "nanoscale", "laser", "quantum", "genetic"),
            "tech`adj`",
            with("cyber", "digital", "electronic", "techno", "hacker", "crypto", "turbo", "mechanical", "servo"),
            "sole`adj`",
            with("sole", "true", "singular", "total", "ultimate", "final", "last"),
            "light`adj`",
            with("bright", "glowing", "solar", "stellar", "lunar", "radiant", "luminous", "shimmering", "gleaming"),
            "light`noun`",
            with("light", "glow", "sun", "star", "moon", "radiance", "dawn", "torch", "shimmer", "gleam"),
            "light`nouns`",
            with("lights", "glimmers", "suns", "stars", "moons", "torches"),
            "shadow`noun`",
            with("shadow", "darkness", "gloom", "blackness", "murk", "twilight"),
            "shadow`nouns`",
            with("shadows", "darkness", "gloom", "blackness", "murk", "twilight"),
            "fire`noun`",
            with("fire", "flame", "inferno", "conflagration", "pyre", "blaze"),
            "fire`nouns`",
            with("fires", "flames", "infernos", "conflagrations", "pyres", "blazes"),
            "ice`noun`",
            with("ice", "frost", "snow", "chill", "blizzard", "cold"),
            "ice`nouns`",
            with("ice", "frosts", "snow", "chills", "blizzards", "cold"),
            "lightning`noun`",
            with("lightning", "thunder", "thunderbolt", "storm", "spark", "shock"),
            "lightning`nouns`",
            with("lightning", "thunder", "thunderbolts", "storms", "sparks", "shocks"),
            "ground`noun`",
            with("earth", "sand", "soil", "loam", "dirt", "clay", "mud", "peat"),
            "lake`noun`",
            with("puddle", "pond", "lake", "sea", "swamp", "bog", "fen", "glade"),
            "leaf`noun`",
            with("leaf", "bark", "root", "thorn", "seed", "branch", "twig", "wort", "cress", "flower", "wood", "vine", "sap", "bud", "blossom", "shoot", "stalk", "stem"),
            "fruit`noun`",
            with("fruit", "berry", "apple", "peach", "cherry", "melon", "lime", "fig", "date", "mango", "banana", "juniper", "grape", "papaya", "pear", "quince", "lemon", "kumquat", "loquat", "plum", "pineapple", "citron"),
            "nut`noun`",
            with("nut", "bean", "almond", "peanut", "pecan", "walnut", "cashew", "pea", "chestnut", "hazelnut", "lentil", "coconut", "pistachio"),
            "vegetable`noun`",
            with("carrot", "corn", "radish", "potato", "pumpkin", "zucchini", "taro", "yam", "mushroom", "spinach", "lettuce", "cabbage", "kale", "asparagus", "eggplant", "broccoli", "cauliflower", "celery", "beet", "onion", "leek", "truffle", "rutabaga", "artichoke", "avocado", "tomato", "fennel", "garlic", "parsley", "rice", "wheat", "barley", "rhubarb"),
            "flower`noun`",
            with("flower", "rose", "lilac", "orchid", "peony", "oleander", "chrysanthemum", "amaryllis", "camellia", "mallow", "lily", "gardenia", "daisy", "hibiscus", "dandelion", "jasmine", "lotus", "lantana", "phlox", "petunia", "tulip", "yarrow", "plumeria"),
            "tree`noun`",
            with("tree", "oak", "pine", "juniper", "maple", "beech", "birch", "larch", "willow", "alder", "cedar", "palm", "magnolia", "hazel", "cactus", "mangrove", "elm", "ficus", "eucalyptus"),
            "bush`noun`",
            with("bush", "brush", "bramble", "thicket", "hedge", "shrub", "manzanita", "privet", "hawthorn", "aloe", "nettle", "thistle", "boxwood", "brier", "milkweed", "dogwood", "fern", "flax", "hemp", "holly", "kudzu", "ragweed", "ragwort", "mesquite", "sorrel", "silkweed", "tansy", "tobacco", "tea", "honeysuckle"),
            "flavor`noun`",
            with("sugar", "spice", "acid", "soap", "salt", "grease", "smoke"),
            "flavor`adj`",
            with("sweet", "spicy", "sour", "bitter", "salty", "savory", "smoky"),
            "color`adj`",
            with("black", "white", "red", "orange", "yellow", "green", "blue", "violet", "gray", "brown", "pink"),
            "shape`adj`",
            with("hollow", "tufted", "drooping", "fibrous", "giant", "miniature", "delicate", "hardy", "spiny", "thorny", "fragile", "sturdy", "long", "stubby", "stiff", "yielding", "ridged", "toothy", "ovate", "tattered", "crusty", "massive", "diminutive", "large", "small", "arched", "flat", "pored", "spongy"),
            "sensory`adj`",
            with("fragrant", "pungent", "rustling", "fuzzy", "glossy", "weeping", "rough", "smooth", "soft", "aromatic", "spotted", "striped", "shaggy", "feathery", "hairy", "furry", "scaly"),
            "liquid`noun`",
            with("liquid", "elixir", "tonic", "fluid", "brew", "broth", "potion", "serum"),
            "liquid`adj`",
            with("bubbling", "effervescent", "swirling", "murky", "thick", "congealing", "sloshing", "slimy", "milky"),
            "bottle`noun`",
            with("bottle", "flask", "vial", "jug", "phial", "flagon", "canister"),
            "bottle`adj`",
            with("clear glass", "smoky glass", "green glass", "brown glass", "fluted crystal", "tarnished silver", "dull pewter", "shining silver", "curvaceous glass", "rough-cut glass", "sharp-edged tin"),
            "calabash`noun`",
            with("hollow gourd", "calabash", "milk carton", "waterskin", "wineskin"),
            "smart`adj`",
            with("brilliant", "smart", "genius", "wise", "clever", "cunning", "mindful", "aware"),
            "smart`noun`",
            with("genius", "wisdom", "cunning", "awareness", "mindfulness", "acumen", "smarts", "knowledge"),
            "stupid`adj`",
            with("stupid", "dumb", "idiotic", "foolish", "reckless", "careless", "sloppy", "dull", "moronic", "complacent"),
            "stupid`noun`",
            with("stupidity", "idiocy", "foolishness", "recklessness", "carelessness", "sloppiness", "complacency"),
            "bandit`noun`",
            with("thief", "raider", "bandit", "rogue", "brigand", "highwayman", "pirate"),
            "bandit`nouns`",
            with("thieves", "raiders", "bandits", "rogues", "brigands", "highwaymen", "pirates"),
            "soldier`noun`",
            with("soldier", "warrior", "fighter", "mercenary", "trooper", "combatant"),
            "soldier`nouns`",
            with("soldiers", "warriors", "fighters", "mercenaries", "troops", "combatants"),
            "guard`noun`",
            with("protector", "guardian", "warden", "defender", "guard", "shield", "sentinel", "watchman", "knight", "paladin", "templar"),
            "guard`nouns`",
            with("protectors", "guardians", "wardens", "defenders", "guards", "shields", "sentinels", "watchmen", "knights", "paladins", "templars"),
            "hunter`noun`",
            with("hunter", "poacher", "trapper", "warden", "stalker", "tracker"),
            "explorer`noun`",
            with("explorer", "pathfinder", "seeker", "questant", "wanderer", "nomad"),
            "hunter`nouns`",
            with("hunters", "poachers", "trappers", "wardens", "stalkers", "trackers"),
            "explorer`nouns`",
            with("explorers", "pathfinders", "seekers", "questants", "wanderers", "nomads"),
            "rage`noun`",
            with("rage", "fury", "anger", "wrath", "frenzy", "vengeance"),
            "ominous`adj`",
            with("ominous", "foreboding", "fateful", "baleful", "portentous"),
            "many`adj`",
            with("many", "myriad", "thousandfold", "infinite", "countless", "unlimited", "manifold"),
            "impossible`adj`",
            with("impossible", "forbidden", "incomprehensible", "ineffable", "unearthly", "abominable", "unspeakable", "indescribable"),
            "gaze`noun`",
            with("eye", "gaze", "stare", "observation", "purveyance", "watch"),
            "pain`noun`",
            with("pain", "agony", "misery", "excruciation", "torture"),
            "god`noun`",
            with("god", "deity", "ruler", "king", "father", "lord", "lordship"),
            "goddess`noun`",
            with("goddess", "deity", "ruler", "queen", "mother", "lady", "ladyship"),
            "hero`noun`",
            with("hero", "champion", "savior", "crusader", "knight"),
            "heroes`nouns`",
            with("heroes", "champions", "saviors", "crusaders", "knights"),
            "heroine`noun`",
            with("heroine", "champion", "savior", "crusader", "knight", "maiden"),
            "heroines`nouns`",
            with("heroines", "champions", "saviors", "crusaders", "knights", "maidens"),
            "popular`adj`",
            with("beloved", "adored", "revered", "worshipped"),
            "unpopular`adj`",
            with("reviled", "despised", "hated", "loathed"),
            "glyph`noun`",
            with("glyph", "sign", "symbol", "sigil", "seal", "mark"),
            "glyph`nouns`",
            with("glyphs", "signs", "symbols", "sigils", "seals", "marks"),
            "power`noun`",
            with("power", "force", "potency", "strength", "authority", "dominance"),
            "power`adj`",
            with("powerful", "forceful", "potent", "strong", "authoritative", "dominant"),
            "plant`term`",     plantTerms,
            "fruit`term`",     fruitTerms,
            "nut`term`",       nutTerms,
            "vegetable`term`", vegetableTerms,
            "flower`term`",    flowerTerms,
            "potion`term`",    potionTerms
    );
    public static final CaseInsensitiveOrderedMap<ObjectList<String>>
            adjective = new CaseInsensitiveOrderedMap<>(categories),
            noun = new CaseInsensitiveOrderedMap<>(categories),
            nouns = new CaseInsensitiveOrderedMap<>(categories);
    public static final CaseInsensitiveOrderedMap<Language> languages = new CaseInsensitiveOrderedMap<Language>(
            Language.registeredNames.length);
    static {
        for (int i = 0; i < Language.registeredNames.length; i++) {
            languages.put(Language.registeredNames[i].replace(' ', '_').toLowerCase() + "`gen`", Language.registered[i]);
        }
    }
    public static final CaseInsensitiveOrderedMap<Integer> numbers = CaseInsensitiveOrderedMap.with(
            "zero`noun`", 0,
            "one`noun`", 1,
            "two`noun`", 2,
            "three`noun`", 3,
            "four`noun`", 4,
            "five`noun`", 5,
            "six`noun`", 6,
            "seven`noun`", 7,
            "eight`noun`", 8,
            "nine`noun`", 9,
            "ten`noun`", 10,
            "eleven`noun`", 11,
            "twelve`noun`", 12,
            "thirteen`noun`", 13,
            "fourteen`noun`", 14,
            "fifteen`noun`", 15,
            "sixteen`noun`", 16,
            "seventeen`noun`", 17,
            "eighteen`noun`", 18,
            "nineteen`noun`", 19,
            "twenty`noun`", 20
    ),
            numberAdjectives = CaseInsensitiveOrderedMap.with(
                    "zero`adj`", 0,
                    "one`adj`", 1,
                    "two`adj`", 2,
                    "three`adj`", 3,
                    "four`adj`", 4,
                    "five`adj`", 5,
                    "six`adj`", 6,
                    "seven`adj`", 7,
                    "eight`adj`", 8,
                    "nine`adj`", 9,
                    "ten`adj`", 10,
                    "eleven`adj`", 11,
                    "twelve`adj`", 12,
                    "thirteen`adj`", 13,
                    "fourteen`adj`", 14,
                    "fifteen`adj`", 15,
                    "sixteen`adj`", 16,
                    "seventeen`adj`", 17,
                    "eighteen`adj`", 18,
                    "nineteen`adj`", 19,
                    "twenty`adj`", 20
            );

    /**
     * Thesaurus preset that changes all text to sound like this speaker: "Desaurus preset dat changez all text to sound
     * like dis speakah." You may be familiar with a certain sci-fi game that has orks who sound like this.
     */
    public static Thesaurus ORK = new Thesaurus("WAAAAAGH!");

    /**
     * Thesaurus preset that sharply reduces the used letters to only: a, b, g, h, m, n, r, and z. This apparently is
     * the canonical set of letters that zombies can use? This will abuse the rules of proper English spelling, using r
     * and h as vowels, kind-of, in addition to a.
     */
    public static Thesaurus ZOMBIE = new Thesaurus("zrrmbrh grh BRRRNZ!");

    static {
        ORK.alterations.add(new Language.Alteration("\\bth", "d"));
        ORK.alterations.add(new Language.Alteration("th", "dd"));
        ORK.alterations.add(new Language.Alteration("er\\b", "ah"));
        ORK.alterations.add(new Language.Alteration("es\\b", "ez"));
        ORK.addReplacement("the", "da")
                .addReplacement("their", "deyr")
                .addReplacement("yes", "ya")
                .addReplacement("your", "youse")
                .addReplacement("yours", "youses")
                .addReplacement("going", "gon'")
                .addReplacement("and", "an'")
                .addReplacement("to", "*snort*")
                .addReplacement("rhythm", "riddim")
                .addReplacement("get", "git")
                .addReplacement("good", "gud");

        ZOMBIE.alterations.add(new Language.Alteration("(?[[\\p{L}]-[A-Za-z]])", "r"));
        ZOMBIE.alterations.add(new Language.Alteration("[pcst]h", "h"));
        ZOMBIE.alterations.add(new Language.Alteration("[eiu]([tckhd]+)", "rh"));
        ZOMBIE.alterations.add(new Language.Alteration("[aei][aeiy]", "aa"));
        ZOMBIE.alterations.add(new Language.Alteration("o[hw]", "ah"));
        ZOMBIE.alterations.add(new Language.Alteration("n[cdtkq]", "nh"));
        ZOMBIE.alterations.add(new Language.Alteration("m\\b", "mm"));
        ZOMBIE.alterations.add(new Language.Alteration("[td]+", "bh"));
        ZOMBIE.alterations.add(new Language.Alteration("[ckq]+", "gh"));
        ZOMBIE.alterations.add(new Language.Alteration("[ai]", "a"));
        ZOMBIE.alterations.add(new Language.Alteration("[bvp]", "b"));
        ZOMBIE.alterations.add(new Language.Alteration("[jg]", "g"));
        ZOMBIE.alterations.add(new Language.Alteration("[fwyh]", "h"));
        ZOMBIE.alterations.add(new Language.Alteration("[m]", "m"));
        ZOMBIE.alterations.add(new Language.Alteration("[n]", "n"));
        ZOMBIE.alterations.add(new Language.Alteration("[lreou]", "r"));
        ZOMBIE.alterations.add(new Language.Alteration("[sxz]", "z"));
        ZOMBIE.alterations.add(new Language.Alteration("([ahmnrz])\\b", "$1$1", 0.2));
        ZOMBIE.alterations.add(new Language.Alteration("(\\b\\w+\\b)", "${!1}", 0.125));
    }
    static {

        // not related to ORK; this filters out synonyms that aren't in the appropriate list
        Iterator<CharSequence> it = adjective.keySet().iterator();
        while (it.hasNext()){
            if(!StringTools.contains(it.next(), "`adj`"))
                it.remove();
        }
        it = noun.keySet().iterator();
        while (it.hasNext()){
            if(!StringTools.contains(it.next(), "`noun`"))
                it.remove();
        }
        it = nouns.keySet().iterator();
        while (it.hasNext()){
            if(!StringTools.contains(it.next(), "`nouns`"))
                it.remove();
        }
    }

//    /**
//     * Gets a stable (large) String that stores all categories this version of Thesaurus knows, as well as all of the
//     * words each category includes. This can be useful in conjunction with {@link #addArchivedCategories(String)} to
//     * load a set of categories stored from an earlier version of SquidLib.
//     * @return a category archive String that can be passed to {@link #addArchivedCategories(String)}
//     */
//    public static String archiveCategories(){
//        return Converters.convertOrderedMap(
//                        Converters.convertString,
//                        Converters.convertObjectList(Converters.convertString)
//                ).stringify(categories);
//    }

    /**
     * Gets a stable (large) String that stores all categories this version of Thesaurus knows, as well as all of the
     * words each category includes. This can be useful in conjunction with
     * {@link #addArchivedCategoriesAlternate(String)} to load a set of categories stored from an earlier version of
     * SquidLib, but its intended purpose is to provide a simpler format for archival that doesn't use ObText. The code
     * for ObText is in SquidLib 3.x, but so far is not in SquidSquad, and is unlikely to be added here.
     * <br>
     * This yields a String that describes an OrderedMap of String keys (categories) mapped to ArrayList of String
     * values (the words each category knows). Each key-value pair is one line, typically separated by {@code "\n"} (but
     * because various OS properties can affect line endings, {@code "\r\n"} must also be handled). Items within the
     * line are separated by the non-breaking space character, Unicode 00A0. The first item on a line is the key, or the
     * category name. Remaining items are words the category knows. This may produce a trailing newline.
     * @return a category archive String that can be passed to {@link #addArchivedCategoriesAlternate(String)}
     */
    public static String archiveCategoriesAlternate(){
        StringBuilder sb = new StringBuilder(8192);
        for (int i = 0, n = categories.size(); i < n; i++) {
            sb.append(categories.keyAt(i)).append('\u00A0').append(StringTools.join("\u00A0", categories.getAt(i))).append('\n');
        }
        return sb.toString();
    }

}
