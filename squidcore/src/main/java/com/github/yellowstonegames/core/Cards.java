package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.Xoshiro256MX3Random;
import com.github.yellowstonegames.core.annotations.Beta;

import java.util.Arrays;

/**
 * A simple class to simulate a deck of cards that can be shuffled or drawn from (without replacement). If the deck is
 * empty and the user tries to draw a card, the deck is shuffled entirely and then a card is drawn. You can draw a card
 * simulated by an int index with {@link #drawInt()}, or get a String name for a card drawn with {@link #drawName()}.
 * <br>
 * An array of String names must be provided (currently) for each card's name, such as "Ace of Spades" or "The Chariot"
 * (for a game like poker, and for tarot, respectively). You can get a name from an int retrieved earlier with
 * {@link #nameForInt(int)}, or just retrieve the name directly with {@link #drawName()}.
 */
@Beta
public class Cards {

    /**
     * Predefined common types of card decks, stored by the names of cards (permitting duplicates).
     */
    public enum DeckType {
        /**
         * The 22 major arcana cards in many tarot decks, typically used for fortune-telling and symbolism.
         * These are sometimes numbered; the first card, The Fool, is numbered 0 or 22, and the rest usually have
         * numbers equivalent to their indices here.
         */
        TAROT_MAJOR_ARCANA(
                "The Fool", "The Magician", "The High Priestess", "The Empress",
                "The Emperor", "The Hierophant", "The Lovers", "The Chariot",
                "Strength", "The Hermit", "Wheel of Fortune", "Justice",
                "The Hanged Man", "Death", "Temperance", "The Devil",
                "The Tower", "The Star", "The Moon", "The Sun",
                "Judgement", "The World"
        ),
        /**
         * The 56 minor arcana cards in the Rider-Waite tarot deck, used for both fortune-telling and card games.
         * These are divided into four suits, which often have different names in different decks: Swords, Cups, Wands,
         * and Pentacles. There are 14 values per suit, starting with the Ace, then Two through Ten, then Page, Knight,
         * Queen, and King. The Page and Knight also sometimes have different names.
         * <br>
         * You can get the index of the suit by taking an int index and using {@code index % 4} or {@code (index & 3)}
         * to get a suit from 0 to 3 (Swords, Cups, Wands, then Pentacles) for a card.
         * You can get the numerical value for a card index by dividing the index by 4, or using {@code (index >> 2)},
         * then adding 1; this produces 1 for Aces, 11 for Pages, 12 for Knights, 13 for Queens, and 14 for Kings.
         */
        TAROT_MINOR_ARCANA(
                "Ace of Swords", "Ace of Cups", "Ace of Wands", "Ace of Pentacles",
                "Two of Swords", "Two of Cups", "Two of Wands", "Two of Pentacles",
                "Three of Swords", "Three of Cups", "Three of Wands", "Three of Pentacles",
                "Four of Swords", "Four of Cups", "Four of Wands", "Four of Pentacles",
                "Five of Swords", "Five of Cups", "Five of Wands", "Five of Pentacles",
                "Six of Swords", "Six of Cups", "Six of Wands", "Six of Pentacles",
                "Seven of Swords", "Seven of Cups", "Seven of Wands", "Seven of Pentacles",
                "Eight of Swords", "Eight of Cups", "Eight of Wands", "Eight of Pentacles",
                "Nine of Swords", "Nine of Cups", "Nine of Wands", "Nine of Pentacles",
                "Ten of Swords", "Ten of Cups", "Ten of Wands", "Ten of Pentacles",
                "Page of Swords", "Page of Cups", "Page of Wands", "Page of Pentacles",
                "Knight of Swords", "Knight of Cups", "Knight of Wands", "Knight of Pentacles",
                "Queen of Swords", "Queen of Cups", "Queen of Wands", "Queen of Pentacles",
                "King of Swords", "King of Cups", "King of Wands", "King of Pentacles"
        ),
        /**
         * A "standard" type of deck with four suits and 13 possible values per suit.
         * You can get the index of the suit by taking an int index and using {@code index % 4} or {@code (index & 3)}
         * to get a suit from 0 to 3 (Spades, Hearts, Clubs, then Diamonds) for a card.
         * You can get the color of a card's suit by taking an int index and using {@code index % 2} or
         * {@code (index & 1)}; 0 is black, 1 is red.
         * You can get the numerical value for a card index by dividing the index by 4, or using {@code (index >> 2)},
         * then adding 1; this produces 1 for Aces, 11 for Jacks, 12 for Queens, and 13 for Kings.
         */
        FRENCH_52(
                "Ace of Spades", "Ace of Hearts", "Ace of Clubs", "Ace of Diamonds",
                "Two of Spades", "Two of Hearts", "Two of Clubs", "Two of Diamonds",
                "Three of Spades", "Three of Hearts", "Three of Clubs", "Three of Diamonds",
                "Four of Spades", "Four of Hearts", "Four of Clubs", "Four of Diamonds",
                "Five of Spades", "Five of Hearts", "Five of Clubs", "Five of Diamonds",
                "Six of Spades", "Six of Hearts", "Six of Clubs", "Six of Diamonds",
                "Seven of Spades", "Seven of Hearts", "Seven of Clubs", "Seven of Diamonds",
                "Eight of Spades", "Eight of Hearts", "Eight of Clubs", "Eight of Diamonds",
                "Nine of Spades", "Nine of Hearts", "Nine of Clubs", "Nine of Diamonds",
                "Ten of Spades", "Ten of Hearts", "Ten of Clubs", "Ten of Diamonds",
                "Jack of Spades", "Jack of Hearts", "Jack of Clubs", "Jack of Diamonds",
                "Queen of Spades", "Queen of Hearts", "Queen of Clubs", "Queen of Diamonds",
                "King of Spades", "King of Hearts", "King of Clubs", "King of Diamonds"
        ),
        /**
         * A "standard" type of deck with four suits and 13 possible values per suit, plus one Joker card.
         * You can get the index of the suit by taking an int index and using {@code index % 4} or {@code (index & 3)}
         * to get a suit from 0 to 3 (Spades, Hearts, Clubs, then Diamonds) for a card, which is suit 0 for the Joker.
         * You can get the numerical value for a card index by dividing the index by 4, or using {@code (index >> 2)},
         * then adding 1; this produces 1 for Aces, 11 for Jacks, 12 for Queens, 13 for Kings, and 14 for the Joker.
         */
        FRENCH_52_WITH_JOKER(
                "Ace of Spades", "Ace of Hearts", "Ace of Clubs", "Ace of Diamonds",
                "Two of Spades", "Two of Hearts", "Two of Clubs", "Two of Diamonds",
                "Three of Spades", "Three of Hearts", "Three of Clubs", "Three of Diamonds",
                "Four of Spades", "Four of Hearts", "Four of Clubs", "Four of Diamonds",
                "Five of Spades", "Five of Hearts", "Five of Clubs", "Five of Diamonds",
                "Six of Spades", "Six of Hearts", "Six of Clubs", "Six of Diamonds",
                "Seven of Spades", "Seven of Hearts", "Seven of Clubs", "Seven of Diamonds",
                "Eight of Spades", "Eight of Hearts", "Eight of Clubs", "Eight of Diamonds",
                "Nine of Spades", "Nine of Hearts", "Nine of Clubs", "Nine of Diamonds",
                "Ten of Spades", "Ten of Hearts", "Ten of Clubs", "Ten of Diamonds",
                "Jack of Spades", "Jack of Hearts", "Jack of Clubs", "Jack of Diamonds",
                "Queen of Spades", "Queen of Hearts", "Queen of Clubs", "Queen of Diamonds",
                "King of Spades", "King of Hearts", "King of Clubs", "King of Diamonds",
                "Joker"
        ),
        /**
         * A "standard" type of deck with four suits and 13 possible values per suit, plus two Joker cards (one black
         * and one red).
         * You can get the index of the suit by taking an int index and using {@code index % 4} or {@code (index & 3)}
         * to get a suit from 0 to 3 (Spades, Hearts, Clubs, then Diamonds) for a card, which is suit 0 for the Black
         * Joker and suit 1 for the Red Joker.
         * You can get the color of a card's suit by taking an int index and using {@code index % 2} or
         * {@code (index & 1)}; 0 is black, 1 is red. This is also true for jokers, which don't exactly have a suit.
         * You can get the numerical value for a card index by dividing the index by 4, or using {@code (index >> 2)},
         * then adding 1; this produces 1 for Aces, 11 for Jacks, 12 for Queens, 13 for Kings, and 14 for either Joker.
         */
        FRENCH_52_WITH_2_JOKERS(
                "Ace of Spades", "Ace of Hearts", "Ace of Clubs", "Ace of Diamonds",
                "Two of Spades", "Two of Hearts", "Two of Clubs", "Two of Diamonds",
                "Three of Spades", "Three of Hearts", "Three of Clubs", "Three of Diamonds",
                "Four of Spades", "Four of Hearts", "Four of Clubs", "Four of Diamonds",
                "Five of Spades", "Five of Hearts", "Five of Clubs", "Five of Diamonds",
                "Six of Spades", "Six of Hearts", "Six of Clubs", "Six of Diamonds",
                "Seven of Spades", "Seven of Hearts", "Seven of Clubs", "Seven of Diamonds",
                "Eight of Spades", "Eight of Hearts", "Eight of Clubs", "Eight of Diamonds",
                "Nine of Spades", "Nine of Hearts", "Nine of Clubs", "Nine of Diamonds",
                "Ten of Spades", "Ten of Hearts", "Ten of Clubs", "Ten of Diamonds",
                "Jack of Spades", "Jack of Hearts", "Jack of Clubs", "Jack of Diamonds",
                "Queen of Spades", "Queen of Hearts", "Queen of Clubs", "Queen of Diamonds",
                "King of Spades", "King of Hearts", "King of Clubs", "King of Diamonds",
                "Black Joker", "Red Joker"
        ),
        ;

