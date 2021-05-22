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

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * MsoDrawingSelection (0x00ED)<p>
 * Reference:
 * [MS-OGRAPH].pdf sec 2.4.69
 */
public final class DrawingSelectionRecord extends StandardRecord {
    public static final short sid = 0x00ED;

    /**
     * From [MS-ODRAW].pdf sec 2.2.1<p>
     * TODO - make EscherRecordHeader {@link LittleEndianInput} aware and refactor with this
     */
    private static final class OfficeArtRecordHeader implements GenericRecord {
        public static final int ENCODED_SIZE = 8;
        /**
         * lower 4 bits is 'version' usually 0x01 or 0x0F (for containers)
         * upper 12 bits is 'instance'
         */
        private final int _verAndInstance;
        /** value should be between 0xF000 and 0xFFFF */
        private final int _type;
        private final int _length;

        public OfficeArtRecordHeader(LittleEndianInput in) {
            _verAndInstance = in.readUShort();
            _type = in.readUShort();
            _length = in.readInt();
        }

        public void serialize(LittleEndianOutput out) {
            out.writeShort(_verAndInstance);
            out.writeShort(_type);
            out.writeInt(_length);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "verAndInstance", () -> _verAndInstance,
                "type", () -> _type,
                "length", () -> _length
            );
        }
    }

    // [MS-OGRAPH].pdf says that the data of this record is an OfficeArtFDGSL structure
    // as described in[MS-ODRAW].pdf sec 2.2.33
    private OfficeArtRecordHeader _header;
    private int _cpsp;
    /** a MSODGSLK enum value for the current selection mode */
    private int _dgslk;
    private int _spidFocus;
    /** selected shape IDs (e.g. from EscherSpRecord.ShapeId) */
    private int[] _shapeIds;

    public DrawingSelectionRecord(RecordInputStream in) {
        _header = new OfficeArtRecordHeader(in);
        _cpsp = in.readInt();
        _dgslk = in.readInt();
        _spidFocus = in.readInt();
        int nShapes = in.available() / 4;
        int[] shapeIds = new int[nShapes];
        for (int i = 0; i < nShapes; i++) {
            shapeIds[i] = in.readInt();
        }
        _shapeIds = shapeIds;
    }

    public short getSid() {
        return sid;
    }

    protected int getDataSize() {
        return OfficeArtRecordHeader.ENCODED_SIZE
            + 12 // 3 int fields
            + _shapeIds.length * 4;
    }

    public void serialize(LittleEndianOutput out) {
        _header.serialize(out);
        out.writeInt(_cpsp);
        out.writeInt(_dgslk);
        out.writeInt(_spidFocus);
        for (int shapeId : _shapeIds) {
            out.writeInt(shapeId);
        }
    }

    @Override
    public DrawingSelectionRecord copy() {
        // currently immutable
        return this;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DRAWING_SELECTION;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "rh", () -> _header,
            "cpsp", () -> _cpsp,
            "dgslk", () -> _dgslk,
            "spidFocus", () -> _spidFocus,
            "shapeIds", () -> _shapeIds
        );
    }
}
