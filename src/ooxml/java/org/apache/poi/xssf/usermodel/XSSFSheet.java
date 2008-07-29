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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CommentsSource;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Patriarch;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.Region;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.Control;
import org.apache.poi.xssf.model.Drawing;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBreak;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDialogsheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageBreak;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageMargins;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageSetUpPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPane;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPrintOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSelection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetFormatPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetView;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetViews;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPane;


public class XSSFSheet implements Sheet {
    protected CTSheet sheet;
    protected CTWorksheet worksheet;
    protected CTDialogsheet dialogsheet;
    protected List<Row> rows;
    protected List<XSSFHyperlink> hyperlinks;
    protected ColumnHelper columnHelper;
    protected XSSFWorkbook workbook;
    protected CommentsSource sheetComments;
    protected CTMergeCells ctMergeCells;
    protected ArrayList<Drawing> drawings;
    protected ArrayList<Control> controls;

    public static final short LeftMargin = 0;
    public static final short RightMargin = 1;
    public static final short TopMargin = 2;
    public static final short BottomMargin = 3;
    public static final short HeaderMargin = 4;
    public static final short FooterMargin = 5;

	public XSSFSheet(CTSheet sheet, CTWorksheet worksheet, XSSFWorkbook workbook, CommentsSource sheetComments, ArrayList<Drawing> drawings, ArrayList<Control> controls) {
		this(sheet, worksheet, workbook, sheetComments);
		this.drawings = drawings;
		this.controls = controls;
	}
	
	public ArrayList<Drawing> getDrawings()
	{
		return drawings;
	}
	
	public ArrayList<Control> getControls()
	{
		return controls;
	}
	
	public XSSFSheet(CTSheet sheet, CTWorksheet worksheet, XSSFWorkbook workbook, CommentsSource sheetComments) {
		this(sheet, worksheet, workbook);
		this.sheetComments = sheetComments;
	}

	public XSSFSheet(CTSheet sheet, CTWorksheet worksheet, XSSFWorkbook workbook) {
        this.workbook = workbook;
        this.sheet = sheet;
        this.worksheet = worksheet;
        if (this.worksheet == null) {
        	this.worksheet = CTWorksheet.Factory.newInstance();
        }
        if (this.worksheet.getSheetData() == null) {
        	this.worksheet.addNewSheetData();
        }
        initRows(this.worksheet);
        initColumns(this.worksheet);
        
    	hyperlinks = new ArrayList<XSSFHyperlink>();
	}

    public XSSFSheet(XSSFWorkbook workbook) {
        this.workbook = workbook;
        
        hyperlinks = new ArrayList<XSSFHyperlink>();
    }

    public XSSFWorkbook getWorkbook() {
        return this.workbook;
    }

