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

package org.apache.poi.hsmf.datatypes;

import java.util.Collection;

import junit.framework.TestCase;

/**
 * Checks various MAPIProperty related logic
 */
public final class TestMAPIProperty extends TestCase {
   public void testGet() throws Exception {
      assertEquals(MAPIProperty.DISPLAY_NAME, MAPIProperty.get(MAPIProperty.DISPLAY_NAME.id));
      assertEquals(MAPIProperty.DISPLAY_BCC, MAPIProperty.get(MAPIProperty.DISPLAY_BCC.id));
      assertNotSame(MAPIProperty.DISPLAY_BCC, MAPIProperty.get(MAPIProperty.DISPLAY_CC.id));
   }
   
   public void testGetAll() throws Exception {
      Collection<MAPIProperty> all = MAPIProperty.getAll();
      assertEquals(true, all.contains(MAPIProperty.DISPLAY_NAME));
      assertEquals(true, all.contains(MAPIProperty.DISPLAY_CC));
      
      // Won't contain custom
      assertEquals(false, all.contains(MAPIProperty.createCustom(1, Types.UNSPECIFIED, "")));
      
      // Won't contain unknown
      assertEquals(false, all.contains(MAPIProperty.UNKNOWN));
   }
   
   public void testCustom() throws Exception {
      MAPIProperty c1 = MAPIProperty.createCustom(1, Types.UNSPECIFIED, "");
      MAPIProperty c2a = MAPIProperty.createCustom(2, Types.UNSPECIFIED, "2");
      MAPIProperty c2b = MAPIProperty.createCustom(2, Types.UNSPECIFIED, "2");
      
      // New object each time
      assertNotSame(c1, c2a);
      assertNotSame(c1, c2b);
      assertNotSame(c2a, c2b);
      
      // Won't be in all list
      Collection<MAPIProperty> all = MAPIProperty.getAll();
      assertEquals(false, all.contains(c1));
      assertEquals(false, all.contains(c2a));
      assertEquals(false, all.contains(c2b));
   }
}
