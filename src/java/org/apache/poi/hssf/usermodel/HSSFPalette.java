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
public class HSSFPalette
{
    private PaletteRecord palette;
    
    protected HSSFPalette(PaletteRecord palette)
    {
        this.palette = palette;
    }
    
    /**
     * Retrieves the color at a given index
     *
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @return the color, or null if the index is not populated
     */
    public HSSFColor getColor(short index)
    {
        byte[] b = palette.getColor(index);
        if (b != null)
        {
            return new CustomColor(index, b);
        }
        return null;
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
        byte[] b = palette.getColor(PaletteRecord.FIRST_COLOR_INDEX);
        for (short i = (short) PaletteRecord.FIRST_COLOR_INDEX; b != null;
            b = palette.getColor(++i))
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
        byte[] b = palette.getColor(PaletteRecord.FIRST_COLOR_INDEX);
        for (short i = (short) PaletteRecord.FIRST_COLOR_INDEX; b != null;
            b = palette.getColor(++i))
        {
            int colorDistance = red - b[0] + green - b[1] + blue - b[2];
            if (colorDistance < minColorDistance)
            {
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
        palette.setColor(index, red, green, blue);
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
        byte[] b = palette.getColor(PaletteRecord.FIRST_COLOR_INDEX);
        short i;
        for (i = (short) PaletteRecord.FIRST_COLOR_INDEX; i < PaletteRecord.STANDARD_PALETTE_SIZE + PaletteRecord.FIRST_COLOR_INDEX; b = palette.getColor(++i))
        {
            if (b == null)
            {
                setColorAtIndex( i, red, green, blue );
                return getColor(i);
            }
        }
        throw new RuntimeException("Could not find free color index");
    }

    private static class CustomColor extends HSSFColor
    {
        private short byteOffset;
        private byte red;
        private byte green;
        private byte blue;
        
        private CustomColor(short byteOffset, byte[] colors)
        {
            this(byteOffset, colors[0], colors[1], colors[2]);
        }
        
        private CustomColor(short byteOffset, byte red, byte green, byte blue)
        {
            this.byteOffset = byteOffset;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
        
        public short getIndex()
        {
            return byteOffset;
        }
        
        public short[] getTriplet()
        {
            return new short[]
            {
                (short) (red   & 0xff),
                (short) (green & 0xff),
                (short) (blue  & 0xff)
            };
        }
        
        public String getHexString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(getGnumericPart(red));
            sb.append(':');
            sb.append(getGnumericPart(green));
            sb.append(':');
            sb.append(getGnumericPart(blue));
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
