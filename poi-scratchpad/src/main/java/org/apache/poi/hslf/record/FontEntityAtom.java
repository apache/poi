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

package org.apache.poi.hslf.record;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.fonts.FontFamily;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * This atom corresponds exactly to a Windows Logical Font (LOGFONT) structure.
 * It keeps all the information needed to define the attributes of a font,
 * such as height, width, etc. For more information, consult the
 * Windows API Programmer's reference.
 */

public final class FontEntityAtom extends RecordAtom {

    private static final int[] FLAGS_MASKS = {
        0x0001, 0x0100, 0x0200, 0x0400, 0x0800
    };

    private static final String[] FLAGS_NAMES = {
        "EMBED_SUBSETTED",
        "RASTER_FONT",
        "DEVICE_FONT",
        "TRUETYPE_FONT",
        "NO_FONT_SUBSTITUTION"
    };

    /**
     * record header
     */
    private final byte[] _header;

    /**
     * record data
     */
    private byte[] _recdata;

    /**
     * Build an instance of <code>FontEntityAtom</code> from on-disk data
     */
    /* package */ FontEntityAtom(byte[] source, int start, int len) {
        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Grab the record data
        _recdata = IOUtils.safelyClone(source, start+8, len-8, getMaxRecordLength());
    }

    /**
     * Create a new instance of <code>FontEntityAtom</code>
     */
    public FontEntityAtom() {
        _recdata = new byte[68];
        _header = new byte[8];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _recdata.length);
    }

    @Override
    public long getRecordType() {
        return RecordTypes.FontEntityAtom.typeID;
    }

    /**
     * A null-terminated string that specifies the typeface name of the font.
     * The length of this string must not exceed 32 characters
     *  including the null terminator.
     * @return font name
     */
    public String getFontName(){
        final int maxLen = Math.min(_recdata.length,64)/2;
        return StringUtil.getFromUnicodeLE0Terminated(_recdata, 0, maxLen);
    }

    /**
     * Set the name of the font.
     * The length of this string must not exceed 32 characters
     *  including the null terminator.
     * Will be converted to null-terminated if not already
     * @param name of the font
     */
    public void setFontName(String name) {
        // Ensure it's not now too long
        int nameLen = name.length() + (name.endsWith("\u0000") ? 0 : 1);
        if (nameLen > 32) {
            throw new HSLFException("The length of the font name, including null termination, must not exceed 32 characters");
        }

        // Everything's happy, so save the name
        byte[] bytes = StringUtil.getToUnicodeLE(name);
        System.arraycopy(bytes, 0, _recdata, 0, bytes.length);
        // null the remaining bytes
        Arrays.fill(_recdata, bytes.length, 64, (byte)0);
    }

    public void setFontIndex(int idx){
        LittleEndian.putShort(_header, 0, (short)idx);
    }

    public int getFontIndex(){
        return LittleEndian.getShort(_header, 0) >> 4;
    }

    /**
     * set the character set
     *
     * @param charset - characterset
     */
    public void setCharSet(int charset){
        _recdata[64] = (byte)charset;
    }

    /**
     * get the character set
     *
     * @return charset - characterset
     */
    public int getCharSet(){
        return _recdata[64];
    }

    /**
     * set the font flags
     * Bit 1: If set, font is subsetted
     *
     * @param flags - the font flags
     */
    public void setFontFlags(int flags){
        _recdata[65] = (byte)flags;
    }

    /**
     * get the character set
     * Bit 1: If set, font is subsetted
     *
     * @return the font flags
     */
    public int getFontFlags(){
        return _recdata[65];
    }

    /**
     * set the font type
     * <p>
     * Bit 1: Raster Font
     * Bit 2: Device Font
     * Bit 3: TrueType Font
     * </p>
     *
     * @param type - the font type
     */
    public void setFontType(int type){
        _recdata[66] = (byte)type;
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
        return _recdata[66];
    }

    /**
     * set lfPitchAndFamily
     *
     *
     * @param val - Corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
     */
    public void setPitchAndFamily(int val){
        _recdata[67] = (byte)val;
    }

    /**
     * get lfPitchAndFamily
     *
     * @return corresponds to the lfPitchAndFamily field of the Win32 API LOGFONT structure
     */
    public int getPitchAndFamily(){
        return _recdata[67];
    }

    /**
     * Write the contents of the record back, so it can be written to disk
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_recdata);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "fontName", this::getFontName,
            "fontIndex", this::getFontIndex,
            "charset", this::getCharSet,
            "fontFlags", getBitsAsString(this::getFontFlags, FLAGS_MASKS, FLAGS_NAMES),
            "fontPitch", () -> FontPitch.valueOfPitchFamily((byte)getPitchAndFamily()),
            "fontFamily", () -> FontFamily.valueOfPitchFamily((byte)getPitchAndFamily())
        );
    }
}
