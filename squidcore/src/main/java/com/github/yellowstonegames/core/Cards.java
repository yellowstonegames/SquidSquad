package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.annotations.Beta;

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

    public String nameForInt(int index) {
        if(index < 0 || index >= names.length) return null;
        return names[index];
    }
}
