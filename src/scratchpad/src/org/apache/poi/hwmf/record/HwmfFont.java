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

package org.apache.poi.hwmf.record;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * The Font object specifies the attributes of a logical font
 */
public class HwmfFont {

    private static final POILogger logger = POILogFactory.getLogger(HwmfFont.class);

    public enum WmfCharset {
        /** Specifies the English character set. */
        ANSI_CHARSET(0x00000000, "Cp1252"),
        /**
         * Specifies a character set based on the current system locale;
         * for example, when the system locale is United States English,
         * the default character set is ANSI_CHARSET.
         */
        DEFAULT_CHARSET(0x00000001, "Cp1252"),
        /** Specifies a character set of symbols. */
        SYMBOL_CHARSET(0x00000002, ""),
        /** Specifies the Apple Macintosh character set. */
        MAC_CHARSET(0x0000004D, "MacRoman"),
        /** Specifies the Japanese character set. */
        SHIFTJIS_CHARSET(0x00000080, "Shift_JIS"),
        /** Also spelled "Hangeul". Specifies the Hangul Korean character set. */
        HANGUL_CHARSET(0x00000081, "cp949"),
        /** Also spelled "Johap". Specifies the Johab Korean character set. */
        JOHAB_CHARSET(0x00000082, "x-Johab"),
        /** Specifies the "simplified" Chinese character set for People's Republic of China. */
        GB2312_CHARSET(0x00000086, "GB2312"),
        /**
         * Specifies the "traditional" Chinese character set, used mostly in
         * Taiwan and in the Hong Kong and Macao Special Administrative Regions.
         */
        CHINESEBIG5_CHARSET(0x00000088, "Big5"),
        /** Specifies the Greek character set. */
        GREEK_CHARSET(0x000000A1, "Cp1253"),
        /** Specifies the Turkish character set. */
        TURKISH_CHARSET(0x000000A2, "Cp1254"),
        /** Specifies the Vietnamese character set. */
        VIETNAMESE_CHARSET(0x000000A3, "Cp1258"),
        /** Specifies the Hebrew character set. */
        HEBREW_CHARSET(0x000000B1, "Cp1255"),
        /** Specifies the Arabic character set. */
        ARABIC_CHARSET(0x000000B2, "Cp1256"),
        /** Specifies the Baltic (Northeastern European) character set. */
        BALTIC_CHARSET(0x000000BA, "Cp1257"),
        /** Specifies the Russian Cyrillic character set. */
        RUSSIAN_CHARSET(0x000000CC, "Cp1251"),
        /** Specifies the Thai character set. */
        THAI_CHARSET(0x000000DE, "x-windows-874"),
        /** Specifies a Eastern European character set. */
        EASTEUROPE_CHARSET(0x000000EE, "Cp1250"),
        /**
         * Specifies a mapping to one of the OEM code pages,
         * according to the current system locale setting.
         */
        OEM_CHARSET(0x000000FF, "Cp1252");

        int flag;
        Charset charset;

        WmfCharset(int flag, String javaCharsetName) {
            this.flag = flag;
            if (javaCharsetName.length() > 0) {
                try {
                    charset = Charset.forName(javaCharsetName);
                    return;
                } catch (UnsupportedCharsetException e) {
                    logger.log(POILogger.WARN, "Unsupported charset: "+javaCharsetName);
                }
            }
            charset = null;
        }

        /**
         *
         * @return charset for the font or <code>null</code> if there is no matching charset or
         *         if the charset is a &quot;default&quot;
         */
        public Charset getCharset() {
            return charset;
        }

        public static WmfCharset valueOf(int flag) {
            for (WmfCharset cs : values()) {
                if (cs.flag == flag) return cs;
            }
            return null;
        }
    }

