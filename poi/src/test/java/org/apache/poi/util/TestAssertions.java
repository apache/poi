/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;

import org.junit.jupiter.api.Assertions;

/**
 * Utility class providing additional JUnit Jupiter-style assertions for use
 * across all POI test modules.
 */
@Internal
public final class TestAssertions {

    private TestAssertions() {
    }

    /**
     * Asserts that {@code testString} contains {@code expectedContains} as a substring.
     * On failure, throws an {@link org.opentest4j.AssertionFailedError} with the full
     * values of both arguments included in the error message, similar to
     * {@code assertEquals}.
     *
     * @param testString       the string to search within
     * @param expectedContains the substring that must be present
     */
    public static void assertContains(String testString, String expectedContains) {
        if (testString == null || expectedContains == null || !testString.contains(expectedContains)) {
            Assertions.fail("expected: <" + testString + "> to contain: <" + expectedContains + ">");
        }
    }
}
