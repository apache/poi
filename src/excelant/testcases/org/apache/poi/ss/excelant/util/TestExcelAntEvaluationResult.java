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
package org.apache.poi.ss.excelant.util;

import junit.framework.TestCase;

public class TestExcelAntEvaluationResult extends TestCase {
	
	private ExcelAntEvaluationResult fixture ;
	
	private boolean completedWithError = false ;
    private boolean passed = false ;
    private double retValue  = 1.1 ; 
    private String errMessage = "error message" ;  
    private double delta = 2.2 ; 
    private String cellId = "testCell!$F$1" ;
    
	public void setUp() {
		fixture = new ExcelAntEvaluationResult( completedWithError,
				                                passed, 
				                                retValue,
				                                errMessage, 
				                                delta,
				                                cellId ) ;
	}
	
	public void tearDown() {
		fixture = null ;
	}
	
	public void testCompletedWithErrorMessage() {
		String errMsg = fixture.getErrorMessage() ;
 		assertNotNull( errMsg ) ;
 		assertEquals( errMsg, errMessage ) ;
	}
	
	public void testPassed() {
		boolean passedValue = fixture.didTestPass() ;
		assertEquals( passedValue, passed ) ;
	}
	
	public void testDelta() {
		double deltaValue = fixture.getDelta() ;
		assertEquals(deltaValue, delta, 0.0 ) ;
	}
	
	public void testCellId() {
		String cellIdValue = fixture.getCellName() ;
		assertNotNull( cellIdValue ) ;
		assertEquals( cellIdValue, cellId ) ;
	}

}
