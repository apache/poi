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

package org.apache.poi.hpbf.model.qcbits;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;

/**
 * A Text based bit of Quill Contents
 */
public final class QCTextBit extends QCBit {

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for QCTextBit
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for QCTextBit
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public QCTextBit(String thingType, String bitType, byte[] data) {
        super(thingType, bitType, data);
    }

    /**
     * Returns the text. Note that line endings
     *  are \r and not \n
     */
    public String getText() {
        return StringUtil.getFromUnicodeLE(getData());
    }

    public void setText(String text) {
        byte[] data = IOUtils.safelyAllocate(text.length() * 2L, MAX_RECORD_LENGTH);
        StringUtil.putUnicodeLE(text, data, 0);
        setData(data);
    }
}
