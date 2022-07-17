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
import static org.apache.poi.hwpf.converter.WordToFoUtils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
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
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

@Beta
public class WordToFoConverter extends AbstractWordConverter
{

    private static final Logger LOG = LogManager.getLogger(WordToFoConverter.class);

    /**
     * Java main() interface to interact with {@link WordToFoConverter}
     *
     * <p>
     * Usage: WordToFoConverter infile outfile
     * </p>
     * Where infile is an input .doc file ( Word 97-2007) which will be rendered
     * as XSL-FO into outfile
     */
    public static void main( String[] args ) throws Exception {
        if ( args.length < 2 )
        {
            System.err.println( "Usage: WordToFoConverter <inputFile.doc> <saveTo.fo>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        Document doc = WordToFoConverter.process( new File( args[0] ) );

        DOMSource domSource = new DOMSource( doc );
        StreamResult streamResult = new StreamResult( new File( args[1] ) );
        // TODO set encoding from a command argument
        Transformer serializer = XMLHelper.newTransformer();
        serializer.transform( domSource, streamResult );
    }

    static Document process( File docFile ) throws Exception
    {
        final DocumentBuilder docBuild = XMLHelper.newDocumentBuilder();
        try (final HWPFDocumentCore hwpfDocument = loadDoc( docFile )) {
            WordToFoConverter wordToFoConverter = new WordToFoConverter(docBuild.newDocument());
            wordToFoConverter.processDocument(hwpfDocument);
            return wordToFoConverter.getDocument();
        }
    }

    private List<Element> endnotes = new ArrayList<>(0);

    protected final FoDocumentFacade foDocumentFacade;

    private AtomicInteger internalLinkCounter = new AtomicInteger( 0 );

    private boolean outputCharactersLanguage;

    private Set<String> usedIds = new LinkedHashSet<>();

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

    public WordToFoConverter( FoDocumentFacade foDocumentFacade )
    {
        this.foDocumentFacade = foDocumentFacade;
    }

    protected Element createNoteInline( String noteIndexText )
    {
        Element inline = foDocumentFacade.createInline();
        inline.setTextContent( noteIndexText );
        inline.setAttribute( "baseline-shift", "super" );
        inline.setAttribute( "font-size", "smaller" );
        return inline;
    }

    protected String createPageMaster( Section section, String type, int sectionIndex ) {
        float height = section.getPageHeight() / TWIPS_PER_INCH;
        float width = section.getPageWidth() / TWIPS_PER_INCH;
        float leftMargin = section.getMarginLeft() / TWIPS_PER_INCH;
        float rightMargin = section.getMarginRight() / TWIPS_PER_INCH;
        float topMargin = section.getMarginTop() / TWIPS_PER_INCH;
        float bottomMargin = section.getMarginBottom() / TWIPS_PER_INCH;

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
                float distance = section.getDistanceBetweenColumns() / TWIPS_PER_INCH;
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

    public boolean isOutputCharactersLanguage()
    {
        return outputCharactersLanguage;
    }

    @Override
    protected void outputCharacters( Element block, CharacterRun characterRun,
            String text )
    {
        Element inline = foDocumentFacade.createInline();

        Triplet triplet = getCharacterRunTriplet( characterRun );

        if ( isNotEmpty( triplet.fontName ) )
            setFontFamily( inline, triplet.fontName );
        setBold( inline, triplet.bold );
        setItalic( inline, triplet.italic );
        setFontSize( inline, characterRun.getFontSize() / 2 );
        setCharactersProperties( characterRun, inline );

        if ( isOutputCharactersLanguage() )
            setLanguage( characterRun, inline );

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
            final String idName = "bookmark_" + bookmark.getName();
            // make sure ID used once
            if ( setId( bookmarkElement, idName ) )
            {
                /*
                 * if it just empty fo:inline without "id" attribute doesn't
                 * making sense to add it to DOM
                 */
                parent.appendChild( bookmarkElement );
                parent = bookmarkElement;
            }
        }

        if ( range != null )
            processCharacters( wordDocument, currentTableLevel, range, parent );
    }

    @Override
    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( isNotEmpty( summaryInformation.getTitle() ) )
            foDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( isNotEmpty( summaryInformation.getAuthor() ) )
            foDocumentFacade.setCreator( summaryInformation.getAuthor() );

        if ( isNotEmpty( summaryInformation.getKeywords() ) )
            foDocumentFacade.setKeywords( summaryInformation.getKeywords() );

        if ( isNotEmpty( summaryInformation.getComments() ) )
            foDocumentFacade.setDescription( summaryInformation.getComments() );
    }

    @Override
    protected void processDrawnObject( HWPFDocument doc,
            CharacterRun characterRun, OfficeDrawing officeDrawing,
            String path, Element block )
    {
        final Element externalGraphic = foDocumentFacade
                .createExternalGraphic( path );
        block.appendChild( externalGraphic );
    }

    @Override
    protected void processEndnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range endnoteTextRange )
    {
        final String textIndex = String.valueOf( internalLinkCounter
                .incrementAndGet() );
        final String forwardLinkName = "endnote_" + textIndex;
        final String backwardLinkName = "endnote_back_" + textIndex;

        Element forwardLink = foDocumentFacade
                .createBasicLinkInternal( forwardLinkName );
        forwardLink.appendChild( createNoteInline( textIndex ) );
        setId( forwardLink, backwardLinkName );
        block.appendChild( forwardLink );

        Element endnote = foDocumentFacade.createBlock();
        Element backwardLink = foDocumentFacade
                .createBasicLinkInternal( backwardLinkName );
        backwardLink.appendChild( createNoteInline( textIndex + " " ) );
        setId( backwardLink, forwardLinkName );
        endnote.appendChild( backwardLink );

        processCharacters( wordDocument, Integer.MIN_VALUE, endnoteTextRange,
                endnote );

        compactInlines( endnote );
        this.endnotes.add( endnote );
    }

