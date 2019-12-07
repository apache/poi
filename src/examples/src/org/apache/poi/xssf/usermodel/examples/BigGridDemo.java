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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Demonstrates a workaround you can use to generate large workbooks and avoid OutOfMemory exception.
 *
 * Note - You probably <em>don't want to use this approach any more</em>! POI
 *  now includes the SXSSF which handles all of this for you, you should
 *  be using that instead! This code remains mostly for historical interest.
 * <p>
 * See <a "http://poi.apache.org/spreadsheet/how-to.html#sxssf">
 *     http://poi.apache.org/spreadsheet/how-to.html#sxssf</a>.
 * <p>
 * If you really want to use this approach, which is also the one that SXSSF
 * does for you, it works as follows:
 *
 * 1. create a template workbook, create sheets and global objects such as cell styles, number formats, etc.
 * 2. create an application that streams data in a text file
 * 3. Substitute the sheet in the template with the generated data
 *
 * <p>
 *  Since 3.8 POI provides a low-memory footprint SXSSF API, which implements
 *  ths "BigGridDemo" strategy. SXSSF is an API-compatible streaming extension
 *  of XSSF to be used when very large spreadsheets have to be produced, and
 *  heap space is limited. SXSSF achieves its low memory footprint by limiting
 *  access to the rows that are within a sliding window, while XSSF gives access
 *  to all rows in the document. Older rows that are no longer in the window
 *  become inaccessible, as they are written to the disk.
 * </p>
 * See <a "http://poi.apache.org/spreadsheet/how-to.html#sxssf">
 *     http://poi.apache.org/spreadsheet/how-to.html#sxssf</a>.
 */
public final class BigGridDemo {
    private static final String XML_ENCODING = "UTF-8";

    private static final Random rnd = new Random();

    private BigGridDemo() {}

