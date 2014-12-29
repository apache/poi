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

import static org.apache.poi.xssf.usermodel.helpers.XSSFPaswordHelper.setPassword;
import static org.apache.poi.xssf.usermodel.helpers.XSSFPaswordHelper.validatePassword;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;

import org.apache.poi.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.formula.udf.IndexedUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.*;
import org.apache.poi.xssf.XLSBUnsupportedException;
import org.apache.poi.xssf.model.*;
import org.apache.poi.xssf.usermodel.helpers.XSSFFormulaUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookView;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookViews;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedNames;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDialogsheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCache;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCaches;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheets;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCalcMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STSheetState;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;

/**
 * High level representation of a SpreadsheetML workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 */
public class XSSFWorkbook extends POIXMLDocument implements Workbook, Iterable<XSSFSheet> {
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    /**
     * Width of one character of the default font in pixels. Same for Calibry and Arial.
     */
    public static final float DEFAULT_CHARACTER_WIDTH = 7.0017f;

    /**
     * Excel silently truncates long sheet names to 31 chars.
     * This constant is used to ensure uniqueness in the first 31 chars
     */
    private static final int MAX_SENSITIVE_SHEET_NAME_LEN = 31;

    /**
     * Images formats supported by XSSF but not by HSSF
     */
    public static final int PICTURE_TYPE_GIF = 8;
    public static final int PICTURE_TYPE_TIFF = 9;
    public static final int PICTURE_TYPE_EPS = 10;
    public static final int PICTURE_TYPE_BMP = 11;
    public static final int PICTURE_TYPE_WPG = 12;

    /**
     * The underlying XML bean
     */
    private CTWorkbook workbook;

    /**
     * this holds the XSSFSheet objects attached to this workbook
     */
    private List<XSSFSheet> sheets;

    /**
     * this holds the XSSFName objects attached to this workbook
     */
    private List<XSSFName> namedRanges;

    /**
     * shared string table - a cache of strings in this workbook
     */
    private SharedStringsTable sharedStringSource;

    /**
     * shared strings table type- to specify use default or map db shared strings table source
     */
    private SharedStringsTableType sharedStringsTableType = SharedStringsTableType.DEFAULT_SST;

    /**
     * A collection of shared objects used for styling content,
     * e.g. fonts, cell styles, colors, etc.
     */
    private StylesTable stylesSource;

    private ThemesTable theme;

    /**
     * The locator of user-defined functions.
     * By default includes functions from the Excel Analysis Toolpack
     */
    private IndexedUDFFinder _udfFinder = new IndexedUDFFinder(UDFFinder.DEFAULT);

    /**
     * TODO
     */
    private CalculationChain calcChain;
    
    /**
     * External Links, for referencing names or cells in other workbooks.
     */
    private List<ExternalLinksTable> externalLinks;

    /**
     * A collection of custom XML mappings
     */
    private MapInfo mapInfo;

    /**
     * Used to keep track of the data formatter so that all
     * createDataFormatter calls return the same one for a given
     * book.  This ensures that updates from one places is visible
     * someplace else.
     */
    private XSSFDataFormat formatter;

    /**
     * The policy to apply in the event of missing or
     *  blank cells when fetching from a row.
     * See {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy}
     */
    private MissingCellPolicy _missingCellPolicy = Row.RETURN_NULL_AND_BLANK;

    /**
     * array of pictures for this workbook
     */
    private List<XSSFPictureData> pictures;

    private static POILogger logger = POILogFactory.getLogger(XSSFWorkbook.class);

    /**
     * cached instance of XSSFCreationHelper for this workbook
     * @see {@link #getCreationHelper()}
     */
    private XSSFCreationHelper _creationHelper;

    /**
     * List of all pivot tables in workbook
     */
    private List<XSSFPivotTable> pivotTables;
    private List<CTPivotCache> pivotCaches;


    /**
     * Create a new SpreadsheetML workbook.
     */
    public XSSFWorkbook() {
        super(newPackage());
        onWorkbookCreate();
    }

    public XSSFWorkbook(SharedStringsTableType sharedStringsTableType) {
        super(newPackage());
        this.sharedStringsTableType = sharedStringsTableType;
        onWorkbookCreate();
    }

    public SharedStringsTableType getSharedStringsTableType() {
        return sharedStringsTableType;
    }

    /**
     * Constructs a XSSFWorkbook object given a OpenXML4J <code>Package</code> object,
     *  see <a href="http://poi.apache.org/oxml4j/">http://poi.apache.org/oxml4j/</a>.
     * 
     * <p>Once you have finished working with the Workbook, you should close the package
     * by calling either {@link #close()} or {@link OPCPackage#close()}, to avoid
     * leaving file handles open.
     * 
     * <p>Creating a XSSFWorkbook from a file-backed OPC Package has a lower memory
     *  footprint than an InputStream backed one.
     *
     * @param pkg the OpenXML4J <code>OPC Package</code> object.
     */
    public XSSFWorkbook(OPCPackage pkg) throws IOException {
        super(pkg);
        
        beforeDocumentRead();
        
        // Build a tree of POIXMLDocumentParts, this workbook being the root
        load(XSSFFactory.getInstance());
    }

    /**
     * Constructs a XSSFWorkbook object, by buffering the whole stream into memory
     *  and then opening an {@link OPCPackage} object for it.
     * 
     * <p>Using an {@link InputStream} requires more memory than using a File, so
     *  if a {@link File} is available then you should instead do something like
     *   <pre><code>
     *       OPCPackage pkg = OPCPackage.open(path);
     *       XSSFWorkbook wb = new XSSFWorkbook(pkg);
     *       // work with the wb object
     *       ......
     *       pkg.close(); // gracefully closes the underlying zip file
     *   </code></pre>
     */
    public XSSFWorkbook(InputStream is) throws IOException {
        super(PackageHelper.open(is));

        beforeDocumentRead();
        
        // Build a tree of POIXMLDocumentParts, this workbook being the root
        load(XSSFFactory.getInstance());
    }

    /**
     * Constructs a XSSFWorkbook object from a given file.
     * 
     * <p>Once you have finished working with the Workbook, you should close 
     * the package by calling  {@link #close()}, to avoid leaving file 
     * handles open.
     * 
     * <p>Opening a XSSFWorkbook from a file has a lower memory footprint 
     *  than opening from an InputStream
     *  
     * @param file   the file to open
     */
    public XSSFWorkbook(File file) throws IOException, InvalidFormatException {
        this(OPCPackage.open(file));
    }
    
    /**
     * Constructs a XSSFWorkbook object given a file name.
     *
     * 
     * <p>Once you have finished working with the Workbook, you should close 
     * the package by calling  {@link #close()}, to avoid leaving file 
     * handles open.
     * 
     * <p>Opening a XSSFWorkbook from a file has a lower memory footprint 
     *  than opening from an InputStream
     *
     * @param path   the file name.
     */
    public XSSFWorkbook(String path) throws IOException {
        this(openPackage(path));
    }
    
    protected void beforeDocumentRead() {
        // Ensure it isn't a XLSB file, which we don't support
        if (getCorePart().getContentType().equals(XSSFRelation.XLSB_BINARY_WORKBOOK.getContentType())) {
            throw new XLSBUnsupportedException();
        }

        // Create arrays for parts attached to the workbook itself
        pivotTables = new ArrayList<XSSFPivotTable>();
        pivotCaches = new ArrayList<CTPivotCache>();
    }

