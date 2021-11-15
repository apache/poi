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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;

public class TextSpecInfoRun implements GenericRecord {

    /**
     * A enum that specifies the spelling status of a run of text.
     */
    public enum SpellInfoEnum {
        /** the text is spelled incorrectly. */
        error(new BitField(1)),
        /** the text needs rechecking. */
        clean(new BitField(2)),
        /** the text has a grammar error. */
        grammar(new BitField(4)),
        /** the text is spelled correct */
        correct(new BitField(0));

        final BitField bitField;

        SpellInfoEnum(BitField bitField) {
            this.bitField = bitField;
        }
    }

    /** A bit that specifies whether the spellInfo field exists. */
    private static final BitField spellFld    = BitFieldFactory.getInstance(0X00000001);
    /** A bit that specifies whether the lid field exists. */
    private static final BitField langFld     = BitFieldFactory.getInstance(0X00000002);
    /** A bit that specifies whether the altLid field exists. */
    private static final BitField altLangFld  = BitFieldFactory.getInstance(0X00000004);
    // unused1, unused2 - Undefined and MUST be ignored.
    /** A bit that specifies whether the pp10runid, reserved3, and grammarError fields exist. */
    private static final BitField pp10extFld  = BitFieldFactory.getInstance(0X00000020);
    /** A bit that specifies whether the bidi field exists. */
    private static final BitField bidiFld     = BitFieldFactory.getInstance(0X00000040);
    // unused3 - Undefined and MUST be ignored.
    // reserved1 - MUST be zero and MUST be ignored.
    /** A bit that specifies whether the smartTags field exists. */
    private static final BitField smartTagFld = BitFieldFactory.getInstance(0X00000200);
    // reserved2 - MUST be zero and MUST be ignored.

    /**
     * An optional unsigned integer that specifies an identifier for a character
     * run that contains StyleTextProp11 data. It MUST exist if and only if pp10ext is TRUE.
     **/
    private static final BitField pp10runidFld = BitFieldFactory.getInstance(0X0000000F);
    // reserved3 - An optional unsigned integer that MUST be zero, and MUST be ignored. It
    // MUST exist if and only if fPp10ext is TRUE.
    /**
     * An optional bit that specifies a grammar error. It MUST exist if and
     * only if fPp10ext is TRUE.
     **/
    private static final BitField grammarErrorFld = BitFieldFactory.getInstance(0X80000000);

    private static final int[] FLAGS_MASKS = {
        0X00000001, 0X00000002, 0X00000004, 0X00000020, 0X00000040, 0X00000200,
    };

    private static final String[] FLAGS_NAMES = {
        "SPELL", "LANG", "ALT_LANG", "PP10_EXT", "BIDI", "SMART_TAG"
    };

    //Length of special info run.
    private int length;

    //Special info mask of this run;
    private int mask;

    // info fields as indicated by the mask.
    // -1 means the bit is not set

    /**
     * An optional SpellingFlags structure that specifies the spelling status of this
     * text. It MUST exist if and only if spell is TRUE.
     * The spellInfo.grammar sub-field MUST be zero.
     * <br>
     * error (1 bit): A bit that specifies whether the text is spelled incorrectly.<br>
     * clean (1 bit): A bit that specifies whether the text needs rechecking.<br>
     * grammar (1 bit): A bit that specifies whether the text has a grammar error.<br>
     * reserved (13 bits): MUST be zero and MUST be ignored.
     */
    private short spellInfo = -1;

    /**
     * An optional TxLCID that specifies the language identifier of this text.
     * It MUST exist if and only if lang is TRUE.
     * <br>
     * 0x0000 = No language.<br>
     * 0x0013 = Any Dutch language is preferred over non-Dutch languages when proofing the text.<br>
     * 0x0400 = No proofing is performed on the text.<br>
     * &gt; 0x0400 = A valid LCID as specified by [MS-LCID].
     */
    private short langId = -1;

