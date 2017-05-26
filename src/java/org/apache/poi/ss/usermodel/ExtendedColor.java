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
package org.apache.poi.ss.usermodel;

import java.util.Locale;

/**
 * Represents a XSSF-style color (based on either a
 *  {@link org.apache.poi.xssf.usermodel.XSSFColor} or a
 *  {@link org.apache.poi.hssf.record.common.ExtendedColor} 
 */
public abstract class ExtendedColor implements Color {

    /**
     *
     * @param clr awt Color to set
     */
    protected void setColor(java.awt.Color clr) {
        setRGB(new byte[]{(byte)clr.getRed(), (byte)clr.getGreen(), (byte)clr.getBlue()});
    }

    /**
     * @return true if the color is automatic
     */
    public abstract boolean isAuto();

    /**
     * @return true if the color is indexed
     */
    public abstract boolean isIndexed();

    /**
     * @return true if the color is RGB / ARGB
     */
    public abstract boolean isRGB();
    
    /**
     * @return true if the color is from a Theme
     */
    public abstract boolean isThemed();
    
    /**
     * @return Indexed Color index value, if {@link #isIndexed()} is true
     */
    public abstract short getIndex();
    
    /**
     * @return Index of Theme color, if {@link #isThemed()} is true
     */
    public abstract int getTheme();

    /**
     * @return Standard Red Green Blue ctColor value (RGB) bytes.
     * If there was an A (Alpha) value, it will be stripped.
     */
    public abstract byte[] getRGB();

    /**
     * @return Standard Alpha Red Green Blue ctColor value (ARGB) bytes.
     */
    public abstract byte[] getARGB();

    /**
     * @return RGB or ARGB bytes or null
     */
    protected abstract byte[] getStoredRBG();
    
    /**
     * Sets the Red Green Blue or Alpha Red Green Blue
     * @param rgb bytes
     */
    public abstract void setRGB(byte[] rgb);

    /**
     * @return RGB or ARGB bytes, either stored or by index
     */
    protected byte[] getRGBOrARGB() {
        if (isIndexed() && getIndex() > 0) {
            byte[] rgb = getIndexedRGB();
            if (rgb != null) {
                return rgb;
            }
        }

        // Grab the colour
        return getStoredRBG();
    }
    
    /**
     * @return index color RGB bytes, if {@link #isIndexed()} == true, null if not indexed or index is invalid
     */
    protected abstract byte[] getIndexedRGB();

    /**
     * @return Standard Red Green Blue ctColor value (RGB) bytes with applied tint.
     * Alpha values are ignored.
     */
    public byte[] getRGBWithTint() {
        byte[] rgb = getStoredRBG();
        if (rgb != null) {
            if(rgb.length == 4) {
               byte[] tmp = new byte[3];
               System.arraycopy(rgb, 1, tmp, 0, 3);
               rgb = tmp;
            }
            double tint = getTint();
            for (int i = 0; i < rgb.length; i++){
                rgb[i] = applyTint(rgb[i] & 0xFF, tint);
            }
        }
        return rgb;
    }

    /**
     * @return the ARGB value in hex string format, eg FF00FF00.
     * Works for both regular and indexed colours.
     */
    public String getARGBHex() {
       byte[] rgb = getARGB();
        if(rgb == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for(byte c : rgb) {
          int i = c & 0xff;
          String cs = Integer.toHexString(i);
          if(cs.length() == 1) {
             sb.append('0');
          }
          sb.append(cs);
       }
       return sb.toString().toUpperCase(Locale.ROOT);
    }
    
    /**
     * Sets the ARGB value from hex format, eg FF0077FF.
     * Only works for regular (non-indexed) colours
     * @param argb color ARGB hex string
     */
    public void setARGBHex(String argb) {
        if (argb.length() == 6 || argb.length() == 8) {
            byte[] rgb = new byte[argb.length()/2];
            for (int i=0; i<rgb.length; i++) {
                String part = argb.substring(i*2,(i+1)*2);
                rgb[i] = (byte)Integer.parseInt(part, 16);
            }
            setRGB(rgb);
        } else {
            throw new IllegalArgumentException("Must be of the form 112233 or FFEEDDCC");
        }
    }

    private static byte applyTint(int lum, double tint){
        if(tint > 0){
            return (byte)(lum * (1.0-tint) + (255 - 255 * (1.0-tint)));
        } else if (tint < 0){
            return (byte)(lum*(1+tint));
        } else {
            return (byte)lum;
        }
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
    public abstract double getTint();

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
    public abstract void setTint(double tint);
}