    /**
     * The output precision defines how closely the output must match the requested font's height,
     * width, character orientation, escapement, pitch, and font type.
     */
    public enum WmfOutPrecision {
        /**
         * A value that specifies default behavior.
         */
        OUT_DEFAULT_PRECIS(0x00000000),
        /**
         * A value that is returned when rasterized fonts are enumerated.
         */
        OUT_STRING_PRECIS(0x00000001),
        /**
         * A value that is returned when TrueType and other outline fonts, and
         * vector fonts are enumerated.
         */
        OUT_STROKE_PRECIS(0x00000003),
        /**
         * A value that specifies the choice of a TrueType font when the system
         * contains multiple fonts with the same name.
         */
        OUT_TT_PRECIS(0x00000004),
        /**
         * A value that specifies the choice of a device font when the system
         * contains multiple fonts with the same name.
         */
        OUT_DEVICE_PRECIS(0x00000005),
        /**
         * A value that specifies the choice of a rasterized font when the system
         * contains multiple fonts with the same name.
         */
        OUT_RASTER_PRECIS(0x00000006),
        /**
         * A value that specifies the requirement for only TrueType fonts. If
         * there are no TrueType fonts installed in the system, default behavior is specified.
         */
        OUT_TT_ONLY_PRECIS(0x00000007),
        /**
         * A value that specifies the requirement for TrueType and other outline fonts.
         */
        OUT_OUTLINE_PRECIS (0x00000008),
        /**
         * A value that specifies a preference for TrueType and other outline fonts.
         */
        OUT_SCREEN_OUTLINE_PRECIS (0x00000009),
        /**
         * A value that specifies a requirement for only PostScript fonts. If there
         * are no PostScript fonts installed in the system, default behavior is specified.
         */
        OUT_PS_ONLY_PRECIS (0x0000000A);


        int flag;
        WmfOutPrecision(int flag) {
            this.flag = flag;
        }

        static WmfOutPrecision valueOf(int flag) {
            for (WmfOutPrecision op : values()) {
                if (op.flag == flag) return op;
            }
            return null;
        }
    }

    /**
     * ClipPrecision Flags specify clipping precision, which defines how to clip characters that are
     * partially outside a clipping region. These flags can be combined to specify multiple options.
     */
    public enum WmfClipPrecision {

        /**
         * Specifies that default clipping MUST be used.
         */
        CLIP_DEFAULT_PRECIS (0x00000000),

        /**
         * This value SHOULD NOT be used.
         */
        CLIP_CHARACTER_PRECIS (0x00000001),

        /**
         * This value MAY be returned when enumerating rasterized, TrueType and vector fonts.
         */
        CLIP_STROKE_PRECIS (0x00000002),

        /**
         * This value is used to control font rotation, as follows:
         * If set, the rotation for all fonts SHOULD be determined by the orientation of the coordinate system;
         * that is, whether the orientation is left-handed or right-handed.
         *
         * If clear, device fonts SHOULD rotate counterclockwise, but the rotation of other fonts
         * SHOULD be determined by the orientation of the coordinate system.
         */
        CLIP_LH_ANGLES (0x00000010),

        /**
         * This value SHOULD NOT be used.
         */
        CLIP_TT_ALWAYS (0x00000020),

        /**
         * This value specifies that font association SHOULD< be turned off.
         */
        CLIP_DFA_DISABLE (0x00000040),

        /**
         * This value specifies that font embedding MUST be used to render document content;
         * embedded fonts are read-only.
         */
        CLIP_EMBEDDED (0x00000080);


        int flag;
        WmfClipPrecision(int flag) {
            this.flag = flag;
        }

        static WmfClipPrecision valueOf(int flag) {
            for (WmfClipPrecision cp : values()) {
                if (cp.flag == flag) return cp;
            }
            return null;
        }
    }

    /**
     * The output quality defines how carefully to attempt to match the logical font attributes to those of an actual
     * physical font.
     */
    public enum WmfFontQuality {
        /**
         * Specifies that the character quality of the font does not matter, so DRAFT_QUALITY can be used.
         */
        DEFAULT_QUALITY (0x00),
        
        /**
         * Specifies that the character quality of the font is less important than the
         * matching of logical attribuetes. For rasterized fonts, scaling SHOULD be enabled, which
         * means that more font sizes are available.
         */
        DRAFT_QUALITY (0x01),
        
        /**
         * Specifies that the character quality of the font is more important than the
         * matching of logical attributes. For rasterized fonts, scaling SHOULD be disabled, and the font
         * closest in size SHOULD be chosen.
         */
        PROOF_QUALITY (0x02),
        
        /**
         * Specifies that anti-aliasing SHOULD NOT be used when rendering text.
         */
        NONANTIALIASED_QUALITY (0x03),
        
        /**
         * Specifies that anti-aliasing SHOULD be used when rendering text, if the font supports it.
         */
        ANTIALIASED_QUALITY (0x04),
        
