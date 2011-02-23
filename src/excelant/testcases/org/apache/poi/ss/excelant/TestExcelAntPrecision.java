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

public class TestExcelAntPrecision extends TestCase {
	
	private ExcelAntPrecision fixture ;

    @Override
	public void setUp() {
		fixture = new ExcelAntPrecision() ;
	}
	
    @Override
	public void tearDown() {
		fixture = null ;
	}
	
	public void testVerifyPrecision() {
		
		double value = 1.0E-1 ;
		
		fixture.setValue( value ) ;
		
		double result = fixture.getValue() ;
		
		assertTrue( result > 0 ) ;
		
		assertEquals( value, result, 0.0 ) ;
	}

}
