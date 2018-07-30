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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.PartAlreadyExistsException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.ss.util.SSCellRange;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.usermodel.XSSFPivotTable.PivotTableReferenceConfigurator;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.apache.poi.xssf.usermodel.helpers.XSSFColumnShifter;
import org.apache.poi.xssf.usermodel.helpers.XSSFIgnoredErrorHelper;
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBreak;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidations;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIgnoredError;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIgnoredErrors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTLegacyDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOleObject;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOleObjects;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOutlinePr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageBreak;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageMargins;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageSetUpPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPane;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPrintOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSelection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetCalcPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetFormatPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetView;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetViews;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTablePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableParts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheetSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCalcMode;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPane;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPaneState;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorksheetDocument;

/**
 * High level representation of a SpreadsheetML worksheet.
 *
 * <p>
 * Sheets are the central structures within a workbook, and are where a user does most of his spreadsheet work.
 * The most common type of sheet is the worksheet, which is represented as a grid of cells. Worksheet cells can
 * contain text, numbers, dates, and formulas. Cells can also be formatted.
 * </p>
 */
public class XSSFSheet extends POIXMLDocumentPart implements Sheet  {
    private static final POILogger logger = POILogFactory.getLogger(XSSFSheet.class);

    private static final double DEFAULT_ROW_HEIGHT = 15.0;
    private static final double DEFAULT_MARGIN_HEADER = 0.3;
    private static final double DEFAULT_MARGIN_FOOTER = 0.3;
    private static final double DEFAULT_MARGIN_TOP = 0.75;
    private static final double DEFAULT_MARGIN_BOTTOM = 0.75;
    private static final double DEFAULT_MARGIN_LEFT = 0.7;
    private static final double DEFAULT_MARGIN_RIGHT = 0.7;
    public static final int TWIPS_PER_POINT = 20;

    //TODO make the two variable below private!
    protected CTSheet sheet;
    protected CTWorksheet worksheet;

    private final SortedMap<Integer, XSSFRow> _rows = new TreeMap<>();
    private List<XSSFHyperlink> hyperlinks;
    private ColumnHelper columnHelper;
    private CommentsTable sheetComments;
    /**
     * cache of master shared formulas in this sheet.
     * Master shared formula is the first formula in a group of shared formulas is saved in the f element.
     */
    private Map<Integer, CTCellFormula> sharedFormulas;
    private SortedMap<String,XSSFTable> tables;
    private List<CellRangeAddress> arrayFormulas;
    private XSSFDataValidationHelper dataValidationHelper;

    /**
     * Creates new XSSFSheet   - called by XSSFWorkbook to create a sheet from scratch.
     *
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createSheet()
     */
    protected XSSFSheet() {
        super();
        dataValidationHelper = new XSSFDataValidationHelper(this);
        onDocumentCreate();
    }

    /**
     * Creates an XSSFSheet representing the given package part and relationship.
     * Should only be called by XSSFWorkbook when reading in an existing file.
     *
     * @param part - The package part that holds xml data representing this sheet.
     *
     * @since POI 3.14-Beta1
     */
    protected XSSFSheet(PackagePart part) {
        super(part);
        dataValidationHelper = new XSSFDataValidationHelper(this);
    }

    /**
     * Returns the parent XSSFWorkbook
     *
     * @return the parent XSSFWorkbook
     */
    @Override
    public XSSFWorkbook getWorkbook() {
        return (XSSFWorkbook)getParent();
    }

    /**
     * Initialize worksheet data when reading in an exisiting file.
     */
    @Override
    protected void onDocumentRead() {
        try {
            read(getPackagePart().getInputStream());
        } catch (IOException e){
            throw new POIXMLException(e);
        }
    }

    protected void read(InputStream is) throws IOException {
        try {
            worksheet = WorksheetDocument.Factory.parse(is, DEFAULT_XML_OPTIONS).getWorksheet();
        } catch (XmlException e){
            throw new POIXMLException(e);
        }

        initRows(worksheet);
        columnHelper = new ColumnHelper(worksheet);
        // Look for bits we're interested in
        for(RelationPart rp : getRelationParts()){
            POIXMLDocumentPart p = rp.getDocumentPart();
            if(p instanceof CommentsTable) {
                sheetComments = (CommentsTable)p;
            }
            if(p instanceof XSSFTable) {
                tables.put( rp.getRelationship().getId(), (XSSFTable)p );
            }
            if(p instanceof XSSFPivotTable) {
                getWorkbook().getPivotTables().add((XSSFPivotTable) p);
            }
        }

        // Process external hyperlinks for the sheet, if there are any
        initHyperlinks();
    }

    /**
     * Initialize worksheet data when creating a new sheet.
     */
    @Override
    protected void onDocumentCreate(){
        worksheet = newSheet();
        initRows(worksheet);
        columnHelper = new ColumnHelper(worksheet);
        hyperlinks = new ArrayList<>();
    }

    private void initRows(CTWorksheet worksheetParam) {
        _rows.clear();
        tables = new TreeMap<>();
        sharedFormulas = new HashMap<>();
        arrayFormulas = new ArrayList<>();
        for (CTRow row : worksheetParam.getSheetData().getRowArray()) {
            XSSFRow r = new XSSFRow(row, this);
            // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
            final Integer rownumI = Integer.valueOf(r.getRowNum()); // NOSONAR
            _rows.put(rownumI, r);
        }
    }