    @Override
    @SuppressWarnings("deprecation") //  getXYZArray() array accessors are deprecated
    protected void onDocumentRead() throws IOException {
        try {
            WorkbookDocument doc = WorkbookDocument.Factory.parse(getPackagePart().getInputStream());
            this.workbook = doc.getWorkbook();

            Map<String, XSSFSheet> shIdMap = new HashMap<String, XSSFSheet>();
            Map<String, ExternalLinksTable> elIdMap = new HashMap<String, ExternalLinksTable>();
            for(POIXMLDocumentPart p : getRelations()){
                if(p instanceof SharedStringsTable) sharedStringSource = (SharedStringsTable)p;
                else if(p instanceof StylesTable) stylesSource = (StylesTable)p;
                else if(p instanceof ThemesTable) theme = (ThemesTable)p;
                else if(p instanceof CalculationChain) calcChain = (CalculationChain)p;
                else if(p instanceof MapInfo) mapInfo = (MapInfo)p;
                else if (p instanceof XSSFSheet) {
                    shIdMap.put(p.getPackageRelationship().getId(), (XSSFSheet)p);
                }
                else if (p instanceof ExternalLinksTable) {
                    elIdMap.put(p.getPackageRelationship().getId(), (ExternalLinksTable)p);
                }
            }
            
            if (stylesSource == null) {
                // Create Styles if it is missing
                stylesSource = (StylesTable)createRelationship(XSSFRelation.STYLES, XSSFFactory.getInstance());
            }
            stylesSource.setTheme(theme);

            if (sharedStringSource == null) {
                // Create SST if it is missing
                sharedStringSource = createSSTSourceBasedOnSSTType();
            }
            
            // Load individual sheets. The order of sheets is defined by the order
            //  of CTSheet elements in the workbook
            sheets = new ArrayList<XSSFSheet>(shIdMap.size());
            for (CTSheet ctSheet : this.workbook.getSheets().getSheetArray()) {
                XSSFSheet sh = shIdMap.get(ctSheet.getId());
                if(sh == null) {
                    logger.log(POILogger.WARN, "Sheet with name " + ctSheet.getName() + " and r:id " + ctSheet.getId()+ " was defined, but didn't exist in package, skipping");
                    continue;
                }
                sh.sheet = ctSheet;
                sh.onDocumentRead();
                sheets.add(sh);
            }
            
            // Load the external links tables. Their order is defined by the order 
            //  of CTExternalReference elements in the workbook
            externalLinks = new ArrayList<ExternalLinksTable>(elIdMap.size());
            if (this.workbook.isSetExternalReferences()) {
                for (CTExternalReference er : this.workbook.getExternalReferences().getExternalReferenceArray()) {
                    ExternalLinksTable el = elIdMap.get(er.getId());
                    if(el == null) {
                        logger.log(POILogger.WARN, "ExternalLinksTable with r:id " + er.getId()+ " was defined, but didn't exist in package, skipping");
                        continue;
                    }
                    externalLinks.add(el);
                }
            }
            
            // Process the named ranges
            reprocessNamedRanges();
        } catch (XmlException e) {
            throw new POIXMLException(e);
        }
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        if (getSharedStringsTableType() == SharedStringsTableType.DEFAULT_SST) {
            super.write(stream);
        } else {
            writeWithPatchingMDBSST(stream);
        }
    }

    public void writeWithPatchingMDBSST(OutputStream stream) throws IOException {
        //Save the template
        File tmplFile = TempFile.createTempFile("poi-sxssf-template", ".xlsx");
        try {
            FileOutputStream os = new FileOutputStream(tmplFile);
            try {
                super.write(os);
            } finally {
                os.close();
            }

            //Substitute the template shared string xml with the temporarily generated xml data file
            injectSharedStringTableXml(tmplFile, stream);
        } finally {
            if (!tmplFile.delete()) {
                throw new IOException("Could not delete temporary file after processing: " + tmplFile);
            }
        }
    }

    private void injectSharedStringTableXml(File zipfile, OutputStream out) throws IOException {
        ZipFile zip = new ZipFile(zipfile);
        DBMappedSharedStringsTable _sst = (DBMappedSharedStringsTable) sharedStringSource;
        try {
            ZipOutputStream zos = new ZipOutputStream(out);
            try {
                Enumeration<? extends ZipEntry> en = zip.entries();
                while (en.hasMoreElements()) {
                    ZipEntry ze = en.nextElement();
                    zos.putNextEntry(new ZipEntry(ze.getName()));
                    InputStream is;
                    if (ze.getName().equals("xl/sharedStrings.xml")) {
                        is = _sst.getSharedStringInputStream(); //injecting shared string table in target output
                    } else {
                        is = zip.getInputStream(ze);
                    }
                    copyStream(is, zos);
                    is.close();
                }
            } finally {
                zos.close();
                if (!_sst.getTemp_shared_string_file().delete()) {
                    throw new RuntimeException("Couldn't delete temporary shared strings table file.");
                }
            }
        } finally {
            zip.close();
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >= 0) {
            out.write(chunk, 0, count);
        }
    }

    /**
     * Create a new CTWorkbook with all values set to default
     */
    private void onWorkbookCreate() {
        workbook = CTWorkbook.Factory.newInstance();

        // don't EVER use the 1904 date system
        CTWorkbookPr workbookPr = workbook.addNewWorkbookPr();
        workbookPr.setDate1904(false);

        CTBookViews bvs = workbook.addNewBookViews();
        CTBookView bv = bvs.addNewWorkbookView();
        bv.setActiveTab(0);
        workbook.addNewSheets();

        POIXMLProperties.ExtendedProperties expProps = getProperties().getExtendedProperties();
        expProps.getUnderlyingProperties().setApplication(DOCUMENT_CREATOR);

        sharedStringSource = createSSTSourceBasedOnSSTType();
        stylesSource = (StylesTable) createRelationship(XSSFRelation.STYLES, XSSFFactory.getInstance());

        namedRanges = new ArrayList<XSSFName>();
        sheets = new ArrayList<XSSFSheet>();
        pivotTables = new ArrayList<XSSFPivotTable>();
    }

