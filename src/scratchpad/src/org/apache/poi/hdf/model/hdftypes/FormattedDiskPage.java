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
 * Represents an FKP data structure. This data structure is used to store the
 * grpprls of the paragraph and character properties of the document. A grpprl
 * is a list of sprms(decompression operations) to perform on a parent style.
 *
 * The style properties for paragraph and character runs
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
public abstract class FormattedDiskPage
{
    protected byte[] _fkp;
    protected int _crun;

    /**
     * Uses a 512-byte array to create a FKP
     */
    public FormattedDiskPage(byte[] fkp)
    {
        _crun = LittleEndian.getUnsignedByte(fkp, 511);
        _fkp = fkp;
    }
    /**
     * Used to get a text offset corresponding to a grpprl in this fkp.
     * @param index The index of the property in this FKP
     * @return an int representing an offset in the "WordDocument" stream
     */
    public int getStart(int index)
    {
        return LittleEndian.getInt(_fkp, (index * 4));
    }
    /**
     * Used to get the end of the text corresponding to a grpprl in this fkp.
     * @param index The index of the property in this fkp.
     * @return an int representing an offset in the "WordDocument" stream
     */
    public int getEnd(int index)
    {
        return LittleEndian.getInt(_fkp, ((index + 1) * 4));
    }
    /**
     * Used to get the total number of grrprl's stored int this FKP
     * @return The number of grpprls in this FKP
     */
    public int size()
    {
        return _crun;
    }

    public abstract byte[] getGrpprl(int index);
}
