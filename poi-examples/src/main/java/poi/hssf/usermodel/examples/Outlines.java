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

package org.apache.poi.hssf.usermodel.examples;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Creates outlines.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class Outlines {
     public static void main(String[] args) throws IOException {
        createCase1( "outline1.xls" );
        System.out.println( "outline1.xls written.  Two expanded groups." );
        createCase2( "outline2.xls" );
        System.out.println( "outline2.xls written.  Two groups.  Inner group collapsed." );
        createCase3( "outline3.xls" );
        System.out.println( "outline3.xls written.  Two groups.  Both collapsed." );
        createCase4( "outline4.xls" );
        System.out.println( "outline4.xls written.  Two groups.  Collapsed then inner group expanded." );
        createCase5( "outline5.xls" );
        System.out.println( "outline5.xls written.  Two groups.  Collapsed then reexpanded." );
        createCase6( "outline6.xls" );
        System.out.println( "outline6.xls written.  Two groups with matching end points.  Second group collapsed." );
        createCase7( "outline7.xls" );
        System.out.println( "outline7.xls written.  Row outlines." );
        createCase8( "outline8.xls" );
        System.out.println( "outline8.xls written.  Row outlines.  Inner group collapsed." );
        createCase9( "outline9.xls" );
        System.out.println( "outline9.xls written.  Row outlines.  Both collapsed." );
        createCase10( "outline10.xls" );
        System.out.println( "outline10.xls written.  Row outlines.  Collapsed then inner group expanded." );
        createCase11( "outline11.xls" );
        System.out.println( "outline11.xls written.  Row outlines.  Collapsed then expanded." );
        createCase12( "outline12.xls" );
        System.out.println( "outline12.xls written.  Row outlines.  Two row groups with matching end points.  Second group collapsed." );
        createCase13( "outline13.xls" );
        System.out.println( "outline13.xls written.  Mixed bag." );
    }

    private static void createCase1(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupColumn(4, 7);

        for (int row = 0; row < 200; row++) {
            HSSFRow r = sheet1.createRow(row);
            for (int column = 0; column < 200; column++) {
                HSSFCell c = r.createCell(column);
                c.setCellValue(column);
            }
        }

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase2(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupColumn(2, 10);
        sheet1.groupColumn(4, 7);
        sheet1.setColumnGroupCollapsed(4, true);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase3(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupColumn(2, 10);
        sheet1.groupColumn(4, 7);
        sheet1.setColumnGroupCollapsed(4, true);
        sheet1.setColumnGroupCollapsed(2, true);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase4(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupColumn(2, 10);
        sheet1.groupColumn(4, 7);
        sheet1.setColumnGroupCollapsed(4, true);
        sheet1.setColumnGroupCollapsed(2, true);

        sheet1.setColumnGroupCollapsed(4, false);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase5(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupColumn(2, 10);
        sheet1.groupColumn(4, 7);
        sheet1.setColumnGroupCollapsed(4, true);
        sheet1.setColumnGroupCollapsed(2, true);

        sheet1.setColumnGroupCollapsed(4, false);
        sheet1.setColumnGroupCollapsed(3, false);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase6(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupColumn(2, 10);
        sheet1.groupColumn(4, 10);
        sheet1.setColumnGroupCollapsed(4, true);
        sheet1.setColumnGroupCollapsed(2, true);

        sheet1.setColumnGroupCollapsed(3, false);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase7(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 10);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase8(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 10);
        sheet1.setRowGroupCollapsed(7, true);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase9(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 10);
        sheet1.setRowGroupCollapsed(7, true);
        sheet1.setRowGroupCollapsed(5, true);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase10(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 10);
        sheet1.setRowGroupCollapsed(7, true);
        sheet1.setRowGroupCollapsed(5, true);
        sheet1.setRowGroupCollapsed(8, false);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase11(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 10);
        sheet1.setRowGroupCollapsed(7, true);
        sheet1.setRowGroupCollapsed(5, true);
        sheet1.setRowGroupCollapsed(8, false);
        sheet1.setRowGroupCollapsed(14, false);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase12(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 14);
        sheet1.setRowGroupCollapsed(7, true);
        sheet1.setRowGroupCollapsed(5, true);
        sheet1.setRowGroupCollapsed(6, false);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void createCase13(String filename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");

        sheet1.groupRow(5, 14);
        sheet1.groupRow(7, 14);
        sheet1.groupRow(16, 19);

        sheet1.groupColumn(4, 7);
        sheet1.groupColumn(9, 12);
        sheet1.groupColumn(10, 11);

        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }
}
