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

package org.apache.poi.hwpf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.hwpf.sprm.SprmIterator;
import org.apache.poi.hwpf.sprm.SprmOperation;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

import static java.lang.System.currentTimeMillis;
import static org.apache.logging.log4j.util.Unbox.box;

/**
 * This class represents the bin table of Word document but it also serves as a
 * holder for all of the paragraphs of document that have been loaded into
 * memory.
 */
@Internal
public class PAPBinTable
{
    private static final Logger LOG = LogManager.getLogger(PAPBinTable.class);

    protected final ArrayList<PAPX> _paragraphs = new ArrayList<>();

    public PAPBinTable()
    {
    }

    public PAPBinTable( byte[] documentStream, byte[] tableStream,
            byte[] dataStream, int offset, int size,
            CharIndexTranslator charIndexTranslator )
    {
        long start = currentTimeMillis();

        {
            PlexOfCps binTable = new PlexOfCps( tableStream, offset, size, 4 );

            int length = binTable.length();
            for ( int x = 0; x < length; x++ )
            {
                GenericPropertyNode node = binTable.getProperty( x );

                int pageNum = LittleEndian.getInt( node.getBytes() );
                int pageOffset = POIFSConstants.SMALLER_BIG_BLOCK_SIZE * pageNum;

                PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(
                        documentStream, dataStream, pageOffset,
                        charIndexTranslator );

                for ( PAPX papx : pfkp.getPAPXs() )
                {
                    if ( papx != null )
                        _paragraphs.add( papx );
                }
            }
        }

        LOG.atDebug().log("PAPX tables loaded in {} ms ({} elements)", box(currentTimeMillis() - start),box(_paragraphs.size()));

        if ( _paragraphs.isEmpty() )
        {
            LOG.atWarn().log("PAPX FKPs are empty");
            _paragraphs.add( new PAPX( 0, 0, new SprmBuffer( 2 ) ) );
        }
    }

    public void rebuild( final StringBuilder docText,
            ComplexFileTable complexFileTable )
    {
        rebuild( docText, complexFileTable, _paragraphs );
    }

