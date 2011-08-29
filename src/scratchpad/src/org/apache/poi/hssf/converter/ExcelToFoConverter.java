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
package org.apache.poi.hssf.converter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hwpf.converter.FoDocumentFacade;
import org.apache.poi.hwpf.converter.FontReplacer.Triplet;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Converts xls files (97-2007) to XSL FO.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Beta
public class ExcelToFoConverter extends AbstractExcelConverter
{
    private static final float CM_PER_INCH = 2.54f;

    private static final float DPI = 72;

    private static final POILogger logger = POILogFactory
            .getLogger( ExcelToFoConverter.class );

    private static final float PAPER_A4_HEIGHT_INCHES = 29.4f / CM_PER_INCH;

    private static final float PAPER_A4_WIDTH_INCHES = 21.0f / CM_PER_INCH;

    /**
     * Java main() interface to interact with {@link ExcelToFoConverter}
     * 
     * <p>
     * Usage: ExcelToHtmlConverter infile outfile
     * </p>
     * Where infile is an input .xls file ( Word 97-2007) which will be rendered
     * as XSL FO into outfile
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: ExcelToFoConverter <inputFile.xls> <saveTo.xml>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        try
        {
            Document doc = ExcelToHtmlConverter.process( new File( args[0] ) );

            FileWriter out = new FileWriter( args[1] );
            DOMSource domSource = new DOMSource( doc );
            StreamResult streamResult = new StreamResult( out );

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            // TODO set encoding from a command argument
            serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            serializer.setOutputProperty( OutputKeys.INDENT, "no" );
            serializer.setOutputProperty( OutputKeys.METHOD, "xml" );
            serializer.transform( domSource, streamResult );
            out.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Converts Excel file (97-2007) into XSL FO file.
     * 
     * @param xlsFile
     *            file to process
     * @return DOM representation of result XSL FO
     */
    public static Document process( File xlsFile ) throws Exception
    {
        final HSSFWorkbook workbook = ExcelToFoUtils.loadXls( xlsFile );
        ExcelToFoConverter excelToHtmlConverter = new ExcelToFoConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        excelToHtmlConverter.processWorkbook( workbook );
        return excelToHtmlConverter.getDocument();
    }

    private final FoDocumentFacade foDocumentFacade;

    public ExcelToFoConverter( Document document )
    {
        this.foDocumentFacade = new FoDocumentFacade( document );
    }

    protected String createPageMaster( HSSFSheet sheet, int maxSheetColumns,
            String pageMasterName )
    {
        final float paperHeightIn;
        final float paperWidthIn;
        {
            float requiredWidthIn = ExcelToFoUtils
                    .getColumnWidthInPx( getSheetWidth( sheet, maxSheetColumns ) )
                    / DPI + 2;

            if ( requiredWidthIn < PAPER_A4_WIDTH_INCHES )
            {
                // portrait orientation
                paperWidthIn = PAPER_A4_WIDTH_INCHES;
                paperHeightIn = PAPER_A4_HEIGHT_INCHES;
            }
            else
            {
                // landscape orientation
                paperWidthIn = requiredWidthIn;
                paperHeightIn = paperWidthIn
                        * ( PAPER_A4_WIDTH_INCHES / PAPER_A4_HEIGHT_INCHES );
            }
        }

        final float leftMargin = 1;
        final float rightMargin = 1;
        final float topMargin = 1;
        final float bottomMargin = 1;

        Element pageMaster = foDocumentFacade
                .addSimplePageMaster( pageMasterName );
        pageMaster.setAttribute( "page-height", paperHeightIn + "in" );
        pageMaster.setAttribute( "page-width", paperWidthIn + "in" );

        Element regionBody = foDocumentFacade.addRegionBody( pageMaster );
        regionBody.setAttribute( "margin", topMargin + "in " + rightMargin
                + "in " + bottomMargin + "in " + leftMargin + "in" );

        return pageMasterName;
    }

    @Override
    protected Document getDocument()
    {
        return foDocumentFacade.getDocument();
    }

