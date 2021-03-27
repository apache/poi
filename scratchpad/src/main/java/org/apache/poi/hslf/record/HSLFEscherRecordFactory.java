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

import java.util.function.Supplier;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;

/**
 * Generates escher records when provided the byte array containing those records.
 *
 * @see EscherRecordFactory
 */
public class HSLFEscherRecordFactory extends DefaultEscherRecordFactory {
    /**
     * Creates an instance of the escher record factory
     */
    public HSLFEscherRecordFactory() {
        // no instance initialisation
    }

    @Override
    protected Supplier<? extends EscherRecord> getConstructor(short options, short recordId) {
        if (recordId == EscherPlaceholder.RECORD_ID) {
            return EscherPlaceholder::new;
        } else if (recordId == EscherClientDataRecord.RECORD_ID) {
            return HSLFEscherClientDataRecord::new;
        }
        return super.getConstructor(options, recordId);
    }
}
