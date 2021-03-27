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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

public final class DrawingRecord extends StandardRecord {
    public static final short sid = 0x00EC;

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private byte[] recordData;
    private byte[] contd;

    public DrawingRecord() {
        recordData = EMPTY_BYTE_ARRAY;
    }

    public DrawingRecord(DrawingRecord other) {
        super(other);
        recordData = (other.recordData == null) ? null : other.recordData.clone();
        // TODO - this code probably never copies a contd array ...
        contd = (other.contd == null) ? null : other.contd.clone();
    }

    public DrawingRecord(RecordInputStream in) {
        recordData = in.readRemainder();
    }

    public DrawingRecord(byte[] data) {
        recordData = data.clone();
    }

    /**
     * @deprecated POI 3.9
     */
    @Deprecated
    void processContinueRecord(byte[] record) {
        //don't merge continue record with the drawing record, it must be serialized separately
        contd = record;
    }

    public void serialize(LittleEndianOutput out) {
        out.write(recordData);
    }

    protected int getDataSize() {
        return recordData.length;
    }

    public short getSid() {
        return sid;
    }

    public byte[] getRecordData(){
        return recordData;
    }

    public void setData(byte[] thedata) {
        if (thedata == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        recordData = thedata;
    }

    /**
     * Cloning of drawing records must be executed through HSSFPatriarch, because all id's must be changed
     * @return cloned drawing records
     */
    @Override
    public DrawingRecord copy() {
        return new DrawingRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DRAWING;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "recordData", this::getRecordData,
            "contd", () -> contd
        );
    }
}
