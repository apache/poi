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

package org.apache.poi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.property.PropertyTable;
import org.junit.jupiter.api.Test;

/**
 * A class for testing the POI Junit TestCase utility class
 */
final class TestPOITestCase {
    @Test
    void assertStartsWith() {
        POITestCase.assertStartsWith("Apache POI", "");
        POITestCase.assertStartsWith("Apache POI", "Apache");
        POITestCase.assertStartsWith("Apache POI", "Apache POI");
    }

    @Test
    void assertEndsWith() {
        POITestCase.assertEndsWith("Apache POI", "");
        POITestCase.assertEndsWith("Apache POI", "POI");
        POITestCase.assertEndsWith("Apache POI", "Apache POI");
    }

    @Test
    void assertContains() {
        POITestCase.assertContains("There is a needle in this haystack", "needle");
    }

    @Test
    void assertContainsIgnoreCase_Locale() {
        POITestCase.assertContainsIgnoreCase("There is a Needle in this haystack", "needlE", Locale.ROOT);
    }

    @Test
    void assertContainsIgnoreCase() {
        POITestCase.assertContainsIgnoreCase("There is a Needle in this haystack", "needlE");
    }

    @Test
    void assertNotContained() {
        POITestCase.assertNotContained("There is a needle in this haystack", "gold");
    }

    @Test
    void assertMapContains() {
        Map<String, String> haystack = Collections.singletonMap("needle", "value");
        POITestCase.assertContains(haystack, "needle");
    }


    /**
     * Utility method to get the value of a private/protected field.
     * Only use this method in test cases!!!
     */
    @Test
    void getFieldValue() throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            PropertyTable actual = POITestCase.getFieldValue(POIFSFileSystem.class, fs, PropertyTable.class, "_property_table");
            assertNotNull(actual);
        }
    }
}