        private final String[] names;
        DeckType(String... names){
            this.names = names;
        }

        /**
         * Returns the size of the deck of cards before any cards have been drawn.
         * @return the size of the deck of cards before any cards have been drawn
         */
        public int size() {
            return names.length;
        }

        /**
         * Tries to get the card with the given index in this DeckType, if the index is valid. Returns null if
         * the index is negative or too large.
         * @param index should be non-negative and less than {@link #size()}
         * @return the card name with the given index
         */
        public String get(int index) {
            if(index < 0 || index > names.length) return null;
            return names[index];
        }

        /**
         * Gets a copy of the array of String names this uses.
         * @return a copy of the array of String names this uses
         */
        public String[] getNames() {
            return Arrays.copyOf(names, names.length);
        }
    }

    public IntDeque deck;
    public EnhancedRandom random;
    public String[] names;

    /**
     * Creates a new Cards using the given array of names (which will be used directly, permitting the name of a card
     * to potentially be changed later if the array is modified) and an {@link EnhancedRandom} generator.
     * <br>
     * If the {@link EnhancedRandom#getMinimumPeriod()} of the generator is at least equal to the
     * {@link com.github.tommyettinger.digital.MathTools#factorial(double)} of {@code names.length}, then the
     * generator will (in theory) be able to produce all possible shuffles of that deck. A generator such as
     * {@link com.github.tommyettinger.random.OrbitalRandom} with a period of 2 to the 71 or greater can produce all
     * shuffles for the 22 tarot major arcana. Larger decks might need as much as a period of 2 to the
     * 256 for a typical playing card deck (with or without jokers). CCG-type decks with 60 or more cards likely can't
     * have all shuffles produced by a single generator currently, though the amount of possible shuffles for even the
     * 22 tarot major arcana cards is astronomically large already.
     * @param names a String array of names for cards, which do not need to be unique; used directly (not copied)
     * @param random an EnhancedRandom generator that will be used directly (not copied)
     */
    public Cards(String[] names, EnhancedRandom random) {
        deck = new IntDeque(ArrayTools.range(names.length));
        this.random = random;
        this.names = names;
    }

