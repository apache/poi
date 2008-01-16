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

package org.apache.poi.xssf.usermodel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationshipTypes;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.TargetMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookView;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookViews;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;


public class XSSFWorkbook implements Workbook {

    private CTWorkbook workbook;
    
    private List<XSSFSheet> sheets = new LinkedList<XSSFSheet>();

    public XSSFWorkbook() {
        this.workbook = CTWorkbook.Factory.newInstance();
        CTBookViews bvs = this.workbook.addNewBookViews();
        CTBookView bv = bvs.addNewWorkbookView();
        bv.setActiveTab(0);
        this.workbook.addNewSheets();
    }
    
    public int addPicture(byte[] pictureData, int format) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int addSSTString(String string) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Sheet cloneSheet(int sheetNum) {
        // TODO Auto-generated method stub
        return null;
    }

    public CellStyle createCellStyle() {
        // TODO Auto-generated method stub
        return null;
    }

    public DataFormat createDataFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    public Font createFont() {
        // TODO Auto-generated method stub
        return null;
    }

    public Name createName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Sheet createSheet() {
        return createSheet(null);
    }

    public Sheet createSheet(String sheetname) {
        CTSheet sheet = workbook.getSheets().addNewSheet();
        if (sheetname != null) {
            sheet.setName(sheetname);
        }
        XSSFSheet wrapper = new XSSFSheet(sheet);
        this.sheets.add(wrapper);
        return wrapper;
    }

    public void dumpDrawingGroupRecords(boolean fat) {
        // TODO Auto-generated method stub

    }

    public Font findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        // TODO Auto-generated method stub
        return null;
    }

    public List getAllEmbeddedObjects() {
        // TODO Auto-generated method stub
        return null;
    }

    public List getAllPictures() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getBackupFlag() {
        // TODO Auto-generated method stub
        return false;
    }

    public byte[] getBytes() {
        // TODO Auto-generated method stub
        return null;
    }

    public CellStyle getCellStyleAt(short idx) {
        // TODO Auto-generated method stub
        return null;
    }

    public HSSFPalette getCustomPalette() {
        // TODO Auto-generated method stub
        return null;
    }

    public short getDisplayedTab() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Font getFontAt(short idx) {
        // TODO Auto-generated method stub
        return null;
    }

    public Name getNameAt(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNameIndex(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getNameName(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public short getNumCellStyles() {
        // TODO Auto-generated method stub
        return 0;
    }

    public short getNumberOfFonts() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getNumberOfNames() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getNumberOfSheets() {
        return this.workbook.getSheets().sizeOfSheetArray();
    }

    public String getPrintArea(int sheetIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSSTString(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public short getSelectedTab() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Sheet getSheet(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Sheet getSheetAt(int index) {
        return this.sheets.get(index - 1);
    }

    public int getSheetIndex(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSheetIndex(Sheet sheet) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getSheetName(int sheet) {
        // TODO Auto-generated method stub
        return null;
    }

    public void insertChartRecord() {
        // TODO Auto-generated method stub

    }

    public void removeName(int index) {
        // TODO Auto-generated method stub

    }

    public void removeName(String name) {
        // TODO Auto-generated method stub

    }

    public void removePrintArea(int sheetIndex) {
        // TODO Auto-generated method stub

    }

    public void removeSheetAt(int index) {
        // TODO Auto-generated method stub

    }

    public void setBackupFlag(boolean backupValue) {
        // TODO Auto-generated method stub

    }

    public void setDisplayedTab(short index) {
        // TODO Auto-generated method stub

    }

    public void setPrintArea(int sheetIndex, String reference) {
        // TODO Auto-generated method stub

    }

    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        // TODO Auto-generated method stub

    }

    public void setRepeatingRowsAndColumns(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        // TODO Auto-generated method stub

    }

    public void setSelectedTab(short index) {
        // TODO Auto-generated method stub

    }

    public void setSheetName(int sheet, String name) {
        // TODO Auto-generated method stub

    }

    public void setSheetName(int sheet, String name, short encoding) {
        // TODO Auto-generated method stub

    }

    public void setSheetOrder(String sheetname, int pos) {
        // TODO Auto-generated method stub

    }

    public void unwriteProtectWorkbook() {
        // TODO Auto-generated method stub

    }

    /**
     * XXX: Horribly naive implementation based on OpenXML4J's Package class,
     * which sucks because it does not allow instantiation using an
     * OutputStream instead of a File. So we write the Package to a temporary
     * file, which we then proceed to read and stream out.
     */
    public void write(OutputStream stream) throws IOException {
        // Create a temporary file
        File file = File.createTempFile("poi-", ".xlsx");
        file.delete();

        try {
            // Create a package referring the temp file.
            Package pkg = Package.create(file);
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName("/xl/workbook.xml");
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT, "rId1");
            // Create main document part
            PackagePart corePart = pkg.createPart(corePartName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml");
            XmlOptions xmlOptions = new XmlOptions();
             // Requests use of whitespace for easier reading
             xmlOptions.setSavePrettyPrint();
             xmlOptions.setSaveOuter();
             // XXX This should not be needed, but apparently the setSaveOuter call above does not work in XMLBeans 2.2
             xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorkbook.type.getName().getNamespaceURI(), "workbook"));
             xmlOptions.setUseDefaultNamespace();
             
             OutputStream out = corePart.getOutputStream();
             workbook.save(out, xmlOptions);
             out.close();
             
             for (int i = 1 ; i <= this.getNumberOfSheets() ; ++i) {
                 XSSFSheet sheet = (XSSFSheet) this.getSheetAt(i);
                 PackagePartName partName = PackagingURIHelper.createPartName("/xl/worksheets/sheet" + i + ".xml");
                 corePart.addRelationship(partName, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet", "rSheet" + 1);
                 PackagePart part = pkg.createPart(partName, 
                         "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml");
                 
                 // XXX This should not be needed, but apparently the setSaveOuter call above does not work in XMLBeans 2.2
                 xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));
                 out = part.getOutputStream();
                 sheet.getWorksheet().save(out, xmlOptions);
                 
                 // XXX DEBUG
                 System.err.println(sheet.getWorksheet().xmlText(xmlOptions));
                 out.close();
             }
             
             pkg.close();
             
             byte[] buf = new byte[8192];
             int nread = 0;
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             try {
                 while ((nread = bis.read(buf)) > 0) {
                     stream.write(buf, 0, nread);
                 }
             } finally {
                 bis.close();
             }
        } catch (InvalidFormatException e) {
            // TODO: replace with more meaningful exception
            throw new RuntimeException(e);
        }
    }

    public void writeProtectWorkbook(String password, String username) {
        // TODO Auto-generated method stub

    }

}
