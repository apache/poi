package org.apache.poi.hwpf.converter;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Beta
public class WordToTextConverter extends AbstractWordConverter
{

    /**
     * Java main() interface to interact with {@link WordToTextConverter}
     * 
     * <p>
     * Usage: WordToTextConverter infile outfile
     * </p>
     * Where infile is an input .doc file ( Word 95-2007) which will be rendered
     * as plain text into outfile
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: WordToTextConverter <inputFile.doc> <saveTo.txt>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        try
        {
            Document doc = WordToTextConverter.process( new File( args[0] ) );

            FileWriter out = new FileWriter( args[1] );
            DOMSource domSource = new DOMSource( doc );
            StreamResult streamResult = new StreamResult( out );

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            // TODO set encoding from a command argument
            serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            serializer.setOutputProperty( OutputKeys.INDENT, "no" );
            serializer.setOutputProperty( OutputKeys.METHOD, "text" );
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
        final HWPFDocumentCore wordDocument = AbstractWordUtils
                .loadDoc( docFile );
        WordToTextConverter wordToTextConverter = new WordToTextConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToTextConverter.processDocument( wordDocument );
        return wordToTextConverter.getDocument();
    }

    private AtomicInteger noteCounters = new AtomicInteger( 1 );

    private Element notes = null;

    private final TextDocumentFacade textDocumentFacade;

    /**
     * Creates new instance of {@link WordToTextConverter}. Can be used for
     * output several {@link HWPFDocument}s into single text document.
     * 
     * @param document
     *            XML DOM Document used as storage for text pieces
     */
    public WordToTextConverter( Document document )
    {
        this.textDocumentFacade = new TextDocumentFacade( document );
    }

    public Document getDocument()
    {
        return textDocumentFacade.getDocument();
    }

    @Override
    protected void outputCharacters( Element block, CharacterRun characterRun,
            String text )
    {
        block.appendChild( textDocumentFacade.createText( text ) );
    }

    @Override
    protected void processBookmarks( HWPFDocumentCore wordDocument,
            Element currentBlock, Range range, int currentTableLevel,
            List<Bookmark> rangeBookmarks )
    {
        processCharacters( wordDocument, currentTableLevel, range, currentBlock );
    }

    @Override
    public void processDocument( HWPFDocumentCore wordDocument )
    {
        super.processDocument( wordDocument );

        if ( notes != null )
            textDocumentFacade.getBody().appendChild( notes );
    }

    @Override
    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getTitle() ) )
            textDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            textDocumentFacade.addAuthor( summaryInformation.getAuthor() );

        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getComments() ) )
            textDocumentFacade
                    .addDescription( summaryInformation.getComments() );

        if ( AbstractWordUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            textDocumentFacade.addKeywords( summaryInformation.getKeywords() );
    }

    @Override
    protected void processDrawnObject( HWPFDocument doc,
            CharacterRun characterRun, OfficeDrawing officeDrawing,
            String path, Element block )
    {
        // ignore
    }

    @Override
    protected void processEndnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range endnoteTextRange )
    {
        processNote( wordDocument, block, endnoteTextRange );
    }

    @Override
    protected void processFootnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range footnoteTextRange )
    {
        processNote( wordDocument, block, footnoteTextRange );
    }

    @Override
    protected void processHyperlink( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String hyperlink )
    {
        processCharacters( wordDocument, currentTableLevel, textRange,
                currentBlock );

        currentBlock.appendChild( textDocumentFacade.createText( " ("
                + UNICODECHAR_ZERO_WIDTH_SPACE
                + hyperlink.replaceAll( "\\/", UNICODECHAR_ZERO_WIDTH_SPACE
                        + "\\/" + UNICODECHAR_ZERO_WIDTH_SPACE )
                + UNICODECHAR_ZERO_WIDTH_SPACE + ")" ) );
    }

    @Override
    protected void processImage( Element currentBlock, boolean inlined,
            Picture picture )
    {
        // ignore
    }

    @Override
    protected void processLineBreak( Element block, CharacterRun characterRun )
    {
        block.appendChild( textDocumentFacade.createText( "\n" ) );
    }

    protected void processNote( HWPFDocument wordDocument, Element block,
            Range noteTextRange )
    {
        final int noteIndex = noteCounters.getAndIncrement();
        block.appendChild( textDocumentFacade
                .createText( UNICODECHAR_ZERO_WIDTH_SPACE + "[" + noteIndex
                        + "]" + UNICODECHAR_ZERO_WIDTH_SPACE ) );

        if ( notes == null )
            notes = textDocumentFacade.createBlock();

        Element note = textDocumentFacade.createBlock();
        notes.appendChild( note );

        note.appendChild( textDocumentFacade.createText( "^" + noteIndex
                + "\t " ) );
        processCharacters( wordDocument, Integer.MIN_VALUE, noteTextRange, note );
        note.appendChild( textDocumentFacade.createText( "\n" ) );
    }

    @Override
    protected void processPageref( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref )
    {
        processCharacters( wordDocument, currentTableLevel, textRange,
                currentBlock );
    }

    @Override
    protected void processParagraph( HWPFDocumentCore wordDocument,
            Element parentElement, int currentTableLevel, Paragraph paragraph,
            String bulletText )
    {
        Element pElement = textDocumentFacade.createParagraph();
        pElement.appendChild( textDocumentFacade.createText( bulletText ) );
        processCharacters( wordDocument, currentTableLevel, paragraph, pElement );
        pElement.appendChild( textDocumentFacade.createText( "\n" ) );
        parentElement.appendChild( pElement );
    }

    @Override
    protected void processSection( HWPFDocumentCore wordDocument,
            Section section, int s )
    {
        Element sectionElement = textDocumentFacade.createBlock();
        processParagraphes( wordDocument, sectionElement, section,
                Integer.MIN_VALUE );
        sectionElement.appendChild( textDocumentFacade.createText( "\n" ) );
        textDocumentFacade.body.appendChild( sectionElement );
    }

    protected void processTable( HWPFDocumentCore hwpfDocument, Element flow,
            Table table )
    {
        final int tableRows = table.numRows();
        for ( int r = 0; r < tableRows; r++ )
        {
            TableRow tableRow = table.getRow( r );

            Element tableRowElement = textDocumentFacade.createTableRow();

            final int rowCells = tableRow.numCells();
            for ( int c = 0; c < rowCells; c++ )
            {
                TableCell tableCell = tableRow.getCell( c );

                Element tableCellElement = textDocumentFacade.createTableCell();

                if ( c != 0 )
                    tableCellElement.appendChild( textDocumentFacade
                            .createText( "\t" ) );

                processParagraphes( hwpfDocument, tableCellElement, tableCell,
                        table.getTableLevel() );
                tableRowElement.appendChild( tableCellElement );
            }

            tableRowElement.appendChild( textDocumentFacade.createText( "\n" ) );
            flow.appendChild( tableRowElement );
        }
    }

}