    static void rebuild( final StringBuilder docText,
            ComplexFileTable complexFileTable, List<PAPX> paragraphs )
    {
        long start = currentTimeMillis();

        if ( complexFileTable != null )
        {
            SprmBuffer[] sprmBuffers = complexFileTable.getGrpprls();

            // adding PAPX from fast-saved SPRMs
            for ( TextPiece textPiece : complexFileTable.getTextPieceTable()
                    .getTextPieces() )
            {
                PropertyModifier prm = textPiece.getPieceDescriptor().getPrm();
                if ( !prm.isComplex() )
                    continue;
                int igrpprl = prm.getIgrpprl();

                if ( igrpprl < 0 || igrpprl >= sprmBuffers.length )
                {
                    LOG.atWarn().log("{}'s PRM references to unknown grpprl", textPiece);
                    continue;
                }

                boolean hasPap = false;
                SprmBuffer sprmBuffer = sprmBuffers[igrpprl];
                for ( SprmIterator iterator = sprmBuffer.iterator(); iterator
                        .hasNext(); )
                {
                    SprmOperation sprmOperation = iterator.next();
                    if ( sprmOperation.getType() == SprmOperation.TYPE_PAP )
                    {
                        hasPap = true;
                        break;
                    }
                }

                if ( hasPap )
                {
                    SprmBuffer newSprmBuffer = new SprmBuffer( 2 );
                    newSprmBuffer.append( sprmBuffer.toByteArray() );

                    PAPX papx = new PAPX( textPiece.getStart(),
                            textPiece.getEnd(), newSprmBuffer );
                    paragraphs.add( papx );
                }
            }

            LOG.atDebug().log("Merged (?) with PAPX from complex file table in {} ms ({} elements in total)", box(currentTimeMillis() - start),box(paragraphs.size()));
            start = currentTimeMillis();
        }

        List<PAPX> oldPapxSortedByEndPos = new ArrayList<>(paragraphs);
        oldPapxSortedByEndPos.sort(PropertyNode.EndComparator);

        LOG.atDebug().log("PAPX sorted by end position in {} ms", box(currentTimeMillis() - start));
        start = currentTimeMillis();

        final Map<PAPX, Integer> papxToFileOrder = new IdentityHashMap<>();
        {
            int counter = 0;
            for ( PAPX papx : paragraphs )
            {
                papxToFileOrder.put( papx, Integer.valueOf( counter++ ) );
            }
        }
        final Comparator<PAPX> papxFileOrderComparator = (o1, o2) -> {
            Integer i1 = papxToFileOrder.get( o1 );
            Integer i2 = papxToFileOrder.get( o2 );
            return i1.compareTo( i2 );
        };

        LOG.atDebug().log("PAPX's order map created in {} ms", box(currentTimeMillis() - start));
        start = currentTimeMillis();

        List<PAPX> newPapxs = new LinkedList<>();
        int lastParStart = 0;
        int lastPapxIndex = 0;
        for ( int charIndex = 0; charIndex < docText.length(); charIndex++ )
        {
            final char c = docText.charAt( charIndex );
            if ( c != 13 && c != 7 && c != 12 )
                continue;

            final int startInclusive = lastParStart;
            final int endExclusive = charIndex + 1;

            boolean broken = false;
            List<PAPX> papxs = new LinkedList<>();
            for ( int papxIndex = lastPapxIndex; papxIndex < oldPapxSortedByEndPos
                    .size(); papxIndex++ )
            {
                broken = false;
                PAPX papx = oldPapxSortedByEndPos.get( papxIndex );

                assert startInclusive == 0
                        || papxIndex + 1 == oldPapxSortedByEndPos.size()
                        || papx.getEnd() > startInclusive;

                if ( papx.getEnd() - 1 > charIndex )
                {
                    lastPapxIndex = papxIndex;
                    broken = true;
                    break;
                }

                papxs.add( papx );
            }
            if ( !broken )
            {
                lastPapxIndex = oldPapxSortedByEndPos.size() - 1;
            }

            if ( papxs.isEmpty() )
            {
                LOG.atWarn().log("Paragraph [{}; {}) has no PAPX. Creating new one.", box(startInclusive),box(endExclusive));
                // create it manually
                PAPX papx = new PAPX( startInclusive, endExclusive,
                        new SprmBuffer( 2 ) );
                newPapxs.add( papx );

                lastParStart = endExclusive;
                continue;
            }

            if ( papxs.size() == 1 )
            {
                // can we reuse existing?
                PAPX existing = papxs.get( 0 );
                if ( existing.getStart() == startInclusive
                        && existing.getEnd() == endExclusive )
                {
                    newPapxs.add( existing );
                    lastParStart = endExclusive;
                    continue;
                }
            }

            // restore file order of PAPX
            papxs.sort(papxFileOrderComparator);

            SprmBuffer sprmBuffer = null;
            for ( PAPX papx : papxs )
            {
                if ( papx.getGrpprl() == null || papx.getGrpprl().length <= 2 )
                    continue;

                if ( sprmBuffer == null ) {
                    sprmBuffer = papx.getSprmBuf().copy();
                } else {
                    sprmBuffer.append( papx.getGrpprl(), 2 );
                }
            }
            PAPX newPapx = new PAPX( startInclusive, endExclusive, sprmBuffer );
            newPapxs.add( newPapx );

            lastParStart = endExclusive;
            continue;
        }
        paragraphs.clear();
        paragraphs.addAll( newPapxs );

        LOG.atDebug().log("PAPX rebuilded from document text in {} ms ({} elements)", box(currentTimeMillis() - start),box(paragraphs.size()));
    }

    public void insert(int listIndex, int cpStart, SprmBuffer buf)
    {

        PAPX forInsert = new PAPX(0, 0, buf);

        // Ensure character offsets are really characters
        forInsert.setStart(cpStart);
        forInsert.setEnd(cpStart);

        if (listIndex == _paragraphs.size())
        {
             _paragraphs.add(forInsert);
        }
        else
        {
            PAPX currentPap = _paragraphs.get(listIndex);
            if (currentPap != null && currentPap.getStart() < cpStart)
            {
                SprmBuffer clonedBuf = currentPap.getSprmBuf().copy();

                // Copy the properties of the one before to afterwards
                // Will go:
                //    Original, until insert at point
                //    New one
                //    Clone of original, on to the old end
                PAPX clone = new PAPX(0, 0, clonedBuf);
                // Again ensure contains character based offsets no matter what
                clone.setStart(cpStart);
                clone.setEnd(currentPap.getEnd());

                currentPap.setEnd(cpStart);

                _paragraphs.add(listIndex + 1, forInsert);
                _paragraphs.add(listIndex + 2, clone);
            }
            else
            {
                _paragraphs.add(listIndex, forInsert);
            }
        }

    }

