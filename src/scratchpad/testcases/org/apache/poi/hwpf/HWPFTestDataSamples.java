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
package org.apache.poi.hwpf;

import org.apache.poi.POIDataSamples;

import java.io.*;

public class HWPFTestDataSamples {

    public static HWPFDocument openSampleFile(String sampleFileName) {
        try {
            InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream(sampleFileName);
            return new HWPFDocument(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Writes a spreadsheet to a <tt>ByteArrayOutputStream</tt> and reads it back
     * from a <tt>ByteArrayInputStream</tt>.<p/>
     * Useful for verifying that the serialisation round trip
     */
    public static HWPFDocument writeOutAndReadBack(HWPFDocument original) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            original.write(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new HWPFDocument(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
