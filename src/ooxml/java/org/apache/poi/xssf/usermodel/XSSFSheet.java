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
import java.util.*;
import javax.xml.namespace.QName;

import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CommentsSource;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.Region;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.Control;
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

    protected CTSheet sheet;
    protected CTWorksheet worksheet;
    protected CTDialogsheet dialogsheet;
    protected List<Row> rows;
    protected List<XSSFHyperlink> hyperlinks;
    protected ColumnHelper columnHelper;
    protected CommentsSource sheetComments;
    protected CTMergeCells ctMergeCells;


    protected List<Control> controls;


    public static final short LeftMargin = 0;
    public static final short RightMargin = 1;
    public static final short TopMargin = 2;
    public static final short BottomMargin = 3;
    public static final short HeaderMargin = 4;
    public static final short FooterMargin = 5;


    public XSSFSheet() {
        super(null, null);
        this.worksheet = newSheet();
        initialize();
    }

    public XSSFSheet(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        worksheet = WorksheetDocument.Factory.parse(part.getInputStream()).getWorksheet();
    }

    public XSSFSheet(CTSheet sheet, CTWorksheet worksheet, XSSFWorkbook workbook, CommentsSource sheetComments) {
        super(null, null);
        this.parent = workbook;
        this.sheet = sheet;
        this.worksheet = worksheet;
        this.sheetComments = sheetComments;

        initialize();
    }

    public XSSFSheet(CTSheet sheet, CTWorksheet worksheet, XSSFWorkbook workbook) {
        this(sheet, worksheet, workbook, null);
    }

    protected XSSFSheet(XSSFWorkbook workbook) {
        super(null, null);
        this.parent = workbook;

        hyperlinks = new ArrayList<XSSFHyperlink>();
    }

    /**
     * Returns the parent XSSFWorkbook
     *
     * @return the parent XSSFWorkbook
     */
    public XSSFWorkbook getWorkbook() {
        return (XSSFWorkbook)getParent();
    }

    protected void initialize(){
        if (this.worksheet.getSheetData() == null) {
            this.worksheet.addNewSheetData();
        }
        initRows(this.worksheet);
        initColumns(this.worksheet);

        for(POIXMLDocumentPart p : getRelations()){
            if(p instanceof CommentsTable) sheetComments = (CommentsTable)p;
        }
        hyperlinks = new ArrayList<XSSFHyperlink>();
    }

    /**
     * Create a new CTWorksheet instance and setup default values
     *
     * @return a new instance
     */
    protected static CTWorksheet newSheet(){
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

    public List<Control> getControls()
    {
        return controls;
    }

    /**
     * Provide access to the underlying XML bean
     *
     * @return the underlying CTWorksheet bean
     */
    public CTWorksheet getWorksheet() {
        return this.worksheet;
    }

    public ColumnHelper getColumnHelper() {
        return columnHelper;
    }

    protected void initRows(CTWorksheet worksheet) {
        this.rows = new LinkedList<Row>();
        for (CTRow row : worksheet.getSheetData().getRowArray()) {
            this.rows.add(new XSSFRow(row, this));
        }
    }

    protected void initColumns(CTWorksheet worksheet) {
        columnHelper = new ColumnHelper(worksheet);
    }

    protected void initHyperlinks(PackageRelationshipCollection hyperRels) {
        if(worksheet.getHyperlinks() == null) return;

        // Turn each one into a XSSFHyperlink
        for(CTHyperlink hyperlink : worksheet.getHyperlinks().getHyperlinkArray()) {
            PackageRelationship hyperRel = null;
            if(hyperlink.getId() != null) {
                hyperRel = hyperRels.getRelationshipByID(hyperlink.getId());
            }

            hyperlinks.add(
                    new XSSFHyperlink(hyperlink, hyperRel)
            );
        }
    }

    protected CTSheet getSheet() {
        return this.sheet;
    }
    public int addMergedRegion(CellRangeAddress cra) {
        Region r = new Region(cra.getFirstRow(), (short)cra.getFirstColumn(),
                cra.getLastRow(), (short)cra.getLastColumn());
        return addMergedRegion(r);
    }


    public int addMergedRegion(Region region) {
        addNewMergeCell(region);
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
   public void autoSizeColumn(short column) {
        autoSizeColumn(column, false);
    }

    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * You can specify whether the content of merged cells should be considered or ignored.
     *  Default is to ignore merged cells.
     *
     * @param column the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    public void autoSizeColumn(short column, boolean useMergedCells) {
        //TODO:
        columnHelper.setColBestFit(column, true);
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
            int drawingNumber = getPackagePart().getPackage().getPartsByRelationshipType(XSSFRelation.DRAWINGS.getRelation()).size() + 1;
            drawing = (XSSFDrawing)createRelationship(XSSFRelation.DRAWINGS, XSSFDrawing.class, drawingNumber);
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
        this.createFreezePane(colSplit, rowSplit);
        this.showInPane((short)topRow, (short)leftmostColumn);
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit	  Horizonatal position of split.
     * @param rowSplit	  Vertical position of split.
     */
    public void createFreezePane(int colSplit, int rowSplit) {
        getPane().setXSplit(colSplit);
        getPane().setYSplit(rowSplit);
        // make bottomRight default active pane
        getPane().setActivePane(STPane.BOTTOM_RIGHT);
    }

    /**
     * Creates a new comment for this sheet. You still
     *  need to assign it to a cell though
     */
    public XSSFComment createComment() {
        return (XSSFComment)getComments().addComment();
    }

    protected XSSFRow addRow(int index, int rownum) {
        CTRow row = this.worksheet.getSheetData().insertNewRow(index);
        XSSFRow xrow = new XSSFRow(row, this);
        xrow.setRowNum(rownum);
        return xrow;
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return High level {@link XSSFRow} object representing a row in the sheet
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    public XSSFRow createRow(int rownum) {
        int index = 0;
        for (Row r : this.rows) {
            if (r.getRowNum() == rownum) {
                // Replace r with new row
                XSSFRow xrow = addRow(index, rownum);
                rows.set(index, xrow);
                return xrow;
            }
            if (r.getRowNum() > rownum) {
                XSSFRow xrow = addRow(index, rownum);
                rows.add(index, xrow);
                return xrow;
            }
            ++index;
        }
        XSSFRow xrow = addRow(index, rownum);
        rows.add(xrow);
        return xrow;
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

    public boolean getAlternateExpression() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getAlternateFormula() {
        // TODO Auto-generated method stub
        return false;
    }

    public XSSFComment getCellComment(int row, int column) {
        if (sheetComments == null) return null;
        else return (XSSFComment)getComments().findCellComment(row, column);
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

    protected CTPageBreak getSheetTypeColumnBreaks() {
        if (worksheet.getColBreaks() == null) {
            worksheet.setColBreaks(CTPageBreak.Factory.newInstance());
        }
        return worksheet.getColBreaks();
    }

    public int getColumnWidth(int columnIndex) {
        CTCol col = columnHelper.getColumn(columnIndex, false);
        return col == null ? getDefaultColumnWidth() : (int)col.getWidth();
    }
    public short getColumnWidth(short column) {
        return (short) getColumnWidth(column & 0xFFFF);
    }

    public int getDefaultColumnWidth() {
        CTSheetFormatPr pr = getSheetTypeSheetFormatPr();
        return pr.isSetDefaultColWidth() ? (int)pr.getDefaultColWidth() : (int)pr.getBaseColWidth();
    }

    public short getDefaultRowHeight() {
        return (short) (getSheetTypeSheetFormatPr().getDefaultRowHeight() * 20);
    }

    protected CTSheetFormatPr getSheetTypeSheetFormatPr() {
        if (worksheet.getSheetFormatPr() == null) {
            worksheet.setSheetFormatPr(CTSheetFormatPr.Factory.newInstance());
        }
        return worksheet.getSheetFormatPr();
    }

    public float getDefaultRowHeightInPoints() {
        return (short) getSheetTypeSheetFormatPr().getDefaultRowHeight();
    }

    public boolean getDialog() {
        if (dialogsheet != null) {
            return true;
        }
        return false;
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
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row row = it.next();
            if (row != null) {
                return row.getRowNum();
            }
        }
        return -1;
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

    protected CTSheetPr getSheetTypeSheetPr() {
        if (worksheet.getSheetPr() == null) {
            worksheet.setSheetPr(CTSheetPr.Factory.newInstance());
        }
        return worksheet.getSheetPr();
    }

    protected CTHeaderFooter getSheetTypeHeaderFooter() {
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
        return new XSSFEvenHeader(getSheetTypeHeaderFooter()
);
    }
    /**
     * Returns the first page header. Not there by
     *  default, but when set, used on the first page.
     */
    public Header getFirstHeader() {
        return new XSSFFirstHeader(getSheetTypeHeaderFooter());
    }


    public boolean getHorizontallyCenter() {
        return getSheetTypePrintOptions().getHorizontalCentered();
    }

    protected CTPrintOptions getSheetTypePrintOptions() {
        if (worksheet.getPrintOptions() == null) {
            worksheet.setPrintOptions(CTPrintOptions.Factory.newInstance());
        }
        return worksheet.getPrintOptions();
    }

    public int getLastRowNum() {
        int lastRowNum = -1;
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row row = it.next();
            if (row != null) {
                lastRowNum = row.getRowNum();
            }
        }
        return lastRowNum;
    }

    public short getLeftCol() {
        String cellRef = worksheet.getSheetViews().getSheetViewArray(0).getTopLeftCell();
        CellReference cellReference = new CellReference(cellRef);
        return (short)cellReference.getCol();
    }

    public double getMargin(short margin) {
        CTPageMargins pageMargins = getSheetTypePageMargins();
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
            throw new POIXMLException( "Unknown margin constant:  " + margin );
        }
    }

    protected CTPageMargins getSheetTypePageMargins() {
        if (worksheet.getPageMargins() == null) {
            worksheet.setPageMargins(CTPageMargins.Factory.newInstance());
        }
        return worksheet.getPageMargins();
    }

    public Region getMergedRegionAt(int index) {
        CTMergeCell ctMergeCell = getMergedCells().getMergeCellArray(index);
        return new Region(ctMergeCell.getRef());
    }

    public int getNumMergedRegions() {
        return getMergedCells().sizeOfMergeCellArray();
    }

    public int getNumHyperlinks() {
        return hyperlinks.size();
    }

    public boolean getObjectProtect() {
        // TODO Auto-generated method stub
        return false;
    }

    public PaneInformation getPaneInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    public short getPassword() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPhysicalNumberOfRows() {
        int counter = 0;
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            if (it.next() != null) {
                counter++;
            }
        }
        return counter;
    }

    public XSSFPrintSetup getPrintSetup() {
        return new XSSFPrintSetup(getWorksheet());
    }

    public boolean getProtect() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Returns the logical row ( 0-based).  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rownum  row to get
     * @return <code>XSSFRow</code> representing the rownumber or <code>null</code> if its not defined on the sheet
     */
    public XSSFRow getRow(int rownum) {
        //TODO current implemenation is expensive, it should take O(1), not O(N)
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row row = it.next();
            if (row.getRowNum() == rownum) {
                 return (XSSFRow)row;
            }
        }
        return null;
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
                ? sheetPr.getOutlinePr() : CTOutlinePr.Factory.newInstance();
        return outlinePr.getSummaryBelow();
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
        CTOutlinePr outlinePr = sheetPr.isSetOutlinePr() ? sheetPr.getOutlinePr() : sheetPr.addNewOutlinePr();
        return outlinePr;
    }

    /**
     * A flag indicating whether scenarios are locked when the sheet is protected.
     *
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getScenarioProtect() {
        return getSheetTypeProtection().getScenarios();
    }

    protected CTSheetProtection getSheetTypeProtection() {
        if (worksheet.getSheetProtection() == null) {
            worksheet.setSheetProtection(CTSheetProtection.Factory.newInstance());
        }
        return worksheet.getSheetProtection();
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
        return getSheetTypePrintOptions().getVerticalCentered();
    }

    /**
     * Group between (0 based) columns
     */
    public void groupColumn(short fromColumn, short toColumn) {
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

    public void groupRow(int fromRow, int toRow) {
          for(int i=fromRow;i<=toRow;i++){
            XSSFRow xrow = getRow(i-1);
            if(xrow == null){//create a new Row
                 xrow = createRow(i-1);
            }
            CTRow ctrow=xrow.getCTRow();
            short outlineLevel=ctrow.getOutlineLevel();
            ctrow.setOutlineLevel((short)(outlineLevel+1));
           }
          setSheetFormatPrOutlineLevelRow();
    }

   private short getMaxOutlineLevelRows(){
        short outlineLevel=0;
        for(Row r:rows){
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

    public boolean isColumnHidden(int columnIndex) {
        return columnHelper.getColumn(columnIndex, false).getHidden();
    }
    public boolean isColumnHidden(short column) {
        return isColumnHidden(column & 0xFFFF);
    }

    public boolean isDisplayFormulas() {
        return getSheetTypeSheetView().getShowFormulas();
    }

    public boolean isDisplayGridlines() {
        return getSheetTypeSheetView().getShowGridLines();
    }

    public boolean isDisplayRowColHeadings() {
        return getSheetTypeSheetView().getShowRowColHeaders();
    }

    public boolean isGridsPrinted() {
        return isPrintGridlines();
    }

    public boolean isPrintGridlines() {
        return getSheetTypePrintOptions().getGridLines();
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

    public void protectSheet(String password) {
        // TODO Auto-generated method stub

    }

    public void removeMergedRegion(int index) {
        CTMergeCell[] mergeCellsArray = new CTMergeCell[getMergedCells().sizeOfMergeCellArray() - 1];
        for (int i = 0 ; i < getMergedCells().sizeOfMergeCellArray() ; i++) {
            if (i < index) {
                mergeCellsArray[i] = getMergedCells().getMergeCellArray(i);
            }
            else if (i > index) {
                mergeCellsArray[i - 1] = getMergedCells().getMergeCellArray(i);
            }
        }
        getMergedCells().setMergeCellArray(mergeCellsArray);
    }

    public void removeRow(Row row) {
        int counter = 0;
        int rowNum=row.getRowNum();
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row r = it.next();
            if (r.getRowNum() == rowNum) {
                it.remove();
                worksheet.getSheetData().removeRow(counter);
            }
            counter++;
        }
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

    public Iterator<Row> rowIterator() {
        return rows.iterator();
    }

    /**
     * Alias for {@link #rowIterator()} to
     *  allow foreach loops
     */
    public Iterator<Row> iterator() {
        return rowIterator();
    }

    public void setAlternativeExpression(boolean b) {
        // TODO Auto-generated method stub

    }

    public void setAlternativeFormula(boolean b) {
        // TODO Auto-generated method stub

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

    public void setColumnBreak(short column) {
        if (! isColumnBroken(column)) {
            CTBreak brk = getSheetTypeColumnBreaks().addNewBrk();
            brk.setId(column);
        }
    }

    public void setColumnGroupCollapsed(short columnNumber, boolean collapsed) {
        // TODO Auto-generated method stub

    }

    public void setColumnHidden(int columnIndex, boolean hidden) {
        columnHelper.setColHidden(columnIndex, hidden);
    }
    public void setColumnHidden(short column, boolean hidden) {
        setColumnHidden(column & 0xFFFF, hidden);
    }

    public void setColumnWidth(int columnIndex, int width) {
        columnHelper.setColWidth(columnIndex, width);
    }
    public void setColumnWidth(short column, short width) {
        setColumnWidth(column & 0xFFFF, width & 0xFFFF);
    }

    public void setDefaultColumnStyle(short column, CellStyle style) {
        columnHelper.setColDefaultStyle(column, style);
    }

    public void setDefaultColumnWidth(int width) {
        getSheetTypeSheetFormatPr().setDefaultColWidth(width);
    }
    public void setDefaultColumnWidth(short width) {
        setDefaultColumnWidth(width & 0xFFFF);
    }

    public void setDefaultRowHeight(short height) {
        getSheetTypeSheetFormatPr().setDefaultRowHeight(height / 20);

    }

    public void setDefaultRowHeightInPoints(float height) {
        getSheetTypeSheetFormatPr().setDefaultRowHeight(height);

    }

    public void setDialog(boolean b) {
        if(b && dialogsheet == null){
            CTDialogsheet dialogSheet = CTDialogsheet.Factory.newInstance();
            dialogsheet = dialogSheet;
        }else{
            dialogsheet = null;
        }
    }

    public void setDisplayFormulas(boolean show) {
        getSheetTypeSheetView().setShowFormulas(show);
    }

    protected CTSheetView getSheetTypeSheetView() {
        if (getDefaultSheetView() == null) {
            getSheetTypeSheetViews().setSheetViewArray(0, CTSheetView.Factory.newInstance());
        }
        return getDefaultSheetView();
    }

    public void setDisplayGridlines(boolean show) {
        getSheetTypeSheetView().setShowGridLines(show);
    }

    public void setDisplayRowColHeadings(boolean show) {
        getSheetTypeSheetView().setShowRowColHeaders(show);
    }

    public void setFitToPage(boolean b) {
        getSheetTypePageSetUpPr().setFitToPage(b);
    }

    public void setGridsPrinted(boolean value) {
        setPrintGridlines(value);
    }

    public void setHorizontallyCenter(boolean value) {
        getSheetTypePrintOptions().setHorizontalCentered(value);
    }

    public void setMargin(short margin, double size) {
        CTPageMargins pageMargins = getSheetTypePageMargins();
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

    public void setPrintGridlines(boolean newPrintGridlines) {
        getSheetTypePrintOptions().setGridLines(newPrintGridlines);
    }

    public void setRowGroupCollapsed(int row, boolean collapse) {
        // TODO Auto-generated method stub

    }

    public void setVerticallyCenter(boolean value) {
        getSheetTypePrintOptions().setVerticalCentered(value);
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
        Float result = new Float(numerator)/new Float(denominator)*100;
        setZoom(result.intValue());
    }

    /**
     * Window zoom magnification for current view representing percent values.
     * Valid values range from 10 to 400. Horizontal & Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * …
     * 100 - 100%
     * …
     * 400 - 400%
     * </pre>
     *
     * Current view can be Normal, Page Layout, or Page Break Preview.
     *
     * @param scale window zoom magnification
     */
    public void setZoom(int scale) {
        getSheetTypeSheetView().setZoomScale(scale);
    }

    /**
     * Zoom magnification to use when in normal view, representing percent values.
     * Valid values range from 10 to 400. Horizontal & Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * …
     * 100 - 100%
     * …
     * 400 - 400%
     * </pre>
     *
     * Applies for worksheet sheet type only; zero implies the automatic setting.
     *
     * @param scale window zoom magnification
     */
    public void setZoomNormal(int scale) {
        getSheetTypeSheetView().setZoomScaleNormal(scale);
    }

    /**
     * Zoom magnification to use when in page layout view, representing percent values.
     * Valid values range from 10 to 400. Horizontal & Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * …
     * 100 - 100%
     * …
     * 400 - 400%
     * </pre>
     *
     * Applies for worksheet sheet type only; zero implies the automatic setting.
     *
     * @param scale
     */
    public void setZoomPageLayoutView(int scale) {
        getSheetTypeSheetView().setZoomScalePageLayoutView(scale);
    }

    /**
     * Zoom magnification to use when in page break preview, representing percent values.
     * Valid values range from 10 to 400. Horizontal & Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * …
     * 100 - 100%
     * …
     * 400 - 400%
     * </pre>
     *
     * Applies for worksheet only; zero implies the automatic setting.
     *
     * @param scale
     */
    public void setZoomSheetLayoutView(int scale) {
        getSheetTypeSheetView().setZoomScaleSheetLayoutView(scale);
    }

    public void shiftRows(int startRow, int endRow, int n) {
        shiftRows(startRow, endRow, n, false, false);
    }

    public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row row = it.next();
            if (!copyRowHeight) {
                row.setHeight((short)0);
            }
            if (resetOriginalRowHeight && getDefaultRowHeight() >= 0) {
                row.setHeight(getDefaultRowHeight());
            }
            if (removeRow(startRow, endRow, n, row.getRowNum())) {
                it.remove();
            }
            else if (row.getRowNum() >= startRow && row.getRowNum() <= endRow) {
                row.setRowNum(row.getRowNum() + n);
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

    public void ungroupColumn(short fromColumn, short toColumn) {
        CTCols cols=worksheet.getColsArray(0);
        for(int index=fromColumn;index<=toColumn;index++){
            CTCol col=columnHelper.getColumn(index, false);
            if(col!=null){
                short outlineLevel=col.getOutlineLevel();
                col.setOutlineLevel((short)(outlineLevel-1));
                index=(int)col.getMax();

                if(col.getOutlineLevel()<=0){
                    int colIndex=columnHelper.getIndexOfColumn(cols,col);
                    worksheet.getColsArray(0).removeCol(colIndex);
                }
            }
        }
        worksheet.setColsArray(0,cols);
        setSheetFormatPrOutlineLevelCol();
    }

    public void ungroupRow(int fromRow, int toRow) {
        for(int i=fromRow;i<=toRow;i++){
            XSSFRow xrow=(XSSFRow)getRow(i-1);
            if(xrow!=null){
                CTRow ctrow=xrow.getCTRow();
                short outlinelevel=ctrow.getOutlineLevel();
                ctrow.setOutlineLevel((short)(outlinelevel-1));
                //remove a row only if the row has no cell and if the outline level is 0
                if(ctrow.getOutlineLevel()==0 && xrow.getFirstCellNum()==-1){
                    removeRow(xrow);
                }
            }
        }
        setSheetFormatPrOutlineLevelRow();
    }

    private void setSheetFormatPrOutlineLevelRow(){
        short maxLevelRow=getMaxOutlineLevelRows();
        getSheetTypeSheetFormatPr().setOutlineLevelRow((short)(maxLevelRow));
    }

    private void setSheetFormatPrOutlineLevelCol(){
        short maxLevelCol=getMaxOutlineLevelCols();
        getSheetTypeSheetFormatPr().setOutlineLevelCol((short)(maxLevelCol));
    }

    protected CTSheetViews getSheetTypeSheetViews() {
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

    protected XSSFSheet cloneSheet() {
        XSSFSheet newSheet = new XSSFSheet(getWorkbook());
        newSheet.setSheet((CTSheet)sheet.copy());
        return newSheet;
    }

    private void setSheet(CTSheet sheet) {
        this.sheet = sheet;
    }

    private CommentsSource getComments() {
        if (sheetComments == null) {
            sheetComments = (CommentsTable)createRelationship(XSSFRelation.SHEET_COMMENTS, CommentsTable.class, (int)sheet.getSheetId());
        }
        return sheetComments;
    }
    /**
     * Returns the sheet's comments object if there is one,
     *  or null if not
     */
    protected CommentsSource getCommentsSourceIfExists() {
        return sheetComments;
    }

    private void addNewMergeCell(Region region) {
        ctMergeCells = getMergedCells();
        CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();
        ctMergeCell.setRef(region.getRegionRef());
    }

    private CTMergeCells getMergedCells() {
        if (ctMergeCells == null) {
            ctMergeCells = worksheet.addNewMergeCells();
        }
        return ctMergeCells;
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

        if(worksheet.getColsArray().length == 1) {
            CTCols col = worksheet.getColsArray(0);
            if(col.getColArray().length == 0) {
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

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTWorksheet.type.getName().getNamespaceURI(), "worksheet"));

        Map map = new HashMap();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        xmlOptions.setSaveSuggestedPrefixes(map);

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        worksheet.save(out, xmlOptions);
        out.close();
    }

}
