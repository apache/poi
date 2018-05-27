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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.AbstractWordUtils.NumberingState;
import org.apache.poi.hwpf.converter.FontReplacer.Triplet;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
import org.apache.poi.hwpf.usermodel.Bookmark;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Field;
import org.apache.poi.hwpf.usermodel.HWPFList;
import org.apache.poi.hwpf.usermodel.Notes;
import org.apache.poi.hwpf.usermodel.OfficeDrawing;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Beta
public abstract class AbstractWordConverter
{
    private static class DeadFieldBoundaries
    {
        final int beginMark;
        final int endMark;
        final int separatorMark;

        public DeadFieldBoundaries( int beginMark, int separatorMark,
                int endMark )
        {
            this.beginMark = beginMark;
            this.separatorMark = separatorMark;
            this.endMark = endMark;
        }
    }

    private static final class Structure implements Comparable<Structure>
    {
        final int end;
        final int start;
        final Object structure;

        Structure( Bookmark bookmark )
        {
            this.start = bookmark.getStart();
            this.end = bookmark.getEnd();
            this.structure = bookmark;
        }

        Structure( DeadFieldBoundaries deadFieldBoundaries, int start, int end )
        {
            this.start = start;
            this.end = end;
            this.structure = deadFieldBoundaries;
        }

        Structure( Field field )
        {
            this.start = field.getFieldStartOffset();
            this.end = field.getFieldEndOffset();
            this.structure = field;
        }

        public int compareTo( Structure o )
        {
            return Integer.compare(start, o.start);
        }

        @Override
        public String toString()
        {
            return "Structure [" + start + "; " + end + "): "
                    + structure;
        }
    }

    private static final byte BEL_MARK = 7;

    private static final byte FIELD_BEGIN_MARK = 19;

    private static final byte FIELD_END_MARK = 21;

    private static final byte FIELD_SEPARATOR_MARK = 20;

    private static final POILogger logger = POILogFactory
            .getLogger( AbstractWordConverter.class );

    private static final Pattern PATTERN_HYPERLINK_EXTERNAL = Pattern
            .compile( "^[ \\t\\r\\n]*HYPERLINK \"(.*)\".*$" );

    private static final Pattern PATTERN_HYPERLINK_LOCAL = Pattern
            .compile( "^[ \\t\\r\\n]*HYPERLINK \\\\l \"(.*)\"[ ](.*)$" );

    private static final Pattern PATTERN_PAGEREF = Pattern
            .compile( "^[ \\t\\r\\n]*PAGEREF ([^ ]*)[ \\t\\r\\n]*\\\\h.*$" );

    private static final byte SPECCHAR_AUTONUMBERED_FOOTNOTE_REFERENCE = 2;

    private static final byte SPECCHAR_DRAWN_OBJECT = 8;

    protected static final char UNICODECHAR_NO_BREAK_SPACE = '\u00a0';

    protected static final char UNICODECHAR_NONBREAKING_HYPHEN = '\u2011';

    protected static final char UNICODECHAR_ZERO_WIDTH_SPACE = '\u200b';

    private static void addToStructures( List<Structure> structures,
            Structure structure )
    {
        for ( Iterator<Structure> iterator = structures.iterator(); iterator
                .hasNext(); )
        {
            Structure another = iterator.next();

            if ( another.start <= structure.start
                    && another.end >= structure.start )
            {
                return;
            }

            if ( ( structure.start < another.start && another.start < structure.end )
                    || ( structure.start < another.start && another.end <= structure.end )
                    || ( structure.start <= another.start && another.end < structure.end ) )
            {
                iterator.remove();
                continue;
            }
        }
        structures.add( structure );
    }

    private final Set<Bookmark> bookmarkStack = new LinkedHashSet<>();

    private FontReplacer fontReplacer = new DefaultFontReplacer();

    private POILogger log = POILogFactory.getLogger( getClass() );

    private NumberingState numberingState = new NumberingState();

    private PicturesManager picturesManager;

    /**
     * Special actions that need to be called after processing complete, like
     * updating stylesheets or building document notes list. Usually they are
     * called once, but it's okay to call them several times.
     */
    protected void afterProcess()
    {
        // by default no such actions needed
    }

    protected Triplet getCharacterRunTriplet(CharacterRun characterRun )
    {
        Triplet original = new Triplet();
        original.bold = characterRun.isBold();
        original.italic = characterRun.isItalic();
        original.fontName = characterRun.getFontName();
        return getFontReplacer().update( original );
    }

