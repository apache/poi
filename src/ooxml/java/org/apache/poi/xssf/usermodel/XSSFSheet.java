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
import java.util.*;
import javax.xml.namespace.QName;

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;


/**
 * High level representation of a SpreadsheetML worksheet.
 *
 * <p>
 * Sheets are the central structures within a workbook, and are where a user does most of his spreadsheet work.
 * The most common type of sheet is the worksheet, which is represented as a grid of cells. Worksheet cells can
 * contain text, numbers, dates, and formulas. Cells can also be formatted.
 * </p>
 */
public class XSSFSheet extends POIXMLDocumentPart implements Sheet {
    private static POILogger logger = POILogFactory.getLogger(XSSFSheet.class);

    /**
     * Column width measured as the number of characters of the maximum digit width of the
     * numbers 0, 1, 2, ..., 9 as rendered in the normal style's font. There are 4 pixels of margin
     * padding (two on each side), plus 1 pixel padding for the gridlines.
     *
     * This value is the same for default font in Office 2007 (Calibry) and Office 2003 and earlier (Arial)
     */
    private static float DEFAULT_COLUMN_WIDTH = 9.140625f;

    protected CTSheet sheet;
    protected CTWorksheet worksheet;
    private TreeMap<Integer, Row> rows;
    private List<XSSFHyperlink> hyperlinks;
    private ColumnHelper columnHelper;
    private CommentsTable sheetComments;

    /**
     * Creates new XSSFSheet   - called by XSSFWorkbook to create a sheet from scratch.
     *
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createSheet()
     */
    protected XSSFSheet() {
        super();
        onDocumentCreate();
    }

    /**
     * Creates an XSSFSheet representing the given package part and relationship.
     * Should only be called by XSSFWorkbook when reading in an exisiting file.
     *
     * @param part - The package part that holds xml data represenring this sheet.
     * @param rel - the relationship of the given package part in the underlying OPC package
     */
    protected XSSFSheet(PackagePart part, PackageRelationship rel) {
        super(part, rel);
    }

