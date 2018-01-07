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
package org.apache.poi.xssf.usermodel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

/**
 * Represents a color in SpreadsheetML
 */
public class XSSFColor extends ExtendedColor {
    private final CTColor ctColor;
    private final IndexedColorMap indexedColorMap;

    /**
     * @param color
     * @param map
     * @return null if color is null, new instance otherwise
     */
    public static XSSFColor from(CTColor color, IndexedColorMap map) {
        return color == null ? null : new XSSFColor(color, map);
    }
    
    /**
     * Create an instance of XSSFColor from the supplied XML bean, with default color indexes
     * @param color The {@link CTColor} to use as color-value.
     * @deprecated 3.17 beta 1 - pass the workbook styles indexed color map, if any
     */
    @Deprecated
    @Removal(version="3.19")
    public XSSFColor(CTColor color) {
        this(color, new DefaultIndexedColorMap());
    }
    
    /**
     * Create an instance of XSSFColor from the supplied XML bean, with the given color indexes
     * @param color The {@link CTColor} to use as color-value.
     * @param map The IndexedColorMap to use instead of the default one
     * @deprecated 4.0.0 - use the factory {@link #from(CTColor, IndexedColorMap)} method instead to check for null CTColor instances.  Make private eventually
     */
    @Deprecated
    public XSSFColor(CTColor color, IndexedColorMap map) {
        this.ctColor = color;
        this.indexedColorMap = map;
    }

    /**
     * Create an new instance of XSSFColor, without knowledge of any custom indexed colors.
     * This is OK for just transiently setting indexes, etc. but is discouraged in read/get uses
     * @deprecated as of 4.0.0, we want to have the indexed map, and all calling contexts have access to it.
     * @see #XSSFColor(IndexedColorMap)
     * @see #from(CTColor, IndexedColorMap)
     */
    @Deprecated
    @Removal(version="4.1")
    public XSSFColor() {
        this(CTColor.Factory.newInstance(), new DefaultIndexedColorMap());
    }

    /**
     * new color with the given indexed color map
     * @param colorMap
     */
    public XSSFColor(IndexedColorMap colorMap) {
        this(CTColor.Factory.newInstance(), colorMap);
    }
    
    /**
     * TEST ONLY
     * @param clr awt Color
     * @param map 
     */
    public XSSFColor(java.awt.Color clr, IndexedColorMap map) {
        this(map);
        setColor(clr);
    }

    /**
     *
     * @param rgb The RGB-byte-values for the Color
     * @param colorMap The IndexedColorMap to use instead of the default one
     */
    public XSSFColor(byte[] rgb, IndexedColorMap colorMap) {
        this(CTColor.Factory.newInstance(), colorMap);
        ctColor.setRgb(rgb);
    }
    
    /**
     * @param indexedColor color index (Enum named for default colors)
     * @param colorMap The IndexedColorMap to use instead of the default one
     */
    public XSSFColor(IndexedColors indexedColor, IndexedColorMap colorMap) {
        this(CTColor.Factory.newInstance(), colorMap);
        ctColor.setIndexed(indexedColor.index);
    }

    /**
     * A boolean value indicating the ctColor is automatic and system ctColor dependent.
     */
    @Override
    public boolean isAuto() {
        return ctColor.getAuto();
    }
    /**
     * @param auto true if the ctColor is automatic and system ctColor dependent.
     */
    public void setAuto(boolean auto) {
        ctColor.setAuto(auto);
    }

    /**
     * A boolean value indicating the ctColor is Indexed
     */
    @Override
    public boolean isIndexed() {
        return ctColor.isSetIndexed();
    }

    /**
     * @return true if the ctColor is RGB or ARGB based
     */
    @Override
    public boolean isRGB() {
        return ctColor.isSetRgb();
    }

    /**
     * @return true if the ctColor is Theme based
     */
    @Override
    public boolean isThemed() {
        return ctColor.isSetTheme();
    }
    
    /**
     * @return true if the ctColor has a alpha
     */
    public boolean hasAlpha() {
        return ctColor.isSetRgb() && ctColor.getRgb().length == 4;
    }

    /**
     * @return true if the ctColor has a tint
     */
    public boolean hasTint() {
        return ctColor.isSetTint() && ctColor.getTint() != 0;
    }