    public abstract Document getDocument();

    public FontReplacer getFontReplacer()
    {
        return fontReplacer;
    }

    protected int getNumberColumnsSpanned( int[] tableCellEdges,
            int currentEdgeIndex, TableCell tableCell )
    {
        int nextEdgeIndex = currentEdgeIndex;
        int colSpan = 0;
        int cellRightEdge = tableCell.getLeftEdge() + tableCell.getWidth();
        while ( tableCellEdges[nextEdgeIndex] < cellRightEdge )
        {
            colSpan++;
            nextEdgeIndex++;
        }
        return colSpan;
    }

    protected int getNumberRowsSpanned( Table table,
            final int[] tableCellEdges, int currentRowIndex,
            int currentColumnIndex, TableCell tableCell )
    {
        if ( !tableCell.isFirstVerticallyMerged() )
            return 1;

        final int numRows = table.numRows();

        int count = 1;
        for ( int r1 = currentRowIndex + 1; r1 < numRows; r1++ )
        {
            TableRow nextRow = table.getRow( r1 );
            if ( currentColumnIndex >= nextRow.numCells() )
                break;

            // we need to skip row if he don't have cells at all
            boolean hasCells = false;
            int currentEdgeIndex = 0;
            for ( int c = 0; c < nextRow.numCells(); c++ )
            {
                TableCell nextTableCell = nextRow.getCell( c );
                if ( !nextTableCell.isVerticallyMerged()
                        || nextTableCell.isFirstVerticallyMerged() )
                {
                    int colSpan = getNumberColumnsSpanned( tableCellEdges,
                            currentEdgeIndex, nextTableCell );
                    currentEdgeIndex += colSpan;

                    if ( colSpan != 0 )
                    {
                        hasCells = true;
                        break;
                    }
                }
                else
                {
                    currentEdgeIndex += getNumberColumnsSpanned(
                            tableCellEdges, currentEdgeIndex, nextTableCell );
                }
            }
            if ( !hasCells )
                continue;

            TableCell nextCell = nextRow.getCell( currentColumnIndex );
            if ( !nextCell.isVerticallyMerged()
                    || nextCell.isFirstVerticallyMerged() )
                break;
            count++;
        }
        return count;
    }

    public PicturesManager getPicturesManager()
    {
        return picturesManager;
    }

    protected abstract void outputCharacters( Element block,
            CharacterRun characterRun, String text );

    /**
     * Wrap range into bookmark(s) and process it. All bookmarks have starts
     * equal to range start and ends equal to range end. Usually it's only one
     * bookmark.
     */
    protected abstract void processBookmarks( HWPFDocumentCore wordDocument,
            Element currentBlock, Range range, int currentTableLevel,
            List<Bookmark> rangeBookmarks );