    /**
     * Read hyperlink relations, link them with CTHyperlink beans in this worksheet
     * and initialize the internal array of XSSFHyperlink objects
     */
    private void initHyperlinks() {
        hyperlinks = new ArrayList<>();

        if(!worksheet.isSetHyperlinks()) {
            return;
        }

        try {
            PackageRelationshipCollection hyperRels =
                    getPackagePart().getRelationshipsByType(XSSFRelation.SHEET_HYPERLINKS.getRelation());

            // Turn each one into a XSSFHyperlink
            for(CTHyperlink hyperlink : worksheet.getHyperlinks().getHyperlinkArray()) {
                PackageRelationship hyperRel = null;
                if(hyperlink.getId() != null) {
                    hyperRel = hyperRels.getRelationshipByID(hyperlink.getId());
                }

                hyperlinks.add( new XSSFHyperlink(hyperlink, hyperRel) );
            }
        } catch (InvalidFormatException e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Create a new CTWorksheet instance with all values set to defaults
     *
     * @return a new instance
     */
    private static CTWorksheet newSheet(){
        CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
        CTSheetFormatPr ctFormat = worksheet.addNewSheetFormatPr();
        ctFormat.setDefaultRowHeight(DEFAULT_ROW_HEIGHT);

        CTSheetView ctView = worksheet.addNewSheetViews().addNewSheetView();
        ctView.setWorkbookViewId(0);

        worksheet.addNewDimension().setRef("A1");

        worksheet.addNewSheetData();

        CTPageMargins ctMargins = worksheet.addNewPageMargins();
        ctMargins.setBottom(DEFAULT_MARGIN_BOTTOM);
        ctMargins.setFooter(DEFAULT_MARGIN_FOOTER);
        ctMargins.setHeader(DEFAULT_MARGIN_HEADER);
        ctMargins.setLeft(DEFAULT_MARGIN_LEFT);
        ctMargins.setRight(DEFAULT_MARGIN_RIGHT);
        ctMargins.setTop(DEFAULT_MARGIN_TOP);

        return worksheet;
    }

    /**
     * Provide access to the CTWorksheet bean holding this sheet's data
     *
     * @return the CTWorksheet bean holding this sheet's data
     */
    @Internal
    public CTWorksheet getCTWorksheet() {
        return this.worksheet;
    }

    public ColumnHelper getColumnHelper() {
        return columnHelper;
    }

    /**
     * Returns the name of this sheet
     *
     * @return the name of this sheet
     */
    @Override
    public String getSheetName() {
        return sheet.getName();
    }

    /**
     * Adds a merged region of cells on a sheet.
     *
     * @param region to merge
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells
     * @throws IllegalStateException if region intersects with a multi-cell array formula
     * @throws IllegalStateException if region intersects with an existing region on this sheet
     */
    @Override
    public int addMergedRegion(CellRangeAddress region) {
        return addMergedRegion(region, true);
    }

    /**
     * Adds a merged region of cells (hence those cells form one).
     * Skips validation. It is possible to create overlapping merged regions
     * or create a merged region that intersects a multi-cell array formula
     * with this formula, which may result in a corrupt workbook.
     *
     * To check for merged regions overlapping array formulas or other merged regions
     * after addMergedRegionUnsafe has been called, call {@link #validateMergedRegions()}, which runs in O(n^2) time.
     *
     * @param region to merge
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells
     */
    @Override
    public int addMergedRegionUnsafe(CellRangeAddress region) {
        return addMergedRegion(region, false);
    }

    /**
     * Adds a merged region of cells (hence those cells form one).
     * If validate is true, check to make sure adding the merged region to the sheet doesn't create a corrupt workbook
     * If validate is false, skips the expensive merged region checks, but may produce a corrupt workbook.
     *
     * @param region to merge
     * @param validate whether to validate merged region
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells (this check is inexpensive and is performed regardless of <tt>validate</tt>)
     * @throws IllegalStateException if region intersects with a multi-cell array formula
     * @throws IllegalStateException if region intersects with an existing region on this sheet
     */
    private int addMergedRegion(CellRangeAddress region, boolean validate) {
        if (region.getNumberOfCells() < 2) {
            throw new IllegalArgumentException("Merged region " + region.formatAsString() + " must contain 2 or more cells");
        }
        region.validate(SpreadsheetVersion.EXCEL2007);

        if (validate) {
            // throw IllegalStateException if the argument CellRangeAddress intersects with
            // a multi-cell array formula defined in this sheet
            validateArrayFormulas(region);

            // Throw IllegalStateException if the argument CellRangeAddress intersects with
            // a merged region already in this sheet 
            validateMergedRegions(region);
        }

        CTMergeCells ctMergeCells = worksheet.isSetMergeCells() ? worksheet.getMergeCells() : worksheet.addNewMergeCells();
        CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();
        ctMergeCell.setRef(region.formatAsString());
        return ctMergeCells.sizeOfMergeCellArray();
    }

    /**
     * Verify that the candidate region does not intersect with an existing multi-cell array formula in this sheet
     *
     * @param region a region that is validated.
     * @throws IllegalStateException if candidate region intersects an existing array formula in this sheet
     */
    private void validateArrayFormulas(CellRangeAddress region) {
        // FIXME: this may be faster if it looped over array formulas directly rather than looping over each cell in
        // the region and searching if that cell belongs to an array formula
        int firstRow = region.getFirstRow();
        int firstColumn = region.getFirstColumn();
        int lastRow = region.getLastRow();
        int lastColumn = region.getLastColumn();
        // for each cell in sheet, if cell belongs to an array formula, check if merged region intersects array formula cells
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            XSSFRow row = getRow(rowIn);
            if (row == null) {
                continue;
            }

            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                XSSFCell cell = row.getCell(colIn);
                if (cell == null) {
                    continue;
                }

                if (cell.isPartOfArrayFormulaGroup()) {
                    CellRangeAddress arrayRange = cell.getArrayFormulaRange();
                    if (arrayRange.getNumberOfCells() > 1 && region.intersects(arrayRange)) {
                        String msg = "The range " + region.formatAsString() + " intersects with a multi-cell array formula. " +
                                "You cannot merge cells of an array.";
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
    }

    /**
     * Verify that none of the merged regions intersect a multi-cell array formula in this sheet
     *
     * @throws IllegalStateException if candidate region intersects an existing array formula in this sheet
     */
    private void checkForMergedRegionsIntersectingArrayFormulas() {
        for (CellRangeAddress region : getMergedRegions()) {
            validateArrayFormulas(region);
        }
    }

    /**
     * Verify that candidate region does not intersect with an existing merged region in this sheet
     *
     * @param candidateRegion
     * @throws IllegalStateException if candidate region intersects an existing merged region in this sheet (or candidateRegion is already merged in this sheet)
     */
    private void validateMergedRegions(CellRangeAddress candidateRegion) {
        for (final CellRangeAddress existingRegion : getMergedRegions()) {
            if (existingRegion.intersects(candidateRegion)) {
                throw new IllegalStateException("Cannot add merged region " + candidateRegion.formatAsString() +
                        " to sheet because it overlaps with an existing merged region (" + existingRegion.formatAsString() + ").");
            }
        }
    }

    /**
     * Verify that no merged regions intersect another merged region in this sheet.
     *
     * @throws IllegalStateException if at least one region intersects with another merged region in this sheet
     */
    private void checkForIntersectingMergedRegions() {
        final List<CellRangeAddress> regions = getMergedRegions();
        final int size = regions.size();
        for (int i=0; i < size; i++) {
            final CellRangeAddress region = regions.get(i);
            for (final CellRangeAddress other : regions.subList(i+1, regions.size())) {
                if (region.intersects(other)) {
                    String msg = "The range " + region.formatAsString() +
                            " intersects with another merged region " +
                            other.formatAsString() + " in this sheet";
                    throw new IllegalStateException(msg);
                }
            }
        }
    }

    /**
     * Verify that merged regions do not intersect multi-cell array formulas and
     * no merged regions intersect another merged region in this sheet.
     *
     * @throws IllegalStateException if region intersects with a multi-cell array formula
     * @throws IllegalStateException if at least one region intersects with another merged region in this sheet
     */
    @Override
    public void validateMergedRegions() {
        checkForMergedRegionsIntersectingArrayFormulas();
        checkForIntersectingMergedRegions();
    }

    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * @param column the column index
     */
    @Override
    public void autoSizeColumn(int column) {
        autoSizeColumn(column, false);
    }

    /**
     * Adjusts the column width to fit the contents.
     * <p>
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     * </p>
     * You can specify whether the content of merged cells should be considered or ignored.
     *  Default is to ignore merged cells.
     *
     * @param column the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    @Override
    public void autoSizeColumn(int column, boolean useMergedCells) {
        double width = SheetUtil.getColumnWidth(this, column, useMergedCells);

        if (width != -1) {
            width *= 256;
            int maxColumnWidth = 255*256; // The maximum column width for an individual cell is 255 characters
            if (width > maxColumnWidth) {
                width = maxColumnWidth;
            }
            setColumnWidth(column, (int)(width));
            columnHelper.setColBestFit(column, true);
        }
    }

    /**
     * Return the sheet's existing drawing, or null if there isn't yet one.
     *
     * Use {@link #createDrawingPatriarch()} to get or create
     *
     * @return a SpreadsheetML drawing
     */
    @Override
    public XSSFDrawing getDrawingPatriarch() {
        CTDrawing ctDrawing = getCTDrawing();
        if (ctDrawing != null) {
            // Search the referenced drawing in the list of the sheet's relations
            for (RelationPart rp : getRelationParts()){
                POIXMLDocumentPart p = rp.getDocumentPart();
                if (p instanceof XSSFDrawing) {
                    XSSFDrawing dr = (XSSFDrawing)p;
                    String drId = rp.getRelationship().getId();
                    if (drId.equals(ctDrawing.getId())){
                        return dr;
                    }
                    break;
                }
            }
            logger.log(POILogger.ERROR, "Can't find drawing with id=" + ctDrawing.getId() + " in the list of the sheet's relationships");
        }
        return null;
    }

    /**
     * Create a new SpreadsheetML drawing. If this sheet already contains a drawing - return that.
     *
     * @return a SpreadsheetML drawing
     */
    @Override
    public XSSFDrawing createDrawingPatriarch() {
        CTDrawing ctDrawing = getCTDrawing();
        if (ctDrawing != null) {
            return getDrawingPatriarch();
        }

        // Default drawingNumber = #drawings.size() + 1
        int drawingNumber = getPackagePart().getPackage().getPartsByContentType(XSSFRelation.DRAWINGS.getContentType()).size() + 1;
        drawingNumber = getNextPartNumber(XSSFRelation.DRAWINGS, drawingNumber);
        RelationPart rp = createRelationship(XSSFRelation.DRAWINGS, XSSFFactory.getInstance(), drawingNumber, false);
        XSSFDrawing drawing = rp.getDocumentPart();
        String relId = rp.getRelationship().getId();

        //add CT_Drawing element which indicates that this sheet contains drawing components built on the drawingML platform.
        //The relationship Id references the part containing the drawingML definitions.
        ctDrawing = worksheet.addNewDrawing();
        ctDrawing.setId(relId);

        // Return the newly created drawing
        return drawing;
    }

    /**
     * Get VML drawing for this sheet (aka 'legacy' drawig)
     *
     * @param autoCreate if true, then a new VML drawing part is created
     *
     * @return the VML drawing of <code>null</code> if the drawing was not found and autoCreate=false
     */
    protected XSSFVMLDrawing getVMLDrawing(boolean autoCreate) {
        XSSFVMLDrawing drawing = null;
        CTLegacyDrawing ctDrawing = getCTLegacyDrawing();
        if(ctDrawing == null) {
            if(autoCreate) {
                //drawingNumber = #drawings.size() + 1
                int drawingNumber = getPackagePart().getPackage().getPartsByContentType(XSSFRelation.VML_DRAWINGS.getContentType()).size() + 1;
                RelationPart rp = createRelationship(XSSFRelation.VML_DRAWINGS, XSSFFactory.getInstance(), drawingNumber, false);
                drawing = rp.getDocumentPart();
                String relId = rp.getRelationship().getId();

                //add CTLegacyDrawing element which indicates that this sheet contains drawing components built on the drawingML platform.
                //The relationship Id references the part containing the drawing definitions.
                ctDrawing = worksheet.addNewLegacyDrawing();
                ctDrawing.setId(relId);
            }
        } else {
            //search the referenced drawing in the list of the sheet's relations
            final String id = ctDrawing.getId();
            for (RelationPart rp : getRelationParts()){
                POIXMLDocumentPart p = rp.getDocumentPart();
                if(p instanceof XSSFVMLDrawing) {
                    XSSFVMLDrawing dr = (XSSFVMLDrawing)p;
                    String drId = rp.getRelationship().getId();
                    if (drId.equals(id)) {
                        drawing = dr;
                        break;
                    }
                    // do not break here since drawing has not been found yet (see bug 52425)
                }
            }
            if(drawing == null){
                logger.log(POILogger.ERROR, "Can't find VML drawing with id=" + id + " in the list of the sheet's relationships");
            }
        }
        return drawing;
    }

    protected CTDrawing getCTDrawing() {
        return worksheet.getDrawing();
    }
    protected CTLegacyDrawing getCTLegacyDrawing() {
        return worksheet.getLegacyDrawing();
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizontal position of split.
     * @param rowSplit      Vertical position of split.
     */
    @Override
    public void createFreezePane(int colSplit, int rowSplit) {
        createFreezePane( colSplit, rowSplit, colSplit, rowSplit );
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     *
     * <p>
     *     If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * </p>
     *
     * @param colSplit      Horizontal position of split.
     * @param rowSplit      Vertical position of split.
     * @param leftmostColumn   Left column visible in right pane.
     * @param topRow        Top row visible in bottom pane
     */
    @Override
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        final boolean removeSplit = colSplit == 0 && rowSplit == 0;
        final CTSheetView ctView = getDefaultSheetView(!removeSplit);

        if (ctView != null) {
            ctView.setSelectionArray(null);
        }

        // If both colSplit and rowSplit are zero then the existing freeze pane is removed
        if (removeSplit) {
            if (ctView != null && ctView.isSetPane()) {
                ctView.unsetPane();
            }
            return;
        }

        assert(ctView != null);
        final CTPane pane = (ctView.isSetPane()) ? ctView.getPane() : ctView.addNewPane();
        assert(pane != null);

        if (colSplit > 0) {
            pane.setXSplit(colSplit);
        } else if (pane.isSetXSplit()) {
            pane.unsetXSplit();
        }
        if (rowSplit > 0) {
            pane.setYSplit(rowSplit);
        } else if(pane.isSetYSplit()) {
            pane.unsetYSplit();
        }

        STPane.Enum activePane = STPane.BOTTOM_RIGHT;
        int pRow = topRow, pCol = leftmostColumn;
        if (rowSplit == 0) {
            pRow = 0;
            activePane = STPane.TOP_RIGHT;
        } else if (colSplit == 0) {
            pCol = 0;
            activePane = STPane.BOTTOM_LEFT;
        }

        pane.setState(STPaneState.FROZEN);
        pane.setTopLeftCell(new CellReference(pRow, pCol).formatAsString());
        pane.setActivePane(activePane);

        ctView.addNewSelection().setPane(activePane);
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * Note: If a row already exists at this position, it is removed/overwritten and
     *      any existing cell is removed!
     *
     * @param rownum  row number
     * @return High level {@link XSSFRow} object representing a row in the sheet
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    @Override
    public XSSFRow createRow(int rownum) {
        // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer rownumI = Integer.valueOf(rownum); // NOSONAR
        CTRow ctRow;
        XSSFRow prev = _rows.get(rownumI);
        if(prev != null){
            // the Cells in an existing row are invalidated on-purpose, in order to clean up correctly, we
            // need to call the remove, so things like ArrayFormulas and CalculationChain updates are done 
            // correctly. 
            // We remove the cell this way as the internal cell-list is changed by the remove call and 
            // thus would cause ConcurrentModificationException otherwise
            while(prev.getFirstCellNum() != -1) {
                prev.removeCell(prev.getCell(prev.getFirstCellNum()));
            }

            ctRow = prev.getCTRow();
            ctRow.set(CTRow.Factory.newInstance());
        } else {
            if(_rows.isEmpty() || rownum > _rows.lastKey()) {
                // we can append the new row at the end
                ctRow = worksheet.getSheetData().addNewRow();
            } else {
                // get number of rows where row index < rownum
                // --> this tells us where our row should go
                int idx = _rows.headMap(rownumI).size();
                ctRow = worksheet.getSheetData().insertNewRow(idx);
            }
        }
        XSSFRow r = new XSSFRow(ctRow, this);
        r.setRowNum(rownum);
        _rows.put(rownumI, r);
        return r;
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizontal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     *                      PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see org.apache.poi.ss.usermodel.Sheet#PANE_LOWER_LEFT
     * @see org.apache.poi.ss.usermodel.Sheet#PANE_LOWER_RIGHT
     * @see org.apache.poi.ss.usermodel.Sheet#PANE_UPPER_LEFT
     * @see org.apache.poi.ss.usermodel.Sheet#PANE_UPPER_RIGHT
     */
    @Override
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
        createFreezePane(xSplitPos, ySplitPos, leftmostColumn, topRow);
        if (xSplitPos > 0 || ySplitPos > 0) {
            final CTPane pane = getPane(true);
            pane.setState(STPaneState.SPLIT);
            pane.setActivePane(STPane.Enum.forInt(activePane));
        }
    }

    /**
     * Return cell comment at row, column, if one exists. Otherwise returns null.
     *
     * @param address the location of the cell comment
     * @return the cell comment, if one exists. Otherwise return null.
     */
    @Override
    public XSSFComment getCellComment(CellAddress address) {
        if (sheetComments == null) {
            return null;
        }

        final int row = address.getRow();
        final int column = address.getColumn();

        CellAddress ref = new CellAddress(row, column);
        CTComment ctComment = sheetComments.getCTComment(ref);
        if(ctComment == null) {
            return null;
        }

        XSSFVMLDrawing vml = getVMLDrawing(false);
        return new XSSFComment(sheetComments, ctComment,
                vml == null ? null : vml.findCommentShape(row, column));
    }

    /**
     * Returns all cell comments on this sheet.
     * @return A map of each Comment in the sheet, keyed on the cell address where
     * the comment is located.
     */
    @Override
    public Map<CellAddress, XSSFComment> getCellComments() {
        if (sheetComments == null) {
            return Collections.emptyMap();
        }
        // the cell comments in sheetComments.getCellComments() do not have the client anchors set
        Map<CellAddress, XSSFComment> map = new HashMap<>();
        for(Iterator<CellAddress> iter = sheetComments.getCellAddresses(); iter.hasNext(); ) {
            CellAddress address = iter.next();
            map.put(address, getCellComment(address));
        }
        return map;
    }

    /**
     * Get a Hyperlink in this sheet anchored at row, column
     *
     * @param row
     * @param column
     * @return hyperlink if there is a hyperlink anchored at row, column; otherwise returns null
     */
    @Override
    public XSSFHyperlink getHyperlink(int row, int column) {
        return getHyperlink(new CellAddress(row, column));
    }

    /**
     * Get a Hyperlink in this sheet located in a cell specified by {code addr}
     *
     * @param addr The address of the cell containing the hyperlink
     * @return hyperlink if there is a hyperlink anchored at {@code addr}; otherwise returns {@code null}
     * @since POI 3.15 beta 3
     */
    @Override
    public XSSFHyperlink getHyperlink(CellAddress addr) {
        String ref = addr.formatAsString();
        for(XSSFHyperlink hyperlink : hyperlinks) {
            if(hyperlink.getCellRef().equals(ref)) {
                return hyperlink;
            }
        }
        return null;
    }

    /**
     * Get a list of Hyperlinks in this sheet
     *
     * @return Hyperlinks for the sheet
     */
    @Override
    public List<XSSFHyperlink> getHyperlinkList() {
        return Collections.unmodifiableList(hyperlinks);
    }

    private int[] getBreaks(CTPageBreak ctPageBreak) {
        CTBreak[] brkArray = ctPageBreak.getBrkArray();
        int[] breaks = new int[brkArray.length];
        for (int i = 0 ; i < brkArray.length ; i++) {
            breaks[i] = (int) brkArray[i].getId() - 1;
        }
        return breaks;
    }

    private void removeBreak(int index, CTPageBreak ctPageBreak) {
        int index1 = index + 1;
        CTBreak[] brkArray = ctPageBreak.getBrkArray();
        for (int i = 0 ; i < brkArray.length ; i++) {
            if (brkArray[i].getId() == index1) {
                ctPageBreak.removeBrk(i);
                // TODO: check if we can break here, i.e. if a page can have more than 1 break on the same id
            }
        }
    }

    /**
     * Vertical page break information used for print layout view, page layout view, drawing print breaks
     * in normal view, and for printing the worksheet.
     *
     * @return column indexes of all the vertical page breaks, never <code>null</code>
     */
    @Override
    public int[] getColumnBreaks() {
        return worksheet.isSetColBreaks() ? getBreaks(worksheet.getColBreaks()) : new int[0];
    }

    /**
     * Get the actual column width (in units of 1/256th of a character width )
     *
     * <p>
     * Note, the returned  value is always gerater that {@link #getDefaultColumnWidth()} because the latter does not include margins.
     * Actual column width measured as the number of characters of the maximum digit width of the
     * numbers 0, 1, 2, ..., 9 as rendered in the normal style's font. There are 4 pixels of margin
     * padding (two on each side), plus 1 pixel padding for the gridlines.
     * </p>
     *
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    @Override
    public int getColumnWidth(int columnIndex) {
        CTCol col = columnHelper.getColumn(columnIndex, false);
        double width = col == null || !col.isSetWidth() ? getDefaultColumnWidth() : col.getWidth();
        return (int)(width*256);
    }

    /**
     * Get the actual column width in pixels
     *
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with the default font size (Calibri 11pt for .xlsx).
     * </p>
     */
    @Override
    public float getColumnWidthInPixels(int columnIndex) {
        float widthIn256 = getColumnWidth(columnIndex);
        return (float)(widthIn256/256.0*Units.DEFAULT_CHARACTER_WIDTH);
    }

    /**
     * Get the default column width for the sheet (if the columns do not define their own width) in
     * characters.
     * <p>
     * Note, this value is different from {@link #getColumnWidth(int)}. The latter is always greater and includes
     * 4 pixels of margin padding (two on each side), plus 1 pixel padding for the gridlines.
     * </p>
     * @return column width, default value is 8
     */
    @Override
    public int getDefaultColumnWidth() {
        CTSheetFormatPr pr = worksheet.getSheetFormatPr();
        return pr == null ? 8 : (int)pr.getBaseColWidth();
    }

    /**
     * Get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of a point)
     *
     * @return  default row height
     */
    @Override
    public short getDefaultRowHeight() {
        return (short)(getDefaultRowHeightInPoints() * TWIPS_PER_POINT);
    }


    /**
     * Get the default row height for the sheet measued in point size (if the rows do not define their own height).
     *
     * @return  default row height in points
     */
    @Override
    public float getDefaultRowHeightInPoints() {
        CTSheetFormatPr pr = worksheet.getSheetFormatPr();
        return (float)(pr == null ? 0 : pr.getDefaultRowHeight());
    }

    private CTSheetFormatPr getSheetTypeSheetFormatPr() {
        return worksheet.isSetSheetFormatPr() ?
                worksheet.getSheetFormatPr() :
                worksheet.addNewSheetFormatPr();
    }

    /**
     * Returns the CellStyle that applies to the given
     *  (0 based) column, or null if no style has been
     *  set for that column
     */
    @Override
    public CellStyle getColumnStyle(int column) {
        int idx = columnHelper.getColDefaultStyle(column);
        return getWorkbook().getCellStyleAt((short)(idx == -1 ? 0 : idx));
    }

    /**
     * Sets whether the worksheet is displayed from right to left instead of from left to right.
     *
     * @param value true for right to left, false otherwise.
     */
    @Override
    public void setRightToLeft(boolean value) {
        final CTSheetView dsv = getDefaultSheetView(true);
        assert(dsv != null);
        dsv.setRightToLeft(value);
    }

    /**
     * Whether the text is displayed in right-to-left mode in the window
     *
     * @return whether the text is displayed in right-to-left mode in the window
     */
    @Override
    public boolean isRightToLeft() {
        final CTSheetView dsv = getDefaultSheetView(false);
        return (dsv != null && dsv.getRightToLeft());
    }

    /**
     * Get whether to display the guts or not,
     * default value is true
     *
     * @return boolean - guts or no guts
     */
    @Override
    public boolean getDisplayGuts() {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTOutlinePr outlinePr = sheetPr.getOutlinePr() == null ? CTOutlinePr.Factory.newInstance() : sheetPr.getOutlinePr();
        return outlinePr.getShowOutlineSymbols();
    }

    /**
     * Set whether to display the guts or not
     *
     * @param value - guts or no guts
     */
    @Override
    public void setDisplayGuts(boolean value) {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTOutlinePr outlinePr = sheetPr.getOutlinePr() == null ? sheetPr.addNewOutlinePr() : sheetPr.getOutlinePr();
        outlinePr.setShowOutlineSymbols(value);
    }

    /**
     * Gets the flag indicating whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     *
     * @return whether all zero values on the worksheet are displayed (defaults to true)
     */
    @Override
    public boolean isDisplayZeros(){
        final CTSheetView dsv = getDefaultSheetView(false);
        return (dsv != null) ? dsv.getShowZeros() : true;
    }

    /**
     * Set whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     *
     * @param value whether to display or hide all zero values on the worksheet
     */
    @Override
    public void setDisplayZeros(boolean value){
        final CTSheetView view = getDefaultSheetView(true);
        assert(view != null);
        view.setShowZeros(value);
    }

    /**
     * Gets the first row on the sheet
     *
     * @return the number of the first logical row on the sheet, zero based
     */
    @Override
    public int getFirstRowNum() {
        return _rows.isEmpty() ? 0 : _rows.firstKey();
    }

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @return <code>true</code>
     */
    @Override
    public boolean getFitToPage() {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTPageSetUpPr psSetup = (sheetPr == null || !sheetPr.isSetPageSetUpPr()) ?
                CTPageSetUpPr.Factory.newInstance() : sheetPr.getPageSetUpPr();
        return psSetup.getFitToPage();
    }

    private CTSheetPr getSheetTypeSheetPr() {
        if (worksheet.getSheetPr() == null) {
            worksheet.setSheetPr(CTSheetPr.Factory.newInstance());
        }
        return worksheet.getSheetPr();
    }

    private CTHeaderFooter getSheetTypeHeaderFooter() {
        if (worksheet.getHeaderFooter() == null) {
            worksheet.setHeaderFooter(CTHeaderFooter.Factory.newInstance());
        }
        return worksheet.getHeaderFooter();
    }



    /**
     * Returns the default footer for the sheet,
     *  creating one as needed.
     * You may also want to look at
     *  {@link #getFirstFooter()},
     *  {@link #getOddFooter()} and
     *  {@link #getEvenFooter()}
     */
    @Override
    public Footer getFooter() {
        // The default footer is an odd footer
        return getOddFooter();
    }

    /**
     * Returns the default header for the sheet,
     *  creating one as needed.
     * You may also want to look at
     *  {@link #getFirstHeader()},
     *  {@link #getOddHeader()} and
     *  {@link #getEvenHeader()}
     */
    @Override
    public Header getHeader() {
        // The default header is an odd header
        return getOddHeader();
    }

    /**
     * Returns the odd footer. Used on all pages unless
     *  other footers also present, when used on only
     *  odd pages.
     */
    public Footer getOddFooter() {
        return new XSSFOddFooter(getSheetTypeHeaderFooter());
    }
    /**
     * Returns the even footer. Not there by default, but
     *  when set, used on even pages.
     */
    public Footer getEvenFooter() {
        return new XSSFEvenFooter(getSheetTypeHeaderFooter());
    }
    /**
     * Returns the first page footer. Not there by
     *  default, but when set, used on the first page.
     */
    public Footer getFirstFooter() {
        return new XSSFFirstFooter(getSheetTypeHeaderFooter());
    }

    /**
     * Returns the odd header. Used on all pages unless
     *  other headers also present, when used on only
     *  odd pages.
     */
    public Header getOddHeader() {
        return new XSSFOddHeader(getSheetTypeHeaderFooter());
    }
    /**
     * Returns the even header. Not there by default, but
     *  when set, used on even pages.
     */
    public Header getEvenHeader() {
        return new XSSFEvenHeader(getSheetTypeHeaderFooter());
    }
    /**
     * Returns the first page header. Not there by
     *  default, but when set, used on the first page.
     */
    public Header getFirstHeader() {
        return new XSSFFirstHeader(getSheetTypeHeaderFooter());
    }


    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */
    @Override
    public boolean getHorizontallyCenter() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getHorizontalCentered();
    }

    @Override
    public int getLastRowNum() {
        return _rows.isEmpty() ? 0 : _rows.lastKey();
    }

    @Override
    public short getLeftCol() {
        String cellRef = worksheet.getSheetViews().getSheetViewArray(0).getTopLeftCell();
        if(cellRef == null) {
            return 0;
        }
        CellReference cellReference = new CellReference(cellRef);
        return cellReference.getCol();
    }

    /**
     * Gets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @return the size of the margin
     * @see Sheet#LeftMargin
     * @see Sheet#RightMargin
     * @see Sheet#TopMargin
     * @see Sheet#BottomMargin
     * @see Sheet#HeaderMargin
     * @see Sheet#FooterMargin
     */
    @Override
    public double getMargin(short margin) {
        if (!worksheet.isSetPageMargins()) {
            return 0;
        }

        CTPageMargins pageMargins = worksheet.getPageMargins();
        switch (margin) {
            case LeftMargin:
                return pageMargins.getLeft();
            case RightMargin:
                return pageMargins.getRight();
            case TopMargin:
                return pageMargins.getTop();
            case BottomMargin:
                return pageMargins.getBottom();
            case HeaderMargin:
                return pageMargins.getHeader();
            case FooterMargin:
                return pageMargins.getFooter();
            default :
                throw new IllegalArgumentException("Unknown margin constant:  " + margin);
        }
    }

    /**
     * Sets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @param size the size of the margin
     * @see Sheet#LeftMargin
     * @see Sheet#RightMargin
     * @see Sheet#TopMargin
     * @see Sheet#BottomMargin
     * @see Sheet#HeaderMargin
     * @see Sheet#FooterMargin
     */
    @Override
    public void setMargin(short margin, double size) {
        CTPageMargins pageMargins = worksheet.isSetPageMargins() ?
                worksheet.getPageMargins() : worksheet.addNewPageMargins();
        switch (margin) {
            case LeftMargin:
                pageMargins.setLeft(size);
                break;
            case RightMargin:
                pageMargins.setRight(size);
                break;
            case TopMargin:
                pageMargins.setTop(size);
                break;
            case BottomMargin:
                pageMargins.setBottom(size);
                break;
            case HeaderMargin:
                pageMargins.setHeader(size);
                break;
            case FooterMargin:
                pageMargins.setFooter(size);
                break;
            default :
                throw new IllegalArgumentException( "Unknown margin constant:  " + margin );
        }
    }

    /**
     * Returns the merged region at the specified index. If you want multiple
     * regions, it is faster to call {@link #getMergedRegions()} than to call
     * this each time.
     *
     * @return the merged region at the specified index
     */
    @Override
    public CellRangeAddress getMergedRegion(int index) {
        CTMergeCells ctMergeCells = worksheet.getMergeCells();
        if(ctMergeCells == null) {
            throw new IllegalStateException("This worksheet does not contain merged regions");
        }

        CTMergeCell ctMergeCell = ctMergeCells.getMergeCellArray(index);
        String ref = ctMergeCell.getRef();
        return CellRangeAddress.valueOf(ref);
    }

    /**
     * Returns the list of merged regions. If you want multiple regions, this is
     * faster than calling {@link #getMergedRegion(int)} each time.
     *
     * @return the list of merged regions
     */
    @Override
    public List<CellRangeAddress> getMergedRegions() {
        List<CellRangeAddress> addresses = new ArrayList<>();
        CTMergeCells ctMergeCells = worksheet.getMergeCells();
        if(ctMergeCells == null) {
            return addresses;
        }

        for(CTMergeCell ctMergeCell : ctMergeCells.getMergeCellArray()) {
            String ref = ctMergeCell.getRef();
            addresses.add(CellRangeAddress.valueOf(ref));
        }
        return addresses;
    }

    /**
     * Returns the number of merged regions defined in this worksheet
     *
     * @return number of merged regions in this worksheet
     */
    @Override
    public int getNumMergedRegions() {
        CTMergeCells ctMergeCells = worksheet.getMergeCells();
        return ctMergeCells == null ? 0 : ctMergeCells.sizeOfMergeCellArray();
    }

    public int getNumHyperlinks() {
        return hyperlinks.size();
    }

    /**
     * Returns the information regarding the currently configured pane (split or freeze).
     *
     * @return null if no pane configured, or the pane information.
     */
    @Override
    public PaneInformation getPaneInformation() {
        final CTPane pane = getPane(false);
        // no pane configured
        if(pane == null) {
            return null;
        }

        short row = 0, col = 0;
        if (pane.isSetTopLeftCell()) {
            final CellReference cellRef = new CellReference(pane.getTopLeftCell());
            row = (short)cellRef.getRow();
            col = (short)cellRef.getCol();
        }

        final short x = (short)pane.getXSplit();
        final short y = (short)pane.getYSplit();
        final byte active = (byte)(pane.getActivePane().intValue() - 1);
        final boolean frozen = pane.getState() == STPaneState.FROZEN;

        return new PaneInformation(x, y, row, col, active, frozen);
    }

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     *
     * @return the number of physically defined rows
     */
    @Override
    public int getPhysicalNumberOfRows() {
        return _rows.size();
    }

    /**
     * Gets the print setup object.
     *
     * @return The user model for the print setup object.
     */
    @Override
    public XSSFPrintSetup getPrintSetup() {
        return new XSSFPrintSetup(worksheet);
    }

    /**
     * Answer whether protection is enabled or disabled
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    @Override
    public boolean getProtect() {
        return isSheetLocked();
    }

    /**
     * Enables sheet protection and sets the password for the sheet.
     * Also sets some attributes on the {@link CTSheetProtection} that correspond to
     * the default values used by Excel
     *
     * @param password to set for protection. Pass <code>null</code> to remove protection
     */
    @Override
    public void protectSheet(String password) {
        if (password != null) {
            CTSheetProtection sheetProtection = safeGetProtectionField();
            setSheetPassword(password, null); // defaults to xor password
            sheetProtection.setSheet(true);
            sheetProtection.setScenarios(true);
            sheetProtection.setObjects(true);
        } else {
            worksheet.unsetSheetProtection();
        }
    }

    /**
     * Sets the sheet password. 
     *
     * @param password if null, the password will be removed
     * @param hashAlgo if null, the password will be set as XOR password (Excel 2010 and earlier)
     *  otherwise the given algorithm is used for calculating the hash password (Excel 2013)
     */
    public void setSheetPassword(String password, HashAlgorithm hashAlgo) {
        if (password == null && !isSheetProtectionEnabled()) {
            return;
        }
        setPassword(safeGetProtectionField(), password, hashAlgo, null);
    }

    /**
     * Validate the password against the stored hash, the hashing method will be determined
     *  by the existing password attributes
     * @return true, if the hashes match (... though original password may differ ...)
     */
    public boolean validateSheetPassword(String password) {
        if (!isSheetProtectionEnabled()) {
            return (password == null);
        }
        return validatePassword(safeGetProtectionField(), password, null);
    }

    /**
     * Returns the logical row ( 0-based).  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rownum  row to get
     * @return <code>XSSFRow</code> representing the rownumber or <code>null</code> if its not defined on the sheet
     */
    @Override
    public XSSFRow getRow(int rownum) {
        // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer rownumI = Integer.valueOf(rownum); // NOSONAR
        return _rows.get(rownumI);
    }

    /**
     * returns all rows between startRow and endRow, inclusive.
     * Rows between startRow and endRow that haven't been created are not included
     * in result unless createRowIfMissing is true
     *
     * @param startRowNum the first row number in this sheet to return
     * @param endRowNum the last row number in this sheet to return
     * @param createRowIfMissing
     * @return All rows between startRow and endRow, inclusive
     * @throws IllegalArgumentException if startRowNum and endRowNum are not in ascending order
     */
    private List<XSSFRow> getRows(int startRowNum, int endRowNum, boolean createRowIfMissing) {
        if (startRowNum > endRowNum) {
            throw new IllegalArgumentException("getRows: startRowNum must be less than or equal to endRowNum");
        }
        final List<XSSFRow> rows = new ArrayList<>();
        if (createRowIfMissing) {
            for (int i = startRowNum; i <= endRowNum; i++) {
                XSSFRow row = getRow(i);
                if (row == null) {
                    row = createRow(i);
                }
                rows.add(row);
            }
        }
        else {
            // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
            final Integer startI = Integer.valueOf(startRowNum); // NOSONAR
            final Integer endI = Integer.valueOf(endRowNum+1); // NOSONAR
            final Collection<XSSFRow> inclusive = _rows.subMap(startI, endI).values();
            rows.addAll(inclusive);
        }
        return rows;
    }

    /**
     * Horizontal page break information used for print layout view, page layout view, drawing print breaks in normal
     *  view, and for printing the worksheet.
     *
     * @return row indexes of all the horizontal page breaks, never <code>null</code>
     */
    @Override
    public int[] getRowBreaks() {
        return worksheet.isSetRowBreaks() ? getBreaks(worksheet.getRowBreaks()) : new int[0];

    }

    /**
     * Flag indicating whether summary rows appear below detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary row is inserted below the detailed data being summarized and a
     * new outline level is established on that row.
     * </p>
     * <p>
     * When false a summary row is inserted above the detailed data being summarized and a new outline level
     * is established on that row.
     * </p>
     * @return <code>true</code> if row summaries appear below detail in the outline
     */
    @Override
    public boolean getRowSumsBelow() {
        CTSheetPr sheetPr = worksheet.getSheetPr();
        CTOutlinePr outlinePr = (sheetPr != null && sheetPr.isSetOutlinePr())
                ? sheetPr.getOutlinePr() : null;
        return outlinePr == null || outlinePr.getSummaryBelow();
    }

    /**
     * Flag indicating whether summary rows appear below detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary row is inserted below the detailed data being summarized and a
     * new outline level is established on that row.
     * </p>
     * <p>
     * When false a summary row is inserted above the detailed data being summarized and a new outline level
     * is established on that row.
     * </p>
     * @param value <code>true</code> if row summaries appear below detail in the outline
     */
    @Override
    public void setRowSumsBelow(boolean value) {
        ensureOutlinePr().setSummaryBelow(value);
    }

    /**
     * Flag indicating whether summary columns appear to the right of detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary column is inserted to the right of the detailed data being summarized
     * and a new outline level is established on that column.
     * </p>
     * <p>
     * When false a summary column is inserted to the left of the detailed data being
     * summarized and a new outline level is established on that column.
     * </p>
     * @return <code>true</code> if col summaries appear right of the detail in the outline
     */
    @Override
    public boolean getRowSumsRight() {
        CTSheetPr sheetPr = worksheet.getSheetPr();
        CTOutlinePr outlinePr = (sheetPr != null && sheetPr.isSetOutlinePr())
                ? sheetPr.getOutlinePr() : CTOutlinePr.Factory.newInstance();
        return outlinePr.getSummaryRight();
    }

    /**
     * Flag indicating whether summary columns appear to the right of detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary column is inserted to the right of the detailed data being summarized
     * and a new outline level is established on that column.
     * </p>
     * <p>
     * When false a summary column is inserted to the left of the detailed data being
     * summarized and a new outline level is established on that column.
     * </p>
     * @param value <code>true</code> if col summaries appear right of the detail in the outline
     */
    @Override
    public void setRowSumsRight(boolean value) {
        ensureOutlinePr().setSummaryRight(value);
    }


    /**
     * Ensure CTWorksheet.CTSheetPr.CTOutlinePr
     */
    private CTOutlinePr ensureOutlinePr(){
        CTSheetPr sheetPr = worksheet.isSetSheetPr() ? worksheet.getSheetPr() : worksheet.addNewSheetPr();
        return sheetPr.isSetOutlinePr() ? sheetPr.getOutlinePr() : sheetPr.addNewOutlinePr();
    }

    /**
     * A flag indicating whether scenarios are locked when the sheet is protected.
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    @Override
    public boolean getScenarioProtect() {
        return worksheet.isSetSheetProtection() && worksheet.getSheetProtection().getScenarios();
    }

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return integer indicating the rownum (0 based) of the top row
     */
    @Override
    public short getTopRow() {
        final CTSheetView dsv = getDefaultSheetView(false);
        final String cellRef = (dsv == null) ? null : dsv.getTopLeftCell();
        if(cellRef == null) {
            return 0;
        }
        return (short) new CellReference(cellRef).getRow();
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     *
     * @return whether printed output for this sheet will be vertically centered.
     */
    @Override
    public boolean getVerticallyCenter() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getVerticalCentered();
    }

    /**
     * Group between (0 based) columns
     */
    @Override
    public void groupColumn(int fromColumn, int toColumn) {
        groupColumn1Based(fromColumn+1, toColumn+1);
    }
    private void groupColumn1Based(int fromColumn, int toColumn) {
        CTCols ctCols=worksheet.getColsArray(0);
        CTCol ctCol=CTCol.Factory.newInstance();

        // copy attributes, as they might be removed by merging with the new column
        // TODO: check if this fix is really necessary or if the sweeping algorithm
        // in addCleanColIntoCols needs to be adapted ...
        CTCol fixCol_before = this.columnHelper.getColumn1Based(toColumn, false);
        if (fixCol_before != null) {
            fixCol_before = (CTCol)fixCol_before.copy();
        }

        ctCol.setMin(fromColumn);
        ctCol.setMax(toColumn);
        this.columnHelper.addCleanColIntoCols(ctCols, ctCol);

        CTCol fixCol_after = this.columnHelper.getColumn1Based(toColumn, false);
        if (fixCol_before != null && fixCol_after != null) {
            this.columnHelper.setColumnAttributes(fixCol_before, fixCol_after);
        }

        for(int index=fromColumn;index<=toColumn;index++){
            CTCol col=columnHelper.getColumn1Based(index, false);
            //col must exist
            short outlineLevel=col.getOutlineLevel();
            col.setOutlineLevel((short)(outlineLevel+1));
            index=(int)col.getMax();
        }
        worksheet.setColsArray(0,ctCols);
        setSheetFormatPrOutlineLevelCol();
    }

    /**
     * Do not leave the width attribute undefined (see #52186).
     */
    private void setColWidthAttribute(CTCols ctCols) {
        for (CTCol col : ctCols.getColArray()) {
            if (!col.isSetWidth()) {
                col.setWidth(getDefaultColumnWidth());
                col.setCustomWidth(false);
            }
        }
    }

    /**
     * Tie a range of cell together so that they can be collapsed or expanded
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    @Override
    public void groupRow(int fromRow, int toRow) {
        for (int i = fromRow; i <= toRow; i++) {
            XSSFRow xrow = getRow(i);
            if (xrow == null) {
                xrow = createRow(i);
            }
            CTRow ctrow = xrow.getCTRow();
            short outlineLevel = ctrow.getOutlineLevel();
            ctrow.setOutlineLevel((short) (outlineLevel + 1));
        }
        setSheetFormatPrOutlineLevelRow();
    }

    private short getMaxOutlineLevelRows(){
        int outlineLevel = 0;
        for (XSSFRow xrow : _rows.values()) {
            outlineLevel = Math.max(outlineLevel, xrow.getCTRow().getOutlineLevel());
        }
        return (short) outlineLevel;
    }

    private short getMaxOutlineLevelCols() {
        CTCols ctCols = worksheet.getColsArray(0);
        int outlineLevel = 0;
        for (CTCol col : ctCols.getColArray()) {
            outlineLevel = Math.max(outlineLevel, col.getOutlineLevel());
        }
        return (short) outlineLevel;
    }

    /**
     * Determines if there is a page break at the indicated column
     */
    @Override
    public boolean isColumnBroken(int column) {
        for (int colBreak : getColumnBreaks()) {
            if (colBreak == column) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the hidden state for a given column.
     *
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    @Override
    public boolean isColumnHidden(int columnIndex) {
        CTCol col = columnHelper.getColumn(columnIndex, false);
        return col != null && col.getHidden();
    }

    /**
     * Gets the flag indicating whether this sheet should display formulas.
     *
     * @return <code>true</code> if this sheet should display formulas.
     */
    @Override
    public boolean isDisplayFormulas() {
        final CTSheetView dsv = getDefaultSheetView(false);
        return (dsv != null) ? dsv.getShowFormulas() : false;
    }

    /**
     * Gets the flag indicating whether this sheet displays the lines
     * between rows and columns to make editing and reading easier.
     *
     * @return <code>true</code> (default) if this sheet displays gridlines.
     * @see #isPrintGridlines() to check if printing of gridlines is turned on or off
     */
    @Override
    public boolean isDisplayGridlines() {
        final CTSheetView dsv = getDefaultSheetView(false);
        return (dsv != null) ? dsv.getShowGridLines() : true;
    }

    /**
     * Sets the flag indicating whether this sheet should display the lines
     * between rows and columns to make editing and reading easier.
     * To turn printing of gridlines use {@link #setPrintGridlines(boolean)}
     *
     *
     * @param show <code>true</code> if this sheet should display gridlines.
     * @see #setPrintGridlines(boolean)
     */
    @Override
    public void setDisplayGridlines(boolean show) {
        final CTSheetView dsv = getDefaultSheetView(true);
        assert(dsv != null);
        dsv.setShowGridLines(show);
    }

    /**
     * Gets the flag indicating whether this sheet should display row and column headings.
     * <p>
     * Row heading are the row numbers to the side of the sheet
     * </p>
     * <p>
     * Column heading are the letters or numbers that appear above the columns of the sheet
     * </p>
     *
     * @return <code>true</code> (default) if this sheet should display row and column headings.
     */
    @Override
    public boolean isDisplayRowColHeadings() {
        final CTSheetView dsv = getDefaultSheetView(false);
        return (dsv != null) ? dsv.getShowRowColHeaders() : true;
    }

    /**
     * Sets the flag indicating whether this sheet should display row and column headings.
     * <p>
     * Row heading are the row numbers to the side of the sheet
     * </p>
     * <p>
     * Column heading are the letters or numbers that appear above the columns of the sheet
     * </p>
     *
     * @param show <code>true</code> if this sheet should display row and column headings.
     */
    @Override
    public void setDisplayRowColHeadings(boolean show) {
        final CTSheetView dsv = getDefaultSheetView(true);
        assert(dsv != null);
        dsv.setShowRowColHeaders(show);
    }

    /**
     * Returns whether gridlines are printed.
     *
     * @return whether gridlines are printed
     */
    @Override
    public boolean isPrintGridlines() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getGridLines();
    }

    /**
     * Turns on or off the printing of gridlines.
     *
     * @param value boolean to turn on or off the printing of gridlines
     */
    @Override
    public void setPrintGridlines(boolean value) {
        CTPrintOptions opts = worksheet.isSetPrintOptions() ?
                worksheet.getPrintOptions() : worksheet.addNewPrintOptions();
        opts.setGridLines(value);
    }

    /**
     * Returns whether row and column headings are printed.
     *
     * @return whether row and column headings are printed
     */
    @Override
    public boolean isPrintRowAndColumnHeadings() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getHeadings();
    }

    /**
     * Turns on or off the printing of row and column headings.
     *
     * @param value boolean to turn on or off the printing of row and column headings
     */
    @Override
    public void setPrintRowAndColumnHeadings(boolean value) {
        CTPrintOptions opts = worksheet.isSetPrintOptions() ?
                worksheet.getPrintOptions() : worksheet.addNewPrintOptions();
        opts.setHeadings(value);
    }

    /**
     * Tests if there is a page break at the indicated row
     *
     * @param row index of the row to test
     * @return <code>true</code> if there is a page break at the indicated row
     */
    @Override
    public boolean isRowBroken(int row) {
        for (int rowBreak : getRowBreaks()) {
            if (rowBreak == row) {
                return true;
            }
        }
        return false;
    }

    private void setBreak(int id, CTPageBreak ctPgBreak, int lastIndex) {
        CTBreak brk = ctPgBreak.addNewBrk();
        brk.setId(id + 1); // this is id of the element which is 1-based: <row r="1" ... >
        brk.setMan(true);
        brk.setMax(lastIndex); //end column of the break

        int nPageBreaks = ctPgBreak.sizeOfBrkArray();
        ctPgBreak.setCount(nPageBreaks);
        ctPgBreak.setManualBreakCount(nPageBreaks);
    }

    /**
     * Sets a page break at the indicated row
     * Breaks occur above the specified row and left of the specified column inclusive.
     *
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param row the row to break, inclusive
     */
    @Override
    public void setRowBreak(int row) {
        if (!isRowBroken(row)) {
            CTPageBreak pgBreak = worksheet.isSetRowBreaks() ? worksheet.getRowBreaks() : worksheet.addNewRowBreaks();
            setBreak(row, pgBreak, SpreadsheetVersion.EXCEL2007.getLastColumnIndex());
        }
    }

    /**
     * Removes a page break at the indicated column
     */
    @Override
    public void removeColumnBreak(int column) {
        if (worksheet.isSetColBreaks()) {
            removeBreak(column, worksheet.getColBreaks());
        } // else no breaks
    }

    /**
     * Removes a merged region of cells (hence letting them free)
     *
     * @param index of the region to unmerge
     */
    @Override
    public void removeMergedRegion(int index) {
        if (!worksheet.isSetMergeCells()) {
            return;
        }

        CTMergeCells ctMergeCells = worksheet.getMergeCells();
        int size = ctMergeCells.sizeOfMergeCellArray();
        assert(0 <= index && index < size);
        if (size > 1) {
            ctMergeCells.removeMergeCell(index);
        } else {
            worksheet.unsetMergeCells();
        }
    }

    /**
     * Removes a number of merged regions of cells (hence letting them free)
     *
     * This method can be used to bulk-remove merged regions in a way
     * much faster than calling removeMergedRegion() for every single
     * merged region.
     *
     * @param indices A set of the regions to unmerge
     */
    @Override
    public void removeMergedRegions(Collection<Integer> indices) {
        if (!worksheet.isSetMergeCells()) {
            return;
        }

        CTMergeCells ctMergeCells = worksheet.getMergeCells();
        List<CTMergeCell> newMergeCells = new ArrayList<>(ctMergeCells.sizeOfMergeCellArray());

        int idx = 0;
        for (CTMergeCell mc : ctMergeCells.getMergeCellArray()) {
            if (!indices.contains(idx++)) {
                newMergeCells.add(mc);
            }
        }

        if (newMergeCells.isEmpty()) {
            worksheet.unsetMergeCells();
        } else {
            CTMergeCell[] newMergeCellsArray = new CTMergeCell[newMergeCells.size()];
            ctMergeCells.setMergeCellArray(newMergeCells.toArray(newMergeCellsArray));
        }
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row  the row to remove.
     */
    @Override
    public void removeRow(Row row) {
        if (row.getSheet() != this) {
            throw new IllegalArgumentException("Specified row does not belong to this sheet");
        }
        // collect cells into a temporary array to avoid ConcurrentModificationException
        ArrayList<XSSFCell> cellsToDelete = new ArrayList<>();
        for (Cell cell : row) {
            cellsToDelete.add((XSSFCell)cell);
        }

        for (XSSFCell cell : cellsToDelete) {
            row.removeCell(cell);
        }

        // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final int rowNum = row.getRowNum();
        final Integer rowNumI = Integer.valueOf(rowNum); // NOSONAR
        // this is not the physical row number!
        final int idx = _rows.headMap(rowNumI).size();
        _rows.remove(rowNumI);
        worksheet.getSheetData().removeRow(idx);

        // also remove any comment located in that row
        if(sheetComments != null) {
            for (CellAddress ref : getCellComments().keySet()) {
                if (ref.getRow() == rowNum) {
                    sheetComments.removeComment(ref);
                }
            }
        }
    }

    /**
     * Removes the page break at the indicated row
     */
    @Override
    public void removeRowBreak(int row) {
        if (worksheet.isSetRowBreaks()) {
            removeBreak(row, worksheet.getRowBreaks());
        } // else no breaks
    }

    /**
     * Control if Excel should be asked to recalculate all formulas on this sheet
     * when the workbook is opened.
     *
     *  <p>
     *  Calculating the formula values with {@link org.apache.poi.ss.usermodel.FormulaEvaluator} is the
     *  recommended solution, but this may be used for certain cases where
     *  evaluation in POI is not possible.
     *  </p>
     *
     *  <p>
     *  It is recommended to force recalcuation of formulas on workbook level using
     *  {@link org.apache.poi.ss.usermodel.Workbook#setForceFormulaRecalculation(boolean)}
     *  to ensure that all cross-worksheet formuals and external dependencies are updated.
     *  </p>
     * @param value true if the application will perform a full recalculation of
     * this worksheet values when the workbook is opened
     *
     * @see org.apache.poi.ss.usermodel.Workbook#setForceFormulaRecalculation(boolean)
     */
    @Override
    public void setForceFormulaRecalculation(boolean value) {
        CTCalcPr calcPr = getWorkbook().getCTWorkbook().getCalcPr();

        if(worksheet.isSetSheetCalcPr()) {
            // Change the current setting
            CTSheetCalcPr calc = worksheet.getSheetCalcPr();
            calc.setFullCalcOnLoad(value);
        }
        else if(value) {
            // Add the Calc block and set it
            CTSheetCalcPr calc = worksheet.addNewSheetCalcPr();
            calc.setFullCalcOnLoad(value);
        }
        if(value && calcPr != null && calcPr.getCalcMode() == STCalcMode.MANUAL) {
            calcPr.setCalcMode(STCalcMode.AUTO);
        }

    }

    /**
     * Whether Excel will be asked to recalculate all formulas when the
     *  workbook is opened.
     */
    @Override
    public boolean getForceFormulaRecalculation() {
        if(worksheet.isSetSheetCalcPr()) {
            CTSheetCalcPr calc = worksheet.getSheetCalcPr();
            return calc.getFullCalcOnLoad();
        }
        return false;
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Row> rowIterator() {
        return (Iterator<Row>)(Iterator<? extends Row>) _rows.values().iterator();
    }

    /**
     * Alias for {@link #rowIterator()} to
     *  allow foreach loops
     */
    @Override
    public Iterator<Row> iterator() {
        return rowIterator();
    }

    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @return <code>true</code> if the sheet displays Automatic Page Breaks.
     */
    @Override
    public boolean getAutobreaks() {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTPageSetUpPr psSetup = (sheetPr == null || !sheetPr.isSetPageSetUpPr()) ?
                CTPageSetUpPr.Factory.newInstance() : sheetPr.getPageSetUpPr();
        return psSetup.getAutoPageBreaks();
    }

    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @param value <code>true</code> if the sheet displays Automatic Page Breaks.
     */
    @Override
    public void setAutobreaks(boolean value) {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTPageSetUpPr psSetup = sheetPr.isSetPageSetUpPr() ? sheetPr.getPageSetUpPr() : sheetPr.addNewPageSetUpPr();
        psSetup.setAutoPageBreaks(value);
    }

    /**
     * Sets a page break at the indicated column.
     * Breaks occur above the specified row and left of the specified column inclusive.
     *
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param column the column to break, inclusive
     */
    @Override
    public void setColumnBreak(int column) {
        if (!isColumnBroken(column)) {
            CTPageBreak pgBreak = worksheet.isSetColBreaks() ? worksheet.getColBreaks() : worksheet.addNewColBreaks();
            setBreak(column, pgBreak, SpreadsheetVersion.EXCEL2007.getLastRowIndex());
        }
    }

    @Override
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        if (collapsed) {
            collapseColumn(columnNumber);
        } else {
            expandColumn(columnNumber);
        }
    }

    private void collapseColumn(int columnNumber) {
        CTCols cols = worksheet.getColsArray(0);
        CTCol col = columnHelper.getColumn(columnNumber, false);
        int colInfoIx = columnHelper.getIndexOfColumn(cols, col);
        if (colInfoIx == -1) {
            return;
        }
        // Find the start of the group.
        int groupStartColInfoIx = findStartOfColumnOutlineGroup(colInfoIx);

        CTCol columnInfo = cols.getColArray(groupStartColInfoIx);

        // Hide all the columns until the end of the group
        int lastColMax = setGroupHidden(groupStartColInfoIx, columnInfo
                .getOutlineLevel(), true);

        // write collapse field
        setColumn(lastColMax + 1, 0, null, null, Boolean.TRUE);

    }

    private void setColumn(int targetColumnIx, Integer style,
                           Integer level, Boolean hidden, Boolean collapsed) {
        CTCols cols = worksheet.getColsArray(0);
        CTCol ci = null;
        for (CTCol tci : cols.getColArray()) {
            long tciMin = tci.getMin();
            long tciMax = tci.getMax();
            if (tciMin >= targetColumnIx && tciMax <= targetColumnIx) {
                ci = tci;
                break;
            }
            if (tciMin > targetColumnIx) {
                // call column infos after k are for later columns
                break; // exit now so k will be the correct insert pos
            }
        }

        if (ci == null) {
            // okay so there ISN'T a column info record that covers this column
            // so lets create one!
            CTCol nci = CTCol.Factory.newInstance();
            nci.setMin(targetColumnIx);
            nci.setMax(targetColumnIx);
            unsetCollapsed(collapsed, nci);
            this.columnHelper.addCleanColIntoCols(cols, nci);
            return;
        }

        boolean styleChanged = style != null && ci.getStyle() != style;
        boolean levelChanged = level != null && ci.getOutlineLevel() != level;
        boolean hiddenChanged = hidden != null && ci.getHidden() != hidden;
        boolean collapsedChanged = collapsed != null && ci.getCollapsed() != collapsed;
        boolean columnChanged = levelChanged || hiddenChanged || collapsedChanged || styleChanged;
        if (!columnChanged) {
            // do nothing...nothing changed.
            return;
        }

        long ciMin = ci.getMin();
        long ciMax = ci.getMax();
        if (ciMin == targetColumnIx && ciMax == targetColumnIx) {
            // ColumnInfo ci for a single column, the target column
            unsetCollapsed(collapsed, ci);
            return;
        }

        if (ciMin == targetColumnIx || ciMax == targetColumnIx) {
            // The target column is at either end of the multi-column ColumnInfo
            // ci
            // we'll just divide the info and create a new one
            if (ciMin == targetColumnIx) {
                ci.setMin(targetColumnIx + 1);
            } else {
                ci.setMax(targetColumnIx - 1);
            }
            CTCol nci = columnHelper.cloneCol(cols, ci);
            nci.setMin(targetColumnIx);
            unsetCollapsed(collapsed, nci);
            this.columnHelper.addCleanColIntoCols(cols, nci);

        } else {
            // split to 3 records
            CTCol ciMid = columnHelper.cloneCol(cols, ci);
            CTCol ciEnd = columnHelper.cloneCol(cols, ci);
            int lastcolumn = (int) ciMax;

            ci.setMax(targetColumnIx - 1);

            ciMid.setMin(targetColumnIx);
            ciMid.setMax(targetColumnIx);
            unsetCollapsed(collapsed, ciMid);
            this.columnHelper.addCleanColIntoCols(cols, ciMid);

            ciEnd.setMin(targetColumnIx + 1);
            ciEnd.setMax(lastcolumn);
            this.columnHelper.addCleanColIntoCols(cols, ciEnd);
        }
    }

    private void unsetCollapsed(boolean collapsed, CTCol ci) {
        if (collapsed) {
            ci.setCollapsed(collapsed);
        } else {
            ci.unsetCollapsed();
        }
    }

    /**
     * Sets all adjacent columns of the same outline level to the specified
     * hidden status.
     *
     * @param pIdx
     *                the col info index of the start of the outline group
     * @return the column index of the last column in the outline group
     */
    private int setGroupHidden(int pIdx, int level, boolean hidden) {
        CTCols cols = worksheet.getColsArray(0);
        int idx = pIdx;
        CTCol[] colArray = cols.getColArray();
        CTCol columnInfo = colArray[idx];
        while (idx < colArray.length) {
            columnInfo.setHidden(hidden);
            if (idx + 1 < colArray.length) {
                CTCol nextColumnInfo = colArray[idx + 1];

                if (!isAdjacentBefore(columnInfo, nextColumnInfo)) {
                    break;
                }

                if (nextColumnInfo.getOutlineLevel() < level) {
                    break;
                }
                columnInfo = nextColumnInfo;
            }
            idx++;
        }
        return (int) columnInfo.getMax();
    }

    private boolean isAdjacentBefore(CTCol col, CTCol otherCol) {
        return col.getMax() == otherCol.getMin() - 1;
    }

    private int findStartOfColumnOutlineGroup(int pIdx) {
        // Find the start of the group.
        CTCols cols = worksheet.getColsArray(0);
        CTCol[] colArray = cols.getColArray();
        CTCol columnInfo = colArray[pIdx];
        int level = columnInfo.getOutlineLevel();
        int idx = pIdx;
        while (idx != 0) {
            CTCol prevColumnInfo = colArray[idx - 1];
            if (!isAdjacentBefore(prevColumnInfo, columnInfo)) {
                break;
            }
            if (prevColumnInfo.getOutlineLevel() < level) {
                break;
            }
            idx--;
            columnInfo = prevColumnInfo;
        }
        return idx;
    }

    private int findEndOfColumnOutlineGroup(int colInfoIndex) {
        CTCols cols = worksheet.getColsArray(0);
        // Find the end of the group.
        CTCol[] colArray = cols.getColArray();
        CTCol columnInfo = colArray[colInfoIndex];
        int level = columnInfo.getOutlineLevel();
        int idx = colInfoIndex;
        int lastIdx = colArray.length - 1;
        while (idx < lastIdx) {
            CTCol nextColumnInfo = colArray[idx + 1];
            if (!isAdjacentBefore(columnInfo, nextColumnInfo)) {
                break;
            }
            if (nextColumnInfo.getOutlineLevel() < level) {
                break;
            }
            idx++;
            columnInfo = nextColumnInfo;
        }
        return idx;
    }

    private void expandColumn(int columnIndex) {
        CTCols cols = worksheet.getColsArray(0);
        CTCol col = columnHelper.getColumn(columnIndex, false);
        int colInfoIx = columnHelper.getIndexOfColumn(cols, col);

        int idx = findColInfoIdx((int) col.getMax(), colInfoIx);
        if (idx == -1) {
            return;
        }

        // If it is already expanded do nothing.
        if (!isColumnGroupCollapsed(idx)) {
            return;
        }

        // Find the start/end of the group.
        int startIdx = findStartOfColumnOutlineGroup(idx);
        int endIdx = findEndOfColumnOutlineGroup(idx);

        // expand:
        // colapsed bit must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can
        // determine
        // this by looking at the hidden bit of the enclosing group. You will
        // have
        // to look at the start and the end of the current group to determine
        // which
        // is the enclosing group
        // hidden bit only is altered for this outline level. ie. don't
        // uncollapse contained groups
        CTCol[] colArray = cols.getColArray();
        CTCol columnInfo = colArray[endIdx];
        if (!isColumnGroupHiddenByParent(idx)) {
            short outlineLevel = columnInfo.getOutlineLevel();
            boolean nestedGroup = false;
            for (int i = startIdx; i <= endIdx; i++) {
                CTCol ci = colArray[i];
                if (outlineLevel == ci.getOutlineLevel()) {
                    ci.unsetHidden();
                    if (nestedGroup) {
                        nestedGroup = false;
                        ci.setCollapsed(true);
                    }
                } else {
                    nestedGroup = true;
                }
            }
        }
        // Write collapse flag (stored in a single col info record after this
        // outline group)
        setColumn((int) columnInfo.getMax() + 1, null, null,
                Boolean.FALSE, Boolean.FALSE);
    }

    private boolean isColumnGroupHiddenByParent(int idx) {
        CTCols cols = worksheet.getColsArray(0);
        // Look out outline details of end
        int endLevel = 0;
        boolean endHidden = false;
        int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(idx);
        CTCol[] colArray = cols.getColArray();
        if (endOfOutlineGroupIdx < colArray.length) {
            CTCol nextInfo = colArray[endOfOutlineGroupIdx + 1];
            if (isAdjacentBefore(colArray[endOfOutlineGroupIdx], nextInfo)) {
                endLevel = nextInfo.getOutlineLevel();
                endHidden = nextInfo.getHidden();
            }
        }
        // Look out outline details of start
        int startLevel = 0;
        boolean startHidden = false;
        int startOfOutlineGroupIdx = findStartOfColumnOutlineGroup(idx);
        if (startOfOutlineGroupIdx > 0) {
            CTCol prevInfo = colArray[startOfOutlineGroupIdx - 1];

            if (isAdjacentBefore(prevInfo, colArray[startOfOutlineGroupIdx])) {
                startLevel = prevInfo.getOutlineLevel();
                startHidden = prevInfo.getHidden();
            }

        }
        if (endLevel > startLevel) {
            return endHidden;
        }
        return startHidden;
    }

    private int findColInfoIdx(int columnValue, int fromColInfoIdx) {
        CTCols cols = worksheet.getColsArray(0);

        if (columnValue < 0) {
            throw new IllegalArgumentException(
                    "column parameter out of range: " + columnValue);
        }
        if (fromColInfoIdx < 0) {
            throw new IllegalArgumentException(
                    "fromIdx parameter out of range: " + fromColInfoIdx);
        }

        CTCol[] colArray = cols.getColArray();
        for (int k = fromColInfoIdx; k < colArray.length; k++) {
            CTCol ci = colArray[k];

            if (containsColumn(ci, columnValue)) {
                return k;
            }

            if (ci.getMin() > fromColInfoIdx) {
                break;
            }

        }
        return -1;
    }

    private boolean containsColumn(CTCol col, int columnIndex) {
        return col.getMin() <= columnIndex && columnIndex <= col.getMax();
    }

    /**
     * 'Collapsed' state is stored in a single column col info record
     * immediately after the outline group
     *
     * @param idx
     * @return a boolean represented if the column is collapsed
     */
    private boolean isColumnGroupCollapsed(int idx) {
        CTCols cols = worksheet.getColsArray(0);
        CTCol[] colArray = cols.getColArray();
        int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(idx);
        int nextColInfoIx = endOfOutlineGroupIdx + 1;
        if (nextColInfoIx >= colArray.length) {
            return false;
        }
        CTCol nextColInfo = colArray[nextColInfoIx];

        CTCol col = colArray[endOfOutlineGroupIdx];
        if (!isAdjacentBefore(col, nextColInfo)) {
            return false;
        }

        return nextColInfo.getCollapsed();
    }

    /**
     * Get the visibility state for a given column.
     *
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */
    @Override
    public void setColumnHidden(int columnIndex, boolean hidden) {
        columnHelper.setColHidden(columnIndex, hidden);
    }

    /**
     * Set the width (in units of 1/256th of a character width)
     *
     * <p>
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font (first font in the workbook).
     * </p>
     *
     * <p>
     * Character width is defined as the maximum digit width
     * of the numbers <code>0, 1, 2, ... 9</code> as rendered
     * using the default font (first font in the workbook).
     * <br>
     * Unless you are using a very special font, the default character is '0' (zero),
     * this is true for Arial (default font font in HSSF) and Calibri (default font in XSSF)
     * </p>
     *
     * <p>
     * Please note, that the width set by this method includes 4 pixels of margin padding (two on each side),
     * plus 1 pixel padding for the gridlines (Section 3.3.1.12 of the OOXML spec).
     * This results is a slightly less value of visible characters than passed to this method (approx. 1/2 of a character).
     * </p>
     * <p>
     * To compute the actual number of visible characters,
     *  Excel uses the following formula (Section 3.3.1.12 of the OOXML spec):
     * </p>
     * <code>
     *     width = Truncate([{Number of Visible Characters} *
     *      {Maximum Digit Width} + {5 pixel padding}]/{Maximum Digit Width}*256)/256
     * </code>
     * <p>Using the Calibri font as an example, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
     *  If you set a column width to be eight characters wide, e.g. <code>setColumnWidth(columnIndex, 8*256)</code>,
     *  then the actual value of visible characters (the value shown in Excel) is derived from the following equation:
     *  <code>
     Truncate([numChars*7+5]/7*256)/256 = 8;
     *  </code>
     *
     *  which gives <code>7.29</code>.
     *
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     * @throws IllegalArgumentException if width &gt; 255*256 (the maximum column width in Excel is 255 characters)
     */
    @Override
    public void setColumnWidth(int columnIndex, int width) {
        if(width > 255*256) {
            throw new IllegalArgumentException("The maximum column width for an individual cell is 255 characters.");
        }

        columnHelper.setColWidth(columnIndex, (double)width/256);
        columnHelper.setCustomWidth(columnIndex, true);
    }

    @Override
    public void setDefaultColumnStyle(int column, CellStyle style) {
        columnHelper.setColDefaultStyle(column, style);
    }

    /**
     * Specifies the number of characters of the maximum digit width of the normal style's font.
     * This value does not include margin padding or extra padding for gridlines. It is only the
     * number of characters.
     *
     * @param width the number of characters. Default value is <code>8</code>.
     */
    @Override
    public void setDefaultColumnWidth(int width) {
        getSheetTypeSheetFormatPr().setBaseColWidth(width);
    }

    /**
     * Set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @param  height default row height in  twips (1/20 of  a point)
     */
    @Override
    public void setDefaultRowHeight(short height) {
        setDefaultRowHeightInPoints((float)height / TWIPS_PER_POINT);
    }

    /**
     * Sets default row height measured in point size.
     *
     * @param height default row height measured in point size.
     */
    @Override
    public void setDefaultRowHeightInPoints(float height) {
        CTSheetFormatPr pr = getSheetTypeSheetFormatPr();
        pr.setDefaultRowHeight(height);
        pr.setCustomHeight(true);
    }

    /**
     * Sets the flag indicating whether this sheet should display formulas.
     *
     * @param show <code>true</code> if this sheet should display formulas.
     */
    @Override
    public void setDisplayFormulas(boolean show) {
        final CTSheetView dsv = getDefaultSheetView(true);
        assert(dsv != null);
        dsv.setShowFormulas(show);
    }

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @param b <code>true</code> if the Fit to Page print option is enabled.
     */
    @Override
    public void setFitToPage(boolean b) {
        getSheetTypePageSetUpPr().setFitToPage(b);
    }

    /**
     * Center on page horizontally when printing.
     *
     * @param value whether to center on page horizontally when printing.
     */
    @Override
    public void setHorizontallyCenter(boolean value) {
        CTPrintOptions opts = worksheet.isSetPrintOptions() ?
                worksheet.getPrintOptions() : worksheet.addNewPrintOptions();
        opts.setHorizontalCentered(value);
    }

    /**
     * Whether the output is vertically centered on the page.
     *
     * @param value true to vertically center, false otherwise.
     */
    @Override
    public void setVerticallyCenter(boolean value) {
        CTPrintOptions opts = worksheet.isSetPrintOptions() ?
                worksheet.getPrintOptions() : worksheet.addNewPrintOptions();
        opts.setVerticalCentered(value);
    }

    /**
     * group the row It is possible for collapsed to be false and yet still have
     * the rows in question hidden. This can be achieved by having a lower
     * outline level collapsed, thus hiding all the child rows. Note that in
     * this case, if the lowest level were expanded, the middle level would
     * remain collapsed.
     *
     * @param rowIndex -
     *                the row involved, 0 based
     * @param collapse -
     *                boolean value for collapse
     */
    @Override
    public void setRowGroupCollapsed(int rowIndex, boolean collapse) {
        if (collapse) {
            collapseRow(rowIndex);
        } else {
            expandRow(rowIndex);
        }
    }

    /**
     * @param rowIndex the zero based row index to collapse
     */
    private void collapseRow(int rowIndex) {
        XSSFRow row = getRow(rowIndex);
        if (row != null) {
            int startRow = findStartOfRowOutlineGroup(rowIndex);

            // Hide all the columns until the end of the group
            int lastRow = writeHidden(row, startRow, true);
            if (getRow(lastRow) != null) {
                getRow(lastRow).getCTRow().setCollapsed(true);
            } else {
                XSSFRow newRow = createRow(lastRow);
                newRow.getCTRow().setCollapsed(true);
            }
        }
    }

    /**
     * @param rowIndex the zero based row index to find from
     */
    private int findStartOfRowOutlineGroup(int rowIndex) {
        // Find the start of the group.
        short level = getRow(rowIndex).getCTRow().getOutlineLevel();
        int currentRow = rowIndex;
        while (getRow(currentRow) != null) {
            if (getRow(currentRow).getCTRow().getOutlineLevel() < level) {
                return currentRow + 1;
            }
            currentRow--;
        }
        return currentRow;
    }

    private int writeHidden(XSSFRow xRow, int rowIndex, boolean hidden) {
        short level = xRow.getCTRow().getOutlineLevel();
        for (Iterator<Row> it = rowIterator(); it.hasNext();) {
            xRow = (XSSFRow) it.next();

            // skip rows before the start of this group
            if(xRow.getRowNum() < rowIndex) {
                continue;
            }

            if (xRow.getCTRow().getOutlineLevel() >= level) {
                xRow.getCTRow().setHidden(hidden);
                rowIndex++;
            }

        }
        return rowIndex;
    }

    /**
     * @param rowNumber the zero based row index to expand
     */
    private void expandRow(int rowNumber) {
        if (rowNumber == -1) {
            return;
        }
        XSSFRow row = getRow(rowNumber);
        // If it is already expanded do nothing.
        if (!row.getCTRow().isSetHidden()) {
            return;
        }

        // Find the start of the group.
        int startIdx = findStartOfRowOutlineGroup(rowNumber);

        // Find the end of the group.
        int endIdx = findEndOfRowOutlineGroup(rowNumber);

        // expand:
        // collapsed must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can
        // determine
        // this by looking at the hidden bit of the enclosing group. You will
        // have
        // to look at the start and the end of the current group to determine
        // which
        // is the enclosing group
        // hidden bit only is altered for this outline level. ie. don't
        // un-collapse contained groups
        short level = row.getCTRow().getOutlineLevel();
        if (!isRowGroupHiddenByParent(rowNumber)) {
            for (int i = startIdx; i < endIdx; i++) {
                if (level == getRow(i).getCTRow().getOutlineLevel()) {
                    getRow(i).getCTRow().unsetHidden();
                } else if (!isRowGroupCollapsed(i)) {
                    getRow(i).getCTRow().unsetHidden();
                }
            }
        }
        // Write collapse field
        CTRow ctRow = getRow(endIdx).getCTRow();
        // This avoids an IndexOutOfBounds if multiple nested groups are collapsed/expanded
        if(ctRow.getCollapsed()) {
            ctRow.unsetCollapsed();
        }
    }

    /**
     * @param row the zero based row index to find from
     */
    public int findEndOfRowOutlineGroup(int row) {
        short level = getRow(row).getCTRow().getOutlineLevel();
        int currentRow;
        final int lastRowNum = getLastRowNum();
        for (currentRow = row; currentRow < lastRowNum; currentRow++) {
            if (getRow(currentRow) == null
                    || getRow(currentRow).getCTRow().getOutlineLevel() < level) {
                break;
            }
        }
        return currentRow;
    }

    /**
     * @param row the zero based row index to find from
     */
    private boolean isRowGroupHiddenByParent(int row) {
        // Look out outline details of end
        int endLevel;
        boolean endHidden;
        int endOfOutlineGroupIdx = findEndOfRowOutlineGroup(row);
        if (getRow(endOfOutlineGroupIdx) == null) {
            endLevel = 0;
            endHidden = false;
        } else {
            endLevel = getRow(endOfOutlineGroupIdx).getCTRow().getOutlineLevel();
            endHidden = getRow(endOfOutlineGroupIdx).getCTRow().getHidden();
        }

        // Look out outline details of start
        int startLevel;
        boolean startHidden;
        int startOfOutlineGroupIdx = findStartOfRowOutlineGroup(row);
        if (startOfOutlineGroupIdx < 0
                || getRow(startOfOutlineGroupIdx) == null) {
            startLevel = 0;
            startHidden = false;
        } else {
            startLevel = getRow(startOfOutlineGroupIdx).getCTRow()
                    .getOutlineLevel();
            startHidden = getRow(startOfOutlineGroupIdx).getCTRow()
                    .getHidden();
        }
        if (endLevel > startLevel) {
            return endHidden;
        }
        return startHidden;
    }

    /**
     * @param row the zero based row index to find from
     */
    private boolean isRowGroupCollapsed(int row) {
        int collapseRow = findEndOfRowOutlineGroup(row) + 1;
        if (getRow(collapseRow) == null) {
            return false;
        }
        return getRow(collapseRow).getCTRow().getCollapsed();
    }

    /**
     * Window zoom magnification for current view representing percent values.
     * Valid values range from 10 to 400. Horizontal &amp; Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * ...
     * 100 - 100%
     * ...
     * 400 - 400%
     * </pre>
     *
     * Current view can be Normal, Page Layout, or Page Break Preview.
     *
     * @param scale window zoom magnification
     * @throws IllegalArgumentException if scale is invalid
     */
    @Override
    public void setZoom(int scale) {
        if (scale < 10 || scale > 400) {
            throw new IllegalArgumentException("Valid scale values range from 10 to 400");
        }
        final CTSheetView dsv = getDefaultSheetView(true);
        assert(dsv != null);
        dsv.setZoomScale(scale);
    }


    /**
     * copyRows rows from srcRows to this sheet starting at destStartRow
     *
     * Additionally copies merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * @param srcRows the rows to copy. Formulas will be offset by the difference
     * in the row number of the first row in srcRows and destStartRow (even if srcRows
     * are from a different sheet).
     * @param destStartRow the row in this sheet to paste the first row of srcRows
     * the remainder of srcRows will be pasted below destStartRow per the cell copy policy
     * @param policy is the cell copy policy, which can be used to merge the source and destination
     * when the source is blank, copy styles only, paste as value, etc
     */
    @Beta
    public void copyRows(List<? extends Row> srcRows, int destStartRow, CellCopyPolicy policy) {
        if (srcRows == null || srcRows.size() == 0) {
            throw new IllegalArgumentException("No rows to copy");
        }
        final Row srcStartRow = srcRows.get(0);
        final Row srcEndRow = srcRows.get(srcRows.size() - 1);

        if (srcStartRow == null) {
            throw new IllegalArgumentException("copyRows: First row cannot be null");
        }

        final int srcStartRowNum = srcStartRow.getRowNum();
        final int srcEndRowNum = srcEndRow.getRowNum();

        // check row numbers to make sure they are continuous and increasing (monotonic)
        // and srcRows does not contain null rows
        final int size = srcRows.size();
        for (int index=1; index < size; index++) {
            final Row curRow = srcRows.get(index);
            if (curRow == null) {
                throw new IllegalArgumentException("srcRows may not contain null rows. Found null row at index " + index + ".");
                //} else if (curRow.getRowNum() != prevRow.getRowNum() + 1) {
                //    throw new IllegalArgumentException("srcRows must contain continuously increasing row numbers. " +
                //            "Got srcRows[" + (index-1) + "]=Row " + prevRow.getRowNum() + ", srcRows[" + index + "]=Row " + curRow.getRowNum() + ".");
                // FIXME: assumes row objects belong to non-null sheets and sheets belong to non-null workbooks.
            } else if (srcStartRow.getSheet().getWorkbook() != curRow.getSheet().getWorkbook()) {
                throw new IllegalArgumentException("All rows in srcRows must belong to the same sheet in the same workbook." +
                        "Expected all rows from same workbook (" + srcStartRow.getSheet().getWorkbook() + "). " +
                        "Got srcRows[" + index + "] from different workbook (" + curRow.getSheet().getWorkbook() + ").");
            } else if (srcStartRow.getSheet() != curRow.getSheet()) {
                throw new IllegalArgumentException("All rows in srcRows must belong to the same sheet. " +
                        "Expected all rows from " + srcStartRow.getSheet().getSheetName() + ". " +
                        "Got srcRows[" + index + "] from " + curRow.getSheet().getSheetName());
            }
        }

        // FIXME: is special behavior needed if srcRows and destRows belong to the same sheets and the regions overlap?

        final CellCopyPolicy options = new CellCopyPolicy(policy);
        // avoid O(N^2) performance scanning through all regions for each row
        // merged regions will be copied after all the rows have been copied
        options.setCopyMergedRegions(false);

        // FIXME: if srcRows contains gaps or null values, clear out those rows that will be overwritten
        // how will this work with merging (copy just values, leave cell styles in place?)

        int r = destStartRow;
        for (Row srcRow : srcRows) {
            int destRowNum;
            if (policy.isCondenseRows()) {
                destRowNum = r++;
            } else {
                final int shift = (srcRow.getRowNum() - srcStartRowNum);
                destRowNum = destStartRow + shift;
            }
            //removeRow(destRowNum); //this probably clears all external formula references to destRow, causing unwanted #REF! errors
            final XSSFRow destRow = createRow(destRowNum);
            destRow.copyRowFrom(srcRow, options);
        }

        // ======================
        // Only do additional copy operations here that cannot be done with Row.copyFromRow(Row, options)
        // reasons: operation needs to interact with multiple rows or sheets

        // Copy merged regions that are contained within the copy region
        if (policy.isCopyMergedRegions()) {
            // FIXME: is this something that rowShifter could be doing?
            final int shift = destStartRow - srcStartRowNum;
            for (CellRangeAddress srcRegion : srcStartRow.getSheet().getMergedRegions()) {
                if (srcStartRowNum <= srcRegion.getFirstRow() && srcRegion.getLastRow() <= srcEndRowNum) {
                    // srcRegion is fully inside the copied rows
                    final CellRangeAddress destRegion = srcRegion.copy();
                    destRegion.setFirstRow(destRegion.getFirstRow() + shift);
                    destRegion.setLastRow(destRegion.getLastRow() + shift);
                    addMergedRegion(destRegion);
                }
            }
        }
    }

    /**
     * Copies rows between srcStartRow and srcEndRow to the same sheet, starting at destStartRow
     * Convenience function for {@link #copyRows(List, int, CellCopyPolicy)}
     *
     * Equivalent to copyRows(getRows(srcStartRow, srcEndRow, false), destStartRow, cellCopyPolicy)
     *
     * @param srcStartRow the index of the first row to copy the cells from in this sheet
     * @param srcEndRow the index of the last row to copy the cells from in this sheet
     * @param destStartRow the index of the first row to copy the cells to in this sheet
     * @param cellCopyPolicy the policy to use to determine how cells are copied
     */
    @Beta
    public void copyRows(int srcStartRow, int srcEndRow, int destStartRow, CellCopyPolicy cellCopyPolicy) {
        final List<XSSFRow> srcRows = getRows(srcStartRow, srcEndRow, false); //FIXME: should be false, no need to create rows where src is only to copy them to dest
        copyRows(srcRows, destStartRow, cellCopyPolicy);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around.
     *
     * Calls shiftRows(startRow, endRow, n, false, false);
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     */
    @Override
    public void shiftRows(int startRow, int endRow, int n) {
        shiftRows(startRow, endRow, n, false, false);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted). All merged regions that are
     * completely overlaid by shifting will be deleted.
     * <p>
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    @Override
    public void shiftRows(int startRow, int endRow, final int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        XSSFVMLDrawing vml = getVMLDrawing(false);

        int sheetIndex = getWorkbook().getSheetIndex(this);
        String sheetName = getWorkbook().getSheetName(sheetIndex);
        FormulaShifter formulaShifter = FormulaShifter.createForRowShift(
                sheetIndex, sheetName, startRow, endRow, n, SpreadsheetVersion.EXCEL2007);
        removeOverwritten(vml, startRow, endRow, n);
        shiftCommentsAndRows(vml, startRow, endRow, n);

        XSSFRowShifter rowShifter = new XSSFRowShifter(this);
        rowShifter.shiftMergedRegions(startRow, endRow, n);
        rowShifter.updateNamedRanges(formulaShifter);
        rowShifter.updateFormulas(formulaShifter);
        rowShifter.updateConditionalFormatting(formulaShifter);
        rowShifter.updateHyperlinks(formulaShifter);

        rebuildRows();
    }

    /**
     * Shifts columns between startColumn and endColumn n number of columns.
     * If you use a negative number, it will shift columns left.
     * Code ensures that columns don't wrap around
     *
     * @param startColumn the column to start shifting
     * @param endColumn the column to end shifting
     * @param n length of the shifting step
     */
    @Override
    public void shiftColumns(int startColumn, int endColumn, final int n) {
        XSSFVMLDrawing vml = getVMLDrawing(false);
        shiftCommentsForColumns(vml, startColumn, endColumn, n);
        FormulaShifter formulaShifter = FormulaShifter.createForColumnShift(this.getWorkbook().getSheetIndex(this), this.getSheetName(), startColumn, endColumn, n, SpreadsheetVersion.EXCEL2007);
        XSSFColumnShifter columnShifter = new XSSFColumnShifter(this);
        columnShifter.shiftColumns(startColumn, endColumn, n);
        columnShifter.shiftMergedRegions(startColumn, startColumn, n);
        columnShifter.updateFormulas(formulaShifter);
        columnShifter.updateConditionalFormatting(formulaShifter);
        columnShifter.updateHyperlinks(formulaShifter);
        columnShifter.updateNamedRanges(formulaShifter);

        rebuildRows();
    }

    private final void rebuildRows() {
        //rebuild the _rows map
        List<XSSFRow> rowList = new ArrayList<>(_rows.values());
        _rows.clear();
        for(XSSFRow r : rowList) {
            // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
            final Integer rownumI = new Integer(r.getRowNum()); // NOSONAR
            _rows.put(rownumI, r);
        }
    }

    // remove all rows which will be overwritten
    private void removeOverwritten(XSSFVMLDrawing vml, int startRow, int endRow, final int n){
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            XSSFRow row = (XSSFRow)it.next();
            int rownum = row.getRowNum();

            // check if we should remove this row as it will be overwritten by the data later
            if (shouldRemoveRow(startRow, endRow, n, rownum)) {
                // remove row from worksheet.getSheetData row array
                // Performance optimization: explicit boxing is slightly faster than auto-unboxing, though may use more memory
                final Integer rownumI = Integer.valueOf(row.getRowNum()); // NOSONAR
                int idx = _rows.headMap(rownumI).size();
                worksheet.getSheetData().removeRow(idx);

                // remove row from _rows
                it.remove();

                // FIXME: (performance optimization) this should be moved outside the for-loop so that comments only needs to be iterated over once.
                // also remove any comments associated with this row
                if(sheetComments != null){
                    CTCommentList lst = sheetComments.getCTComments().getCommentList();
                    for (CTComment comment : lst.getCommentArray()) {
                        String strRef = comment.getRef();
                        CellAddress ref = new CellAddress(strRef);

                        // is this comment part of the current row?
                        if(ref.getRow() == rownum) {
                            sheetComments.removeComment(ref);
                            vml.removeCommentShape(ref.getRow(), ref.getColumn());
                        }
                    }
                }
                // FIXME: (performance optimization) this should be moved outside the for-loop so that hyperlinks only needs to be iterated over once.
                // also remove any hyperlinks associated with this row
                if (hyperlinks != null) {
                    for (XSSFHyperlink link : new ArrayList<>(hyperlinks)) {
                        CellReference ref = new CellReference(link.getCellRef());
                        if (ref.getRow() == rownum) {
                            hyperlinks.remove(link);
                        }
                    }
                }
            }
        }

    }

    private void shiftCommentsAndRows(XSSFVMLDrawing vml, int startRow, int endRow, final int n){
        // then do the actual moving and also adjust comments/rowHeight
        // we need to sort it in a way so the shifting does not mess up the structures,
        // i.e. when shifting down, start from down and go up, when shifting up, vice-versa
        SortedMap<XSSFComment, Integer> commentsToShift = new TreeMap<>(new Comparator<XSSFComment>() {
            @Override
            public int compare(XSSFComment o1, XSSFComment o2) {
                int row1 = o1.getRow();
                int row2 = o2.getRow();

                if (row1 == row2) {
                    // ordering is not important when row is equal, but don't return zero to still
                    // get multiple comments per row into the map
                    return o1.hashCode() - o2.hashCode();
                }

                // when shifting down, sort higher row-values first
                if (n > 0) {
                    return row1 < row2 ? 1 : -1;
                } else {
                    // sort lower-row values first when shifting up
                    return row1 > row2 ? 1 : -1;
                }
            }
        });


        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            XSSFRow row = (XSSFRow)it.next();
            int rownum = row.getRowNum();

            if(sheetComments != null){
                // calculate the new rownum
                int newrownum = shiftedRowNum(startRow, endRow, n, rownum);

                // is there a change necessary for the current row?
                if(newrownum != rownum) {
                    CTCommentList lst = sheetComments.getCTComments().getCommentList();
                    for (CTComment comment : lst.getCommentArray()) {
                        String oldRef = comment.getRef();
                        CellReference ref = new CellReference(oldRef);

                        // is this comment part of the current row?
                        if(ref.getRow() == rownum) {
                            XSSFComment xssfComment = new XSSFComment(sheetComments, comment,
                                    vml == null ? null : vml.findCommentShape(rownum, ref.getCol()));

                            // we should not perform the shifting right here as we would then find
                            // already shifted comments and would shift them again...
                            commentsToShift.put(xssfComment, newrownum);
                        }
                    }
                }
            }

            if(rownum < startRow || rownum > endRow) {
                continue;
            }
            row.shift(n);
        }
        // adjust all the affected comment-structures now
        // the Map is sorted and thus provides them in the order that we need here,
        // i.e. from down to up if shifting down, vice-versa otherwise
        for(Map.Entry<XSSFComment, Integer> entry : commentsToShift.entrySet()) {
            entry.getKey().setRow(entry.getValue());
        }

        rebuildRows();
    }

    private int shiftedRowNum(int startRow, int endRow, int n, int rownum) {
        // no change if before any affected row
        if(rownum < startRow && (n > 0 || (startRow - rownum) > n)) {
            return rownum;
        }

        // no change if after any affected row
        if(rownum > endRow && (n < 0 || (rownum - endRow) > n)) {
            return rownum;
        }

        // row before and things are moved up
        if(rownum < startRow) {
            // row is moved down by the shifting
            return rownum + (endRow - startRow);
        }

        // row is after and things are moved down
        if(rownum > endRow) {
            // row is moved up by the shifting
            return rownum - (endRow - startRow);
        }

        // row is part of the shifted block
        return rownum + n;
    }

    private void shiftCommentsForColumns(XSSFVMLDrawing vml, int startColumnIndex, int endColumnIndex, final int n){
        // then do the actual moving and also adjust comments/rowHeight
        // we need to sort it in a way so the shifting does not mess up the structures, 
        // i.e. when shifting down, start from down and go up, when shifting up, vice-versa
        SortedMap<XSSFComment, Integer> commentsToShift = new TreeMap<>(new Comparator<XSSFComment>() {
            @Override
            public int compare(XSSFComment o1, XSSFComment o2) {
                int column1 = o1.getColumn();
                int column2 = o2.getColumn();

                if (column1 == column2) {
                    // ordering is not important when row is equal, but don't return zero to still 
                    // get multiple comments per row into the map
                    return o1.hashCode() - o2.hashCode();
                }

                // when shifting down, sort higher row-values first
                if (n > 0) {
                    return column1 < column2 ? 1 : -1;
                } else {
                    // sort lower-row values first when shifting up
                    return column1 > column2 ? 1 : -1;
                }
            }
        });


        if(sheetComments != null){
            CTCommentList lst = sheetComments.getCTComments().getCommentList();
            for (CTComment comment : lst.getCommentArray()) {
                String oldRef = comment.getRef();
                CellReference ref = new CellReference(oldRef);

                int columnIndex =ref.getCol();
                int newColumnIndex = shiftedRowNum(startColumnIndex, endColumnIndex, n, columnIndex);
                if(newColumnIndex != columnIndex){
                    XSSFComment xssfComment = new XSSFComment(sheetComments, comment,
                            vml == null ? null : vml.findCommentShape(ref.getRow(), columnIndex));
                    commentsToShift.put(xssfComment, newColumnIndex);
                }
            }
        }
        // adjust all the affected comment-structures now
        // the Map is sorted and thus provides them in the order that we need here, 
        // i.e. from down to up if shifting down, vice-versa otherwise
        for(Map.Entry<XSSFComment, Integer> entry : commentsToShift.entrySet()) {
            entry.getKey().setColumn(entry.getValue());
        }

        rebuildRows();
    }

    /**
     * Location of the top left visible cell Location of the top left visible cell in the bottom right
     * pane (when in Left-to-Right mode).
     *
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    @Override
    public void showInPane(int toprow, int leftcol) {
        final CellReference cellReference = new CellReference(toprow, leftcol);
        final String cellRef = cellReference.formatAsString();
        final CTPane pane = getPane(true);
        assert(pane != null);
        pane.setTopLeftCell(cellRef);
    }

    @Override
    public void ungroupColumn(int fromColumn, int toColumn) {
        CTCols cols = worksheet.getColsArray(0);
        for (int index = fromColumn; index <= toColumn; index++) {
            CTCol col = columnHelper.getColumn(index, false);
            if (col != null) {
                short outlineLevel = col.getOutlineLevel();
                col.setOutlineLevel((short) (outlineLevel - 1));
                index = (int) col.getMax();

                if (col.getOutlineLevel() <= 0) {
                    int colIndex = columnHelper.getIndexOfColumn(cols, col);
                    worksheet.getColsArray(0).removeCol(colIndex);
                }
            }
        }
        worksheet.setColsArray(0, cols);
        setSheetFormatPrOutlineLevelCol();
    }

    /**
     * Ungroup a range of rows that were previously groupped
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    @Override
    public void ungroupRow(int fromRow, int toRow) {
        for (int i = fromRow; i <= toRow; i++) {
            XSSFRow xrow = getRow(i);
            if (xrow != null) {
                CTRow ctRow = xrow.getCTRow();
                int outlineLevel = ctRow.getOutlineLevel();
                ctRow.setOutlineLevel((short) (outlineLevel - 1));
                //remove a row only if the row has no cell and if the outline level is 0
                if (outlineLevel == 1 && xrow.getFirstCellNum() == -1) {
                    removeRow(xrow);
                }
            }
        }
        setSheetFormatPrOutlineLevelRow();
    }

    private void setSheetFormatPrOutlineLevelRow(){
        short maxLevelRow=getMaxOutlineLevelRows();
        getSheetTypeSheetFormatPr().setOutlineLevelRow(maxLevelRow);
    }

    private void setSheetFormatPrOutlineLevelCol(){
        short maxLevelCol=getMaxOutlineLevelCols();
        getSheetTypeSheetFormatPr().setOutlineLevelCol(maxLevelCol);
    }

    protected CTSheetViews getSheetTypeSheetViews(final boolean create) {
        final CTSheetViews views = (worksheet.isSetSheetViews() || !create)
                ? worksheet.getSheetViews() : worksheet.addNewSheetViews();
        assert(views != null || !create);
        if (views == null) {
            return null;
        }
        if (views.sizeOfSheetViewArray() == 0 && create) {
            views.addNewSheetView();
        }
        return views;
    }

    /**
     * Returns a flag indicating whether this sheet is selected.
     * <p>
     * When only 1 sheet is selected and active, this value should be in synch with the activeTab value.
     * In case of a conflict, the Start Part setting wins and sets the active sheet tab.
     * </p>
     * Note: multiple sheets can be selected, but only one sheet can be active at one time.
     *
     * @return <code>true</code> if this sheet is selected
     */
    @Override
    public boolean isSelected() {
        final CTSheetView dsv = getDefaultSheetView(false);
        return (dsv != null) ? dsv.getTabSelected() : false;
    }

    /**
     * Sets a flag indicating whether this sheet is selected.
     *
     * <p>
     * When only 1 sheet is selected and active, this value should be in synch with the activeTab value.
     * In case of a conflict, the Start Part setting wins and sets the active sheet tab.
     * </p>
     * Note: multiple sheets can be selected, but only one sheet can be active at one time.
     *
     * @param value <code>true</code> if this sheet is selected
     */
    @Override
    public void setSelected(boolean value) {
        final CTSheetViews views = getSheetTypeSheetViews(true);
        assert(views != null);
        for (CTSheetView view : views.getSheetViewArray()) {
            view.setTabSelected(value);
        }
    }

    /**
     * Register a hyperlink in the collection of hyperlinks on this sheet
     *
     * @param hyperlink the link to add
     */
    @Internal
    public void addHyperlink(XSSFHyperlink hyperlink) {
        hyperlinks.add(hyperlink);
    }

    /**
     * Removes a hyperlink in the collection of hyperlinks on this sheet
     *
     * @param row row index
     * @param column column index
     */
    @Internal
    public void removeHyperlink(int row, int column) {
        // CTHyperlinks is regenerated from scratch when writing out the spreadsheet
        // so don't worry about maintaining hyperlinks and CTHyperlinks in parallel.
        // only maintain hyperlinks
        String ref = new CellReference(row, column).formatAsString();
        for (Iterator<XSSFHyperlink> it = hyperlinks.iterator(); it.hasNext();) {
            XSSFHyperlink hyperlink = it.next();
            if (hyperlink.getCellRef().equals(ref)) {
                it.remove();
                return;
            }
        }
    }

    /**
     * Return location of the active cell, e.g. <code>A1</code>.
     *
     * @return the location of the active cell.
     */
    @Override
    public CellAddress getActiveCell() {
        final CTSelection sts = getSheetTypeSelection(false);
        final String address = (sts != null) ? sts.getActiveCell() : null;
        return (address != null) ? new CellAddress(address) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveCell(CellAddress address) {
        final CTSelection ctsel = getSheetTypeSelection(true);
        assert(ctsel != null);
        String ref = address.formatAsString();
        ctsel.setActiveCell(ref);
        ctsel.setSqref(Collections.singletonList(ref));
    }

    /**
     * Does this sheet have any comments on it? We need to know,
     *  so we can decide about writing it to disk or not
     */
    public boolean hasComments() {
        return sheetComments != null && sheetComments.getNumberOfComments() > 0;
    }

    protected int getNumberOfComments() {
        return sheetComments == null ? 0 : sheetComments.getNumberOfComments();
    }

    private CTSelection getSheetTypeSelection(final boolean create) {
        final CTSheetView dsv = getDefaultSheetView(create);
        assert(dsv != null || !create);
        if (dsv == null) {
            return null;
        }
        final int sz = dsv.sizeOfSelectionArray();
        if (sz == 0) {
            return create ? dsv.addNewSelection() : null;
        }
        return dsv.getSelectionArray(sz - 1);
    }

    /**
     * Return the default sheet view. This is the last one if the sheet's views, according to sec. 3.3.1.83
     * of the OOXML spec: "A single sheet view definition. When more than 1 sheet view is defined in the file,
     * it means that when opening the workbook, each sheet view corresponds to a separate window within the
     * spreadsheet application, where each window is showing the particular sheet. containing the same
     * workbookViewId value, the last sheetView definition is loaded, and the others are discarded.
     * When multiple windows are viewing the same sheet, multiple sheetView elements (with corresponding
     * workbookView entries) are saved."
     */
    private CTSheetView getDefaultSheetView(final boolean create) {
        final CTSheetViews views = getSheetTypeSheetViews(create);
        assert(views != null || !create);
        if (views == null) {
            return null;
        }
        final int sz = views.sizeOfSheetViewArray();
        assert(sz > 0 || !create);
        return (sz == 0) ? null : views.getSheetViewArray(sz - 1);
    }

    /**
     * Returns the sheet's comments object if there is one,
     *  or null if not
     *
     * @param create create a new comments table if it does not exist
     */
    protected CommentsTable getCommentsTable(boolean create) {
        if(sheetComments == null && create){
            // Try to create a comments table with the same number as
            //  the sheet has (i.e. sheet 1 -> comments 1)
            try {
                sheetComments = (CommentsTable)createRelationship(
                        XSSFRelation.SHEET_COMMENTS, XSSFFactory.getInstance(), (int)sheet.getSheetId());
            } catch(PartAlreadyExistsException e) {
                // Technically a sheet doesn't need the same number as
                //  it's comments, and clearly someone has already pinched
                //  our number! Go for the next available one instead
                sheetComments = (CommentsTable)createRelationship(
                        XSSFRelation.SHEET_COMMENTS, XSSFFactory.getInstance(), -1);
            }
        }
        return sheetComments;
    }

    private CTPageSetUpPr getSheetTypePageSetUpPr() {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        return sheetPr.isSetPageSetUpPr() ? sheetPr.getPageSetUpPr() : sheetPr.addNewPageSetUpPr();
    }

    private static boolean shouldRemoveRow(int startRow, int endRow, int n, int rownum) {
        // is this row in the target-window where the moved rows will land?
        if (rownum >= (startRow + n) && rownum <= (endRow + n)) {
            // only remove it if the current row is not part of the data that is copied
            if (n > 0 && rownum > endRow) {
                return true;
            }
            else if (n < 0 && rownum < startRow) {
                return true;
            }
        }
        return false;
    }

    private CTPane getPane(final boolean create) {
        final CTSheetView dsv = getDefaultSheetView(create);
        assert(dsv != null || !create);
        if (dsv == null) {
            return null;
        }
        return (dsv.isSetPane() || !create) ? dsv.getPane() : dsv.addNewPane();
    }

    /**
     * Return a master shared formula by index
     *
     * @param sid shared group index
     * @return a CTCellFormula bean holding shared formula or <code>null</code> if not found
     */
    @Internal
    public CTCellFormula getSharedFormula(int sid){
        return sharedFormulas.get(sid);
    }

    void onReadCell(XSSFCell cell){
        //collect cells holding shared formulas
        CTCell ct = cell.getCTCell();
        CTCellFormula f = ct.getF();
        if (f != null && f.getT() == STCellFormulaType.SHARED && f.isSetRef() && f.getStringValue() != null) {
            // save a detached  copy to avoid XmlValueDisconnectedException,
            // this may happen when the master cell of a shared formula is changed
            CTCellFormula sf = (CTCellFormula)f.copy();
            CellRangeAddress sfRef = CellRangeAddress.valueOf(sf.getRef());
            CellReference cellRef = new CellReference(cell);
            // If the shared formula range precedes the master cell then the preceding  part is discarded, e.g.
            // if the cell is E60 and the shared formula range is C60:M85 then the effective range is E60:M85
            // see more details in https://issues.apache.org/bugzilla/show_bug.cgi?id=51710
            if(cellRef.getCol() > sfRef.getFirstColumn() || cellRef.getRow() > sfRef.getFirstRow()){
                String effectiveRef = new CellRangeAddress(
                        Math.max(cellRef.getRow(), sfRef.getFirstRow()), sfRef.getLastRow(),
                        Math.max(cellRef.getCol(), sfRef.getFirstColumn()), sfRef.getLastColumn()).formatAsString();
                sf.setRef(effectiveRef);
            }

            sharedFormulas.put((int)f.getSi(), sf);
        }
        if (f != null && f.getT() == STCellFormulaType.ARRAY && f.getRef() != null) {
            arrayFormulas.add(CellRangeAddress.valueOf(f.getRef()));
        }
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        write(out);
        out.close();
    }

    protected void write(OutputStream out) throws IOException {
        boolean setToNull = false;
        if(worksheet.sizeOfColsArray() == 1) {
            CTCols col = worksheet.getColsArray(0);
            if(col.sizeOfColArray() == 0) {
                setToNull = true;
                // this is necessary so that we do not write an empty <cols/> item into the sheet-xml in the xlsx-file
                // Excel complains about a corrupted file if this shows up there!
                worksheet.setColsArray(null);
            } else {
                setColWidthAttribute(col);
            }
        }

        // Now re-generate our CTHyperlinks, if needed
        if(hyperlinks.size() > 0) {
            if(worksheet.getHyperlinks() == null) {
                worksheet.addNewHyperlinks();
            }
            CTHyperlink[] ctHls = new CTHyperlink[hyperlinks.size()];
            for(int i=0; i<ctHls.length; i++) {
                // If our sheet has hyperlinks, have them add
                //  any relationships that they might need
                XSSFHyperlink hyperlink = hyperlinks.get(i);
                hyperlink.generateRelationIfNeeded(getPackagePart());
                // Now grab their underling object
                ctHls[i] = hyperlink.getCTHyperlink();
            }
            worksheet.getHyperlinks().setHyperlinkArray(ctHls);
        }
        else {
            if (worksheet.getHyperlinks() != null) {
                final int count = worksheet.getHyperlinks().sizeOfHyperlinkArray();
                for (int i=count-1; i>=0; i--) {
                    worksheet.getHyperlinks().removeHyperlink(i);
                }
                // For some reason, we have to remove the hyperlinks one by one from the CTHyperlinks array
                // before unsetting the hyperlink array.
                // Resetting the hyperlink array seems to break some XML nodes.
                //worksheet.getHyperlinks().setHyperlinkArray(new CTHyperlink[0]);
                worksheet.unsetHyperlinks();
            } /*else {
                // nothing to do
            }*/
        }

        int minCell = Integer.MAX_VALUE, maxCell = Integer.MIN_VALUE;
        for(Map.Entry<Integer, XSSFRow> entry : _rows.entrySet()) {
            XSSFRow row = entry.getValue();

            // first perform the normal write actions for the row
            row.onDocumentWrite();

            // then calculate min/max cell-numbers for the worksheet-dimension
            if(row.getFirstCellNum() != -1) {
                minCell = Math.min(minCell, row.getFirstCellNum());
            }
            if(row.getLastCellNum() != -1) {
                maxCell = Math.max(maxCell, row.getLastCellNum()-1);
            }
        }

        // finally, if we had at least one cell we can populate the optional dimension-field
        if(minCell != Integer.MAX_VALUE) {
            String ref = new CellRangeAddress(getFirstRowNum(), getLastRowNum(), minCell, maxCell).formatAsString();
            if(worksheet.isSetDimension()) {
                worksheet.getDimension().setRef(ref);
            } else {
                worksheet.addNewDimension().setRef(ref);
            }
        }

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));

        worksheet.save(out, xmlOptions);

        // Bug 52233: Ensure that we have a col-array even if write() removed it
        if(setToNull) {
            worksheet.addNewCols();
        }
    }

    /**
     * @return true when Autofilters are locked and the sheet is protected.
     */
    public boolean isAutoFilterLocked() {
        return isSheetLocked() && safeGetProtectionField().getAutoFilter();
    }

    /**
     * @return true when Deleting columns is locked and the sheet is protected.
     */
    public boolean isDeleteColumnsLocked() {
        return isSheetLocked() && safeGetProtectionField().getDeleteColumns();
    }

    /**
     * @return true when Deleting rows is locked and the sheet is protected.
     */
    public boolean isDeleteRowsLocked() {
        return isSheetLocked() && safeGetProtectionField().getDeleteRows();
    }

    /**
     * @return true when Formatting cells is locked and the sheet is protected.
     */
    public boolean isFormatCellsLocked() {
        return isSheetLocked() && safeGetProtectionField().getFormatCells();
    }

    /**
     * @return true when Formatting columns is locked and the sheet is protected.
     */
    public boolean isFormatColumnsLocked() {
        return isSheetLocked() && safeGetProtectionField().getFormatColumns();
    }

    /**
     * @return true when Formatting rows is locked and the sheet is protected.
     */
    public boolean isFormatRowsLocked() {
        return isSheetLocked() && safeGetProtectionField().getFormatRows();
    }

    /**
     * @return true when Inserting columns is locked and the sheet is protected.
     */
    public boolean isInsertColumnsLocked() {
        return isSheetLocked() && safeGetProtectionField().getInsertColumns();
    }

    /**
     * @return true when Inserting hyperlinks is locked and the sheet is protected.
     */
    public boolean isInsertHyperlinksLocked() {
        return isSheetLocked() && safeGetProtectionField().getInsertHyperlinks();
    }

    /**
     * @return true when Inserting rows is locked and the sheet is protected.
     */
    public boolean isInsertRowsLocked() {
        return isSheetLocked() && safeGetProtectionField().getInsertRows();
    }

    /**
     * @return true when Pivot tables are locked and the sheet is protected.
     */
    public boolean isPivotTablesLocked() {
        return isSheetLocked() && safeGetProtectionField().getPivotTables();
    }

    /**
     * @return true when Sorting is locked and the sheet is protected.
     */
    public boolean isSortLocked() {
        return isSheetLocked() && safeGetProtectionField().getSort();
    }

    /**
     * @return true when Objects are locked and the sheet is protected.
     */
    public boolean isObjectsLocked() {
        return isSheetLocked() && safeGetProtectionField().getObjects();
    }

    /**
     * @return true when Scenarios are locked and the sheet is protected.
     */
    public boolean isScenariosLocked() {
        return isSheetLocked() && safeGetProtectionField().getScenarios();
    }

    /**
     * @return true when Selection of locked cells is locked and the sheet is protected.
     */
    public boolean isSelectLockedCellsLocked() {
        return isSheetLocked() && safeGetProtectionField().getSelectLockedCells();
    }

    /**
     * @return true when Selection of unlocked cells is locked and the sheet is protected.
     */
    public boolean isSelectUnlockedCellsLocked() {
        return isSheetLocked() && safeGetProtectionField().getSelectUnlockedCells();
    }

    /**
     * @return true when Sheet is Protected.
     */
    public boolean isSheetLocked() {
        return worksheet.isSetSheetProtection() && safeGetProtectionField().getSheet();
    }

    /**
     * Enable sheet protection
     */
    public void enableLocking() {
        safeGetProtectionField().setSheet(true);
    }

    /**
     * Disable sheet protection
     */
    public void disableLocking() {
        safeGetProtectionField().setSheet(false);
    }

    /**
     * Enable or disable Autofilters locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockAutoFilter(boolean enabled) {
        safeGetProtectionField().setAutoFilter(enabled);
    }

    /**
     * Enable or disable Deleting columns locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockDeleteColumns(boolean enabled) {
        safeGetProtectionField().setDeleteColumns(enabled);
    }

    /**
     * Enable or disable Deleting rows locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockDeleteRows(boolean enabled) {
        safeGetProtectionField().setDeleteRows(enabled);
    }

    /**
     * Enable or disable Formatting cells locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockFormatCells(boolean enabled) {
        safeGetProtectionField().setFormatCells(enabled);
    }

    /**
     * Enable or disable Formatting columns locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockFormatColumns(boolean enabled) {
        safeGetProtectionField().setFormatColumns(enabled);
    }

    /**
     * Enable or disable Formatting rows locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockFormatRows(boolean enabled) {
        safeGetProtectionField().setFormatRows(enabled);
    }

    /**
     * Enable or disable Inserting columns locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockInsertColumns(boolean enabled) {
        safeGetProtectionField().setInsertColumns(enabled);
    }

    /**
     * Enable or disable Inserting hyperlinks locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockInsertHyperlinks(boolean enabled) {
        safeGetProtectionField().setInsertHyperlinks(enabled);
    }

    /**
     * Enable or disable Inserting rows locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockInsertRows(boolean enabled) {
        safeGetProtectionField().setInsertRows(enabled);
    }

    /**
     * Enable or disable Pivot Tables locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockPivotTables(boolean enabled) {
        safeGetProtectionField().setPivotTables(enabled);
    }

    /**
     * Enable or disable Sort locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockSort(boolean enabled) {
        safeGetProtectionField().setSort(enabled);
    }

    /**
     * Enable or disable Objects locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockObjects(boolean enabled) {
        safeGetProtectionField().setObjects(enabled);
    }

    /**
     * Enable or disable Scenarios locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockScenarios(boolean enabled) {
        safeGetProtectionField().setScenarios(enabled);
    }

    /**
     * Enable or disable Selection of locked cells locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockSelectLockedCells(boolean enabled) {
        safeGetProtectionField().setSelectLockedCells(enabled);
    }

    /**
     * Enable or disable Selection of unlocked cells locking.
     * This does not modify sheet protection status.
     * To enforce this un-/locking, call {@link #disableLocking()} or {@link #enableLocking()}
     */
    public void lockSelectUnlockedCells(boolean enabled) {
        safeGetProtectionField().setSelectUnlockedCells(enabled);
    }

    private CTSheetProtection safeGetProtectionField() {
        if (!isSheetProtectionEnabled()) {
            return worksheet.addNewSheetProtection();
        }
        return worksheet.getSheetProtection();
    }

    /* package */ boolean isSheetProtectionEnabled() {
        return (worksheet.isSetSheetProtection());
    }

    /* package */ boolean isCellInArrayFormulaContext(XSSFCell cell) {
        for (CellRangeAddress range : arrayFormulas) {
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return true;
            }
        }
        return false;
    }

    /* package */ XSSFCell getFirstCellInArrayFormula(XSSFCell cell) {
        for (CellRangeAddress range : arrayFormulas) {
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return getRow(range.getFirstRow()).getCell(range.getFirstColumn());
            }
        }
        return null;
    }

    /**
     * Also creates cells if they don't exist
     */
    private CellRange<XSSFCell> getCellRange(CellRangeAddress range) {
        int firstRow = range.getFirstRow();
        int firstColumn = range.getFirstColumn();
        int lastRow = range.getLastRow();
        int lastColumn = range.getLastColumn();
        int height = lastRow - firstRow + 1;
        int width = lastColumn - firstColumn + 1;
        List<XSSFCell> temp = new ArrayList<>(height * width);
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                XSSFRow row = getRow(rowIn);
                if (row == null) {
                    row = createRow(rowIn);
                }
                XSSFCell cell = row.getCell(colIn);
                if (cell == null) {
                    cell = row.createCell(colIn);
                }
                temp.add(cell);
            }
        }
        return SSCellRange.create(firstRow, firstColumn, height, width, temp, XSSFCell.class);
    }