    /**
     * Returns the parent XSSFWorkbook
     *
     * @return the parent XSSFWorkbook
     */
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
            worksheet = WorksheetDocument.Factory.parse(is).getWorksheet();
        } catch (XmlException e){
            throw new POIXMLException(e);
        }

        initRows(worksheet);
        columnHelper = new ColumnHelper(worksheet);

        for(POIXMLDocumentPart p : getRelations()){
            if(p instanceof CommentsTable) sheetComments = (CommentsTable)p;
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
        hyperlinks = new ArrayList<XSSFHyperlink>();
    }

    private void initRows(CTWorksheet worksheet) {
        rows = new TreeMap<Integer, Row>();
        for (CTRow row : worksheet.getSheetData().getRowArray()) {
            XSSFRow r = new XSSFRow(row, this);
            rows.put(r.getRowNum(), r);
        }
    }

    /**
     * Read hyperlink relations, link them with CTHyperlink beans in this worksheet
     * and initialize the internal array of XSSFHyperlink objects
     */
    private void initHyperlinks() {
        hyperlinks = new ArrayList<XSSFHyperlink>();

        if(!worksheet.isSetHyperlinks()) return;

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
        ctFormat.setDefaultRowHeight(15.0);

        CTSheetView ctView = worksheet.addNewSheetViews().addNewSheetView();
        ctView.setWorkbookViewId(0);

        worksheet.addNewDimension().setRef("A1");

        worksheet.addNewSheetData();

        CTPageMargins ctMargins = worksheet.addNewPageMargins();
        ctMargins.setBottom(0.75);
        ctMargins.setFooter(0.3);
        ctMargins.setHeader(0.3);
        ctMargins.setLeft(0.7);
        ctMargins.setRight(0.7);
        ctMargins.setTop(0.75);

        return worksheet;
    }

    /**
     * Provide access to the CTWorksheet bean holding this sheet's data
     *
     * @return the CTWorksheet bean holding this sheet's data
     */
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
    public String getSheetName() {
        return sheet.getName();
    }

    /**
     * Adds a merged region of cells (hence those cells form one).
     *
     * @param cra (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */
    public int addMergedRegion(CellRangeAddress cra) {
        CTMergeCells ctMergeCells = worksheet.isSetMergeCells() ? worksheet.getMergeCells() : worksheet.addNewMergeCells();
        CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();
        ctMergeCell.setRef(cra.formatAsString());
        return ctMergeCells.sizeOfMergeCellArray();
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
    public void autoSizeColumn(int column, boolean useMergedCells) {
        double width = ColumnHelper.getColumnWidth(this, column, useMergedCells);
        if(width != -1){
            columnHelper.setColBestFit(column, true);
            columnHelper.setCustomWidth(column, true);
            columnHelper.setColWidth(column, width);
        }
    }

    /**
     * Create a new SpreadsheetML drawing. If this sheet already contains a drawing - return that.
     *
     * @return a SpreadsheetML drawing
     */
    public XSSFDrawing createDrawingPatriarch() {
        XSSFDrawing drawing = null;
        CTDrawing ctDrawing = worksheet.getDrawing();
        if(ctDrawing == null) {
            //drawingNumber = #drawings.size() + 1
            int drawingNumber = getPackagePart().getPackage().getPartsByContentType(XSSFRelation.DRAWINGS.getContentType()).size() + 1;
            drawing = (XSSFDrawing)createRelationship(XSSFRelation.DRAWINGS, XSSFFactory.getInstance(), drawingNumber);
            String relId = drawing.getPackageRelationship().getId();

            //add CT_Drawing element which indicates that this sheet contains drawing components built on the drawingML platform.
            //The relationship Id references the part containing the drawingML definitions.
            ctDrawing = worksheet.addNewDrawing();
            ctDrawing.setId(relId);
        } else {
            //search the referenced drawing in the list of the sheet's relations
            for(POIXMLDocumentPart p : getRelations()){
                if(p instanceof XSSFDrawing) {
                    XSSFDrawing dr = (XSSFDrawing)p;
                    String drId = dr.getPackageRelationship().getId();
                    if(drId.equals(ctDrawing.getId())){
                        drawing = dr;
                        break;
                    }
                    break;
                }
            }
            if(drawing == null){
                logger.log(POILogger.ERROR, "Can't find drawing with id=" + ctDrawing.getId() + " in the list of the sheet's relationships");
            }
        }
        return drawing;
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit	  Horizonatal position of split.
     * @param rowSplit	  Vertical position of split.
     * @param topRow		Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        CTPane pane = getPane();
        if (colSplit > 0) pane.setXSplit(colSplit);
        if (rowSplit > 0) pane.setYSplit(rowSplit);
        pane.setState(STPaneState.FROZEN);
        if (rowSplit == 0) {
            pane.setTopLeftCell(new CellReference(0, topRow).formatAsString());
            pane.setActivePane(STPane.TOP_RIGHT);
        } else if (colSplit == 0) {
            pane.setTopLeftCell(new CellReference(rowSplit, 0).formatAsString());
            pane.setActivePane(STPane.BOTTOM_LEFT);
        } else {
            pane.setTopLeftCell(new CellReference(leftmostColumn, topRow).formatAsString());
            pane.setActivePane(STPane.BOTTOM_RIGHT);
        }

        CTSheetView ctView = getDefaultSheetView();
        ctView.setSelectionArray(null);
        CTSelection sel = ctView.addNewSelection();
        sel.setPane(pane.getActivePane());
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit	  Horizonatal position of split.
     * @param rowSplit	  Vertical position of split.
     */
    public void createFreezePane(int colSplit, int rowSplit) {
        createFreezePane( colSplit, rowSplit, colSplit, rowSplit );
    }

    /**
     * Creates a new comment for this sheet. You still
     *  need to assign it to a cell though
     */
    public XSSFComment createComment() {
        if (sheetComments == null) {
            sheetComments = (CommentsTable)createRelationship(XSSFRelation.SHEET_COMMENTS, XSSFFactory.getInstance(), (int)sheet.getSheetId());
        }
        return sheetComments.addComment();
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return High level {@link XSSFRow} object representing a row in the sheet
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    public XSSFRow createRow(int rownum) {
        CTRow ctRow = CTRow.Factory.newInstance();
        XSSFRow r = new XSSFRow(ctRow, this);
        r.setRowNum(rownum);
        rows.put(r.getRowNum(), r);
        return r;
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos	  Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos	  Vertical position of split (in 1/20th of a point).
     * @param topRow		Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane	Active pane.  One of: PANE_LOWER_RIGHT,
     *					  PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see #PANE_LOWER_LEFT
     * @see #PANE_LOWER_RIGHT
     * @see #PANE_UPPER_LEFT
     * @see #PANE_UPPER_RIGHT
     */
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
        createFreezePane(xSplitPos, ySplitPos, leftmostColumn, topRow);
        getPane().setActivePane(STPane.Enum.forInt(activePane));
    }

    public XSSFComment getCellComment(int row, int column) {
        if (sheetComments == null) return null;
        else return sheetComments.findCellComment(row, column);
    }

    public XSSFHyperlink getHyperlink(int row, int column) {
        String ref = new CellReference(row, column).formatAsString();
        for(XSSFHyperlink hyperlink : hyperlinks) {
            if(hyperlink.getCellRef().equals(ref)) {
                return hyperlink;
            }
        }
        return null;
    }

    /**
     * Vertical page break information used for print layout view, page layout view, drawing print breaks
     * in normal view, and for printing the worksheet.
     *
     * @return column indexes of all the vertical page breaks, never <code>null</code>
     */
    public int[] getColumnBreaks() {
        if (!worksheet.isSetColBreaks() || worksheet.getColBreaks().sizeOfBrkArray() == 0) {
            return new int[0];
        }

        CTBreak[] brkArray = worksheet.getColBreaks().getBrkArray();
        int[] breaks = new int[brkArray.length];
        for (int i = 0 ; i < brkArray.length ; i++) {
            CTBreak brk = brkArray[i];
            breaks[i] = (int)brk.getId();
        }
        return breaks;
    }

    private CTPageBreak getSheetTypeColumnBreaks() {
        if (worksheet.getColBreaks() == null) {
            worksheet.setColBreaks(CTPageBreak.Factory.newInstance());
        }
        return worksheet.getColBreaks();
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
    public int getColumnWidth(int columnIndex) {
        CTCol col = columnHelper.getColumn(columnIndex, false);
        double width = col == null || !col.isSetWidth() ? DEFAULT_COLUMN_WIDTH : col.getWidth();
        return (int)(width*256);
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
    public int getDefaultColumnWidth() {
        CTSheetFormatPr pr = worksheet.getSheetFormatPr();
        return pr == null ? 8 : (int)pr.getBaseColWidth();
    }

    /**
     * Get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @return  default row height
     */
    public short getDefaultRowHeight() {
        return (short)(getDefaultRowHeightInPoints() * 20);
    }

    /**
     * Get the default row height for the sheet measued in point size (if the rows do not define their own height).
     *
     * @return  default row height in points
     */
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
     * Get whether to display the guts or not,
     * default value is true
     *
     * @return boolean - guts or no guts
     */
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
    public void setDisplayGuts(boolean value) {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTOutlinePr outlinePr = sheetPr.getOutlinePr() == null ? sheetPr.addNewOutlinePr() : sheetPr.getOutlinePr();
        outlinePr.setShowOutlineSymbols(value);
    }

    /**
     * Gets the first row on the sheet
     *
     * @return the number of the first logical row on the sheet, zero based
     */
    public int getFirstRowNum() {
        return rows.size() == 0 ? -1 : rows.firstKey();
    }

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @return <code>true</code>
     */
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
    public boolean getHorizontallyCenter() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getHorizontalCentered();
    }

    public int getLastRowNum() {
        return rows.size() == 0 ? -1 : rows.lastKey();
    }

    public short getLeftCol() {
        String cellRef = worksheet.getSheetViews().getSheetViewArray(0).getTopLeftCell();
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
    public double getMargin(short margin) {
        if (!worksheet.isSetPageMargins()) return 0;

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
                throw new POIXMLException("Unknown margin constant:  " + margin);
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
    public void setMargin(short margin, double size) {
        CTPageMargins pageMargins = worksheet.isSetPageMargins() ?
                worksheet.getPageMargins() : worksheet.addNewPageMargins();
        switch (margin) {
            case LeftMargin:
                pageMargins.setLeft(size);
            case RightMargin:
                pageMargins.setRight(size);
            case TopMargin:
                pageMargins.setTop(size);
            case BottomMargin:
                pageMargins.setBottom(size);
            case HeaderMargin:
                pageMargins.setHeader(size);
            case FooterMargin:
                pageMargins.setFooter(size);
        }
    }

    /**
     * @return the merged region at the specified index
     * @throws IllegalStateException if this worksheet does not contain merged regions
     */
    public CellRangeAddress getMergedRegion(int index) {
        CTMergeCells ctMergeCells = worksheet.getMergeCells();
        if(ctMergeCells == null) throw new IllegalStateException("This worksheet does not contain merged regions");

        CTMergeCell ctMergeCell = ctMergeCells.getMergeCellArray(index);
        String ref = ctMergeCell.getRef();
        CellReference cell1 = new CellReference(ref.substring(0, ref.indexOf(":")));
        CellReference cell2 = new CellReference(ref.substring(ref.indexOf(":") + 1));
        return new CellRangeAddress(cell1.getRow(), cell2.getRow(), cell1.getCol(), cell2.getCol());
    }

    /**
     * Returns the number of merged regions defined in this worksheet
     *
     * @return number of merged regions in this worksheet
     */
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
    public PaneInformation getPaneInformation() {
        CTPane pane = getPane();
        CellReference cellRef = pane.isSetTopLeftCell() ? new CellReference(pane.getTopLeftCell()) : null;
        return new PaneInformation((short)pane.getXSplit(), (short)pane.getYSplit(),
                (short)(cellRef == null ? 0 : cellRef.getRow()),(cellRef == null ? 0 : cellRef.getCol()),
                (byte)pane.getActivePane().intValue(), pane.getState() == STPaneState.FROZEN);
    }

    /**
     * Returns the number of phsyically defined rows (NOT the number of rows in the sheet)
     *
     * @return the number of phsyically defined rows
     */
    public int getPhysicalNumberOfRows() {
        return rows.size();
    }

    /**
     * Gets the print setup object.
     *
     * @return The user model for the print setup object.
     */
    public XSSFPrintSetup getPrintSetup() {
        return new XSSFPrintSetup(worksheet);
    }

    /**
     * Answer whether protection is enabled or disabled
     *
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getProtect() {
        return worksheet.isSetSheetProtection() && worksheet.getSheetProtection().getSheet();
    }

    /**
     * Returns the logical row ( 0-based).  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rownum  row to get
     * @return <code>XSSFRow</code> representing the rownumber or <code>null</code> if its not defined on the sheet
     */
    public XSSFRow getRow(int rownum) {
        return (XSSFRow)rows.get(rownum);
    }

    /**
     * Horizontal page break information used for print layout view, page layout view, drawing print breaks in normal
     *  view, and for printing the worksheet.
     *
     * @return row indexes of all the horizontal page breaks, never <code>null</code>
     */
    public int[] getRowBreaks() {
        if (!worksheet.isSetRowBreaks() || worksheet.getRowBreaks().sizeOfBrkArray() == 0) {
            return new int[0];
        }

        CTBreak[] brkArray = worksheet.getRowBreaks().getBrkArray();
        int[] breaks = new int[brkArray.length];
        for (int i = 0 ; i < brkArray.length ; i++) {
            CTBreak brk = brkArray[i];
            breaks[i] = (int)brk.getId();
        }
        return breaks;
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
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getScenarioProtect() {
        return worksheet.isSetSheetProtection() && worksheet.getSheetProtection().getScenarios();
    }

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return integer indicating the rownum (0 based) of the top row
     */
    public short getTopRow() {
        String cellRef = getSheetTypeSheetView().getTopLeftCell();
        CellReference cellReference = new CellReference(cellRef);
        return (short) cellReference.getRow();
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     *
     * @return whether printed output for this sheet will be vertically centered.
     */
    public boolean getVerticallyCenter() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getVerticalCentered();
    }

    /**
     * Group between (0 based) columns
     */
    public void groupColumn(int fromColumn, int toColumn) {
        groupColumn1Based(fromColumn+1, toColumn+1);
    }
    private void groupColumn1Based(int fromColumn, int toColumn) {
        CTCols ctCols=worksheet.getColsArray(0);
        CTCol ctCol=CTCol.Factory.newInstance();
        ctCol.setMin(fromColumn);
        ctCol.setMax(toColumn);
        this.columnHelper.addCleanColIntoCols(ctCols, ctCol);
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
     * Tie a range of cell together so that they can be collapsed or expanded
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
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
        short outlineLevel=0;
        for(Row r : rows.values()){
            XSSFRow xrow=(XSSFRow)r;
            outlineLevel=xrow.getCTRow().getOutlineLevel()>outlineLevel? xrow.getCTRow().getOutlineLevel(): outlineLevel;
        }
        return outlineLevel;
    }


    private short getMaxOutlineLevelCols(){
        CTCols ctCols=worksheet.getColsArray(0);
        CTCol[]colArray=ctCols.getColArray();
        short outlineLevel=0;
        for(CTCol col: colArray){
            outlineLevel=col.getOutlineLevel()>outlineLevel? col.getOutlineLevel(): outlineLevel;
        }
        return outlineLevel;
    }

    /**
     * Determines if there is a page break at the indicated column
     */
    public boolean isColumnBroken(short column) {
        int[] colBreaks = getColumnBreaks();
        for (int i = 0 ; i < colBreaks.length ; i++) {
            if (colBreaks[i] == column) {
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
    public boolean isColumnHidden(int columnIndex) {
        return columnHelper.getColumn(columnIndex, false).getHidden();
    }

    /**
     * Gets the flag indicating whether this sheet should display formulas.
     *
     * @return <code>true</code> if this sheet should display formulas.
     */
    public boolean isDisplayFormulas() {
        return getSheetTypeSheetView().getShowFormulas();
    }

    /**
     * Gets the flag indicating whether this sheet displays the lines
     * between rows and columns to make editing and reading easier.
     *
     * @return <code>true</code> if this sheet displays gridlines.
     * @see #isPrintGridlines() to check if printing of gridlines is turned on or off
     */
    public boolean isDisplayGridlines() {
        return getSheetTypeSheetView().getShowGridLines();
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
    public void setDisplayGridlines(boolean show) {
        getSheetTypeSheetView().setShowGridLines(show);
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
     * @return <code>true</code> if this sheet should display row and column headings.
     */
    public boolean isDisplayRowColHeadings() {
        return getSheetTypeSheetView().getShowRowColHeaders();
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
    public void setDisplayRowColHeadings(boolean show) {
        getSheetTypeSheetView().setShowRowColHeaders(show);
    }

    /**
     * Returns whether gridlines are printed.
     *
     * @return whether gridlines are printed
     */
    public boolean isPrintGridlines() {
        CTPrintOptions opts = worksheet.getPrintOptions();
        return opts != null && opts.getGridLines();
    }

    /**
     * Turns on or off the printing of gridlines.
     *
     * @param value boolean to turn on or off the printing of gridlines
     */
    public void setPrintGridlines(boolean value) {
        CTPrintOptions opts = worksheet.isSetPrintOptions() ?
                worksheet.getPrintOptions() : worksheet.addNewPrintOptions();
        opts.setGridLines(value);
    }

    /**
     * Tests if there is a page break at the indicated row
     *
     * @param row index of the row to test
     * @return <code>true</code> if there is a page break at the indicated row
     */
    public boolean isRowBroken(int row) {
        int[] rowBreaks = getRowBreaks();
        for (int i = 0 ; i < rowBreaks.length ; i++) {
            if (rowBreaks[i] == row) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a page break at the indicated row
     */
    public void setRowBreak(int row) {
        CTPageBreak pgBreak = worksheet.isSetRowBreaks() ? worksheet.getRowBreaks() : worksheet.addNewRowBreaks();
        if (! isRowBroken(row)) {
            CTBreak brk = pgBreak.addNewBrk();
            brk.setId(row);
        }
    }

    /**
     * Removes a page break at the indicated column
     */
    public void removeColumnBreak(short column) {
        CTBreak[] brkArray = getSheetTypeColumnBreaks().getBrkArray();
        for (int i = 0 ; i < brkArray.length ; i++) {
            if (brkArray[i].getId() == column) {
                getSheetTypeColumnBreaks().removeBrk(i);
            }
        }
    }

    /**
     * Removes a merged region of cells (hence letting them free)
     *
     * @param index of the region to unmerge
     */
    public void removeMergedRegion(int index) {
        CTMergeCells ctMergeCells = worksheet.getMergeCells();

        CTMergeCell[] mergeCellsArray = new CTMergeCell[ctMergeCells.sizeOfMergeCellArray() - 1];
        for (int i = 0 ; i < ctMergeCells.sizeOfMergeCellArray() ; i++) {
            if (i < index) {
                mergeCellsArray[i] = ctMergeCells.getMergeCellArray(i);
            }
            else if (i > index) {
                mergeCellsArray[i - 1] = ctMergeCells.getMergeCellArray(i);
            }
        }
        ctMergeCells.setMergeCellArray(mergeCellsArray);
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row  the row to remove.
     */
    public void removeRow(Row row) {
        rows.remove(row.getRowNum());
    }

    /**
     * Removes the page break at the indicated row
     */
    public void removeRowBreak(int row) {
        CTPageBreak pgBreak = worksheet.isSetRowBreaks() ? worksheet.getRowBreaks() : worksheet.addNewRowBreaks();
        CTBreak[] brkArray = pgBreak.getBrkArray();
        for (int i = 0 ; i < brkArray.length ; i++) {
            if (brkArray[i].getId() == row) {
                pgBreak.removeBrk(i);
            }
        }
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    public Iterator<Row> rowIterator() {
        return rows.values().iterator();
    }

    /**
     * Alias for {@link #rowIterator()} to
     *  allow foreach loops
     */
    public Iterator<Row> iterator() {
        return rowIterator();
    }

    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @return <code>true</code> if the sheet displays Automatic Page Breaks.
     */
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
    public void setAutobreaks(boolean value) {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        CTPageSetUpPr psSetup = sheetPr.isSetPageSetUpPr() ? sheetPr.getPageSetUpPr() : sheetPr.addNewPageSetUpPr();
        psSetup.setAutoPageBreaks(value);
    }

    /**
     * Sets a page break at the indicated column
     *
     * @param column the column to break
     */
    public void setColumnBreak(short column) {
        if (! isColumnBroken(column)) {
            CTBreak brk = getSheetTypeColumnBreaks().addNewBrk();
            brk.setId(column);
        }
    }

    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        // TODO Auto-generated method stub

    }

    public void setRowGroupCollapsed(int row, boolean collapse) {
        // TODO Auto-generated method stub

    }

    /**
     * Get the visibility state for a given column.
     *
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */
     public void setColumnHidden(int columnIndex, boolean hidden) {
        columnHelper.setColHidden(columnIndex, hidden);
     }

    /**
     * Set the width (in units of 1/256th of a character width)
     *
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     */
    public void setColumnWidth(int columnIndex, int width) {
        columnHelper.setColWidth(columnIndex, (double)width/256);
    }

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
    public void setDefaultColumnWidth(int width) {
        getSheetTypeSheetFormatPr().setBaseColWidth(width);
    }

    /**
     * Set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @param  height default row height in  twips (1/20 of  a point)
     */
    public void setDefaultRowHeight(short height) {
        getSheetTypeSheetFormatPr().setDefaultRowHeight((double)height / 20);

    }

    /**
     * Sets default row height measured in point size.
     *
     * @param height default row height measured in point size.
     */
    public void setDefaultRowHeightInPoints(float height) {
        getSheetTypeSheetFormatPr().setDefaultRowHeight(height);

    }

    /**
     * Sets the flag indicating whether this sheet should display formulas.
     *
     * @param show <code>true</code> if this sheet should display formulas.
     */
    public void setDisplayFormulas(boolean show) {
        getSheetTypeSheetView().setShowFormulas(show);
    }

    private CTSheetView getSheetTypeSheetView() {
        if (getDefaultSheetView() == null) {
            getSheetTypeSheetViews().setSheetViewArray(0, CTSheetView.Factory.newInstance());
        }
        return getDefaultSheetView();
    }

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @param b <code>true</code> if the Fit to Page print option is enabled.
     */
     public void setFitToPage(boolean b) {
        getSheetTypePageSetUpPr().setFitToPage(b);
    }

    /**
     * Center on page horizontally when printing.
     *
     * @param value whether to center on page horizontally when printing.
     */
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
    public void setVerticallyCenter(boolean value) {
        CTPrintOptions opts = worksheet.isSetPrintOptions() ?
                worksheet.getPrintOptions() : worksheet.addNewPrintOptions();
        opts.setVerticalCentered(value);
    }

    /**
     * Sets the zoom magnication for the sheet.  The zoom is expressed as a
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     *
     * @param numerator	 The numerator for the zoom magnification.
     * @param denominator   The denominator for the zoom magnification.
     * @see #setZoom(int)
     */
    public void setZoom(int numerator, int denominator) {
        int zoom = 100*numerator/denominator;
        setZoom(zoom);
    }

    /**
     * Window zoom magnification for current view representing percent values.
     * Valid values range from 10 to 400. Horizontal & Vertical scale together.
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
    public void setZoom(int scale) {
        if(scale < 10 || scale > 400) throw new IllegalArgumentException("Valid scale values range from 10 to 400");
        getSheetTypeSheetView().setZoomScale(scale);
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
     * rows (ie. merged 2 cells on a row to be shifted).
     * <p>
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row row = it.next();

            if (!copyRowHeight) {
                row.setHeight((short)-1);
            }

            if (resetOriginalRowHeight && getDefaultRowHeight() >= 0) {
                row.setHeight(getDefaultRowHeight());
            }
            if (removeRow(startRow, endRow, n, row.getRowNum())) {
                it.remove();
            }
            else if (row.getRowNum() >= startRow && row.getRowNum() <= endRow) {
                row.setRowNum(row.getRowNum() + n);
                if (row.getFirstCellNum() > -1) {
                    modifyCellReference((XSSFRow) row);
                }
            }
        }
        //rebuild the rows map
        TreeMap<Integer, Row> map = new TreeMap<Integer, Row>();
        for(Row r : this) map.put(r.getRowNum(), r);
        rows = map;
    }


    private void modifyCellReference(XSSFRow row) {
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            XSSFCell c = row.getCell(i);
            if (c != null) {
                c.modifyCellReference(row);
            }
        }
    }

    /**
     * Location of the top left visible cell Location of the top left visible cell in the bottom right
     * pane (when in Left-to-Right mode).
     *
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    public void showInPane(short toprow, short leftcol) {
        CellReference cellReference = new CellReference(toprow, leftcol);
        String cellRef = cellReference.formatAsString();
        getSheetTypeSheetView().setTopLeftCell(cellRef);
    }

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
    public void ungroupRow(int fromRow, int toRow) {
        for (int i = fromRow; i <= toRow; i++) {
            XSSFRow xrow = getRow(i);
            if (xrow != null) {
                CTRow ctrow = xrow.getCTRow();
                short outlinelevel = ctrow.getOutlineLevel();
                ctrow.setOutlineLevel((short) (outlinelevel - 1));
                //remove a row only if the row has no cell and if the outline level is 0
                if (ctrow.getOutlineLevel() == 0 && xrow.getFirstCellNum() == -1) {
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

    private CTSheetViews getSheetTypeSheetViews() {
        if (worksheet.getSheetViews() == null) {
            worksheet.setSheetViews(CTSheetViews.Factory.newInstance());
            worksheet.getSheetViews().addNewSheetView();
        }
        return worksheet.getSheetViews();
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
    public boolean isSelected() {
        CTSheetView view = getDefaultSheetView();
        return view != null && view.getTabSelected();
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
    public void setSelected(boolean value) {
        CTSheetViews views = getSheetTypeSheetViews();
        for (CTSheetView view : views.getSheetViewArray()) {
            view.setTabSelected(value);
        }
    }

    /**
     * Assign a cell comment to a cell region in this worksheet
     *
     * @param cellRef cell region
     * @param comment the comment to assign
     */
    public void setCellComment(String cellRef, XSSFComment comment) {
        CellReference cellReference = new CellReference(cellRef);

        comment.setRow(cellReference.getRow());
        comment.setColumn(cellReference.getCol());
    }

    protected void setCellHyperlink(XSSFHyperlink hyperlink) {
        hyperlinks.add(hyperlink);
    }

    /**
     * Return location of the active cell, e.g. <code>A1</code>.
     *
     * @return the location of the active cell.
     */
    public String getActiveCell() {
        return getSheetTypeSelection().getActiveCell();
    }

    /**
     * Sets location of the active cell
     *
     * @param cellRef the location of the active cell, e.g. <code>A1</code>..
     */
    public void setActiveCell(String cellRef) {
        CTSelection ctsel = getSheetTypeSelection();
        ctsel.setActiveCell(cellRef);
        ctsel.setSqref(Arrays.asList(cellRef));
    }

    /**
     * Does this sheet have any comments on it? We need to know,
     *  so we can decide about writing it to disk or not
     */
    public boolean hasComments() {
        if(sheetComments == null) { return false; }
        return (sheetComments.getNumberOfComments() > 0);
    }

    protected int getNumberOfComments() {
        if(sheetComments == null) { return 0; }
        return sheetComments.getNumberOfComments();
    }

    private CTSelection getSheetTypeSelection() {
        if (getSheetTypeSheetView().sizeOfSelectionArray() == 0) {
            getSheetTypeSheetView().insertNewSelection(0);
        }
        return getSheetTypeSheetView().getSelectionArray(0);
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
    private CTSheetView getDefaultSheetView() {
        CTSheetViews views = getSheetTypeSheetViews();
        if (views == null || views.getSheetViewArray() == null || views.getSheetViewArray().length <= 0) {
            return null;
        }
        return views.getSheetViewArray(views.getSheetViewArray().length - 1);
    }

    /**
     * Returns the sheet's comments object if there is one,
     *  or null if not
     */
    protected CommentsTable getCommentsTable() {
        return sheetComments;
    }

    private CTPageSetUpPr getSheetTypePageSetUpPr() {
        CTSheetPr sheetPr = getSheetTypeSheetPr();
        return sheetPr.isSetPageSetUpPr() ? sheetPr.getPageSetUpPr() : sheetPr.addNewPageSetUpPr();
    }

    private boolean removeRow(int startRow, int endRow, int n, int rownum) {
        if (rownum >= (startRow + n) && rownum <= (endRow + n)) {
            if (n > 0 && rownum > endRow) {
                return true;
            }
            else if (n < 0 && rownum < startRow) {
                return true;
            }
        }
        return false;
    }

    private CTPane getPane() {
        if (getDefaultSheetView().getPane() == null) {
            getDefaultSheetView().addNewPane();
        }
        return getDefaultSheetView().getPane();
    }


    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        write(out);
        out.close();
    }

    protected void write(OutputStream out) throws IOException {

        if(worksheet.getColsArray().length == 1) {
            CTCols col = worksheet.getColsArray(0);
            CTCol[] cols = col.getColArray();
            if(cols.length == 0) {
                worksheet.setColsArray(null);
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

        CTSheetData sheetData = worksheet.getSheetData();
        ArrayList<CTRow> rArray = new ArrayList<CTRow>(rows.size());
        for(Row row : rows.values()){
            XSSFRow r = (XSSFRow)row;
            r.onDocumentWrite();
            rArray.add(r.getCTRow());
        }
        sheetData.setRowArray(rArray.toArray(new CTRow[rArray.size()]));

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));
        Map map = new HashMap();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        xmlOptions.setSaveSuggestedPrefixes(map);

        worksheet.save(out, xmlOptions);
    }
}