    protected boolean processCharacters( final HWPFDocumentCore wordDocument,
            final int currentTableLevel, final Range range, final Element block )
    {
        if ( range == null )
            return false;

        boolean haveAnyText = false;

        /*
         * In text there can be fields, bookmarks, may be other structures (code
         * below allows extension). Those structures can overlaps, so either we
         * should process char-by-char (slow) or find a correct way to
         * reconstruct the structure of range -- sergey
         */
        List<Structure> structures = new LinkedList<>();
        if ( wordDocument instanceof HWPFDocument )
        {
            final HWPFDocument doc = (HWPFDocument) wordDocument;

            Map<Integer, List<Bookmark>> rangeBookmarks = doc.getBookmarks()
                    .getBookmarksStartedBetween( range.getStartOffset(),
                            range.getEndOffset() );

            if ( rangeBookmarks != null )
            {
                for ( List<Bookmark> lists : rangeBookmarks.values() )
                {
                    for ( Bookmark bookmark : lists )
                    {
                        if ( !bookmarkStack.contains( bookmark ) )
                            addToStructures( structures, new Structure(
                                    bookmark ) );
                    }
                }
            }

            // TODO: dead fields?
            int skipUntil = -1;
            for ( int c = 0; c < range.numCharacterRuns(); c++ )
            {
                CharacterRun characterRun = range.getCharacterRun( c );
                if ( characterRun == null )
                    throw new AssertionError();
                if ( characterRun.getStartOffset() < skipUntil )
                    continue;
                String text = characterRun.text();
                if ( text == null || text.length() == 0
                        || text.charAt( 0 ) != FIELD_BEGIN_MARK )
                    continue;

                Field aliveField = ( (HWPFDocument) wordDocument ).getFields()
                        .getFieldByStartOffset( FieldsDocumentPart.MAIN,
                                characterRun.getStartOffset() );
                if ( aliveField != null )
                {
                    addToStructures( structures, new Structure( aliveField ) );
                }
                else
                {
                    int[] separatorEnd = tryDeadField_lookupFieldSeparatorEnd(
                            wordDocument, range, c );
                    if ( separatorEnd != null )
                    {
                        addToStructures(
                                structures,
                                new Structure( new DeadFieldBoundaries( c,
                                        separatorEnd[0], separatorEnd[1] ),
                                        characterRun.getStartOffset(), range
                                                .getCharacterRun(
                                                        separatorEnd[1] )
                                                .getEndOffset() ) );
                        c = separatorEnd[1];
                    }
                }
            }
        }

        structures = new ArrayList<>(structures);
        Collections.sort( structures );

        int previous = range.getStartOffset();
        for ( Structure structure : structures )
        {
            if ( structure.start != previous )
            {
                Range subrange = new Range( previous, structure.start, range )
                {
                    @Override
                    public String toString()
                    {
                        return "BetweenStructuresSubrange " + super.toString();
                    }
                };
                processCharacters( wordDocument, currentTableLevel, subrange,
                        block );
            }

            if ( structure.structure instanceof Bookmark )
            {
                // other bookmarks with same boundaries
                List<Bookmark> bookmarks = new LinkedList<>();
                for ( Bookmark bookmark : ( (HWPFDocument) wordDocument )
                        .getBookmarks()
                        .getBookmarksStartedBetween( structure.start,
                                structure.start + 1 ).values().iterator()
                        .next() )
                {
                    if ( bookmark.getStart() == structure.start
                            && bookmark.getEnd() == structure.end )
                    {
                        bookmarks.add( bookmark );
                    }
                }

                bookmarkStack.addAll( bookmarks );
                try
                {
                    int end = Math.min( range.getEndOffset(), structure.end );
                    Range subrange = new Range( structure.start, end, range )
                    {
                        @Override
                        public String toString()
                        {
                            return "BookmarksSubrange " + super.toString();
                        }
                    };

                    processBookmarks( wordDocument, block, subrange,
                            currentTableLevel, bookmarks );
                }
                finally
                {
                    bookmarkStack.removeAll( bookmarks );
                }
            }
            else if ( structure.structure instanceof Field )
            {
                Field field = (Field) structure.structure;
                processField( (HWPFDocument) wordDocument, range,
                        currentTableLevel, field, block );
            }
            else if ( structure.structure instanceof DeadFieldBoundaries )
            {
                DeadFieldBoundaries boundaries = (DeadFieldBoundaries) structure.structure;
                processDeadField( wordDocument, block, range,
                        currentTableLevel, boundaries.beginMark,
                        boundaries.separatorMark, boundaries.endMark );
            }
            else
            {
                throw new UnsupportedOperationException( "NYI: "
                        + structure.structure.getClass() );
            }

            previous = Math.min( range.getEndOffset(), structure.end );
        }

        if ( previous != range.getStartOffset() )
        {
            if ( previous > range.getEndOffset() )
            {
                logger.log( POILogger.WARN, "Latest structure in ", range,
                        " ended at #" + previous, " after range boundaries [",
                        range.getStartOffset() + "; " + range.getEndOffset(),
                        ")" );
                return true;
            }

            if ( previous < range.getEndOffset() )
            {
                Range subrange = new Range( previous, range.getEndOffset(),
                        range )
                {
                    @Override
                    public String toString()
                    {
                        return "AfterStructureSubrange " + super.toString();
                    }
                };
                processCharacters( wordDocument, currentTableLevel, subrange,
                        block );
            }
            return true;
        }

        for ( int c = 0; c < range.numCharacterRuns(); c++ )
        {
            CharacterRun characterRun = range.getCharacterRun( c );

            if ( characterRun == null )
                throw new AssertionError();

            if ( wordDocument instanceof HWPFDocument
                    && ( (HWPFDocument) wordDocument ).getPicturesTable()
                            .hasPicture( characterRun ) )
            {
                HWPFDocument newFormat = (HWPFDocument) wordDocument;
                Picture picture = newFormat.getPicturesTable().extractPicture(
                        characterRun, true );

                processImage( block, characterRun.text().charAt( 0 ) == 0x01,
                        picture );
                continue;
            }

            String text = characterRun.text();
            if ( text.isEmpty() ) continue;

            if ( characterRun.isSpecialCharacter() )
            {
                if ( text.charAt( 0 ) == SPECCHAR_AUTONUMBERED_FOOTNOTE_REFERENCE
                        && ( wordDocument instanceof HWPFDocument ) )
                {
                    HWPFDocument doc = (HWPFDocument) wordDocument;
                    processNoteAnchor( doc, characterRun, block );
                    continue;
                }
                if ( text.charAt( 0 ) == SPECCHAR_DRAWN_OBJECT
                        && ( wordDocument instanceof HWPFDocument ) )
                {
                    HWPFDocument doc = (HWPFDocument) wordDocument;
                    processDrawnObject( doc, characterRun, block );
                    continue;
                }
                if ( characterRun.isOle2()
                        && ( wordDocument instanceof HWPFDocument ) )
                {
                    HWPFDocument doc = (HWPFDocument) wordDocument;
                    processOle2( doc, characterRun, block );
                    continue;
                }
                if ( characterRun.isSymbol()
                        && ( wordDocument instanceof HWPFDocument ) )
                {
                    HWPFDocument doc = (HWPFDocument) wordDocument;
                    processSymbol( doc, characterRun, block );
                    continue;
                }
            }

            if ( text.charAt(0) == FIELD_BEGIN_MARK )
            {
                if ( wordDocument instanceof HWPFDocument )
                {
                    Field aliveField = ( (HWPFDocument) wordDocument )
                            .getFields().getFieldByStartOffset(
                                    FieldsDocumentPart.MAIN,
                                    characterRun.getStartOffset() );
                    if ( aliveField != null )
                    {
                        processField( ( (HWPFDocument) wordDocument ), range,
                                currentTableLevel, aliveField, block );

                        int continueAfter = aliveField.getFieldEndOffset();
                        while ( c < range.numCharacterRuns()
                                && range.getCharacterRun( c ).getEndOffset() <= continueAfter )
                            c++;

                        if ( c < range.numCharacterRuns() )
                            c--;

                        continue;
                    }
                }

                int skipTo = tryDeadField( wordDocument, range,
                        currentTableLevel, c, block );

                if ( skipTo != c )
                {
                    c = skipTo;
                    continue;
                }

                continue;
            }
            if ( text.charAt(0) == FIELD_SEPARATOR_MARK )
            {
                // shall not appear without FIELD_BEGIN_MARK
                continue;
            }
            if ( text.charAt(0) == FIELD_END_MARK )
            {
                // shall not appear without FIELD_BEGIN_MARK
                continue;
            }

            if ( characterRun.isSpecialCharacter() || characterRun.isObj()
                    || characterRun.isOle2() )
            {
                continue;
            }

            if ( text.endsWith( "\r" )
                    || ( text.charAt( text.length() - 1 ) == BEL_MARK && currentTableLevel != Integer.MIN_VALUE ) )
                text = text.substring( 0, text.length() - 1 );

            {
                // line breaks
                StringBuilder stringBuilder = new StringBuilder();
                for ( char charChar : text.toCharArray() )
                {
                    if ( charChar == 11 )
                    {
                        if ( stringBuilder.length() > 0 )
                        {
                            outputCharacters( block, characterRun,
                                    stringBuilder.toString() );
                            stringBuilder.setLength( 0 );
                        }
                        processLineBreak( block, characterRun );
                    }
                    else if ( charChar == 30 )
                    {
                        // Non-breaking hyphens are stored as ASCII 30
                        stringBuilder.append( UNICODECHAR_NONBREAKING_HYPHEN );
                    }
                    else if ( charChar == 31 )
                    {
                        // Non-required hyphens to zero-width space
                        stringBuilder.append( UNICODECHAR_ZERO_WIDTH_SPACE );
                    }
                    else if ( charChar >= 0x20 || charChar == 0x09
                            || charChar == 0x0A || charChar == 0x0D )
                    {
                        stringBuilder.append( charChar );
                    }
                }
                if ( stringBuilder.length() > 0 )
                {
                    outputCharacters( block, characterRun,
                            stringBuilder.toString() );
                    stringBuilder.setLength( 0 );
                }
            }

            haveAnyText |= text.trim().length() != 0;
        }

        return haveAnyText;
    }

