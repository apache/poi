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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * A class for testing the POI Junit TestCase utility class
 */
public final class TestPOITestCase {
    @Test
    public void assertStartsWith() {
        POITestCase.assertStartsWith("Apache POI", "");
        POITestCase.assertStartsWith("Apache POI", "Apache");
        POITestCase.assertStartsWith("Apache POI", "Apache POI");
    }

    @Test
    public void assertEndsWith() {
        POITestCase.assertEndsWith("Apache POI", "");
        POITestCase.assertEndsWith("Apache POI", "POI");
        POITestCase.assertEndsWith("Apache POI", "Apache POI");
    }
    
    @Test
    public void assertContains() {
        POITestCase.assertContains("There is a needle in this haystack", "needle");
        /*try {
            POITestCase.assertContains("There is gold in this haystack", "needle");
            fail("found a needle");
        } catch (final junit.framework.AssertionFailedError e) {
            // expected
        }*/
    }

    @Test
    public void assertContainsIgnoreCase_Locale() {
        POITestCase.assertContainsIgnoreCase("There is a Needle in this haystack", "needlE", Locale.ROOT);
        // FIXME: test failing case
    }

    @Test
    public void assertContainsIgnoreCase() {
        POITestCase.assertContainsIgnoreCase("There is a Needle in this haystack", "needlE");
        // FIXME: test failing case
    }

    @Test
    public void assertNotContained() {
        POITestCase.assertNotContained("There is a needle in this haystack", "gold");
        // FIXME: test failing case
    }

    @Test
    public void assertMapContains() {
        Map<String, String> haystack = Collections.singletonMap("needle", "value");
        POITestCase.assertContains(haystack, "needle");
        // FIXME: test failing case
    }

     
    /**
     * Utility method to get the value of a private/protected field.
     * Only use this method in test cases!!!
     */
    @Ignore
    @Test
    public void getFieldValue() {
        /*
        final Class<? super T> clazz;
        final T instance;
        final Class<R> fieldType;
        final String fieldName;

        final R expected;
        final R actual = POITestCase.getFieldValue(clazz, instance, fieldType, fieldName);
        assertEquals(expected, actual);
        */
    }
     
    /**
     * Utility method to call a private/protected method.
     * Only use this method in test cases!!!
     */
    @Ignore
    @Test
    public void callMethod() {
        /*
        final Class<? super T> clazz;
        final T instance;
        final Class<R> returnType;
        final String methodName;
        final Class<?>[] parameterTypes;
        final Object[] parameters;

        final R expected;
        final R actual = POITestCase.callMethod(clazz, instance, returnType, methodName, parameterTypes, parameters);
        assertEquals(expected, actual);
        */
    }

    /**
     * Utility method to shallow compare all fields of the objects
     * Only use this method in test cases!!!
     */
    @Ignore
    @Test
    public void assertReflectEquals() throws Exception {
        /*
        final Object expected;
        final Object actual;
        POITestCase.assertReflectEquals(expected, actual);
        */
    }
}
