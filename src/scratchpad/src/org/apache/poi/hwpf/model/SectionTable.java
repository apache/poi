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

  protected ArrayList _sections = new ArrayList();
  protected List _text;

  public SectionTable()
  {
  }


  public SectionTable(byte[] documentStream, byte[] tableStream, int offset,
                      int size, int fcMin,
                      List tpt)
  {
    PlexOfCps sedPlex = new PlexOfCps(tableStream, offset, size, SED_SIZE);
    _text = tpt;

    int length = sedPlex.length();

    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = sedPlex.getProperty(x);
      SectionDescriptor sed = new SectionDescriptor(node.getBytes(), 0);

      int fileOffset = sed.getFc();

      // check for the optimization
      if (fileOffset == 0xffffffff)
      {
        _sections.add(new SEPX(sed, CPtoFC(node.getStart()), CPtoFC(node.getEnd()), new byte[0]));
      }
      else
      {
        // The first short at the offset is the size of the grpprl.
        int sepxSize = LittleEndian.getShort(documentStream, fileOffset);
        byte[] buf = new byte[sepxSize];
        fileOffset += LittleEndian.SHORT_SIZE;
        System.arraycopy(documentStream, fileOffset, buf, 0, buf.length);
        _sections.add(new SEPX(sed, CPtoFC(node.getStart()), CPtoFC(node.getEnd()), buf));
      }
    }
  }

  public void adjustForInsert(int listIndex, int length)
  {
    int size = _sections.size();
    SEPX sepx = (SEPX)_sections.get(listIndex);
    sepx.setEnd(sepx.getEnd() + length);

    for (int x = listIndex + 1; x < size; x++)
    {
      sepx = (SEPX)_sections.get(x);
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
        TP = (TextPiece)_text.get(i);

        if(CP >= TP.getCP()) break;
      }
      int FC = TP.getPieceDescriptor().getFilePosition();
      int offset = CP - TP.getCP();
      FC = FC+offset-((TextPiece)_text.get(0)).getPieceDescriptor().getFilePosition();
      return FC;
    }

    // Ryans code
    private int FCtoCP(int fc)
   {
     int size = _text.size();
     int cp = 0;
     for (int x = 0; x < size; x++)
     {
       TextPiece piece = (TextPiece)_text.get(x);

       if (fc <= piece.getEnd())
       {
         cp += (fc - piece.getStart());
         break;
       }
       else
       {
         cp += (piece.getEnd() - piece.getStart());
       }
     }
     return cp;
   }


  public ArrayList getSections()
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
      SEPX sepx = (SEPX)_sections.get(x);
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
      GenericPropertyNode property = new GenericPropertyNode(FCtoCP(sepx.getStart()), FCtoCP(sepx.getEnd()), sed.toByteArray());


      plex.addProperty(property);

      offset = docStream.getOffset();
    }
    tableStream.write(plex.toByteArray());
  }
}
