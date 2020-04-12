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

import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;

/**
 * Shows where the Interface Records ends (MMS)
 */
public final class InterfaceEndRecord extends StandardRecord {

    public static final short sid = 0x00E2;
    public static final InterfaceEndRecord instance = new InterfaceEndRecord();

    private InterfaceEndRecord() {
        // enforce singleton
    }

    public static org.apache.poi.hssf.record.Record create(RecordInputStream in) {
        switch (in.remaining()) {
            case 0:
                return instance;
            case 2:
                return new InterfaceHdrRecord(in);
        }
        throw new RecordFormatException("Invalid record data size: " + in.remaining());
    }

    public void serialize(LittleEndianOutput out) {
        // no instance data
    }

    protected int getDataSize() {
        return 0;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public InterfaceEndRecord copy() {
        return instance;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.INTERFACE_END;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
