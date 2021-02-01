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

package org.apache.poi.hemf.record.emfplus;


import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hemf.draw.HemfGraphics;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInputStream;

@Internal
public interface HemfPlusRecord extends GenericRecord {

    HemfPlusRecordType getEmfPlusRecordType();

    int getFlags();

    /**
     * Init record from stream
     *
     * @param leis the little endian input stream
     * @param dataSize the size limit for this record
     * @param recordId the id of the {@link HemfPlusRecordType}
     * @param flags the record flags
     *
     * @return count of processed bytes
     *
     * @throws IOException when the inputstream is malformed
     */
    long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException;



    /**
     * Draws the record, the default redirects to the parent WMF record drawing
     * @param ctx the drawing context
     */
    default void draw(HemfGraphics ctx) {
    }

    default void calcBounds(Rectangle2D window, Rectangle2D viewport, HemfGraphics.EmfRenderState[] renderState) {
    }

    @Override
    default HemfPlusRecordType getGenericRecordType() {
        return getEmfPlusRecordType();
    }
}
