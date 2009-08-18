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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Represents a workbook color palette.
 * Internally, the XLS format refers to colors using an offset into the palette
 * record.  Thus, the first color in the palette has the index 0x8, the second
 * has the index 0x9, etc. through 0x40
 *
 * @author Brian Sanders (bsanders at risklabs dot com)
 */
public final class HSSFPalette {
    private PaletteRecord _palette;

    protected HSSFPalette(PaletteRecord palette)
    {
        _palette = palette;
    }

    /**
     * Retrieves the color at a given index
     *
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @return the color, or null if the index is not populated
     */
    public HSSFColor getColor(short index)
    {
        //Handle the special AUTOMATIC case
        if (index == HSSFColor.AUTOMATIC.index) {
            return HSSFColor.AUTOMATIC.getInstance();
        }
        byte[] b = _palette.getColor(index);
          if (b != null)
          {
             return new CustomColor(index, b);
          }
        return null;
    }
    /**
     * Retrieves the color at a given index
     *
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @return the color, or null if the index is not populated
     */
    public HSSFColor getColor(int index) {
    	return getColor((short)index);
    }

    /**
     * Finds the first occurance of a given color
     *
     * @param red the RGB red component, between 0 and 255 inclusive
     * @param green the RGB green component, between 0 and 255 inclusive
     * @param blue the RGB blue component, between 0 and 255 inclusive
     * @return the color, or null if the color does not exist in this palette
     */
    public HSSFColor findColor(byte red, byte green, byte blue)
    {
        byte[] b = _palette.getColor(PaletteRecord.FIRST_COLOR_INDEX);
        for (short i = PaletteRecord.FIRST_COLOR_INDEX; b != null;
            b = _palette.getColor(++i))
        {
            if (b[0] == red && b[1] == green && b[2] == blue)
            {
                return new CustomColor(i, b);
            }
        }
        return null;
    }

    /**
     * Finds the closest matching color in the custom palette.  The
     * method for finding the distance between the colors is fairly
     * primative.
     *
     * @param red   The red component of the color to match.
     * @param green The green component of the color to match.
     * @param blue  The blue component of the color to match.
     * @return  The closest color or null if there are no custom
     *          colors currently defined.
     */
    public HSSFColor findSimilarColor(byte red, byte green, byte blue)
    {
        HSSFColor result = null;
        int minColorDistance = Integer.MAX_VALUE;
        byte[] b = _palette.getColor(PaletteRecord.FIRST_COLOR_INDEX);
        for (short i = PaletteRecord.FIRST_COLOR_INDEX; b != null;
            b = _palette.getColor(++i))
        {
            int colorDistance = Math.abs(red - b[0]) +
            	Math.abs(green - b[1]) + Math.abs(blue - b[2]);
            if (colorDistance < minColorDistance)
            {
                minColorDistance = colorDistance;
                result = getColor(i);
            }
        }
        return result;
    }

    /**
     * Sets the color at the given offset
     *
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @param red the RGB red component, between 0 and 255 inclusive
     * @param green the RGB green component, between 0 and 255 inclusive
     * @param blue the RGB blue component, between 0 and 255 inclusive
     */
    public void setColorAtIndex(short index, byte red, byte green, byte blue)
    {
        _palette.setColor(index, red, green, blue);
    }

    /**
     * Adds a new color into an empty color slot.
     * @param red       The red component
     * @param green     The green component
     * @param blue      The blue component
     *
     * @return  The new custom color.
     *
     * @throws RuntimeException if there are more more free color indexes.
     */
    public HSSFColor addColor( byte red, byte green, byte blue )
    {
        byte[] b = _palette.getColor(PaletteRecord.FIRST_COLOR_INDEX);
        short i;
        for (i = PaletteRecord.FIRST_COLOR_INDEX; i < PaletteRecord.STANDARD_PALETTE_SIZE + PaletteRecord.FIRST_COLOR_INDEX; b = _palette.getColor(++i))
        {
            if (b == null)
            {
                setColorAtIndex( i, red, green, blue );
                return getColor(i);
            }
        }
        throw new RuntimeException("Could not find free color index");
    }

    private static final class CustomColor extends HSSFColor {
        private short _byteOffset;
        private byte _red;
        private byte _green;
        private byte _blue;

        public CustomColor(short byteOffset, byte[] colors)
        {
            this(byteOffset, colors[0], colors[1], colors[2]);
        }

        private CustomColor(short byteOffset, byte red, byte green, byte blue)
        {
            _byteOffset = byteOffset;
            _red = red;
            _green = green;
            _blue = blue;
        }

        public short getIndex()
        {
            return _byteOffset;
        }

        public short[] getTriplet()
        {
            return new short[]
            {
                (short) (_red   & 0xff),
                (short) (_green & 0xff),
                (short) (_blue  & 0xff)
            };
        }

        public String getHexString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(getGnumericPart(_red));
            sb.append(':');
            sb.append(getGnumericPart(_green));
            sb.append(':');
            sb.append(getGnumericPart(_blue));
            return sb.toString();
        }

        private String getGnumericPart(byte color)
        {
            String s;
            if (color == 0)
            {
                s = "0";
            }
            else
            {
                int c = color & 0xff; //as unsigned
                c = (c << 8) | c; //pad to 16-bit
                s = Integer.toHexString(c).toUpperCase();
                while (s.length() < 4)
                {
                    s = "0" + s;
                }
            }
            return s;
        }
    }
}