    public void adjustForDelete(int listIndex, int offset, int length)
    {
        int size = _paragraphs.size();
        int endMark = offset + length;
        int endIndex = listIndex;

        PAPX papx = _paragraphs.get(endIndex);
        while (papx.getEnd() < endMark)
        {
            papx = _paragraphs.get(++endIndex);
        }
        if (listIndex == endIndex)
        {
            papx = _paragraphs.get(endIndex);
            papx.setEnd((papx.getEnd() - endMark) + offset);
        }
        else
        {
            papx = _paragraphs.get(listIndex);
            papx.setEnd(offset);
            for (int x = listIndex + 1; x < endIndex; x++)
            {
                papx = _paragraphs.get(x);
                papx.setStart(offset);
                papx.setEnd(offset);
            }
            papx = _paragraphs.get(endIndex);
            papx.setEnd((papx.getEnd() - endMark) + offset);
        }

        for (int x = endIndex + 1; x < size; x++)
        {
            papx = _paragraphs.get(x);
            papx.setStart(papx.getStart() - length);
            papx.setEnd(papx.getEnd() - length);
        }
    }


    public void adjustForInsert(int listIndex, int length)
    {
        int size = _paragraphs.size();
        PAPX papx = _paragraphs.get(listIndex);
        papx.setEnd(papx.getEnd() + length);

        for (int x = listIndex + 1; x < size; x++)
        {
            papx = _paragraphs.get(x);
            papx.setStart(papx.getStart() + length);
            papx.setEnd(papx.getEnd() + length);
        }
    }


    public ArrayList<PAPX> getParagraphs()
    {
        return _paragraphs;
    }

    public void writeTo( ByteArrayOutputStream wordDocumentStream,
            ByteArrayOutputStream tableStream, CharIndexTranslator translator )
            throws IOException
    {

        PlexOfCps binTable = new PlexOfCps(4);

        // each FKP must start on a 512 byte page.
        int docOffset = wordDocumentStream.size();
        int mod = docOffset % POIFSConstants.SMALLER_BIG_BLOCK_SIZE;
        if (mod != 0)
        {
            byte[] padding = new byte[POIFSConstants.SMALLER_BIG_BLOCK_SIZE - mod];
            wordDocumentStream.write(padding);
        }

        // get the page number for the first fkp
        docOffset = wordDocumentStream.size();
        int pageNum = docOffset/POIFSConstants.SMALLER_BIG_BLOCK_SIZE;

        // get the ending fc
        // int endingFc = _paragraphs.get(_paragraphs.size() - 1).getEnd();
        // endingFc += fcMin;
        int endingFc = translator.getByteIndex( _paragraphs.get(_paragraphs.size() - 1 ).getEnd() );

        ArrayList<PAPX> overflow = _paragraphs;
        do
        {
            PAPX startingProp = overflow.get(0);

            // int start = startingProp.getStart() + fcMin;
            int start = translator.getByteIndex( startingProp.getStart() );

            PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage();
            pfkp.fill(overflow);

            byte[] bufFkp = pfkp.toByteArray(tableStream, translator);
            wordDocumentStream.write(bufFkp);
            overflow = pfkp.getOverflow();

            int end = endingFc;
            if (overflow != null)
            {
                // end = overflow.get(0).getStart() + fcMin;
                end = translator.getByteIndex( overflow.get( 0 ).getStart() );
            }

            byte[] intHolder = new byte[4];
            LittleEndian.putInt(intHolder, 0, pageNum++);
            binTable.addProperty(new GenericPropertyNode(start, end, intHolder));

        }
        while (overflow != null);
        tableStream.write(binTable.toByteArray());
    }
}
