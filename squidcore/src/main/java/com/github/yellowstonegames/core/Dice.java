package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import regexodus.Matcher;
import regexodus.Pattern;

import javax.annotation.Nonnull;

/**
 * Class for emulating various traditional RPG-style dice rolls.
 * Supports rolling multiple virtual dice of arbitrary size, summing all, the highest <i>n</i>, or the lowest <i>n</i>
 * dice, treating dice as "exploding" as in some tabletop games (where the max result is rolled again and added),
 * getting value from inside a range, and applying simple arithmetic modifiers to the result (like adding a number).
 * Typically you'll want to use the {@link #roll(String)} method if you have a String like {@code "2d8+6"}, or the
 * various other methods if you have int variables for things like "number of dice to roll" and "sides on each die."
 * <br>
 * Based on code from the Blacken library.
 *
 * @author yam655
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 */
public class Dice {
    
    // The Creature.
    private static final Matcher mat = Pattern.compile("\\s*(?:({=op}[+/*-])?\\s*({=sn}-?\\d+)?\\s*(?:({=im}[:><])\\s*({=mn}\\d+))?\\s*(?:({=mm}[d:!])\\s*({=en}\\d+))?)\\s*").matcher();
    private EnhancedRandom rng;
    private transient IntList temp = new IntList(20);
    private transient Rule tempRule = new Rule();
    /**
     * Creates a new dice roller that uses a random RNG seed for an RNG that it owns.
     */
    public Dice() {
        rng = new LaserRandom();
    }

    /**
     * Creates a new dice roller that uses the given EnhancedRandom, which can be seeded before it's given here. The EnhancedRandom will be
     * shared, not copied, so requesting a random number from the same EnhancedRandom in another place may change the value of the
     * next die roll this makes, and dice rolls this makes will change the state of the shared EnhancedRandom.
     * @param rng an EnhancedRandom, such as {@link LaserRandom}; will be shared (dice rolls will change the IRNG state outside here)
     */
    public Dice(EnhancedRandom rng)
    {
        this.rng = rng;
    }

    /**
     * Creates a new dice roller that will use its own LaserRandom as its RNG, seeded with the given seed.
     * @param seed a long to use as a seed for a new LaserRandom (can also be an int, short, or byte)
     */
    public Dice(long seed)
    {
        rng = new LaserRandom(seed);
    }
    /**
     * Creates a new dice roller that will use its own LaserRandom as its RNG, seeded with a hash of the
     * given String seed. This can take any CharSequence, not just String, and uses {@link Hasher#dantalion}
     * and {@link Hasher#decarabia_} to hash the seed.
     * @param seed a String or other CharSequence to use as a seed for a new LaserRandom
     */
    public Dice(CharSequence seed)
    {
        rng = new LaserRandom(Hasher.dantalion.hash64(seed), Hasher.decarabia_.hash64(seed));
    }
    /**
     * Sets the random number generator to be used.
     *
     * This method does not need to be called before using the methods of this
     * class.
     *
     * @param rng an EnhancedRandom as the source of randomness
     */
    public void setRandom(@Nonnull EnhancedRandom rng) {
        this.rng = rng;
    }

    /**
     * Rolls the given number of dice with the given number of sides and returns
     * the total of the best n dice.
     *
     * @param n number of best dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of best n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int bestOf(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollDice(1, sides));
        }

        return bestOf(rolls, temp);

    }

    /**
     * Rolls the given number of exploding dice with the given number of sides and returns
     * the total of the best n dice (counting a die that explodes as one die).
     *
     * @param n number of best dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of best n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int bestOfExploding(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollExplodingDice(1, sides));
        }
        return bestOf(rolls, temp);
    }

    /**
     * Rolls the given number of dice with the given number of sides and returns
     * the total of the lowest n dice.
     *
     * @param n number of worst dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of worst n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int worstOf(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollDice(1, sides));
        }
        return worstOf(rolls, temp);
    }

    /**
     * Rolls the given number of exploding dice with the given number of sides and returns
     * the total of the lowest n dice (counting a die that explodes as one die).
     *
     * @param n number of worst dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of worst n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int worstOfExploding(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollExplodingDice(1, sides));
        }
        return worstOf(rolls, temp);
    }

    /**
     * Totals the highest n numbers in the pool.
     *
     * @param n the number of dice to be totaled
     * @param pool the dice to pick from
     * @return the sum
     */
    private int bestOf(int n, IntList pool) {
        int rolls = Math.min(n, pool.size());
        pool.sort();

        int ret = 0;
        for (int i = pool.size() - 1, r = 0;  r < rolls && i >= 0; i--, r++) {
            ret += pool.get(i);
        }
        return ret;
    }

