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

import java.util.ArrayList;
import java.io.IOException;
import java.util.List;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.*;

/**
 * @author Ryan Ackley
 */
public class SectionTable
{
  private static final int SED_SIZE = 12;

  protected ArrayList<SEPX> _sections = new ArrayList<SEPX>();
  protected List<TextPiece> _text;

  /** So we can know if things are unicode or not */
  private TextPieceTable tpt;

  public SectionTable()
  {
  }


  public SectionTable(byte[] documentStream, byte[] tableStream, int offset,
                      int size, int fcMin,
                      TextPieceTable tpt, CPSplitCalculator cps)
  {
    PlexOfCps sedPlex = new PlexOfCps(tableStream, offset, size, SED_SIZE);
    this.tpt = tpt;
    this._text = tpt.getTextPieces();

    int length = sedPlex.length();

    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = sedPlex.getProperty(x);
      SectionDescriptor sed = new SectionDescriptor(node.getBytes(), 0);

      int fileOffset = sed.getFc();
      int startAt = CPtoFC(node.getStart());
      int endAt = CPtoFC(node.getEnd());

      // check for the optimization
      if (fileOffset == 0xffffffff)
      {
        _sections.add(new SEPX(sed, startAt, endAt, tpt, new byte[0]));
      }
      else
      {
        // The first short at the offset is the size of the grpprl.
        int sepxSize = LittleEndian.getShort(documentStream, fileOffset);
        byte[] buf = new byte[sepxSize];
        fileOffset += LittleEndian.SHORT_SIZE;
        System.arraycopy(documentStream, fileOffset, buf, 0, buf.length);
        _sections.add(new SEPX(sed, startAt, endAt, tpt, buf));
      }
    }

    // Some files seem to lie about their unicode status, which
    //  is very very pesky. Try to work around these, but this
    //  is getting on for black magic...
    int mainEndsAt = cps.getMainDocumentEnd();
    boolean matchAt = false;
    boolean matchHalf = false;
    for(int i=0; i<_sections.size(); i++) {
    	SEPX s = _sections.get(i);
    	if(s.getEnd() == mainEndsAt) {
    		matchAt = true;
    	} else if(s.getEndBytes() == mainEndsAt || s.getEndBytes() == mainEndsAt-1) {
    		matchHalf = true;
    	}
    }
    if(! matchAt && matchHalf) {
    	System.err.println("Your document seemed to be mostly unicode, but the section definition was in bytes! Trying anyway, but things may well go wrong!");
        for(int i=0; i<_sections.size(); i++) {
        	SEPX s = _sections.get(i);
            GenericPropertyNode node = sedPlex.getProperty(i);

        	s.setStart( CPtoFC(node.getStart()) );
        	s.setEnd( CPtoFC(node.getEnd()) );
        }
    }
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
   private int CPtoFC(int CP)
  {
      TextPiece TP = null;

      for(int i=_text.size()-1; i>-1; i--)
      {
        TP = _text.get(i);

        if(CP >= TP.getCP()) break;
      }
      int FC = TP.getPieceDescriptor().getFilePosition();
      int offset = CP - TP.getCP();
      if (TP.isUnicode()) {
        offset = offset*2;
      }
      FC = FC+offset;
      return FC;
    }

  public ArrayList<SEPX> getSections()
  {
    return _sections;
  }

  public void writeTo(HWPFFileSystem sys, int fcMin)
    throws IOException
  {
    HWPFOutputStream docStream = sys.getStream("WordDocument");
    HWPFOutputStream tableStream = sys.getStream("1Table");

    int offset = docStream.getOffset();
    int len = _sections.size();
    PlexOfCps plex = new PlexOfCps(SED_SIZE);

    for (int x = 0; x < len; x++)
    {
      SEPX sepx = _sections.get(x);
      byte[] grpprl = sepx.getGrpprl();

      // write the sepx to the document stream. starts with a 2 byte size
      // followed by the grpprl
      byte[] shortBuf = new byte[2];
      LittleEndian.putShort(shortBuf, (short)grpprl.length);

      docStream.write(shortBuf);
      docStream.write(grpprl);

      // set the fc in the section descriptor
      SectionDescriptor sed = sepx.getSectionDescriptor();
      sed.setFc(offset);

      // add the section descriptor bytes to the PlexOfCps.


      // original line -
      //GenericPropertyNode property = new GenericPropertyNode(sepx.getStart(), sepx.getEnd(), sed.toByteArray());

      // Line using Ryan's FCtoCP() conversion method -
      // unable to observe any effect on our testcases when using this code - piers
      GenericPropertyNode property = new GenericPropertyNode(tpt.getCharIndex(sepx.getStartBytes()), tpt.getCharIndex(sepx.getEndBytes()), sed.toByteArray());


      plex.addProperty(property);

      offset = docStream.getOffset();
    }
    tableStream.write(plex.toByteArray());
  }
}
