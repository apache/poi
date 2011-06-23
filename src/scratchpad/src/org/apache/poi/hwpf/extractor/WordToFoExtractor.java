/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListTables;
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

import static org.apache.poi.hwpf.extractor.WordToFoUtils.TWIPS_PER_INCH;

/**
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class WordToFoExtractor extends AbstractToFoExtractor
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

    private static final byte BEL_MARK = 7;

    private static final byte FIELD_BEGIN_MARK = 19;

    private static final byte FIELD_END_MARK = 21;

    private static final byte FIELD_SEPARATOR_MARK = 20;

    private static final POILogger logger = POILogFactory
            .getLogger( WordToFoExtractor.class );

    private static HWPFDocument loadDoc( File docFile ) throws IOException
    {
        final FileInputStream istream = new FileInputStream( docFile );
        try
        {
            return new HWPFDocument( istream );
        }
        finally
        {
            try
            {
                istream.close();
            }
            catch ( Exception exc )
            {
                logger.log( POILogger.ERROR,
                        "Unable to close FileInputStream: " + exc, exc );
            }
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
        final HWPFDocument hwpfDocument = loadDoc( docFile );
        WordToFoExtractor wordToFoExtractor = new WordToFoExtractor(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToFoExtractor.processDocument( hwpfDocument );
        return wordToFoExtractor.getDocument();
    }

    private final Stack<BlockProperies> blocksProperies = new Stack<BlockProperies>();

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
        super( document );
    }

    protected String createPageMaster( SectionProperties sep, String type,
            int section )
    {
        float height = sep.getYaPage() / TWIPS_PER_INCH;
        float width = sep.getXaPage() / TWIPS_PER_INCH;
        float leftMargin = sep.getDxaLeft() / TWIPS_PER_INCH;
        float rightMargin = sep.getDxaRight() / TWIPS_PER_INCH;
        float topMargin = sep.getDyaTop() / TWIPS_PER_INCH;
        float bottomMargin = sep.getDyaBottom() / TWIPS_PER_INCH;

        // add these to the header
        String pageMasterName = type + "-page" + section;

        Element pageMaster = addSimplePageMaster( pageMasterName );
        pageMaster.setAttribute( "page-height", height + "in" );
        pageMaster.setAttribute( "page-width", width + "in" );

        Element regionBody = addRegionBody( pageMaster );
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
            regionBody
                    .setAttribute( "column-count", "" + (sep.getCcolM1() + 1) );
            if ( sep.getFEvenlySpaced() )
            {
                regionBody.setAttribute( "column-gap",
                        (sep.getDxaColumns() / TWIPS_PER_INCH) + "in" );
            }
            else
            {
                regionBody.setAttribute( "column-gap", "0.25in" );
            }
        }

        return pageMasterName;
    }

    protected boolean processCharacters( HWPFDocument hwpfDocument,
            int currentTableLevel, Paragraph paragraph, final Element block,
            final int start, final int end )
    {
        boolean haveAnyText = false;

        for ( int c = start; c < end; c++ )
        {
            CharacterRun characterRun = paragraph.getCharacterRun( c );

            if ( hwpfDocument.getPicturesTable().hasPicture( characterRun ) )
            {
                Picture picture = hwpfDocument.getPicturesTable()
                        .extractPicture( characterRun, true );

                processImage( block, characterRun.text().charAt( 0 ) == 0x01,
                        picture );
                continue;
            }

            String text = characterRun.text();
            if ( text.getBytes().length == 0 )
                continue;

            if ( text.getBytes()[0] == FIELD_BEGIN_MARK )
            {
                int skipTo = tryField( hwpfDocument, paragraph,
                        currentTableLevel, c, block );

                if ( skipTo != c )
                {
                    c = skipTo;
                    continue;
                }

                continue;
            }
            if ( text.getBytes()[0] == FIELD_SEPARATOR_MARK )
            {
                // shall not appear without FIELD_BEGIN_MARK
                continue;
            }
            if ( text.getBytes()[0] == FIELD_END_MARK )
            {
                // shall not appear without FIELD_BEGIN_MARK
                continue;
            }

            if ( characterRun.isSpecialCharacter() || characterRun.isObj()
                    || characterRun.isOle2() )
            {
                continue;
            }

            BlockProperies blockProperies = this.blocksProperies.peek();
            Element inline = createInline();
            if ( characterRun.isBold() != blockProperies.pBold )
            {
                WordToFoUtils.setBold( inline, characterRun.isBold() );
            }
            if ( characterRun.isItalic() != blockProperies.pItalic )
            {
                WordToFoUtils.setItalic( inline, characterRun.isItalic() );
            }
            if ( !WordToFoUtils.equals( characterRun.getFontName(),
                    blockProperies.pFontName ) )
            {
                WordToFoUtils
                        .setFontFamily( inline, characterRun.getFontName() );
            }
            if ( characterRun.getFontSize() / 2 != blockProperies.pFontSize )
            {
                WordToFoUtils.setFontSize( inline,
                        characterRun.getFontSize() / 2 );
            }
            WordToFoUtils.setCharactersProperties( characterRun, inline );
            block.appendChild( inline );

            if ( text.endsWith( "\r" )
                    || (text.charAt( text.length() - 1 ) == BEL_MARK && currentTableLevel != 0) )
                text = text.substring( 0, text.length() - 1 );

            Text textNode = createText( text );
            inline.appendChild( textNode );

            haveAnyText |= text.trim().length() != 0;
        }

        return haveAnyText;
    }

    public void processDocument( HWPFDocument hwpfDocument )
    {
        final Range range = hwpfDocument.getRange();

        for ( int s = 0; s < range.numSections(); s++ )
        {
            processSection( hwpfDocument, range.getSection( s ), s );
        }
    }

    protected void processField( HWPFDocument hwpfDocument,
            Element currentBlock, Paragraph paragraph, int currentTableLevel,
            int beginMark, int separatorMark, int endMark )
    {

        Pattern hyperlinkPattern = Pattern
                .compile( "[ \\t\\r\\n]*HYPERLINK \"(.*)\"[ \\t\\r\\n]*" );
        Pattern pagerefPattern = Pattern
                .compile( "[ \\t\\r\\n]*PAGEREF ([^ ]*)[ \\t\\r\\n]*\\\\h[ \\t\\r\\n]*" );

        if ( separatorMark - beginMark > 1 )
        {
            CharacterRun firstAfterBegin = paragraph
                    .getCharacterRun( beginMark + 1 );

            final Matcher hyperlinkMatcher = hyperlinkPattern
                    .matcher( firstAfterBegin.text() );
            if ( hyperlinkMatcher.matches() )
            {
                String hyperlink = hyperlinkMatcher.group( 1 );
                processHyperlink( hwpfDocument, currentBlock, paragraph,
                        currentTableLevel, hyperlink, separatorMark + 1,
                        endMark );
                return;
            }

            final Matcher pagerefMatcher = pagerefPattern
                    .matcher( firstAfterBegin.text() );
            if ( pagerefMatcher.matches() )
            {
                String pageref = pagerefMatcher.group( 1 );
                processPageref( hwpfDocument, currentBlock, paragraph,
                        currentTableLevel, pageref, separatorMark + 1, endMark );
                return;
            }
        }

        StringBuilder debug = new StringBuilder( "Unsupported field type: \n" );
        for ( int i = beginMark; i <= endMark; i++ )
        {
            debug.append( "\t" );
            debug.append( paragraph.getCharacterRun( i ) );
            debug.append( "\n" );
        }
        logger.log( POILogger.WARN, debug );

        // just output field value
        if ( separatorMark + 1 < endMark )
            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    currentBlock, separatorMark + 1, endMark );

        return;
    }

    protected void processHyperlink( HWPFDocument hwpfDocument,
            Element currentBlock, Paragraph paragraph, int currentTableLevel,
            String hyperlink, int beginTextInclusive, int endTextExclusive )
    {
        Element basicLink = createBasicLinkExternal( hyperlink );
        currentBlock.appendChild( basicLink );

        if ( beginTextInclusive < endTextExclusive )
            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    basicLink, beginTextInclusive, endTextExclusive );
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
        currentBlock.appendChild( document.createComment( "Image link to '"
                + picture.suggestFullFileName() + "' can be here" ) );
    }

    protected void processPageref( HWPFDocument hwpfDocument,
            Element currentBlock, Paragraph paragraph, int currentTableLevel,
            String pageref, int beginTextInclusive, int endTextExclusive )
    {
        Element basicLink = createBasicLinkInternal( pageref );
        currentBlock.appendChild( basicLink );

        if ( beginTextInclusive < endTextExclusive )
            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    basicLink, beginTextInclusive, endTextExclusive );
    }

    protected void processParagraph( HWPFDocument hwpfDocument,
            Element parentFopElement, int currentTableLevel,
            Paragraph paragraph, String bulletText )
    {
        final Element block = createBlock();
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
                Element inline = createInline();
                block.appendChild( inline );

                Text textNode = createText( bulletText );
                inline.appendChild( textNode );

                haveAnyText |= bulletText.trim().length() != 0;
            }

            haveAnyText = processCharacters( hwpfDocument, currentTableLevel,
                    paragraph, block, 0, charRuns );

            if ( !haveAnyText )
            {
                Element leader = createLeader();
                block.appendChild( leader );
            }
        }
        finally
        {
            blocksProperies.pop();
        }

        return;
    }

    protected void processSection( HWPFDocument hwpfDocument, Section section,
            int sectionCounter )
    {
        String regularPage = createPageMaster(
                WordToFoUtils.getSectionProperties( section ), "page",
                sectionCounter );

        Element pageSequence = addPageSequence( regularPage );
        Element flow = addFlowToPageSequence( pageSequence, "xsl-region-body" );

        processSectionParagraphes( hwpfDocument, flow, section, 0 );
    }

    protected void processSectionParagraphes( HWPFDocument hwpfDocument,
            Element flow, Range range, int currentTableLevel )
    {
        final Map<Integer, Table> allTables = new HashMap<Integer, Table>();
        for ( TableIterator tableIterator = WordToFoUtils.newTableIterator(
                range, currentTableLevel + 1 ); tableIterator.hasNext(); )
        {
            Table next = tableIterator.next();
            allTables.put( Integer.valueOf( next.getStartOffset() ), next );
        }

        final ListTables listTables = hwpfDocument.getListTables();
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
                processTable( hwpfDocument, flow, table, currentTableLevel + 1 );
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

                    processParagraph( hwpfDocument, flow, currentTableLevel,
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

                    processParagraph( hwpfDocument, flow, currentTableLevel,
                            paragraph, WordToFoUtils.EMPTY );
                }
            }
            else
            {
                processParagraph( hwpfDocument, flow, currentTableLevel,
                        paragraph, WordToFoUtils.EMPTY );
            }
        }

    }

    protected void processTable( HWPFDocument hwpfDocument, Element flow,
            Table table, int thisTableLevel )
    {
        Element tableHeader = createTableHeader();
        Element tableBody = createTableBody();

        final int tableRows = table.numRows();

        int maxColumns = Integer.MIN_VALUE;
        for ( int r = 0; r < tableRows; r++ )
        {
            maxColumns = Math.max( maxColumns, table.getRow( r ).numCells() );
        }

        for ( int r = 0; r < tableRows; r++ )
        {
            TableRow tableRow = table.getRow( r );

            Element tableRowElement = createTableRow();
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

                Element tableCellElement = createTableCell();
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
                        tableCellElement
                                .setAttribute( "number-columns-spanned", ""
                                        + (maxColumns - c) );
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

                processSectionParagraphes( hwpfDocument, tableCellElement,
                        tableCell, thisTableLevel );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( createBlock() );
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

        final Element tableElement = createTable();
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

    protected int tryField( HWPFDocument hwpfDocument, Paragraph paragraph,
            int currentTableLevel, int beginMark, Element currentBlock )
    {
        int separatorMark = -1;
        int endMark = -1;
        for ( int c = beginMark + 1; c < paragraph.numCharacterRuns(); c++ )
        {
            CharacterRun characterRun = paragraph.getCharacterRun( c );

            String text = characterRun.text();
            if ( text.getBytes().length == 0 )
                continue;

            if ( text.getBytes()[0] == FIELD_SEPARATOR_MARK )
            {
                if ( separatorMark != -1 )
                {
                    // double;
                    return beginMark;
                }

                separatorMark = c;
                continue;
            }

            if ( text.getBytes()[0] == FIELD_END_MARK )
            {
                if ( endMark != -1 )
                {
                    // double;
                    return beginMark;
                }

                endMark = c;
                break;
            }

        }

        if ( separatorMark == -1 || endMark == -1 )
            return beginMark;

        processField( hwpfDocument, currentBlock, paragraph, currentTableLevel,
                beginMark, separatorMark, endMark );

        return endMark;
    }
}
