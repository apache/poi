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

package org.apache.poi.hslf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;

public class HSLFTestDataSamples {

    private static final POIDataSamples _inst = POIDataSamples.getSlideShowInstance();

    public static InputStream openSampleFileStream(String sampleFileName) {
        return _inst.openResourceAsStream(sampleFileName);
    }

    public static File getSampleFile(String sampleFileName) {
       return _inst.getFile(sampleFileName);
    }

    public static byte[] getTestDataFileContent(String fileName) {
        return _inst.readFile(fileName);
    }

    public static HSLFSlideShow getSlideShow(String fileName) throws IOException {
        try (InputStream is = openSampleFileStream(fileName)) {
            return new HSLFSlideShow(is);
        }
    }

    /**
     * Writes a slideshow to a {@code UnsynchronizedByteArrayOutputStream} and reads it back
     * from a {@code ByteArrayInputStream}.<p>
     * Useful for verifying that the serialisation round trip
     */
    public static HSLFSlideShowImpl writeOutAndReadBack(HSLFSlideShowImpl original) {
        try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            original.write(baos);
            try (InputStream is = baos.toInputStream()) {
                return new HSLFSlideShowImpl(is);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a slideshow to a {@code UnsynchronizedByteArrayOutputStream} and reads it back
     * from a {@code ByteArrayInputStream}.<p>
     * Useful for verifying that the serialisation round trip
     */
    public static HSLFSlideShow writeOutAndReadBack(HSLFSlideShow original) {
        try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().setBufferSize(4096).get()) {
            original.write(baos);
            return new HSLFSlideShow(baos.toInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
