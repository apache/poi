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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Palette;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.SharedStringSource;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.strings.SharedStringsTable;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackageRelationshipTypes;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.TargetMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookView;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookViews;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDialogsheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorksheetDocument;


public class XSSFWorkbook extends POIXMLDocument implements Workbook {

    private static final String WORKSHEET_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";

    private static final String WORKSHEET_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet";

    private static final String SHARED_STRINGS_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings";
    
    private static final String DRAWING_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing";
    
    private static final String IMAGE_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
    
    private CTWorkbook workbook;
    
    private List<XSSFSheet> sheets = new LinkedList<XSSFSheet>();
    
    private SharedStringSource sharedStringSource;

    private static POILogger log = POILogFactory.getLogger(XSSFWorkbook.class);
    
    public XSSFWorkbook() {
        this.workbook = CTWorkbook.Factory.newInstance();
        CTBookViews bvs = this.workbook.addNewBookViews();
        CTBookView bv = bvs.addNewWorkbookView();
        bv.setActiveTab(0);
        this.workbook.addNewSheets();
    }
    
    public XSSFWorkbook(String path) throws IOException {
    	this(openPackage(path));
    }
    public XSSFWorkbook(Package pkg) throws IOException {
        super(pkg);
        try {
            WorkbookDocument doc = WorkbookDocument.Factory.parse(getCorePart().getInputStream());
            this.workbook = doc.getWorkbook();
            // Load shared strings
            PackageRelationshipCollection prc = getCorePart().getRelationshipsByType(SHARED_STRINGS_RELATIONSHIP);
            Iterator<PackageRelationship> it = prc.iterator();
            if (it.hasNext()) { 
                PackageRelationship rel = it.next();
                PackagePart part = getTargetPart(rel);
                this.sharedStringSource = new SharedStringsTable(part);
            }
            // Load individual sheets
            for (CTSheet ctSheet : this.workbook.getSheets().getSheetArray()) {
                PackagePart part = getPackagePart(ctSheet);
                if (part == null) {
                    continue;
                }
                WorksheetDocument worksheetDoc = WorksheetDocument.Factory.parse(part.getInputStream());
                XSSFSheet sheet = new XSSFSheet(ctSheet, worksheetDoc.getWorksheet(), this);
                this.sheets.add(sheet);
            }
        } catch (XmlException e) {
            throw new IOException(e.toString());
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString());
        }
    }

    protected CTWorkbook getWorkbook() {
        return this.workbook;
    }

    /**
     * Get the PackagePart corresponding to a given sheet.
     * 
     * @param ctSheet The sheet
     * @return A PackagePart, or null if no matching part found.
     * @throws InvalidFormatException
     */
    private PackagePart getPackagePart(CTSheet ctSheet) throws InvalidFormatException {
        PackageRelationship rel = this.getCorePart().getRelationship(ctSheet.getId());
        if (rel == null) {
            log.log(POILogger.WARN, "No relationship found for sheet " + ctSheet.getId());
            return null;
        }
        return getTargetPart(rel);
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
        XSSFSheet srcSheet = sheets.get(sheetNum);
        String srcName = getSheetName(sheetNum);
        if (srcSheet != null) {
            XSSFSheet clonedSheet = srcSheet.cloneSheet();

            sheets.add(clonedSheet);
            CTSheet newcts = this.workbook.getSheets().addNewSheet();
            newcts.set(clonedSheet.getSheet());
            
            int i = 1;
            while (true) {
                //Try and find the next sheet name that is unique
                String name = srcName;
                String index = Integer.toString(i++);
                if (name.length() + index.length() + 2 < 31) {
                    name = name + "("+index+")";
                } else {
                    name = name.substring(0, 31 - index.length() - 2) + "(" +index + ")";
                }

                //If the sheet name is unique, then set it otherwise move on to the next number.
                if (getSheetIndex(name) == -1) {
                    setSheetName(sheets.size() - 1, name);
                    break;
                }
            }
            return clonedSheet;
        }
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
        return createSheet(sheetname, null);
    }
    
    public Sheet createSheet(String sheetname, CTWorksheet worksheet) {
        CTSheet sheet = addSheet(sheetname);
        XSSFWorksheet wrapper = new XSSFWorksheet(sheet, worksheet, this);
        this.sheets.add(wrapper);
        return wrapper;
    }
    
    public Sheet createDialogsheet(String sheetname, CTDialogsheet dialogsheet) {
    	  CTSheet sheet = addSheet(sheetname);
    	  XSSFDialogsheet wrapper = new XSSFDialogsheet(sheet, dialogsheet, this);
    	  this.sheets.add(wrapper);
    	  return wrapper;
    }

	private CTSheet addSheet(String sheetname) {
		CTSheet sheet = workbook.getSheets().addNewSheet();
        if (sheetname != null) {
            sheet.setName(sheetname);
        }
		return sheet;
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

    public List<PictureData> getAllPictures() {
        // In OOXML pictures are referred to in sheets
        List<PictureData> pictures = new LinkedList<PictureData>();
        for (CTSheet ctSheet : this.workbook.getSheets().getSheetArray()) {
            try {
                PackagePart sheetPart = getPackagePart(ctSheet);
                if (sheetPart == null) {
                    continue;
                }
                PackageRelationshipCollection prc = sheetPart.getRelationshipsByType(DRAWING_RELATIONSHIP);
                for (PackageRelationship rel : prc) {
                    PackagePart drawingPart = getTargetPart(rel);
                    PackageRelationshipCollection prc2 = drawingPart.getRelationshipsByType(IMAGE_RELATIONSHIP);
                    for (PackageRelationship rel2 : prc2) {
                        PackagePart imagePart = getTargetPart(rel2);
                        XSSFPictureData pd = new XSSFPictureData(imagePart);
                        pictures.add(pd);
                    }
                }
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return pictures;
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

    public Palette getCustomPalette() {
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
        short i = 0;
        for (XSSFSheet sheet : this.sheets) {
            if (sheet.isSelected()) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public Sheet getSheet(String name) {
        CTSheet[] sheets = this.workbook.getSheets().getSheetArray();  
        for (int i = 0 ; i < sheets.length ; ++i) {
            if (name.equals(sheets[i].getName())) {
                return this.sheets.get(i);
            }
        }
        return null;
    }

    public Sheet getSheetAt(int index) {
        return this.sheets.get(index);
    }

    public int getSheetIndex(String name) {
        CTSheet[] sheets = this.workbook.getSheets().getSheetArray();  
        for (int i = 0 ; i < sheets.length ; ++i) {
            if (name.equals(sheets[i].getName())) {
                return i;
            }
        }
        return -1;
    }

    public int getSheetIndex(Sheet sheet) {
        return this.sheets.indexOf(sheet);
    }

    public String getSheetName(int sheet) {
        return this.workbook.getSheets().getSheetArray(sheet).getName();
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
        this.sheets.remove(index);
        this.workbook.getSheets().removeSheet(index);
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

    /**
     * We only set one sheet as selected for compatibility with HSSF.
     */
    public void setSelectedTab(short index) {
        for (int i = 0 ; i < this.sheets.size() ; ++i) {
            XSSFSheet sheet = this.sheets.get(i);
            sheet.setSelected(i == index);
        }
    }

    public void setSheetName(int sheet, String name) {
        this.workbook.getSheets().getSheetArray(sheet).setName(name);
    }

    public void setSheetName(int sheet, String name, short encoding) {
        this.workbook.getSheets().getSheetArray(sheet).setName(name);
    }

    public void setSheetOrder(String sheetname, int pos) {
        int idx = getSheetIndex(sheetname);
        sheets.add(pos, sheets.remove(idx));
        // Reorder CTSheets
        XmlObject cts = this.workbook.getSheets().getSheetArray(idx).copy();
        this.workbook.getSheets().removeSheet(idx);
        CTSheet newcts = this.workbook.getSheets().insertNewSheet(pos);
        newcts.set(cts);
    }

    public void unwriteProtectWorkbook() {
        // TODO Auto-generated method stub

    }

    public void write(OutputStream stream) throws IOException {

        try {
            // Create a package referring the temp file.
            Package pkg = Package.create(stream);
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
             
             for (int i = 0 ; i < this.getNumberOfSheets() ; ++i) {
            	 XSSFSheet sheet = (XSSFSheet) this.getSheetAt(i);
                 PackagePartName partName = PackagingURIHelper.createPartName("/xl/worksheets/sheet" + i + ".xml");
                 corePart.addRelationship(partName, TargetMode.INTERNAL, WORKSHEET_RELATIONSHIP, "rSheet" + 1);
                 PackagePart part = pkg.createPart(partName, WORKSHEET_TYPE);
                 
                 // XXX This should not be needed, but apparently the setSaveOuter call above does not work in XMLBeans 2.2
                 xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));
                 out = part.getOutputStream();
                 sheet.getWorksheet().save(out, xmlOptions);
                 
                 out.close();
             }
             
             pkg.close();
             
        } catch (InvalidFormatException e) {
            // TODO: replace with more meaningful exception
            throw new RuntimeException(e);
        }
    }

    public void writeProtectWorkbook(String password, String username) {
        // TODO Auto-generated method stub

    }

    public SharedStringSource getSharedStringSource() {
        return this.sharedStringSource;
    }

    protected void setSharedStringSource(SharedStringSource sharedStringSource) {
        this.sharedStringSource = sharedStringSource;
    }

    public CreationHelper getCreationHelper() {
    	return new XSSFCreationHelper(this);
    }
}
