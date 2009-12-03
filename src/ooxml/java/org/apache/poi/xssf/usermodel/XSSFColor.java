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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.apache.poi.util.Internal;

/**
 * Represents a color in SpreadsheetML
 */
public class XSSFColor {
	
	private CTColor ctColor;

    /**
     * Create an instance of XSSFColor from the supplied XML bean
     */
    public XSSFColor(CTColor color) {
		this.ctColor = color;
	}
	
    /**
     * Create an new instance of XSSFColor
     */
    public XSSFColor() {
        this.ctColor = CTColor.Factory.newInstance();
    }

    public XSSFColor(java.awt.Color clr) {
        this();
        ctColor.setRgb(new byte[]{(byte)clr.getRed(), (byte)clr.getGreen(), (byte)clr.getBlue()});
    }

    /**
     * A boolean value indicating the ctColor is automatic and system ctColor dependent.
     */
    public boolean isAuto() {
		return ctColor.getAuto();
	}
	
    /**
     * A boolean value indicating the ctColor is automatic and system ctColor dependent.
     */
	public void setAuto(boolean auto) {
		ctColor.setAuto(auto);
	}

    /**
     * Indexed ctColor value. Only used for backwards compatibility. References a ctColor in indexedColors.
     */
    public short getIndexed() {
		return (short)ctColor.getIndexed();
	}
	
    /**
     * Indexed ctColor value. Only used for backwards compatibility. References a ctColor in indexedColors.
     */
	public void setIndexed(int indexed) {
		ctColor.setIndexed(indexed);
	}

    /**
     * Standard Alpha Red Green Blue ctColor value (ARGB).
     */
    public byte[] getRgb() {
		return ctColor.getRgb();
	}
	
    /**
     * Standard Alpha Red Green Blue ctColor value (ARGB).
     */
	public void setRgb(byte[] rgb) {
		ctColor.setRgb(rgb);
	}

    /**
     * Index into the <clrScheme> collection, referencing a particular <sysClr> or
     *  <srgbClr> value expressed in the Theme part.
     */
    public int getTheme() {
		return (int)ctColor.getTheme();
	}
	
    /**
     * Index into the <clrScheme> collection, referencing a particular <sysClr> or
     *  <srgbClr> value expressed in the Theme part.
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

    public int hashCode(){
        return ctColor.toString().hashCode();
    }

    public boolean equals(Object o){
        if(o == null || !(o instanceof XSSFColor)) return false;

        XSSFColor cf = (XSSFColor)o;
        return ctColor.toString().equals(cf.getCTColor().toString());
    }

}
