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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.xssf.usermodel.helpers.XSSFPasswordHelper.setPassword;
import static org.apache.poi.xssf.usermodel.helpers.XSSFPasswordHelper.validatePassword;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.util.PackageHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.IndexedUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Date1904Support;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Removal;
import org.apache.poi.xssf.XLSBUnsupportedException;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.apache.poi.xssf.model.MapInfo;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.helpers.XSSFFormulaUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
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
public class XSSFWorkbook extends POIXMLDocument implements Workbook, Date1904Support {
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

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
     * this holds the XSSFName objects attached to this workbook, keyed by lower-case name
     */
    private ListValuedMap<String, XSSFName> namedRangesByName;

    /**
     * this holds the XSSFName objects attached to this workbook
     */
    private List<XSSFName> namedRanges;

    /**
     * shared string table - a cache of strings in this workbook
     */
    private SharedStringsTable sharedStringSource;

    /**
     * A collection of shared objects used for styling content,
     * e.g. fonts, cell styles, colors, etc.
     */
    private StylesTable stylesSource;

    /**
     * The locator of user-defined functions.
     * By default includes functions from the Excel Analysis Toolpack
     */
    private final IndexedUDFFinder _udfFinder = new IndexedUDFFinder(AggregatingUDFFinder.DEFAULT);

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
    private MissingCellPolicy _missingCellPolicy = MissingCellPolicy.RETURN_NULL_AND_BLANK;

    /**
     * Whether a call to {@link XSSFCell#setCellFormula(String)} will validate the formula or not.
     */
    private boolean cellFormulaValidation = true;

    /**
     * array of pictures for this workbook
     */
    private List<XSSFPictureData> pictures;

    private static final POILogger logger = POILogFactory.getLogger(XSSFWorkbook.class);

    /**
     * cached instance of XSSFCreationHelper for this workbook
     * @see #getCreationHelper()
     */
    private XSSFCreationHelper _creationHelper;

    /**
     * List of all pivot tables in workbook
     */
    private List<XSSFPivotTable> pivotTables;
    private List<CTPivotCache> pivotCaches;

    private final XSSFFactory xssfFactory;

    /**
     * Create a new SpreadsheetML workbook.
     */
    public XSSFWorkbook() {
        this(XSSFWorkbookType.XLSX);
    }

    public XSSFWorkbook(XSSFFactory factory) {
        this(XSSFWorkbookType.XLSX, factory);
    }

    /**
     * Create a new SpreadsheetML workbook.
     * @param workbookType The type of workbook to make (.xlsx or .xlsm).
     */
    public XSSFWorkbook(XSSFWorkbookType workbookType) {
        this(workbookType, null);
    }

    private XSSFWorkbook(XSSFWorkbookType workbookType, XSSFFactory factory) {
        super(newPackage(workbookType));
        this.xssfFactory = (factory == null) ? XSSFFactory.getInstance() : factory;
        onWorkbookCreate();
    }

    /**
     * Constructs a XSSFWorkbook object given a OpenXML4J <code>Package</code> object,
     *  see <a href="https://poi.apache.org/oxml4j/">https://poi.apache.org/oxml4j/</a>.
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
        this.xssfFactory = XSSFFactory.getInstance();

        beforeDocumentRead();

        // Build a tree of POIXMLDocumentParts, this workbook being the root
        load(this.xssfFactory);

        // some broken Workbooks miss this...
        setBookViewsIfMissing();
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
        this(PackageHelper.open(is));
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

    /**
     * Constructs a XSSFWorkbook object using Package Part.
     * @param part  package part
     * @since POI 4.0.0
     */
    public XSSFWorkbook(PackagePart part) throws IOException {
        this(part.getInputStream());
    }

    protected void beforeDocumentRead() {
        // Ensure it isn't a XLSB file, which we don't support
        if (getCorePart().getContentType().equals(XSSFRelation.XLSB_BINARY_WORKBOOK.getContentType())) {
            throw new XLSBUnsupportedException();
        }

        // Create arrays for parts attached to the workbook itself
        pivotTables = new ArrayList<>();
        pivotCaches = new ArrayList<>();
    }

