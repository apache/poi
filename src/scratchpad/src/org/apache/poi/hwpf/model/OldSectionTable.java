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

import org.apache.poi.util.LittleEndian;

/**
 * This class holds all of the section formatting 
 *  properties from Old (Word 6 / Word 95) documents.
 * Unlike with Word 97+, it all gets held in the
 *  same stream.
 * In common with the rest of the old support, it 
 *  is read only
 */
public final class OldSectionTable extends SectionTable
{
  public OldSectionTable(byte[] documentStream, int offset,
                      int size, int fcMin,
                      TextPieceTable tpt)
  {
    PlexOfCps sedPlex = new PlexOfCps(documentStream, offset, size, 12);

    int length = sedPlex.length();

    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = sedPlex.getProperty(x);
      SectionDescriptor sed = new SectionDescriptor(node.getBytes(), 0);

      int fileOffset = sed.getFc();
      int startAt = node.getStart();
      int endAt = node.getEnd();

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
  }
}
