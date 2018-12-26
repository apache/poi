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

import org.apache.poi.EmptyFileException;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

public class TestSlideShowDumper extends BasePPTIteratingTest {
    private static final Set<String> FAILING = new HashSet<>();
    static {
        FAILING.add("cryptoapi-proc2356.ppt");
        FAILING.add("41384.ppt");
        FAILING.add("bug56240.ppt");
    }

    @Test
    public void testMain() throws IOException {
        SlideShowDumper.main(new String[0]);

        // SlideShowDumper calls IOUtils.toByteArray(is), which would fail if a different size is defined
        IOUtils.setByteArrayMaxOverride(-1);

        SlideShowDumper.main(new String[] {
                HSLFTestDataSamples.getSampleFile("slide_master.ppt").getAbsolutePath(),
                HSLFTestDataSamples.getSampleFile("pictures.ppt").getAbsolutePath()
        });

        try {
            SlideShowDumper.main(new String[]{"invalidfile"});
            fail("Should catch exception here");
        } catch (EmptyFileException e) {
            // expected here
        }
    }

    @Override
    void runOneFile(File pFile) throws Exception {
        try {
            // SlideShowDumper calls IOUtils.toByteArray(is), which would fail if a different size is defined
            IOUtils.setByteArrayMaxOverride(-1);

            SlideShowDumper.main(new String[]{pFile.getAbsolutePath()});
        } catch (ArrayIndexOutOfBoundsException e) {
            // some corrupted documents currently can cause this excpetion
            if (!FAILING.contains(pFile.getName()) && !ENCRYPTED_FILES.contains(pFile.getName())) {
                throw e;
            }
        } catch (FileNotFoundException e) {
            // some old files are not detected correctly
            if(!OLD_FILES.contains(pFile.getName())) {
                throw e;
            }
        }
    }
}