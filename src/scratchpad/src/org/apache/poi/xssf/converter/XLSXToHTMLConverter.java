
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.apache.poi.hwpf.converter.NumberFormatter;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

//Original XLS to HTML Converter license here:
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

/**
 * XLSX to HTML Converter
 * Original XLS to HTML converter written by @author Sergey Vladimirov (vlsergey {at} gmail {dot} com). Rewritten for XLSX to HTML conversion
 * 
 * Only offers basic functionality
 * Color formatting is not preserved (due to method changes from HSSF to XSSF)
 * 
 * Wrapping content into two additional DIVs not included
 * 
 * Logger not included
 * 
 * Merged methods into one class for functionality test
 * 
 * @author Sifan Ye (sye8 {at} u {dot} rochester {dot} edu)
 * 
 * @see http://svn.apache.org/repos/asf/poi/trunk/src/scratchpad/src/org/apache/poi/hssf/converter/
 *
 */
@Beta
public class XLSXToHTMLConverter{

	private final HtmlDocumentFacade htmlDocFacade;
	
    private String cssClassPrefixCell = "c";
    private String cssClassPrefixRow = "r";
    private String cssClassPrefixTable = "t";
    
    private boolean outputColumnHeaders = true;
    private boolean outputHiddenColumns = false;
    private boolean outputHiddenRows = false;
    private boolean outputLeadingSpacesAsNonBreaking = true;
    private boolean outputRowNumbers = true;
    
    private Map<Short, String> excelStyleToClass = new LinkedHashMap<Short, String>();
    
    private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
    private static final int UNIT_OFFSET_LENGTH = 7;
    
    //TODO: No XSSFDataFormatter found, test with HSSFDataFormatter
    protected final HSSFDataFormatter hssfDataFormatter = new HSSFDataFormatter();
	
    public XLSXToHTMLConverter(Document doc){
    	htmlDocFacade = new HtmlDocumentFacade(doc);
    }
    
    public XLSXToHTMLConverter(HtmlDocumentFacade htmlDocFacade){
    	this.htmlDocFacade = htmlDocFacade;
    }
    
    public boolean isOutputColumnHeaders() {
		return outputColumnHeaders;
	}

	public void setOutputColumnHeaders(boolean outputColumnHeaders) {
		this.outputColumnHeaders = outputColumnHeaders;
	}

	public boolean isOutputHiddenColumns() {
		return outputHiddenColumns;
	}

	public void setOutputHiddenColumns(boolean outputHiddenColumns) {
		this.outputHiddenColumns = outputHiddenColumns;
	}

	public boolean isOutputHiddenRows() {
		return outputHiddenRows;
	}

	public void setOutputHiddenRows(boolean outputHiddenRows) {
		this.outputHiddenRows = outputHiddenRows;
	}

	public boolean isOutputLeadingSpacesAsNonBreaking() {
		return outputLeadingSpacesAsNonBreaking;
	}

	public void setOutputLeadingSpacesAsNonBreaking(boolean outputLeadingSpacesAsNonBreaking) {
		this.outputLeadingSpacesAsNonBreaking = outputLeadingSpacesAsNonBreaking;
	}

	public boolean isOutputRowNumbers() {
		return outputRowNumbers;
	}

	public void setOutputRowNumbers(boolean outputRowNumbers) {
		this.outputRowNumbers = outputRowNumbers;
	}

    public Document getDocument(){
        return htmlDocFacade.getDocument();
    }
    
    protected String getStyleClassName(XSSFWorkbook workbook, XSSFCellStyle cellStyle){
        final Short cellStyleKey = Short.valueOf(cellStyle.getIndex());
        String knownClass = excelStyleToClass.get(cellStyleKey);
        
        if (knownClass != null){
        	return knownClass;
        }
        String cssStyle = buildStyle(workbook, cellStyle);
        String cssClass = htmlDocFacade.getOrCreateCssClass(cssClassPrefixCell, cssStyle);
        excelStyleToClass.put(cellStyleKey, cssClass);
        
        return cssClass;
    }
    
    public static String getAlign(HorizontalAlignment alignment){
        switch(alignment){
	        case CENTER:
	            return "center";
	        case CENTER_SELECTION:
	            return "center";
	        case FILL:
	            // XXX: shall we support fill?
	            return "";
	        case GENERAL:
	            return "";
	        case JUSTIFY:
	            return "justify";
	        case LEFT:
	            return "left";
	        case RIGHT:
	            return "right";
	        default:
	            return "";
        }
    }

