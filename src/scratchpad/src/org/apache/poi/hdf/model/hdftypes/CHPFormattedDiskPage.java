package org.apache.poi.hdf.model.hdftypes;

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
 */
public class CHPFormattedDiskPage extends FormattedDiskPage
{


    /**
     * This constructs a CHPFormattedDiskPage from a raw fkp (512 byte array
     * read from a Word file).
     *
     * @param fkp The 512 byte array to read data from
     */
    public CHPFormattedDiskPage(byte[] fkp)
    {
        super(fkp);
    }

    /**
     * Gets the chpx for the character run at index in this fkp.
     *
     * @param index The index of the chpx to get.
     * @return a chpx grpprl.
     */
    public byte[] getGrpprl(int index)
    {
        int chpxOffset = 2 * LittleEndian.getUnsignedByte(_fkp, ((_crun + 1) * 4) + index);

        //optimization if offset == 0 use "Normal" style
        if(chpxOffset == 0)
        {
            return new byte[0];

        }

        int size = LittleEndian.getUnsignedByte(_fkp, chpxOffset);

        byte[] chpx = new byte[size];

        System.arraycopy(_fkp, ++chpxOffset, chpx, 0, size);
        return chpx;
    }
}