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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates escher records when provided the byte array containing those records.
 *
 * @author Glen Stampoultzis
 * @author Nick Burch   (nick at torchbox . com)
 *
 * @see EscherRecordFactory
 */
public class DefaultEscherRecordFactory implements EscherRecordFactory {
    private static Class<?>[] escherRecordClasses = {
        EscherBSERecord.class, EscherOptRecord.class, EscherClientAnchorRecord.class, EscherDgRecord.class,
        EscherSpgrRecord.class, EscherSpRecord.class, EscherClientDataRecord.class, EscherDggRecord.class,
        EscherSplitMenuColorsRecord.class, EscherChildAnchorRecord.class, EscherTextboxRecord.class
    };
    private static Map<Short, Constructor<? extends EscherRecord>> recordsMap = recordsToMap( escherRecordClasses );

    /**
     * Creates an instance of the escher record factory
     */
    public DefaultEscherRecordFactory() {
        // no instance initialisation
    }

    /**
     * Generates an escher record including the any children contained under that record.
     * An exception is thrown if the record could not be generated.
     *
     * @param data   The byte array containing the records
     * @param offset The starting offset into the byte array
     * @return The generated escher record
     */
    public EscherRecord createRecord(byte[] data, int offset) {
        EscherRecord.EscherRecordHeader header = EscherRecord.EscherRecordHeader.readHeader( data, offset );

        // Options of 0x000F means container record
        // However, EscherTextboxRecord are containers of records for the
        //  host application, not of other Escher records, so treat them
        //  differently
        if ( ( header.getOptions() & (short) 0x000F ) == (short) 0x000F
             && header.getRecordId() != EscherTextboxRecord.RECORD_ID ) {
            EscherContainerRecord r = new EscherContainerRecord();
            r.setRecordId( header.getRecordId() );
            r.setOptions( header.getOptions() );
            return r;
        }

        if (header.getRecordId() >= EscherBlipRecord.RECORD_ID_START
                && header.getRecordId() <= EscherBlipRecord.RECORD_ID_END) {
            EscherBlipRecord r;
            if (header.getRecordId() == EscherBitmapBlip.RECORD_ID_DIB ||
                    header.getRecordId() == EscherBitmapBlip.RECORD_ID_JPEG ||
                    header.getRecordId() == EscherBitmapBlip.RECORD_ID_PNG)
            {
                r = new EscherBitmapBlip();
            }
            else if (header.getRecordId() == EscherMetafileBlip.RECORD_ID_EMF ||
                    header.getRecordId() == EscherMetafileBlip.RECORD_ID_WMF ||
                    header.getRecordId() == EscherMetafileBlip.RECORD_ID_PICT)
            {
                r = new EscherMetafileBlip();
            } else {
                r = new EscherBlipRecord();
            }
            r.setRecordId( header.getRecordId() );
            r.setOptions( header.getOptions() );
            return r;
        }

        Constructor<? extends EscherRecord> recordConstructor = recordsMap.get(Short.valueOf(header.getRecordId()));
        EscherRecord escherRecord = null;
        if (recordConstructor == null) {
            return new UnknownEscherRecord();
        }
        try {
            escherRecord = recordConstructor.newInstance(new Object[] {});
        } catch (Exception e) {
            return new UnknownEscherRecord();
        }
        escherRecord.setRecordId(header.getRecordId());
        escherRecord.setOptions(header.getOptions());
        return escherRecord;
    }

    /**
     * Converts from a list of classes into a map that contains the record id as the key and
     * the Constructor in the value part of the map.  It does this by using reflection to look up
     * the RECORD_ID field then using reflection again to find a reference to the constructor.
     *
     * @param recClasses The records to convert
     * @return The map containing the id/constructor pairs.
     */
    private static Map<Short, Constructor<? extends EscherRecord>> recordsToMap(Class<?>[] recClasses) {
        Map<Short, Constructor<? extends EscherRecord>> result = new HashMap<Short, Constructor<? extends EscherRecord>>();
        final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

        for (int i = 0; i < recClasses.length; i++) {
            @SuppressWarnings("unchecked")
            Class<? extends EscherRecord> recCls = (Class<? extends EscherRecord>) recClasses[i];
            short sid;
            try {
                sid = recCls.getField("RECORD_ID").getShort(null);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            Constructor<? extends EscherRecord> constructor;
            try {
                constructor = recCls.getConstructor( EMPTY_CLASS_ARRAY );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            result.put(Short.valueOf(sid), constructor);
        }
        return result;
    }
}
