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

import org.apache.poi.util.LittleEndianOutput;
/**
 * DrawingRecord (0x00EC)
 */
public final class DrawingRecord extends StandardRecord implements Cloneable {
    public static final short sid = 0x00EC;

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private byte[] recordData;
    private byte[] contd;

    public DrawingRecord() {
        recordData = EMPTY_BYTE_ARRAY;
    }

    public DrawingRecord(RecordInputStream in) {
        recordData = in.readRemainder();
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
    public DrawingRecord clone() {
        DrawingRecord rec = new DrawingRecord();
        rec.recordData = recordData.clone();
        if (contd != null) {
            // TODO - this code probably never executes
            rec.contd = contd.clone();
        }

        return rec;
    }

    @Override
    public String toString() {
        return "DrawingRecord["+recordData.length+"]";
    }
}