    public static void main(String[] args) throws Exception {

        // Step 1. Create a template file. Setup sheets and workbook-level objects such as
        // cell styles, number formats, etc.

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Big Grid");

            Map<String, XSSFCellStyle> styles = createStyles(wb);
            //name of the zip entry holding sheet data, e.g. /xl/worksheets/sheet1.xml
            String sheetRef = sheet.getPackagePart().getPartName().getName();

            //save the template
            try (FileOutputStream os = new FileOutputStream("template.xlsx")) {
                wb.write(os);
            }

            //Step 2. Generate XML file.
            File tmp = File.createTempFile("sheet", ".xml");
            try (
                    FileOutputStream stream = new FileOutputStream(tmp);
                    Writer fw = new OutputStreamWriter(stream, XML_ENCODING)
                ) {
                generate(fw, styles);
            }

            //Step 3. Substitute the template entry with the generated data
            try (FileOutputStream out = new FileOutputStream("big-grid.xlsx")) {
                substitute(new File("template.xlsx"), tmp, sheetRef.substring(1), out);
            }
        }
    }

    /**
     * Create a library of cell styles.
     */
    private static Map<String, XSSFCellStyle> createStyles(XSSFWorkbook wb){
        Map<String, XSSFCellStyle> styles = new HashMap<>();
        XSSFDataFormat fmt = wb.createDataFormat();

        XSSFCellStyle style1 = wb.createCellStyle();
        style1.setAlignment(HorizontalAlignment.RIGHT);
        style1.setDataFormat(fmt.getFormat("0.0%"));
        styles.put("percent", style1);

        XSSFCellStyle style2 = wb.createCellStyle();
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setDataFormat(fmt.getFormat("0.0X"));
        styles.put("coeff", style2);

        XSSFCellStyle style3 = wb.createCellStyle();
        style3.setAlignment(HorizontalAlignment.RIGHT);
        style3.setDataFormat(fmt.getFormat("$#,##0.00"));
        styles.put("currency", style3);

        XSSFCellStyle style4 = wb.createCellStyle();
        style4.setAlignment(HorizontalAlignment.RIGHT);
        style4.setDataFormat(fmt.getFormat("mmm dd"));
        styles.put("date", style4);

        XSSFCellStyle style5 = wb.createCellStyle();
        XSSFFont headerFont = wb.createFont();
        headerFont.setBold(true);
        style5.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style5.setFont(headerFont);
        styles.put("header", style5);

        return styles;
    }

    private static void generate(Writer out, Map<String, XSSFCellStyle> styles) throws Exception {

        Calendar calendar = Calendar.getInstance();

        SpreadsheetWriter sw = new SpreadsheetWriter(out);
        sw.beginSheet();

        //insert header row
        sw.insertRow(0);
        int styleIndex = styles.get("header").getIndex();
        sw.createCell(0, "Title", styleIndex);
        sw.createCell(1, "% Change", styleIndex);
        sw.createCell(2, "Ratio", styleIndex);
        sw.createCell(3, "Expenses", styleIndex);
        sw.createCell(4, "Date", styleIndex);

        sw.endRow();

        //write data rows
        for (int rownum = 1; rownum < 100000; rownum++) {
            sw.insertRow(rownum);

            sw.createCell(0, "Hello, " + rownum + "!");
            sw.createCell(1, (double)rnd.nextInt(100)/100, styles.get("percent").getIndex());
            sw.createCell(2, (double)rnd.nextInt(10)/10, styles.get("coeff").getIndex());
            sw.createCell(3, rnd.nextInt(10000), styles.get("currency").getIndex());
            sw.createCell(4, calendar, styles.get("date").getIndex());

            sw.endRow();

            calendar.roll(Calendar.DAY_OF_YEAR, 1);
        }
        sw.endSheet();
    }

    /**
     *
     * @param zipfile the template file
     * @param tmpfile the XML file with the sheet data
     * @param entry the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
     * @param out the stream to write the result to
     */
    private static void substitute(File zipfile, File tmpfile, String entry, OutputStream out) throws IOException {
        try (ZipFile zip = ZipHelper.openZipFile(zipfile)) {
            try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out)) {
                Enumeration<? extends ZipArchiveEntry> en = zip.getEntries();
                while (en.hasMoreElements()) {
                    ZipArchiveEntry ze = en.nextElement();
                    if (!ze.getName().equals(entry)) {
                        zos.putArchiveEntry(new ZipArchiveEntry(ze.getName()));
                        try (InputStream is = zip.getInputStream(ze)) {
                            copyStream(is, zos);
                        }
                        zos.closeArchiveEntry();
                    }
                }
                zos.putArchiveEntry(new ZipArchiveEntry(entry));
                try (InputStream is = new FileInputStream(tmpfile)) {
                    copyStream(is, zos);
                }
                zos.closeArchiveEntry();
            }
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >=0 ) {
          out.write(chunk,0,count);
        }
    }

    /**
     * Writes spreadsheet data in a Writer.
     * (YK: in future it may evolve in a full-featured API for streaming data in Excel)
     */
    public static class SpreadsheetWriter {
        private final Writer _out;
        private int _rownum;

        SpreadsheetWriter(Writer out){
            _out = out;
        }

        void beginSheet() throws IOException {
            _out.write("<?xml version=\"1.0\" encoding=\""+XML_ENCODING+"\"?>" +
                    "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" );
            _out.write("<sheetData>\n");
        }

        void endSheet() throws IOException {
            _out.write("</sheetData>");
            _out.write("</worksheet>");
        }

        /**
         * Insert a new row
         *
         * @param rownum 0-based row number
         */
        void insertRow(int rownum) throws IOException {
            _out.write("<row r=\""+(rownum+1)+"\">\n");
            this._rownum = rownum;
        }

        /**
         * Insert row end marker
         */
        void endRow() throws IOException {
            _out.write("</row>\n");
        }

        public void createCell(int columnIndex, String value, int styleIndex) throws IOException {
            String ref = new CellReference(_rownum, columnIndex).formatAsString();
            _out.write("<c r=\""+ref+"\" t=\"inlineStr\"");
            if(styleIndex != -1) {
                _out.write(" s=\""+styleIndex+"\"");
            }
            _out.write(">");
            _out.write("<is><t>"+value+"</t></is>");
            _out.write("</c>");
        }

        public void createCell(int columnIndex, String value) throws IOException {
            createCell(columnIndex, value, -1);
        }

        public void createCell(int columnIndex, double value, int styleIndex) throws IOException {
            String ref = new CellReference(_rownum, columnIndex).formatAsString();
            _out.write("<c r=\""+ref+"\" t=\"n\"");
            if(styleIndex != -1) {
                _out.write(" s=\""+styleIndex+"\"");
            }
            _out.write(">");
            _out.write("<v>"+value+"</v>");
            _out.write("</c>");
        }

        public void createCell(int columnIndex, double value) throws IOException {
            createCell(columnIndex, value, -1);
        }

        public void createCell(int columnIndex, Calendar value, int styleIndex) throws IOException {
            createCell(columnIndex, DateUtil.getExcelDate(value, false), styleIndex);
        }
    }
}
