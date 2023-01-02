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

package com.github.yellowstonegames.core;

/**
 * Allows access to an int identifier for an object, which is often but not necessarily unique.
 * You can generate identifiers however you wish, but a simple counter is probably easiest and fastest, plus it is
 * guaranteed to produce all possible identifiers without duplicates before cycling.
 */
@FunctionalInterface
public interface IIdentified {
    /**
     * Gets the identifier for this object, as an int.
     * @return the int identifier for this
     */
    int getIdentifier();
}