    public static String getBorderStyle(BorderStyle xlsBorder){
        final String borderStyle;
        switch(xlsBorder){
	        case NONE:
	            borderStyle = "none";
	            break;
	        case DASH_DOT:
	        case DASH_DOT_DOT:
	        case DOTTED:
	        case HAIR:
	        case MEDIUM_DASH_DOT:
	        case MEDIUM_DASH_DOT_DOT:
	        case SLANTED_DASH_DOT:
	            borderStyle = "dotted";
	            break;
	        case DASHED:
	        case MEDIUM_DASHED:
	            borderStyle = "dashed";
	            break;
	        case DOUBLE:
	            borderStyle = "double";
	            break;
	        default:
	            borderStyle = "solid";
	            break;
	        }
        return borderStyle;
    }

    public static String getBorderWidth(BorderStyle xlsBorder){
        final String borderWidth;
        switch(xlsBorder){
	        case MEDIUM_DASH_DOT:
	        case MEDIUM_DASH_DOT_DOT:
	        case MEDIUM_DASHED:
	            borderWidth = "2pt";
	            break;
	        case THICK:
	            borderWidth = "thick";
	            break;
	        default:
	            borderWidth = "thin";
	            break;
	        }
        return borderWidth;
    }
    
    /**
     * See <a href="http://apache-poi.1045710.n5.nabble.com/Excel-Column-Width-Unit-Converter-pixels-excel-column-width-units-td2301481.html">here</a> for Xio explanation and details
     */
    public static int getColumnWidthInPx(int widthUnits){
        int pixels = (widthUnits/EXCEL_COLUMN_WIDTH_FACTOR)*UNIT_OFFSET_LENGTH;
        int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
        pixels += Math.round(offsetWidthUnits / ((float)EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));

        return pixels;
    }

    protected String buildStyle(XSSFWorkbook workbook, XSSFCellStyle cellStyle){
        StringBuilder style = new StringBuilder();

        style.append("white-space:pre-wrap;");
        appendAlign(style, cellStyle.getAlignmentEnum().getCode());
       
        buildStyle_border(workbook, style, "top", cellStyle.getBorderTopEnum(), cellStyle.getTopBorderColor());
        buildStyle_border(workbook, style, "right", cellStyle.getBorderRightEnum(), cellStyle.getRightBorderColor());
        buildStyle_border(workbook, style, "bottom", cellStyle.getBorderBottomEnum(), cellStyle.getBottomBorderColor());
        buildStyle_border( workbook, style, "left", cellStyle.getBorderLeftEnum(),cellStyle.getLeftBorderColor());

        XSSFFont font = cellStyle.getFont();
        buildStyle_font(workbook, style, font);

        return style.toString();
    }

    private void buildStyle_border(XSSFWorkbook workbook, StringBuilder style,String type, BorderStyle xlsBorder, short borderColor){
        if(xlsBorder == BorderStyle.NONE){
            return;
        }

        StringBuilder borderStyle = new StringBuilder();
        borderStyle.append(getBorderWidth(xlsBorder));
        borderStyle.append(' ');
        borderStyle.append(getBorderStyle(xlsBorder));

        style.append( "border-" + type + ":" + borderStyle + ";" );
    }

    private void buildStyle_font(XSSFWorkbook workbook, StringBuilder style, XSSFFont font){
        if(font.getBold()){
            style.append( "font-weight:bold;" );
        }
        if(font.getFontHeightInPoints() != 0){
        	style.append("font-size:" + font.getFontHeightInPoints() + "pt;");
        }
        if (font.getItalic()){
            style.append("font-style:italic;");
        }
    }
    
    private static void appendAlign(StringBuilder style, short alignment){
        String cssAlign = getAlign(HorizontalAlignment.forInt(alignment));
        if(isEmpty(cssAlign)){
        	return;
        }
        style.append("text-align:");
        style.append(cssAlign);
        style.append(";");
    }
	
