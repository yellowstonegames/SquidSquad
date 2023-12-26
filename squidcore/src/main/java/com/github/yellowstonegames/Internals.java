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

package com.github.yellowstonegames;

import com.github.yellowstonegames.core.annotations.GwtIncompatible;

/**
 * As much as I hate package-private code, this interface is really just meant to be used by tests, and shouldn't even
 * appear in GWT's list of files to translate. This should never be used from production code.
 * <br>
 * For tests to use this at all, they need to be in the {@code com.github.yellowstonegames} package. It is suggested
 * for test modules to create a public class in that package, implementing Internals and doing very little else. You
 * can check its {@link #PRINTING} constant to see if printing is appropriate for tests in that module.
 */
@GwtIncompatible
interface Internals {
    /**
     * To be used by tests. If true, tests can print as much as they please; if false and a test relies on printing
     * sample values rather than assertions, then that test should terminate before printing. This value is meant to be
     * edited in the Internals class to affect all tests' printing status.
     */
    boolean PRINTING = true;
}
