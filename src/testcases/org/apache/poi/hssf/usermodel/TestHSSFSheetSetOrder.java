
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
        

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.hssf.model.Sheet;

/**
* Tests HSSFWorkbook method setSheetOrder()
*
*
* @author Ruel Loehr (loehr1 at us.ibm.com)
*/

public class TestHSSFSheetSetOrder
       extends TestCase
{
   public TestHSSFSheetSetOrder(String s)
   {
       super(s);
   }

   /**
    * Test the sheet set order method
    */

   public void testBackupRecord()
           throws Exception
   {
       HSSFWorkbook wb = new HSSFWorkbook();
       
       for (int i=0; i < 10; i++)
       {
			HSSFSheet s = wb.createSheet("Sheet " +i);
			Sheet sheet = s.getSheet();
       }

     wb.getWorkbook().setSheetOrder("Sheet 6", 0);
	  wb.getWorkbook().setSheetOrder("Sheet 3", 7);
	  wb.getWorkbook().setSheetOrder("Sheet 1", 9);
	  
	 
   }


}


