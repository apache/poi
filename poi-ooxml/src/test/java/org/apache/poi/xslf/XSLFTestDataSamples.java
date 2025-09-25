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
package org.apache.poi.xslf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

public class XSLFTestDataSamples {

    public static XMLSlideShow openSampleDocument(String sampleName) {
        try (InputStream is = POIDataSamples.getSlideShowInstance().openResourceAsStream(sampleName)) {
            return new XMLSlideShow(OPCPackage.open(is));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static XMLSlideShow openSampleDocumentReadOnly(String sampleName) throws InvalidFormatException {
        File file = POIDataSamples.getSlideShowInstance().getFile(sampleName);
        return new XMLSlideShow(OPCPackage.open(file, PackageAccess.READ));
    }

    public static XMLSlideShow writeOutAndReadBack(XMLSlideShow doc) throws IOException {
        try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().setBufferSize(4096).get()) {
            doc.write(baos);
            try (InputStream bais = baos.toInputStream()) {
                return new XMLSlideShow(bais);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
