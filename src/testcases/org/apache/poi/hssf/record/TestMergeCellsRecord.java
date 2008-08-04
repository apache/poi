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

package org.apache.poi.hssf.record;

import junit.framework.TestCase;

import org.apache.poi.hssf.util.CellRangeAddress;

/**
 * Make sure the merge cells record behaves
 * @author Danny Mui (dmui at apache dot org)
 *
 */
public final class TestMergeCellsRecord extends TestCase {
   
   /**
    * Make sure when a clone is called, we actually clone it.
    * @throws Exception
    */
   public void testCloneReferences() throws Exception {
      MergeCellsRecord merge = new MergeCellsRecord();
      merge.addArea(0, (short)0, 1, (short)2);
      MergeCellsRecord clone = (MergeCellsRecord)merge.clone();
      
      assertNotSame("Merged and cloned objects are the same", merge, clone);
      
      CellRangeAddress mergeRegion = merge.getAreaAt(0);
      CellRangeAddress cloneRegion = clone.getAreaAt(0);
      assertNotSame("Should not point to same objects when cloning", mergeRegion, cloneRegion);
      assertEquals("New Clone Row From doesnt match", mergeRegion.getFirstRow(), cloneRegion.getFirstRow());
      assertEquals("New Clone Row To doesnt match", mergeRegion.getLastRow(), cloneRegion.getLastRow());
      assertEquals("New Clone Col From doesnt match", mergeRegion.getFirstColumn(), cloneRegion.getFirstColumn());
      assertEquals("New Clone Col To doesnt match", mergeRegion.getLastColumn(), cloneRegion.getLastColumn());      
      
      merge.removeAreaAt(0);
      assertNotNull("Clone's item not removed", clone.getAreaAt(0));
   }
}