    /**
     * An optional TxLCID that specifies the alternate language identifier of this text.
     * It MUST exist if and only if altLang is TRUE.
     */
    private short altLangId = -1;

    /**
     * An optional signed integer that specifies whether the text contains bidirectional
     * characters. It MUST exist if and only if fBidi is TRUE.
     * 0x0000 = Contains no bidirectional characters,
     * 0x0001 = Contains bidirectional characters.
     */
    private short bidi = -1;

    private int pp10extMask = -1;
    private byte[] smartTagsBytes;

    /**
     * Inits a TextSpecInfoRun with default values
     *
     * @param len the length of the one and only run
     */
    public TextSpecInfoRun(int len) {
        setLength(len);
        setLangId((short)0);
    }

    public TextSpecInfoRun(LittleEndianByteArrayInputStream source) {
        length = source.readInt();
        mask = source.readInt();
        if (spellFld.isSet(mask)) {
            spellInfo = source.readShort();
        }
        if (langFld.isSet(mask)) {
            langId = source.readShort();
        }
        if (altLangFld.isSet(mask)) {
            altLangId = source.readShort();
        }
        if (bidiFld.isSet(mask)) {
            bidi = source.readShort();
        }
        if (pp10extFld.isSet(mask)) {
            pp10extMask = source.readInt();
        }
        if (smartTagFld.isSet(mask)) {
            // An unsigned integer specifies the count of items in rgSmartTagIndex.
            int count = source.readInt();
            smartTagsBytes = IOUtils.safelyAllocate(4 + count * 4L, RecordAtom.getMaxRecordLength());
            LittleEndian.putInt(smartTagsBytes, 0, count);
            // An array of SmartTagIndex that specifies the indices.
            // The count of items in the array is specified by count.
            source.readFully(smartTagsBytes, 4, count*4);
        }
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        final byte[] buf = new byte[4];
        LittleEndian.putInt(buf, 0, length);
        out.write(buf);
        LittleEndian.putInt(buf, 0, mask);
        out.write(buf);
        Object[] flds = {
                spellFld, spellInfo, "spell info",
                langFld, langId, "lang id",
                altLangFld, altLangId, "alt lang id",
                bidiFld, bidi, "bidi",
                pp10extFld, pp10extMask, "pp10 extension field",
                smartTagFld, smartTagsBytes, "smart tags"
        };

        for (int i=0; i<flds.length-1; i+=3) {
            BitField fld = (BitField)flds[i+0];
            Object valO = flds[i+1];
            if (!fld.isSet(mask)) {
                continue;
            }
            boolean valid;
            if (valO instanceof byte[]) {
                byte[] bufB = (byte[]) valO;
                valid = bufB.length > 0;
                out.write(bufB);
            } else if (valO instanceof Integer) {
                int valI = ((Integer)valO);
                valid = (valI != -1);
                LittleEndian.putInt(buf, 0, valI);
                out.write(buf);
            } else if (valO instanceof Short) {
                short valS = ((Short)valO);
                valid = (valS != -1);
                LittleEndian.putShort(buf, 0, valS);
                out.write(buf, 0, 2);
            } else {
                valid = false;
            }
            if (!valid) {
                Object fval = (i + 2) < flds.length ? flds[i + 2] : null;
                throw new IOException(fval + " is activated, but its value is invalid");
            }
        }
    }

    /**
     * @return Spelling status of this text. null if not defined.
     */
    public SpellInfoEnum getSpellInfo(){
        if (spellInfo == -1) {
            return null;
        }
        for (SpellInfoEnum si : new SpellInfoEnum[]{SpellInfoEnum.clean,SpellInfoEnum.error,SpellInfoEnum.grammar}) {
            if (si.bitField.isSet(spellInfo)) {
                return si;
            }
        }
        return SpellInfoEnum.correct;
    }

    /**
     * @param spellInfo Spelling status of this text. null if not defined.
     */
    public void setSpellInfo(SpellInfoEnum spellInfo) {
        this.spellInfo = (spellInfo == null)
            ? -1
            : (short)spellInfo.bitField.set(0);
        mask = spellFld.setBoolean(mask, spellInfo != null);
    }

