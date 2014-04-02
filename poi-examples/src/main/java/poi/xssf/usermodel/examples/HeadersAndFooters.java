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

import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class HeadersAndFooters {


    public static void main(String[]args) throws Exception {
        Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();
        Sheet sheet = wb.createSheet("first-header - format sheet");
        sheet.createRow(0).createCell(0).setCellValue(123);

        //set page numbers in the footer
        Footer footer = sheet.getFooter();
        //&P == current page number
        //&N == page numbers
        footer.setRight("Page &P of &N");

        
        Header firstHeader=((XSSFSheet)sheet).getFirstHeader();
        //&F == workbook file name
        firstHeader.setLeft("&F ......... first header");
        
        for(int i=0;i<100;i=i+10){
            sheet.createRow(i).createCell(0).setCellValue(123);
        }
        
        
        XSSFSheet sheet2 = (XSSFSheet)wb.createSheet("odd header-even footer");
        Header oddHeader=sheet2.getOddHeader();
        //&B == bold
        //&E == double underline
        //&D == date
        oddHeader.setCenter("&B &E oddHeader     &D ");
        
        Footer evenFooter=sheet2.getEvenFooter();
        evenFooter.setRight("even footer &P");
        sheet2.createRow(10).createCell(0).setCellValue("Second sheet with an oddHeader and an evenFooter");

        for(int i=0;i<200;i=i+10){
            sheet2.createRow(i).createCell(0).setCellValue(123);
        }
        
        XSSFSheet sheet3 = (XSSFSheet)wb.createSheet("odd header- odd footer");
        sheet3.createRow(10).createCell(0).setCellValue("Third sheet with oddHeader and oddFooter");
        Header oddH=sheet3.getOddHeader();
        //&C == centered
        oddH.setCenter("centered oddHeader");
        oddH.setLeft("left ");
        oddH.setRight("right ");
        
        Footer oddF=sheet3.getOddFooter();
        oddF.setLeft("Page &P");
        oddF.setRight("Pages &N ");
        
        FileOutputStream fileOut = new FileOutputStream("headerFooter.xlsx");
        wb.write(fileOut);
        fileOut.close();

    }
}
