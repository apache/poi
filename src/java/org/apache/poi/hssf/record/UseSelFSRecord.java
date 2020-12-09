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

package org.apache.poi.hssf.record;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Tells the GUI if this was written by something that can use "natural language" formulas. HSSF can't.
 */
public final class UseSelFSRecord extends StandardRecord {
    public static final short sid   = 0x0160;

    private static final BitField useNaturalLanguageFormulasFlag = BitFieldFactory.getInstance(0x0001);

    private int _options;

    private UseSelFSRecord(UseSelFSRecord other) {
        super(other);
        _options = other._options;
    }

    private UseSelFSRecord(int options) {
        _options = options;
    }

    public UseSelFSRecord(RecordInputStream in) {
        this(in.readUShort());
    }

    public UseSelFSRecord(boolean b) {
        this(0);
        _options = useNaturalLanguageFormulasFlag.setBoolean(_options, b);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_options);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public UseSelFSRecord copy() {
        return new UseSelFSRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.USE_SEL_FS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("options", getBitsAsString(() -> _options,
             new BitField[]{useNaturalLanguageFormulasFlag}, new String[]{"USE_NATURAL_LANGUAGE_FORMULAS_FLAG"}));
    }
}