    /**
     * Tweaks the CTWorksheet to fit with what Excel
     *  will accept without a massive huff, and write into
     *  the OutputStream supplied.
     */
    protected void save(PackagePart sheetPart, XmlOptions xmlOptions) throws IOException {
    	// Excel objects to <cols/>
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
	    		hyperlink.generateRelationIfNeeded(sheetPart);
	    		// Now grab their underling object
	    		ctHls[i] = hyperlink.getCTHyperlink();
	    	}
	    	worksheet.getHyperlinks().setHyperlinkArray(ctHls);
    	}

    	// Save
    	OutputStream out = sheetPart.getOutputStream();
        worksheet.save(out, xmlOptions);
        out.close();
    }
    
    protected CTWorksheet getWorksheet() {
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
    
    public int addMergedRegion(Region region) {
    	addNewMergeCell(region);
    	return ctMergeCells.sizeOfMergeCellArray();
    }

    public void autoSizeColumn(short column) {
    	columnHelper.setColBestFit(column, true);
    }

    public Patriarch createDrawingPatriarch() {
        // TODO Auto-generated method stub
        return null;
    }

    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
    	this.createFreezePane(colSplit, rowSplit);
    	this.showInPane((short)topRow, (short)leftmostColumn);
    }

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
    public Comment createComment() {
    	return getComments().addComment();
    }

    protected XSSFRow addRow(int index, int rownum) {
        CTRow row = this.worksheet.getSheetData().insertNewRow(index);
        XSSFRow xrow = new XSSFRow(row, this);
        xrow.setRowNum(rownum);
        return xrow;
    }

    public Row createRow(int rownum) {
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

    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
    	createFreezePane(xSplitPos, ySplitPos, leftmostColumn, topRow);
    	getPane().setActivePane(STPane.Enum.forInt(activePane));
    }

    public void dumpDrawingRecords(boolean fat) {
        // TODO Auto-generated method stub

    }

    public boolean getAlternateExpression() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getAlternateFormula() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getAutobreaks() {
        return getSheetTypePageSetUpPr().getAutoPageBreaks();
    }

    public Comment getCellComment(int row, int column) {
    	return getComments().findCellComment(row, column);
    }
    
    public Hyperlink getHyperlink(int row, int column) {
    	String ref = new CellReference(row, column).formatAsString();
    	for(XSSFHyperlink hyperlink : hyperlinks) {
    		if(hyperlink.getCellRef().equals(ref)) {
    			return hyperlink;
    		}
    	}
    	return null;
    }

    public short[] getColumnBreaks() {
        CTBreak[] brkArray = getSheetTypeColumnBreaks().getBrkArray();
        if (brkArray.length == 0) {
            return null;
        }
        short[] breaks = new short[brkArray.length];
        for (int i = 0 ; i < brkArray.length ; i++) {
            CTBreak brk = brkArray[i];
            breaks[i] = (short) brk.getId();
        }
        return breaks;
    }

	protected CTPageBreak getSheetTypeColumnBreaks() {
		if (worksheet.getColBreaks() == null) {
			worksheet.setColBreaks(CTPageBreak.Factory.newInstance());
		}
		return worksheet.getColBreaks();
	}

    public short getColumnWidth(short column) {
        return (short) columnHelper.getColumn(column, false).getWidth();
    }

    public short getDefaultColumnWidth() {
        return (short) getSheetTypeSheetFormatPr().getDefaultColWidth();
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

    public boolean getDisplayGuts() {
        // TODO Auto-generated method stub
        return false;
    }

    public int getFirstRowNum() {
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row row = it.next();
            if (row != null) {
                return row.getRowNum();
            }
        }
        return -1;
    }

    public boolean getFitToPage() {
        return getSheetTypePageSetUpPr().getFitToPage();
    }

    public Footer getFooter() {
        return getOddFooter();
    }
    
    public Footer getOddFooter() {
        return new XSSFOddFooter(getSheetTypeHeaderFooter());
    }

	protected CTHeaderFooter getSheetTypeHeaderFooter() {
		if (worksheet.getHeaderFooter() == null) {
			worksheet.setHeaderFooter(CTHeaderFooter.Factory.newInstance());
		}
		return worksheet.getHeaderFooter();
	}
    
    public Footer getEvenFooter() {
        return new XSSFEvenFooter(getSheetTypeHeaderFooter());
    }
    
    public Footer getFirstFooter() {
        return new XSSFFirstFooter(getSheetTypeHeaderFooter());
    }

    public Header getHeader() {
        return getOddHeader();
    }
    
    public Header getOddHeader() {
        return new XSSFOddHeader(getSheetTypeHeaderFooter());
    }
    
    public Header getEvenHeader() {
        return new XSSFEvenHeader(getSheetTypeHeaderFooter()
);
    }
    
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
            throw new RuntimeException( "Unknown margin constant:  " + margin );
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

    public PrintSetup getPrintSetup() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getProtect() {
        // TODO Auto-generated method stub
        return false;
    }

    public Row getRow(int rownum) {
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
                Row row = it.next();
                if (row.getRowNum() == rownum) {
                        return row;
                }
        }
        return null;
    }

    public int[] getRowBreaks() {
        CTPageBreak rowBreaks = getSheetTypeRowBreaks();
        int breaksCount = rowBreaks.getBrkArray().length;
        if (breaksCount == 0) {
            return null;
        }
        int[] breaks = new int[breaksCount];
        for (int i = 0 ; i < breaksCount ; i++) {
            CTBreak brk = rowBreaks.getBrkArray(i);
            breaks[i] = (int) brk.getId();
        }
        return breaks;
    }

	protected CTPageBreak getSheetTypeRowBreaks() {
		if (worksheet.getRowBreaks() == null) {
			worksheet.setRowBreaks(CTPageBreak.Factory.newInstance());
		}
		return worksheet.getRowBreaks();
	}

    public boolean getRowSumsBelow() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getRowSumsRight() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getScenarioProtect() {
    	return getSheetTypeProtection().getScenarios();
    }

	protected CTSheetProtection getSheetTypeProtection() {
		if (worksheet.getSheetProtection() == null) {
			worksheet.setSheetProtection(CTSheetProtection.Factory.newInstance());
		}
		return worksheet.getSheetProtection();
	}

    public short getTopRow() {
    	String cellRef = getSheetTypeSheetView().getTopLeftCell();
    	CellReference cellReference = new CellReference(cellRef);
        return (short) cellReference.getRow();
    }

    // Right signature method. Remove the wrong one when it will be removed in HSSFSheet (and interface)
    public boolean getVerticallyCenter() {
    	return getVerticallyCenter(true);
    }

    public boolean getVerticallyCenter(boolean value) {
    	return getSheetTypePrintOptions().getVerticalCentered();
    }

    public void groupColumn(short fromColumn, short toColumn) {
        // TODO Auto-generated method stub

    }

    public void groupRow(int fromRow, int toRow) {
        // TODO Auto-generated method stub

    }

    public boolean isColumnBroken(short column) {
        CTBreak[] brkArray = getSheetTypeColumnBreaks().getBrkArray();
        for (int i = 0 ; i < brkArray.length ; i++) {
            if (brkArray[i].getId() == column) {
                return true;
            }
        }
        return false;
    }

    public boolean isColumnHidden(short column) {
        return columnHelper.getColumn(column, false).getHidden();
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

    public boolean isRowBroken(int row) {
        int[] rowBreaks = getRowBreaks();
        if (rowBreaks == null) {
            return false;
        }
        for (int i = 0 ; i < rowBreaks.length ; i++) {
            if (rowBreaks[i] == row) {
                return true;
            }
        }
        return false;
    }

    public void protectSheet(String password) {
        // TODO Auto-generated method stub

    }

    public void removeColumnBreak(short column) {
        CTBreak[] brkArray = getSheetTypeColumnBreaks().getBrkArray();
        for (int i = 0 ; i < brkArray.length ; i++) {
            if (brkArray[i].getId() == column) {
                getSheetTypeColumnBreaks().removeBrk(i);
                continue;
            }
        }
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
        for (Iterator<Row> it = rowIterator() ; it.hasNext() ; ) {
            Row r = it.next();
            if (r.getRowNum() == row.getRowNum()) {
                it.remove();
                worksheet.getSheetData().removeRow(counter);
            }
            counter++;
        }
    }

    public void removeRowBreak(int row) {
        CTBreak[] brkArray = getSheetTypeRowBreaks().getBrkArray();
        for (int i = 0 ; i < brkArray.length ; i++) {
            if (brkArray[i].getId() == row) {
                getSheetTypeRowBreaks().removeBrk(i);
                continue;
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

    public void setAutobreaks(boolean b) {
        getSheetTypePageSetUpPr().setAutoPageBreaks(b);
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

    public void setColumnHidden(short column, boolean hidden) {
        columnHelper.setColHidden(column, hidden);
    }

    public void setColumnWidth(short column, short width) {
        columnHelper.setColWidth(column, width);
    }

    public void setDefaultColumnStyle(short column, CellStyle style) {
    	columnHelper.setColDefaultStyle(column, style);
    }

    public void setDefaultColumnWidth(short width) {
        getSheetTypeSheetFormatPr().setDefaultColWidth((double) width);
    }

    public void setDefaultRowHeight(short height) {
        getSheetTypeSheetFormatPr().setDefaultRowHeight(height / 20);

    }

    public void setDefaultRowHeightInPoints(float height) {
        getSheetTypeSheetFormatPr().setDefaultRowHeight(height);

    }

    public void setDialog(boolean b) {
        // TODO Auto-generated method stub
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

    public void setDisplayGuts(boolean b) {
        // TODO Auto-generated method stub

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

    public void setProtect(boolean protect) {
        // TODO Auto-generated method stub

    }

    public void setRowBreak(int row) {
        CTPageBreak pageBreak = getSheetTypeRowBreaks();
        if (! isRowBroken(row)) {
            CTBreak brk = pageBreak.addNewBrk();
            brk.setId(row);
        }
    }

    public void setRowGroupCollapsed(int row, boolean collapse) {
        // TODO Auto-generated method stub

    }

    public void setRowSumsBelow(boolean b) {
        // TODO Auto-generated method stub

    }

    public void setRowSumsRight(boolean b) {
        // TODO Auto-generated method stub

    }

    public void setVerticallyCenter(boolean value) {
    	getSheetTypePrintOptions().setVerticalCentered(value);
    }

    // HSSFSheet compatibility methods. See also the following zoom related methods
    public void setZoom(int numerator, int denominator) {
    	setZoom((numerator/denominator) * 100);
    }

    public void setZoom(long scale) {
    	getSheetTypeSheetView().setZoomScale(scale);
    }

    public void setZoomNormal(long scale) {
    	getSheetTypeSheetView().setZoomScaleNormal(scale);
    }

    public void setZoomPageLayoutView(long scale) {
    	getSheetTypeSheetView().setZoomScalePageLayoutView(scale);
    }

    public void setZoomSheetLayoutView(long scale) {
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

    public void showInPane(short toprow, short leftcol) {
    	CellReference cellReference = new CellReference(toprow, leftcol);
    	String cellRef = cellReference.formatAsString();
    	getSheetTypeSheetView().setTopLeftCell(cellRef);
    }

    public void ungroupColumn(short fromColumn, short toColumn) {
        // TODO Auto-generated method stub

    }

    public void ungroupRow(int fromRow, int toRow) {
        // TODO Auto-generated method stub

    }

    public void setSelected(boolean flag) {
        CTSheetViews views = getSheetTypeSheetViews();
        for (CTSheetView view : views.getSheetViewArray()) {
            view.setTabSelected(flag);
        }
    }

	protected CTSheetViews getSheetTypeSheetViews() {
		if (worksheet.getSheetViews() == null) {
			worksheet.setSheetViews(CTSheetViews.Factory.newInstance());
			worksheet.getSheetViews().addNewSheetView();
		}
		return worksheet.getSheetViews();
	}
    
    public boolean isSelected() {
        CTSheetView view = getDefaultSheetView();
        return view != null && view.getTabSelected();
    }
    
    public void setCellComment(String cellRef, XSSFComment comment) {
		CellReference cellReference = new CellReference(cellRef);
    	
		comment.setRow(cellReference.getRow());
		comment.setColumn((short)cellReference.getCol());
    }
    
    public void setCellHyperlink(XSSFHyperlink hyperlink) {
    	hyperlinks.add(hyperlink);
    }
    
    public String getActiveCell() {
    	return getSheetTypeSelection().getActiveCell();
    }

	public void setActiveCell(String cellRef) {
		getSheetTypeSelection().setActiveCell(cellRef);
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
    	XSSFSheet newSheet = new XSSFSheet(this.workbook);
    	newSheet.setSheet((CTSheet)sheet.copy());
        return newSheet;
    }
    
	private void setSheet(CTSheet sheet) {
		this.sheet = sheet;
	}
	
	private CommentsSource getComments() {
		if (sheetComments == null) {
			sheetComments = new CommentsTable();
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
    	if (getSheetTypeSheetPr().getPageSetUpPr() == null) {
    		getSheetTypeSheetPr().setPageSetUpPr(CTPageSetUpPr.Factory.newInstance());
    	}
		return getSheetTypeSheetPr().getPageSetUpPr();
	}

	protected CTSheetPr getSheetTypeSheetPr() {
    	if (worksheet.getSheetPr() == null) {
    		worksheet.setSheetPr(CTSheetPr.Factory.newInstance());
    	}
		return worksheet.getSheetPr();
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
}