    /**
     * Converts Excel file (97-2007) into HTML file.
     *
     * @param workbook
     *            workbook instance to process
     * @param keepColumnHeaders If user wish to keep column headers on output
     * @param keepRowNumbers If user wish to keep row numbers on output
     * @return DOM representation of result HTML
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document convert(XSSFWorkbook workbook, boolean keepColumnHeaders, boolean keepRowNumbers) throws IOException, ParserConfigurationException {
        XLSXToHTMLConverter xlsxToHTMLConverter = new XLSXToHTMLConverter(
                                                                          XMLHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument());
        xlsxToHTMLConverter.setOutputColumnHeaders(keepColumnHeaders);
        xlsxToHTMLConverter.setOutputRowNumbers(keepRowNumbers);
        xlsxToHTMLConverter.processWorkbook(workbook);
        Document doc = xlsxToHTMLConverter.getDocument();
        return doc;
    }

	
	protected void processWorkbook(XSSFWorkbook workbook){
	   for(int s = 0; s < workbook.getNumberOfSheets(); s++){
           XSSFSheet sheet = workbook.getSheetAt(s);
           processSheet(sheet);
	   }
	   htmlDocFacade.updateStylesheet();
    }
    
	protected void processSheet(XSSFSheet sheet){
    	processSheetHeader(htmlDocFacade.getBody(), sheet);
    	final int physicalNumOfRows = sheet.getPhysicalNumberOfRows();
    	if (physicalNumOfRows <= 0){
    		return;
    	}
    	Element table = htmlDocFacade.createTable();
        htmlDocFacade.addStyleClass(table, cssClassPrefixTable,"border-collapse:collapse;border-spacing:0;");
        Element tableBody = htmlDocFacade.createTableBody();
        final CellRangeAddress[][] mergedRanges = buildMergedRangesMap(sheet);
        final List<Element> emptyRowElements = new ArrayList<Element>(physicalNumOfRows);
        
        int maxSheetColumns = 1;
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++){
            XSSFRow row = sheet.getRow(r);
            if (row == null){
            	continue;
            }
            if (!isOutputHiddenRows() && row.getZeroHeight()){
            	continue;
            }
                
            Element tableRowElement = htmlDocFacade.createTableRow();
            htmlDocFacade.addStyleClass(tableRowElement, cssClassPrefixRow, "height:" + (row.getHeight()/20f) + "pt;");

            int maxRowColumnNumber = processRow(mergedRanges, row, tableRowElement);

            if (maxRowColumnNumber == 0){
                emptyRowElements.add(tableRowElement);
            }else{
                if (!emptyRowElements.isEmpty()){
                    for (Element emptyRowElement : emptyRowElements){
                        tableBody.appendChild(emptyRowElement);
                    }
                    emptyRowElements.clear();
                }
                tableBody.appendChild(tableRowElement);
            }
            maxSheetColumns = Math.max(maxSheetColumns, maxRowColumnNumber);
        }

        processColumnWidths(sheet, maxSheetColumns, table);
        if(isOutputColumnHeaders()){
            processColumnHeaders(sheet, maxSheetColumns, table);
        }

        table.appendChild(tableBody);

        htmlDocFacade.getBody().appendChild(table);  
    }
    

    
    /**
     * @return maximum 1-base index of column that were rendered, zero if none
     */
    protected int processRow(CellRangeAddress[][] mergedRanges, XSSFRow row, Element tableRowElement){
        final XSSFSheet sheet = row.getSheet();
        final short maxColIx = row.getLastCellNum();
        if(maxColIx <= 0){
        	return 0;
        }
            
        final List<Element> emptyCells = new ArrayList<Element>(maxColIx );

        if (isOutputRowNumbers()){
            Element tableRowNumberCellElement = htmlDocFacade.createTableHeaderCell();
            processRowNumber(row, tableRowNumberCellElement);
            emptyCells.add(tableRowNumberCellElement);
        }

        int maxRenderedColumn = 0;
        for(int colIx = 0; colIx < maxColIx; colIx++){
            if(!isOutputHiddenColumns() && sheet.isColumnHidden(colIx)){
            	continue;
            }
            CellRangeAddress range = ExcelToHtmlUtils.getMergedRange(mergedRanges, row.getRowNum(), colIx);

            if(range != null && (range.getFirstColumn() != colIx || range.getFirstRow() != row.getRowNum())){
            	 continue;
            }
               
            XSSFCell cell = row.getCell( colIx );

            Element tableCellElement = htmlDocFacade.createTableCell();

            if(range != null){
                if(range.getFirstColumn() != range.getLastColumn()){
                	tableCellElement.setAttribute("colspan", String.valueOf(range.getLastColumn() - range.getFirstColumn() + 1));
                }                  
                if(range.getFirstRow() != range.getLastRow()){
                	tableCellElement.setAttribute("rowspan", String.valueOf(range.getLastRow() - range.getFirstRow() + 1));
                }                    
            }

            boolean emptyCell;
            if(cell != null){
                emptyCell = processCell(cell, tableCellElement, getColumnWidth(sheet, colIx), 0, row.getHeight()/20f);
            }else{
                emptyCell = true;
            }

            if(emptyCell){
                emptyCells.add(tableCellElement);
            }else{
                for(Element emptyCellElement: emptyCells){
                    tableRowElement.appendChild(emptyCellElement);
                }
                emptyCells.clear();

                tableRowElement.appendChild(tableCellElement);
                maxRenderedColumn = colIx;
            }
        }

        return maxRenderedColumn + 1;
    }

