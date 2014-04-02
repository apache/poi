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


import java.util.Collection;

import junit.framework.TestCase;

/**
 * Parent class for POI JUnit TestCases, which provide additional
 *  features 
 */
public class POITestCase extends TestCase {
    public static void assertContains(String haystack, String needle) {
        assertTrue(
              "Unable to find expected text '" + needle + "' in text:\n" + haystack,
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
}
