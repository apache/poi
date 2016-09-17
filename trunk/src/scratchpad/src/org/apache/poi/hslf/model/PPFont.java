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

package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.FontEntityAtom;

/**
 * Represents a Font used in a presenation.
 * <p>
 * In PowerPoint Font is a shared resource and can be shared among text object in the presentation.
 * </p>
 * Some commonly used fonts are predefined in static constants.
 *
 * @author Yegor Kozlov
 */
public final class PPFont {
    /**
     * ANSI character set
     */
    public final static byte ANSI_CHARSET = 0;

    /**
     * Default character set.
     */
    public final static byte DEFAULT_CHARSET = 1;

    /**
     * Symbol character set
     */
    public final static byte SYMBOL_CHARSET = 2;


    /**
     * Constants for the pitch and family of the font.
     * The two low-order bits specify the pitch of the font and can be one of the following values
     */
    public final static byte DEFAULT_PITCH  = 0;
    public final static byte FIXED_PITCH    = 1;
    public final static byte VARIABLE_PITCH = 2;

    /**
     * Don't care or don't know.
     */
    public final static byte FF_DONTCARE    = 0;
    /**
     * Fonts with variable stroke width (proportional) and with serifs. Times New Roman is an example.
     */
    public final static byte FF_ROMAN       = 16;
    /**
     * Fonts with variable stroke width (proportional) and without serifs. Arial is an example.
     */
    public final static byte FF_SWISS       = 32;
    /**
     * Fonts designed to look like handwriting. Script and Cursive are examples.
     */
    public final static byte FF_SCRIPT      = 64;
    /**
     * Fonts with constant stroke width (monospace), with or without serifs.
     * Monospace fonts are usually modern. CourierNew is an example
     */
    public final static byte FF_MODERN      = 48;
    /**
     * Novelty fonts. Old English is an example
     */
    public final static byte FF_DECORATIVE  = 80;


    protected int charset;
    protected int type;
    protected int flags;
    protected int pitch;
    protected String name;

    /**
     * Creates a new instance of PPFont
     */
    public PPFont(){

    }

    /**
     * Creates a new instance of PPFont and initialize it from the supplied font atom
     */
    public PPFont(FontEntityAtom fontAtom){
        name = fontAtom.getFontName();
        charset = fontAtom.getCharSet();
        type = fontAtom.getFontType();
        flags = fontAtom.getFontFlags();
        pitch = fontAtom.getPitchAndFamily();
    }

    /**
     * set the name for the font (i.e. Arial)
     *
     * @param val  String representing the name of the font to use
     */
     public void setFontName(String val){
        name = val;
    }

    /**
     * get the name for the font (i.e. Arial)
     *
     * @return String representing the name of the font to use
     */
    public String getFontName(){
        return name;
    }

    /**
     * set the character set
     *
     * @param val - characterset
     */
    public void setCharSet(int val){
        charset = val;
    }

    /**
     * get the character set
     *
     * @return charset - characterset
     */
    public int getCharSet(){
        return charset;
    }

    /**
     * set the font flags
     * Bit 1: If set, font is subsetted
     *
     * @param val - the font flags
     */
    public void setFontFlags(int val){
        flags = val;
    }

    /**
     * get the character set
     * Bit 1: If set, font is subsetted
     *
     * @return the font flags
     */
    public int getFontFlags(){
        return flags;
    }

    /**
     * set the font type
     * <p>
     * Bit 1: Raster Font
     * Bit 2: Device Font
     * Bit 3: TrueType Font
     * </p>
     *
     * @param val - the font type
     */
    public void setFontType(int val){
        type = val;
    }

    /**
     * get the font type
     * <p>
     * Bit 1: Raster Font
     * Bit 2: Device Font
     * Bit 3: TrueType Font
     * </p>
     *
     * @return the font type
     */
    public int getFontType(){
        return type;
    }

    /**
     * set lfPitchAndFamily
     *
     *
     * @param val - Corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
     */
    public void setPitchAndFamily(int val){
        pitch = val;
    }

    /**
     * get lfPitchAndFamily
     *
     * @return corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
     */
    public int getPitchAndFamily(){
        return pitch;
    }

    public static final PPFont ARIAL;
    public static final PPFont TIMES_NEW_ROMAN ;
    public static final PPFont COURIER_NEW;
    public static final PPFont WINGDINGS;
    static {
        ARIAL = new PPFont();
        ARIAL.setFontName("Arial");
        ARIAL.setCharSet(ANSI_CHARSET);
        ARIAL.setFontType(4);
        ARIAL.setFontFlags(0);
        ARIAL.setPitchAndFamily(VARIABLE_PITCH | FF_SWISS);

        TIMES_NEW_ROMAN = new PPFont();
        TIMES_NEW_ROMAN.setFontName("Times New Roman");
        TIMES_NEW_ROMAN.setCharSet(ANSI_CHARSET);
        TIMES_NEW_ROMAN.setFontType(4);
        TIMES_NEW_ROMAN.setFontFlags(0);
        TIMES_NEW_ROMAN.setPitchAndFamily(VARIABLE_PITCH | FF_ROMAN);

        COURIER_NEW = new PPFont();
        COURIER_NEW.setFontName("Courier New");
        COURIER_NEW.setCharSet(ANSI_CHARSET);
        COURIER_NEW.setFontType(4);
        COURIER_NEW.setFontFlags(0);
        COURIER_NEW.setPitchAndFamily(FIXED_PITCH | FF_MODERN);

        WINGDINGS = new PPFont();
        WINGDINGS.setFontName("Wingdings");
        WINGDINGS.setCharSet(SYMBOL_CHARSET);
        WINGDINGS.setFontType(4);
        WINGDINGS.setFontFlags(0);
        WINGDINGS.setPitchAndFamily(VARIABLE_PITCH | FF_DONTCARE);
    }
}