    /**
     * Creates a new Cards using the specified {@link DeckType} and the given {@link EnhancedRandom} generator.
     * <br>
     * If the {@link EnhancedRandom#getMinimumPeriod()} of the generator is at least equal to the
     * {@link com.github.tommyettinger.digital.MathTools#factorial(double)} of {@link DeckType#size()}, then the
     * generator will (in theory) be able to produce all possible shuffles of that deck type. A generator such as
     * {@link com.github.tommyettinger.random.OrbitalRandom} with a period of 2 to the 71 or greater can produce all
     * shuffles for {@link DeckType#TAROT_MAJOR_ARCANA}. Larger decks might need as much as a period of 2 to the
     * 256 for a typical playing card deck (with or without jokers). CCG-type decks with 60 or more cards likely can't
     * have all shuffles produced by a single generator currently, though the amount of possible shuffles for even the
     * 22 tarot major arcana cards is astronomically large already.
     *
     * @param type a predefined {@link DeckType} enum constant
     */
    public Cards(DeckType type, EnhancedRandom random) {
        this(Arrays.copyOf(type.names, type.names.length), random);
    }

    /**
     * Creates a new Cards using the specified {@link DeckType} and an unseeded {@link Xoshiro256MX3Random} generator,
     * which is guaranteed to be able to produce all possible shuffles of an up-to-57-card deck.
     * @param type a predefined {@link DeckType} enum constant
     */
    public Cards(DeckType type) {
        this(type, new Xoshiro256MX3Random());
    }

    /**
     * If {@code full} is true, shuffles the entire deck back in to restart it from its original size in a new order.
     * Otherwise, this only shuffles the remaining cards in the deck into a new order.
     * @param full if true, the deck will start again at its original size; if false, only the remaining cards will be shuffled
     */
    public void shuffleDeck(boolean full) {
        if(full){
            deck.clear();
            for (int i = 0, n = names.length; i < n; i++) {
                deck.add(i);
            }
        }
        deck.shuffle(random);
    }

    /**
     * If the deck has any remaining cards, this removes the last card from the deck and returns its index.
     * Otherwise, this shuffles the entire deck back up to its original size, and then removes the last card and
     * returns its index.
     * @return an index of a card in the deck
     */
    public int drawInt() {
        if(deck.isEmpty())
            shuffleDeck(true);
        return deck.pop();
    }

    /**
     * If the deck has any remaining cards, this removes the last card from the deck and returns its name.
     * Otherwise, this shuffles the entire deck back up to its original size, and then removes the last card and
     * returns its name.
     * @return a String name of a card in the deck
     */
    public String drawName() {
        return names[drawInt()];
    }

    /**
     * @param index typically returned by {@link #drawInt()}
     * @return the name associated with {@code index}, or null if the index is invalid.
     */
    public String nameForInt(int index) {
        if(index < 0 || index >= names.length) return null;
        return names[index];
    }

    /**
     * Gets how many cards are left in the deck, before drawing a card would need to shuffle.
     * @return how many cards are left in the deck
     */
    public int remainingCards() {
        return deck.size();
    }
}
