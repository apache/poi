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

package org.apache.poi.hdf.model.hdftypes;

import org.apache.poi.util.LittleEndian;

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
public final class PAPFormattedDiskPage extends FormattedDiskPage
{

    /**
     * Creates a PAPFormattedDiskPage from a 512 byte array
     *
     * @param fkp a 512 byte array.
     */
    public PAPFormattedDiskPage(byte[] fkp)
    {
        super(fkp);
    }

    /**
     * Gets the papx for the pagraph at index in this fkp.
     *
     * @param index The index of the papx to get.
     * @return a papx grpprl.
     */
    public byte[] getGrpprl(int index)
    {
        int papxOffset = 2 * LittleEndian.getUnsignedByte(_fkp, ((_crun + 1) * 4) + (index * 13));
        int size = 2 * LittleEndian.getUnsignedByte(_fkp, papxOffset);
        if(size == 0)
        {
            size = 2 * LittleEndian.getUnsignedByte(_fkp, ++papxOffset);
        }
        else
        {
            size--;
        }

        byte[] papx = new byte[size];
        System.arraycopy(_fkp, ++papxOffset, papx, 0, size);
        return papx;
    }
}
