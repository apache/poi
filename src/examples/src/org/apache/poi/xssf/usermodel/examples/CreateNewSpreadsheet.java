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
		Workbook wb = new XSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		
		Sheet s1 = wb.createSheet("Sheet One");
		Sheet s2 = wb.createSheet("2nd Sheet");
		
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

/*
		// Comment
		Comment comment = ((XSSFSheet)s1).createComment();
//		HSSFPatriarch patriach = (HSSFPatriarch)s1.createDrawingPatriarch();
//		Comment comment = patriach.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short)4, 2, (short) 6, 5));
		
		comment.setAuthor("Apache POI");
		comment.setString(createHelper.createRichTextString("I am a comment"));
		s1.getRow(0).getCell(0).setCellComment(comment);
		
		// Hyperlink
		Hyperlink hyperlink = createHelper.createHyperlink(Hyperlink.LINK_URL);
		hyperlink.setAddress("http://poi.apache.org/");
		hyperlink.setLabel("Link to POI");
		s1.getRow(1).createCell(1).setHyperlink(hyperlink);
		s1.getRow(1).getCell(1).setCellValue(createHelper.createRichTextString("Link to POI"));
*/
		// Save
		FileOutputStream fout = new FileOutputStream("NewFile.xlsx");
		wb.write(fout);
		fout.close();
		System.out.println("Done");
	}
}
