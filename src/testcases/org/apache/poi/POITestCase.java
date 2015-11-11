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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;

import org.apache.poi.util.SuppressForbidden;

/**
 * Parent class for POI JUnit TestCases, which provide additional
 *  features 
 */
public class POITestCase {
    public static void assertContains(String haystack, String needle) {
        assertTrue(
              "Unable to find expected text '" + needle + "' in text:\n" + haystack,
              haystack.contains(needle)
        );
     }
    public static void assertNotContained(String haystack, String needle) {
        assertFalse(
              "Unexpectedly found text '" + needle + "' in text:\n" + haystack,
              haystack.contains(needle)
        );
     }
    
    public static <T> void assertContains(T needle, T[] haystack)
    {
       // Check
       for (T thing : haystack) {
          if (thing.equals(needle)) {
             return;
          }
       }

       // Failed, try to build a nice error
       StringBuilder sb = new StringBuilder();
       sb.append("Unable to find ").append(needle).append(" in [");
       for (T thing : haystack) {
           sb.append(" ").append(thing.toString()).append(" ,");
        }
        sb.setCharAt(sb.length()-1, ']');

        fail(sb.toString());
     }
    
     public static  <T> void assertContains(T needle, Collection<T> haystack) {
        if (haystack.contains(needle)) {
           return;
        }
        fail("Unable to find " + needle + " in " + haystack);
     }
     
     /** Utility method to get the value of a private/protected field.
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
     
     /** Utility method to call a private/protected method.
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
