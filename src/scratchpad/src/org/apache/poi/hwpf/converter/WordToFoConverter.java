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
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.FontReplacer.Triplet;
import org.apache.poi.hwpf.usermodel.Bookmark;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class WordToFoConverter extends AbstractWordConverter
{

    private static final POILogger logger = POILogFactory
            .getLogger( WordToFoConverter.class );

    /**
     * Java main() interface to interact with {@link WordToFoConverter}
     * 
     * <p>
     * Usage: WordToFoConverter infile outfile
     * </p>
     * Where infile is an input .doc file ( Word 97-2007) which will be rendered
     * as XSL-FO into outfile
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: WordToFoConverter <inputFile.doc> <saveTo.fo>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        try
        {
            Document doc = WordToFoConverter.process( new File( args[0] ) );

            FileWriter out = new FileWriter( args[1] );
            DOMSource domSource = new DOMSource( doc );
            StreamResult streamResult = new StreamResult( out );
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            // TODO set encoding from a command argument
            serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            serializer.setOutputProperty( OutputKeys.INDENT, "yes" );
            serializer.transform( domSource, streamResult );
            out.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void processEndnoteAutonumbered( HWPFDocument doc, int noteIndex,
            Element block, Range endnoteTextRange )
    {
        // TODO: add endnote implementation?
        processFootnoteAutonumbered( doc, noteIndex, block, endnoteTextRange );
    }

    @Override
    protected void processFootnoteAutonumbered( HWPFDocument doc,
            int noteIndex, Element block, Range footnoteTextRange )
    {
        String textIndex = String.valueOf( noteIndex + 1 );

        {
            Element inline = foDocumentFacade.createInline();
            inline.setTextContent( textIndex );
            inline.setAttribute( "baseline-shift", "super" );
            inline.setAttribute( "font-size", "smaller" );
            block.appendChild( inline );
        }

        Element footnoteBody = foDocumentFacade.createFootnoteBody();
        Element footnoteBlock = foDocumentFacade.createBlock();
        footnoteBody.appendChild( footnoteBlock );
        block.appendChild( footnoteBody );

        {
            Element inline = foDocumentFacade.createInline();
            inline.setTextContent( textIndex );
            inline.setAttribute( "baseline-shift", "super" );
            inline.setAttribute( "font-size", "smaller" );
            footnoteBlock.appendChild( inline );
        }

        processCharacters( doc, Integer.MIN_VALUE, footnoteTextRange,
                footnoteBlock );
    }

    static Document process( File docFile ) throws Exception
    {
        final HWPFDocumentCore hwpfDocument = WordToFoUtils.loadDoc( docFile );
        WordToFoConverter wordToFoConverter = new WordToFoConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToFoConverter.processDocument( hwpfDocument );
        return wordToFoConverter.getDocument();
    }

    private final Stack<BlockProperies> blocksProperies = new Stack<BlockProperies>();

    protected final FoDocumentFacade foDocumentFacade;

    /**
     * Creates new instance of {@link WordToFoConverter}. Can be used for output
     * several {@link HWPFDocument}s into single FO document.
     * 
     * @param document
     *            XML DOM Document used as XSL FO document. Shall support
     *            namespaces
     */
    public WordToFoConverter( Document document )
    {
        this.foDocumentFacade = new FoDocumentFacade( document );
    }

    protected String createPageMaster( Section section, String type,
            int sectionIndex )
    {
        float height = section.getPageHeight() / WordToFoUtils.TWIPS_PER_INCH;
        float width = section.getPageWidth() / WordToFoUtils.TWIPS_PER_INCH;
        float leftMargin = section.getMarginLeft()
                / WordToFoUtils.TWIPS_PER_INCH;
        float rightMargin = section.getMarginRight()
                / WordToFoUtils.TWIPS_PER_INCH;
        float topMargin = section.getMarginTop() / WordToFoUtils.TWIPS_PER_INCH;
        float bottomMargin = section.getMarginBottom()
                / WordToFoUtils.TWIPS_PER_INCH;

        // add these to the header
        String pageMasterName = type + "-page" + sectionIndex;

        Element pageMaster = foDocumentFacade
                .addSimplePageMaster( pageMasterName );
        pageMaster.setAttribute( "page-height", height + "in" );
        pageMaster.setAttribute( "page-width", width + "in" );

        Element regionBody = foDocumentFacade.addRegionBody( pageMaster );
        regionBody.setAttribute( "margin", topMargin + "in " + rightMargin
                + "in " + bottomMargin + "in " + leftMargin + "in" );

        /*
         * 6.4.14 fo:region-body
         * 
         * The values of the padding and border-width traits must be "0".
         */
        // WordToFoUtils.setBorder(regionBody, sep.getBrcTop(), "top");
        // WordToFoUtils.setBorder(regionBody, sep.getBrcBottom(), "bottom");
        // WordToFoUtils.setBorder(regionBody, sep.getBrcLeft(), "left");
        // WordToFoUtils.setBorder(regionBody, sep.getBrcRight(), "right");

        if ( section.getNumColumns() > 1 )
        {
            regionBody.setAttribute( "column-count",
                    "" + ( section.getNumColumns() ) );
            if ( section.isColumnsEvenlySpaced() )
            {
                float distance = section.getDistanceBetweenColumns()
                        / WordToFoUtils.TWIPS_PER_INCH;
                regionBody.setAttribute( "column-gap", distance + "in" );
            }
            else
            {
                regionBody.setAttribute( "column-gap", "0.25in" );
            }
        }

        return pageMasterName;
    }

    public Document getDocument()
    {
        return foDocumentFacade.getDocument();
    }

    @Override
    protected void outputCharacters( Element block, CharacterRun characterRun,
            String text )
    {
        BlockProperies blockProperies = this.blocksProperies.peek();
        Element inline = foDocumentFacade.createInline();

        Triplet triplet = getCharacterRunTriplet( characterRun );

        if ( triplet.bold != blockProperies.pBold )
        {
            WordToFoUtils.setBold( inline, triplet.bold );
        }
        if ( triplet.italic != blockProperies.pItalic )
        {
            WordToFoUtils.setItalic( inline, triplet.italic );
        }
        if ( WordToFoUtils.isNotEmpty( triplet.fontName )
                && !WordToFoUtils.equals( triplet.fontName,
                        blockProperies.pFontName ) )
        {
            WordToFoUtils.setFontFamily( inline, characterRun.getFontName() );
        }
        if ( characterRun.getFontSize() / 2 != blockProperies.pFontSize )
        {
            WordToFoUtils.setFontSize( inline, characterRun.getFontSize() / 2 );
        }
        WordToFoUtils.setCharactersProperties( characterRun, inline );
        block.appendChild( inline );

        Text textNode = foDocumentFacade.createText( text );
        inline.appendChild( textNode );
    }

    @Override
    protected void processBookmarks( HWPFDocumentCore wordDocument,
            Element currentBlock, Range range, int currentTableLevel,
            List<Bookmark> rangeBookmarks )
    {
        Element parent = currentBlock;
        for ( Bookmark bookmark : rangeBookmarks )
        {
            Element bookmarkElement = foDocumentFacade.createInline();
            bookmarkElement.setAttribute( "id", bookmark.getName() );
            parent.appendChild( bookmarkElement );
            parent = bookmarkElement;
        }

        if ( range != null )
            processCharacters( wordDocument, currentTableLevel, range, parent );
    }

    @Override
    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getTitle() ) )
            foDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            foDocumentFacade.setCreator( summaryInformation.getAuthor() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            foDocumentFacade.setKeywords( summaryInformation.getKeywords() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getComments() ) )
            foDocumentFacade.setDescription( summaryInformation.getComments() );
    }

    protected void processHyperlink( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String hyperlink )
    {
        Element basicLink = foDocumentFacade
                .createBasicLinkExternal( hyperlink );
        currentBlock.appendChild( basicLink );

        if ( textRange != null )
            processCharacters( wordDocument, currentTableLevel, textRange,
                    basicLink );
    }

    /**
     * This method shall store image bytes in external file and convert it if
     * necessary. Images shall be stored using PNG format (for bitmap) or SVG
     * (for vector). Other formats may be not supported by your XSL FO
     * processor.
     * <p>
     * Please note the
     * {@link WordToFoUtils#setPictureProperties(Picture, Element)} method.
     * 
     * @param currentBlock
     *            currently processed FO element, like <tt>fo:block</tt>. Shall
     *            be used as parent of newly created
     *            <tt>fo:external-graphic</tt> or
     *            <tt>fo:instream-foreign-object</tt>
     * @param inlined
     *            if image is inlined
     * @param picture
     *            HWPF object, contained picture data and properties
     */
    protected void processImage( Element currentBlock, boolean inlined,
            Picture picture )
    {
        // no default implementation -- skip
        currentBlock.appendChild( foDocumentFacade.getDocument().createComment(
                "Image link to '" + picture.suggestFullFileName()
                        + "' can be here" ) );
    }

    @Override
    protected void processLineBreak( Element block, CharacterRun characterRun )
    {
        block.appendChild( foDocumentFacade.createBlock() );
    }

    protected void processPageref( HWPFDocumentCore hwpfDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref )
    {
        Element basicLink = foDocumentFacade.createBasicLinkInternal( pageref );
        currentBlock.appendChild( basicLink );

        if ( textRange != null )
            processCharacters( hwpfDocument, currentTableLevel, textRange,
                    basicLink );
    }

    protected void processParagraph( HWPFDocumentCore hwpfDocument,
            Element parentFopElement, int currentTableLevel,
            Paragraph paragraph, String bulletText )
    {
        final Element block = foDocumentFacade.createBlock();
        parentFopElement.appendChild( block );

        WordToFoUtils.setParagraphProperties( paragraph, block );

        final int charRuns = paragraph.numCharacterRuns();

        if ( charRuns == 0 )
        {
            return;
        }

        {
            CharacterRun characterRun = paragraph.getCharacterRun( 0 );
            int pFontSize = characterRun.getFontSize() / 2;
            Triplet triplet = getCharacterRunTriplet( characterRun );

            WordToFoUtils.setFontFamily( block, triplet.fontName );
            WordToFoUtils.setFontSize( block, pFontSize );
            WordToFoUtils.setBold( block, triplet.bold );
            WordToFoUtils.setItalic( block, triplet.italic );

            blocksProperies.push( new BlockProperies( triplet.fontName,
                    pFontSize, triplet.bold, triplet.italic ) );
        }
        try
        {
            boolean haveAnyText = false;

            if ( WordToFoUtils.isNotEmpty( bulletText ) )
            {
                Element inline = foDocumentFacade.createInline();
                block.appendChild( inline );

                Text textNode = foDocumentFacade.createText( bulletText );
                inline.appendChild( textNode );

                haveAnyText |= bulletText.trim().length() != 0;
            }

            haveAnyText = processCharacters( hwpfDocument, currentTableLevel,
                    paragraph, block );

            if ( !haveAnyText )
            {
                Element leader = foDocumentFacade.createLeader();
                block.appendChild( leader );
            }
        }
        finally
        {
            blocksProperies.pop();
        }

        return;
    }

    protected void processSection( HWPFDocumentCore wordDocument,
            Section section, int sectionCounter )
    {
        String regularPage = createPageMaster( section, "page", sectionCounter );

        Element pageSequence = foDocumentFacade.addPageSequence( regularPage );
        Element flow = foDocumentFacade.addFlowToPageSequence( pageSequence,
                "xsl-region-body" );

        processParagraphes( wordDocument, flow, section, Integer.MIN_VALUE );
    }

    protected void processTable( HWPFDocumentCore wordDocument, Element flow,
            Table table )
    {
        Element tableHeader = foDocumentFacade.createTableHeader();
        Element tableBody = foDocumentFacade.createTableBody();

        final int tableRows = table.numRows();

        int maxColumns = Integer.MIN_VALUE;
        for ( int r = 0; r < tableRows; r++ )
        {
            maxColumns = Math.max( maxColumns, table.getRow( r ).numCells() );
        }

        for ( int r = 0; r < tableRows; r++ )
        {
            TableRow tableRow = table.getRow( r );

            Element tableRowElement = foDocumentFacade.createTableRow();
            WordToFoUtils.setTableRowProperties( tableRow, tableRowElement );

            final int rowCells = tableRow.numCells();
            for ( int c = 0; c < rowCells; c++ )
            {
                TableCell tableCell = tableRow.getCell( c );

                if ( tableCell.isMerged() && !tableCell.isFirstMerged() )
                    continue;

                if ( tableCell.isVerticallyMerged()
                        && !tableCell.isFirstVerticallyMerged() )
                    continue;

                Element tableCellElement = foDocumentFacade.createTableCell();
                WordToFoUtils.setTableCellProperties( tableRow, tableCell,
                        tableCellElement, r == 0, r == tableRows - 1, c == 0,
                        c == rowCells - 1 );

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
                    tableCellElement.setAttribute( "number-columns-spanned", ""
                            + count );
                }
                else
                {
                    if ( c == rowCells - 1 && c != maxColumns - 1 )
                    {
                        tableCellElement.setAttribute(
                                "number-columns-spanned", ""
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
                    tableCellElement.setAttribute( "number-rows-spanned", ""
                            + count );
                }

                processParagraphes( wordDocument, tableCellElement, tableCell,
                        table.getTableLevel() );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( foDocumentFacade
                            .createBlock() );
                }

                tableRowElement.appendChild( tableCellElement );
            }

            if ( tableRow.isTableHeader() )
            {
                tableHeader.appendChild( tableRowElement );
            }
            else
            {
                tableBody.appendChild( tableRowElement );
            }
        }

        final Element tableElement = foDocumentFacade.createTable();
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
            logger.log(
                    POILogger.WARN,
                    "Table without body starting on offset "
                            + table.getStartOffset() + " -- "
                            + table.getEndOffset() );
        }
    }

    /**
     * Holds properties values, applied to current <tt>fo:block</tt> element.
     * Those properties shall not be doubled in children <tt>fo:inline</tt>
     * elements.
     */
    private static class BlockProperies
    {
        final boolean pBold;
        final String pFontName;
        final int pFontSize;
        final boolean pItalic;

        public BlockProperies( String pFontName, int pFontSize, boolean pBold,
                boolean pItalic )
        {
            this.pFontName = pFontName;
            this.pFontSize = pFontSize;
            this.pBold = pBold;
            this.pItalic = pItalic;
        }
    }

}
