/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */


package org.apache.poi.hwpf.model.hdftypes;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.*;

public class SectionTable
{
  private static final int SED_SIZE = 12;

  private ArrayList _sections;

  public SectionTable(byte[] documentStream, byte[] tableStream, int offset,
                      int size)
  {
    PlexOfCps sedPlex = new PlexOfCps(tableStream, offset, size, SED_SIZE);

    int length = sedPlex.length();

    for (int x = 0; x < length; x++)
    {
      PropertyNode node = sedPlex.getProperty(x);
      SectionDescriptor sed = new SectionDescriptor(node.getBuf(), 0);

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

  public void writeTo(HWPFFileSystem sys)
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
      PropertyNode property = new PropertyNode(sepx.getStart(), sepx.getEnd(), sed.toByteArray());
      plex.addProperty(property);

      offset = docStream.getOffset();
    }
    tableStream.write(plex.toByteArray());
  }

}
