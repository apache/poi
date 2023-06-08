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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class HWPFTestDataSamples {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();

    public static HWPFDocument openSampleFile(String sampleFileName) {
        try (InputStream is = SAMPLES.openResourceAsStream(sampleFileName)) {
            return new HWPFDocument(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HWPFOldDocument openOldSampleFile(String sampleFileName) {
       try {
           InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream(sampleFileName);
           return new HWPFOldDocument(new POIFSFileSystem(is));
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }
    /**
     * Writes a spreadsheet to a {@code ByteArrayOutputStream} and reads it back
     * from a {@code ByteArrayInputStream}.<p>
     * Useful for verifying that the serialisation round trip
     */
    public static HWPFDocument writeOutAndReadBack(HWPFDocument original) {
        try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().setBufferSize(4096).get()) {
            original.write(baos);
            return new HWPFDocument(baos.toInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
