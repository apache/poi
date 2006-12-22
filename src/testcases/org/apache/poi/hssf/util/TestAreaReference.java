
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
        

package org.apache.poi.hssf.util;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class TestAreaReference extends TestCase {
     public TestAreaReference(String s) {
        super(s);
    }
    public void testAreaRef1() {
        AreaReference ar = new AreaReference("$A$1:$B$2");
        assertTrue("Two cells expected",ar.getCells().length == 2);
        CellReference cf = ar.getCells()[0];
        assertTrue("row is 4",cf.getRow()==0);
        assertTrue("col is 1",cf.getCol()==0);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $A$1",cf.toString().equals("$A$1"));
        
        cf = ar.getCells()[1];
        assertTrue("row is 4",cf.getRow()==1);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $B$2",cf.toString().equals("$B$2"));
    }
    
    /**
     * References failed when sheet names were being used
     * Reported by Arne.Clauss@gedas.de
     */
    public void testReferenceWithSheet() {
    	String ref = "Tabelle1!$B$5";
		AreaReference myAreaReference = new AreaReference(ref);
		CellReference[] myCellReference = myAreaReference.getCells();
		
		assertNotNull("cell reference not null : "+myCellReference[0]);
    	assertEquals("Not Column B", (short)1,myCellReference[0].getCol());
		assertEquals("Not Row 5", 4,myCellReference[0].getRow());
    }
    
	public static void main(java.lang.String[] args) {        
		junit.textui.TestRunner.run(TestAreaReference.class);
	}
        
}
