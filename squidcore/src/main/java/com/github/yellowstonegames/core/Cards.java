package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.annotations.Beta;

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
    public IntDeque deck;
    public EnhancedRandom random;
    public String[] names;

    public Cards(String[] names, EnhancedRandom random) {
        deck = new IntDeque(ArrayTools.range(names.length));
        this.random = random;
        this.names = names;
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

    public int drawInt() {
        if(deck.isEmpty())
            shuffleDeck(true);
        return deck.pop();
    }

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

    public int remainingCards() {
        return deck.size();
    }
}
