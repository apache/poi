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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableIterator;
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

    public abstract Document getDocument();

    protected abstract void outputCharacters( Element block,
            CharacterRun characterRun, String text );

    protected boolean processCharacters( HWPFDocumentCore hwpfDocument,
            int currentTableLevel, Paragraph paragraph, final Element block,
            List<CharacterRun> characterRuns, final int start, final int end )
    {
        boolean haveAnyText = false;

        for ( int c = start; c < end; c++ )
        {
            CharacterRun characterRun = characterRuns.get( c );

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
                int skipTo = tryField( hwpfDocument, paragraph,
                        currentTableLevel, characterRuns, c, block );

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

            outputCharacters( block, characterRun, text );

            haveAnyText |= text.trim().length() != 0;
        }

        return haveAnyText;
    }

    public void processDocument( HWPFDocumentCore wordDocument )
    {
        final Range range = wordDocument.getRange();
        for ( int s = 0; s < range.numSections(); s++ )
        {
            processSection( wordDocument, range.getSection( s ), s );
        }
    }

    protected void processField( HWPFDocumentCore wordDocument,
            Element currentBlock, Paragraph paragraph, int currentTableLevel,
            List<CharacterRun> characterRuns, int beginMark, int separatorMark,
            int endMark )
    {

        Pattern hyperlinkPattern = Pattern
                .compile( "[ \\t\\r\\n]*HYPERLINK \"(.*)\"[ \\t\\r\\n]*" );
        Pattern pagerefPattern = Pattern
                .compile( "[ \\t\\r\\n]*PAGEREF ([^ ]*)[ \\t\\r\\n]*\\\\h[ \\t\\r\\n]*" );

        if ( separatorMark - beginMark > 1 )
        {
            int index = beginMark + 1;
            CharacterRun firstAfterBegin = null;
            while ( index < separatorMark )
            {
                firstAfterBegin = paragraph.getCharacterRun( index );
                if ( firstAfterBegin == null )
                {
                    logger.log( POILogger.WARN,
                            "Paragraph " + paragraph.getStartOffset() + "--"
                                    + paragraph.getEndOffset()
                                    + " contains null CharacterRun #" + index );
                    index++;
                    continue;
                }
                break;
            }

            if ( firstAfterBegin != null )
            {
                final Matcher hyperlinkMatcher = hyperlinkPattern
                        .matcher( firstAfterBegin.text() );
                if ( hyperlinkMatcher.matches() )
                {
                    String hyperlink = hyperlinkMatcher.group( 1 );
                    processHyperlink( wordDocument, currentBlock, paragraph,
                            characterRuns, currentTableLevel, hyperlink,
                            separatorMark + 1, endMark );
                    return;
                }

                final Matcher pagerefMatcher = pagerefPattern
                        .matcher( firstAfterBegin.text() );
                if ( pagerefMatcher.matches() )
                {
                    String pageref = pagerefMatcher.group( 1 );
                    processPageref( wordDocument, currentBlock, paragraph,
                            characterRuns, currentTableLevel, pageref,
                            separatorMark + 1, endMark );
                    return;
                }
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
            processCharacters( wordDocument, currentTableLevel, paragraph,
                    currentBlock, characterRuns, separatorMark + 1, endMark );

        return;
    }

    protected abstract void processHyperlink( HWPFDocumentCore wordDocument,
            Element currentBlock, Paragraph paragraph,
            List<CharacterRun> characterRuns, int currentTableLevel,
            String hyperlink, int i, int endMark );

    protected abstract void processImage( Element currentBlock,
            boolean inlined, Picture picture );

    protected abstract void processPageref( HWPFDocumentCore wordDocument,
            Element currentBlock, Paragraph paragraph,
            List<CharacterRun> characterRuns, int currentTableLevel,
            String pageref, int beginTextInclusive, int endTextExclusive );

    protected abstract void processParagraph( HWPFDocumentCore wordDocument,
            Element parentFopElement, int currentTableLevel,
            Paragraph paragraph, String bulletText );

    protected abstract void processSection( HWPFDocumentCore wordDocument,
            Section section, int s );

    protected void processSectionParagraphes( HWPFDocumentCore wordDocument,
            Element flow, Range range, int currentTableLevel )
    {
        final Map<Integer, Table> allTables = new HashMap<Integer, Table>();
        for ( TableIterator tableIterator = AbstractWordUtils.newTableIterator(
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
            Element flow, Table table, int newTableLevel );

    protected int tryField( HWPFDocumentCore wordDocument, Paragraph paragraph,
            int currentTableLevel, List<CharacterRun> characterRuns,
            int beginMark, Element currentBlock )
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

        processField( wordDocument, currentBlock, paragraph, currentTableLevel,
                characterRuns, beginMark, separatorMark, endMark );

        return endMark;
    }

}
