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
package org.apache.poi.xwpf;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.POIDataSamples;

import java.io.*;

/**
 * @author Yegor Kozlov
 */
public class XWPFTestDataSamples {

    public static XWPFDocument openSampleDocument(String sampleName) {
        InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream(sampleName);
        try {
            return new XWPFDocument(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static XWPFDocument writeOutAndReadBack(XWPFDocument doc) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            doc.write(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new XWPFDocument(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