    @Override
    protected void onDocumentRead() throws IOException {
        try {
            WorkbookDocument doc = WorkbookDocument.Factory.parse(getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);
            this.workbook = doc.getWorkbook();

            ThemesTable theme = null;
            Map<String, XSSFSheet> shIdMap = new HashMap<>();
            Map<String, ExternalLinksTable> elIdMap = new HashMap<>();
            for(RelationPart rp : getRelationParts()){
                POIXMLDocumentPart p = rp.getDocumentPart();
                if(p instanceof SharedStringsTable) {
                    sharedStringSource = (SharedStringsTable)p;
                } else if(p instanceof StylesTable) {
                    stylesSource = (StylesTable)p;
                } else if(p instanceof ThemesTable) {
                    theme = (ThemesTable)p;
                } else if(p instanceof CalculationChain) {
                    calcChain = (CalculationChain)p;
                } else if(p instanceof MapInfo) {
                    mapInfo = (MapInfo)p;
                } else if (p instanceof XSSFSheet) {
                    shIdMap.put(rp.getRelationship().getId(), (XSSFSheet)p);
                } else if (p instanceof ExternalLinksTable) {
                    elIdMap.put(rp.getRelationship().getId(), (ExternalLinksTable)p);
                }
            }
            boolean packageReadOnly = (getPackage().getPackageAccess() == PackageAccess.READ);

            if (stylesSource == null) {
                // Create Styles if it is missing
                if (packageReadOnly) {
                    stylesSource = new StylesTable();
                } else {
                    stylesSource = (StylesTable)createRelationship(XSSFRelation.STYLES, this.xssfFactory);
                }
            }
            stylesSource.setWorkbook(this);
            stylesSource.setTheme(theme);

            if (sharedStringSource == null) {
                // Create SST if it is missing
                if (packageReadOnly) {
                    sharedStringSource = new SharedStringsTable();
                } else {
                    sharedStringSource = (SharedStringsTable)createRelationship(XSSFRelation.SHARED_STRINGS, this.xssfFactory);
                }
            }

            // Load individual sheets. The order of sheets is defined by the order
            //  of CTSheet elements in the workbook
            sheets = new ArrayList<>(shIdMap.size());
            for (CTSheet ctSheet : this.workbook.getSheets().getSheetArray()) {
                parseSheet(shIdMap, ctSheet);
            }

            // Load the external links tables. Their order is defined by the order
            //  of CTExternalReference elements in the workbook
            externalLinks = new ArrayList<>(elIdMap.size());
            if (this.workbook.isSetExternalReferences()) {
                for (CTExternalReference er : this.workbook.getExternalReferences().getExternalReferenceArray()) {
                    ExternalLinksTable el = elIdMap.get(er.getId());
                    if(el == null) {
                        logger.log(POILogger.WARN, "ExternalLinksTable with r:id ", er.getId(), " was defined, but didn't exist in package, skipping");
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

    /**
     * Not normally to be called externally, but possibly to be overridden to avoid
     * the DOM based parse of large sheets (see examples).
     */
    public void parseSheet(Map<String, XSSFSheet> shIdMap, CTSheet ctSheet) {
        XSSFSheet sh = shIdMap.get(ctSheet.getId());
        if(sh == null) {
            logger.log(POILogger.WARN, "Sheet with name ", ctSheet.getName(), " and r:id ",
                    ctSheet.getId(), " was defined, but didn't exist in package, skipping");
            return;
        }
        sh.sheet = ctSheet;
        sh.onDocumentRead();
        sheets.add(sh);
    }

    /**
     * Create a new CTWorkbook with all values set to default
     */
    private void onWorkbookCreate() {
        workbook = CTWorkbook.Factory.newInstance();

        // don't EVER use the 1904 date system
        CTWorkbookPr workbookPr = workbook.addNewWorkbookPr();
        workbookPr.setDate1904(false);

        setBookViewsIfMissing();
        workbook.addNewSheets();

        POIXMLProperties.ExtendedProperties expProps = getProperties().getExtendedProperties();
        expProps.getUnderlyingProperties().setApplication(DOCUMENT_CREATOR);

        sharedStringSource = (SharedStringsTable)createRelationship(XSSFRelation.SHARED_STRINGS, this.xssfFactory);
        stylesSource = (StylesTable)createRelationship(XSSFRelation.STYLES, this.xssfFactory);
        stylesSource.setWorkbook(this);

        namedRanges = new ArrayList<>();
        namedRangesByName = new ArrayListValuedHashMap<>();
        sheets = new ArrayList<>();
        pivotTables = new ArrayList<>();
    }

    private void setBookViewsIfMissing() {
        if(!workbook.isSetBookViews()) {
            CTBookViews bvs = workbook.addNewBookViews();
            CTBookView bv = bvs.addNewWorkbookView();
            bv.setActiveTab(0);
        }
    }

    /**
     * Create a new SpreadsheetML package and setup the default minimal content
     */
    protected static OPCPackage newPackage(XSSFWorkbookType workbookType) {
        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.create(new ByteArrayOutputStream());    // NOSONAR - we do not want to close this here
            // Main part
            PackagePartName corePartName = PackagingURIHelper.createPartName(XSSFRelation.WORKBOOK.getDefaultFileName());
            // Create main part relationship
            pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT);
            // Create main document part
            pkg.createPart(corePartName, workbookType.getContentType());

            pkg.getPackageProperties().setCreatorProperty(DOCUMENT_CREATOR);
        } catch (Exception e) {
            IOUtils.closeQuietly(pkg);
            throw new POIXMLException(e);
        }
        return pkg;
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
        XSSFPictureData img = createRelationship(XSSFPictureData.RELATIONS[format], this.xssfFactory, imageNumber, true).getDocumentPart();
        try (OutputStream out = img.getPackagePart().getOutputStream()) {
            out.write(pictureData);
        } catch (IOException e) {
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
        XSSFPictureData img = createRelationship(XSSFPictureData.RELATIONS[format], this.xssfFactory, imageNumber, true).getDocumentPart();
        try (OutputStream out = img.getPackagePart().getOutputStream()) {
            IOUtils.copy(is, out);
        }
        pictures.add(img);
        return imageNumber - 1;
    }

    /**
     * Create an XSSFSheet from an existing sheet in the XSSFWorkbook.
     *  The cloned sheet is a deep copy of the original.
     *
     * @param sheetNum The index of the sheet to clone
     * @return XSSFSheet representing the cloned sheet.
     * @throws IllegalArgumentException if the sheet index in invalid
     * @throws POIXMLException if there were errors when cloning
     */
    @Override
    public XSSFSheet cloneSheet(int sheetNum) {
        return cloneSheet(sheetNum, null);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            IOUtils.closeQuietly(sharedStringSource);
        }
    }

    /**
     * Create an XSSFSheet from an existing sheet in the XSSFWorkbook.
     *  The cloned sheet is a deep copy of the original but with a new given
     *  name.
     *
     * @param sheetNum The index of the sheet to clone
     * @param newName The name to set for the newly created sheet
     * @return XSSFSheet representing the cloned sheet.
     * @throws IllegalArgumentException if the sheet index or the sheet
     *         name is invalid
     * @throws POIXMLException if there were errors when cloning
     */
    public XSSFSheet cloneSheet(int sheetNum, String newName) {
        validateSheetIndex(sheetNum);
        XSSFSheet srcSheet = sheets.get(sheetNum);

        if (newName == null) {
            String srcName = srcSheet.getSheetName();
            newName = getUniqueSheetName(srcName);
        } else {
            validateSheetName(newName);
        }

        XSSFSheet clonedSheet = createSheet(newName);

        // copy sheet's relations
        List<RelationPart> rels = srcSheet.getRelationParts();
        // if the sheet being cloned has a drawing then remember it and re-create it too
        XSSFDrawing dg = null;
        for(RelationPart rp : rels) {
            POIXMLDocumentPart r = rp.getDocumentPart();
            // do not copy the drawing relationship, it will be re-created
            if(r instanceof XSSFDrawing) {
                dg = (XSSFDrawing)r;
                continue;
            }

            addRelation(rp, clonedSheet);
        }

        try {
            for(PackageRelationship pr : srcSheet.getPackagePart().getRelationships()) {
                if (pr.getTargetMode() == TargetMode.EXTERNAL) {
                    clonedSheet.getPackagePart().addExternalRelationship
                            (pr.getTargetURI().toASCIIString(), pr.getRelationshipType(), pr.getId());
                }
            }
        } catch (InvalidFormatException e) {
            throw new POIXMLException("Failed to clone sheet", e);
        }


        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            srcSheet.write(out);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray())) {
                clonedSheet.read(bis);
            }
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

        // clone the sheet drawing along with its relationships
        if (dg != null) {
            if(ct.isSetDrawing()) {
                // unset the existing reference to the drawing,
                // so that subsequent call of clonedSheet.createDrawingPatriarch() will create a new one
                ct.unsetDrawing();
            }
            XSSFDrawing clonedDg = clonedSheet.createDrawingPatriarch();
            // copy drawing contents
            clonedDg.getCTDrawing().set(dg.getCTDrawing().copy());

            // Clone drawing relations
            List<RelationPart> srcRels = srcSheet.createDrawingPatriarch().getRelationParts();
            for (RelationPart rp : srcRels) {
                addRelation(rp, clonedDg);
            }
        }
        return clonedSheet;
    }

    /**
     * @since 3.14-Beta1
     */
    private static void addRelation(RelationPart rp, POIXMLDocumentPart target) {
        PackageRelationship rel = rp.getRelationship();
        if (rel.getTargetMode() == TargetMode.EXTERNAL) {
            target.getPackagePart().addRelationship(
                    rel.getTargetURI(), rel.getTargetMode(), rel.getRelationshipType(), rel.getId());
        } else {
            XSSFRelation xssfRel = XSSFRelation.getInstance(rel.getRelationshipType());
            if (xssfRel == null) {
                // Don't copy all relations blindly, but only the ones we know about
                throw new POIXMLException("Can't clone sheet - unknown relation type found: "+rel.getRelationshipType());
            }
            target.addRelation(rel.getId(), xssfRel, rp.getDocumentPart());
        }
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
     * Returns the workbook's data format table (a factory for creating data format strings).
     *
     * @return the XSSFDataFormat object
     * @see org.apache.poi.ss.usermodel.DataFormat
     */
    @Override
    public XSSFDataFormat createDataFormat() {
        if (formatter == null) {
            formatter = new XSSFDataFormat(stylesSource);
        }
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
        return createAndStoreName(ctName);
    }

    private XSSFName createAndStoreName(CTDefinedName ctName) {
        XSSFName name = new XSSFName(ctName, this);
        namedRanges.add(name);
        namedRangesByName.put(ctName.getName().toLowerCase(Locale.ENGLISH), name);
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

        validateSheetName(sheetname);

        // YK: Mimic Excel and silently truncate sheet names longer than 31 characters
        if(sheetname.length() > 31) {
            sheetname = sheetname.substring(0, 31);
        }
        WorkbookUtil.validateSheetName(sheetname);

        CTSheet sheet = addSheet(sheetname);

        int sheetNumber = 1;
        outerloop:
        while(true) {
            for(XSSFSheet sh : sheets) {
                sheetNumber = (int)Math.max(sh.sheet.getSheetId() + 1, sheetNumber);
            }

            // Bug 57165: We also need to check that the resulting file name is not already taken
            // this can happen when moving/cloning sheets
            String sheetName = XSSFRelation.WORKSHEET.getFileName(sheetNumber);
            for(POIXMLDocumentPart relation : getRelations()) {
                if(relation.getPackagePart() != null &&
                        sheetName.equals(relation.getPackagePart().getPartName().getName())) {
                    // name is taken => try next one
                    sheetNumber++;
                    continue outerloop;
                }
            }

            // no duplicate found => use this one
            break;
        }

        RelationPart rp = createRelationship(XSSFRelation.WORKSHEET, this.xssfFactory, sheetNumber, false);
        XSSFSheet wrapper = rp.getDocumentPart();
        wrapper.sheet = sheet;
        sheet.setId(rp.getRelationship().getId());
        sheet.setSheetId(sheetNumber);
        if (sheets.isEmpty()) {
            wrapper.setSelected(true);
        }
        sheets.add(wrapper);
        return wrapper;
    }

    private void validateSheetName(final String sheetName) throws IllegalArgumentException {
        if (containsSheet( sheetName, sheets.size() )) {
            throw new IllegalArgumentException("The workbook already contains a sheet named '" + sheetName + "'");
        }
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
     *
     * @return the font with the matched attributes or <code>null</code>
     */
    @Override
    public XSSFFont findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        return stylesSource.findFont(bold, color, fontHeight, name, italic, strikeout, typeOffset, underline);
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
            pictures = new ArrayList<>(mediaParts.size());
            for(PackagePart part : mediaParts){
                pictures.add(new XSSFPictureData(part));
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
    public XSSFCellStyle getCellStyleAt(int idx) {
        return stylesSource.getStyleAt(idx);
    }

    @Override
    public XSSFFont getFontAt(int idx) {
        return stylesSource.getFontAt(idx);
    }

    /**
     * Get the first named range with the given name.
     *
     * Note: names of named ranges are not unique as they are scoped by sheet.
     * {@link #getNames(String name)} returns all named ranges with the given name.
     *
     * @param name  named range name
     * @return XSSFName with the given name. <code>null</code> is returned no named range could be found.
     */
    @Override
    public XSSFName getName(String name) {
        Collection<XSSFName> list = getNames(name);
        if (list.isEmpty()) {
            return null;
        }
        return list.iterator().next();
    }

    /**
     * Get the named ranges with the given name.
     * <i>Note:</i>Excel named ranges are case-insensitive and
     * this method performs a case-insensitive search.
     *
     * @param name  named range name
     * @return list of XSSFNames with the given name. An empty list if no named ranges could be found
     */
    @Override
    public List<XSSFName> getNames(String name) {
        return Collections.unmodifiableList(namedRangesByName.get(name.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Get the named range at the given index. No longer public and only used in tests.
     *
     * @param nameIndex the index of the named range
     * @return the XSSFName at the given index
     */
    @Deprecated
    XSSFName getNameAt(int nameIndex) {
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
     * Get a list of all the named ranges in the workbook.
     *
     * @return list of XSSFNames in the workbook
     */
    @Override
    public List<XSSFName> getAllNames() {
        return Collections.unmodifiableList(namedRanges);
    }

    /**
     * Gets the named range index by name. No longer public and only used in tests.
     *
     * @param name named range name
     * @return named range index. <code>-1</code> is returned if no named ranges could be found.
     *
     * @deprecated 3.16. New projects should avoid accessing named ranges by index.
     * Use {@link #getName(String)} instead.
     */
    @Deprecated
    int getNameIndex(String name) {
        XSSFName nm = getName(name);
        if (nm != null) {
            return namedRanges.indexOf(nm);
        }
        return -1;
    }

    /**
     * Get the number of styles the workbook contains
     *
     * @return count of cell styles
     */
    @Override
    public int getNumCellStyles() {
        return stylesSource.getNumCellStyles();
    }

    @Override
    public int getNumberOfFonts() {
        return stylesSource.getFonts().size();
    }

    @Override
    @Deprecated
    @Removal(version = "6.0.0")
    public int getNumberOfFontsAsInt() {
        return getNumberOfFonts();
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
        if (name == null) {
            return null;
        }
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
     * @param index of the sheet number (0-based physical &amp; logical)
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
     * @return index of the sheet (0 based) or <tt>-1</tt> if not found
     */
    @Override
    public int getSheetIndex(String name) {
        int idx = 0;
        for (XSSFSheet sh : sheets) {
            if (name.equalsIgnoreCase(sh.getSheetName())) {
                return idx;
            }
            idx++;
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
            if(sh == sheet) {
                return idx;
            }
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
     * Returns an iterator of the sheets in the workbook
     * in sheet order. Includes hidden and very hidden sheets.
     *
     * Note: remove() is not supported on this iterator.
     * Use {@link #removeSheetAt(int)} to remove sheets instead.
     *
     * @return an iterator of the sheets.
     */
    @Override
    public Iterator<Sheet> sheetIterator() {
        return new SheetIterator<>();
    }

    /**
     * Alias for {@link #sheetIterator()} to allow
     * foreach loops
     *
     * Note: remove() is not supported on this iterator.
     * Use {@link #removeSheetAt(int)} to remove sheets instead.
     *
     * @return an iterator of the sheets.
     */
    @Override
    public Iterator<Sheet> iterator() {
        return sheetIterator();
    }

    private final class SheetIterator<T extends Sheet> implements Iterator<T> {
        final private Iterator<T> it;
        @SuppressWarnings("unchecked")
        public SheetIterator() {
            it = (Iterator<T>) sheets.iterator();
        }
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }
        @Override
        public T next() throws NoSuchElementException {
            return it.next();
        }
        /**
         * Unexpected behavior may occur if sheets are reordered after iterator
         * has been created. Support for the remove method may be added in the future
         * if someone can figure out a reliable implementation.
         */
        @Override
        public void remove() throws IllegalStateException {
            throw new UnsupportedOperationException("remove method not supported on XSSFWorkbook.iterator(). "+
                    "Use Sheet.removeSheetAt(int) instead.");
        }
    }

    /**
     * Are we a normal workbook (.xlsx), or a
     *  macro enabled workbook (.xlsm)?
     */
    public boolean isMacroEnabled() {
        return getPackagePart().getContentType().equals(XSSFRelation.MACROS_WORKBOOK.getContentType());
    }

    /**
     * @param name the name to remove.
     *
     * @throws IllegalArgumentException if the named range is not a part of this XSSFWorkbook
     */
    @Override
    public void removeName(Name name) {
        if (!namedRangesByName.removeMapping(name.getNameName().toLowerCase(Locale.ENGLISH), name)
                || !namedRanges.remove(name)) {
            throw new IllegalArgumentException("Name was not found: " + name);
        }
    }

    void updateName(XSSFName name, String oldName) {
        if (!namedRangesByName.removeMapping(oldName.toLowerCase(Locale.ENGLISH), name)) {
            throw new IllegalArgumentException("Name was not found: " + name);
        }
        namedRangesByName.put(name.getNameName().toLowerCase(Locale.ENGLISH), name);
    }


    /**
     * Delete the printarea for the sheet specified
     *
     * @param sheetIndex 0-based sheet index (0 = First Sheet)
     */
    @Override
    public void removePrintArea(int sheetIndex) {
        XSSFName name = getBuiltInName(XSSFName.BUILTIN_PRINT_AREA, sheetIndex);
        if (name != null) {
            removeName(name);
        }
    }

    /**
     * Removes sheet at the given index.<p>
     *
     * Care must be taken if the removed sheet is the currently active or only selected sheet in
     * the workbook. There are a few situations when Excel must have a selection and/or active
     * sheet. (For example when printing - see Bug 40414).<br>
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

        // only set new sheet if there are still some left
        if(sheets.size() == 0) {
            return;
        }

        // the index of the closest remaining sheet to the one just deleted
        int newSheetIndex = index;
        if (newSheetIndex >= sheets.size()) {
            newSheetIndex = sheets.size()-1;
        }

        // adjust active sheet
        int active = getActiveSheetIndex();
        if(active == index) {
            // removed sheet was the active one, reset active sheet if there is still one left now
            setActiveSheet(newSheetIndex);
        } else if (active > index) {
            // removed sheet was below the active one => active is one less now
            setActiveSheet(active-1);
        }
    }

    /**
     * Gracefully remove references to the sheet being deleted
     *
     * @param index the 0-based index of the sheet to delete
     */
    private void onSheetDelete(int index) {
        // remove all sheet relations
        final XSSFSheet sheet = getSheetAt(index);

        sheet.onSheetDelete();

        //delete the CTSheet reference from workbook.xml
        workbook.getSheets().removeSheet(index);

        //calculation chain is auxiliary, remove it as it may contain orphan references to deleted cells
        if(calcChain != null) {
            removeRelation(calcChain);
            calcChain = null;
        }

        //adjust indices of names ranges
        List<XSSFName> toRemove = new ArrayList<>();
        for (XSSFName nm : namedRanges) {
            CTDefinedName ct = nm.getCTName();
            if(!ct.isSetLocalSheetId()) {
                continue;
            }
            if (ct.getLocalSheetId() == index) {
                toRemove.add(nm);
            } else if (ct.getLocalSheetId() > index){
                // Bump down by one, so still points at the same sheet
                ct.setLocalSheetId(ct.getLocalSheetId()-1);
            }
        }
        for (XSSFName nm : toRemove) {
            removeName(nm);
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
        return (short) bookView.getFirstSheet();
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
        bookView.setFirstSheet(index);
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
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < parts.length; i++) {
            if(i>0) {
                sb.append(',');
            }
            SheetNameFormatter.appendFormat(sb, getSheetName(sheetIndex));
            sb.append('!');
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

    private static String getReferencePrintArea(String sheetName, int startC, int endC, int startR, int endR) {
        //windows excel example: Sheet1!$C$3:$E$4
        CellReference colRef = new CellReference(sheetName, startR, startC, true, true);
        CellReference colRef2 = new CellReference(sheetName, endR, endC, true, true);

        return "$" + colRef.getCellRefParts()[2] + "$" + colRef.getCellRefParts()[1] + ":$" + colRef2.getCellRefParts()[2] + "$" + colRef2.getCellRefParts()[1];
    }

    XSSFName getBuiltInName(String builtInCode, int sheetNumber) {
        for (XSSFName name : namedRangesByName.get(builtInCode.toLowerCase(Locale.ENGLISH))) {
            if (name.getSheetIndex() == sheetNumber) {
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

        if (getBuiltInName(builtInName, sheetNumber) != null) {
            throw new POIXMLException("Builtin (" + builtInName
                    + ") already exists for sheet (" + sheetNumber + ")");
        }

        return createAndStoreName(nameRecord);
    }

    /**
     * We only set one sheet as selected for compatibility with HSSF.
     */
    @Override
    public void setSelectedTab(int index) {
        int idx = 0;
        for (XSSFSheet sh : sheets) {
            sh.setSelected(idx == index);
            idx++;
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
        if (sheetname == null) {
            throw new IllegalArgumentException( "sheetName must not be null" );
        }

        validateSheetIndex(sheetIndex);
        String oldSheetName = getSheetName(sheetIndex);

        // YK: Mimic Excel and silently truncate sheet names longer than 31 characters
        if(sheetname.length() > 31) {
            sheetname = sheetname.substring(0, 31);
        }
        WorkbookUtil.validateSheetName(sheetname);

        // Do nothing if no change
        if (sheetname.equals(oldSheetName)) {
            return;
        }

        // Check it isn't already taken
        if (containsSheet(sheetname, sheetIndex )) {
            throw new IllegalArgumentException( "The workbook already contains a sheet of this name" );
        }

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

        updateNamedRangesAfterSheetReorder(idx, pos);
        updateActiveSheetAfterSheetReorder(idx, pos);
    }

    /**
     * update sheet-scoped named ranges in this workbook after changing the sheet order
     * of a sheet at oldIndex to newIndex.
     * Sheets between these indices will move left or right by 1.
     *
     * @param oldIndex the original index of the re-ordered sheet
     * @param newIndex the new index of the re-ordered sheet
     */
    private void updateNamedRangesAfterSheetReorder(int oldIndex, int newIndex) {
        // update sheet index of sheet-scoped named ranges
        for (final XSSFName name : namedRanges) {
            final int i = name.getSheetIndex();
            // name has sheet-level scope
            if (i != -1) {
                // name refers to this sheet
                if (i == oldIndex) {
                    name.setSheetIndex(newIndex);
                }
                // if oldIndex > newIndex then this sheet moved left and sheets between newIndex and oldIndex moved right
                else if (newIndex <= i && i < oldIndex) {
                    name.setSheetIndex(i+1);
                }
                // if oldIndex < newIndex then this sheet moved right and sheets between oldIndex and newIndex moved left
                else if (oldIndex < i && i <= newIndex) {
                    name.setSheetIndex(i-1);
                }
            }
        }
    }

    private void updateActiveSheetAfterSheetReorder(int oldIndex, int newIndex) {
        // adjust active sheet if necessary
        int active = getActiveSheetIndex();
        if(active == oldIndex) {
            // moved sheet was the active one
            setActiveSheet(newIndex);
        } else if ((active < oldIndex && active < newIndex) ||
                (active > oldIndex && active > newIndex)) {
            // not affected
        } else if (newIndex > oldIndex) {
            // moved sheet was below before and is above now => active is one less
            setActiveSheet(active-1);
        } else {
            // remaining case: moved sheet was higher than active before and is lower now => active is one more
            setActiveSheet(active+1);
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

    private void reprocessNamedRanges() {
        namedRangesByName = new ArrayListValuedHashMap<>();
        namedRanges = new ArrayList<>();
        if(workbook.isSetDefinedNames()) {
            for(CTDefinedName ctName : workbook.getDefinedNames().getDefinedNameArray()) {
                createAndStoreName(ctName);
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

        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            workbook.save(out, xmlOptions);
        }
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
        if (stylesSource == null) {
            return null;
        }
        return stylesSource.getTheme();
    }

    /**
     * Returns an object that handles instantiating concrete
     *  classes of the various instances for XSSF.
     */
    @Override
    public XSSFCreationHelper getCreationHelper() {
        if(_creationHelper == null) {
            _creationHelper = new XSSFCreationHelper(this);
        }
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

            if (excludeSheetIdx != i && name.equalsIgnoreCase(ctName)) {
                return true;
            }
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
    @Internal
    @Override
    public boolean isDate1904() {
        CTWorkbookPr workbookPr = workbook.getWorkbookPr();
        return workbookPr != null && workbookPr.getDate1904();
    }

    /**
     * Get the document's embedded files.
     */
    @Override
    public List<PackagePart> getAllEmbeddedParts() throws OpenXML4JException {
        List<PackagePart> embedds = new LinkedList<>();

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
    @NotImplemented
    public boolean isHidden() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    @NotImplemented
    public void setHidden(boolean hiddenFlag) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean isSheetHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        CTSheet ctSheet = sheets.get(sheetIx).sheet;
        return ctSheet.getState() == STSheetState.HIDDEN;
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        CTSheet ctSheet = sheets.get(sheetIx).sheet;
        return ctSheet.getState() == STSheetState.VERY_HIDDEN;
    }

    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        validateSheetIndex(sheetIx);
        final CTSheet ctSheet = sheets.get(sheetIx).sheet;
        final STSheetState.Enum state = ctSheet.getState();
        if (state == STSheetState.VISIBLE) {
            return SheetVisibility.VISIBLE;
        }
        if (state == STSheetState.HIDDEN) {
            return SheetVisibility.HIDDEN;
        }
        if (state == STSheetState.VERY_HIDDEN) {
            return SheetVisibility.VERY_HIDDEN;
        }
        throw new IllegalArgumentException("This should never happen");
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden) {
        setSheetVisibility(sheetIx, hidden ? SheetVisibility.HIDDEN : SheetVisibility.VISIBLE);
    }

    @Override
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        validateSheetIndex(sheetIx);

        final CTSheet ctSheet = sheets.get(sheetIx).sheet;
        switch (visibility) {
            case VISIBLE:
                ctSheet.setState(STSheetState.VISIBLE);
                break;
            case HIDDEN:
                ctSheet.setState(STSheetState.HIDDEN);
                break;
            case VERY_HIDDEN:
                ctSheet.setState(STSheetState.VERY_HIDDEN);
                break;
            default:
                throw new IllegalArgumentException("This should never happen");
        }
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
        return mapInfo == null ? new ArrayList<>() : mapInfo.getAllXSSFMaps();
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
     *  Note: this is not implemented and thus currently throws an Exception stating this.
     *
     * @param name The name the workbook will be referenced as in formulas
     * @param workbook The open workbook to fetch the link required information from
     *
     * @throws RuntimeException stating that this method is not implemented yet.
     */
    @Override
    @NotImplemented
    public int linkExternalWorkbook(String name, Workbook workbook) {
        throw new RuntimeException("Not Implemented - see bug #57184");
    }

    /**
     * Specifies a boolean value that indicates whether structure of workbook is locked. <br>
     * A value true indicates the structure of the workbook is locked. Worksheets in the workbook can't be moved,
     * deleted, hidden, unhidden, or renamed, and new worksheets can't be inserted.<br>
     * A value of false indicates the structure of the workbook is not locked.<br>
     *
     * @return true if structure of workbook is locked
     */
    public boolean isStructureLocked() {
        return workbookProtectionPresent() && workbook.getWorkbookProtection().getLockStructure();
    }

    /**
     * Specifies a boolean value that indicates whether the windows that comprise the workbook are locked. <br>
     * A value of true indicates the workbook windows are locked. Windows are the same size and position each time the
     * workbook is opened.<br>
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
        if (password == null && !workbookProtectionPresent()) {
            return;
        }
        setPassword(safeGetWorkbookProtection(), password, hashAlgo, "workbook");
    }

    /**
     * Validate the password against the stored hash, the hashing method will be determined
     *  by the existing password attributes
     * @return true, if the hashes match (... though original password may differ ...)
     */
    public boolean validateWorkbookPassword(String password) {
        if (!workbookProtectionPresent()) {
            return (password == null);
        }
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
        if (password == null && !workbookProtectionPresent()) {
            return;
        }
        setPassword(safeGetWorkbookProtection(), password, hashAlgo, "revisions");
    }

    /**
     * Validate the password against the stored hash, the hashing method will be determined
     *  by the existing password attributes
     * @return true if the hashes match (... though original password may differ ...)
     */
    public boolean validateRevisionsPassword(String password) {
        if (!workbookProtectionPresent()) {
            return (password == null);
        }
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
        // when set to true, will tell Excel that it needs to recalculate all formulas
        // in the workbook the next time the file is opened.
        calcPr.setFullCalcOnLoad(value);

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
        return calcPr != null && calcPr.isSetFullCalcOnLoad() && calcPr.getFullCalcOnLoad();
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
            pivotCaches = new ArrayList<>();
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

    public XSSFWorkbookType getWorkbookType() {
        return isMacroEnabled() ? XSSFWorkbookType.XLSM : XSSFWorkbookType.XLSX;
    }

    /**
     * Sets whether the workbook will be an .xlsx or .xlsm (macro-enabled) file.
     */
    public void setWorkbookType(XSSFWorkbookType type) {
        try {
            getPackagePart().setContentType(type.getContentType());
        } catch (InvalidFormatException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * Adds a vbaProject.bin file to the workbook.  This will change the workbook
     * type if necessary.
     *
     * @throws IOException If copying data from the stream fails.
     */
    public void setVBAProject(InputStream vbaProjectStream) throws IOException {
        if (!isMacroEnabled()) {
            setWorkbookType(XSSFWorkbookType.XLSM);
        }

        PackagePartName ppName;
        try {
            ppName = PackagingURIHelper.createPartName(XSSFRelation.VBA_MACROS.getDefaultFileName());
        } catch (InvalidFormatException e) {
            throw new POIXMLException(e);
        }
        OPCPackage opc = getPackage();
        OutputStream outputStream;
        if (!opc.containPart(ppName)) {
            POIXMLDocumentPart relationship = createRelationship(XSSFRelation.VBA_MACROS, this.xssfFactory);
            outputStream = relationship.getPackagePart().getOutputStream();
        } else {
            PackagePart part = opc.getPart(ppName);
            outputStream = part.getOutputStream();
        }
        try {
            IOUtils.copy(vbaProjectStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Adds a vbaProject.bin file taken from another, given workbook to this one.
     * @throws IOException If copying the VBAProject stream fails.
     * @throws InvalidFormatException If an error occurs while handling parts of the XSSF format
     */
    public void setVBAProject(XSSFWorkbook macroWorkbook) throws IOException, InvalidFormatException {
        if (!macroWorkbook.isMacroEnabled()) {
            return;
        }
        InputStream vbaProjectStream = XSSFRelation.VBA_MACROS.getContents(macroWorkbook.getCorePart());
        if (vbaProjectStream != null) {
            setVBAProject(vbaProjectStream);
        }
    }

    /**
     * Returns the spreadsheet version (EXCLE2007) of this workbook
     *
     * @return EXCEL2007 SpreadsheetVersion enum
     * @since 3.14 beta 2
     */
    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    /**
     * Returns the data table with the given name (case insensitive).
     *
     * @param name the data table name (case-insensitive)
     * @return The Data table in the workbook named <tt>name</tt>, or <tt>null</tt> if no table is named <tt>name</tt>.
     * @since 3.15 beta 2
     */
    public XSSFTable getTable(String name) {
        if (name != null && sheets != null) {
            for (XSSFSheet sheet : sheets) {
                for (XSSFTable tbl : sheet.getTables()) {
                    if (name.equalsIgnoreCase(tbl.getName())) {
                        return tbl;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int addOlePackage(byte[] oleData, String label, String fileName, String command)
            throws IOException {
        final XSSFRelation rel = XSSFRelation.OLEEMBEDDINGS;

        // find an unused part name
        OPCPackage opc = getPackage();
        PackagePartName pnOLE;
        int oleId;
        try {
            oleId = opc.getUnusedPartIndex(rel.getDefaultFileName());
            pnOLE = PackagingURIHelper.createPartName(rel.getFileName(oleId));
        } catch (InvalidFormatException e) {
            throw new IOException("ole object name not recognized", e);
        }

        PackagePart pp = opc.createPart( pnOLE, rel.getContentType() );

        Ole10Native ole10 = new Ole10Native(label, fileName, command, oleData);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(oleData.length+500)) {
            ole10.writeOut(bos);

            try (POIFSFileSystem poifs = new POIFSFileSystem()) {
                DirectoryNode root = poifs.getRoot();
                root.createDocument(Ole10Native.OLE10_NATIVE, new ByteArrayInputStream(bos.toByteArray()));
                root.setStorageClsid(ClassIDPredefined.OLE_V1_PACKAGE.getClassID());

                // TODO: generate CombObj stream

                try (OutputStream os = pp.getOutputStream()) {
                    poifs.writeFilesystem(os);
                }
            }
        }

        return oleId;
    }

    /**
     * Whether a call to {@link XSSFCell#setCellFormula(String)} will validate the formula or not.
     *
     * @param value true if the application will validate the formula is correct
     * @since 3.17
     */
    public void setCellFormulaValidation(final boolean value) {
        this.cellFormulaValidation = value;
    }

    /**
     * Whether a call to {@link XSSFCell#setCellFormula(String)} will validate the formula or not.
     *
     * @since 3.17
     */
    public boolean getCellFormulaValidation() {
        return this.cellFormulaValidation;
    }

    @Override
    public XSSFEvaluationWorkbook createEvaluationWorkbook() {
        return XSSFEvaluationWorkbook.create(this);
    }
}