    private SharedStringsTable createSSTSourceBasedOnSSTType() {
        return (SharedStringsTable) createRelationship(XSSFRelation.SHARED_STRINGS, new POIXMLFactory() {
            @Override
            public POIXMLDocumentPart createDocumentPart(POIXMLDocumentPart parent, PackageRelationship rel, PackagePart part) {
                try {
                    Class<? extends POIXMLDocumentPart> cls = sharedStringsTableType.getInstance();
                    Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor(PackagePart.class, PackageRelationship.class);
                    return constructor.newInstance(part, rel);
                } catch (Exception e) {
                    throw new POIXMLException(e);
                }
            }

            @Override
            public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor) {
                try {
                    Class<? extends POIXMLDocumentPart> cls = sharedStringsTableType.getInstance();
                    Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor();
                    return constructor.newInstance();
                } catch (Exception e) {
                    throw new POIXMLException(e);
                }
            }
        });
    }

    /**
     * Create a new SpreadsheetML package and setup the default minimal content
     */
    protected static OPCPackage newPackage() {
        try {
            OPCPackage pkg = OPCPackage.create(new ByteArrayOutputStream());
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName(XSSFRelation.WORKBOOK.getDefaultFileName());
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT);
            // Create main document part
            pkg.createPart(corePartName, XSSFRelation.WORKBOOK.getContentType());

            pkg.getPackageProperties().setCreatorProperty(DOCUMENT_CREATOR);

            return pkg;
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Return the underlying XML bean
     *
     * @return the underlying CTWorkbook bean
     */
    @Internal
    public CTWorkbook getCTWorkbook() {
        return this.workbook;
    }

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.
     *
     * @return the index to this picture (0 based), the added picture can be obtained from {@link #getAllPictures()} .
     * @see Workbook#PICTURE_TYPE_EMF
     * @see Workbook#PICTURE_TYPE_WMF
     * @see Workbook#PICTURE_TYPE_PICT
     * @see Workbook#PICTURE_TYPE_JPEG
     * @see Workbook#PICTURE_TYPE_PNG
     * @see Workbook#PICTURE_TYPE_DIB
     * @see #getAllPictures()
     */
    @Override
    public int addPicture(byte[] pictureData, int format) {
        int imageNumber = getAllPictures().size() + 1;
        XSSFPictureData img = (XSSFPictureData)createRelationship(XSSFPictureData.RELATIONS[format], XSSFFactory.getInstance(), imageNumber, true);
        try {
            OutputStream out = img.getPackagePart().getOutputStream();
            out.write(pictureData);
            out.close();
        } catch (IOException e){
            throw new POIXMLException(e);
        }
        pictures.add(img);
        return imageNumber - 1;
    }

    /**
     * Adds a picture to the workbook.
     *
     * @param is                The sream to read image from
     * @param format            The format of the picture.
     *
     * @return the index to this picture (0 based), the added picture can be obtained from {@link #getAllPictures()} .
     * @see Workbook#PICTURE_TYPE_EMF
     * @see Workbook#PICTURE_TYPE_WMF
     * @see Workbook#PICTURE_TYPE_PICT
     * @see Workbook#PICTURE_TYPE_JPEG
     * @see Workbook#PICTURE_TYPE_PNG
     * @see Workbook#PICTURE_TYPE_DIB
     * @see #getAllPictures()
     */
    public int addPicture(InputStream is, int format) throws IOException {
        int imageNumber = getAllPictures().size() + 1;
        XSSFPictureData img = (XSSFPictureData)createRelationship(XSSFPictureData.RELATIONS[format], XSSFFactory.getInstance(), imageNumber, true);
        OutputStream out = img.getPackagePart().getOutputStream();
        IOUtils.copy(is, out);
        out.close();
        pictures.add(img);
        return imageNumber - 1;
    }

    /**
     * Create an XSSFSheet from an existing sheet in the XSSFWorkbook.
     *  The cloned sheet is a deep copy of the original.
     *
     * @return XSSFSheet representing the cloned sheet.
     * @throws IllegalArgumentException if the sheet index in invalid
     * @throws POIXMLException if there were errors when cloning
     */
    @Override
    public XSSFSheet cloneSheet(int sheetNum) {
        validateSheetIndex(sheetNum);

        XSSFSheet srcSheet = sheets.get(sheetNum);
        String srcName = srcSheet.getSheetName();
        String clonedName = getUniqueSheetName(srcName);

        XSSFSheet clonedSheet = createSheet(clonedName);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            srcSheet.write(out);
            clonedSheet.read(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e){
            throw new POIXMLException("Failed to clone sheet", e);
        }
        CTWorksheet ct = clonedSheet.getCTWorksheet();
        if(ct.isSetLegacyDrawing()) {
            logger.log(POILogger.WARN, "Cloning sheets with comments is not yet supported.");
            ct.unsetLegacyDrawing();
        }
        if (ct.isSetPageSetup()) {
            logger.log(POILogger.WARN, "Cloning sheets with page setup is not yet supported.");
            ct.unsetPageSetup();
        }

        clonedSheet.setSelected(false);

        // copy sheet's relations
        List<POIXMLDocumentPart> rels = srcSheet.getRelations();
        // if the sheet being cloned has a drawing then rememebr it and re-create tpoo
        XSSFDrawing dg = null;
        for(POIXMLDocumentPart r : rels) {
            // do not copy the drawing relationship, it will be re-created
            if(r instanceof XSSFDrawing) {
                dg = (XSSFDrawing)r;
                continue;
            }

            PackageRelationship rel = r.getPackageRelationship();
            clonedSheet.getPackagePart().addRelationship(
                    rel.getTargetURI(), rel.getTargetMode(),rel.getRelationshipType());
            clonedSheet.addRelation(rel.getId(), r);
        }

        // clone the sheet drawing alongs with its relationships
        if (dg != null) {
            if(ct.isSetDrawing()) {
                // unset the existing reference to the drawing,
                // so that subsequent call of clonedSheet.createDrawingPatriarch() will create a new one
                ct.unsetDrawing();
            }
            XSSFDrawing clonedDg = clonedSheet.createDrawingPatriarch();
            // copy drawing contents
            clonedDg.getCTDrawing().set(dg.getCTDrawing());

            // Clone drawing relations
            List<POIXMLDocumentPart> srcRels = srcSheet.createDrawingPatriarch().getRelations();
            for (POIXMLDocumentPart rel : srcRels) {
                PackageRelationship relation = rel.getPackageRelationship();
                clonedSheet
                        .createDrawingPatriarch()
                        .getPackagePart()
                        .addRelationship(relation.getTargetURI(), relation.getTargetMode(),
                                relation.getRelationshipType(), relation.getId());
            }
        }
        return clonedSheet;
    }

    /**
     * Generate a valid sheet name based on the existing one. Used when cloning sheets.
     *
     * @param srcName the original sheet name to
     * @return clone sheet name
     */
    private String getUniqueSheetName(String srcName) {
        int uniqueIndex = 2;
        String baseName = srcName;
        int bracketPos = srcName.lastIndexOf('(');
        if (bracketPos > 0 && srcName.endsWith(")")) {
            String suffix = srcName.substring(bracketPos + 1, srcName.length() - ")".length());
            try {
                uniqueIndex = Integer.parseInt(suffix.trim());
                uniqueIndex++;
                baseName = srcName.substring(0, bracketPos).trim();
            } catch (NumberFormatException e) {
                // contents of brackets not numeric
            }
        }
        while (true) {
            // Try and find the next sheet name that is unique
            String index = Integer.toString(uniqueIndex++);
            String name;
            if (baseName.length() + index.length() + 2 < 31) {
                name = baseName + " (" + index + ")";
            } else {
                name = baseName.substring(0, 31 - index.length() - 2) + "(" + index + ")";
            }

            //If the sheet name is unique, then set it otherwise move on to the next number.
            if (getSheetIndex(name) == -1) {
                return name;
            }
        }
    }

    /**
     * Create a new XSSFCellStyle and add it to the workbook's style table
     *
     * @return the new XSSFCellStyle object
     */
    @Override
    public XSSFCellStyle createCellStyle() {
        return stylesSource.createCellStyle();
    }

    /**
     * Returns the instance of XSSFDataFormat for this workbook.
     *
     * @return the XSSFDataFormat object
     * @see org.apache.poi.ss.usermodel.DataFormat
     */
    @Override
    public XSSFDataFormat createDataFormat() {
        if (formatter == null)
            formatter = new XSSFDataFormat(stylesSource);
        return formatter;
    }

    /**
     * Create a new Font and add it to the workbook's font table
     *
     * @return new font object
     */
    @Override
    public XSSFFont createFont() {
        XSSFFont font = new XSSFFont();
        font.registerTo(stylesSource);
        return font;
    }

    @Override
    public XSSFName createName() {
        CTDefinedName ctName = CTDefinedName.Factory.newInstance();
        ctName.setName("");
        XSSFName name = new XSSFName(ctName, this);
        namedRanges.add(name);
        return name;
    }

    /**
     * Create an XSSFSheet for this workbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return XSSFSheet representing the new sheet.
     */
    @Override
    public XSSFSheet createSheet() {
        String sheetname = "Sheet" + (sheets.size());
        int idx = 0;
        while(getSheet(sheetname) != null) {
            sheetname = "Sheet" + idx;
            idx++;
        }
        return createSheet(sheetname);
    }

    /**
     * Create a new sheet for this Workbook and return the high level representation.
     * Use this to create new sheets.
     *
     * <p>
     *     Note that Excel allows sheet names up to 31 chars in length but other applications
     *     (such as OpenOffice) allow more. Some versions of Excel crash with names longer than 31 chars,
     *     others - truncate such names to 31 character.
     * </p>
     * <p>
     *     POI's SpreadsheetAPI silently truncates the input argument to 31 characters.
     *     Example:
     *
     *     <pre><code>
     *     Sheet sheet = workbook.createSheet("My very long sheet name which is longer than 31 chars"); // will be truncated
     *     assert 31 == sheet.getSheetName().length();
     *     assert "My very long sheet name which i" == sheet.getSheetName();
     *     </code></pre>
     * </p>
     *
     * Except the 31-character constraint, Excel applies some other rules:
     * <p>
     * Sheet name MUST be unique in the workbook and MUST NOT contain the any of the following characters:
     * <ul>
     * <li> 0x0000 </li>
     * <li> 0x0003 </li>
     * <li> colon (:) </li>
     * <li> backslash (\) </li>
     * <li> asterisk (*) </li>
     * <li> question mark (?) </li>
     * <li> forward slash (/) </li>
     * <li> opening square bracket ([) </li>
     * <li> closing square bracket (]) </li>
     * </ul>
     * The string MUST NOT begin or end with the single quote (') character.
     * </p>
     *
     * <p>
     * See {@link org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)}
     *      for a safe way to create valid names
     * </p>
     * @param sheetname  sheetname to set for the sheet.
     * @return Sheet representing the new sheet.
     * @throws IllegalArgumentException if the name is null or invalid
     *  or workbook already contains a sheet with this name
     * @see org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)
     */
    @Override
    public XSSFSheet createSheet(String sheetname) {
        if (sheetname == null) {
            throw new IllegalArgumentException("sheetName must not be null");
        }

        if (containsSheet( sheetname, sheets.size() ))
               throw new IllegalArgumentException( "The workbook already contains a sheet of this name");

        // YK: Mimic Excel and silently truncate sheet names longer than 31 characters
        if(sheetname.length() > 31) sheetname = sheetname.substring(0, 31);
        WorkbookUtil.validateSheetName(sheetname);

        CTSheet sheet = addSheet(sheetname);

        int sheetNumber = 1;
        for(XSSFSheet sh : sheets) sheetNumber = (int)Math.max(sh.sheet.getSheetId() + 1, sheetNumber);

        XSSFSheet wrapper = (XSSFSheet)createRelationship(XSSFRelation.WORKSHEET, XSSFFactory.getInstance(), sheetNumber);
        wrapper.sheet = sheet;
        sheet.setId(wrapper.getPackageRelationship().getId());
        sheet.setSheetId(sheetNumber);
        if(sheets.size() == 0) wrapper.setSelected(true);
        sheets.add(wrapper);
        return wrapper;
    }

    protected XSSFDialogsheet createDialogsheet(String sheetname, CTDialogsheet dialogsheet) {
        XSSFSheet sheet = createSheet(sheetname);
        return new XSSFDialogsheet(sheet);
    }

    private CTSheet addSheet(String sheetname) {
        CTSheet sheet = workbook.getSheets().addNewSheet();
        sheet.setName(sheetname);
        return sheet;
    }

    /**
     * Finds a font that matches the one with the supplied attributes
     */
    @Override
    public XSSFFont findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        return stylesSource.findFont(boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
    }

    /**
     * Convenience method to get the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     */
    @Override
    public int getActiveSheetIndex() {
        //activeTab (Active Sheet Index) Specifies an unsignedInt
        //that contains the index to the active sheet in this book view.
        return (int)workbook.getBookViews().getWorkbookViewArray(0).getActiveTab();
    }

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link XSSFPictureData} objects.)
     * @see #addPicture(byte[], int)
     */
    @Override
    public List<XSSFPictureData> getAllPictures() {
        if(pictures == null){
            List<PackagePart> mediaParts = getPackage().getPartsByName(Pattern.compile("/xl/media/.*?"));
            pictures = new ArrayList<XSSFPictureData>(mediaParts.size());
            for(PackagePart part : mediaParts){
                pictures.add(new XSSFPictureData(part, null));
            }
        }
        return pictures; //YK: should return Collections.unmodifiableList(pictures);
    }

    /**
     * Get the cell style object at the given index
     *
     * @param idx  index within the set of styles
     * @return XSSFCellStyle object at the index
     */
    @Override
    public XSSFCellStyle getCellStyleAt(short idx) {
        return stylesSource.getStyleAt(idx);
    }

    /**
     * Get the font at the given index number
     *
     * @param idx  index number
     * @return XSSFFont at the index
     */
    @Override
    public XSSFFont getFontAt(short idx) {
        return stylesSource.getFontAt(idx);
    }

    @Override
    public XSSFName getName(String name) {
        int nameIndex = getNameIndex(name);
        if (nameIndex < 0) {
            return null;
        }
        return namedRanges.get(nameIndex);
    }

    @Override
    public XSSFName getNameAt(int nameIndex) {
        int nNames = namedRanges.size();
        if (nNames < 1) {
            throw new IllegalStateException("There are no defined names in this workbook");
        }
        if (nameIndex < 0 || nameIndex > nNames) {
            throw new IllegalArgumentException("Specified name index " + nameIndex
                    + " is outside the allowable range (0.." + (nNames-1) + ").");
        }
        return namedRanges.get(nameIndex);
    }

    /**
     * Gets the named range index by his name
     * <i>Note:</i>Excel named ranges are case-insensitive and
     * this method performs a case-insensitive search.
     *
     * @param name named range name
     * @return named range index
     */
    @Override
    public int getNameIndex(String name) {
        int i = 0;
        for(XSSFName nr : namedRanges) {
            if(nr.getNameName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Get the number of styles the workbook contains
     *
     * @return count of cell styles
     */
    @Override
    public short getNumCellStyles() {
        return (short) (stylesSource).getNumCellStyles();
    }

    /**
     * Get the number of fonts in the this workbook
     *
     * @return number of fonts
     */
    @Override
    public short getNumberOfFonts() {
        return (short)stylesSource.getFonts().size();
    }

    /**
     * Get the number of named ranges in the this workbook
     *
     * @return number of named ranges
     */
    @Override
    public int getNumberOfNames() {
        return namedRanges.size();
    }

    /**
     * Get the number of worksheets in the this workbook
     *
     * @return number of worksheets
     */
    @Override
    public int getNumberOfSheets() {
        return sheets.size();
    }

    /**
     * Retrieves the reference for the printarea of the specified sheet, the sheet name is appended to the reference even if it was not specified.
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @return String Null if no print area has been defined
     */
    @Override
    public String getPrintArea(int sheetIndex) {
        XSSFName name = getBuiltInName(XSSFName.BUILTIN_PRINT_AREA, sheetIndex);
        if (name == null) return null;
        //adding one here because 0 indicates a global named region; doesnt make sense for print areas
        return name.getRefersToFormula();

    }

    /**
     * Get sheet with the given name (case insensitive match)
     *
     * @param name of the sheet
     * @return XSSFSheet with the name provided or <code>null</code> if it does not exist
     */
    @Override
    public XSSFSheet getSheet(String name) {
        for (XSSFSheet sheet : sheets) {
            if (name.equalsIgnoreCase(sheet.getSheetName())) {
                return sheet;
            }
        }
        return null;
    }

    /**
     * Get the XSSFSheet object at the given index.
     *
     * @param index of the sheet number (0-based physical & logical)
     * @return XSSFSheet at the provided index
     * @throws IllegalArgumentException if the index is out of range (index
     *            &lt; 0 || index &gt;= getNumberOfSheets()).
     */
    @Override
    public XSSFSheet getSheetAt(int index) {
        validateSheetIndex(index);
        return sheets.get(index);
    }

    /**
     * Returns the index of the sheet by his name (case insensitive match)
     *
     * @param name the sheet name
     * @return index of the sheet (0 based) or <tt>-1</tt if not found
     */
    @Override
    public int getSheetIndex(String name) {
        for (int i = 0 ; i < sheets.size() ; ++i) {
            XSSFSheet sheet = sheets.get(i);
            if (name.equalsIgnoreCase(sheet.getSheetName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the given sheet
     *
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based). <tt>-1</tt> if not found
     */
    @Override
    public int getSheetIndex(Sheet sheet) {
        int idx = 0;
        for(XSSFSheet sh : sheets){
            if(sh == sheet) return idx;
            idx++;
        }
        return -1;
    }

    /**
     * Get the sheet name
     *
     * @param sheetIx Number
     * @return Sheet name
     */
    @Override
    public String getSheetName(int sheetIx) {
        validateSheetIndex(sheetIx);
        return sheets.get(sheetIx).getSheetName();
    }

    /**
     * Allows foreach loops:
     * <pre><code>
     * XSSFWorkbook wb = new XSSFWorkbook(package);
     * for(XSSFSheet sheet : wb){
     *
     * }
     * </code></pre>
     */
    @Override
    public Iterator<XSSFSheet> iterator() {
        return sheets.iterator();
    }
    /**
     * Are we a normal workbook (.xlsx), or a
     *  macro enabled workbook (.xlsm)?
     */
    public boolean isMacroEnabled() {
        return getPackagePart().getContentType().equals(XSSFRelation.MACROS_WORKBOOK.getContentType());
    }

    @Override
    public void removeName(int nameIndex) {
        namedRanges.remove(nameIndex);
    }

    @Override
    public void removeName(String name) {
        for (int i = 0; i < namedRanges.size(); i++) {
            XSSFName nm = namedRanges.get(i);
            if(nm.getNameName().equalsIgnoreCase(name)) {
                removeName(i);
                return;
            }
        }
        throw new IllegalArgumentException("Named range was not found: " + name);
    }


    /**
     * As {@link #removeName(String)} is not necessarily unique
     * (name + sheet index is unique), this method is more accurate.
     *
     * @param name the name to remove.
     */
    void removeName(XSSFName name) {
        if (!namedRanges.remove(name)) {
            throw new IllegalArgumentException("Name was not found: " + name);
        }
    }


    /**
     * Delete the printarea for the sheet specified
     *
     * @param sheetIndex 0-based sheet index (0 = First Sheet)
     */
    @Override
    public void removePrintArea(int sheetIndex) {
        int cont = 0;
        for (XSSFName name : namedRanges) {
            if (name.getNameName().equals(XSSFName.BUILTIN_PRINT_AREA) && name.getSheetIndex() == sheetIndex) {
                namedRanges.remove(cont);
                break;
            }
            cont++;
        }
    }

    /**
     * Removes sheet at the given index.<p/>
     *
     * Care must be taken if the removed sheet is the currently active or only selected sheet in
     * the workbook. There are a few situations when Excel must have a selection and/or active
     * sheet. (For example when printing - see Bug 40414).<br/>
     *
     * This method makes sure that if the removed sheet was active, another sheet will become
     * active in its place.  Furthermore, if the removed sheet was the only selected sheet, another
     * sheet will become selected.  The newly active/selected sheet will have the same index, or
     * one less if the removed sheet was the last in the workbook.
     *
     * @param index of the sheet  (0-based)
     */
    @Override
    public void removeSheetAt(int index) {
        validateSheetIndex(index);

        onSheetDelete(index);

        XSSFSheet sheet = getSheetAt(index);
        removeRelation(sheet);
        sheets.remove(index);
    }

    /**
     * Gracefully remove references to the sheet being deleted
     *
     * @param index the 0-based index of the sheet to delete
     */
    private void onSheetDelete(int index) {
        //delete the CTSheet reference from workbook.xml
        workbook.getSheets().removeSheet(index);

        //calculation chain is auxiliary, remove it as it may contain orphan references to deleted cells
        if(calcChain != null) {
            removeRelation(calcChain);
            calcChain = null;
        }

        //adjust indices of names ranges
        for (Iterator<XSSFName> it = namedRanges.iterator(); it.hasNext();) {
            XSSFName nm = it.next();
            CTDefinedName ct = nm.getCTName();
            if(!ct.isSetLocalSheetId()) continue;
            if (ct.getLocalSheetId() == index) {
                it.remove();
            } else if (ct.getLocalSheetId() > index){
                // Bump down by one, so still points at the same sheet
                ct.setLocalSheetId(ct.getLocalSheetId()-1);
            }
        }
    }

    /**
     * Retrieves the current policy on what to do when
     *  getting missing or blank cells from a row.
     * The default is to return blank and null cells.
     *  {@link MissingCellPolicy}
     */
    @Override
    public MissingCellPolicy getMissingCellPolicy() {
        return _missingCellPolicy;
    }
    /**
     * Sets the policy on what to do when
     *  getting missing or blank cells from a row.
     * This will then apply to all calls to
     *  {@link Row#getCell(int)}}. See
     *  {@link MissingCellPolicy}
     */
    @Override
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        _missingCellPolicy = missingCellPolicy;
    }

    /**
     * Convenience method to set the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     */
    @Override
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    public void setActiveSheet(int index) {

        validateSheetIndex(index);

        for (CTBookView arrayBook : workbook.getBookViews().getWorkbookViewArray()) {
            arrayBook.setActiveTab(index);
        }
    }

    /**
     * Validate sheet index
     *
     * @param index the index to validate
     * @throws IllegalArgumentException if the index is out of range (index
     *            &lt; 0 || index &gt;= getNumberOfSheets()).
    */
    private void validateSheetIndex(int index) {
        int lastSheetIx = sheets.size() - 1;
        if (index < 0 || index > lastSheetIx) {
            String range = "(0.." +    lastSheetIx + ")";
            if (lastSheetIx == -1) {
                range = "(no sheets)";
            }
            throw new IllegalArgumentException("Sheet index ("
                    + index +") is out of range " + range);
        }
    }

    /**
     * Gets the first tab that is displayed in the list of tabs in excel.
     *
     * @return integer that contains the index to the active sheet in this book view.
     */
    @Override
    public int getFirstVisibleTab() {
        CTBookViews bookViews = workbook.getBookViews();
        CTBookView bookView = bookViews.getWorkbookViewArray(0);
        return (short) bookView.getActiveTab();
    }

    /**
     * Sets the first tab that is displayed in the list of tabs in excel.
     *
     * @param index integer that contains the index to the active sheet in this book view.
     */
    @Override
    public void setFirstVisibleTab(int index) {
        CTBookViews bookViews = workbook.getBookViews();
        CTBookView bookView= bookViews.getWorkbookViewArray(0);
        bookView.setActiveTab(index);
    }

    /**
     * Sets the printarea for the sheet provided
     * <p>
     * i.e. Reference = $A$1:$B$2
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @param reference Valid name Reference for the Print Area
     */
    @Override
    public void setPrintArea(int sheetIndex, String reference) {
        XSSFName name = getBuiltInName(XSSFName.BUILTIN_PRINT_AREA, sheetIndex);
        if (name == null) {
            name = createBuiltInName(XSSFName.BUILTIN_PRINT_AREA, sheetIndex);
        }
        //short externSheetIndex = getWorkbook().checkExternSheet(sheetIndex);
        //name.setExternSheetNumber(externSheetIndex);
        String[] parts = COMMA_PATTERN.split(reference);
        StringBuffer sb = new StringBuffer(32);
        for (int i = 0; i < parts.length; i++) {
            if(i>0) {
                sb.append(",");
            }
            SheetNameFormatter.appendFormat(sb, getSheetName(sheetIndex));
            sb.append("!");
            sb.append(parts[i]);
        }
        name.setRefersToFormula(sb.toString());
    }

    /**
     * For the Convenience of Java Programmers maintaining pointers.
     * @see #setPrintArea(int, String)
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     * @param startColumn Column to begin printarea
     * @param endColumn Column to end the printarea
     * @param startRow Row to begin the printarea
     * @param endRow Row to end the printarea
     */
    @Override
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        String reference=getReferencePrintArea(getSheetName(sheetIndex), startColumn, endColumn, startRow, endRow);
        setPrintArea(sheetIndex, reference);
    }


    /**
     * Sets the repeating rows and columns for a sheet.
     * <p/>
     * To set just repeating columns:
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,0,1,-1,-1);
     * </pre>
     * To set just repeating rows:
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,-1,-1,0,4);
     * </pre>
     * To remove all repeating rows and columns for a sheet.
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,-1,-1,-1,-1);
     * </pre>
     *
     * @param sheetIndex  0 based index to sheet.
     * @param startColumn 0 based start of repeating columns.
     * @param endColumn   0 based end of repeating columns.
     * @param startRow    0 based start of repeating rows.
     * @param endRow      0 based end of repeating rows.
     *
     * @deprecated use {@link XSSFSheet#setRepeatingRows(CellRangeAddress)}
     *        or {@link XSSFSheet#setRepeatingColumns(CellRangeAddress)}
     */
    @Deprecated
    @Override
    public void setRepeatingRowsAndColumns(int sheetIndex,
                                           int startColumn, int endColumn,
                                           int startRow, int endRow) {
      XSSFSheet sheet = getSheetAt(sheetIndex);

      CellRangeAddress rows = null;
      CellRangeAddress cols = null;

      if (startRow != -1) {
        rows = new CellRangeAddress(startRow, endRow, -1, -1);
      }
      if (startColumn != -1) {
        cols = new CellRangeAddress(-1, -1, startColumn, endColumn);
      }

      sheet.setRepeatingRows(rows);
      sheet.setRepeatingColumns(cols);
    }

    private static String getReferencePrintArea(String sheetName, int startC, int endC, int startR, int endR) {
        //windows excel example: Sheet1!$C$3:$E$4
        CellReference colRef = new CellReference(sheetName, startR, startC, true, true);
        CellReference colRef2 = new CellReference(sheetName, endR, endC, true, true);

        return "$" + colRef.getCellRefParts()[2] + "$" + colRef.getCellRefParts()[1] + ":$" + colRef2.getCellRefParts()[2] + "$" + colRef2.getCellRefParts()[1];
    }

    XSSFName getBuiltInName(String builtInCode, int sheetNumber) {
        for (XSSFName name : namedRanges) {
            if (name.getNameName().equalsIgnoreCase(builtInCode) && name.getSheetIndex() == sheetNumber) {
                return name;
            }
        }
        return null;
    }

    /**
     * Generates a NameRecord to represent a built-in region
     *
     * @return a new NameRecord
     * @throws IllegalArgumentException if sheetNumber is invalid
     * @throws POIXMLException if such a name already exists in the workbook
     */
    XSSFName createBuiltInName(String builtInName, int sheetNumber) {
        validateSheetIndex(sheetNumber);

        CTDefinedNames names = workbook.getDefinedNames() == null ? workbook.addNewDefinedNames() : workbook.getDefinedNames();
        CTDefinedName nameRecord = names.addNewDefinedName();
        nameRecord.setName(builtInName);
        nameRecord.setLocalSheetId(sheetNumber);

        XSSFName name = new XSSFName(nameRecord, this);
        for (XSSFName nr : namedRanges) {
            if (nr.equals(name))
                throw new POIXMLException("Builtin (" + builtInName
                        + ") already exists for sheet (" + sheetNumber + ")");
        }

        namedRanges.add(name);
        return name;
    }

    /**
     * We only set one sheet as selected for compatibility with HSSF.
     */
    @Override
    public void setSelectedTab(int index) {
        for (int i = 0 ; i < sheets.size() ; ++i) {
            XSSFSheet sheet = sheets.get(i);
            sheet.setSelected(i == index);
        }
    }

    /**
     * Set the sheet name.
     *
     * @param sheetIndex sheet number (0 based)
     * @param sheetname  the new sheet name
     * @throws IllegalArgumentException if the name is null or invalid
     *  or workbook already contains a sheet with this name
     * @see #createSheet(String)
     * @see org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)
     */
    @Override
    public void setSheetName(int sheetIndex, String sheetname) {
        validateSheetIndex(sheetIndex);
        String oldSheetName = getSheetName(sheetIndex);

        // YK: Mimic Excel and silently truncate sheet names longer than 31 characters
        if(sheetname != null && sheetname.length() > 31) sheetname = sheetname.substring(0, 31);
        WorkbookUtil.validateSheetName(sheetname);
        // findbugs fix - validateSheetName has already checked for null value
        assert(sheetname != null); 

        // Do nothing if no change
        if (sheetname.equals(oldSheetName)) return;
        
        // Check it isn't already taken
        if (containsSheet(sheetname, sheetIndex ))
            throw new IllegalArgumentException( "The workbook already contains a sheet of this name" );

        // Update references to the name
        XSSFFormulaUtils utils = new XSSFFormulaUtils(this);
        utils.updateSheetName(sheetIndex, oldSheetName, sheetname);

        workbook.getSheets().getSheetArray(sheetIndex).setName(sheetname);
    }

    /**
     * sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */
    @Override
    @SuppressWarnings("deprecation")
    public void setSheetOrder(String sheetname, int pos) {
        int idx = getSheetIndex(sheetname);
        sheets.add(pos, sheets.remove(idx));
        // Reorder CTSheets
        CTSheets ct = workbook.getSheets();
        XmlObject cts = ct.getSheetArray(idx).copy();
        workbook.getSheets().removeSheet(idx);
        CTSheet newcts = ct.insertNewSheet(pos);
        newcts.set(cts);

        //notify sheets
        CTSheet[] sheetArray = ct.getSheetArray();
        for(int i=0; i < sheetArray.length; i++) {
            sheets.get(i).sheet = sheetArray[i];
        }
    }

    /**
     * marshal named ranges from the {@link #namedRanges} collection to the underlying CTWorkbook bean
     */
    private void saveNamedRanges(){
        // Named ranges
        if(namedRanges.size() > 0) {
            CTDefinedNames names = CTDefinedNames.Factory.newInstance();
            CTDefinedName[] nr = new CTDefinedName[namedRanges.size()];
            int i = 0;
            for(XSSFName name : namedRanges) {
                nr[i] = name.getCTName();
                i++;
            }
            names.setDefinedNameArray(nr);
            if(workbook.isSetDefinedNames()) {
                workbook.unsetDefinedNames();
            }
            workbook.setDefinedNames(names);
                        
            // Re-process the named ranges
            reprocessNamedRanges();
        } else {
            if(workbook.isSetDefinedNames()) {
                workbook.unsetDefinedNames();
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private void reprocessNamedRanges() {
        namedRanges = new ArrayList<XSSFName>();
        if(workbook.isSetDefinedNames()) {
            for(CTDefinedName ctName : workbook.getDefinedNames().getDefinedNameArray()) {
                namedRanges.add(new XSSFName(ctName, this));
            }
        }
    }

    private void saveCalculationChain(){
        if(calcChain != null){
            int count = calcChain.getCTCalcChain().sizeOfCArray();
            if(count == 0){
                removeRelation(calcChain);
                calcChain = null;
            }
        }
    }

    @Override
    protected void commit() throws IOException {
        saveNamedRanges();
        saveCalculationChain();

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorkbook.type.getName().getNamespaceURI(), "workbook"));
        Map<String, String> map = new HashMap<String, String>();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        xmlOptions.setSaveSuggestedPrefixes(map);

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        workbook.save(out, xmlOptions);
        out.close();
    }
    
    /**
     * Closes the underlying {@link OPCPackage} from which
     *  the Workbook was read, if any. Has no effect on newly 
     *  created Workbooks.
     */
    @Override
    public void close() throws IOException {
        super.close();
    }

    /**
     * Returns SharedStringsTable - tha cache of string for this workbook
     *
     * @return the shared string table
     */
    @Internal
    public SharedStringsTable getSharedStringSource() {
        return this.sharedStringSource;
    }

    /**
     * Return a object representing a collection of shared objects used for styling content,
     * e.g. fonts, cell styles, colors, etc.
     */
    public StylesTable getStylesSource() {
        return this.stylesSource;
    }

    /**
     * Returns the Theme of current workbook.
     */
    public ThemesTable getTheme() {
        return theme;
    }

    /**
     * Returns an object that handles instantiating concrete
     *  classes of the various instances for XSSF.
     */
    @Override
    public XSSFCreationHelper getCreationHelper() {
        if(_creationHelper == null) _creationHelper = new XSSFCreationHelper(this);
        return _creationHelper;
    }

    /**
     * Determines whether a workbook contains the provided sheet name.
     * For the purpose of comparison, long names are truncated to 31 chars.
     *
     * @param name the name to test (case insensitive match)
     * @param excludeSheetIdx the sheet to exclude from the check or -1 to include all sheets in the check.
     * @return true if the sheet contains the name, false otherwise.
     */
    @SuppressWarnings("deprecation") //  getXYZArray() array accessors are deprecated
    private boolean containsSheet(String name, int excludeSheetIdx) {
        CTSheet[] ctSheetArray = workbook.getSheets().getSheetArray();

        if (name.length() > MAX_SENSITIVE_SHEET_NAME_LEN) {
            name = name.substring(0, MAX_SENSITIVE_SHEET_NAME_LEN);
        }

        for (int i = 0; i < ctSheetArray.length; i++) {
            String ctName = ctSheetArray[i].getName();
            if (ctName.length() > MAX_SENSITIVE_SHEET_NAME_LEN) {
                ctName = ctName.substring(0, MAX_SENSITIVE_SHEET_NAME_LEN);
            }

            if (excludeSheetIdx != i && name.equalsIgnoreCase(ctName))
                return true;
        }
        return false;
    }

    /**
     * Gets a boolean value that indicates whether the date systems used in the workbook starts in 1904.
     * <p>
     * The default value is false, meaning that the workbook uses the 1900 date system,
     * where 1/1/1900 is the first day in the system..
     * </p>
     * @return true if the date systems used in the workbook starts in 1904
     */
    protected boolean isDate1904(){
        CTWorkbookPr workbookPr = workbook.getWorkbookPr();
        return workbookPr != null && workbookPr.getDate1904();
    }

    /**
     * Get the document's embedded files.
     */
    @Override
	public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
        List<PackagePart> embedds = new LinkedList<PackagePart>();

        for(XSSFSheet sheet : sheets){
            // Get the embeddings for the workbook
            for(PackageRelationship rel : sheet.getPackagePart().getRelationshipsByType(XSSFRelation.OLEEMBEDDINGS.getRelation())) {
                embedds.add( sheet.getPackagePart().getRelatedPart(rel) );
            }

            for(PackageRelationship rel : sheet.getPackagePart().getRelationshipsByType(XSSFRelation.PACKEMBEDDINGS.getRelation())) {
               embedds.add( sheet.getPackagePart().getRelatedPart(rel) );
            }
        }
        return embedds;
    }

    @Override
    public boolean isHidden() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void setHidden(boolean hiddenFlag) {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Check whether a sheet is hidden.
     * <p>
     * Note that a sheet could instead be set to be very hidden, which is different
     *  ({@link #isSheetVeryHidden(int)})
     * </p>
     * @param sheetIx Number
     * @return <code>true</code> if sheet is hidden
     */
    @Override
    public boolean isSheetHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        CTSheet ctSheet = sheets.get(sheetIx).sheet;
        return ctSheet.getState() == STSheetState.HIDDEN;
    }

    /**
     * Check whether a sheet is very hidden.
     * <p>
     * This is different from the normal hidden status
     *  ({@link #isSheetHidden(int)})
     * </p>
     * @param sheetIx sheet index to check
     * @return <code>true</code> if sheet is very hidden
     */
    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        CTSheet ctSheet = sheets.get(sheetIx).sheet;
        return ctSheet.getState() == STSheetState.VERY_HIDDEN;
    }

    /**
     * Sets the visible state of this sheet.
     * <p>
     *   Calling <code>setSheetHidden(sheetIndex, true)</code> is equivalent to
     *   <code>setSheetHidden(sheetIndex, Workbook.SHEET_STATE_HIDDEN)</code>.
     * <br/>
     *   Calling <code>setSheetHidden(sheetIndex, false)</code> is equivalent to
     *   <code>setSheetHidden(sheetIndex, Workbook.SHEET_STATE_VISIBLE)</code>.
     * </p>
     *
     * @param sheetIx   the 0-based index of the sheet
     * @param hidden whether this sheet is hidden
     * @see #setSheetHidden(int, int)
     */
    @Override
    public void setSheetHidden(int sheetIx, boolean hidden) {
        setSheetHidden(sheetIx, hidden ? SHEET_STATE_HIDDEN : SHEET_STATE_VISIBLE);
    }

    /**
     * Hide or unhide a sheet.
     *
     * <ul>
     *  <li>0 - visible. </li>
     *  <li>1 - hidden. </li>
     *  <li>2 - very hidden.</li>
     * </ul>
     * @param sheetIx the sheet index (0-based)
     * @param state one of the following <code>Workbook</code> constants:
     *        <code>Workbook.SHEET_STATE_VISIBLE</code>,
     *        <code>Workbook.SHEET_STATE_HIDDEN</code>, or
     *        <code>Workbook.SHEET_STATE_VERY_HIDDEN</code>.
     * @throws IllegalArgumentException if the supplied sheet index or state is invalid
     */
    @Override
    public void setSheetHidden(int sheetIx, int state) {
        validateSheetIndex(sheetIx);
        WorkbookUtil.validateSheetState(state);
        CTSheet ctSheet = sheets.get(sheetIx).sheet;
        ctSheet.setState(STSheetState.Enum.forInt(state + 1));
    }

    /**
     * Fired when a formula is deleted from this workbook,
     * for example when calling cell.setCellFormula(null)
     *
     * @see XSSFCell#setCellFormula(String)
     */
    protected void onDeleteFormula(XSSFCell cell){
        if(calcChain != null) {
            int sheetId = (int)cell.getSheet().sheet.getSheetId();
            calcChain.removeItem(sheetId, cell.getReference());
        }
    }

    /**
     * Return the {@link CalculationChain} object for this workbook
     * <p>
     *   The calculation chain object specifies the order in which the cells in a workbook were last calculated
     * </p>
     *
     * @return the <code>CalculationChain</code> object or <code>null</code> if not defined
     */
    @Internal
    public CalculationChain getCalculationChain() {
        return calcChain;
    }
    
    /**
     * Returns the list of {@link ExternalLinksTable} object for this workbook
     * 
     * <p>The external links table specifies details of named ranges etc
     *  that are referenced from other workbooks, along with the last seen
     *  values of what they point to.</p>
     *
     * <p>Note that Excel uses index 0 for the current workbook, so the first
     *  External Links in a formula would be '[1]Foo' which corresponds to
     *  entry 0 in this list.</p>

     * @return the <code>ExternalLinksTable</code> list, which may be empty
     */
    @Internal
    public List<ExternalLinksTable> getExternalLinksTable() {
        return externalLinks;
    }

    /**
     *
     * @return a collection of custom XML mappings defined in this workbook
     */
    public Collection<XSSFMap> getCustomXMLMappings(){
        return mapInfo == null ? new ArrayList<XSSFMap>() : mapInfo.getAllXSSFMaps();
    }

    /**
     *
     * @return the helper class used to query the custom XML mapping defined in this workbook
     */
    @Internal
    public MapInfo getMapInfo(){
    	return mapInfo;
    }

    /**
     * Adds the External Link Table part and relations required to allow formulas 
     *  referencing the specified external workbook to be added to this one. Allows
     *  formulas such as "[MyOtherWorkbook.xlsx]Sheet3!$A$5" to be added to the 
     *  file, for workbooks not already linked / referenced.
     *
     * @param name The name the workbook will be referenced as in formulas
     * @param workbook The open workbook to fetch the link required information from
     */
    public int linkExternalWorkbook(String name, Workbook workbook) {
        throw new RuntimeException("Not Implemented - see bug #57184");
    }

	/**
	 * Specifies a boolean value that indicates whether structure of workbook is locked. <br/>
	 * A value true indicates the structure of the workbook is locked. Worksheets in the workbook can't be moved,
	 * deleted, hidden, unhidden, or renamed, and new worksheets can't be inserted.<br/>
	 * A value of false indicates the structure of the workbook is not locked.<br/>
	 *
	 * @return true if structure of workbook is locked
	 */
	public boolean isStructureLocked() {
		return workbookProtectionPresent() && workbook.getWorkbookProtection().getLockStructure();
	}

	/**
	 * Specifies a boolean value that indicates whether the windows that comprise the workbook are locked. <br/>
	 * A value of true indicates the workbook windows are locked. Windows are the same size and position each time the
	 * workbook is opened.<br/>
	 * A value of false indicates the workbook windows are not locked.
	 *
	 * @return true if windows that comprise the workbook are locked
	 */
	public boolean isWindowsLocked() {
		return workbookProtectionPresent() && workbook.getWorkbookProtection().getLockWindows();
	}

	/**
	 * Specifies a boolean value that indicates whether the workbook is locked for revisions.
	 *
	 * @return true if the workbook is locked for revisions.
	 */
	public boolean isRevisionLocked() {
		return workbookProtectionPresent() && workbook.getWorkbookProtection().getLockRevision();
	}

	/**
	 * Locks the structure of workbook.
	 */
	public void lockStructure() {
	    safeGetWorkbookProtection().setLockStructure(true);
	}

	/**
	 * Unlocks the structure of workbook.
	 */
	public void unLockStructure() {
	    safeGetWorkbookProtection().setLockStructure(false);
	}

	/**
	 * Locks the windows that comprise the workbook.
	 */
	public void lockWindows() {
	    safeGetWorkbookProtection().setLockWindows(true);
	}

	/**
	 * Unlocks the windows that comprise the workbook.
	 */
	public void unLockWindows() {
	    safeGetWorkbookProtection().setLockWindows(false);
	}

	/**
	 * Locks the workbook for revisions.
	 */
	public void lockRevision() {
	    safeGetWorkbookProtection().setLockRevision(true);
	}

	/**
	 * Unlocks the workbook for revisions.
	 */
	public void unLockRevision() {
	    safeGetWorkbookProtection().setLockRevision(false);
	}

	/**
	 * Sets the workbook password. 
	 * 
	 * @param password if null, the password will be removed
	 * @param hashAlgo if null, the password will be set as XOR password (Excel 2010 and earlier)
	 *  otherwise the given algorithm is used for calculating the hash password (Excel 2013)
	 */
	public void setWorkbookPassword(String password, HashAlgorithm hashAlgo) {
        if (password == null && !workbookProtectionPresent()) return;
        setPassword(safeGetWorkbookProtection(), password, hashAlgo, "workbook");
	}

    /**
     * Validate the password against the stored hash, the hashing method will be determined
     *  by the existing password attributes
     * @return true, if the hashes match (... though original password may differ ...)
     */
    public boolean validateWorkbookPassword(String password) {
        if (!workbookProtectionPresent()) return (password == null);
        return validatePassword(safeGetWorkbookProtection(), password, "workbook");
    }

    /**
     * Sets the revisions password.
     * 
     * @param password if null, the password will be removed
     * @param hashAlgo if null, the password will be set as XOR password (Excel 2010 and earlier)
     *  otherwise the given algorithm is used for calculating the hash password (Excel 2013)
     */
    public void setRevisionsPassword(String password, HashAlgorithm hashAlgo) {
        if (password == null && !workbookProtectionPresent()) return;
        setPassword(safeGetWorkbookProtection(), password, hashAlgo, "revisions");
    }

    /**
     * Validate the password against the stored hash, the hashing method will be determined
     *  by the existing password attributes
     * @return true if the hashes match (... though original password may differ ...)
     */
    public boolean validateRevisionsPassword(String password) {
        if (!workbookProtectionPresent()) return (password == null);
        return validatePassword(safeGetWorkbookProtection(), password, "revisions");
    }
    
    /**
     * Removes the workbook protection settings
     */
    public void unLock() {
        if (workbookProtectionPresent()) {
            workbook.unsetWorkbookProtection();
        }
    }
    
	private boolean workbookProtectionPresent() {
		return workbook.isSetWorkbookProtection();
	}

    private CTWorkbookProtection safeGetWorkbookProtection() {
        if (!workbookProtectionPresent()){
            return workbook.addNewWorkbookProtection();
        }
        return workbook.getWorkbookProtection();
    }
	
    /**
     *
     * Returns the locator of user-defined functions.
     * <p>
     * The default instance extends the built-in functions with the Excel Analysis Tool Pack.
     * To set / evaluate custom functions you need to register them as follows:
     *
     *
     *
     * </p>
     * @return wrapped instance of UDFFinder that allows seeking functions both by index and name
     */
    /*package*/ UDFFinder getUDFFinder() {
        return _udfFinder;
    }

    /**
     * Register a new toolpack in this workbook.
     *
     * @param toopack the toolpack to register
     */
    @Override
    public void addToolPack(UDFFinder toopack){
        _udfFinder.add(toopack);
    }

    /**
     * Whether the application shall perform a full recalculation when the workbook is opened.
     * <p>
     * Typically you want to force formula recalculation when you modify cell formulas or values
     * of a workbook previously created by Excel. When set to true, this flag will tell Excel
     * that it needs to recalculate all formulas in the workbook the next time the file is opened.
     * </p>
     * <p>
     * Note, that recalculation updates cached formula results and, thus, modifies the workbook.
     * Depending on the version, Excel may prompt you with "Do you want to save the changes in <em>filename</em>?"
     * on close.
     * </p>
     *
     * @param value true if the application will perform a full recalculation of
     * workbook values when the workbook is opened
     * @since 3.8
     */
    @Override
    public void setForceFormulaRecalculation(boolean value){
        CTWorkbook ctWorkbook = getCTWorkbook();
        CTCalcPr calcPr = ctWorkbook.isSetCalcPr() ? ctWorkbook.getCalcPr() : ctWorkbook.addNewCalcPr();
        // when set to 0, will tell Excel that it needs to recalculate all formulas
        // in the workbook the next time the file is opened.
        calcPr.setCalcId(0);

        if(value && calcPr.getCalcMode() == STCalcMode.MANUAL) {
            calcPr.setCalcMode(STCalcMode.AUTO);
        }
    }

    /**
     * Whether Excel will be asked to recalculate all formulas when the  workbook is opened.
     *
     * @since 3.8
     */
    @Override
    public boolean getForceFormulaRecalculation(){
        CTWorkbook ctWorkbook = getCTWorkbook();
        CTCalcPr calcPr = ctWorkbook.getCalcPr();
        return calcPr != null && calcPr.getCalcId() != 0;
    }



    /**
     * Add pivotCache to the workbook
     */
    @Beta
    protected CTPivotCache addPivotCache(String rId) {
        CTWorkbook ctWorkbook = getCTWorkbook();
        CTPivotCaches caches;
        if (ctWorkbook.isSetPivotCaches()) {
            caches = ctWorkbook.getPivotCaches();
        } else {
            caches = ctWorkbook.addNewPivotCaches();
        }
        CTPivotCache cache = caches.addNewPivotCache();

        int tableId = getPivotTables().size()+1;
        cache.setCacheId(tableId);
        cache.setId(rId);
        if(pivotCaches == null) {
            pivotCaches = new ArrayList<CTPivotCache>();
        }
        pivotCaches.add(cache);
        return cache;
    }

    @Beta
    public List<XSSFPivotTable> getPivotTables() {
        return pivotTables;
    }

    @Beta
    protected void setPivotTables(List<XSSFPivotTable> pivotTables) {
        this.pivotTables = pivotTables;
    }
}