    /**
     * Totals the lowest n numbers in the pool.
     *
     * @param n the number of dice to be totaled
     * @param pool the dice to pick from
     * @return the sum
     */
    private int worstOf(int n, IntList pool) {
        int rolls = Math.min(n, pool.size());
        pool.sort();

        int ret = 0;
        for (int r = 0;  r < rolls; r++) {
            ret += pool.get(r);
        }
        return ret;
    }

    /**
     * Find the best n totals from the provided number of dice rolled according
     * to the roll group string.
     *
     * @param n number of roll groups to total
     * @param dice number of roll groups to roll
     * @param group string encoded roll grouping
     * @return the sum
     */
    public int bestOf(int n, int dice, String group) {
        int rolls = Math.min(n, dice);
        temp.clear();

        for (int i = 0; i < dice; i++) {
            temp.add(roll(group));
        }

        return bestOf(rolls, temp);
    }

    /**
     * Find the worst n totals from the provided number of dice rolled according
     * to the roll group string.
     *
     * @param n number of roll groups to total
     * @param dice number of roll groups to roll
     * @param group string encoded roll grouping
     * @return the sum
     */
    public int worstOf(int n, int dice, String group) {
        int rolls = Math.min(n, dice);
        temp.clear();

        for (int i = 0; i < dice; i++) {
            temp.add(roll(group));
        }

        return worstOf(rolls, temp);
    }

    /**
     * Emulate a dice roll and return the sum.
     *
     * @param n number of dice to sum
     * @param sides positive integer; number of sides on the rolled dice
     * @return sum of rolled dice
     */
    public int rollDice(int n, int sides) {
        int ret = 0;
        for (int i = 0; i < n; i++) {
            ret += rng.nextInt(sides) + 1;
        }
        return ret;
    }

    /**
     * Emulate an exploding dice roll and return the sum.
     *
     * @param n number of dice to sum
     * @param sides number of sides on the rollDice; should be greater than 1
     * @return sum of rollDice
     */
    public int rollExplodingDice(int n, int sides) {
        int ret = 0, curr;
        if(sides <= 1) return n; // avoid infinite loop, act like they can't explode
        for (int i = 0; i < n;) {
            ret += (curr = rng.nextInt(sides) + 1);
            if(curr != sides) i++;
        }
        return ret;
    }

    /**
     * Get a list of the independent results of n rolls of dice with the given
     * number of sides.
     *
     * @param n number of dice used
     * @param sides positive integer; number of sides on each die
     * @return list of results
     */
    public IntList independentRolls(int n, int sides) {
        IntList ret = new IntList(n);
        for (int i = 0; i < n; i++) {
            ret.add(rng.nextInt(sides) + 1);
        }
        return ret;
    }

