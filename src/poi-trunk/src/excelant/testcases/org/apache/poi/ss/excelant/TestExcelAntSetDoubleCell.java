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
package org.apache.poi.ss.excelant;

import junit.framework.TestCase;

import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtil;
import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtilFactory;

public class TestExcelAntSetDoubleCell extends TestCase {
	
	private ExcelAntSetDoubleCell fixture ;
	
	private ExcelAntWorkbookUtil util ;

    private static final String mortgageCalculatorFileName =
        BuildFileTest.getDataDir() + "/spreadsheet/mortgage-calculation.xls" ;

    @Override
	public void setUp() {
		fixture = new ExcelAntSetDoubleCell() ;
		util = ExcelAntWorkbookUtilFactory.getInstance(
				                                  mortgageCalculatorFileName ) ;
		fixture.setWorkbookUtil( util ) ;
	}
	
    @Override
	public void tearDown() {
		fixture = null ;
	}
	
	public void testSetDouble() {
		String cellId = "'Sheet3'!$A$1" ;
		double testValue = 1.1 ;
		
		fixture.setCell( cellId ) ;
		fixture.setValue( testValue ) ;
		
		double value = fixture.getCellValue() ;
		
		assertTrue( value > 0 ) ;
		assertEquals( testValue, value, 0.0 ) ;
		
		fixture.execute() ;
		
		double setValue = util.getCellAsDouble( cellId ) ;
		
		assertEquals( setValue, testValue, 0.0 ) ;
	}
	

}
