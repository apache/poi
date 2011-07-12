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
package org.apache.poi.hwpf.converter;

import java.io.File;
import java.io.FileWriter;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.converter.FontReplacer.Triplet;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import static org.apache.poi.hwpf.converter.AbstractWordUtils.TWIPS_PER_INCH;

/**
 * Converts Word files (95-2007) into HTML files.
 * <p>
 * This implementation doesn't create images or links to them. This can be
 * changed by overriding {@link #processImage(Element, boolean, Picture)}
 * method.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class WordToHtmlConverter extends AbstractWordConverter
{

    /**
     * Holds properties values, applied to current <tt>p</tt> element. Those
     * properties shall not be doubled in children <tt>span</tt> elements.
     */
    private static class BlockProperies
    {
        final String pFontName;
        final int pFontSize;

        public BlockProperies( String pFontName, int pFontSize )
        {
            this.pFontName = pFontName;
            this.pFontSize = pFontSize;
        }
    }

    private static final POILogger logger = POILogFactory
            .getLogger( WordToHtmlConverter.class );

    private static String getSectionStyle( Section section )
    {
        SectionProperties sep = WordToHtmlUtils.getSectionProperties( section );

        float leftMargin = sep.getDxaLeft() / TWIPS_PER_INCH;
        float rightMargin = sep.getDxaRight() / TWIPS_PER_INCH;
        float topMargin = sep.getDyaTop() / TWIPS_PER_INCH;
        float bottomMargin = sep.getDyaBottom() / TWIPS_PER_INCH;

        String style = "margin: " + topMargin + "in " + rightMargin + "in "
                + bottomMargin + "in " + leftMargin + "in; ";

        if ( sep.getCcolM1() > 0 )
        {
            style += "column-count: " + ( sep.getCcolM1() + 1 ) + "; ";
            if ( sep.getFEvenlySpaced() )
            {
                style += "column-gap: "
                        + ( sep.getDxaColumns() / TWIPS_PER_INCH ) + "in; ";
            }
            else
            {
                style += "column-gap: 0.25in; ";
            }
        }
        return style;
    }

    /**
     * Java main() interface to interact with {@link WordToHtmlConverter}
     * 
     * <p>
     * Usage: WordToHtmlConverter infile outfile
     * </p>
     * Where infile is an input .doc file ( Word 95-2007) which will be rendered
     * as HTML into outfile
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: WordToHtmlConverter <inputFile.doc> <saveTo.html>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        try
        {
            Document doc = WordToHtmlConverter.process( new File( args[0] ) );

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

    static Document process( File docFile ) throws Exception
    {
        final HWPFDocumentCore wordDocument = WordToHtmlUtils.loadDoc( docFile );
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToHtmlConverter.processDocument( wordDocument );
        return wordToHtmlConverter.getDocument();
    }

    private final Stack<BlockProperies> blocksProperies = new Stack<BlockProperies>();

    private final HtmlDocumentFacade htmlDocumentFacade;

    /**
     * Creates new instance of {@link WordToHtmlConverter}. Can be used for
     * output several {@link HWPFDocument}s into single HTML document.
     * 
     * @param document
     *            XML DOM Document used as HTML document
     */
    public WordToHtmlConverter( Document document )
    {
        this.htmlDocumentFacade = new HtmlDocumentFacade( document );
    }

    public Document getDocument()
    {
        return htmlDocumentFacade.getDocument();
    }

    @Override
    protected void outputCharacters( Element pElement,
            CharacterRun characterRun, String text )
    {
        Element span = htmlDocumentFacade.document.createElement( "span" );
        pElement.appendChild( span );

        StringBuilder style = new StringBuilder();
        BlockProperies blockProperies = this.blocksProperies.peek();
        Triplet triplet = getCharacterRunTriplet( characterRun );

        if ( WordToHtmlUtils.isNotEmpty( triplet.fontName )
                && !WordToHtmlUtils.equals( triplet.fontName,
                        blockProperies.pFontName ) )
        {
            style.append( "font-family: " + triplet.fontName + "; " );
        }
        if ( characterRun.getFontSize() / 2 != blockProperies.pFontSize )
        {
            style.append( "font-size: " + characterRun.getFontSize() / 2 + "; " );
        }
        if ( triplet.bold )
        {
            style.append( "font-weight: bold; " );
        }
        if ( triplet.italic )
        {
            style.append( "font-style: italic; " );
        }

        WordToHtmlUtils.addCharactersProperties( characterRun, style );
        if ( style.length() != 0 )
            span.setAttribute( "style", style.toString() );

        Text textNode = htmlDocumentFacade.createText( text );
        span.appendChild( textNode );
    }

    @Override
    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getTitle() ) )
            htmlDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            htmlDocumentFacade.addAuthor( summaryInformation.getAuthor() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            htmlDocumentFacade.addKeywords( summaryInformation.getKeywords() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getComments() ) )
            htmlDocumentFacade
                    .addDescription( summaryInformation.getComments() );
    }

    @Override
    protected void processHyperlink( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String hyperlink )
    {
        Element basicLink = htmlDocumentFacade.createHyperlink( hyperlink );
        currentBlock.appendChild( basicLink );

        if ( textRange != null )
            processCharacters( wordDocument, currentTableLevel, textRange,
                    basicLink );
    }

    @Override
    protected void processLineBreak( Element block, CharacterRun characterRun )
    {
        block.appendChild( htmlDocumentFacade.createLineBreak() );
    }

    /**
     * This method shall store image bytes in external file and convert it if
     * necessary. Images shall be stored using PNG format. Other formats may be
     * not supported by user browser.
     * <p>
     * Please note the
     * {@link WordToHtmlUtils#setPictureProperties(Picture, Element)} method.
     * 
     * @param currentBlock
     *            currently processed HTML element, like <tt>p</tt>. Shall be
     *            used as parent of newly created <tt>img</tt>
     * @param inlined
     *            if image is inlined
     * @param picture
     *            HWPF object, contained picture data and properties
     */
    protected void processImage( Element currentBlock, boolean inlined,
            Picture picture )
    {
        // no default implementation -- skip
        currentBlock.appendChild( htmlDocumentFacade.document
                .createComment( "Image link to '"
                        + picture.suggestFullFileName() + "' can be here" ) );
    }

    protected void processPageref( HWPFDocumentCore hwpfDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref )
    {
        Element basicLink = htmlDocumentFacade.createHyperlink( "#" + pageref );
        currentBlock.appendChild( basicLink );

        if ( textRange != null )
            processCharacters( hwpfDocument, currentTableLevel, textRange,
                    basicLink );
    }

    protected void processParagraph( HWPFDocumentCore hwpfDocument,
            Element parentFopElement, int currentTableLevel,
            Paragraph paragraph, String bulletText )
    {
        final Element pElement = htmlDocumentFacade.createParagraph();
        parentFopElement.appendChild( pElement );

        StringBuilder style = new StringBuilder();
        WordToHtmlUtils.addParagraphProperties( paragraph, style );

        final int charRuns = paragraph.numCharacterRuns();

        if ( charRuns == 0 )
        {
            return;
        }

        {
            final String pFontName;
            final int pFontSize;
            final CharacterRun characterRun = paragraph.getCharacterRun( 0 );
            if ( characterRun != null )
            {
                Triplet triplet = getCharacterRunTriplet(characterRun);
                pFontSize = characterRun.getFontSize() / 2;
                pFontName = triplet.fontName;
                WordToHtmlUtils.addFontFamily( pFontName, style );
                WordToHtmlUtils.addFontSize( pFontSize, style );
            }
            else
            {
                pFontSize = -1;
                pFontName = WordToHtmlUtils.EMPTY;
            }
            blocksProperies.push( new BlockProperies( pFontName, pFontSize ) );
        }
        try
        {
            if ( WordToHtmlUtils.isNotEmpty( bulletText ) )
            {
                Text textNode = htmlDocumentFacade.createText( bulletText );
                pElement.appendChild( textNode );
            }

            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    pElement );
        }
        finally
        {
            blocksProperies.pop();
        }

        if ( style.length() > 0 )
            pElement.setAttribute( "style", style.toString() );

        return;
    }

    protected void processSection( HWPFDocumentCore wordDocument,
            Section section, int sectionCounter )
    {
        Element div = htmlDocumentFacade.document.createElement( "div" );
        div.setAttribute( "style", getSectionStyle( section ) );
        htmlDocumentFacade.body.appendChild( div );

        processSectionParagraphes( wordDocument, div, section,
                Integer.MIN_VALUE );
    }

    @Override
    protected void processSingleSection( HWPFDocumentCore wordDocument,
            Section section )
    {
        htmlDocumentFacade.body.setAttribute( "style",
                getSectionStyle( section ) );

        processSectionParagraphes( wordDocument, htmlDocumentFacade.body,
                section, Integer.MIN_VALUE );
    }

    protected void processTable( HWPFDocumentCore hwpfDocument, Element flow,
            Table table )
    {
        Element tableHeader = htmlDocumentFacade.createTableHeader();
        Element tableBody = htmlDocumentFacade.createTableBody();

        final int tableRows = table.numRows();

        int maxColumns = Integer.MIN_VALUE;
        for ( int r = 0; r < tableRows; r++ )
        {
            maxColumns = Math.max( maxColumns, table.getRow( r ).numCells() );
        }

        for ( int r = 0; r < tableRows; r++ )
        {
            TableRow tableRow = table.getRow( r );

            Element tableRowElement = htmlDocumentFacade.createTableRow();
            StringBuilder tableRowStyle = new StringBuilder();
            WordToHtmlUtils.addTableRowProperties( tableRow, tableRowStyle );

            final int rowCells = tableRow.numCells();
            for ( int c = 0; c < rowCells; c++ )
            {
                TableCell tableCell = tableRow.getCell( c );

                if ( tableCell.isMerged() && !tableCell.isFirstMerged() )
                    continue;

                if ( tableCell.isVerticallyMerged()
                        && !tableCell.isFirstVerticallyMerged() )
                    continue;

                Element tableCellElement;
                if ( tableRow.isTableHeader() )
                {
                    tableCellElement = htmlDocumentFacade
                            .createTableHeaderCell();
                }
                else
                {
                    tableCellElement = htmlDocumentFacade.createTableCell();
                }
                StringBuilder tableCellStyle = new StringBuilder();
                WordToHtmlUtils.addTableCellProperties( tableRow, tableCell,
                        r == 0, r == tableRows - 1, c == 0, c == rowCells - 1,
                        tableCellStyle );

                if ( tableCell.isFirstMerged() )
                {
                    int count = 0;
                    for ( int c1 = c; c1 < rowCells; c1++ )
                    {
                        TableCell nextCell = tableRow.getCell( c1 );
                        if ( nextCell.isMerged() )
                            count++;
                        if ( !nextCell.isMerged() )
                            break;
                    }
                    tableCellElement.setAttribute( "colspan", "" + count );
                }
                else
                {
                    if ( c == rowCells - 1 && c != maxColumns - 1 )
                    {
                        tableCellElement.setAttribute( "colspan", ""
                                + ( maxColumns - c ) );
                    }
                }

                if ( tableCell.isFirstVerticallyMerged() )
                {
                    int count = 0;
                    for ( int r1 = r; r1 < tableRows; r1++ )
                    {
                        TableRow nextRow = table.getRow( r1 );
                        if ( nextRow.numCells() < c )
                            break;
                        TableCell nextCell = nextRow.getCell( c );
                        if ( nextCell.isVerticallyMerged() )
                            count++;
                        if ( !nextCell.isVerticallyMerged() )
                            break;
                    }
                    tableCellElement.setAttribute( "rowspan", "" + count );
                }

                processSectionParagraphes( hwpfDocument, tableCellElement,
                        tableCell, table.getTableLevel() );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( htmlDocumentFacade
                            .createParagraph() );
                }
                if ( tableCellStyle.length() > 0 )
                    tableCellElement.setAttribute( "style",
                            tableCellStyle.toString() );

                tableRowElement.appendChild( tableCellElement );
            }

            if ( tableRowStyle.length() > 0 )
                tableRowElement
                        .setAttribute( "style", tableRowStyle.toString() );

            if ( tableRow.isTableHeader() )
            {
                tableHeader.appendChild( tableRowElement );
            }
            else
            {
                tableBody.appendChild( tableRowElement );
            }
        }

        final Element tableElement = htmlDocumentFacade.createTable();
        if ( tableHeader.hasChildNodes() )
        {
            tableElement.appendChild( tableHeader );
        }
        if ( tableBody.hasChildNodes() )
        {
            tableElement.appendChild( tableBody );
            flow.appendChild( tableElement );
        }
        else
        {
            logger.log( POILogger.WARN, "Table without body starting at [",
                    Integer.valueOf( table.getStartOffset() ), "; ",
                    Integer.valueOf( table.getEndOffset() ), ")" );
        }
    }

}