        /**
         * Specifies that ClearType anti-aliasing SHOULD be used when rendering text, if the font supports it.
         * 
         * Fonts that do not support ClearType anti-aliasing include type 1 fonts, PostScript fonts,
         * OpenType fonts without TrueType outlines, rasterized fonts, vector fonts, and device fonts.
         */
        CLEARTYPE_QUALITY (0x05);
        
        int flag;
        WmfFontQuality(int flag) {
            this.flag = flag;
        }

        static WmfFontQuality valueOf(int flag) {
            for (WmfFontQuality fq : values()) {
                if (fq.flag == flag) return fq;
            }
            return null;
        }
    }
    
    /**
     * A property of a font that describes its general appearance.
     */
    public enum WmfFontFamilyClass {
        /**
         * The default font is specified, which is implementation-dependent.
         */
        FF_DONTCARE (0x00),
        /**
         * Fonts with variable stroke widths, which are proportional to the actual widths of
         * the glyphs, and which have serifs. "MS Serif" is an example.
         */
        FF_ROMAN (0x01),
        /**
         * Fonts with variable stroke widths, which are proportional to the actual widths of the
         * glyphs, and which do not have serifs. "MS Sans Serif" is an example.
         */
        FF_SWISS (0x02),
        /**
         * Fonts with constant stroke width, with or without serifs. Fixed-width fonts are
         * usually modern. "Pica", "Elite", and "Courier New" are examples.
         */
        FF_MODERN (0x03),
        /**
         * Fonts designed to look like handwriting. "Script" and "Cursive" are examples.
         */
        FF_SCRIPT (0x04),
        /**
         * Novelty fonts. "Old English" is an example.
         */
        FF_DECORATIVE (0x05);
        
        int flag;
        WmfFontFamilyClass(int flag) {
            this.flag = flag;
        }

        static WmfFontFamilyClass valueOf(int flag) {
            for (WmfFontFamilyClass ff : values()) {
                if (ff.flag == flag) return ff;
            }
            return null;
        }
    }

    /**
     * A property of a font that describes the pitch, of the characters.
     */
    public enum WmfFontPitch {
        /**
         * The default pitch, which is implementation-dependent.
         */
        DEFAULT_PITCH (0x00),
        /**
         * A fixed pitch, which means that all the characters in the font occupy the same
         * width when output in a string.
         */
        FIXED_PITCH (0x01),
        /**
         * A variable pitch, which means that the characters in the font occupy widths
         * that are proportional to the actual widths of the glyphs when output in a string. For example,
         * the "i" and space characters usually have much smaller widths than a "W" or "O" character.
         */
        VARIABLE_PITCH (0x02);
        
        int flag;
        WmfFontPitch(int flag) {
            this.flag = flag;
        }

        static WmfFontPitch valueOf(int flag) {
            for (WmfFontPitch fp : values()) {
                if (fp.flag == flag) return fp;
            }
            return null;
        }        
    }
    
    /**
     * A 16-bit signed integer that specifies the height, in logical units, of the font's
     * character cell. The character height is computed as the character cell height minus the
     * internal leading. The font mapper SHOULD interpret the height as follows.
     *
     * negative value:
     * The font mapper SHOULD transform this value into device units and match its
     * absolute value against the character height of available fonts.
     *
     * zero value:
     * A default height value MUST be used when creating a physical font.
     *
     * positive value:
     * The font mapper SHOULD transform this value into device units and match it
     * against the cell height of available fonts.
     *
     * For all height comparisons, the font mapper SHOULD find the largest physical
     * font that does not exceed the requested size.
     */
    int height;

    /**
     * A 16-bit signed integer that defines the average width, in logical units, of
     * characters in the font. If Width is 0x0000, the aspect ratio of the device SHOULD be matched
     * against the digitization aspect ratio of the available fonts to find the closest match,
     * determined by the absolute value of the difference.
     */
    int width;

    /**
     * A 16-bit signed integer that defines the angle, in tenths of degrees, between the
     * escapement vector and the x-axis of the device. The escapement vector is parallel
     * to the base line of a row of text.
     */
    int escapement;

    /**
     * A 16-bit signed integer that defines the angle, in tenths of degrees,
     * between each character's base line and the x-axis of the device.
     */
    int orientation;

    /**
     * A 16-bit signed integer that defines the weight of the font in the range 0
     * through 1000. For example, 400 is normal and 700 is bold. If this value is 0x0000,
     * a default weight SHOULD be used.
     */
    int weight;