    protected boolean processCell(XSSFCell cell, Element tableCellElement, int normalWidthPx, int maxSpannedWidthPx, float normalHeightPt){
        final XSSFCellStyle cellStyle = cell.getCellStyle();
        String value;
        
        switch (cell.getCellTypeEnum()){
        case STRING:
            // XXX: enrich
            value = cell.getRichStringCellValue().getString();
            break;
        case FORMULA:
            switch (cell.getCachedFormulaResultTypeEnum()){
            case STRING:
                XSSFRichTextString str = cell.getRichStringCellValue();
                if (str != null && str.length() > 0){
                    value = ( str.toString() );
                }else{
                    value = "";
                }
                break;
            case NUMERIC:
                double nValue = cell.getNumericCellValue();
                short df = cellStyle.getDataFormat();
                String dfs = cellStyle.getDataFormatString();
                value = hssfDataFormatter.formatRawCellContents(nValue, df, dfs);
                break;
            case BOOLEAN:
                value = String.valueOf( cell.getBooleanCellValue() );
                break;
            case ERROR:
                value = ErrorEval.getText( cell.getErrorCellValue() );
                break;
            default:
            	System.out.println("Unexpected cell cachedFormulaResultType (" + cell.getCachedFormulaResultTypeEnum() + ")");
                value = "";
                break;
            }
            break;
        case BLANK:
            value = "";
            break;
        case NUMERIC:
            value = hssfDataFormatter.formatCellValue( cell );
            break;
        case BOOLEAN:
            value = String.valueOf( cell.getBooleanCellValue() );
            break;
        case ERROR:
            value = ErrorEval.getText( cell.getErrorCellValue() );
            break;
        default:
            System.out.println("Unexpected cell type (" + cell.getCellTypeEnum() + ")");
            return true;
        }

        final boolean noText = isEmpty(value);

        if(cellStyle.getIndex() != 0){
            XSSFWorkbook workbook = cell.getRow().getSheet().getWorkbook();
            String mainCssClass = getStyleClassName(workbook, cellStyle);     
            tableCellElement.setAttribute("class", mainCssClass);

            if (noText){
                /*
                 * if cell style is defined (like borders, etc.) but cell text
                 * is empty, add "&nbsp;" to output, so browser won't collapse
                 * and ignore cell
                 */
                value = "\u00A0";
            }
        }

        if(isOutputLeadingSpacesAsNonBreaking() && value.startsWith(" ")){
            StringBuilder builder = new StringBuilder();
            for(int c = 0; c < value.length(); c++){
                if(value.charAt(c)!= ' '){
                	break;
                }                  
                builder.append('\u00a0');
            }

            if(value.length() != builder.length()){
            	builder.append(value.substring(builder.length()));
            }
               
            value = builder.toString();
        }

        Text text = htmlDocFacade.createText(value);    
        tableCellElement.appendChild(text);
        
        return isEmpty(value) && (cellStyle.getIndex() == 0);
    }

    
    protected void processSheetHeader(Element htmlBody, XSSFSheet sheet){
        Element h2 = htmlDocFacade.createHeader2();
        h2.appendChild(htmlDocFacade.createText(sheet.getSheetName()));
        htmlBody.appendChild(h2);
    }
    
    protected void processRowNumber(XSSFRow row, Element tableRowNumberCellElement){
        tableRowNumberCellElement.setAttribute("class", "rownumber");
        Text text = htmlDocFacade.createText(getRowName(row));
        tableRowNumberCellElement.appendChild(text);
    }
    
