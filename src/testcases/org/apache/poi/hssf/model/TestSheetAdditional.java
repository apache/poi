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

package org.apache.poi.hssf.model;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.ColumnInfoRecord;

/**
 * @author Tony Poppleton
 */
public final class TestSheetAdditional extends TestCase {
	
	public void testGetCellWidth() {
		Sheet sheet = Sheet.createSheet();
		ColumnInfoRecord nci = new ColumnInfoRecord();

		// Prepare test model
		nci.setFirstColumn((short)5);
		nci.setLastColumn((short)10);
		nci.setColumnWidth((short)100);
		
		
		sheet._columnInfos.insertColumn(nci);

		assertEquals((short)100,sheet.getColumnWidth((short)5));
		assertEquals((short)100,sheet.getColumnWidth((short)6));
		assertEquals((short)100,sheet.getColumnWidth((short)7));
		assertEquals((short)100,sheet.getColumnWidth((short)8));
		assertEquals((short)100,sheet.getColumnWidth((short)9));
		assertEquals((short)100,sheet.getColumnWidth((short)10));

		sheet.setColumnWidth((short)6,(short)200);

		assertEquals((short)100,sheet.getColumnWidth((short)5));
		assertEquals((short)200,sheet.getColumnWidth((short)6));
		assertEquals((short)100,sheet.getColumnWidth((short)7));
		assertEquals((short)100,sheet.getColumnWidth((short)8));
		assertEquals((short)100,sheet.getColumnWidth((short)9));
		assertEquals((short)100,sheet.getColumnWidth((short)10));
	}

    public void testMaxColumnWidth() {
        Sheet sheet = Sheet.createSheet();
        sheet.setColumnWidth(0, 255*256); //the limit
        try {
            sheet.setColumnWidth(0, 256*256); //the limit
            fail("expected exception");
        } catch (Exception e){
            ;
        }
    }
}