    @Override
    public CellRange<XSSFCell> setArrayFormula(String formula, CellRangeAddress range) {
        CellRange<XSSFCell> cr = getCellRange(range);

        XSSFCell mainArrayFormulaCell = cr.getTopLeftCell();
        mainArrayFormulaCell.setCellArrayFormula(formula, range);
        arrayFormulas.add(range);
        return cr;
    }

    @Override
    public CellRange<XSSFCell> removeArrayFormula(Cell cell) {
        if (cell.getSheet() != this) {
            throw new IllegalArgumentException("Specified cell does not belong to this sheet.");
        }
        for (CellRangeAddress range : arrayFormulas) {
            if (range.isInRange(cell)) {
                arrayFormulas.remove(range);
                CellRange<XSSFCell> cr = getCellRange(range);
                for (XSSFCell c : cr) {
                    c.setCellType(CellType.BLANK);
                }
                return cr;
            }
        }
        String ref = ((XSSFCell)cell).getCTCell().getR();
        throw new IllegalArgumentException("Cell " + ref + " is not part of an array formula.");
    }


    @Override
    public DataValidationHelper getDataValidationHelper() {
        return dataValidationHelper;
    }

    @Override
    public List<XSSFDataValidation> getDataValidations() {
        List<XSSFDataValidation> xssfValidations = new ArrayList<>();
        CTDataValidations dataValidations = this.worksheet.getDataValidations();
        if( dataValidations!=null && dataValidations.getCount() > 0 ) {
            for (CTDataValidation ctDataValidation : dataValidations.getDataValidationArray()) {
                CellRangeAddressList addressList = new CellRangeAddressList();

                @SuppressWarnings("unchecked")
                List<String> sqref = ctDataValidation.getSqref();
                for (String stRef : sqref) {
                    String[] regions = stRef.split(" ");
                    for (String region : regions) {
                        String[] parts = region.split(":");
                        CellReference begin = new CellReference(parts[0]);
                        CellReference end = parts.length > 1 ? new CellReference(parts[1]) : begin;
                        CellRangeAddress cellRangeAddress = new CellRangeAddress(begin.getRow(), end.getRow(), begin.getCol(), end.getCol());
                        addressList.addCellRangeAddress(cellRangeAddress);
                    }
                }
                XSSFDataValidation xssfDataValidation = new XSSFDataValidation(addressList, ctDataValidation);
                xssfValidations.add(xssfDataValidation);
            }
        }
        return xssfValidations;
    }

