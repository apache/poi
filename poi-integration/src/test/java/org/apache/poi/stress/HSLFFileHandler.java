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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.junit.jupiter.api.Test;

public class HSLFFileHandler extends SlideShowHandler {

    private static final Logger LOGGER = PoiLogManager.getLogger(HSLFFileHandler.class);

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        HSLFSlideShowImpl slide = new HSLFSlideShowImpl(stream);
        assertNotNull(slide.getCurrentUserAtom());
        assertNotNull(slide.getEmbeddedObjects());
        assertNotNull(slide.getUnderlyingBytes());
        assertNotNull(slide.getPictureData());
        org.apache.poi.hslf.record.Record[] records = slide.getRecords();
        assertNotNull(records);
        for(org.apache.poi.hslf.record.Record record : records) {
            assertNotNull( record, "Found a record which was null" );
            assertTrue(record.getRecordType() >= 0);
        }

        handlePOIDocument(slide);

        HSLFSlideShow ss = new HSLFSlideShow(slide);
        handleSlideShow(ss);
    }

    @Test
    void testOne() throws Exception {
        testOneFile(new File("test-data/slideshow/54880_chinese.ppt"));
    }

    // a test-case to test all .ppt files without executing the full TestAllFiles
    @Override
    @Test
    void test() throws Exception {
        File[] files = new File("test-data/slideshow/").listFiles((dir, name) -> name.endsWith(".ppt"));
        assertNotNull(files);

        System.out.println("Testing " + files.length + " files");

        for(File file : files) {
            try {
                testOneFile(file);
            } catch (Throwable e) {
                LOGGER.atWarn().withThrowable(e).log("Failed to handle file {}", file);
            }
        }
    }

    private void testOneFile(File file) throws Exception {
        System.out.println(file);

        try (InputStream stream = new FileInputStream(file)) {
            handleFile(stream, file.getPath());
        }

        handleExtracting(file);

        handleAdditional(file);
    }

    public static void main(String[] args) throws Exception {
        try (InputStream stream = new FileInputStream(args[0])) {
            new HSLFFileHandler().handleFile(stream, args[0]);
        }
    }
}
