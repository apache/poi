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
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hwpf.model.io.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;

public class PAPBinTable
{
  ArrayList _paragraphs = new ArrayList();

  public PAPBinTable(byte[] documentStream, byte[] tableStream, int offset,
                     int size, int fcMin)
  {
    PlexOfCps binTable = new PlexOfCps(tableStream, offset, size, 4);

    int length = binTable.length();
    for (int x = 0; x < length; x++)
    {
      PropertyNode node = binTable.getProperty(x);

      int pageNum = LittleEndian.getInt(node.getBuf());
      int pageOffset = POIFSConstants.BIG_BLOCK_SIZE * pageNum;

      PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(documentStream,
        pageOffset, fcMin);

      int fkpSize = pfkp.size();

      for (int y = 0; y < fkpSize; y++)
      {
        _paragraphs.add(pfkp.getPAPX(y));
      }
    }
  }

  public ArrayList getParagraphs()
  {
    return _paragraphs;
  }

  public void writeTo(HWPFFileSystem sys, int fcMin)
    throws IOException
  {

    HWPFOutputStream docStream = sys.getStream("WordDocument");
    OutputStream tableStream = sys.getStream("1Table");

    PlexOfCps binTable = new PlexOfCps(4);

    // each FKP must start on a 512 byte page.
    int docOffset = docStream.getOffset();
    int mod = docOffset % POIFSConstants.BIG_BLOCK_SIZE;
    if (mod != 0)
    {
      byte[] padding = new byte[POIFSConstants.BIG_BLOCK_SIZE - mod];
      docStream.write(padding);
    }

    // get the page number for the first fkp
    docOffset = docStream.getOffset();
    int pageNum = docOffset/POIFSConstants.BIG_BLOCK_SIZE;

    // get the ending fc
    int endingFc = ((PropertyNode)_paragraphs.get(_paragraphs.size() - 1)).getEnd();
    endingFc += fcMin;


    ArrayList overflow = _paragraphs;
    do
    {
      PropertyNode startingProp = (PropertyNode)overflow.get(0);
      int start = startingProp.getStart() + fcMin;

      PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage();
      pfkp.fill(overflow);

      byte[] bufFkp = pfkp.toByteArray(fcMin);
      docStream.write(bufFkp);
      overflow = pfkp.getOverflow();

      int end = endingFc;
      if (overflow != null)
      {
        end = ((PropertyNode)overflow.get(0)).getStart() + fcMin;
      }

      byte[] intHolder = new byte[4];
      LittleEndian.putInt(intHolder, pageNum++);
      binTable.addProperty(new PropertyNode(start, end, intHolder));

    }
    while (overflow != null);
    tableStream.write(binTable.toByteArray());
  }


}