    /**
     * Windows LANGID for this text.
     *
     * @return Windows LANGID for this text, -1 if it's not set
     */
    public short getLangId(){
        return langId;
    }

    /**
     * @param langId Windows LANGID for this text, -1 to unset
     */
    public void setLangId(short langId) {
        this.langId = langId;
        mask = langFld.setBoolean(mask, langId != -1);
    }

    /**
     * Alternate Windows LANGID of this text;
     * must be a valid non-East Asian LANGID if the text has an East Asian language,
     * otherwise may be an East Asian LANGID or language neutral (zero).
     *
     * @return  Alternate Windows LANGID of this text, -1 if it's not set
     */
    public short getAltLangId(){
        return altLangId;
    }

    public void setAltLangId(short altLangId) {
        this.altLangId = altLangId;
        mask = altLangFld.setBoolean(mask, altLangId != -1);
    }

    /**
     * @return Length of special info run.
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length Length of special info run.
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the bidirectional characters flag. false = not bidi, true = is bidi, null = not set
     */
    public Boolean getBidi() {
        return (bidi == -1 ? null : bidi != 0);
    }

    /**
     * @param bidi the bidirectional characters flag. false = not bidi, true = is bidi, null = not set
     */
    public void setBidi(Boolean bidi) {
        this.bidi = (bidi == null) ? -1 : (short)(bidi ? 1 : 0);
        mask = bidiFld.setBoolean(mask, bidi != null);
    }

    /**
     * @return the unparsed smart tags
     */
    public byte[] getSmartTagsBytes() {
        return smartTagsBytes;
    }

    /**
     * @param smartTagsBytes the unparsed smart tags, null to unset
     */
    public void setSmartTagsBytes(byte[] smartTagsBytes) {
        this.smartTagsBytes = (smartTagsBytes == null) ? null : smartTagsBytes.clone();
        mask = smartTagFld.setBoolean(mask, smartTagsBytes != null);
    }

    /**
     * @return an identifier for a character run that contains StyleTextProp11 data.
     */
    public int getPP10RunId() {
        return (pp10extMask == -1 || !pp10extFld.isSet(mask)) ? -1 : pp10runidFld.getValue(pp10extMask);

    }

    /**
     * @param pp10RunId an identifier for a character run that contains StyleTextProp11 data, -1 to unset
     */
    public void setPP10RunId(int pp10RunId) {
        if (pp10RunId == -1) {
            pp10extMask = (getGrammarError() == null) ? -1 : pp10runidFld.clear(pp10extMask);
        } else {
            pp10extMask = pp10runidFld.setValue(pp10extMask, pp10RunId);
        }
        // if both parameters are invalid, remove the extension mask
        mask = pp10extFld.setBoolean(mask, pp10extMask != -1);
    }

    public Boolean getGrammarError() {
        return (pp10extMask == -1 || !pp10extFld.isSet(mask)) ? null : grammarErrorFld.isSet(pp10extMask);
    }

    public void getGrammarError(Boolean grammarError) {
        if (grammarError == null) {
            pp10extMask = (getPP10RunId() == -1) ? -1 : grammarErrorFld.clear(pp10extMask);
        } else {
            pp10extMask = grammarErrorFld.set(pp10extMask);
        }
        // if both parameters are invalid, remove the extension mask
        mask = pp10extFld.setBoolean(mask, pp10extMask != -1);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("flags", getBitsAsString(() -> mask, FLAGS_MASKS, FLAGS_NAMES));
        m.put("spellInfo", this::getSpellInfo);
        m.put("langId", this::getLangId);
        m.put("altLangId", this::getAltLangId);
        m.put("bidi", this::getBidi);
        m.put("pp10RunId", this::getPP10RunId);
        m.put("grammarError", this::getGrammarError);
        m.put("smartTags", this::getSmartTagsBytes);
        return Collections.unmodifiableMap(m);
    }
}
