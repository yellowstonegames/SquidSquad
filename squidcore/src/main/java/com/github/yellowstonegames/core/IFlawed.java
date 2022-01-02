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

package com.github.yellowstonegames.core;

/**
 * An empty marker interface to indicate that an implementor has known or intentional issues with a key property of its
 * functionality. This is almost always combined with another interface, and used to indicate that a class that
 * implements both IFlawed and a "normal" interface implements the second one abnormally. Typically, you would use a
 * flawed implementation to compare with a non-flawed one, or because the flaws have aesthetic merit from their biases.
 */
public interface IFlawed {
}
