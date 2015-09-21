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

package org.apache.poi.sl.usermodel;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

public class BaseTestSlideShowFactory {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    public void testFactory(String file, String protectedFile, String password)
    throws Exception {
        SlideShow<?,?> ss;
        // from file
        ss = SlideShowFactory.create(fromFile(file));
        assertNotNull(ss);
        // from stream
        ss = SlideShowFactory.create(fromStream(file));
        assertNotNull(ss);
        // from NPOIFS
        if (!file.contains("pptx")) {
            NPOIFSFileSystem npoifs = new NPOIFSFileSystem(fromFile(file));
            ss = SlideShowFactory.create(npoifs);
            assertNotNull(ss);
            npoifs.close();
        }
        // from protected file
        ss = SlideShowFactory.create(fromFile(protectedFile), password);
        assertNotNull(ss);
        // from protected stream
        ss = SlideShowFactory.create(fromStream(protectedFile), password);
        assertNotNull(ss);
        // from protected NPOIFS
        NPOIFSFileSystem npoifs = new NPOIFSFileSystem(fromFile(protectedFile));
        ss = SlideShowFactory.create(npoifs, password);
        assertNotNull(ss);
        npoifs.close();
    }
    
    private static File fromFile(String file) {
        return (file.contains("/") || file.contains("\\"))
            ? new File(file)
            : _slTests.getFile(file);
    }

    private static InputStream fromStream(String file) throws IOException {
        return (file.contains("/") || file.contains("\\"))
            ? new FileInputStream(file)
            : _slTests.openResourceAsStream(file);
    }
}