    protected void processDeadField( HWPFDocumentCore wordDocument,
            Element currentBlock, Range range, int currentTableLevel,
            int beginMark, int separatorMark, int endMark )
    {
        if ( beginMark + 1 < separatorMark && separatorMark + 1 < endMark )
        {
            Range formulaRange = new Range( range.getCharacterRun(
                    beginMark + 1 ).getStartOffset(), range.getCharacterRun(
                    separatorMark - 1 ).getEndOffset(), range )
            {
                @Override
                public String toString()
                {
                    return "Dead field formula subrange: " + super.toString();
                }
            };
            Range valueRange = new Range( range.getCharacterRun(
                    separatorMark + 1 ).getStartOffset(), range
                    .getCharacterRun( endMark - 1 ).getEndOffset(), range )
            {
                @Override
                public String toString()
                {
                    return "Dead field value subrange: " + super.toString();
                }
            };
            String formula = formulaRange.text();
            final Matcher matcher = PATTERN_HYPERLINK_LOCAL.matcher( formula );
            if ( matcher.matches() )
            {
                String localref = matcher.group( 1 );
                processPageref( wordDocument, currentBlock, valueRange,
                        currentTableLevel, localref );
                return;
            }
        }

        StringBuilder debug = new StringBuilder( "Unsupported field type: \n" );
        for ( int i = beginMark; i <= endMark; i++ )
        {
            debug.append( "\t" );
            debug.append( range.getCharacterRun( i ) );
            debug.append( "\n" );
        }
        logger.log( POILogger.WARN, debug );

        Range deadFieldValueSubrage = new Range( range.getCharacterRun(
                separatorMark ).getStartOffset() + 1, range.getCharacterRun(
                endMark ).getStartOffset(), range )
        {
            @Override
            public String toString()
            {
                return "DeadFieldValueSubrange (" + super.toString() + ")";
            }
        };

        // just output field value
        if ( separatorMark + 1 < endMark )
            processCharacters( wordDocument, currentTableLevel,
                    deadFieldValueSubrage, currentBlock );
    }

