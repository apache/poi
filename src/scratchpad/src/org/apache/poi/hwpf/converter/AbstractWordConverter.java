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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.FontReplacer.Triplet;
import org.apache.poi.hwpf.model.Field;
import org.apache.poi.hwpf.model.FieldsTables;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractWordConverter
{
    private static final byte BEL_MARK = 7;

    private static final byte FIELD_BEGIN_MARK = 19;

    private static final byte FIELD_END_MARK = 21;

    private static final byte FIELD_SEPARATOR_MARK = 20;

    private static final POILogger logger = POILogFactory
            .getLogger( AbstractWordConverter.class );

    private FontReplacer fontReplacer = new DefaultFontReplacer();

    protected Triplet getCharacterRunTriplet( CharacterRun characterRun )
    {
        Triplet original = new Triplet();
        original.bold = characterRun.isBold();
        original.italic = characterRun.isItalic();
        original.fontName = characterRun.getFontName();
        Triplet updated = getFontReplacer().update( original );
        return updated;
    }

    public abstract Document getDocument();

    public FontReplacer getFontReplacer()
    {
        return fontReplacer;
    }

    protected abstract void outputCharacters( Element block,
            CharacterRun characterRun, String text );

    protected boolean processCharacters( HWPFDocumentCore hwpfDocument,
            int currentTableLevel, Range range, final Element block )
    {
        if ( range == null )
            return false;

        boolean haveAnyText = false;

        for ( int c = 0; c < range.numCharacterRuns(); c++ )
        {
            CharacterRun characterRun = range.getCharacterRun( c );

            if ( characterRun == null )
                throw new AssertionError();

            if ( hwpfDocument instanceof HWPFDocument
                    && ( (HWPFDocument) hwpfDocument ).getPicturesTable()
                            .hasPicture( characterRun ) )
            {
                HWPFDocument newFormat = (HWPFDocument) hwpfDocument;
                Picture picture = newFormat.getPicturesTable().extractPicture(
                        characterRun, true );

                processImage( block, characterRun.text().charAt( 0 ) == 0x01,
                        picture );
                continue;
            }

            String text = characterRun.text();
            if ( text.getBytes().length == 0 )
                continue;

            if ( text.getBytes()[0] == FIELD_BEGIN_MARK )
            {
                if ( hwpfDocument instanceof HWPFDocument )
                {
                    Field aliveField = ( (HWPFDocument) hwpfDocument )
                            .getFieldsTables().lookupFieldByStartOffset(
                                    FieldsTables.PLCFFLDMOM,
                                    characterRun.getStartOffset() );
                    if ( aliveField != null )
                    {
                        processField( ( (HWPFDocument) hwpfDocument ), range,
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

                int skipTo = tryDeadField( hwpfDocument, range,
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

            if ( text.endsWith( "\r" )
                    || ( text.charAt( text.length() - 1 ) == BEL_MARK && currentTableLevel != 0 ) )
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
                    else
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

        return;
    }

    public void processDocument( HWPFDocumentCore wordDocument )
    {
        final SummaryInformation summaryInformation = wordDocument
                .getSummaryInformation();
        if ( summaryInformation != null )
        {
            processDocumentInformation( summaryInformation );
        }

        final Range range = wordDocument.getRange();
        for ( int s = 0; s < range.numSections(); s++ )
        {
            processSection( wordDocument, range.getSection( s ), s );
        }
    }

    protected abstract void processDocumentInformation(
            SummaryInformation summaryInformation );

    protected void processField( HWPFDocument hwpfDocument, Range parentRange,
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
                Pattern pagerefPattern = Pattern
                        .compile( "[ \\t\\r\\n]*PAGEREF ([^ ]*)[ \\t\\r\\n]*\\\\h[ \\t\\r\\n]*" );
                Matcher matcher = pagerefPattern.matcher( formula );
                if ( matcher.find() )
                {
                    String pageref = matcher.group( 1 );
                    processPageref( hwpfDocument, currentBlock,
                            field.secondSubrange( parentRange ),
                            currentTableLevel, pageref );
                    return;
                }
            }
            break;
        }
        case 88: // hyperlink
        {
            final Range firstSubrange = field.firstSubrange( parentRange );
            if ( firstSubrange != null )
            {
                String formula = firstSubrange.text();
                Pattern hyperlinkPattern = Pattern
                        .compile( "[ \\t\\r\\n]*HYPERLINK \"(.*)\"[ \\t\\r\\n]*" );
                Matcher matcher = hyperlinkPattern.matcher( formula );
                if ( matcher.find() )
                {
                    String hyperlink = matcher.group( 1 );
                    processHyperlink( hwpfDocument, currentBlock,
                            field.secondSubrange( parentRange ),
                            currentTableLevel, hyperlink );
                    return;
                }
            }
            break;
        }
        }

        logger.log( POILogger.WARN, parentRange + " contains " + field
                + " with unsupported type or format" );
        processCharacters( hwpfDocument, currentTableLevel,
                field.secondSubrange( parentRange ), currentBlock );
    }

    protected Field processField( HWPFDocumentCore wordDocument,
            Range charactersRange, int currentTableLevel, int startOffset,
            Element currentBlock )
    {
        if ( !( wordDocument instanceof HWPFDocument ) )
            return null;

        HWPFDocument hwpfDocument = (HWPFDocument) wordDocument;
        Field field = hwpfDocument.getFieldsTables().lookupFieldByStartOffset(
                FieldsTables.PLCFFLDMOM, startOffset );
        if ( field == null )
            return null;

        processField( hwpfDocument, charactersRange, currentTableLevel, field,
                currentBlock );

        return field;
    }

    protected abstract void processHyperlink( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String hyperlink );

    protected abstract void processImage( Element currentBlock,
            boolean inlined, Picture picture );

    protected abstract void processLineBreak( Element block,
            CharacterRun characterRun );

    protected abstract void processPageref( HWPFDocumentCore wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref );

    protected abstract void processParagraph( HWPFDocumentCore wordDocument,
            Element parentFopElement, int currentTableLevel,
            Paragraph paragraph, String bulletText );

    protected abstract void processSection( HWPFDocumentCore wordDocument,
            Section section, int s );

    protected void processSectionParagraphes( HWPFDocumentCore wordDocument,
            Element flow, Range range, int currentTableLevel )
    {
        final ListTables listTables = wordDocument.getListTables();
        int currentListInfo = 0;

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

                    String label = AbstractWordUtils.getBulletText( listTables,
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
                            paragraph, AbstractWordUtils.EMPTY );
                }
            }
            else
            {
                processParagraph( wordDocument, flow, currentTableLevel,
                        paragraph, AbstractWordUtils.EMPTY );
            }
        }

    }

    protected void processSingleSection( HWPFDocumentCore wordDocument,
            Section section )
    {
        processSection( wordDocument, section, 0 );
    }

    protected abstract void processTable( HWPFDocumentCore wordDocument,
            Element flow, Table table );

    public void setFontReplacer( FontReplacer fontReplacer )
    {
        this.fontReplacer = fontReplacer;
    }

    protected int tryDeadField( HWPFDocumentCore wordDocument, Range range,
            int currentTableLevel, int beginMark, Element currentBlock )
    {
        int separatorMark = -1;
        int endMark = -1;
        for ( int c = beginMark + 1; c < range.numCharacterRuns(); c++ )
        {
            CharacterRun characterRun = range.getCharacterRun( c );

            String text = characterRun.text();
            if ( text.getBytes().length == 0 )
                continue;

            if ( text.getBytes()[0] == FIELD_BEGIN_MARK )
            {
                // nested?
                Field possibleField = processField( wordDocument, range,
                        currentTableLevel, characterRun.getStartOffset(),
                        currentBlock );
                if ( possibleField != null )
                {
                    c = possibleField.getFieldEndOffset();
                }
                else
                {
                    continue;
                }
            }

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

        processDeadField( wordDocument, currentBlock, range, currentTableLevel,
                beginMark, separatorMark, endMark );

        return endMark;
    }

}
