
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
        

package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndian;

/**
 * PaletteRecord - Supports custom palettes.
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Brian Sanders (bsanders at risklabs dot com) - custom palette editing
 * @version 2.0-pre
 */

public class PaletteRecord
    extends Record
{
    public final static short sid = 0x92;
    /** The standard size of an XLS palette */
    public final static byte STANDARD_PALETTE_SIZE = (byte) 56;
    /** The byte index of the first color */
    public final static short FIRST_COLOR_INDEX = (short) 0x8;
    
    private short field_1_numcolors;
    private List  field_2_colors;

    public PaletteRecord()
    {
      createDefaultPalette();
    }

    public PaletteRecord(RecordInputStream in)
    {
       field_1_numcolors = in.readShort();
       field_2_colors    = new ArrayList(field_1_numcolors);
       for (int k = 0; k < field_1_numcolors; k++) {
           field_2_colors.add(new PColor(
                                         in.readByte(),
                                         in.readByte(),
                                         in.readByte()
                                        )
                              );
           //Read unused byte.
           in.readByte();
       } 
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PALETTE]\n");
        buffer.append("  numcolors     = ").append(field_1_numcolors)
              .append('\n');
        for (int k = 0; k < field_1_numcolors; k++) {
        PColor c = (PColor) field_2_colors.get(k);
        buffer.append("* colornum      = ").append(k)
              .append('\n');
        buffer.append(c.toString());
        buffer.append("/*colornum      = ").append(k)
              .append('\n');
        }
        buffer.append("[/PALETTE]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short) (getRecordSize() - 4));
        LittleEndian.putShort(data, 4 + offset, field_1_numcolors);
        for (int k = 0; k < field_1_numcolors; k++) {
          PColor c = (PColor)field_2_colors.get(k);
          c.serialize(data, (6+offset+(k*4)));
        }

        return getRecordSize();
    }

    protected int getDataSize() {
        return 2 + (field_1_numcolors * 4);
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Returns the color value at a given index
     *
     * @return the RGB triplet for the color, or null if the specified index
     * does not exist
     */
    public byte[] getColor(short byteIndex)
    {
        int i = byteIndex - FIRST_COLOR_INDEX;
        if (i < 0 || i >= field_2_colors.size())
        {
            return null;
        }
        PColor color = (PColor) field_2_colors.get(i);
        return new byte[] { color.red, color.green, color.blue };
    }
    
    /**
     * Sets the color value at a given index
     *
     * If the given index is greater than the current last color index,
     * then black is inserted at every index required to make the palette continuous.
     *
     * @param byteIndex the index to set; if this index is less than 0x8 or greater than
     * 0x40, then no modification is made
     */
    public void setColor(short byteIndex, byte red, byte green, byte blue)
    {
        int i = byteIndex - FIRST_COLOR_INDEX;
        if (i < 0 || i >= STANDARD_PALETTE_SIZE)
        {
            return;
        }
        while (field_2_colors.size() <= i)
        {
            field_2_colors.add(new PColor((byte) 0, (byte) 0, (byte) 0));
        }
        PColor custColor = new PColor(red, green, blue);
        field_2_colors.set(i, custColor);
    }
    
    /**
     * Creates the default palette as PaletteRecord binary data
     *
     * @see org.apache.poi.hssf.model.Workbook#createPalette
     */
    private void createDefaultPalette()
    {
      field_1_numcolors = STANDARD_PALETTE_SIZE;
      field_2_colors    = new ArrayList(field_1_numcolors);
      byte[] palette = new byte[]
        {
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, //color 0...
            (byte) 255, (byte) 255, (byte) 255, (byte) 0,
            (byte) 255, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 255, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 255, (byte) 0,
            (byte) 255, (byte) 255, (byte) 0, (byte) 0,
            (byte) 255, (byte) 0, (byte) 255, (byte) 0,
            (byte) 0, (byte) 255, (byte) 255, (byte) 0,
            (byte) 128, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 128, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 128, (byte) 0,
            (byte) 128, (byte) 128, (byte) 0, (byte) 0,
            (byte) 128, (byte) 0, (byte) 128, (byte) 0,
            (byte) 0, (byte) 128, (byte) 128, (byte) 0,
            (byte) 192, (byte) 192, (byte) 192, (byte) 0,
            (byte) 128, (byte) 128, (byte) 128, (byte) 0,
            (byte) 153, (byte) 153, (byte) 255, (byte) 0,
            (byte) 153, (byte) 51, (byte) 102, (byte) 0,
            (byte) 255, (byte) 255, (byte) 204, (byte) 0,
            (byte) 204, (byte) 255, (byte) 255, (byte) 0,
            (byte) 102, (byte) 0, (byte) 102, (byte) 0,
            (byte) 255, (byte) 128, (byte) 128, (byte) 0,
            (byte) 0, (byte) 102, (byte) 204, (byte) 0,
            (byte) 204, (byte) 204, (byte) 255, (byte) 0,
            (byte) 0, (byte) 0, (byte) 128, (byte) 0,
            (byte) 255, (byte) 0, (byte) 255, (byte) 0,
            (byte) 255, (byte) 255, (byte) 0, (byte) 0,
            (byte) 0, (byte) 255, (byte) 255, (byte) 0, 
            (byte) 128, (byte) 0, (byte) 128, (byte) 0,
            (byte) 128, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 128, (byte) 128, (byte) 0,
            (byte) 0, (byte) 0, (byte) 255, (byte) 0,
            (byte) 0, (byte) 204, (byte) 255, (byte) 0,
            (byte) 204, (byte) 255, (byte) 255, (byte) 0,
            (byte) 204, (byte) 255, (byte) 204, (byte) 0,
            (byte) 255, (byte) 255, (byte) 153, (byte) 0,
            (byte) 153, (byte) 204, (byte) 255, (byte) 0,
            (byte) 255, (byte) 153, (byte) 204, (byte) 0,
            (byte) 204, (byte) 153, (byte) 255, (byte) 0,
            (byte) 255, (byte) 204, (byte) 153, (byte) 0,
            (byte) 51, (byte) 102, (byte) 255, (byte) 0,
            (byte) 51, (byte) 204, (byte) 204, (byte) 0,
            (byte) 153, (byte) 204, (byte) 0, (byte) 0,
            (byte) 255, (byte) 204, (byte) 0, (byte) 0,
            (byte) 255, (byte) 153, (byte) 0, (byte) 0,
            (byte) 255, (byte) 102, (byte) 0, (byte) 0,
            (byte) 102, (byte) 102, (byte) 153, (byte) 0,
            (byte) 150, (byte) 150, (byte) 150, (byte) 0,
            (byte) 0, (byte) 51, (byte) 102, (byte) 0,
            (byte) 51, (byte) 153, (byte) 102, (byte) 0,
            (byte) 0, (byte) 51, (byte) 0, (byte) 0,
            (byte) 51, (byte) 51, (byte) 0, (byte) 0,
            (byte) 153, (byte) 51, (byte) 0, (byte) 0,
            (byte) 153, (byte) 51, (byte) 102, (byte) 0,
            (byte) 51, (byte) 51, (byte) 153, (byte) 0,
            (byte) 51, (byte) 51, (byte) 51, (byte) 0
        };
      
      for (int k = 0; k < field_1_numcolors; k++) {
          field_2_colors.add(new PColor(
                                        palette[k*4],
                                        palette[k*4+1],
                                        palette[k*4+2]
                                       )
                             );
      }
      
    }
}

/**
 * PColor - element in the list of colors - consider it a "struct"
 */
class PColor {
  public byte red;
  public byte green;
  public byte blue;
  public PColor(byte red, byte green, byte blue) {
    this.red=red;
    this.green=green;
    this.blue=blue;
  }

  public void serialize(byte[] data, int offset) {
     data[offset + 0] = red;
     data[offset + 1] = green;
     data[offset + 2] = blue;
     data[offset + 3] = 0;
  }

  public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("  red           = ").append(red & 0xff).append('\n');
        buffer.append("  green         = ").append(green & 0xff).append('\n');
        buffer.append("  blue          = ").append(blue & 0xff).append('\n');
        return buffer.toString();
  }
}