    public void processDocument( HWPFDocumentCore wordDocument )
    {
        try
        {
            final SummaryInformation summaryInformation = wordDocument
                    .getSummaryInformation();
            if ( summaryInformation != null )
            {
                processDocumentInformation( summaryInformation );
            }
        }
        catch ( Exception exc )
        {
            logger.log( POILogger.WARN,
                    "Unable to process document summary information: ", exc,
                    exc );
        }

        final Range docRange = wordDocument.getRange();

        if ( docRange.numSections() == 1 )
        {
            processSingleSection( wordDocument, docRange.getSection( 0 ) );
            afterProcess();
            return;
        }

        processDocumentPart( wordDocument, docRange );
        afterProcess();
    }

    protected abstract void processDocumentInformation(
            SummaryInformation summaryInformation );

    protected void processDocumentPart( HWPFDocumentCore wordDocument,
            final Range range )
    {
        for ( int s = 0; s < range.numSections(); s++ )
        {
            processSection( wordDocument, range.getSection( s ), s );
        }
    }

    protected void processDrawnObject( HWPFDocument doc,
            CharacterRun characterRun, Element block )
    {
        if ( getPicturesManager() == null )
            return;

        // TODO: support headers
        OfficeDrawing officeDrawing = doc.getOfficeDrawingsMain()
                .getOfficeDrawingAt( characterRun.getStartOffset() );
        if ( officeDrawing == null )
        {
            logger.log( POILogger.WARN, "Characters #" + characterRun
                    + " references missing drawn object" );
            return;
        }

        byte[] pictureData = officeDrawing.getPictureData();
        if ( pictureData == null )
            // usual shape?
            return;

        float width = ( officeDrawing.getRectangleRight() - officeDrawing
                .getRectangleLeft() ) / AbstractWordUtils.TWIPS_PER_INCH;
        float height = ( officeDrawing.getRectangleBottom() - officeDrawing
                .getRectangleTop() ) / AbstractWordUtils.TWIPS_PER_INCH;

        final PictureType type = PictureType.findMatchingType( pictureData );
        String path = getPicturesManager()
                .savePicture( pictureData, type,
                        "s" + characterRun.getStartOffset() + "." + type,
                        width, height );

        processDrawnObject( doc, characterRun, officeDrawing, path, block );
    }