    @Override
    protected void processFootnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range footnoteTextRange )
    {
        final String textIndex = String.valueOf( internalLinkCounter
                .incrementAndGet() );
        final String forwardLinkName = "footnote_" + textIndex;
        final String backwardLinkName = "footnote_back_" + textIndex;

        Element footNote = foDocumentFacade.createFootnote();
        block.appendChild( footNote );

        Element inline = foDocumentFacade.createInline();
        Element forwardLink = foDocumentFacade
                .createBasicLinkInternal( forwardLinkName );
        forwardLink.appendChild( createNoteInline( textIndex ) );
        setId( forwardLink, backwardLinkName );
        inline.appendChild( forwardLink );
        footNote.appendChild( inline );

        Element footnoteBody = foDocumentFacade.createFootnoteBody();
        Element footnoteBlock = foDocumentFacade.createBlock();
        Element backwardLink = foDocumentFacade
                .createBasicLinkInternal( backwardLinkName );
        backwardLink.appendChild( createNoteInline( textIndex + " " ) );
        setId( backwardLink, forwardLinkName );
        footnoteBlock.appendChild( backwardLink );
        footnoteBody.appendChild( footnoteBlock );
        footNote.appendChild( footnoteBody );

        processCharacters( wordDocument, Integer.MIN_VALUE, footnoteTextRange,
                footnoteBlock );

        compactInlines( footnoteBlock );
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

    protected void processImage( Element currentBlock, boolean inlined,
            Picture picture, String url )
    {
        final Element externalGraphic = foDocumentFacade
                .createExternalGraphic( url );
        setPictureProperties( picture, externalGraphic );
        currentBlock.appendChild( externalGraphic );
    }

    @Override
    protected void processImageWithoutPicturesManager( Element currentBlock,
            boolean inlined, Picture picture )
    {
        // no default implementation -- skip
        currentBlock.appendChild( foDocumentFacade.getDocument()
                .createComment( "Image link to '"
                        + picture.suggestFullFileName() + "' can be here" ) );
    }

    @Override
    protected void processLineBreak( Element block, CharacterRun characterRun )
    {
        block.appendChild( foDocumentFacade.createBlock() );
    }

    @Override
    protected void processPageBreak( HWPFDocumentCore wordDocument, Element flow )
    {
        Element block = null;
        NodeList childNodes = flow.getChildNodes();
        if ( childNodes.getLength() > 0 )
        {
            Node lastChild = childNodes.item( childNodes.getLength() - 1 );
            if ( lastChild instanceof Element )
            {
                Element lastElement = (Element) lastChild;
                if ( !lastElement.hasAttribute( "break-after" ) )
                {
                    block = lastElement;
                }
            }
        }

        if ( block == null )
        {
            block = foDocumentFacade.createBlock();
            flow.appendChild( block );
        }
        block.setAttribute( "break-after", "page" );
    }

    protected void processPageref( HWPFDocumentCore hwpfDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref )
    {
        Element basicLink = foDocumentFacade
                .createBasicLinkInternal( "bookmark_" + pageref );
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

        setParagraphProperties( paragraph, block );

        final int charRuns = paragraph.numCharacterRuns();

        if ( charRuns == 0 )
        {
            return;
        }

        boolean haveAnyText = false;

        if ( isNotEmpty( bulletText ) )
        {
            Element inline = foDocumentFacade.createInline();
            block.appendChild( inline );

            Text textNode = foDocumentFacade.createText( bulletText );
            inline.appendChild( textNode );

            haveAnyText |= StringUtil.isNotBlank(bulletText);
        }

        haveAnyText = processCharacters( hwpfDocument, currentTableLevel,
                paragraph, block );

        if ( !haveAnyText )
        {
            Element leader = foDocumentFacade.createLeader();
            block.appendChild( leader );
        }

        compactInlines( block );
    }

    protected void processSection( HWPFDocumentCore wordDocument,
            Section section, int sectionCounter )
    {
        String regularPage = createPageMaster( section, "page", sectionCounter );

        Element pageSequence = foDocumentFacade.addPageSequence( regularPage );
        Element flow = foDocumentFacade.addFlowToPageSequence( pageSequence,
                "xsl-region-body" );

        processParagraphes( wordDocument, flow, section, Integer.MIN_VALUE );

        if ( endnotes != null && !endnotes.isEmpty() )
        {
            for ( Element endnote : endnotes )
                flow.appendChild( endnote );
            endnotes.clear();
        }
    }

    protected void processTable( HWPFDocumentCore wordDocument, Element flow,
            Table table )
    {
        Element tableHeader = foDocumentFacade.createTableHeader();
        Element tableBody = foDocumentFacade.createTableBody();

        final int[] tableCellEdges = buildTableCellEdgesArray( table );
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
            setTableRowProperties( tableRow, tableRowElement );

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

                Element tableCellElement = foDocumentFacade.createTableCell();
                setTableCellProperties( tableRow, tableCell,
                        tableCellElement, r == 0, r == tableRows - 1, c == 0,
                        c == rowCells - 1 );

                int colSpan = getNumberColumnsSpanned( tableCellEdges,
                        currentEdgeIndex, tableCell );
                currentEdgeIndex += colSpan;

                if ( colSpan == 0 )
                    continue;

                if ( colSpan != 1 )
                    tableCellElement.setAttribute( "number-columns-spanned",
                            String.valueOf( colSpan ) );

                final int rowSpan = getNumberRowsSpanned( table,
                        tableCellEdges, r, c, tableCell );
                if ( rowSpan > 1 )
                    tableCellElement.setAttribute( "number-rows-spanned",
                            String.valueOf( rowSpan ) );

                processParagraphes( wordDocument, tableCellElement, tableCell,
                        table.getTableLevel() );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( foDocumentFacade
                            .createBlock() );
                }

                tableRowElement.appendChild( tableCellElement );
            }

            if ( tableRowElement.hasChildNodes() )
            {
                if ( tableRow.isTableHeader() )
                {
                    tableHeader.appendChild( tableRowElement );
                }
                else
                {
                    tableBody.appendChild( tableRowElement );
                }
            }
        }

        final Element tableElement = foDocumentFacade.createTable();
        tableElement.setAttribute( "table-layout", "fixed" );
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
            LOG.atWarn().log("Table without body starting on offset {} -- {}", box(table.getStartOffset()),box(table.getEndOffset()));
        }
    }

    protected boolean setId( Element element, final String id )
    {
        // making sure ID used once
        if ( usedIds.contains( id ) )
        {
            LOG.atWarn().log("Tried to create element with same ID '{}'. Skipped", id);
            return false;
        }

        element.setAttribute( "id", id );
        usedIds.add( id );
        return true;
    }

    public void setOutputCharactersLanguage( boolean outputCharactersLanguage )
    {
        this.outputCharactersLanguage = outputCharactersLanguage;
    }

}