    /**
     * Evaluate the String {@code rollCode} as dice roll notation and roll to get a random result of that dice roll.
     * You should consider whether you want to parse the rollCode every time (which can be computationally costly); if
     * you make a lot of similar dice rolls, you can use {@link #parseRollRuleInto(Rule, String)} to get a reusable
     * rule for rolls.
     * <br>
     * This can handle a good amount of dice
     * terminology. One of the more frequent uses is rolling some amount of dice and summing their values, which can be
     * done with e.g. "4d10" to roll four ten-sided dice and add up their results. You can choose to sum only some of
     * the dice, either the "n highest" or "n lowest" values in a group, with "3&gt;4d6" to sum the three greatest-value
     * dice in four rolls of six-sided dice, or "2&lt;3d8" to sum the two lowest-value dice in three rolls of
     * eight-sided dice. You can apply modifiers to these results, such as "1d20+7" to roll one twenty-sided die and add
     * 7 to its result. These modifiers can be other dice, such as "1d10-1d6", and while multiplication and division are
     * supported, order of operations isn't, so it just rolls dice from left to right and applies operators it finds
     * along the way. You can get a random value in an inclusive range with "50:100", which is equivalent to "1d51+49"
     * but is easier to read and understand. You can treat dice as "exploding," where any dice that get the maximum
     * result are rolled again and added to the total along with the previous maximum result. As an example, if two
     * exploding six-sided dice are rolled, and their results are 3 and 6, then because 6 is the maximum value it is
     * rolled again and added to the earlier rolls; if the additional roll is a 5, then the sum is 3 + 6 + 5 (for a
     * total of 14), but if the additional roll was a 6, then it would be rolled again and added again, potentially many
     * times if 6 is rolled continually. Some players may be familiar with this game mechanic from various tabletop
     * games, but many potential players might not be, so it should be explained if you show the kinds of dice being
     * rolled to players. The syntax used for exploding dice replaces the "d" in "3d6" for normal dice with "!", making
     * "3!6" for three six-sided exploding dice. Inclusive ranges are not supported with best-of and worst-of notation,
     * but exploding dice are. If using a range, the upper bound can be random, decided by dice rolls such as with
     * "1:6d6" (which rolls six 6-sided dice and uses that as the upper bound of the range) or by other ranges such as
     * with "10:100:200", which gets a random number between 100 and 200, then returns a random number between 10 and
     * that. While it is technically allowed to end a dice string with an operator, the partial operator will be
     * ignored. If you start a dice string with an operator, its left-hand-side will always be 0. If you have two
     * operators in a row, only the last will be used, unless one is '-' and can be treated as part of a negative number
     * (this allows "1d20 * -3" to work). Whitespace is allowed between most parts of a dice string.
     * <br>
     * The following notation is supported:
     * <ul>
     *     <li>{@code 42} : simple absolute string; can start with {@code -} to make it negative</li>
     *     <li>{@code 3d6} : sum of 3 6-sided dice</li>
     *     <li>{@code d6} : synonym for {@code 1d6}</li>
     *     <li>{@code 3>4d6} : best 3 of 4 6-sided dice</li>
     *     <li>{@code 3:4d6} : gets a random value between 3 and a roll of {@code 4d6}; this syntax has changed</li>
     *     <li>{@code 2<5d6} : worst 2 of 5 6-sided dice</li>
     *     <li>{@code 10:20} : simple random range (inclusive between 10 and 20)</li>
     *     <li>{@code :20} : synonym for {@code 0:20}</li>
     *     <li>{@code 3!6} : sum of 3 "exploding" 6-sided dice; see above for the semantics of "exploding" dice</li>
     *     <li>{@code !6} : synonym for {@code 1!6}</li>
     * </ul>
     * The following types of operators are supported:
     * <ul>
     *     <li>{@code +4} : add 4 to the value</li>
     *     <li>{@code -3} : subtract 3 from the value</li>
     *     <li>{@code *100} : multiply value by 100</li>
     *     <li>{@code /8} : integer-divide value by 8</li>
     * </ul>
     * @param rollCode dice string using the above notation
     * @return a random number that is possible with the given dice string
     */
    public int roll(String rollCode) {
        return runRollRule(tempRule.reset(rollCode));
    }