    protected abstract void processDrawnObject( HWPFDocument doc,
            CharacterRun characterRun, OfficeDrawing officeDrawing,
            String path, Element block );

    protected void processDropDownList( Element block,
            CharacterRun characterRun, String[] values, int defaultIndex )
    {
        outputCharacters( block, characterRun, values[defaultIndex] );
    }

    protected abstract void processEndnoteAutonumbered(
            HWPFDocument wordDocument, int noteIndex, Element block,
            Range endnoteTextRange );

    protected void processField( HWPFDocument wordDocument, Range parentRange,
            int currentTableLevel, Field field, Element currentBlock )
    {
        switch ( field.getType() )
        {
        case 37: // page reference
        {
            final Range firstSubrange = field.firstSubrange( parentRange );
            if ( firstSubrange != null )
            {
                String formula = firstSubrange.text();
                Matcher matcher = PATTERN_PAGEREF.matcher( formula );
                if ( matcher.find() )
                {
                    String pageref = matcher.group( 1 );
                    processPageref( wordDocument, currentBlock,
                            field.secondSubrange( parentRange ),
                            currentTableLevel, pageref );
                    return;
                }
            }
            break;
        }
        case 58: // Embedded Object
        {
            if ( !field.hasSeparator() )
            {
                logger.log( POILogger.WARN, parentRange + " contains " + field
                        + " with 'Embedded Object' but without separator mark" );
                return;
            }

            CharacterRun separator = field
                    .getMarkSeparatorCharacterRun( parentRange );

            if ( separator.isOle2() )
            {
                // the only supported so far
                boolean processed = processOle2( wordDocument, separator,
                        currentBlock );

                // if we didn't output OLE - output field value
                if ( !processed )
                {
                    processCharacters( wordDocument, currentTableLevel,
                            field.secondSubrange( parentRange ), currentBlock );
                }

                return;
            }

            break;
        }
        case 83: // drop down
        {
            Range fieldContent = field.firstSubrange( parentRange );
            CharacterRun cr = fieldContent.getCharacterRun( fieldContent
                    .numCharacterRuns() - 1 );
            String[] values = cr.getDropDownListValues();
            Integer defIndex = cr.getDropDownListDefaultItemIndex();

            if ( values != null && values.length > 0 )
            {
                processDropDownList( currentBlock, cr, values,
                        defIndex == null ? -1 : defIndex.intValue() );
                return;
            }
            break;
        }
        case 88: // hyperlink
        {
            final Range firstSubrange = field.firstSubrange( parentRange );
            if ( firstSubrange != null )
            {
                String formula = firstSubrange.text();
                Matcher matcher = PATTERN_HYPERLINK_EXTERNAL.matcher( formula );
                if ( matcher.matches() )
                {
                    String hyperlink = matcher.group( 1 );
                    processHyperlink( wordDocument, currentBlock,
                            field.secondSubrange( parentRange ),
                            currentTableLevel, hyperlink );
                    return;
                }
                matcher.usePattern( PATTERN_HYPERLINK_LOCAL );
                if ( matcher.matches() )
                {
                    String hyperlink = matcher.group( 1 );
                    Range textRange = null;
                    String text = matcher.group( 2 );
                    if ( AbstractWordUtils.isNotEmpty( text ) )
                    {
                        textRange = new Range( firstSubrange.getStartOffset()
                                + matcher.start( 2 ),
                                firstSubrange.getStartOffset()
                                        + matcher.end( 2 ), firstSubrange )
                        {
                            @Override
                            public String toString()
                            {
                                return "Local hyperlink text";
                            }
                        };
                    }
                    processPageref( wordDocument, currentBlock, textRange,
                            currentTableLevel, hyperlink );
                    return;
                }
            }
            break;
        }
        }

        logger.log( POILogger.WARN, parentRange + " contains " + field
                + " with unsupported type or format" );
        processCharacters( wordDocument, currentTableLevel,
                field.secondSubrange( parentRange ), currentBlock );
    }

    protected abstract void processFootnoteAutonumbered(
            HWPFDocument wordDocument, int noteIndex, Element block,
            Range footnoteTextRange );

