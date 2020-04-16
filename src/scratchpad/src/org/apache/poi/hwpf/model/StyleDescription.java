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

import java.util.Arrays;
import java.util.Objects;

import org.apache.poi.hwpf.model.types.StdfBaseAbstractType;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */
@Internal
public final class StyleDescription {

    private static final POILogger logger = POILogFactory.getLogger(StyleDescription.class);
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private final static int PARAGRAPH_STYLE = 1;
    private final static int CHARACTER_STYLE = 2;
    // private final static int TABLE_STYLE = 3;
    // private final static int NUMBERING_STYLE = 4;

    private int _baseLength;
    private StdfBase _stdfBase;
    private StdfPost2000 _stdfPost2000;

    UPX[] _upxs;
    String _name;
    @Deprecated
    ParagraphProperties _pap;
    @Deprecated
    CharacterProperties _chp;

    public StyleDescription() {
//      _pap = new ParagraphProperties();
//      _chp = new CharacterProperties();
    }

    public StyleDescription(byte[] std, int baseLength, int offset, boolean word9) {
        _baseLength = baseLength;
        int nameStart = offset + baseLength;

        boolean readStdfPost2000 = false;
        if (baseLength == 0x0012) {
            readStdfPost2000 = true;
        } else if (baseLength == 0x000A) {
            readStdfPost2000 = false;
        } else {
            logger.log(POILogger.WARN, "Style definition has non-standard size of ", baseLength);
        }

        _stdfBase = new StdfBase(std, offset);
        offset += StdfBaseAbstractType.getSize();

        if (readStdfPost2000) {
            _stdfPost2000 = new StdfPost2000(std, offset);
            // offset += StdfPost2000.getSize();
        }

        //first byte(s) of variable length section of std is the length of the
        //style name and aliases string
        int nameLength = 0;
        int multiplier = 1;
        if (word9) {
            nameLength = LittleEndian.getShort(std, nameStart);
            multiplier = 2;
            nameStart += LittleEndianConsts.SHORT_SIZE;
        } else {
            nameLength = std[nameStart];
        }

        _name = StringUtil.getFromUnicodeLE(std, nameStart, (nameLength * multiplier) / 2);

        //length then null terminator.

        // the spec only refers to two possible upxs but it mentions
        // that more may be added in the future
        int varOffset = ((nameLength + 1) * multiplier) + nameStart;
        int countOfUPX = _stdfBase.getCupx();
        _upxs = new UPX[countOfUPX];
        for (int x = 0; x < countOfUPX; x++) {
            int upxSize = LittleEndian.getShort(std, varOffset);
            varOffset += LittleEndianConsts.SHORT_SIZE;

            byte[] upx = IOUtils.safelyClone(std, varOffset, upxSize, Short.MAX_VALUE);
            _upxs[x] = new UPX(upx);
            varOffset += upxSize;


            // the upx will always start on a word boundary.
            if ((upxSize & 1) == 1) {
                ++varOffset;
            }

        }


    }

    public int getBaseStyle() {
        return _stdfBase.getIstdBase();
    }

    public byte[] getCHPX() {
        switch (_stdfBase.getStk()) {
            case PARAGRAPH_STYLE:
                if (_upxs.length > 1) {
                    return _upxs[1].getUPX();
                }
                return null;
            case CHARACTER_STYLE:
                return _upxs[0].getUPX();
            default:
                return null;
        }

    }

    public byte[] getPAPX() {
        return _stdfBase.getStk() == PARAGRAPH_STYLE ? _upxs[0].getUPX() : null;
    }

    @Deprecated
    public ParagraphProperties getPAP() {
        return _pap;
    }

    @Deprecated
    public CharacterProperties getCHP() {
        return _chp;
    }

    @Deprecated
    void setPAP(ParagraphProperties pap) {
        _pap = pap;
    }

    @Deprecated
    void setCHP(CharacterProperties chp) {
        _chp = chp;
    }

    public String getName() {
        return _name;
    }

    public byte[] toByteArray() {
        // size equals _baseLength bytes for known variables plus 2 bytes for name
        // length plus name length * 2 plus 2 bytes for null plus upx's preceded by
        // length
        int size = _baseLength + 2 + ((_name.length() + 1) * 2);

        // determine the size needed for the upxs. They always fall on word
        // boundaries.
        size += _upxs[0].size() + 2;
        for (int x = 1; x < _upxs.length; x++) {
            size += _upxs[x - 1].size() % 2;
            size += _upxs[x].size() + 2;
        }


        byte[] buf = new byte[size];
        _stdfBase.serialize(buf, 0);

        int offset = _baseLength;

        char[] letters = _name.toCharArray();
        LittleEndian.putShort(buf, _baseLength, (short) letters.length);
        offset += LittleEndianConsts.SHORT_SIZE;
        for (char letter : letters) {
            LittleEndian.putShort(buf, offset, (short) letter);
            offset += LittleEndianConsts.SHORT_SIZE;
        }
        // get past the null delimiter for the name.
        offset += LittleEndianConsts.SHORT_SIZE;

        for (UPX upx : _upxs) {
            short upxSize = (short) upx.size();
            LittleEndian.putShort(buf, offset, upxSize);
            offset += LittleEndianConsts.SHORT_SIZE;
            System.arraycopy(upx.getUPX(), 0, buf, offset, upxSize);
            offset += upxSize + (upxSize % 2);
        }

        return buf;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{_name,_stdfBase,_upxs});
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StyleDescription))
            return false;
        StyleDescription other = (StyleDescription) obj;
        if (!Objects.equals(_name, other._name)) {
            return false;
        }
        if (!Objects.equals(_stdfBase, other._stdfBase)) {
            return false;
        }
        return Arrays.equals(_upxs, other._upxs);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[STD]: '");
        result.append(_name);
        result.append("'");
        result.append(("\nStdfBase:\t" + _stdfBase).replaceAll("\n",
                "\n    "));
        result.append(("\nStdfPost2000:\t" + _stdfPost2000).replaceAll(
                "\n", "\n    "));
        for (UPX upx : _upxs) {
            result.append(("\nUPX:\t" + upx).replaceAll("\n", "\n    "));
        }
        return result.toString();
    }
}
