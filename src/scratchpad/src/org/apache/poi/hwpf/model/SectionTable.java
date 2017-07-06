/* ====================================================================
     Licensed to the Apache Software Foundation (ASF) under one or more
     contributor license agreements.    See the NOTICE file distributed with
     this work for additional information regarding copyright ownership.
     The ASF licenses this file to You under the Apache License, Version 2.0
     (the "License"); you may not use this file except in compliance with
     the License.    You may obtain a copy of the License at

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
import java.util.Collections;
import java.util.List;

import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * @author Ryan Ackley
 */
@Internal
public class SectionTable
{
    private final static POILogger _logger = POILogFactory.getLogger(SectionTable.class);
    private static final int SED_SIZE = 12;

    protected List<SEPX> _sections = new ArrayList<SEPX>();
    protected List<TextPiece> _text;

    /** So we can know if things are unicode or not */
    //private TextPieceTable tpt;

    public SectionTable()
    {
    }


    public SectionTable(
            byte[] documentStream, byte[] tableStream,
            int offset, int size, int fcMin, TextPieceTable tpt, int mainLength)
    {
        PlexOfCps sedPlex = new PlexOfCps(tableStream, offset, size, SED_SIZE);
        //this.tpt = tpt;
        this._text = tpt.getTextPieces();

        int length = sedPlex.length();

        for (int x = 0; x < length; x++)
        {
            GenericPropertyNode node = sedPlex.getProperty(x);
            SectionDescriptor sed = new SectionDescriptor(node.getBytes(), 0);

            int fileOffset = sed.getFc();
            // int startAt = CPtoFC(node.getStart());
            // int endAt = CPtoFC(node.getEnd());
            int startAt = node.getStart();
            int endAt = node.getEnd();

            // check for the optimization
            if (fileOffset == 0xffffffff)
            {
                _sections.add(new SEPX(sed, startAt, endAt, new byte[0]));
            }
            else
            {
                // The first short at the offset is the size of the grpprl.
                int sepxSize = LittleEndian.getShort(documentStream, fileOffset);
                byte[] buf = new byte[sepxSize];
                fileOffset += LittleEndian.SHORT_SIZE;
                System.arraycopy(documentStream, fileOffset, buf, 0, buf.length);
                _sections.add(new SEPX(sed, startAt, endAt, buf));
            }
        }

        // Some files seem to lie about their unicode status, which
        //    is very very pesky. Try to work around these, but this
        //    is getting on for black magic...
        int mainEndsAt = mainLength;
        boolean matchAt = false;
        boolean matchHalf = false;
        for (int i=0; i<_sections.size(); i++) {
            SEPX s = _sections.get(i);
            if (s.getEnd() == mainEndsAt) {
                matchAt = true;
            } else if(s.getEnd() == mainEndsAt || s.getEnd() == mainEndsAt-1) {
                matchHalf = true;
            }
        }
        if(! matchAt && matchHalf) {
            _logger.log(POILogger.WARN, "Your document seemed to be mostly unicode, but the section definition was in bytes! Trying anyway, but things may well go wrong!");
            for(int i=0; i<_sections.size(); i++) {
                SEPX s = _sections.get(i);
                GenericPropertyNode node = sedPlex.getProperty(i);

                // s.setStart( CPtoFC(node.getStart()) );
                // s.setEnd( CPtoFC(node.getEnd()) );
                int startAt = node.getStart();
                int endAt = node.getEnd();
                s.setStart( startAt );
                s.setEnd( endAt );
            }
        }

        Collections.sort( _sections, PropertyNode.StartComparator.instance );
    }

    public void adjustForInsert(int listIndex, int length)
    {
        int size = _sections.size();
        SEPX sepx = _sections.get(listIndex);
        sepx.setEnd(sepx.getEnd() + length);

        for (int x = listIndex + 1; x < size; x++)
        {
            sepx = _sections.get(x);
            sepx.setStart(sepx.getStart() + length);
            sepx.setEnd(sepx.getEnd() + length);
        }
    }

    // goss version of CPtoFC - this takes into account non-contiguous textpieces
    // that we have come across in real world documents. Tests against the example
    // code in HWPFDocument show no variation to Ryan's version of the code in
    // normal use, but this version works with our non-contiguous test case.
    // So far unable to get this test case to be written out as well due to
    // other issues. - piers
    //
    // i'm commenting this out, because it just doesn't work with non-contiguous
    // textpieces :( Usual (as for PAPX and CHPX) call to TextPiecesTable does.
    // private int CPtoFC(int CP)
    // {
    // TextPiece TP = null;
    //
    // for(int i=_text.size()-1; i>-1; i--)
    // {
    // TP = _text.get(i);
    //
    // if(CP >= TP.getCP()) break;
    // }
    // int FC = TP.getPieceDescriptor().getFilePosition();
    // int offset = CP - TP.getCP();
    // if (TP.isUnicode()) {
    // offset = offset*2;
    // }
    // FC = FC+offset;
    // return FC;
    // }

    public List<SEPX> getSections()
    {
        return _sections;
    }

    @Deprecated
    public void writeTo( HWPFFileSystem sys, int fcMin ) throws IOException
    {
        ByteArrayOutputStream docStream = sys.getStream( "WordDocument" );
        ByteArrayOutputStream tableStream = sys.getStream( "1Table" );

        writeTo( docStream, tableStream );
    }

    public void writeTo(
            ByteArrayOutputStream wordDocumentStream,
            ByteArrayOutputStream tableStream ) throws IOException
    {

        int offset = wordDocumentStream.size();
        int len = _sections.size();
        PlexOfCps plex = new PlexOfCps(SED_SIZE);

        for (int x = 0; x < len; x++)
        {
            SEPX sepx = _sections.get(x);
            byte[] grpprl = sepx.getGrpprl();

            // write the sepx to the document stream. starts with a 2 byte size
            // followed by the grpprl
            byte[] shortBuf = new byte[2];
            LittleEndian.putShort(shortBuf, 0, (short)grpprl.length);

            wordDocumentStream.write(shortBuf);
            wordDocumentStream.write(grpprl);

            // set the fc in the section descriptor
            SectionDescriptor sed = sepx.getSectionDescriptor();
            sed.setFc(offset);

            // add the section descriptor bytes to the PlexOfCps.

            /* original line */
            GenericPropertyNode property = new GenericPropertyNode(
                            sepx.getStart(), sepx.getEnd(), sed.toByteArray() );
            /*
             * Line using Ryan's FCtoCP() conversion method - unable to observe
             * any effect on our testcases when using this code - piers
             */
            /*
             * there is an effect on Bug45743.doc actually. writeoutreadback
             * changes byte offset of chars (but preserve string offsets).
             * Changing back to original lines - sergey
             */
            // GenericPropertyNode property = new GenericPropertyNode(
            // tpt.getCharIndex( sepx.getStartBytes() ),
            // tpt.getCharIndex( sepx.getEndBytes() ), sed.toByteArray() );

            plex.addProperty(property);

            offset = wordDocumentStream.size();
        }
        tableStream.write(plex.toByteArray());
    }
}
