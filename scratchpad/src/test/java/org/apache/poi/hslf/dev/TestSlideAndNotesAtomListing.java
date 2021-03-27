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
package org.apache.poi.hslf.dev;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EmptyFileException;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.junit.jupiter.api.Test;

public class TestSlideAndNotesAtomListing extends BaseTestPPTIterating {
    @Test
    void testMain() throws IOException {
        // calls System.exit(): SlideAndNotesAtomListing.main(new String[0]);
        SlideAndNotesAtomListing.main(new String[] {
                HSLFTestDataSamples.getSampleFile("slide_master.ppt").getAbsolutePath()
        });
        assertThrows(EmptyFileException.class, () -> SlideAndNotesAtomListing.main(new String[]{"invalidfile"}));
    }

    @Override
    void runOneFile(File pFile) throws Exception {
        SlideAndNotesAtomListing.main(new String[]{pFile.getAbsolutePath()});
    }
}