    protected void processColumnHeaders(XSSFSheet sheet, int maxSheetColumns, Element table){
        Element tableHeader = htmlDocFacade.createTableHeader();
        table.appendChild( tableHeader );

        Element tr = htmlDocFacade.createTableRow();

        if (isOutputRowNumbers()){
            // empty row at left-top corner
            tr.appendChild(htmlDocFacade.createTableHeaderCell());
        }

        for(int c = 0; c < maxSheetColumns; c++){
            if(!isOutputHiddenColumns() && sheet.isColumnHidden(c)){
            	continue;
            }              
            Element th = htmlDocFacade.createTableHeaderCell();
            String text = getColumnName(c);
            th.appendChild(htmlDocFacade.createText(text));
            tr.appendChild(th);
        }
        tableHeader.appendChild(tr);
    }

    /**
     * Creates COLGROUP element with width specified for all columns. (Except
     * first if <tt>{@link #isOutputRowNumbers()}==true</tt>)
     */
    protected void processColumnWidths(XSSFSheet sheet, int maxSheetColumns, Element table){
        // draw COLS after we know max column number
        Element columnGroup = htmlDocFacade.createTableColumnGroup();
        if(isOutputRowNumbers()){
            columnGroup.appendChild( htmlDocFacade.createTableColumn() );
        }
        for (int c = 0; c < maxSheetColumns; c++){
            if(!isOutputHiddenColumns() && sheet.isColumnHidden(c)){
            	continue;
            }
            Element col = htmlDocFacade.createTableColumn();
            col.setAttribute("width", String.valueOf(getColumnWidth(sheet, c)));
            columnGroup.appendChild(col);
        }
        table.appendChild(columnGroup);
    }


    
    /**
     * Creates a map (i.e. two-dimensional array) filled with ranges. Allow fast
     * retrieving {@link CellRangeAddress} of any cell, if cell is contained in
     * range.
     * 
     * @see #getMergedRange(CellRangeAddress[][], int, int)
     */
    private static CellRangeAddress[][] buildMergedRangesMap(XSSFSheet sheet){
        CellRangeAddress[][] mergedRanges = new CellRangeAddress[1][];
        for (final CellRangeAddress cellRangeAddress : sheet.getMergedRegions()){
            final int requiredHeight = cellRangeAddress.getLastRow() + 1;
            if(mergedRanges.length < requiredHeight){
                CellRangeAddress[][] newArray = new CellRangeAddress[requiredHeight][];
                System.arraycopy(mergedRanges, 0, newArray, 0, mergedRanges.length);
                mergedRanges = newArray;
            }
            for(int r = cellRangeAddress.getFirstRow(); r <= cellRangeAddress.getLastRow(); r++){
                final int requiredWidth = cellRangeAddress.getLastColumn() + 1;
                CellRangeAddress[] rowMerged = mergedRanges[r];
                if(rowMerged == null){
                    rowMerged = new CellRangeAddress[requiredWidth];
                    mergedRanges[r] = rowMerged;
                }else{
                    final int rowMergedLength = rowMerged.length;
                    if(rowMergedLength < requiredWidth){
                        final CellRangeAddress[] newRow = new CellRangeAddress[requiredWidth];
                        System.arraycopy(rowMerged, 0, newRow, 0, rowMergedLength);
                        mergedRanges[r] = newRow;
                        rowMerged = newRow;
                    }
                }
                Arrays.fill(rowMerged, cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn() + 1, cellRangeAddress);
            }
        }
        return mergedRanges;
    }
    
    /**
     * Generates name for output as column header in case
     * <tt>{@link #isOutputColumnHeaders()} == true</tt>
     * 
     * @param columnIndex
     *            0-based column index
     */
    protected String getColumnName(int columnIndex){
        return NumberFormatter.getNumber( columnIndex + 1, 3 );
    }
    
    /**
     * Generates name for output as row number in case
     * <tt>{@link #isOutputRowNumbers()} == true</tt>
     */
    protected String getRowName(XSSFRow row){
        return String.valueOf( row.getRowNum() + 1 );
    }
    
    private static int getColumnWidth(XSSFSheet sheet, int columnIndex){
    	int widthUnits = sheet.getColumnWidth(columnIndex);
        int pixels = (widthUnits/EXCEL_COLUMN_WIDTH_FACTOR)*UNIT_OFFSET_LENGTH;

        int offsetWidthUnits = widthUnits%EXCEL_COLUMN_WIDTH_FACTOR;
        pixels += Math.round(offsetWidthUnits/((float)EXCEL_COLUMN_WIDTH_FACTOR/UNIT_OFFSET_LENGTH));

        return pixels;
    }

    protected static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }

}