    /**
     * Indexed ctColor value. Only used for backwards compatibility. References a ctColor in indexedColors.
     */
    @Override
    public short getIndex() {
        return (short)ctColor.getIndexed();
    }
    /**
     * @return Indexed ctColor value. Only used for backwards compatibility. References a ctColor in indexedColors.
     */
    public short getIndexed() {
        return getIndex();
    }

    /**
     * Indexed ctColor value. Only used for backwards compatibility. References a ctColor in indexedColors.
     * @param indexed color index
     */
    public void setIndexed(int indexed) {
        ctColor.setIndexed(indexed);
    }

   /**
    * Standard Red Green Blue ctColor value (RGB).
    * If there was an A (Alpha) value, it will be stripped.
    */
   @Override
   public byte[] getRGB() {
      byte[] rgb = getRGBOrARGB();
      if(rgb == null) {
          return null;
      }

      if(rgb.length == 4) {
         // Need to trim off the alpha
         byte[] tmp = new byte[3];
         System.arraycopy(rgb, 1, tmp, 0, 3);
         return tmp;
      } else {
         return rgb;
      }
   }

   /**
    * Standard Alpha Red Green Blue ctColor value (ARGB).
    */
   @Override
   public byte[] getARGB() {
      byte[] rgb = getRGBOrARGB();
      if(rgb == null) {
          return null;
      }

      if(rgb.length == 3) {
         // Pad with the default Alpha
         byte[] tmp = new byte[4];
         tmp[0] = -1;
         System.arraycopy(rgb, 0, tmp, 1, 3);
         return tmp;
      } else {
         return rgb;
      }
   }

   @Override
   protected byte[] getStoredRBG() {
       return ctColor.getRgb();
   }

   protected byte[] getIndexedRGB() {
       if (isIndexed()) {
           if (indexedColorMap != null) return indexedColorMap.getRGB(getIndex());
           return DefaultIndexedColorMap.getDefaultRGB(getIndex());
       }
       return null;
   }
   
    /**
     * Standard Alpha Red Green Blue ctColor value (ARGB).
     */
   @Override
    public void setRGB(byte[] rgb) {
       ctColor.setRgb(rgb);
    }

    /**
     * Index into the <clrScheme> collection, referencing a particular <sysClr> or
     *  <srgbClr> value expressed in the Theme part.
     */
   @Override
   public int getTheme() {
      return (int)ctColor.getTheme();
    }

    /**
     * Index into the <clrScheme> collection, referencing a particular <sysClr> or
     *  <srgbClr> value expressed in the Theme part.
     * @param theme index
     */
    public void setTheme(int theme) {
        ctColor.setTheme(theme);
    }

    /**
     * Specifies the tint value applied to the ctColor.
     *
     * <p>
     * If tint is supplied, then it is applied to the RGB value of the ctColor to determine the final
     * ctColor applied.
     * </p>
     * <p>
     * The tint value is stored as a double from -1.0 .. 1.0, where -1.0 means 100% darken and
     * 1.0 means 100% lighten. Also, 0.0 means no change.
     * </p>
     * <p>
     * In loading the RGB value, it is converted to HLS where HLS values are (0..HLSMAX), where
     * HLSMAX is currently 255.
     * </p>
     * Here are some examples of how to apply tint to ctColor:
     * <blockquote>
     * <pre>
     * If (tint &lt; 0)
     * Lum' = Lum * (1.0 + tint)
     *
     * For example: Lum = 200; tint = -0.5; Darken 50%
     * Lum' = 200 * (0.5) =&gt; 100
     * For example: Lum = 200; tint = -1.0; Darken 100% (make black)
     * Lum' = 200 * (1.0-1.0) =&gt; 0
     * If (tint &gt; 0)
     * Lum' = Lum * (1.0-tint) + (HLSMAX - HLSMAX * (1.0-tint))
     * For example: Lum = 100; tint = 0.75; Lighten 75%
     *
     * Lum' = 100 * (1-.75) + (HLSMAX - HLSMAX*(1-.75))
     * = 100 * .25 + (255 - 255 * .25)
     * = 25 + (255 - 63) = 25 + 192 = 217
     * For example: Lum = 100; tint = 1.0; Lighten 100% (make white)
     * Lum' = 100 * (1-1) + (HLSMAX - HLSMAX*(1-1))
     * = 100 * 0 + (255 - 255 * 0)
     * = 0 + (255 - 0) = 255
     * </pre>
     * </blockquote>
     *
     * @return the tint value
     */
    @Override
    public double getTint() {
        return ctColor.getTint();
    }

