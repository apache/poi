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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.apache.poi.ss.usermodel.SharedStringSource;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.SheetReferences;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.BinaryPart;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.Control;
import org.apache.poi.xssf.model.Drawing;
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
	public static final XSSFRelation WORKBOOK = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/workbook",
			"/xl/workbook.xml",
			null
	);
	public static final XSSFRelation MACROS_WORKBOOK = new XSSFRelation(
			"application/vnd.ms-excel.sheet.macroEnabled.main+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument",
			"/xl/workbook.xml",
			null
	);
	public static final XSSFRelation WORKSHEET = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet",
			"/xl/worksheets/sheet#.xml",
			null
	);
	public static final XSSFRelation SHARED_STRINGS = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings",
			"/xl/sharedStrings.xml",
			SharedStringsTable.class
	);
	public static final XSSFRelation STYLES = new XSSFRelation(
		    "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml",
		    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles",
		    "/xl/styles.xml",
		    StylesTable.class
	);
	public static final XSSFRelation DRAWINGS = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing",
			"/xl/drawings/drawing#.xml",
			null
	);
	public static final XSSFRelation VML_DRAWINGS = new XSSFRelation(
			"application/vnd.openxmlformats-officedocument.vmlDrawing",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing",
			"/xl/drawings/vmlDrawing#.vml",
			null
	);
    public static final XSSFRelation IMAGES = new XSSFRelation(
    		"image/x-emf", // TODO
     		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
    		"/xl/media/image#.emf",
    		null
    );
	public static final XSSFRelation SHEET_COMMENTS = new XSSFRelation(
		    "application/vnd.openxmlformats-officedocument.spreadsheetml.comments+xml",
		    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
		    "/xl/comments#.xml",
		    CommentsTable.class
	);
	public static final XSSFRelation SHEET_HYPERLINKS = new XSSFRelation(
		    null,
		    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
		    null,
		    null
	);
	public static final XSSFRelation OLEEMBEDDINGS = new XSSFRelation(
	        null,
	        OLE_OBJECT_REL_TYPE,
	        null,
	        BinaryPart.class
	);
	public static final XSSFRelation PACKEMBEDDINGS = new XSSFRelation(
            null,
            PACK_OBJECT_REL_TYPE,
            null,
            BinaryPart.class
    );

	public static final XSSFRelation VBA_MACROS = new XSSFRelation(
            "application/vnd.ms-office.vbaProject",
            "http://schemas.microsoft.com/office/2006/relationships/vbaProject",
            "/xl/vbaProject.bin",
	        BinaryPart.class
    );
	public static final XSSFRelation ACTIVEX_CONTROLS = new XSSFRelation(
			"application/vnd.ms-office.activeX+xml",
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/control",
			"/xl/activeX/activeX#.xml",
			null
	);
	public static final XSSFRelation ACTIVEX_BINS = new XSSFRelation(
			"application/vnd.ms-office.activeX",
			"http://schemas.microsoft.com/office/2006/relationships/activeXControlBinary",
			"/xl/activeX/activeX#.bin",
	        BinaryPart.class
	);	
	
   
	public static class XSSFRelation {
		private String TYPE;
		private String REL;
		private String DEFAULT_NAME;
		private Class<? extends XSSFModel> CLASS;
		private XSSFRelation(String TYPE, String REL, String DEFAULT_NAME, Class<? extends XSSFModel> CLASS) {
			this.TYPE = TYPE;
			this.REL = REL;
			this.DEFAULT_NAME = DEFAULT_NAME;
			this.CLASS = CLASS;
		}
		public String getContentType() { return TYPE; }
		public String getRelation() { return REL; }
		public String getDefaultFileName() { return DEFAULT_NAME; }
		
		/**
		 * Does one of these exist for the given core
		 *  package part?
		 */
		public boolean exists(PackagePart corePart) throws IOException, InvalidFormatException {
			if(corePart == null) {
				// new file, can't exist
				return false;
			}
			
            PackageRelationshipCollection prc =
            	corePart.getRelationshipsByType(REL);
            Iterator<PackageRelationship> it = prc.iterator();
            if(it.hasNext()) {
            	return true;
            } else {
            	return false;
            }
		}
		
		/**
		 * Returns the filename for the nth one of these, 
		 *  eg /xl/comments4.xml
		 */
		public String getFileName(int index) {
			if(DEFAULT_NAME.indexOf("#") == -1) {
				// Generic filename in all cases
				return getDefaultFileName();
			}
			return DEFAULT_NAME.replace("#", Integer.toString(index));
		}

		/**
		 * Fetches the InputStream to read the contents, based
		 *  of the specified core part
		 */
		public InputStream getContents(PackagePart corePart) throws IOException, InvalidFormatException {
            PackageRelationshipCollection prc =
            	corePart.getRelationshipsByType(REL);
            Iterator<PackageRelationship> it = prc.iterator();
            if(it.hasNext()) {
                PackageRelationship rel = it.next();
                PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
                PackagePart part = corePart.getPackage().getPart(relName);
                return part.getInputStream();
            } else {
            	log.log(POILogger.WARN, "No part " + DEFAULT_NAME + " found");
            	return null;
            }
		}
		/**
		 * Load, off the specified core part
		 */
		public XSSFModel load(PackagePart corePart) throws Exception {
			Constructor<? extends XSSFModel> c = CLASS.getConstructor(InputStream.class);
			XSSFModel model = null;
			
			InputStream inp = getContents(corePart);
			if(inp != null) {
                try {
                	model = c.newInstance(inp);
                } finally {
                	inp.close();
                }
            }
            return model;
		}
		
		/**
		 * Save, with the default name
		 * @return The internal reference ID it was saved at, normally then used as an r:id
		 */
		private String save(XSSFModel model, PackagePart corePart) throws IOException {
			return save(model, corePart, DEFAULT_NAME);
		}
		/**
		 * Save, with the specified name
		 * @return The internal reference ID it was saved at, normally then used as an r:id
		 */
		private String save(XSSFModel model, PackagePart corePart, String name) throws IOException {
            PackagePartName ppName = null;
            try {
            	ppName = PackagingURIHelper.createPartName(name);
            } catch(InvalidFormatException e) {
            	throw new IllegalStateException(e);
            }
            PackageRelationship rel =
            	corePart.addRelationship(ppName, TargetMode.INTERNAL, REL);
            PackagePart part = corePart.getPackage().createPart(ppName, TYPE);
            
            OutputStream out = part.getOutputStream();
            model.writeTo(out);
            out.close();
            
            return rel.getId();
		}
	}

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
    public XSSFWorkbook(Package pkg) throws IOException {
        super(pkg);
        try {
            WorkbookDocument doc = WorkbookDocument.Factory.parse(getCorePart().getInputStream());
            this.workbook = doc.getWorkbook();
            
            // Are we macro enabled, or just normal?
            isMacroEnabled = 
            		getCorePart().getContentType().equals(MACROS_WORKBOOK.getContentType());
            
            try {
	            // Load shared strings
	            this.sharedStringSource = (SharedStringSource)
	            	SHARED_STRINGS.load(getCorePart());
            } catch(Exception e) {
            	throw new IOException("Unable to load shared strings - " + e.toString());
            }
            try {
	            // Load styles source
	            this.stylesSource = (StylesSource)
	            	STYLES.load(getCorePart());
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
                
                // Get the comments for the sheet, if there are any
                CommentsSource comments = null;
                PackageRelationshipCollection commentsRel =
                	part.getRelationshipsByType(SHEET_COMMENTS.REL);
                if(commentsRel != null && commentsRel.size() > 0) {
                	PackagePart commentsPart = 
                		getTargetPart(commentsRel.getRelationship(0));
                	comments = new CommentsTable(commentsPart.getInputStream());
                }
                
                // Get the drawings for the sheet, if there are any
                ArrayList<Drawing> drawings = new ArrayList<Drawing>();
                for(PackageRelationship rel : part.getRelationshipsByType(VML_DRAWINGS.REL)) {
                	PackagePart drawingPart = getTargetPart(rel);
                	Drawing drawing = new Drawing(drawingPart.getInputStream(), rel.getId());
                	drawing.findChildren(drawingPart);
                	drawings.add(drawing);
                }
                
                // Get the activeX controls for the sheet, if there are any
                ArrayList<Control> controls = new ArrayList<Control>();
                for(PackageRelationship rel : part.getRelationshipsByType(ACTIVEX_CONTROLS.REL)) {
                	PackagePart controlPart = getTargetPart(rel);
                	Control control = new Control(controlPart.getInputStream(), rel.getId());
                	control.findChildren(controlPart);
                	controls.add(control);
                }
                
                // Now create the sheet
                WorksheetDocument worksheetDoc = WorksheetDocument.Factory.parse(part.getInputStream());
                XSSFSheet sheet = new XSSFSheet(ctSheet, worksheetDoc.getWorksheet(), this, comments, drawings, controls);
                this.sheets.add(sheet);
                
                // Process external hyperlinks for the sheet,
                //  if there are any
                PackageRelationshipCollection hyperlinkRels =
                	part.getRelationshipsByType(SHEET_HYPERLINKS.REL);
                sheet.initHyperlinks(hyperlinkRels);
                
                // Get the embeddings for the workbook
                for(PackageRelationship rel : part.getRelationshipsByType(OLEEMBEDDINGS.REL))
                    embedds.add(getTargetPart(rel)); // TODO: Add this reference to each sheet as well
                
                for(PackageRelationship rel : part.getRelationshipsByType(PACKEMBEDDINGS.REL))
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
    	return new XSSFCellStyle(stylesSource);
    }

    public DataFormat createDataFormat() {
    	return getCreationHelper().createDataFormat();
    }

    public Font createFont() {
        // TODO Auto-generated method stub
        return null;
    }

    public XSSFName createName() {
    	XSSFName name = new XSSFName(this);
    	namedRanges.add(name);
    	return name;
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
                PackageRelationshipCollection prc = sheetPart.getRelationshipsByType(DRAWINGS.getRelation());
                for (PackageRelationship rel : prc) {
                    PackagePart drawingPart = getTargetPart(rel);
                    PackageRelationshipCollection prc2 = drawingPart.getRelationshipsByType(IMAGES.getRelation());
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
        // TODO Auto-generated method stub
        return 0;
    }

    public short getNumberOfFonts() {
        // TODO Auto-generated method stub
        return 0;
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

    public String getSSTString(int index) {
        return getSharedStringSource().getSharedStringAt(index);
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
    
    public SheetReferences getSheetReferences() {
    	SheetReferences sr = new SheetReferences();
    	for(int i=0; i<getNumberOfSheets(); i++) {
    		sr.addSheetReference(getSheetName(i), i);
    	}
    	return sr;
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
    	// What kind of workbook are we?
    	XSSFRelation workbookRelation = WORKBOOK;
    	if(isMacroEnabled) {
    		workbookRelation = MACROS_WORKBOOK;
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
                		WORKSHEET.getFileName(sheetNumber));
                PackageRelationship rel =
                	 corePart.addRelationship(partName, TargetMode.INTERNAL, WORKSHEET.getRelation(), "rSheet" + sheetNumber);
                PackagePart part = pkg.createPart(partName, WORKSHEET.getContentType());
                 
                // XXX This should not be needed, but apparently the setSaveOuter call above does not work in XMLBeans 2.2
                xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));
                sheet.save(part, xmlOptions);
                 
                // Update our internal reference for the package part
                workbook.getSheets().getSheetArray(i).setId(rel.getId());
                workbook.getSheets().getSheetArray(i).setSheetId(sheetNumber);
                
                // If our sheet has comments, then write out those
                if(sheet.hasComments()) {
                	CommentsTable ct = (CommentsTable)sheet.getCommentsSourceIfExists();
                    PackagePartName ctName = PackagingURIHelper.createPartName(
                    		SHEET_COMMENTS.getFileName(sheetNumber));
                    part.addRelationship(ctName, TargetMode.INTERNAL, SHEET_COMMENTS.getRelation(), "rComments");
                    PackagePart ctPart = pkg.createPart(ctName, SHEET_COMMENTS.getContentType());
                    
                    out = ctPart.getOutputStream();
                    ct.writeTo(out);
                    out.close();
                }
                
                // If our sheet has drawings, then write out those
                if(sheet.getDrawings() != null) {
                	int drawingIndex = 1;
                	for(Drawing drawing : sheet.getDrawings()) {
                        PackagePartName drName = PackagingURIHelper.createPartName(
                        		VML_DRAWINGS.getFileName(drawingIndex));
                        part.addRelationship(drName, TargetMode.INTERNAL, VML_DRAWINGS.getRelation(), drawing.getOriginalId());
                        PackagePart drPart = pkg.createPart(drName, VML_DRAWINGS.getContentType());
                        
                        drawing.writeChildren(drPart);
                        out = drPart.getOutputStream();
                        drawing.writeTo(out);
                        out.close();
                		drawingIndex++;
                	}
                }
                
                // If our sheet has controls, then write out those
                if(sheet.getControls() != null) {
                	int controlIndex = 1;
                	for(Control control : sheet.getControls()) {
                        PackagePartName crName = PackagingURIHelper.createPartName(
                        		ACTIVEX_CONTROLS.getFileName(controlIndex));
                        part.addRelationship(crName, TargetMode.INTERNAL, ACTIVEX_CONTROLS.getRelation(), control.getOriginalId());
                        PackagePart crPart = pkg.createPart(crName, ACTIVEX_CONTROLS.getContentType());
                        
                        control.writeChildren(crPart);
                        out = crPart.getOutputStream();
                        control.writeTo(out);
                        out.close();
                		controlIndex++;
                	}
                }
            }
             
            // Write shared strings and styles
            if(sharedStringSource != null) {
	             SharedStringsTable sst = (SharedStringsTable)sharedStringSource;
	             SHARED_STRINGS.save(sst, corePart);
            }
            if(stylesSource != null) {
	             StylesTable st = (StylesTable)stylesSource;
	             STYLES.save(st, corePart);
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
	            if(VBA_MACROS.exists( getCorePart() )) {
	            	try {
		            	XSSFModel vba = VBA_MACROS.load(getCorePart());
		            	VBA_MACROS.save(vba, corePart);
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
