/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.old.v300;

/**
 * Intended for compatibility with StatefulRandomness from SquidLib 3.x, this should be applied to LegacyRandom
 * implementations that have one {@code long} value or two {@code int} values for state, and allow freely getting and
 * setting those states. Optimally, all states should be allowed, but this doesn't have to be the case as long as
 * {@link #setState(long)} can correct an invalid state and set the state accordingly.
 */
public interface StatefulRandomness {
    long getState();
    void setState(long state);
}
