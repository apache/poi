
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

/**
 * Tests the Range Address Utility Functionality
 * @author Danny Mui (danny at muibros.com)
 */
public class TestRangeAddress extends TestCase {
     public TestRangeAddress(String s) {
        super(s);
    }
    
	public static void main(java.lang.String[] args) {        
		junit.textui.TestRunner.run(TestRangeAddress.class);
	}
    
    
    
    public void testReferenceParse() {
    	String reference = "Sheet2!$A$1:$C$3";
        RangeAddress ra = new RangeAddress(reference);
        
        assertEquals("Sheet2!A1:C3", ra.getAddress()); 
        
    }
}
