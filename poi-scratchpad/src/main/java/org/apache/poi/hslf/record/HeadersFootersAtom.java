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

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * An atom record that specifies options for displaying headers and footers
 * on a presentation slide or notes slide.
 */

public final class HeadersFootersAtom extends RecordAtom {

    /**
     * A bit that specifies whether the date is displayed in the footer.
     * @see #getMask()
     * @see #setMask(int)
     */
    public static final int fHasDate = 1;

    /**
     * A bit that specifies whether the current datetime is used for displaying the datetime.
     * @see #getMask()
     * @see #setMask(int)
     */
    public static final int fHasTodayDate = 2;

    /**
     * A bit that specifies whether the date specified in UserDateAtom record
     * is used for displaying the datetime.
     *
     * @see #getMask()
     * @see #setMask(int)
     */
     public static final int fHasUserDate = 4;

    /**
     * A bit that specifies whether the slide number is displayed in the footer.
     *
     * @see #getMask()
     * @see #setMask(int)
     */
    public static final int fHasSlideNumber = 8;

    /**
     * bit that specifies whether the header text is displayed.
     *
     * @see #getMask()
     * @see #setMask(int)
     */
    public static final int fHasHeader = 16;

    /**
     * bit that specifies whether the footer text is displayed.
     *
     * @see #getMask()
     * @see #setMask(int)
     */
    public static final int fHasFooter = 32;

    private static final int[] PLACEHOLDER_MASKS = {
        fHasDate,
        fHasTodayDate,
        fHasUserDate,
        fHasSlideNumber,
        fHasHeader,
        fHasFooter
    };

    private static final String[] PLACEHOLDER_NAMES = {
        "DATE", "TODAY_DATE", "USER_DATE", "SLIDE_NUMBER", "HEADER", "FOOTER"
    };



    /**
     * record header
     */
    private final byte[] _header;

    /**
     * record data
     */
    private final byte[] _recdata;

    /**
     * Build an instance of {@code HeadersFootersAtom} from on-disk data
     */
    HeadersFootersAtom(byte[] source, int start, int len) {
        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Grab the record data
        _recdata = IOUtils.safelyClone(source, start+8, len-8, getMaxRecordLength());
    }

    /**
     * Create a new instance of {@code HeadersFootersAtom}
     */
    public HeadersFootersAtom() {
        _recdata = new byte[4];

        _header = new byte[8];
        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _recdata.length);
    }

    @Override
    public long getRecordType() {
        return RecordTypes.HeadersFootersAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written to disk
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_recdata);
    }

    /**
     * A signed integer that specifies the format ID to be used to style the datetime.
     * <p>
     * It MUST be in the range [0, 12]. <p>
     * This value is converted into a string as specified by the index field of the DateTimeMCAtom record.
     * It MUST be ignored unless fHasTodayDate is TRUE.
     *
     * @return  A signed integer that specifies the format ID to be used to style the datetime.
     */
    public int getFormatId(){
        return LittleEndian.getShort(_recdata, 0);
    }


    /**
     * A signed integer that specifies the format ID to be used to style the datetime.
     *
     * @param formatId  A signed integer that specifies the format ID to be used to style the datetime.
     */
    public void setFormatId(int formatId){
         LittleEndian.putUShort(_recdata, 0, formatId);
    }

    /**
     *  A bit mask specifying options for displaying headers and footers
     *
     * <ul>
     * <li> A - {@link #fHasDate} (1 bit): A bit that specifies whether the date is displayed in the footer.
     * <li> B - {@link #fHasTodayDate} (1 bit): A bit that specifies whether the current datetime is used for
     *      displaying the datetime.
     * <li> C - {@link #fHasUserDate} (1 bit): A bit that specifies whether the date specified in UserDateAtom record
     *      is used for displaying the datetime.
     * <li> D - {@link #fHasSlideNumber} (1 bit): A bit that specifies whether the slide number is displayed in the footer.
     * <li> E - {@link #fHasHeader} (1 bit): A bit that specifies whether the header text specified by HeaderAtom
     *      record is displayed.
     * <li> F - {@link #fHasFooter} (1 bit): A bit that specifies whether the footer text specified by FooterAtom
     *      record is displayed.
     * <li> reserved (10 bits): MUST be zero and MUST be ignored.
     * </ul>
     *
     * @return A bit mask specifying options for displaying headers and footers
     */
    public int getMask(){
        return LittleEndian.getShort(_recdata, 2);
    }

    /**
     *  A bit mask specifying options for displaying headers and footers
     *
     * @param mask A bit mask specifying options for displaying headers and footers
     */
    public void setMask(int mask){
        LittleEndian.putUShort(_recdata, 2, mask);
    }

    /**
     * @param bit the bit to check
     * @return whether the specified flag is set
     */
    public boolean getFlag(int bit){
        return (getMask() & bit) != 0;
    }

    /**
     * @param  bit the bit to set
     * @param  value whether the specified bit is set
     */
    public void setFlag(int bit, boolean value){
        int mask = getMask();
        if(value) mask |= bit;
        else mask &= ~bit;
        setMask(mask);
    }

    public String toString(){
        return
            "HeadersFootersAtom\n" +
            "\tFormatId: " + getFormatId() + "\n" +
            "\tMask    : " + getMask() + "\n" +
            "\t  fHasDate        : " + getFlag(fHasDate) + "\n" +
            "\t  fHasTodayDate   : " + getFlag(fHasTodayDate) + "\n" +
            "\t  fHasUserDate    : " + getFlag(fHasUserDate) + "\n" +
            "\t  fHasSlideNumber : " + getFlag(fHasSlideNumber) + "\n" +
            "\t  fHasHeader      : " + getFlag(fHasHeader) + "\n" +
            "\t  fHasFooter      : " + getFlag(fHasFooter) + "\n";
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "formatIndex", this::getFormatId,
            "flags", getBitsAsString(this::getMask, PLACEHOLDER_MASKS, PLACEHOLDER_NAMES)
        );
    }
}
