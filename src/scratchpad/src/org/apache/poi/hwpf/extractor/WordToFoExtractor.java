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
package org.apache.poi.hwpf.extractor;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class WordToFoExtractor extends AbstractWordExtractor
{

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

    private static final POILogger logger = POILogFactory
            .getLogger( WordToFoExtractor.class );

    public static String getBorderType( BorderCode borderCode )
    {
        if ( borderCode == null )
            throw new IllegalArgumentException( "borderCode is null" );

        switch ( borderCode.getBorderType() )
        {
        case 1:
        case 2:
            return "solid";
        case 3:
            return "double";
        case 5:
            return "solid";
        case 6:
            return "dotted";
        case 7:
        case 8:
            return "dashed";
        case 9:
            return "dotted";
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
        case 15:
        case 16:
        case 17:
        case 18:
        case 19:
            return "double";
        case 20:
            return "solid";
        case 21:
            return "double";
        case 22:
            return "dashed";
        case 23:
            return "dashed";
        case 24:
            return "ridge";
        case 25:
            return "grooved";
        default:
            return "solid";
        }
    }

    /**
     * Java main() interface to interact with WordToFoExtractor
     * 
     * <p>
     * Usage: WordToFoExtractor infile outfile
     * </p>
     * Where infile is an input .doc file ( Word 97-2007) which will be rendered
     * as XSL-FO into outfile
     * 
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: WordToFoExtractor <inputFile.doc> <saveTo.fo>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        try
        {
            Document doc = WordToFoExtractor.process( new File( args[0] ) );

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

    static Document process( File docFile ) throws Exception
    {
        final HWPFDocumentCore hwpfDocument = WordToFoUtils.loadDoc( docFile );
        WordToFoExtractor wordToFoExtractor = new WordToFoExtractor(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToFoExtractor.processDocument( hwpfDocument );
        return wordToFoExtractor.getDocument();
    }

    private final Stack<BlockProperies> blocksProperies = new Stack<BlockProperies>();

    protected final FoDocumentFacade foDocumentFacade;

    /**
     * Creates new instance of {@link WordToFoExtractor}. Can be used for output
     * several {@link HWPFDocument}s into single FO document.
     * 
     * @param document
     *            XML DOM Document used as XSL FO document. Shall support
     *            namespaces
     */
    public WordToFoExtractor( Document document )
    {
        this.foDocumentFacade = new FoDocumentFacade( document );
    }

    protected String createPageMaster( SectionProperties sep, String type,
            int section )
    {
        float height = sep.getYaPage() / WordToFoUtils.TWIPS_PER_INCH;
        float width = sep.getXaPage() / WordToFoUtils.TWIPS_PER_INCH;
        float leftMargin = sep.getDxaLeft() / WordToFoUtils.TWIPS_PER_INCH;
        float rightMargin = sep.getDxaRight() / WordToFoUtils.TWIPS_PER_INCH;
        float topMargin = sep.getDyaTop() / WordToFoUtils.TWIPS_PER_INCH;
        float bottomMargin = sep.getDyaBottom() / WordToFoUtils.TWIPS_PER_INCH;

        // add these to the header
        String pageMasterName = type + "-page" + section;

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

        if ( sep.getCcolM1() > 0 )
        {
            regionBody.setAttribute( "column-count", ""
                    + ( sep.getCcolM1() + 1 ) );
            if ( sep.getFEvenlySpaced() )
            {
                regionBody.setAttribute( "column-gap",
                        ( sep.getDxaColumns() / WordToFoUtils.TWIPS_PER_INCH )
                                + "in" );
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
        if ( characterRun.isBold() != blockProperies.pBold )
        {
            WordToFoUtils.setBold( inline, characterRun.isBold() );
        }
        if ( characterRun.isItalic() != blockProperies.pItalic )
        {
            WordToFoUtils.setItalic( inline, characterRun.isItalic() );
        }
        if ( characterRun.getFontName() != null
                && !AbstractWordUtils.equals( characterRun.getFontName(),
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

    protected void processHyperlink( HWPFDocumentCore hwpfDocument,
            Element currentBlock, Paragraph paragraph,
            List<CharacterRun> characterRuns, int currentTableLevel,
            String hyperlink, int beginTextInclusive, int endTextExclusive )
    {
        Element basicLink = foDocumentFacade
                .createBasicLinkExternal( hyperlink );
        currentBlock.appendChild( basicLink );

        if ( beginTextInclusive < endTextExclusive )
            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    basicLink, characterRuns, beginTextInclusive,
                    endTextExclusive );
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

    protected void processPageref( HWPFDocumentCore hwpfDocument,
            Element currentBlock, Paragraph paragraph,
            List<CharacterRun> characterRuns, int currentTableLevel,
            String pageref, int beginTextInclusive, int endTextExclusive )
    {
        Element basicLink = foDocumentFacade.createBasicLinkInternal( pageref );
        currentBlock.appendChild( basicLink );

        if ( beginTextInclusive < endTextExclusive )
            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    basicLink, characterRuns, beginTextInclusive,
                    endTextExclusive );
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
            final String pFontName;
            final int pFontSize;
            final boolean pBold;
            final boolean pItalic;
            {
                CharacterRun characterRun = paragraph.getCharacterRun( 0 );
                pFontSize = characterRun.getFontSize() / 2;
                pFontName = characterRun.getFontName();
                pBold = characterRun.isBold();
                pItalic = characterRun.isItalic();
            }
            WordToFoUtils.setFontFamily( block, pFontName );
            WordToFoUtils.setFontSize( block, pFontSize );
            WordToFoUtils.setBold( block, pBold );
            WordToFoUtils.setItalic( block, pItalic );

            blocksProperies.push( new BlockProperies( pFontName, pFontSize,
                    pBold, pItalic ) );
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

            List<CharacterRun> characterRuns = WordToFoUtils
                    .findCharacterRuns( paragraph );
            haveAnyText = processCharacters( hwpfDocument, currentTableLevel,
                    paragraph, block, characterRuns, 0, characterRuns.size() );

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
        String regularPage = createPageMaster(
                WordToFoUtils.getSectionProperties( section ), "page",
                sectionCounter );

        Element pageSequence = foDocumentFacade.addPageSequence( regularPage );
        Element flow = foDocumentFacade.addFlowToPageSequence( pageSequence,
                "xsl-region-body" );

        processSectionParagraphes( wordDocument, flow, section, 0 );
    }

    protected void processSectionParagraphes( HWPFDocument wordDocument,
            Element flow, Range range, int currentTableLevel )
    {
        final Map<Integer, Table> allTables = new HashMap<Integer, Table>();
        for ( TableIterator tableIterator = WordToFoUtils.newTableIterator(
                range, currentTableLevel + 1 ); tableIterator.hasNext(); )
        {
            Table next = tableIterator.next();
            allTables.put( Integer.valueOf( next.getStartOffset() ), next );
        }

        final ListTables listTables = wordDocument.getListTables();
        int currentListInfo = 0;

        final int paragraphs = range.numParagraphs();
        for ( int p = 0; p < paragraphs; p++ )
        {
            Paragraph paragraph = range.getParagraph( p );

            if ( allTables.containsKey( Integer.valueOf( paragraph
                    .getStartOffset() ) ) )
            {
                Table table = allTables.get( Integer.valueOf( paragraph
                        .getStartOffset() ) );
                processTable( wordDocument, flow, table, currentTableLevel + 1 );
                continue;
            }

            if ( paragraph.isInTable()
                    && paragraph.getTableLevel() != currentTableLevel )
            {
                continue;
            }

            if ( paragraph.getIlfo() != currentListInfo )
            {
                currentListInfo = paragraph.getIlfo();
            }

            if ( currentListInfo != 0 )
            {
                if ( listTables != null )
                {
                    final ListFormatOverride listFormatOverride = listTables
                            .getOverride( paragraph.getIlfo() );

                    String label = WordToFoUtils.getBulletText( listTables,
                            paragraph, listFormatOverride.getLsid() );

                    processParagraph( wordDocument, flow, currentTableLevel,
                            paragraph, label );
                }
                else
                {
                    logger.log( POILogger.WARN,
                            "Paragraph #" + paragraph.getStartOffset() + "-"
                                    + paragraph.getEndOffset()
                                    + " has reference to list structure #"
                                    + currentListInfo
                                    + ", but listTables not defined in file" );

                    processParagraph( wordDocument, flow, currentTableLevel,
                            paragraph, WordToFoUtils.EMPTY );
                }
            }
            else
            {
                processParagraph( wordDocument, flow, currentTableLevel,
                        paragraph, WordToFoUtils.EMPTY );
            }
        }

    }

    protected void processTable( HWPFDocumentCore wordDocument, Element flow,
            Table table, int thisTableLevel )
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

                processSectionParagraphes( wordDocument, tableCellElement,
                        tableCell, thisTableLevel );

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

}
