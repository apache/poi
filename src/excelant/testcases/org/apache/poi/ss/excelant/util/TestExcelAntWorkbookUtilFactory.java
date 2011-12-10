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


/**
 * Tests for the ExcelAntWorbookUtilFactory.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public class TestExcelAntWorkbookUtilFactory extends TestCase{

	private final String mortgageCalculatorWorkbookFile = 
		                                 "test-data/spreadsheet/mortgage-calculation.xls" ;
	
	
	/**
	 * Simple test to determine if the factory properly returns an non-null
	 * instance of the ExcelAntWorkbookUtil class.
	 */
	public void testGetNewWorkbookUtilInstance() {
		
		ExcelAntWorkbookUtil util = ExcelAntWorkbookUtilFactory.getInstance(
				                              mortgageCalculatorWorkbookFile ) ;
		
		assertNotNull( util ) ;
		
	}
	
	
	/**
	 * Test whether or not the factory will properly return the same reference
	 * to an ExcelAnt WorkbookUtil when two different Strings, that point to
	 * the same resource, are passed in. 
	 */
	public void testVerifyEquivalence() {
		String sameFileName = "test-data/spreadsheet/mortgage-calculation.xls" ;
		
		ExcelAntWorkbookUtil util = ExcelAntWorkbookUtilFactory.getInstance(
                mortgageCalculatorWorkbookFile ) ;

		ExcelAntWorkbookUtil util2 = ExcelAntWorkbookUtilFactory.getInstance(
				                       sameFileName ) ;
		
		assertNotNull( util ) ;
		assertNotNull( util2 ) ;
		
		assertEquals( util, util2 ) ;
	}
	
}