    protected int getSheetWidth( HSSFSheet sheet, int maxSheetColumns )
    {
        int width = 0;
        if ( isOutputRowNumbers() )
        {
            width += sheet.getDefaultColumnWidth();
        }

        for ( int columnIndex = 0; columnIndex < maxSheetColumns; columnIndex++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( columnIndex ) )
                continue;
            width += sheet.getColumnWidth( columnIndex );
        }
        return width;
    }

    protected boolean processCell( HSSFWorkbook workbook, HSSFCell cell,
            Element tableCellElement, int normalWidthPx, int maxSpannedWidthPx,
            float normalHeightPt )
    {
        final HSSFCellStyle cellStyle = cell.getCellStyle();

        String value;
        switch ( cell.getCellType() )
        {
        case HSSFCell.CELL_TYPE_STRING:
            // XXX: enrich
            value = cell.getRichStringCellValue().getString();
            break;
        case HSSFCell.CELL_TYPE_FORMULA:
            switch ( cell.getCachedFormulaResultType() )
            {
            case HSSFCell.CELL_TYPE_STRING:
                HSSFRichTextString str = cell.getRichStringCellValue();
                if ( str != null && str.length() > 0 )
                {
                    value = ( str.toString() );
                }
                else
                {
                    value = ExcelToHtmlUtils.EMPTY;
                }
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                HSSFCellStyle style = cellStyle;
                if ( style == null )
                {
                    value = String.valueOf( cell.getNumericCellValue() );
                }
                else
                {
                    value = ( _formatter.formatRawCellContents(
                            cell.getNumericCellValue(), style.getDataFormat(),
                            style.getDataFormatString() ) );
                }
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                value = String.valueOf( cell.getBooleanCellValue() );
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                value = ErrorEval.getText( cell.getErrorCellValue() );
                break;
            default:
                logger.log(
                        POILogger.WARN,
                        "Unexpected cell cachedFormulaResultType ("
                                + cell.getCachedFormulaResultType() + ")" );
                value = ExcelToHtmlUtils.EMPTY;
                break;
            }
            break;
        case HSSFCell.CELL_TYPE_BLANK:
            value = ExcelToHtmlUtils.EMPTY;
            break;
        case HSSFCell.CELL_TYPE_NUMERIC:
            value = _formatter.formatCellValue( cell );
            break;
        case HSSFCell.CELL_TYPE_BOOLEAN:
            value = String.valueOf( cell.getBooleanCellValue() );
            break;
        case HSSFCell.CELL_TYPE_ERROR:
            value = ErrorEval.getText( cell.getErrorCellValue() );
            break;
        default:
            logger.log( POILogger.WARN,
                    "Unexpected cell type (" + cell.getCellType() + ")" );
            return true;
        }

        final boolean noText = ExcelToHtmlUtils.isEmpty( value );
        final boolean wrapInDivs = !noText && !cellStyle.getWrapText();

        final short cellStyleIndex = cellStyle.getIndex();
        if ( cellStyleIndex != 0 )
        {
            if ( noText )
            {
                /*
                 * if cell style is defined (like borders, etc.) but cell text
                 * is empty, add "&nbsp;" to output, so browser won't collapse
                 * and ignore cell
                 */
                value = "\u00A0";
            }
        }

        if ( isOutputLeadingSpacesAsNonBreaking() && value.startsWith( " " ) )
        {
            StringBuilder builder = new StringBuilder();
            for ( int c = 0; c < value.length(); c++ )
            {
                if ( value.charAt( c ) != ' ' )
                    break;
                builder.append( '\u00a0' );
            }

            if ( value.length() != builder.length() )
                builder.append( value.substring( builder.length() ) );

            value = builder.toString();
        }

        Text text = foDocumentFacade.createText( value );
        Element block = foDocumentFacade.createBlock();

        if ( wrapInDivs )
        {
            block.setAttribute( "absolute-position", "fixed" );
            block.setAttribute( "left", "0px" );
            block.setAttribute( "top", "0px" );
            block.setAttribute( "bottom", "0px" );
            block.setAttribute( "min-width", normalWidthPx + "px" );

            if ( maxSpannedWidthPx != Integer.MAX_VALUE )
            {
                block.setAttribute( "max-width", maxSpannedWidthPx + "px" );
            }

            block.setAttribute( "overflow", "hidden" );
            block.setAttribute( "height", normalHeightPt + "pt" );
            block.setAttribute( "keep-together.within-line", "always" );
        }

        processCellStyle( workbook, cell.getCellStyle(), tableCellElement,
                block );

        block.appendChild( text );
        tableCellElement.appendChild( block );

        return ExcelToHtmlUtils.isEmpty( value ) && cellStyleIndex == 0;
    }

    protected void processCellStyle( HSSFWorkbook workbook,
            HSSFCellStyle cellStyle, Element cellTarget, Element blockTarget )
    {
        blockTarget.setAttribute( "white-space-collapse", "false" );
        {
            String textAlign = ExcelToFoUtils.getAlign( cellStyle
                    .getAlignment() );
            if ( ExcelToFoUtils.isNotEmpty( textAlign ) )
                blockTarget.setAttribute( "text-align", textAlign );
        }

        if ( cellStyle.getFillPattern() == 0 )
        {
            // no fill
        }
        else if ( cellStyle.getFillPattern() == 1 )
        {
            final HSSFColor foregroundColor = cellStyle
                    .getFillForegroundColorColor();
            if ( foregroundColor != null )
                cellTarget.setAttribute( "background-color",
                        ExcelToFoUtils.getColor( foregroundColor ) );
        }
        else
        {
            final HSSFColor backgroundColor = cellStyle
                    .getFillBackgroundColorColor();
            if ( backgroundColor != null )
                cellTarget.setAttribute( "background-color",
                        ExcelToHtmlUtils.getColor( backgroundColor ) );
        }

        processCellStyleBorder( workbook, cellTarget, "top",
                cellStyle.getBorderTop(), cellStyle.getTopBorderColor() );
        processCellStyleBorder( workbook, cellTarget, "right",
                cellStyle.getBorderRight(), cellStyle.getRightBorderColor() );
        processCellStyleBorder( workbook, cellTarget, "bottom",
                cellStyle.getBorderBottom(), cellStyle.getBottomBorderColor() );
        processCellStyleBorder( workbook, cellTarget, "left",
                cellStyle.getBorderLeft(), cellStyle.getLeftBorderColor() );

        HSSFFont font = cellStyle.getFont( workbook );
        processCellStyleFont( workbook, blockTarget, font );
    }

    protected void processCellStyleBorder( HSSFWorkbook workbook,
            Element cellTarget, String type, short xlsBorder, short borderColor )
    {
        if ( xlsBorder == HSSFCellStyle.BORDER_NONE )
            return;

        StringBuilder borderStyle = new StringBuilder();
        borderStyle.append( ExcelToHtmlUtils.getBorderWidth( xlsBorder ) );
        borderStyle.append( ' ' );
        borderStyle.append( ExcelToHtmlUtils.getBorderStyle( xlsBorder ) );

        final HSSFColor color = workbook.getCustomPalette().getColor(
                borderColor );
        if ( color != null )
        {
            borderStyle.append( ' ' );
            borderStyle.append( ExcelToHtmlUtils.getColor( color ) );
        }

        cellTarget.setAttribute( "border-" + type, borderStyle.toString() );
    }

    protected void processCellStyleFont( HSSFWorkbook workbook,
            Element blockTarget, HSSFFont font )
    {
        Triplet triplet = new Triplet();
        triplet.fontName = font.getFontName();

        switch ( font.getBoldweight() )
        {
        case HSSFFont.BOLDWEIGHT_BOLD:
            triplet.bold = true;
            break;
        case HSSFFont.BOLDWEIGHT_NORMAL:
            triplet.bold = false;
            break;
        }

        if ( font.getItalic() )
        {
            triplet.italic = true;
        }

        getFontReplacer().update( triplet );
        setBlockProperties( blockTarget, triplet );

        final HSSFColor fontColor = workbook.getCustomPalette().getColor(
                font.getColor() );
        if ( fontColor != null )
            blockTarget.setAttribute( "color",
                    ExcelToHtmlUtils.getColor( fontColor ) );

        if ( font.getFontHeightInPoints() != 0 )
            blockTarget.setAttribute( "font-size", font.getFontHeightInPoints()
                    + "pt" );

    }

    protected void processColumnHeaders( HSSFSheet sheet, int maxSheetColumns,
            Element table )
    {
        Element tableHeader = foDocumentFacade.createTableHeader();
        Element row = foDocumentFacade.createTableRow();

        if ( isOutputRowNumbers() )
        {
            // empty cell at left-top corner
            final Element tableCellElement = foDocumentFacade.createTableCell();
            tableCellElement.appendChild( foDocumentFacade.createBlock() );
            row.appendChild( tableCellElement );
        }

        for ( int c = 0; c < maxSheetColumns; c++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( c ) )
                continue;

            Element cell = foDocumentFacade.createTableCell();
            Element block = foDocumentFacade.createBlock();
            block.setAttribute( "text-align", "center" );
            block.setAttribute( "font-weight", "bold" );

            String text = getColumnName( c );
            block.appendChild( foDocumentFacade.createText( text ) );

            cell.appendChild( block );
            row.appendChild( cell );
        }

        tableHeader.appendChild( row );
        table.appendChild( tableHeader );
    }

    /**
     * Creates COLGROUP element with width specified for all columns. (Except
     * first if <tt>{@link #isOutputRowNumbers()}==true</tt>)
     */
    protected void processColumnWidths( HSSFSheet sheet, int maxSheetColumns,
            Element table )
    {
        if ( isOutputRowNumbers() )
        {
            table.appendChild( foDocumentFacade.createTableColumn() );
        }

        for ( int c = 0; c < maxSheetColumns; c++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( c ) )
                continue;

            Element col = foDocumentFacade.createTableColumn();
            col.setAttribute( "column-width",
                    String.valueOf( getColumnWidth( sheet, c ) / DPI ) + "in" );
            table.appendChild( col );
        }
    }

    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( ExcelToFoUtils.isNotEmpty( summaryInformation.getTitle() ) )
            foDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( ExcelToFoUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            foDocumentFacade.setCreator( summaryInformation.getAuthor() );

        if ( ExcelToFoUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            foDocumentFacade.setKeywords( summaryInformation.getKeywords() );

        if ( ExcelToFoUtils.isNotEmpty( summaryInformation.getComments() ) )
            foDocumentFacade.setDescription( summaryInformation.getComments() );
    }

    /**
     * @return maximum 1-base index of column that were rendered, zero if none
     */
    protected int processRow( HSSFWorkbook workbook,
            CellRangeAddress[][] mergedRanges, HSSFRow row,
            Element tableRowElement )
    {
        final HSSFSheet sheet = row.getSheet();
        final short maxColIx = row.getLastCellNum();
        if ( maxColIx <= 0 )
        {
            Element emptyCellElement = foDocumentFacade.createTableCell();
            emptyCellElement.appendChild( foDocumentFacade.createBlock() );
            tableRowElement.appendChild( emptyCellElement );
            return 0;
        }

        final List<Element> emptyCells = new ArrayList<Element>( maxColIx );

        if ( isOutputRowNumbers() )
        {
            Element tableRowNumberCellElement = processRowNumber( row );
            emptyCells.add( tableRowNumberCellElement );
        }

        int maxRenderedColumn = 0;
        for ( int colIx = 0; colIx < maxColIx; colIx++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( colIx ) )
                continue;

            CellRangeAddress range = ExcelToHtmlUtils.getMergedRange(
                    mergedRanges, row.getRowNum(), colIx );

            if ( range != null
                    && ( range.getFirstColumn() != colIx || range.getFirstRow() != row
                            .getRowNum() ) )
                continue;

            HSSFCell cell = row.getCell( colIx );

            // spanning using overlapping blocks
            int divWidthPx = 0;
            {
                divWidthPx = getColumnWidth( sheet, colIx );

                boolean hasBreaks = false;
                for ( int nextColumnIndex = colIx + 1; nextColumnIndex < maxColIx; nextColumnIndex++ )
                {
                    if ( !isOutputHiddenColumns()
                            && sheet.isColumnHidden( nextColumnIndex ) )
                        continue;

                    if ( row.getCell( nextColumnIndex ) != null
                            && !isTextEmpty( row.getCell( nextColumnIndex ) ) )
                    {
                        hasBreaks = true;
                        break;
                    }

                    divWidthPx += getColumnWidth( sheet, nextColumnIndex );
                }

                if ( !hasBreaks )
                    divWidthPx = Integer.MAX_VALUE;
            }

            Element tableCellElement = foDocumentFacade.createTableCell();

            if ( range != null )
            {
                if ( range.getFirstColumn() != range.getLastColumn() )
                    tableCellElement.setAttribute(
                            "number-columns-spanned",
                            String.valueOf( range.getLastColumn()
                                    - range.getFirstColumn() + 1 ) );
                if ( range.getFirstRow() != range.getLastRow() )
                    tableCellElement.setAttribute(
                            "number-rows-spanned",
                            String.valueOf( range.getLastRow()
                                    - range.getFirstRow() + 1 ) );
            }

            boolean emptyCell;
            if ( cell != null )
            {
                emptyCell = processCell( workbook, cell, tableCellElement,
                        getColumnWidth( sheet, colIx ), divWidthPx,
                        row.getHeight() / 20f );
            }
            else
            {
                tableCellElement.appendChild( foDocumentFacade.createBlock() );
                emptyCell = true;
            }

            if ( emptyCell )
            {
                emptyCells.add( tableCellElement );
            }
            else
            {
                for ( Element emptyCellElement : emptyCells )
                {
                    tableRowElement.appendChild( emptyCellElement );
                }
                emptyCells.clear();

                tableRowElement.appendChild( tableCellElement );
                maxRenderedColumn = colIx;
            }
        }

        return maxRenderedColumn + 1;
    }

    protected Element processRowNumber( HSSFRow row )
    {
        Element tableRowNumberCellElement = foDocumentFacade.createTableCell();

        Element block = foDocumentFacade.createBlock();
        block.setAttribute( "text-align", "right" );
        block.setAttribute( "font-weight", "bold" );

        Text text = foDocumentFacade.createText( getRowName( row ) );
        block.appendChild( text );

        tableRowNumberCellElement.appendChild( block );
        return tableRowNumberCellElement;
    }

    protected int processSheet( HSSFWorkbook workbook, HSSFSheet sheet,
            Element flow )
    {
        final int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        if ( physicalNumberOfRows <= 0 )
            return 0;

        processSheetName( sheet, flow );

        Element table = foDocumentFacade.createTable();
        Element tableBody = foDocumentFacade.createTableBody();

        final CellRangeAddress[][] mergedRanges = ExcelToHtmlUtils
                .buildMergedRangesMap( sheet );

        final List<Element> emptyRowElements = new ArrayList<Element>(
                physicalNumberOfRows );
        int maxSheetColumns = 1;
        for ( int r = 0; r < physicalNumberOfRows; r++ )
        {
            HSSFRow row = sheet.getRow( r );

            if ( row == null )
                continue;

            if ( !isOutputHiddenRows() && row.getZeroHeight() )
                continue;

            Element tableRowElement = foDocumentFacade.createTableRow();
            tableRowElement.setAttribute( "height", row.getHeight() / 20f
                    + "pt" );

            int maxRowColumnNumber = processRow( workbook, mergedRanges, row,
                    tableRowElement );

            if ( maxRowColumnNumber == 0 )
            {
                emptyRowElements.add( tableRowElement );
            }
            else
            {
                if ( !emptyRowElements.isEmpty() )
                {
                    for ( Element emptyRowElement : emptyRowElements )
                    {
                        tableBody.appendChild( emptyRowElement );
                    }
                    emptyRowElements.clear();
                }

                tableBody.appendChild( tableRowElement );
            }
            maxSheetColumns = Math.max( maxSheetColumns, maxRowColumnNumber );
        }

        processColumnWidths( sheet, maxSheetColumns, table );

        if ( isOutputColumnHeaders() )
        {
            processColumnHeaders( sheet, maxSheetColumns, table );
        }

        table.appendChild( tableBody );
        flow.appendChild( table );

        return maxSheetColumns;
    }

    protected void processSheetName( HSSFSheet sheet, Element flow )
    {
        Element titleBlock = foDocumentFacade.createBlock();

        Triplet triplet = new Triplet();
        triplet.bold = true;
        triplet.italic = false;
        triplet.fontName = "Arial";
        getFontReplacer().update( triplet );

        setBlockProperties( titleBlock, triplet );
        titleBlock.setAttribute( "font-size", "200%" );

        Element titleInline = foDocumentFacade.createInline();
        titleInline.appendChild( foDocumentFacade.createText( sheet
                .getSheetName() ) );
        titleBlock.appendChild( titleInline );
        flow.appendChild( titleBlock );

        Element titleBlock2 = foDocumentFacade.createBlock();
        Element titleInline2 = foDocumentFacade.createInline();
        titleBlock2.appendChild( titleInline2 );
        flow.appendChild( titleBlock2 );
    }

    public void processWorkbook( HSSFWorkbook workbook )
    {
        final SummaryInformation summaryInformation = workbook
                .getSummaryInformation();
        if ( summaryInformation != null )
        {
            processDocumentInformation( summaryInformation );
        }

        for ( int s = 0; s < workbook.getNumberOfSheets(); s++ )
        {
            String pageMasterName = "sheet-" + s;

            Element pageSequence = foDocumentFacade
                    .createPageSequence( pageMasterName );
            Element flow = foDocumentFacade.addFlowToPageSequence(
                    pageSequence, "xsl-region-body" );

            HSSFSheet sheet = workbook.getSheetAt( s );
            int maxSheetColumns = processSheet( workbook, sheet, flow );

            if ( maxSheetColumns != 0 )
            {
                createPageMaster( sheet, maxSheetColumns, pageMasterName );
                foDocumentFacade.addPageSequence( pageSequence );
            }
        }
    }

    private void setBlockProperties( Element textBlock, Triplet triplet )
    {
        if ( triplet.bold )
            textBlock.setAttribute( "font-weight", "bold" );

        if ( triplet.italic )
            textBlock.setAttribute( "font-style", "italic" );

        if ( ExcelToFoUtils.isNotEmpty( triplet.fontName ) )
            textBlock.setAttribute( "font-family", triplet.fontName );
    }

}
