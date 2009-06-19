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

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.util.LittleEndian;

/**
 * Represents a CHP fkp. The style properties for paragraph and character runs
 * are stored in fkps. There are PAP fkps for paragraph properties and CHP fkps
 * for character run properties. The first part of the fkp for both CHP and PAP
 * fkps consists of an array of 4 byte int offsets that represent a
 * Paragraph's or Character run's text offset in the main stream. The ending
 * offset is the next value in the array. For example, if an fkp has X number of
 * Paragraph's stored in it then there are (x + 1) 4 byte ints in the beginning
 * array. The number X is determined by the last byte in a 512 byte fkp.
 *
 * CHP and PAP fkps also store the compressed styles(grpprl) that correspond to
 * the offsets on the front of the fkp. The offset of the grpprls is determined
 * differently for CHP fkps and PAP fkps.
 *
 * @author Ryan Ackley
 */
public final class CHPFormattedDiskPage extends FormattedDiskPage
{
    private static final int FC_SIZE = 4;

    private ArrayList _chpxList = new ArrayList();
    private ArrayList _overFlow;


    public CHPFormattedDiskPage()
    {
    }

    /**
     * This constructs a CHPFormattedDiskPage from a raw fkp (512 byte array
     * read from a Word file).
     */
    public CHPFormattedDiskPage(byte[] documentStream, int offset, int fcMin, TextPieceTable tpt)
    {
      super(documentStream, offset);

      for (int x = 0; x < _crun; x++)
      {
    	int startAt = getStart(x);
		int endAt = getEnd(x);
		_chpxList.add(new CHPX(startAt, endAt, tpt, getGrpprl(x)));
      }
    }

    public CHPX getCHPX(int index)
    {
      return (CHPX)_chpxList.get(index);
    }

    public void fill(List filler)
    {
      _chpxList.addAll(filler);
    }

    public ArrayList getOverflow()
    {
      return _overFlow;
    }

    /**
     * Gets the chpx for the character run at index in this fkp.
     *
     * @param index The index of the chpx to get.
     * @return a chpx grpprl.
     */
    protected byte[] getGrpprl(int index)
    {
        int chpxOffset = 2 * LittleEndian.getUnsignedByte(_fkp, _offset + (((_crun + 1) * 4) + index));

        //optimization if offset == 0 use "Normal" style
        if(chpxOffset == 0)
        {
            return new byte[0];
        }

        int size = LittleEndian.getUnsignedByte(_fkp, _offset + chpxOffset);

        byte[] chpx = new byte[size];

        System.arraycopy(_fkp, _offset + ++chpxOffset, chpx, 0, size);
        return chpx;
    }

    protected byte[] toByteArray(int fcMin)
    {
      byte[] buf = new byte[512];
      int size = _chpxList.size();
      int grpprlOffset = 511;
      int offsetOffset = 0;
      int fcOffset = 0;

      // total size is currently the size of one FC
      int totalSize = FC_SIZE + 2;

      int index = 0;
      for (; index < size; index++)
      {
        int grpprlLength = ((CHPX)_chpxList.get(index)).getGrpprl().length;

        // check to see if we have enough room for an FC, the grpprl offset,
        // the grpprl size byte and the grpprl.
        totalSize += (FC_SIZE + 2 + grpprlLength);
        // if size is uneven we will have to add one so the first grpprl falls
        // on a word boundary
        if (totalSize > 511 + (index % 2))
        {
          totalSize -= (FC_SIZE + 2 + grpprlLength);
          break;
        }

        // grpprls must fall on word boundaries
        if ((1 + grpprlLength) % 2 > 0)
        {
          totalSize += 1;
        }
      }

      // see if we couldn't fit some
      if (index != size)
      {
        _overFlow = new ArrayList();
        _overFlow.addAll(_chpxList.subList(index, size));
      }

      // index should equal number of CHPXs that will be in this fkp now.
      buf[511] = (byte)index;

      offsetOffset = (FC_SIZE * index) + FC_SIZE;
      //grpprlOffset =  offsetOffset + index + (grpprlOffset % 2);

      CHPX chpx = null;
      for (int x = 0; x < index; x++)
      {
        chpx = (CHPX)_chpxList.get(x);
        byte[] grpprl = chpx.getGrpprl();

        LittleEndian.putInt(buf, fcOffset, chpx.getStartBytes() + fcMin);
        grpprlOffset -= (1 + grpprl.length);
        grpprlOffset -= (grpprlOffset % 2);
        buf[offsetOffset] = (byte)(grpprlOffset/2);
        buf[grpprlOffset] = (byte)grpprl.length;
        System.arraycopy(grpprl, 0, buf, grpprlOffset + 1, grpprl.length);

        offsetOffset += 1;
        fcOffset += FC_SIZE;
      }
      // put the last chpx's end in
      LittleEndian.putInt(buf, fcOffset, chpx.getEndBytes() + fcMin);
      return buf;
    }

}
