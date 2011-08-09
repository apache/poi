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
import org.apache.poi.hwpf.usermodel.OfficeDrawing;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.util.Beta;
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
@Beta
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
        float leftMargin = section.getMarginLeft() / TWIPS_PER_INCH;
        float rightMargin = section.getMarginRight() / TWIPS_PER_INCH;
        float topMargin = section.getMarginTop() / TWIPS_PER_INCH;
        float bottomMargin = section.getMarginBottom() / TWIPS_PER_INCH;

        String style = "margin: " + topMargin + "in " + rightMargin + "in "
                + bottomMargin + "in " + leftMargin + "in;";

        if ( section.getNumColumns() > 1 )
        {
            style += "column-count: " + ( section.getNumColumns() ) + ";";
            if ( section.isColumnsEvenlySpaced() )
            {
                float distance = section.getDistanceBetweenColumns()
                        / TWIPS_PER_INCH;
                style += "column-gap: " + distance + "in;";
            }
            else
            {
                style += "column-gap: 0.25in;";
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

    private Element notes = null;

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
            style.append( "font-family:" + triplet.fontName + ";" );
        }
        if ( characterRun.getFontSize() / 2 != blockProperies.pFontSize )
        {
            style.append( "font-size:" + characterRun.getFontSize() / 2 + "pt;" );
        }
        if ( triplet.bold )
        {
            style.append( "font-weight:bold;" );
        }
        if ( triplet.italic )
        {
            style.append( "font-style:italic;" );
        }

        WordToHtmlUtils.addCharactersProperties( characterRun, style );
        if ( style.length() != 0 )
            span.setAttribute( "class", htmlDocumentFacade.getOrCreateCssClass(
                    span.getTagName(), "s", style.toString() ) );

        Text textNode = htmlDocumentFacade.createText( text );
        span.appendChild( textNode );
    }

    @Override
    protected void processBookmarks( HWPFDocumentCore wordDocument,
            Element currentBlock, Range range, int currentTableLevel,
            List<Bookmark> rangeBookmarks )
    {
        Element parent = currentBlock;
        for ( Bookmark bookmark : rangeBookmarks )
        {
            Element bookmarkElement = htmlDocumentFacade
                    .createBookmark( bookmark.getName() );
            parent.appendChild( bookmarkElement );
            parent = bookmarkElement;
        }

        if ( range != null )
            processCharacters( wordDocument, currentTableLevel, range, parent );
    }

    @Override
    public void processDocument( HWPFDocumentCore wordDocument )
    {
        super.processDocument( wordDocument );

        if ( notes != null )
            htmlDocumentFacade.getBody().appendChild( notes );

        htmlDocumentFacade.updateStylesheet();
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
    protected void processDrawnObject( HWPFDocument doc,
            CharacterRun characterRun, OfficeDrawing officeDrawing,
            String path, Element block )
    {
        Element img = htmlDocumentFacade.createImage( path );
        block.appendChild( img );
    }

    @Override
    protected void processEndnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range endnoteTextRange )
    {
        processNoteAutonumbered( wordDocument, "end", noteIndex, block,
                endnoteTextRange );
    }

    @Override
    protected void processFootnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range footnoteTextRange )
    {
        processNoteAutonumbered( wordDocument, "foot", noteIndex, block,
                footnoteTextRange );
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

    /**
     * This method shall store image bytes in external file and convert it if
     * necessary. Images shall be stored using PNG format. Other formats may be
     * not supported by user browser.
     * <p>
     * Please note the {@link #processImage(Element, boolean, Picture, String)}.
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
        PicturesManager fileManager = getPicturesManager();
        if ( fileManager != null )
        {
            String url = fileManager
                    .savePicture( picture.getContent(),
                            picture.suggestPictureType(),
                            picture.suggestFullFileName() );

            if ( WordToHtmlUtils.isNotEmpty( url ) )
            {
                processImage( currentBlock, inlined, picture, url );
                return;
            }
        }

        // no default implementation -- skip
        currentBlock.appendChild( htmlDocumentFacade.document
                .createComment( "Image link to '"
                        + picture.suggestFullFileName() + "' can be here" ) );
    }

    protected void processImage( Element currentBlock, boolean inlined,
            Picture picture, String imageSourcePath )
    {
        final int aspectRatioX = picture.getHorizontalScalingFactor();
        final int aspectRatioY = picture.getVerticalScalingFactor();

        StringBuilder style = new StringBuilder();

        final float imageWidth;
        final float imageHeight;

        final float cropTop;
        final float cropBottom;
        final float cropLeft;
        final float cropRight;

        if ( aspectRatioX > 0 )
        {
            imageWidth = picture.getDxaGoal() * aspectRatioX / 1000
                    / TWIPS_PER_INCH;
            cropRight = picture.getDxaCropRight() * aspectRatioX / 1000
                    / TWIPS_PER_INCH;
            cropLeft = picture.getDxaCropLeft() * aspectRatioX / 1000
                    / TWIPS_PER_INCH;
        }
        else
        {
            imageWidth = picture.getDxaGoal() / TWIPS_PER_INCH;
            cropRight = picture.getDxaCropRight() / TWIPS_PER_INCH;
            cropLeft = picture.getDxaCropLeft() / TWIPS_PER_INCH;
        }

        if ( aspectRatioY > 0 )
        {
            imageHeight = picture.getDyaGoal() * aspectRatioY / 1000
                    / TWIPS_PER_INCH;
            cropTop = picture.getDyaCropTop() * aspectRatioY / 1000
                    / TWIPS_PER_INCH;
            cropBottom = picture.getDyaCropBottom() * aspectRatioY / 1000
                    / TWIPS_PER_INCH;
        }
        else
        {
            imageHeight = picture.getDyaGoal() / TWIPS_PER_INCH;
            cropTop = picture.getDyaCropTop() / TWIPS_PER_INCH;
            cropBottom = picture.getDyaCropBottom() / TWIPS_PER_INCH;
        }

        Element root;
        if ( cropTop != 0 || cropRight != 0 || cropBottom != 0 || cropLeft != 0 )
        {
            float visibleWidth = Math
                    .max( 0, imageWidth - cropLeft - cropRight );
            float visibleHeight = Math.max( 0, imageHeight - cropTop
                    - cropBottom );

            root = htmlDocumentFacade.createBlock();
            root.setAttribute( "class", htmlDocumentFacade.getOrCreateCssClass(
                    root.getTagName(), "d", "vertical-align:text-bottom;width:"
                            + visibleWidth + "in;height:" + visibleHeight
                            + "in;" ) );

            // complex
            Element inner = htmlDocumentFacade.createBlock();
            inner.setAttribute( "class", htmlDocumentFacade
                    .getOrCreateCssClass( inner.getTagName(), "d",
                            "position:relative;width:" + visibleWidth
                                    + "in;height:" + visibleHeight
                                    + "in;overflow:hidden;" ) );
            root.appendChild( inner );

            Element image = htmlDocumentFacade.createImage( imageSourcePath );
            image.setAttribute( "class", htmlDocumentFacade
                    .getOrCreateCssClass( image.getTagName(), "i",
                            "position:absolute;left:-" + cropLeft + ";top:-"
                                    + cropTop + ";width:" + imageWidth
                                    + "in;height:" + imageHeight + "in;" ) );
            inner.appendChild( image );

            style.append( "overflow:hidden;" );
        }
        else
        {
            root = htmlDocumentFacade.createImage( imageSourcePath );
            root.setAttribute( "style", "width:" + imageWidth + "in;height:"
                    + imageHeight + "in;vertical-align:text-bottom;" );
        }

        currentBlock.appendChild( root );
    }

    @Override
    protected void processLineBreak( Element block, CharacterRun characterRun )
    {
        block.appendChild( htmlDocumentFacade.createLineBreak() );
    }

    protected void processNoteAutonumbered( HWPFDocument doc, String type,
            int noteIndex, Element block, Range noteTextRange )
    {
        final String textIndex = String.valueOf( noteIndex + 1 );
        final String textIndexClass = htmlDocumentFacade.getOrCreateCssClass(
                "a", "a", "vertical-align:super;font-size:smaller;" );
        final String forwardNoteLink = type + "note_" + textIndex;
        final String backwardNoteLink = type + "note_back_" + textIndex;

        Element anchor = htmlDocumentFacade.createHyperlink( "#"
                + forwardNoteLink );
        anchor.setAttribute( "name", backwardNoteLink );
        anchor.setAttribute( "class", textIndexClass + " " + type
                + "noteanchor" );
        anchor.setTextContent( textIndex );
        block.appendChild( anchor );

        if ( notes == null )
        {
            notes = htmlDocumentFacade.createBlock();
            notes.setAttribute( "class", "notes" );
        }

        Element note = htmlDocumentFacade.createBlock();
        note.setAttribute( "class", type + "note" );
        notes.appendChild( note );

        Element bookmark = htmlDocumentFacade.createBookmark( forwardNoteLink );
        bookmark.setAttribute( "href", "#" + backwardNoteLink );
        bookmark.setTextContent( textIndex );
        bookmark.setAttribute( "class", textIndexClass + " " + type
                + "noteindex" );
        note.appendChild( bookmark );
        note.appendChild( htmlDocumentFacade.createText( " " ) );

        Element span = htmlDocumentFacade.getDocument().createElement( "span" );
        span.setAttribute( "class", type + "notetext" );
        note.appendChild( span );

        this.blocksProperies.add( new BlockProperies( "", -1 ) );
        try
        {
            processCharacters( doc, Integer.MIN_VALUE, noteTextRange, span );
        }
        finally
        {
            this.blocksProperies.pop();
        }
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
            Element parentElement, int currentTableLevel, Paragraph paragraph,
            String bulletText )
    {
        final Element pElement = htmlDocumentFacade.createParagraph();
        parentElement.appendChild( pElement );

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
                Triplet triplet = getCharacterRunTriplet( characterRun );
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
            pElement.setAttribute(
                    "class",
                    htmlDocumentFacade.getOrCreateCssClass(
                            pElement.getTagName(), "p", style.toString() ) );

        WordToHtmlUtils.compactSpans( pElement );
        return;
    }

    protected void processSection( HWPFDocumentCore wordDocument,
            Section section, int sectionCounter )
    {
        Element div = htmlDocumentFacade.createBlock();
        div.setAttribute( "class", htmlDocumentFacade.getOrCreateCssClass(
                div.getTagName(), "d", getSectionStyle( section ) ) );
        htmlDocumentFacade.body.appendChild( div );

        processParagraphes( wordDocument, div, section, Integer.MIN_VALUE );
    }

    @Override
    protected void processSingleSection( HWPFDocumentCore wordDocument,
            Section section )
    {
        htmlDocumentFacade.body
                .setAttribute( "class", htmlDocumentFacade.getOrCreateCssClass(
                        "body", "b", getSectionStyle( section ) ) );

        processParagraphes( wordDocument, htmlDocumentFacade.body, section,
                Integer.MIN_VALUE );
    }

    protected void processTable( HWPFDocumentCore hwpfDocument, Element flow,
            Table table )
    {
        Element tableHeader = htmlDocumentFacade.createTableHeader();
        Element tableBody = htmlDocumentFacade.createTableBody();

        final int[] tableCellEdges = WordToHtmlUtils
                .buildTableCellEdgesArray( table );
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

            // index of current element in tableCellEdges[]
            int currentEdgeIndex = 0;
            final int rowCells = tableRow.numCells();
            for ( int c = 0; c < rowCells; c++ )
            {
                TableCell tableCell = tableRow.getCell( c );

                if ( tableCell.isVerticallyMerged()
                        && !tableCell.isFirstVerticallyMerged() )
                {
                    currentEdgeIndex += getTableCellEdgesIndexSkipCount( table,
                            r, tableCellEdges, currentEdgeIndex, c, tableCell );
                    continue;
                }

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

                int colSpan = getNumberColumnsSpanned( tableCellEdges,
                        currentEdgeIndex, tableCell );
                currentEdgeIndex += colSpan;

                if ( colSpan == 0 )
                    continue;

                if ( colSpan != 1 )
                    tableCellElement.setAttribute( "colspan",
                            String.valueOf( colSpan ) );

                final int rowSpan = getNumberRowsSpanned( table, r, c,
                        tableCell );
                if ( rowSpan > 1 )
                    tableCellElement.setAttribute( "rowspan",
                            String.valueOf( rowSpan ) );

                processParagraphes( hwpfDocument, tableCellElement, tableCell,
                        table.getTableLevel() );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( htmlDocumentFacade
                            .createParagraph() );
                }
                if ( tableCellStyle.length() > 0 )
                    tableCellElement.setAttribute( "class", htmlDocumentFacade
                            .getOrCreateCssClass(
                                    tableCellElement.getTagName(),
                                    tableCellElement.getTagName(),
                                    tableCellStyle.toString() ) );

                tableRowElement.appendChild( tableCellElement );
            }

            if ( tableRowStyle.length() > 0 )
                tableRowElement.setAttribute( "class", htmlDocumentFacade
                        .getOrCreateCssClass( "tr", "r",
                                tableRowStyle.toString() ) );

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
        tableElement
                .setAttribute(
                        "class",
                        htmlDocumentFacade.getOrCreateCssClass(
                                tableElement.getTagName(), "t",
                                "table-layout:fixed;border-collapse:collapse;border-spacing:0;" ) );
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
