/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.*;

/**
 * @author Ryan Ackley
 */
public class SectionTable
{
  private static final int SED_SIZE = 12;

  private ArrayList _sections = new ArrayList();

  public SectionTable()
  {
  }

  public SectionTable(byte[] documentStream, byte[] tableStream, int offset,
                      int size, int fcMin)
  {
    PlexOfCps sedPlex = new PlexOfCps(tableStream, offset, size, SED_SIZE);

    int length = sedPlex.length();

    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = sedPlex.getProperty(x);
      SectionDescriptor sed = new SectionDescriptor(node.getBytes(), 0);

      int fileOffset = sed.getFc();

      // check for the optimization
      if (fileOffset == 0xffffffff)
      {
        _sections.add(new SEPX(sed, node.getStart(), node.getEnd(), new byte[0]));
      }
      else
      {
        // The first short at the offset is the size of the grpprl.
        int sepxSize = LittleEndian.getShort(documentStream, fileOffset);
        byte[] buf = new byte[sepxSize];
        fileOffset += LittleEndian.SHORT_SIZE;
        System.arraycopy(documentStream, fileOffset, buf, 0, buf.length);
        _sections.add(new SEPX(sed, node.getStart(), node.getEnd(), buf));
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
      GenericPropertyNode property = new GenericPropertyNode(sepx.getStart(), sepx.getEnd(), sed.toByteArray());
      plex.addProperty(property);

      offset = docStream.getOffset();
    }
    tableStream.write(plex.toByteArray());
  }

}