    @Override
    public void addValidationData(DataValidation dataValidation) {
        XSSFDataValidation xssfDataValidation = (XSSFDataValidation)dataValidation;
        CTDataValidations dataValidations = worksheet.getDataValidations();
        if( dataValidations==null ) {
            dataValidations = worksheet.addNewDataValidations();
        }
        int currentCount = dataValidations.sizeOfDataValidationArray();
        CTDataValidation newval = dataValidations.addNewDataValidation();
        newval.set(xssfDataValidation.getCtDdataValidation());
        dataValidations.setCount(currentCount + 1);

    }

    @Override
    public XSSFAutoFilter setAutoFilter(CellRangeAddress range) {
        CTAutoFilter af = worksheet.getAutoFilter();
        if(af == null) {
            af = worksheet.addNewAutoFilter();
        }

        CellRangeAddress norm = new CellRangeAddress(range.getFirstRow(), range.getLastRow(),
                range.getFirstColumn(), range.getLastColumn());
        String ref = norm.formatAsString();
        af.setRef(ref);

        XSSFWorkbook wb = getWorkbook();
        int sheetIndex = getWorkbook().getSheetIndex(this);
        XSSFName name = wb.getBuiltInName(XSSFName.BUILTIN_FILTER_DB, sheetIndex);
        if (name == null) {
            name = wb.createBuiltInName(XSSFName.BUILTIN_FILTER_DB, sheetIndex);
        }

        name.getCTName().setHidden(true);
        CellReference r1 = new CellReference(getSheetName(), range.getFirstRow(), range.getFirstColumn(), true, true);
        CellReference r2 = new CellReference(null, range.getLastRow(), range.getLastColumn(), true, true);
        String fmla = r1.formatAsString() + ":" + r2.formatAsString();
        name.setRefersToFormula(fmla);

        return new XSSFAutoFilter(this);
    }

