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

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class CreateNewSpreadsheet {
	public static void main(String[] args) throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		
		XSSFSheet s1 = wb.createSheet("Sheet One");
		XSSFSheet s2 = wb.createSheet("Sheet Two");
		
		// Create a few cells
		s1.createRow(0);
		s1.createRow(1);
		s1.createRow(2);
		s1.createRow(3);
		s2.createRow(2);
		
		s1.getRow(0).createCell(0).setCellValue(1.2);
		s1.getRow(0).createCell(1).setCellValue(createHelper.createRichTextString("Sheet 1 text"));
		s1.getRow(1).createCell(0).setCellValue(4.22);
		s1.getRow(2).createCell(0).setCellValue(5.44);
		s1.getRow(3).createCell(0).setCellFormula("SUM(A1:A3)");
		
		s2.getRow(2).createCell(1).setCellValue(createHelper.createRichTextString("Sheet 2"));

        s1.groupRow(0, 3);

        s1.getRow(1).setHeightInPoints(10.4f);
        //s1.setActiveCell("A2");
        //s2.setSelected(true);

        // Save
		FileOutputStream fout = new FileOutputStream("NewFile.xlsx");
		wb.write(fout);
		fout.close();
		System.out.println("Done");
	}
}