    protected abstract void processHyperlink( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String hyperlink );

    protected void processImage( Element currentBlock, boolean inlined, Picture picture ) {
        PicturesManager fileManager = getPicturesManager();
        if ( fileManager != null ) {
            final float aspectRatioX = picture.getHorizontalScalingFactor();
            final float aspectRatioY = picture.getVerticalScalingFactor();

            float imageWidth = picture.getDxaGoal();
            if (aspectRatioX > 0) {
                imageWidth *= aspectRatioX / 1000f;
            }
            imageWidth /= AbstractWordUtils.TWIPS_PER_INCH;
            
            float imageHeight = picture.getDyaGoal();
            if (aspectRatioY > 0) {
                imageHeight *= aspectRatioY / 1000f;
            }
            imageHeight /= AbstractWordUtils.TWIPS_PER_INCH;

            String url = fileManager.savePicture( picture.getContent(),
                    picture.suggestPictureType(),
                    picture.suggestFullFileName(), imageWidth, imageHeight );

            if ( WordToFoUtils.isNotEmpty( url ) )
            {
                processImage( currentBlock, inlined, picture, url );
                return;
            }
        }

        processImageWithoutPicturesManager( currentBlock, inlined, picture );

    }

    protected abstract void processImage( Element currentBlock,
            boolean inlined, Picture picture, String url );

    @Internal
    protected abstract void processImageWithoutPicturesManager(
            Element currentBlock, boolean inlined, Picture picture );

    protected abstract void processLineBreak( Element block,
            CharacterRun characterRun );

    protected void processNoteAnchor( HWPFDocument doc,
            CharacterRun characterRun, final Element block )
    {
        {
            Notes footnotes = doc.getFootnotes();
            int noteIndex = footnotes
                    .getNoteIndexByAnchorPosition( characterRun
                            .getStartOffset() );
            if ( noteIndex != -1 )
            {
                Range footnoteRange = doc.getFootnoteRange();
                int rangeStartOffset = footnoteRange.getStartOffset();
                int noteTextStartOffset = footnotes
                        .getNoteTextStartOffset( noteIndex );
                int noteTextEndOffset = footnotes
                        .getNoteTextEndOffset( noteIndex );

                Range noteTextRange = new Range( rangeStartOffset
                        + noteTextStartOffset, rangeStartOffset
                        + noteTextEndOffset, doc );

                processFootnoteAutonumbered( doc, noteIndex, block,
                        noteTextRange );
                return;
            }
        }
        {
            Notes endnotes = doc.getEndnotes();
            int noteIndex = endnotes.getNoteIndexByAnchorPosition( characterRun
                    .getStartOffset() );
            if ( noteIndex != -1 )
            {
                Range endnoteRange = doc.getEndnoteRange();
                int rangeStartOffset = endnoteRange.getStartOffset();
                int noteTextStartOffset = endnotes
                        .getNoteTextStartOffset( noteIndex );
                int noteTextEndOffset = endnotes
                        .getNoteTextEndOffset( noteIndex );

                Range noteTextRange = new Range( rangeStartOffset
                        + noteTextStartOffset, rangeStartOffset
                        + noteTextEndOffset, doc );

                processEndnoteAutonumbered( doc, noteIndex, block,
                        noteTextRange );
            }
        }
    }

    private boolean processOle2( HWPFDocument doc, CharacterRun characterRun,
            Element block )
    {
        Entry entry = doc.getObjectsPool().getObjectById(
                "_" + characterRun.getPicOffset() );
        if ( entry == null )
        {
            logger.log( POILogger.WARN, "Referenced OLE2 object '",
                    Integer.valueOf( characterRun.getPicOffset() ),
                    "' not found in ObjectPool" );
            return false;
        }

        try
        {
            return processOle2( doc, block, entry );
        }
        catch ( Exception exc )
        {
            logger.log( POILogger.WARN,
                    "Unable to convert internal OLE2 object '",
                    Integer.valueOf( characterRun.getPicOffset() ), "': ", exc,
                    exc );
            return false;
        }
    }

    protected boolean processOle2( HWPFDocument wordDocument, Element block,
            Entry entry ) throws Exception
    {
        return false;
    }

    protected abstract void processPageBreak( HWPFDocumentCore wordDocument,
            Element flow );

    protected abstract void processPageref( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref );

    protected abstract void processParagraph( HWPFDocumentCore wordDocument,
            Element parentElement, int currentTableLevel, Paragraph paragraph,
            String bulletText );

    protected void processParagraphes( HWPFDocumentCore wordDocument,
            Element flow, Range range, int currentTableLevel )
    {
        final int paragraphs = range.numParagraphs();
        for ( int p = 0; p < paragraphs; p++ )
        {
            Paragraph paragraph = range.getParagraph( p );

            if ( paragraph.isInTable()
                    && paragraph.getTableLevel() != currentTableLevel )
            {
                if ( paragraph.getTableLevel() < currentTableLevel )
                    throw new IllegalStateException(
                            "Trying to process table cell with higher level ("
                                    + paragraph.getTableLevel()
                                    + ") than current table level ("
                                    + currentTableLevel
                                    + ") as inner table part" );

                Table table = range.getTable( paragraph );
                processTable( wordDocument, flow, table );

                p += table.numParagraphs();
                p--;
                continue;
            }

            if ( paragraph.text().equals( "\u000c" ) )
            {
                processPageBreak( wordDocument, flow );
            }

            boolean processed = false;
            if ( paragraph.isInList() )
            {
                try
                {
                    HWPFList hwpfList = paragraph.getList();

                    String label = AbstractWordUtils.getBulletText(
                            numberingState, hwpfList,
                            (char) paragraph.getIlvl() );

                    processParagraph( wordDocument, flow, currentTableLevel,
                            paragraph, label );
                    processed = true;
                }
                catch ( Exception exc )
                {
                    log.log(
                            POILogger.WARN,
                            "Can't process paragraph as list entry, will be processed without list information",
                            exc );
                }
            }

            if (!processed)
            {
                processParagraph( wordDocument, flow, currentTableLevel,
                        paragraph, AbstractWordUtils.EMPTY );
            }
        }

    }

    protected abstract void processSection( HWPFDocumentCore wordDocument,
            Section section, int s );

    protected void processSingleSection( HWPFDocumentCore wordDocument,
            Section section )
    {
        processSection( wordDocument, section, 0 );
    }

    protected void processSymbol( HWPFDocument doc, CharacterRun characterRun,
            Element block )
    {

    }

    protected abstract void processTable( HWPFDocumentCore wordDocument,
            Element flow, Table table );

    public void setFontReplacer( FontReplacer fontReplacer )
    {
        this.fontReplacer = fontReplacer;
    }

    public void setPicturesManager( PicturesManager fileManager )
    {
        this.picturesManager = fileManager;
    }

    protected int tryDeadField( HWPFDocumentCore wordDocument, Range range,
            int currentTableLevel, int beginMark, Element currentBlock )
    {
        int[] separatorEnd = tryDeadField_lookupFieldSeparatorEnd(
                wordDocument, range, beginMark );
        if ( separatorEnd == null )
            return beginMark;

        processDeadField( wordDocument, currentBlock, range, currentTableLevel,
                beginMark, separatorEnd[0], separatorEnd[1] );

        return separatorEnd[1];
    }

    private int[] tryDeadField_lookupFieldSeparatorEnd(
            HWPFDocumentCore wordDocument, Range range, int beginMark )
    {
        int separatorMark = -1;
        int endMark = -1;
        for ( int c = beginMark + 1; c < range.numCharacterRuns(); c++ )
        {
            CharacterRun characterRun = range.getCharacterRun( c );

            String text = characterRun.text();
            if ( text.isEmpty() ) continue;

            final char firstByte = text.charAt(0);
            if ( firstByte == FIELD_BEGIN_MARK )
            {
                int[] nested = tryDeadField_lookupFieldSeparatorEnd(
                        wordDocument, range, c );
                if ( nested != null )
                {
                    c = nested[1];
                }
                continue;
            }

            if ( firstByte == FIELD_SEPARATOR_MARK )
            {
                if ( separatorMark != -1 )
                {
                    // double; incorrect format
                    return null;
                }

                separatorMark = c;
                continue;
            }

            if ( firstByte == FIELD_END_MARK )
            {
                if ( endMark != -1 )
                {
                    // double;
                    return null;
                }

                endMark = c;
                break;
            }
        }

        if ( separatorMark == -1 || endMark == -1 )
            return null;

        return new int[] { separatorMark, endMark };
    }

}
