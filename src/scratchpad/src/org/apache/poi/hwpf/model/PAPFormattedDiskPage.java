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

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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
public final class PAPFormattedDiskPage extends FormattedDiskPage {

    private static final int BX_SIZE = 13;
    private static final int FC_SIZE = 4;

    private ArrayList<PAPX> _papxList = new ArrayList<PAPX>();
    private ArrayList<PAPX> _overFlow;
    private byte[] _dataStream;


    public PAPFormattedDiskPage(byte[] dataStream)
    {
      _dataStream = dataStream;
    }

    /**
     * Creates a PAPFormattedDiskPage from a 512 byte array
     */
    public PAPFormattedDiskPage(byte[] documentStream, byte[] dataStream, int offset, int fcMin, TextPieceTable tpt)
    {
      super(documentStream, offset);
      for (int x = 0; x < _crun; x++) {
         int startAt = getStart(x);
         int endAt = getEnd(x);
         _papxList.add(new PAPX(startAt, endAt, tpt, getGrpprl(x), getParagraphHeight(x), dataStream));
      }
      _fkp = null;
      _dataStream = dataStream;
    }

    /**
     * Fills the queue for writing.
     *
     * @param filler a List of PAPXs
     */
    public void fill(List<PAPX> filler)
    {
      _papxList.addAll(filler);
    }

    /**
     * Used when writing out a Word docunment. This method is part of a sequence
     * that is necessary because there is no easy and efficient way to
     * determine the number PAPX's that will fit into one FKP. THe sequence is
     * as follows:
     *
     * fill()
     * toByteArray()
     * getOverflow()
     *
     * @return The remaining PAPXs that didn't fit into this FKP.
     */
    ArrayList<PAPX> getOverflow()
    {
      return _overFlow;
    }

    /**
     * Gets the PAPX at index.
     * @param index The index to get the PAPX for.
     * @return The PAPX at index.
     */
    public PAPX getPAPX(int index)
    {
      return _papxList.get(index);
    }

    /**
     * Gets the papx grpprl for the paragraph at index in this fkp.
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

    /**
     * Creates a byte array representation of this data structure. Suitable for
     * writing to a Word document.
     *
     * @param fcMin The file offset in the main stream where text begins.
     * @return A byte array representing this data structure.
     */
    protected byte[] toByteArray(int fcMin)
    {
      byte[] buf = new byte[512];
      int size = _papxList.size();
      int grpprlOffset = 0;
      int bxOffset = 0;
      int fcOffset = 0;
      byte[] lastGrpprl = new byte[0];

      // total size is currently the size of one FC
      int totalSize = FC_SIZE;

      int index = 0;
      for (; index < size; index++)
      {
        byte[] grpprl = ((PAPX)_papxList.get(index)).getGrpprl();
        int grpprlLength = grpprl.length;

        // is grpprl huge?
        if(grpprlLength > 488)
        {
          grpprlLength = 8; // set equal to size of sprmPHugePapx grpprl
        }

        // check to see if we have enough room for an FC, a BX, and the grpprl
        // and the 1 byte size of the grpprl.
        int addition = 0;
        if (!Arrays.equals(grpprl, lastGrpprl))
        {
          addition = (FC_SIZE + BX_SIZE + grpprlLength + 1);
        }
        else
        {
          addition = (FC_SIZE + BX_SIZE);
        }

        totalSize += addition;

        // if size is uneven we will have to add one so the first grpprl falls
        // on a word boundary
        if (totalSize > 511 + (index % 2))
        {
          totalSize -= addition;
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
        lastGrpprl = grpprl;
      }

      // see if we couldn't fit some
      if (index != size)
      {
        _overFlow = new ArrayList<PAPX>();
        _overFlow.addAll(_papxList.subList(index, size));
      }

      // index should equal number of papxs that will be in this fkp now.
      buf[511] = (byte)index;

      bxOffset = (FC_SIZE * index) + FC_SIZE;
      grpprlOffset =  511;

      PAPX papx = null;
      lastGrpprl = new byte[0];
      for (int x = 0; x < index; x++)
      {
        papx = _papxList.get(x);
        byte[] phe = papx.getParagraphHeight().toByteArray();
        byte[] grpprl = papx.getGrpprl();

        // is grpprl huge?
        if(grpprl.length > 488)
        {
          // if so do we have storage at getHugeGrpprlOffset()
          int hugeGrpprlOffset = papx.getHugeGrpprlOffset();
          if(hugeGrpprlOffset == -1) // then we have no storage...
          {
            throw new UnsupportedOperationException(
                  "This Paragraph has no dataStream storage.");
          }
          // we have some storage...

          // get the size of the existing storage
          int maxHugeGrpprlSize = LittleEndian.getUShort(_dataStream, hugeGrpprlOffset);

          if (maxHugeGrpprlSize < grpprl.length-2) { // grpprl.length-2 because we don't store the istd
              throw new UnsupportedOperationException(
                  "This Paragraph's dataStream storage is too small.");
          }

          // store grpprl at hugeGrpprlOffset
          System.arraycopy(grpprl, 2, _dataStream, hugeGrpprlOffset + 2,
                           grpprl.length - 2); // grpprl.length-2 because we don't store the istd
          LittleEndian.putUShort(_dataStream, hugeGrpprlOffset, grpprl.length - 2);

          // grpprl = grpprl containing only a sprmPHugePapx2
          int istd = LittleEndian.getUShort(grpprl, 0);
          grpprl = new byte[8];
          LittleEndian.putUShort(grpprl, 0, istd);
          LittleEndian.putUShort(grpprl, 2, 0x6646); // sprmPHugePapx2
          LittleEndian.putInt(grpprl, 4, hugeGrpprlOffset);
        }

        boolean same = Arrays.equals(lastGrpprl, grpprl);
        if (!same)
        {
          grpprlOffset -= (grpprl.length + (2 - grpprl.length % 2));
          grpprlOffset -= (grpprlOffset % 2);
        }
        LittleEndian.putInt(buf, fcOffset, papx.getStartBytes() + fcMin);
        buf[bxOffset] = (byte)(grpprlOffset/2);
        System.arraycopy(phe, 0, buf, bxOffset + 1, phe.length);

        // refer to the section on PAPX in the spec. Places a size on the front
        // of the PAPX. Has to do with how the grpprl stays on word
        // boundaries.
        if (!same)
        {
          int copyOffset = grpprlOffset;
          if ( (grpprl.length % 2) > 0)
          {
            buf[copyOffset++] = (byte) ( (grpprl.length + 1) / 2);
          }
          else
          {
            buf[++copyOffset] = (byte) ( (grpprl.length) / 2);
            copyOffset++;
          }
          System.arraycopy(grpprl, 0, buf, copyOffset, grpprl.length);
          lastGrpprl = grpprl;
        }

        bxOffset += BX_SIZE;
        fcOffset += FC_SIZE;

      }

      LittleEndian.putInt(buf, fcOffset, papx.getEndBytes() + fcMin);
      return buf;
    }

    /**
     * Used to get the ParagraphHeight of a PAPX at a particular index.
     * @param index
     * @return The ParagraphHeight
     */
    private ParagraphHeight getParagraphHeight(int index)
    {
      int pheOffset = _offset + 1 + (((_crun + 1) * 4) + (index * 13));

      ParagraphHeight phe = new ParagraphHeight(_fkp, pheOffset);

      return phe;
    }
}
