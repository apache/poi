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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.poi.util.Internal;
import org.apache.poi.util.SuppressForbidden;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

/**
 * Util class for POI JUnit TestCases, which provide additional features
 */
@Internal
@SuppressWarnings("java:S2187")
public final class POITestCase {

    private POITestCase() {
    }

    public static void assertStartsWith(String string, String prefix) {
        assertNotNull(string);
        assertNotNull(prefix);
        assertThat(string, startsWith(prefix));
    }

    public static void assertStartsWith(String message, String string, String prefix) {
        assertNotNull(message, string);
        assertNotNull(message, prefix);
        assertThat(message, string, startsWith(prefix));
    }

    public static void assertEndsWith(String string, String suffix) {
        assertNotNull(string);
        assertNotNull(suffix);
        assertThat(string, endsWith(suffix));
    }

    public static void assertContains(String haystack, String needle) {
        assertNotNull(haystack);
        assertNotNull(needle);
        assertThat(haystack, containsString(needle));
    }

    public static void assertContains(String message, String haystack, String needle) {
        assertNotNull(message, haystack);
        assertNotNull(message, needle);
        assertThat(message, haystack, containsString(needle));
    }

    public static void assertContainsIgnoreCase(String haystack, String needle, Locale locale) {
        assertNotNull(haystack);
        assertNotNull(needle);
        String hay = haystack.toLowerCase(locale);
        String n = needle.toLowerCase(locale);
        assertTrue(hay.contains(n), "Unable to find expected text '" + needle + "' in text:\n" + haystack);
    }
    public static void assertContainsIgnoreCase(String haystack, String needle) {
        assertContainsIgnoreCase(haystack, needle, Locale.ROOT);
    }

    public static void assertNotContained(String haystack, String needle) {
        assertNotNull(haystack);
        assertNotNull(needle);
        assertThat(haystack, not(containsString(needle)));
    }

    /**
     * @param map haystack
     * @param key needle
     */
    public static  <T> void assertContains(Map<T, ?> map, T key) {
        assertTrue(map.containsKey(key), "Unable to find " + key + " in " + map);
    }

    public static <T> void assertNotContained(Set<T> set, T element) {
        assertThat("Set should not contain " + element, set, not(hasItem(element)));
    }

    /**
     * Utility method to get the value of a private/protected field.
     * Only use this method in test cases!!!
     */
    @SuppressWarnings({"unused", "unchecked"})
    @SuppressForbidden("For test usage only")
    public static <R,T> R getFieldValue(final Class<? super T> clazz, final T instance, final Class<R> fieldType, final String fieldName) {
        assertTrue(clazz.getName().startsWith("org.apache.poi."), "Reflection of private fields is only allowed for POI classes.");
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<R>) () -> {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return (R) f.get(instance);
            });
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException("Cannot access field '" + fieldName + "' of class " + clazz, pae.getException());
        }
    }

    /**
     * Utility method to shallow compare all fields of the objects
     * Only use this method in test cases!!!
     */
    public static void assertReflectEquals(final Object expected, Object actual) {
        // as long as ReflectionEquals is provided by Mockito, use it ... otherwise use commons.lang for the tests

        // JaCoCo Code Coverage adds its own field, don't look at this one here
        assertTrue(new ReflectionEquals(expected, "$jacocoData").matches(actual));
    }


    /**
     * Ensures that the temporary directory is defined and exists and
     * ensures ImageIO uses this directory for cache-files
     */
    public static void setImageIOCacheDir() {
        final String tmpDirProperty = System.getProperty("java.io.tmpdir");
        if(tmpDirProperty == null || "".equals(tmpDirProperty)) {
            return;
        }
        // ensure that temp-dir exists because ImageIO requires it
        File tmpDir = new File(tmpDirProperty);
        if(!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IllegalStateException("Could not create temporary directory " + tmpDirProperty + ", full path " + tmpDir.getAbsolutePath());
        }
        ImageIO.setCacheDirectory(tmpDir);
    }

    /**
     * Returns the major version of Java as simple integer, i.e.
     * "8" for JDK 1.8, "11" for JDK 11, "12" for JDK 12 and so on.
     *
     * @return The major version of Java
     */
    public static int getJDKVersion() {
        return Integer.parseInt(System.getProperty("java.version").replaceAll("^(?:1\\.)?(\\d+).*", "$1"));
    }
}