    /**
     * Creates a new Table, and associates it with this Sheet. The table does
     * not yet have an area defined and needs to be initialized by calling
     * {@link XSSFTable#setArea(AreaReference)}.
     *
     * @deprecated Use {@link #createTable(AreaReference))} instead
     */
    @Deprecated
    @Removal(version = "4.2.0")
    public XSSFTable createTable() {
        return createTable(null);
    }

    /**
     * Creates a new Table, and associates it with this Sheet.
     *
     * @param tableArea
     *            the area that the table should cover, should not be {@null}
     * @return the created table
     * @since 4.0.0
     */
    public XSSFTable createTable(AreaReference tableArea) {
        if (!worksheet.isSetTableParts()) {
            worksheet.addNewTableParts();
        }

        CTTableParts tblParts = worksheet.getTableParts();
        CTTablePart tbl = tblParts.addNewTablePart();

        // Table numbers need to be unique in the file, not just
        //  unique within the sheet. Find the next one
        int tableNumber = getPackagePart().getPackage().getPartsByContentType(XSSFRelation.TABLE.getContentType()).size() + 1;

        // the id could already be taken after insertion/deletion of different tables
        outerloop:
        while(true) {
            for (PackagePart packagePart : getPackagePart().getPackage().getPartsByContentType(XSSFRelation.TABLE.getContentType())) {
                String fileName = XSSFRelation.TABLE.getFileName(tableNumber);
                if(fileName.equals(packagePart.getPartName().getName())) {
                    // duplicate found, increase the number and start iterating again
                    tableNumber++;
                    continue outerloop;
                }
            }

            break;
        }

        RelationPart rp = createRelationship(XSSFRelation.TABLE, XSSFFactory.getInstance(), tableNumber, false);
        XSSFTable table = rp.getDocumentPart();
        tbl.setId(rp.getRelationship().getId());
        table.getCTTable().setId(tableNumber);

        tables.put(tbl.getId(), table);

        if(tableArea != null) {
            table.setArea(tableArea);
        }

        return table;
    }