    /**
     * Specifies the tint value applied to the ctColor.
     *
     * <p>
     * If tint is supplied, then it is applied to the RGB value of the ctColor to determine the final
     * ctColor applied.
     * </p>
     * <p>
     * The tint value is stored as a double from -1.0 .. 1.0, where -1.0 means 100% darken and
     * 1.0 means 100% lighten. Also, 0.0 means no change.
     * </p>
     * <p>
     * In loading the RGB value, it is converted to HLS where HLS values are (0..HLSMAX), where
     * HLSMAX is currently 255.
     * </p>
     * Here are some examples of how to apply tint to ctColor:
     * <blockquote>
     * <pre>
     * If (tint &lt; 0)
     * Lum' = Lum * (1.0 + tint)
     *
     * For example: Lum = 200; tint = -0.5; Darken 50%
     * Lum' = 200 * (0.5) =&gt; 100
     * For example: Lum = 200; tint = -1.0; Darken 100% (make black)
     * Lum' = 200 * (1.0-1.0) =&gt; 0
     * If (tint &gt; 0)
     * Lum' = Lum * (1.0-tint) + (HLSMAX - HLSMAX * (1.0-tint))
     * For example: Lum = 100; tint = 0.75; Lighten 75%
     *
     * Lum' = 100 * (1-.75) + (HLSMAX - HLSMAX*(1-.75))
     * = 100 * .25 + (255 - 255 * .25)
     * = 25 + (255 - 63) = 25 + 192 = 217
     * For example: Lum = 100; tint = 1.0; Lighten 100% (make white)
     * Lum' = 100 * (1-1) + (HLSMAX - HLSMAX*(1-1))
     * = 100 * 0 + (255 - 255 * 0)
     * = 0 + (255 - 0) = 255
     * </pre>
     * </blockquote>
     *
     * @param tint the tint value
     */
    @Override
    public void setTint(double tint) {
        ctColor.setTint(tint);
    }

    /**
     * Returns the underlying XML bean
     *
     * @return the underlying XML bean
     */
    @Internal
    public CTColor getCTColor(){
        return ctColor;
    }

    /**
     * Checked type cast <tt>color</tt> to an XSSFColor.
     *
     * @param color the color to type cast
     * @return the type casted color
     * @throws IllegalArgumentException if color is null or is not an instance of XSSFColor
     */
    public static XSSFColor toXSSFColor(Color color) {
        // FIXME: this method would be more useful if it could convert any Color to an XSSFColor
        // Currently the only benefit of this method is to throw an IllegalArgumentException
        // instead of a ClassCastException.
        if (color != null && !(color instanceof XSSFColor)) {
            throw new IllegalArgumentException("Only XSSFColor objects are supported, but had " + color.getClass());
        }
        return (XSSFColor)color;
    }
    
    @Override
    public int hashCode(){
        return ctColor.toString().hashCode();
    }

    // Helper methods for {@link #equals(Object)}
    private boolean sameIndexed(XSSFColor other) {
        if (isIndexed() == other.isIndexed()) {
            return !isIndexed() || getIndexed() == other.getIndexed();
        }
        return false;
    }
    private boolean sameARGB(XSSFColor other) {
        if (isRGB() == other.isRGB()) {
            return !isRGB() || Arrays.equals(getARGB(), other.getARGB());
        }
        return false;
    }
    private boolean sameTheme(XSSFColor other) {
        if (isThemed() == other.isThemed()) {
            return !isThemed() || getTheme() == other.getTheme();
        }
        return false;
    }
    private boolean sameTint(XSSFColor other) {
        if (hasTint() == other.hasTint()) {
            return !hasTint() || getTint() == other.getTint();
        }
        return false;
    }
    private boolean sameAuto(XSSFColor other) {
        return isAuto() == other.isAuto();
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof XSSFColor)) {
            return false;
        }

        XSSFColor other = (XSSFColor)o;
        
        // Compare each field in ctColor.
        // Cannot compare ctColor's XML string representation because equivalent
        // colors may have different relation namespace URI's
        return sameARGB(other)
                && sameTheme(other)
                && sameIndexed(other)
                && sameTint(other)
                && sameAuto(other);
    }
}
