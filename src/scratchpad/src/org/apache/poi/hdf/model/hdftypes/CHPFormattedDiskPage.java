/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.hdf.model.hdftypes;

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
public class CHPFormattedDiskPage extends FormattedDiskPage
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
     *
     * @param fkp The 512 byte array to read data from
     */
    public CHPFormattedDiskPage(byte[] documentStream, int offset, int fcMin)
    {
      super(documentStream, offset);

      for (int x = 0; x < _crun; x++)
      {
        _chpxList.add(new CHPX(getStart(x) - fcMin, getEnd(x) - fcMin, getGrpprl(x)));
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
      int grpprlOffset = 0;
      int offsetOffset = 0;
      int fcOffset = 0;

      // total size is currently the size of one FC
      int totalSize = FC_SIZE;

      int index = 0;
      for (; index < size; index++)
      {
        int grpprlLength = ((CHPX)_chpxList.get(index)).getGrpprl().length;

        // check to see if we have enough room for an FC, a byte, and the grpprl.
        totalSize += (FC_SIZE + 1 + grpprlLength);
        // if size is uneven we will have to add one so the first grpprl falls
        // on a word boundary
        if (totalSize > 511 + (index % 2))
        {
          totalSize -= (FC_SIZE + 1 + grpprlLength);
          break;
        }

        // grpprls must fall on word boundaries
        if (grpprlLength % 2 > 0)
        {
          totalSize += 1;
        }
      }

      // see if we couldn't fit some
      if (index != size)
      {
        _overFlow = new ArrayList();
        _overFlow.addAll(index, _chpxList);
      }

      // index should equal number of CHPXs that will be in this fkp now.
      buf[511] = (byte)index;

      offsetOffset = (FC_SIZE * index) + FC_SIZE;
      grpprlOffset =  offsetOffset + index + (grpprlOffset % 2);

      CHPX chpx = null;
      for (int x = 0; x < index; x++)
      {
        chpx = (CHPX)_chpxList.get(x);
        byte[] grpprl = chpx.getGrpprl();

        LittleEndian.putInt(buf, fcOffset, chpx.getStart() + fcMin);
        buf[offsetOffset] = (byte)(grpprlOffset/2);
        System.arraycopy(grpprl, 0, buf, grpprlOffset, grpprl.length);

        grpprlOffset += grpprl.length + (grpprl.length % 2);
        offsetOffset += 1;
        fcOffset += FC_SIZE;
      }
      // put the last chpx's end in
      LittleEndian.putInt(buf, fcOffset, chpx.getEnd() + fcMin);
      return buf;
    }

}