    /**
     * Returns any tables associated with this Sheet
     */
    public List<XSSFTable> getTables() {
        return new ArrayList<>(tables.values());
    }

    /**
     * Remove table references and relations
     * @param t table to remove
     */
    public void removeTable(XSSFTable t) {
        long id = t.getCTTable().getId();
        Map.Entry<String, XSSFTable> toDelete = null;

        for (Map.Entry<String, XSSFTable> entry : tables.entrySet()) {
            if (entry.getValue().getCTTable().getId() == id) toDelete = entry;
        }
        if (toDelete != null) {
            removeRelation(getRelationById(toDelete.getKey()), true);
            tables.remove(toDelete.getKey());
            toDelete.getValue().onTableDelete();
        }
    }

    @Override
    public XSSFSheetConditionalFormatting getSheetConditionalFormatting(){
        return new XSSFSheetConditionalFormatting(this);
    }

    /**
     * Get background color of the sheet tab.
     * Returns <tt>null</tt> if no sheet tab color is set.
     *
     * @return the background color of the sheet tab
     */
    public XSSFColor getTabColor() {
        CTSheetPr pr = worksheet.getSheetPr();
        if(pr == null) {
            pr = worksheet.addNewSheetPr();
        }
        if (!pr.isSetTabColor()) {
            return null;
        }
        return XSSFColor.from(pr.getTabColor(), getWorkbook().getStylesSource().getIndexedColors());
    }

