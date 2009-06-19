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
import java.io.OutputStream;

import org.apache.poi.hwpf.model.io.*;
import org.apache.poi.hwpf.sprm.SprmBuffer;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;

/**
 * This class represents the bin table of Word document but it also serves as a
 * holder for all of the paragraphs of document that have been loaded into
 * memory.
 *
 * @author Ryan Ackley
 */
public final class PAPBinTable
{
  protected ArrayList _paragraphs = new ArrayList();
  byte[] _dataStream;

  /** So we can know if things are unicode or not */
  private TextPieceTable tpt;

  public PAPBinTable()
  {
  }

  public PAPBinTable(byte[] documentStream, byte[] tableStream, byte[] dataStream, int offset,
                     int size, int fcMin, TextPieceTable tpt)
  {
    PlexOfCps binTable = new PlexOfCps(tableStream, offset, size, 4);
    this.tpt = tpt;

    int length = binTable.length();
    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = binTable.getProperty(x);

      int pageNum = LittleEndian.getInt(node.getBytes());
      int pageOffset = POIFSConstants.BIG_BLOCK_SIZE * pageNum;

      PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(documentStream,
        dataStream, pageOffset, fcMin, tpt);

      int fkpSize = pfkp.size();

      for (int y = 0; y < fkpSize; y++)
      {
    	PAPX papx = pfkp.getPAPX(y);
        _paragraphs.add(papx);
      }
    }
    _dataStream = dataStream;
  }

  public void insert(int listIndex, int cpStart, SprmBuffer buf)
  {

    PAPX forInsert = new PAPX(0, 0, tpt, buf, _dataStream);

    // Ensure character offsets are really characters
    forInsert.setStart(cpStart);
    forInsert.setEnd(cpStart);

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

    	// Copy the properties of the one before to afterwards
    	// Will go:
    	//  Original, until insert at point
    	//  New one
    	//  Clone of original, on to the old end
        PAPX clone = new PAPX(0, 0, tpt, clonedBuf, _dataStream);
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

