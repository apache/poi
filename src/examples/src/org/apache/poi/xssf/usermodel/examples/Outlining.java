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

package org.apache.poi.xssf.usermodel.examples;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Outlining {

    public static void main(String[]args) throws Exception{
	Outlining o=new Outlining();
	o.groupRowColumn();
	o.collapseExpandRowColumn();
    }


    private void groupRowColumn() throws Exception{
	Workbook wb = new XSSFWorkbook();
	Sheet sheet1 = wb.createSheet("new sheet");

	sheet1.groupRow( 5, 14 );
	sheet1.groupRow( 7, 14 );
	sheet1.groupRow( 16, 19 );

	sheet1.groupColumn( (short)4, (short)7 );
	sheet1.groupColumn( (short)9, (short)12 );
	sheet1.groupColumn( (short)10, (short)11 );

	FileOutputStream fileOut = new FileOutputStream("outlining.xlsx");
	wb.write(fileOut);
	fileOut.close();

    }

    private void collapseExpandRowColumn()throws Exception{
	Workbook wb2 = new XSSFWorkbook();
	Sheet sheet2 = wb2.createSheet("new sheet");
	sheet2.groupRow( 5, 14 );
	sheet2.groupRow( 7, 14 );
	sheet2.groupRow( 16, 19 );

	sheet2.groupColumn( (short)4, (short)7 );
	sheet2.groupColumn( (short)9, (short)12 );
	sheet2.groupColumn( (short)10, (short)11 );
	
	
	sheet2.setRowGroupCollapsed( 7, true );
	//sheet1.setRowGroupCollapsed(7,false);
	
	sheet2.setColumnGroupCollapsed( (short)4, true );	
	sheet2.setColumnGroupCollapsed( (short)4, false );
	
	FileOutputStream fileOut = new FileOutputStream("outlining_collapsed.xlsx");
	wb2.write(fileOut);
	fileOut.close();
    }
}