    /**
     * Parses the String {@code rollCode} as dice roll notation and returns instructions as a Rule so the roll can
     * be performed later with {@link #runRollRule(Rule)}. This method allocates a new Rule every time, which may
     * not be optimal; consider reusing a Rule with {@link #parseRollRuleInto(Rule, String)}.
     * <br>
     * This effectively allows storing instructions for how to roll a
     * particular set of dice, but leaves the actual roll for later. By storing the instructions instead of parsing them
     * every time, this can save quite a bit of effort for dice-roll-heavy games.
     * <br>
     * This can handle a good amount of dice
     * terminology. One of the more frequent uses is rolling some amount of dice and summing their values, which can be
     * done with e.g. "4d10" to roll four ten-sided dice and add up their results. You can choose to sum only some of
     * the dice, either the "n highest" or "n lowest" values in a group, with "3&gt;4d6" to sum the three greatest-value
     * dice in four rolls of six-sided dice, or "2&lt;3d8" to sum the two lowest-value dice in three rolls of
     * eight-sided dice. You can apply modifiers to these results, such as "1d20+7" to roll one twenty-sided die and add
     * 7 to its result. These modifiers can be other dice, such as "1d10-1d6", and while multiplication and division are
     * supported, order of operations isn't, so it just rolls dice from left to right and applies operators it finds
     * along the way. You can get a random value in an inclusive range with "50:100", which is equivalent to "1d51+49"
     * but is easier to read and understand. You can treat dice as "exploding," where any dice that get the maximum
     * result are rolled again and added to the total along with the previous maximum result. As an example, if two
     * exploding six-sided dice are rolled, and their results are 3 and 6, then because 6 is the maximum value it is
     * rolled again and added to the earlier rolls; if the additional roll is a 5, then the sum is 3 + 6 + 5 (for a
     * total of 14), but if the additional roll was a 6, then it would be rolled again and added again, potentially many
     * times if 6 is rolled continually. Some players may be familiar with this game mechanic from various tabletop
     * games, but many potential players might not be, so it should be explained if you show the kinds of dice being
     * rolled to players. The syntax used for exploding dice replaces the "d" in "3d6" for normal dice with "!", making
     * "3!6" for three six-sided exploding dice. Inclusive ranges are not supported with best-of and worst-of notation,
     * but exploding dice are. If using a range, the upper bound can be random, decided by dice rolls such as with
     * "1:6d6" (which rolls six 6-sided dice and uses that as the upper bound of the range) or by other ranges such as
     * with "10:100:200", which gets a random number between 100 and 200, then returns a random number between 10 and
     * that. While it is technically allowed to end a dice string with an operator, the partial operator will be
     * ignored. If you start a dice string with an operator, its left-hand-side will always be 0. If you have two
     * operators in a row, only the last will be used, unless one is '-' and can be treated as part of a negative number
     * (this allows "1d20 * -3" to work). Whitespace is allowed between most parts of a dice string.
     * <br>
     * The following notation is supported:
     * <ul>
     *     <li>{@code 42} : simple absolute string; can start with {@code -} to make it negative</li>
     *     <li>{@code 3d6} : sum of 3 6-sided dice</li>
     *     <li>{@code d6} : synonym for {@code 1d6}</li>
     *     <li>{@code 3>4d6} : best 3 of 4 6-sided dice</li>
     *     <li>{@code 3:4d6} : gets a random value between 3 and a roll of {@code 4d6}; this syntax has changed</li>
     *     <li>{@code 2<5d6} : worst 2 of 5 6-sided dice</li>
     *     <li>{@code 10:20} : simple random range (inclusive between 10 and 20)</li>
     *     <li>{@code :20} : synonym for {@code 0:20}</li>
     *     <li>{@code 3!6} : sum of 3 "exploding" 6-sided dice; see above for the semantics of "exploding" dice</li>
     *     <li>{@code !6} : synonym for {@code 1!6}</li>
     * </ul>
     * The following types of operators are supported:
     * <ul>
     *     <li>{@code +4} : add 4 to the value</li>
     *     <li>{@code -3} : subtract 3 from the value</li>
     *     <li>{@code *100} : multiply value by 100</li>
     *     <li>{@code /8} : integer-divide value by 8</li>
     * </ul>
     *
     * @param rollCode dice string using the above notation
     * @return a roll rule that can be run with {@link #runRollRule(Rule)}
     */
    public static Rule parseRollRule(String rollCode){
        return new Rule(rollCode);
    }
    /**
     * Parses the String {@code rollCode} as dice roll notation and appends instructions into {@code into} so rolls can
     * be performed later with {@link #runRollRule(Rule)}. This method does not clear {@code into}, so you should
     * clear it yourself if you don't want to save its contents (or it didn't store a roll rule). You can append to an
     * existing roll rule, which is about the same as adding a {@code +} between the two roll codes and parsing that.
     * <br>
     * This is the main way of using the Dice class. This effectively allows storing instructions for how to roll a
     * particular set of dice, but leaves the actual roll for later. By storing the instructions instead of parsing them
     * every time, this can save quite a bit of effort for dice-roll-heavy games.
     * <br>
     * This can handle a good amount of dice
     * terminology. One of the more frequent uses is rolling some amount of dice and summing their values, which can be
     * done with e.g. "4d10" to roll four ten-sided dice and add up their results. You can choose to sum only some of
     * the dice, either the "n highest" or "n lowest" values in a group, with "3&gt;4d6" to sum the three greatest-value
     * dice in four rolls of six-sided dice, or "2&lt;3d8" to sum the two lowest-value dice in three rolls of
     * eight-sided dice. You can apply modifiers to these results, such as "1d20+7" to roll one twenty-sided die and add
     * 7 to its result. These modifiers can be other dice, such as "1d10-1d6", and while multiplication and division are
     * supported, order of operations isn't, so it just rolls dice from left to right and applies operators it finds
     * along the way. You can get a random value in an inclusive range with "50:100", which is equivalent to "1d51+49"
     * but is easier to read and understand. You can treat dice as "exploding," where any dice that get the maximum
     * result are rolled again and added to the total along with the previous maximum result. As an example, if two
     * exploding six-sided dice are rolled, and their results are 3 and 6, then because 6 is the maximum value it is
     * rolled again and added to the earlier rolls; if the additional roll is a 5, then the sum is 3 + 6 + 5 (for a
     * total of 14), but if the additional roll was a 6, then it would be rolled again and added again, potentially many
     * times if 6 is rolled continually. Some players may be familiar with this game mechanic from various tabletop
     * games, but many potential players might not be, so it should be explained if you show the kinds of dice being
     * rolled to players. The syntax used for exploding dice replaces the "d" in "3d6" for normal dice with "!", making
     * "3!6" for three six-sided exploding dice. Inclusive ranges are not supported with best-of and worst-of notation,
     * but exploding dice are. If using a range, the upper bound can be random, decided by dice rolls such as with
     * "1:6d6" (which rolls six 6-sided dice and uses that as the upper bound of the range) or by other ranges such as
     * with "10:100:200", which gets a random number between 100 and 200, then returns a random number between 10 and
     * that. While it is technically allowed to end a dice string with an operator, the partial operator will be
     * ignored. If you start a dice string with an operator, its left-hand-side will always be 0. If you have two
     * operators in a row, only the last will be used, unless one is '-' and can be treated as part of a negative number
     * (this allows "1d20 * -3" to work). Whitespace is allowed between most parts of a dice string.
     * <br>
     * The following notation is supported:
     * <ul>
     *     <li>{@code 42} : simple absolute string; can start with {@code -} to make it negative</li>
     *     <li>{@code 3d6} : sum of 3 6-sided dice</li>
     *     <li>{@code d6} : synonym for {@code 1d6}</li>
     *     <li>{@code 3>4d6} : best 3 of 4 6-sided dice</li>
     *     <li>{@code 3:4d6} : gets a random value between 3 and a roll of {@code 4d6}; this syntax has changed</li>
     *     <li>{@code 2<5d6} : worst 2 of 5 6-sided dice</li>
     *     <li>{@code 10:20} : simple random range (inclusive between 10 and 20)</li>
     *     <li>{@code :20} : synonym for {@code 0:20}</li>
     *     <li>{@code 3!6} : sum of 3 "exploding" 6-sided dice; see above for the semantics of "exploding" dice</li>
     *     <li>{@code !6} : synonym for {@code 1!6}</li>
     * </ul>
     * The following types of operators are supported:
     * <ul>
     *     <li>{@code +4} : add 4 to the value</li>
     *     <li>{@code -3} : subtract 3 from the value</li>
     *     <li>{@code *100} : multiply value by 100</li>
     *     <li>{@code /8} : integer-divide value by 8</li>
     * </ul>
     *
     * @param into a Rule that this will append to, placing instructions for how to perform a roll
     * @param rollCode dice string using the above notation
     * @return a roll rule that can be run with {@link #runRollRule(Rule)}
     */
    public static Rule parseRollRuleInto(Rule into, String rollCode){
        into.rollCode = rollCode;
        mat.setTarget(rollCode);
        int op = mat.pattern().groupId("op");
        int sn = mat.pattern().groupId("sn");
        int im = mat.pattern().groupId("im");
        int mn = mat.pattern().groupId("mn");
        int mm = mat.pattern().groupId("mm");
        int en = mat.pattern().groupId("en");
        boolean starting = true;
        while (mat.find()) {
            System.out.println(mat.group());
            if(starting && !mat.isCaptured(op)){
                into.instructions.add('+');
                starting = false;
            }
            if(mat.isCaptured("op")) // math op
            {
                into.instructions.add(mat.charAt(0, op)); // gets char 0 from the math op group
                starting = false;
            }

            boolean startNum = mat.isCaptured(sn); // number constant
            int initialMode = mat.isCaptured(im) ? mat.charAt(0, im) : 0; // between, best, or worst notation
            boolean midNum = mat.isCaptured(mn); // number constant
            int mainMode = mat.isCaptured(mm) ? mat.charAt(0, mm) : 0; // dice, range, or explode
            boolean endNum = mat.isCaptured(en); // number constant
            if(!(startNum || midNum || endNum))
                break;
            int startN = startNum ? DigitTools.intFromDec(rollCode, mat.start(sn), mat.end(sn)) : 0;
            int midN = midNum ? DigitTools.intFromDec(rollCode, mat.start(mn), mat.end(mn)) : 0;
            if(!startNum && endNum && mainMode != ':')
                midN = 1;
            int endN = endNum ? DigitTools.intFromDec(rollCode, mat.start(en), mat.end(en)) : 0;

            into.instructions.add(startN);
            into.instructions.add(initialMode);
            into.instructions.add(midN);
            into.instructions.add(mainMode);
            into.instructions.add(endN);
        }
        return into;
    }

