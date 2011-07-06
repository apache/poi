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
package org.apache.poi.hssf.usermodel.converter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Converts xls files (97-2007) to HTML file.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class ExcelToHtmlConverter
{

    private static final POILogger logger = POILogFactory
            .getLogger( ExcelToHtmlConverter.class );

    /**
     * Java main() interface to interact with {@link ExcelToHtmlConverter}
     * 
     * <p>
     * Usage: ExcelToHtmlConverter infile outfile
     * </p>
     * Where infile is an input .xls file ( Word 97-2007) which will be rendered
     * as HTML into outfile
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: ExcelToHtmlConverter <inputFile.doc> <saveTo.html>" );
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
            serializer.setOutputProperty( OutputKeys.INDENT, "yes" );
            serializer.setOutputProperty( OutputKeys.METHOD, "html" );
            serializer.transform( domSource, streamResult );
            out.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Converts Excel file (97-2007) into HTML file.
     * 
     * @param xlsFile
     *            file to process
     * @return DOM representation of result HTML
     */
    public static Document process( File xlsFile ) throws Exception
    {
        final HSSFWorkbook workbook = ExcelToHtmlUtils.loadXls( xlsFile );
        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        excelToHtmlConverter.processWorkbook( workbook );
        return excelToHtmlConverter.getDocument();
    }

    private final HSSFDataFormatter _formatter = new HSSFDataFormatter();

    private final HtmlDocumentFacade htmlDocumentFacade;

    private final Element styles;

    private final Set<Short> usedStyles = new LinkedHashSet<Short>();

    public ExcelToHtmlConverter( Document doc )
    {
        htmlDocumentFacade = new HtmlDocumentFacade( doc );

        styles = doc.createElement( "style" );
        styles.setAttribute( "type", "text/css" );
        htmlDocumentFacade.getHead().appendChild( styles );
    }

    private String buildStyle( HSSFWorkbook workbook, HSSFCellStyle cellStyle )
    {
        StringBuilder style = new StringBuilder();

        style.append( "white-space: pre-wrap; " );

        switch ( cellStyle.getAlignment() )
        {
        case HSSFCellStyle.ALIGN_CENTER:
            style.append( "text-align: center; " );
            break;
        case HSSFCellStyle.ALIGN_CENTER_SELECTION:
            style.append( "text-align: center; " );
            break;
        case HSSFCellStyle.ALIGN_FILL:
            // XXX: shall we support fill?
            break;
        case HSSFCellStyle.ALIGN_GENERAL:
            break;
        case HSSFCellStyle.ALIGN_JUSTIFY:
            style.append( "text-align: justify; " );
            break;
        case HSSFCellStyle.ALIGN_LEFT:
            style.append( "text-align: left; " );
            break;
        case HSSFCellStyle.ALIGN_RIGHT:
            style.append( "text-align: right; " );
            break;
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
                style.append( "background-color: "
                        + ExcelToHtmlUtils.getColor( foregroundColor ) + "; " );
        }
        else
        {
            final HSSFColor backgroundColor = cellStyle
                    .getFillBackgroundColorColor();
            if ( backgroundColor != null )
                style.append( "background-color: "
                        + ExcelToHtmlUtils.getColor( backgroundColor ) + "; " );
        }

        buildStyle_border( workbook, style, "top", cellStyle.getBorderTop(),
                cellStyle.getTopBorderColor() );
        buildStyle_border( workbook, style, "right",
                cellStyle.getBorderRight(), cellStyle.getRightBorderColor() );
        buildStyle_border( workbook, style, "bottom",
                cellStyle.getBorderBottom(), cellStyle.getBottomBorderColor() );
        buildStyle_border( workbook, style, "left", cellStyle.getBorderLeft(),
                cellStyle.getLeftBorderColor() );

        HSSFFont font = cellStyle.getFont( workbook );
        buildStyle_font( workbook, style, font );

        return style.toString();
    }

    private void buildStyle_border( HSSFWorkbook workbook, StringBuilder style,
            String type, short xlsBorder, short borderColor )
    {
        style.append( type + "-border-style: "
                + ExcelToHtmlUtils.getBorderStyle( xlsBorder ) + "; " );

        if ( xlsBorder == HSSFCellStyle.BORDER_NONE )
            return;

        style.append( type + "-border-width: "
                + ExcelToHtmlUtils.getBorderWidth( xlsBorder ) + "; " );

        final HSSFColor color = workbook.getCustomPalette().getColor(
                borderColor );
        if ( color != null )
            style.append( type + "-border-color: "
                    + ExcelToHtmlUtils.getColor( color ) + "; " );
    }

    void buildStyle_font( HSSFWorkbook workbook, StringBuilder style,
            HSSFFont font )
    {
        switch ( font.getBoldweight() )
        {
        case HSSFFont.BOLDWEIGHT_BOLD:
            style.append( "font-weight: bold; " );
            break;
        case HSSFFont.BOLDWEIGHT_NORMAL:
            style.append( "font-weight: normal; " );
            break;
        }

        final HSSFColor fontColor = workbook.getCustomPalette().getColor(
                font.getColor() );
        if ( fontColor != null )
            style.append( "color: " + ExcelToHtmlUtils.getColor( fontColor )
                    + "; " );

        if ( font.getFontHeightInPoints() != 0 )
            style.append( "font-size: " + font.getFontHeightInPoints() + "pt; " );

        if ( font.getItalic() )
        {
            style.append( "font-style: italic; " );
        }
    }

    public Document getDocument()
    {
        return htmlDocumentFacade.getDocument();
    }

    protected boolean processCell( HSSFCell cell, Element tableCellElement )
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

        final short cellStyleIndex = cellStyle.getIndex();
        if ( cellStyleIndex != 0 )
        {
            tableCellElement.setAttribute( "class", "cellstyle_"
                    + cellStyleIndex );
            usedStyles.add( Short.valueOf( cellStyleIndex ) );
            if ( ExcelToHtmlUtils.isEmpty( value ) )
            {
                /*
                 * if cell style is defined (like borders, etc.) but cell text
                 * is empty, add "&nbsp;" to output, so browser won't collapse
                 * and ignore cell
                 */
                value = "\u00A0";
            }
        }

        Text text = htmlDocumentFacade.createText( value );
        tableCellElement.appendChild( text );

        return ExcelToHtmlUtils.isEmpty( value ) && cellStyleIndex == 0;
    }

    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( ExcelToHtmlUtils.isNotEmpty( summaryInformation.getTitle() ) )
            htmlDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( ExcelToHtmlUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            htmlDocumentFacade.addAuthor( summaryInformation.getAuthor() );

        if ( ExcelToHtmlUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            htmlDocumentFacade.addKeywords( summaryInformation.getKeywords() );

        if ( ExcelToHtmlUtils.isNotEmpty( summaryInformation.getComments() ) )
            htmlDocumentFacade
                    .addDescription( summaryInformation.getComments() );
    }

    protected boolean processRow( HSSFRow row, Element tableRowElement )
    {
        boolean emptyRow = true;

        final short maxColIx = row.getLastCellNum();
        if ( maxColIx <= 0 )
            return true;

        final List<Element> emptyCells = new ArrayList<Element>( maxColIx );

        for ( int colIx = 0; colIx < maxColIx; colIx++ )
        {
            HSSFCell cell = row.getCell( colIx );

            Element tableCellElement = htmlDocumentFacade.createTableCell();

            boolean emptyCell;
            if ( cell != null )
            {
                emptyCell = processCell( cell, tableCellElement );
            }
            else
            {
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
                emptyRow = false;
            }
        }

        return emptyRow;
    }

    protected void processSheet( HSSFSheet sheet )
    {
        Element h1 = htmlDocumentFacade.createHeader1();
        h1.appendChild( htmlDocumentFacade.createText( sheet.getSheetName() ) );
        htmlDocumentFacade.getBody().appendChild( h1 );

        final int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        if ( physicalNumberOfRows <= 0 )
            return;

        Element table = htmlDocumentFacade.createTable();
        Element tableBody = htmlDocumentFacade.createTableBody();

        final List<Element> emptyRowElements = new ArrayList<Element>(
                physicalNumberOfRows );

        for ( int r = 0; r < physicalNumberOfRows; r++ )
        {
            HSSFRow row = sheet.getRow( r );

            Element tableRowElement = htmlDocumentFacade.createTableRow();

            boolean emptyRow;
            if ( row != null )
            {
                emptyRow = processRow( row, tableRowElement );
            }
            else
            {
                emptyRow = true;
            }

            if ( emptyRow )
            {
                emptyRowElements.add( tableRowElement );
            }
            else
            {
                if ( !emptyRowElements.isEmpty() )
                {
                    for ( Element emptyCellElement : emptyRowElements )
                    {
                        tableBody.appendChild( emptyCellElement );
                    }
                    emptyRowElements.clear();
                }

                tableBody.appendChild( tableRowElement );
                emptyRow = false;
            }
        }

        table.appendChild( tableBody );
        htmlDocumentFacade.getBody().appendChild( table );
    }

    public void processWorkbook( HSSFWorkbook workbook )
    {
        final SummaryInformation summaryInformation = workbook
                .getSummaryInformation();
        if ( summaryInformation != null )
        {
            processDocumentInformation( summaryInformation );
        }

        for ( short i = 0; i < workbook.getNumCellStyles(); i++ )
        {
            HSSFCellStyle cellStyle = workbook.getCellStyleAt( i );

            if ( cellStyle == null )
                continue;

            if ( usedStyles.contains( Short.valueOf( i ) ) )
                styles.appendChild( htmlDocumentFacade
                        .createText( "td.cellstyle_" + i + "{"
                                + buildStyle( workbook, cellStyle ) + "}\n" ) );
        }

        for ( int s = 0; s < workbook.getNumberOfSheets(); s++ )
        {
            HSSFSheet sheet = workbook.getSheetAt( s );
            processSheet( sheet );
        }
    }
}
