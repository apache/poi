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

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.hwpf.converter.AbstractWordUtils.TWIPS_PER_INCH;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.apache.poi.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Converts Word files (95-2007) into HTML files.
 * <p>
 * This implementation doesn't create images or links to them. This can be
 * changed by overriding {@link #processImage(Element, boolean, Picture)}
 * method.
 */
@Beta
public class WordToHtmlConverter extends AbstractWordConverter
{
    /**
     * Holds properties values, applied to current {@code p} element. Those
     * properties shall not be doubled in children {@code span} elements.
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

    private static final Logger LOG = LogManager.getLogger(WordToHtmlConverter.class);

    private final Deque<BlockProperies> blocksProperies = new LinkedList<>();

    private final HtmlDocumentFacade htmlDocumentFacade;

    private Element notes;

    /**
     * Creates new instance of WordToHtmlConverter. Can be used for
     * output several {@link HWPFDocument}s into single HTML document.
     *
     * @param document XML DOM Document used as HTML document
     */
    public WordToHtmlConverter( Document document ) {
        this.htmlDocumentFacade = new HtmlDocumentFacade( document );
    }

    public WordToHtmlConverter( HtmlDocumentFacade htmlDocumentFacade ) {
        this.htmlDocumentFacade = htmlDocumentFacade;
    }

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
     * Java main() interface to interact with WordToHtmlConverter<p>
     *
     * Usage: WordToHtmlConverter infile outfile<p>
     *
     * Where infile is an input .doc file ( Word 95-2007) which will be rendered
     * as HTML into outfile
     */
    public static void main( String[] args )
    throws IOException, ParserConfigurationException, TransformerException {
        if ( args.length < 2 ) {
            System.err.println( "Usage: WordToHtmlConverter <inputFile.doc> <saveTo.html>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );

        Document doc = WordToHtmlConverter.process( new File( args[0] ) );

        DOMSource domSource = new DOMSource( doc );
        StreamResult streamResult = new StreamResult( new File(args[1]) );

        Transformer serializer = XMLHelper.newTransformer();
        // TODO set encoding from a command argument
        serializer.setOutputProperty( OutputKeys.METHOD, "html" );
        serializer.transform( domSource, streamResult );
    }

    static Document process( File docFile ) throws IOException, ParserConfigurationException
    {
        final DocumentBuilder docBuild = XMLHelper.newDocumentBuilder();
        try (final HWPFDocumentCore wordDocument = AbstractWordUtils.loadDoc( docFile )) {
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(docBuild.newDocument());
            wordToHtmlConverter.processDocument(wordDocument);
            return wordToHtmlConverter.getDocument();
        }
    }

    @Override
    protected void afterProcess()
    {
        if ( notes != null ) {
            htmlDocumentFacade.getBody().appendChild( notes );
        }

        htmlDocumentFacade.updateStylesheet();
    }

    @Override
    public Document getDocument()
    {
        return htmlDocumentFacade.getDocument();
    }

    @Override
    protected void outputCharacters( Element pElement,
            CharacterRun characterRun, String text )
    {
        Element span = htmlDocumentFacade.getDocument().createElement( "span" );
        pElement.appendChild( span );

        StringBuilder style = new StringBuilder();
        BlockProperies blockProperies = this.blocksProperies.peek();
        Triplet triplet = getCharacterRunTriplet( characterRun );

        if ( AbstractWordUtils.isNotEmpty( triplet.fontName )
                && !Objects.equals( triplet.fontName,
                        blockProperies.pFontName ) )
        {
            style.append("font-family:").append(triplet.fontName).append(";");
        }
        if ( characterRun.getFontSize() / 2 != blockProperies.pFontSize )
        {
            style.append("font-size:").append(characterRun.getFontSize() / 2).append("pt;");
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
        if ( style.length() != 0 ) {
            htmlDocumentFacade.addStyleClass( span, "s", style.toString() );
        }

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

        if ( range != null ) {
            processCharacters( wordDocument, currentTableLevel, range, parent );
        }
    }

    @Override
    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getTitle() ) ) {
            htmlDocumentFacade.setTitle( summaryInformation.getTitle() );
        }

        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getAuthor() ) ) {
            htmlDocumentFacade.addAuthor( summaryInformation.getAuthor() );
        }

        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getKeywords() ) ) {
            htmlDocumentFacade.addKeywords( summaryInformation.getKeywords() );
        }

        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getComments() ) ) {
            htmlDocumentFacade
                    .addDescription( summaryInformation.getComments() );
        }
    }

    @Override
    public void processDocumentPart( HWPFDocumentCore wordDocument, Range range )
    {
        super.processDocumentPart( wordDocument, range );
        afterProcess();
    }

    @Override
    protected void processDropDownList( Element block,
            CharacterRun characterRun, String[] values, int defaultIndex )
    {
        Element select = htmlDocumentFacade.createSelect();
        for ( int i = 0; i < values.length; i++ )
        {
            select.appendChild( htmlDocumentFacade.createOption( values[i],
                    defaultIndex == i ) );
        }
        block.appendChild( select );
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

        if ( textRange != null ) {
            processCharacters( wordDocument, currentTableLevel, textRange,
                    basicLink );
        }
    }

    @Override
    protected void processImage( Element currentBlock, boolean inlined,
            Picture picture, String imageSourcePath )
    {
        final int aspectRatioX = picture.getHorizontalScalingFactor();
        final int aspectRatioY = picture.getVerticalScalingFactor();

        final float imageWidth;
        final float imageHeight;

        final float cropTop;
        final float cropBottom;
        final float cropLeft;
        final float cropRight;

        if ( aspectRatioX > 0 )
        {
            imageWidth = aspectRatioX / 1000.f * picture.getDxaGoal()
                    / TWIPS_PER_INCH;
            cropRight = aspectRatioX / 1000.f * picture.getDxaCropRight()
                    / TWIPS_PER_INCH;
            cropLeft = aspectRatioX / 1000.f * picture.getDxaCropLeft()
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
            imageHeight = aspectRatioY / 1000.f * picture.getDyaGoal()
                    / TWIPS_PER_INCH;
            cropTop = aspectRatioY / 1000.f * picture.getDyaCropTop()
                    / TWIPS_PER_INCH;
            cropBottom = aspectRatioY / 1000.f * picture.getDyaCropBottom()
                    / TWIPS_PER_INCH;
        }
        else
        {
            imageHeight = picture.getDyaGoal() / TWIPS_PER_INCH;
            cropTop = picture.getDyaCropTop() / TWIPS_PER_INCH;
            cropBottom = picture.getDyaCropBottom() / TWIPS_PER_INCH;
        }

        Element root;
        if ( Math.abs(cropTop)+Math.abs(cropRight)+Math.abs(cropBottom)+Math.abs(cropLeft) > 0 )
        {
            float visibleWidth = Math
                    .max( 0, imageWidth - cropLeft - cropRight );
            float visibleHeight = Math.max( 0, imageHeight - cropTop
                    - cropBottom );

            root = htmlDocumentFacade.createBlock();
            htmlDocumentFacade.addStyleClass( root, "d",
                    "vertical-align:text-bottom;width:" + visibleWidth
                            + "in;height:" + visibleHeight + "in;" );

            // complex
            Element inner = htmlDocumentFacade.createBlock();
            htmlDocumentFacade.addStyleClass( inner, "d",
                    "position:relative;width:" + visibleWidth + "in;height:"
                            + visibleHeight + "in;overflow:hidden;" );
            root.appendChild( inner );

            Element image = htmlDocumentFacade.createImage( imageSourcePath );
            htmlDocumentFacade.addStyleClass( image, "i",
                    "position:absolute;left:-" + cropLeft + ";top:-" + cropTop
                            + ";width:" + imageWidth + "in;height:"
                            + imageHeight + "in;" );
            inner.appendChild( image );

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
    protected void processImageWithoutPicturesManager( Element currentBlock,
            boolean inlined, Picture picture )
    {
        // no default implementation -- skip
        currentBlock.appendChild( htmlDocumentFacade.getDocument()
                .createComment( "Image link to '"
                        + picture.suggestFullFileName() + "' can be here" ) );
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
                "a", "vertical-align:super;font-size:smaller;" );
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

    @Override
    protected void processPageBreak( HWPFDocumentCore wordDocument, Element flow )
    {
        flow.appendChild( htmlDocumentFacade.createLineBreak() );
    }

    @Override
    protected void processPageref( HWPFDocumentCore hwpfDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref )
    {
        Element basicLink = htmlDocumentFacade.createHyperlink( "#" + pageref );
        currentBlock.appendChild( basicLink );

        if ( textRange != null ) {
            processCharacters( hwpfDocument, currentTableLevel, textRange,
                    basicLink );
        }
    }

    @Override
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
                pFontName = AbstractWordUtils.EMPTY;
            }
            blocksProperies.push( new BlockProperies( pFontName, pFontSize ) );
        }
        try
        {
            if ( AbstractWordUtils.isNotEmpty( bulletText ) )
            {
                if ( bulletText.endsWith( "\t" ) )
                {
                    /*
                     * We don't know how to handle all cases in HTML, but at
                     * least simplest case shall be handled
                     */
                    final float defaultTab = TWIPS_PER_INCH / 2;
                    // char have some space
                    float firstLinePosition = paragraph.getIndentFromLeft()
                            + paragraph.getFirstLineIndent() + 20f;

                    float nextStop = (float) ( Math.ceil( firstLinePosition
                            / defaultTab ) * defaultTab );

                    final float spanMinWidth = nextStop - firstLinePosition;

                    Element span = htmlDocumentFacade.getDocument()
                            .createElement( "span" );
                    htmlDocumentFacade
                            .addStyleClass( span, "s",
                                    "display: inline-block; text-indent: 0; min-width: "
                                            + ( spanMinWidth / TWIPS_PER_INCH )
                                            + "in;" );
                    pElement.appendChild( span );

                    Text textNode = htmlDocumentFacade.createText( bulletText
                            .substring( 0, bulletText.length() - 1 )
                            + UNICODECHAR_ZERO_WIDTH_SPACE
                            + UNICODECHAR_NO_BREAK_SPACE );
                    span.appendChild( textNode );
                }
                else
                {
                    Text textNode = htmlDocumentFacade.createText( bulletText
                            .substring( 0, bulletText.length() - 1 ) );
                    pElement.appendChild( textNode );
                }
            }

            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    pElement );
        }
        finally
        {
            blocksProperies.pop();
        }

        if ( style.length() > 0 ) {
            htmlDocumentFacade.addStyleClass( pElement, "p", style.toString() );
        }

        WordToHtmlUtils.compactSpans( pElement );
    }

    @Override
    protected void processSection( HWPFDocumentCore wordDocument,
            Section section, int sectionCounter )
    {
        Element div = htmlDocumentFacade.createBlock();
        htmlDocumentFacade.addStyleClass( div, "d", getSectionStyle( section ) );
        htmlDocumentFacade.getBody().appendChild( div );

        processParagraphes( wordDocument, div, section, Integer.MIN_VALUE );
    }

    @Override
    protected void processSingleSection( HWPFDocumentCore wordDocument,
            Section section )
    {
        htmlDocumentFacade.addStyleClass( htmlDocumentFacade.getBody(), "b",
                getSectionStyle( section ) );

        processParagraphes( wordDocument, htmlDocumentFacade.getBody(), section,
                Integer.MIN_VALUE );
    }

    @Override
    protected void processTable( HWPFDocumentCore hwpfDocument, Element flow,
            Table table )
    {
        Element tableHeader = htmlDocumentFacade.createTableHeader();
        Element tableBody = htmlDocumentFacade.createTableBody();

        final int[] tableCellEdges = AbstractWordUtils
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
                    currentEdgeIndex += getNumberColumnsSpanned(
                            tableCellEdges, currentEdgeIndex, tableCell );
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

                if ( colSpan == 0 ) {
                    continue;
                }

                if ( colSpan != 1 ) {
                    tableCellElement.setAttribute( "colspan",
                            String.valueOf( colSpan ) );
                }

                final int rowSpan = getNumberRowsSpanned( table,
                        tableCellEdges, r, c, tableCell );
                if ( rowSpan > 1 ) {
                    tableCellElement.setAttribute( "rowspan",
                            String.valueOf( rowSpan ) );
                }

                processParagraphes( hwpfDocument, tableCellElement, tableCell,
                        table.getTableLevel() );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( htmlDocumentFacade
                            .createParagraph() );
                }
                if ( tableCellStyle.length() > 0 ) {
                    htmlDocumentFacade.addStyleClass( tableCellElement,
                            tableCellElement.getTagName(),
                            tableCellStyle.toString() );
                }

                tableRowElement.appendChild( tableCellElement );
            }

            if ( tableRowStyle.length() > 0 ) {
                tableRowElement.setAttribute( "class", htmlDocumentFacade
                        .getOrCreateCssClass( "r", tableRowStyle.toString() ) );
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

        final Element tableElement = htmlDocumentFacade.createTable();
        tableElement
                .setAttribute(
                        "class",
                        htmlDocumentFacade
                                .getOrCreateCssClass( "t",
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
            LOG.atWarn().log("Table without body starting at [{}; {})", box(table.getStartOffset()),box(table.getEndOffset()));
        }
    }

}
