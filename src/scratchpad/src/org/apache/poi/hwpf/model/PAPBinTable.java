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

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hwpf.model.io.*;
import org.apache.poi.hwpf.sprm.SprmBuffer;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;

/**
 * This class represents the bin table of Word document but it also serves as a
 * holder for all of the paragraphs of document that have been loaded into
 * memory.
 */
public class PAPBinTable
{
  ArrayList _paragraphs = new ArrayList();
  byte[] _dataStream;

  public PAPBinTable(byte[] documentStream, byte[] tableStream, byte[] dataStream, int offset,
                     int size, int fcMin)
  {
    PlexOfCps binTable = new PlexOfCps(tableStream, offset, size, 4);

    int length = binTable.length();
    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = binTable.getProperty(x);

      int pageNum = LittleEndian.getInt(node.getBytes());
      int pageOffset = POIFSConstants.BIG_BLOCK_SIZE * pageNum;

      PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(documentStream,
        dataStream, pageOffset, fcMin);

      int fkpSize = pfkp.size();

      for (int y = 0; y < fkpSize; y++)
      {
        _paragraphs.add(pfkp.getPAPX(y));
      }
    }
    _dataStream = dataStream;
  }

  public void insert(int listIndex, int cpStart, SprmBuffer buf)
  {
    PAPX forInsert = new PAPX(cpStart, cpStart, buf, _dataStream);
    if (listIndex == _paragraphs.size())
    {
       _paragraphs.add(forInsert);
    }
    else
    {
      PAPX currentPap = (PAPX)_paragraphs.get(listIndex);
      if (currentPap != null && currentPap.getStart() < cpStart)
      {
        SprmBuffer clonedBuf = null;
        try
        {
          clonedBuf = (SprmBuffer)currentPap.getSprmBuf().clone();
        }
        catch (CloneNotSupportedException exc)
        {
          exc.printStackTrace();
        }
        currentPap.setEnd(cpStart);
        PAPX splitPap = new PAPX(cpStart, currentPap.getEnd(), clonedBuf, _dataStream);
        _paragraphs.add(++listIndex, forInsert);
        _paragraphs.add(++listIndex, splitPap);
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

    PAPX papx = (PAPX)_paragraphs.get(endIndex);
    while (papx.getEnd() < endMark)
    {
      papx = (PAPX)_paragraphs.get(++endIndex);
    }
    if (listIndex == endIndex)
    {
      papx = (PAPX)_paragraphs.get(endIndex);
      papx.setEnd((papx.getEnd() - endMark) + offset);
    }
    else
    {
      papx = (PAPX)_paragraphs.get(listIndex);
      papx.setEnd(offset);
      for (int x = listIndex + 1; x < endIndex; x++)
      {
        papx = (PAPX)_paragraphs.get(x);
        papx.setStart(offset);
        papx.setEnd(offset);
      }
      papx = (PAPX)_paragraphs.get(endIndex);
      papx.setEnd((papx.getEnd() - endMark) + offset);
    }

    for (int x = endIndex + 1; x < size; x++)
    {
      papx = (PAPX)_paragraphs.get(x);
      papx.setStart(papx.getStart() - length);
      papx.setEnd(papx.getEnd() - length);
    }
  }


  public void adjustForInsert(int listIndex, int length)
  {
    int size = _paragraphs.size();
    PAPX papx = (PAPX)_paragraphs.get(listIndex);
    papx.setEnd(papx.getEnd() + length);

    for (int x = listIndex + 1; x < size; x++)
    {
      papx = (PAPX)_paragraphs.get(x);
      papx.setStart(papx.getStart() + length);
      papx.setEnd(papx.getEnd() + length);
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

      PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(_dataStream);
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
      binTable.addProperty(new GenericPropertyNode(start, end, intHolder));

    }
    while (overflow != null);
    tableStream.write(binTable.toByteArray());
  }


}

