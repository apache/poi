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

package org.apache.poi.hssf.record.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

@Internal
public class ExtRst implements Comparable<ExtRst>, GenericRecord {
    private static final POILogger _logger = POILogFactory.getLogger(ExtRst.class);
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private short reserved;

    // This is a Phs (see page 881)
    private short formattingFontIndex;
    private short formattingOptions;

    // This is a RPHSSub (see page 894)
    private int numberOfRuns;
    private String phoneticText;

    // This is an array of PhRuns (see page 881)
    private PhRun[] phRuns;
    // Sometimes there's some cruft at the end
    private byte[] extraData;

    protected ExtRst() {
        populateEmpty();
    }

    protected ExtRst(ExtRst other) {
        this();
        reserved = other.reserved;
        formattingFontIndex = other.formattingFontIndex;
        formattingOptions = other.formattingOptions;
        numberOfRuns = other.numberOfRuns;
        phoneticText = other.phoneticText;
        phRuns = (other.phRuns == null) ? null : Stream.of(other.phRuns).map(PhRun::new).toArray(PhRun[]::new);
    }

    protected ExtRst(LittleEndianInput in, int expectedLength) {
        reserved = in.readShort();

        // Old style detection (Reserved = 0xFF)
        if(reserved == -1) {
            populateEmpty();
            return;
        }

        // Spot corrupt records
        if(reserved != 1) {
            _logger.log(POILogger.WARN, "Warning - ExtRst has wrong magic marker, expecting 1 but found ", reserved, " - ignoring");
            // Grab all the remaining data, and ignore it
            for(int i=0; i<expectedLength-2; i++) {
                in.readByte();
            }
            // And make us be empty
            populateEmpty();
            return;
        }

        // Carry on reading in as normal
        short stringDataSize = in.readShort();

        formattingFontIndex = in.readShort();
        formattingOptions   = in.readShort();

        // RPHSSub
        numberOfRuns = in.readUShort();
        short length1 = in.readShort();
        // No really. Someone clearly forgot to read
        //  the docs on their datastructure...
        short length2 = in.readShort();
        // And sometimes they write out garbage :(
        if(length1 == 0 && length2 > 0) {
            length2 = 0;
        }
        if(length1 != length2) {
            throw new IllegalStateException(
                    "The two length fields of the Phonetic Text don't agree! " +
                            length1 + " vs " + length2
            );
        }
        phoneticText = StringUtil.readUnicodeLE(in, length1);

        int runData = stringDataSize - 4 - 6 - (2*phoneticText.length());
        int numRuns = (runData / 6);
        phRuns = new PhRun[numRuns];
        for(int i=0; i<phRuns.length; i++) {
            phRuns[i] = new PhRun(in);
        }

        int extraDataLength = runData - (numRuns*6);
        if(extraDataLength < 0) {
            _logger.log( POILogger.WARN, "Warning - ExtRst overran by ",  (0-extraDataLength), " bytes");
            extraDataLength = 0;
        }
        extraData = IOUtils.safelyAllocate(extraDataLength, MAX_RECORD_LENGTH);
        for(int i=0; i<extraData.length; i++) {
            extraData[i] = in.readByte();
        }
    }

    private void populateEmpty() {
        reserved = 1;
        phoneticText = "";
        phRuns = new PhRun[0];
        extraData = new byte[0];
    }

    /**
     * Returns our size, excluding our
     *  4 byte header
     */
    protected int getDataSize() {
        return 4 + 6 + (2*phoneticText.length()) +
                (6*phRuns.length) + extraData.length;
    }
    protected void serialize(ContinuableRecordOutput out) {
        int dataSize = getDataSize();

        out.writeContinueIfRequired(8);
        out.writeShort(reserved);
        out.writeShort(dataSize);
        out.writeShort(formattingFontIndex);
        out.writeShort(formattingOptions);

        out.writeContinueIfRequired(6);
        out.writeShort(numberOfRuns);
        out.writeShort(phoneticText.length());
        out.writeShort(phoneticText.length());

        out.writeContinueIfRequired(phoneticText.length()*2);
        StringUtil.putUnicodeLE(phoneticText, out);

        for (PhRun phRun : phRuns) {
            phRun.serialize(out);
        }

        out.write(extraData);
    }

    public boolean equals(Object obj) {
        if(! (obj instanceof ExtRst)) {
            return false;
        }
        ExtRst other = (ExtRst)obj;
        return (compareTo(other) == 0);
    }
    public int compareTo(ExtRst o) {
        int result;

        result = reserved - o.reserved;
        if (result != 0) {
            return result;
        }
        result = formattingFontIndex - o.formattingFontIndex;
        if (result != 0) {
            return result;
        }
        result = formattingOptions - o.formattingOptions;
        if (result != 0) {
            return result;
        }
        result = numberOfRuns - o.numberOfRuns;
        if (result != 0) {
            return result;
        }

        result = phoneticText.compareTo(o.phoneticText);
        if (result != 0) {
            return result;
        }

        result = phRuns.length - o.phRuns.length;
        if (result != 0) {
            return result;
        }
        for(int i=0; i<phRuns.length; i++) {
            result = phRuns[i].phoneticTextFirstCharacterOffset - o.phRuns[i].phoneticTextFirstCharacterOffset;
            if (result != 0) {
                return result;
            }
            result = phRuns[i].realTextFirstCharacterOffset - o.phRuns[i].realTextFirstCharacterOffset;
            if (result != 0) {
                return result;
            }
            result = phRuns[i].realTextLength - o.phRuns[i].realTextLength;
            if (result != 0) {
                return result;
            }
        }

        result = Arrays.hashCode(extraData)-Arrays.hashCode(o.extraData);

        return result;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{reserved, formattingFontIndex, formattingOptions, numberOfRuns, phoneticText, phRuns});
    }

    public ExtRst copy() {
        return new ExtRst(this);
    }

    public short getFormattingFontIndex() {
        return formattingFontIndex;
    }
    public short getFormattingOptions() {
        return formattingOptions;
    }
    public int getNumberOfRuns() {
        return numberOfRuns;
    }
    public String getPhoneticText() {
        return phoneticText;
    }
    public PhRun[] getPhRuns() {
        return phRuns;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "reserved", () -> reserved,
            "formattingFontIndex", this::getFormattingFontIndex,
            "formattingOptions", this::getFormattingOptions,
            "numberOfRuns", this::getNumberOfRuns,
            "phoneticText", this::getPhoneticText,
            "phRuns", this::getPhRuns,
            "extraData", () -> extraData
        );
    }
}
