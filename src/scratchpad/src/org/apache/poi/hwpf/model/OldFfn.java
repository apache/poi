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

package org.apache.poi.hwpf.model;

import java.nio.charset.Charset;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

/**
 * Word 6.0 Font information
 */
@Internal
public final class OldFfn {

    private static final POILogger logger = POILogFactory.getLogger(OldFfn.class);

    private byte _chs;// character set identifier

    private final String fontName;
    private final String altFontName;

    private final int length; //length in bytes for this record

    /**
     * try to read an OldFfn starting at offset; read no farther than end
     *
     * @param buf          buffer from which to read
     * @param offset       offset at which to start
     * @param fontTableEnd read no farther than this
     * @return an OldFfn or null if asked to read beyond end
     */
    static OldFfn build(byte[] buf, int offset, int fontTableEnd) {
        int start = offset;
        //preliminary bytes
        if (offset + 6 > fontTableEnd) {
            return null;
        }
        //first byte
        short fontDescriptionLength = buf[offset];
        offset += 1;
        if (offset + fontDescriptionLength > fontTableEnd) {
            logger.log(POILogger.WARN, "Asked to read beyond font table end. Skipping font");
            return null;
        }

        //no idea what these 3 bytes do
        offset += 3;
        byte chs = buf[offset];
        Charset charset = null;
        FontCharset wmfCharset = FontCharset.valueOf(chs & 0xff);
        if (wmfCharset == null) {
            logger.log(POILogger.WARN, "Couldn't find font for type: ", (chs & 0xff));
        } else {
            charset = wmfCharset.getCharset();
        }
        charset = charset == null ? StringUtil.WIN_1252 : charset;
        offset += LittleEndianConsts.BYTE_SIZE;
        //if this byte here == 7, it _may_ signify existence of
        //an altername font name

        //not sure what the byte after the _chs does
        offset += LittleEndianConsts.BYTE_SIZE;
        int fontNameLength = -1;
        for (int i = offset; i < fontTableEnd; i++) {
            if (buf[i] == 0) {
                fontNameLength = i - offset;
                break;
            }
        }
        if (fontNameLength == -1) {
            logger.log(POILogger.WARN, "Couldn't find the zero-byte delimited font name length");
            return null;
        }
        String fontName = new String(buf, offset, fontNameLength, charset);
        String altFontName = null;
        int altFontNameLength = -1;
        offset += fontNameLength + 1;
        if (offset - start < fontDescriptionLength) {
            for (int i = offset; i <= start + fontDescriptionLength; i++) {
                if (buf[i] == 0) {
                    altFontNameLength = i - offset;
                    break;
                }
            }
            if (altFontNameLength > -1) {
                altFontName = new String(buf, offset, altFontNameLength, charset);
            }
        }
        //reset to 0 for length calculation
        altFontNameLength = (altFontNameLength < 0) ? 0 : altFontNameLength + 1;//add one for zero byte

        int len = LittleEndianConsts.INT_SIZE + LittleEndianConsts.BYTE_SIZE + LittleEndianConsts.BYTE_SIZE +//6 starting bytes
                fontNameLength + altFontNameLength + 1;//+1 is for the zero byte
        //this len should == fontDescriptionLength

        return new OldFfn(chs, fontName, altFontName, len);

    }

    public OldFfn(byte charsetIdentifier, String fontName, String altFontName, int length) {
        this._chs = charsetIdentifier;
        this.fontName = fontName;
        this.altFontName = altFontName;
        this.length = length;
    }

    public byte getChs() {
        return _chs;
    }

    public String getMainFontName() {
        return fontName;
    }

    /**
     * @return altFontName if it exists, null otherwise
     */
    public String getAltFontName() {
        return altFontName;
    }


    /**
     * @return length in bytes for this record
     */
    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "OldFfn{" +
                "_chs=" + (_chs & 0xff) +
                ", fontName='" + fontName + '\'' +
                ", altFontName='" + altFontName + '\'' +
                ", length=" + length +
                '}';
    }
}