    /**
     * Performs a roll of the given {@code rule}, which should store instructions from
     * {@link #parseRollRule(String)} or {@link #parseRollRuleInto(Rule, String)}. Two rolls of the same roll rule
     * have no guarantee of having the same or different result, just that they will use the same dice and operations.
     * @param rule a Rule generated by {@link #parseRollRule(String)} or {@link #parseRollRuleInto(Rule, String)}
     * @return the result of rolling the dice as instructed by rollRule
     */
    public int runRollRule(Rule rule) {
        if(rule == null || rule.instructions.isEmpty()) return 0;
        IntList rollRule = rule.instructions;
        int currentResult, previousTotal = 0;
        for (int i = 0; i < rollRule.size(); i+=6) {
            int currentMode = rollRule.get(i);
            currentResult = 0;


            int startN = rollRule.get(i+1); // number constant
            int midMode = rollRule.get(i+2); // between notation
            int midN = rollRule.get(i+3); // number constant
            int mainMode = rollRule.get(i+4); // dice, range, or explode
            int endN = rollRule.get(i+5); // number constant

            if (mainMode != 0) {
                if (midMode != 0) {
                    if ('>' == midMode) {
                        if ('d' == (mainMode)) {
                            currentResult = bestOf(startN, midN, endN);
                        }
                        else if('!' == (mainMode))
                        {
                            currentResult = bestOfExploding(startN, midN, endN);
                        }
                    }
                    else if('<' == midMode)
                    {
                        if ('d' == (mainMode)) {
                            currentResult = worstOf(startN, midN, endN);
                        }
                        else if('!' == (mainMode))
                        {
                            currentResult = worstOfExploding(startN, midN, endN);
                        }
                    }
                    else
                    // Here, midMode is ":", there is a constant lower bound for the range, and the upper bound is some
                    // dice roll or other range. This can be negative, easily, if the random upper bound is negative
                    {
                        if ('d' == (mainMode)) {
                            currentResult = startN + rng.nextSignedInt(rollDice(midN, endN) + 1 - startN);
                        } else if ('!' == (mainMode)) {
                            currentResult = startN + rng.nextSignedInt(rollExplodingDice(midN, endN) + 1 - startN);
                        } else if (':' == (mainMode)) {
                            currentResult = startN + rng.nextSignedInt(midN + rng.nextSignedInt(endN + 1 - midN) + 1 - startN);
                        }
                    }
                } else if ('d' == (mainMode)) {
                    currentResult = rollDice(startN, endN);
                } else if ('!' == (mainMode)) {
                    currentResult = rollExplodingDice(startN, endN);
                } else if (':' == (mainMode)) {
                    currentResult = startN + rng.nextSignedInt(endN + 1 - startN);
                }
            } else {
                if (':' == (midMode)) {
                    currentResult = startN + rng.nextSignedInt(midN + 1 - startN);
                } else {
                    currentResult = startN;
                }
            }
            switch (currentMode)
            {
                case '-':
                    previousTotal -= currentResult;
                    break;
                case '*':
                    previousTotal *= currentResult;
                    break;
                case '/':
                    previousTotal /= currentResult;
                    break;
                default:
                    previousTotal += currentResult;
                    break;
            }
        }
        return previousTotal;
    }