    /**
     * Set background color of the sheet tab
     *
     * @param color the color to set
     */
    public void setTabColor(XSSFColor color) {
        CTSheetPr pr = worksheet.getSheetPr();
        if(pr == null) {
            pr = worksheet.addNewSheetPr();
        }
        pr.setTabColor(color.getCTColor());
    }

    @Override
    public CellRangeAddress getRepeatingRows() {
        return getRepeatingRowsOrColums(true);
    }


    @Override
    public CellRangeAddress getRepeatingColumns() {
        return getRepeatingRowsOrColums(false);
    }

    @Override
    public void setRepeatingRows(CellRangeAddress rowRangeRef) {
        CellRangeAddress columnRangeRef = getRepeatingColumns();
        setRepeatingRowsAndColumns(rowRangeRef, columnRangeRef);
    }


    @Override
    public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
        CellRangeAddress rowRangeRef = getRepeatingRows();
        setRepeatingRowsAndColumns(rowRangeRef, columnRangeRef);
    }


    private void setRepeatingRowsAndColumns(
            CellRangeAddress rowDef, CellRangeAddress colDef) {
        int col1 = -1;
        int col2 =  -1;
        int row1 = -1;
        int row2 =  -1;

        if (rowDef != null) {
            row1 = rowDef.getFirstRow();
            row2 = rowDef.getLastRow();
            if ((row1 == -1 && row2 != -1)
                    || row1 < -1 || row2 < -1 || row1 > row2) {
                throw new IllegalArgumentException("Invalid row range specification");
            }
        }
        if (colDef != null) {
            col1 = colDef.getFirstColumn();
            col2 = colDef.getLastColumn();
            if ((col1 == -1 && col2 != -1)
                    || col1 < -1 || col2 < -1 || col1 > col2) {
                throw new IllegalArgumentException(
                        "Invalid column range specification");
            }
        }

        int sheetIndex = getWorkbook().getSheetIndex(this);

        boolean removeAll = rowDef == null && colDef == null;

        XSSFName name = getWorkbook().getBuiltInName(
                XSSFName.BUILTIN_PRINT_TITLE, sheetIndex);
        if (removeAll) {
            if (name != null) {
                getWorkbook().removeName(name);
            }
            return;
        }
        if (name == null) {
            name = getWorkbook().createBuiltInName(
                    XSSFName.BUILTIN_PRINT_TITLE, sheetIndex);
        }

        String reference = getReferenceBuiltInRecord(
                name.getSheetName(), col1, col2, row1, row2);
        name.setRefersToFormula(reference);

        // If the print setup isn't currently defined, then add it
        //  in but without printer defaults
        // If it's already there, leave it as-is!
        if (worksheet.isSetPageSetup() && worksheet.isSetPageMargins()) {
            // Everything we need is already there
        } else {
            // Have initial ones put in place
            getPrintSetup().setValidSettings(false);
        }
    }

    private static String getReferenceBuiltInRecord(
            String sheetName, int startC, int endC, int startR, int endR) {
        // Excel example for built-in title:
        //   'second sheet'!$E:$F,'second sheet'!$2:$3

        CellReference colRef =
                new CellReference(sheetName, 0, startC, true, true);
        CellReference colRef2 =
                new CellReference(sheetName, 0, endC, true, true);
        CellReference rowRef =
                new CellReference(sheetName, startR, 0, true, true);
        CellReference rowRef2 =
                new CellReference(sheetName, endR, 0, true, true);

        String escapedName = SheetNameFormatter.format(sheetName);

        String c = "";
        String r = "";

        if (startC != -1 || endC != -1) {
            String col1 = colRef.getCellRefParts()[2];
            String col2 = colRef2.getCellRefParts()[2];
            c = escapedName + "!$" + col1 + ":$" + col2;
        }

        if (startR != -1 || endR != -1) {
            String row1 = rowRef.getCellRefParts()[1];
            String row2 = rowRef2.getCellRefParts()[1];
            if (!row1.equals("0") && !row2.equals("0")) {
                r = escapedName + "!$" + row1 + ":$" + row2;
            }
        }

        StringBuilder rng = new StringBuilder();
        rng.append(c);
        if(rng.length() > 0 && r.length() > 0) {
            rng.append(',');
        }
        rng.append(r);
        return rng.toString();
    }


    private CellRangeAddress getRepeatingRowsOrColums(boolean rows) {
        int sheetIndex = getWorkbook().getSheetIndex(this);
        XSSFName name = getWorkbook().getBuiltInName(
                XSSFName.BUILTIN_PRINT_TITLE, sheetIndex);
        if (name == null ) {
            return null;
        }
        String refStr = name.getRefersToFormula();
        if (refStr == null) {
            return null;
        }
        String[] parts = refStr.split(",");
        int maxRowIndex = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
        int maxColIndex = SpreadsheetVersion.EXCEL2007.getLastColumnIndex();
        for (String part : parts) {
            CellRangeAddress range = CellRangeAddress.valueOf(part);
            if ((range.getFirstColumn() == 0
                    && range.getLastColumn() == maxColIndex)
                    || (range.getFirstColumn() == -1
                    && range.getLastColumn() == -1)) {
                if (rows) {
                    return range;
                }
            } else if (range.getFirstRow() == 0
                    && range.getLastRow() == maxRowIndex
                    || (range.getFirstRow() == -1
                    && range.getLastRow() == -1)) {
                if (!rows) {
                    return range;
                }
            }
        }
        return null;
    }

    /**
     * Creates an empty XSSFPivotTable and sets up all its relationships
     * including: pivotCacheDefinition, pivotCacheRecords
     * @return returns a pivotTable
     */
    @Beta
    private XSSFPivotTable createPivotTable() {
        XSSFWorkbook wb = getWorkbook();
        List<XSSFPivotTable> pivotTables = wb.getPivotTables();
        int tableId = getWorkbook().getPivotTables().size()+1;
        //Create relationship between pivotTable and the worksheet
        XSSFPivotTable pivotTable = (XSSFPivotTable) createRelationship(XSSFRelation.PIVOT_TABLE,
                XSSFFactory.getInstance(), tableId);
        pivotTable.setParentSheet(this);
        pivotTables.add(pivotTable);
        XSSFWorkbook workbook = getWorkbook();

        //Create relationship between the pivot cache defintion and the workbook
        XSSFPivotCacheDefinition pivotCacheDefinition = (XSSFPivotCacheDefinition) workbook.
                createRelationship(XSSFRelation.PIVOT_CACHE_DEFINITION, XSSFFactory.getInstance(), tableId);
        String rId = workbook.getRelationId(pivotCacheDefinition);
        //Create relationship between pivotTable and pivotCacheDefinition without creating a new instance
        PackagePart pivotPackagePart = pivotTable.getPackagePart();
        pivotPackagePart.addRelationship(pivotCacheDefinition.getPackagePart().getPartName(),
                TargetMode.INTERNAL, XSSFRelation.PIVOT_CACHE_DEFINITION.getRelation());

        pivotTable.setPivotCacheDefinition(pivotCacheDefinition);

        //Create pivotCache and sets up it's relationship with the workbook
        pivotTable.setPivotCache(new XSSFPivotCache(workbook.addPivotCache(rId)));

        //Create relationship between pivotcacherecord and pivotcachedefinition
        XSSFPivotCacheRecords pivotCacheRecords = (XSSFPivotCacheRecords) pivotCacheDefinition.
                createRelationship(XSSFRelation.PIVOT_CACHE_RECORDS, XSSFFactory.getInstance(), tableId);

        //Set relationships id for pivotCacheDefinition to pivotCacheRecords
        pivotTable.getPivotCacheDefinition().getCTPivotCacheDefinition().setId(pivotCacheDefinition.getRelationId(pivotCacheRecords));

        wb.setPivotTables(pivotTables);

        return pivotTable;
    }

    /**
     * Create a pivot table using the AreaReference range on sourceSheet, at the given position.
     * If the source reference contains a sheet name, it must match the sourceSheet
     * @param source location of pivot data
     * @param position A reference to the top left cell where the pivot table will start
     * @param sourceSheet The sheet containing the source data, if the source reference doesn't contain a sheet name
     * @throws IllegalArgumentException if source references a sheet different than sourceSheet
     * @return The pivot table
     */
    @Beta
    public XSSFPivotTable createPivotTable(final AreaReference source, CellReference position, Sheet sourceSheet) {
        final String sourceSheetName = source.getFirstCell().getSheetName();
        if(sourceSheetName != null && !sourceSheetName.equalsIgnoreCase(sourceSheet.getSheetName())) {
            throw new IllegalArgumentException("The area is referenced in another sheet than the "
                    + "defined source sheet " + sourceSheet.getSheetName() + ".");
        }

        return createPivotTable(position, sourceSheet, new PivotTableReferenceConfigurator() {
            @Override
            public void configureReference(CTWorksheetSource wsSource) {
                final String[] firstCell = source.getFirstCell().getCellRefParts();
                final String firstRow = firstCell[1];
                final String firstCol = firstCell[2];
                final String[] lastCell = source.getLastCell().getCellRefParts();
                final String lastRow = lastCell[1];
                final String lastCol = lastCell[2];
                final String ref = firstCol+firstRow+':'+lastCol+lastRow; //or just source.formatAsString()
                wsSource.setRef(ref);
            }
        });
    }

    /**
     * Create a pivot table using the AreaReference or named/table range on sourceSheet, at the given position.
     * If the source reference contains a sheet name, it must match the sourceSheet.
     * @param position A reference to the top left cell where the pivot table will start
     * @param sourceSheet The sheet containing the source data, if the source reference doesn't contain a sheet name
     * @param refConfig A reference to the pivot table configurator
     * @throws IllegalArgumentException if source references a sheet different than sourceSheet
     * @return The pivot table
     */
    private XSSFPivotTable createPivotTable(CellReference position, Sheet sourceSheet, PivotTableReferenceConfigurator refConfig) {

        XSSFPivotTable pivotTable = createPivotTable();
        //Creates default settings for the pivot table
        pivotTable.setDefaultPivotTableDefinition();

        //Set sources and references
        pivotTable.createSourceReferences(position, sourceSheet, refConfig);

        //Create cachefield/s and empty SharedItems - must be after creating references
        pivotTable.getPivotCacheDefinition().createCacheFields(sourceSheet);
        pivotTable.createDefaultDataColumns();

        return pivotTable;
    }

    /**
     * Create a pivot table using the AreaReference range, at the given position.
     * If the source reference contains a sheet name, that sheet is used, otherwise this sheet is assumed as the source sheet.
     * @param source location of pivot data
     * @param position A reference to the top left cell where the pivot table will start
     * @return The pivot table
     */
    @Beta
    public XSSFPivotTable createPivotTable(AreaReference source, CellReference position){
        final String sourceSheetName = source.getFirstCell().getSheetName();
        if(sourceSheetName != null && !sourceSheetName.equalsIgnoreCase(this.getSheetName())) {
            final XSSFSheet sourceSheet = getWorkbook().getSheet(sourceSheetName);
            return createPivotTable(source, position, sourceSheet);
        }
        return createPivotTable(source, position, this);
    }

    /**
     * Create a pivot table using the Name range reference on sourceSheet, at the given position.
     * If the source reference contains a sheet name, it must match the sourceSheet
     * @param source location of pivot data
     * @param position A reference to the top left cell where the pivot table will start
     * @param sourceSheet The sheet containing the source data, if the source reference doesn't contain a sheet name
     * @throws IllegalArgumentException if source references a sheet different than sourceSheet
     * @return The pivot table
     */
    @Beta
    public XSSFPivotTable createPivotTable(final Name source, CellReference position, Sheet sourceSheet) {
        if(source.getSheetName() != null && !source.getSheetName().equals(sourceSheet.getSheetName())) {
            throw new IllegalArgumentException("The named range references another sheet than the "
                    + "defined source sheet " + sourceSheet.getSheetName() + ".");
        }

        return createPivotTable(position, sourceSheet, new PivotTableReferenceConfigurator() {
            @Override
            public void configureReference(CTWorksheetSource wsSource) {
                wsSource.setName(source.getNameName());
            }
        });
    }

    /**
     * Create a pivot table using the Name range, at the given position.
     * If the source reference contains a sheet name, that sheet is used, otherwise this sheet is assumed as the source sheet.
     * @param source location of pivot data
     * @param position A reference to the top left cell where the pivot table will start
     * @return The pivot table
     */
    @Beta
    public XSSFPivotTable createPivotTable(Name source, CellReference position) {
        return createPivotTable(source, position, getWorkbook().getSheet(source.getSheetName()));
    }

    /**
     * Create a pivot table using the Table, at the given position.
     * Tables are required to have a sheet reference, so no additional logic around reference sheet is needed.
     * @param source location of pivot data
     * @param position A reference to the top left cell where the pivot table will start
     * @return The pivot table
     */
    @Beta
    public XSSFPivotTable createPivotTable(final Table source, CellReference position) {
        return createPivotTable(position, getWorkbook().getSheet(source.getSheetName()), new PivotTableReferenceConfigurator() {
            @Override
            public void configureReference(CTWorksheetSource wsSource) {
                wsSource.setName(source.getName());
            }
        });
    }

    /**
     * Returns all the pivot tables for this Sheet
     */
    @Beta
    public List<XSSFPivotTable> getPivotTables() {
        List<XSSFPivotTable> tables = new ArrayList<>();
        for (XSSFPivotTable table : getWorkbook().getPivotTables()) {
            if (table.getParent() == this) {
                tables.add(table);
            }
        }
        return tables;
    }

    @Override
    public int getColumnOutlineLevel(int columnIndex) {
        CTCol col = columnHelper.getColumn(columnIndex, false);
        if (col == null) {
            return 0;
        }
        return col.getOutlineLevel();
    }

    /**
     * Add ignored errors (usually to suppress them in the UI of a consuming
     * application).
     *
     * @param cell Cell.
     * @param ignoredErrorTypes Types of error to ignore there.
     */
    public void addIgnoredErrors(CellReference cell, IgnoredErrorType... ignoredErrorTypes) {
        addIgnoredErrors(cell.formatAsString(), ignoredErrorTypes);
    }

    /**
     * Ignore errors across a range of cells.
     *
     * @param region Range of cells.
     * @param ignoredErrorTypes Types of error to ignore there.
     */
    public void addIgnoredErrors(CellRangeAddress region, IgnoredErrorType... ignoredErrorTypes) {
        region.validate(SpreadsheetVersion.EXCEL2007);
        addIgnoredErrors(region.formatAsString(), ignoredErrorTypes);
    }

    /**
     * Returns the errors currently being ignored and the ranges
     * where they are ignored.
     *
     * @return Map of error type to the range(s) where they are ignored.
     */
    public Map<IgnoredErrorType, Set<CellRangeAddress>> getIgnoredErrors() {
        Map<IgnoredErrorType, Set<CellRangeAddress>> result = new LinkedHashMap<>();
        if (worksheet.isSetIgnoredErrors()) {
            for (CTIgnoredError err : worksheet.getIgnoredErrors().getIgnoredErrorList()) {
                for (IgnoredErrorType errType : XSSFIgnoredErrorHelper.getErrorTypes(err)) {
                    if (!result.containsKey(errType)) {
                        result.put(errType, new LinkedHashSet<>());
                    }
                    for (Object ref : err.getSqref()) {
                        result.get(errType).add(CellRangeAddress.valueOf(ref.toString()));
                    }
                }
            }
        }
        return result;
    }

    private void addIgnoredErrors(String ref, IgnoredErrorType... ignoredErrorTypes) {
        CTIgnoredErrors ctIgnoredErrors = worksheet.isSetIgnoredErrors() ? worksheet.getIgnoredErrors() : worksheet.addNewIgnoredErrors();
        CTIgnoredError ctIgnoredError = ctIgnoredErrors.addNewIgnoredError();
        XSSFIgnoredErrorHelper.addIgnoredErrors(ctIgnoredError, ref, ignoredErrorTypes);
    }

    /**
     * called when a sheet is being deleted/removed from a workbook, to clean up relations and other document pieces tied to the sheet
     */
    protected void onSheetDelete() {
        for (RelationPart part : getRelationParts()) {
            if (part.getDocumentPart() instanceof XSSFTable) {
                // call table delete
                removeTable(part.getDocumentPart());
                continue;
            }
            removeRelation(part.getDocumentPart(), true);
        }
    }

    /**
     *  when a cell with a 'master' shared formula is removed,  the next cell in the range becomes the master
     */
    protected void onDeleteFormula(XSSFCell cell){

        CTCellFormula f = cell.getCTCell().getF();
        if (f != null && f.getT() == STCellFormulaType.SHARED && f.isSetRef() && f.getStringValue() != null) {

            CellRangeAddress ref = CellRangeAddress.valueOf(f.getRef());
            if(ref.getNumberOfCells() > 1){
                DONE:
                for(int i = cell.getRowIndex(); i <= ref.getLastRow(); i++){
                    XSSFRow row = getRow(i);
                    if(row != null) for(int j = cell.getColumnIndex(); j <= ref.getLastColumn(); j++){
                        XSSFCell nextCell = row.getCell(j);
                        if(nextCell != null && nextCell != cell){
                            CTCellFormula nextF = nextCell.getCTCell().getF();
                            nextF.setStringValue(nextCell.getCellFormula());
                            CellRangeAddress nextRef = new CellRangeAddress(
                                    nextCell.getRowIndex(), ref.getLastRow(),
                                    nextCell.getColumnIndex(), ref.getLastColumn());
                            nextF.setRef(nextRef.formatAsString());

                            sharedFormulas.put((int)nextF.getSi(), nextF);
                            break DONE;
                        }
                    }
                }
            }

        }
    }

    /**
     * Determine the OleObject which links shapes with embedded resources
     *
     * @param shapeId the shape id
     * @return the CTOleObject of the shape
     */
    protected CTOleObject readOleObject(long shapeId) {
        if (!getCTWorksheet().isSetOleObjects()) {
            return null;
        }

        // we use a XmlCursor here to handle oleObject with-/out AlternateContent wrappers
        String xquery = "declare namespace p='"+XSSFRelation.NS_SPREADSHEETML+"' .//p:oleObject";
        XmlCursor cur = getCTWorksheet().getOleObjects().newCursor();
        try {
            cur.selectPath(xquery);
            CTOleObject coo = null;
            while (cur.toNextSelection()) {
                String sId = cur.getAttributeText(new QName(null, "shapeId"));
                if (sId == null || Long.parseLong(sId)  != shapeId) {
                    continue;
                }

                XmlObject xObj = cur.getObject();
                if (xObj instanceof CTOleObject) {
                    // the unusual case ...
                    coo = (CTOleObject)xObj;
                } else {
                    XMLStreamReader reader = cur.newXMLStreamReader();
                    try {
                        CTOleObjects coos = CTOleObjects.Factory.parse(reader);
                        if (coos.sizeOfOleObjectArray() == 0) {
                            continue;
                        }
                        coo = coos.getOleObjectArray(0);
                    } catch (XmlException e) {
                        logger.log(POILogger.INFO, "can't parse CTOleObjects", e);
                    } finally {
                        try {
                            reader.close();
                        } catch (XMLStreamException e) {
                            logger.log(POILogger.INFO, "can't close reader", e);
                        }
                    }
                }

                // there are choice and fallback OleObject ... we prefer the one having the objectPr element,
                // which is in the choice element
                if (cur.toChild(XSSFRelation.NS_SPREADSHEETML, "objectPr")) {
                    break;
                }
            }
            return (coo == null) ? null : coo;
        } finally {
            cur.dispose();
        }
    }

    public XSSFHeaderFooterProperties getHeaderFooterProperties() {
        return new XSSFHeaderFooterProperties(getSheetTypeHeaderFooter());
    }
}
