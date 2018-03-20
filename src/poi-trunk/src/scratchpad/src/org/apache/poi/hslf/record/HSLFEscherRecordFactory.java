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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.poi.ddf.*;
import org.apache.poi.util.LittleEndian;

/**
 * Generates escher records when provided the byte array containing those records.
 *
 * @see EscherRecordFactory
 */
public class HSLFEscherRecordFactory extends DefaultEscherRecordFactory {
    private static Class<?>[] escherRecordClasses = { EscherPlaceholder.class, HSLFEscherClientDataRecord.class };
    private static Map<Short, Constructor<? extends EscherRecord>> recordsMap = recordsToMap( escherRecordClasses );

    
    /**
     * Creates an instance of the escher record factory
     */
    public HSLFEscherRecordFactory() {
        // no instance initialisation
    }
    
    @Override
    public EscherRecord createRecord(byte[] data, int offset) {
        short options = LittleEndian.getShort( data, offset );
        short recordId = LittleEndian.getShort( data, offset + 2 );
        // int remainingBytes = LittleEndian.getInt( data, offset + 4 );

        Constructor<? extends EscherRecord> recordConstructor = recordsMap.get(Short.valueOf(recordId));
        if (recordConstructor == null) {
            return super.createRecord(data, offset);
        }
        EscherRecord escherRecord = null;
        try {
            escherRecord = recordConstructor.newInstance(new Object[] {});
        } catch (Exception e) {
            return super.createRecord(data, offset);
        }
        escherRecord.setRecordId(recordId);
        escherRecord.setOptions(options);
        if (escherRecord instanceof EscherContainerRecord) {
            escherRecord.fillFields(data, offset, this);
        }
        
        return escherRecord;
    }
}