    public static class Rule {
        public String rollCode;
        public @Nonnull IntList instructions;

        protected Rule(){
            rollCode = "";
            instructions = new IntList(10);
        }

        /**
         * Stores the given {@code rollCode} as-is, and parses instructions using {@link Dice#parseRollRule(String)}.
         * See the Dice method for more info.
         * @param rollCode a dice string using the notation described in {@link Dice#parseRollRule(String)}
         */
        public Rule(@Nonnull String rollCode){
            instructions = new IntList(10);
            Dice.parseRollRuleInto(this, rollCode);
        }

        /**
         * Resets this Rile to be empty, with "" as its rollCode and empty instructions.
         * @return this, for chaining
         */
        public Rule reset(){
            rollCode = "";
            instructions.clear();
            return this;
        }
        /**
         * Resets this Rule to store the given {@code rollCode} as-is, and parses instructions using
         * {@link Dice#parseRollRuleInto(Rule, String)}. See the Dice method for more info.
         * @param rollCode a dice string using the notation described in {@link Dice#parseRollRuleInto(Rule, String)}
         * @return this, for chaining
         */
        public Rule reset(@Nonnull String rollCode){
            instructions.clear();
            Dice.parseRollRuleInto(this, rollCode);
            return this;
        }

        /**
         * Returns a string representation of the rollCode.
         *
         * @return a string representation of the rollCode.
         */
        @Override
        public String toString() {
            return rollCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Rule rule = (Rule) o;

            return instructions.equals(rule.instructions);
        }

        @Override
        public int hashCode() {
            return instructions.hashCode();
        }
    }
}
