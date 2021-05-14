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

package org.apache.poi.ddf;

import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Generates escher records when provided the byte array containing those records.
 *
 * @see EscherRecordFactory
 */
public class DefaultEscherRecordFactory implements EscherRecordFactory {
    private static final BitField IS_CONTAINER = BitFieldFactory.getInstance(0xF);

    /**
     * Creates an instance of the escher record factory
     */
    public DefaultEscherRecordFactory() {
        // no instance initialisation
    }

    @Override
    public EscherRecord createRecord(byte[] data, int offset) {
        short options = LittleEndian.getShort( data, offset );
        short recordId = LittleEndian.getShort( data, offset + 2 );
        // int remainingBytes = LittleEndian.getInt( data, offset + 4 );

        final EscherRecord escherRecord = getConstructor(options, recordId).get();
        escherRecord.setRecordId(recordId);
        escherRecord.setOptions(options);
        return escherRecord;
    }

    protected Supplier<? extends EscherRecord> getConstructor(short options, short recordId) {
        EscherRecordTypes recordTypes = EscherRecordTypes.forTypeID(recordId);

        // Options of 0x000F means container record
        // However, EscherTextboxRecord are containers of records for the host application,
        // not of other Escher records, but those are returned by the above anyway
        if (recordTypes == EscherRecordTypes.UNKNOWN && IS_CONTAINER.isAllSet(options)) {
            return EscherContainerRecord::new;
        }

        if (recordTypes.constructor != null && recordTypes != EscherRecordTypes.UNKNOWN) {
            return recordTypes.constructor;
        }

        // handle unknown blip records
        if (EscherBlipRecord.RECORD_ID_START <= recordId && recordId <= EscherBlipRecord.RECORD_ID_END) {
            return EscherBlipRecord::new;
        }

        // catch all
        return UnknownEscherRecord::new;
    }
}
