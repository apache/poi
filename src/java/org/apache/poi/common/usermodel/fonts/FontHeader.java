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

package org.apache.poi.common.usermodel.fonts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;


/**
 * The header data of an EOT font.<p>
 *
 * Currently only version 1 fields are read to identify a stream to be embedded.
 *
 * @see <a href="http://www.w3.org/Submission/EOT">Embedded OpenType (EOT) File Format</a>
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "Duplicates"})
public class FontHeader implements FontInfo {
    /**
     * Fonts with a font weight of 400 are regarded as regular weighted.
     * Higher font weights (up to 1000) are bold - lower weights are thin.
     */
    public static final int REGULAR_WEIGHT = 400;

    private int eotSize;
    private int fontDataSize;
    private int version;
    private int flags;
    private final byte[] panose = new byte[10];
    private byte charset;
    private byte italic;
    private int weight;
    private int fsType;
    private int magic;
    private int unicodeRange1;
    private int unicodeRange2;
    private int unicodeRange3;
    private int unicodeRange4;
    private int codePageRange1;
    private int codePageRange2;
    private int checkSumAdjustment;
    private String familyName;
    private String styleName;
    private String versionName;
    private String fullName;

    public void init(byte[] source, int offset, int length) {
        init(new LittleEndianByteArrayInputStream(source, offset, length));
    }

    public void init(LittleEndianInput leis) {
        eotSize = leis.readInt();
        fontDataSize = leis.readInt();
        version = leis.readInt();
        if (version != 0x00010000 && version != 0x00020001 && version != 0x00020002) {
            throw new RuntimeException("not a EOT font data stream");
        }
        flags = leis.readInt();
        leis.readFully(panose);
        charset = leis.readByte();
        italic = leis.readByte();
        weight = leis.readInt();
        fsType = leis.readUShort();
        magic = leis.readUShort();
        if (magic != 0x504C) {
            throw new RuntimeException("not a EOT font data stream");
        }
        unicodeRange1 = leis.readInt();
        unicodeRange2 = leis.readInt();
        unicodeRange3 = leis.readInt();
        unicodeRange4 = leis.readInt();
        codePageRange1 = leis.readInt();
        codePageRange2 = leis.readInt();
        checkSumAdjustment = leis.readInt();
        int reserved1 = leis.readInt();
        int reserved2 = leis.readInt();
        int reserved3 = leis.readInt();
        int reserved4 = leis.readInt();
        familyName = readName(leis);
        styleName = readName(leis);
        versionName = readName(leis);
        fullName = readName(leis);

    }

    public InputStream bufferInit(InputStream fontStream) throws IOException {
        LittleEndianInputStream is = new LittleEndianInputStream(fontStream);
        is.mark(1000);
        init(is);
        is.reset();
        return is;
    }

    private String readName(LittleEndianInput leis) {
        // padding
        leis.readShort();
        int nameSize = leis.readUShort();
        byte[] nameBuf = IOUtils.safelyAllocate(nameSize, 1000);
        leis.readFully(nameBuf);
        // may be 0-terminated, just trim it away
        return new String(nameBuf, 0, nameSize, StandardCharsets.UTF_16LE).trim();
    }

    public boolean isItalic() {
        return italic != 0;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isBold() {
        return getWeight() > REGULAR_WEIGHT;
    }

    public byte getCharsetByte() {
        return charset;
    }

    public FontCharset getCharset() {
        return FontCharset.valueOf(getCharsetByte());
    }

    public FontPitch getPitch() {
        byte familyKind = panose[0];
        switch (familyKind) {
            default:
            // Any
            case 0:
            // No Fit
            case 1:
                return FontPitch.VARIABLE;

            // Latin Text
            case 2:
                // Latin Decorative
            case 4:
                byte proportion = panose[3];
                return proportion == 9 ? FontPitch.FIXED : FontPitch.VARIABLE;

            // Latin Hand Written
            case 3:
                // Latin Symbol
            case 5:
                byte spacing = panose[3];
                return spacing == 3 ? FontPitch.FIXED : FontPitch.VARIABLE;
        }

    }

    public FontFamily getFamily() {
        switch (panose[0]) {
            // Any
            case 0:
            // No Fit
            case 1:
                return FontFamily.FF_DONTCARE;
            // Latin Text
            case 2:
                byte serifStyle = panose[1];
                return (10 <= serifStyle && serifStyle <= 15)
                    ? FontFamily.FF_SWISS : FontFamily.FF_ROMAN;
            // Latin Hand Written
            case 3:
                return FontFamily.FF_SCRIPT;
            // Latin Decorative
            default:
            case 4:
                return FontFamily.FF_DECORATIVE;
            // Latin Symbol
            case 5:
                return FontFamily.FF_MODERN;
        }
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getStyleName() {
        return styleName;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getFullName() {
        return fullName;
    }

    public byte[] getPanose() {
        return panose;
    }

    @Override
    public String getTypeface() {
        return getFamilyName();
    }

    public int getFlags() {
        return flags;
    }
}



