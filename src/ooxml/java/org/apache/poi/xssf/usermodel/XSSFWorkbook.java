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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CommentsSource;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Palette;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.Control;
import org.apache.poi.xssf.model.Drawing;
import org.apache.poi.xssf.model.SharedStringSource;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.XSSFModel;
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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedNames;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDialogsheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorksheetDocument;


public class XSSFWorkbook extends POIXMLDocument implements Workbook {
	/** Are we a normal workbook, or a macro enabled one? */
	private boolean isMacroEnabled = false;

    private CTWorkbook workbook;

    private List<XSSFSheet> sheets = new LinkedList<XSSFSheet>();
    private List<XSSFName> namedRanges = new LinkedList<XSSFName>();

    private SharedStringSource sharedStringSource;
    private StylesSource stylesSource;

    private MissingCellPolicy missingCellPolicy = Row.RETURN_NULL_AND_BLANK;

    private static POILogger log = POILogFactory.getLogger(XSSFWorkbook.class);

    public XSSFWorkbook() {
        this.workbook = CTWorkbook.Factory.newInstance();
        CTBookViews bvs = this.workbook.addNewBookViews();
        CTBookView bv = bvs.addNewWorkbookView();
        bv.setActiveTab(0);
        this.workbook.addNewSheets();

        // We always require styles and shared strings
        sharedStringSource = new SharedStringsTable();
        stylesSource = new StylesTable();
    }

    public XSSFWorkbook(String path) throws IOException {
    	this(openPackage(path));
    }
    public XSSFWorkbook(InputStream  is) throws IOException {
    	this(openPackage(is));
    }

    public XSSFWorkbook(Package pkg) throws IOException {
        super(pkg);
        try {
            WorkbookDocument doc = WorkbookDocument.Factory.parse(getCorePart().getInputStream());
            this.workbook = doc.getWorkbook();

            // Are we macro enabled, or just normal?
            isMacroEnabled =
            		getCorePart().getContentType().equals(XSSFRelation.MACROS_WORKBOOK.getContentType());

            try {
	            // Load shared strings
	            this.sharedStringSource = (SharedStringSource)
	            	XSSFRelation.SHARED_STRINGS.load(getCorePart());
            } catch(Exception e) {
            	throw new IOException("Unable to load shared strings - " + e.toString());
            }
            try {
	            // Load styles source
	            this.stylesSource = (StylesSource)
	            	XSSFRelation.STYLES.load(getCorePart());
            } catch(Exception e) {
            	e.printStackTrace();
            	throw new IOException("Unable to load styles - " + e.toString());
            }

            // Load individual sheets
            for (CTSheet ctSheet : this.workbook.getSheets().getSheetArray()) {
                PackagePart part = getPackagePart(ctSheet);
                if (part == null) {
                	log.log(POILogger.WARN, "Sheet with name " + ctSheet.getName() + " and r:id " + ctSheet.getId()+ " was defined, but didn't exist in package, skipping");
                    continue;
                }

                // Load child streams of the sheet
                ArrayList<? extends XSSFModel> childModels;
                CommentsSource comments = null;
                ArrayList<Drawing> drawings;
                ArrayList<Control> controls;
                try {
                	// Get the comments for the sheet, if there are any
                	childModels = XSSFRelation.SHEET_COMMENTS.loadAll(part);
                	if(childModels.size() > 0) {
                		comments = (CommentsSource)childModels.get(0);
                	}

	                // Get the drawings for the sheet, if there are any
	                drawings = (ArrayList<Drawing>)XSSFRelation.VML_DRAWINGS.loadAll(part);
	                // Get the activeX controls for the sheet, if there are any
	                controls = (ArrayList<Control>)XSSFRelation.ACTIVEX_CONTROLS.loadAll(part);
                } catch(Exception e) {
                	throw new RuntimeException("Unable to construct child part",e);
                }

                // Now create the sheet
                WorksheetDocument worksheetDoc = WorksheetDocument.Factory.parse(part.getInputStream());
                XSSFSheet sheet = new XSSFSheet(ctSheet, worksheetDoc.getWorksheet(), this, comments, drawings, controls);
                this.sheets.add(sheet);

                // Process external hyperlinks for the sheet,
                //  if there are any
                PackageRelationshipCollection hyperlinkRels =
                	part.getRelationshipsByType(XSSFRelation.SHEET_HYPERLINKS.getRelation());
                sheet.initHyperlinks(hyperlinkRels);

                // Get the embeddings for the workbook
                for(PackageRelationship rel : part.getRelationshipsByType(XSSFRelation.OLEEMBEDDINGS.getRelation()))
                    embedds.add(getTargetPart(rel)); // TODO: Add this reference to each sheet as well

                for(PackageRelationship rel : part.getRelationshipsByType(XSSFRelation.PACKEMBEDDINGS.getRelation()))
                    embedds.add(getTargetPart(rel));
            }
        } catch (XmlException e) {
            throw new IOException(e.toString());
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString());
        }