    /**
     * A 8-bit Boolean value that specifies the italic attribute of the font.
     * 0 = not italic / 1 = italic.
     */
    boolean italic;

    /**
     * An 8-bit Boolean value that specifies the underline attribute of the font.
     * 0 = not underlined / 1 = underlined
     */
    boolean underline;

    /**
     * An 8-bit Boolean value that specifies the strike out attribute of the font.
     * 0 = not striked out / 1 = striked out
     */
    boolean strikeOut;

    /**
     * An 8-bit unsigned integer that defines the character set.
     * It SHOULD be set to a value in the {@link WmfCharset} Enumeration.
     *
     * The DEFAULT_CHARSET value MAY be used to allow the name and size of a font to fully
     * describe the logical font. If the specified font name does not exist, a font in another character
     * set MAY be substituted. The DEFAULT_CHARSET value is set to a value based on the current
     * system locale. For example, when the system locale is United States, it is set to ANSI_CHARSET.
     * If a typeface name in the FaceName field is specified, the CharSet value MUST match the
     * character set of that typeface.
     */
    WmfCharset charSet;

    /**
     * An 8-bit unsigned integer that defines the output precision.
     */
    WmfOutPrecision outPrecision;

    /**
     * An 8-bit unsigned integer that defines the clipping precision.
     * These flags can be combined to specify multiple options.
     *
     * @see WmfClipPrecision
     */
    WmfClipPrecision clipPrecision;

    /**
     * An 8-bit unsigned integer that defines the output quality.
     */
    WmfFontQuality quality;

    /**
     * A PitchAndFamily object that defines the pitch and the family of the font.
     * Font families specify the look of fonts in a general way and are intended for
     * specifying fonts when the exact typeface wanted is not available.
     */
    int pitchAndFamily;
    
    /**
     * Font families specify the look of fonts in a general way and are
     * intended for specifying fonts when the exact typeface wanted is not available.
     * (LSB 4 bits)
     */
    WmfFontFamilyClass family;
    
    /**
     * A property of a font that describes the pitch (MSB 2 bits)
     */
    WmfFontPitch pitch;

    /**
     * A null-terminated string of 8-bit Latin-1 [ISO/IEC-8859-1] ANSI
     * characters that specifies the typeface name of the font. The length of this string MUST NOT
     * exceed 32 8-bit characters, including the terminating null.
     */
    String facename;

    public int init(LittleEndianInputStream leis) throws IOException {
        height = leis.readShort();
        width = leis.readShort();
        escapement = leis.readShort();
        orientation = leis.readShort();
        weight = leis.readShort();
        italic = leis.readByte() != 0;
        underline = leis.readByte() != 0;
        strikeOut = leis.readByte() != 0;
        charSet = WmfCharset.valueOf(leis.readUByte());
        outPrecision = WmfOutPrecision.valueOf(leis.readUByte());
        clipPrecision = WmfClipPrecision.valueOf(leis.readUByte());
        quality = WmfFontQuality.valueOf(leis.readUByte());
        pitchAndFamily = leis.readUByte();
        
        byte buf[] = new byte[32], b, readBytes = 0;
        do {
            if (readBytes == 32) {
                throw new IOException("Font facename can't be determined.");
            }

            buf[readBytes++] = b = leis.readByte();
        } while (b != 0 && b != -1 && readBytes <= 32);
        
        facename = new String(buf, 0, readBytes-1, Charset.forName("ISO-8859-1"));
        
        return 5*LittleEndianConsts.SHORT_SIZE+8*LittleEndianConsts.BYTE_SIZE+readBytes;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getEscapement() {
        return escapement;
    }

    public int getOrientation() {
        return orientation;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public boolean isStrikeOut() {
        return strikeOut;
    }

    public WmfCharset getCharSet() {
        return charSet;
    }

    public WmfOutPrecision getOutPrecision() {
        return outPrecision;
    }

    public WmfClipPrecision getClipPrecision() {
        return clipPrecision;
    }

    public WmfFontQuality getQuality() {
        return quality;
    }

    public int getPitchAndFamily() {
        return pitchAndFamily;
    }

    public WmfFontFamilyClass getFamily() {
        return WmfFontFamilyClass.valueOf(pitchAndFamily & 0xF);
    }

    public WmfFontPitch getPitch() {
        return WmfFontPitch.valueOf((pitchAndFamily >>> 6) & 3);
    }

    public String getFacename() {
        return facename;
    }
}
