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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.hasItem;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.util.SuppressForbidden;
import org.apache.poi.util.Internal;

/**
 * Util class for POI JUnit TestCases, which provide additional features 
 */
@Internal
public final class POITestCase {

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
        assertTrue("Unable to find expected text '" + needle + "' in text:\n" + haystack,
                hay.contains(n)
        );
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
        if (map.containsKey(key)) {
            return;
        }
        fail("Unable to find " + key + " in " + map);
    }

    public static <T> void assertNotContained(Set<T> set, T element) {
        assertThat(set, not(hasItem(element)));
        /*if (set.contains(element)) {
            fail("Set should not contain " + element);
        }*/
    }
     
    /**
     * Utility method to get the value of a private/protected field.
     * Only use this method in test cases!!!
     */
    public static <R,T> R getFieldValue(final Class<? super T> clazz, final T instance, final Class<R> fieldType, final String fieldName) {
        assertTrue("Reflection of private fields is only allowed for POI classes.", clazz.getName().startsWith("org.apache.poi."));
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<R>() {
                @Override
                @SuppressWarnings("unchecked")
                @SuppressForbidden("For test usage only")
                public R run() throws Exception {
                    Field f = clazz.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return (R) f.get(instance);
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException("Cannot access field '" + fieldName + "' of class " + clazz, pae.getException());
        }
    }
     
    /**
     * Utility method to call a private/protected method.
     * Only use this method in test cases!!!
     */
    public static <R,T> R callMethod(final Class<? super T> clazz, final T instance, final Class<R> returnType, final String methodName,
        final Class<?>[] parameterTypes, final Object[] parameters) {
        assertTrue("Reflection of private methods is only allowed for POI classes.", clazz.getName().startsWith("org.apache.poi."));
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<R>() {
                @Override
                @SuppressWarnings("unchecked")
                @SuppressForbidden("For test usage only")
                public R run() throws Exception {
                    Method m = clazz.getDeclaredMethod(methodName, parameterTypes);
                    m.setAccessible(true);
                    return (R) m.invoke(instance, parameters);
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException("Cannot access method '" + methodName + "' of class " + clazz, pae.getException());
        }
    }

    /**
     * Utility method to shallow compare all fields of the objects
     * Only use this method in test cases!!!
     */
    public static void assertReflectEquals(final Object expected, Object actual) throws Exception {
        final List<Field> fields;
        try {
            fields = AccessController.doPrivileged(new PrivilegedExceptionAction<List<Field>>() {
                @Override
                @SuppressForbidden("Test only")
                public List<Field> run() throws Exception {
                    List<Field> flds = new ArrayList<>();
                    for (Class<?> c = expected.getClass(); c != null; c = c.getSuperclass()) {
                        Field[] fs = c.getDeclaredFields();
                        AccessibleObject.setAccessible(fs, true);                        
                        for (Field f : fs) {
                            // JaCoCo Code Coverage adds it's own field, don't look at this one here
                            if(f.getName().equals("$jacocoData")) {
                                continue;
                            }
                            
                            flds.add(f);
                        }
                    }
                    return flds;
                }
            });
        } catch (PrivilegedActionException pae) {
            throw pae.getException();
        }
        
        for (Field f : fields) {
            Class<?> t = f.getType();
            if (t.isArray()) {
                if (Object[].class.isAssignableFrom(t)) {
                    assertArrayEquals((Object[])f.get(expected), (Object[])f.get(actual));
                } else if (byte[].class.isAssignableFrom(t)) {
                    assertArrayEquals((byte[])f.get(expected), (byte[])f.get(actual));
                } else {
                    fail("Array type is not yet implemented ... add it!");
                }
            } else {
                assertEquals(f.get(expected), f.get(actual));
            }
        }
    }
    
    /**
     * Rather than adding {@literal @}Ignore to known-failing tests,
     * write the test so that it notifies us if it starts passing.
     * This is useful for closing related or forgotten bugs.
     * 
     * An Example:
     * <code><pre>
     * public static int add(int a, int b) {
     *     // a known bug in behavior that has not been fixed yet
     *     raise UnsupportedOperationException("add");
     * }
     * 
     * {@literal @}Test
     * public void knownFailingUnitTest() {
     *     try {
     *         assertEquals(2, add(1,1));
     *         // this test fails because the assumption that this bug had not been fixed is false
     *         testPassesNow(12345);
     *     } catch (UnsupportedOperationException e) {
     *         // test is skipped because the assumption that this bug had not been fixed is true
     *         skipTest(e);
     *     }
     * }
     * 
     * Once passing, this unit test can be rewritten as:
     * {@literal @}Test
     * public void knownPassingUnitTest() {
     *     assertEquals(2, add(1,1));
     * }
     * 
     * If you have a better idea how to simplify test code while still notifying
     * us when a previous known-failing test now passes, please improve these.
     * As a bonus, a known-failing test that fails should not be counted as a
     * passing test.
     * 
     * One possible alternative is to expect the known exception, but without
     * a clear message that it is a good thing to no longer get the expected
     * exception once the test passes.
     * {@literal @}Test(expected=UnsupportedOperationException.class)
     * public void knownFailingUnitTest() {
     *     assertEquals(2, add(1,1));
     * }
     *
     * @param e  the exception that was caught that will no longer
     * be raised when the bug is fixed 
     */
    public static void skipTest(Throwable e) {
        assumeTrue("This test currently fails with " + e, false);
    }
    /**
     * @see #skipTest(Throwable)
     *
     * @param bug  the bug number corresponding to a known bug in bugzilla
     */
    public static void testPassesNow(int bug) {
        fail("This test passes now. Please update the unit test and bug " + bug + ".");
    }
    
    public static void assertBetween(String message, int value, int min, int max) {
        assertTrue(message + ": " + value + " is less than the minimum value of " + min,
                min <= value);
        assertTrue(message + ": " + value + " is greater than the maximum value of " + max,
                value <= max);
    }
    public static void assertStrictlyBetween(String message, int value, int min, int max) {
        assertTrue(message + ": " + value + " is less than or equal to the minimum value of " + min,
                min < value);
        assertTrue(message + ": " + value + " is greater than or equal to the maximum value of " + max,
                value < max);
    }

}
