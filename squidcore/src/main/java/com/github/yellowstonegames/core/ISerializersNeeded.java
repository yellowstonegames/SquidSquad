/*
 * Copyright (c) 2026; see AUTHORS file.
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

import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.List;

/**
 * A single-method interface that allows getting the {@link Class}es that must be registered with a serialization
 * library for an object of this type to be serialized successfully. This is meant to help register classes for Fory
 * serialization, but the same classes are typically needed for Kryo serialization. There is an exception for (1D)
 * primitive array and String array types, which Fory doesn't need registered, but Kryo does if one such type is given
 * by {@link #getSerializersNeeded()} here. This is an instance method because individual objects may sometimes need
 * different classes registered, e.g. {@link com.github.tommyettinger.random.EnhancedRandom} may need to be registered
 * along with the concrete subclass of EnhancedRandom that the current object uses.
 */
public interface ISerializersNeeded {
    /**
     * Gets a List of Class instances that must each be registered with a serialization library before this object can
     * be successfully serialized or deserialized. This is {@link GwtIncompatible}; none of the serialization libraries
     * this is meant for have any support for GWT.
     * @return a List of Class instances that must each be registered with a serialization library
     */
    @GwtIncompatible
    List<Class<?>> getSerializersNeeded();
}
