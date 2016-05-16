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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import org.apache.poi.util.SuppressForbidden;

/**
 * Util class for POI JUnit TestCases, which provide additional features 
 */
public final class POITestCase {
    public static void assertContains(String haystack, String needle) {
        assertNotNull(haystack);
        assertTrue(
              "Unable to find expected text '" + needle + "' in text:\n" + haystack,
              haystack.contains(needle)
        );
    }
    
    public static void assertNotContained(String haystack, String needle) {
        assertNotNull(haystack);
        assertFalse(
              "Unexpectedly found text '" + needle + "' in text:\n" + haystack,
              haystack.contains(needle)
        );
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
}
