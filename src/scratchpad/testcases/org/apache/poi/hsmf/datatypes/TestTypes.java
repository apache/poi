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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Verifies that the Types class is behaving properly.
 * Also check that no changes have been made that will
 *  break the library.
 */
public final class TestTypes {
   @Test
   void testTypeIds() {
      assertEquals(0x1e, Types.ASCII_STRING.getId());
      assertEquals(0x1f, Types.UNICODE_STRING.getId());

      assertEquals(0x0102, Types.BINARY.getId());
      assertEquals(0x000B, Types.BOOLEAN.getId());
      assertEquals(0x0003, Types.LONG.getId());
      assertEquals(0x0040, Types.TIME.getId());

      assertEquals(Types.ASCII_STRING, Types.getById(0x1e));
      assertEquals(Types.UNICODE_STRING, Types.getById(0x1f));

      assertEquals(Types.BINARY, Types.getById(0x0102));
      assertEquals(Types.BOOLEAN, Types.getById(0x000B));
      assertEquals(Types.LONG, Types.getById(0x0003));
      assertEquals(Types.TIME, Types.getById(0x0040));
   }

   @Test
   void testTypeFormatting() {
      assertEquals("0000", Types.asFileEnding(0x0000));
      assertEquals("0020", Types.asFileEnding(0x0020));
      assertEquals("0102", Types.asFileEnding(0x0102));
      assertEquals("FEDC", Types.asFileEnding(0xfedc));
   }

   @Test
   void testName() {
      assertEquals("ASCII String", Types.ASCII_STRING.getName());
      assertEquals("Boolean", Types.BOOLEAN.getName());
   }
}
