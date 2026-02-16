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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hemf.record.emf.HemfRecordType;
import org.apache.poi.hemf.record.emf.HemfText;
import org.apache.poi.hemf.usermodel.HemfPicture;
import org.junit.jupiter.api.Test;

public class HEMFFileHandler implements FileHandler {

    @Override
    public void handleExtracting(File file) throws Exception {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            HemfPicture picture = new HemfPicture(stream);

            // mimic a bit what e.g. Tika does to extract some information from .emf files
            for (HemfRecord record : picture.getRecords()) {
                if (record.getEmfRecordType().equals(HemfRecordType.extTextOutW)) {
                    assertInstanceOf(HemfText.EmfExtTextOutW.class, record);
                    HemfText.EmfExtTextOutW textOut = (HemfText.EmfExtTextOutW) record;
                    textOut.getText(StandardCharsets.UTF_16LE);
                } else if (record.getEmfRecordType().equals(HemfRecordType.extTextOutA)) {
                    assertInstanceOf(HemfText.EmfExtTextOutA.class, record);
                    HemfText.EmfExtTextOutA textOut = (HemfText.EmfExtTextOutA) record;
                    textOut.getText(StandardCharsets.UTF_8);
                }
            }
        }
    }

    @Override
    public void handleAdditional(File file) throws Exception {
        // no additional checks for now
    }

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        HemfPicture picture = new HemfPicture(stream);

        for (HemfRecord record : picture.getRecords()) {
            record.getEmfRecordType();
            record.getGenericRecordType();
        }

        BufferedImage dest = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        picture.draw(dest.createGraphics(), new Rectangle2D.Double(0, 0, 256, 256));
    }

    @Test
    void test() throws Exception {
        String file = "test-data/slideshow/wrench.emf";

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            handleFile(stream, file);
        }

        handleExtracting(new File(file));
    }
}
