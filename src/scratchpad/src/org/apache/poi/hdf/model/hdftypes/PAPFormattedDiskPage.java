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
package org.apache.poi.hwpf.model.hdftypes;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a PAP FKP. The style properties for paragraph and character runs
 * are stored in fkps. There are PAP fkps for paragraph properties and CHP fkps
 * for character run properties. The first part of the fkp for both CHP and PAP
 * fkps consists of an array of 4 byte int offsets in the main stream for that
 * Paragraph's or Character run's text. The ending offset is the next
 * value in the array. For example, if an fkp has X number of Paragraph's
 * stored in it then there are (x + 1) 4 byte ints in the beginning array. The
 * number X is determined by the last byte in a 512 byte fkp.
 *
 * CHP and PAP fkps also store the compressed styles(grpprl) that correspond to
 * the offsets on the front of the fkp. The offset of the grpprls is determined
 * differently for CHP fkps and PAP fkps.
 *
 * @author Ryan Ackley
 */
public class PAPFormattedDiskPage extends FormattedDiskPage
{

    private static final int BX_SIZE = 13;
    private static final int FC_SIZE = 4;

    private ArrayList _papxList = new ArrayList();
    private ArrayList _overFlow;


    public PAPFormattedDiskPage()
    {

    }

    /**
     * Creates a PAPFormattedDiskPage from a 512 byte array
     *
     * @param fkp a 512 byte array.
     */
    public PAPFormattedDiskPage(byte[] documentStream, int offset, int fcMin)
    {
      super(documentStream, offset);

      for (int x = 0; x < _crun; x++)
      {
        _papxList.add(new PAPX(getStart(x) - fcMin, getEnd(x) - fcMin, getGrpprl(x), getParagraphHeight(x)));
      }
      _fkp = null;
    }

    public void fill(List filler)
    {
      _papxList.addAll(filler);
    }

    public ArrayList getOverflow()
    {
      return _overFlow;
    }

    public PAPX getPAPX(int index)
    {
      return (PAPX)_papxList.get(index);
    }

    /**
     * Gets the papx for the paragraph at index in this fkp.
     *
     * @param index The index of the papx to get.
     * @return a papx grpprl.
     */
    protected byte[] getGrpprl(int index)
    {
        int papxOffset = 2 * LittleEndian.getUnsignedByte(_fkp, _offset + (((_crun + 1) * FC_SIZE) + (index * BX_SIZE)));
        int size = 2 * LittleEndian.getUnsignedByte(_fkp, _offset + papxOffset);
        if(size == 0)
        {
            size = 2 * LittleEndian.getUnsignedByte(_fkp, _offset + ++papxOffset);
        }
        else
        {
            size--;
        }

        byte[] papx = new byte[size];
        System.arraycopy(_fkp, _offset + ++papxOffset, papx, 0, size);
        return papx;
    }

    protected byte[] toByteArray(int fcMin)
    {
      byte[] buf = new byte[512];
      int size = _papxList.size();
      int grpprlOffset = 0;
      int bxOffset = 0;
      int fcOffset = 0;

      // total size is currently the size of one FC
      int totalSize = FC_SIZE;

      int index = 0;
      for (; index < size; index++)
      {
        int grpprlLength = ((PAPX)_papxList.get(index)).getGrpprl().length;

        // check to see if we have enough room for an FC, a BX, and the grpprl
        // and the 1 byte size of the grpprl.
        totalSize += (FC_SIZE + BX_SIZE + grpprlLength + 1);
        // if size is uneven we will have to add one so the first grpprl falls
        // on a word boundary
        if (totalSize > 511 + (index % 2))
        {
          totalSize -= (FC_SIZE + BX_SIZE + grpprlLength);
          break;
        }

        // grpprls must fall on word boundaries
        if (grpprlLength % 2 > 0)
        {
          totalSize += 1;
        }
        else
        {
          totalSize += 2;
        }
      }

      // see if we couldn't fit some
      if (index != size)
      {
        _overFlow = new ArrayList();
        _overFlow.addAll(index, _papxList);
      }

      // index should equal number of papxs that will be in this fkp now.
      buf[511] = (byte)index;

      bxOffset = (FC_SIZE * index) + FC_SIZE;
      grpprlOffset =  bxOffset + (BX_SIZE * index) + (grpprlOffset % 2);

      PAPX papx = null;
      for (int x = 0; x < index; x++)
      {
        papx = (PAPX)_papxList.get(x);
        byte[] phe = papx.getParagraphHeight().toByteArray();
        byte[] grpprl = papx.getGrpprl();

        LittleEndian.putInt(buf, fcOffset, papx.getStart() + fcMin);
        buf[bxOffset] = (byte)(grpprlOffset/2);
        System.arraycopy(phe, 0, buf, bxOffset + 1, phe.length);

        // refer to the section on PAPX in the spec. Places a size on the front
        // of the PAPX. Has to do with how the grpprl stays on word
        // boundaries.
        if ((grpprl.length % 2) > 0)
        {
          buf[grpprlOffset++] = (byte)((grpprl.length + 1)/2);
        }
        else
        {
          buf[++grpprlOffset] = (byte)((grpprl.length)/2);
          grpprlOffset++;
        }
        System.arraycopy(grpprl, 0, buf, grpprlOffset, grpprl.length);

        bxOffset += BX_SIZE;
        fcOffset += FC_SIZE;
      }
      // put the last papx's end in
      LittleEndian.putInt(buf, fcOffset, papx.getEnd() + fcMin);
      return buf;
    }

    private ParagraphHeight getParagraphHeight(int index)
    {
      int pheOffset = 1 + (2 * LittleEndian.getUnsignedByte(_fkp, _offset + (((_crun + 1) * 4) + (index * 13))));

      ParagraphHeight phe = new ParagraphHeight(_fkp, pheOffset);

      return phe;
    }
}
