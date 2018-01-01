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

package org.apache.poi.hmef.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.POIDataSamples;
import org.apache.poi.util.TempFile;
import org.junit.Test;

public class TestHMEFContentsExtractor {
    @Test
    public void TestMain() throws IOException {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        File message = samples.getFile("quick-winmail.dat");
        File outputDirectory = TempFile.createTempDirectory("quick-winmail-main");
        String[] args = new String[] { message.getAbsolutePath(), outputDirectory.getAbsolutePath() };
        HMEFContentsExtractor.main(args);

        String[] contents = new String[] {
                "message.rtf", // from extractMessageBody
                "quick.txt", "quick.pdf", "quick.xml", "quick.doc", "quick.html" // from extractAttachments
        };
        
        for (String filename : contents) {
            File f = new File(outputDirectory, filename);
            assertTrue(f + " does not exist", f.exists());
            assertTrue(f.delete());
        }

        String[] list = outputDirectory.list();
        assertNotNull(list);
        assertEquals( "Had: " + Arrays.toString(list), 0, list.length);
        assertTrue(outputDirectory.delete());
    }

    @Test
    public void TestExtractMessageBody_OutputStream() throws IOException {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        File winmailTNEFFile = samples.getFile("quick-winmail.dat");
        HMEFContentsExtractor extractor = new HMEFContentsExtractor(winmailTNEFFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        extractor.extractMessageBody(out);
        assertTrue(out.size() > 0);
        byte[] expectedMagic = new byte[] { '{', '\\', 'r', 't', 'f' };
        byte[] magic = Arrays.copyOf(out.toByteArray(), 5);
        assertArrayEquals("RTF magic number", expectedMagic, magic);
        out.close();
    }

    @Test
    public void TestExtractMessageBody_File() throws IOException {
        POIDataSamples samples = POIDataSamples.getHMEFInstance();
        File winmailTNEFFile = samples.getFile("quick-winmail.dat");
        HMEFContentsExtractor extractor = new HMEFContentsExtractor(winmailTNEFFile);
        File rtf = TempFile.createTempFile("quick-winmail-message-body", ".rtf");
        assertTrue(rtf.delete());
        extractor.extractMessageBody(rtf);
        assertTrue("RTF message body is empty", rtf.length() > 0);
    }
}
