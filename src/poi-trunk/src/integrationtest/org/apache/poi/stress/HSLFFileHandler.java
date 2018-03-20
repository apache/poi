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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.*;

import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SystemOutLogger;
import org.junit.Test;

public class HSLFFileHandler extends SlideShowHandler {
    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        HSLFSlideShowImpl slide = new HSLFSlideShowImpl(stream);
        assertNotNull(slide.getCurrentUserAtom());
        assertNotNull(slide.getEmbeddedObjects());
        assertNotNull(slide.getUnderlyingBytes());
        assertNotNull(slide.getPictureData());
        Record[] records = slide.getRecords();
        assertNotNull(records);
        for(Record record : records) {
            assertNotNull("Found a record which was null", record);
            assertTrue(record.getRecordType() >= 0);
        }
        
        handlePOIDocument(slide);
        
        HSLFSlideShow ss = new HSLFSlideShow(slide);
        handleSlideShow(ss);
    }
    
    @Test
    public void testOne() throws Exception {
        testOneFile(new File("test-data/slideshow/54880_chinese.ppt"));
    }

    // a test-case to test all .ppt files without executing the full TestAllFiles
    @Override
    @Test
    public void test() throws Exception {
        File[] files = new File("test-data/slideshow/").listFiles((dir, name) -> name.endsWith(".ppt"));
        assertNotNull(files);

        System.out.println("Testing " + files.length + " files");

        POILogger logger = new SystemOutLogger();
        for(File file : files) {
            try {
                testOneFile(file);
            } catch (Throwable e) {
                logger.log(POILogger.WARN, "Failed to handle file " + file, e);
            }
        }
    }

    private void testOneFile(File file) throws Exception {
        System.out.println(file);

        //System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SystemOutLogger");
        try (InputStream stream = new FileInputStream(file)) {
            handleFile(stream, file.getPath());
        }

        handleExtracting(file);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SystemOutLogger");
        try (InputStream stream = new FileInputStream(args[0])) {
            new HSLFFileHandler().handleFile(stream, args[0]);
        }
    }
}
