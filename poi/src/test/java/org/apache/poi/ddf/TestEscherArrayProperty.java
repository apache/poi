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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

class TestEscherArrayProperty {

    /**
     * {@code OfficeArtArrayPropertyData.nElems} is an unsigned 16-bit integer
     * ([MS-ODRAW] 2.3.7.2). {@link EscherArrayProperty#setArrayData} must read it
     * with {@code getUShort}, consistently with the
     * {@link EscherArrayProperty#getNumberOfElementsInArray()} and
     * {@link EscherArrayProperty#getNumberOfElementsInMemory()} accessors (both
     * {@code getUShort}). Reading it as a signed short makes the
     * {@code arraySize == complexSize} "size excludes header" detection fail for
     * arrays with 32768..65535 elements, leaving {@code sizeIncludesHeaderSize}
     * wrong and mis-sizing the property when it is serialized again.
     */
    @Test
    void setArrayDataReadsElementCountAsUnsigned() {
        final int nElems = 0x8000;              // 32768 -> negative when read as signed short
        final short cbElem = 2;                 // bytes per element
        final int arraySize = nElems * cbElem;  // 65536

        // "size excludes header" layout: the simple-part complexSize equals arraySize
        EscherArrayProperty prop = new EscherArrayProperty((short) 0x0145, arraySize);

        byte[] data = new byte[6];
        LittleEndian.putUShort(data, 0, nElems);   // nElems
        LittleEndian.putUShort(data, 2, nElems);   // nElemsInMemory
        LittleEndian.putShort(data, 4, cbElem);    // cbElem

        prop.setArrayData(data, 0);

        // With nElems read as unsigned, arraySize == complexSize is detected and the
        // 6-byte array header is added back; read as signed, that detection fails.
        assertEquals(arraySize + 6, prop.getComplexSize());
    }
}
