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

package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.util.LittleEndianInputStream;

public interface HwmfRecord extends GenericRecord {
    HwmfRecordType getWmfRecordType();

    /**
     * Init record from stream
     *
     * @param leis the little endian input stream
     * @return count of processed bytes
     * @throws IOException
     */
    int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException;
    
    /**
     * Apply the record settings to the graphics context
     *
     * @param ctx the graphics context to modify
     */
    void draw(HwmfGraphics ctx);

    @Override
    default Enum getGenericRecordType() {
        return getWmfRecordType();
    }
}
