package com.github.yellowstonegames.path.sg.utils;

import java.util.function.Consumer;

import com.github.yellowstonegames.path.sg.algorithms.SearchStep;

public interface SearchProcessor<V> extends Consumer<SearchStep<V>> {



}