        // Process the named ranges
        if(workbook.getDefinedNames() != null) {
        	for(CTDefinedName ctName : workbook.getDefinedNames().getDefinedNameArray()) {
        		namedRanges.add(new XSSFName(ctName, this));
        	}
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
            log.log(POILogger.WARN, "No relationship found for sheet " + ctSheet.getId() + " - core part has " + this.getCorePart().getRelationships().size() + " relations defined");
            return null;
        }
        return getTargetPart(rel);
    }

    public int addPicture(byte[] pictureData, int format) {
        // TODO Auto-generated method stub
        return 0;
    }

    public XSSFSheet cloneSheet(int sheetNum) {
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

    public XSSFCellStyle createCellStyle() {
    	return new XSSFCellStyle(stylesSource);
    }

    public DataFormat createDataFormat() {
    	return getCreationHelper().createDataFormat();
    }

    public XSSFFont createFont() {
        return new XSSFFont();
    }

    public XSSFName createName() {
    	XSSFName name = new XSSFName(this);
    	namedRanges.add(name);
    	return name;
    }

    public XSSFSheet createSheet() {
        String sheetname = "Sheet" + (sheets.size() + 1);
        return createSheet(sheetname);
    }

    public XSSFSheet createSheet(String sheetname) {
        return createSheet(sheetname, null);
    }

    public XSSFSheet createSheet(String sheetname, CTWorksheet worksheet) {
        CTSheet sheet = addSheet(sheetname);
        XSSFWorksheet wrapper = new XSSFWorksheet(sheet, worksheet, this);
        this.sheets.add(wrapper);
        return wrapper;
    }

    public XSSFSheet createDialogsheet(String sheetname, CTDialogsheet dialogsheet) {
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

    public XSSFFont findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
    	short fontNum=getNumberOfFonts();
        for (short i = 0; i < fontNum; i++) {
            XSSFFont xssfFont = getFontAt(i);
            if (    xssfFont.getBold() == (boldWeight == XSSFFont.BOLDWEIGHT_BOLD)
                    && xssfFont.getColor() == color
                    && xssfFont.getFontHeightInPoints() == fontHeight
                    && xssfFont.getFontName().equals(name)
                    && xssfFont.getItalic() == italic
                    && xssfFont.getStrikeout() == strikeout
                    && xssfFont.getTypeOffset() == typeOffset
                    && xssfFont.getUnderline() == underline)
            {
                return xssfFont;
            }
        }
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
                PackageRelationshipCollection prc = sheetPart.getRelationshipsByType(XSSFRelation.DRAWINGS.getRelation());
                for (PackageRelationship rel : prc) {
                    PackagePart drawingPart = getTargetPart(rel);
                    PackageRelationshipCollection prc2 = drawingPart.getRelationshipsByType(XSSFRelation.IMAGES.getRelation());
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
        return stylesSource.getStyleAt(idx);
    }

    public Palette getCustomPalette() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * get the first tab that is displayed in the list of tabs in excel.
     */
    public int getFirstVisibleTab() {
        CTBookViews bookViews = workbook.getBookViews();
        CTBookView bookView = bookViews.getWorkbookViewArray(0);
        return (short) bookView.getActiveTab();
    }
    /**
     * deprecated Aug 2008
     * @deprecated - Misleading name - use getFirstVisibleTab()
     */
    public short getDisplayedTab() {
        return (short) getFirstVisibleTab();
    }

    public XSSFFont getFontAt(short idx) {
        return (XSSFFont)stylesSource.getFontAt(idx);
    }

    public XSSFName getNameAt(int index) {
    	return namedRanges.get(index);
    }
    public String getNameName(int index) {
        return getNameAt(index).getNameName();
    }
    public int getNameIndex(String name) {
    	for(int i=0; i<namedRanges.size(); i++) {
    		if(namedRanges.get(i).getNameName().equals(name)) {
    			return i;
    		}
    	}
    	return -1;
    }

    /**
     * TODO - figure out what the hell this methods does in
     *  HSSF...
     */
    public String resolveNameXText(int refIndex, int definedNameIndex) {
		// TODO Replace with something proper
		return null;
	}

    public short getNumCellStyles() {
        return (short) ((StylesTable)stylesSource).getNumCellStyles();
    }

    public short getNumberOfFonts() {
        // TODO Auto-generated method stub
        return (short)((StylesTable)stylesSource).getNumberOfFonts();
    }

    public int getNumberOfNames() {
        return namedRanges.size();
    }

    public int getNumberOfSheets() {
        return this.workbook.getSheets().sizeOfSheetArray();
    }

    public String getPrintArea(int sheetIndex) {
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

    /**
     * Doesn't do anything - returns the same index
     * TODO - figure out if this is a ole2 specific thing, or
     *  if we need to do something proper here too!
     */
    public int getSheetIndexFromExternSheetIndex(int externSheetNumber) {
		return externSheetNumber;
	}
    /**
     * Doesn't do anything special - returns the same as getSheetName()
     * TODO - figure out if this is a ole2 specific thing, or
     *  if we need to do something proper here too!
     */
    public String findSheetNameFromExternSheet(int externSheetIndex) {
		return getSheetName(externSheetIndex);
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

    /**
     * Returns the external sheet index of the sheet
     *  with the given internal index, creating one
     *  if needed.
     * Used by some of the more obscure formula and
     *  named range things.
     * Fairly easy on XSSF (we think...) since the
     *  internal and external indicies are the same
     */
    public int getExternalSheetIndex(int internalSheetIndex) {
    	return internalSheetIndex;
    }

    public String getSheetName(int sheet) {
        return this.workbook.getSheets().getSheetArray(sheet).getName();
    }
    
    /**
     * Are we a normal workbook (.xlsx), or a 
     *  macro enabled workbook (.xlsm)?
     */
    public boolean isMacroEnabled() {
    	return isMacroEnabled;
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

	/**
	 * Retrieves the current policy on what to do when
	 *  getting missing or blank cells from a row.
	 * The default is to return blank and null cells.
	 *  {@link MissingCellPolicy}
	 */
	public MissingCellPolicy getMissingCellPolicy() {
		return missingCellPolicy;
	}
	/**
	 * Sets the policy on what to do when
	 *  getting missing or blank cells from a row.
	 * This will then apply to all calls to 
	 *  {@link Row.getCell()}. See
	 *  {@link MissingCellPolicy}
	 */
	public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
		this.missingCellPolicy = missingCellPolicy;
	}
	
    public void setBackupFlag(boolean backupValue) {
        // TODO Auto-generated method stub

    }

    /**
     * sets the first tab that is displayed in the list of tabs
     * in excel.
     * @param index
     */
    public void setFirstVisibleTab(short index) {
        CTBookViews bookViews = workbook.getBookViews();
        CTBookView bookView= bookViews.getWorkbookViewArray(0);
        bookView.setActiveTab(index);
    }
    /**
     * deprecated Aug 2008
     * @deprecated - Misleading name - use setFirstVisibleTab() 
     */
    public void setDisplayedTab(short index) {
        setFirstVisibleTab(index);
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
    	// What kind of workbook are we?
    	XSSFRelation workbookRelation = XSSFRelation.WORKBOOK;
    	if(isMacroEnabled) {
    		workbookRelation = XSSFRelation.MACROS_WORKBOOK;
    	}

        try {
            // Create a package referring the temp file.
            Package pkg = Package.create(stream);
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName(workbookRelation.getDefaultFileName());
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT, "rId1");
            // Create main document part
            PackagePart corePart = pkg.createPart(corePartName, workbookRelation.getContentType());
            OutputStream out;

            XmlOptions xmlOptions = new XmlOptions();
            // Requests use of whitespace for easier reading
            xmlOptions.setSavePrettyPrint();
            xmlOptions.setSaveOuter();
            xmlOptions.setUseDefaultNamespace();
            
            // Write out our sheets, updating the references
            //  to them in the main workbook as we go
            for (int i=0 ; i < this.getNumberOfSheets(); i++) {
            	int sheetNumber = (i+1);
            	XSSFSheet sheet = (XSSFSheet) this.getSheetAt(i);
                PackagePartName partName = PackagingURIHelper.createPartName(
                		XSSFRelation.WORKSHEET.getFileName(sheetNumber));
                PackageRelationship rel =
                	 corePart.addRelationship(partName, TargetMode.INTERNAL, XSSFRelation.WORKSHEET.getRelation(), "rSheet" + sheetNumber);
                PackagePart part = pkg.createPart(partName, XSSFRelation.WORKSHEET.getContentType());
                 
                // XXX This should not be needed, but apparently the setSaveOuter call above does not work in XMLBeans 2.2
                xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));
                sheet.save(part, xmlOptions);
                 
                // Update our internal reference for the package part
                workbook.getSheets().getSheetArray(i).setId(rel.getId());
                workbook.getSheets().getSheetArray(i).setSheetId(sheetNumber);
                
                // If our sheet has comments, then write out those
                if(sheet.hasComments()) {
                	CommentsTable ct = (CommentsTable)sheet.getCommentsSourceIfExists();
                	XSSFRelation.SHEET_COMMENTS.save(ct, part, sheetNumber);
                }
                
                // If our sheet has drawings, then write out those
                if(sheet.getDrawings() != null) {
                	int drawingIndex = 1;
                	for(Drawing drawing : sheet.getDrawings()) {
                		XSSFRelation.VML_DRAWINGS.save(
                				drawing,
                				part,
                				drawingIndex
                		);
                		drawingIndex++;
                	}
                }
                
                // If our sheet has controls, then write out those
                if(sheet.getControls() != null) {
                	int controlIndex = 1;
                	for(Control control : sheet.getControls()) {
                		XSSFRelation.ACTIVEX_CONTROLS.save(
                				control,
                				part,
                				controlIndex
                		);
                		controlIndex++;
                	}
                }
            }
             
            // Write shared strings and styles
            if(sharedStringSource != null) {
	             SharedStringsTable sst = (SharedStringsTable)sharedStringSource;
	             XSSFRelation.SHARED_STRINGS.save(sst, corePart);
            }
            if(stylesSource != null) {
	             StylesTable st = (StylesTable)stylesSource;
	             XSSFRelation.STYLES.save(st, corePart);
            }
            
            // Named ranges
            if(namedRanges.size() > 0) {
            	CTDefinedNames names = CTDefinedNames.Factory.newInstance();
            	CTDefinedName[] nr = new CTDefinedName[namedRanges.size()];
            	for(int i=0; i<namedRanges.size(); i++) {
            		nr[i] = namedRanges.get(i).getCTName();
            	}
            	names.setDefinedNameArray(nr);
            	workbook.setDefinedNames(names);
            } else {
            	if(workbook.isSetDefinedNames()) {
            		workbook.setDefinedNames(null);
            	}
            }
            
            // Macro related bits
            if(isMacroEnabled) {
	            // Copy VBA Macros if present
	            if(XSSFRelation.VBA_MACROS.exists( getCorePart() )) {
	            	try {
		            	XSSFModel vba = XSSFRelation.VBA_MACROS.load(getCorePart());
		            	XSSFRelation.VBA_MACROS.save(vba, corePart);
	            	} catch(Exception e) {
	            		throw new RuntimeException("Unable to copy vba macros over", e);
	            	}
	            }
            }

            // Now we can write out the main Workbook, with
            //  the correct references to the other parts
            out = corePart.getOutputStream();
            // XXX This should not be needed, but apparently the setSaveOuter call above does not work in XMLBeans 2.2
            xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorkbook.type.getName().getNamespaceURI(), "workbook"));
            workbook.save(out, xmlOptions);
            out.close();
             
            //  All done
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
    
    public StylesSource getStylesSource() {
    	return this.stylesSource;
    }
    protected void setStylesSource(StylesSource stylesSource) {
    	this.stylesSource = stylesSource;
    }

    public CreationHelper getCreationHelper() {
    	return new XSSFCreationHelper(this);
    }
}
